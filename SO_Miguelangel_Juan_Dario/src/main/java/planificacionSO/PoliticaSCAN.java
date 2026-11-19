package planificacionSO;
import estructuraDeDatos.Cola;
import estructuraDeDatos.Criterio;
import sistemaOperativo.SolicitudES;
/**
 *
 * @author jmmor
 */
public class PoliticaSCAN implements PoliticaDisco{
      @Override
    public SolicitudES elegir(Cola colaES, int cabezalActual) {
        if (colaES.estaVacia()) return null;

        SolicitudES mejor = null;
        int distanciaMinima = Integer.MAX_VALUE;

        for (int i = 0; i < colaES.getTamano(); i++) {
            SolicitudES s = (SolicitudES) colaES.desencolar();
            // SCAN solo busca los que están ADELANTE (asumiendo que va subiendo)
            if (s.getBloqueDestino() >= cabezalActual) {
                int dist = s.getBloqueDestino() - cabezalActual;
                if (dist < distanciaMinima) {
                    distanciaMinima = dist;
                    mejor = s;
                }
            }
            colaES.encolar(s); // Lo devolvemos a la cola
        }
        
        // Si no hay nadie adelante, va al final del disco (199)
        if (mejor == null) return null; // (El Disco.java debe manejar el rebote)
        
        return mejor;
    }
    
     @Override
    public String getNombre() {
        return "SCAN";
    }
    
}
