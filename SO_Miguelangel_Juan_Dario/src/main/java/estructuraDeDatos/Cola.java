
package estructuraDeDatos;
import java.util.concurrent.Semaphore;
/**
 *
 * @author Miguel
 */
public class Cola {
    
    
    private Nodo cabeza;
    private Nodo cola;
    private int tamano;
    
    // EL CANDADO: Solo un hilo puede entrar a la vez
    private final Semaphore mutex = new Semaphore(1);

    public Cola() {
        this.cabeza = null;
        this.cola = null;
        this.tamano = 0;
    }

    // ---------- Getters y Setters ----------
    public Nodo getCabeza() { return cabeza; }
    public void setCabeza(Nodo cabeza) { this.cabeza = cabeza; }
    public Nodo getCola() { return cola; }
    public void setCola(Nodo cola) { this.cola = cola; }

    public int getTamano() {
        try {
            mutex.acquire();
            return tamano;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return 0;
        } finally {
            mutex.release();
        }
    }

    // ---------- Operaciones básicas protegidas ----------

    public Object verFrente() {
        try {
            mutex.acquire();
            if (cabeza == null) return null;
            return cabeza.getDato();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } finally {
            mutex.release();
        }
    }

    public void encolar(Object elemento) {
        try {
            mutex.acquire();
            Nodo nuevo = new Nodo(elemento);
            if (cabeza == null) {
                cabeza = nuevo;
                cola = nuevo;
            } else {
                cola.setpNext(nuevo);
                cola = nuevo;
            }
            tamano++;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            mutex.release();
        }
    }

    public Object desencolar() {
        try {
            mutex.acquire();
            if (cabeza == null) return null;
            
            Object elemento = cabeza.getDato();
            cabeza = cabeza.getpNext();
            if (cabeza == null) {
                cola = null;
            }
            tamano--;
            return elemento;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } finally {
            mutex.release();
        }
    }

    public boolean estaVacia() {
        try {
            mutex.acquire();
            return cabeza == null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return true;
        } finally {
            mutex.release();
        }
    }

    public void eliminar(Object objetivo) {
        try {
            mutex.acquire();
            if (cabeza == null) return;

            Nodo actual = cabeza;
            Nodo anterior = null;

            while (actual != null) {
                if (actual.getDato().equals(objetivo)) {
                    if (anterior == null) {
                        cabeza = actual.getpNext();
                        if (cabeza == null) cola = null;
                    } else {
                        anterior.setpNext(actual.getpNext());
                        if (actual.getpNext() == null) cola = anterior;
                    }
                    tamano--;
                    return;
                }
                anterior = actual;
                actual = actual.getpNext();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            mutex.release();
        }
    }

    public boolean contiene(Object objetivo) {
        try {
            mutex.acquire();
            Nodo actual = cabeza;
            while (actual != null) {
                if (actual.getDato().equals(objetivo)) return true;
                actual = actual.getpNext();
            }
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } finally {
            mutex.release();
        }
    }
    
    public Object verMejor(Criterio criterio) {
        try {
            mutex.acquire();
            if (cabeza == null) return null;
            Nodo actual = cabeza;
            Object mejor = actual.getDato();
            while (actual != null) {
                if (criterio.mejor(actual.getDato(), mejor)) {
                    mejor = actual.getDato();
                }
                actual = actual.getpNext();
            }
            return mejor;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } finally {
            mutex.release();
        }
    }

    /** 
     * MÉTODO NUEVO PARA LA GUI: Devuelve los nombres de los procesos 
     * de forma segura sin que el hilo de Swing choque con el Reloj.
     */
    public String getNombresProcesos() {
          try {
            mutex.acquire();
            if (cabeza == null) return "[]";
            
            StringBuilder sb = new StringBuilder("[");
            Nodo aux = cabeza;
            
            while (aux != null) {

                String nombreCompleto = aux.getDato().toString().split("\\[")[0]; 
                sb.append(nombreCompleto);
                
                // 3. Si está bloqueado, le pegamos el motivo
                sistemaOperativo.PCB p = (sistemaOperativo.PCB) aux.getDato();
                if (p.getEstado() == losEnums.EstadoProceso.BLOQUEADO && p.getMotivoBloqueo() != null) {
                    sb.append("(").append(p.getMotivoBloqueo().name()).append(")");
                }
                
                // 4. Agregamos una coma si hay más procesos adelante
                if (aux.getpNext() != null) {
                    sb.append(", ");
                }
                
                aux = aux.getpNext();
            }
            sb.append("]");
            return sb.toString();
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "[]";
        } finally {
            mutex.release();
        }
    }

    public void imprimir() {
        try {
            mutex.acquire();
            Nodo puntero = cabeza;
            while (puntero != null) {
                System.out.print("[ " + puntero.getDato() + " ] -> ");
                puntero = puntero.getpNext();
            }
            System.out.println("NULL");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            mutex.release();
        }
    }
    

    
}
