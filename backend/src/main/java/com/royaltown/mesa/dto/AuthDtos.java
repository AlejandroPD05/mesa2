package com.royaltown.mesa.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record LoginPeticion(
            @NotBlank(message = "El correo es obligatorio")
            @Email(message = "El correo no tiene un formato válido")
            String email,

            @NotBlank(message = "La contraseña es obligatoria")
            String password) {
    }

    public record RegistroPeticion(
            @NotBlank(message = "El nombre es obligatorio")
            @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
            String nombre,

            @NotBlank(message = "El correo es obligatorio")
            @Email(message = "El correo no tiene un formato válido")
            @Size(max = 150, message = "El correo es demasiado largo")
            String email,

            @NotBlank(message = "La contraseña es obligatoria")
            @Size(min = 8, max = 100, message = "La contraseña debe tener al menos 8 caracteres")
            String password) {
    }

    /** Vista pública de un usuario: nunca sale de aquí el hash de la contraseña. */
    public record UsuarioDto(
            Integer id,
            String nombre,
            String email,
            String rol,
            String departamento) {
    }
}
