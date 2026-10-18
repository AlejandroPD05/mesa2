package com.royaltown.mesa.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LimitadorIntentosTest {

    /** Reloj controlado: las pruebas no duermen. */
    private final AtomicLong reloj = new AtomicLong(1_000_000);

    private LimitadorIntentos crear(int intentos, Duration ventana) {
        return new LimitadorIntentos(intentos, ventana, reloj::get);
    }

    @Test
    @DisplayName("permite hasta el límite de fallos y bloquea el siguiente")
    void bloqueaAlSuperarElLimite() {
        LimitadorIntentos limitador = crear(3, Duration.ofMinutes(15));
        for (int intento = 1; intento <= 3; intento++) {
            assertThat(limitador.permitir("10.0.0.1"))
                    .as("intento %d debe estar permitido", intento)
                    .isTrue();
            limitador.registrarFallo("10.0.0.1");
        }
        assertThat(limitador.permitir("10.0.0.1")).isFalse();
        // Otra IP conserva su propio margen.
        assertThat(limitador.permitir("10.0.0.2")).isTrue();
    }

    @Test
    @DisplayName("al pasar la ventana, la clave vuelve a tener margen")
    void laVentanaCaduca() {
        LimitadorIntentos limitador = crear(2, Duration.ofMinutes(10));
        limitador.registrarFallo("10.0.0.1");
        limitador.registrarFallo("10.0.0.1");
        assertThat(limitador.permitir("10.0.0.1")).isFalse();

        reloj.addAndGet(Duration.ofMinutes(10).toMillis());

        assertThat(limitador.permitir("10.0.0.1")).isTrue();
    }

    @Test
    @DisplayName("un límite desactivado no bloquea nunca")
    void desactivadoNoBloquea() {
        LimitadorIntentos limitador = crear(0, Duration.ofMinutes(15));
        for (int intento = 0; intento < 100; intento++) {
            limitador.registrarFallo("10.0.0.1");
        }
        assertThat(limitador.activo()).isFalse();
        assertThat(limitador.permitir("10.0.0.1")).isTrue();
    }
}
