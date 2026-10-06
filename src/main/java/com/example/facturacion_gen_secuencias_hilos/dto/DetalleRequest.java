package com.example.facturacion_gen_secuencias_hilos.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record DetalleRequest(
        @NotNull(message = "El ID del producto es obligatorio")
        Long productoId,

        @Min(value = 1, message = "La cantidad debe ser al menos 1")
        int cantidad,

        List<String> comentarios
) {}

