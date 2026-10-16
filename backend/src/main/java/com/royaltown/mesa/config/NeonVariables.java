package com.royaltown.mesa.config;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Traduce las variables de entorno que escribe la CLI de Neon
 * ({@code neon env pull}) a la configuración JDBC de Spring, siguiendo la
 * recomendación de Neon:
 *
 * <ul>
 *   <li>{@code DATABASE_URL} — cadena <b>pooled</b> (host con {@code -pooler}):
 *       tráfico normal de la aplicación.</li>
 *   <li>{@code DATABASE_URL_UNPOOLED} — cadena <b>directa</b>: migraciones de
 *       Flyway (el PgBouncer que hay detrás de la pooled no soporta estado de
 *       sesión, y las migraciones deben ir por la conexión directa).</li>
 * </ul>
 *
 * <p>Si el entorno define {@code SPRING_DATASOURCE_URL} de forma explícita, o
 * el arranque usa el perfil {@code local} (H2 de fichero), esta clase no toca
 * nada: la configuración clásica siempre manda.</p>
 */
public final class NeonVariables {

    static final String URL_APLICACION = "spring.datasource.url";
    static final String USUARIO_APLICACION = "spring.datasource.username";
    static final String CLAVE_APLICACION = "spring.datasource.password";
    static final String URL_FLYWAY = "spring.flyway.url";
    static final String USUARIO_FLYWAY = "spring.flyway.user";
    static final String CLAVE_FLYWAY = "spring.flyway.password";

    private NeonVariables() {
    }

    /**
     * Aplica las variables de Neon del entorno como propiedades del sistema.
     * Se llama al comienzo de {@code main}, antes de crear el contexto.
     */
    public static void aplicar(String[] argumentos) {
        Map<String, String> propiedades = mapear(System.getenv(), List.of(argumentos));
        if (propiedades.isEmpty()) {
            return;
        }
        propiedades.forEach(System::setProperty);
        System.out.println("[mesa2] Conexión a Neon detectada por DATABASE_URL."
                + " Aplicación: " + propiedades.get(URL_APLICACION)
                + " | Flyway: " + propiedades.get(URL_FLYWAY));
    }

    /**
     * Calcula las propiedades a definir. Es una función pura para poder
     * probarla sin tocar el entorno real del proceso.
     */
    static Map<String, String> mapear(Map<String, String> entorno, List<String> argumentos) {
        if (!esVacio(entorno.get("SPRING_DATASOURCE_URL"))) {
            return Map.of(); // configuración clásica explícita: manda ella
        }
        String pooled = entorno.get("DATABASE_URL");
        if (esVacio(pooled)) {
            return Map.of();
        }
        if (perfilLocalActivo(entorno, argumentos)) {
            return Map.of(); // perfil local = H2 de fichero, no lo pisamos
        }
        String jdbcAplicacion = aJdbc(pooled);
        if (jdbcAplicacion == null) {
            return Map.of();
        }
        String jdbcFlyway = aJdbc(entorno.get("DATABASE_URL_UNPOOLED"));
        if (jdbcFlyway == null) {
            jdbcFlyway = jdbcAplicacion; // sin cadena directa, se usa la pooled
        }

        Map<String, String> propiedades = new LinkedHashMap<>();
        propiedades.put(URL_APLICACION, jdbcAplicacion);
        propiedades.put(URL_FLYWAY, jdbcFlyway);
        Partes credenciales = parsear(pooled);
        if (credenciales != null && !credenciales.usuario().isBlank()) {
            propiedades.put(USUARIO_APLICACION, credenciales.usuario());
            propiedades.put(CLAVE_APLICACION, credenciales.clave());
            propiedades.put(USUARIO_FLYWAY, credenciales.usuario());
            propiedades.put(CLAVE_FLYWAY, credenciales.clave());
        }
        return propiedades;
    }

