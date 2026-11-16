
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

    // ---------- Getters / Setters ----------
    public PCB getProcesoActual() { return procesoActual; }
    public void setProcesoActual(PCB procesoActual) { this.procesoActual = procesoActual; }

    public ModoEjecucion getModo() { return modo; }
    public void setModo(ModoEjecucion modo) { this.modo = modo; }

    public int getCiclosOcupados() { return ciclosOcupados; }
    public void setCiclosOcupados(int ciclosOcupados) { this.ciclosOcupados = ciclosOcupados; }

    public int getInstruccionesSistema() { return instruccionesSistema; }
    public int getInstruccionesUsuario() { return instruccionesUsuario; }

    public boolean estaLibre() { return procesoActual == null; }

    // ---------- Modo sistema / usuario ----------
    /** SOLO cambia el modo. NO cuenta instrucciones (las cuenta tic()). */
    public void entrarModoSistema() { this.modo = ModoEjecucion.SISTEMA; }
    public void salirModoSistema()  { this.modo = ModoEjecucion.USUARIO; }

    /**
     * Corrige el conteo del ciclo actual: la instrucción ya fue contada
     * como USUARIO por tic(), pero en realidad fue una syscall.
     */
    public void registrarOperacionSistema() {
        if (instruccionesUsuario > 0) instruccionesUsuario--;
        instruccionesSistema++;
    }

    // ---------- Ciclo de vida del proceso ----------
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

    /** Un ciclo de CPU. Lo llama Computador.tic(). */
    public void tic(int ciclo) {
        if (procesoActual == null) return;
        ciclosOcupados++;
        if (modo == ModoEjecucion.SISTEMA) instruccionesSistema++;
        else                                instruccionesUsuario++;
    }
}
