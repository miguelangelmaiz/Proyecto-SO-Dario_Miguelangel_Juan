
package sistemaOperativo;
import hardware.Computador;
import hardware.RelojGlobal;
import planificacionSO.FCFS;
import planificacionSO.PoliticaPlanificacion;

/**
 *
 * @author Miguel
 */
public class Cluster {
    private final Computador[] computadores;
    private int cantidad;
    private final RelojGlobal reloj;
    private final GestorBuffers gestorBuffers;

    public Cluster(int numComputadores, int ramPorComputador, int duracionCicloMs) {
        this.computadores = new Computador[numComputadores];
        this.cantidad = 0;
        this.gestorBuffers = new GestorBuffers();
        this.reloj = new RelojGlobal(duracionCicloMs, numComputadores);

        for (int i = 0; i < numComputadores; i++) {
            Computador c = new Computador(i + 1, ramPorComputador,
                                          new FCFS(), gestorBuffers);
            computadores[cantidad++] = c;
            reloj.suscribir(c);
        }
    }

    public int getCantidad() { return cantidad; }
    public Computador getComputador(int i) { return computadores[i]; }
    public RelojGlobal getReloj() { return reloj; }
    public GestorBuffers getGestorBuffers() { return gestorBuffers; }

    public void agregarComputador(Computador c) {
        computadores[cantidad++] = c;
        reloj.suscribir(c);
    }

    public void iniciar() {
        reloj.iniciar();
        Thread t = new Thread(reloj, "reloj-avilaos");
        t.setDaemon(true);
        t.start();
    }

    public void detener() {
        reloj.detener();
    }
}
