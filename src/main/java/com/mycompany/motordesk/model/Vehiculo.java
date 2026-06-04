// Definición del paquete del proyecto
package com.mycompany.motordesk.model;

// Clase pública Vehiculo que gestiona la lógica correspondiente
public class Vehiculo {
    private int idVehiculo;
    private int idClienteFk;
    private String placa;
    private String marca;
    private String modelo;
    private int anio;

    // Constructor por defecto (vacío)
    public Vehiculo() {}

    public Vehiculo(int idVehiculo, int idClienteFk, String placa, String marca, String modelo, int anio) {
        this.idVehiculo = idVehiculo;
        this.idClienteFk = idClienteFk;
        this.placa = placa;
        this.marca = marca;
        this.modelo = modelo;
        this.anio = anio;
    }

    // Método Getter para recuperar el valor de IdVehiculo
    public int getIdVehiculo() { return idVehiculo; }
    // Método Setter para establecer el valor de IdVehiculo
    public void setIdVehiculo(int idVehiculo) { this.idVehiculo = idVehiculo; }

    // Método Getter para recuperar el valor de IdClienteFk
    public int getIdClienteFk() { return idClienteFk; }
    // Método Setter para establecer el valor de IdClienteFk
    public void setIdClienteFk(int idClienteFk) { this.idClienteFk = idClienteFk; }

    // Método Getter para recuperar el valor de Placa
    public String getPlaca() { return placa; }
    // Método Setter para establecer el valor de Placa
    public void setPlaca(String placa) { this.placa = placa; }

    // Método Getter para recuperar el valor de Marca
    public String getMarca() { return marca; }
    // Método Setter para establecer el valor de Marca
    public void setMarca(String marca) { this.marca = marca; }

    // Método Getter para recuperar el valor de Modelo
    public String getModelo() { return modelo; }
    // Método Setter para establecer el valor de Modelo
    public void setModelo(String modelo) { this.modelo = modelo; }

    // Método Getter para recuperar el valor de Anio
    public int getAnio() { return anio; }
    // Método Setter para establecer el valor de Anio
    public void setAnio(int anio) { this.anio = anio; }
}
