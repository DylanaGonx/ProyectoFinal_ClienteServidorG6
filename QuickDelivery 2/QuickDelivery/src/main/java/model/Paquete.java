/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 * 
 * @author Grupo 6
 */
public class Paquete {
    
    //Datos del paquete
    private int id;
    private String destinatario;
    private String direccion;
    private double peso;
    private EstadoPaquete estado;
    private Vehiculo vehiculo;

    //Constructor
    public Paquete(int id, String destinatario, String direccion, double peso, EstadoPaquete estado, Vehiculo vehiculo) {
        this.id = id;
        this.destinatario = destinatario;
        this.direccion = direccion;
        this.peso = peso;
        this.estado = estado;
        this.vehiculo = vehiculo;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDestinatario() {
        return destinatario;
    }

    public void setDestinatario(String destinatario) {
        this.destinatario = destinatario;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public double getPeso() {
        return peso;
    }

    public void setPeso(double peso) {
        this.peso = peso;
    }

    public EstadoPaquete getEstado() {
        return estado;
    }

    public void setEstado(EstadoPaquete estado) {
        this.estado = estado;
    }

    public Vehiculo getVehiculo() {
        return vehiculo;
    }

    public void setVehiculo(Vehiculo vehiculo) {
        this.vehiculo = vehiculo;
    }
    
}

