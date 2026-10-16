package com.royaltown.mesa.error;

/** El recurso no existe o (deliberadamente) no se confirma que exista. */
public class NoEncontradoException extends RuntimeException {

    public NoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
