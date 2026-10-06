package com.example.facturacion_gen_secuencias_hilos.repository;

import com.example.facturacion_gen_secuencias_hilos.model.InventarioLote;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface InventarioLoteRepository extends ListCrudRepository<InventarioLote, Long> {

    @Query("SELECT * FROM inventario_lote WHERE producto_id = :productoId AND cantidad_disponible > 0 ORDER BY fecha_vencimiento ASC, id ASC")
    List<InventarioLote> findDisponiblesFefo(@Param("productoId") Long productoId);

    @Query("SELECT * FROM inventario_lote WHERE producto_id = :productoId ORDER BY fecha_vencimiento ASC, id ASC")
    List<InventarioLote> findByProductoId(@Param("productoId") Long productoId);


    @Modifying
    @Query("UPDATE inventario_lote SET cantidad_disponible = cantidad_disponible - :cantidad WHERE id = :loteId AND cantidad_disponible >= :cantidad")
    int descontarStock(@Param("loteId") Long loteId, @Param("cantidad") int cantidad);

    @Modifying
    @Query("UPDATE inventario_lote SET cantidad_disponible = cantidad_disponible + :cantidad WHERE id = :loteId")
    int sumarStock(@Param("loteId") Long loteId, @Param("cantidad") int cantidad);
}
