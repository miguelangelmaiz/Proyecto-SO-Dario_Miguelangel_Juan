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
    private volatile int version = 0; // sube con cada cambio: la interfaz redibuja el JTree solo cuando cambia

    public SistemaArchivos(int totalBloques) {
        this.raiz = new NodoFS("raiz", true, "admin");
        this.mapa = new MapaBloques(totalBloques);
    }

    public int getVersion() { return version; }
    public MapaBloques getMapaBloques() { return mapa; }
    public NodoFS getRaiz() { return raiz; }

    // ================================================================== consultas para el núcleo

    public int primerBloqueReal(String ruta) {
        try {
            mutex.acquire();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return -1;
        }
        try {
            NodoFS nodo = buscarRutaInterno(ruta);
            if (nodo == null || nodo.isEsDirectorio()) return -1;
            return nodo.getBloqueInicio();
        } finally {
            mutex.release();
        }
    }

    /**
     * Bloque al que debe ir el cabezal para atender la operación.
     *  - CREAR un archivo: el primer bloque libre (ahí empezará a escribirse).
     *  - LEER / ACTUALIZAR / ELIMINAR un archivo: su primer bloque.
     *  - Directorios: no ocupan bloques; la operación solo toca metadatos y el cabezal no se mueve.
     */
    public int bloqueDestino(TipoOperacionES op, String ruta, boolean esDirectorio, int cabezalActual) {
        if (op == TipoOperacionES.CREAR) {
            if (esDirectorio) return cabezalActual;
            int libre = mapa.primerLibre();
            return libre >= 0 ? libre : cabezalActual;
        }
        int b = primerBloqueReal(ruta);
        return b >= 0 ? b : cabezalActual;
    }

    /** Cuántos bloques tiene que leer o escribir el disco para la operación (cada bloque cuesta un ciclo). */
    public int bloquesATransferir(TipoOperacionES op, String ruta, int bloquesSolicitados) {
        if (op == TipoOperacionES.CREAR) return Math.max(1, bloquesSolicitados);
        if (op == TipoOperacionES.LEER) {
            try {
                mutex.acquire();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return 1;
            }
            try {
                NodoFS n = buscarRutaInterno(ruta);
                return (n == null || n.isEsDirectorio()) ? 1 : Math.max(1, n.getCantidadBloques());
            } finally {
                mutex.release();
            }
        }
        return 1; // renombrar o borrar solo toca la entrada del directorio
    }

    // ================================================================== validación y CRUD

    /** Devuelve null si la operación se puede hacer, o el motivo del rechazo. */
    public String validar(TipoOperacionES op, String ruta, String usuario, boolean admin,
                          boolean esDirectorio, int bloques, String nuevoNombre) {
        try {
            mutex.acquire();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "Operación interrumpida";
        }
        try {
            return validarInterno(op, ruta, usuario, admin, esDirectorio, bloques, nuevoNombre);
        } finally {
            mutex.release();
        }
    }

    private String validarInterno(TipoOperacionES op, String ruta, String usuario, boolean admin,
                                  boolean esDirectorio, int bloques, String nuevoNombre) {
        if (op == null) return "Debe indicar una operación";
        if (ruta == null || ruta.trim().isEmpty()) return "Debe indicar la ruta";
        ruta = ruta.trim();
        if (!ruta.startsWith("/")) return "La ruta debe empezar con / (ejemplo: /usuarios/notas.txt)";
        if (ruta.length() > 1 && ruta.endsWith("/")) return "La ruta no puede terminar en /";
        if (ruta.contains("//")) return "La ruta tiene dos / seguidas";

        NodoFS nodo = buscarRutaInterno(ruta);
        boolean esDueno = nodo != null && usuario != null && usuario.equals(nodo.getDueno());

        switch (op) {
            case LEER:
                if (nodo == null) return "No existe " + ruta;
                if (nodo.isEsDirectorio()) return ruta + " es un directorio; solo se leen archivos";
                if (!admin && !esDueno && !nodo.isPublico())
                    return "Acceso denegado: " + ruta + " es privado de " + nodo.getDueno();
                return null;

            case CREAR: {
                if (nodo != null) return "Ya existe " + ruta;
                String nombre = obtenerNombre(ruta);
                if (nombre.isEmpty() || nombre.equals("raiz")) return "Nombre inválido";
                NodoFS padre = buscarRutaInterno(obtenerRutaPadre(ruta));
                if (padre == null || !padre.isEsDirectorio())
                    return "No existe el directorio " + obtenerRutaPadre(ruta);
                if (!esDirectorio && bloques <= 0) return "Un archivo necesita al menos 1 bloque";
                if (!admin) {
                    if (esDirectorio) return "Acceso denegado: solo el administrador crea directorios";
                    boolean suyo = usuario != null && usuario.equals(padre.getDueno());
                    if (!suyo && !ruta.startsWith("/usuarios/"))
                        return "Acceso denegado: un usuario solo crea archivos en sus directorios o en /usuarios";
                }
                if (!esDirectorio && mapa.libres() < bloques)
                    return "No hay bloques libres suficientes: pide " + bloques + ", quedan " + mapa.libres();
                return null;
            }

            case ACTUALIZAR: {
                if (!admin) return "Acceso denegado: solo el administrador puede renombrar";
                if (nodo == null) return "No existe " + ruta;
                if (nodo == raiz) return "No se puede renombrar la raíz";
                String nuevo = nombreNuevoPara(ruta, nuevoNombre);
                if (nuevo.contains("/")) return "El nombre nuevo no puede llevar /";
                NodoFS padre = buscarRutaInterno(obtenerRutaPadre(ruta));
                if (padre != null && buscarHijo(padre, nuevo) != null)
                    return "Ya existe " + nuevo + " en " + obtenerRutaPadre(ruta);
                return null;
            }

            case ELIMINAR:
                if (nodo == null) return "No existe " + ruta;
                if (nodo == raiz) return "No se puede eliminar la raíz";
                if (!admin) {
                    if (nodo.isEsDirectorio()) return "Acceso denegado: solo el administrador elimina directorios";
                    if (!esDueno) return "Acceso denegado: " + ruta + " es de " + nodo.getDueno();
                }
                return null;

            default:
                return "Operación no soportada: " + op;
        }
    }

    /** Si no se indicó nombre nuevo (el JSON del enunciado no lo trae), se agrega "_mod". */
    private String nombreNuevoPara(String ruta, String nuevoNombre) {
        if (nuevoNombre != null && !nuevoNombre.trim().isEmpty()) return nuevoNombre.trim();
        return obtenerNombre(ruta) + "_mod";
    }

    /**
     * Ejecuta la operación (la llama el núcleo cuando el disco terminó la solicitud).
     * Devuelve null si salió bien, o el motivo del error.
     */
    public String aplicarCRUD(TipoOperacionES op, String ruta, String usuario, boolean admin,
                              int bloquesSolicitados, boolean esDirectorio, boolean publico,
                              String nuevoNombre, String creadoPor) {
        try {
            mutex.acquire();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "Operación interrumpida";
        }
        try {
            String error = validarInterno(op, ruta, usuario, admin, esDirectorio, bloquesSolicitados, nuevoNombre);
            if (error != null) return error;
            ruta = ruta.trim();
            NodoFS nodo = buscarRutaInterno(ruta);

            switch (op) {
                case LEER:
                    return null; // No se simula el contenido: basta con que el disco haya leído los bloques

                case CREAR: {
                    NodoFS padre = buscarRutaInterno(obtenerRutaPadre(ruta));
                    NodoFS nuevo = new NodoFS(obtenerNombre(ruta), esDirectorio, usuario);
                    nuevo.setPermisoLectura(publico);
                    nuevo.setCreadoPor(creadoPor);
                    if (!esDirectorio) {
                        int bloque = mapa.asignarCadena(bloquesSolicitados, ruta);
                        if (bloque == -1) return "No hay espacio en disco";
                        nuevo.setBloqueInicio(bloque);
                        nuevo.setCantidadBloques(bloquesSolicitados);
                    }
                    padre.getHijos().insertFinale(nuevo);
                    break;
                }

                case ACTUALIZAR: {
                    String nuevoNom = nombreNuevoPara(ruta, nuevoNombre);
                    String rutaPadre = obtenerRutaPadre(ruta);
                    String rutaNueva = (rutaPadre.equals("/") ? "" : rutaPadre) + "/" + nuevoNom;
                    nodo.setNombre(nuevoNom);
                    mapa.renombrarDueno(ruta, rutaNueva);
                    break;
                }

                case ELIMINAR:
                    eliminarRecursivo(nodo);
                    eliminarNodoDePadre(ruta);
                    break;

                default:
                    return "Operación no soportada";
            }
            version++;
            return null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "Operación interrumpida";
        } finally {
            mutex.release();
        }
    }

    // ================================================================== árbol

    private NodoFS buscarRutaInterno(String ruta) {
        if (ruta == null || ruta.trim().isEmpty() || ruta.trim().equals("/") || ruta.equals("raiz")) {
            return raiz;
        }
        String[] partes = ruta.trim().split("/");
        NodoFS actual = raiz;
        for (String p : partes) {
            if (p.isEmpty()) continue;
            if (!actual.isEsDirectorio()) return null;
            NodoFS encontrado = buscarHijo(actual, p);
            if (encontrado == null) return null;
            actual = encontrado;
        }
        return actual;
    }

    private NodoFS buscarHijo(NodoFS dir, String nombre) {
        ListaSimple hijos = dir.getHijos();
        for (int i = 0; i < hijos.getSize(); i++) {
            NodoFS h = (NodoFS) hijos.getValue(i);
            if (h.getNombre().equals(nombre)) return h;
        }
        return null;
    }

    /** Borra un archivo (libera su cadena) o un directorio con todo lo que tiene adentro. */
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
        NodoFS padre = buscarRutaInterno(obtenerRutaPadre(ruta));
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
        if (ruta == null || ruta.trim().isEmpty() || ruta.trim().equals("/")) return "raiz";
        String[] partes = ruta.trim().split("/");
        if (partes.length == 0) return "raiz";
        return partes[partes.length - 1];
    }

    private String obtenerRutaPadre(String ruta) {
        ruta = ruta.trim();
        if (ruta.equals("/") || ruta.equals("raiz") || ruta.lastIndexOf('/') <= 0) return "/";
        return ruta.substring(0, ruta.lastIndexOf('/'));
    }

    // ================================================================== lo que observa la interfaz

    /**
     * Una línea por nodo, con dos espacios de sangría por nivel. Los campos van separados
     * por tabuladores: nombre, D/F, dueño, permiso, bloques, primer bloque, cadena, ruta.
     * PanelArbolFS arma el JTree con esto y muestra los datos del nodo seleccionado.
     */
    public String getSnapshotArbol() {
        try {
            mutex.acquire();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "";
        }
        try {
            StringBuilder sb = new StringBuilder();
            generarSnapshotRecursivo(raiz, "", "/", sb);
            return sb.toString();
        } finally {
            mutex.release();
        }
    }

    private void generarSnapshotRecursivo(NodoFS nodo, String prefijo, String ruta, StringBuilder sb) {
        sb.append(prefijo).append(nodo == raiz ? "/" : nodo.getNombre())
          .append('\t').append(nodo.isEsDirectorio() ? "D" : "F")
          .append('\t').append(nodo.getDueno())
          .append('\t').append(nodo.isPublico() ? "público" : "privado")
          .append('\t').append(nodo.getCantidadBloques())
          .append('\t').append(nodo.getBloqueInicio())
          .append('\t').append(nodo.isEsDirectorio() ? "-" : mapa.cadenaDesde(nodo.getBloqueInicio()))
          .append('\t').append(ruta)
          .append("\n");
        ListaSimple hijos = nodo.getHijos();
        for (int i = 0; i < hijos.getSize(); i++) {
            NodoFS h = (NodoFS) hijos.getValue(i);
            String rutaHijo = (ruta.equals("/") ? "" : ruta) + "/" + h.getNombre();
            generarSnapshotRecursivo(h, prefijo + "  ", rutaHijo, sb);
        }
    }

    /** Filas de la tabla de asignación: nombre, ruta, bloques, primer bloque, dueño, permiso, creado por, cadena. */
    public Object[][] getFilasTabla() {
        try {
            mutex.acquire();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new Object[0][];
        }
        try {
            int n = contarArchivos(raiz);
            Object[][] filas = new Object[n][];
            llenarFilas(raiz, "/", filas, new int[]{0});
            return filas;
        } finally {
            mutex.release();
        }
    }

    private int contarArchivos(NodoFS nodo) {
        int c = nodo.isEsDirectorio() ? 0 : 1;
        for (int i = 0; i < nodo.getHijos().getSize(); i++) c += contarArchivos((NodoFS) nodo.getHijos().getValue(i));
        return c;
    }

    private void llenarFilas(NodoFS nodo, String ruta, Object[][] filas, int[] k) {
        if (!nodo.isEsDirectorio()) {
            filas[k[0]++] = new Object[]{
                nodo.getNombre(), ruta, nodo.getCantidadBloques(), nodo.getBloqueInicio(),
                nodo.getDueno(), nodo.isPublico() ? "público" : "privado", nodo.getCreadoPor(),
                mapa.cadenaDesde(nodo.getBloqueInicio())
            };
        }
        for (int i = 0; i < nodo.getHijos().getSize(); i++) {
            NodoFS h = (NodoFS) nodo.getHijos().getValue(i);
            llenarFilas(h, (ruta.equals("/") ? "" : ruta) + "/" + h.getNombre(), filas, k);
        }
    }

    // ================================================================== JSON

    /**
     * Carga el sistema de archivos del JSON y reserva el swap. Devuelve un texto con los
     * avisos (entradas que no se pudieron cargar), vacío si todo salió bien.
     */
    public String inicializarDesdeJSON(FileSystemConfig configFS, int swapBloques) {
        StringBuilder avisos = new StringBuilder();
        if (configFS == null || configFS.fileSystem == null) return "El JSON no trae \"fileSystem\".";

        int total = (configFS.diskConfig != null && configFS.diskConfig.totalBlocks > 0)
                ? configFS.diskConfig.totalBlocks : 200;
        try {
            mutex.acquire();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "Carga interrumpida";
        }
        try {
            this.raiz = new NodoFS("raiz", true, "admin");
            this.mapa = new MapaBloques(total);

            // 1. Directorios, de menor a mayor profundidad (así el padre siempre existe antes que el hijo)
            for (int nivel = 1; nivel <= 64; nivel++) {
                for (FileSystemConfig.FileSystemDTO dto : configFS.fileSystem) {
                    if (dto == null || dto.path == null || !"DIRECTORY".equalsIgnoreCase(dto.type)) continue;
                    String ruta = dto.path.trim();
                    if (ruta.equals("/") || profundidad(ruta) != nivel) continue; // la raíz ya existe
                    NodoFS padre = buscarRutaInterno(obtenerRutaPadre(ruta));
                    if (padre == null || !padre.isEsDirectorio()) {
                        avisos.append("Directorio sin padre, se omite: ").append(ruta).append('\n');
                        continue;
                    }
                    if (buscarHijo(padre, obtenerNombre(ruta)) != null) continue;
                    NodoFS d = new NodoFS(obtenerNombre(ruta), true, dto.owner == null ? "admin" : dto.owner);
                    d.setPermisoLectura(!"private".equalsIgnoreCase(dto.permissions));
                    padre.getHijos().insertFinale(d);
                }
            }

            // 2. Archivos, con su cadena de bloques
            for (FileSystemConfig.FileSystemDTO dto : configFS.fileSystem) {
                if (dto == null || dto.path == null || !"FILE".equalsIgnoreCase(dto.type)) continue;
                String ruta = dto.path.trim();
                NodoFS padre = buscarRutaInterno(obtenerRutaPadre(ruta));
                if (padre == null || !padre.isEsDirectorio()) {
                    avisos.append("Archivo sin directorio padre, se omite: ").append(ruta).append('\n');
                    continue;
                }
                int bloques = dto.blocks > 0 ? dto.blocks : 1;
                boolean ok = (dto.chain != null && dto.chain.length == bloques && dto.chain[0] == dto.startBlock)
                        ? mapa.marcarCadenaExacta(dto.chain, ruta)
                        : mapa.marcarCadena(dto.startBlock, bloques, ruta);
                if (!ok) {
                    avisos.append("Bloques inválidos u ocupados, se omite: ").append(ruta)
                          .append(" (startBlock ").append(dto.startBlock).append(")\n");
                    continue;
                }
                NodoFS f = new NodoFS(obtenerNombre(ruta), false, dto.owner == null ? "admin" : dto.owner);
                f.setBloqueInicio(dto.startBlock);
                f.setCantidadBloques(bloques);
                f.setPermisoLectura(!"private".equalsIgnoreCase(dto.permissions));
                padre.getHijos().insertFinale(f);
            }

            // 3. Área de intercambio: los bloques libres más cercanos al final del disco
            int reservados = mapa.reservarSwap(Math.max(0, swapBloques));
            if (reservados < swapBloques) {
                avisos.append("Solo cupieron ").append(reservados).append(" de ")
                      .append(swapBloques).append(" bloques de swap\n");
            }
            version++;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            avisos.append("Carga interrumpida\n");
        } finally {
            mutex.release();
        }
        return avisos.toString();
    }

    private int profundidad(String ruta) {
        int n = 0;
        for (String p : ruta.split("/")) if (!p.isEmpty()) n++;
        return n;
    }

    public FileSystemConfig exportarAJSON(int totalBloques, int cabezal) {
        FileSystemConfig config = new FileSystemConfig();
        config.diskConfig = new FileSystemConfig.DiskConfigDTO();
        config.diskConfig.totalBlocks = totalBloques;
        config.diskConfig.initialHead = cabezal;
        try {
            mutex.acquire();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return config;
        }
        try {
            int totalNodos = contarNodos(raiz) - 1; // la raíz no se guarda
            config.fileSystem = new FileSystemConfig.FileSystemDTO[totalNodos];
            int[] k = {0};
            for (int i = 0; i < raiz.getHijos().getSize(); i++) {
                NodoFS h = (NodoFS) raiz.getHijos().getValue(i);
                llenarDTO(h, "/" + h.getNombre(), config.fileSystem, k);
            }
        } finally {
            mutex.release();
        }
        return config;
    }

    private int contarNodos(NodoFS nodo) {
        int count = 1;
        for (int i = 0; i < nodo.getHijos().getSize(); i++) {
            count += contarNodos((NodoFS) nodo.getHijos().getValue(i));
        }
        return count;
    }

    private void llenarDTO(NodoFS nodo, String ruta, FileSystemConfig.FileSystemDTO[] arr, int[] index) {
        FileSystemConfig.FileSystemDTO dto = new FileSystemConfig.FileSystemDTO();
        dto.path = ruta;
        dto.type = nodo.isEsDirectorio() ? "DIRECTORY" : "FILE";
        dto.owner = nodo.getDueno();
        dto.permissions = nodo.isPublico() ? "public" : "private";
        if (!nodo.isEsDirectorio()) {
            dto.startBlock = nodo.getBloqueInicio();
            dto.blocks = nodo.getCantidadBloques();
            dto.chain = mapa.cadenaComoArreglo(nodo.getBloqueInicio());
        }
        arr[index[0]++] = dto;
        for (int i = 0; i < nodo.getHijos().getSize(); i++) {
            NodoFS h = (NodoFS) nodo.getHijos().getValue(i);
            llenarDTO(h, ruta + "/" + h.getNombre(), arr, index);
        }
    }
}