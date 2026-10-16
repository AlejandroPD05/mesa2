package com.royaltown.mesa.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

public final class TicketDtos {

    private TicketDtos() {
    }

    /** Estado del SLA respecto al reloj: relevante mientras el ticket no cierra. */
    public record SlaDto(boolean relevante, boolean incumplido, Long minutosRestantes) {
    }

    public record TicketResumen(
            Integer id,
            String titulo,
            String estado,
            boolean esFinal,
            String prioridad,
            Integer slaHoras,
            String categoria,
            String creador,
            String tecnico,
            LocalDateTime fechaCreacion,
            LocalDateTime fechaLimite,
            LocalDateTime fechaCierre,
            SlaDto sla) {
    }

    public record ComentarioDto(
            Integer id,
            String autor,
            boolean interno,
            String texto,
            LocalDateTime fecha) {
    }

    public record HistorialDto(
            String campo,
            String anterior,
            String nuevo,
            String autor,
            LocalDateTime fecha) {
    }

    public record ValoracionDto(
            Integer puntuacion,
            String comentario,
            LocalDateTime fecha) {
    }

    public record TicketDetalle(
            Integer id,
            String titulo,
            String descripcion,
            String estado,
            boolean esFinal,
            String prioridad,
            Integer slaHoras,
            String categoria,
            Integer idCategoria,
            Integer idPrioridad,
            String creador,
            Integer idCreador,
            String tecnico,
            Integer idTecnico,
            LocalDateTime fechaCreacion,
            LocalDateTime fechaLimite,
            LocalDateTime fechaCierre,
            SlaDto sla,
            List<ComentarioDto> comentarios,
            List<HistorialDto> historial,
            ValoracionDto valoracion) {
    }

    public record CrearTicketPeticion(
            @NotBlank(message = "El título es obligatorio")
            @Size(min = 5, max = 150, message = "El título debe tener entre 5 y 150 caracteres")
            String titulo,

            @NotBlank(message = "La descripción es obligatoria")
            @Size(min = 15, max = 5000, message = "La descripción debe tener entre 15 y 5000 caracteres")
            String descripcion,

            @NotNull(message = "Elige una categoría")
            Integer idCategoria,

            @NotNull(message = "Elige una prioridad")
            Integer idPrioridad) {
    }

    public record ComentarioPeticion(
            @NotBlank(message = "El comentario no puede estar vacío")
            @Size(max = 2000, message = "El comentario es demasiado largo")
            String texto,
            boolean interno) {
    }

    public record TransicionPeticion(
            @NotNull(message = "Indica el estado de destino")
            Integer estadoDestino,

            @Size(max = 2000, message = "La resolución es demasiado larga")
            String resolucion) {
    }

    public record PatchTicketPeticion(
            Integer idPrioridad,
            Integer idCategoria) {
    }

    /** {@code idTecnico} nulo desasigna el ticket; con valor, asigna a ese técnico. */
    public record AsignacionPeticion(Integer idTecnico) {
    }

    public record ValoracionPeticion(
            @NotNull(message = "Indica una puntuación")
            @Min(value = 1, message = "La puntuación va de 1 a 5")
            @Max(value = 5, message = "La puntuación va de 1 a 5")
            Integer puntuacion,

            @Size(max = 255, message = "El comentario es demasiado largo")
            String comentario) {
    }

    public record PaginaTickets(
            long total,
            int pagina,
            int tamano,
            List<TicketResumen> elementos) {
    }
}
