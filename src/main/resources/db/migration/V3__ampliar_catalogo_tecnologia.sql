-- ==============================================================================
-- Migración V3: Ampliación Integral del Catálogo de Tecnología y Lotes FEFO
-- ==============================================================================

-- ------------------------------------------------------------------------------
-- 1. PORTÁTILES ADICIONALES
-- ------------------------------------------------------------------------------
INSERT INTO producto (id, sku, nombre, categoria, precio_unitario, activo) VALUES
(nextval('seq_producto'), 'LAP-005', 'Portátil Lenovo Legion Pro 7i Core i9 32GB RTX 4080 1TB SSD', 'PORTATILES', 11499900.00, TRUE),
(nextval('seq_producto'), 'LAP-006', 'MacBook Pro 16" M3 Max 36GB RAM 1TB SSD Negro Espacial', 'PORTATILES', 14299900.00, TRUE),
(nextval('seq_producto'), 'LAP-007', 'Portátil Dell XPS 15 OLED Core i7 16GB 512GB SSD RTX 4050', 'PORTATILES', 7899900.00, TRUE),
(nextval('seq_producto'), 'LAP-008', 'Portátil Acer Predator Helios 16 Core i7 16GB RTX 4060 1TB', 'PORTATILES', 5899900.00, TRUE),
(nextval('seq_producto'), 'LAP-009', 'Portátil HP Omen 16 Ryzen 7 16GB RTX 4070 1TB SSD', 'PORTATILES', 6499900.00, TRUE),
(nextval('seq_producto'), 'LAP-010', 'Portátil ASUS Zenbook Duo 14" Dual OLED Intel Core Ultra 7', 'PORTATILES', 8299900.00, TRUE),
(nextval('seq_producto'), 'LAP-011', 'Portátil MSI Katana 15 Core i7 16GB RTX 4060 512GB SSD', 'PORTATILES', 4699900.00, TRUE),
(nextval('seq_producto'), 'LAP-012', 'Portátil LG Gram 17 Ultraliviano Core i7 16GB 1TB SSD', 'PORTATILES', 5999900.00, TRUE);

-- ------------------------------------------------------------------------------
-- 2. COMPUTADORES DE ESCRITORIO ADICIONALES
-- ------------------------------------------------------------------------------
INSERT INTO producto (id, sku, nombre, categoria, precio_unitario, activo) VALUES
(nextval('seq_producto'), 'DESK-004', 'Workstation CAD Asus ProArt Core i9 64GB RTX 4080 2TB SSD', 'ESCRITORIO', 13999900.00, TRUE),
(nextval('seq_producto'), 'DESK-005', 'PC Gamer Extreme Ryzen 9 7950X3D RTX 4090 64GB Líquida', 'ESCRITORIO', 18999900.00, TRUE),
(nextval('seq_producto'), 'DESK-006', 'Mini PC ASUS NUC 14 Pro Core Ultra 5 16GB SSD 512GB', 'ESCRITORIO', 2499900.00, TRUE),
(nextval('seq_producto'), 'DESK-007', 'PC Torre Oficina Lenovo ThinkCentre M70s Core i5 8GB 256GB', 'ESCRITORIO', 1999900.00, TRUE),
(nextval('seq_producto'), 'DESK-008', 'Servidor Torre Dell PowerEdge T150 Xeon E-2314 16GB 2TB', 'ESCRITORIO', 5699900.00, TRUE),
(nextval('seq_producto'), 'DESK-009', 'Mac Mini M2 Pro 16GB RAM 512GB SSD Apple', 'ESCRITORIO', 5399900.00, TRUE),
(nextval('seq_producto'), 'DESK-010', 'PC Gamer Entrada Core i5 12400F GTX 1650 16GB SSD 512GB', 'ESCRITORIO', 2399900.00, TRUE);

