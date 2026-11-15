
package sistemaOperativo;

import estructuraDeDatos.Cola;
import losEnums.EstadoProceso;
import losEnums.MotivoBloqueo;
import losEnums.TipoProceso;
import hardware.CPU;
import hardware.MemoriaPrincipal;
import planificacionSO.PoliticaPlanificacion;
/**
 *
 * @author Miguel
 */
public class Nucleo {
    
   private final int idComputador;
    private final CPU cpu;
    private final MemoriaPrincipal ram;
    private PoliticaPlanificacion politica;
    private final GestorBuffers gestorBuffers;
    private Cluster cluster;

    // Colas del Proyecto 1
    private final Cola colaNuevos;
    private final Cola colaListos;
    private final Cola colaBloqueados;
    private final Cola terminados;

    // NUEVAS COLAS DEL PROYECTO 2 (Suspensión)
    private final Cola colaListosSusp;
    private final Cola colaBloqueadosSusp;

    private int ciclosEnCpu = 0;

    public Nucleo(int idComputador, CPU cpu, MemoriaPrincipal ram,
                  PoliticaPlanificacion politica, GestorBuffers gestorBuffers) {
        this.idComputador = idComputador;
        this.cpu = cpu;
        this.ram = ram;
        this.politica = politica;
        this.gestorBuffers = gestorBuffers;
        
        this.colaNuevos = new Cola();
        this.colaListos = new Cola();
        this.colaBloqueados = new Cola();
        this.terminados = new Cola();
        
        this.colaListosSusp = new Cola();
        this.colaBloqueadosSusp = new Cola();
    }

    // ---------- Getters ----------
    public int getIdComputador() { return idComputador; }
    public Cola getColaNuevos()     { return colaNuevos; }
    public Cola getColaListos()     { return colaListos; }
    public Cola getColaBloqueados() { return colaBloqueados; }
    public Cola getTerminados()     { return terminados; }
    public Cola getColaListosSusp()     { return colaListosSusp; }
    public Cola getColaBloqueadosSusp() { return colaBloqueadosSusp; }

    public CPU getCpu() { return cpu; }
    public MemoriaPrincipal getRam() { return ram; }
    public PoliticaPlanificacion getPolitica() { return politica; }
    public void setPolitica(PoliticaPlanificacion politica) { this.politica = politica; }
    public GestorBuffers getGestorBuffers() { return gestorBuffers; }
    public Cluster getCluster() { return cluster; }
    public void setCluster(Cluster cluster) { this.cluster = cluster; }

    // ---------- API del sistema ----------
    public void admitir(PCB p) {
        p.setEstado(EstadoProceso.NUEVO);
        p.setCicloLlegada(cluster.getReloj().getCiclo());
        colaNuevos.encolar(p);
    }

    // ---------- EL TIC (Con enganches del P2) ----------
    public void tic(int ciclo) {
        vencerPlazos(ciclo);           // 1. + liberar swap si vence suspendido
        despertarBloqueados();         // 2. E/S de disco: sin contador
        admitirSiHayMemoria(ciclo);    // 3. + hacerEspacio si no cabe
        planificar(ciclo);             // 4. Igual que P1
        ejecutarInstruccion(ciclo);    // 5. + crear SolicitudES
        if (!cpu.estaLibre()) ciclosEnCpu++; else ciclosEnCpu = 0;
        cerrarCiclo(ciclo);            // 6. + reactivar si se liberó RAM
    }

    // ---------- Paso 1 ----------
    private void vencerPlazos(int ciclo) {
        vencerEnCola(colaNuevos, ciclo);
        vencerEnCola(colaListos, ciclo);
        vencerEnCola(colaBloqueados, ciclo);
        vencerEnCola(colaListosSusp, ciclo);
        vencerEnCola(colaBloqueadosSusp, ciclo);

        PCB enCpu = cpu.getProcesoActual();
        if (enCpu != null) {
            enCpu.setDeadline(enCpu.getDeadline() - 1);
            if (enCpu.getDeadline() <= 0) {
                cpu.liberar();
                ram.liberar(enCpu.getMemoria());
                enCpu.setEstado(EstadoProceso.TERMINADO);
                enCpu.setCicloFin(ciclo);
                terminados.encolar(enCpu);
            }
        }
    }

