package com.example.facturacion_gen_secuencias_hilos.dto;

public record LoteAsignado(
        long tempIdDetalleLote,
        long tempIdMovimiento,
        Long loteId,
        String codigoLote,
        int cantidad
) {}
