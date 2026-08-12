/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.fidelitas.java.avance2Servidor;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
/**
 * Primera prueba de sockets del Avance 2.
 *
 * Este servidor solo intercambia mensajes de texto, sin logica de negocio ni
 * base de datos. Se dejo dentro del proyecto porque muestra el paso previo al
 * servidor definitivo, que es servidor.ServidorQuickDelivery.
 *
 * Escucha en el puerto 5500 para no chocar con el servidor definitivo, que usa
 * el 5433.
 *
 * @author dg106
 */
public class Avance2MultiHilo {

    public static final int PUERTO = 5500;

    public static void main(String[] args) {
        ServerSocket server = null;
        try {
            //se crea un canal de conexion (pero aun no esta abierto)
            server = new ServerSocket(PUERTO);
        } catch (IOException e) {
            System.out.println(e.getLocalizedMessage());
        }

        //si el puerto estaba ocupado el canal quedo nulo y no se puede seguir
        if (server == null) {
            System.out.println("No se pudo abrir el puerto " + PUERTO + ".");
            return;
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
