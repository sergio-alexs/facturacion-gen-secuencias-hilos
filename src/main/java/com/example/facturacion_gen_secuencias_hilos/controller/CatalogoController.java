package com.example.facturacion_gen_secuencias_hilos.controller;

import com.example.facturacion_gen_secuencias_hilos.dto.LoteStockInfo;
import com.example.facturacion_gen_secuencias_hilos.dto.ProductoStockResponse;
import com.example.facturacion_gen_secuencias_hilos.model.InventarioLote;
import com.example.facturacion_gen_secuencias_hilos.model.Producto;
import com.example.facturacion_gen_secuencias_hilos.repository.InventarioLoteRepository;
import com.example.facturacion_gen_secuencias_hilos.repository.ProductoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/catalogo")
public class CatalogoController {

    private final ProductoRepository productoRepository;
    private final InventarioLoteRepository loteRepository;

    public CatalogoController(ProductoRepository productoRepository,
                              InventarioLoteRepository loteRepository) {
        this.productoRepository = productoRepository;
        this.loteRepository = loteRepository;
    }

    @GetMapping("/productos")
    public List<Producto> listarProductos() {
        return productoRepository.findByActivoTrue();
    }

    @GetMapping("/productos/paginados")
    public Page<Producto> listarProductosPaginados(
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String palabraClave,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamanio) {
        String catFiltro = (categoria != null && !categoria.isBlank()) ? categoria.trim().toUpperCase() : null;
        String pattern = (palabraClave != null && !palabraClave.isBlank())
                ? "%" + palabraClave.trim().toLowerCase() + "%"
                : null;

        int limit = Math.max(1, tamanio);
        int pageNumber = Math.max(0, pagina);
        long offset = (long) pageNumber * limit;

        List<Producto> contenido = productoRepository.buscarPaginado(catFiltro, pattern, limit, offset);
        long total = productoRepository.contarBuscarPaginado(catFiltro, pattern);

        return new PageImpl<>(contenido, PageRequest.of(pageNumber, limit), total);
    }

    @GetMapping("/stock")
    public List<ProductoStockResponse> consultarStockCatálogo() {
        List<Producto> productos = productoRepository.findByActivoTrue();
        List<ProductoStockResponse> resultado = new ArrayList<>();

        for (Producto p : productos) {
            List<InventarioLote> lotes = loteRepository.findByProductoId(p.getId());

            int stockTotal = 0;
            List<LoteStockInfo> lotesInfo = new ArrayList<>();

            for (InventarioLote l : lotes) {
                stockTotal += l.getCantidadDisponible();
                lotesInfo.add(new LoteStockInfo(
                        l.getId(),
                        l.getCodigoLote(),
                        l.getCantidadDisponible(),
                        l.getFechaIngreso(),
                        l.getFechaVencimiento()
                ));
            }

            String estadoStock;
            if (stockTotal == 0) {
                estadoStock = "AGOTADO";
            } else if (stockTotal < 5) {
                estadoStock = "BAJO_STOCK";
            } else {
                estadoStock = "DISPONIBLE";
            }

            resultado.add(new ProductoStockResponse(
                    p.getId(),
                    p.getSku(),
                    p.getNombre(),
                    p.getCategoria(),
                    p.getPrecioUnitario(),
                    stockTotal,
                    estadoStock,
                    lotesInfo
            ));
        }

        return resultado;
    }
}
