package com.royaltown.mesa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Tablero de KPI y catálogos: quién entra y qué devuelven. */
class InformesApiTest extends BasePruebas {

    @Test
    @DisplayName("los KPI exigen sesión de soporte")
    void kpisProtegidos() throws Exception {
        mockMvc.perform(get("/api/informes/kpis"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/informes/kpis").cookie(comoEmpleado()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("un técnico ve los KPI de las vistas SQL")
    void kpisParaTecnico() throws Exception {
        JsonNode kpis = leer(mockMvc.perform(get("/api/informes/kpis").cookie(comoTecnico()))
                .andExpect(status().isOk())
                .andReturn());

        assertThat(kpis.get("abiertos").asLong()).isGreaterThanOrEqualTo(0);
        assertThat(kpis.get("slaIncumplidos").asLong()).isGreaterThanOrEqualTo(0);

        // Los cinco estados del ciclo de vida están siempre presentes.
        assertThat(kpis.get("porEstado")).hasSize(5);

        // La semilla trae cierres, así que hay rendimiento que mostrar.
        assertThat(kpis.get("porTecnico")).isNotEmpty();
        assertThat(kpis.get("porTecnico").get(0).has("resueltos")).isTrue();
    }

    @Test
    @DisplayName("el rol administrador también entra al tablero")
    void kpisParaAdmin() throws Exception {
        mockMvc.perform(get("/api/informes/kpis").cookie(comoAdmin()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("el catálogo alimenta los formularios: 5 estados, 4 prioridades, 5 categorías y 10 transiciones")
    void catalogoCompleto() throws Exception {
        mockMvc.perform(get("/api/catalogo"))
                .andExpect(status().isUnauthorized());

        JsonNode catalogo = leer(mockMvc.perform(get("/api/catalogo").cookie(comoEmpleado()))
                .andExpect(status().isOk())
                .andReturn());

        assertThat(catalogo.get("estados")).hasSize(5);
        assertThat(catalogo.get("prioridades")).hasSize(4);
        assertThat(catalogo.get("categorias")).hasSize(5);
        assertThat(catalogo.get("transiciones")).hasSize(10);

        JsonNode critica = catalogo.get("prioridades").get(3);
        assertThat(critica.get("nombre").asText()).isEqualTo("Critica");
        assertThat(critica.get("slaHoras").asInt()).isEqualTo(4);
    }
}
