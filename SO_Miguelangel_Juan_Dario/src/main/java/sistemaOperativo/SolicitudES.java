package sistemaOperativo;

import losEnums.TipoOperacionES;
/**
 *
 * @author Miguel
 */
public class SolicitudES {
    private final PCB pcb;
    private final TipoOperacionES operacion;
    private final int bloqueDestino;
    private final int bloquesRequeridos;
    private final int cicloLlegada;

    public SolicitudES(PCB pcb, TipoOperacionES operacion, int bloqueDestino, int bloquesRequeridos, int cicloLlegada) {
        this.pcb = pcb;
        this.operacion = operacion;
        this.bloqueDestino = bloqueDestino;
        this.bloquesRequeridos = bloquesRequeridos;
        this.cicloLlegada = cicloLlegada;
    }

    public PCB getPcb() {
        return pcb;
    }

    public TipoOperacionES getOperacion() {
        return operacion;
    }

    public int getBloqueDestino() {
        return bloqueDestino;
    }

    public int getBloquesRequeridos() {
        return bloquesRequeridos;
    }

    public int getCicloLlegada() {
        return cicloLlegada;
    }

    /** [AJUSTE 25-nov] Nombre del proceso dueño de la solicitud (para el log y la interfaz). */
    public String getNombreProceso() {
        return pcb == null ? "?" : "P" + pcb.getId() + "-" + pcb.getNombre();
    }

    /** [AJUSTE 25-nov] Así se ve cada solicitud en la cola de E/S de la interfaz. */
    @Override
    public String toString() {
        return getNombreProceso() + " " + operacion + "→b" + bloqueDestino
                + (bloquesRequeridos > 1 ? " (" + bloquesRequeridos + " bloques)" : "");
    }
}
