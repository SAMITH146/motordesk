package com.mycompany.motordesk.model;

/**
 * Clase Servicio: representa un tipo de trabajo que el taller ofrece a sus clientes en el catálogo maestro.
 * Ejemplos: "Cambio de aceite", "Alineación y balanceo", "Revisión general".
 */
public class Servicio {
    // Identificador único del servicio en el catálogo (llave primaria)
    private int idServicio;
    // Nombre descriptivo del servicio a realizar
    private String nombre;
    // Precio base o estándar que se cobra generalmente por este servicio
    private double precioEstandar;

    public Servicio() {}

    public Servicio(int idServicio, String nombre, double precioEstandar) {
        this.idServicio = idServicio;
        this.nombre = nombre;
        this.precioEstandar = precioEstandar;
    }

    public int getIdServicio() { return idServicio; }
    public void setIdServicio(int idServicio) { this.idServicio = idServicio; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public double getPrecioEstandar() { return precioEstandar; }
    public void setPrecioEstandar(double precioEstandar) { this.precioEstandar = precioEstandar; }
}
