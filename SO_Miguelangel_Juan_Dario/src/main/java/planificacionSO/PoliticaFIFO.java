package planificacionSO;

/**
 *
 * @author jmmor
 */

import estructuraDeDatos.Cola;
import sistemaOperativo.SolicitudES;

public class PoliticaFIFO implements PoliticaDisco {
      // Este método recibe la cola y devuelve cuál es la siguiente solicitud a atender
     @Override
    public SolicitudES elegir(Cola colaES, int cabezalActual) {
        // FIFO simplemente atiende al primero que llegó
        return (SolicitudES) colaES.verFrente();
        
        
    }
     @Override
    public String getNombre() {
        return "FIFO";
    }

}
