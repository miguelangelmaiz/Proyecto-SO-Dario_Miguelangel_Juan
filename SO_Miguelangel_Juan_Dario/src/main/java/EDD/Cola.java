/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package EDD;

/**
 *
 * @author Miguel
 */
public class Cola {
    
    
    private Nodo cabeza;
    private Nodo cola;   // el "tail"
    private int tamano;

    public Cola() {
        this.cabeza = null;
        this.cola = null;
        this.tamano = 0;
    }

    // ---------- Getters y Setters ----------
    public Nodo getCabeza() {
        return cabeza;
    }

    public void setCabeza(Nodo cabeza) {
        this.cabeza = cabeza;
    }

    public Nodo getCola() {
        return cola;
    }

    public void setCola(Nodo cola) {
        this.cola = cola;
    }

    public int getTamano() {
        return tamano;
    }

    public void setTamano(int tamano) {
        this.tamano = tamano;
    }

    // ---------- Operaciones básicas ----------

    /** Devuelve el primer elemento sin extraerlo. */
    public Object verFrente() {
        if (estaVacia()) {
            return null;
        }
        return cabeza.getDato();
    }

    /** Inserta al final de la cola. */
    public void encolar(Object elemento) {
        Nodo nuevo = new Nodo(elemento);
        if (estaVacia()) {
            setCabeza(nuevo);
            setCola(nuevo);
        } else {
            getCola().setpNext(nuevo);
            setCola(nuevo);
        }
        tamano++;
        // System.out.println("Elemento agregado a la cola: " + elemento);
    }

    /** Extrae y devuelve el primer elemento. */
    public Object desencolar() {
        if (estaVacia()) {
            // System.out.println("[ERROR] Se intentó extraer de una cola vacía.");
            return null;
        }
        Object elemento = cabeza.getDato();
        cabeza = cabeza.getpNext();
        if (cabeza == null) {
            cola = null; // si la cola quedó vacía, tail también
        }
        tamano--;
        // System.out.println("Elemento eliminado de la cola: " + elemento);
        return elemento;
    }

    public boolean estaVacia() {
        return cabeza == null;
    }

    /** Imprime la cola en consola (útil para depurar). */
    public void imprimir() {
        Nodo puntero = cabeza;
        while (puntero != null) {
            System.out.print("[ " + puntero.getDato() + " ] -> ");
            puntero = puntero.getpNext();
        }
        System.out.println("NULL");
    }

    /**
     * Elimina de la cola el elemento que sea "igual" al objetivo.
     * Usa equals() del objeto. Sirve cuando un proceso sale de la cola
     * de listos para pasar a ejecución, o cuando se cancela.
     */
    public void eliminar(Object objetivo) {
        if (estaVacia()) {
            // System.out.println("[ERROR] Intento de eliminar de una cola vacía.");
            return;
        }

        Nodo actual = cabeza;
        Nodo anterior = null;

        while (actual != null) {
            if (actual.getDato().equals(objetivo)) {
                if (anterior == null) {
                    // Es el primer nodo
                    cabeza = actual.getpNext();
                    if (cabeza == null) {
                        cola = null;
                    }
                } else {
                    anterior.setpNext(actual.getpNext());
                    if (actual.getpNext() == null) {
                        cola = anterior;
                    }
                }
                tamano--;
                // System.out.println("[INFO] Elemento eliminado de la cola: " + objetivo);
                return;
            }
            anterior = actual;
            actual = actual.getpNext();
        }

        // System.out.println("[ERROR] Elemento no encontrado en la cola.");
    }

    /** Devuelve true si la cola contiene ese elemento. */
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
