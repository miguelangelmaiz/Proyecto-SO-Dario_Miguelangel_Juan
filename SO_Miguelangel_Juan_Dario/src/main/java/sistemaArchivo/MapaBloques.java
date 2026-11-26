package sistemaArchivo;

import java.util.concurrent.Semaphore;

/**
 *
 * @author dariu
 */
public class MapaBloques {
    private final int total;
    private final boolean[] ocupado;
    private final boolean[] esSwap;
    private final int[] siguienteBloque;
    private final String[] duenoBloque;
    private final Semaphore mutex = new Semaphore(1);

    public MapaBloques(int totalBloques) {
        this.total = totalBloques;
        this.ocupado = new boolean[totalBloques];
        this.esSwap = new boolean[totalBloques];
        this.siguienteBloque = new int[totalBloques];
        this.duenoBloque = new String[totalBloques];
        for (int i = 0; i < totalBloques; i++) {
            ocupado[i] = false;
            esSwap[i] = false;
            siguienteBloque[i] = -1;
            duenoBloque[i] = null;
        }
    }

    public int getTotal() { return total; }

    // ------------------------------------------------------------------ archivos

    /** Asigna una cadena de bloques (fuera del swap) y devuelve el primero, o -1 si no hay espacio. */
    public int asignarCadena(int cantidad, String dueno) throws InterruptedException {
        return asignarEn(cantidad, dueno, false);
    }

    public int asignarCadena(int cantidad) throws InterruptedException {
        return asignarCadena(cantidad, "?");
    }

    /** Recorre el disco de 0 en adelante y toma los primeros bloques libres de la zona pedida. */
    private int asignarEn(int cantidad, String dueno, boolean zonaSwap) throws InterruptedException {
        if (cantidad <= 0) return -1;
        mutex.acquire();
        try {
            int libres = 0;
            for (int i = 0; i < total; i++) if (!ocupado[i] && esSwap[i] == zonaSwap) libres++;
            if (libres < cantidad) return -1; // No hay espacio

            int primerBloque = -1;
            int anterior = -1;
            for (int i = 0; i < total && cantidad > 0; i++) {
                if (!ocupado[i] && esSwap[i] == zonaSwap) {
                    ocupado[i] = true;
                    duenoBloque[i] = dueno;
                    siguienteBloque[i] = -1;
                    if (anterior == -1) primerBloque = i;
                    else siguienteBloque[anterior] = i; // Enlace encadenado
                    anterior = i;
                    cantidad--;
                }
            }
            return primerBloque;
        } finally {
            mutex.release();
        }
    }

    /**
     * Archivos que vienen del JSON: el primer bloque es startBlock y los siguientes son
     * los próximos bloques libres después del anterior. Devuelve false si startBlock está
     * fuera del disco u ocupado, o si no alcanzan los bloques.
     */
    public boolean marcarCadena(int inicio, int cantidad, String dueno) throws InterruptedException {
        if (cantidad <= 0 || inicio < 0 || inicio >= total) return false;
        mutex.acquire();
        try {
            if (ocupado[inicio] || esSwap[inicio]) return false;
            int[] cadena = new int[cantidad];
            cadena[0] = inicio;
            int n = 1;
            for (int i = inicio + 1; i < total && n < cantidad; i++) {
                if (!ocupado[i] && !esSwap[i]) cadena[n++] = i;
            }
            for (int i = 0; i < inicio && n < cantidad; i++) { // da la vuelta al disco si hace falta
                if (!ocupado[i] && !esSwap[i]) cadena[n++] = i;
            }
            if (n < cantidad) return false;
            enlazar(cadena, dueno);
            return true;
        } finally {
            mutex.release();
        }
    }

    /** Archivos guardados por este mismo programa: la cadena exacta viene en el JSON ("chain"). */
    public boolean marcarCadenaExacta(int[] cadena, String dueno) throws InterruptedException {
        if (cadena == null || cadena.length == 0) return false;
        mutex.acquire();
        try {
            for (int b : cadena) {
                if (b < 0 || b >= total || ocupado[b] || esSwap[b]) return false;
            }
            enlazar(cadena, dueno);
            return true;
        } finally {
            mutex.release();
        }
    }

    private void enlazar(int[] cadena, String dueno) {
        for (int k = 0; k < cadena.length; k++) {
            int b = cadena[k];
            ocupado[b] = true;
            duenoBloque[b] = dueno;
            siguienteBloque[b] = (k + 1 < cadena.length) ? cadena[k + 1] : -1;
        }
    }

    /** Libera la cadena de bloques a partir del primer bloque (sirve para archivos y para swap). */
    public void liberarCadena(int primerBloque) throws InterruptedException {
        if (primerBloque < 0 || primerBloque >= total) return;
        mutex.acquire();
        try {
            int actual = primerBloque;
            int pasos = 0;
            while (actual != -1 && pasos++ <= total) {
                int siguiente = siguienteBloque[actual];
                ocupado[actual] = false;
                siguienteBloque[actual] = -1;
                duenoBloque[actual] = null;
                actual = siguiente;
            }
        } finally {
            mutex.release();
        }
    }

