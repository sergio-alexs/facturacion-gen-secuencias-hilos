package com.example.facturacion_gen_secuencias_hilos.service;

import com.example.facturacion_gen_secuencias_hilos.config.SimulacionProperties;
import com.example.facturacion_gen_secuencias_hilos.dto.*;
import com.example.facturacion_gen_secuencias_hilos.exception.ClienteBloqueadoException;
import com.example.facturacion_gen_secuencias_hilos.model.Cliente;
import com.example.facturacion_gen_secuencias_hilos.model.Factura;
import com.example.facturacion_gen_secuencias_hilos.model.ReservaStockLedger;
import com.example.facturacion_gen_secuencias_hilos.repository.ClienteRepository;
import com.example.facturacion_gen_secuencias_hilos.repository.InventarioLoteRepository;
import com.example.facturacion_gen_secuencias_hilos.secuencia.ContextoSecuencias;
import com.example.facturacion_gen_secuencias_hilos.secuencia.NombreSecuencia;
import com.example.facturacion_gen_secuencias_hilos.secuencia.SecuenciaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Component
@Slf4j
public class FacturacionOrquestador {

    private final SecuenciaService secuenciaService;
    private final DetalleProcessor detalleProcessor;
    private final FacturaPersistenciaService persistenciaService;
    private final ClienteRepository clienteRepository;
    private final ClienteValidacionService clienteValidacionService;
    private final ExecutorService detallesExecutor;
    private final ExecutorService clienteExecutor;
    private final SimulacionProperties properties;

    public FacturacionOrquestador(SecuenciaService secuenciaService,
                                 DetalleProcessor detalleProcessor,
                                 FacturaPersistenciaService persistenciaService,
                                 ClienteRepository clienteRepository,
                                 ClienteValidacionService clienteValidacionService,
                                 @Qualifier("detallesExecutor") ExecutorService detallesExecutor,
                                 @Qualifier("clienteExecutor") ExecutorService clienteExecutor,
                                 SimulacionProperties properties) {
        this.secuenciaService = secuenciaService;
        this.detalleProcessor = detalleProcessor;
        this.persistenciaService = persistenciaService;
        this.clienteRepository = clienteRepository;
        this.clienteValidacionService = clienteValidacionService;
        this.detallesExecutor = detallesExecutor;
        this.clienteExecutor = clienteExecutor;
        this.properties = properties;
    }

    public FacturaResultado procesarFactura(FacturaRequest request, Escenario escenario, Modo modo) {
        long inicioGlobal = System.currentTimeMillis();

        if (modo == Modo.SECUENCIAL) {
            return procesarModoSecuencial(request, escenario, inicioGlobal);
        } else {
            return procesarModoHilosVirtuales(request, escenario, inicioGlobal);
        }
    }

    private FacturaResultado procesarModoHilosVirtuales(FacturaRequest request, Escenario escenario, long inicioGlobal) {
        // 0. Disparar validación de cliente en 2do plano (Virtual Thread) de 300ms a 2s
        CompletableFuture<ClienteValidacionService.ResultadoValidacionCliente> futureCliente = CompletableFuture.supplyAsync(
                () -> clienteValidacionService.validarCliente(request.cliente().identificacion()),
                clienteExecutor
        );

        ContextoSecuencias ctx = new ContextoSecuencias(secuenciaService);
        ReservaStockLedger ledger = new ReservaStockLedger();

        // 1. Asignar secuencia simulada para Factura y solicitar en 2do plano de inmediato
        long tempIdFactura = ctx.registrar(NombreSecuencia.FACTURA);

        // 2. Cliente: Asignar secuencia simulada si es nuevo y solicitar en 2do plano
        Long tempIdCliente = null;
        Optional<Cliente> optCliente = clienteRepository.findByIdentificacion(request.cliente().identificacion());
        if (optCliente.isEmpty()) {
            tempIdCliente = ctx.registrar(NombreSecuencia.CLIENTE);
        }

        long inicioDetalles = System.currentTimeMillis();

        // 3. Procesar asignación de detalles en paralelo (Virtual Threads) mientras corren secuencias y cliente en 2do plano
        List<CompletableFuture<DetalleProcesado>> futures = new ArrayList<>();
        for (DetalleRequest dReq : request.detalles()) {
            long demoraValidacionMs = calcularDemoraValidacion(escenario);
            CompletableFuture<DetalleProcesado> future = CompletableFuture.supplyAsync(
                    () -> detalleProcessor.procesarDetalle(dReq, ctx, ledger, demoraValidacionMs),
                    detallesExecutor
            );
            futures.add(future);
        }

        List<DetalleProcesado> detallesProcesados = futures.stream()
                .map(CompletableFuture::join)
                .toList();

        long tiempoProcesamientoDetallesMs = System.currentTimeMillis() - inicioDetalles;

        // 4. Punto de verificación: Verificar que no existan secuencias pendientes en 2do plano
        ctx.cerrarYEsperar(15000);
        long tiempoEsperaSecuenciasMs = ctx.getTiempoEsperaMs();

        // 5. Esperar la validación del cliente antes de insertar. Si está reportado, abortar (el stock en BD no ha sido tocado)
        ClienteValidacionService.ResultadoValidacionCliente resCliente = futureCliente.join();
        if (resCliente.bloqueado()) {
            log.warn("Facturación rechazada: El cliente [{}] está bloqueado/reportado: {}.",
                    request.cliente().identificacion(), resCliente.motivo());
            throw new ClienteBloqueadoException(
                    request.cliente().identificacion(),
                    "Facturación rechazada: El cliente con identificación "
                    + request.cliente().identificacion() + " se encuentra reportado/bloqueado. Motivo: "
                    + resCliente.motivo());
        }

        // 6. Persistencia transaccional: Descuento atómico de stock en BD, recálculo de encabezado e inserción
        long inicioInsercion = System.currentTimeMillis();
        Factura facturaGuardada = persistenciaService.guardarFactura(
                request,
                detallesProcesados,
                ctx,
                tempIdFactura,
                tempIdCliente
        );
        long tiempoInsercionMs = System.currentTimeMillis() - inicioInsercion;

        long tiempoTotalMs = System.currentTimeMillis() - inicioGlobal;
        Cliente clienteEntity = clienteRepository.findById(facturaGuardada.getClienteId()).orElse(null);

        MetricasSimulacion metricas = new MetricasSimulacion(
                escenario.name(),
                Modo.HILOS.name(),
                tiempoTotalMs,
                tiempoProcesamientoDetallesMs,
                tiempoEsperaSecuenciasMs,
                ctx.getTotalSolicitadas(),
                ctx.getPendientesAlCerrar(),
                tiempoInsercionMs,
                armarMapaMapeoLog(ctx)
        );

        return new FacturaResultado(
                facturaGuardada.getId(),
                facturaGuardada.getNumero(),
                clienteEntity,
                facturaGuardada.getEstado(),
                facturaGuardada.getSubtotal(),
                facturaGuardada.getImpuesto(),
                facturaGuardada.getTotal(),
                facturaGuardada.getDetallesFacturados(),
                facturaGuardada.getDetallesParciales(),
                facturaGuardada.getDetallesNoFacturados(),
                detallesProcesados,
                metricas
        );
    }

