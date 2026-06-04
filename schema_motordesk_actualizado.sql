-- ============================================================
-- SCRIPT DE RECONSTRUCCIÓN DE BASE DE DATOS: motordesk
-- ESTRUCTURA LIMPIA Y ACTUALIZADA CON EL BACKEND JAVA
-- ============================================================
DROP DATABASE IF EXISTS motordesk;
CREATE DATABASE motordesk;
USE motordesk;

-- 1. Tabla de Roles
CREATE TABLE IF NOT EXISTS roles (
    id_rol INT AUTO_INCREMENT PRIMARY KEY,
    nombre_rol VARCHAR(50) NOT NULL
);

-- 2. Tabla de Cargos
CREATE TABLE IF NOT EXISTS cargo (
    id_cargo INT AUTO_INCREMENT PRIMARY KEY,
    desc_cargo VARCHAR(100) NOT NULL
);

-- 3. Tabla de Empleados
CREATE TABLE IF NOT EXISTS empleado (
    doc_emple VARCHAR(20) PRIMARY KEY,
    nom_empleado VARCHAR(100) NOT NULL,
    id_cargo_fk INT,
    id_rol_fk INT,
    pin_acceso VARCHAR(20) NOT NULL,
    estado_empleado VARCHAR(20) DEFAULT 'ACTIVO',
    fecha_ingreso DATE,
    FOREIGN KEY (id_rol_fk) REFERENCES roles(id_rol) ON DELETE SET NULL,
    FOREIGN KEY (id_cargo_fk) REFERENCES cargo(id_cargo) ON DELETE SET NULL
);

-- 4. Tabla de Clientes
CREATE TABLE IF NOT EXISTS cliente (
    id_cliente INT AUTO_INCREMENT PRIMARY KEY,
    nom_cliente VARCHAR(100) NOT NULL,
    doc_cliente VARCHAR(20) UNIQUE NOT NULL,
    direccion_cliente VARCHAR(150)
);

-- 5. Tabla de Vehículos
CREATE TABLE IF NOT EXISTS vehiculo (
    id_vehiculo INT AUTO_INCREMENT PRIMARY KEY,
    id_cliente_fk INT NOT NULL,
    placa VARCHAR(10) UNIQUE NOT NULL,
    marca VARCHAR(50) NOT NULL,
    modelo VARCHAR(50) NOT NULL,
    anio INT NOT NULL,
    FOREIGN KEY (id_cliente_fk) REFERENCES cliente(id_cliente) ON DELETE CASCADE
);

-- 6. Tabla de Productos (Catálogo unificado para Repuestos/Productos con Stock)
CREATE TABLE IF NOT EXISTS producto (
    id_producto INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    categoria VARCHAR(50) DEFAULT 'General',
    stock INT DEFAULT 0,
    precio DECIMAL(10,2) NOT NULL,
    estado VARCHAR(20) DEFAULT 'Activo',
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    tipo_vehiculo VARCHAR(50),
    seccion VARCHAR(50)
);

-- 7. Tabla de Órdenes de Trabajo
CREATE TABLE IF NOT EXISTS ordentrabajo (
    id_orden INT AUTO_INCREMENT PRIMARY KEY,
    id_vehiculo_fk INT NULL,
    doc_emple_fk VARCHAR(20) NOT NULL,
    fecha DATE DEFAULT (CURRENT_DATE),
    estado VARCHAR(30) DEFAULT 'ABIERTA',
    total DECIMAL(10,2) DEFAULT 0.00,
    descripcion TEXT,
    placa_vehiculo VARCHAR(10),
    motivo_espera VARCHAR(255) NULL,
    tiempo_espera VARCHAR(100) NULL,
    FOREIGN KEY (id_vehiculo_fk) REFERENCES vehiculo(id_vehiculo) ON DELETE SET NULL,
    FOREIGN KEY (doc_emple_fk) REFERENCES empleado(doc_emple) ON DELETE CASCADE
);

-- 8. Tabla de Detalles de las Órdenes de Trabajo
CREATE TABLE IF NOT EXISTS detalleorden (
    id_detalle INT AUTO_INCREMENT PRIMARY KEY,
    id_orden_fk INT NOT NULL,
    id_servicio_fk INT NULL,
    id_repuesto_fk INT NOT NULL, -- Apunta a producto.id_producto
    cantidad INT NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (id_orden_fk) REFERENCES ordentrabajo(id_orden) ON DELETE CASCADE,
    FOREIGN KEY (id_repuesto_fk) REFERENCES producto(id_producto)
);

