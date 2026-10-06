package com.example.facturacion_gen_secuencias_hilos.service;

import com.example.facturacion_gen_secuencias_hilos.model.*;
import com.example.facturacion_gen_secuencias_hilos.repository.*;
import com.example.facturacion_gen_secuencias_hilos.secuencia.NombreSecuencia;
import com.example.facturacion_gen_secuencias_hilos.secuencia.SecuenciaService;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class ReabastecimientoService {

    private final ProductoRepository productoRepository;
    private final InventarioLoteRepository loteRepository;
    private final ReabastecimientoRepository reabastecimientoRepository;
    private final MovimientoInventarioRepository movimientoRepository;
    private final SecuenciaService secuenciaService;
    private final JdbcAggregateTemplate aggregateTemplate;

    public ReabastecimientoService(ProductoRepository productoRepository,
                                  InventarioLoteRepository loteRepository,
                                  ReabastecimientoRepository reabastecimientoRepository,
                                  MovimientoInventarioRepository movimientoRepository,
                                  SecuenciaService secuenciaService,
                                  JdbcAggregateTemplate aggregateTemplate) {
        this.productoRepository = productoRepository;
        this.loteRepository = loteRepository;
        this.reabastecimientoRepository = reabastecimientoRepository;
        this.movimientoRepository = movimientoRepository;
        this.secuenciaService = secuenciaService;
        this.aggregateTemplate = aggregateTemplate;
    }

    @Transactional
    public Reabastecimiento reabastecerAutomatico(String observacion) {
        List<Producto> productos = productoRepository.findByActivoTrue();
        if (productos.isEmpty()) {
            throw new IllegalStateException("No hay productos en el catálogo para reabastecer");
        }

        long idReabastecimiento = secuenciaService.obtenerAhora(NombreSecuencia.REABASTECIMIENTO);
        Instant ahora = Instant.now();
        Reabastecimiento reab = new Reabastecimiento(idReabastecimiento, ahora, observacion != null ? observacion : "Reabastecimiento automático global");
        aggregateTemplate.insert(reab);

        for (Producto prod : productos) {
            List<InventarioLote> lotes = loteRepository.findDisponiblesFefo(prod.getId());
            int totalDisponible = lotes.stream().mapToInt(InventarioLote::getCantidadDisponible).sum();

            if (totalDisponible < 15) {
                int cantidadNuevas = 30;
                crearNuevoLoteYMovimiento(prod.getId(), cantidadNuevas, idReabastecimiento, ahora);
            }
        }

        return reab;
    }

    @Transactional
    public Reabastecimiento reabastecerProducto(Long productoId, int cantidad, String observacion) {
        Producto prod = productoRepository.findById(productoId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + productoId));

        long idReabastecimiento = secuenciaService.obtenerAhora(NombreSecuencia.REABASTECIMIENTO);
        Instant ahora = Instant.now();
        String obsFinal = observacion != null ? observacion : "Reabastecimiento puntual para producto " + prod.getSku();
        Reabastecimiento reab = new Reabastecimiento(idReabastecimiento, ahora, obsFinal);
        aggregateTemplate.insert(reab);

        crearNuevoLoteYMovimiento(prod.getId(), cantidad, idReabastecimiento, ahora);
        return reab;
    }

    private void crearNuevoLoteYMovimiento(Long productoId, int cantidad, Long reabastecimientoId, Instant ahora) {
        long idLote = secuenciaService.obtenerAhora(NombreSecuencia.LOTE);
        String codLote = "LOT-REAB-" + idLote;
        Instant vence = ahora.plus(365, ChronoUnit.DAYS);

        InventarioLote nuevoLote = new InventarioLote(idLote, productoId, codLote, cantidad, ahora, vence);
        aggregateTemplate.insert(nuevoLote);

        long idMov = secuenciaService.obtenerAhora(NombreSecuencia.MOVIMIENTO_INVENTARIO);
        MovimientoInventario mov = new MovimientoInventario(idMov, idLote, productoId, null, reabastecimientoId, "ENTRADA_REABASTECIMIENTO", cantidad, ahora);
        aggregateTemplate.insert(mov);
    }
}
