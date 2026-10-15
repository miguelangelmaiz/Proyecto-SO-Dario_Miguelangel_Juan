package estructuraDeDatos;

/**
 *
 * @author Miguel
 */
public class Nodo {
    private Object dato;
    private Nodo pNext;

    public Nodo() {
        this.dato = null;
        this.pNext = null;
    }
    
    public Nodo(Object dato) {
        this.dato = dato;
        this.pNext = null;
    }

    public Object getDato() {
        return dato;
    }

    public void setDato(Object dato) {
        this.dato = dato;
    }

    public Nodo getpNext() {
        return pNext;
    }

    public void setpNext(Nodo pNext) {
        this.pNext = pNext;
    }
}
