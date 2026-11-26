
package hardware;

/**
 *
 * @author jmmor
 */
public class MemoriaPrincipal {
    private int total, usada;
    
    public MemoriaPrincipal(int totalKB){
        if(totalKB<=0){
        throw new IllegalArgumentException("La memoria total debe ser positiva");
        }
        this.total = totalKB;
        this.usada = 0;
       
    }
    //Metodos de memoria
    public int getTotal(){
    return total;
    }
    
    public int getUsada(){
    return usada;
    }
    public int getLibre(){
    return total - usada;
    }
    
    public boolean hayEspacio(int kb){
    return kb > 0 && kb <= getLibre();
    }
    //Asigna proceso a la memoria principal
    public boolean asignar(int kb){
        if(!hayEspacio(kb)){
        return false;
        }
        usada += kb;
        return true;  
    }
    //Libera procesos de la memoria principal
    public boolean liberar(int kb) {
        if (kb <= 0 || kb > usada) return false;
        usada -= kb;
        return true;
    }
    /** [AJUSTE 25-nov] Aplica "ramCapacity" del JSON. Solo si cabe lo que ya está usado. */
    public boolean setTotal(int totalKB) {
        if (totalKB <= 0 || totalKB < usada) return false;
        this.total = totalKB;
        return true;
    }

    public void reset(){ 
    this.usada = 0; 
    
    }
}
