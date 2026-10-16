package com.royaltown.mesa.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Documentación viva de la API en /swagger-ui.html. */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI apiMesa() {
        return new OpenAPI()
                .info(new Info()
                        .title("Mesa 2.0 — API")
                        .version("1.0.0")
                        .description("""
                                Portal de soporte IT autoservicio y helpdesk web (universo RoyalTown).

                                La sesión viaja en una cookie httpOnly; para probar los endpoints
                                protegidos desde esta UI, inicia sesión primero en /api/auth/login.
                                """))
                .components(new Components().addSecuritySchemes("cookieSesion",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.COOKIE)
                                .name("mesa2_sesion")));
    }
}
