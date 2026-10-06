package com.example.facturacion_gen_secuencias_hilos.secuencia;

import com.example.facturacion_gen_secuencias_hilos.config.SimulacionProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;

@Service
@Slf4j
public class SecuenciaService {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final ExecutorService secuenciasExecutor;
    private final SimulacionProperties properties;
    private final Semaphore conexionPoolPermit;

    public SecuenciaService(NamedParameterJdbcTemplate jdbcTemplate,
                            @Qualifier("secuenciasExecutor") ExecutorService secuenciasExecutor,
                            SimulacionProperties properties) {
        this.jdbcTemplate = jdbcTemplate;
        this.secuenciasExecutor = secuenciasExecutor;
        this.properties = properties;
        int max = properties.getMaxSecuenciasConcurrentes() > 0 ? properties.getMaxSecuenciasConcurrentes() : 8;
        this.conexionPoolPermit = new Semaphore(max, true);
    }

    /**
     * Solicita una secuencia de forma asíncrona mediante hilos virtuales.
     * Simula la demora configurada (ej: 300ms) usando Thread.sleep en paralelo.
     * Limita el acceso a la base de datos a máximo 8 hilos al tiempo mediante Semáforo.
     */
    public void solicitar(NombreSecuencia nombre, long tempId, SecuenciaListener listener) {
        secuenciasExecutor.submit(() -> {
            try {
                long demoraMs = properties.getDemoraSecuenciaMs();
                if (demoraMs > 0) {
                    Thread.sleep(demoraMs);
                }

                conexionPoolPermit.acquire();
                long valorReal;
                try {
                    valorReal = consultarNextVal(nombre);
                } finally {
                    conexionPoolPermit.release();
                }

                listener.onGenerada(nombre, tempId, valorReal);
            } catch (Throwable t) {
                log.error("Error al obtener secuencia {} para tempId {}", nombre, tempId, t);
                listener.onError(nombre, tempId, t);
            }
        });
    }

    /**
     * Obtiene una secuencia de forma síncrona en el hilo actual (usado para modo SECUENCIAL).
     */
    public long obtenerAhora(NombreSecuencia nombre) {
        try {
            long demoraMs = properties.getDemoraSecuenciaMs();
            if (demoraMs > 0) {
                Thread.sleep(demoraMs);
            }
            conexionPoolPermit.acquire();
            try {
                return consultarNextVal(nombre);
            } finally {
                conexionPoolPermit.release();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupción al esperar secuencia síncrona", e);
        }
    }

    public int getPermisosDisponibles() {
        return conexionPoolPermit.availablePermits();
    }

    private long consultarNextVal(NombreSecuencia nombre) {
        String sql = "SELECT nextval(:nombreSql)";
        MapSqlParameterSource params = new MapSqlParameterSource("nombreSql", nombre.getNombreSql());
        Long val = jdbcTemplate.queryForObject(sql, params, Long.class);
        if (val == null) {
            throw new IllegalStateException("Secuencia retornó null para " + nombre);
        }
        return val;
    }
}
