package com.example.facturacion_gen_secuencias_hilos.service;

import com.example.facturacion_gen_secuencias_hilos.dto.ClienteRequest;
import com.example.facturacion_gen_secuencias_hilos.dto.DetalleRequest;
import com.example.facturacion_gen_secuencias_hilos.dto.FacturaRequest;
import com.example.facturacion_gen_secuencias_hilos.model.Cliente;
import com.example.facturacion_gen_secuencias_hilos.model.Producto;
import com.example.facturacion_gen_secuencias_hilos.repository.ClienteRepository;
import com.example.facturacion_gen_secuencias_hilos.repository.ProductoRepository;
import net.datafaker.Faker;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

@Service
public class GeneradorPeticionAleatoria {

    private final ProductoRepository productoRepository;
    private final ClienteRepository clienteRepository;
    private final Random random = new Random();

    public GeneradorPeticionAleatoria(ProductoRepository productoRepository,
                                       ClienteRepository clienteRepository) {
        this.productoRepository = productoRepository;
        this.clienteRepository = clienteRepository;
    }

    public FacturaRequest generarPeticionAleatoria(int numDetalles, int maxComentariosPorDetalle, boolean clienteExistente) {
        Faker faker = new Faker(Locale.of("es"));

        ClienteRequest clienteReq;
        List<Cliente> clientesExistentes = clienteRepository.findAll();

        if (clienteExistente && !clientesExistentes.isEmpty()) {
            Cliente c = clientesExistentes.get(random.nextInt(clientesExistentes.size()));
            clienteReq = new ClienteRequest(c.getIdentificacion(), c.getNombre(), c.getEmail(), c.getTelefono(), c.getDireccion());
        } else {
            String idFalsa = String.valueOf(1000000000L + random.nextInt(900000000));
            clienteReq = new ClienteRequest(
                    idFalsa,
                    faker.name().fullName() + " S.A.S.",
                    faker.internet().emailAddress(),
                    faker.phoneNumber().cellPhone(),
                    faker.address().fullAddress()
            );
        }

        List<Producto> productos = productoRepository.findByActivoTrue();
        if (productos.isEmpty()) {
            throw new IllegalStateException("No hay productos disponibles en la base de datos");
        }

        List<String> comentariosPosibles = List.of(
                "Verificar empaque sellado de fábrica",
                "Requiere certificado de garantía por 2 años",
                "Enviar por transporte terrestre asegurado",
                "Licencia de software preinstalada OEM",
                "Comprobante fiscal con IVA discriminado",
                "Verificar número de serie e inspeccionar puerto HDMI antes de envío",
                "Empaque con protección de espuma antiestática para componente delicado"
        );

        List<DetalleRequest> detalles = new ArrayList<>();
        for (int i = 0; i < numDetalles; i++) {
            Producto prod = productos.get(random.nextInt(productos.size()));
            int cantidad = 1 + random.nextInt(4);

            List<String> comentariosDetalle = new ArrayList<>();
            int numComentarios = maxComentariosPorDetalle > 0 ? random.nextInt(maxComentariosPorDetalle + 1) : 0;
            for (int k = 0; k < numComentarios; k++) {
                comentariosDetalle.add(comentariosPosibles.get(random.nextInt(comentariosPosibles.size())));
            }

            detalles.add(new DetalleRequest(prod.getId(), cantidad, comentariosDetalle));
        }

        return new FacturaRequest(clienteReq, detalles);
    }
}
