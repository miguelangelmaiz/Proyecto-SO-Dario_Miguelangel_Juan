package sistemaArchivo;

/**
 *
 * @author dariu
 */


public class FileSystemConfig {
    // 1. AGREGAR ESTA CLASE INTERNA PARA EL DISCO
    public static class DiskConfigDTO {
        public int totalBlocks;
        public int initialHead;
    }

    // 2. AGREGAR ESTA VARIABLE
    public DiskConfigDTO diskConfig; 

    // Lo que ya tenías
    public NodoFSDTO[] nodos;

    public static class NodoFSDTO {
        public String ruta;
        public boolean esDirectorio;
        public String dueno;
        public int bloqueInicio;
        public int cantidadBloques;
        public boolean permisoLectura = true;
    }
}