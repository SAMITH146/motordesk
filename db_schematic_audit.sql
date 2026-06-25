-- =============================================================================
-- BASE DE DATOS: MotorDesk (Esquema Relacional Estándar - 3NF Completo)
-- Compatible con versiones antiguas de MySQL (Sin condicionales IF NOT EXISTS)
-- =============================================================================
-- Este script define la estructura física definitiva de la base de datos de 
-- MotorDesk con todas las restricciones de integridad (FOREIGN KEY) y las reglas 
-- de borrado/actualización seguras (RESTRICT, CASCADE, SET NULL) para garantizar 
-- la consistencia contable y de negocio.
--
-- Ejecutar en: MySQL Workbench / phpMyAdmin / Consola MySQL.
-- =============================================================================

CREATE DATABASE motordesk CHARACTER SET utf8 COLLATE utf8_general_ci;
USE motordesk;

-- Desactivar temporalmente restricciones para poder crear sin importar el orden
SET FOREIGN_KEY_CHECKS = 0;


-- =============================================================================
-- 1. TABLAS MAESTRAS (INDEPENDIENTES)
-- =============================================================================

-- A) TABLA: roles
-- Define el perfil del usuario para el control de acceso en la aplicación
CREATE TABLE roles (
    id_rol INT AUTO_INCREMENT PRIMARY KEY,
    nombre_rol VARCHAR(50) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- B) TABLA: cargo
-- Define el cargo laboral del empleado en el taller
CREATE TABLE cargo (
    id_cargo INT AUTO_INCREMENT PRIMARY KEY,
    desc_cargo VARCHAR(100) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- C) TABLA: proveedor
-- Proveedores autorizados de repuestos para el reabastecimiento de inventario
CREATE TABLE proveedor (
    id_proveedor INT AUTO_INCREMENT PRIMARY KEY,
    nombre_proveedor VARCHAR(150) NOT NULL UNIQUE,
    contacto VARCHAR(100) NULL,
    telefono VARCHAR(20) NULL,
    correo VARCHAR(100) NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- D) TABLA: producto (Repuestos e Insumos)
-- Catálogo maestro de productos con control de existencias
CREATE TABLE producto (
    id_producto INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    precio DECIMAL(12,2) NOT NULL,
    stock INT NOT NULL,
    categoria VARCHAR(50) NULL,
    estado VARCHAR(20) NULL,
    tipo_vehiculo VARCHAR(50) NULL,
    seccion VARCHAR(50) NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- E) TABLA: servicio
-- Catálogo maestro de servicios prestados (mano de obra)
CREATE TABLE servicio (
    id_servicio INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(200) NOT NULL UNIQUE,
    precio_estandar DECIMAL(12,2) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- F) TABLA: cliente
-- Datos personales de dueños de vehículos
CREATE TABLE cliente (
    id_cliente INT AUTO_INCREMENT PRIMARY KEY,
    nom_cliente VARCHAR(150) NOT NULL,
    doc_cliente VARCHAR(20) NOT NULL UNIQUE,
    direccion_cliente VARCHAR(255) NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- G) TABLA: empleado_historico
-- Tabla de persistencia histórica de nombres y documentos de empleados.
-- Evita que al eliminar un usuario de 'empleado', la contabilidad/ordenes queden huerfanas.
CREATE TABLE empleado_historico (
    doc_emple VARCHAR(20) PRIMARY KEY,
    nom_empleado VARCHAR(150) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


-- =============================================================================
-- 2. TABLAS DEPENDIENTES / RELACIONALES (CON VALIDADORES DE INTEGRIDAD)
-- =============================================================================

-- H) TABLA: empleado (Usuarios del sistema)
-- Vinculado a roles y cargos laborales
CREATE TABLE empleado (
    doc_emple VARCHAR(20) PRIMARY KEY,
    nom_empleado VARCHAR(100) NOT NULL,
    id_cargo_fk INT NOT NULL,
    id_rol_fk INT NOT NULL,
    pin_acceso VARCHAR(20) NOT NULL,
    estado_empleado VARCHAR(20) DEFAULT 'Activo',
    fecha_ingreso DATE NOT NULL,
    
    CONSTRAINT fk_empleado_cargo 
        FOREIGN KEY (id_cargo_fk) REFERENCES cargo(id_cargo)
        ON DELETE RESTRICT ON UPDATE CASCADE,
        
    CONSTRAINT fk_empleado_rol 
        FOREIGN KEY (id_rol_fk) REFERENCES roles(id_rol)
        ON DELETE RESTRICT ON UPDATE CASCADE,
        
    CONSTRAINT fk_empleado_a_historico 
        FOREIGN KEY (doc_emple) REFERENCES empleado_historico(doc_emple)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- I) TABLA: vehiculo
