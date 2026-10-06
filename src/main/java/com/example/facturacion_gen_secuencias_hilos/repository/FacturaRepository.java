package com.example.facturacion_gen_secuencias_hilos.repository;

import com.example.facturacion_gen_secuencias_hilos.model.Factura;
import org.springframework.data.repository.ListCrudRepository;

public interface FacturaRepository extends ListCrudRepository<Factura, Long> {
}
