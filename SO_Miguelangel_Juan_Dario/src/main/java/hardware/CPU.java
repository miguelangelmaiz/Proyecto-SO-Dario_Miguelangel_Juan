
package hardware;

/**
 *
 * @author jmmor
 */
import losEnums.ModoEjecucion;
import losEnums.EstadoProceso;
import sistemaOperativo.PCB;

public class CPU {


 private PCB procesoActual;
    private ModoEjecucion modo;
    private int ciclosOcupados;
    private int instruccionesSistema = 0;
    private int instruccionesUsuario = 0;

    public CPU() {
        this.procesoActual = null;
        this.ciclosOcupados = 0;
        this.modo = ModoEjecucion.USUARIO;
    }

    public PCB getProcesoActual() {
     return procesoActual; }
    
    public void setProcesoActual(PCB procesoActual) { 
        this.procesoActual = procesoActual; }
    
    public ModoEjecucion getModo() { 
        return modo; }
    
    public void setModo(ModoEjecucion modo) { 
        this.modo = modo; }
    
    public int getCiclosOcupados() { 
        return ciclosOcupados; }
    public int getInstruccionesSistema() { return instruccionesSistema; }
    public void setCiclosOcupados(int ciclosOcupados) { 
        this.ciclosOcupados = ciclosOcupados; }

    public boolean estaLibre() { return procesoActual == null; }

    public void entrarModoSistema() {
    this.modo = ModoEjecucion.SISTEMA;
    this.instruccionesSistema++;
}
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
     Un ciclo de CPU. Lo llama el Núcleo en el paso 5 de su tic().
     * Solo cuenta ciclos ocupados; la ejecución de instrucciones
     * la hace el Núcleo para poder interceptar semWait/semSignal/buffer.
     */
    public void tic(int ciclo) {
    if (procesoActual == null) return;
    ciclosOcupados++;
    if (modo == ModoEjecucion.SISTEMA) {
        instruccionesSistema++;
    } else {
        instruccionesUsuario++;
    }
}

}
