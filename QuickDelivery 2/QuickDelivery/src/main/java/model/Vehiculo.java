/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author ssanc
 */
public abstract class Vehiculo {
    
    //Datos del vehiculo
    private String placa;
    private double capacidad;
    private String conductor;
    
    //Constructor
    public Vehiculo(String placa, double capacidad, String conductor) {
        this.placa = placa;
        this.capacidad = capacidad;
        this.conductor = conductor;
    }
    
    //Obtiene la placa
    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }
    public double getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(double capacidad) {
        this.capacidad = capacidad;
    }

    public String getConductor() {
        return conductor;
    }

    public void setConductor(String conductor) {
        this.conductor = conductor;
    }
    
    public abstract String getTipo();
    
    

    
}