    private void vencerEnCola(Cola c, int ciclo) {
        int n = c.getTamano();
        for (int i = 0; i < n; i++) {
            PCB p = (PCB) c.desencolar();
            p.setDeadline(p.getDeadline() - 1);

            if (p.getDeadline() <= 0) {
                if (!p.isEnSwap() && (p.getEstado() == EstadoProceso.LISTO || p.getEstado() == EstadoProceso.BLOQUEADO)) {
                    ram.liberar(p.getMemoria());
                } else if (p.isEnSwap()) {
                    // TODO: Liberar los bloques de swap en el disco (lo hará el Disco)
                }
                p.setEstado(EstadoProceso.TERMINADO);
                p.setCicloFin(ciclo);
                terminados.encolar(p);
            } else {
                c.encolar(p);
            }
        }
    }

    // ---------- Paso 2 ----------
    private void despertarBloqueados() {
        int n = colaBloqueados.getTamano();
        for (int i = 0; i < n; i++) {
            PCB p = (PCB) colaBloqueados.desencolar();
            p.setCiclosBloqueado(p.getCiclosBloqueado() + 1);

            // NOTA: La E/S de disco ya no usa ciclosParaSatisfacer.
            // Ahora el disco debe llamar a alTerminarES() para despertarlo.
            if (p.getMotivoBloqueo() != MotivoBloqueo.E_S) {
                colaBloqueados.encolar(p);
            }
            // Si es E/S, se queda aquí hasta que el Disco lo despierte.
        }
    }

    // ---------- Paso 3 ----------
    private void admitirSiHayMemoria(int ciclo) {
        PCB p = (PCB) colaNuevos.verFrente();
        while (p != null) {
            if (ram.hayEspacio(p.getMemoria())) {
                colaNuevos.desencolar();
                ram.asignar(p.getMemoria());
                p.setEstado(EstadoProceso.LISTO);
                colaListos.encolar(p);
            } else {
                // Si no cabe, intenta hacer espacio suspendiendo a alguien
                if (!hacerEspacio(p, ciclo)) {
                    break; // No se pudo hacer espacio, se detiene la admisión
                }
            }
            p = (PCB) colaNuevos.verFrente();
        }
    }

    // ---------- Planificador a Mediano Plazo ----------
    private boolean hacerEspacio(PCB nuevoProceso, int ciclo) {
        // Criterio de víctima: el primer proceso Bloqueado que encontremos.
        // (Pueden mejorarlo eligiendo el de menor prioridad o más tiempo bloqueado)
        if (colaBloqueados.estaVacia()) return false; 

        PCB victima = (PCB) colaBloqueados.desencolar();
        victima.setEstado(EstadoProceso.BLOQUEADO_SUSPENDIDO);
        victima.setEnSwap(true);
        
        // Creamos la solicitud de suspensión (el disco la procesará)
        // NOTA: Bloqueamos la RAM hasta que el disco termine de escribir.
        cluster.registrarEvento("Ciclo " + ciclo + ": Solicitando suspender " + victima.getNombre() + " para admitir a " + nuevoProceso.getNombre());
        
        // TODO: Enviar SolicitudES(SUSPENDER) al Disco. 
        // Por ahora, simulamos que ya se suspendió (para que el código compile):
        ram.liberar(victima.getMemoria());
        colaBloqueadosSusp.encolar(victima);
        
        return true;
    }

