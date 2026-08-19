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
import java.sql.Types;

import java.util.ArrayList;

import model.Automovil;
import model.Camion;
import model.Furgon;
import model.Motocicleta;
import model.Vehiculo;

/**
 * Acceso a la tabla vehiculos.
 *
 * @author Grupo 6
 */
public class VehiculoDAO {

    // Estado 1 = Disponible, segun la tabla estados_vehiculo
    private static final int ESTADO_DISPONIBLE = 1;

    public void insertar(Vehiculo vehiculo) throws SQLException {

        String sql = "INSERT INTO vehiculos "
                   + "(id_tipo_vehiculo, id_estado_vehiculo, id_conductor, placa, capacidad_kg, activo) "
                   + "VALUES (?, ?, ?, ?, ?, TRUE)";

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, obtenerIdTipo(conn, vehiculo.getTipo()));
            ps.setInt(2, ESTADO_DISPONIBLE);

            Integer idConductor = obtenerIdConductor(conn, vehiculo.getConductor());

            if (idConductor == null) {
                ps.setNull(3, Types.INTEGER);
            } else {
                ps.setInt(3, idConductor);
            }

            ps.setString(4, vehiculo.getPlaca());
            ps.setDouble(5, vehiculo.getCapacidad());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    vehiculo.setId(rs.getInt(1));
                }
            }
        }
    }

    public void actualizar(Vehiculo vehiculo) throws SQLException {

        String sql = "UPDATE vehiculos "
                   + "SET id_tipo_vehiculo = ?, id_conductor = ?, capacidad_kg = ? "
                   + "WHERE placa = ?";

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, obtenerIdTipo(conn, vehiculo.getTipo()));

            Integer idConductor = obtenerIdConductor(conn, vehiculo.getConductor());

            if (idConductor == null) {
                ps.setNull(2, Types.INTEGER);
            } else {
                ps.setInt(2, idConductor);
            }

            ps.setDouble(3, vehiculo.getCapacidad());
            ps.setString(4, vehiculo.getPlaca());

            ps.executeUpdate();
        }
    }

    /*
     * El vehiculo no se borra fisicamente. Sus ubicaciones, asignaciones e
     * incidencias lo referencian con llaves foraneas RESTRICT, y ademas esa
     * informacion es parte de la trazabilidad y no se debe perder.
     * Se marca como inactivo, que es lo que revisa listarTodos().
     * Es el mismo criterio que se usa con los usuarios.
     */
    public void eliminar(String placa) throws SQLException {

        String sql = "UPDATE vehiculos SET activo = FALSE WHERE placa = ?";

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, placa);
            ps.executeUpdate();
        }
    }

    public ArrayList<Vehiculo> listarTodos() throws SQLException {

        String sql = "SELECT v.id_vehiculo, v.placa, v.capacidad_kg, t.nombre_tipo, u.usuario "
                   + "FROM vehiculos v "
                   + "INNER JOIN tipos_vehiculo t ON t.id_tipo_vehiculo = v.id_tipo_vehiculo "
                   + "LEFT JOIN usuarios u ON u.id_usuario = v.id_conductor "
                   + "WHERE v.activo = TRUE "
                   + "ORDER BY v.id_vehiculo";

        ArrayList<Vehiculo> vehiculos = new ArrayList<>();

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                vehiculos.add(mapearVehiculo(rs));
            }
        }

        return vehiculos;
    }

    /*
     * Vehiculos que tiene asignados un conductor. Se usa cuando el conductor
     * entra al sistema y va a reportar su ubicacion, para que solo pueda
     * reportar por los vehiculos que le corresponden.
     */
    public ArrayList<Vehiculo> listarPorConductor(String usuario) throws SQLException {

        String sql = "SELECT v.id_vehiculo, v.placa, v.capacidad_kg, t.nombre_tipo, u.usuario "
                   + "FROM vehiculos v "
                   + "INNER JOIN tipos_vehiculo t ON t.id_tipo_vehiculo = v.id_tipo_vehiculo "
                   + "INNER JOIN usuarios u ON u.id_usuario = v.id_conductor "
                   + "WHERE u.usuario = ? AND v.activo = TRUE "
                   + "ORDER BY v.placa";

        ArrayList<Vehiculo> vehiculos = new ArrayList<>();

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, usuario);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    vehiculos.add(mapearVehiculo(rs));
                }
            }
        }

        return vehiculos;
    }

    public Vehiculo obtenerPorPlaca(String placa) throws SQLException {

        String sql = "SELECT v.id_vehiculo, v.placa, v.capacidad_kg, t.nombre_tipo, u.usuario "
                   + "FROM vehiculos v "
                   + "INNER JOIN tipos_vehiculo t ON t.id_tipo_vehiculo = v.id_tipo_vehiculo "
                   + "LEFT JOIN usuarios u ON u.id_usuario = v.id_conductor "
                   + "WHERE v.placa = ?";

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, placa);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearVehiculo(rs);
                } else {
                    return null;
                }
            }
        }
    }

    // Cambia el estado del vehiculo (Disponible / En ruta)
    public void actualizarEstado(String placa, String nombreEstado) throws SQLException {

        String sql = "UPDATE vehiculos "
                   + "SET id_estado_vehiculo = (SELECT id_estado_vehiculo FROM estados_vehiculo WHERE nombre_estado = ?) "
                   + "WHERE placa = ?";

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nombreEstado);
            ps.setString(2, placa);

            ps.executeUpdate();
        }
    }

    /*
     * El nombre del tipo guardado en la base es el mismo que devuelve el metodo
     * getTipo() de cada subclase, por eso se puede instanciar la clase correcta.
     */
    public Vehiculo mapearVehiculo(ResultSet rs) throws SQLException {

        String placa = rs.getString("placa");
        double capacidad = rs.getDouble("capacidad_kg");
        String conductor = rs.getString("usuario");
        String tipo = rs.getString("nombre_tipo");

        if (conductor == null) {
            conductor = "";
        }

        Vehiculo vehiculo;

        switch (tipo) {
            case "Motocicleta":
                vehiculo = new Motocicleta(placa, capacidad, conductor);
                break;
            case "Camion":
                vehiculo = new Camion(placa, capacidad, conductor);
                break;
            case "Furgon":
                vehiculo = new Furgon(placa, capacidad, conductor);
                break;
            default:
                vehiculo = new Automovil(placa, capacidad, conductor);
                break;
        }

        vehiculo.setId(rs.getInt("id_vehiculo"));

        return vehiculo;
    }

    // Busca el id del tipo de vehiculo por su nombre
    private int obtenerIdTipo(Connection conn, String nombreTipo) throws SQLException {

        String sql = "SELECT id_tipo_vehiculo FROM tipos_vehiculo WHERE nombre_tipo = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nombreTipo);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_tipo_vehiculo");
                }
            }
        }

        throw new SQLException("El tipo de vehiculo " + nombreTipo + " no existe en la base de datos.");
    }

    /*
     * Busca el id del conductor a partir del nombre de usuario.
     * Devuelve null cuando el vehiculo no tiene conductor asignado o cuando el
     * usuario digitado no esta registrado como conductor.
     */
    private Integer obtenerIdConductor(Connection conn, String usuario) throws SQLException {

        if (usuario == null || usuario.trim().isEmpty()) {
            return null;
        }

        String sql = "SELECT c.id_usuario "
                   + "FROM conductores c "
                   + "INNER JOIN usuarios u ON u.id_usuario = c.id_usuario "
                   + "WHERE u.usuario = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, usuario.trim());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_usuario");
                }
            }
        }

        return null;
    }

}
