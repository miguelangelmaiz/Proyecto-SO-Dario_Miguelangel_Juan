/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemaOperativo;

/**
 *
 * @author jmmor
 */
public class configuracion {
   public int numComputadores;
    public int ramPorComputador;
    public int duracionCicloMs;
    public int latenciaRed;
    
    public static class ProcesoDTO {
        public String nombre;
        public String tipo;
        public int prioridad;
        public int memoria;
        public int deadline;
        public int instrucciones;
        public int computador;
    }
    
    public static class BufferDTO {
        public int capacidad;
        public int anfitrion;
    }
    
    public ProcesoDTO[] procesosIniciales;
    public BufferDTO[] buffersIniciales;
}
