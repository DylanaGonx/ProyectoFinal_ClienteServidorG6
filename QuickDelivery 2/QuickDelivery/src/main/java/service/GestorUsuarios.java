/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import dao.UsuarioDAO;
import exceptions.UsuarioDuplicadoException;

import java.sql.SQLException;
import java.util.ArrayList;

import model.Usuario;

/**
 * 
 * @author Grupo 6
 */
public class GestorUsuarios {

    private UsuarioDAO usuarioDAO;

    // Constructor
    public GestorUsuarios() {
        usuarioDAO = new UsuarioDAO();
    }

    // Agrega un usuario
    public void agregarUsuario(Usuario usuario) throws UsuarioDuplicadoException, SQLException {

        // Revisa si el usuario ya existe
        if (usuarioDAO.obtenerPorUsuario(usuario.getUsuario()) != null) {
            throw new UsuarioDuplicadoException("Ya existe un usuario con ese nombre.");
        }

        usuarioDAO.insertar(usuario);
    }

    // Obtiene todos los usuarios
    public ArrayList<Usuario> obtenerUsuarios() throws SQLException {
        return usuarioDAO.listarTodos();
    }

    // Obtiene unicamente los usuarios con rol de conductor
    public ArrayList<Usuario> obtenerConductores() throws SQLException {
        return usuarioDAO.listarConductores();
    }

    // Busca un usuario por su nombre de usuario
    public Usuario buscarUsuario(String usuario) throws SQLException {
        return usuarioDAO.obtenerPorUsuario(usuario);
    }

    // Elimina un usuario
    public boolean eliminarUsuario(String usuario) throws SQLException {

        if (usuarioDAO.obtenerPorUsuario(usuario) == null) {
            return false;
        }

        usuarioDAO.eliminar(usuario);
        return true;
    }

    // Modifica un usuario
    public boolean modificarUsuario(Usuario nuevoUsuario) throws SQLException {

        if (usuarioDAO.obtenerPorUsuario(nuevoUsuario.getUsuario()) == null) {
            return false;
        }

        usuarioDAO.actualizar(nuevoUsuario);
        return true;
    }

}
