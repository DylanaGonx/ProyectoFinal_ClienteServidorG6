/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import dao.IncidenciaDAO;
import exceptions.DatosInvalidosException;

import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Logica de negocio de las incidencias que reportan los conductores (HU-07).
 *
 * @author Grupo 6
 */
public class GestorIncidencias {

    private IncidenciaDAO incidenciaDAO;

    public GestorIncidencias() {
        incidenciaDAO = new IncidenciaDAO();
    }

    /*
     * Registra la incidencia despues de revisar las condiciones que exige la
     * tabla: el paquete tiene que ir en un vehiculo y ese vehiculo tiene que
     * tener un conductor, porque la incidencia queda a nombre de el.
     */
    public void registrarIncidencia(int idPaquete, String placa, String tipo, String descripcion)
            throws DatosInvalidosException, SQLException {

        if (placa == null || placa.trim().isEmpty()) {
            throw new DatosInvalidosException(
                    "El paquete no tiene vehiculo asignado. Primero se le asigna un vehiculo.");
        }

        if (descripcion == null || descripcion.trim().isEmpty()) {
            throw new DatosInvalidosException("Debe describir la incidencia.");
        }

        if (!incidenciaDAO.vehiculoTieneConductor(placa)) {
            throw new DatosInvalidosException(
                    "El vehiculo " + placa + " no tiene conductor asignado, "
                    + "por eso no se le puede registrar la incidencia a nadie.");
        }

        incidenciaDAO.insertar(idPaquete, placa, tipo, descripcion.trim());
    }

    public ArrayList<String> obtenerIncidencias() throws SQLException {
        return incidenciaDAO.listarTodas();
    }

    public ArrayList<String> obtenerTipos() throws SQLException {
        return incidenciaDAO.listarTipos();
    }

}
