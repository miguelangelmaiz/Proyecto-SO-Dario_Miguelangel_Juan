
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
    private final GestorBuffers gestorBuffers;
    private final ListaSimple computadores ;
    
    public Cluster (int numComputadores, int ramPorComputador, int duracionCicloMs){
            this(ramIgualParaTodos(numComputadores, ramPorComputador), duracionCicloMs);

}
/** Constructor con RAM distinta por computador. */
    public Cluster(int[] ramsPorComputador, int duracionCicloMs) {
        this.computadores = new ListaSimple();
        this.gestorBuffers = new GestorBuffers();
        this.reloj = new RelojGlobal(duracionCicloMs, ramsPorComputador.length);

        for (int i = 0; i < ramsPorComputador.length; i++) {
            Computador c = new Computador(i + 1, ramsPorComputador[i],
                                          new FCFS(), gestorBuffers);
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

    public RelojGlobal getReloj() { return reloj; }
    public GestorBuffers getGestorBuffers() { return gestorBuffers; }

   
    // ---------- Control ----------
    
     public void agregarComputador(Computador c) {
        computadores.insertFinale(c);   // ← ya no desborda
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