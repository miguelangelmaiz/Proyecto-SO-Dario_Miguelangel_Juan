/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package SO.Planificacion;
import EDD.Cola;
import SO.PCB;
/**
 *
 * @author Miguel
 */
public class RoundRobin implements PoliticaPlanificacion {
     private int quantum;

    public RoundRobin(int quantum) {
        this.quantum = quantum;
    }

    public PCB seleccionar(Cola colaListos) {
        return (PCB) colaListos.verFrente();   // el primero de la cola
    }

    public boolean esApropiativa() {
        return true;   // desaloja por reloj (quantum)
    }

    public String getNombre() {
        return "Round Robin (q=" + quantum + ")";
    }

    public boolean esMejor(PCB candidato, PCB enCpu) {
        return false;  // RR no compara por prioridad/deadline
    }

    public int getQuantum() {
        return quantum;
    }

    public int getQuantumActual() {
        return quantum;
    }
}
