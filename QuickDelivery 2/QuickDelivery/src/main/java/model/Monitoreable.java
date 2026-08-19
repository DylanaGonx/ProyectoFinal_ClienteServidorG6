/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 * Contrato de todo lo que la empresa puede monitorear en tiempo real.
 *
 * Esta interfaz es la que aparece en el diagrama de clases del Avance 1. La
 * implementa Vehiculo, de manera que el servidor puede trabajar con cualquier
 * cosa monitoreable sin importar si es una moto, un automovil, un camion o un
 * furgon.
 *
 * Si el dia de mañana se quiere monitorear otra cosa (un dron, una bodega
 * movil), basta con que implemente esta interfaz y el servidor no cambia.
 *
 * @author Grupo 6
 */
public interface Monitoreable {

    //Guarda la ultima posicion reportada
    void actualizarUbicacion(double latitud, double longitud);

    //Devuelve como esta el objeto en este momento, listo para mostrar
    String obtenerEstado();

}
