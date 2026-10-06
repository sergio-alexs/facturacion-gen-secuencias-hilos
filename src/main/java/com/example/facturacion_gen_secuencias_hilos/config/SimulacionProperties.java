package com.example.facturacion_gen_secuencias_hilos.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "simulacion")
@Getter
@Setter
public class SimulacionProperties {

    private long demoraSecuenciaMs = 300;
    private long demoraValidacionMinMs = 10;
    private long demoraValidacionMaxMs = 30;

    private long demoraClienteMinMs = 300;
    private long demoraClienteMaxMs = 2000;
    private double probabilidadClienteBloqueado = 0.15;

    private int maxSecuenciasConcurrentes = 8;
    private int maxDetallesConcurrentes = 10;
}

