// Definición del paquete del proyecto
package com.mycompany.motordesk.model;

// Clase pública Producto que gestiona la lógica correspondiente
public class Producto {
    private int idProducto;
    private String nombreProducto;
    private int stock;
    private double precioUnitario;
    private String estado;
    private String tipoVehiculo;
    private String seccion;

    // Constructor por defecto (vacío)
    public Producto() {}

    // Método Getter para recuperar el valor de IdProducto
    public int getIdProducto() {
        // Retornar el valor obtenido
        return idProducto;
    }

    // Método Setter para establecer el valor de IdProducto
    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
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

    // Método Getter para recuperar el valor de Stock
    public int getStock() {
        // Retornar el valor obtenido
        return stock;
    }

    // Método Setter para establecer el valor de Stock
    public void setStock(int stock) {
        this.stock = stock;
    }

    // Método Getter para recuperar el valor de PrecioUnitario
    public double getPrecioUnitario() {
        // Retornar el valor obtenido
        return precioUnitario;
    }

    // Método Setter para establecer el valor de PrecioUnitario
    public void setPrecioUnitario(double precioUnitario) {
        this.precioUnitario = precioUnitario;
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

    // Método Getter para recuperar el valor de TipoVehiculo
    public String getTipoVehiculo() {
        // Retornar el valor obtenido
        return tipoVehiculo;
    }

    // Método Setter para establecer el valor de TipoVehiculo
    public void setTipoVehiculo(String tipoVehiculo) {
        this.tipoVehiculo = tipoVehiculo;
    }

    // Método Getter para recuperar el valor de Seccion
    public String getSeccion() {
        // Retornar el valor obtenido
        return seccion;
    }

    // Método Setter para establecer el valor de Seccion
    public void setSeccion(String seccion) {
        this.seccion = seccion;
    }
}
