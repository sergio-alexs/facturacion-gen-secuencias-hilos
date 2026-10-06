-- Clientes iniciales
INSERT INTO cliente (id, identificacion, nombre, email, telefono, direccion) VALUES
(nextval('seq_cliente'), '10987654321', 'Carlos Mendoza', 'carlos.mendoza@email.com', '3101234567', 'Calle 100 #15-20, Bogotá'),
(nextval('seq_cliente'), '9876543210', 'Empresa Tech Soluciones S.A.S.', 'contacto@techsoluciones.com', '3209876543', 'Av. El Dorado #68B-31, Bogotá'),
(nextval('seq_cliente'), '1122334455', 'Ana María Gómez', 'ana.gomez@email.com', '3005554433', 'Carrera 43A #1-50, Medellín')
ON CONFLICT (identificacion) DO NOTHING;

-- Catálogo de Productos (Computadores de escritorio, Portátiles, Partes, Accesorios)
-- Categoria: PORTATILES
INSERT INTO producto (id, sku, nombre, categoria, precio_unitario, activo) VALUES
(nextval('seq_producto'), 'LAP-001', 'Portátil Gamer Asus ROG Strix Core i7 16GB RTX 4060', 'PORTATILES', 5499900.00, TRUE),
(nextval('seq_producto'), 'LAP-002', 'Portátil Lenovo ThinkPad E14 Ryzen 7 16GB SSD 512GB', 'PORTATILES', 3299900.00, TRUE),
(nextval('seq_producto'), 'LAP-003', 'MacBook Air M3 13" 8GB SSD 256GB Gris Espacial', 'PORTATILES', 4899900.00, TRUE),
(nextval('seq_producto'), 'LAP-004', 'Portátil HP Pavilion Core i5 8GB SSD 512GB', 'PORTATILES', 2199900.00, TRUE);

-- Categoria: ESCRITORIO
INSERT INTO producto (id, sku, nombre, categoria, precio_unitario, activo) VALUES
(nextval('seq_producto'), 'DESK-001', 'PC Escritorio Gamer Ryzen 7 5700X RTX 3060 32GB RAM SSD 1TB', 'ESCRITORIO', 4299900.00, TRUE),
(nextval('seq_producto'), 'DESK-002', 'All in One HP 24" Core i5 16GB SSD 512GB Windows 11 Pro', 'ESCRITORIO', 2999900.00, TRUE),
(nextval('seq_producto'), 'DESK-003', 'PC de Oficina Dell OptiPlex Core i3 8GB SSD 256GB', 'ESCRITORIO', 1799900.00, TRUE);

-- Categoria: PARTES
INSERT INTO producto (id, sku, nombre, categoria, precio_unitario, activo) VALUES
(nextval('seq_producto'), 'PART-001', 'Memoria RAM Kingston Fury Beast 16GB DDR4 3200MHz', 'PARTES', 199900.00, TRUE),
(nextval('seq_producto'), 'PART-002', 'Disco Sólido SSD NVMe M.2 Samsung 980 1TB Pro', 'PARTES', 449900.00, TRUE),
(nextval('seq_producto'), 'PART-003', 'Tarjeta de Video Nvidia RTX 4070 Super 12GB Asus TUF', 'PARTES', 3299900.00, TRUE),
(nextval('seq_producto'), 'PART-004', 'Procesador AMD Ryzen 7 7800X3D Socket AM5', 'PARTES', 1899900.00, TRUE),
(nextval('seq_producto'), 'PART-005', 'Fuente de Poder Corsair 750W 80 Plus Gold Modular', 'PARTES', 499900.00, TRUE);

-- Categoria: ACCESORIOS
INSERT INTO producto (id, sku, nombre, categoria, precio_unitario, activo) VALUES
(nextval('seq_producto'), 'ACC-001', 'Monitor LG 27" Full HD IPS 100Hz HDMI', 'ACCESORIOS', 629900.00, TRUE),
(nextval('seq_producto'), 'ACC-002', 'Teclado Mecánico Redragon Kumara RGB Switch Red', 'ACCESORIOS', 189900.00, TRUE),
(nextval('seq_producto'), 'ACC-003', 'Mouse Inalámbrico Logitech MX Master 3S', 'ACCESORIOS', 459900.00, TRUE),
(nextval('seq_producto'), 'ACC-004', 'Diadema Headset Gamer HyperX Cloud II Red', 'ACCESORIOS', 349900.00, TRUE);

