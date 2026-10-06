package com.example.facturacion_gen_secuencias_hilos.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Table("factura_detalle_comentario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DetalleComentario {

    @Id
    private Long id;
    private Integer orden;
    private String comentario;
    private Instant fecha;
}
