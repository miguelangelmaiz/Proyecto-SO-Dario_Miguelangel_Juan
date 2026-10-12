/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hardware;

/**
 *
 * @author jmmor
 */
import Enums.TipoProceso;
import SO.PCB;
import SO.Nucleo;
import SO.Planificacion.FCFS;
import SO.Planificacion.PoliticaPlanificacion;
import hardware.CPU;
import SO.GestorBuffers;

public class Computador implements ComponenteReloj {
     private final int id;
    private final CPU cpu;
    private final MemoriaPrincipal ram;
    private final Nucleo nucleo;

    public Computador(int id, int ramKB, PoliticaPlanificacion politica,
                      GestorBuffers gestorBuffers) {
        this.id = id;
        this.cpu = new CPU();
        this.ram = new MemoriaPrincipal(ramKB);
        this.nucleo = new Nucleo(id, this.cpu, this.ram, politica, gestorBuffers);
    }

    public int getId() { return id; }
    public CPU getCpu() { return cpu; }
    public MemoriaPrincipal getRam() { return ram; }
    public Nucleo getNucleo() { return nucleo; }

    public void setPolitica(PoliticaPlanificacion politica) {
        nucleo.setPolitica(politica);
    }

    @Override
    public void tic(int ciclo) {
        nucleo.tic(ciclo);
    }

    public void reset() {
        cpu.reset();
        ram.reset();
    }
    
}
