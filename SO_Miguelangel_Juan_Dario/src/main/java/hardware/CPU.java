/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hardware;

/**
 *
 * @author jmmor
 */
import Enums.ModoEjecucion;
import Enums.EstadoProceso;
import SO.PCB;
public class CPU implements ComponenteReloj{


 private PCB procesoActual;
    private ModoEjecucion modo;
    private int ciclosOcupados;

    public CPU() {
        this.procesoActual = null;
        this.ciclosOcupados = 0;
        this.modo = ModoEjecucion.USUARIO;
    }

    public PCB getProcesoActual() { return procesoActual; }
    public void setProcesoActual(PCB procesoActual) { this.procesoActual = procesoActual; }
    public ModoEjecucion getModo() { return modo; }
    public void setModo(ModoEjecucion modo) { this.modo = modo; }
    public int getCiclosOcupados() { return ciclosOcupados; }
    public void setCiclosOcupados(int ciclosOcupados) { this.ciclosOcupados = ciclosOcupados; }

    public boolean estaLibre() { return procesoActual == null; }

    public void entrarModoSistema() { this.modo = ModoEjecucion.SISTEMA; }
    public void salirModoSistema() { this.modo = ModoEjecucion.USUARIO; }

    public void cargar(PCB p) {
        this.procesoActual = p;
        this.modo = ModoEjecucion.USUARIO;
        if (p != null) p.setEstado(EstadoProceso.EJECUCION);
    }

    public PCB liberar() {
        PCB saliente = this.procesoActual;
        this.procesoActual = null;
        this.modo = ModoEjecucion.USUARIO;
        return saliente;
    }

    public void reset() {
        this.procesoActual = null;
        this.ciclosOcupados = 0;
        this.modo = ModoEjecucion.USUARIO;
    }

    /**
     * La CPU ya no ejecuta instrucciones: eso lo hace el Núcleo, porque
     * necesita interceptar semWait / semSignal / accesos a buffer.
     * Aquí sólo contamos ciclos ocupados.
     */
    @Override
    public void tic(int ciclo) {
        if (procesoActual == null) return;
        ciclosOcupados++;
    }

}