-- ------------------------------------------------------------------------------
-- 3. PARTES Y COMPONENTES ADICIONALES
-- ------------------------------------------------------------------------------
INSERT INTO producto (id, sku, nombre, categoria, precio_unitario, activo) VALUES
(nextval('seq_producto'), 'PART-006', 'Procesador Intel Core i9 14900K LGA1700 24 Núcleos', 'PARTES', 2699900.00, TRUE),
(nextval('seq_producto'), 'PART-007', 'Procesador Intel Core i7 14700K LGA1700 20 Núcleos', 'PARTES', 1899900.00, TRUE),
(nextval('seq_producto'), 'PART-008', 'Procesador AMD Ryzen 5 7600X Socket AM5 6 Núcleos', 'PARTES', 999900.00, TRUE),
(nextval('seq_producto'), 'PART-009', 'Tarjeta de Video Asus ROG Strix GeForce RTX 4090 24GB', 'PARTES', 9899900.00, TRUE),
(nextval('seq_producto'), 'PART-010', 'Tarjeta de Video MSI Gaming X Slim RTX 4080 Super 16GB', 'PARTES', 4899900.00, TRUE),
(nextval('seq_producto'), 'PART-011', 'Tarjeta de Video Sapphire Nitro+ AMD Radeon RX 7900 XTX 24GB', 'PARTES', 4599900.00, TRUE),
(nextval('seq_producto'), 'PART-012', 'Tarjeta de Video Gigabyte RTX 4060 Ti Eagle OC 8GB', 'PARTES', 1999900.00, TRUE),
(nextval('seq_producto'), 'PART-013', 'Placa Base ASUS ROG Strix B650E-F Gaming WiFi AM5', 'PARTES', 1399900.00, TRUE),
(nextval('seq_producto'), 'PART-014', 'Placa Base MSI MAG Z790 Tomahawk WiFi LGA1700', 'PARTES', 1299900.00, TRUE),
(nextval('seq_producto'), 'PART-015', 'Memoria RAM Corsair Dominator Titanium 32GB (2x16GB) DDR5 6000MHz', 'PARTES', 799900.00, TRUE),
(nextval('seq_producto'), 'PART-016', 'Memoria RAM Kingston Fury Beast 32GB (2x16GB) DDR5 5600MHz', 'PARTES', 489900.00, TRUE),
(nextval('seq_producto'), 'PART-017', 'Disco Sólido SSD NVMe PCIe 4.0 Kingston KC3000 2TB 7000MB/s', 'PARTES', 699900.00, TRUE),
(nextval('seq_producto'), 'PART-018', 'Disco Sólido SSD NVMe PCIe 4.0 Samsung 990 Pro 2TB con Disipador', 'PARTES', 849900.00, TRUE),
(nextval('seq_producto'), 'PART-019', 'Fuente de Poder EVGA SuperNOVA 1000W 80 Plus Platinum Modular', 'PARTES', 929900.00, TRUE),
(nextval('seq_producto'), 'PART-020', 'Fuente de Poder Seasonic Focus GX-850 850W 80 Plus Gold Modular', 'PARTES', 649900.00, TRUE),
(nextval('seq_producto'), 'PART-021', 'Refrigeración Líquida AIO NZXT Kraken Elite 360 RGB LCD', 'PARTES', 1399900.00, TRUE),
(nextval('seq_producto'), 'PART-022', 'Gabinete Torre Corsair 5000D Airflow Vidrio Templado Blanco', 'PARTES', 699900.00, TRUE);

