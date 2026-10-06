# POC Facturación de Alto Rendimiento con Hilos Virtuales (Java 21)

Sistema de facturación electrónica y gestión de inventario FEFO para una tienda de tecnología (computadores, partes y accesorios), diseñado para demostrar el manejo eficiente de **secuencias asíncronas con latencia artificial (~300ms)** y **concurrencia masiva utilizando Virtual Threads (Project Loom en JDK 21)**.

---

## 🚀 Características Principales

1. **Generación Asíncrona de Secuencias Independientes**:
   - No utiliza secuencias pre-reservadas en memoria.
   - Simula la latencia de red/base de datos (~300ms) mediante `Thread.sleep` en Java (sin bloquear la base de datos con `pg_sleep`).
   - Cada entidad (Factura, Detalle, Lote, Comentario, Movimiento, Cliente) genera un identificador temporal (`tempId`) y solicita su secuencia real en paralelo sobre **Virtual Threads**.
   - El hilo orquestador principal continúa validando los detalles y solo se suspende si quedan secuencias pendientes al finalizar el procesamiento.

2. **Cero Carrier Pinning en Java 21**:
   - Implementación estricta sin bloques `synchronized` en los puntos de espera de hilos virtuales.
   - Coordinación de hilos mediante `ReentrantLock` y `Condition` en `ContextoSecuencias`, permitiendo que el Carrier Thread de la JVM quede libre para atender otras tareas.

3. **Inventario FEFO con Reparto Multi-Lote**:
   - Asignación de stock basada en **First Expired, First Out** (`fecha_vencimiento ASC`).
   - Si un lote no tiene la cantidad suficiente, el detalle se divide automáticamente entre múltiples lotes disponibles.
   - Si no hay stock suficiente para cubrir la totalidad, se reserva únicamente lo disponible registrando `cantidad_solicitada` vs `cantidad_facturada`.

4. **Tratamiento de Ítems Sin Stock (`NO_FACTURADO`)**:
   - Los ítems sin stock se guardan en la base de datos con estado `NO_FACTURADO` y motivo `SIN_STOCK` para trazabilidad de la demanda insatisfecha.
   - **Exclusión contable**: Los ítems `NO_FACTURADO` se excluyen de los totales financieros (`subtotal`, `impuesto`, `total`) del encabezado de la factura.

5. **Concurrencia Atómica sin Bloqueos Pesimistas (`FOR UPDATE` descartado)**:
   - Se evita `SELECT ... FOR UPDATE` para prevenir bloqueos de tablas/filas y deadlocks en escenarios de alta concurrencia.
   - Se utiliza una sentencia de actualización atómica condicional:
     ```sql
     UPDATE inventario_lote 
     SET cantidad_disponible = cantidad_disponible - :cantidad 
     WHERE id = :loteId AND cantidad_disponible >= :cantidad
     ```
   - Si la fila no es modificada debido a que otra transacción concurrente tomó el inventario, el sistema verifica el stock remanente disponible y ajusta el detalle en caliente (`FACTURADO_PARCIAL` o `NO_FACTURADO`), recalculando los totales contables de manera atómica.

6. **Catálogo de Tecnología Ampliado**:
   - 60 productos categorizados (`PORTATILES`, `ESCRITORIO`, `PARTES`, `ACCESORIOS`).
   - Búsqueda paginada con filtrado dinámico por categoría y palabra clave (SKU / Nombre).

---

## 🏗️ Arquitectura en Capas

El proyecto sigue una arquitectura desacoplada por capas:

