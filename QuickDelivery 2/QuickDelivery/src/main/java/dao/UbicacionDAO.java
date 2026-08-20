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

import java.util.ArrayList;

/**
 * 
 * @author Grupo 6
 */
public class UbicacionDAO {

    public void insertar(String placa, double latitud, double longitud, String estadoReportado) throws SQLException {

        String sql = "INSERT INTO ubicaciones_vehiculo "
                   + "(id_vehiculo, latitud, longitud, fecha_hora, estado_reportado) "
                   + "VALUES ((SELECT id_vehiculo FROM vehiculos WHERE placa = ?), ?, ?, NOW(), ?)";

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, placa);
            ps.setDouble(2, latitud);
            ps.setDouble(3, longitud);
            ps.setString(4, estadoReportado);

            ps.executeUpdate();
        }
    }

    /*
     * Ultima posicion de cada vehiculo que esta En ruta, para que el
     * administrador y el despachador vean en el monitor lo que va mandando la
     * flota, sin tener que preguntarle nada al conductor.
     *
     * El JOIN busca, para cada vehiculo, la fila mas reciente de
     * ubicaciones_vehiculo (la de mayor id_ubicacion). Es el mismo patron de
     * subconsulta que ya se usa en obtenerUltimaUbicacion().
     */
    public ArrayList<String> listarUbicacionesEnRuta() throws SQLException {

        String sql = "SELECT v.placa, t.nombre_tipo, u.usuario AS conductor, "
                   + "       ub.latitud, ub.longitud, ub.fecha_hora "
                   + "FROM vehiculos v "
                   + "INNER JOIN tipos_vehiculo t ON t.id_tipo_vehiculo = v.id_tipo_vehiculo "
                   + "INNER JOIN estados_vehiculo ev ON ev.id_estado_vehiculo = v.id_estado_vehiculo "
                   + "LEFT JOIN usuarios u ON u.id_usuario = v.id_conductor "
                   + "INNER JOIN ubicaciones_vehiculo ub ON ub.id_ubicacion = ("
                   + "     SELECT id_ubicacion FROM ubicaciones_vehiculo "
                   + "     WHERE id_vehiculo = v.id_vehiculo "
                   + "     ORDER BY id_ubicacion DESC LIMIT 1) "
                   + "WHERE ev.nombre_estado = 'En ruta' AND v.activo = TRUE "
                   + "ORDER BY ub.fecha_hora DESC";

        ArrayList<String> ubicaciones = new ArrayList<>();

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                String conductor = rs.getString("conductor");

                ubicaciones.add(rs.getString("placa")
                        + " (" + rs.getString("nombre_tipo") + ")"
                        + (conductor != null ? " - " + conductor : "")
                        + " en " + rs.getDouble("latitud") + ", " + rs.getDouble("longitud")
                        + " - " + rs.getTimestamp("fecha_hora"));
            }
        }

        return ubicaciones;
    }

    // Ultima posicion reportada por cada vehiculo, para el monitor
    public String obtenerUltimaUbicacion(String placa) throws SQLException {

        String sql = "SELECT latitud, longitud, estado_reportado "
                   + "FROM ubicaciones_vehiculo "
                   + "WHERE id_vehiculo = (SELECT id_vehiculo FROM vehiculos WHERE placa = ?) "
                   + "ORDER BY id_ubicacion DESC "
                   + "LIMIT 1";

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, placa);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("latitud") + ", " + rs.getDouble("longitud")
                            + " (" + rs.getString("estado_reportado") + ")";
                } else {
                    return "Sin reportes";
                }
            }
        }
    }

}