-- ------------------------------------------------------------------------------
-- 4. ACCESORIOS Y PERIFÉRICOS ADICIONALES
-- ------------------------------------------------------------------------------
INSERT INTO producto (id, sku, nombre, categoria, precio_unitario, activo) VALUES
(nextval('seq_producto'), 'ACC-005', 'Monitor Gamer Samsung Odyssey OLED G9 49" Curvo 240Hz Dual QHD', 'ACCESORIOS', 5999900.00, TRUE),
(nextval('seq_producto'), 'ACC-006', 'Monitor ASUS ROG Swift 27" OLED 240Hz 0.03ms QHD', 'ACCESORIOS', 3899900.00, TRUE),
(nextval('seq_producto'), 'ACC-007', 'Monitor Dell UltraSharp 32" 4K IPS Black Hub USB-C', 'ACCESORIOS', 3499900.00, TRUE),
(nextval('seq_producto'), 'ACC-008', 'Teclado Mecánico Inalámbrico Logitech G915 TKL Lightspeed RGB', 'ACCESORIOS', 899900.00, TRUE),
(nextval('seq_producto'), 'ACC-009', 'Mouse Gamer Inalámbrico Logitech G Pro X Superlight 2', 'ACCESORIOS', 629900.00, TRUE),
(nextval('seq_producto'), 'ACC-010', 'Mouse Gamer Razer DeathAdder V3 Pro Wireless 30K DPI', 'ACCESORIOS', 589900.00, TRUE),
(nextval('seq_producto'), 'ACC-011', 'Diadema Gamer Inalámbrica SteelSeries Arctis Nova Pro Wireless', 'ACCESORIOS', 1499900.00, TRUE),
(nextval('seq_producto'), 'ACC-012', 'Micrófono Profesional Shure MV7X XLR/USB para Podcast y Streaming', 'ACCESORIOS', 1199900.00, TRUE),
(nextval('seq_producto'), 'ACC-013', 'Cámara Web 4K Elgato Facecam Pro 4K60 Ultra HD', 'ACCESORIOS', 1399900.00, TRUE),
(nextval('seq_producto'), 'ACC-014', 'Silla Ergonómica Gamer Secretlab Titan Evo 2024 Tejido SoftWeave', 'ACCESORIOS', 2499900.00, TRUE),
(nextval('seq_producto'), 'ACC-015', 'Docking Station CalDigit TS4 Thunderbolt 4 18 Puertos', 'ACCESORIOS', 1799900.00, TRUE),
(nextval('seq_producto'), 'ACC-016', 'Regulador de Voltaje y UPS APC Smart-UPS 1500VA LCD 120V', 'ACCESORIOS', 2199900.00, TRUE);

-- ==============================================================================
-- 5. LOTES DE INVENTARIO CON DISTRIBUCIÓN FEFO REALISTA
-- ==============================================================================

-- A. Lotes con stock dividido (Multi-Lote) para LAP-005 (Lenovo Legion)
INSERT INTO inventario_lote (id, producto_id, codigo_lote, cantidad_disponible, fecha_ingreso, fecha_vencimiento)
SELECT nextval('seq_lote'), p.id, 'LOT-LEGION-01', 2, CURRENT_TIMESTAMP - INTERVAL '45 days', CURRENT_TIMESTAMP + INTERVAL '180 days' FROM producto p WHERE p.sku = 'LAP-005';
INSERT INTO inventario_lote (id, producto_id, codigo_lote, cantidad_disponible, fecha_ingreso, fecha_vencimiento)
SELECT nextval('seq_lote'), p.id, 'LOT-LEGION-02', 6, CURRENT_TIMESTAMP - INTERVAL '15 days', CURRENT_TIMESTAMP + INTERVAL '365 days' FROM producto p WHERE p.sku = 'LAP-005';

-- B. Lote de MacBook Pro 16 M3 Max (stock crítico: 1 unidad)
INSERT INTO inventario_lote (id, producto_id, codigo_lote, cantidad_disponible, fecha_ingreso, fecha_vencimiento)
SELECT nextval('seq_lote'), p.id, 'LOT-MBP16-CRITICO', 1, CURRENT_TIMESTAMP - INTERVAL '10 days', CURRENT_TIMESTAMP + INTERVAL '400 days' FROM producto p WHERE p.sku = 'LAP-006';

-- C. Lotes abundantes para Dell XPS 15 (LAP-007)
INSERT INTO inventario_lote (id, producto_id, codigo_lote, cantidad_disponible, fecha_ingreso, fecha_vencimiento)
SELECT nextval('seq_lote'), p.id, 'LOT-DELLXPS-A', 15, CURRENT_TIMESTAMP - INTERVAL '20 days', CURRENT_TIMESTAMP + INTERVAL '500 days' FROM producto p WHERE p.sku = 'LAP-007';

