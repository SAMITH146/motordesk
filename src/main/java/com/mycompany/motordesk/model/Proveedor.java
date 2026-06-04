// Definición del paquete del proyecto
package com.mycompany.motordesk.model;

// Clase pública Proveedor que gestiona la lógica correspondiente
public class Proveedor {
    private int idProveedor;
    private String nombreProveedor;
    private String contacto;
    private String telefono;
    private String correo;

    // Constructor por defecto (vacío)
    public Proveedor() {
    }

    public Proveedor(int idProveedor, String nombreProveedor, String contacto, String telefono, String correo) {
        this.idProveedor = idProveedor;
        this.nombreProveedor = nombreProveedor;
        this.contacto = contacto;
        this.telefono = telefono;
        this.correo = correo;
    }

    // Método Getter para recuperar el valor de IdProveedor
    public int getIdProveedor() {
        // Retornar el valor obtenido
        return idProveedor;
    }

    // Método Setter para establecer el valor de IdProveedor
    public void setIdProveedor(int idProveedor) {
        this.idProveedor = idProveedor;
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

    // Método Getter para recuperar el valor de Contacto
    public String getContacto() {
        // Retornar el valor obtenido
        return contacto;
    }

    // Método Setter para establecer el valor de Contacto
    public void setContacto(String contacto) {
        this.contacto = contacto;
    }

    // Método Getter para recuperar el valor de Telefono
    public String getTelefono() {
        // Retornar el valor obtenido
        return telefono;
    }

    // Método Setter para establecer el valor de Telefono
    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    // Método Getter para recuperar el valor de Correo
    public String getCorreo() {
        // Retornar el valor obtenido
        return correo;
    }

    // Método Setter para establecer el valor de Correo
    public void setCorreo(String correo) {
        this.correo = correo;
    }
}
