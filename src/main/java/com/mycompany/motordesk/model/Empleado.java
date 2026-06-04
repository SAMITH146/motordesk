// Definición del paquete del proyecto
package com.mycompany.motordesk.model;

// Importación de dependencias y clases necesarias
import java.sql.Date;

// Clase pública Empleado que gestiona la lógica correspondiente
public class Empleado {

    private String idEmpleado;     // doc_emple
    private String nombre;       // nom_empleado
    private String pin;          // pin_acceso
    private int idRol;
    private int idCargo;
    private String estadoEmpleado;
    private Date fechaIngreso;

    // Constructor por defecto (vacío)
    public Empleado() {
    }

    // ===== GETTERS & SETTERS =====
    // Método Setter para establecer el valor de IdEmpleado
    public void setIdEmpleado(String idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    // Método Getter para recuperar el valor de IdEmpleado
    public String getIdEmpleado() {
        // Retornar el valor obtenido
        return idEmpleado;
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

    // Método Getter para recuperar el valor de Pin
    public String getPin() {
        // Retornar el valor obtenido
        return pin;
    }

    // Método Setter para establecer el valor de Pin
    public void setPin(String pin) {
        this.pin = pin;
    }

    // Método Getter para recuperar el valor de IdRol
    public int getIdRol() {
        // Retornar el valor obtenido
        return idRol;
    }

    // Método Setter para establecer el valor de IdRol
    public void setIdRol(int idRol) {
        this.idRol = idRol;
    }

    // Método Getter para recuperar el valor de IdCargo
    public int getIdCargo() {
        // Retornar el valor obtenido
        return idCargo;
    }

    // Método Setter para establecer el valor de IdCargo
    public void setIdCargo(int idCargo) {
        this.idCargo = idCargo;
    }

    // Método Getter para recuperar el valor de EstadoEmpleado
    public String getEstadoEmpleado() {
        // Retornar el valor obtenido
        return estadoEmpleado;
    }

    // Método Setter para establecer el valor de EstadoEmpleado
    public void setEstadoEmpleado(String estadoEmpleado) {
        this.estadoEmpleado = estadoEmpleado;
    }

    // Método Getter para recuperar el valor de FechaIngreso
    public Date getFechaIngreso() {
        // Retornar el valor obtenido
        return fechaIngreso;
    }

    // Método Setter para establecer el valor de FechaIngreso
    public void setFechaIngreso(Date fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }
}




