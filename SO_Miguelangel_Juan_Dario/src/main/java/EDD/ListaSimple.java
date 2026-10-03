/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package EDD;

/**
 *
 * @author Miguel
 */
public class ListaSimple {
    
    
     private Nodo pFirst;
    private int size;

    public ListaSimple() {
        this.pFirst = null;
        this.size = 0;
    }

    public Nodo getpFirst() {
        return pFirst;
    }

    public void setpFirst(Nodo pFirst) {
        this.pFirst = pFirst;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public boolean isEmpty() {
        return this.pFirst == null;
    }

    public void insertStart(Object dato) {
        Nodo pNew = new Nodo(dato);

        if (this.isEmpty()) {
            this.setpFirst(pNew);

        } else {
            Nodo aux = this.pFirst;
            pNew.setpNext(aux);
            this.pFirst = pNew;

        }
        size++;

    }

    public void insertFinale(Object dato) {
        Nodo pNew = new Nodo(dato);

        if (this.isEmpty()) {
            this.setpFirst(pNew);

        } else {
            if (size == 1) {
                this.getpFirst().setpNext(pNew);
            } else {
                Nodo aux = this.pFirst;
                while (aux.getpNext() != null) {
                    aux = aux.getpNext();
                }
                aux.setpNext(pNew);
            }
        }

        size++;
    }

    //Con for
    public void insertForIndex(int index, Object dato) {
        if (index >= 0 && index < size) {
            if (index == 0) {
                this.insertStart(dato);
            } else {
                if (index == size - 1) {
                    this.insertFinale(dato);
                } else {
                    Nodo nuevo = new Nodo(dato);
                    Nodo aux = pFirst;
                    for (int i = 0; i < (index - 1); i++) {
                        aux = aux.getpNext();
                    }
                    Nodo siguiente = aux.getpNext();
                    aux.setpNext(nuevo);
                    nuevo.setpNext(siguiente);
                }
            }
            size++;
        }
    }

    public void insertForTwoPosition(int position, Object dato) {
        int index = position + 2;

        if (index >= 0 && index < size) {
            if (index == 0) {
                this.insertStart(dato);
            } else {
                if (index == size - 1) {
                    this.insertFinale(dato);
                } else {
                    Nodo nuevo = new Nodo(dato);
                    Nodo aux = pFirst;
                    for (int i = 0; i < (index - 1); i++) {
                        aux = aux.getpNext();
                    }
                    Nodo siguiente = aux.getpNext();
                    aux.setpNext(nuevo);
                    nuevo.setpNext(siguiente);
                }
            }
            size++;
        }
    }

    public void insertWhileIndex(int index, Object dato) {
        if (index >= 0 && index < size) {
            if (index == 0) {
                this.insertStart(dato);
            } else {
                if (index == size - 1) {
                    this.insertFinale(dato);
                } else {
                    Nodo nuevo = new Nodo(dato);
                    Nodo aux = pFirst;
                    int count = 0;

                    while (count != index - 1) {
                        aux = aux.getpNext();
                        count++;
                    }
                    Nodo siguiente = aux.getpNext();
                    aux.setpNext(nuevo);
                    nuevo.setpNext(siguiente);
                }
            }
            size++;

        }
    }

    public void insertForReference(Object ref, Object dato) {
        if (this.search(ref) || ref == null) {
            if (ref == null) {
                this.insertFinale(dato);
            } else {
                Nodo nuevo = new Nodo(dato);

                Nodo aux = this.pFirst;
                while (aux.getDato() != ref) {
                    aux = aux.getpNext();
                }
                Nodo siguiente = aux.getpNext();
                aux.setpNext(nuevo);
                nuevo.setpNext(siguiente);

            }
            size++;
        } else {
            System.out.println("No se encuentra la referencia.");
        }
    }

    public void editObject(Object ref, Object newValue) {
        if (this.search(ref)) {
            if (this.size == 1) {
                this.pFirst.setDato(newValue);
            } else {
                Nodo aux = this.pFirst;
                while (aux != null) {
                    if (aux.getDato() == ref) {
                        aux.setDato(newValue);
                    }
                    aux = aux.getpNext();
                }
            }
        } else {
            System.out.println("El objeto " + ref + " no se encuentra en la lista, por lo tanto, no se pudo actualizar.");
        }
    }

    public void editForIndex(int index, Object newValue) {
        if (index >= 0 && index < size) {
            int count = 0;
            Nodo aux = this.pFirst;

            while (count != index) {
                aux = aux.getpNext();
                count++;
            }
            aux.setDato(newValue);

        } else {
            System.out.println("No se encuentra el indice en la lista ");
        }
    }

    public Object getValue(int index) {
        if (!isEmpty()) {
            if (index >= 0 && index < size) {
                int count = 0;
                Nodo aux = this.pFirst;

                while (count != index) {
                    aux = aux.getpNext();
                    count++;
                }
                return aux.getDato();

            }
        }
        return null;

    }

    public int getPosition(Object dato) {
        if (this.search(dato)) {
            if (this.size == 1) {
                return 0;
            } else {
                Nodo aux = this.pFirst;
                int count = 0;
                while (aux != null) {
                    if (aux.getDato() == dato) {
                        return count;
                    }
                    count++;
                    aux = aux.getpNext();
                }
            }
        }

        return -1;
    }

    public void deleteStart() {
        if (!this.isEmpty()) {
            if (this.size == 1) {
                this.setpFirst(null);
            } else {
                Nodo aux = this.pFirst;
                this.pFirst = aux.getpNext();
                aux.setpNext(null);

            }
            size--;
        }
    }

    public void deleteFinale() {
        if (!this.isEmpty()) {
            if (this.size == 1) {
                this.setpFirst(null);
            } else {
                Nodo aux = this.pFirst;
                while (aux.getpNext().getpNext() != null) {
                    aux = aux.getpNext();
                }
                aux.setpNext(null);
            }
            size--;
        }
    }

    //Con for
    public void deleteForIndex(int index) {
        if (index >= 0 && index < size) {
            if (index == 0) {
                this.deleteStart();
            } else {
                if (index == size - 1) {
                    this.deleteFinale();
                } else {
                    Nodo aux = pFirst;
                    for (int i = 0; i < index - 1; i++) {
                        aux = aux.getpNext();
                    }
                    Nodo siguiente = aux.getpNext();
                    aux.setpNext(siguiente.getpNext());
                    size--;
                }
            }

        }
    }

    public void deleteWhileindex(int index) {
        if (index >= 0 && index < size) {
            if (index == 0) {
                this.deleteStart();
            } else {
                if (index == size - 1) {
                    this.deleteFinale();
                } else {
                    Nodo aux = pFirst;
                    int count = 0;
                    while (index - 1 != count) {
                        aux = aux.getpNext();
                        count++;
                    }
                    Nodo siguiente = aux.getpNext();
                    aux.setpNext(siguiente.getpNext());
                    size--;

                }

            }
        }
    }

    public void deleteForReference(Object referencia) {

        if (this.search(referencia)) {
            if (pFirst.getDato() == referencia) {
                pFirst = pFirst.getpNext();
            } else {
                Nodo aux = pFirst;
                while (aux.getpNext().getDato() != referencia) {
                    aux = aux.getpNext();
                }
                Nodo siguiente = aux.getpNext().getpNext();
                aux.setpNext(siguiente);
            }
            size--;
        }
    }

    public void deleteElements(Object element) {
        if (!this.isEmpty()) {
            if (this.search(element)) {
                while (this.search(element)) {
                    this.deleteForReference(element);
                }
            } else {
                System.out.println("El elemento no se encuentra en la lista.");
            }
        } else {
            System.out.println("La lista esta vacia");
        }
    }

    public boolean search(Object dato) {
        boolean encontrado = false;
        if (!this.isEmpty()) {
            if (this.size == 1 && this.pFirst.getDato() == dato) {
                encontrado = true;
            } else {
                Nodo aux = this.pFirst;
                while (aux != null) {
                    if (aux.getDato() == dato) {
                        encontrado = true;
                    }

                    aux = aux.getpNext();
                }
            }
        }
        return encontrado;
    }

    public String convertString() {
        if (!this.isEmpty()) {
            Nodo aux = pFirst;
            String expresion = "";
            
            for (int i = 0; i < size; i++) {
                expresion += aux.getDato().toString() + "\n";
                aux = aux.getpNext();
            }
            return expresion;
        }
        return "Lista vacia";
    }

    public void print() {
        if (!this.isEmpty()) {
            Nodo aux = this.pFirst;
            while (aux != null) {
                System.out.println(aux.getDato());
                aux = aux.getpNext();
            }
        } else {
            System.out.println("La lista esta vacia");
        }
    }
    
}
