package planificacionSO;

/**
 *
 * @author jmmor
 */

import estructuraDeDatos.Cola;
import sistemaOperativo.SolicitudES;

public class PoliticaFIFO {
      // Este método recibe la cola y devuelve cuál es la siguiente solicitud a atender
    public SolicitudES elegir(Cola colaES) {
        // Simplemente sacamos la primera de la cola
        return (SolicitudES) colaES.verFrente(); }
}