-- Inventario de Lotes (FEFO: Lotes con fechas de vencimiento/ingreso)
-- Lotes para Portátil LAP-001 (id 1)
INSERT INTO inventario_lote (id, producto_id, codigo_lote, cantidad_disponible, fecha_ingreso, fecha_vencimiento)
SELECT nextval('seq_lote'), p.id, 'LOT-LAP001-A', 5, CURRENT_TIMESTAMP - INTERVAL '30 days', CURRENT_TIMESTAMP + INTERVAL '365 days' FROM producto p WHERE p.sku = 'LAP-001';
INSERT INTO inventario_lote (id, producto_id, codigo_lote, cantidad_disponible, fecha_ingreso, fecha_vencimiento)
SELECT nextval('seq_lote'), p.id, 'LOT-LAP001-B', 10, CURRENT_TIMESTAMP - INTERVAL '10 days', CURRENT_TIMESTAMP + INTERVAL '400 days' FROM producto p WHERE p.sku = 'LAP-001';

-- Lotes para Portátil LAP-002
INSERT INTO inventario_lote (id, producto_id, codigo_lote, cantidad_disponible, fecha_ingreso, fecha_vencimiento)
SELECT nextval('seq_lote'), p.id, 'LOT-LAP002-A', 8, CURRENT_TIMESTAMP - INTERVAL '20 days', CURRENT_TIMESTAMP + INTERVAL '300 days' FROM producto p WHERE p.sku = 'LAP-002';

-- Lotes para PC Gamer DESK-001
INSERT INTO inventario_lote (id, producto_id, codigo_lote, cantidad_disponible, fecha_ingreso, fecha_vencimiento)
SELECT nextval('seq_lote'), p.id, 'LOT-DESK001-A', 3, CURRENT_TIMESTAMP - INTERVAL '40 days', CURRENT_TIMESTAMP + INTERVAL '200 days' FROM producto p WHERE p.sku = 'DESK-001';
INSERT INTO inventario_lote (id, producto_id, codigo_lote, cantidad_disponible, fecha_ingreso, fecha_vencimiento)
SELECT nextval('seq_lote'), p.id, 'LOT-DESK001-B', 7, CURRENT_TIMESTAMP - INTERVAL '5 days', CURRENT_TIMESTAMP + INTERVAL '500 days' FROM producto p WHERE p.sku = 'DESK-001';

-- Lotes para RAM PART-001 (Stock abundante)
INSERT INTO inventario_lote (id, producto_id, codigo_lote, cantidad_disponible, fecha_ingreso, fecha_vencimiento)
SELECT nextval('seq_lote'), p.id, 'LOT-RAM-01', 15, CURRENT_TIMESTAMP - INTERVAL '60 days', CURRENT_TIMESTAMP + INTERVAL '700 days' FROM producto p WHERE p.sku = 'PART-001';
INSERT INTO inventario_lote (id, producto_id, codigo_lote, cantidad_disponible, fecha_ingreso, fecha_vencimiento)
SELECT nextval('seq_lote'), p.id, 'LOT-RAM-02', 25, CURRENT_TIMESTAMP - INTERVAL '15 days', CURRENT_TIMESTAMP + INTERVAL '800 days' FROM producto p WHERE p.sku = 'PART-001';

-- Lotes para SSD PART-002
INSERT INTO inventario_lote (id, producto_id, codigo_lote, cantidad_disponible, fecha_ingreso, fecha_vencimiento)
SELECT nextval('seq_lote'), p.id, 'LOT-SSD-01', 12, CURRENT_TIMESTAMP - INTERVAL '25 days', CURRENT_TIMESTAMP + INTERVAL '600 days' FROM producto p WHERE p.sku = 'PART-002';

-- Lotes para Monitor ACC-001
INSERT INTO inventario_lote (id, producto_id, codigo_lote, cantidad_disponible, fecha_ingreso, fecha_vencimiento)
SELECT nextval('seq_lote'), p.id, 'LOT-MON-01', 10, CURRENT_TIMESTAMP - INTERVAL '12 days', CURRENT_TIMESTAMP + INTERVAL '365 days' FROM producto p WHERE p.sku = 'ACC-001';

-- Lote con Stock Bajo/Limitado para probar SIN_STOCK o FACTURADO_PARCIAL (RTX 4070)
INSERT INTO inventario_lote (id, producto_id, codigo_lote, cantidad_disponible, fecha_ingreso, fecha_vencimiento)
SELECT nextval('seq_lote'), p.id, 'LOT-GPU-01', 2, CURRENT_TIMESTAMP - INTERVAL '50 days', CURRENT_TIMESTAMP + INTERVAL '180 days' FROM producto p WHERE p.sku = 'PART-003';

-- Lote con 0 stock para probar SIN_STOCK (MacBook Air)
INSERT INTO inventario_lote (id, producto_id, codigo_lote, cantidad_disponible, fecha_ingreso, fecha_vencimiento)
SELECT nextval('seq_lote'), p.id, 'LOT-MAC-AGOTADO', 0, CURRENT_TIMESTAMP - INTERVAL '90 days', CURRENT_TIMESTAMP + INTERVAL '90 days' FROM producto p WHERE p.sku = 'LAP-003';
