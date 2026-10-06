package com.example.facturacion_gen_secuencias_hilos.service;

import com.example.facturacion_gen_secuencias_hilos.dto.*;
import com.example.facturacion_gen_secuencias_hilos.model.*;
import com.example.facturacion_gen_secuencias_hilos.repository.ClienteRepository;
import com.example.facturacion_gen_secuencias_hilos.repository.InventarioLoteRepository;
import com.example.facturacion_gen_secuencias_hilos.repository.MovimientoInventarioRepository;
import com.example.facturacion_gen_secuencias_hilos.secuencia.ContextoSecuencias;
import com.example.facturacion_gen_secuencias_hilos.secuencia.NombreSecuencia;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FacturaPersistenciaService {

    private final ClienteRepository clienteRepository;
    private final InventarioLoteRepository loteRepository;
    private final MovimientoInventarioRepository movimientoRepository;
    private final JdbcAggregateTemplate aggregateTemplate;

    public FacturaPersistenciaService(ClienteRepository clienteRepository,
                                       InventarioLoteRepository loteRepository,
                                       MovimientoInventarioRepository movimientoRepository,
                                       JdbcAggregateTemplate aggregateTemplate) {
        this.clienteRepository = clienteRepository;
        this.loteRepository = loteRepository;
        this.movimientoRepository = movimientoRepository;
        this.aggregateTemplate = aggregateTemplate;
    }

    @Transactional
    public Factura guardarFactura(FacturaRequest request,
                                  List<DetalleProcesado> detallesProcesados,
                                  ContextoSecuencias ctx,
                                  long tempIdFactura,
                                  Long tempIdCliente) {
        Instant ahora = Instant.now();

        // 1. Cliente: Buscar o Crear
        Cliente cliente;
        Optional<Cliente> optCliente = clienteRepository.findByIdentificacion(request.cliente().identificacion());
        if (optCliente.isPresent()) {
            cliente = optCliente.get();
        } else {
            long idRealCliente = ctx.real(NombreSecuencia.CLIENTE, tempIdCliente);
            cliente = new Cliente(
                    idRealCliente,
                    request.cliente().identificacion(),
                    request.cliente().nombre(),
                    request.cliente().email(),
                    request.cliente().telefono(),
                    request.cliente().direccion(),
                    ahora
            );
            aggregateTemplate.insert(cliente);
        }

        // 2. Descuento Atómico Transaccional de Stock y Construcción de Entidades con IDs Reales
        long idRealFactura = ctx.real(NombreSecuencia.FACTURA, tempIdFactura);
        List<FacturaDetalle> facturaDetallesEntidades = new ArrayList<>();
        List<MovimientoInventario> movimientosAInsertar = new ArrayList<>();

        BigDecimal subtotalAcumulado = BigDecimal.ZERO;
        int contFacturados = 0;
        int contParciales = 0;
        int contNoFacturados = 0;

        int lineaIndex = 0;
        for (DetalleProcesado dp : detallesProcesados) {
            long idRealDetalle = ctx.real(NombreSecuencia.FACTURA_DETALLE, dp.tempIdDetalle());

            List<DetalleLote> lotesEntidad = new ArrayList<>();
            int cantidadEfectivaFacturada = 0;
            int ordenLote = 0;

            for (LoteAsignado la : dp.lotes()) {
                int aDescontarReal = 0;
                if (la.cantidad() > 0) {
                    // Actualización atómica condicional directa dentro de la transacción
                    int filas = loteRepository.descontarStock(la.loteId(), la.cantidad());
                    if (filas > 0) {
                        aDescontarReal = la.cantidad();
                    } else {
                        // Concurrencia inter-transaccional redujo el stock: verificar remanente disponible
                        InventarioLote loteDb = loteRepository.findById(la.loteId()).orElse(null);
                        int remanente = (loteDb != null) ? Math.max(0, loteDb.getCantidadDisponible()) : 0;
                        if (remanente > 0) {
                            int filasParcial = loteRepository.descontarStock(la.loteId(), remanente);
                            if (filasParcial > 0) {
                                aDescontarReal = remanente;
                            }
                        }
                    }
                }

                if (aDescontarReal > 0) {
                    cantidadEfectivaFacturada += aDescontarReal;

                    long idRealDetalleLote = ctx.real(NombreSecuencia.DETALLE_LOTE, la.tempIdDetalleLote());
                    lotesEntidad.add(new DetalleLote(idRealDetalleLote, ordenLote++, la.loteId(), la.codigoLote(), aDescontarReal));

                    long idRealMovimiento = ctx.real(NombreSecuencia.MOVIMIENTO_INVENTARIO, la.tempIdMovimiento());
                    movimientosAInsertar.add(new MovimientoInventario(
                            idRealMovimiento,
                            la.loteId(),
                            dp.productoId(),
                            idRealDetalleLote,
                            null,
                            "SALIDA_VENTA",
                            aDescontarReal,
                            ahora
                    ));
                } else {
                    log.warn("Lote ID {} sin stock disponible en BD durante la transacción para producto ID {}",
                            la.loteId(), dp.productoId());
                }
            }

            // Comentarios por detalle
            List<DetalleComentario> comentariosEntidad = new ArrayList<>();
            int ordenComentario = 0;
            for (ComentarioProcesado cp : dp.comentarios()) {
                long idRealComentario = ctx.real(NombreSecuencia.DETALLE_COMENTARIO, cp.tempIdComentario());
                comentariosEntidad.add(new DetalleComentario(idRealComentario, ordenComentario++, cp.comentario(), ahora));
            }

            // Determinar estado y totales reales basados en lo efectivamente descontado en BD
            String estadoReal;
            String motivoReal = null;
            if (cantidadEfectivaFacturada == dp.cantidadSolicitada()) {
                estadoReal = "FACTURADO";
                contFacturados++;
            } else if (cantidadEfectivaFacturada > 0) {
                estadoReal = "FACTURADO_PARCIAL";
                motivoReal = "STOCK_PARCIAL";
                contParciales++;
            } else {
                estadoReal = "NO_FACTURADO";
                motivoReal = "SIN_STOCK";
                contNoFacturados++;
            }

            BigDecimal totalLineaReal = dp.precioUnitario().multiply(BigDecimal.valueOf(cantidadEfectivaFacturada));
            if (!"NO_FACTURADO".equals(estadoReal)) {
                subtotalAcumulado = subtotalAcumulado.add(totalLineaReal);
            }

            FacturaDetalle detalleEntidad = new FacturaDetalle(
                    idRealDetalle,
                    lineaIndex++,
                    dp.productoId(),
                    dp.cantidadSolicitada(),
                    cantidadEfectivaFacturada,
                    dp.precioUnitario(),
                    totalLineaReal,
                    estadoReal,
                    motivoReal,
                    lotesEntidad,
                    comentariosEntidad
            );

            facturaDetallesEntidades.add(detalleEntidad);
        }

        // 3. Totales de Factura (IVA 19%) calculados estrictamente sobre lo efectivamente facturado
        BigDecimal subtotal = subtotalAcumulado.setScale(2, RoundingMode.HALF_UP);
        BigDecimal impuesto = subtotal.multiply(new BigDecimal("0.19")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(impuesto).setScale(2, RoundingMode.HALF_UP);

        String estadoFactura;
        if (contFacturados == detallesProcesados.size()) {
            estadoFactura = "PROCESADA";
        } else if (contFacturados > 0 || contParciales > 0) {
            estadoFactura = "CON_ITEMS_PENDIENTES";
        } else {
            estadoFactura = "SIN_ITEMS_FACTURADOS";
        }

        String numeroFactura = "F-TECH-" + String.format("%07d", idRealFactura);

        Factura factura = new Factura(
                idRealFactura,
                numeroFactura,
                cliente.getId(),
                ahora,
                estadoFactura,
                subtotal,
                impuesto,
                total,
                contFacturados,
                contParciales,
                contNoFacturados,
                facturaDetallesEntidades
        );

        // 4. Inserción atómica del Agregado Raíz y de los Movimientos bajo la misma transacción
        aggregateTemplate.insert(factura);
        for (MovimientoInventario mov : movimientosAInsertar) {
            aggregateTemplate.insert(mov);
        }

        return factura;
    }
}
