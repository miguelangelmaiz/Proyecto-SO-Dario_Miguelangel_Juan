
package sistemaOperativo;

import estructuraDeDatos.Cola;
import estructuraDeDatos.Criterio;
import losEnums.EstadoProceso;
import losEnums.MotivoBloqueo;
import losEnums.TipoProceso;
import losEnums.TipoOperacionES;
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
    private Cluster cluster;

    private final Cola colaNuevos;
    private final Cola colaListos;
    private final Cola colaBloqueados;
    private final Cola terminados;

    private final Cola colaListosSusp;
    private final Cola colaBloqueadosSusp;

    private int ciclosEnCpu = 0;

    public Nucleo(int idComputador, CPU cpu, MemoriaPrincipal ram,
                  PoliticaPlanificacion politica) {
        this.idComputador = idComputador;
        this.cpu = cpu;
        this.ram = ram;
        this.politica = politica;
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

    public Cluster getCluster() { return cluster; }
    public void setCluster(Cluster cluster) { this.cluster = cluster; }

    // ---------- Admitir ----------
    public void admitir(PCB p) {
        p.setEstado(EstadoProceso.NUEVO);
        p.setCicloLlegada(cluster.getReloj().getCiclo());
        colaNuevos.encolar(p);
        cluster.registrarEvento("Ciclo " + cluster.getReloj().getCiclo() + 
                ": Proceso " + p.getNombre() + " creado y admitido en cola NUEVOS.");
    }

    public Resultado crearProcesoES(String nombre, TipoProceso tipo, int prioridad,
                                    int memoria, int deadline, int restante,
                                    TipoOperacionES operacion, String ruta,
                                    int bloques, String dueno) {
        if (nombre == null || nombre.trim().isEmpty())
            return Resultado.error("El nombre no puede estar vacío.");
        if (memoria <= 0 || deadline <= 0 || restante <= 0)
            return Resultado.error("Memoria, deadline y restante deben ser > 0.");
        if (memoria > ram.getTotal())
            return Resultado.error("RAM insuficiente: pide " + memoria
                    + " / total " + ram.getTotal() + ".");
        if (operacion == null)
            return Resultado.error("Debe indicar una operación de E/S.");
        if (ruta == null || ruta.trim().isEmpty())
            return Resultado.error("Debe indicar la ruta del archivo.");

        PCB p = new PCB(nombre, tipo, prioridad, memoria, deadline, restante, idComputador);
        p.setOperacion(operacion);
        p.setRutaArchivo(ruta);
        p.setBloques(bloques);
        p.setDueno(dueno);
        admitir(p);
        return Resultado.ok(p);
    }

    // ---------- Ciclo ----------
    public void tic(int ciclo) {
        vencerPlazos(ciclo);
        despertarBloqueados();
        admitirSiHayMemoria(ciclo);
        planificar(ciclo);
        ejecutarInstruccion(ciclo);
        if (!cpu.estaLibre()) ciclosEnCpu++; else ciclosEnCpu = 0;
        cerrarCiclo(ciclo);
    }

    // ---------- Paso 1: vencer plazos ----------
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
                cluster.registrarEvento("Ciclo " + ciclo + ": Proceso " + 
                        enCpu.getNombre() + " terminó por DEADLINE en CPU.");
            }
        }
    }

    private void vencerEnCola(Cola c, int ciclo) {
        int n = c.getTamano();
        for (int i = 0; i < n; i++) {
            PCB p = (PCB) c.desencolar();
            p.setDeadline(p.getDeadline() - 1);

            if (p.getDeadline() <= 0) {
                if (!p.isEnSwap() && (p.getEstado() == EstadoProceso.LISTO
                        || p.getEstado() == EstadoProceso.BLOQUEADO)) {
                    ram.liberar(p.getMemoria());
                } else if (p.isEnSwap()) {
                    // TODO (Juan/Darío): liberar bloques de swap en el Disco
                }
                p.setEstado(EstadoProceso.TERMINADO);
                p.setCicloFin(ciclo);
                terminados.encolar(p);
                cluster.registrarEvento("Ciclo " + ciclo + ": Proceso " + 
                        p.getNombre() + " terminó por DEADLINE en cola.");
            } else {
                c.encolar(p);
            }
        }
    }

    // ---------- Paso 2: despertar bloqueados ----------
    private void despertarBloqueados() {
        int n = colaBloqueados.getTamano();
        for (int i = 0; i < n; i++) {
            PCB p = (PCB) colaBloqueados.desencolar();
            // Los bloqueos por E/S esperan al Disco, no cuentan ciclos aquí.
            if (p.getMotivoBloqueo() != MotivoBloqueo.E_S) {
                p.setCiclosBloqueado(p.getCiclosBloqueado() + 1);
            }
            colaBloqueados.encolar(p);   // SIEMPRE vuelve a la cola
        }
    }

    // ---------- Paso 3: admisión + planificador a mediano plazo ----------
    private void admitirSiHayMemoria(int ciclo) {
        PCB p = (PCB) colaNuevos.verFrente();
        if (p == null) return;

        if (ram.hayEspacio(p.getMemoria())) {
            colaNuevos.desencolar();
            ram.asignar(p.getMemoria());
            p.setEstado(EstadoProceso.LISTO);
            colaListos.encolar(p);
        } else {
            hacerEspacio(p, ciclo); 
        }
    }

    private boolean hacerEspacio(PCB nuevoProceso, int ciclo) {
        if (colaBloqueados.estaVacia()) return false;

        Criterio masTiempoBloqueado = (a, b) ->
                ((PCB) a).getCiclosBloqueado() > ((PCB) b).getCiclosBloqueado();

        PCB victima = (PCB) colaBloqueados.verMejor(masTiempoBloqueado);
        if (victima == null) return false;
        colaBloqueados.eliminar(victima);

        victima.setEnSwap(true); // CLAVE: Lo marcamos para que LEER sepa que está en trámite

        cluster.registrarEvento("Ciclo " + ciclo + ": Solicitando suspensión de "
                + victima.getNombre() + " para admitir a " + nuevoProceso.getNombre());

        SolicitudES solicitud = new SolicitudES(victima, TipoOperacionES.SUSPENDER, -1, victima.getMemoria(), ciclo);
        cluster.getDisco().encolarSolicitud(solicitud);
        
        return false; 
    }

    private void reactivar(int ciclo) {
        int n = colaListosSusp.getTamano();
        for (int i = 0; i < n; i++) {
            PCB p = (PCB) colaListosSusp.desencolar();
            
            if (ram.hayEspacio(p.getMemoria())) {
                // ENVIAMOS LA SOLICITUD AL DISCO REAL
                SolicitudES solicitud = new SolicitudES(p, TipoOperacionES.REACTIVAR, -1, p.getMemoria(), ciclo);
                cluster.getDisco().encolarSolicitud(solicitud);
                
                cluster.registrarEvento("Ciclo " + ciclo + ": Solicitando reactivación de " + p.getNombre());
            } else {
                colaListosSusp.encolar(p); // Se queda ahí si no hay RAM
            }
        }
    }

    public void despertar(PCB p) {
        p.setEstado(EstadoProceso.LISTO);
        p.setMotivoBloqueo(null);
        colaBloqueados.eliminar(p);
        colaListos.encolar(p);
        if (cluster != null) {
            cluster.registrarEvento("Ciclo " + cluster.getReloj().getCiclo() + 
                ": Proceso " + p.getNombre() + " DESPERTÓ y pasó a Listos.");
        }
    }
    
    /**
     * Llamado por el Disco (Juan/Darío) al terminar una SolicitudES.
     * Distingue si el proceso estaba o no suspendido.
     */
        public void alTerminarES(SolicitudES s) {
        PCB p = s.getPcb();
        
        if (s.getOperacion() == TipoOperacionES.SUSPENDER) {
            ram.liberar(p.getMemoria());
            if (p.getEstado() != EstadoProceso.LISTO_SUSPENDIDO) {
                p.setEstado(EstadoProceso.BLOQUEADO_SUSPENDIDO);
                colaBloqueadosSusp.encolar(p);
                cluster.registrarEvento("Ciclo " + cluster.getReloj().getCiclo() + ": " + p.getNombre() + " pasó a BLOQ/SUSP (Esperando E/S).");
            }
            cluster.registrarEvento("Ciclo " + cluster.getReloj().getCiclo() + ": " + p.getNombre() + " suspendido en Disco (Swap).");
            
        } else if (s.getOperacion() == TipoOperacionES.REACTIVAR) {
            ram.asignar(p.getMemoria()); 
            p.setEstado(EstadoProceso.LISTO);
            p.setEnSwap(false);
            colaListos.encolar(p);
            cluster.registrarEvento("Ciclo " + cluster.getReloj().getCiclo() + ": " + p.getNombre() + " reactivado a RAM.");
            
        } else {
            // LEER / ESCRIBIR / CREAR / ELIMINAR
            if (cluster.getSistemaArchivos() != null) {
                String error = cluster.getSistemaArchivos().aplicarCRUD(
                        s.getOperacion(), p.getRutaArchivo(), p.getDueno(), true, p.getBloques());
                
                if (error != null) {
                    cluster.registrarEvento("Error de FS: " + error);
                } else {
                    cluster.registrarEvento("Ciclo " + cluster.getReloj().getCiclo() + ": " + p.getNombre() + " ejecutó " + s.getOperacion() + " en " + p.getRutaArchivo());
                }
            }

            if (p.isEnSwap()) {
                colaBloqueadosSusp.eliminar(p); 
                p.setEstado(EstadoProceso.LISTO_SUSPENDIDO);
                p.setMotivoBloqueo(null);
                colaListosSusp.encolar(p);
                cluster.registrarEvento("Ciclo " + cluster.getReloj().getCiclo() + ": " + p.getNombre() + " terminó E/S estando suspendido. Pasa a Listo/Susp.");
            } else {
                despertar(p); 
            }
        } 
    }  
     
    // ---------- Paso 4: planificación CPU ----------
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

    // ---------- Paso 5: ejecución ----------
    private void ejecutarInstruccion(int ciclo) {
        PCB p = cpu.getProcesoActual();
        if (p == null) return;

        if (p.getTipo() == TipoProceso.IO_BOUND) {
            ejecutarIOBound(p, ciclo);
        } else {
            p.ejecutarInstruccion();
        }
    }

          private void ejecutarIOBound(PCB p, int ciclo) {
        if (p.getOperacion() == null) {
            p.ejecutarInstruccion();
            return;
        }

        p.ejecutarInstruccion(); // Consume 1 instrucción al pedir E/S
        
        if (p.estaTerminado()) {
            return;
        }

        cpu.registrarOperacionSistema();
        cpu.entrarModoSistema();
        try {
            // TRUCO TEMPORAL PARA PROBAR EL DISCO: 
            // Usamos los bloques del anexo del PDF para que el cabezal se mueva
            int[] bloquesPrueba = {82, 15, 140, 43, 190, 24, 16};
            int bloqueReal = bloquesPrueba[p.getId() % bloquesPrueba.length];

            // (Cuando Darío arregle su código, cambiarás esto por:)
            // int bloqueReal = cluster.getSistemaArchivos().primerBloqueReal(p.getRutaArchivo());
            // if (bloqueReal < 0) bloqueReal = 50;

            SolicitudES solicitud = new SolicitudES(p, p.getOperacion(), bloqueReal, 1, ciclo);
            cluster.getDisco().encolarSolicitud(solicitud); 

            cluster.registrarEvento("Ciclo " + ciclo + ": " + p.getNombre()
                    + " solicitó " + p.getOperacion()
                    + " sobre " + p.getRutaArchivo() + " (Bloque: " + bloqueReal + ")");
        } finally {
            cpu.salirModoSistema();
        }

        p.setEstado(EstadoProceso.BLOQUEADO);
        p.setMotivoBloqueo(MotivoBloqueo.E_S);
        cpu.liberar();
        colaBloqueados.encolar(p);
    }
    // ---------- Paso 6: cierre ----------
    private void cerrarCiclo(int ciclo) {
        PCB p = cpu.getProcesoActual();
        if (p == null) return;

        if (p.estaTerminado()) {
            p.setCicloFin(ciclo);
            cpu.liberar();
            ram.liberar(p.getMemoria());
            terminados.encolar(p);
            cluster.registrarEvento("Ciclo " + ciclo + ": Proceso " + 
                    p.getNombre() + " TERMINÓ sus instrucciones.");
            admitirSiHayMemoria(ciclo);
            reactivar(ciclo);
        }
    }
}