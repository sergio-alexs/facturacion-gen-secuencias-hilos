package com.example.facturacion_gen_secuencias_hilos.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Table("movimiento_inventario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoInventario {

    @Id
    private Long id;
    private Long loteId;
    private Long productoId;
    private Long facturaDetalleLoteId;
    private Long reabastecimientoId;
    private String tipo;
    private Integer cantidad;
    private Instant fecha;
}
