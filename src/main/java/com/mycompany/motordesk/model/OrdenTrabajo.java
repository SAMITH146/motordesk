// Definición del paquete del proyecto
package com.mycompany.motordesk.model;

/**
 * Clase pública OrdenTrabajo que representa un servicio o reparación a realizar en el taller.
 * Agrupa los datos del vehículo, el mecánico asignado y el estado general del trabajo.
 */
public class OrdenTrabajo {
    // Identificador único de la orden de trabajo (llave primaria)
    private int idOrden;
    // Placa del vehículo al que se le realiza el trabajo
    private String placaVehiculo;
    // Descripción detallada del problema reportado o el trabajo a realizar
    private String descripcion;
    // Documento del empleado (mecánico) asignado a esta orden (llave foránea)
    private String docEmpleFk;
    // Estado actual de la orden (ej. 'En progreso', 'Finalizada', 'En espera')
    private String estado;
    // Motivo por el cual la orden está en espera, si aplica (ej. 'Falta de repuesto')
    private String motivoEspera;
    // Tiempo estimado o registrado de espera en la orden
    private String tiempoEspera;
    // Costo total de la orden de trabajo sumando servicios y repuestos
    private double total;
    // Fecha en la que se generó la orden de trabajo
    private java.sql.Date fecha; 
    // Identificador interno del vehículo en el sistema (llave foránea)
    private int idVehiculoFk; 
    // Atributo auxiliar para mostrar el nombre del mecánico asignado en las vistas
    private String nombreMecanico; 

    // Constructor por defecto (vacío)
    public OrdenTrabajo() {}

    // Método Getter para recuperar el valor de IdVehiculoFk
    public int getIdVehiculoFk() {
        // Retornar el valor obtenido
        return idVehiculoFk;
    }

    // Método Setter para establecer el valor de IdVehiculoFk
    public void setIdVehiculoFk(int idVehiculoFk) {
        this.idVehiculoFk = idVehiculoFk;
    }

    // Método Getter para recuperar el valor de NombreMecanico
    public String getNombreMecanico() {
        // Retornar el valor obtenido
        return nombreMecanico;
    }

    // Método Setter para establecer el valor de NombreMecanico
    public void setNombreMecanico(String nombreMecanico) {
        this.nombreMecanico = nombreMecanico;
    }

    // Método Getter para recuperar el valor de Total
    public double getTotal() {
        // Retornar el valor obtenido
        return total;
    }

    // Método Setter para establecer el valor de Total
    public void setTotal(double total) {
        this.total = total;
    }

    // Método Getter para recuperar el valor de IdOrden
    public int getIdOrden() {
        // Retornar el valor obtenido
        return idOrden;
    }

    // Método Setter para establecer el valor de IdOrden
    public void setIdOrden(int idOrden) {
        this.idOrden = idOrden;
    }

    // Método Getter para recuperar el valor de PlacaVehiculo
    public String getPlacaVehiculo() {
        // Retornar el valor obtenido
        return placaVehiculo;
    }

    // Método Setter para establecer el valor de PlacaVehiculo
    public void setPlacaVehiculo(String placaVehiculo) {
        this.placaVehiculo = placaVehiculo;
    }

    // Método Getter para recuperar el valor de Descripcion
    public String getDescripcion() {
        // Retornar el valor obtenido
        return descripcion;
    }

    // Método Setter para establecer el valor de Descripcion
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    // Método Getter para recuperar el valor de DocEmpleFk
    public String getDocEmpleFk() {
        // Retornar el valor obtenido
        return docEmpleFk;
    }

    // Método Setter para establecer el valor de DocEmpleFk
    public void setDocEmpleFk(String docEmpleFk) {
        this.docEmpleFk = docEmpleFk;
    }

    // Método Getter para recuperar el valor de Estado
    public String getEstado() {
        // Retornar el valor obtenido
        return estado;
    }

    // Método Setter para establecer el valor de Estado
    public void setEstado(String estado) {
        this.estado = estado;
    }

    // Método Getter para recuperar el valor de MotivoEspera
    public String getMotivoEspera() {
        // Retornar el valor obtenido
        return motivoEspera;
    }

    // Método Setter para establecer el valor de MotivoEspera
    public void setMotivoEspera(String motivoEspera) {
        this.motivoEspera = motivoEspera;
    }

    // Método Getter para recuperar el valor de TiempoEspera
    public String getTiempoEspera() {
        // Retornar el valor obtenido
        return tiempoEspera;
    }

    // Método Setter para establecer el valor de TiempoEspera
    public void setTiempoEspera(String tiempoEspera) {
        this.tiempoEspera = tiempoEspera;
    }

    // Método Getter para recuperar el valor de Fecha
    public java.sql.Date getFecha() {
        // Retornar el valor obtenido
        return fecha;
    }

    // Método Setter para establecer el valor de Fecha
    public void setFecha(java.sql.Date fecha) {
        this.fecha = fecha;
    }
}
