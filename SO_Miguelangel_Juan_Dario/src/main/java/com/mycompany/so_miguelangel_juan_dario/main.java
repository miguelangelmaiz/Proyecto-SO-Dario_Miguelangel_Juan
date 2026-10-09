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
import hardware.RelojGlobal;
import SO.Planificacion.PoliticaPlanificacion;
import hardware.Computador;
import hardware.CPU;

/**
 *
 * @author Miguel
 */
public class main {

    public static void main(String[] args) throws InterruptedException {
  
RelojGlobal reloj = new RelojGlobal(300, 2);

        PoliticaPlanificacion p1 = new FCFS();
        PoliticaPlanificacion p2 = new FCFS();

        Computador c1 = new Computador(1, 2048, p1);
        Computador c2 = new Computador(2, 2048, p2);

        reloj.suscribir(c1);
        reloj.suscribir(c2);

        c1.getNucleo().admitir(new PCB("A", TipoProceso.CPU_BOUND, 2, 256, 20, 5, 1));
        c1.getNucleo().admitir(new PCB("B", TipoProceso.CPU_BOUND, 5, 256, 30, 8, 1));
        c1.getNucleo().admitir(new PCB("C", TipoProceso.CPU_BOUND, 1, 256, 40, 3, 1));

        Thread hilo = new Thread(reloj, "reloj-prueba");
        hilo.setDaemon(true);
        hilo.start();
        reloj.iniciar();

        Thread.sleep(3000);
        reloj.detener();

        System.out.println("=== Fin ===");
        System.out.println("Ciclo: " + reloj.getCiclo());
        System.out.println("Nuevos: " + c1.getNucleo().getColaNuevos().getTamano());
        System.out.println("Listos: " + c1.getNucleo().getColaListos().getTamano());
        System.out.println("Terminados: " + c1.getNucleo().getTerminados().getTamano());
        System.out.println("CPU c1: " + c1.getCpu());
        System.out.println("RAM c1: " + c1.getRam());

        }
    }
        
    

