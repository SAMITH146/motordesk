// Este archivo pertenece al paquete "model" — la capa que representa los datos del negocio
package com.mycompany.motordesk.model;

/**
 * Clase Cliente: representa una fila de la tabla "cliente" en la base de datos MySQL.
 * Se encarga de almacenar y transportar la información personal de los clientes registrados en el taller.
 */
public class Cliente {

    // ID único generado automáticamente por la base de datos (AUTO_INCREMENT)
    private int idCliente;

    // Nombre completo del cliente (columna: nom_cliente)
    private String nombre;

    // Número de documento de identidad del cliente (columna: doc_cliente) — es único
    private String documento;

    // Dirección del cliente para registros (columna: direccion_cliente)
    private String direccion;

    // Constructor vacío — se usa cuando creamos un Cliente sin datos iniciales
    public Cliente() {}

    // Constructor completo — se usa para crear un Cliente con todos sus datos de una vez
    // Por ejemplo, cuando leemos una fila de la base de datos y la "empaquetamos" en este objeto
    public Cliente(int idCliente, String nombre, String documento, String direccion) {
        this.idCliente = idCliente;     // Asignamos el ID
        this.nombre = nombre;           // Asignamos el nombre
        this.documento = documento;     // Asignamos el documento
        this.direccion = direccion;     // Asignamos la dirección
    }

    // GETTERS y SETTERS: métodos para leer y escribir cada atributo desde afuera de la clase
    // Los getters los usa el JSP con expresiones como ${cliente.nombre}
    // Los setters los usa el DAO para llenar el objeto con datos de la BD

    // Retorna el ID del cliente
    public int getIdCliente() { return idCliente; }
    // Establece el ID del cliente (usado después de un INSERT para guardar el ID generado por MySQL)
    public void setIdCliente(int idCliente) { this.idCliente = idCliente; }

    // Retorna el nombre
    public String getNombre() { return nombre; }
    // Actualiza el nombre (útil al editar un cliente)
    public void setNombre(String nombre) { this.nombre = nombre; }

    // Retorna el número de documento
    public String getDocumento() { return documento; }
    // Establece el documento
    public void setDocumento(String documento) { this.documento = documento; }

    // Retorna la dirección
    public String getDireccion() { return direccion; }
    // Establece la dirección
    public void setDireccion(String direccion) { this.direccion = direccion; }
}
