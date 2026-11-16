/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemaOperativo;

/**
 *
 * @author Miguel
 */
public class Resultado {
    private final boolean ok;
    private final String motivo;
    private final PCB pcb;

    private Resultado(boolean ok, String motivo, PCB pcb) {
        this.ok = ok;
        this.motivo = motivo;
        this.pcb = pcb;
    }

    public static Resultado ok(PCB p)            { return new Resultado(true,  null, p); }
    public static Resultado error(String motivo) { return new Resultado(false, motivo, null); }

    public boolean isOk()      { return ok; }
    public String  getMotivo() { return motivo; }
    public PCB     getPcb()    { return pcb; }
}
