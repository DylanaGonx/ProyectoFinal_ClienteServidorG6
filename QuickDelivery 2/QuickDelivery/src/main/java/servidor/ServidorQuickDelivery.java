/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package servidor;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Servidor concurrente de QuickDelivery.
 *
 * Se queda esperando conexiones de los conductores y cada una se la entrega a
 * un ExecutorService, que es un grupo de hilos ya creados. Asi el servidor no
 * abre un hilo nuevo por cada conexion, sino que reparte el trabajo entre los
 * hilos del grupo, igual que el ejemplo de la tienda visto en clase.
 *
 * Esta clase no tiene main. La levanta la aplicacion del servidor
 * (com.fidelitas.java.quickdelivery.AplicacionServidor) junto con el backoffice.
 *
 * @author Grupo 6
 */
public class ServidorQuickDelivery {

    public static final int PUERTO = 5433;

    // Cantidad de conductores que se pueden atender a la vez
    public static final int MAX_CONDUCTORES = 20;

    private ServerSocket servidor;
    private ExecutorService ejecutor;

    /*
     * Abre el puerto y se queda atendiendo. Este metodo no devuelve el control
     * hasta que el servidor se detenga, por eso quien lo llama lo hace desde un
     * hilo aparte.
     */
    public void iniciar() {

        //Grupo de hilos: se crean de una vez y se reutilizan
        ejecutor = Executors.newFixedThreadPool(MAX_CONDUCTORES);

        try {

            servidor = new ServerSocket(PUERTO);

            System.out.println("Servidor QuickDelivery iniciado en el puerto " + PUERTO + ".");
            System.out.println("Capacidad: " + MAX_CONDUCTORES + " conductores a la vez.");

            while (true) {

                /*
                 * accept() devuelve el socket del conductor que se acaba de
                 * conectar y se le entrega al grupo de hilos. El ejecutor
                 * decide cual hilo lo atiende.
                 */
                ejecutor.execute(new ManejadorConductor(servidor.accept()));
            }

        } catch (IOException ex) {
            System.out.println("Error del servidor: " + ex.toString());

        } finally {
            ejecutor.shutdown();
        }
    }

}
