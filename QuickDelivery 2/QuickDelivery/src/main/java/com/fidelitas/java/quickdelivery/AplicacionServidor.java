/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package com.fidelitas.java.quickdelivery;

import servidor.ServidorQuickDelivery;
import ui.Login;

/**
 * 
 * @author Grupo 6
 */
public class AplicacionServidor {

    public static void main(String[] args) {

        System.out.println("QuickDelivery S.A. - Aplicacion Servidor (backoffice)");

        /*
         * El servidor de sockets corre en su propio hilo porque el metodo
         * iniciar() se queda esperando conexiones y nunca devuelve el control.
         * Si se llamara directo, la ventana del backoffice nunca abriria.
         */
        Thread hiloServidor = new Thread(new Runnable() {
            @Override
            public void run() {
                new ServidorQuickDelivery().iniciar();
            }
        });

        hiloServidor.start();

        //La ventana del backoffice arranca en el hilo de Swing
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Login().setVisible(true);
            }
        });
    }

}
