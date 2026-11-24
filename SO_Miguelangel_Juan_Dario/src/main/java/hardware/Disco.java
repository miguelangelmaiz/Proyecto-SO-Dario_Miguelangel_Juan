package hardware;

import sistemaOperativo.SolicitudES;
import estructuraDeDatos.Cola;
import planificacionSO.PoliticaFIFO;
import planificacionSO.PoliticaDisco;
import sistemaOperativo.Cluster;
import losEnums.DireccionCabezal;

/**
 *
 * @author jmmor
 */
public class Disco implements ComponenteReloj {
    
   private int cabezal = 50;
    private int posPorCiclo = 1; 
    private int totalBloques = 200; // Inicializado por defecto
    private final Cola colaES = new Cola();
    
    
    
    private DireccionCabezal direccion = DireccionCabezal.SUBE;
    private SolicitudES solicitudActual = null;
    private int bloquesRestantes = 0;
    public int desplazamientoTotal = 0;
    public int solicitudesAtendidas = 0;
    public double sumaTiemposEspera = 0;
    public StringBuilder ordenAtencion = new StringBuilder(); 
    
    
    private Cluster cluster;
    private PoliticaDisco politica = new PoliticaFIFO();
    
    public void setCluster(Cluster cluster) { this.cluster = cluster; }
    public void setPolitica(PoliticaDisco p) { this.politica = p; }
    public void setTotalBloques(int total) { this.totalBloques = total; }
    public void setCabezal(int cabezal) { this.cabezal = cabezal; }
    
    public int getDesplazamientoTotal() { return desplazamientoTotal; }
    public int getSolicitudesAtendidas() { return solicitudesAtendidas; }
    public double getSumaTiemposEspera() { return sumaTiemposEspera; }
    public int getCabezal() { return cabezal; }
    public int getTotalBloques() { return totalBloques; }
    public planificacionSO.PoliticaDisco getPolitica() { return politica; }


    public void encolarSolicitud(SolicitudES s) {
        colaES.encolar(s);
    }

        @Override
    public void tic(int ciclo) {
        // 1. SI ESTÁ TRANSFIRIENDO, SE QUEDA QUIETO
         if (bloquesRestantes > 0) {
            bloquesRestantes--;
            if (bloquesRestantes == 0) {
                solicitudesAtendidas++; // <--- MÉTRICA: Sumamos 1 solicitud terminada
                
                if (cluster != null) {
                    cluster.getComputador(0).getNucleo().alTerminarES(solicitudActual);
                }
                solicitudActual = null; 
            }
            return; 
        }

        // 2. SI NO TIENE SOLICITUD, PIDE UNA A LA COLA
       if (solicitudActual == null) {
            solicitudActual = politica.elegir(colaES, cabezal, direccion, totalBloques);
            
            if (solicitudActual != null) {
                colaES.eliminar(solicitudActual); 
                
                // <--- MÉTRICA: Calculamos cuánto esperó en la cola
                if (cluster != null) {
                    int cicloActual = cluster.getReloj().getCiclo();
                    sumaTiemposEspera += (cicloActual - solicitudActual.getCicloLlegada());
                }
                // --------------------------------------------------
                
            } else if (politica.esBarrido() && !colaES.estaVacia()) {
                // Si la política es de barrido (SCAN/CSCAN) y no hay solicitudes a la vista
                int destinoExtremo = politica.getSiguienteDestino(cabezal, totalBloques, direccion);
                
                if (cabezal != destinoExtremo) {
                    // Mueve el cabezal un paso hacia el extremo
                    if (cabezal < destinoExtremo) {
                        int mover = Math.min(posPorCiclo, destinoExtremo - cabezal);
                        cabezal += mover;
                        desplazamientoTotal += mover;
                    } else {
                        int mover = Math.min(posPorCiclo, cabezal - destinoExtremo);
                        cabezal -= mover;
                        desplazamientoTotal += mover;
                    }
                } else {
                    // Llegamos al extremo
                    if (politica.esCircular()) {
                        cabezal = 0; // Salto del C-SCAN
                    } else {
                        // Rebote del SCAN
                        direccion = (direccion == DireccionCabezal.SUBE) ? DireccionCabezal.BAJA : DireccionCabezal.SUBE;
                    }
                }
                return;
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
            direccion = DireccionCabezal.SUBE;
        } else if (cabezal > destino) {
            int mover = Math.min(posPorCiclo, cabezal - destino);
            cabezal -= mover;
            desplazamientoTotal += mover;
            direccion = DireccionCabezal.BAJA;
        }

        // 4. SI YA LLEGÓ, TRANSFIERE
        if (cabezal == destino) {
            bloquesRestantes = solicitudActual.getBloquesRequeridos();
            
            // NUEVO: Registrar el bloque en el orden de atención
            if (ordenAtencion.length() > 0) {
                ordenAtencion.append(", ");
            }
            ordenAtencion.append(destino);
        }
    }
    
     public boolean colaVacia() {
        return colaES.estaVacia();
    }
}

    
    
