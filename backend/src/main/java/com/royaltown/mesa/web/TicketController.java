package com.royaltown.mesa.web;

import com.royaltown.mesa.dto.TicketDtos.AsignacionPeticion;
import com.royaltown.mesa.dto.TicketDtos.ComentarioPeticion;
import com.royaltown.mesa.dto.TicketDtos.CrearTicketPeticion;
import com.royaltown.mesa.dto.TicketDtos.PaginaTickets;
import com.royaltown.mesa.dto.TicketDtos.PatchTicketPeticion;
import com.royaltown.mesa.dto.TicketDtos.TicketDetalle;
import com.royaltown.mesa.dto.TicketDtos.TransicionPeticion;
import com.royaltown.mesa.dto.TicketDtos.ValoracionPeticion;
import com.royaltown.mesa.servicio.TicketServicio;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * El listado es «role-aware»: un empleado solo ve sus tickets aunque manipule
 * los filtros; el equipo de soporte ve todo el tablero. Las reglas finas
 * (transiciones, valoración) viven en TicketServicio.
 */
@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketServicio tickets;

    public TicketController(TicketServicio tickets) {
        this.tickets = tickets;
    }

    @GetMapping
    public PaginaTickets listar(
            @RequestParam(required = false) Integer estado,
            @RequestParam(required = false) Integer prioridad,
            @RequestParam(required = false) Integer categoria,
            @RequestParam(required = false) String texto,
            @RequestParam(name = "mios", defaultValue = "false") boolean soloMios,
            @RequestParam(name = "sinAsignar", defaultValue = "false") boolean sinAsignar,
            @RequestParam(defaultValue = "1") int pagina,
            @RequestParam(defaultValue = "20") int tamano) {
        return tickets.listar(estado, prioridad, categoria, texto, soloMios, sinAsignar, pagina, tamano);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TicketDetalle crear(@Valid @RequestBody CrearTicketPeticion peticion) {
        return tickets.crear(peticion);
    }

    @GetMapping("/{id}")
    public TicketDetalle detalle(@PathVariable Integer id) {
        return tickets.detalle(id);
    }

    @PostMapping("/{id}/comentarios")
    @ResponseStatus(HttpStatus.CREATED)
    public TicketDetalle comentar(@PathVariable Integer id,
            @Valid @RequestBody ComentarioPeticion peticion) {
        return tickets.comentar(id, peticion);
    }

    @PostMapping("/{id}/transiciones")
    public TicketDetalle transicionar(@PathVariable Integer id,
            @Valid @RequestBody TransicionPeticion peticion) {
        return tickets.transicionar(id, peticion);
    }

    /** Prioridad y categoría: solo el equipo de soporte (lo exige el servicio). */
    @PatchMapping("/{id}")
    public TicketDetalle parchear(@PathVariable Integer id,
            @RequestBody PatchTicketPeticion peticion) {
        return tickets.parchear(id, peticion);
    }

    @PostMapping("/{id}/asignacion")
    public TicketDetalle asignar(@PathVariable Integer id,
            @RequestBody AsignacionPeticion peticion) {
        return tickets.asignar(id, peticion);
    }

    @PostMapping("/{id}/valoracion")
    @ResponseStatus(HttpStatus.CREATED)
    public TicketDetalle valorar(@PathVariable Integer id,
            @Valid @RequestBody ValoracionPeticion peticion) {
        return tickets.valorar(id, peticion);
    }
}
