package com.example.facturacion_gen_secuencias_hilos.service;

import com.example.facturacion_gen_secuencias_hilos.config.SimulacionProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;

@Service
@Slf4j
public class ClienteValidacionService {

    public record ResultadoValidacionCliente(
            boolean bloqueado,
            String motivo,
            long demoraMs
    ) {}

    private final SimulacionProperties properties;

    public ClienteValidacionService(SimulacionProperties properties) {
        this.properties = properties;
    }

    /**
     * Simula la llamada a un servicio externo para verificar si el cliente está bloqueado o reportado.
     * La llamada toma de 300ms a 2 segundos de forma aleatoria sobre un hilo virtual.
     */
    public ResultadoValidacionCliente validarCliente(String identificacion) {
        long min = properties.getDemoraClienteMinMs();
        long max = properties.getDemoraClienteMaxMs();
        long demoraMs = (max > min)
                ? ThreadLocalRandom.current().nextLong(min, max + 1)
                : min;

        log.info("Iniciando validación de cliente [{}] en servicio externo (demora simulada: {}ms)...", identificacion, demoraMs);

        try {
            Thread.sleep(demoraMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Validación de cliente interrumpida", e);
        }

        boolean bloqueado;
        String motivo = null;

        String idNorm = identificacion != null ? identificacion.toUpperCase() : "";
        if (idNorm.contains("BLOQUEADO") || idNorm.contains("REPORTADO")) {
            bloqueado = true;
            motivo = "Cliente reportado en listas restrictivas y prevención de fraude";
        } else if (idNorm.contains("HABILITADO") || idNorm.contains("VALIDO") || idNorm.contains("OK")) {
            bloqueado = false;
        } else {
            // Decisión aleatoria basada en la probabilidad configurada
            double prob = properties.getProbabilidadClienteBloqueado();
            bloqueado = prob > 0 && (ThreadLocalRandom.current().nextDouble() < prob);
            if (bloqueado) {
                motivo = "Cliente reportado aleatoriamente en centrales de riesgo financiero";
            }
        }

        log.info("Resultado validación cliente [{}]: bloqueado={}, motivo='{}', tiempo={}ms",
                identificacion, bloqueado, motivo, demoraMs);

        return new ResultadoValidacionCliente(bloqueado, motivo, demoraMs);
    }
}
