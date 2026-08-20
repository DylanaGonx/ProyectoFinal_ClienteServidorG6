/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package exceptions;

/**
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
