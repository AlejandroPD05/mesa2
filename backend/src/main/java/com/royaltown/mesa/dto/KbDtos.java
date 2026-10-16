package com.royaltown.mesa.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public final class KbDtos {

    private KbDtos() {
    }

    public record ArticuloResumenDto(
            Integer id,
            String slug,
            String titulo,
            String resumen,
            String categoria,
            Integer visitas,
            Integer votosUtil,
            Integer votosNoUtil,
            boolean publicado) {
    }

    public record ArticuloDetalleDto(
            Integer id,
            String slug,
            String titulo,
            String resumen,
            String contenido,
            String categoria,
            Integer idCategoria,
            boolean publicado,
            Integer visitas,
            Integer votosUtil,
            Integer votosNoUtil,
            LocalDateTime fechaCreacion) {
    }

    public record ArticuloPeticion(
            @NotBlank(message = "El título es obligatorio")
            @Size(max = 150, message = "El título es demasiado largo")
            String titulo,

            @Size(max = 120, message = "El slug es demasiado largo")
            String slug,

            @NotBlank(message = "El resumen es obligatorio")
            @Size(max = 300, message = "El resumen es demasiado largo")
            String resumen,

            @NotBlank(message = "El contenido es obligatorio")
            @Size(max = 20000, message = "El contenido es demasiado largo")
            String contenido,

            @NotNull(message = "Elige una categoría")
            Integer idCategoria,

            @NotNull(message = "Indica si el artículo está publicado")
            Boolean publicado) {
    }

    public record FeedbackPeticion(@NotNull(message = "Indica si el artículo fue útil") Boolean util) {
    }

    public record FeedbackDto(Integer votosUtil, Integer votosNoUtil) {
    }
}
