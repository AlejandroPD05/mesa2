package com.royaltown.mesa.dto;

import java.util.List;

public final class CatalogoDtos {

    private CatalogoDtos() {
    }

    public record EstadoDto(Integer id, String nombre, Integer orden, boolean esFinal) {
    }

    public record PrioridadDto(Integer id, String nombre, Integer slaHoras) {
    }

    public record CategoriaDto(Integer id, String nombre, String descripcion) {
    }

    public record TransicionDto(Integer origen, Integer destino) {
    }

    /**
     * Todo lo que necesitan los formularios del portal en una sola llamada:
     * estados, prioridades (con su SLA), categorías y la matriz de transiciones.
     */
    public record CatalogoDto(
            List<EstadoDto> estados,
            List<PrioridadDto> prioridades,
            List<CategoriaDto> categorias,
            List<TransicionDto> transiciones) {
    }
}
