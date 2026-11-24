package planificacionSO;

import estructuraDeDatos.Cola;
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
        
        SolicitudES mejor = null;
        int distMin = Integer.MAX_VALUE;
        
        for (int i = 0; i < colaES.getTamano(); i++) {
            SolicitudES s = (SolicitudES) colaES.desencolar();
            int destino = s.getBloqueDestino();
            
            if (destino >= cabezalActual) {
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
    public String getNombre() { return "C-SCAN"; }

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
