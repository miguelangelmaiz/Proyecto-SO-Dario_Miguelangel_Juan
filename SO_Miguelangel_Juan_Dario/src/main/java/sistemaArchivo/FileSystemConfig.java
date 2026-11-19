/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemaArchivo;

/**
 *
 * @author dariu
 */
import java.util.List;

public class FileSystemConfig {
    // Esta clase es solo para que Gson lea y guarde el JSON
    public List<NodoFSDTO> nodos;

    public static class NodoFSDTO {
        public String ruta;
        public boolean esDirectorio;
        public String dueno;
        public int bloqueInicio;
        public int cantidadBloques;
    }
}