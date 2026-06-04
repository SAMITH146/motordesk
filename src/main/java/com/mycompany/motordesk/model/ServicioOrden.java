// Definición del paquete del proyecto
package com.mycompany.motordesk.model;

// Clase pública ServicioOrden — representa una línea de servicio (mano de obra)
// dentro de una Orden de Trabajo. Una orden puede tener N servicios.
public class ServicioOrden {

    private int    idServicio;   // PK autogenerada por MySQL
    private int    idOrdenFk;    // FK → ordentrabajo.id_orden
    private String nombre;       // Nombre del servicio (ej: "Despinche", "Revisión de suspensión")
    private double valor;        // Precio de la mano de obra para este servicio

    // Constructor por defecto (vacío)
    public ServicioOrden() {}

    // Constructor de conveniencia para crear objetos desde el controlador
    public ServicioOrden(int idOrdenFk, String nombre, double valor) {
        this.idOrdenFk = idOrdenFk;
        this.nombre    = nombre;
        this.valor     = valor;
    }

    // ---- Getters y Setters ----

    // Método Getter para recuperar el valor de IdServicio
    public int getIdServicio() {
        // Retornar el valor obtenido
        return idServicio;
    }

    // Método Setter para establecer el valor de IdServicio
    public void setIdServicio(int idServicio) {
        this.idServicio = idServicio;
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

    // Método Getter para recuperar el valor de Nombre
    public String getNombre() {
        // Retornar el valor obtenido
        return nombre;
    }

    // Método Setter para establecer el valor de Nombre
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    // Método Getter para recuperar el valor de Valor
    public double getValor() {
        // Retornar el valor obtenido
        return valor;
    }

    // Método Setter para establecer el valor de Valor
    public void setValor(double valor) {
        this.valor = valor;
    }
}
