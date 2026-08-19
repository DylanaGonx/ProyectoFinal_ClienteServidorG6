/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import dao.VehiculoDAO;
import exceptions.VehiculoDuplicadoException;

import java.sql.SQLException;
import java.util.ArrayList;

import model.Vehiculo;

/**
 * Logica de negocio de la flota. La informacion se guarda en la base de datos
 * por medio de VehiculoDAO.
 *
 * @author ssanc
 */
public class GestorVehiculos {

    private VehiculoDAO vehiculoDAO;

    //Constructor
    public GestorVehiculos() {
        vehiculoDAO = new VehiculoDAO();
    }

    //Agrega un vehiculo
    public void agregarVehiculo(Vehiculo vehiculo) throws VehiculoDuplicadoException, SQLException {

        //Revisa si esa placa ya existe
        if (vehiculoDAO.obtenerPorPlaca(vehiculo.getPlaca()) != null) {
            throw new VehiculoDuplicadoException("Ya existe un vehiculo con esa placa.");
        }

        vehiculoDAO.insertar(vehiculo);
    }

    public ArrayList<Vehiculo> obtenerVehiculos() throws SQLException {
        return vehiculoDAO.listarTodos();
    }

    //Vehiculos asignados a un conductor
    public ArrayList<Vehiculo> obtenerVehiculosDeConductor(String usuario) throws SQLException {
        return vehiculoDAO.listarPorConductor(usuario);
    }

    //Busca los vehiculos por la placa
    public Vehiculo buscarVehiculo(String placa) throws SQLException {
        return vehiculoDAO.obtenerPorPlaca(placa);
    }

    //Eliminar un vehiculo por placa
    public boolean eliminarVehiculo(String placa) throws SQLException {

        if (vehiculoDAO.obtenerPorPlaca(placa) == null) {
            return false;
        }

        vehiculoDAO.eliminar(placa);
        return true;
    }

    //Modificar el vehiculo
    public boolean modificarVehiculo(Vehiculo nuevoVehiculo) throws SQLException {

        if (vehiculoDAO.obtenerPorPlaca(nuevoVehiculo.getPlaca()) == null) {
            return false;
        }

        vehiculoDAO.actualizar(nuevoVehiculo);
        return true;
    }

    //Cambia el estado del vehiculo cuando sale o regresa de una ruta
    public void cambiarEstado(String placa, String nombreEstado) throws SQLException {
        vehiculoDAO.actualizarEstado(placa, nombreEstado);
    }

}