    private void reactivar(int ciclo) {
        // Revisa si algún Listo/Suspendido puede volver a la RAM
        int n = colaListosSusp.getTamano();
        for (int i = 0; i < n; i++) {
            PCB p = (PCB) colaListosSusp.desencolar();
            if (ram.hayEspacio(p.getMemoria())) {
                // TODO: Enviar SolicitudES(REACTIVAR) al Disco.
                // Por ahora simulamos que ya se leyó:
                ram.asignar(p.getMemoria());
                p.setEstado(EstadoProceso.LISTO);
                p.setEnSwap(false);
                colaListos.encolar(p);
                cluster.registrarEvento("Ciclo " + ciclo + ": Proceso " + p.getNombre() + " reactivado.");
            } else {
                colaListosSusp.encolar(p);
            }
        }
    }

    // Método que llamará el Disco cuando termine una operación
    public void alTerminarES(SolicitudES s) {
        PCB p = s.getPcb();
        // Dependiendo de la operación, se aplica en el SistemaArchivos (lo hará Dario)
        
        // Despertamos al proceso
        colaBloqueados.eliminar(p);
        p.setEstado(EstadoProceso.LISTO);
        p.setMotivoBloqueo(null);
        colaListos.encolar(p);
    }

    // ---------- Paso 4 ----------
    private void planificar(int ciclo) {
        if (!cpu.estaLibre()) {
            PCB enCpu = cpu.getProcesoActual();
            boolean desalojar = false;

            if (politica.getQuantum() > 0 && ciclosEnCpu >= politica.getQuantum()) {
                desalojar = true;
            } else if (politica.esApropiativa() && !colaListos.estaVacia()) {
                PCB candidato = politica.seleccionar(colaListos);
                if (candidato != null && politica.esMejor(candidato, enCpu)) {
                    desalojar = true;
                }
            }

            if (desalojar) {
                cpu.liberar();
                enCpu.setEstado(EstadoProceso.LISTO);
                colaListos.encolar(enCpu);
                ciclosEnCpu = 0;
            }
        }

        if (cpu.estaLibre() && !colaListos.estaVacia()) {
            PCB elegido = politica.seleccionar(colaListos);
            if (elegido != null) {
                colaListos.eliminar(elegido);
                if (elegido.getCicloInicio() < 0) {
                    elegido.setCicloInicio(ciclo);
                }
                cpu.cargar(elegido);
                ciclosEnCpu = 0;
            }
        }
    }

    // ---------- Paso 5 ----------
    private void ejecutarInstruccion(int ciclo) {
        PCB p = cpu.getProcesoActual();
        if (p == null) return;
        
        cpu.setCiclosOcupados(cpu.getCiclosOcupados() + 1);

        if (p.getTipo() == TipoProceso.IO_BOUND) {
            ejecutarIOBound(p, ciclo);
        } else {
            p.ejecutarInstruccion();
        }
    }

    private void ejecutarIOBound(PCB p, int ciclo) {
        // Si no tiene una operación asignada, avanza normal
        if (p.getOperacion() == null) {
            p.ejecutarInstruccion();
            return;
        }

        // Genera la solicitud al disco
        int bloqueFalso = 50; // Esto lo dará el SistemaArchivos de Darío luego
        SolicitudES solicitud = new SolicitudES(p, p.getOperacion(), bloqueFalso, 1, ciclo);
        
        // Bloquea el proceso
        p.setEstado(EstadoProceso.BLOQUEADO);
        p.setMotivoBloqueo(MotivoBloqueo.E_S);
        cpu.liberar();
        colaBloqueados.encolar(p);
        
        // Enviar al disco
        // cluster.getDisco().encolarSolicitud(solicitud);
        cluster.registrarEvento("Ciclo " + ciclo + ": " + p.getNombre() + " solicitó E/S de disco.");
    }

    // ---------- Paso 6 ----------
    private void cerrarCiclo(int ciclo) {
        PCB p = cpu.getProcesoActual();
        if (p == null) return;

        if (p.estaTerminado()) {
            p.setCicloFin(ciclo);
            cpu.liberar();
            ram.liberar(p.getMemoria());
            terminados.encolar(p);
            admitirSiHayMemoria(ciclo); // Barrido obligatorio
            reactivar(ciclo);            // Tratar de traer suspendidos
        }
    }
    
}
