/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package SO.Planificacion;
import EDD.Cola;
import EDD.Criterio;
import SO.PCB;
/**
 *
 * @author Miguel
 */
public class PrioridadApropiativa implements PoliticaPlanificacion{
    // Mayor prioridad es mejor
    private final Criterio porPrioridad = (a, b) ->
            ((PCB) a).getPrioridad() > ((PCB) b).getPrioridad();

    public PCB seleccionar(Cola colaListos) {
        return (PCB) colaListos.verMejor(porPrioridad);
    }

    public boolean esApropiativa() {
        return true;
    }

    public String getNombre() {
        return "Prioridades";
    }

    public boolean esMejor(PCB candidato, PCB enCpu) {
        return candidato.getPrioridad() > enCpu.getPrioridad();
    }

    public int getQuantum() {
        return -1;
    }
}
