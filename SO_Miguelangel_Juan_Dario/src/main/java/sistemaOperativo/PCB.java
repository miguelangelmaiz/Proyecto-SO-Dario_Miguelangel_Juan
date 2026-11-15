
package sistemaOperativo;
import losEnums.EstadoProceso;
import losEnums.MotivoBloqueo;
import losEnums.TipoProceso;
import losEnums.TipoOperacionES;
/**
 *
 * @author Miguel
 */
public class PCB  {
private static int contadorId = 1;
    public static final int PRIORIDAD_BAJA  = 1;
    public static final int PRIORIDAD_MEDIA = 2;
    public static final int PRIORIDAD_ALTA  = 3;

    private final int id;
    private String nombre;
    private int computador;

    private EstadoProceso estado;
    private TipoProceso tipo;
    private int prioridad;

    private int memoria;
    private int deadline;
    private int restante;

    private int pc;
    private int mar;

    private int cicloLlegada;
    private int cicloInicio;
    private int cicloFin;
    private int ciclosBloqueado;

    private int ciclosParaSatisfacer;
    private MotivoBloqueo motivoBloqueo;
    private boolean latenciaPagada = false;

    // ==========================================
    // NUEVOS CAMPOS PARA EL PROYECTO 2
    // ==========================================
    private TipoOperacionES operacion; // CREAR, LEER, ELIMINAR, etc.
    private String rutaArchivo;        // Ej: "/sistema/config.sys"
    private int bloques;               // Cuántos bloques pide en disco
    private String dueno;              // "admin", "user1", etc.
    private boolean enSwap;            // Si está suspendido en el disco

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
        this.enSwap = false;
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

    public int getCiclosParaSatisfacer() { return ciclosParaSatisfacer; }
    public void setCiclosParaSatisfacer(int ciclosParaSatisfacer) { this.ciclosParaSatisfacer = ciclosParaSatisfacer; }
    public MotivoBloqueo getMotivoBloqueo() { return motivoBloqueo; }
    public void setMotivoBloqueo(MotivoBloqueo motivoBloqueo) { this.motivoBloqueo = motivoBloqueo; }
    public boolean isLatenciaPagada() { return latenciaPagada; }
    public void setLatenciaPagada(boolean latenciaPagada) { this.latenciaPagada = latenciaPagada; }

    // Nuevos getters y setters del P2
    public TipoOperacionES getOperacion() { return operacion; }
    public void setOperacion(TipoOperacionES operacion) { this.operacion = operacion; }

    public String getRutaArchivo() { return rutaArchivo; }
    public void setRutaArchivo(String rutaArchivo) { this.rutaArchivo = rutaArchivo; }

    public int getBloques() { return bloques; }
    public void setBloques(int bloques) { this.bloques = bloques; }

    public String getDueno() { return dueno; }
    public void setDueno(String dueno) { this.dueno = dueno; }

    public boolean isEnSwap() { return enSwap; }
    public void setEnSwap(boolean enSwap) { this.enSwap = enSwap; }

    // ---------- Métodos ----------
    public void ejecutarInstruccion() {
        if (restante > 0) {
            pc++;
            mar++;
            restante--;
        }
        if (restante <= 0) this.estado = EstadoProceso.TERMINADO;
    }

    public boolean estaCompletado() { return restante <= 0; }
    public boolean estaTerminado() { return estado == EstadoProceso.TERMINADO; }

    @Override
    public String toString() {
        return "P" + id + "-" + nombre + "[" + estado + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PCB)) return false;
        return this.id == ((PCB) o).id;
    }

    @Override
    public int hashCode() { return id; }

}
