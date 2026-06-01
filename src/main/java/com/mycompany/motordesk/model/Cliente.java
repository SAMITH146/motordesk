package com.mycompany.motordesk.model;

public class Cliente {
    private int idCliente;
    private String nombre;
    private String documento;
    private String direccion;

    public Cliente() {}

    public Cliente(int idCliente, String nombre, String documento, String direccion) {
        this.idCliente = idCliente;
        this.nombre = nombre;
        this.documento = documento;
        this.direccion = direccion;
    }

    public int getIdCliente() { return idCliente; }
    public void setIdCliente(int idCliente) { this.idCliente = idCliente; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
}
