package planificacionSO;
import estructuraDeDatos.Cola;
import losEnums.DireccionCabezal;
import sistemaOperativo.SolicitudES;
/**
 *
 * @author jmmor
 */
public class PoliticaSCAN implements PoliticaDisco{
     @Override
    public SolicitudES elegir(Cola colaES, int cabezalActual, DireccionCabezal dir, int maxBloques) {
        if (colaES.estaVacia()) return null;
        
        SolicitudES mejor = null;
        int distMin = Integer.MAX_VALUE;
        
        for (int i = 0; i < colaES.getTamano(); i++) {
            SolicitudES s = (SolicitudES) colaES.desencolar();
            int destino = s.getBloqueDestino();
            
            if ((dir == DireccionCabezal.SUBE && destino >= cabezalActual) ||
                (dir == DireccionCabezal.BAJA && destino <= cabezalActual)) {
                
                int dist = Math.abs(destino - cabezalActual);
                if (dist < distMin) {
                    distMin = dist;
                    mejor = s;
                }
            }
            colaES.encolar(s);
        }
        return mejor; 
    }

    @Override
    public String getNombre() { return "SCAN"; }

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
    

