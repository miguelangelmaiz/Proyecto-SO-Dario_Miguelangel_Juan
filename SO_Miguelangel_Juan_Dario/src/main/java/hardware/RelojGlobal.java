
package hardware;

import estructuraDeDatos.ListaSimple;
/**
 *
 * @author jmmor
 */
public class RelojGlobal implements Runnable {
     private volatile boolean corriendo;
    private volatile int duracionCicloMs;
    private int ciclo;
    private final ListaSimple suscritos;   // ← antes era arreglo

    public RelojGlobal(int duracionCicloMs) {   // ← ya no pide maxComputadores
        this.duracionCicloMs = duracionCicloMs;
        this.ciclo = 0;
        this.corriendo = false;
        this.suscritos = new ListaSimple();
    }

    public boolean isCorriendo() { return corriendo; }
    public int getDuracionCicloMs() { return duracionCicloMs; }
    public int getCiclo() { return ciclo; }
    public int getCantidad() { return suscritos.getSize(); }

    public void setDuracionCicloMs(int ms) { this.duracionCicloMs = ms; }

    public void suscribir(ComponenteReloj c) {
        suscritos.insertFinale(c);   // ← ya no hay if
    }

    public void iniciar() { this.corriendo = true; }
    public void detener() { this.corriendo = false; }

    public void run() {
        while (corriendo) {
            ciclo++;
            for (int i = 0; i < suscritos.getSize(); i++) {
                ComponenteReloj c = (ComponenteReloj) suscritos.getValue(i);
                c.tic(ciclo);
            }
            // [AJUSTE 25-nov] Se duerme en tramos cortos revisando "corriendo": así Pausar
            // detiene el reloj en menos de 20 ms SIN interrumpir el hilo a mitad de un ciclo
            // (interrumpirlo hacía fallar los semáforos de las colas y se perdían procesos).
            long hasta = System.currentTimeMillis() + duracionCicloMs;
            while (corriendo && System.currentTimeMillis() < hasta) {
                try {
                    Thread.sleep(Math.max(1, Math.min(20, hasta - System.currentTimeMillis())));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }}}

