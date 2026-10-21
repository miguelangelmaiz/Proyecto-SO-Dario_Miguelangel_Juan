
package planificacionSO;
import estructuraDeDatos.Cola;
import sistemaOperativo.PCB;



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
    public boolean esMejor(PCB candidato, PCB enCpu) {
    return false;   // FCFS nunca expropia
}

public int getQuantum() {
    return -1;      // no aplica
}
}
