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

import model.EstadoPaquete;
import model.Paquete;

/**
 * 
 * @author Grupo 6
 */
public class PaqueteDAO {

    // Direccion de la bodega central, registrada en el script de la base
    private static final int DIRECCION_ORIGEN = 1;

    // Estado 1 = Pendiente, segun la tabla estados_asignacion
    private static final int ASIGNACION_PENDIENTE = 1;

    private VehiculoDAO vehiculoDAO = new VehiculoDAO();

    // Consulta que se reutiliza en el listado y en la busqueda por id
    private static final String SQL_SELECT =
              "SELECT p.id_paquete, p.destinatario, p.peso, "
            + "       ep.nombre_estado, d.detalle, "
            + "       v.id_vehiculo, v.placa, v.capacidad_kg, t.nombre_tipo, u.usuario "
            + "FROM paquetes p "
            + "INNER JOIN estados_paquete ep ON ep.id_estado_paquete = p.id_estado_paquete "
            + "INNER JOIN direcciones d ON d.id_direccion = p.id_direccion_destino "
            + "LEFT JOIN asignaciones a ON a.id_paquete = p.id_paquete "
            + "LEFT JOIN vehiculos v ON v.id_vehiculo = a.id_vehiculo "
            + "LEFT JOIN tipos_vehiculo t ON t.id_tipo_vehiculo = v.id_tipo_vehiculo "
            + "LEFT JOIN usuarios u ON u.id_usuario = v.id_conductor ";

    /*
     * Registra el paquete. Se usa una transaccion porque hay que guardar
     * primero la direccion de destino, despues el paquete y de ultimo la
     * asignacion del vehiculo. Si algo falla no queda nada a medias.
     */
    public void insertar(Paquete paquete, Integer idDespachador) throws SQLException {

        String sqlDireccion = "INSERT INTO direcciones (provincia, canton, distrito, detalle) "
                            + "VALUES (?, ?, ?, ?)";

        String sqlPaquete = "INSERT INTO paquetes "
                          + "(id_paquete, id_estado_paquete, id_direccion_origen, id_direccion_destino, "
                          + " destinatario, peso, descripcion, fecha_registro) "
                          + "VALUES (?, (SELECT id_estado_paquete FROM estados_paquete WHERE nombre_estado = ?), "
                          + "        ?, ?, ?, ?, ?, NOW())";

        try (Connection conn = ConexionBD.obtenerConexion()) {

            conn.setAutoCommit(false);

            try {

                int idDireccion;

                try (PreparedStatement psDir = conn.prepareStatement(sqlDireccion, Statement.RETURN_GENERATED_KEYS)) {

                    psDir.setString(1, "");
                    psDir.setString(2, "");
                    psDir.setString(3, "");
                    psDir.setString(4, paquete.getDireccion());

                    psDir.executeUpdate();

                    try (ResultSet rs = psDir.getGeneratedKeys()) {
                        rs.next();
                        idDireccion = rs.getInt(1);
                    }
                }

                try (PreparedStatement psPaq = conn.prepareStatement(sqlPaquete)) {

                    psPaq.setInt(1, paquete.getId());
                    psPaq.setString(2, paquete.getEstado().toString());
                    psPaq.setInt(3, DIRECCION_ORIGEN);
                    psPaq.setInt(4, idDireccion);
                    psPaq.setString(5, paquete.getDestinatario());
                    psPaq.setDouble(6, paquete.getPeso());
                    psPaq.setString(7, "Paquete para " + paquete.getDestinatario());

                    psPaq.executeUpdate();
                }

                if (paquete.getVehiculo() != null) {
                    guardarAsignacion(conn, paquete.getId(), paquete.getVehiculo().getPlaca(), idDespachador);
                }

                conn.commit();

            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    /*
     * Actualiza los datos del paquete, el detalle de la direccion de destino y
     * vuelve a generar la asignacion del vehiculo.
     */
    public void actualizar(Paquete paquete, Integer idDespachador) throws SQLException {

        String sqlPaquete = "UPDATE paquetes "
                          + "SET destinatario = ?, peso = ?, "
                          + "    id_estado_paquete = (SELECT id_estado_paquete FROM estados_paquete WHERE nombre_estado = ?) "
                          + "WHERE id_paquete = ?";

        String sqlDireccion = "UPDATE direcciones "
                            + "SET detalle = ? "
                            + "WHERE id_direccion = (SELECT id_direccion_destino FROM paquetes WHERE id_paquete = ?)";

        try (Connection conn = ConexionBD.obtenerConexion()) {

            conn.setAutoCommit(false);

            try {

                try (PreparedStatement psDir = conn.prepareStatement(sqlDireccion)) {

                    psDir.setString(1, paquete.getDireccion());
                    psDir.setInt(2, paquete.getId());

                    psDir.executeUpdate();
                }

                try (PreparedStatement psPaq = conn.prepareStatement(sqlPaquete)) {

                    psPaq.setString(1, paquete.getDestinatario());
                    psPaq.setDouble(2, paquete.getPeso());
                    psPaq.setString(3, paquete.getEstado().toString());
                    psPaq.setInt(4, paquete.getId());

                    psPaq.executeUpdate();
                }

                borrarAsignaciones(conn, paquete.getId());

                if (paquete.getVehiculo() != null) {
                    guardarAsignacion(conn, paquete.getId(), paquete.getVehiculo().getPlaca(), idDespachador);
                }

                conn.commit();

            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    /*
     * Elimina el paquete. Antes hay que borrar las incidencias y las
     * asignaciones porque las llaves foraneas estan definidas con RESTRICT.
     */
    public void eliminar(int idPaquete) throws SQLException {

        String sqlIncidencias = "DELETE FROM incidencias WHERE id_paquete = ?";
        String sqlPaquete = "DELETE FROM paquetes WHERE id_paquete = ?";

        try (Connection conn = ConexionBD.obtenerConexion()) {

            conn.setAutoCommit(false);

            try {

                try (PreparedStatement psInc = conn.prepareStatement(sqlIncidencias)) {
                    psInc.setInt(1, idPaquete);
                    psInc.executeUpdate();
                }

                borrarAsignaciones(conn, idPaquete);

                try (PreparedStatement psPaq = conn.prepareStatement(sqlPaquete)) {
                    psPaq.setInt(1, idPaquete);
                    psPaq.executeUpdate();
                }

                conn.commit();

            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public ArrayList<Paquete> listarTodos() throws SQLException {

        String sql = SQL_SELECT + "ORDER BY p.id_paquete";

        ArrayList<Paquete> paquetes = new ArrayList<>();

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                paquetes.add(mapearPaquete(rs));
            }
        }

        return paquetes;
    }

    public Paquete obtenerPorId(int idPaquete) throws SQLException {

        String sql = SQL_SELECT + "WHERE p.id_paquete = ?";

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idPaquete);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearPaquete(rs);
                } else {
                    return null;
                }
            }
        }
    }

    // Paquetes asignados a un vehiculo, se usa desde el cliente del conductor
    public ArrayList<Paquete> listarPorPlaca(String placa) throws SQLException {

        String sql = SQL_SELECT + "WHERE v.placa = ? ORDER BY p.id_paquete";

        ArrayList<Paquete> paquetes = new ArrayList<>();

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, placa);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    paquetes.add(mapearPaquete(rs));
                }
            }
        }

