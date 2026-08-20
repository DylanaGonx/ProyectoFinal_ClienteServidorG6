/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import dao.UbicacionDAO;

import java.sql.SQLException;
import java.util.ArrayList;

/**
 * 
 * @author Grupo 6
 */
public class GestorUbicaciones {

    private UbicacionDAO ubicacionDAO;

    public GestorUbicaciones() {
        ubicacionDAO = new UbicacionDAO();
    }

    public void registrarUbicacion(String placa, double latitud, double longitud, String estado) throws SQLException {
        ubicacionDAO.insertar(placa, latitud, longitud, estado);
    }

    public String obtenerUltimaUbicacion(String placa) throws SQLException {
        return ubicacionDAO.obtenerUltimaUbicacion(placa);
    }

    //Ubicacion de todos los vehiculos que estan en ruta, para el monitor del backoffice
    public ArrayList<String> obtenerUbicacionesFlota() throws SQLException {
        return ubicacionDAO.listarUbicacionesEnRuta();
    }

}
