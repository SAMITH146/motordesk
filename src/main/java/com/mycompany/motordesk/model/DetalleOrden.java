// Definición del paquete del proyecto
package com.mycompany.motordesk.model;

/**
 * Clase pública DetalleOrden que gestiona la lógica correspondiente a los elementos dentro de una orden de trabajo.
 * Cada detalle representa un producto/repuesto utilizado o un servicio aplicado en una orden específica.
 */
public class DetalleOrden {
    // Identificador único del detalle de la orden (llave primaria)
    private int idDetalle;
    // Identificador de la orden de trabajo asociada (llave foránea)
    private int idOrdenFk;
    // Identificador del producto o repuesto utilizado en esta orden (llave foránea)
    private int idProductoFk;
    // Cantidad de productos o unidades del servicio utilizadas
    private int cantidad;
    // Subtotal calculado (cantidad * precio unitario) para este detalle
    private double subtotal;
    
    // Atributo auxiliar para mostrar el nombre del producto en las vistas
    private String nombreProducto;

    // Constructor por defecto (vacío)
    public DetalleOrden() {}

    // Método Getter para recuperar el valor de IdDetalle
    public int getIdDetalle() {
        // Retornar el valor obtenido
        return idDetalle;
    }

    // Método Setter para establecer el valor de IdDetalle
    public void setIdDetalle(int idDetalle) {
        this.idDetalle = idDetalle;
    }

    // Método Getter para recuperar el valor de IdOrdenFk
    public int getIdOrdenFk() {
        // Retornar el valor obtenido
        return idOrdenFk;
    }

    // Método Setter para establecer el valor de IdOrdenFk
    public void setIdOrdenFk(int idOrdenFk) {
        this.idOrdenFk = idOrdenFk;
    }

    // Método Getter para recuperar el valor de IdProductoFk
    public int getIdProductoFk() {
        // Retornar el valor obtenido
        return idProductoFk;
    }

    // Método Setter para establecer el valor de IdProductoFk
    public void setIdProductoFk(int idProductoFk) {
        this.idProductoFk = idProductoFk;
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

    // Método Getter para recuperar el valor de Subtotal
    public double getSubtotal() {
        // Retornar el valor obtenido
        return subtotal;
    }

    // Método Setter para establecer el valor de Subtotal
    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    // Método Getter para recuperar el valor de NombreProducto
    public String getNombreProducto() {
        // Retornar el valor obtenido
        return nombreProducto;
    }

    // Método Setter para establecer el valor de NombreProducto
    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }
}
