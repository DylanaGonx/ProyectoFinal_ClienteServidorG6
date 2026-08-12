/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.io.Serializable;

/**
 * Configuracion del sistema. Este objeto se guarda y se lee de un archivo
 * usando serializacion, por eso implementa Serializable.
 *
 * Ahi se guarda la direccion del servidor, el puerto y cada cuanto reporta el
 * conductor su ubicacion, para no tener esos valores quemados en el codigo.
 *
 * @author Grupo 6
 */
public class Configuracion implements Serializable {

    private String servidor;
    private int puerto;
    private int intervaloUbicacion;
    private int maxIntentos;

    //Constructor con los valores por defecto
    public Configuracion() {
        this.servidor = "127.0.0.1";
        this.puerto = 5433;
        this.intervaloUbicacion = 5000;
        this.maxIntentos = 3;
    }

    public Configuracion(String servidor, int puerto, int intervaloUbicacion, int maxIntentos) {
        this.servidor = servidor;
        this.puerto = puerto;
        this.intervaloUbicacion = intervaloUbicacion;
        this.maxIntentos = maxIntentos;
    }

    public String getServidor() {
        return servidor;
    }

    public void setServidor(String servidor) {
        this.servidor = servidor;
    }

    public int getPuerto() {
        return puerto;
    }

    public void setPuerto(int puerto) {
        this.puerto = puerto;
    }

    public int getIntervaloUbicacion() {
        return intervaloUbicacion;
    }

    public void setIntervaloUbicacion(int intervaloUbicacion) {
        this.intervaloUbicacion = intervaloUbicacion;
    }

    public int getMaxIntentos() {
        return maxIntentos;
    }

    public void setMaxIntentos(int maxIntentos) {
        this.maxIntentos = maxIntentos;
    }

    @Override
    public String toString() {
        return "Servidor " + servidor + ":" + puerto
                + ", reporte cada " + intervaloUbicacion + " ms"
                + ", " + maxIntentos + " intentos de ingreso";
    }

}
