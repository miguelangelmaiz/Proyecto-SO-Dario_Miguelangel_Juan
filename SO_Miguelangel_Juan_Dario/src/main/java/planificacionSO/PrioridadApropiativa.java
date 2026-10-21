
package planificacionSO;
import estructuraDeDatos.Cola;
import estructuraDeDatos.Criterio;
import sistemaOperativo.PCB;
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
