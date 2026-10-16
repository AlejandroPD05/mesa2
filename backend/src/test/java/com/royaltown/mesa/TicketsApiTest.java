package com.royaltown.mesa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.http.Cookie;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

/** Reglas de negocio del ciclo de vida de un ticket, de punta a punta. */
class TicketsApiTest extends BasePruebas {

    private static final String MARTA = "Marta Sanchez Lopez";
    private static final String LAURA = "Laura Gomez Martin";
    private static final String DIEGO = "Diego Fernandez Ibanez";

    // ---------------------------------------------------------------
    // Creación y SLA
    // ---------------------------------------------------------------

    @Test
    @DisplayName("crear ticket fija el SLA de la prioridad y registra el historial")
    void crearTicketFijaSla() throws Exception {
        Cookie marta = comoEmpleado();
        MvcResult resultado = mockMvc.perform(post("/api/tickets")
                        .cookie(marta)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "titulo", "La impresora de contabilidad atasca los folios",
                                "descripcion",
                                "Desde ayer cada tercer folio sale arrugado y hay que reiniciar la cola.",
                                "idCategoria", 1,
                                "idPrioridad", 2))))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode ticket = leer(resultado);
        assertThat(ticket.get("estado").asText()).isEqualTo("Abierto");
        assertThat(ticket.get("prioridad").asText()).isEqualTo("Media");
        assertThat(ticket.get("slaHoras").asInt()).isEqualTo(24);
        assertThat(Duration.between(
                LocalDateTime.parse(ticket.get("fechaCreacion").asText()),
                LocalDateTime.parse(ticket.get("fechaLimite").asText())))
                .isEqualTo(Duration.ofHours(24));
        assertThat(ticket.get("historial").get(0).get("campo").asText()).isEqualTo("estado");
        assertThat(ticket.get("historial").get(0).get("nuevo").asText()).isEqualTo("Abierto");
    }

    @Test
    @DisplayName("un empleado solo ve sus tickets; el técnico ve todo el tablero")
    void listadoPorRol() throws Exception {
        Cookie marta = comoEmpleado();
        Cookie laura = comoTecnico();

        JsonNode propios = leer(mockMvc.perform(get("/api/tickets")
                        .param("tamano", "100").cookie(marta))
                .andExpect(status().isOk())
                .andReturn());
        assertThat(propios.get("total").asLong()).isGreaterThanOrEqualTo(1);
        for (JsonNode elemento : propios.get("elementos")) {
            assertThat(elemento.get("creador").asText()).isEqualTo(MARTA);
        }

        JsonNode todos = leer(mockMvc.perform(get("/api/tickets")
                        .param("tamano", "100").cookie(laura))
                .andExpect(status().isOk())
                .andReturn());
        assertThat(todos.get("total").asLong()).isGreaterThan(propios.get("total").asLong());
    }

    @Test
    @DisplayName("el detalle de un ticket ajeno da 404 a un empleado y 200 al técnico")
    void detalleAjeno() throws Exception {
        // El ticket 1 de la semilla pertenece a Javier Torres, no a Marta.
        mockMvc.perform(get("/api/tickets/1").cookie(comoEmpleado()))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/tickets/1").cookie(comoTecnico()))
                .andExpect(status().isOk());
    }

    // ---------------------------------------------------------------
    // Conversación
    // ---------------------------------------------------------------

    @Test
    @DisplayName("las notas internas solo existen para el equipo de soporte")
    void comentariosInternos() throws Exception {
        Cookie marta = comoEmpleado();
        Cookie laura = comoTecnico();
        long id = crearTicket(marta, "Pantalla que parpadea en el puesto de Recepción");

        mockMvc.perform(post("/api/tickets/" + id + "/comentarios").cookie(laura)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("texto", "Nota interna: el equipo está en garantía.", "interno", true))))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/tickets/" + id + "/comentarios").cookie(laura)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("texto", "Estamos revisando el cableado, te contamos pronto.", "interno", false))))
                .andExpect(status().isCreated());

        JsonNode vistaEmpleada = detalle(marta, id);
        assertThat(vistaEmpleada.get("comentarios")).hasSize(1);
        assertThat(vistaEmpleada.get("comentarios").get(0).get("interno").asBoolean()).isFalse();

        JsonNode vistaTecnico = detalle(laura, id);
        assertThat(vistaTecnico.get("comentarios")).hasSize(2);

        mockMvc.perform(post("/api/tickets/" + id + "/comentarios").cookie(marta)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("texto", "Intento de nota interna.", "interno", true))))
                .andExpect(status().isForbidden());
    }

    // ---------------------------------------------------------------
    // Ciclo de vida
    // ---------------------------------------------------------------

    @Test
    @DisplayName("una transición ilegal explica qué estados sí son válidos")
    void transicionIlegal() throws Exception {
        Cookie marta = comoEmpleado();
        Cookie laura = comoTecnico();
        long id = crearTicket(marta, "Ratón que hace doble clic accidental");

        MvcResult resultado = mockMvc.perform(post("/api/tickets/" + id + "/transiciones").cookie(laura)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("estadoDestino", 5))))
                .andExpect(status().isBadRequest())
                .andReturn();

        JsonNode problema = leer(resultado);
        assertThat(problema.get("estadoActual").asText()).isEqualTo("Abierto");
        java.util.List<String> validas = new java.util.ArrayList<>();
        problema.get("transicionesValidas").forEach(n -> validas.add(n.asText()));
        assertThat(validas).contains("En_Progreso", "Pendiente_Usuario");
    }

    @Test
    @DisplayName("un empleado no puede mover el ticket por su cuenta")
    void empleadoNoPuedeTransicionar() throws Exception {
        Cookie marta = comoEmpleado();
        long id = crearTicket(marta, "Correo duplicado en el buzón compartido");

        mockMvc.perform(post("/api/tickets/" + id + "/transiciones").cookie(marta)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("estadoDestino", 2))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("flujo completo: progreso → resolución → confirmación → valoración")
    void flujoCompleto() throws Exception {
        Cookie marta = comoEmpleado();
        Cookie laura = comoTecnico();
        long id = crearTicket(marta, "Sin acceso a la carpeta de presupuestos");

        // La técnica se hace cargo: se asigna sola y el SLA sigue vivo.
        JsonNode enProgreso = transicionar(laura, id, 2, null);
        assertThat(enProgreso.get("estado").asText()).isEqualTo("En_Progreso");
        assertThat(enProgreso.get("tecnico").asText()).isEqualTo(LAURA);
        assertThat(enProgreso.get("fechaCierre").isNull()).isTrue();
        assertThat(enProgreso.get("sla").get("relevante").asBoolean()).isTrue();

        // Resolver sin explicar la solución no vale.
        mockMvc.perform(post("/api/tickets/" + id + "/transiciones").cookie(laura)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("estadoDestino", 4))))
                .andExpect(status().isBadRequest());

        // Resolver con solución deja comentario público y congela el SLA.
        JsonNode resuelto = transicionar(laura, id, 4, "Se revisó el permiso del grupo y se añadió a la usuaria.");
        assertThat(resuelto.get("estado").asText()).isEqualTo("Resuelto");
        assertThat(resuelto.get("fechaCierre").isNull()).isFalse();
        assertThat(resuelto.get("sla").get("relevante").asBoolean()).isFalse();
        assertThat(textosDe(resuelto.get("comentarios")))
                .anyMatch(texto -> texto.startsWith("Solución aplicada:"));

        // La empleada confirma el cierre…
        JsonNode cerrado = transicionar(marta, id, 5, null);
        assertThat(cerrado.get("esFinal").asBoolean()).isTrue();

        // …y valora una única vez.
        MvcResult valoracion = mockMvc.perform(post("/api/tickets/" + id + "/valoracion").cookie(marta)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("puntuacion", 5, "comentario", "Rápido y claro."))))
                .andExpect(status().isCreated())
                .andReturn();
        assertThat(leer(valoracion).get("valoracion").get("puntuacion").asInt()).isEqualTo(5);

        mockMvc.perform(post("/api/tickets/" + id + "/valoracion").cookie(marta)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("puntuacion", 4))))
                .andExpect(status().isBadRequest());

        // El equipo de soporte no valora sus propios tickets.
        mockMvc.perform(post("/api/tickets/" + id + "/valoracion").cookie(laura)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("puntuacion", 5))))
                .andExpect(status().isForbidden());
    }

    // ---------------------------------------------------------------
    // Gestión del equipo
    // ---------------------------------------------------------------

    @Test
    @DisplayName("cambiar la prioridad recalcula la fecha límite y queda en el historial")
    void parchearPrioridad() throws Exception {
        Cookie marta = comoEmpleado();
        Cookie laura = comoTecnico();
        long id = crearTicket(marta, "Cuenta bloqueada tras varios intentos fallidos");

        mockMvc.perform(patch("/api/tickets/" + id).cookie(marta)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("idPrioridad", 4))))
                .andExpect(status().isForbidden());

        JsonNode modificado = leer(mockMvc.perform(patch("/api/tickets/" + id).cookie(laura)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("idPrioridad", 4))))
                .andExpect(status().isOk())
                .andReturn());

        assertThat(modificado.get("prioridad").asText()).isEqualTo("Critica");
        assertThat(Duration.between(
                LocalDateTime.parse(modificado.get("fechaCreacion").asText()),
                LocalDateTime.parse(modificado.get("fechaLimite").asText())))
                .isEqualTo(Duration.ofHours(4));
        assertThat(camposDe(modificado.get("historial"))).contains("prioridad");
    }

    @Test
    @DisplayName("asignar a otro técnico y desasignar, con rastro en el historial")
    void asignarYDesasignar() throws Exception {
        Cookie marta = comoEmpleado();
        Cookie laura = comoTecnico();
        long id = crearTicket(marta, "Teclado con teclas que no responden");

        JsonNode asignado = leer(mockMvc.perform(post("/api/tickets/" + id + "/asignacion").cookie(laura)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("idTecnico", 4))))
                .andExpect(status().isOk())
                .andReturn());
        assertThat(asignado.get("tecnico").asText()).isEqualTo(DIEGO);

        JsonNode liberado = leer(mockMvc.perform(post("/api/tickets/" + id + "/asignacion").cookie(laura)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(nodo().putNull("idTecnico").toString()))
                .andExpect(status().isOk())
                .andReturn());
        assertThat(liberado.get("tecnico").isNull()).isTrue();

        // No se puede asignar a quien no es del equipo de soporte.
        mockMvc.perform(post("/api/tickets/" + id + "/asignacion").cookie(laura)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("idTecnico", 1))))
                .andExpect(status().isBadRequest());

        // Un empleado no reparte trabajo.
        mockMvc.perform(post("/api/tickets/" + id + "/asignacion").cookie(marta)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("idTecnico", 3))))
                .andExpect(status().isForbidden());

        // El selector de asignación lista al equipo y solo lo ve el equipo.
        JsonNode equipo = leer(mockMvc.perform(get("/api/usuarios/tecnicos").cookie(laura))
                .andExpect(status().isOk())
                .andReturn());
        boolean hayDiego = false;
        for (JsonNode miembro : equipo) {
            assertThat(miembro.get("rol").asText()).isIn("tecnico", "administrador");
            if (miembro.get("nombre").asText().equals(DIEGO)) {
                hayDiego = true;
            }
        }
        assertThat(hayDiego).isTrue();
        mockMvc.perform(get("/api/usuarios/tecnicos").cookie(marta))
                .andExpect(status().isForbidden());
    }

    // ---------------------------------------------------------------
    // Utilidades
    // ---------------------------------------------------------------

    private JsonNode detalle(Cookie sesion, long id) throws Exception {
        return leer(mockMvc.perform(get("/api/tickets/" + id).cookie(sesion))
                .andExpect(status().isOk())
                .andReturn());
    }

    private JsonNode transicionar(Cookie sesion, long id, int destino, String resolucion) throws Exception {
        var nodo = nodo().put("estadoDestino", destino);
        if (resolucion != null) {
            nodo.put("resolucion", resolucion);
        }
        return leer(mockMvc.perform(post("/api/tickets/" + id + "/transiciones").cookie(sesion)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(nodo.toString()))
                .andExpect(status().isOk())
                .andReturn());
    }

    private static java.util.List<String> textosDe(JsonNode comentarios) {
        java.util.List<String> textos = new java.util.ArrayList<>();
        comentarios.forEach(c -> textos.add(c.get("texto").asText()));
        return textos;
    }

    private static java.util.List<String> camposDe(JsonNode historial) {
        java.util.List<String> campos = new java.util.ArrayList<>();
        historial.forEach(h -> campos.add(h.get("campo").asText()));
        return campos;
    }
}
