package hardware;

import sistemaOperativo.SolicitudES;
import estructuraDeDatos.Cola;
import planificacionSO.PoliticaFIFO;
import planificacionSO.PoliticaDisco;
import sistemaOperativo.Cluster;
import losEnums.DireccionCabezal;
import losEnums.TipoPoliticaDisco;

/**
 *
 * @author jmmor
 */
public class Disco implements ComponenteReloj {
    
   private volatile int cabezal = 50;
    private volatile int posPorCiclo = 5;
    private volatile int ciclosArranque = 10;   // configurable desde la interfaz
    private volatile boolean arrancado = false;
    private int arranqueRestante = -1;
    private volatile int totalBloques = 200; // Inicializado por defecto
    private final Cola colaES = new Cola();

    private volatile DireccionCabezal direccion = DireccionCabezal.SUBE;
    private volatile SolicitudES objetivo = null;        // hacia dónde va el cabezal (sigue en la cola)
    private volatile SolicitudES solicitudActual = null; // la que se está leyendo/escribiendo
    private int bloquesRestantes = 0;

    public int desplazamientoTotal = 0;
    public int solicitudesAtendidas = 0;
    public double sumaTiemposEspera = 0;
    private final int[] atendidasPorPolitica = new int[TipoPoliticaDisco.values().length];
    public StringBuilder ordenAtencion = new StringBuilder();

    private Cluster cluster;
    private volatile PoliticaDisco politica = new PoliticaFIFO();

    public void setCluster(Cluster cluster) { this.cluster = cluster; }
    public void setPolitica(PoliticaDisco p) { if (p != null) this.politica = p; }
    public void setTotalBloques(int total) {
        if (total <= 0) return;
        this.totalBloques = total;
        if (cabezal >= total) cabezal = total - 1;
    }
    public void setCabezal(int cabezal) {
        if (cabezal < 0) cabezal = 0;
        if (cabezal >= totalBloques) cabezal = totalBloques - 1;
        this.cabezal = cabezal;
    }
    public void setPosPorCiclo(int p) { if (p >= 1) this.posPorCiclo = p; }
    public void setCiclosArranque(int c) { if (c >= 0) this.ciclosArranque = c; }
    public int getCiclosArranque() { return ciclosArranque; }
    public boolean isArrancado() { return arrancado; }

    /** Al cargar un JSON nuevo el disco vuelve a estar "apagado": arrancará con la primera solicitud. */
    public void reiniciarArranque() {
        arrancado = false;
        arranqueRestante = -1;
    }

    public int getDesplazamientoTotal() { return desplazamientoTotal; }
    public int getSolicitudesAtendidas() { return solicitudesAtendidas; }
    public double getSumaTiemposEspera() { return sumaTiemposEspera; }
    public double getEsperaPromedio() {
        return solicitudesAtendidas > 0 ? sumaTiemposEspera / solicitudesAtendidas : 0.0;
    }
    public int getAtendidasPor(TipoPoliticaDisco t) { return atendidasPorPolitica[t.ordinal()]; }
    public int getCabezal() { return cabezal; }
    public int getTotalBloques() { return totalBloques; }
    public int getPosPorCiclo() { return posPorCiclo; }
    public DireccionCabezal getDireccion() { return direccion; }
    public PoliticaDisco getPolitica() { return politica; }
    public SolicitudES getObjetivo() { return objetivo; }
    public SolicitudES getSolicitudActual() { return solicitudActual; }
    public int getTamanoCola() { return colaES.getTamano(); }

    /** Texto de la cola de E/S para la interfaz: "[P3 LEER→b95, ...]". */
    public String getResumenCola() { return colaES.getNombresProcesos(); }

    public void encolarSolicitud(SolicitudES s) {
        colaES.encolar(s);
    }

    @Override
    public void tic(int ciclo) {
        // 1. SI ESTÁ TRANSFIRIENDO, EL CABEZAL SE QUEDA QUIETO (cada bloque cuesta un ciclo)
        if (solicitudActual != null) {
            bloquesRestantes--;
            if (bloquesRestantes <= 0) {
                SolicitudES terminada = solicitudActual;
                solicitudActual = null;
                solicitudesAtendidas++;
                if (cluster != null) {
                    cluster.getComputador(0).getNucleo().alTerminarES(terminada);
                }
            }
            return;
        }

        if (colaES.estaVacia()) {
            objetivo = null;
            return;
        }

        // ARRANQUE DEL MOTOR: la primera vez que llega trabajo, el disco tarda ciclosArranque ciclos
        if (!arrancado) {
            if (arranqueRestante < 0) {
                arranqueRestante = ciclosArranque;
                registrar(ciclo, "El disco arranca el motor (" + ciclosArranque + " ciclos); las solicitudes esperan en la cola.");
            }
            if (arranqueRestante > 0) {
                arranqueRestante--;
                return;
            }
            arrancado = true;
            registrar(ciclo, "Disco listo. Solicitudes en cola: " + colaES.getNombresProcesos());
        }

        // 2. EN CADA CICLO LA POLÍTICA VUELVE A ESCOGER EL OBJETIVO
        objetivo = politica.elegir(colaES, cabezal, direccion, totalBloques);

        if (objetivo == null) {
            // Política de barrido (SCAN / C-SCAN) sin solicitudes en su dirección:
            // el cabezal sigue hasta el extremo del disco.
            if (politica.esBarrido()) {
                int destinoExtremo = politica.getSiguienteDestino(cabezal, totalBloques, direccion);
                if (cabezal != destinoExtremo) {
                    mover(destinoExtremo);
                } else if (politica.esCircular()) {
                    cabezal = 0; // Salto del C-SCAN: el regreso no cuenta como desplazamiento
                    registrar(ciclo, "El cabezal llegó al bloque " + (totalBloques - 1)
                            + " y salta al bloque 0 (" + politica.getNombre() + ")");
                } else {
                    direccion = (direccion == DireccionCabezal.SUBE) ? DireccionCabezal.BAJA : DireccionCabezal.SUBE;
                    registrar(ciclo, "El cabezal llegó al extremo y cambia de dirección a "
                            + direccion + " (" + politica.getNombre() + ")");
                }
            }
            return;
        }

        // 3. MOVER EL CABEZAL HACIA EL OBJETIVO
        int destino = objetivo.getBloqueDestino();
        if (cabezal != destino) {
            mover(destino);
        }

        // 4. SI YA LLEGÓ: LA SOLICITUD SALE DE LA COLA Y EMPIEZA LA TRANSFERENCIA
        if (cabezal == destino) {
            SolicitudES s = objetivo;
            colaES.eliminar(s);
            objetivo = null;
            solicitudActual = s;
            bloquesRestantes = Math.max(1, s.getBloquesRequeridos());
            sumaTiemposEspera += (ciclo - s.getCicloLlegada());
            atendidasPorPolitica[politica.getTipo().ordinal()]++;

            if (ordenAtencion.length() > 0) ordenAtencion.append(", ");
            ordenAtencion.append(destino);

            registrar(ciclo, "El disco atiende la solicitud de " + s.getNombreProceso()
                    + " (" + s.getOperacion() + ") en el bloque " + destino
                    + " (" + politica.getNombre() + ")");
        }
    }

    /** Mueve el cabezal hasta posPorCiclo posiciones hacia el destino y suma el desplazamiento. */
    private void mover(int destino) {
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
    }

    private void registrar(int ciclo, String texto) {
        if (cluster != null) cluster.registrarEvento("Ciclo " + ciclo + ": " + texto);
    }

    public boolean colaVacia() {
        return colaES.estaVacia();
    }
}

    
    
