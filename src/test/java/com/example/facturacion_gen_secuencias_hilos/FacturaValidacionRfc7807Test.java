package com.example.facturacion_gen_secuencias_hilos;

import com.example.facturacion_gen_secuencias_hilos.secuencia.SecuenciaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class FacturaValidacionRfc7807Test {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private SecuenciaService secuenciaService;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }


    @Test
    @DisplayName("Debe retornar RFC 7807 (400 Bad Request) cuando faltan datos obligatorios o son inválidos")
    void testValidacionCamposRetornaProblemDetail() throws Exception {
        // Enviar JSON con cliente sin identificación, email inválido y detalles vacíos
        String jsonInvalido = """
                {
                    "cliente": {
                        "identificacion": "",
                        "nombre": "",
                        "email": "correo-no-valido"
                    },
                    "detalles": []
                }
                """;

        mockMvc.perform(post("/api/facturas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonInvalido))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", containsString("application/problem+json")))
                .andExpect(jsonPath("$.type", is("urn:problem-type:validation-error")))
                .andExpect(jsonPath("$.title", is("Error de Validación")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.detail", notNullValue()))
                .andExpect(jsonPath("$.invalidParams", hasSize(greaterThanOrEqualTo(3))))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @Test
    @DisplayName("Debe retornar RFC 7807 (400 Bad Request) cuando la cantidad de un detalle es menor a 1")
    void testValidacionDetalleCantidadInvalida() throws Exception {
        String jsonCantidadInvalida = """
                {
                    "cliente": {
                        "identificacion": "12345678",
                        "nombre": "Juan Perez",
                        "email": "juan@test.com"
                    },
                    "detalles": [
                        {
                            "productoId": 1,
                            "cantidad": 0
                        }
                    ]
                }
                """;

        mockMvc.perform(post("/api/facturas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonCantidadInvalida))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", containsString("application/problem+json")))
                .andExpect(jsonPath("$.type", is("urn:problem-type:validation-error")))
                .andExpect(jsonPath("$.invalidParams[*].campo", hasItem(containsString("cantidad"))));
    }

    @Test
    @DisplayName("Debe retornar RFC 7807 (422 Unprocessable Content) con identificador cuando el cliente está bloqueado")
    void testClienteBloqueadoRetornaProblemDetail422() throws Exception {
        String identificacionBloqueada = "ID-BLOQUEADO-TEST99";
        String jsonClienteBloqueado = """
                {
                    "cliente": {
                        "identificacion": "%s",
                        "nombre": "Cliente Moroso",
                        "email": "moroso@test.com"
                    },
                    "detalles": [
                        {
                            "productoId": 1,
                            "cantidad": 1
                        }
                    ]
                }
                """.formatted(identificacionBloqueada);

        mockMvc.perform(post("/api/facturas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonClienteBloqueado))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(header().string("Content-Type", containsString("application/problem+json")))
                .andExpect(jsonPath("$.type", is("urn:problem-type:cliente-bloqueado")))
                .andExpect(jsonPath("$.title", is("Cliente Bloqueado")))
                .andExpect(jsonPath("$.status", is(422)))
                .andExpect(jsonPath("$.identificacion", is(identificacionBloqueada)))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @Autowired
    private com.example.facturacion_gen_secuencias_hilos.service.DetalleProcessor detalleProcessor;

    @Test
    @DisplayName("Debe verificar que el semáforo de secuencias tiene 8 permisos disponibles y no sufre fugas")
    void testSemaforoSecuenciasPermisosDisponibles() {
        assertEquals(8, secuenciaService.getPermisosDisponibles(),
                "El semáforo debe mantener exactamente 8 permisos disponibles en reposo");
    }

    @Test
    @DisplayName("Debe verificar que el semáforo de procesamiento de detalles tiene 10 permisos disponibles")
    void testSemaforoDetallesPermisosDisponibles() {
        assertEquals(10, detalleProcessor.getPermisosDisponibles(),
                "El semáforo de detalles debe mantener exactamente 10 permisos disponibles en reposo");
    }

    @Test
    @DisplayName("Debe procesar factura con 15 detalles respetando límites concurrentes sin fugas de permisos")
    void testProcesamientoMasivoDetallesRespetaPermisos() throws Exception {
        mockMvc.perform(post("/api/facturas/simular")
                        .param("detalles", "15")
                        .param("escenario", "CON_ESPERA")
                        .param("modo", "HILOS")
                        .param("clienteExistente", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.detalles", hasSize(15)))
                .andExpect(jsonPath("$.estado", notNullValue()));

        assertEquals(8, secuenciaService.getPermisosDisponibles(),
                "El semáforo de secuencias debe recuperar los 8 permisos");
        assertEquals(10, detalleProcessor.getPermisosDisponibles(),
                "El semáforo de detalles debe recuperar los 10 permisos");
    }
}

