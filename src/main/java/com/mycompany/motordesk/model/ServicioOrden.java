package com.mycompany.motordesk.model;

/**
 * Clase pública ServicioOrden — representa la relación entre una Orden de Trabajo y un Servicio específico del catálogo de la base de datos.
 * Sirve para detallar qué servicios exactos se aplicaron a una orden particular (Normalización 3NF).
 */
public class ServicioOrden {

    // Identificador único de este registro o detalle de servicio (llave primaria autogenerada)
    private int idServicio;
    // Identificador de la orden de trabajo a la que se le aplicó el servicio (llave foránea)
    private int idOrdenFk;
    // Identificador del servicio del catálogo maestro que se realizó (llave foránea)
    private int idServicioFk;
    // Valor real que se cobró al cliente en esta orden por el servicio (puede diferir del precio estándar)
    private double valorCobrado;
    // Campo de apoyo o auxiliar: nombre del servicio (se llena dinámicamente mediante una consulta JOIN en la base de datos)
    private String nombre;

    // Constructor por defecto (vacío)
    public ServicioOrden() {}

    // Constructor de conveniencia
    public ServicioOrden(int idOrdenFk, int idServicioFk, double valorCobrado) {
        this.idOrdenFk = idOrdenFk;
        this.idServicioFk = idServicioFk;
        this.valorCobrado = valorCobrado;
    }

    // ---- Getters y Setters ----

    public int getIdServicio() { return idServicio; }
    public void setIdServicio(int idServicio) { this.idServicio = idServicio; }

    public int getIdOrdenFk() { return idOrdenFk; }
    public void setIdOrdenFk(int idOrdenFk) { this.idOrdenFk = idOrdenFk; }

    public int getIdServicioFk() { return idServicioFk; }
    public void setIdServicioFk(int idServicioFk) { this.idServicioFk = idServicioFk; }

    public double getValorCobrado() { return valorCobrado; }
    public void setValorCobrado(double valorCobrado) { this.valorCobrado = valorCobrado; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
}
