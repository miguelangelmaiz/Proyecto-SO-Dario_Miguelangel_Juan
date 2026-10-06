/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package EDD;

/**
 *
 * @author jmmor
 */
public class ColaPrioridad {
   private Nodo cabeza;
   private int size;
   private Criterio criterio;


   public ColaPrioridad(Criterio criterio){
       this.cabeza = null;
       this.size = 0;
       this.criterio = criterio;
       
    
   }
 
// GETTERS AND SETTERS  
    /**
     * @return the cabeza
     */
    public Nodo getCabeza() {
        return cabeza;
    }
    

    /**
     * @param cabeza the cabeza to set
     */
    public void setCabeza(Nodo cabeza) {
        this.cabeza = cabeza;
    }

    /**
     * @return the size
     */
    public int getSize() {
        return size;
    }

    /**
     * @param size the size to set
     */
    public void setSize(int size) {
        this.size = size;
    }
    
    /**
     * @return the criterio
     */
    public Criterio getCriterio() {
        return criterio;
    }

    /**
     * @param criterio the criterio to set
     */
    public void setCriterio(Criterio criterio) {
        this.criterio = criterio;
    }
  
    
   // Metodos de la clase ColaPrioridad
 
    
    public boolean estaVacia(){
       return cabeza == null;
   }
   
    /** Devuelve el elemento de mayor prioridad sin extraerlo. */
    public Object verMin() {
        if (estaVacia()) {
            return null;
        }
        return cabeza.getDato();
    }
    /**
     * Compara los 2 objectos de una pila
     * @param a
     * @param b
     * @return 
     */
    public void insertarObjeto(Object elemento){
        Nodo nuevo = new Nodo(elemento);
    // Caso 1: cola vacía o el nuevo es mejor que la cabeza
        if (estaVacia() || criterio.mejor(elemento, cabeza.getDato())) {
            nuevo.setpNext(cabeza);
            setCabeza(nuevo);
        } else {
            // Caso 2: buscar la posición correcta
            Nodo actual = cabeza;
            while (actual.getpNext() != null &&
                   !criterio.mejor(elemento, actual.getpNext().getDato())) {
                actual = actual.getpNext();
            }
            nuevo.setpNext(actual.getpNext());
            actual.setpNext(nuevo);
        }
        size++;
    }
      
     /**
     * Extrae y devuelve el elemento de mayor prioridad (el menor).
     */
    public Object extraerMin() {
        if (estaVacia()) {
            return null;
        }
        Object elemento = cabeza.getDato();
        cabeza = cabeza.getpNext();
        size--;
        return elemento;
    }
    // Metodo para insetar un elemento a la cola
     public void encolar(Object elemento){
         insertarObjeto(elemento);
     
     }
     // Metodo para quitar un elemento de la cola
     public Object desencolar(){
     return extraerMin();
     }
    
    
    /*
    Devuelve true si la cola contiene ese elemento.
    */
    public boolean contiene(Object objetivo) {
        Nodo actual = cabeza;
        while (actual != null) {
            if (actual.getDato().equals(objetivo)) {
                return true;
            }
            actual = actual.getpNext();
        }
        return false;
    }

   
}