-- D. Lotes para Workstation DESK-004 (2 lotes FEFO)
INSERT INTO inventario_lote (id, producto_id, codigo_lote, cantidad_disponible, fecha_ingreso, fecha_vencimiento)
SELECT nextval('seq_lote'), p.id, 'LOT-PROART-L1', 3, CURRENT_TIMESTAMP - INTERVAL '30 days', CURRENT_TIMESTAMP + INTERVAL '300 days' FROM producto p WHERE p.sku = 'DESK-004';
INSERT INTO inventario_lote (id, producto_id, codigo_lote, cantidad_disponible, fecha_ingreso, fecha_vencimiento)
SELECT nextval('seq_lote'), p.id, 'LOT-PROART-L2', 5, CURRENT_TIMESTAMP - INTERVAL '5 days', CURRENT_TIMESTAMP + INTERVAL '600 days' FROM producto p WHERE p.sku = 'DESK-004';

-- E. Lote para PC Extreme RTX 4090 (DESK-005) (stock: 2)
INSERT INTO inventario_lote (id, producto_id, codigo_lote, cantidad_disponible, fecha_ingreso, fecha_vencimiento)
SELECT nextval('seq_lote'), p.id, 'LOT-PC4090-01', 2, CURRENT_TIMESTAMP - INTERVAL '12 days', CURRENT_TIMESTAMP + INTERVAL '365 days' FROM producto p WHERE p.sku = 'DESK-005';

-- F. Lotes abundantes para Mini PC NUC (DESK-006)
INSERT INTO inventario_lote (id, producto_id, codigo_lote, cantidad_disponible, fecha_ingreso, fecha_vencimiento)
SELECT nextval('seq_lote'), p.id, 'LOT-NUC-01', 20, CURRENT_TIMESTAMP - INTERVAL '8 days', CURRENT_TIMESTAMP + INTERVAL '700 days' FROM producto p WHERE p.sku = 'DESK-006';

-- G. Lotes para Procesador Core i9 14900K (PART-006) (Multi-lote: 4 + 8)
INSERT INTO inventario_lote (id, producto_id, codigo_lote, cantidad_disponible, fecha_ingreso, fecha_vencimiento)
SELECT nextval('seq_lote'), p.id, 'LOT-14900K-A', 4, CURRENT_TIMESTAMP - INTERVAL '50 days', CURRENT_TIMESTAMP + INTERVAL '250 days' FROM producto p WHERE p.sku = 'PART-006';
INSERT INTO inventario_lote (id, producto_id, codigo_lote, cantidad_disponible, fecha_ingreso, fecha_vencimiento)
SELECT nextval('seq_lote'), p.id, 'LOT-14900K-B', 8, CURRENT_TIMESTAMP - INTERVAL '10 days', CURRENT_TIMESTAMP + INTERVAL '500 days' FROM producto p WHERE p.sku = 'PART-006';

-- H. Lotes para Tarjeta Gráfica RTX 4090 (PART-009) (stock crítico: 1 unidad)
INSERT INTO inventario_lote (id, producto_id, codigo_lote, cantidad_disponible, fecha_ingreso, fecha_vencimiento)
SELECT nextval('seq_lote'), p.id, 'LOT-RTX4090-LIMITED', 1, CURRENT_TIMESTAMP - INTERVAL '5 days', CURRENT_TIMESTAMP + INTERVAL '300 days' FROM producto p WHERE p.sku = 'PART-009';

-- I. Lotes para Tarjeta Gráfica RTX 4080 Super (PART-010) (stock: 4)
INSERT INTO inventario_lote (id, producto_id, codigo_lote, cantidad_disponible, fecha_ingreso, fecha_vencimiento)
SELECT nextval('seq_lote'), p.id, 'LOT-RTX4080S-01', 4, CURRENT_TIMESTAMP - INTERVAL '15 days', CURRENT_TIMESTAMP + INTERVAL '400 days' FROM producto p WHERE p.sku = 'PART-010';

-- J. Lotes para Memoria DDR5 Corsair Titanium (PART-015) (abundante: 25)
INSERT INTO inventario_lote (id, producto_id, codigo_lote, cantidad_disponible, fecha_ingreso, fecha_vencimiento)
SELECT nextval('seq_lote'), p.id, 'LOT-DDR5-CORSAIR', 25, CURRENT_TIMESTAMP - INTERVAL '25 days', CURRENT_TIMESTAMP + INTERVAL '800 days' FROM producto p WHERE p.sku = 'PART-015';

