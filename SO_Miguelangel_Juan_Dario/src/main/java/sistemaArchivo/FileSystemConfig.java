package sistemaArchivo;

/**
 *
 * @author dariu
 */
import java.util.List;

public class FileSystemConfig {
    // Esta clase es solo para que Gson lea y guarde el JSON
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