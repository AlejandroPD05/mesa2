package com.royaltown.mesa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.http.Cookie;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

class AuthApiTest extends BasePruebas {

    @Test
    @DisplayName("login correcto devuelve el perfil y la cookie httpOnly")
    void loginCorrecto() throws Exception {
        MvcResult resultado = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", "marta.sanchez@royaltown.local", "password", PASSWORD))))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode cuerpo = leer(resultado);
        assertThat(cuerpo.get("email").asText()).isEqualTo("marta.sanchez@royaltown.local");
        assertThat(cuerpo.get("rol").asText()).isEqualTo("empleado");
        // El hash de la contraseña nunca sale de la API.
        assertThat(cuerpo.has("passwordHash")).isFalse();
        assertThat(resultado.getResponse().getCookie("mesa2_sesion")).isNotNull();
    }

    @Test
    @DisplayName("login con contraseña incorrecta devuelve 401 en formato problem+json")
    void loginConPasswordIncorrecta() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", "marta.sanchez@royaltown.local", "password", "incorrecta"))))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("Content-Type", containsString("problem+json")));
    }

    @Test
    @DisplayName("/me sin sesión devuelve 401")
    void meSinSesion() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("/me con sesión devuelve el perfil de la usuaria")
    void meConSesion() throws Exception {
        Cookie sesion = comoEmpleado();
        MvcResult resultado = mockMvc.perform(get("/api/auth/me").cookie(sesion))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(leer(resultado).get("nombre").asText()).isEqualTo("Marta Sanchez Lopez");
    }

    @Test
    @DisplayName("el registro crea una cuenta de empleado y ya deja la sesión puesta")
    void registroCreaCuenta() throws Exception {
        String correo = correoUnico();
        MvcResult resultado = mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "nombre", "Persona De Prueba",
                                "email", correo,
                                "password", "unaClaveSegura123"))))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode cuerpo = leer(resultado);
        assertThat(cuerpo.get("email").asText()).isEqualTo(correo);
        assertThat(cuerpo.get("rol").asText()).isEqualTo("empleado");
        assertThat(resultado.getResponse().getCookie("mesa2_sesion")).isNotNull();
    }

    @Test
    @DisplayName("no se puede registrar dos veces el mismo correo")
    void registroConEmailDuplicado() throws Exception {
        MvcResult resultado = mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "nombre", "Copia De Marta",
                                "email", "marta.sanchez@royaltown.local",
                                "password", "unaClaveSegura123"))))
                .andExpect(status().isBadRequest())
                .andReturn();
        assertThat(leer(resultado).get("campo").asText()).isEqualTo("email");
    }

    @Test
    @DisplayName("la validación del registro explica los campos con error")
    void registroConPasswordCorta() throws Exception {
        MvcResult resultado = mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "nombre", "Clave Corta",
                                "email", correoUnico(),
                                "password", "corta"))))
                .andExpect(status().isBadRequest())
                .andReturn();
        JsonNode errores = leer(resultado).get("errores");
        assertThat(errores.has("password")).isTrue();
    }

    @Test
    @DisplayName("el logout caduca la cookie de sesión")
    void logoutCaducaLaCookie() throws Exception {
        Cookie sesion = comoEmpleado();
        mockMvc.perform(post("/api/auth/logout").cookie(sesion))
                .andExpect(status().isNoContent())
                .andExpect(header().string("Set-Cookie", containsString("Max-Age=0")));
    }
}