    private FacturaResultado procesarModoSecuencial(FacturaRequest request, Escenario escenario, long inicioGlobal) {
        log.info("Ejecutando simulación en Modo SECUENCIAL (simulando arquitectura actual)...");
        ContextoSecuencias ctx = new ContextoSecuencias(secuenciaService);
        ReservaStockLedger ledger = new ReservaStockLedger();

        long tempIdFactura = ctx.registrar(NombreSecuencia.FACTURA);

        Long tempIdCliente = null;
        if (clienteRepository.findByIdentificacion(request.cliente().identificacion()).isEmpty()) {
            tempIdCliente = ctx.registrar(NombreSecuencia.CLIENTE);
        }

        long inicioDetalles = System.currentTimeMillis();
        List<DetalleProcesado> detallesProcesados = new ArrayList<>();

        for (DetalleRequest dReq : request.detalles()) {
            long demoraValidacionMs = calcularDemoraValidacion(escenario);
            DetalleProcesado dp = detalleProcessor.procesarDetalle(dReq, ctx, ledger, demoraValidacionMs);
            detallesProcesados.add(dp);
        }

        long tiempoProcesamientoDetallesMs = System.currentTimeMillis() - inicioDetalles;

        // Punto de verificación y espera de secuencias
        ctx.cerrarYEsperar(30000);
        long tiempoEsperaSecuenciasMs = ctx.getTiempoEsperaMs();

        // Validación secuencial de cliente antes de insertar
        ClienteValidacionService.ResultadoValidacionCliente resCliente = clienteValidacionService.validarCliente(request.cliente().identificacion());
        if (resCliente.bloqueado()) {
            log.warn("Facturación rechazada en modo secuencial: El cliente [{}] está bloqueado/reportado: {}.",
                    request.cliente().identificacion(), resCliente.motivo());
            throw new ClienteBloqueadoException(
                    request.cliente().identificacion(),
                    "Facturación rechazada: El cliente con identificación "
                    + request.cliente().identificacion() + " se encuentra reportado/bloqueado. Motivo: "
                    + resCliente.motivo());
        }

        long inicioInsercion = System.currentTimeMillis();
        Factura facturaGuardada = persistenciaService.guardarFactura(
                request,
                detallesProcesados,
                ctx,
                tempIdFactura,
                tempIdCliente
        );
        long tiempoInsercionMs = System.currentTimeMillis() - inicioInsercion;

        long tiempoTotalMs = System.currentTimeMillis() - inicioGlobal;
        Cliente clienteEntity = clienteRepository.findById(facturaGuardada.getClienteId()).orElse(null);

        MetricasSimulacion metricas = new MetricasSimulacion(
                escenario.name(),
                Modo.SECUENCIAL.name(),
                tiempoTotalMs,
                tiempoProcesamientoDetallesMs,
                tiempoEsperaSecuenciasMs,
                ctx.getTotalSolicitadas(),
                ctx.getPendientesAlCerrar(),
                tiempoInsercionMs,
                armarMapaMapeoLog(ctx)
        );

        return new FacturaResultado(
                facturaGuardada.getId(),
                facturaGuardada.getNumero(),
                clienteEntity,
                facturaGuardada.getEstado(),
                facturaGuardada.getSubtotal(),
                facturaGuardada.getImpuesto(),
                facturaGuardada.getTotal(),
                facturaGuardada.getDetallesFacturados(),
                facturaGuardada.getDetallesParciales(),
                facturaGuardada.getDetallesNoFacturados(),
                detallesProcesados,
                metricas
        );
    }

    private long calcularDemoraValidacion(Escenario escenario) {
        return switch (escenario) {
            case CON_ESPERA -> 15;
            case SIN_ESPERA -> 400;
            case ALEATORIO -> (long) (20 + Math.random() * 500);
            case MULTI_LOTE -> 350;
        };
    }

    private Map<String, Map<String, Long>> armarMapaMapeoLog(ContextoSecuencias ctx) {
        Map<String, Map<String, Long>> resultado = new HashMap<>();
        ctx.getMapaReales().forEach((clave, valReal) -> {
            resultado.computeIfAbsent(clave.nombre().name(), k -> new HashMap<>())
                    .put("tempId_" + clave.tempId(), valReal);
        });
        return resultado;
    }
}
