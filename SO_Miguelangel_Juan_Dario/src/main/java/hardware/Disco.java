package hardware;

import sistemaOperativo.SolicitudES;
import estructuraDeDatos.Cola;
import planificacionSO.PoliticaFIFO;

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

    public void encolarSolicitud(SolicitudES s) {
        colaES.encolar(s);
    }

   @Override
     public void tic(int ciclo) {
        // 1. SI ESTÁ TRANSFIRIENDO, SE QUEDA QUIETO
        if (bloquesRestantes > 0) {
            bloquesRestantes--;
            if (bloquesRestantes == 0) {
                System.out.println("   -> Terminó transferencia en bloque " + cabezal);
                solicitudActual = null; // Queda libre para la siguiente
            }
            return; // IMPORTANTE: No se mueve en este ciclo, ya está en el destino
        }

        // 2. SI NO TIENE SOLICITUD, PIDE UNA A LA COLA
        if (solicitudActual == null) {
            solicitudActual = (SolicitudES) colaES.verFrente();
            if (solicitudActual != null) {
                colaES.desencolar();
                System.out.println("   -> Atendiendo solicitud destino: " + solicitudActual.getBloqueDestino());
            } else {
                return; // Disco ocioso, no hay nada que hacer
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

        // 4. SI YA LLEGÓ AL DESTINO, EMPIEZA A TRANSFERIR (en el siguiente ciclo)
        if (cabezal == destino) {
            bloquesRestantes = solicitudActual.getBloquesRequeridos();
            System.out.println("   -> Llegó al bloque " + destino + ". Iniciando transferencia.");
        }
    }
    

    public int getCabezal() { return cabezal; }
    
}
