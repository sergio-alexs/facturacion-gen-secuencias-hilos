package com.example.facturacion_gen_secuencias_hilos.dto;

import com.example.facturacion_gen_secuencias_hilos.model.Cliente;

import java.math.BigDecimal;
import java.util.List;

public record FacturaResultado(
        Long facturaId,
        String numero,
        Cliente cliente,
        String estado,
        BigDecimal subtotal,
        BigDecimal impuesto,
        BigDecimal total,
        int detallesFacturados,
        int detallesParciales,
        int detallesNoFacturados,
        List<DetalleProcesado> detalles,
        MetricasSimulacion metricas
) {}