        return paquetes;
    }

    /*
     * Cambia el estado del paquete. Lo usa el servidor cuando el conductor
     * reporta una entrega desde el cliente vehiculo (RF-13).
     */
    public void actualizarEstado(int idPaquete, String nombreEstado) throws SQLException {

        String sqlPaquete = "UPDATE paquetes "
                          + "SET id_estado_paquete = (SELECT id_estado_paquete FROM estados_paquete WHERE nombre_estado = ?) "
                          + "WHERE id_paquete = ?";

        String sqlAsignacion = "UPDATE asignaciones "
                             + "SET id_estado_asignacion = 2, fecha_entrega = NOW() "
                             + "WHERE id_paquete = ?";

        try (Connection conn = ConexionBD.obtenerConexion()) {

            conn.setAutoCommit(false);

            try {

                try (PreparedStatement ps = conn.prepareStatement(sqlPaquete)) {
                    ps.setString(1, nombreEstado);
                    ps.setInt(2, idPaquete);
                    ps.executeUpdate();
                }

                // La asignacion se cierra unicamente cuando el paquete se entrega
                if (nombreEstado.equals(EstadoPaquete.ENTREGADO.toString())) {
                    try (PreparedStatement ps = conn.prepareStatement(sqlAsignacion)) {
                        ps.setInt(1, idPaquete);
                        ps.executeUpdate();
                    }
                }

                conn.commit();

            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public Paquete mapearPaquete(ResultSet rs) throws SQLException {

        // Cuando el paquete no tiene vehiculo asignado la placa viene nula
        model.Vehiculo vehiculo = null;

        if (rs.getString("placa") != null) {
            vehiculo = vehiculoDAO.mapearVehiculo(rs);
        }

        return new Paquete(
                rs.getInt("id_paquete"),
                rs.getString("destinatario"),
                rs.getString("detalle"),
                rs.getDouble("peso"),
                EstadoPaquete.valueOf(rs.getString("nombre_estado")),
                vehiculo);
    }

    // Guarda la relacion paquete - vehiculo en la tabla asignaciones
    private void guardarAsignacion(Connection conn, int idPaquete, String placa, Integer idDespachador) throws SQLException {

        String sql = "INSERT INTO asignaciones "
                   + "(id_paquete, id_vehiculo, id_despachador, id_estado_asignacion, fecha_asignada) "
                   + "VALUES (?, (SELECT id_vehiculo FROM vehiculos WHERE placa = ?), ?, ?, NOW())";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idPaquete);
            ps.setString(2, placa);

            if (idDespachador == null) {
                ps.setNull(3, Types.INTEGER);
            } else {
                ps.setInt(3, idDespachador);
            }

            ps.setInt(4, ASIGNACION_PENDIENTE);

            ps.executeUpdate();
        }
    }

    private void borrarAsignaciones(Connection conn, int idPaquete) throws SQLException {

        String sql = "DELETE FROM asignaciones WHERE id_paquete = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPaquete);
            ps.executeUpdate();
        }
    }

}
