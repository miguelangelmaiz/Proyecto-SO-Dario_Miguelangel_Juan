
package planificacionSO;

import losEnums.DireccionCabezal;
/**
 *
 * @author jmmor
 */

import estructuraDeDatos.Cola;
import sistemaOperativo.SolicitudES;


public interface PoliticaDisco {
    SolicitudES elegir(Cola colaES, int cabezalActual, DireccionCabezal dir, int maxBloques);
    String getNombre();
}
