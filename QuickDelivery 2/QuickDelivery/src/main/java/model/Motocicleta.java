/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 * 
 * @author Grupo 6
 */
public class Motocicleta extends Vehiculo {
    
    //Constructor
    public Motocicleta(String placa, double capacidad, String conductor) {
        super(placa, capacidad, conductor);
    }

    @Override
    public String getTipo() {
        return "Motocicleta";
   
    }
    
    
    
    
}
