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
    
    // NUEVOS CAMPOS PARA EL ESCENARIO
    public PCB[] procesosIniciales; 
    public Buffer[] buffersIniciales;
}
