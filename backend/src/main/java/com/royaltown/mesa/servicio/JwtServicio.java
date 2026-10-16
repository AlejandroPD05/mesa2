package com.royaltown.mesa.servicio;

import com.royaltown.mesa.config.AjustesMesa;
import com.royaltown.mesa.modelo.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.Optional;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtServicio {

    private final SecretKey clave;
    private final long milisExpiracion;

    public JwtServicio(AjustesMesa ajustes) {
        byte[] bytes = ajustes.jwt().secreto().getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException(
                    "mesa2.jwt.secreto debe tener al menos 32 bytes (HS256); revisa MESA2_JWT_SECRET");
        }
        this.clave = Keys.hmacShaKeyFor(bytes);
        this.milisExpiracion = Duration.ofHours(ajustes.jwt().horasExpiracion()).toMillis();
    }

    public String crear(Usuario usuario) {
        return Jwts.builder()
                .subject(String.valueOf(usuario.getId()))
                .claim("nombre", usuario.getNombreCompleto())
                .claim("email", usuario.getEmail())
                .claim("rol", usuario.getRol().getNombre())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + milisExpiracion))
                .signWith(clave)
                .compact();
    }

    /** Devuelve la sesión si el token es válido y no ha caducado. */
    public Optional<Sesion> leer(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(clave).build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Optional.of(new Sesion(
                    Integer.valueOf(claims.getSubject()),
                    claims.get("nombre", String.class),
                    claims.get("email", String.class),
                    claims.get("rol", String.class)));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
