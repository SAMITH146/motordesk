package com.mycompany.motordesk.model;

public class DetalleCompra {
    private int idDetalleCompra;
    private int idCompraFk;
    private int idRepuestoFk;
    private int cantidad;
    private double costoUnitario;

    // Auxiliar para mostrar el nombre del repuesto en las vistas
    private String nombreRepuesto;
    
    // Auxiliar para establecer el nuevo precio de venta al público en la tabla de productos
    private double nuevoPrecioVenta;

    public DetalleCompra() {
    }

    public DetalleCompra(int idDetalleCompra, int idCompraFk, int idRepuestoFk, int cantidad, double costoUnitario) {
        this.idDetalleCompra = idDetalleCompra;
        this.idCompraFk = idCompraFk;
        this.idRepuestoFk = idRepuestoFk;
        this.cantidad = cantidad;
        this.costoUnitario = costoUnitario;
    }

    public int getIdDetalleCompra() {
        return idDetalleCompra;
    }

    public void setIdDetalleCompra(int idDetalleCompra) {
        this.idDetalleCompra = idDetalleCompra;
    }

    public int getIdCompraFk() {
        return idCompraFk;
    }

    public void setIdCompraFk(int idCompraFk) {
        this.idCompraFk = idCompraFk;
    }

    public int getIdRepuestoFk() {
        return idRepuestoFk;
    }

    public void setIdRepuestoFk(int idRepuestoFk) {
        this.idRepuestoFk = idRepuestoFk;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public double getCostoUnitario() {
        return costoUnitario;
    }

    public void setCostoUnitario(double costoUnitario) {
        this.costoUnitario = costoUnitario;
    }

    public String getNombreRepuesto() {
        return nombreRepuesto;
    }

    public void setNombreRepuesto(String nombreRepuesto) {
        this.nombreRepuesto = nombreRepuesto;
    }

    public double getNuevoPrecioVenta() {
        return nuevoPrecioVenta;
    }

    public void setNuevoPrecioVenta(double nuevoPrecioVenta) {
        this.nuevoPrecioVenta = nuevoPrecioVenta;
    }
}
