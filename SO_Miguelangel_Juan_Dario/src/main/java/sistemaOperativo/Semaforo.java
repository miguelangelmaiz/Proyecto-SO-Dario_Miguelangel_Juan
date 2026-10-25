
package sistemaOperativo;

import estructuraDeDatos.Cola;
import losEnums.EstadoProceso;
import losEnums.MotivoBloqueo;
/**
 *
 * @author Miguel
 */
public class Semaforo {
    private int valor;
    private final Cola bloqueados;
    private final String nombre;

    public Semaforo(int valorInicial, String nombre) {
        this.valor = valorInicial;
        this.nombre = nombre;
        this.bloqueados = new Cola();
    }

    public int getValor() { return valor; }
    public String getNombre() { return nombre; }
    public Cola getBloqueados() { return bloqueados; }

    public void semWait(PCB p, Nucleo k, MotivoBloqueo motivo) {
        valor--;
        if (valor < 0) {
            p.setEstado(EstadoProceso.BLOQUEADO);
            p.setMotivoBloqueo(motivo);
            bloqueados.encolar(p);
            k.getCpu().liberar();
            if (k.getCluster() != null) {
                k.getCluster().registrarEvento("Ciclo " + k.getCluster().getReloj().getCiclo() + ": Proceso " + p.toString() + " BLOQUEADO en semáforo " + nombre);}
        }
    }

    /**
     * C-2 fix: el proceso despertado vuelve a SU propio núcleo,
     * no al del proceso que hizo la señal.
     */
    public void semSignal(Nucleo k) {
        valor++;
        if (valor <= 0) {
            PCB p = (PCB) bloqueados.desencolar();
            if (p != null) {
                p.setEstado(EstadoProceso.LISTO);
                p.setMotivoBloqueo(null);

                Nucleo destino = null;
                if (k.getCluster() != null) {
                    destino = k.getCluster().getNucleoDe(p.getComputador());
                }
                if (destino == null) destino = k;   // fallback
                destino.getColaListos().encolar(p);
                if (k.getCluster() != null) {
                    k.getCluster().registrarEvento("Ciclo " + k.getCluster().getReloj().getCiclo() + ": Proceso " + p.toString() + " DESPERTÓ de semáforo " + nombre);
                }
                
                
            }
        }
    }
}
