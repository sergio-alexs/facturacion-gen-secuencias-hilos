package com.example.facturacion_gen_secuencias_hilos.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationException(MethodArgumentNotValidException ex) {
        log.warn("Error de validación en la petición: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Uno o más campos contienen valores inválidos o faltantes"
        );
        problemDetail.setTitle("Error de Validación");
        problemDetail.setType(URI.create("urn:problem-type:validation-error"));
        problemDetail.setProperty("timestamp", Instant.now());

        List<Map<String, String>> invalidParams = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map(error -> {
                    String campo = (error instanceof FieldError fe) ? fe.getField() : error.getObjectName();
                    String mensaje = error.getDefaultMessage() != null ? error.getDefaultMessage() : "Valor inválido";
                    return Map.of("campo", campo, "mensaje", mensaje);
                })
                .toList();

        problemDetail.setProperty("invalidParams", invalidParams);
        return problemDetail;
    }

    @ExceptionHandler(ClienteBloqueadoException.class)
    public ProblemDetail handleClienteBloqueado(ClienteBloqueadoException ex) {
        log.warn("Cliente bloqueado detectado: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNPROCESSABLE_ENTITY,
                ex.getMessage()
        );
        problemDetail.setTitle("Cliente Bloqueado");
        problemDetail.setType(URI.create("urn:problem-type:cliente-bloqueado"));
        problemDetail.setProperty("timestamp", Instant.now());
        if (ex.getIdentificacion() != null) {
            problemDetail.setProperty("identificacion", ex.getIdentificacion());
        }

        return problemDetail;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Argumento ilegal: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );
        problemDetail.setTitle("Parámetro Inválido");
        problemDetail.setType(URI.create("urn:problem-type:bad-request"));
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneralException(Exception ex) {
        log.error("Error no controlado durante la ejecución: ", ex);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrió un error inesperado al procesar la solicitud"
        );
        problemDetail.setTitle("Error Interno del Servidor");
        problemDetail.setType(URI.create("urn:problem-type:internal-server-error"));
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }
}