```
src/main/java/com/example/facturacion_gen_secuencias_hilos/
├── config/                  # Configuración de Executors de Virtual Threads y propiedades
│   ├── ExecutorsConfig.java
│   └── SimulacionProperties.java
├── controller/              # Controladores REST
│   ├── CatalogoController.java
│   ├── FacturaController.java
│   └── InventarioController.java
├── dto/                     # DTOs de peticiones, respuestas, métricas y proyecciones
│   ├── DetalleRequest.java / DetalleProcesado.java
│   ├── FacturaRequest.java / FacturaResultado.java
│   ├── MetricasSimulacion.java
│   └── ProductoStockResponse.java / LoteStockInfo.java
├── model/                   # Entidades de dominio (Spring Data JDBC)
│   ├── Factura.java / FacturaDetalle.java
│   ├── DetalleLote.java / DetalleComentario.java
│   ├── InventarioLote.java / MovimientoInventario.java
│   ├── Producto.java / Cliente.java
│   └── ReservaStockLedger.java
├── repository/              # ListCrudRepositories e interfaces de consulta Spring Data JDBC
│   ├── FacturaRepository.java
│   ├── InventarioLoteRepository.java
│   └── ProductoRepository.java
├── secuencia/               # Motor de secuencias y suscriptor reactivo sin pinning
│   ├── ContextoSecuencias.java
│   ├── SecuenciaService.java
│   ├── NombreSecuencia.java
│   └── ClaveTemporal.java
└── service/                 # Lógica de negocio y orquestación
    ├── DetalleProcessor.java
    ├── FacturacionOrquestador.java
    ├── FacturaPersistenciaService.java
    ├── GeneradorPeticionAleatoria.java
    └── ReabastecimientoService.java
```

---

## 🗄️ Modelo de Base de Datos y Secuencias

PostgreSQL 17 gestionado mediante migraciones automáticas de **Flyway**:

- `V1__esquema.sql`:
  - Secuencias independientes: `seq_cliente_id`, `seq_factura_id`, `seq_factura_detalle_id`, `seq_detalle_lote_id`, `seq_detalle_comentario_id`, `seq_movimiento_inventario_id`, `seq_reabastecimiento_id`.
  - Tablas relacionales con llaves foráneas e índices para FEFO (`producto_id`, `fecha_vencimiento`).
- `V2__datos_iniciales.sql`:
  - Clientes iniciales y primeros lotes de prueba.
- `V3__ampliar_catalogo_tecnologia.sql`:
  - Inserción de 60 productos y más de 80 lotes FEFO con diversas fechas y stock.

---

## ⚙️ Requisitos Previos

- **Java**: JDK 21 o superior
- **Docker** y **Docker Compose**
- **Maven** (incluido mediante wrapper `./mvnw`)

---

## 🛠️ Puesta en Marcha

### 1. Iniciar la Base de Datos (PostgreSQL)

```bash
docker compose up -d
```

Verificar que el contenedor esté arriba:
```bash
docker compose ps
```

### 2. Compilar y Ejecutar las Pruebas

```bash
./mvnw clean test
```

> **Nota**: Los tests incluyen `PinningTest`, el cual valida mediante eventos JFR / ThreadMXBean que no se generen fijaciones de Carrier Threads durante la generación asíncrona de secuencias.

