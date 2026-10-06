package com.example.facturacion_gen_secuencias_hilos.exception;

public class ClienteBloqueadoException extends RuntimeException {
    private final String identificacion;

    public ClienteBloqueadoException(String identificacion, String mensaje) {
        super(mensaje);
        this.identificacion = identificacion;
    }

    public ClienteBloqueadoException(String mensaje) {
        super(mensaje);
        this.identificacion = null;
    }

    public String getIdentificacion() {
        return identificacion;
    }
}

