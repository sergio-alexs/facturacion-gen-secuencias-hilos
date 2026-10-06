package com.example.facturacion_gen_secuencias_hilos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

@SpringBootApplication
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class FacturacionGenSecuenciasHilosApplication {

	public static void main(String[] args) {
		SpringApplication.run(FacturacionGenSecuenciasHilosApplication.class, args);
	}

}
