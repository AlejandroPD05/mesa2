package com.royaltown.mesa.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Fallback de la SPA: cualquiera de las rutas del frontend devuelve index.html
 * para que React Router resuelva la pantalla (recarga directa y deep links).
 * Las rutas /api/** no se tocan aquí; los 404 de API siguen siendo JSON.
 */
@Controller
public class SpaController {

    @GetMapping({
            "/",
            "/login",
            "/registro",
            "/ayuda",
            "/ayuda/**",
            "/mis-tickets",
            "/tickets/**",
            "/bandeja",
            "/panel",
            "/admin",
            "/admin/**"
    })
    public String index() {
        return "forward:/index.html";
    }
}
