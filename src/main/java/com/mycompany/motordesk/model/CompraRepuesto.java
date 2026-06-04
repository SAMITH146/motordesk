// Definición del paquete del proyecto
package com.mycompany.motordesk.model;

// Importación de dependencias y clases necesarias
import java.util.Date;

// Clase pública CompraRepuesto que gestiona la lógica correspondiente
public class CompraRepuesto {
    private int idCompra;
    private int idProveedorFk;
    private Date fechaCompra;
    private double total;

    // Auxiliar para mostrar informacion en las vistas
    private String nombreProveedor;

    // Constructor por defecto (vacío)
    public CompraRepuesto() {
    }

    public CompraRepuesto(int idCompra, int idProveedorFk, Date fechaCompra, double total) {
        this.idCompra = idCompra;
        this.idProveedorFk = idProveedorFk;
        this.fechaCompra = fechaCompra;
        this.total = total;
    }

    // Método Getter para recuperar el valor de IdCompra
    public int getIdCompra() {
        // Retornar el valor obtenido
        return idCompra;
    }

    // Método Setter para establecer el valor de IdCompra
    public void setIdCompra(int idCompra) {
        this.idCompra = idCompra;
    }

    // Método Getter para recuperar el valor de IdProveedorFk
    public int getIdProveedorFk() {
        // Retornar el valor obtenido
        return idProveedorFk;
    }

    // Método Setter para establecer el valor de IdProveedorFk
    public void setIdProveedorFk(int idProveedorFk) {
        this.idProveedorFk = idProveedorFk;
    }

    // Método Getter para recuperar el valor de FechaCompra
    public Date getFechaCompra() {
        // Retornar el valor obtenido
        return fechaCompra;
    }

    // Método Setter para establecer el valor de FechaCompra
    public void setFechaCompra(Date fechaCompra) {
        this.fechaCompra = fechaCompra;
    }

    // Método Getter para recuperar el valor de Total
    public double getTotal() {
        // Retornar el valor obtenido
        return total;
    }

    // Método Setter para establecer el valor de Total
    public void setTotal(double total) {
        this.total = total;
    }

    // Método Getter para recuperar el valor de NombreProveedor
    public String getNombreProveedor() {
        // Retornar el valor obtenido
        return nombreProveedor;
    }

    // Método Setter para establecer el valor de NombreProveedor
    public void setNombreProveedor(String nombreProveedor) {
        this.nombreProveedor = nombreProveedor;
    }
}
