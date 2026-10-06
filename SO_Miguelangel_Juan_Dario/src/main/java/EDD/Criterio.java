/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package EDD;

/**
 *
 * @author jmmor
 */
public interface Criterio {
       
    /** Devuelve true si "a" es mejor candidato que "b". */
    boolean mejor(Object a, Object b);

    
}
