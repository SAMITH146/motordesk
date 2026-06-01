package com.mycompany.motordesk.model;

public class OrdenTrabajo {
    private int idOrden;
    private String placaVehiculo;
    private String descripcion;
    private String docEmpleFk;
    private String estado;
    private String motivoEspera;
    private String tiempoEspera;
    private double total;
    private java.sql.Date fecha; // Changed to java.sql.Date to match diagram
    private int idVehiculoFk; // Added to link to Vehiculo table
    private String nombreMecanico; // Added to show mechanic name in views

    public OrdenTrabajo() {}

    public int getIdVehiculoFk() {
        return idVehiculoFk;
    }

    public void setIdVehiculoFk(int idVehiculoFk) {
        this.idVehiculoFk = idVehiculoFk;
    }

    public String getNombreMecanico() {
        return nombreMecanico;
    }

    public void setNombreMecanico(String nombreMecanico) {
        this.nombreMecanico = nombreMecanico;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public int getIdOrden() {
        return idOrden;
    }

    public void setIdOrden(int idOrden) {
        this.idOrden = idOrden;
    }

    public String getPlacaVehiculo() {
        return placaVehiculo;
    }

    public void setPlacaVehiculo(String placaVehiculo) {
        this.placaVehiculo = placaVehiculo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDocEmpleFk() {
        return docEmpleFk;
    }

    public void setDocEmpleFk(String docEmpleFk) {
        this.docEmpleFk = docEmpleFk;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getMotivoEspera() {
        return motivoEspera;
    }

    public void setMotivoEspera(String motivoEspera) {
        this.motivoEspera = motivoEspera;
    }

    public String getTiempoEspera() {
        return tiempoEspera;
    }

    public void setTiempoEspera(String tiempoEspera) {
        this.tiempoEspera = tiempoEspera;
    }

    public java.sql.Date getFecha() {
        return fecha;
    }

    public void setFecha(java.sql.Date fecha) {
        this.fecha = fecha;
    }
}
