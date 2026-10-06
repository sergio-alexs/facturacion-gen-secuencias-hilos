package com.example.facturacion_gen_secuencias_hilos.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClienteRequest(
        @NotBlank(message = "La identificación del cliente es obligatoria")
        @Size(max = 50, message = "La identificación no puede exceder 50 caracteres")
        String identificacion,

        @NotBlank(message = "El nombre del cliente es obligatorio")
        @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
        String nombre,

        @Email(message = "El formato del email es inválido")
        String email,

        String telefono,
        String direccion
) {}

