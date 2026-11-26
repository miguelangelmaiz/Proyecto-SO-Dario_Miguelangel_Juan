package planificacionSO;
import estructuraDeDatos.Cola;
import estructuraDeDatos.Criterio;
import losEnums.DireccionCabezal;
import sistemaOperativo.SolicitudES;
/**
 *
 * @author jmmor
 */
public class PoliticaSCAN implements PoliticaDisco{
     static boolean enDireccion(int destino, int cabezal, DireccionCabezal dir) {
        return (dir == DireccionCabezal.SUBE) ? destino >= cabezal : destino <= cabezal;
    }

    @Override
    public SolicitudES elegir(Cola colaES, int cabezalActual, DireccionCabezal dir, int maxBloques) {
        if (colaES.estaVacia()) return null;

        Criterio masCercanaEnDireccion = (a, b) -> {
            int da = ((SolicitudES) a).getBloqueDestino();
            int db = ((SolicitudES) b).getBloqueDestino();
            boolean aSirve = enDireccion(da, cabezalActual, dir);
            boolean bSirve = enDireccion(db, cabezalActual, dir);
            if (aSirve != bSirve) return aSirve;          // la que está en el camino gana
            return Math.abs(da - cabezalActual) < Math.abs(db - cabezalActual);
        };
        SolicitudES mejor = (SolicitudES) colaES.verMejor(masCercanaEnDireccion);
        if (mejor == null || !enDireccion(mejor.getBloqueDestino(), cabezalActual, dir)) return null;
        return mejor;
    }

    @Override
    public String getNombre() { return "SCAN"; }

    @Override
    public losEnums.TipoPoliticaDisco getTipo() { return losEnums.TipoPoliticaDisco.SCAN; }

    @Override
    public boolean esBarrido() { return true; }

    @Override
    public boolean esCircular() { return false; }

    @Override
    public int getSiguienteDestino(int cabezal, int maxBloques, DireccionCabezal dir) {
        // Si va subiendo, va al final. Si va bajando, va al 0
        return (dir == DireccionCabezal.SUBE) ? maxBloques - 1 : 0;
    }
}
    

