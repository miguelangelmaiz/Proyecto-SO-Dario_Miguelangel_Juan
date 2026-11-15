/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
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

    public PCB getPcb() { return pcb; }
    public TipoOperacionES getOperacion() { return operacion; }
    public int getBloqueDestino() { return bloqueDestino; }
    public int getBloquesRequeridos() { return bloquesRequeridos; }
    public int getCicloLlegada() { return cicloLlegada; }
}
