
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
