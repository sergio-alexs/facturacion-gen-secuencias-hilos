package com.example.facturacion_gen_secuencias_hilos.repository;

import com.example.facturacion_gen_secuencias_hilos.model.Cliente;
import org.springframework.data.repository.ListCrudRepository;

import java.util.Optional;

public interface ClienteRepository extends ListCrudRepository<Cliente, Long> {

    Optional<Cliente> findByIdentificacion(String identificacion);
}