    /** Cuando se renombra un archivo o directorio, los bloques pasan a mostrar la ruta nueva. */
    public void renombrarDueno(String rutaVieja, String rutaNueva) throws InterruptedException {
        mutex.acquire();
        try {
            for (int i = 0; i < total; i++) {
                String d = duenoBloque[i];
                if (d == null) continue;
                if (d.equals(rutaVieja)) duenoBloque[i] = rutaNueva;
                else if (d.startsWith(rutaVieja + "/")) duenoBloque[i] = rutaNueva + d.substring(rutaVieja.length());
            }
        } finally {
            mutex.release();
        }
    }

    // ------------------------------------------------------------------ swap

    /**
     * Reserva el área de intercambio: los "cantidad" bloques libres más cercanos al final
     * del disco. Devuelve cuántos pudo reservar.
     */
    public int reservarSwap(int cantidad) throws InterruptedException {
        mutex.acquire();
        try {
            int reservados = 0;
            for (int i = total - 1; i >= 0 && reservados < cantidad; i--) {
                if (!ocupado[i] && !esSwap[i]) {
                    esSwap[i] = true;
                    reservados++;
                }
            }
            return reservados;
        } finally {
            mutex.release();
        }
    }

    /**
     * Cambia el tamaño del área de intercambio (el swapSizeBlocks llega en el JSON de procesos,
     * después de haber cargado el del sistema de archivos). Los bloques de swap que ya ocupa
     * un proceso suspendido se respetan.
     */
    public int redefinirSwap(int cantidad) throws InterruptedException {
        mutex.acquire();
        int enUso = 0;
        try {
            for (int i = 0; i < total; i++) {
                if (esSwap[i]) {
                    if (ocupado[i]) enUso++;
                    else esSwap[i] = false;
                }
            }
        } finally {
            mutex.release();
        }
        return enUso + reservarSwap(Math.max(0, cantidad - enUso));
    }

    /** Escribe un proceso suspendido en el swap: una cadena de "cantidad" bloques del área de intercambio. */
    public int asignarSwap(int cantidad, String dueno) throws InterruptedException {
        return asignarEn(cantidad, dueno, true);
    }

    // ------------------------------------------------------------------ consultas

    private int contar(boolean swap, boolean soloLibres) {
        int n = 0;
        for (int i = 0; i < total; i++) {
            if (esSwap[i] == swap && (!soloLibres || !ocupado[i])) n++;
        }
        return n;
    }

    public int libres() {
        try { mutex.acquire(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); return 0; }
        try { return contar(false, true); } finally { mutex.release(); }
    }

    public int libresSwap() {
        try { mutex.acquire(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); return 0; }
        try { return contar(true, true); } finally { mutex.release(); }
    }

    public int totalSwap() {
        try { mutex.acquire(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); return 0; }
        try { return contar(true, false); } finally { mutex.release(); }
    }

    /** Primer bloque libre fuera del swap (adonde irá un archivo nuevo), o -1. */
    public int primerLibre() {
        try { mutex.acquire(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); return -1; }
        try {
            for (int i = 0; i < total; i++) if (!ocupado[i] && !esSwap[i]) return i;
            return -1;
        } finally { mutex.release(); }
    }

    /** "11 → 12 → 40": la cadena de un archivo, para la tabla y el JTree. */
    public String cadenaDesde(int primerBloque) {
        if (primerBloque < 0 || primerBloque >= total) return "-";
        try { mutex.acquire(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); return "-"; }
        try {
            StringBuilder sb = new StringBuilder();
            int actual = primerBloque;
            int pasos = 0;
            while (actual != -1 && pasos++ <= total) {
                if (sb.length() > 0) sb.append(" → ");
                sb.append(actual);
                actual = siguienteBloque[actual];
            }
            return sb.toString();
        } finally { mutex.release(); }
    }

    /** La cadena como arreglo, para guardarla en el JSON. */
    public int[] cadenaComoArreglo(int primerBloque) {
        if (primerBloque < 0 || primerBloque >= total) return new int[0];
        try { mutex.acquire(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); return new int[0]; }
        try {
            int n = 0;
            for (int a = primerBloque; a != -1 && n <= total; a = siguienteBloque[a]) n++;
            int[] r = new int[n];
            int k = 0;
            for (int a = primerBloque; a != -1 && k < n; a = siguienteBloque[a]) r[k++] = a;
            return r;
        } finally { mutex.release(); }
    }

    // Lecturas sueltas para pintar el disco (la interfaz solo observa)
    public boolean estaOcupado(int i) { return i >= 0 && i < total && ocupado[i]; }
    public boolean esSwap(int i) { return i >= 0 && i < total && esSwap[i]; }
    public String getDuenoBloque(int i) { return (i >= 0 && i < total) ? duenoBloque[i] : null; }
    public int getSiguiente(int i) { return (i >= 0 && i < total) ? siguienteBloque[i] : -1; }

    public boolean[] getOcupado() { return ocupado.clone(); }
}