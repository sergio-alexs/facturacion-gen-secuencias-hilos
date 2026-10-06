package com.example.facturacion_gen_secuencias_hilos.secuencia;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class ContextoSecuencias implements SecuenciaListener {

    private final SecuenciaService secuenciaService;
    private final Map<NombreSecuencia, AtomicLong> contadores = new EnumMap<>(NombreSecuencia.class);
    private final Map<ClaveTemporal, Long> reales = new ConcurrentHashMap<>();

    private final ReentrantLock lock = new ReentrantLock();
    private final Condition todasGeneradas = lock.newCondition();

    private int pendientes = 0;
    private int totalSolicitadas = 0;
    private boolean cerrado = false;
    private Throwable error = null;

    @Getter
    private long tiempoEsperaMs = 0;
    @Getter
    private int pendientesAlCerrar = 0;

    public ContextoSecuencias(SecuenciaService secuenciaService) {
        this.secuenciaService = secuenciaService;
        for (NombreSecuencia nombre : NombreSecuencia.values()) {
            contadores.put(nombre, new AtomicLong(0));
        }
    }

    /**
     * Registra una solicitud de secuencia temporal y dispara su obtención asíncrona.
     * @return El tempId numérico asignado (1, 2, 3...)
     */
    public long registrar(NombreSecuencia nombre) {
        long tempId = contadores.get(nombre).incrementAndGet();

        lock.lock();
        try {
            if (cerrado) {
                throw new IllegalStateException("No se pueden registrar más secuencias en un contexto cerrado");
            }
            pendientes++;
            totalSolicitadas++;
        } finally { lock.unlock(); }

        secuenciaService.solicitar(nombre, tempId, this);
        return tempId;
    }

    @Override
    public void onGenerada(NombreSecuencia nombre, long tempId, long valorReal) {
        reales.put(new ClaveTemporal(nombre, tempId), valorReal);

        lock.lock();
        try {
            pendientes--;
            if (pendientes <= 0 && cerrado) {
                todasGeneradas.signalAll();
            }
        } finally { lock.unlock(); }
    }

    @Override
    public void onError(NombreSecuencia nombre, long tempId, Throwable error) {
        log.error("Fallo al generar secuencia {} para tempId {}", nombre, tempId, error);
        lock.lock();
        try {
            this.error = error;
            pendientes--;
            todasGeneradas.signalAll();
        } finally { lock.unlock(); }
    }

    /**
     * El hilo principal llama a este método tras haber terminado el procesamiento de los detalles.
     * Cierra el registro de nuevas secuencias y espera a que todas las solicitadas terminen de generarse.
     */
    public void cerrarYEsperar(long timeoutMs) {
        long inicio = System.currentTimeMillis();

        lock.lock();
        try {
            this.cerrado = true;
            this.pendientesAlCerrar = this.pendientes;

            while (this.pendientes > 0 && this.error == null) {
                boolean fueNotificado = todasGeneradas.await(timeoutMs, TimeUnit.MILLISECONDS);
                if (!fueNotificado && this.pendientes > 0) {
                    throw new RuntimeException("Timeout de " + timeoutMs + "ms esperando secuencias (" + pendientes + " pendientes)");
                }
            }

            if (this.error != null) {
                throw new RuntimeException("Fallo durante la generación asíncrona de secuencias", this.error);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Hilo principal interrumpido mientras esperaba secuencias", e);
        } finally {
            this.tiempoEsperaMs = System.currentTimeMillis() - inicio;
            lock.unlock();
        }
    }

    /**
     * Obtiene el valor real de la secuencia dado su nombre y tempId.
     */
    public long real(NombreSecuencia nombre, long tempId) {
        Long val = reales.get(new ClaveTemporal(nombre, tempId));
        if (val == null) {
            throw new IllegalArgumentException("No existe secuencia real generada para " + nombre + " con tempId " + tempId);
        }
        return val;
    }

    public int getTotalSolicitadas() {
        lock.lock();
        try {
            return totalSolicitadas;
        } finally { lock.unlock(); }
    }

    public Map<ClaveTemporal, Long> getMapaReales() {
        return Map.copyOf(reales);
    }
}
