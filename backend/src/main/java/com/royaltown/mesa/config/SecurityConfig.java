package com.royaltown.mesa.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties(AjustesMesa.class)
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain cadena(HttpSecurity http, JwtAuthFilter jwtFilter,
            LimiteLoginFilter limiteLoginFilter, ObjectMapper om) throws Exception {
        http
                // CSRF desactivado a propósito: la sesión va en cookie httpOnly con
                // SameSite=Lax y la SPA se sirve desde el mismo origen (en local, el
                // proxy de Vite). Lax no viaja en peticiones mutantes cross-site, que
                // es exactamente lo que protege un token CSRF. Razonado en el README.
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/registro").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/kb/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/kb/articulos/*/feedback").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/api/**").authenticated()
                        // SPA, Swagger y estáticos: públicos; el frontend redirige al login.
                        .anyRequest().permitAll())
                .exceptionHandling(excepciones -> excepciones
                        .authenticationEntryPoint((peticion, respuesta, excepcion) ->
                                problema(respuesta, om, 401, "No autenticado",
                                        "Inicia sesión para continuar"))
                        .accessDeniedHandler((peticion, respuesta, excepcion) ->
                                problema(respuesta, om, 403, "Sin permiso",
                                        "Tu perfil no tiene acceso a esta operación")))
                // El JWT se registra primero en la cadena; el límite de intentos
                // queda justo delante de él para cortar la fuerza bruta contra el
                // login sin llegar a la autenticación.
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(limiteLoginFilter, JwtAuthFilter.class)
                .logout(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable);
        return http.build();
    }

    /** Errores de la propia cadena de seguridad, en el mismo formato RFC 7807. */
    private static void problema(HttpServletResponse respuesta, ObjectMapper om, int estado,
            String titulo, String detalle) throws IOException {
        respuesta.setStatus(estado);
        respuesta.setContentType("application/problem+json");
        respuesta.setCharacterEncoding(StandardCharsets.UTF_8.name());
        om.writeValue(respuesta.getOutputStream(), Map.of(
                "type", "about:blank",
                "title", titulo,
                "status", estado,
                "detail", detalle));
    }
}
