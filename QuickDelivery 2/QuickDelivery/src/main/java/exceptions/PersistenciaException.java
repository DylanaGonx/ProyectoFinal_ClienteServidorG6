/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package exceptions;

/**
 * 
 * @author Grupo 6
 */
public class PersistenciaException extends QuickDeliveryException {

    // Constructor de la excepcion
    public PersistenciaException(String message) {
        super(message);
    }

    public PersistenciaException(String message, Throwable causa) {
        super(message, causa);
    }

}
