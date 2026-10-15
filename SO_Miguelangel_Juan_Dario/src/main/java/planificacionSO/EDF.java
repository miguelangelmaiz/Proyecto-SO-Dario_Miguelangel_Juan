
package planificacionSO;
import estructuraDeDatos.Cola;
import estructuraDeDatos.Criterio;
import sistemaOperativo.PCB;
/**
 *
 * @author Miguel
 */
public class EDF implements PoliticaPlanificacion {
    // Menor deadline es mejor
    private final Criterio porDeadline = (a, b) ->
            ((PCB) a).getDeadline() < ((PCB) b).getDeadline();

    public PCB seleccionar(Cola colaListos) {
        return (PCB) colaListos.verMejor(porDeadline);
    }

    public boolean esApropiativa() {
        return true;
    }

    public String getNombre() {
        return "EDF";
    }

    public boolean esMejor(PCB candidato, PCB enCpu) {
        return candidato.getDeadline() < enCpu.getDeadline();
    }

    public int getQuantum() {
        return -1;
    }
}
