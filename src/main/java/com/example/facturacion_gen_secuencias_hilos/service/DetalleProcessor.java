package com.example.facturacion_gen_secuencias_hilos.service;

import com.example.facturacion_gen_secuencias_hilos.config.SimulacionProperties;
import com.example.facturacion_gen_secuencias_hilos.dto.*;
import com.example.facturacion_gen_secuencias_hilos.model.InventarioLote;
import com.example.facturacion_gen_secuencias_hilos.model.Producto;
import com.example.facturacion_gen_secuencias_hilos.model.ReservaStockLedger;
import com.example.facturacion_gen_secuencias_hilos.repository.InventarioLoteRepository;
import com.example.facturacion_gen_secuencias_hilos.repository.ProductoRepository;
import com.example.facturacion_gen_secuencias_hilos.secuencia.ContextoSecuencias;
import com.example.facturacion_gen_secuencias_hilos.secuencia.NombreSecuencia;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

@Component
@Slf4j
public class DetalleProcessor {

    private final ProductoRepository productoRepository;
    private final InventarioLoteRepository loteRepository;
    private final Semaphore concurrenciaDetallesPermit;

    public DetalleProcessor(ProductoRepository productoRepository,
                            InventarioLoteRepository loteRepository,
                            SimulacionProperties properties) {
        this.productoRepository = productoRepository;
        this.loteRepository = loteRepository;
        int max = properties.getMaxDetallesConcurrentes() > 0 ? properties.getMaxDetallesConcurrentes() : 10;
        this.concurrenciaDetallesPermit = new Semaphore(max, true);
    }

    public DetalleProcesado procesarDetalle(DetalleRequest req,
                                            ContextoSecuencias ctx,
                                            ReservaStockLedger ledger,
                                            long demoraValidacionMs) {
        try {
            concurrenciaDetallesPermit.acquire();
            try {
                // 1. Asignar secuencias temporales simuladas al inicio y encargar al servicio en segundo plano
                long tempIdDetalle = ctx.registrar(NombreSecuencia.FACTURA_DETALLE);

                List<ComentarioProcesado> comentariosProcesados = new ArrayList<>();
                if (req.comentarios() != null) {
                    int ordenComentario = 0;
                    for (String textoComentario : req.comentarios()) {
                        long tempIdComentario = ctx.registrar(NombreSecuencia.DETALLE_COMENTARIO);
                        comentariosProcesados.add(new ComentarioProcesado(tempIdComentario, ordenComentario++, textoComentario));
                    }
                }

                // Secuencias simuladas especulativas para el primer lote y movimiento (se solicitarán en 2do plano de inmediato)
                long tempIdLoteEspeculativo = ctx.registrar(NombreSecuencia.DETALLE_LOTE);
                long tempIdMovEspeculativo = ctx.registrar(NombreSecuencia.MOVIMIENTO_INVENTARIO);

                // Simulación opcional de demora de validación de negocio
                if (demoraValidacionMs > 0) {
                    try {
                        Thread.sleep(demoraValidacionMs);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Procesamiento de detalle interrumpido", e);
                    }
                }

                Producto producto = productoRepository.findById(req.productoId())
                        .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + req.productoId()));

                // 2. Coordinación de lotes FEFO y reserva provisional en memoria
                List<InventarioLote> lotesFefo = loteRepository.findDisponiblesFefo(req.productoId());
                ReservaStockLedger.ResultadoReserva reserva = ledger.reservar(req.productoId(), req.cantidad(), lotesFefo);

                // 3. Asignación de lotes y secuencias temporales en segundo plano (el descuento real en BD se hará en persistencia transaccional)
                List<LoteAsignado> lotesAsignados = new ArrayList<>();
                List<ReservaStockLedger.ReservaLote> reservas = reserva.lotes();

                for (int i = 0; i < reservas.size(); i++) {
                    ReservaStockLedger.ReservaLote r = reservas.get(i);
                    if (r.cantidadReservada() > 0) {
                        long tempLoteId;
                        long tempMovId;
                        if (lotesAsignados.isEmpty()) {
                            tempLoteId = tempIdLoteEspeculativo;
                            tempMovId = tempIdMovEspeculativo;
                        } else {
                            tempLoteId = ctx.registrar(NombreSecuencia.DETALLE_LOTE);
                            tempMovId = ctx.registrar(NombreSecuencia.MOVIMIENTO_INVENTARIO);
                        }
                        lotesAsignados.add(new LoteAsignado(tempLoteId, tempMovId, r.loteId(), r.codigoLote(), r.cantidadReservada()));
                    }
                }

                int cantidadFacturada = reserva.cantidadFacturada();
                String estado;
                String motivo = null;
                if (cantidadFacturada == req.cantidad()) {
                    estado = "FACTURADO";
                } else if (cantidadFacturada > 0) {
                    estado = "FACTURADO_PARCIAL";
                    motivo = "STOCK_PARCIAL";
                } else {
                    estado = "NO_FACTURADO";
                    motivo = "SIN_STOCK";
                }

                BigDecimal precioUnitario = producto.getPrecioUnitario();
                BigDecimal total = precioUnitario.multiply(BigDecimal.valueOf(cantidadFacturada));

                return new DetalleProcesado(
                        tempIdDetalle,
                        req.productoId(),
                        req.cantidad(),
                        cantidadFacturada,
                        precioUnitario,
                        total,
                        estado,
                        motivo,
                        lotesAsignados,
                        comentariosProcesados
                );
            } finally {
                concurrenciaDetallesPermit.release();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Espera en cola de procesamiento de detalles interrumpida", e);
        }
    }

    public int getPermisosDisponibles() {
        return concurrenciaDetallesPermit.availablePermits();
    }
}

