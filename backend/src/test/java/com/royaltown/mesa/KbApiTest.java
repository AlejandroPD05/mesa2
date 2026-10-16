package com.royaltown.mesa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.http.Cookie;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

/** Centro de ayuda: buscador público, feedback y gestión solo para admin. */
class KbApiTest extends BasePruebas {

    @Test
    @DisplayName("el buscador es público y filtra por texto")
    void buscadorPublico() throws Exception {
        MvcResult resultado = mockMvc.perform(get("/api/kb/articulos").param("texto", "impresora"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode articulos = leer(resultado);
        assertThat(articulos).isNotEmpty();
        for (JsonNode articulo : articulos) {
            assertThat(articulo.get("publicado").asBoolean()).isTrue();
        }
    }

    @Test
    @DisplayName("abrir un artículo suma visita y el feedback suma votos")
    void visitasYFeedback() throws Exception {
        JsonNode antes = detallePublico("la-impresora-no-imprime");
        JsonNode despues = detallePublico("la-impresora-no-imprime");
        assertThat(despues.get("visitas").asInt()).isEqualTo(antes.get("visitas").asInt() + 1);

        int votosAntes = despues.get("votosUtil").asInt();
        MvcResult feedback = mockMvc.perform(post("/api/kb/articulos/"
                        + despues.get("id").asLong() + "/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("util", true))))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(leer(feedback).get("votosUtil").asInt()).isEqualTo(votosAntes + 1);
    }

    @Test
    @DisplayName("solo el admin gestiona artículos; el técnico recibe 403")
    void gestionSoloAdmin() throws Exception {
        Cookie tecnico = comoTecnico();
        Cookie admin = comoAdmin();
        String slug = "articulo-de-prueba-" + System.nanoTime() % 100000;

        String peticion = json(Map.of(
                "titulo", "Artículo de prueba de gestión",
                "slug", slug,
                "resumen", "Resumen corto para el listado del centro de ayuda.",
                "contenido", "## Contenido\n\nPasos de prueba del artículo.",
                "idCategoria", 1,
                "publicado", true));

        mockMvc.perform(post("/api/kb/articulos").cookie(tecnico)
                        .contentType(MediaType.APPLICATION_JSON).content(peticion))
                .andExpect(status().isForbidden());

        MvcResult creado = mockMvc.perform(post("/api/kb/articulos").cookie(admin)
                        .contentType(MediaType.APPLICATION_JSON).content(peticion))
                .andExpect(status().isCreated())
                .andReturn();
        long id = leer(creado).get("id").asLong();

        // Slug duplicado: no se permite.
        mockMvc.perform(post("/api/kb/articulos").cookie(admin)
                        .contentType(MediaType.APPLICATION_JSON).content(peticion))
                .andExpect(status().isBadRequest());

        mockMvc.perform(put("/api/kb/articulos/" + id).cookie(admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "titulo", "Artículo de prueba (revisado)",
                                "slug", slug,
                                "resumen", "Resumen actualizado del artículo de prueba.",
                                "contenido", "## Contenido\n\nVersión corregida.",
                                "idCategoria", 2,
                                "publicado", true))))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/kb/articulos/" + id).cookie(admin))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/kb/articulos/" + slug))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("los borradores solo existen para el equipo; el público recibe 404")
    void borradoresOcultos() throws Exception {
        Cookie admin = comoAdmin();
        String slug = "borrador-" + System.nanoTime() % 100000;

        mockMvc.perform(post("/api/kb/articulos").cookie(admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "titulo", "Borrador interno sin publicar",
                                "slug", slug,
                                "resumen", "Todavía no debe verlo nadie del portal.",
                                "contenido", "## Borrador\n\nEn revisión.",
                                "idCategoria", 1,
                                "publicado", false))))
                .andExpect(status().isCreated());

        // Sin sesión: ni en el listado ni por URL directa.
        JsonNode listadoPublico = leer(mockMvc.perform(get("/api/kb/articulos")
                        .param("texto", "borrador")).andExpect(status().isOk()).andReturn());
        for (JsonNode articulo : listadoPublico) {
            assertThat(articulo.get("slug").asText()).isNotEqualTo(slug);
        }
        mockMvc.perform(get("/api/kb/articulos/" + slug))
                .andExpect(status().isNotFound());

        // El admin sí lo ve, listado como borrador.
        JsonNode listadoAdmin = leer(mockMvc.perform(get("/api/kb/articulos")
                        .param("texto", "borrador").param("todos", "true").cookie(admin))
                .andExpect(status().isOk())
                .andReturn());
        boolean encontrado = false;
        for (JsonNode articulo : listadoAdmin) {
            if (articulo.get("slug").asText().equals(slug)) {
                encontrado = true;
                assertThat(articulo.get("publicado").asBoolean()).isFalse();
            }
        }
        assertThat(encontrado).isTrue();
    }

    private JsonNode detallePublico(String slug) throws Exception {
        return leer(mockMvc.perform(get("/api/kb/articulos/" + slug))
                .andExpect(status().isOk())
                .andReturn());
    }
}
