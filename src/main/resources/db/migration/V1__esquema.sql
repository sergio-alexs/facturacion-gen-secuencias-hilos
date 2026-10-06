-- Secuencias independientes (sin atar implícitamente a DEFAULT de tablas)
CREATE SEQUENCE IF NOT EXISTS seq_cliente START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS seq_producto START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS seq_lote START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS seq_factura START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS seq_factura_detalle START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS seq_detalle_lote START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS seq_detalle_comentario START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS seq_movimiento_inventario START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS seq_reabastecimiento START WITH 1 INCREMENT BY 1;

-- 1. Tabla de Clientes
CREATE TABLE IF NOT EXISTS cliente (
    id BIGINT PRIMARY KEY,
    identificacion VARCHAR(50) UNIQUE NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    email VARCHAR(100),
    telefono VARCHAR(30),
    direccion VARCHAR(200),
    fecha_creacion TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 2. Tabla de Catálogo de Productos (Computadores, Partes, Accesorios)
CREATE TABLE IF NOT EXISTS producto (
    id BIGINT PRIMARY KEY,
    sku VARCHAR(50) UNIQUE NOT NULL,
    nombre VARCHAR(200) NOT NULL,
    categoria VARCHAR(50) NOT NULL, -- PORTATILES, ESCRITORIO, PARTES, ACCESORIOS
    precio_unitario NUMERIC(12, 2) NOT NULL,
    activo BOOLEAN DEFAULT TRUE
);

-- 3. Tabla de Inventario / Lotes (FEFO)
CREATE TABLE IF NOT EXISTS inventario_lote (
    id BIGINT PRIMARY KEY,
    producto_id BIGINT NOT NULL REFERENCES producto(id),
    codigo_lote VARCHAR(50) NOT NULL,
    cantidad_disponible INT NOT NULL CONSTRAINT check_cant_disp CHECK (cantidad_disponible >= 0),
    fecha_ingreso TIMESTAMP WITH TIME ZONE NOT NULL,
    fecha_vencimiento TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_inventario_prod_fefo ON inventario_lote (producto_id, fecha_vencimiento, id);

-- 4. Tabla Encabezado de Factura
CREATE TABLE IF NOT EXISTS factura (
    id BIGINT PRIMARY KEY,
    numero VARCHAR(50) UNIQUE NOT NULL,
    cliente_id BIGINT NOT NULL REFERENCES cliente(id),
    fecha TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    estado VARCHAR(30) NOT NULL, -- PROCESADA, CON_ITEMS_PENDIENTES, CANCELADA
    subtotal NUMERIC(14, 2) NOT NULL,
    impuesto NUMERIC(14, 2) NOT NULL,
    total NUMERIC(14, 2) NOT NULL,
    detalles_facturados INT NOT NULL,
    detalles_parciales INT NOT NULL,
    detalles_no_facturados INT NOT NULL
);

-- 5. Tabla Detalle de Factura
CREATE TABLE IF NOT EXISTS factura_detalle (
    id BIGINT PRIMARY KEY,
    factura_id BIGINT NOT NULL REFERENCES factura(id) ON DELETE CASCADE,
    linea INT NOT NULL, -- Índice de colección Spring Data JDBC
    producto_id BIGINT NOT NULL REFERENCES producto(id),
    cantidad_solicitada INT NOT NULL,
    cantidad_facturada INT NOT NULL,
    precio_unitario NUMERIC(12, 2) NOT NULL,
    total NUMERIC(14, 2) NOT NULL,
    estado VARCHAR(30) NOT NULL, -- FACTURADO, FACTURADO_PARCIAL, NO_FACTURADO
    motivo VARCHAR(100) -- SIN_STOCK, STOCK_PARCIAL, etc.
);

-- 6. Tabla Desglose Lote por Detalle de Factura
CREATE TABLE IF NOT EXISTS factura_detalle_lote (
    id BIGINT PRIMARY KEY,
    factura_detalle_id BIGINT NOT NULL REFERENCES factura_detalle(id) ON DELETE CASCADE,
    orden INT NOT NULL,
    lote_id BIGINT NOT NULL REFERENCES inventario_lote(id),
    codigo_lote VARCHAR(50) NOT NULL,
    cantidad INT NOT NULL
);

-- 7. Tabla Comentarios de Detalle de Factura
CREATE TABLE IF NOT EXISTS factura_detalle_comentario (
    id BIGINT PRIMARY KEY,
    factura_detalle_id BIGINT NOT NULL REFERENCES factura_detalle(id) ON DELETE CASCADE,
    orden INT NOT NULL,
    comentario TEXT NOT NULL,
    fecha TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 8. Tabla Movimientos de Inventario
CREATE TABLE IF NOT EXISTS movimiento_inventario (
    id BIGINT PRIMARY KEY,
    lote_id BIGINT NOT NULL REFERENCES inventario_lote(id),
    producto_id BIGINT NOT NULL REFERENCES producto(id),
    factura_detalle_lote_id BIGINT REFERENCES factura_detalle_lote(id),
    reabastecimiento_id BIGINT,
    tipo VARCHAR(30) NOT NULL, -- SALIDA_VENTA, ENTRADA_REABASTECIMIENTO
    cantidad INT NOT NULL,
    fecha TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 9. Tabla Reabastecimiento
CREATE TABLE IF NOT EXISTS reabastecimiento (
    id BIGINT PRIMARY KEY,
    fecha TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    observacion VARCHAR(255)
);
