package com.royaltown.mesa.servicio;

import com.royaltown.mesa.dto.CatalogoDtos.CatalogoDto;
import com.royaltown.mesa.dto.CatalogoDtos.CategoriaDto;
import com.royaltown.mesa.dto.CatalogoDtos.EstadoDto;
import com.royaltown.mesa.dto.CatalogoDtos.PrioridadDto;
import com.royaltown.mesa.dto.CatalogoDtos.TransicionDto;
import com.royaltown.mesa.repo.CategoriaRepository;
import com.royaltown.mesa.repo.EstadoRepository;
import com.royaltown.mesa.repo.PrioridadRepository;
import com.royaltown.mesa.repo.TransicionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Catálogos para los formularios del portal: una sola llamada autenticada
 * devuelve estados, prioridades (con su SLA), categorías y la matriz de
 * transiciones, para que la SPA no duplique reglas del ciclo de vida.
 */
@Service
public class CatalogoServicio {

    private final EstadoRepository estados;
    private final PrioridadRepository prioridades;
    private final CategoriaRepository categorias;
    private final TransicionRepository transiciones;

    public CatalogoServicio(EstadoRepository estados, PrioridadRepository prioridades,
            CategoriaRepository categorias, TransicionRepository transiciones) {
        this.estados = estados;
        this.prioridades = prioridades;
        this.categorias = categorias;
        this.transiciones = transiciones;
    }

    @Transactional(readOnly = true)
    public CatalogoDto catalogo() {
        return new CatalogoDto(
                estados.findAllByOrderByOrdenAsc().stream()
                        .map(e -> new EstadoDto(e.getId(), e.getNombre(), e.getOrden(), e.isEsFinal()))
                        .toList(),
                prioridades.findAllByOrderByOrdenAsc().stream()
                        .map(p -> new PrioridadDto(p.getId(), p.getNombre(), p.getSlaHoras()))
                        .toList(),
                categorias.findAllByOrderByNombreAsc().stream()
                        .map(c -> new CategoriaDto(c.getId(), c.getNombre(), c.getDescripcion()))
                        .toList(),
                transiciones.todas().stream()
                        .map(t -> new TransicionDto(t.getOrigen().getId(), t.getDestino().getId()))
                        .toList());
    }
}
