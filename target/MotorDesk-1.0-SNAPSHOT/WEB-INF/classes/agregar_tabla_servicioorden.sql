-- ============================================================
-- SCRIPT: Agregar tabla servicioorden a la BD de MotorDesk
-- Propósito: Permitir múltiples líneas de servicio (mano de obra)
--            dentro de una misma orden de trabajo.
-- Ejecutar en: MySQL Workbench / phpMyAdmin / terminal MySQL
-- ============================================================

-- Crear la tabla de servicios de mano de obra por orden
CREATE TABLE IF NOT EXISTS servicioorden (
    id_servicio   INT AUTO_INCREMENT PRIMARY KEY,
    id_orden_fk   INT          NOT NULL,
    nombre        VARCHAR(200) NOT NULL  COMMENT 'Nombre del servicio, ej: Despinche, Revision suspension',
    valor         DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT 'Valor de la mano de obra para este servicio',
    CONSTRAINT fk_servicio_orden
        FOREIGN KEY (id_orden_fk)
        REFERENCES ordentrabajo(id_orden)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Servicios de mano de obra por orden de trabajo';