-- Registrado a nombre de un cliente
CREATE TABLE vehiculo (
    id_vehiculo INT AUTO_INCREMENT PRIMARY KEY,
    id_cliente_fk INT NOT NULL,
    placa VARCHAR(10) NOT NULL UNIQUE,
    marca VARCHAR(50) NOT NULL,
    modelo VARCHAR(50) NOT NULL,
    anio INT NULL,
    tipo_vehiculo VARCHAR(50) NULL,
    
    CONSTRAINT fk_vehiculo_cliente 
        FOREIGN KEY (id_cliente_fk) REFERENCES cliente(id_cliente)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- J) TABLA: ordentrabajo (Cabecera de Orden)
-- Registra el diagnóstico del vehículo, asociado al mecánico histórico.
CREATE TABLE ordentrabajo (
    id_orden INT AUTO_INCREMENT PRIMARY KEY,
    id_vehiculo_fk INT NOT NULL,
    doc_emple_fk VARCHAR(20) NULL,
    estado VARCHAR(30) NOT NULL DEFAULT 'ABIERTA',
    descripcion TEXT NULL,
    total DECIMAL(12,2) DEFAULT 0.00,
    placa_vehiculo VARCHAR(10) NOT NULL,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    motivo_espera VARCHAR(255) NULL,
    tiempo_espera VARCHAR(100) NULL,
    
    CONSTRAINT fk_orden_vehiculo 
        FOREIGN KEY (id_vehiculo_fk) REFERENCES vehiculo(id_vehiculo)
        ON DELETE RESTRICT ON UPDATE CASCADE,
        
    CONSTRAINT fk_orden_empleado_hist 
        FOREIGN KEY (doc_emple_fk) REFERENCES empleado_historico(doc_emple)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- K) TABLA: detalleorden (Repuestos e Insumos usados en la Orden - Intersección M:N)
-- Si la orden se elimina, sus detalles de insumos se eliminan en cascada.
CREATE TABLE detalleorden (
    id_detalle INT AUTO_INCREMENT PRIMARY KEY,
    id_orden_fk INT NOT NULL,
    id_repuesto_fk INT NOT NULL,
    cantidad INT NOT NULL,
    subtotal DECIMAL(12,2) NOT NULL,
    
    CONSTRAINT fk_detalle_orden 
        FOREIGN KEY (id_orden_fk) REFERENCES ordentrabajo(id_orden)
        ON DELETE CASCADE ON UPDATE CASCADE,
        
    CONSTRAINT fk_detalle_producto 
        FOREIGN KEY (id_repuesto_fk) REFERENCES producto(id_producto)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- L) TABLA: servicioorden (Servicios/Mano de obra aplicados en la Orden - Intersección M:N)
-- Si la orden se elimina, sus detalles de servicios se eliminan en cascada.
CREATE TABLE servicioorden (
    id_servicioorden INT AUTO_INCREMENT PRIMARY KEY,
    id_orden_fk INT NOT NULL,
    id_servicio_fk INT NOT NULL,
    valor_cobrado DECIMAL(12,2) NOT NULL,
    
    CONSTRAINT fk_servicioorden_orden 
        FOREIGN KEY (id_orden_fk) REFERENCES ordentrabajo(id_orden)
        ON DELETE CASCADE ON UPDATE CASCADE,
        
    CONSTRAINT fk_servicioorden_servicio 
        FOREIGN KEY (id_servicio_fk) REFERENCES servicio(id_servicio)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- M) TABLA: factura
