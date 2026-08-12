/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package exceptions;

/**
 * Excepcion base de todo el sistema QuickDelivery.
 *
 * De ella heredan las demas excepciones propias del proyecto. Sirve para poder
 * atrapar de un solo golpe cualquier error del negocio:
 *
 *     catch (QuickDeliveryException ex) { ... }
 *
 * y tambien para distinguirlos de los errores tecnicos como SQLException o
 * IOException, que se manejan aparte.
 *
 * @author Grupo 6
 */
public class QuickDeliveryException extends Exception {

    // Constructor de la excepcion
    public QuickDeliveryException(String message) {
        super(message);
    }

    /*
     * Este constructor guarda el error original que provoco la falla. Sirve
     * para no perder el motivo real cuando un error tecnico se convierte en un
     * error del negocio.
     */
    public QuickDeliveryException(String message, Throwable causa) {
        super(message, causa);
    }

}
