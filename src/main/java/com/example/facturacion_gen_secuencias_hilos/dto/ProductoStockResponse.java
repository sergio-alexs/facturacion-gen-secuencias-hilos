package com.example.facturacion_gen_secuencias_hilos.dto;

import java.math.BigDecimal;
import java.util.List;

public record ProductoStockResponse(
        Long productoId,
        String sku,
        String nombre,
        String categoria,
        BigDecimal precioUnitario,
        int stockTotal,
        String estadoStock, // DISPONIBLE, BAJO_STOCK, AGOTADO
        List<LoteStockInfo> lotes
) {}
