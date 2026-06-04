// Definición del paquete del proyecto
package com.mycompany.motordesk.model;

// Clase pública Cliente que gestiona la lógica correspondiente
public class Cliente {
    private int idCliente;
    private String nombre;
    private String documento;
    private String direccion;

    // Constructor por defecto (vacío)
    public Cliente() {}

    public Cliente(int idCliente, String nombre, String documento, String direccion) {
        this.idCliente = idCliente;
        this.nombre = nombre;
        this.documento = documento;
        this.direccion = direccion;
    }

    // Método Getter para recuperar el valor de IdCliente
    public int getIdCliente() { return idCliente; }
    // Método Setter para establecer el valor de IdCliente
    public void setIdCliente(int idCliente) { this.idCliente = idCliente; }

    // Método Getter para recuperar el valor de Nombre
    public String getNombre() { return nombre; }
    // Método Setter para establecer el valor de Nombre
    public void setNombre(String nombre) { this.nombre = nombre; }

    // Método Getter para recuperar el valor de Documento
    public String getDocumento() { return documento; }
    // Método Setter para establecer el valor de Documento
    public void setDocumento(String documento) { this.documento = documento; }

    // Método Getter para recuperar el valor de Direccion
    public String getDireccion() { return direccion; }
    // Método Setter para establecer el valor de Direccion
    public void setDireccion(String direccion) { this.direccion = direccion; }
}
