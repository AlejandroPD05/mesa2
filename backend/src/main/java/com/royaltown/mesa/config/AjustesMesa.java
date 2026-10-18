package com.royaltown.mesa.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propiedades propias de Mesa 2.0 (prefijo {@code mesa2} en application.yml).
 * Se registran con {@code @EnableConfigurationProperties(AjustesMesa.class)}
 * en la configuración de seguridad.
 */
@ConfigurationProperties(prefix = "mesa2")
public record AjustesMesa(Jwt jwt, Cookie cookie, LimiteLogin limiteLogin) {

    public record Jwt(String secreto, int horasExpiracion) {
    }

    public record Cookie(String nombre, boolean secure) {
    }

    /** Fuerza bruta en login/registro; {@code intentos <= 0} desactiva el límite. */
    public record LimiteLogin(int intentos, int ventanaMinutos) {
    }
}
