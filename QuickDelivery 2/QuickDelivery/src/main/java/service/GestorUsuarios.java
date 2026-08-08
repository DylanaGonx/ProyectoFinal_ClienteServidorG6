/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;
import exceptions.UsuarioDuplicadoException;
import java.util.ArrayList;
import model.Usuario;

/**
 *
 * @author Valeria
 */

public class GestorUsuarios {

    // Lista de usuarios registrados
    private ArrayList<Usuario> listaUsuarios;

    // Constructor
    public GestorUsuarios() {
        listaUsuarios = new ArrayList<>();
    }

    // Agrega un usuario
    public void agregarUsuario(Usuario usuario) throws UsuarioDuplicadoException {

        // Revisa si el usuario ya existe
        for (Usuario u : listaUsuarios) {

            if (u.getUsuario().equalsIgnoreCase(usuario.getUsuario())) {

                throw new UsuarioDuplicadoException("Ya existe un usuario con ese nombre.");

            }

        }

        listaUsuarios.add(usuario);

    }

    // Obtiene todos los usuarios
    public ArrayList<Usuario> obtenerUsuarios() {

        return listaUsuarios;

    }

    // Busca un usuario por su nombre de usuario
    public Usuario buscarUsuario(String usuario) {

        for (Usuario u : listaUsuarios) {

            if (u.getUsuario().equalsIgnoreCase(usuario)) {

                return u;

            }

        }

        return null;

    }

    // Elimina un usuario
    public boolean eliminarUsuario(String usuario) {

        Usuario encontrado = buscarUsuario(usuario);

        if (encontrado != null) {

            listaUsuarios.remove(encontrado);
            return true;

        }

        return false;

    }

    // Modifica un usuario
    public boolean modificarUsuario(Usuario nuevoUsuario) {

        Usuario usuario = buscarUsuario(nuevoUsuario.getUsuario());

        if (usuario != null) {

            usuario.setContrasenia(nuevoUsuario.getContrasenia());
            usuario.setrol(nuevoUsuario.getrol());

            return true;

        }

        return false;

    }

}

