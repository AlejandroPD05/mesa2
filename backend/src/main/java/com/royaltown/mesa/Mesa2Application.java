package com.royaltown.mesa;

import com.royaltown.mesa.config.NeonVariables;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

/**
 * Mesa 2.0 — portal de soporte IT autoservicio y helpdesk web.
 *
 * <p>Compañero web del CLI del repositorio {@code ticketing}: el mismo dominio
 * (usuarios, tickets, estados, SLA) con autenticación y una interfaz para los
 * empleados y el equipo de soporte.</p>
 */
// La autenticación es propia (JWT en cookie + SecurityFilterChain), así que la
// UserDetailsService por defecto de Spring Boot solo genera ruido en el log.
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class Mesa2Application {

    public static void main(String[] args) {
        // Si el entorno trae DATABASE_URL (Neon), se traduce aquí mismo a la
        // configuración JDBC: pooled para la app, directa para Flyway.
        NeonVariables.aplicar(args);
        SpringApplication.run(Mesa2Application.class, args);
    }
}
