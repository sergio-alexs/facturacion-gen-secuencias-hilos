package com.example.facturacion_gen_secuencias_hilos.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Table("reabastecimiento")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Reabastecimiento {

    @Id
    private Long id;
    private Instant fecha;
    private String observacion;
}
