package planificacionSO;
import estructuraDeDatos.Cola;
import losEnums.DireccionCabezal;
import sistemaOperativo.SolicitudES;
/**
 *
 * @author jmmor
 */
public class PoliticaSCAN implements PoliticaDisco{
    public SolicitudES elegir(Cola colaES, int cabezalActual, DireccionCabezal dir, int maxBloques) {
        if (colaES.estaVacia()) return null;
        
        SolicitudES mejor = null;
        int distMin = Integer.MAX_VALUE;
        
        for (int i = 0; i < colaES.getTamano(); i++) {
            SolicitudES s = (SolicitudES) colaES.desencolar();
            int destino = s.getBloqueDestino();
            
            // Si va subiendo, busca los mayores o iguales. Si baja, los menores o iguales.
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
        
        // Si no hay nada en esa dirección, devuelve null para que el disco vaya al extremo
        return mejor; 
    }
    
    @Override
    public String getNombre() { return "SCAN"; }
}
    

