package com.example.facturacion_gen_secuencias_hilos;

import com.example.facturacion_gen_secuencias_hilos.dto.*;
import com.example.facturacion_gen_secuencias_hilos.exception.ClienteBloqueadoException;
import com.example.facturacion_gen_secuencias_hilos.model.InventarioLote;
import com.example.facturacion_gen_secuencias_hilos.model.Producto;
import com.example.facturacion_gen_secuencias_hilos.repository.InventarioLoteRepository;
import com.example.facturacion_gen_secuencias_hilos.repository.ProductoRepository;
import com.example.facturacion_gen_secuencias_hilos.service.FacturacionOrquestador;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FacturacionFlujoIntegrationTest {

    @Autowired
    private FacturacionOrquestador orquestador;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private InventarioLoteRepository loteRepository;

    @Test
    @DisplayName("Debe procesar factura exitosa para cliente habilitado, asignar secuencia real y estado PROCESADA")
    void testFacturaConStockExitosoYRecalculoCorrecto() {
        // Buscar un producto que tenga lotes disponibles
        Producto producto = productoRepository.findAll().stream()
                .filter(p -> !loteRepository.findDisponiblesFefo(p.getId()).isEmpty())
                .findFirst()
                .orElseThrow();

        ClienteRequest clienteReq = new ClienteRequest(
                "ID-HABILITADO-" + UUID.randomUUID().toString().substring(0, 8),
                "Cliente Habilitado",
                "test@test.com",
                "3001234567",
                "Calle 123"
        );

        DetalleRequest detalleReq = new DetalleRequest(
                producto.getId(),
                1,
                List.of("Comentario de prueba")
        );

        FacturaRequest request = new FacturaRequest(clienteReq, List.of(detalleReq));

        FacturaResultado resultado = orquestador.procesarFactura(request, Escenario.CON_ESPERA, Modo.HILOS);

        assertNotNull(resultado);
        assertNotNull(resultado.facturaId());
        assertTrue(resultado.facturaId() > 0);
        assertEquals("PROCESADA", resultado.estado());
        assertEquals(1, resultado.detallesFacturados());
        assertEquals(0, resultado.detallesNoFacturados());

        BigDecimal subtotalEsperado = producto.getPrecioUnitario().setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalEsperado = subtotalEsperado.multiply(new BigDecimal("1.19")).setScale(2, RoundingMode.HALF_UP);
        assertEquals(subtotalEsperado, resultado.subtotal());
        assertEquals(totalEsperado, resultado.total());

        // Verificar reemplazo de secuencias reales
        assertTrue(resultado.metricas().mapeoTemporalReal().containsKey("FACTURA"));
        assertTrue(resultado.metricas().mapeoTemporalReal().containsKey("FACTURA_DETALLE"));
    }

    @Test
    @DisplayName("Debe confirmar detalle sin stock, marcar NO_FACTURADO y recalcular encabezado a CON_ITEMS_PENDIENTES")
    void testFacturaConItemSinStockRecalculaEncabezado() {
        Producto prodConStock = productoRepository.findAll().stream()
                .filter(p -> !loteRepository.findDisponiblesFefo(p.getId()).isEmpty())
                .findFirst()
                .orElseThrow();

        Producto prodSinStock = productoRepository.findAll().stream()
                .filter(p -> loteRepository.findDisponiblesFefo(p.getId()).isEmpty())
                .findFirst()
                .orElseThrow();

        ClienteRequest clienteReq = new ClienteRequest(
                "ID-HABILITADO-" + UUID.randomUUID().toString().substring(0, 8),
                "Cliente Parcial",
                "parcial@test.com",
                "3001234567",
                "Carrera 45"
        );

        DetalleRequest itemConStock = new DetalleRequest(prodConStock.getId(), 1, List.of());
        DetalleRequest itemSinStock = new DetalleRequest(prodSinStock.getId(), 2, List.of()); // Sin stock

        FacturaRequest request = new FacturaRequest(clienteReq, List.of(itemConStock, itemSinStock));

        FacturaResultado resultado = orquestador.procesarFactura(request, Escenario.CON_ESPERA, Modo.HILOS);

        assertNotNull(resultado);
        assertEquals("CON_ITEMS_PENDIENTES", resultado.estado());
        assertEquals(1, resultado.detallesFacturados());
        assertEquals(1, resultado.detallesNoFacturados());

        // El subtotal debe reflejar ÚNICAMENTE el ítem con stock confirmado
        BigDecimal subtotalEsperado = prodConStock.getPrecioUnitario().setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalEsperado = subtotalEsperado.multiply(new BigDecimal("1.19")).setScale(2, RoundingMode.HALF_UP);
        assertEquals(subtotalEsperado, resultado.subtotal());
        assertEquals(totalEsperado, resultado.total());

        // Verificar estados de cada detalle
        assertEquals("FACTURADO", resultado.detalles().get(0).estado());
        assertEquals("NO_FACTURADO", resultado.detalles().get(1).estado());
        assertEquals("SIN_STOCK", resultado.detalles().get(1).motivo());
    }

    @Test
    @DisplayName("Debe recalcular encabezado a SIN_ITEMS_FACTURADOS y totales en cero si ningún ítem tiene stock")
    void testFacturaTodosSinStockRecalculaEncabezadoASinItems() {
        Producto prodSinStock = productoRepository.findAll().stream()
                .filter(p -> loteRepository.findDisponiblesFefo(p.getId()).isEmpty())
                .findFirst()
                .orElseThrow();

        ClienteRequest clienteReq = new ClienteRequest(
                "ID-HABILITADO-" + UUID.randomUUID().toString().substring(0, 8),
                "Cliente Sin Stock",
                "sinstock@test.com",
                "3001234567",
                "Diagonal 5"
        );

        DetalleRequest itemSinStock = new DetalleRequest(prodSinStock.getId(), 1, List.of());

        FacturaRequest request = new FacturaRequest(clienteReq, List.of(itemSinStock));

        FacturaResultado resultado = orquestador.procesarFactura(request, Escenario.CON_ESPERA, Modo.HILOS);

        assertNotNull(resultado);
        assertEquals("SIN_ITEMS_FACTURADOS", resultado.estado());
        assertEquals(0, resultado.detallesFacturados());
        assertEquals(1, resultado.detallesNoFacturados());

        assertEquals(new BigDecimal("0.00"), resultado.subtotal());
        assertEquals(new BigDecimal("0.00"), resultado.total());
    }

    @Test
    @DisplayName("Debe abortar facturación si el cliente está bloqueado y revertir el stock descontado en paralelo")
    void testClienteBloqueadoRechazaFacturaYRevierteStock() {
        Producto prodConStock = productoRepository.findAll().stream()
                .filter(p -> !loteRepository.findDisponiblesFefo(p.getId()).isEmpty())
                .findFirst()
                .orElseThrow();

        List<InventarioLote> lotesAntes = loteRepository.findDisponiblesFefo(prodConStock.getId());
        int stockTotalAntes = lotesAntes.stream().mapToInt(InventarioLote::getCantidadDisponible).sum();

        // Cliente con identificación explícita de BLOQUEADO
        ClienteRequest clienteBloqueadoReq = new ClienteRequest(
                "ID-BLOQUEADO-" + UUID.randomUUID().toString().substring(0, 8),
                "Cliente Reportado",
                "bloqueado@riesgo.com",
                "3009998877",
                "Calle 0 #0-0"
        );

        DetalleRequest detalleReq = new DetalleRequest(prodConStock.getId(), 2, List.of());
        FacturaRequest request = new FacturaRequest(clienteBloqueadoReq, List.of(detalleReq));

        // Debe lanzar ClienteBloqueadoException
        ClienteBloqueadoException ex = assertThrows(
                ClienteBloqueadoException.class,
                () -> orquestador.procesarFactura(request, Escenario.CON_ESPERA, Modo.HILOS)
        );

        assertTrue(ex.getMessage().contains("reportado") || ex.getMessage().contains("bloqueado"));
        assertEquals(clienteBloqueadoReq.identificacion(), ex.getIdentificacion());

        // Verificar que el inventario en base de datos quedó intacto (el descuento es 100% transaccional al final)
        List<InventarioLote> lotesDespues = loteRepository.findDisponiblesFefo(prodConStock.getId());
        int stockTotalDespues = lotesDespues.stream().mapToInt(InventarioLote::getCantidadDisponible).sum();

        assertEquals(stockTotalAntes, stockTotalDespues, "El inventario debe permanecer intacto");
    }
}

