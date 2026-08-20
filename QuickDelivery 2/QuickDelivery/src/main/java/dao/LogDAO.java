/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import db.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * 
 * @author Grupo 6
 */
public class LogDAO {

    public void insertar(int idUsuario, String accion, String modulo, String detalle) throws SQLException {

        String sql = "INSERT INTO logs_sistema (id_usuario, accion, modulo, fecha_hora, detalle) "
                   + "VALUES (?, ?, ?, NOW(), ?)";

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idUsuario);
            ps.setString(2, accion);
            ps.setString(3, modulo);
            ps.setString(4, detalle);

            ps.executeUpdate();
        }
    }

    /*
     * Version que no interrumpe el flujo del programa. El registro de auditoria
     * no debe hacer fallar la operacion principal, por eso el error solo se
     * muestra en consola.
     */
    public void registrar(int idUsuario, String accion, String modulo, String detalle) {
        try {
            insertar(idUsuario, accion, modulo, detalle);
        } catch (SQLException ex) {
            System.out.println("No se pudo guardar el log: " + ex.getLocalizedMessage());
        }
    }

}
