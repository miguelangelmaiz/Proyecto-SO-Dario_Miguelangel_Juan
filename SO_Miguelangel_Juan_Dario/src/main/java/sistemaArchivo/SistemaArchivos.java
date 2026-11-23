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

            // Validación de permisos
            if (nodo != null && !admin && !nodo.getDueno().equals(usuario)) {
                return "Permiso denegado: no eres el dueño";
            }

            switch (op) {
                case CREAR:
                    if (nodo != null) return "El archivo ya existe";
                    String rutaPadre = obtenerRutaPadre(ruta);
                    NodoFS padre = buscarRutaInterno(rutaPadre);
                    if (padre == null || !padre.isEsDirectorio()) return "El directorio padre no existe";
                    
                    // Validar que el usuario cree en su carpeta
                    if (!admin && !ruta.contains("/" + usuario)) return "Solo puedes crear en tu carpeta";

                    int bloque = mapa.asignarCadena(bloquesSolicitados);
                    if (bloque == -1) return "No hay espacio en disco";
                    
                    NodoFS nuevo = new NodoFS(obtenerNombre(ruta), false, usuario);
                    nuevo.setBloqueInicio(bloque);
                    nuevo.setCantidadBloques(bloquesSolicitados);
                    padre.getHijos().insertFinale(nuevo);
                    return null; // Éxito

                case ELIMINAR:
                    if (nodo == null) return "No existe";
                    if (nodo == raiz) return "No puedes borrar la raíz";
                    
                    // Borrado recursivo si es directorio
                    if (nodo.isEsDirectorio()) {
                        if (!admin) return "Solo el admin puede borrar directorios";
                        eliminarRecursivo(nodo);
                    } else {
                        mapa.liberarCadena(nodo.getBloqueInicio());
                    }
                    
                    eliminarNodoDePadre(ruta);
                    return null; // Éxito

                case LEER:
                    if (nodo == null || nodo.isEsDirectorio()) return "Archivo inválido";
                    return null; // Éxito

                case ACTUALIZAR:
                    if (nodo == null) return "No existe";
                    if (!admin) return "Solo el admin puede renombrar";
                    // Renombrar
                    String nuevoNombre = obtenerNombre(ruta) + "_modificado";
                    nodo.setNombre(nuevoNombre);
                    return null; // Éxito

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
         if (ruta == null || ruta.trim().isEmpty() || ruta.equals("/") || ruta.equals("raiz")) {
            return raiz;
        }
        String[] partes = ruta.split("/");
        NodoFS actual = raiz;
        for (String p : partes) {
            if (p.isEmpty()) continue;
            NodoFS encontrado = null;
            ListaSimple hijos = actual.getHijos();
            for (int i = 0; i < hijos.getSize(); i++) {
                NodoFS h = (NodoFS) hijos.getValue(i);
                // Imprimimos para depurar en consola
                System.out.println("Buscando: " + p + " | En nodo: " + actual.getNombre() + " | Hijo actual: " + h.getNombre());
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

     private void eliminarRecursivo(NodoFS nodo) throws InterruptedException {
        if (!nodo.isEsDirectorio()) {
            mapa.liberarCadena(nodo.getBloqueInicio());
        } else {
            ListaSimple hijos = nodo.getHijos();
            for (int i = 0; i < hijos.getSize(); i++) {
                eliminarRecursivo((NodoFS) hijos.getValue(i));
            }
        }
    }

    private void eliminarNodoDePadre(String ruta) {
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
       if (ruta == null || ruta.trim().isEmpty() || ruta.equals("/")) return "raiz";
        String[] partes = ruta.split("/");
        if (partes.length == 0) return "raiz";
        return partes[partes.length - 1];
    }

      private String obtenerRutaPadre(String ruta) {
        if (ruta.equals("/") || ruta.equals("raiz") || ruta.lastIndexOf('/') <= 0) return "/";
        return ruta.substring(0, ruta.lastIndexOf('/'));
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

 public void inicializarDesdeJSON(sistemaArchivo.FileSystemConfig configFS) {
        if (configFS == null || configFS.fileSystem == null) return;
        
        try {
            mutex.acquire();
            // 1. Reiniciar el árbol
            this.raiz = new NodoFS("raiz", true, "admin");
            
            // 2. Crear primero todos los DIRECTORIOS
            for (sistemaArchivo.FileSystemConfig.FileSystemDTO dto : configFS.fileSystem) {
                if (dto.type.equals("DIRECTORY")) {
                    String nombre = obtenerNombre(dto.path);
                    NodoFS nuevoDir = new NodoFS(nombre, true, dto.owner);
                    insertarNodo(dto.path, nuevoDir);
                }
            }
            
            // 3. Crear los ARCHIVOS y marcar sus bloques
            for (sistemaArchivo.FileSystemConfig.FileSystemDTO dto : configFS.fileSystem) {
                if (dto.type.equals("FILE")) {
                    String nombre = obtenerNombre(dto.path);
                    NodoFS nuevoArch = new NodoFS(nombre, false, dto.owner);
                    nuevoArch.setBloqueInicio(dto.startBlock);
                    nuevoArch.setCantidadBloques(dto.blocks);
                    
                    insertarNodo(dto.path, nuevoArch);
                    
                    // Marcar sus bloques en el MapaBloques
                    mapa.marcarOcupado(dto.startBlock, dto.blocks);
                }
            }
        } catch (Exception e) {
            System.out.println("Error al inicializar árbol: " + e.getMessage());
        } finally {
            mutex.release();
        }
    }


    
}