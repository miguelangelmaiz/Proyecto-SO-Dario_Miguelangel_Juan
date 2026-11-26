package planificacionSO;

/**
 *
 * @author jmmor
 */
public class FabricaPoliticas {
      public static PoliticaPlanificacion crear(String seleccion, int quantum) {
        switch (normalizar(seleccion)) {
            case "FCFS": return new FCFS();
            case "Round Robin": return new RoundRobin(quantum > 0 ? quantum : 3);
            case "EDF": return new EDF();
            case "Prioridades": return new PrioridadApropiativa();
            default: return new FCFS();
        }
    }

    /**
     * [AJUSTE 25-nov] Lleva el texto del JSON ("RR", "ROUND_ROBIN", "PRIORITY"...) al nombre
     * que usa el combo de la interfaz. Así el JSON y la GUI hablan el mismo idioma.
     */
    public static String normalizar(String texto) {
        if (texto == null) return "FCFS";
        String t = texto.trim().toUpperCase().replace("_", "").replace("-", "").replace(" ", "");
        if (t.startsWith("RR") || t.startsWith("ROUNDROBIN")) return "Round Robin";
        if (t.equals("EDF")) return "EDF";
        if (t.startsWith("PRIOR")) return "Prioridades";
        return "FCFS";
    }
}
