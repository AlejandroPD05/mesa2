package com.royaltown.mesa.error;

/** El usuario está autenticado pero su rol no permite esta operación. */
public class SinPermisoException extends RuntimeException {

    public SinPermisoException(String mensaje) {
        super(mensaje);
    }
}
