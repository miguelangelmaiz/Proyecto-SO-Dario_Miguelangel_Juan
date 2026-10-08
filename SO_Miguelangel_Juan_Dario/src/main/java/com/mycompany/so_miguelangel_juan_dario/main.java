/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.so_miguelangel_juan_dario;
import EDD.Cola;
import EDD.ColaPrioridad;
import SO.PCB;
import EDD.Nodo;
import Enums.TipoProceso;
import EDD.Criterio;
import hardware.CPU;
import hardware.ComponenteReloj;
import hardware.MemoriaPrincipal;
import SO.Nucleo;
import SO.Planificacion.FCFS;

import hardware.CPU;

/**
 *
 * @author Miguel
 */
public class main {

    public static void main(String[] args) {
        CPU cpu = new CPU();
        MemoriaPrincipal ram = new MemoriaPrincipal(2048);
        Nucleo nucleo = new Nucleo(1, cpu, ram, new FCFS());

        PCB p1 = new PCB("A", TipoProceso.CPU_BOUND,
                PCB.PRIORIDAD_MEDIA, 256, 20, 5, 1);
        PCB p2 = new PCB("B", TipoProceso.CPU_BOUND,
                PCB.PRIORIDAD_MEDIA, 256, 20, 3, 1);

        nucleo.admitir(p1);
        nucleo.admitir(p2);

        System.out.println("=== INICIO ===");
        System.out.println("RAM usada/libre: "
                + ram.getUsada() + " / " + ram.getLibre());

        for (int i = 1; i <= 12; i++) {
            nucleo.tic(i);

            System.out.println("--- Ciclo " + i + " ---");
            PCB enCpu = cpu.getProcesoActual();
            System.out.println("CPU: " + (enCpu == null ? "(libre)" : enCpu));
            System.out.print("Listos: ");
            nucleo.getColaListos().imprimir();
            System.out.print("Terminados: ");
            nucleo.getTerminados().imprimir();
            System.out.println("RAM usada/libre: "
                    + ram.getUsada() + " / " + ram.getLibre());
        }
    }
        
    
}
