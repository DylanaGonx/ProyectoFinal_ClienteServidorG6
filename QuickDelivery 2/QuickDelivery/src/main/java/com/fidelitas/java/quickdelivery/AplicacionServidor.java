/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package com.fidelitas.java.quickdelivery;

import servidor.ServidorQuickDelivery;
import ui.Login;

/**
 * APLICACION 1 de 2: SERVIDOR / BACKOFFICE DE LA EMPRESA
 *
 * Es la aplicacion que se instala en las oficinas de QuickDelivery. Hace dos
 * cosas al mismo tiempo:
 *
 *   1. Levanta el servidor de sockets, que queda escuchando a los conductores
 *      que se conectan desde la aplicacion cliente
 *   2. Abre el sistema de escritorio del backoffice: login, monitor de
 *      paquetes, registro de vehiculos, asignaciones e incidencias
 *
 * Las dos cosas van juntas a proposito: el backoffice no sirve de nada sin el
 * servidor escuchando, y el servidor sin el backoffice no tiene quien despache.
 *
 * La otra aplicacion es AplicacionCliente, la del conductor.
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
