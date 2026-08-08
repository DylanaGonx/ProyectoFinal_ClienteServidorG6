/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import exceptions.PaqueteDuplicadoException;
import java.util.ArrayList;
import model.Paquete;

/**
 *
 * @author ssanc
 */
public class GestorPaquetes {
    
    //Lista de los paquetes registrados
    private ArrayList<Paquete> listaPaquetes;
    
    //Constructor
    public GestorPaquetes(){
        listaPaquetes = new ArrayList<>();
    }
    
    //Agrega un paquete
    public void agregarPaquete(Paquete paquete) throws PaqueteDuplicadoException {
        
        //Revisa si el Id existe
        for(Paquete p : listaPaquetes) {
            if(p.getId() == paquete.getId()) {
                throw new PaqueteDuplicadoException("Ya existe un paquete con ese ID.");
            }
        }
        listaPaquetes.add(paquete);
    }
    public ArrayList<Paquete> obtenerPaquetes(){
        return listaPaquetes;
    }
    
    //Busca un paquete por el Id
    public Paquete buscarPaquete(int id) {
        
        for(Paquete paquete : listaPaquetes) {
            if(paquete.getId() == id) {
                return paquete;
            }
        }
        return null;
        
    }
    
    //Eliminar un paquete
    public boolean eliminarPaquete(int id) {
        Paquete paquete = buscarPaquete(id);
        
        if (paquete != null) {
            
            listaPaquetes.remove(paquete);
            return true;
        }
        
        return false;
        
    }
    //Modifica un paquete
    public boolean modificarPaquete(Paquete nuevoPaquete) {
        Paquete paquete = buscarPaquete(nuevoPaquete.getId());
        
        if(paquete != null) {
            
            paquete.setDestinatario(nuevoPaquete.getDestinatario());
            paquete.setDireccion(nuevoPaquete.getDireccion());
            paquete.setPeso(nuevoPaquete.getPeso());
            paquete.setEstado(nuevoPaquete.getEstado());
            paquete.setVehiculo(nuevoPaquete.getVehiculo());
            
            return true;
            
            
        }
        
        return false;
    }
    
}
