
package hardware;


/**
 *
 * @author jmmor
 */
public class RelojGlobal implements Runnable {
    
    private volatile boolean corriendo;
    private volatile int duracionCicloMs;
    private int ciclo;
    private ComponenteReloj[] suscritos;
    private int cantidad;

      public RelojGlobal(int duracionCicloMs, int maxComputadores) {
        this.duracionCicloMs = duracionCicloMs;
        this.ciclo = 0;
        this.corriendo = false;
        this.suscritos = new ComponenteReloj[maxComputadores];
        this.cantidad = 0;
    }


    /**
     * @return the corriendo
     */
    public boolean Corriendo() {
        return corriendo;
    }

    /**
     * @return the duracionCicloMs
     */
    public int getDuracionCicloMs() {
        return duracionCicloMs;
    }

    /**
     * @return the ciclo
     */
    public int getCiclo() {
        return ciclo;
    }

    /**
     * @return the suscritos
     */
    public ComponenteReloj[] getSuscritos() {
        return suscritos;
    }

    /**
     * @return the cantidad
     */
    public int getCantidad() {
        return cantidad;
    }

    /**
     * @param corriendo the corriendo to set
     */
    public void setCorriendo(boolean corriendo) {
        this.corriendo = corriendo;
    }

    /**
     * @param duracionCicloMs the duracionCicloMs to set
     */
    public void setDuracionCicloMs(int ms) {
        this.duracionCicloMs = ms;
    }

   

  
        
     public void suscribir(ComponenteReloj c) {
        if (cantidad < suscritos.length) {
            suscritos[cantidad++] = c;
        }
    }
     
     public void iniciar(){
         this.corriendo = true;
     }
     
     public void detener(){
         this.corriendo = false;
     }
    
     public boolean isCorriendo() {
         return corriendo;
     }

public void run() {
    while (corriendo) {
        ciclo++;
        for (int i = 0; i < cantidad; i++) {
            suscritos[i].tic(ciclo);
        }
        try {
            Thread.sleep(duracionCicloMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            break;
        }
    }
    
     
     
     
     
     
     
     
}
}
