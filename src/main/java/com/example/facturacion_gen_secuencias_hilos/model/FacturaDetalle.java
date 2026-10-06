package com.example.facturacion_gen_secuencias_hilos.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Table("factura_detalle")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FacturaDetalle {

    @Id
    private Long id;
    private Integer linea;
    private Long productoId;
    private Integer cantidadSolicitada;
    private Integer cantidadFacturada;
    private BigDecimal precioUnitario;
    private BigDecimal total;
    private String estado;
    private String motivo;

    @MappedCollection(idColumn = "factura_detalle_id", keyColumn = "orden")
    private List<DetalleLote> lotes = new ArrayList<>();

    @MappedCollection(idColumn = "factura_detalle_id", keyColumn = "orden")
    private List<DetalleComentario> comentarios = new ArrayList<>();
}
