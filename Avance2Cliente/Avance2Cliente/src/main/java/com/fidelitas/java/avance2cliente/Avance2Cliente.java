/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package com.fidelitas.java.avance2cliente;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;

/**
 *
 * @author dg106
 */
public class Avance2Cliente {
    public static void main(String[] args) {
        //se crea carrera de comunicacion pero aun no apunta a nada
        Socket sc = null;
        try {
            //se le asigna a quien conectarse(ip local de la compu)
            sc = new Socket("127.0.0.1", 5433);
        } catch (IOException e) {
            System.out.println(e.getLocalizedMessage());
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
