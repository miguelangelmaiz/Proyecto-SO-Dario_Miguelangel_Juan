
package sistemaOperativo;
import hardware.Computador;
import hardware.RelojGlobal;
import planificacionSO.FCFS;
import planificacionSO.PoliticaPlanificacion;
import estructuraDeDatos.ListaSimple;
import hardware.Disco;
/**
 *
 * @author Miguel
 */
public class Cluster {
    private final RelojGlobal reloj;
    
    private final ListaSimple computadores;
 
    
    // EL ARREGLO DEL LOG:
    private final Log logSistema = new Log(); 
    private final Disco disco; // <--- NUEVO
    private Thread hiloReloj;

    public Cluster(int numComputadores, int ramPorComputador, int duracionCicloMs) {
        this(ramIgualParaTodos(numComputadores, ramPorComputador), duracionCicloMs);
    }

    /** Constructor con RAM distinta por computador. */
    public Cluster(int[] ramsPorComputador, int duracionCicloMs) {
           this.computadores = new ListaSimple();
        this.reloj = new RelojGlobal(duracionCicloMs);
        this.disco = new Disco();
        this.disco.setCluster(this); // Para que el disco pueda avisarle al núcleo
        this.reloj.suscribir(this.disco); // El disco ahora late con el reloj

        for (int i = 0; i < ramsPorComputador.length; i++) {
            // Ya sin gestorBuffers
            Computador c = new Computador(i + 1, ramsPorComputador[i], new FCFS());
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
    public void registrarEvento(String evento) {
        logSistema.registrar(evento); // Usa la nueva clase
    }

    // ---------- Getters ----------
    public Disco getDisco() { return disco; }

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
    public String getLogEventos() {
        return logSistema.getTexto();
    }
    public int getVersionLog() {
        return logSistema.getVersion();
    }

    public RelojGlobal getReloj() { return reloj; }
 

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
    
     
}