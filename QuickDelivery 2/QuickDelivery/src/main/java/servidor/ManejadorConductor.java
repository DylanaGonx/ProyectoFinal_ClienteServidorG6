/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package servidor;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;

import model.EstadoPaquete;
import model.Monitoreable;
import model.Paquete;
import model.Usuario;
import model.Vehiculo;

import service.GestorAuditoria;
import service.GestorIncidencias;
import service.GestorPaquetes;
import service.GestorUbicaciones;
import service.GestorUsuarios;
import service.GestorVehiculos;

/**
 * 
 * @author Grupo 6
 */
public class ManejadorConductor implements Runnable {

    private String placa;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    // Id del usuario conductor, se usa para la auditoria
    private int idConductor;

    /*
     * El vehiculo que atiende este hilo, visto como Monitoreable. Se guarda con
     * el tipo de la interfaz y no con el de la clase concreta, para que al hilo
     * le de igual si es una moto, un camion o un furgon.
     */
    private Monitoreable vehiculoMonitoreado;

    /*
     * Flota conectada, compartida por todos los hilos del servidor.
     *
     * Se usa un ConcurrentHashMap y no un HashMap normal porque varios hilos
     * entran y salen de este mapa al mismo tiempo. La coleccion concurrente se
     * encarga ella misma del bloqueo, asi que no hay que envolver cada acceso
     * en un synchronized y los hilos no se quedan esperando unos a otros.
     *
     * La llave es la placa del vehiculo y el valor es la salida de ese
     * conductor, que es por donde se le mandan los avisos.
     */
    private static ConcurrentHashMap<String, DataOutputStream> flotaConectada = new ConcurrentHashMap<>();

    private GestorVehiculos gestorVehiculos = new GestorVehiculos();
    private GestorPaquetes gestorPaquetes = new GestorPaquetes();
    private GestorUbicaciones gestorUbicaciones = new GestorUbicaciones();
    private GestorIncidencias gestorIncidencias = new GestorIncidencias();
    private GestorUsuarios gestorUsuarios = new GestorUsuarios();
    private GestorAuditoria gestorAuditoria = new GestorAuditoria();

