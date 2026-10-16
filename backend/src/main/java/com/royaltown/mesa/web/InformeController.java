package com.royaltown.mesa.web;

import com.royaltown.mesa.dto.InformeDtos.KpisDto;
import com.royaltown.mesa.servicio.InformeServicio;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/informes")
public class InformeController {

    private final InformeServicio informes;

    public InformeController(InformeServicio informes) {
        this.informes = informes;
    }

    /** Los KPI son para el equipo de soporte; un empleado no ve el tablero. */
    @GetMapping("/kpis")
    @PreAuthorize("hasAnyRole('TECNICO', 'ADMINISTRADOR')")
    public KpisDto kpis() {
        return informes.kpis();
    }
}
