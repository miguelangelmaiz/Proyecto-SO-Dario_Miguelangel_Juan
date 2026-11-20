package sistemaArchivo;

/**
 *
 * @author dariu
 */

import estructuraDeDatos.ListaSimple;
import losEnums.TipoOperacionES;
import java.util.concurrent.Semaphore;

public class SistemaArchivos {
    private NodoFS raiz;
    private MapaBloques mapa;
    private final Semaphore mutex = new Semaphore(1);

    public SistemaArchivos(int totalBloques) {
        this.raiz = new NodoFS("raiz", true, "admin");
        this.mapa = new MapaBloques(totalBloques);
    }

    public int primerBloqueReal(String ruta) {
        try {
            mutex.acquire();
            NodoFS nodo = buscarRutaInterno(ruta);
            if (nodo == null || nodo.isEsDirectorio()) return -1;
            return nodo.getBloqueInicio();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return -1;
        } finally {
            mutex.release();
        }
    }

    // AHORA DEVUELVE String: null si todo bien, o el mensaje de error
    public String aplicarCRUD(TipoOperacionES op, String ruta, String usuario, boolean admin, int bloquesSolicitados) {
        try {
            mutex.acquire();
            NodoFS nodo = buscarRutaInterno(ruta);

            if (nodo != null && !admin && !nodo.getDueno().equals(usuario)) {
                return "Permiso denegado: no eres el dueño";
            }

            switch (op) {
                case CREAR:
                    if (nodo != null) return "El archivo ya existe";
                    int bloque = mapa.asignarCadena(bloquesSolicitados);
                    if (bloque == -1) return "No hay espacio en disco";
                    
                    NodoFS nuevo = new NodoFS(obtenerNombre(ruta), false, usuario);
                    nuevo.setBloqueInicio(bloque);
                    nuevo.setCantidadBloques(bloquesSolicitados);
                    insertarNodo(ruta, nuevo);
                    return null; // null significa ÉXITO

                case ELIMINAR:
                    if (nodo == null) return "No existe";
                    if (nodo.isEsDirectorio() && nodo.getHijos().getSize() > 0) {
                        return "El directorio no está vacío";
                    }
                    if (!nodo.isEsDirectorio()) {
                        mapa.liberarCadena(nodo.getBloqueInicio());
                    }
                    eliminarNodo(ruta);
                    return null; // null significa ÉXITO

                case LEER:
                case ACTUALIZAR:
                    if (nodo == null || nodo.isEsDirectorio()) return "Archivo inválido";
                    return null; // null significa ÉXITO

                default:
                    return "Operación no soportada";
            }
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        } finally {
            mutex.release();
        }
    }

    private NodoFS buscarRutaInterno(String ruta) {
        if (ruta.equals("/") || ruta.equals("raiz")) return raiz;
        String[] partes = ruta.split("/");
        NodoFS actual = raiz;
        for (String p : partes) {
            if (p.isEmpty()) continue;
            NodoFS encontrado = null;
            ListaSimple hijos = actual.getHijos();
            for (int i = 0; i < hijos.getSize(); i++) {
                NodoFS h = (NodoFS) hijos.getValue(i);
                if (h.getNombre().equals(p)) {
                    encontrado = h;
                    break;
                }
            }
            if (encontrado == null) return null;
            actual = encontrado;
        }
        return actual;
    }

    private void insertarNodo(String ruta, NodoFS nuevo) {
        String rutaPadre = obtenerRutaPadre(ruta);
        NodoFS padre = buscarRutaInterno(rutaPadre);
        if (padre != null && padre.isEsDirectorio()) {
            padre.getHijos().insertFinale(nuevo);
        } else {
            raiz.getHijos().insertFinale(nuevo); 
        }
    }

    private void eliminarNodo(String ruta) {
        String rutaPadre = obtenerRutaPadre(ruta);
        NodoFS padre = buscarRutaInterno(rutaPadre);
        if (padre != null) {
            String nombre = obtenerNombre(ruta);
            ListaSimple hijos = padre.getHijos();
            for (int i = 0; i < hijos.getSize(); i++) {
                NodoFS h = (NodoFS) hijos.getValue(i);
                if (h.getNombre().equals(nombre)) {
                    hijos.deleteForIndex(i);
                    break;
                }
            }
        }
    }

    private String obtenerNombre(String ruta) {
        String[] partes = ruta.split("/");
        return partes[partes.length - 1];
    }

    private String obtenerRutaPadre(String ruta) {
        int ultimoSlash = ruta.lastIndexOf('/');
        if (ultimoSlash <= 0) return "/";
        return ruta.substring(0, ultimoSlash);
    }

    public String getSnapshotArbol() {
        try {
            mutex.acquire();
            StringBuilder sb = new StringBuilder();
            generarSnapshotRecursivo(raiz, "", sb);
            return sb.toString();
        } catch (InterruptedException e) {
            return "Error";
        } finally {
            mutex.release();
        }
    }

    private void generarSnapshotRecursivo(NodoFS nodo, String prefijo, StringBuilder sb) {
        sb.append(prefijo).append(nodo.getNombre());
        if (!nodo.isEsDirectorio()) {
            sb.append(" [Bloque: ").append(nodo.getBloqueInicio()).append("]");
        }
        sb.append("\n");
        ListaSimple hijos = nodo.getHijos();
        for (int i = 0; i < hijos.getSize(); i++) {
            generarSnapshotRecursivo((NodoFS) hijos.getValue(i), prefijo + "  ", sb);
        }
    }
    public NodoFS getRaiz() { return raiz; }

public void inicializarDesdeJSON(FileSystemConfig config) {
    for (FileSystemConfig.NodoFSDTO dto : config.nodos) {
        NodoFS nuevo = new NodoFS(obtenerNombre(dto.ruta), dto.esDirectorio, dto.dueno);
        nuevo.setBloqueInicio(dto.bloqueInicio);
        nuevo.setCantidadBloques(dto.cantidadBloques);
        insertarNodo(dto.ruta, nuevo);
    }}
    
}