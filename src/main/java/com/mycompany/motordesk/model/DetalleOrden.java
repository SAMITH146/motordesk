package com.mycompany.motordesk.model;

public class DetalleOrden {
    private int idDetalle;
    private int idOrdenFk;
    private int idProductoFk;
    private int cantidad;
    private double subtotal;
    
    // Auxiliary field for display
    private String nombreProducto;

    public DetalleOrden() {}

    public int getIdDetalle() {
        return idDetalle;
    }

    public void setIdDetalle(int idDetalle) {
        this.idDetalle = idDetalle;
    }

    public int getIdOrdenFk() {
        return idOrdenFk;
    }

    public void setIdOrdenFk(int idOrdenFk) {
        this.idOrdenFk = idOrdenFk;
    }

    public int getIdProductoFk() {
        return idProductoFk;
    }

    public void setIdProductoFk(int idProductoFk) {
        this.idProductoFk = idProductoFk;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }
}
