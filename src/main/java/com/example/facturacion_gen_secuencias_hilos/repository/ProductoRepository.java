package com.example.facturacion_gen_secuencias_hilos.repository;

import com.example.facturacion_gen_secuencias_hilos.model.Producto;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductoRepository extends ListCrudRepository<Producto, Long> {

    List<Producto> findByActivoTrue();

    @Query("""
        SELECT * FROM producto
        WHERE activo = true
          AND (CAST(:categoria AS VARCHAR) IS NULL OR categoria = :categoria)
          AND (CAST(:pattern AS VARCHAR) IS NULL OR LOWER(nombre) LIKE :pattern OR LOWER(sku) LIKE :pattern)
        ORDER BY id ASC
        LIMIT :limit OFFSET :offset
    """)
    List<Producto> buscarPaginado(
            @Param("categoria") String categoria,
            @Param("pattern") String pattern,
            @Param("limit") int limit,
            @Param("offset") long offset
    );

    @Query("""
        SELECT COUNT(*) FROM producto
        WHERE activo = true
          AND (CAST(:categoria AS VARCHAR) IS NULL OR categoria = :categoria)
          AND (CAST(:pattern AS VARCHAR) IS NULL OR LOWER(nombre) LIKE :pattern OR LOWER(sku) LIKE :pattern)
    """)
    long contarBuscarPaginado(
            @Param("categoria") String categoria,
            @Param("pattern") String pattern
    );
}
