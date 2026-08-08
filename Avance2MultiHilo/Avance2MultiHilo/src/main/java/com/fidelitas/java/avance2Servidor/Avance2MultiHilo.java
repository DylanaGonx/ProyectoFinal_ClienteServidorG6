/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.fidelitas.java.avance2Servidor;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
/**
 *
 * @author dg106
 */
public class Avance2MultiHilo {

    public static void main(String[] args) {
        ServerSocket server = null;
        try {
            //se crea un canal de conexion (pero aun no esta abierto)
            server = new ServerSocket(5433);
        } catch (IOException e) {
            System.out.println(e.getLocalizedMessage());
        }
 
        System.out.println("Servidor arriba, esperando conexiones...");
 
        
        while (true) {
            try {
                //con .accept puede recibir otro cliente sin esperar a que el primero termine
                Socket s1 = server.accept();
                System.out.println("Se conecto un cliente: " + s1.getInetAddress());
 
                ManejadorCliente tarea = new ManejadorCliente(s1);
                Thread hilo = new Thread(tarea);
                hilo.start();
 
            } catch (IOException ex) {
                System.out.println(ex.getLocalizedMessage());
            }
        }
    }
}
