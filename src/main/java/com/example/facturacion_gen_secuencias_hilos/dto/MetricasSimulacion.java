package com.example.facturacion_gen_secuencias_hilos.dto;

import java.util.Map;

public record MetricasSimulacion(
        String escenario,
        String modo,
        long tiempoTotalMs,
        long tiempoProcesamientoDetallesMs,
        long tiempoEsperaSecuenciasMs,
        int secuenciasSolicitadas,
        int secuenciasPendientesAlEsperar,
        long tiempoInsercionMs,
        Map<String, Map<String, Long>> mapeoTemporalReal
) {}
