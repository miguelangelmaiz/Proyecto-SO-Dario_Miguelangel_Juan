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

        // Busca el más cercano que esté por delante (o igual)
        for (int i = 0; i < colaES.getTamano(); i++) {
            SolicitudES s = (SolicitudES) colaES.desencolar();
            if (s.getBloqueDestino() >= cabezalActual) {
                int dist = s.getBloqueDestino() - cabezalActual;
                if (dist < distanciaMinima) {
                    distanciaMinima = dist;
                    mejor = s;
                }
            }
            colaES.encolar(s);
        }
        
        // Si no hay nada adelante, devolvemos null para que el disco vaya al final
        return mejor; 
    }
    
    @Override
    public String getNombre() { 
        return "SCAN"; }
    
}
