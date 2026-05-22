package com.mycompany.motordesk.model;

public class OrdenTrabajo {
    private int idOrden;
    private String placaVehiculo;
    private String descripcion;
    private String docEmpleFk;
    private String estado;
    private String motivoEspera;
    private String tiempoEspera;
    private java.sql.Date fecha; // Changed to java.sql.Date to match diagram

    public OrdenTrabajo() {}

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
