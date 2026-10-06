/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hardware;

/**
 *
 * @author jmmor
 */
public interface ComponenteReloj {
    /**
     * Avanza un ciclo. Lo llama el RelojGlobal en cada pulso.
     * @param ciclo número de ciclo global (1, 2, 3, ...)
     */
    void tic(int ciclo);
    
}
