package com.royaltown.mesa.config;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongSupplier;

/**
 * Contador de intentos fallidos por clave (la IP del cliente) con ventana fija
 * y estado en memoria. Sin dependencias nuevas: el portal corre en una sola
 * instancia y el objetivo es frenar la fuerza bruta contra el login, no
 * sustituir a un WAF ni a un limitador distribuido.
 *
 * <p>Un límite {@code intentos <= 0} desactiva el contador (perfil de tests).</p>
 */
public final class LimitadorIntentos {

    /** Tope defensivo para que el mapa no crezca sin fin con IPs falsas. */
    private static final int MAX_CLAVES = 10_000;

    private final int intentos;
    private final long ventanaMilis;
    private final LongSupplier reloj;
    private final Map<String, Ventana> ventanas = new ConcurrentHashMap<>();

    public LimitadorIntentos(int intentos, Duration ventana) {
        this(intentos, ventana, System::currentTimeMillis);
    }

    /** Constructor para tests: permite inyectar un reloj controlado. */
    LimitadorIntentos(int intentos, Duration ventana, LongSupplier reloj) {
        this.intentos = intentos;
        this.ventanaMilis = ventana == null ? 0 : ventana.toMillis();
        this.reloj = reloj;
    }

    public boolean activo() {
        return intentos > 0 && ventanaMilis > 0;
    }

    /** ¿Queda margen para esta clave? No cuenta el intento; eso se hace al fallar. */
    public boolean permitir(String clave) {
        if (!activo()) {
            return true;
        }
        Ventana ventana = ventanas.get(clave);
        if (ventana == null) {
            return true;
        }
        if (expirada(ventana, reloj.getAsLong())) {
            ventanas.remove(clave);
            return true;
        }
        return ventana.cuenta() < intentos;
    }

    /** Suma un intento fallido a la clave. */
    public void registrarFallo(String clave) {
        if (!activo()) {
            return;
        }
        depurar();
        ventanas.compute(clave, (ignorada, actual) -> {
            long ahora = reloj.getAsLong();
            if (actual == null || expirada(actual, ahora)) {
                return new Ventana(ahora, 1);
            }
            return new Ventana(actual.inicio(), actual.cuenta() + 1);
        });
    }

    private boolean expirada(Ventana ventana, long ahora) {
        return ahora - ventana.inicio() >= ventanaMilis;
    }

    /** De vez en cuando quita ventanas caducadas para acotar la memoria. */
    private void depurar() {
        if (ventanas.size() < MAX_CLAVES) {
            return;
        }
        long ahora = reloj.getAsLong();
        ventanas.values().removeIf(ventana -> expirada(ventana, ahora));
        if (ventanas.size() >= MAX_CLAVES) {
            // Peor caso: empezar de cero antes que crecer sin límite.
            ventanas.clear();
        }
    }

    private record Ventana(long inicio, int cuenta) {
    }
}
