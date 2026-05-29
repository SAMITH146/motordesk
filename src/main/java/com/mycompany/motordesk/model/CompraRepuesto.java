package com.mycompany.motordesk.model;

import java.util.Date;

public class CompraRepuesto {
    private int idCompra;
    private int idProveedorFk;
    private Date fechaCompra;
    private double total;

    // Auxiliar para mostrar informacion en las vistas
    private String nombreProveedor;

    public CompraRepuesto() {
    }

    public CompraRepuesto(int idCompra, int idProveedorFk, Date fechaCompra, double total) {
        this.idCompra = idCompra;
        this.idProveedorFk = idProveedorFk;
        this.fechaCompra = fechaCompra;
        this.total = total;
    }

    public int getIdCompra() {
        return idCompra;
    }

    public void setIdCompra(int idCompra) {
        this.idCompra = idCompra;
    }

    public int getIdProveedorFk() {
        return idProveedorFk;
    }

    public void setIdProveedorFk(int idProveedorFk) {
        this.idProveedorFk = idProveedorFk;
    }

    public Date getFechaCompra() {
        return fechaCompra;
    }

    public void setFechaCompra(Date fechaCompra) {
        this.fechaCompra = fechaCompra;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public String getNombreProveedor() {
        return nombreProveedor;
    }

    public void setNombreProveedor(String nombreProveedor) {
        this.nombreProveedor = nombreProveedor;
    }
}
