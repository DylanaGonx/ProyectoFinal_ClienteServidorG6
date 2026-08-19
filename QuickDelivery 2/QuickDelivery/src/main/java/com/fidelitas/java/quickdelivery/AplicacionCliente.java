/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package com.fidelitas.java.quickdelivery;

import cliente.ClienteVehiculo;

/**
 * APLICACION 2 de 2: CLIENTE DEL CONDUCTOR
 *
 * Es la aplicacion que lleva el conductor en la ruta. Se conecta por sockets al
 * servidor de la empresa, se identifica con la placa del vehiculo y desde ahi:
 *
 *   - manda su ubicacion cada cierto tiempo
 *   - consulta los paquetes que lleva asignados
 *   - cambia el estado de un paquete cuando lo entrega
 *   - reporta incidencias de la ruta
 *
 * No toca la base de datos directamente: todo lo pide por el socket. Por eso
 * puede correr en otra computadora, que es la idea de un cliente.
 *
 * Se pueden abrir varias, una por cada vehiculo, para probar la concurrencia.
 *
 * @author Grupo 6
 */
public class AplicacionCliente {

    public static void main(String[] args) {

        System.out.println("QuickDelivery S.A. - Aplicacion Cliente (conductor)");

        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {

                ClienteVehiculo clienteVehiculo = new ClienteVehiculo();
                clienteVehiculo.setVisible(true);

                //La conexion corre en su propio hilo para no congelar la ventana
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        clienteVehiculo.conectar();
                    }
                }).start();
            }
        });
    }

}
