package com.example.facturacion_gen_secuencias_hilos.dto;

import java.time.Instant;

public record LoteStockInfo(
        Long loteId,
        String codigoLote,
        int cantidadDisponible,
        Instant fechaIngreso,
        Instant fechaVencimiento
) {}
