package com.example.facturacion_gen_secuencias_hilos.repository;

import com.example.facturacion_gen_secuencias_hilos.model.MovimientoInventario;
import org.springframework.data.repository.ListCrudRepository;

public interface MovimientoInventarioRepository extends ListCrudRepository<MovimientoInventario, Long> {
}
