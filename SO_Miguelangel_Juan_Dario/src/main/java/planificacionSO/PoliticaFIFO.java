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

    @Override
    public losEnums.TipoPoliticaDisco getTipo() { return losEnums.TipoPoliticaDisco.FIFO; }

    @Override
    public boolean esBarrido() { return false; }

    @Override
    public boolean esCircular() { return false; }

    @Override
    public int getSiguienteDestino(int cabezal, int maxBloques, DireccionCabezal dir) {
        return cabezal; // FIFO no se mueve a los extremos, se queda donde está
    }
}
