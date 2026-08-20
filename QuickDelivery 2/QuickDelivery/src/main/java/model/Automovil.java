/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 * 
 * @author Grupo 6
 */
public class Automovil extends Vehiculo {
    
    //Constructor
    public Automovil(String placa, double capacidad, String conductor) {
        super(placa, capacidad, conductor);
    }

    @Override
    public String getTipo() {
        return "Automovil";
    }
    
}
