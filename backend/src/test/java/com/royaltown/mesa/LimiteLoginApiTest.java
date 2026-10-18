package com.royaltown.mesa;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/**
 * El límite de intentos de login necesita su propio contexto: en el perfil de
 * tests viene desactivado (application-test.yml) para no interferir con el
 * resto de pruebas, así que aquí se activa con propiedades propias.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "mesa2.limite-login.intentos=3",
        "mesa2.limite-login.ventana-minutos=15"
})
class LimiteLoginApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper om;

    @Test
    @DisplayName("tras agotar los intentos fallidos, el login responde 429 en problem+json")
    void bloqueaTrasAgotarIntentos() throws Exception {
        String credencialesMalas = om.writeValueAsString(Map.of(
                "email", "marta.sanchez@royaltown.local", "password", "incorrecta"));
        for (int intento = 0; intento < 3; intento++) {
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(credencialesMalas))
                    .andExpect(status().isUnauthorized());
        }

        // Incluso con la contraseña correcta, la IP sigue bloqueada hasta que
        // pase la ventana: es lo que frena la fuerza bruta.
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(Map.of(
                                "email", "marta.sanchez@royaltown.local",
                                "password", "royaltown2026"))))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("Content-Type", containsString("problem+json")))
                .andExpect(header().string("Retry-After", "900"));
    }
}
