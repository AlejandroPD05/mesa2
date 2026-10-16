package com.royaltown.mesa.servicio;

import com.royaltown.mesa.error.NoAutenticadoException;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/** Punto único para resolver la sesión en curso dentro de los servicios. */
@Service
public class SesionServicio {

    public Sesion actual() {
        return opcional().orElseThrow(() -> new NoAutenticadoException("Inicia sesión para continuar"));
    }

    public Optional<Sesion> opcional() {
        Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacion != null && autenticacion.getPrincipal() instanceof Sesion sesion) {
            return Optional.of(sesion);
        }
        return Optional.empty();
    }
}
