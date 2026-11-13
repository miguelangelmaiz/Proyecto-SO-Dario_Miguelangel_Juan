package planificacionSO;

/**
 *
 * @author jmmor
 */
public class FabricaPoliticas {
      public static PoliticaPlanificacion crear(String seleccion, int quantum) {
        switch (seleccion) {
            case "FCFS": return new FCFS();
            case "Round Robin": return new RoundRobin(quantum);
            case "EDF": return new EDF();
            case "Prioridades": return new PrioridadApropiativa();
            default: return new FCFS();
        }
    }
}
