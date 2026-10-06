package com.example.facturacion_gen_secuencias_hilos.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Table("factura")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Factura {

    @Id
    private Long id;
    private String numero;
    private Long clienteId;
    private Instant fecha;
    private String estado;
    private BigDecimal subtotal;
    private BigDecimal impuesto;
    private BigDecimal total;
    private Integer detallesFacturados;
    private Integer detallesParciales;
    private Integer detallesNoFacturados;

    @MappedCollection(idColumn = "factura_id", keyColumn = "linea")
    private List<FacturaDetalle> detalles = new ArrayList<>();
}
