package estructuraDeDatos;

/**
 *
 * @author jmmor
 */
public interface Criterio {
       
    /** Devuelve true si "a" es mejor candidato que "b". */
    boolean mejor(Object a, Object b);

    
}
