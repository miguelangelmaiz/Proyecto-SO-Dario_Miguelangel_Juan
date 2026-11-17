
package planificacionSO;

/**
 *
 * @author jmmor
 */

import estructuraDeDatos.Cola;
import sistemaOperativo.SolicitudES;


public interface PoliticaDisco {
    SolicitudES elegir(Cola colaES, int cabezalActual);
}
