/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package EDD;

/**
 *
 * @author Miguel
 */
public class Pila {
    private Nodo pInicio;
    private int size;


    public Pila() {
        this.pInicio = null;
        this.size = 0;
    }
    /**
     * Constructor para inicializar una pila con un nodo de inicio y un tamaño
     * definido.
     *
     * @param pInicio el nodo inicial de la pila.
     * @param size el tamaño inicial de la pila.
     */
    public Pila(Nodo pInicio, int size) {
        this.pInicio = pInicio;
        this.size = size;
    }
    
    /**
     * Obtiene el nodo inicial de la pila.
     *
     * @return el nodo inicial (`pInicio`) de la pila.
     */
    public Nodo getpInicio() {
        return pInicio;
    }

    /**
     * Establece el nodo inicial de la pila.
     *
     * @param pInicio el nodo que se asignará como inicio de la pila.
     */
    public void setpInicio(Nodo pInicio) {
        this.pInicio = pInicio;
    }

    /**
     * Obtiene el tamaño actual de la pila.
     *
     * @return el tamaño de la pila.
     */
    public int getSize() {
        return size;
    }

    /**
     * Establece el tamaño de la pila.
     *
     * @param size el nuevo tamaño de la pila.
     */
    public void setSize(int size) {
        this.size = size;
    }
    
    /**
     * Verifica si la pila está vacía.
     *
     * @return true si la pila no contiene ningún elemento (es decir, si su nodo
     * inicial es nulo), false en caso contrario.
     */
    public boolean esVacio(){
        return pInicio == null;
    }
    
    /**
     * Obtiene el dato almacenado en el nodo inicial de la pila (la cabeza).
     *
     * @return el dato contenido en el nodo inicial (`pInicio`) de la pila.
     */ 
    public Object leerCabeza(){
        return pInicio.getDato();
    }
}
