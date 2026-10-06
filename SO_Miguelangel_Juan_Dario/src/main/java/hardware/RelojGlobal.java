/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
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

    @Override
    public void run() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
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
    public void setDuracionCicloMs(int duracionCicloMs) {
        this.duracionCicloMs = duracionCicloMs;
    }

    /**
     * @param ciclo the ciclo to set
     */
    public void setCiclo(int ciclo) {
        this.ciclo = ciclo;
    }

    /**
     * @param suscritos the suscritos to set
     */
    public void setSuscritos(ComponenteReloj[] suscritos) {
        this.suscritos = suscritos;
    }

    /**
     * @param cantidad the cantidad to set
     */
    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
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
     
     
     
     
     
     
     
     
}
