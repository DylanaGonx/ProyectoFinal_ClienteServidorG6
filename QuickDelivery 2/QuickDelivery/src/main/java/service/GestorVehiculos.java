/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import java.util.ArrayList;
import model.Vehiculo;
import exceptions.VehiculoDuplicadoException;

/**
 *
 * @author ssanc
 */
public class GestorVehiculos {
    
    //Lista de los vehiculos registrados
    private ArrayList<Vehiculo> listaVehiculos;
    //Constructor
    public GestorVehiculos(){
        listaVehiculos = new ArrayList<>();
        
    }
    
    //Agrega un vehiculo
    public void agregarVehiculo(Vehiculo vehiculo) throws VehiculoDuplicadoException {

        //Revisa si esa placa ya existe
        for(Vehiculo v : listaVehiculos) {
            if(vehiculo.getPlaca().equalsIgnoreCase(v.getPlaca())) {
                throw new VehiculoDuplicadoException("Ya existe un vehiculo con esa placa.");
            }
        }
        listaVehiculos.add(vehiculo);
        
        
    }
        
    
    public ArrayList<Vehiculo> obtenerVehiculos() {
        return listaVehiculos;
        
    }
    //Busca los vehiculos por la placa
    public Vehiculo buscarVehiculo(String placa) {
        
        for(Vehiculo vehiculo : listaVehiculos) {
            
            if(vehiculo.getPlaca().equalsIgnoreCase(placa)) {
                return vehiculo;
            }
        }
        return null;
            
        }
    
        //Eliminar un vehiculo por placa
    public boolean eliminarVehiculo(String placa){
         Vehiculo vehiculo = buscarVehiculo(placa);
         
         if(vehiculo != null){
             listaVehiculos.remove(vehiculo);
             return true;
         }
    return false;
    }
   
    //Modificar el vehiculo
    public boolean modificarVehiculo(Vehiculo nuevoVehiculo){
        Vehiculo vehiculo = buscarVehiculo(nuevoVehiculo.getPlaca());
        
        if (vehiculo != null){
            vehiculo.setCapacidad(nuevoVehiculo.getCapacidad());
            vehiculo.setConductor(nuevoVehiculo.getConductor());
            
          return true;
        }
        
        return false;
    }
}
