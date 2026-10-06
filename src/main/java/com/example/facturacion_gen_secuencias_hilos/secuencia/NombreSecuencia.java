package com.example.facturacion_gen_secuencias_hilos.secuencia;

import lombok.Getter;

@Getter
public enum NombreSecuencia {
    CLIENTE("seq_cliente"),
    PRODUCTO("seq_producto"),
    LOTE("seq_lote"),
    FACTURA("seq_factura"),
    FACTURA_DETALLE("seq_factura_detalle"),
    DETALLE_LOTE("seq_detalle_lote"),
    DETALLE_COMENTARIO("seq_detalle_comentario"),
    MOVIMIENTO_INVENTARIO("seq_movimiento_inventario"),
    REABASTECIMIENTO("seq_reabastecimiento");

    private final String nombreSql;

    NombreSecuencia(String nombreSql) {
        this.nombreSql = nombreSql;
    }
}
