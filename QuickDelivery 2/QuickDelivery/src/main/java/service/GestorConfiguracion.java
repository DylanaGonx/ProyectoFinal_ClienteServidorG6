/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import exceptions.PersistenciaException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import model.Configuracion;

/**
 * Guarda y lee el objeto Configuracion de un archivo, usando serializacion.
 *
 * El archivo queda en la carpeta del proyecto con el nombre configuracion.dat.
 * Si todavia no existe, se crea con los valores por defecto.
 *
 * @author Grupo 6
 */
public class GestorConfiguracion {

    private static final String ARCHIVO = "configuracion.dat";

    //Escribe el objeto completo en el archivo
    public void guardar(Configuracion configuracion) throws PersistenciaException {

        //No tiene sentido guardar un nulo, dejaria el archivo inservible
        if (configuracion == null) {
            throw new PersistenciaException("No se puede guardar una configuracion vacia.");
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ARCHIVO))) {

            oos.writeObject(configuracion);

        } catch (IOException ex) {
            throw new PersistenciaException("Error al guardar la configuracion: " + ex.getMessage(), ex);
        }
    }

    /*
     * Lee el objeto del archivo. La primera vez que corre el sistema el archivo
     * no existe, entonces se crea uno con los valores por defecto.
     */
    public Configuracion cargar() throws PersistenciaException {

        File archivo = new File(ARCHIVO);

        if (!archivo.exists()) {
            Configuracion porDefecto = new Configuracion();
            guardar(porDefecto);
            return porDefecto;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(ARCHIVO))) {

            Configuracion leida = (Configuracion) ois.readObject();

            //Si el archivo venia dañado se rehace con los valores por defecto
            if (leida == null) {
                Configuracion porDefecto = new Configuracion();
                guardar(porDefecto);
                return porDefecto;
            }

            return leida;

        } catch (IOException | ClassNotFoundException ex) {
            throw new PersistenciaException("Error al leer la configuracion: " + ex.getMessage(), ex);
        }
    }

    /*
     * Version que no interrumpe el arranque del programa. Si el archivo esta
     * dañado se sigue trabajando con los valores por defecto.
     */
    public Configuracion cargarSeguro() {

        try {
            return cargar();
        } catch (PersistenciaException ex) {
            System.out.println(ex.getMessage());
            return new Configuracion();
        }
    }

}
