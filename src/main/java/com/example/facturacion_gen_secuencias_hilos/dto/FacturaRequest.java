package com.example.facturacion_gen_secuencias_hilos.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record FacturaRequest(
        @NotNull(message = "Los datos del cliente son obligatorios")
        @Valid
        ClienteRequest cliente,

        @NotEmpty(message = "La factura debe contener al menos un detalle de producto")
        List<@Valid DetalleRequest> detalles
) {}

