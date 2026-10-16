package com.royaltown.mesa.config;

import com.royaltown.mesa.servicio.JwtServicio;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Lee el JWT de la cookie httpOnly y deja la sesión en el SecurityContext.
 * Si no hay cookie o el token no es válido, la petición sigue como anónima y
 * será el punto de entrada de seguridad quien responda 401 si hace falta.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtServicio jwt;
    private final AjustesMesa ajustes;

    public JwtAuthFilter(JwtServicio jwt, AjustesMesa ajustes) {
        this.jwt = jwt;
        this.ajustes = ajustes;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            leerCookie(request)
                    .flatMap(jwt::leer)
                    .ifPresent(sesion -> {
                        var autoridades = List.of(new SimpleGrantedAuthority(
                                "ROLE_" + sesion.rol().toUpperCase(Locale.ROOT)));
                        var autenticacion =
                                new UsernamePasswordAuthenticationToken(sesion, null, autoridades);
                        SecurityContextHolder.getContext().setAuthentication(autenticacion);
                    });
        }
        chain.doFilter(request, response);
    }

    private Optional<String> leerCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }
        for (Cookie cookie : cookies) {
            if (ajustes.cookie().nombre().equals(cookie.getName())) {
                return Optional.ofNullable(cookie.getValue());
            }
        }
        return Optional.empty();
    }
}
