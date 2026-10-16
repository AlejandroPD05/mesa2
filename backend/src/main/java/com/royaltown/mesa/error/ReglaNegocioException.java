package com.royaltown.mesa.error;

import java.util.Map;

/**
 * Violación de una regla de negocio (no de validación de formato): el estado no
 * permite esa transición, el ticket ya está valorado, etc. El manejador devuelve
 * 400 con el detalle y, si aporta contexto extra, en propiedades del ProblemDetail.
 */
public class ReglaNegocioException extends RuntimeException {

    private final transient Map<String, Object> extra;

    public ReglaNegocioException(String mensaje) {
        this(mensaje, Map.of());
    }

    public ReglaNegocioException(String mensaje, Map<String, Object> extra) {
        super(mensaje);
        this.extra = extra;
    }

    public Map<String, Object> getExtra() {
        return extra;
    }
}
