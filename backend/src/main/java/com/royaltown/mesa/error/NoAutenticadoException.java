package com.royaltown.mesa.error;

/** No hay sesión: el manejador lo traduce a 401. */
public class NoAutenticadoException extends RuntimeException {

    public NoAutenticadoException(String mensaje) {
        super(mensaje);
    }
}
