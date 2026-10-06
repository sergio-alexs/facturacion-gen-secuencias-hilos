package com.example.facturacion_gen_secuencias_hilos.secuencia;

public interface SecuenciaListener {
    void onGenerada(NombreSecuencia nombre, long tempId, long valorReal);

    void onError(NombreSecuencia nombre, long tempId, Throwable error);
}
