package com.example.facturacion_gen_secuencias_hilos.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Table("inventario_lote")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InventarioLote {

    @Id
    private Long id;
    private Long productoId;
    private String codigoLote;
    private Integer cantidadDisponible;
    private Instant fechaIngreso;
    private Instant fechaVencimiento;
}
