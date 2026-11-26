
package planificacionSO;

import losEnums.DireccionCabezal;
import losEnums.TipoPoliticaDisco;
/**
 *
 * @author jmmor
 */

import estructuraDeDatos.Cola;
import sistemaOperativo.SolicitudES;


public interface PoliticaDisco {
    SolicitudES elegir(Cola colaES, int cabezalActual, DireccionCabezal dir, int maxBloques);
    String getNombre();
    TipoPoliticaDisco getTipo(); // [AJUSTE 25-nov] para las métricas por política
    
    // NUEVOS MÉTODOS PARA QUITAR EL IF DEL DISCO
    boolean esBarrido(); // true para SCAN/CSCAN, false para FIFO/SSTF
    boolean esCircular(); // true solo para CSCAN
    int getSiguienteDestino(int cabezal, int maxBloques, DireccionCabezal dir); 
}
