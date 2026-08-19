/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Clase encargada de entregar la conexion a la base de datos quick_delivery.
 *
 * @author Grupo 6
 */
public class ConexionBD {

    private static final String URL = "jdbc:mysql://localhost:3306/quick_delivery?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String CLAVE = "root";

    // Carga el controlador de MySQL una sola vez
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ex) {
            System.out.println("No se pudo cargar el controlador de MySQL: " + ex.getLocalizedMessage());
        }
    }

    public static Connection obtenerConexion() throws SQLException {
        return DriverManager.getConnection(URL, USER, CLAVE);
    }

}
