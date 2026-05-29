USE motordesk;

-- 1. Table for Spare Parts (Coherent with Repuesto table in diagram)
-- Note: 'Inventario' is a separate table in the diagram, but we keep stock here for simplicity if preferred, 
-- or you can use your current 'Producto' table renamed to 'Repuesto'.
CREATE TABLE IF NOT EXISTS repuesto (
    id_repuesto INT AUTO_INCREMENT PRIMARY KEY,
    nombre_repuesto VARCHAR(100) NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    descripcion VARCHAR(150),
    stock INT DEFAULT 0 -- Added for functionality, though diagram shows a separate 'inventario' table
);

-- 2. Orders Table (Matching 'ordentrabajo' in diagram)
CREATE TABLE IF NOT EXISTS ordentrabajo (
    id_orden INT AUTO_INCREMENT PRIMARY KEY,
    id_vehiculo_fk INT, -- Links to 'vehiculo' table
    doc_emple_fk VARCHAR(20) NOT NULL, -- Links to 'empleado' (VARCHAR)
    fecha DATE DEFAULT (CURRENT_DATE),
    estado VARCHAR(30) DEFAULT 'ABIERTA',
    total DECIMAL(10,2) DEFAULT 0.00,
    descripcion VARCHAR(150),
    CONSTRAINT fk_orden_empleado FOREIGN KEY (doc_emple_fk) REFERENCES empleado(doc_emple) ON DELETE CASCADE
);

-- 3. Order Details Table (Matching 'detalleorden' in diagram)
CREATE TABLE IF NOT EXISTS detalleorden (
    id_detalle INT AUTO_INCREMENT PRIMARY KEY,
    id_orden_fk INT NOT NULL,
    id_servicio_fk INT, -- Optional link to 'servicio' table
    id_repuesto_fk INT, -- Link to 'repuesto' table
    cantidad INT NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_detalle_orden_master FOREIGN KEY (id_orden_fk) REFERENCES ordentrabajo(id_orden) ON DELETE CASCADE,
    CONSTRAINT fk_detalle_repuesto FOREIGN KEY (id_repuesto_fk) REFERENCES repuesto(id_repuesto)
);
