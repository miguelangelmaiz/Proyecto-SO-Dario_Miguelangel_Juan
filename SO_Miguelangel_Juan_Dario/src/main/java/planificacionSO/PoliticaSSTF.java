package planificacionSO;

/**
 *
 * @author jmmor
 */

import estructuraDeDatos.Cola;
import estructuraDeDatos.Criterio;
import sistemaOperativo.SolicitudES;
import losEnums.DireccionCabezal;

public class PoliticaSSTF implements PoliticaDisco {
    @Override
    public SolicitudES elegir(Cola colaES, int cabezalActual, DireccionCabezal dir, int maxBloques) {
        Criterio masCercano = (a, b) -> {
            SolicitudES solA = (SolicitudES) a;
            SolicitudES solB = (SolicitudES) b;
            int distA = Math.abs(solA.getBloqueDestino() - cabezalActual);
            int distB = Math.abs(solB.getBloqueDestino() - cabezalActual);
            return distA < distB;
        };
        return (SolicitudES) colaES.verMejor(masCercano);
    }

    @Override
    public String getNombre() { return "SSTF"; }

    @Override
    public losEnums.TipoPoliticaDisco getTipo() { return losEnums.TipoPoliticaDisco.SSTF; }

    @Override
    public boolean esBarrido() { return false; }

    @Override
    public boolean esCircular() { return false; }

    @Override
    public int getSiguienteDestino(int cabezal, int maxBloques, DireccionCabezal dir) {
        return cabezal; // SSTF tampoco va a los extremos ciegamente
    }
}
