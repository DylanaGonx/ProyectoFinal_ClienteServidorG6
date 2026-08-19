/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import db.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import java.util.ArrayList;

/**
 * Acceso a la tabla incidencias (HU-07).
 *
 * @author Grupo 6
 */
public class IncidenciaDAO {

    /*
     * La latitud y la longitud vienen nulas cuando la incidencia se registra
     * desde el escritorio (el despachador no tiene un GPS a la mano). Cuando
     * la reporta el conductor desde el vehiculo si traen la posicion real de
     * ese momento.
     */
    public void insertar(int idPaquete, String placa, String tipo, String descripcion,
            Double latitud, Double longitud) throws SQLException {

        String sql = "INSERT INTO incidencias "
                   + "(id_paquete, id_vehiculo, id_conductor, id_tipo_incidencia, descripcion, "
                   + " latitud, longitud, fecha) "
                   + "VALUES (?, "
                   + "        (SELECT id_vehiculo FROM vehiculos WHERE placa = ?), "
                   + "        (SELECT id_conductor FROM vehiculos WHERE placa = ?), "
                   + "        (SELECT id_tipo_incidencia FROM tipos_incidencia WHERE nombre_tipo = ?), "
                   + "        ?, ?, ?, NOW())";

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idPaquete);
            ps.setString(2, placa);
            ps.setString(3, placa);
            ps.setString(4, tipo);
            ps.setString(5, descripcion);

            if (latitud == null || longitud == null) {
                ps.setNull(6, Types.DECIMAL);
                ps.setNull(7, Types.DECIMAL);
            } else {
                ps.setDouble(6, latitud);
                ps.setDouble(7, longitud);
            }

            ps.executeUpdate();
        }
    }

    /*
     * La tabla incidencias exige vehiculo y conductor, los dos obligatorios.
     * Con este metodo se revisa antes de intentar guardar, para poder avisar
     * con un mensaje claro en vez de dejar que falle la llave foranea.
     */
    public boolean vehiculoTieneConductor(String placa) throws SQLException {

        String sql = "SELECT id_conductor FROM vehiculos WHERE placa = ?";

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, placa);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    rs.getInt("id_conductor");
                    return !rs.wasNull();
                }
            }
        }

        return false;
    }

    // Tipos disponibles, para llenar la lista del cliente vehiculo
    public ArrayList<String> listarTipos() throws SQLException {

        String sql = "SELECT nombre_tipo FROM tipos_incidencia ORDER BY id_tipo_incidencia";

        ArrayList<String> tipos = new ArrayList<>();

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                tipos.add(rs.getString("nombre_tipo"));
            }
        }

        return tipos;
    }

    // Incidencias registradas, se muestran en las alertas del monitor
    public ArrayList<String> listarTodas() throws SQLException {

        String sql = "SELECT i.id_paquete, v.placa, t.nombre_tipo, i.descripcion, "
                   + "       i.latitud, i.longitud "
                   + "FROM incidencias i "
                   + "INNER JOIN vehiculos v ON v.id_vehiculo = i.id_vehiculo "
                   + "INNER JOIN tipos_incidencia t ON t.id_tipo_incidencia = i.id_tipo_incidencia "
                   + "ORDER BY i.id_incidencia DESC";

        ArrayList<String> incidencias = new ArrayList<>();

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                double latitud = rs.getDouble("latitud");
                boolean sinUbicacion = rs.wasNull();
                double longitud = rs.getDouble("longitud");

                String linea = "Paquete " + rs.getInt("id_paquete")
                        + " - " + rs.getString("placa")
                        + " - " + rs.getString("nombre_tipo")
                        + ": " + rs.getString("descripcion");

                //Se agrega el lugar donde ocurrio la incidencia cuando se conoce
                if (!sinUbicacion) {
                    linea += " (ubicacion: " + latitud + ", " + longitud + ")";
                }

                incidencias.add(linea);
            }
        }

        return incidencias;
    }

}
