/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
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
    return usada - total;
    }
    
    public boolean hayEspacio(int kb){
    return kb > 0 && kb <= getLibre();
    }
    
    public boolean asignar(int kb){
        if(!hayEspacio(kb)){
        return false;
        }
        usada += kb;
        return true;  
    }
    public boolean liberar(int kb) {
        if (kb <= 0 || kb > usada) return false;
        usada -= kb;
        return true;
    }
    public void reset(){ 
    this.usada = 0; 
    
    }
}
