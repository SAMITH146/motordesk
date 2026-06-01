package com.mycompany.motordesk.model;

public class Vehiculo {
    private int idVehiculo;
    private int idClienteFk;
    private String placa;
    private String marca;
    private String modelo;
    private int anio;

    public Vehiculo() {}

    public Vehiculo(int idVehiculo, int idClienteFk, String placa, String marca, String modelo, int anio) {
        this.idVehiculo = idVehiculo;
        this.idClienteFk = idClienteFk;
        this.placa = placa;
        this.marca = marca;
        this.modelo = modelo;
        this.anio = anio;
    }

    public int getIdVehiculo() { return idVehiculo; }
    public void setIdVehiculo(int idVehiculo) { this.idVehiculo = idVehiculo; }

    public int getIdClienteFk() { return idClienteFk; }
    public void setIdClienteFk(int idClienteFk) { this.idClienteFk = idClienteFk; }

    public String getPlaca() { return placa; }
    public void setPlaca(String placa) { this.placa = placa; }

    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }

    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }

    public int getAnio() { return anio; }
    public void setAnio(int anio) { this.anio = anio; }
}
