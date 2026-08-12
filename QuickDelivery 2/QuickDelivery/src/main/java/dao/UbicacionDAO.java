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

/**
 * Acceso a la tabla ubicaciones_vehiculo.
 *
 * Cada vez que un conductor reporta su posicion se guarda una fila nueva, de
 * manera que queda el historico del recorrido (RF-12).
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
