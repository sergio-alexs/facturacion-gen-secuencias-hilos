package com.example.facturacion_gen_secuencias_hilos.controller;

import com.example.facturacion_gen_secuencias_hilos.model.Reabastecimiento;
import com.example.facturacion_gen_secuencias_hilos.service.ReabastecimientoService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventario")
public class InventarioController {

    private final ReabastecimientoService reabastecimientoService;

    public InventarioController(ReabastecimientoService reabastecimientoService) {
        this.reabastecimientoService = reabastecimientoService;
    }

    @PostMapping("/reabastecer")
    public Reabastecimiento reabastecer(
            @RequestParam(value = "productoId", required = false) Long productoId,
            @RequestParam(value = "cantidad", defaultValue = "20") int cantidad,
            @RequestParam(value = "observacion", required = false) String observacion) {

        if (productoId != null) {
            return reabastecimientoService.reabastecerProducto(productoId, cantidad, observacion);
        } else {
            return reabastecimientoService.reabastecerAutomatico(observacion);
        }
    }
}