-- K. Lotes para SSD Samsung 990 Pro 2TB (PART-018) (Multi-lote: 5 + 10)
INSERT INTO inventario_lote (id, producto_id, codigo_lote, cantidad_disponible, fecha_ingreso, fecha_vencimiento)
SELECT nextval('seq_lote'), p.id, 'LOT-990PRO-01', 5, CURRENT_TIMESTAMP - INTERVAL '40 days', CURRENT_TIMESTAMP + INTERVAL '350 days' FROM producto p WHERE p.sku = 'PART-018';
INSERT INTO inventario_lote (id, producto_id, codigo_lote, cantidad_disponible, fecha_ingreso, fecha_vencimiento)
SELECT nextval('seq_lote'), p.id, 'LOT-990PRO-02', 10, CURRENT_TIMESTAMP - INTERVAL '10 days', CURRENT_TIMESTAMP + INTERVAL '700 days' FROM producto p WHERE p.sku = 'PART-018';

-- L. Lotes para Fuente EVGA 1000W Platinum (PART-019) (stock: 6)
INSERT INTO inventario_lote (id, producto_id, codigo_lote, cantidad_disponible, fecha_ingreso, fecha_vencimiento)
SELECT nextval('seq_lote'), p.id, 'LOT-EVGA1000-01', 6, CURRENT_TIMESTAMP - INTERVAL '18 days', CURRENT_TIMESTAMP + INTERVAL '600 days' FROM producto p WHERE p.sku = 'PART-019';

-- M. Lotes para Enfriamiento Kraken 360 (PART-021) (stock: 8)
INSERT INTO inventario_lote (id, producto_id, codigo_lote, cantidad_disponible, fecha_ingreso, fecha_vencimiento)
SELECT nextval('seq_lote'), p.id, 'LOT-KRAKEN-01', 8, CURRENT_TIMESTAMP - INTERVAL '22 days', CURRENT_TIMESTAMP + INTERVAL '500 days' FROM producto p WHERE p.sku = 'PART-021';

-- N. Lote Monitor Samsung OLED G9 49" (ACC-005) (stock: 3)
INSERT INTO inventario_lote (id, producto_id, codigo_lote, cantidad_disponible, fecha_ingreso, fecha_vencimiento)
SELECT nextval('seq_lote'), p.id, 'LOT-G9OLED-01', 3, CURRENT_TIMESTAMP - INTERVAL '14 days', CURRENT_TIMESTAMP + INTERVAL '365 days' FROM producto p WHERE p.sku = 'ACC-005';

-- O. Lotes para Mouse Logitech G Pro Superlight 2 (ACC-009) (abundante: 30)
INSERT INTO inventario_lote (id, producto_id, codigo_lote, cantidad_disponible, fecha_ingreso, fecha_vencimiento)
SELECT nextval('seq_lote'), p.id, 'LOT-GPX2-01', 30, CURRENT_TIMESTAMP - INTERVAL '30 days', CURRENT_TIMESTAMP + INTERVAL '600 days' FROM producto p WHERE p.sku = 'ACC-009';

-- P. Lotes para Teclado Logitech G915 (ACC-008) (stock: 12)
INSERT INTO inventario_lote (id, producto_id, codigo_lote, cantidad_disponible, fecha_ingreso, fecha_vencimiento)
SELECT nextval('seq_lote'), p.id, 'LOT-G915-01', 12, CURRENT_TIMESTAMP - INTERVAL '20 days', CURRENT_TIMESTAMP + INTERVAL '500 days' FROM producto p WHERE p.sku = 'ACC-008';

-- Q. Producto intencionalmente SIN STOCK para pruebas de NO_FACTURADO (Monitor ASUS OLED ACC-006)
INSERT INTO inventario_lote (id, producto_id, codigo_lote, cantidad_disponible, fecha_ingreso, fecha_vencimiento)
SELECT nextval('seq_lote'), p.id, 'LOT-ASUSOLED-SIN-STOCK', 0, CURRENT_TIMESTAMP - INTERVAL '90 days', CURRENT_TIMESTAMP + INTERVAL '60 days' FROM producto p WHERE p.sku = 'ACC-006';
