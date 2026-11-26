package planificacionSO;

import estructuraDeDatos.Cola;
import estructuraDeDatos.Criterio;
import sistemaOperativo.SolicitudES;
import losEnums.DireccionCabezal;
/**
 *
 * @author jmmor
 */
public class PoliticaCSCAN implements PoliticaDisco{
    
   @Override
    public SolicitudES elegir(Cola colaES, int cabezalActual, DireccionCabezal dir, int maxBloques) {
        if (colaES.estaVacia()) return null;

        Criterio masCercanaArriba = (a, b) -> {
            int da = ((SolicitudES) a).getBloqueDestino();
            int db = ((SolicitudES) b).getBloqueDestino();
            boolean aSirve = da >= cabezalActual;
            boolean bSirve = db >= cabezalActual;
            if (aSirve != bSirve) return aSirve;
            return Math.abs(da - cabezalActual) < Math.abs(db - cabezalActual);
        };
        SolicitudES mejor = (SolicitudES) colaES.verMejor(masCercanaArriba);
        if (mejor == null || mejor.getBloqueDestino() < cabezalActual) return null;
        return mejor;
    }

    @Override
    public String getNombre() { return "C-SCAN"; }

    @Override
    public losEnums.TipoPoliticaDisco getTipo() { return losEnums.TipoPoliticaDisco.C_SCAN; }

    @Override
    public boolean esBarrido() { return true; }

    @Override
    public boolean esCircular() { return true; }

    @Override
    public int getSiguienteDestino(int cabezal, int maxBloques, DireccionCabezal dir) {
        // C-SCAN siempre va hacia el final del disco
        return maxBloques - 1;
    }
}
