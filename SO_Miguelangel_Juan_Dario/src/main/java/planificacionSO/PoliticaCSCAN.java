package planificacionSO;

import estructuraDeDatos.Cola;
import sistemaOperativo.SolicitudES;
/**
 *
 * @author jmmor
 */
public class PoliticaCSCAN implements PoliticaDisco{
    
      @Override
    public SolicitudES elegir(Cola colaES, int cabezalActual) {
        if (colaES.estaVacia()) return null;
        
        SolicitudES mejor = null;
        int distanciaMinima = Integer.MAX_VALUE;

        for (int i = 0; i < colaES.getTamano(); i++) {
            SolicitudES s = (SolicitudES) colaES.desencolar();
            // C-SCAN siempre barre de izquierda a derecha
            if (s.getBloqueDestino() >= cabezalActual) {
                int dist = s.getBloqueDestino() - cabezalActual;
                if (dist < distanciaMinima) {
                    distanciaMinima = dist;
                    mejor = s;
                }
            }
            colaES.encolar(s);
        }
        return mejor;
    }
    
     @Override
    public String getNombre() {
        return "CSCAN";
    }
    
}
