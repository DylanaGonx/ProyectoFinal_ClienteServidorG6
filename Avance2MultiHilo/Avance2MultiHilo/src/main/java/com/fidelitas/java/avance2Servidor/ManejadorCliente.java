/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.fidelitas.java.avance2Servidor;
//imports

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 *
 * @author dg106
 */
public class ManejadorCliente implements Runnable {

    //atributo
    private Socket socket;

//contructor para cliente
    public ManejadorCliente(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        //toma el nombre del hilo que esta operando, 
        //para ver en consola cual hilo esta atendiendo a este cliente
        System.out.println("[" + Thread.currentThread().getName() + "] Atendiendo cliente");
        try {
            
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            DataInputStream dis = new DataInputStream(socket.getInputStream());

            dos.writeUTF("Hola me conecté");
            
            //se maneja el mensaje que entra del cliente como un string
            String msjEntrante = "";

            //mientras no mande el mensaje exit
            while (!msjEntrante.equals("exit")) {
                //va seguir leyendo lo que mande este cliente 
                msjEntrante = dis.readUTF();
                System.out.println("Cliente dice: " + msjEntrante);
            }
            //cierra salida y entrada
            dos.close();
            dis.close();
            //cierra socket/canal
            socket.close();

        } catch (IOException ex) {
            System.out.println(ex.getLocalizedMessage());
        }
        //cuando sale del loop se avisa que terminó la conexion 
        System.out.println("[" + Thread.currentThread().getName() + "] TERMINATED");
    }
}// fin clase
