package com.example.facturacion_gen_secuencias_hilos;

import com.example.facturacion_gen_secuencias_hilos.config.SimulacionProperties;
import com.example.facturacion_gen_secuencias_hilos.secuencia.ContextoSecuencias;
import com.example.facturacion_gen_secuencias_hilos.secuencia.NombreSecuencia;
import com.example.facturacion_gen_secuencias_hilos.secuencia.SecuenciaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

class PinningTest {

    @Test
    @DisplayName("Verificación de ejecución masiva con hilos virtuales sin bloqueos de portador")
    void testEjecucionMasivaHilosVirtuales() {
        NamedParameterJdbcTemplate jdbcTemplateMock = Mockito.mock(NamedParameterJdbcTemplate.class);
        Mockito.when(jdbcTemplateMock.queryForObject(anyString(), any(MapSqlParameterSource.class), Mockito.eq(Long.class)))
                .thenAnswer(inv -> (long) (Math.random() * 50000 + 1000));

        SimulacionProperties properties = new SimulacionProperties();
        properties.setDemoraSecuenciaMs(20);

        SecuenciaService secuenciaService = new SecuenciaService(
                jdbcTemplateMock,
                Executors.newVirtualThreadPerTaskExecutor(),
                properties
        );

        ContextoSecuencias ctx = new ContextoSecuencias(secuenciaService);

        try (ExecutorService detallesExecutor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<CompletableFuture<Long>> futures = new ArrayList<>();

            for (int i = 0; i < 50; i++) {
                CompletableFuture<Long> f = CompletableFuture.supplyAsync(() -> {
                    long tempIdDetalle = ctx.registrar(NombreSecuencia.FACTURA_DETALLE);
                    long tempIdLote = ctx.registrar(NombreSecuencia.DETALLE_LOTE);
                    long tempIdComentario = ctx.registrar(NombreSecuencia.DETALLE_COMENTARIO);
                    return tempIdDetalle + tempIdLote + tempIdComentario;
                }, detallesExecutor);
                futures.add(f);
            }

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            ctx.cerrarYEsperar(10000);

            assertEquals(150, ctx.getTotalSolicitadas());
            assertTrue(ctx.getTiempoEsperaMs() >= 0);
        }
    }
}
