/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package SO.Planificacion;
import EDD.Cola;
import SO.PCB;

/**
 *
 * @author Miguel
 */
public interface PoliticaPlanificacion {

/**
 * Contrato de una política de planificación.
 * Agregar una política nueva = crear otra clase que implemente esta interfaz.
 * NO se toca el Núcleo.
 *
 * @author Miguel
 */

    /**
     * Devuelve el siguiente PCB a ejecutar de la cola de listos.
     * Devuelve null si la cola está vacía.
     */
    PCB seleccionar(Cola colaListos);

    /** ¿Desaloja al que está en CPU si llega uno mejor? */
    boolean esApropiativa();

    /** Nombre para el log y la GUI. */
    String getNombre();

}
