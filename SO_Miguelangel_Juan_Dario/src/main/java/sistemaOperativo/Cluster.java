
package sistemaOperativo;
import hardware.Computador;
import hardware.RelojGlobal;
import planificacionSO.FCFS;
import planificacionSO.PoliticaPlanificacion;
import estructuraDeDatos.ListaSimple;
/**
 *
 * @author Miguel
 */
public class Cluster {
    private final RelojGlobal reloj;
    private final GestorBuffers gestorBuffers; // Lo dejamos por ahora para no romper Computador
    private final ListaSimple computadores;
    
    // EL ARREGLO DEL LOG:
    private final Log logSistema = new Log(); 
    
    private Thread hiloReloj;

    public Cluster(int numComputadores, int ramPorComputador, int duracionCicloMs) {
        this(ramIgualParaTodos(numComputadores, ramPorComputador), duracionCicloMs);
    }

    /** Constructor con RAM distinta por computador. */
    public Cluster(int[] ramsPorComputador, int duracionCicloMs) {
        this.computadores = new ListaSimple();
        this.gestorBuffers = new GestorBuffers();
        this.reloj = new RelojGlobal(duracionCicloMs);

        for (int i = 0; i < ramsPorComputador.length; i++) {
            Computador c = new Computador(i + 1, ramsPorComputador[i],
                                          new FCFS(), gestorBuffers);
            c.getNucleo().setCluster(this);
            computadores.insertFinale(c);
            reloj.suscribir(c);
        }
    }

    private static int[] ramIgualParaTodos(int n, int ram) {
        int[] r = new int[n];
        for (int i = 0; i < n; i++) r[i] = ram;
        return r;
    }

    // ---------- Getters ----------

    public int getCantidad() {
        return computadores.getSize();
    }

    public Computador getComputador(int i) {
        return (Computador) computadores.getValue(i);
    }

    public Nucleo getNucleoDe(int idComputador) {
        for (int i = 0; i < computadores.getSize(); i++) {
            Computador c = (Computador) computadores.getValue(i);
            if (c.getId() == idComputador) return c.getNucleo();
        }
        return null;
    }

    public RelojGlobal getReloj() { return reloj; }
    public GestorBuffers getGestorBuffers() { return gestorBuffers; }

    // ---------- Control ----------

    public void agregarComputador(Computador c) {
        c.getNucleo().setCluster(this);
        computadores.insertFinale(c);
        reloj.suscribir(c);
    }

    public void iniciar() {
        if (hiloReloj != null && hiloReloj.isAlive()) {
            return; 
        }
        reloj.iniciar();
        hiloReloj = new Thread(reloj, "reloj-avilaos");
        hiloReloj.setDaemon(true);
        hiloReloj.start();
    }

    public void detener() {
        reloj.detener();
        if (hiloReloj != null) {
            hiloReloj.interrupt();
        }
    }
    
    // ==========================================
    // ARREGLOS DEL LOG (Error 3 del PDF)
    // ==========================================
    public void registrarEvento(String evento) {
        logSistema.registrar(evento);
    }

    public String getLogEventos() {
        return logSistema.getTexto();
    }

    public int getVersionLog() {
        return logSistema.getVersion();
    }
}