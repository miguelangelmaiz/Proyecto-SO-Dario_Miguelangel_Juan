/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package planificacionSO;
import losEnums.TipoPoliticaDisco;
/**
 *
 * @author Miguel
 */
public class FabricaPoliticasDisco {
    public static PoliticaDisco crear(TipoPoliticaDisco tipo) {
        switch (tipo) {
            case SSTF:   return new PoliticaSSTF();
            case SCAN:   return new PoliticaSCAN();
            case C_SCAN: return new PoliticaCSCAN();
            case FIFO:
            default:     return new PoliticaFIFO();
        }
    } 
}
