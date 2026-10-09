/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package SO;
import EDD.Cola;
import Enums.EstadoProceso;
import Enums.MotivoBloqueo;
import hardware.CPU;
import hardware.MemoriaPrincipal;
import SO.Planificacion.PoliticaPlanificacion;
/**
 *
 * @author Miguel
 */
public class Nucleo {
    
    private final int idComputador;
    private final CPU cpu;
    private final MemoriaPrincipal ram;
    private PoliticaPlanificacion politica;

    // Las cuatro colas del sistema
    private final Cola colaNuevos;
    private final Cola colaListos;
    private final Cola colaBloqueados;
    private final Cola terminados;

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
    public void setPolitica(PoliticaPlanificacion politica) {
        this.politica = politica;
    }

    // ---------- API del sistema ----------

    /** La GUI (o el Clúster) llama a esto para crear un proceso. */
    public void admitir(PCB p) {
        p.setEstado(EstadoProceso.NUEVO);
        colaNuevos.encolar(p);
    }

    /** Lo llamará un semWait cuando el proceso no pueda continuar. */
    public void bloquear(PCB p, MotivoBloqueo motivo) {
        p.setEstado(EstadoProceso.BLOQUEADO);
        p.setMotivoBloqueo(motivo);
        colaListos.eliminar(p);
        colaBloqueados.encolar(p);
    }

    /** Lo llamará un semSignal cuando despierte a alguien. */
    public void despertar(PCB p) {
        p.setEstado(EstadoProceso.LISTO);
        p.setMotivoBloqueo(null);
        colaBloqueados.eliminar(p);
        colaListos.encolar(p);
    }

    // ---------- El tic, con los 6 pasos del enunciado ----------

    public void tic(int ciclo) {
        vencerPlazos(ciclo);         // 1
        despertarBloqueados();       // 2
        admitirSiHayMemoria();       // 3
        planificar(ciclo);           // 4
        cpu.tic(ciclo);              // 5 (ejecuta una instrucción)
        cerrarCiclo(ciclo);          // 6
    }

    // ---------- Paso 1: vencer plazos ----------

    private void vencerPlazos(int ciclo) {
        vencerEnCola(colaNuevos, ciclo);
        vencerEnCola(colaListos, ciclo);
        vencerEnCola(colaBloqueados, ciclo);

        // El de la CPU también baja su deadline
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
                // Solo libera RAM si ya había sido admitido
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

            // Solo E/S se despierta sola por el reloj.
            // Los de semáforo los despierta semSignal (Semana 4).
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
        // Si hay alguien en CPU y la política es apropiativa,
        // aquí irá la lógica de expropiación (Semana 3).
        if (!cpu.estaLibre()) {
            return;
        }

        if (colaListos.estaVacia()) {
            return;
        }

        PCB elegido = politica.seleccionar(colaListos);
        if (elegido != null) {
            colaListos.eliminar(elegido);
            if (elegido.getCicloInicio() < 0) {
                elegido.setCicloInicio(ciclo);
            }
            cpu.cargar(elegido);  // el CPU ya le pone el estado EJECUCION
        }
    }

    // ---------- Paso 6: cerrar el ciclo ----------

    private void cerrarCiclo(int ciclo) {
        PCB p = cpu.getProcesoActual();
        if (p == null) return;

        if (p.estaTerminado()) {
            p.setCicloFin(ciclo);
            cpu.liberar();
            ram.liberar(p.getMemoria());
            terminados.encolar(p);

            // Barrido obligatorio: si se liberó RAM, puede entrar alguien más
            admitirSiHayMemoria();
        }
    }
    
}
