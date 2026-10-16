package com.royaltown.mesa.servicio;

/**
 * Identidad que viaja dentro del JWT y se convierte en el principal de Spring
 * Security. Lleva solo lo que necesita la autorización, nunca el hash.
 */
public record Sesion(Integer id, String nombre, String email, String rol) {

    public boolean esEmpleado() {
        return "empleado".equals(rol);
    }

    public boolean esTecnico() {
        return "tecnico".equals(rol);
    }

    public boolean esAdmin() {
        return "administrador".equals(rol);
    }

    public boolean esStaff() {
        return esTecnico() || esAdmin();
    }
}
