package com.example.facturacion_gen_secuencias_hilos.dto;

import java.math.BigDecimal;
import java.util.List;

public record DetalleProcesado(
        long tempIdDetalle,
        Long productoId,
        int cantidadSolicitada,
        int cantidadFacturada,
        BigDecimal precioUnitario,
        BigDecimal total,
        String estado,
        String motivo,
        List<LoteAsignado> lotes,
        List<ComentarioProcesado> comentarios
) {}
