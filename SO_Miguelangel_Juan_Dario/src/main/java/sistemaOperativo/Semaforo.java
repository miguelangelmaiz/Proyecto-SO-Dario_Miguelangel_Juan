
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

    /**
     * Si el valor queda negativo, el proceso se bloquea en la cola de ESTE
     * semáforo y libera la CPU.
     */
    public void semWait(PCB p, Nucleo k, MotivoBloqueo motivo) {
        valor--;
        if (valor < 0) {
            p.setEstado(EstadoProceso.BLOQUEADO);
            p.setMotivoBloqueo(motivo);
            bloqueados.encolar(p);
            k.getCpu().liberar();   // el proceso sale de la CPU
        }
    }

    /**
     * Si había alguien esperando (valor <= 0), despierta a UNO y lo devuelve
     * a la cola de listos de su núcleo.
     */
    public void semSignal(Nucleo k) {
        valor++;
        if (valor <= 0) {
            PCB p = (PCB) bloqueados.desencolar();
            if (p != null) {
                p.setEstado(EstadoProceso.LISTO);
                p.setMotivoBloqueo(null);
                k.getColaListos().encolar(p);
            }
        }
    }
}
