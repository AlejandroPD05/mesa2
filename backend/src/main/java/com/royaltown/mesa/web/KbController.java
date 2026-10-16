package com.royaltown.mesa.web;

import com.royaltown.mesa.dto.KbDtos.ArticuloDetalleDto;
import com.royaltown.mesa.dto.KbDtos.ArticuloPeticion;
import com.royaltown.mesa.dto.KbDtos.ArticuloResumenDto;
import com.royaltown.mesa.dto.KbDtos.FeedbackDto;
import com.royaltown.mesa.dto.KbDtos.FeedbackPeticion;
import com.royaltown.mesa.servicio.KbServicio;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Centro de ayuda. Lectura y feedback son públicos (el portal tiene que
 * responder a la primera visita sin login); la gestión es solo del admin.
 */
@RestController
@RequestMapping("/api/kb/articulos")
public class KbController {

    private final KbServicio kb;

    public KbController(KbServicio kb) {
        this.kb = kb;
    }

    @GetMapping
    public List<ArticuloResumenDto> listar(
            @RequestParam(required = false) String texto,
            @RequestParam(required = false) Integer categoria,
            @RequestParam(name = "todos", defaultValue = "false") boolean incluirNoPublicados) {
        return kb.listar(texto, categoria, incluirNoPublicados);
    }

    @GetMapping("/{slug}")
    public ArticuloDetalleDto detalle(@PathVariable String slug) {
        return kb.detalle(slug);
    }

    @PostMapping("/{id}/feedback")
    public FeedbackDto feedback(@PathVariable Integer id,
            @Valid @RequestBody FeedbackPeticion peticion) {
        return kb.feedback(id, peticion.util());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @ResponseStatus(HttpStatus.CREATED)
    public ArticuloDetalleDto crear(@Valid @RequestBody ArticuloPeticion peticion) {
        return kb.crear(peticion);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ArticuloDetalleDto actualizar(@PathVariable Integer id,
            @Valid @RequestBody ArticuloPeticion peticion) {
        return kb.actualizar(id, peticion);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Integer id) {
        kb.eliminar(id);
    }
}
