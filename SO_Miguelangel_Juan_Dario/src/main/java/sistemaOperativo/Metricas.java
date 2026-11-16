/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemaOperativo;
import estructuraDeDatos.Nodo;
/**
 *
 * @author Miguel
 */
public class Metricas {
    public static double getThroughput(Cluster cluster) {
        int terminados = 0;
        for (int i = 0; i < cluster.getCantidad(); i++) {
            terminados += cluster.getComputador(i).getNucleo().getTerminados().getTamano();
        }
        int ciclo = cluster.getReloj().getCiclo();
        return ciclo > 0 ? (double) terminados / ciclo : 0.0;
    }

    /** Respuesta: cicloInicio - cicloLlegada. Hasta que toca CPU por primera vez. */
    public static double getTiempoRespuestaPromedio(Cluster cluster) {
        double suma = 0; int total = 0;
        for (int i = 0; i < cluster.getCantidad(); i++) {
            Nodo aux = cluster.getComputador(i).getNucleo().getTerminados().getCabeza();
            while (aux != null) {
                PCB p = (PCB) aux.getDato();
                if (p.getCicloInicio() != -1 && p.getCicloLlegada() >= 0) {
                    suma += (p.getCicloInicio() - p.getCicloLlegada());
                    total++;
                }
                aux = aux.getpNext();
            }
        }
        return total > 0 ? suma / total : 0.0;
    }

    /** Retorno (turnaround): cicloFin - cicloLlegada. */
    public static double getTiempoRetornoPromedio(Cluster cluster) {
        double suma = 0; int total = 0;
        for (int i = 0; i < cluster.getCantidad(); i++) {
            Nodo aux = cluster.getComputador(i).getNucleo().getTerminados().getCabeza();
            while (aux != null) {
                PCB p = (PCB) aux.getDato();
                if (p.getCicloFin() != -1 && p.getCicloLlegada() >= 0) {
                    suma += (p.getCicloFin() - p.getCicloLlegada());
                    total++;
                }
                aux = aux.getpNext();
            }
        }
        return total > 0 ? suma / total : 0.0;
    }

    /** Utilización de CPU: ciclos ocupados / ciclo global. */
    public static double getUtilizacionCpu(Cluster cluster) {
        int ocupados = 0;
        for (int i = 0; i < cluster.getCantidad(); i++) {
            ocupados += cluster.getComputador(i).getCpu().getCiclosOcupados();
        }
        int ciclo = cluster.getReloj().getCiclo();
        return ciclo > 0 ? (double) ocupados / ciclo : 0.0;
    }
}
