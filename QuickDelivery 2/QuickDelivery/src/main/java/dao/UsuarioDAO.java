/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import db.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

import java.util.ArrayList;

import model.Administrador;
import model.Conductor;
import model.Despachador;
import model.Usuario;

/**
 * Acceso a la tabla usuarios.
 *
 * @author Grupo 6
 */
public class UsuarioDAO {

    /*
     * Registra el usuario. Se usa una transaccion porque ademas de la fila en
     * usuarios hay que crear la fila de la tabla especializada que corresponde
     * al rol, cuando ese rol guarda datos propios (conductores y
     * despachadores). El administrador no lleva tabla aparte: ser administrador
     * es un rol, y el rol ya quedo guardado en la fila de usuarios.
     */
    public void insertar(Usuario usuario) throws SQLException {

        String sqlUsuario = "INSERT INTO usuarios (id_rol, usuario, contrasena_hash, intentos, activo) "
                          + "VALUES ((SELECT id_rol FROM roles WHERE nombre_rol = ?), ?, ?, 0, TRUE)";

        try (Connection conn = ConexionBD.obtenerConexion()) {

            conn.setAutoCommit(false);

            try {

                int idUsuario;

                try (PreparedStatement ps = conn.prepareStatement(sqlUsuario, Statement.RETURN_GENERATED_KEYS)) {

                    ps.setString(1, usuario.getrol());
                    ps.setString(2, usuario.getUsuario());
                    ps.setString(3, usuario.getContrasenia());

                    ps.executeUpdate();

                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        rs.next();
                        idUsuario = rs.getInt(1);
                    }
                }

                insertarEspecializacion(conn, idUsuario, usuario.getrol());

                usuario.setId(idUsuario);

                conn.commit();

            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public void actualizar(Usuario usuario) throws SQLException {

        String sql = "UPDATE usuarios SET contrasena_hash = ? WHERE usuario = ?";

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, usuario.getContrasenia());
            ps.setString(2, usuario.getUsuario());

            ps.executeUpdate();
        }
    }

    /*
     * El usuario no se borra fisicamente porque otras tablas lo referencian.
     * Se desactiva, que es lo que revisa el metodo validar().
     */
    public void eliminar(String usuario) throws SQLException {

        String sql = "UPDATE usuarios SET activo = FALSE WHERE usuario = ?";

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, usuario);
            ps.executeUpdate();
        }
    }

    // Crea la fila de la tabla especializada segun el rol
    private void insertarEspecializacion(Connection conn, int idUsuario, String rol) throws SQLException {

        String sql;

        switch (rol) {
            case "Conductor":
                sql = "INSERT INTO conductores (id_usuario, licencia, telefono) VALUES (?, ?, '')";
                break;
            case "Despachador":
                sql = "INSERT INTO despachadores (id_usuario, zona) VALUES (?, '')";
                break;
            default:
                return;
        }

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idUsuario);

            // La licencia del conductor es unica, se arma con el id del usuario
            if (rol.equals("Conductor")) {
                ps.setString(2, "LIC-" + idUsuario);
            }

            ps.executeUpdate();
        }
    }

    // Valida las credenciales contra la base de datos (RF-01)
    public Usuario validar(String usuario, String contrasenia) throws SQLException {

        String sql = "SELECT u.id_usuario, u.usuario, u.contrasena_hash, r.nombre_rol "
                   + "FROM usuarios u "
                   + "INNER JOIN roles r ON r.id_rol = u.id_rol "
                   + "WHERE u.usuario = ? AND u.contrasena_hash = ? AND u.activo = TRUE";

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, usuario);
            ps.setString(2, contrasenia);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearUsuario(rs);
                } else {
                    return null;
                }
            }
        }
    }

    // Busca un usuario por su nombre de usuario
    public Usuario obtenerPorUsuario(String usuario) throws SQLException {

        String sql = "SELECT u.id_usuario, u.usuario, u.contrasena_hash, r.nombre_rol "
                   + "FROM usuarios u "
                   + "INNER JOIN roles r ON r.id_rol = u.id_rol "
                   + "WHERE u.usuario = ?";

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, usuario);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearUsuario(rs);
                } else {
                    return null;
                }
            }
        }
    }

    /*
     * Lista los usuarios activos. Los que se dieron de baja no aparecen, igual
     * que pasa con los vehiculos inactivos en VehiculoDAO.
     */
    public ArrayList<Usuario> listarTodos() throws SQLException {

        String sql = "SELECT u.id_usuario, u.usuario, u.contrasena_hash, r.nombre_rol "
                   + "FROM usuarios u "
                   + "INNER JOIN roles r ON r.id_rol = u.id_rol "
                   + "WHERE u.activo = TRUE "
                   + "ORDER BY u.id_usuario";

        ArrayList<Usuario> usuarios = new ArrayList<>();

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                usuarios.add(mapearUsuario(rs));
            }
        }

        return usuarios;
    }

    // Lista unicamente los usuarios que tienen rol de conductor
    public ArrayList<Usuario> listarConductores() throws SQLException {

        String sql = "SELECT u.id_usuario, u.usuario, u.contrasena_hash, r.nombre_rol "
                   + "FROM usuarios u "
                   + "INNER JOIN roles r ON r.id_rol = u.id_rol "
                   + "INNER JOIN conductores c ON c.id_usuario = u.id_usuario "
                   + "WHERE u.activo = TRUE "
                   + "ORDER BY u.usuario";

        ArrayList<Usuario> conductores = new ArrayList<>();

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                conductores.add(mapearUsuario(rs));
            }
        }

        return conductores;
    }

    /*
     * Segun el rol que trae la consulta se crea la subclase que corresponde.
     * Aqui se aprovecha el polimorfismo: los tres objetos se devuelven como
     * Usuario aunque cada uno sea de un tipo distinto.
     */
    public Usuario mapearUsuario(ResultSet rs) throws SQLException {

        String nombreUsuario = rs.getString("usuario");
        String contrasenia = rs.getString("contrasena_hash");
        String rol = rs.getString("nombre_rol");

        Usuario usuario;

        switch (rol) {
            case "Administrador":
                usuario = new Administrador(nombreUsuario, contrasenia);
                break;
            case "Conductor":
                usuario = new Conductor(nombreUsuario, contrasenia);
                break;
            case "Despachador":
                usuario = new Despachador(nombreUsuario, contrasenia);
                break;
            default:
                usuario = new Usuario(nombreUsuario, contrasenia, rol);
                break;
        }

        usuario.setId(rs.getInt("id_usuario"));

        return usuario;
    }

}
