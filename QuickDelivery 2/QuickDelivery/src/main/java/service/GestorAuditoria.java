/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import dao.LogDAO;
import exceptions.PersistenciaException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.ArrayList;

/**
 * Auditoria del sistema.
 *
 * Cada movimiento queda anotado en dos lugares a la vez:
 *
 *   1. En la tabla logs_sistema de la base de datos, por medio de LogDAO
 *   2. En el archivo bitacora.dat, que queda en la carpeta del proyecto
 *
 * El archivo sirve de respaldo: si la base de datos esta caida, el movimiento
 * igual queda anotado y despues se puede revisar.
 *
 * @author Grupo 6
 */
public class GestorAuditoria {

    private static final String ARCHIVO = "bitacora.txt";

    private LogDAO logDAO;

    public GestorAuditoria() {
        logDAO = new LogDAO();
    }

    /*
     * Registra el movimiento en la base y en el archivo. Ninguno de los dos
     * puede hacer fallar la operacion principal del usuario, por eso los
     * errores solo se avisan en consola.
     */
    public void registrar(int idUsuario, String usuario, String accion, String modulo, String detalle) {

        //1. Base de datos
        logDAO.registrar(idUsuario, accion, modulo, detalle);

        //2. Archivo, con la fecha y la hora adelante
        try {
            escribirEnArchivo(fechaHora() + " | " + usuario + " | " + accion
                    + " | " + modulo + " | " + detalle);
        } catch (PersistenciaException ex) {
            System.out.println(ex.getMessage());
        }
    }

    /*
     * Agrega una linea al final del archivo. El segundo parametro del
     * FileOutputStream en true es lo que hace que no se borre lo anterior.
     *
     * Se escribe con write() y no con writeUTF() a proposito. writeUTF le mete
     * dos bytes de largo adelante de cada texto y no pone salto de linea, con
     * lo cual el archivo sale ilegible en el Bloc de notas. Asi queda como un
     * archivo de texto de verdad, que cualquiera puede abrir y leer.
     */
    public void escribirEnArchivo(String linea) throws PersistenciaException {

        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(ARCHIVO, true))) {

            dos.write((linea + "\r\n").getBytes("UTF-8"));

        } catch (IOException ex) {
            throw new PersistenciaException("Error al escribir en la bitacora: " + ex.getMessage(), ex);
        }
    }

    /*
     * Anota el movimiento unicamente en el archivo. Se usa para los intentos de
     * ingreso fallidos: no se pueden guardar en logs_sistema porque esa tabla
     * exige un id de usuario valido, y en un intento fallido no se sabe quien
     * trato de entrar.
     */
    public void registrarSoloArchivo(String usuario, String accion, String modulo, String detalle) {

        try {
            escribirEnArchivo(fechaHora() + " | " + usuario + " | " + accion
                    + " | " + modulo + " | " + detalle);
        } catch (PersistenciaException ex) {
            System.out.println(ex.getMessage());
        }
    }

    //Devuelve la fecha y la hora del momento, para encabezar cada linea
    private String fechaHora() {
        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());
    }

    /*
     * Lee todas las lineas del archivo y las devuelve en una lista.
     */
    public ArrayList<String> leerBitacora() throws PersistenciaException {

        ArrayList<String> lineas = new ArrayList<>();

        File archivo = new File(ARCHIVO);

        if (!archivo.exists()) {
            return lineas;
        }

        try (DataInputStream dis = new DataInputStream(new FileInputStream(ARCHIVO))) {

            String contenido = new String(dis.readAllBytes(), "UTF-8");

            for (String linea : contenido.split("\r\n")) {
                if (!linea.trim().isEmpty()) {
                    lineas.add(linea);
                }
            }

        } catch (IOException ex) {
            throw new PersistenciaException("Error al leer la bitacora: " + ex.getMessage(), ex);
        }

        return lineas;
    }

}
