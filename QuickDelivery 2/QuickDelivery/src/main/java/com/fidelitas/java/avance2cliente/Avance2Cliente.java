/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package com.fidelitas.java.avance2cliente;

import com.fidelitas.java.avance2Servidor.Avance2MultiHilo;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;

/**
 * Cliente de la primera prueba de sockets del Avance 2. Solo se conecta y lee
 * lo que le mande el servidor.
 *
 * El cliente definitivo, el que se identifica con la placa y reporta ubicacion,
 * es cliente.ClienteVehiculo.
 *
 * @author dg106
 */
public class Avance2Cliente {
    public static void main(String[] args) {
        //se crea carrera de comunicacion pero aun no apunta a nada
        Socket sc = null;
        try {
            //se le asigna a quien conectarse(ip local de la compu)
            sc = new Socket("127.0.0.1", Avance2MultiHilo.PUERTO);
        } catch (IOException e) {
            System.out.println(e.getLocalizedMessage());
        }

        //si el servidor no estaba arriba el socket quedo nulo y no se puede seguir
        if (sc == null) {
            System.out.println("No se pudo conectar con el servidor.");
            return;
        }

        try {
            DataInputStream dis = new DataInputStream(sc.getInputStream());


            while (true) {
                try {
                    String msj = dis.readUTF();
                    System.out.println(msj);
                    //se usa EOF para salir cuando no hay mas que leer/el sevidor corta comunicacion
                } catch (EOFException ex) { //para evitar el spam de socket is closed
                    break;
                }
            }

            //se cierra todo
            dis.close();
            sc.close();
        } catch (IOException e) {
            System.out.println(e.getLocalizedMessage());
        }
    }
}
