package sistemaArchivo;

import java.util.concurrent.Semaphore;

/**
 *
 * @author dariu
 */
public class MapaBloques {
    private boolean[] ocupado;
    private int[] siguienteBloque;
    private final Semaphore mutex = new Semaphore(1);

    public MapaBloques(int totalBloques) {
        this.ocupado = new boolean[totalBloques];
        this.siguienteBloque = new int[totalBloques];
        for (int i = 0; i < totalBloques; i++) {
            ocupado[i] = false;
            siguienteBloque[i] = -1;
        }
    }

    // Asigna una cadena de bloques y devuelve el índice del primer bloque
    public int asignarCadena(int cantidad) throws InterruptedException {
        mutex.acquire();
        try {
            // Buscar si hay espacio suficiente
            int libres = 0;
            for (boolean b : ocupado) if (!b) libres++;
            if (libres < cantidad) return -1; // No hay espacio

            int primerBloque = -1;
            int anterior = -1;

            for (int i = 0; i < ocupado.length && cantidad > 0; i++) {
                if (!ocupado[i]) {
                    ocupado[i] = true;
                    if (anterior == -1) {
                        primerBloque = i;
                    } else {
                        siguienteBloque[anterior] = i; // Enlace encadenado
                    }
                    anterior = i;
                    cantidad--;
                }
            }
            return primerBloque;
        } finally {
            mutex.release();
        }
    }

    // Libera la cadena de bloques a partir del primer bloque
    public void liberarCadena(int primerBloque) throws InterruptedException {
        if (primerBloque == -1) return;
        mutex.acquire();
        try {
            int actual = primerBloque;
            while (actual != -1) {
                int siguiente = siguienteBloque[actual];
                ocupado[actual] = false;
                siguienteBloque[actual] = -1;
                actual = siguiente;
            }
        } finally {
            mutex.release();
        }
    }
    
     // Método para marcar bloques que ya vienen asignados desde el JSON
    public void marcarOcupado(int inicio, int cantidad) throws InterruptedException {
        mutex.acquire();
        try {
            for (int i = 0; i < cantidad; i++) {
                if (inicio + i < ocupado.length) {
                    ocupado[inicio + i] = true;
                }
            }
        } finally {
            mutex.release();
        }
    }
}