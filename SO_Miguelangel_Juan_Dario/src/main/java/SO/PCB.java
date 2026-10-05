/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package SO;
import Enums.EstadoProceso;
import Enums.ModoEjecucion;
import Enums.MotivoBloqueo;
import Enums.TipoProceso;
/**
 *
 * @author Miguel
 */
public class PCB  {

    private static int contadorId = 1;

    // Convención de prioridad: a mayor número, mayor prioridad.
    //   1 → baja
    //   2 → media
    //   3 → alta
    // El envejecimiento (aging) sube este valor con el tiempo.
    public static final int PRIORIDAD_BAJA  = 1;
    public static final int PRIORIDAD_MEDIA = 2;
    public static final int PRIORIDAD_ALTA  = 3;

    // ---------- Identificación ----------
    private final int id;
    private String nombre;
    private int computador;

    // ---------- Estado y tipo ----------
    private EstadoProceso estado;
    private TipoProceso tipo;
    private int prioridad;

    // ---------- Recursos ----------
    private int memoria;
    private int deadline;
    private int restante;

    // ---------- Contexto de ejecución ----------
    private int pc;   // program counter
    private int mar;  // memory address register

    // ---------- Métricas ----------
    private int cicloLlegada;
    private int cicloInicio;
    private int cicloFin;
    private int ciclosBloqueado;

    // ---------- Productor / consumidor ----------
    private int bufferAsociado;
    private int ciclosPorElemento;
    private int elementosRestantes;

    // ---------- CPU / IO bound ----------
    private int ciclosParaSatisfacer;

    // ---------- Bloqueo ----------
    private MotivoBloqueo motivoBloqueo;

    public PCB(String nombre, TipoProceso tipo, int prioridad,
               int memoria, int deadline, int restante, int computador) {
        this.id = contadorId++;
        this.nombre = nombre;
        this.tipo = tipo;
        this.prioridad = prioridad;
        this.memoria = memoria;
        this.deadline = deadline;
        this.restante = restante;
        this.computador = computador;
        this.estado = EstadoProceso.NUEVO;
        this.pc = 0;
        this.mar = 0;
        this.cicloLlegada = 0;
        this.cicloInicio = -1;
        this.cicloFin = -1;
        this.ciclosBloqueado = 0;
        this.motivoBloqueo = null;
    }

    // ---------- Getters y Setters ----------
    public int getId() { return id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public int getComputador() { return computador; }

    public EstadoProceso getEstado() { return estado; }
    public void setEstado(EstadoProceso estado) { this.estado = estado; }

    public TipoProceso getTipo() { return tipo; }

    public int getPrioridad() { return prioridad; }
    public void setPrioridad(int prioridad) { this.prioridad = prioridad; }

    public int getMemoria() { return memoria; }

    public int getDeadline() { return deadline; }
    public void setDeadline(int deadline) { this.deadline = deadline; }

    public int getRestante() { return restante; }
    public void setRestante(int restante) { this.restante = restante; }

    public int getPc() { return pc; }
    public void setPc(int pc) { this.pc = pc; }

    public int getMar() { return mar; }
    public void setMar(int mar) { this.mar = mar; }

    public int getCicloLlegada() { return cicloLlegada; }
    public void setCicloLlegada(int cicloLlegada) { this.cicloLlegada = cicloLlegada; }

    public int getCicloInicio() { return cicloInicio; }
    public void setCicloInicio(int cicloInicio) { this.cicloInicio = cicloInicio; }

    public int getCicloFin() { return cicloFin; }
    public void setCicloFin(int cicloFin) { this.cicloFin = cicloFin; }

    public int getCiclosBloqueado() { return ciclosBloqueado; }
    public void setCiclosBloqueado(int ciclosBloqueado) { this.ciclosBloqueado = ciclosBloqueado; }

    public int getBufferAsociado() { return bufferAsociado; }
    public void setBufferAsociado(int bufferAsociado) { this.bufferAsociado = bufferAsociado; }

    public int getCiclosPorElemento() { return ciclosPorElemento; }
    public void setCiclosPorElemento(int ciclosPorElemento) { this.ciclosPorElemento = ciclosPorElemento; }

    public int getElementosRestantes() { return elementosRestantes; }
    public void setElementosRestantes(int elementosRestantes) { this.elementosRestantes = elementosRestantes; }

    public int getCiclosParaSatisfacer() { return ciclosParaSatisfacer; }
    public void setCiclosParaSatisfacer(int ciclosParaSatisfacer) { this.ciclosParaSatisfacer = ciclosParaSatisfacer; }

    public MotivoBloqueo getMotivoBloqueo() { return motivoBloqueo; }
    public void setMotivoBloqueo(MotivoBloqueo motivoBloqueo) { this.motivoBloqueo = motivoBloqueo; }

    // ----------  ----------

    /** Avanza una instrucción: PC++, MAR++, restante--. */
    public void ejecutarInstruccion() {
        if (restante > 0) {
            pc++;
            mar++;
            restante--;
        }
        if (restante <= 0) {
            this.estado = EstadoProceso.TERMINADO;
        }
    }

    /** ¿Ya no le quedan instrucciones? */
    public boolean estaCompletado() {
        return restante <= 0;
    }

    /** ¿Está en CPU? */
    public boolean estaEjecutando() {
        return estado == EstadoProceso.EJECUCION;
    }

    /** ¿Está bloqueado? */
    public boolean estaBloqueado() {
        return estado == EstadoProceso.BLOQUEADO;
    }

    /** ¿Ya terminó (por fin normal o por deadline)? */
    public boolean estaTerminado() {
        return estado == EstadoProceso.TERMINADO;
    }

    /** Tipo legible para la GUI y el log. */
    public String getTipoString() {
        if (tipo == TipoProceso.CPU_BOUND) return "CPU-Bound";
        if (tipo == TipoProceso.IO_BOUND)  return "I/O-Bound";
        if (tipo == TipoProceso.PRODUCTOR) return "Productor";
        return "Consumidor";
    }

    /** Prioridad legible para la GUI. */
    public String getPrioridadString() {
        if (prioridad >= PRIORIDAD_ALTA)  return "Alta";
        if (prioridad >= PRIORIDAD_MEDIA) return "Media";
        return "Baja";
    }

    // ---------- Utilidades ----------

    /** Para el log y la GUI. */
    public String toString() {
        return "P" + id + "-" + nombre + "[" + estado + "]";
    }

    /** Igualdad por id: un PCB es único en todo el sistema distribuido. */
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PCB)) return false;
        PCB otro = (PCB) o;
        return this.id == otro.id;
    }

    public int hashCode() {
        return id;
    }
}