-- 9. Tabla de Proveedores
CREATE TABLE IF NOT EXISTS proveedor (
    id_proveedor INT AUTO_INCREMENT PRIMARY KEY,
    nombre_proveedor VARCHAR(100) NOT NULL,
    contacto VARCHAR(100) NULL,
    telefono VARCHAR(20) NULL,
    correo VARCHAR(100) NULL
);

-- 10. Tabla de Compras de Repuestos (Cabecera)
CREATE TABLE IF NOT EXISTS comprarepuesto (
    id_compra INT AUTO_INCREMENT PRIMARY KEY,
    id_proveedor_fk INT NOT NULL,
    fecha_compra DATE DEFAULT (CURRENT_DATE),
    total DECIMAL(10,2) DEFAULT 0.00,
    FOREIGN KEY (id_proveedor_fk) REFERENCES proveedor(id_proveedor) ON DELETE CASCADE
);

-- 11. Tabla de Detalles de Compras de Repuestos
CREATE TABLE IF NOT EXISTS detallecompra (
    id_detalle_compra INT AUTO_INCREMENT PRIMARY KEY,
    id_compra_fk INT NOT NULL,
    id_repuesto_fk INT NOT NULL, -- Apunta a producto.id_producto
    cantidad INT NOT NULL,
    costo_unitario DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (id_compra_fk) REFERENCES comprarepuesto(id_compra) ON DELETE CASCADE,
    FOREIGN KEY (id_repuesto_fk) REFERENCES producto(id_producto)
);


-- ============================================================
-- INSERT DE DATOS SEMILLA INICIALES
-- ============================================================

-- Inserción de Roles
INSERT INTO roles (id_rol, nombre_rol) VALUES 
(1, 'Administrador'),
(2, 'Mecanico')
ON DUPLICATE KEY UPDATE nombre_rol=VALUES(nombre_rol);

-- Inserción de Cargos
INSERT INTO cargo (id_cargo, desc_cargo) VALUES 
(1, 'Administrador de Taller'),
(2, 'Mecanico Principal')
ON DUPLICATE KEY UPDATE desc_cargo=VALUES(desc_cargo);

-- Inserción de Empleado Administrador Inicial (PIN: 1234)
INSERT INTO empleado (doc_emple, nom_empleado, id_cargo_fk, id_rol_fk, pin_acceso, estado_empleado, fecha_ingreso) VALUES 
('10001', 'Administrador General', 1, 1, '1234', 'ACTIVO', CURDATE())
ON DUPLICATE KEY UPDATE nom_empleado=VALUES(nom_empleado), pin_acceso=VALUES(pin_acceso);

-- Inserción de Empleado Mecánico Inicial (PIN: 4321)
INSERT INTO empleado (doc_emple, nom_empleado, id_cargo_fk, id_rol_fk, pin_acceso, estado_empleado, fecha_ingreso) VALUES 
('10002', 'Mecanico Juan', 2, 2, '4321', 'ACTIVO', CURDATE())
ON DUPLICATE KEY UPDATE nom_empleado=VALUES(nom_empleado), pin_acceso=VALUES(pin_acceso);

-- Inserción de un Proveedor por defecto
INSERT INTO proveedor (id_proveedor, nombre_proveedor, contacto, telefono, correo) VALUES 
(1, 'Autopartes General S.A.S', 'Carlos Gómez', '3001234567', 'contacto@autopartesgeneral.com')
ON DUPLICATE KEY UPDATE nombre_proveedor=VALUES(nombre_proveedor);

-- Inserción de algunos Productos / Repuestos de prueba
INSERT INTO producto (id_producto, nombre, categoria, stock, precio, estado, tipo_vehiculo, seccion) VALUES
(1, 'Aceite de Motor 10W-30', 'Lubricantes', 20, 25000.00, 'Activo', 'Automóvil', 'Mantenimiento'),
(2, 'Pastillas de Freno Delanteras', 'Frenos', 15, 85000.00, 'Activo', 'Universal', 'Frenos'),
(3, 'Filtro de Aire de Motor', 'Filtros', 30, 18000.00, 'Activo', 'Universal', 'Mantenimiento'),
(4, 'Bujía de Iridio', 'Encendido', 50, 15000.00, 'Activo', 'Universal', 'Encendido'),
(5, 'Batería 12V 800AMP', 'Eléctrico', 8, 320000.00, 'Activo', 'Universal', 'Eléctrico')
ON DUPLICATE KEY UPDATE nombre=VALUES(nombre), stock=VALUES(stock), precio=VALUES(precio);
