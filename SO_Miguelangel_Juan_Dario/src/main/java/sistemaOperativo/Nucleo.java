
package sistemaOperativo;

import estructuraDeDatos.Cola;
import losEnums.EstadoProceso;
import losEnums.MotivoBloqueo;
import losEnums.TipoProceso;
import hardware.CPU;
import hardware.MemoriaPrincipal;
import planificacionSO.PoliticaPlanificacion;
import estructuraDeDatos.ListaSimple;
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

    // C-2 fix: para devolver procesos a su propio núcleo
    private Cluster cluster;

    private final Cola colaNuevos;
    private final Cola colaListos;
    private final Cola colaBloqueados;
    private final Cola terminados;

    private int ciclosEnCpu = 0;
    private int latenciaRed = 3;

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
    }

    // ---------- Getters y Setters ----------
    public int getIdComputador() { return idComputador; }
    public Cola getColaNuevos()     { return colaNuevos; }
    public Cola getColaListos()     { return colaListos; }
    public Cola getColaBloqueados() { return colaBloqueados; }
    public Cola getTerminados()     { return terminados; }
    public CPU getCpu() { return cpu; }
    public MemoriaPrincipal getRam() { return ram; }
    public PoliticaPlanificacion getPolitica() { return politica; }
    public void setPolitica(PoliticaPlanificacion politica) { this.politica = politica; }
    public GestorBuffers getGestorBuffers() { return gestorBuffers; }
    public Cluster getCluster() { return cluster; }
    public void setCluster(Cluster cluster) { this.cluster = cluster; }
    public int getLatenciaRed() { return latenciaRed; }
    public void setLatenciaRed(int latenciaRed) { this.latenciaRed = latenciaRed; }

    // ---------- API ----------
    public void admitir(PCB p) {
        p.setEstado(EstadoProceso.NUEVO);
        colaNuevos.encolar(p);
    }

    public void bloquear(PCB p, MotivoBloqueo motivo) {
        p.setEstado(EstadoProceso.BLOQUEADO);
        p.setMotivoBloqueo(motivo);
        colaListos.eliminar(p);
        colaBloqueados.encolar(p);
        if (cluster != null) cluster.registrarEvento("Ciclo " + cluster.getReloj().getCiclo() + ": Proceso " + p.getNombre() + " BLOQUEADO por " + motivo);
    }

    public void despertar(PCB p) {
        p.setEstado(EstadoProceso.LISTO);
        p.setMotivoBloqueo(null);
        colaBloqueados.eliminar(p);
        colaListos.encolar(p);
    }

    // ---------- EL TIC ----------
    public void tic(int ciclo) {
        vencerPlazos(ciclo);
        despertarBloqueados();
        admitirSiHayMemoria();
        planificar(ciclo);
        ejecutarInstruccion(ciclo);

        if (!cpu.estaLibre()) ciclosEnCpu++;
        else ciclosEnCpu = 0;

        cerrarCiclo(ciclo);
    }

    // ---------- Paso 1 ----------
    private void vencerPlazos(int ciclo) {
        vencerEnCola(colaNuevos, ciclo);
        vencerEnCola(colaListos, ciclo);
        vencerEnCola(colaBloqueados, ciclo);
        vencerEnSemaforos(ciclo);

        PCB enCpu = cpu.getProcesoActual();
        if (enCpu != null) {
            enCpu.setDeadline(enCpu.getDeadline() - 1);
            if (enCpu.getDeadline() <= 0) {
                cpu.liberar();
                ram.liberar(enCpu.getMemoria());
                enCpu.setEstado(EstadoProceso.TERMINADO);
                enCpu.setCicloFin(ciclo);
                terminados.encolar(enCpu);
                if (cluster != null) cluster.registrarEvento("Ciclo " + ciclo + ": Proceso " + enCpu.getNombre() + " TERMINADO por Deadline en Comp " + idComputador);
            }
        }
    }

    private void vencerEnCola(Cola c, int ciclo) {
        int n = c.getTamano();
        for (int i = 0; i < n; i++) {
            PCB p = (PCB) c.desencolar();
            p.setDeadline(p.getDeadline() - 1);

            if (p.getDeadline() <= 0) {
                if (p.getEstado() == EstadoProceso.LISTO
                        || p.getEstado() == EstadoProceso.BLOQUEADO) {
                    ram.liberar(p.getMemoria());
                }
                p.setEstado(EstadoProceso.TERMINADO);
                p.setCicloFin(ciclo);
                terminados.encolar(p);
                 if (cluster != null) cluster.registrarEvento("Ciclo " + ciclo + ": Proceso " + p.getNombre() + " TERMINADO por Deadline en espera (Comp " + idComputador + ")");
            } else {
                c.encolar(p);
            }
        }
    }

    private void vencerEnSemaforos(int ciclo) {
        ListaSimple buffers = gestorBuffers.getLista();
        for (int i = 0; i < buffers.getSize(); i++) {
            Buffer b = (Buffer) buffers.getValue(i);
            vencerEnSemaforo(b.getMutex(), ciclo);
            vencerEnSemaforo(b.getVacios(), ciclo);
            vencerEnSemaforo(b.getLlenos(), ciclo);
        }
    }

    /**
     * C-1 fix: solo procesa los PCBs de ESTE computador.
     * Los ajenos se reencolan sin tocarlos.
     */
    private void vencerEnSemaforo(Semaforo s, int ciclo) {
        int n = s.getBloqueados().getTamano();
        for (int i = 0; i < n; i++) {
            PCB p = (PCB) s.getBloqueados().desencolar();

            if (p.getComputador() != this.idComputador) {
                s.getBloqueados().encolar(p);
                continue;
            }

            p.setDeadline(p.getDeadline() - 1);
            p.setCiclosBloqueado(p.getCiclosBloqueado() + 1);

            if (p.getDeadline() <= 0) {
                ram.liberar(p.getMemoria());
                p.setEstado(EstadoProceso.TERMINADO);
                p.setCicloFin(ciclo);
                terminados.encolar(p);
            } else {
                s.getBloqueados().encolar(p);
            }
        }
    }

    // ---------- Paso 2 ----------
    private void despertarBloqueados() {
        int n = colaBloqueados.getTamano();
        for (int i = 0; i < n; i++) {
            PCB p = (PCB) colaBloqueados.desencolar();
            p.setCiclosBloqueado(p.getCiclosBloqueado() + 1);

            if (p.getMotivoBloqueo() == MotivoBloqueo.E_S
                    || p.getMotivoBloqueo() == MotivoBloqueo.RED) {
                p.setCiclosParaSatisfacer(p.getCiclosParaSatisfacer() - 1);
                if (p.getCiclosParaSatisfacer() <= 0) {
                    p.setEstado(EstadoProceso.LISTO);
                    p.setMotivoBloqueo(null);
                    colaListos.encolar(p);
                } else {
                    colaBloqueados.encolar(p);
                }
            } else {
                colaBloqueados.encolar(p);
            }
        }
    }

    // ---------- Paso 3 ----------
    private void admitirSiHayMemoria() {
        PCB p = (PCB) colaNuevos.verFrente();
        while (p != null && ram.hayEspacio(p.getMemoria())) {
            colaNuevos.desencolar();
            ram.asignar(p.getMemoria());
            p.setEstado(EstadoProceso.LISTO);
            colaListos.encolar(p);
            if (cluster != null) cluster.registrarEvento("Ciclo " + cluster.getReloj().getCiclo() + ": Proceso " + p.getNombre() + " admitido en Comp " + idComputador);
            p = (PCB) colaNuevos.verFrente();
        }
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
        
         // --- LÍNEA NUEVA PARA QUE EL GRÁFICO FUNCIONE ---
        cpu.setCiclosOcupados(cpu.getCiclosOcupados() + 1);

        if (p.getTipo() == TipoProceso.PRODUCTOR
                || p.getTipo() == TipoProceso.CONSUMIDOR) {
            ejecutarProductorConsumidor(p);
        } else if (p.getTipo() == TipoProceso.IO_BOUND) {
            ejecutarIOBound(p);   // C-4 fix
        } else {
            p.ejecutarInstruccion();
            
        }
    }

    /** C-3 fix: modo SISTEMA mientras opera el buffer. */
    private void ejecutarProductorConsumidor(PCB p) {
        p.setContadorCiclos(p.getContadorCiclos() + 1);

        if (p.getContadorCiclos() < p.getCiclosPorElemento()) {
            p.setPc(p.getPc() + 1);
            p.setMar(p.getMar() + 1);
            return;
        }

        Buffer b = gestorBuffers.getBuffer(p.getBufferAsociado());
        if (b == null) {
            p.setContadorCiclos(0);
            p.setPc(p.getPc() + 1);
            p.setMar(p.getMar() + 1);
            return;
        }

        boolean esRemoto = (p.getComputador() != b.getAnfitrion());
        if (esRemoto && !p.isLatenciaPagada()) {
            p.setLatenciaPagada(true);
            p.setCiclosParaSatisfacer(latenciaRed);
            bloquear(p, MotivoBloqueo.RED);
            cpu.liberar();
            return;
        }

        // C-3 fix
         cpu.entrarModoSistema();
        boolean exito;
        if (p.getTipo() == TipoProceso.PRODUCTOR) {
            exito = b.producir(p, this);
        } else {
            exito = b.consumir(p, this);
        }
        cpu.salirModoSistema();
    }

    /** C-4 fix: I/O bound se bloquea cada N ciclos pidiendo E/S. */
    private void ejecutarIOBound(PCB p) {
        // Defaults si no se configuraron: cada 5 ciclos pide E/S de 3
        int cadaN = (p.getCiclosPorElemento() > 0) ? p.getCiclosPorElemento() : 5;
        int duracion = (p.getCiclosParaSatisfacer() > 0) ? p.getCiclosParaSatisfacer() : 3;

        p.setContadorCiclos(p.getContadorCiclos() + 1);

        if (p.getContadorCiclos() >= cadaN) {
            p.setContadorCiclos(0);
            p.setCiclosParaSatisfacer(duracion);
            bloquear(p, MotivoBloqueo.E_S);
            cpu.liberar();
            return;
        }

        p.ejecutarInstruccion();
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
            if (cluster != null) cluster.registrarEvento("Ciclo " + ciclo + ": Proceso " + p.getNombre() + " TERMINADO en Comp " + idComputador);
            admitirSiHayMemoria();
        }
    }
        public double getTiempoRespuestaPromedio() {
        int cantidad = terminados.getTamano();
        if (cantidad == 0) return 0.0;
        
        double suma = 0;
        estructuraDeDatos.Nodo actual = terminados.getCabeza();
        while (actual != null) {
            PCB p = (PCB) actual.getDato();
            suma += (p.getCicloFin() - p.getCicloLlegada());
            actual = actual.getpNext();
        }
        return suma / cantidad;
    }
    
}
