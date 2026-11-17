package planificacionSO;

/**
 *
 * @author jmmor
 */

import estructuraDeDatos.Cola;
import estructuraDeDatos.Criterio;
import sistemaOperativo.SolicitudES;

public class PoliticaSSTF implements PoliticaDisco {
     @Override
    public SolicitudES elegir(Cola colaES, int cabezalActual) {
        // Usamos el método verMejor() de tu Cola del Proyecto 1
        Criterio masCercano = (a, b) -> {
            SolicitudES solA = (SolicitudES) a;
            SolicitudES solB = (SolicitudES) b;
            
            // Calculamos la distancia de cada solicitud al cabezal
            int distA = Math.abs(solA.getBloqueDestino() - cabezalActual);
            int distB = Math.abs(solB.getBloqueDestino() - cabezalActual);
            
            // Es mejor (true) si está más cerca
            return distA < distB;
        };
        
        // Le pedimos a la cola que nos devuelva la solicitud más cercana
        return (SolicitudES) colaES.verMejor(masCercano);
    }
}