    /** Convierte {@code postgresql://…} en una URL JDBC; {@code null} si no aplica. */
    static String aJdbc(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }
        String limpia = url.trim();
        if (limpia.startsWith("jdbc:")) {
            return limpia;
        }
        Partes partes = parsear(limpia);
        if (partes == null) {
            return null;
        }
        return "jdbc:postgresql://" + partes.hostPuerto() + "/" + partes.base()
                + "?" + consultaJdbc(partes.consulta());
    }

    /**
     * Normaliza la consulta para el driver JDBC: quita {@code channel_binding}
     * (es una opción de libpq, no de PgJDBC) y garantiza {@code sslmode=require}
     * (Neon exige TLS).
     */
    static String consultaJdbc(String consulta) {
        List<String> parametros = new ArrayList<>();
        boolean tieneSsl = false;
        for (String parametro : consulta.split("&")) {
            if (parametro.isBlank()) {
                continue;
            }
            if (parametro.startsWith("channel_binding=")) {
                continue;
            }
            if (parametro.startsWith("sslmode=") || parametro.equals("ssl=true")) {
                tieneSsl = true;
            }
            parametros.add(parametro);
        }
        if (!tieneSsl) {
            parametros.add("sslmode=require");
        }
        return String.join("&", parametros);
    }

    /**
     * Descompone una cadena {@code postgresql://usuario:clave@host[:puerto]/base?…}.
     */
    static Partes parsear(String url) {
        if (url == null) {
            return null;
        }
        String limpia = url.trim();
        int corte = limpia.indexOf("://");
        if (corte < 0) {
            return null;
        }
        String esquema = limpia.substring(0, corte).toLowerCase(Locale.ROOT);
        if (!esquema.equals("postgresql") && !esquema.equals("postgres")) {
            return null;
        }
        String resto = limpia.substring(corte + 3);

        String consulta = "";
        int interrogacion = resto.indexOf('?');
        if (interrogacion >= 0) {
            consulta = resto.substring(interrogacion + 1);
            resto = resto.substring(0, interrogacion);
        }
        String base = "";
        int barra = resto.indexOf('/');
        if (barra >= 0) {
            base = resto.substring(barra + 1);
            resto = resto.substring(0, barra);
        }
        String usuario = "";
        String clave = "";
        int arroba = resto.lastIndexOf('@');
        if (arroba >= 0) {
            String credenciales = resto.substring(0, arroba);
            resto = resto.substring(arroba + 1);
            int dosPuntos = credenciales.indexOf(':');
            if (dosPuntos < 0) {
                usuario = credenciales;
            } else {
                usuario = credenciales.substring(0, dosPuntos);
                clave = credenciales.substring(dosPuntos + 1);
            }
        }
        if (resto.isBlank() || base.isBlank()) {
            return null;
        }
        return new Partes(resto, base, consulta, decodificar(usuario), decodificar(clave));
    }

    private static boolean perfilLocalActivo(Map<String, String> entorno, List<String> argumentos) {
        List<String> perfiles = new ArrayList<>();
        String prefijo = "--spring.profiles.active=";
        for (String argumento : argumentos) {
            if (argumento.startsWith(prefijo)) {
                perfiles.addAll(List.of(argumento.substring(prefijo.length()).split(",")));
            }
        }
        String deEntorno = entorno.get("SPRING_PROFILES_ACTIVE");
        if (deEntorno != null) {
            perfiles.addAll(List.of(deEntorno.split(",")));
        }
        return perfiles.stream().map(String::trim).anyMatch("local"::equals);
    }

    private static boolean esVacio(String valor) {
        return valor == null || valor.isBlank();
    }

    private static String decodificar(String valor) {
        if (valor == null || valor.indexOf('%') < 0) {
            return valor;
        }
        return URLDecoder.decode(valor, StandardCharsets.UTF_8);
    }

    /** Piezas de una cadena de conexión de Postgres. */
    record Partes(String hostPuerto, String base, String consulta, String usuario, String clave) {
    }
}
