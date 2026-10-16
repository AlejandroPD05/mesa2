package com.royaltown.mesa.error;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Todos los errores de la API salen como ProblemDetail (RFC 7807), también los
 * de seguridad: la SPA solo tiene que entender un formato.
 */
@RestControllerAdvice
public class ManejadorErrores {

    private static final Logger log = LoggerFactory.getLogger(ManejadorErrores.class);

    @ExceptionHandler(ReglaNegocioException.class)
    ProblemDetail reglaDeNegocio(ReglaNegocioException e) {
        ProblemDetail problema = problema(HttpStatus.BAD_REQUEST, "Operación no permitida", e.getMessage(),
                "regla-de-negocio");
        e.getExtra().forEach(problema::setProperty);
        return problema;
    }

    @ExceptionHandler(NoEncontradoException.class)
    ProblemDetail noEncontrado(NoEncontradoException e) {
        return problema(HttpStatus.NOT_FOUND, "No encontrado", e.getMessage(), "no-encontrado");
    }

    @ExceptionHandler(NoAutenticadoException.class)
    ProblemDetail noAutenticado(NoAutenticadoException e) {
        return problema(HttpStatus.UNAUTHORIZED, "No autenticado", e.getMessage(), "no-autenticado");
    }

    @ExceptionHandler(SinPermisoException.class)
    ProblemDetail sinPermiso(SinPermisoException e) {
        return problema(HttpStatus.FORBIDDEN, "Sin permiso", e.getMessage(), "sin-permiso");
    }

    @ExceptionHandler(AccessDeniedException.class)
    ProblemDetail accesoDenegado(AccessDeniedException e) {
        return problema(HttpStatus.FORBIDDEN, "Sin permiso",
                "Tu perfil no tiene acceso a esta operación", "sin-permiso");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail validacion(MethodArgumentNotValidException e) {
        Map<String, String> errores = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors()
                .forEach(error -> errores.putIfAbsent(error.getField(), error.getDefaultMessage()));
        ProblemDetail problema = problema(HttpStatus.BAD_REQUEST, "Validación fallida",
                "Hay campos con errores en el formulario", "validacion");
        problema.setProperty("errores", errores);
        return problema;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ProblemDetail cuerpoIlegible(HttpMessageNotReadableException e) {
        return problema(HttpStatus.BAD_REQUEST, "Petición mal formada",
                "El cuerpo de la petición no es JSON válido", "cuerpo-ilegible");
    }

    /** Una ruta que no existe sigue siendo un 404, no un 500. */
    @ExceptionHandler(NoResourceFoundException.class)
    ProblemDetail rutaNoEncontrada(NoResourceFoundException e) {
        return problema(HttpStatus.NOT_FOUND, "No encontrado",
                "La ruta solicitada no existe", "ruta-no-encontrada");
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail inesperado(Exception e) {
        log.error("Error inesperado en la API", e);
        return problema(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno",
                "Algo ha fallado por nuestra parte; inténtalo de nuevo", "error-interno");
    }

    private static ProblemDetail problema(HttpStatus estado, String titulo, String detalle, String sufijo) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(estado, detalle);
        problema.setTitle(titulo);
        problema.setType(URI.create("https://mesa2.dev/errores/" + sufijo));
        return problema;
    }
}
