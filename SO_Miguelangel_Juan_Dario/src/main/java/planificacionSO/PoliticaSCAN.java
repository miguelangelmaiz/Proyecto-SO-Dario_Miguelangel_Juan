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

        // Buscamos la solicitud más cercana EN LA DIRECCIÓN ACTUAL
        Criterio masCercanoArriba = (a, b) -> {
            SolicitudES sa = (SolicitudES) a;
            SolicitudES sb = (SolicitudES) b;
            return sa.getBloqueDestino() < sb.getBloqueDestino();
        };

        // (Lógica simplificada para el proyecto: si hay alguna arriba, vamos a la más cercana.
        // Si no, a la más alta)
        // Nota: Aquí puedes usar el criterio de verMejor o iterar.
        return null; // (Completa esto usando la lógica del PDF)
    }
    
}
