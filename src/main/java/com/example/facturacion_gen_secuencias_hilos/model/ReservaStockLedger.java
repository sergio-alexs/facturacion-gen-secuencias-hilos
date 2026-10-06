package com.example.facturacion_gen_secuencias_hilos.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class ReservaStockLedger {

    public record ReservaLote(Long loteId, String codigoLote, int cantidadReservada) {}

    public record ResultadoReserva(int cantidadSolicitada, int cantidadFacturada, List<ReservaLote> lotes) {}

    private final Map<Long, Integer> reservadoPorLote = new ConcurrentHashMap<>();
    private final Map<Long, ReentrantLock> locksPorProducto = new ConcurrentHashMap<>();

    public ResultadoReserva reservar(Long productoId, int cantidadSolicitada, List<InventarioLote> lotesFefo) {
        ReentrantLock lock = locksPorProducto.computeIfAbsent(productoId, k -> new ReentrantLock());
        lock.lock();
        try {
            int restante = cantidadSolicitada;
            List<ReservaLote> reservas = new ArrayList<>();

            for (InventarioLote lote : lotesFefo) {
                if (restante <= 0) {
                    break;
                }
                int yaReservado = reservadoPorLote.getOrDefault(lote.getId(), 0);
                int disponibleEfectivo = lote.getCantidadDisponible() - yaReservado;

                if (disponibleEfectivo > 0) {
                    int aTomar = Math.min(restante, disponibleEfectivo);
                    reservadoPorLote.put(lote.getId(), yaReservado + aTomar);
                    reservas.add(new ReservaLote(lote.getId(), lote.getCodigoLote(), aTomar));
                    restante -= aTomar;
                }
            }

            int cantidadFacturada = cantidadSolicitada - restante;
            return new ResultadoReserva(cantidadSolicitada, cantidadFacturada, reservas);
        } finally {
            lock.unlock();
        }
    }
}
