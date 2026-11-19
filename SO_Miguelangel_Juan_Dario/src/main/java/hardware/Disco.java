package hardware;

import sistemaOperativo.SolicitudES;
import estructuraDeDatos.Cola;
import planificacionSO.PoliticaFIFO;
import planificacionSO.PoliticaDisco;
import sistemaOperativo.Cluster;

/**
 *
 * @author jmmor
 */
public class Disco implements ComponenteReloj {
    
   private int cabezal = 50;
    private int posPorCiclo = 5; 
    private final Cola colaES = new Cola();
    
    private SolicitudES solicitudActual = null;
    private int bloquesRestantes = 0;
    public int desplazamientoTotal = 0;
    
    private Cluster cluster;
    private PoliticaDisco politica = new PoliticaFIFO(); // Por defecto FIFO

    public void setCluster(Cluster cluster) { this.cluster = cluster; }
    public void setPolitica(PoliticaDisco p) { this.politica = p; }
    
    public int getDesplazamientoTotal() {
        return desplazamientoTotal;
    }

    public void encolarSolicitud(SolicitudES s) {
        colaES.encolar(s);
    }

    @Override
    public void tic(int ciclo) {
        // 1. SI ESTÁ TRANSFIRIENDO, SE QUEDA QUIETO
        if (bloquesRestantes > 0) {
            bloquesRestantes--;
            if (bloquesRestantes == 0) {
                // AQUÍ AVISAMOS AL NÚCLEO QUE TERMINÓ
                if (cluster != null) {
                    cluster.getComputador(0).getNucleo().alTerminarES(solicitudActual);
                }
                solicitudActual = null; 
            }
            return; 
        }

        
        // 2. SI NO TIENE SOLICITUD, PIDE UNA A LA COLA USANDO LA POLÍTICA
        if (solicitudActual == null) {
            solicitudActual = politica.elegir(colaES, cabezal);
            if (solicitudActual != null) {
                colaES.eliminar(solicitudActual); // Lo sacamos de la cola
                System.out.println("   -> Disco atiende solicitud destino: " + solicitudActual.getBloqueDestino());
            } else {
                return; 
            }
        }

        // 3. TIENE SOLICITUD: MOVER EL CABEZAL HACIA EL DESTINO
        int destino = solicitudActual.getBloqueDestino();
        if (cabezal < destino) {
            int mover = Math.min(posPorCiclo, destino - cabezal);
            cabezal += mover;
            desplazamientoTotal += mover;
        } else if (cabezal > destino) {
            int mover = Math.min(posPorCiclo, cabezal - destino);
            cabezal -= mover;
            desplazamientoTotal += mover;
        }

        // 4. SI YA LLEGÓ AL DESTINO, EMPIEZA A TRANSFERIR
        if (cabezal == destino) {
            bloquesRestantes = solicitudActual.getBloquesRequeridos();
        }
    }
    
    public planificacionSO.PoliticaDisco getPolitica() {
        return this.politica;
    }
    
    public int getCabezal() { return cabezal; }
    
}
