/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package servidor;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Servidor concurrente de QuickDelivery.
 *
 * Se queda esperando conexiones y por cada conductor que entra levanta un hilo
 * ManejadorConductor, de manera que varios vehiculos pueden estar reportando su
 * ubicacion al mismo tiempo sin bloquearse entre ellos.
 *
 * Se ejecuta con la clase servidor.ServidorQuickDelivery como clase principal.
 *
 * @author Grupo 6
 */
public class ServidorQuickDelivery {

    public static final int PUERTO = 5433;

    public static void main(String[] args) {

        try {

            System.out.println("Servidor QuickDelivery iniciado en el puerto " + PUERTO + ".");

            ServerSocket servidor = new ServerSocket(PUERTO);

            while (true) {

                // accept() devuelve el socket del conductor que se acaba de
                // conectar y de una vez se le asigna su propio hilo
                ManejadorConductor manejador = new ManejadorConductor(servidor.accept());
                manejador.start();
            }

        } catch (IOException ex) {
            System.out.println("Error: " + ex.toString());
        }
    }

}
