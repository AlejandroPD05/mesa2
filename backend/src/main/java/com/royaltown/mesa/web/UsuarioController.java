package com.royaltown.mesa.web;

import com.royaltown.mesa.dto.UsuarioDtos.StaffDto;
import com.royaltown.mesa.repo.UsuarioRepository;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioRepository usuarios;

    public UsuarioController(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    /** Selector de asignación de la bandeja: técnicos y administradores. */
    @GetMapping("/tecnicos")
    @PreAuthorize("hasAnyRole('TECNICO', 'ADMINISTRADOR')")
    public List<StaffDto> equipo() {
        return usuarios.equipoDeSoporte().stream()
                .map(usuario -> new StaffDto(usuario.getId(), usuario.getNombreCompleto(),
                        usuario.getRol().getNombre()))
                .toList();
    }
}
