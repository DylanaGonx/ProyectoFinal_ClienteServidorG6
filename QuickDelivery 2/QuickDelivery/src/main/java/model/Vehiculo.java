/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 * 
 * @author Grupo 6
 */
public abstract class Vehiculo implements Monitoreable {

    //Datos del vehiculo
    private int id;
    private String placa;
    private double capacidad;
    private String conductor;

    //Ultima posicion reportada por el conductor
    private double latitud;
    private double longitud;
    private String estado = "Disponible";

    //Constructor
    public Vehiculo(String placa, double capacidad, String conductor) {
        this.placa = placa;
        this.capacidad = capacidad;
        this.conductor = conductor;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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
    
    /*
     * Metodos de la interfaz Monitoreable. Cualquier subclase de Vehiculo se
     * puede tratar como Monitoreable sin saber de que tipo es.
     */
    @Override
    public void actualizarUbicacion(double latitud, double longitud) {
        this.latitud = latitud;
        this.longitud = longitud;
        this.estado = "En ruta";
    }

    @Override
    public String obtenerEstado() {

        if (latitud == 0 && longitud == 0) {
            return placa + " (" + getTipo() + ") - " + estado + ", sin reportes de ubicacion";
        }

        return placa + " (" + getTipo() + ") - " + estado + " en " + latitud + ", " + longitud;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public double getLatitud() {
        return latitud;
    }

    public double getLongitud() {
        return longitud;
    }

    public abstract String getTipo();
    
    

    
}
