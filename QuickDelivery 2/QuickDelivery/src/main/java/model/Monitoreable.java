/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 * 
 * @author Grupo 6
 */
public interface Monitoreable {

    //Guarda la ultima posicion reportada
    void actualizarUbicacion(double latitud, double longitud);

    //Devuelve como esta el objeto en este momento, listo para mostrar
    String obtenerEstado();

}
