package com.royaltown.mesa.dto;

public final class UsuarioDtos {

    private UsuarioDtos() {
    }

    /** Miembro del equipo de soporte, para los selectores de asignación. */
    public record StaffDto(Integer id, String nombre, String rol) {
    }
}