-- Registro de facturación independiente del cobro final.
-- Si la orden se elimina, la factura se elimina en cascada.
-- El empleado (cajero) está enlazado a empleado_historico para que la factura siga existiendo contablemente.
CREATE TABLE factura (
    id_factura INT AUTO_INCREMENT PRIMARY KEY,
    id_orden_fk INT NOT NULL UNIQUE,
    doc_emple_fk VARCHAR(20) NULL,
    numero_factura VARCHAR(50) NOT NULL,
    fecha_emision TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    subtotal DECIMAL(12,2) NOT NULL,
    iva DECIMAL(12,2) NOT NULL,
    total DECIMAL(12,2) NOT NULL,
    metodo_pago VARCHAR(50) NOT NULL,
    estado VARCHAR(30) DEFAULT 'PAGADA',
    
    CONSTRAINT fk_factura_orden 
        FOREIGN KEY (id_orden_fk) REFERENCES ordentrabajo(id_orden)
        ON DELETE CASCADE ON UPDATE CASCADE,
        
    CONSTRAINT fk_factura_empleado 
        FOREIGN KEY (doc_emple_fk) REFERENCES empleado_historico(doc_emple)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- N) TABLA: comprarepuesto (Compras de Inventario)
-- Registra compras generales de insumos a proveedores
CREATE TABLE comprarepuesto (
    id_compra INT AUTO_INCREMENT PRIMARY KEY,
    id_proveedor_fk INT NOT NULL,
    fecha_compra DATE NOT NULL,
    total DECIMAL(12,2) NOT NULL,
    
    CONSTRAINT fk_compra_proveedor 
        FOREIGN KEY (id_proveedor_fk) REFERENCES proveedor(id_proveedor)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- O) TABLA: detallecompra (Detalles de Compras - Intersección M:N)
-- Si la compra principal se borra, sus detalles se borran en cascada.
CREATE TABLE detallecompra (
    id_detalle INT AUTO_INCREMENT PRIMARY KEY,
    id_compra_fk INT NOT NULL,
    id_repuesto_fk INT NOT NULL,
    cantidad INT NOT NULL,
    costo_unitario DECIMAL(12,2) NOT NULL,
    
    CONSTRAINT fk_detallecompra_compra 
        FOREIGN KEY (id_compra_fk) REFERENCES comprarepuesto(id_compra)
        ON DELETE CASCADE ON UPDATE CASCADE,
        
    CONSTRAINT fk_detallecompra_producto 
        FOREIGN KEY (id_repuesto_fk) REFERENCES producto(id_producto)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- P) TABLA: bitacora (Registro de auditoría del sistema)
-- Guarda las acciones críticas. Si se borra el histórico del empleado, la bitácora persiste 
-- y el campo se establece en NULL, manteniendo el registro y nombre del usuario histórico.
CREATE TABLE bitacora (
    id_bitacora INT AUTO_INCREMENT PRIMARY KEY,
    doc_emple_fk VARCHAR(20) NULL,
    nombre_usuario VARCHAR(150) NULL,
    accion VARCHAR(255) NOT NULL,
    detalle TEXT NULL,
    fecha_hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_bitacora_empleado 
        FOREIGN KEY (doc_emple_fk) REFERENCES empleado_historico(doc_emple)
        ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


-- Volver a activar las restricciones de base de datos
SET FOREIGN_KEY_CHECKS = 1;


-- =============================================================================
-- DIAGNÓSTICO E INSERCIONES DE PRUEBA BÁSICAS
-- =============================================================================
-- Inserción de Roles iniciales
INSERT INTO roles (id_rol, nombre_rol) VALUES 
(1, 'Administrador'), 
(2, 'Mecanico');

-- Inserción de Cargos iniciales
INSERT INTO cargo (id_cargo, desc_cargo) VALUES 
(1, 'Administrador'), 
(2, 'Mecanico General');
