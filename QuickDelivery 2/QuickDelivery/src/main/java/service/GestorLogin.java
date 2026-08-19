/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import dao.UsuarioDAO;

import java.sql.SQLException;

import model.Usuario;

/**
 * Autenticacion de los usuarios del sistema (RF-01 y RF-02).
 *
 * @author Valeria
 */
public class GestorLogin {

    private UsuarioDAO usuarioDAO;
    private GestorAuditoria gestorAuditoria;

    public GestorLogin() {
        usuarioDAO = new UsuarioDAO();
        gestorAuditoria = new GestorAuditoria();
    }

    /*
     * Devuelve el usuario cuando las credenciales son correctas y null cuando
     * no lo son. El objeto que regresa es un Administrador, un Conductor o un
     * Despachador segun el rol que tenga en la base de datos.
     */
    public Usuario iniciarSesion(String usuario, String contrasenia) throws SQLException {

        Usuario encontrado = usuarioDAO.validar(usuario, contrasenia);

        if (encontrado != null) {

            gestorAuditoria.registrar(encontrado.getId(), encontrado.getUsuario(),
                    "LOGIN", "Seguridad",
                    "Inicio de sesion del usuario " + encontrado.getUsuario());

        } else {

            /*
             * El intento fallido tambien se anota, pero solo en el archivo,
             * porque logs_sistema necesita un id de usuario valido y aqui no
             * se sabe quien intento entrar.
             */
            gestorAuditoria.registrarSoloArchivo(usuario, "LOGIN_FALLIDO", "Seguridad",
                    "Credenciales incorrectas");
        }

        return encontrado;
    }

    // Deja registrada la salida del usuario en la auditoria
    public void cerrarSesion(Usuario usuario) {

        if (usuario != null) {
            gestorAuditoria.registrar(usuario.getId(), usuario.getUsuario(),
                    "LOGOUT", "Seguridad",
                    "Cierre de sesion del usuario " + usuario.getUsuario());
        }
    }

}
