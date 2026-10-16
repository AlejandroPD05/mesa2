package com.royaltown.mesa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.http.Cookie;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Base de las pruebas de API: contexto completo con Flyway sobre H2 en modo
 * PostgreSQL y utilidades para autenticarse con los usuarios de la semilla.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class BasePruebas {

    /** Contraseña común de toda la semilla del helpdesk. */
    protected static final String PASSWORD = "royaltown2026";

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper om;

    protected Cookie iniciarSesion(String email) throws Exception {
        MvcResult resultado = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", email, "password", PASSWORD))))
                .andExpect(status().isOk())
                .andReturn();
        Cookie cookie = resultado.getResponse().getCookie("mesa2_sesion");
        assertThat(cookie).as("la cookie de sesión debe venir en la respuesta").isNotNull();
        return cookie;
    }

    protected Cookie comoEmpleado() throws Exception {
        return iniciarSesion("marta.sanchez@royaltown.local");
    }

    protected Cookie comoTecnico() throws Exception {
        return iniciarSesion("laura.gomez@royaltown.local");
    }

    protected Cookie comoTecnico2() throws Exception {
        return iniciarSesion("diego.fernandez@royaltown.local");
    }

    protected Cookie comoAdmin() throws Exception {
        return iniciarSesion("alejandro.ortiz@royaltown.local");
    }

    /** Crea un ticket con la sesión dada y devuelve su id. */
    protected long crearTicket(Cookie sesion, String titulo) throws Exception {
        MvcResult resultado = mockMvc.perform(post("/api/tickets")
                        .cookie(sesion)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "titulo", titulo,
                                "descripcion",
                                "Descripción de prueba con la longitud mínima necesaria para la validación.",
                                "idCategoria", 1,
                                "idPrioridad", 2))))
                .andExpect(status().isCreated())
                .andReturn();
        return leer(resultado).get("id").asLong();
    }

    protected String json(Object valor) throws Exception {
        return om.writeValueAsString(valor);
    }

    protected JsonNode leer(MvcResult resultado) throws Exception {
        return om.readTree(resultado.getResponse().getContentAsByteArray());
    }

    protected ObjectNode nodo() {
        return om.createObjectNode();
    }

    protected static String correoUnico() {
        return "prueba-" + UUID.randomUUID().toString().substring(0, 8) + "@royaltown.local";
    }
}
