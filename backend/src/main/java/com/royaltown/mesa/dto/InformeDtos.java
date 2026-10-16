package com.royaltown.mesa.dto;

import java.util.List;

public final class InformeDtos {

    private InformeDtos() {
    }

    public record EstadoTotal(String estado, long total) {
    }

    /** {@code minutosMedios} nulo si no hay cierres que promediar. */
    public record TecnicoRendimiento(String tecnico, long resueltos, Long minutosMedios) {
    }

    public record KpisDto(
            long abiertos,
            long slaIncumplidos,
            List<EstadoTotal> porEstado,
            List<TecnicoRendimiento> porTecnico) {
    }
}
