/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package com.fidelitas.java.quickdelivery;

import cliente.ClienteVehiculo;

/**
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
