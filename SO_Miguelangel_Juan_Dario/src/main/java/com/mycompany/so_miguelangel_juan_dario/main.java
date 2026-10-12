/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.so_miguelangel_juan_dario;
import EDD.Cola;
import EDD.ColaPrioridad;
import SO.PCB;
import EDD.Nodo;
import Enums.TipoProceso;
<<<<<<< Updated upstream
import EDD.Criterio;
=======
import SO.Cluster;
import SO.Sincronizacion.Buffer;
import SO.Sincronizacion.GestorBuffers;
>>>>>>> Stashed changes
import hardware.CPU;
import hardware.ComponenteReloj;

/**
 *
 * @author Miguel
 */
public class main {

<<<<<<< Updated upstream
    public static void main(String[] args) {
        Criterio porPrioridad = (a, b) ->
                ((PCB) a).getPrioridad() > ((PCB) b).getPrioridad();

        ColaPrioridad cp = new ColaPrioridad(porPrioridad);
cp.insertarObjeto(new PCB("A", TipoProceso.CPU_BOUND, 2, 256, 20, 5, 1));
cp.insertarObjeto(new PCB("B", TipoProceso.CPU_BOUND, 5, 256, 20, 5, 1));
cp.insertarObjeto(new PCB("C", TipoProceso.CPU_BOUND, 1, 256, 20, 5, 1));

System.out.println("--- ColaPrioridad ---");
Nodo aux = cp.getCabeza();
while (aux != null) {
    System.out.print(aux.getDato() + " -> ");
    aux = aux.getpNext();
}
System.out.println("NULL");}
}
=======
    public static void main(String[] args) throws InterruptedException {

        System.out.println("===========================================");
        System.out.println("  ÁvilaOS - Demostración Semanas 3 y 4");
        System.out.println("===========================================\n");

        probarCuatroPoliticas();
        probarClusterDosComputadores();
        probarProductorConsumidorLocal();

        System.out.println("\n=== FIN DE LA DEMOSTRACIÓN ===");
    }

    // -----------------------------------------------------------------
    // 1) Comparación de las 4 políticas sobre 1 computador
    // -----------------------------------------------------------------
    private static void probarCuatroPoliticas() {
        System.out.println(">>> TEST 1: comparación de las 4 políticas (1 computador)\n");

        correr("FCFS",                new FCFS());
        correr("Round Robin (q=2)",   new RoundRobin(2));
        correr("EDF",                 new EDF());
        correr("Prioridades",         new PrioridadApropiativa());
    }

    private static void correr(String nombre, PoliticaPlanificacion pol) {

        System.out.println("========== " + nombre + " ==========");

        Cluster cluster = new Cluster(1, 2048, 0); // 0 ms: corre sin dormir
        cluster.getComputador(0).setPolitica(pol);

        PCB p1 = new PCB("A", TipoProceso.CPU_BOUND, PCB.PRIORIDAD_BAJA,  256, 25, 4, 1);
        PCB p2 = new PCB("B", TipoProceso.CPU_BOUND, PCB.PRIORIDAD_ALTA,  256, 30, 6, 1);
        PCB p3 = new PCB("C", TipoProceso.CPU_BOUND, PCB.PRIORIDAD_MEDIA, 256, 15, 5, 1);
        PCB p4 = new PCB("D", TipoProceso.CPU_BOUND, PCB.PRIORIDAD_MEDIA, 256, 20, 3, 1);

        cluster.getComputador(0).getNucleo().admitir(p1);
        cluster.getComputador(0).getNucleo().admitir(p2);
        cluster.getComputador(0).getNucleo().admitir(p3);
        cluster.getComputador(0).getNucleo().admitir(p4);

        for (int i = 1; i <= 40; i++) {
            cluster.getComputador(0).getNucleo().tic(i);
            PCB enCpu = cluster.getComputador(0).getCpu().getProcesoActual();
            String cpuStr = (enCpu == null) ? "(libre)" : enCpu.toString();
            System.out.println("Ciclo " + i + " | CPU: " + cpuStr
                    + " | Listos: " + cluster.getComputador(0).getNucleo().getColaListos().getTamano());
        }

        System.out.println("-- Terminados " + nombre + ":");
        cluster.getComputador(0).getNucleo().getTerminados().imprimir();
        System.out.println();
    }

    // -----------------------------------------------------------------
    // 2) Clúster con 2 computadores, políticas distintas
    // -----------------------------------------------------------------
    private static void probarClusterDosComputadores() {
        System.out.println(">>> TEST 2: clúster con 2 computadores\n");

        Cluster cluster = new Cluster(2, 2048, 0);
        cluster.getComputador(0).setPolitica(new FCFS());
        cluster.getComputador(1).setPolitica(new RoundRobin(3));

        PCB a1 = new PCB("A1", TipoProceso.CPU_BOUND, PCB.PRIORIDAD_ALTA, 256, 30, 5, 1);
        PCB a2 = new PCB("A2", TipoProceso.CPU_BOUND, PCB.PRIORIDAD_MEDIA, 256, 30, 4, 1);
        PCB b1 = new PCB("B1", TipoProceso.CPU_BOUND, PCB.PRIORIDAD_ALTA, 256, 30, 7, 2);
        PCB b2 = new PCB("B2", TipoProceso.CPU_BOUND, PCB.PRIORIDAD_MEDIA, 256, 30, 5, 2);

        cluster.getComputador(0).getNucleo().admitir(a1);
        cluster.getComputador(0).getNucleo().admitir(a2);
        cluster.getComputador(1).getNucleo().admitir(b1);
        cluster.getComputador(1).getNucleo().admitir(b2);

        for (int i = 1; i <= 30; i++) {
            cluster.getComputador(0).getNucleo().tic(i);
            cluster.getComputador(1).getNucleo().tic(i);

            PCB c1 = cluster.getComputador(0).getCpu().getProcesoActual();
            PCB c2 = cluster.getComputador(1).getCpu().getProcesoActual();
            System.out.println("Ciclo " + i
                    + " | C1(" + cluster.getComputador(0).getNucleo().getPolitica().getNombre() + "): "
                    + (c1 == null ? "(libre)" : c1)
                    + " | C2(" + cluster.getComputador(1).getNucleo().getPolitica().getNombre() + "): "
                    + (c2 == null ? "(libre)" : c2));
        }

        System.out.println("-- Terminados C1:");
        cluster.getComputador(0).getNucleo().getTerminados().imprimir();
        System.out.println("-- Terminados C2:");
        cluster.getComputador(1).getNucleo().getTerminados().imprimir();
        System.out.println();
    }

    // -----------------------------------------------------------------
    // 3) Productor-Consumidor local con buffer acotado
    // -----------------------------------------------------------------
    private static void probarProductorConsumidorLocal() {
        System.out.println(">>> TEST 3: productor-consumidor local (buffer cap=4)\n");

        Cluster cluster = new Cluster(1, 4096, 0);
        cluster.getComputador(0).setPolitica(new RoundRobin(2));

        GestorBuffers gb = cluster.getGestorBuffers();
        Buffer b1 = gb.crearBuffer(4, 1); // buffer B1, capacidad 4, anfitrión C1
        System.out.println("Buffer creado: " + b1);

        // Productor: buffer 1, produce cada 2 ciclos, 6 elementos
        PCB prod = new PCB("PROD", TipoProceso.PRODUCTOR, PCB.PRIORIDAD_ALTA,
                           128, 100, 100, 1);
        prod.setBufferAsociado(b1.getId());
        prod.setCiclosPorElemento(2);
        prod.setElementosRestantes(6);

        // Consumidor: buffer 1, consume cada 3 ciclos, 6 elementos
        PCB cons = new PCB("CONS", TipoProceso.CONSUMIDOR, PCB.PRIORIDAD_ALTA,
                           128, 100, 100, 1);
        cons.setBufferAsociado(b1.getId());
        cons.setCiclosPorElemento(3);
        cons.setElementosRestantes(6);

        cluster.getComputador(0).getNucleo().admitir(prod);
        cluster.getComputador(0).getNucleo().admitir(cons);

        for (int i = 1; i <= 40; i++) {
            cluster.getComputador(0).getNucleo().tic(i);

            PCB enCpu = cluster.getComputador(0).getCpu().getProcesoActual();
            System.out.println("Ciclo " + i
                    + " | CPU=" + (enCpu == null ? "(libre)" : enCpu)
                    + " | B1 elementos=" + b1.getElementos()
                    + " | vacios=" + b1.getVacios().getValor()
                    + " llenos=" + b1.getLlenos().getValor()
                    + " mutex=" + b1.getMutex().getValor());
        }

        System.out.println("\n-- Terminados:");
        cluster.getComputador(0).getNucleo().getTerminados().imprimir();
        System.out.println();
    }
}
>>>>>>> Stashed changes
