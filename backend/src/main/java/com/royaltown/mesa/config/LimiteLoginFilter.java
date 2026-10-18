package com.royaltown.mesa.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Frena la fuerza bruta contra el login y el registro: cuenta los intentos
 * fallidos (401) por IP y, al superar el límite de la configuración
 * ({@code mesa2.limite-login.*}), responde 429 en formato RFC 7807 igual que
 * el resto de errores de seguridad.
 *
 * <p>Un login correcto nunca penaliza; solo cuentan las credenciales
 * rechazadas. El estado vive en memoria: con varias instancias cada una
 * aplicaría su propio límite (aceptable para el despliegue actual).</p>
 */
@Component
public class LimiteLoginFilter extends OncePerRequestFilter {

    private static final Set<String> RUTAS = Set.of("/api/auth/login", "/api/auth/registro");

    private final LimitadorIntentos limitador;
    private final long ventanaSegundos;
    private final ObjectMapper om;

    public LimiteLoginFilter(AjustesMesa ajustes, ObjectMapper om) {
        AjustesMesa.LimiteLogin limite = ajustes.limiteLogin();
        int intentos = limite == null ? 0 : limite.intentos();
        int minutos = limite == null ? 0 : Math.max(limite.ventanaMinutos(), 0);
        Duration ventana = Duration.ofMinutes(minutos);
        this.limitador = new LimitadorIntentos(intentos, ventana);
        this.ventanaSegundos = ventana.toSeconds();
        this.om = om;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest peticion) {
        return !("POST".equals(peticion.getMethod()) && RUTAS.contains(peticion.getRequestURI()));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest peticion, HttpServletResponse respuesta,
            FilterChain cadena) throws ServletException, IOException {
        String clave = peticion.getRemoteAddr();
        if (!limitador.permitir(clave)) {
            problema(respuesta);
            return;
        }
        cadena.doFilter(peticion, respuesta);
        if (respuesta.getStatus() == HttpStatus.UNAUTHORIZED.value()) {
            limitador.registrarFallo(clave);
        }
    }

    private void problema(HttpServletResponse respuesta) throws IOException {
        respuesta.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        respuesta.setHeader("Retry-After", String.valueOf(ventanaSegundos));
        respuesta.setContentType("application/problem+json");
        respuesta.setCharacterEncoding(StandardCharsets.UTF_8.name());
        om.writeValue(respuesta.getOutputStream(), Map.of(
                "type", "about:blank",
                "title", "Demasiados intentos",
                "status", HttpStatus.TOO_MANY_REQUESTS.value(),
                "detail", "Has superado el límite de intentos. Vuelve a probar dentro de un rato."));
    }
}
