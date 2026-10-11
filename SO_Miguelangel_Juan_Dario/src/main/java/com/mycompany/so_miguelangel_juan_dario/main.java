/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.so_miguelangel_juan_dario;
import SO.PCB;
import SO.Nucleo;
import SO.Planificacion.FCFS;
import SO.Planificacion.RoundRobin;
import SO.Planificacion.EDF;
import SO.Planificacion.PrioridadApropiativa;
import SO.Planificacion.PoliticaPlanificacion;
import Enums.TipoProceso;
import hardware.CPU;
import hardware.MemoriaPrincipal;

/**
 *
 * @author Miguel
 */
public class main {

    public static void main(String[] args) throws InterruptedException {
  
System.out.println("========================================");
        System.out.println("   COMPARACION DE LAS 4 POLITICAS");
        System.out.println("========================================\n");

        correr("FCFS", new FCFS());
        correr("Round Robin q=2", new RoundRobin(2));
        correr("EDF", new EDF());
        correr("Prioridades", new PrioridadApropiativa());
    }

    private static void correr(String nombre, PoliticaPlanificacion pol) {

        System.out.println("========== " + nombre + " ==========");

        CPU cpu = new CPU();
        MemoriaPrincipal ram = new MemoriaPrincipal(2048);
        Nucleo nucleo = new Nucleo(1, cpu, ram, pol);

        // Mismos 4 procesos en cada prueba
        PCB p1 = new PCB("A", TipoProceso.CPU_BOUND,
                PCB.PRIORIDAD_BAJA,  256, 25, 4, 1);
        PCB p2 = new PCB("B", TipoProceso.CPU_BOUND,
                PCB.PRIORIDAD_ALTA,  256, 30, 6, 1);
        PCB p3 = new PCB("C", TipoProceso.CPU_BOUND,
                PCB.PRIORIDAD_MEDIA, 256, 15, 5, 1);
        PCB p4 = new PCB("D", TipoProceso.CPU_BOUND,
                PCB.PRIORIDAD_MEDIA, 256, 20, 3, 1);

        nucleo.admitir(p1);
        nucleo.admitir(p2);
        nucleo.admitir(p3);
        nucleo.admitir(p4);

        // Correr 40 ciclos o hasta que todos terminen
        for (int i = 1; i <= 40; i++) {
            nucleo.tic(i);

            PCB enCpu = cpu.getProcesoActual();
            String cpuStr = (enCpu == null) ? "(libre)" : enCpu.toString();
            System.out.println("Ciclo " + i + " | CPU: " + cpuStr
                    + " | Listos: " + contar(nucleo));
        }

        System.out.println("\n-- Resultado " + nombre + " --");
        System.out.print("Terminados: ");
        nucleo.getTerminados().imprimir();
        System.out.println("RAM libre: " + ram.getLibre());
        System.out.println();
    }

    private static int contar(Nucleo n) {
        return n.getColaListos().getTamano();
    }
    }
        
    

