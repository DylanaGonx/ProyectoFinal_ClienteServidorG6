/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author ssanc
 */
public class Usuario {
    // Datos del usuario
    private String usuario;
    private String contrasenia;
    private String rol;
    
    //Constructor
    public Usuario(String usuario, String contrasenia, String rol) {
        this.usuario = usuario;
        this.contrasenia = contrasenia;
        this.rol = rol;
    }

    // getters y setters
    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getContrasenia() {
        return contrasenia;
    }

    public void setContrasenia(String contrasenia) {
        this.contrasenia = contrasenia;
    }
    public String getrol(){
        return rol;
    }
    
    public void setrol(String rol){
        this.rol = rol;
    }
    

    
}
