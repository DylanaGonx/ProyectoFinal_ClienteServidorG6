/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;
import model.Usuario;

/**
 *
 * @author Valeria
 */
public class GestorLogin {

    private GestorUsuarios gestorUsuarios;

    public GestorLogin(GestorUsuarios gestorUsuarios) {
        this.gestorUsuarios = gestorUsuarios;
    }

    public Usuario iniciarSesion(String usuario, String contrasenia) {

        Usuario encontrado = gestorUsuarios.buscarUsuario(usuario);

        if (encontrado != null &&
            encontrado.getContrasenia().equals(contrasenia)) {

            return encontrado;
        }

        return null;
    }

}
