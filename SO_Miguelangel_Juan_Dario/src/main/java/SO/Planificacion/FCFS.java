/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package SO.Planificacion;
import EDD.Cola;
import SO.PCB;
import SO.Planificacion.PoliticaPlanificacion;


/**
 *
 * @author Miguel
 */
public class FCFS implements PoliticaPlanificacion {
    
    public PCB seleccionar(Cola colaListos) {
        return (PCB) colaListos.verFrente();   // ← el cuerpo
    }

    public boolean esApropiativa() {
        return false;                          // ← el cuerpo
    }

    public String getNombre() {
        return "FCFS";                         // ← el cuerpo
    }
    
}
