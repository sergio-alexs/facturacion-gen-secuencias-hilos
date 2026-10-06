package com.example.facturacion_gen_secuencias_hilos.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Table("cliente")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Cliente {

    @Id
    private Long id;
    private String identificacion;
    private String nombre;
    private String email;
    private String telefono;
    private String direccion;
    private Instant fechaCreacion;
}