### 3. Ejecutar la Aplicación

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21) # En macOS
./mvnw spring-boot:run
```

La aplicación iniciará en el puerto `8080`.

---

## 📡 Catálogo de Endpoints REST

### 1. Catálogo de Productos y Búsqueda Paginada

#### Búsqueda Paginada con Filtros
Permite filtrar por categoría (`PORTATILES`, `ESCRITORIO`, `PARTES`, `ACCESORIOS`) y por palabra clave sobre SKU y Nombre:
```http
GET /api/catalogo/productos/paginados?categoria=PARTES&palabraClave=ryzen&pagina=0&tamanio=10
```

**Respuesta de ejemplo (`Page<Producto>`)**:
```json
{
  "content": [
    {
      "id": 16,
      "sku": "CPU-001",
      "nombre": "Procesador AMD Ryzen 7 7800X3D 8-Core 4.2GHz",
      "categoria": "PARTES",
      "precioUnitario": 1950000.00,
      "activo": true
    },
    {
      "id": 17,
      "sku": "CPU-002",
      "nombre": "Procesador AMD Ryzen 9 7950X 16-Core 4.5GHz",
      "categoria": "PARTES",
      "precioUnitario": 2690000.00,
      "activo": true
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10
  },
  "totalElements": 2,
  "totalPages": 1,
  "last": true,
  "first": true
}
```

#### Listar Todos los Productos Activos
```http
GET /api/catalogo/productos
```

#### Consultar Stock y Lotes FEFO
Muestra el estado consolidado (`DISPONIBLE`, `BAJO_STOCK`, `AGOTADO`) y el desglose de cada lote con sus fechas de vencimiento:
```http
GET /api/catalogo/stock
```

---

### 2. Simulación y Facturación de Alto Rendimiento

#### Simular Facturación Concurrente
Genera una orden sintética aleatoria de tecnología con clientes, productos y comentarios, midiendo los tiempos de procesamiento, esperas y secuencias:
```http
POST /api/facturas/simular?detalles=10&maxComentarios=2&escenario=CON_ESPERA&modo=HILOS
```

**Parámetros:**
- `detalles`: Número de líneas en la factura (ej: 10, 25, 50).
- `maxComentarios`: Cantidad máxima de comentarios por detalle (0 a 3).
- `escenario`:
  - `CON_ESPERA`: Validación rápida de detalle (30ms) vs Secuencia lenta (300ms) $\rightarrow$ El hilo principal espera la diferencia.
  - `SIN_ESPERA`: Validación de detalle lenta (400ms) vs Secuencia (300ms) $\rightarrow$ Tiempo de espera de secuencias = 0 ms (solapamiento perfecto).
- `modo`:
  - `HILOS`: Hilos virtuales para detalles y secuencias.
  - `SECUENCIAL`: Procesamiento en un solo hilo para contrastar métricas.

**Respuesta de ejemplo (Métricas de Ejecución)**:
```json
{
  "facturaId": 7,
  "numeroFactura": "FAC-202610-00007",
  "total": 93115834.00,
  "metricas": {
    "escenario": "CON_ESPERA",
    "modo": "HILOS",
    "tiempoTotalMs": 375,
    "tiempoProcesamientoDetallesMs": 31,
    "tiempoEsperaSecuenciasMs": 277,
    "secuenciasSolicitadas": 43,
    "secuenciasPendientesAlEsperar": 43,
    "tiempoInsercionMs": 61,
    "mapeoTemporalReal": {
      "FACTURA": { "tempId_1": 7 },
      "CLIENTE": { "tempId_1": 10 }
    }
  }
}
```

#### Crear Factura Directa (JSON Body)
```http
POST /api/facturas?escenario=CON_ESPERA&modo=HILOS
Content-Type: application/json

{
  "cliente": {
    "clienteId": 1
  },
  "detalles": [
    {
      "productoId": 1,
      "cantidad": 3,
      "comentarios": ["Entregar con software preinstalado"]
    }
  ]
}
```

#### Consultar Factura Persistida por ID
```http
GET /api/facturas/7
```

---

### 3. Reabastecimiento de Inventario

#### Reabastecimiento Puntual de un Producto
Ingresa un nuevo lote para un producto específico:
```http
POST /api/inventario/reabastecer?productoId=3&cantidad=30&observacion=Ingreso+importacion+MacBook
```

#### Reabastecimiento Global Automático
Detecta productos con stock cero o bajo stock y genera automáticamente lotes de reabastecimiento:
```http
POST /api/inventario/reabastecer?observacion=Reabastecimiento+general+semanal
```

---

## 📮 Colección de Postman

El proyecto incluye una colección lista para importar en [Postman](file:///Users/sergio.aragonez/Desktop/facturacion-gen-secuencias-hilos/postman/facturacion-poc.postman_collection.json):
- Ubicación: `postman/facturacion-poc.postman_collection.json`
- Contiene requests preconfigurados para todos los escenarios de simulación, consulta de stock, reabastecimiento puntual y búsqueda paginada.
