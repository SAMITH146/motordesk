// Definición del paquete del proyecto
package com.mycompany.motordesk.model;

/**
 * Clase pública DetalleCompra que gestiona la lógica correspondiente a los detalles de una compra.
 * Representa cada uno de los repuestos o productos específicos adquiridos en una compra general a un proveedor.
 */
public class DetalleCompra {
    // Identificador único del detalle de compra (llave primaria)
    private int idDetalleCompra;
    // Identificador de la compra a la que pertenece este detalle (llave foránea)
    private int idCompraFk;
    // Identificador del repuesto o producto adquirido (llave foránea)
    private int idRepuestoFk;
    // Cantidad de unidades compradas de este repuesto
    private int cantidad;
    // Precio o costo por unidad del repuesto comprado
    private double costoUnitario;

    // Atributo auxiliar para mostrar el nombre del repuesto en las interfaces gráficas o vistas
    private String nombreRepuesto;
    
    // Atributo auxiliar usado para calcular y establecer el nuevo precio de venta al público en la tabla de productos
    private double nuevoPrecioVenta;

    // Constructor por defecto (vacío)
    public DetalleCompra() {
    }

    public DetalleCompra(int idDetalleCompra, int idCompraFk, int idRepuestoFk, int cantidad, double costoUnitario) {
        this.idDetalleCompra = idDetalleCompra;
        this.idCompraFk = idCompraFk;
        this.idRepuestoFk = idRepuestoFk;
        this.cantidad = cantidad;
        this.costoUnitario = costoUnitario;
    }

    // Método Getter para recuperar el valor de IdDetalleCompra
    public int getIdDetalleCompra() {
        // Retornar el valor obtenido
        return idDetalleCompra;
    }

    // Método Setter para establecer el valor de IdDetalleCompra
    public void setIdDetalleCompra(int idDetalleCompra) {
        this.idDetalleCompra = idDetalleCompra;
    }

    // Método Getter para recuperar el valor de IdCompraFk
    public int getIdCompraFk() {
        // Retornar el valor obtenido
        return idCompraFk;
    }

    // Método Setter para establecer el valor de IdCompraFk
    public void setIdCompraFk(int idCompraFk) {
        this.idCompraFk = idCompraFk;
    }

    // Método Getter para recuperar el valor de IdRepuestoFk
    public int getIdRepuestoFk() {
        // Retornar el valor obtenido
        return idRepuestoFk;
    }

    // Método Setter para establecer el valor de IdRepuestoFk
    public void setIdRepuestoFk(int idRepuestoFk) {
        this.idRepuestoFk = idRepuestoFk;
    }

    // Método Getter para recuperar el valor de Cantidad
    public int getCantidad() {
        // Retornar el valor obtenido
        return cantidad;
    }

    // Método Setter para establecer el valor de Cantidad
    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    // Método Getter para recuperar el valor de CostoUnitario
    public double getCostoUnitario() {
        // Retornar el valor obtenido
        return costoUnitario;
    }

    // Método Setter para establecer el valor de CostoUnitario
    public void setCostoUnitario(double costoUnitario) {
        this.costoUnitario = costoUnitario;
    }

    // Método Getter para recuperar el valor de NombreRepuesto
    public String getNombreRepuesto() {
        // Retornar el valor obtenido
        return nombreRepuesto;
    }

    // Método Setter para establecer el valor de NombreRepuesto
    public void setNombreRepuesto(String nombreRepuesto) {
        this.nombreRepuesto = nombreRepuesto;
    }

    // Método Getter para recuperar el valor de NuevoPrecioVenta
    public double getNuevoPrecioVenta() {
        // Retornar el valor obtenido
        return nuevoPrecioVenta;
    }

    // Método Setter para establecer el valor de NuevoPrecioVenta
    public void setNuevoPrecioVenta(double nuevoPrecioVenta) {
        this.nuevoPrecioVenta = nuevoPrecioVenta;
    }
}
