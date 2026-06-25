// Definición del paquete del proyecto
package com.mycompany.motordesk.model;

/**
 * Clase pública Vehiculo que representa el medio de transporte del cliente que ingresa al taller.
 * Almacena datos técnicos del vehículo y a qué cliente le pertenece.
 */
public class Vehiculo {
    // Identificador único del vehículo (llave primaria)
    private int idVehiculo;
    // Identificador del cliente que es dueño de este vehículo (llave foránea)
    private int idClienteFk;
    // Número de placa del vehículo (identificador físico único)
    private String placa;
    // Marca fabricante del vehículo (ej. Chevrolet, Mazda, Yamaha)
    private String marca;
    // Línea o modelo específico del vehículo dentro de la marca
    private String modelo;
    // Año de fabricación o modelo del vehículo
    private int anio;
    // Tipo de vehículo para categorizarlo (ej. Moto, Carro, Camión, etc.)
    private String tipoVehiculo;

    // Constructor por defecto (vacío)
    public Vehiculo() {}

    public String getTipoVehiculo() {
        return tipoVehiculo;
    }

    public void setTipoVehiculo(String tipoVehiculo) {
        this.tipoVehiculo = tipoVehiculo;
    }

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
