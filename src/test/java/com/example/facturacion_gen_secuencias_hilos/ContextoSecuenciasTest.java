package com.example.facturacion_gen_secuencias_hilos;

import com.example.facturacion_gen_secuencias_hilos.config.SimulacionProperties;
import com.example.facturacion_gen_secuencias_hilos.secuencia.ContextoSecuencias;
import com.example.facturacion_gen_secuencias_hilos.secuencia.NombreSecuencia;
import com.example.facturacion_gen_secuencias_hilos.secuencia.SecuenciaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

class ContextoSecuenciasTest {

    private SecuenciaService secuenciaServiceMock;

    @BeforeEach
    void setUp() {
        NamedParameterJdbcTemplate jdbcTemplateMock = Mockito.mock(NamedParameterJdbcTemplate.class);
        Mockito.when(jdbcTemplateMock.queryForObject(anyString(), any(org.springframework.jdbc.core.namedparam.MapSqlParameterSource.class), Mockito.eq(Long.class)))
                .thenAnswer(inv -> (long) (Math.random() * 10000 + 100));

        SimulacionProperties properties = new SimulacionProperties();
        properties.setDemoraSecuenciaMs(50);

        secuenciaServiceMock = new SecuenciaService(
                jdbcTemplateMock,
                Executors.newVirtualThreadPerTaskExecutor(),
                properties
        );
    }

    @Test
    @DisplayName("Debe asignar tempIds numéricos ascendentes por cada secuencia")
    void testAsignacionTempIds() {
        ContextoSecuencias ctx = new ContextoSecuencias(secuenciaServiceMock);

        long t1 = ctx.registrar(NombreSecuencia.FACTURA_DETALLE);
        long t2 = ctx.registrar(NombreSecuencia.FACTURA_DETALLE);
        long t3 = ctx.registrar(NombreSecuencia.CLIENTE);

        assertEquals(1, t1);
        assertEquals(2, t2);
        assertEquals(1, t3);

        ctx.cerrarYEsperar(5000);

        assertTrue(ctx.real(NombreSecuencia.FACTURA_DETALLE, t1) > 0);
        assertTrue(ctx.real(NombreSecuencia.FACTURA_DETALLE, t2) > 0);
        assertTrue(ctx.real(NombreSecuencia.CLIENTE, t3) > 0);
    }
}
