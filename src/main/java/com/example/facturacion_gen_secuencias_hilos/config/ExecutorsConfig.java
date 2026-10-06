package com.example.facturacion_gen_secuencias_hilos.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class ExecutorsConfig {

    @Bean(name = "detallesExecutor")
    public ExecutorService detallesExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    @Bean(name = "secuenciasExecutor")
    public ExecutorService secuenciasExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    @Bean(name = "clienteExecutor")
    public ExecutorService clienteExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}
