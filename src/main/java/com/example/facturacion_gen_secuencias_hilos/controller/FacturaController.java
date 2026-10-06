package com.example.facturacion_gen_secuencias_hilos.controller;

import com.example.facturacion_gen_secuencias_hilos.dto.*;
import com.example.facturacion_gen_secuencias_hilos.model.Factura;
import com.example.facturacion_gen_secuencias_hilos.repository.FacturaRepository;
import com.example.facturacion_gen_secuencias_hilos.service.FacturacionOrquestador;
import com.example.facturacion_gen_secuencias_hilos.service.GeneradorPeticionAleatoria;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/facturas")
public class FacturaController {

    private final FacturacionOrquestador orquestador;
    private final GeneradorPeticionAleatoria generadorAleatorio;
    private final FacturaRepository facturaRepository;

    public FacturaController(FacturacionOrquestador orquestador,
                             GeneradorPeticionAleatoria generadorAleatorio,
                             FacturaRepository facturaRepository) {
        this.orquestador = orquestador;
        this.generadorAleatorio = generadorAleatorio;
        this.facturaRepository = facturaRepository;
    }

    @PostMapping
    public ResponseEntity<FacturaResultado> crearFactura(
            @Valid @RequestBody FacturaRequest request,
            @RequestParam(value = "escenario", defaultValue = "CON_ESPERA") Escenario escenario,
            @RequestParam(value = "modo", defaultValue = "HILOS") Modo modo) {
        FacturaResultado resultado = orquestador.procesarFactura(request, escenario, modo);
        return ResponseEntity.ok(resultado);
    }

    @PostMapping("/simular")
    public ResponseEntity<FacturaResultado> simularFactura(
            @RequestParam(value = "detalles", defaultValue = "10") int detalles,
            @RequestParam(value = "maxComentarios", defaultValue = "2") int maxComentarios,
            @RequestParam(value = "escenario", defaultValue = "CON_ESPERA") Escenario escenario,
            @RequestParam(value = "modo", defaultValue = "HILOS") Modo modo,
            @RequestParam(value = "clienteExistente", defaultValue = "false") boolean clienteExistente) {

        FacturaRequest request = generadorAleatorio.generarPeticionAleatoria(detalles, maxComentarios, clienteExistente);
        FacturaResultado resultado = orquestador.procesarFactura(request, escenario, modo);
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Factura> obtenerFactura(@PathVariable("id") Long id) {
        return facturaRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}

