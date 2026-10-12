/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package SO;
import EDD.Cola;
import Enums.EstadoProceso;
import Enums.MotivoBloqueo;
import Enums.TipoProceso;
import hardware.CPU;
import hardware.MemoriaPrincipal;
import SO.Planificacion.PoliticaPlanificacion;
import SO.Sincronizacion.Buffer;
import SO.Sincronizacion.GestorBuffers;
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

    private final Cola colaNuevos;
    private final Cola colaListos;
    private final Cola colaBloqueados;
    private final Cola terminados;

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
    }

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

    // ---------- API del sistema ----------

    public void admitir(PCB p) {
        p.setEstado(EstadoProceso.NUEVO);
        p.setCicloLlegada(0);
        colaNuevos.encolar(p);
    }

    public void bloquear(PCB p, MotivoBloqueo motivo) {
        p.setEstado(EstadoProceso.BLOQUEADO);
        p.setMotivoBloqueo(motivo);
        colaListos.eliminar(p);
        colaBloqueados.encolar(p);
    }

    public void despertar(PCB p) {
        p.setEstado(EstadoProceso.LISTO);
        p.setMotivoBloqueo(null);
        colaBloqueados.eliminar(p);
        colaListos.encolar(p);
    }

    // ---------- EL TIC ----------

    public void tic(int ciclo) {
        vencerPlazos(ciclo);           // 1
        despertarBloqueados();         // 2
        admitirSiHayMemoria();         // 3
        planificar(ciclo);             // 4
        ejecutarInstruccion(ciclo);    // 5

        // Cuántos ciclos lleva en CPU (para Round Robin)
        if (!cpu.estaLibre()) ciclosEnCpu++;
        else ciclosEnCpu = 0;

        cerrarCiclo(ciclo);            // 6
    }

    // ---------- Paso 1: vencer plazos ----------

    private void vencerPlazos(int ciclo) {
        vencerEnCola(colaNuevos, ciclo);
        vencerEnCola(colaListos, ciclo);
        vencerEnCola(colaBloqueados, ciclo);

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
                if (p.getEstado() == EstadoProceso.LISTO
                        || p.getEstado() == EstadoProceso.BLOQUEADO) {
                    ram.liberar(p.getMemoria());
                }
                p.setEstado(EstadoProceso.TERMINADO);
                p.setCicloFin(ciclo);
                terminados.encolar(p);
            } else {
                c.encolar(p);
            }
        }
    }

    // ---------- Paso 2: despertar bloqueados por E/S ----------

    private void despertarBloqueados() {
        int n = colaBloqueados.getTamano();
        for (int i = 0; i < n; i++) {
            PCB p = (PCB) colaBloqueados.desencolar();

            if (p.getMotivoBloqueo() == MotivoBloqueo.E_S) {
                p.setCiclosParaSatisfacer(p.getCiclosParaSatisfacer() - 1);
                if (p.getCiclosParaSatisfacer() <= 0) {
                    p.setEstado(EstadoProceso.LISTO);
                    p.setMotivoBloqueo(null);
                    colaListos.encolar(p);
                } else {
                    colaBloqueados.encolar(p);
                }
            } else {
                // Semáforo y RED no se despiertan por reloj: los despierta semSignal
                colaBloqueados.encolar(p);
            }
        }
    }

    // ---------- Paso 3: admitir de la cola de nuevos ----------

    private void admitirSiHayMemoria() {
        PCB p = (PCB) colaNuevos.verFrente();
        while (p != null && ram.hayEspacio(p.getMemoria())) {
            colaNuevos.desencolar();
            ram.asignar(p.getMemoria());
            p.setEstado(EstadoProceso.LISTO);
            colaListos.encolar(p);
            p = (PCB) colaNuevos.verFrente();
        }
    }

    // ---------- Paso 4: planificar ----------

    private void planificar(int ciclo) {

        // ¿Hay que desalojar al de CPU?
        if (!cpu.estaLibre()) {
            PCB enCpu = cpu.getProcesoActual();
            boolean desalojar = false;

            // Por quantum (RR)
            if (politica.getQuantum() > 0 && ciclosEnCpu >= politica.getQuantum()) {
                desalojar = true;
            }
            // Por expropiación (Prioridades, EDF)
            else if (politica.esApropiativa() && !colaListos.estaVacia()) {
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

        // Si CPU libre, meter al siguiente
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

    // ---------- Paso 5: ejecutar UNA instrucción (o producir/consumir) ----------

    private void ejecutarInstruccion(int ciclo) {
        PCB p = cpu.getProcesoActual();
        if (p == null) return;

        if (p.getTipo() == TipoProceso.PRODUCTOR || p.getTipo() == TipoProceso.CONSUMIDOR) {
            ejecutarProductorConsumidor(p);
        } else {
            p.ejecutarInstruccion();
        }
    }

    private void ejecutarProductorConsumidor(PCB p) {
        p.setContadorCiclos(p.getContadorCiclos() + 1);

        // Todavía no toca producir/consumir: sólo avanza un ciclo
        if (p.getContadorCiclos() < p.getCiclosPorElemento()) {
            p.setPc(p.getPc() + 1);
            p.setMar(p.getMar() + 1);
            return;
        }

        Buffer b = gestorBuffers.getBuffer(p.getBufferAsociado());
        if (b == null) {
            // Buffer inexistente: avanza como instrucción normal y sigue
            p.setContadorCiclos(0);
            p.setPc(p.getPc() + 1);
            p.setMar(p.getMar() + 1);
            return;
        }

        boolean exito;
        if (p.getTipo() == TipoProceso.PRODUCTOR) {
            exito = b.producir(p, this);
        } else {
            exito = b.consumir(p, this);
        }

        if (exito) {
            p.setContadorCiclos(0);
            p.setElementosRestantes(p.getElementosRestantes() - 1);
            p.setPc(p.getPc() + 1);
            p.setMar(p.getMar() + 1);
            if (p.getElementosRestantes() <= 0) {
                p.setEstado(EstadoProceso.TERMINADO);
            }
        }
        // Si no tuvo éxito, ya quedó BLOQUEADO y la CPU quedó libre.
    }

    // ---------- Paso 6: cerrar ciclo ----------

    private void cerrarCiclo(int ciclo) {
        PCB p = cpu.getProcesoActual();
        if (p == null) return;

        if (p.estaTerminado()) {
            p.setCicloFin(ciclo);
            cpu.liberar();
            ram.liberar(p.getMemoria());
            terminados.encolar(p);
            admitirSiHayMemoria();
        }
    }
}