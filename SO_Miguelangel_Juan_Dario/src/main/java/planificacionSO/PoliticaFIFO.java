package planificacionSO;

import losEnums.DireccionCabezal;

/**
 *
 * @author jmmor
 */

import estructuraDeDatos.Cola;
import sistemaOperativo.SolicitudES;
import losEnums.DireccionCabezal;

public class PoliticaFIFO implements PoliticaDisco {
   @Override
    public SolicitudES elegir(Cola colaES, int cabezalActual, DireccionCabezal dir, int maxBloques) {
        return (SolicitudES) colaES.verFrente();
    }
    @Override
    public String getNombre() { return "FIFO"; }

}
