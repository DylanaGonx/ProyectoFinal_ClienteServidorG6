/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import dao.PaqueteDAO;
import exceptions.DatosInvalidosException;
import exceptions.PaqueteDuplicadoException;

import java.sql.SQLException;
import java.util.ArrayList;

import model.EstadoPaquete;
import model.Paquete;
import model.Usuario;

/**
 * 
 * @author Grupo 6
 */
public class GestorPaquetes {

    private PaqueteDAO paqueteDAO;

    // Usuario que tiene la sesion abierta, se necesita para saber que
    // despachador queda registrado en la asignacion
    private Usuario usuarioSesion;

    //Constructor
    public GestorPaquetes() {
        paqueteDAO = new PaqueteDAO();
    }

    public void setUsuarioSesion(Usuario usuarioSesion) {
        this.usuarioSesion = usuarioSesion;
    }

    //Agrega un paquete
    public void agregarPaquete(Paquete paquete) throws PaqueteDuplicadoException, DatosInvalidosException, SQLException {

        //Revisa si el Id existe
        if (paqueteDAO.obtenerPorId(paquete.getId()) != null) {
            throw new PaqueteDuplicadoException("Ya existe un paquete con ese ID.");
        }

        validarCapacidad(paquete);

        paqueteDAO.insertar(paquete, obtenerIdDespachador());
    }

    public ArrayList<Paquete> obtenerPaquetes() throws SQLException {
        return paqueteDAO.listarTodos();
    }

    //Busca un paquete por el Id
    public Paquete buscarPaquete(int id) throws SQLException {
        return paqueteDAO.obtenerPorId(id);
    }

    //Paquetes que lleva un vehiculo
    public ArrayList<Paquete> obtenerPaquetesPorPlaca(String placa) throws SQLException {
        return paqueteDAO.listarPorPlaca(placa);
    }

    //Eliminar un paquete
    public boolean eliminarPaquete(int id) throws SQLException {

        if (paqueteDAO.obtenerPorId(id) == null) {
            return false;
        }

        paqueteDAO.eliminar(id);
        return true;
    }

    //Modifica un paquete
    public boolean modificarPaquete(Paquete nuevoPaquete) throws DatosInvalidosException, SQLException {

        if (paqueteDAO.obtenerPorId(nuevoPaquete.getId()) == null) {
            return false;
        }

        validarCapacidad(nuevoPaquete);

        paqueteDAO.actualizar(nuevoPaquete, obtenerIdDespachador());
        return true;
    }

    //Cambia el estado del paquete, lo usa el servidor cuando el conductor reporta
    public void cambiarEstado(int id, EstadoPaquete estado) throws SQLException {
        paqueteDAO.actualizarEstado(id, estado.toString());
    }

    /*
     * RF-11: el vehiculo asignado tiene que soportar el peso del paquete.
     * La validacion vive aqui, en la capa de servicio, y no en la ventana.
     */
    private void validarCapacidad(Paquete paquete) throws DatosInvalidosException {

        if (paquete.getPeso() <= 0) {
            throw new DatosInvalidosException("El peso del paquete tiene que ser mayor a cero.");
        }

        if (paquete.getVehiculo() != null
                && paquete.getPeso() > paquete.getVehiculo().getCapacidad()) {

            throw new DatosInvalidosException(
                    "El vehiculo " + paquete.getVehiculo().getPlaca()
                    + " soporta " + paquete.getVehiculo().getCapacidad()
                    + " kg y el paquete pesa " + paquete.getPeso() + " kg.");
        }
    }

    /*
     * Solo un despachador queda registrado como responsable de la asignacion.
     * Si el que asigna es el administrador la columna queda nula.
     */
    private Integer obtenerIdDespachador() {

        if (usuarioSesion != null && usuarioSesion.getrol().equals("Despachador")) {
            return usuarioSesion.getId();
        }

        return null;
    }

}