    public ManejadorConductor(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {

        try {

            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            // Primero se identifica el vehiculo y despues se atienden reportes
            if (identificarVehiculo()) {
                atenderReportes();
            }

        } catch (IOException ex) {
            System.out.println("Conductor del vehiculo " + placa + " desconectado.");
        } finally {
            desconectar();
        }
    }

    /*
     * Le pide la placa al cliente hasta que mande una que exista en la base de
     * datos y que no este siendo usada por otra conexion.
     */
    private boolean identificarVehiculo() throws IOException {

        while (true) {

            out.writeUTF("SUBMITPLACA");

            String placaRecibida = in.readUTF();

            if (placaRecibida.equals("SALIR")) {
                return false;
            }

            Vehiculo vehiculo;

            try {
                vehiculo = gestorVehiculos.buscarVehiculo(placaRecibida);
            } catch (SQLException ex) {
                out.writeUTF("ERROR;No hay conexion con la base de datos. " + ex.getLocalizedMessage());
                return false;
            }

            if (vehiculo == null) {
                out.writeUTF("PLACANOEXISTE");
                continue;
            }

            /*
             * putIfAbsent hace en un solo paso lo que antes eran dos: revisa si
             * la placa ya estaba y, si no estaba, la agrega. Al ser una sola
             * operacion atomica del ConcurrentHashMap, dos conductores con la
             * misma placa no pueden colarse a la vez.
             * Devuelve null cuando la placa no estaba ocupada.
             */
            if (flotaConectada.putIfAbsent(placaRecibida, out) != null) {
                out.writeUTF("PLACAENUSO");
                continue;
            }

            placa = placaRecibida;

            //Se guarda como Monitoreable: polimorfismo por interfaz
            vehiculoMonitoreado = vehiculo;

            int enLinea = flotaConectada.size();

            buscarIdConductor(vehiculo);

            out.writeUTF("PLACAACEPTADA");
            System.out.println("Vehiculo " + placa + " conectado. Vehiculos en linea: " + enLinea);

            gestorAuditoria.registrar(idConductor, "vehiculo " + placa, "CONEXION", "Servidor",
                    "El vehiculo " + placa + " se conecto al servidor");

            // El vehiculo pasa a estar en ruta mientras este conectado
            cambiarEstadoVehiculo("En ruta");

            enviarPaquetes();

            return true;
        }
    }

    // Ciclo principal: se queda leyendo los reportes que manda el conductor
    private void atenderReportes() throws IOException {

        while (true) {

            String mensaje = in.readUTF();

            if (mensaje.equals("SALIR")) {
                break;
            }

            if (mensaje.startsWith("UBICACION")) {
                procesarUbicacion(mensaje);

            } else if (mensaje.startsWith("ESTADO")) {
                procesarEstado(mensaje);

            } else if (mensaje.startsWith("INCIDENCIA")) {
                procesarIncidencia(mensaje);

            } else if (mensaje.startsWith("PAQUETES")) {
                enviarPaquetes();

            } else {
                out.writeUTF("ERROR;Instruccion no reconocida: " + mensaje);
            }
        }
    }

    // UBICACION;latitud;longitud  (RF-11 y RF-12)
    private void procesarUbicacion(String mensaje) throws IOException {

        String datos[] = mensaje.split(";");

        if (datos.length < 3) {
            out.writeUTF("ERROR;La ubicacion viene incompleta.");
            return;
        }

        try {

            double latitud = Double.parseDouble(datos[1]);
            double longitud = Double.parseDouble(datos[2]);

            gestorUbicaciones.registrarUbicacion(placa, latitud, longitud, "En ruta");

            /*
             * Se actualiza el objeto a traves de la interfaz. El hilo no sabe
             * ni le importa que subclase de Vehiculo es.
             */
            vehiculoMonitoreado.actualizarUbicacion(latitud, longitud);

            System.out.println("[" + placa + "] " + vehiculoMonitoreado.obtenerEstado());

            out.writeUTF("OK;Ubicacion registrada");

            // Se le avisa a los demas conductores conectados
            avisarFlota("FLOTA;" + placa + ";" + latitud + ";" + longitud);

        } catch (NumberFormatException ex) {
            out.writeUTF("ERROR;La latitud y la longitud tienen que ser numeros.");

        } catch (SQLException ex) {
            out.writeUTF("ERROR;No se pudo guardar la ubicacion. " + ex.getLocalizedMessage());
        }
    }

    // ESTADO;idPaquete;NUEVOESTADO  (RF-13)
    private void procesarEstado(String mensaje) throws IOException {

        String datos[] = mensaje.split(";");

        if (datos.length < 3) {
            out.writeUTF("ERROR;El cambio de estado viene incompleto.");
            return;
        }

        try {

            int idPaquete = Integer.parseInt(datos[1]);
            EstadoPaquete estado = EstadoPaquete.valueOf(datos[2]);

            gestorPaquetes.cambiarEstado(idPaquete, estado);

            System.out.println("[" + placa + "] paquete " + idPaquete + " paso a " + estado);

            gestorAuditoria.registrar(idConductor, "vehiculo " + placa, "CAMBIO_ESTADO", "Entregas",
                    "El paquete " + idPaquete + " paso a " + estado);

            out.writeUTF("OK;El paquete " + idPaquete + " quedo como " + estado);

            enviarPaquetes();

        } catch (NumberFormatException ex) {
            out.writeUTF("ERROR;El numero de paquete no es valido.");

        } catch (IllegalArgumentException ex) {
            out.writeUTF("ERROR;El estado enviado no existe.");

        } catch (SQLException ex) {
            out.writeUTF("ERROR;No se pudo cambiar el estado. " + ex.getLocalizedMessage());
        }
    }

    // INCIDENCIA;idPaquete;tipo;descripcion  (HU-07)
    // INCIDENCIA;idPaquete;tipo;descripcion;latitud;longitud
    private void procesarIncidencia(String mensaje) throws IOException {

        String datos[] = mensaje.split(";");

        if (datos.length < 4) {
            out.writeUTF("ERROR;La incidencia viene incompleta.");
            return;
        }

        try {

            int idPaquete = Integer.parseInt(datos[1]);

            /*
             * La posicion es opcional en el protocolo: si el cliente la manda
             * (datos[4] y datos[5]) se guarda con la incidencia, para que el
             * despachador sepa donde ocurrio. Si no viene, queda nula.
             */
            Double latitud = null;
            Double longitud = null;

            if (datos.length >= 6) {
                try {
                    latitud = Double.parseDouble(datos[4]);
                    longitud = Double.parseDouble(datos[5]);
                } catch (NumberFormatException ex) {
                    // La ubicacion vino con datos raros, se guarda la incidencia igual pero sin ubicacion
                    latitud = null;
                    longitud = null;
                }
            }

            gestorIncidencias.registrarIncidencia(idPaquete, placa, datos[2], datos[3], latitud, longitud);

            String detalleUbicacion = (latitud != null) ? " en " + latitud + ", " + longitud : "";
            System.out.println("[" + placa + "] incidencia en el paquete " + idPaquete + ": " + datos[3] + detalleUbicacion);

            gestorAuditoria.registrar(idConductor, "vehiculo " + placa, "INCIDENCIA", "Entregas",
                    "Incidencia en el paquete " + idPaquete + ": " + datos[3] + detalleUbicacion);

            out.writeUTF("OK;Incidencia registrada");

        } catch (NumberFormatException ex) {
            out.writeUTF("ERROR;El numero de paquete no es valido.");

        } catch (exceptions.DatosInvalidosException ex) {
            out.writeUTF("ERROR;" + ex.getMessage());

        } catch (SQLException ex) {
            out.writeUTF("ERROR;No se pudo guardar la incidencia. " + ex.getLocalizedMessage());
        }
    }

    // Le manda al conductor la lista de paquetes que lleva su vehiculo
    private void enviarPaquetes() throws IOException {

        try {

            StringBuilder lista = new StringBuilder();

            for (Paquete paquete : gestorPaquetes.obtenerPaquetesPorPlaca(placa)) {
                lista.append("Paquete ").append(paquete.getId())
                     .append(" - ").append(paquete.getDestinatario())
                     .append(" - ").append(paquete.getDireccion())
                     .append(" - ").append(paquete.getPeso()).append(" kg")
                     .append(" - ").append(paquete.getEstado())
                     .append("\n");
            }

            if (lista.length() == 0) {
                lista.append("El vehiculo no tiene paquetes asignados.\n");
            }

            out.writeUTF("PAQUETES;" + lista.toString());

        } catch (SQLException ex) {
            out.writeUTF("ERROR;No se pudieron consultar los paquetes. " + ex.getLocalizedMessage());
        }
    }

    /*
     * Recorre los escritores de todos los conductores conectados y les manda el
     * mismo mensaje. El recorrido va dentro de synchronized para que otro hilo
     * no agregue o quite conexiones mientras se esta escribiendo.
     */
    private void avisarFlota(String mensaje) {

        /*
         * El recorrido no necesita synchronized: el ConcurrentHashMap permite
         * recorrerlo mientras otro hilo agrega o quita vehiculos, sin lanzar
         * ConcurrentModificationException como haria un HashMap normal.
         */
        for (String otraPlaca : flotaConectada.keySet()) {

            // No tiene sentido devolverle el aviso al mismo que lo mando
            if (otraPlaca.equals(placa)) {
                continue;
            }

            try {
                flotaConectada.get(otraPlaca).writeUTF(mensaje);
            } catch (IOException ex) {
                System.out.println("No se le pudo avisar al vehiculo " + otraPlaca
                        + ": " + ex.getLocalizedMessage());
            }
        }
    }

    // Busca el id del usuario conductor que tiene asignado el vehiculo
    private void buscarIdConductor(Vehiculo vehiculo) {

        try {

            Usuario conductor = gestorUsuarios.buscarUsuario(vehiculo.getConductor());

            if (conductor != null) {
                idConductor = conductor.getId();
            }

        } catch (SQLException ex) {
            System.out.println("No se pudo identificar al conductor: " + ex.getLocalizedMessage());
        }
    }

    private void cambiarEstadoVehiculo(String estado) {

        try {
            gestorVehiculos.cambiarEstado(placa, estado);
        } catch (SQLException ex) {
            System.out.println("No se pudo cambiar el estado del vehiculo: " + ex.getLocalizedMessage());
        }
    }

    /*
     * Limpia todo lo que dejo la conexion: saca la placa y el escritor de los
     * conjuntos compartidos, devuelve el vehiculo a disponible y cierra el
     * socket.
     */
    private void desconectar() {

        if (placa != null) {

            //Sacar del mapa concurrente tampoco necesita bloqueo manual
            flotaConectada.remove(placa);

            int enLinea = flotaConectada.size();

            cambiarEstadoVehiculo("Disponible");

            gestorAuditoria.registrar(idConductor, "vehiculo " + placa, "DESCONEXION", "Servidor",
                    "El vehiculo " + placa + " se desconecto del servidor");

            System.out.println("Vehiculo " + placa + " desconectado. Vehiculos en linea: " + enLinea);
        }

        try {
            socket.close();
        } catch (IOException ex) {
            System.out.println("Error: " + ex.toString());
        }
    }

}
