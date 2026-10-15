
package com.mycompany.so_miguelangel_juan_dario;
import sistemaOperativo.PCB;
import sistemaOperativo.Cluster;
import sistemaOperativo.Buffer;
import sistemaOperativo.GestorBuffers;
import planificacionSO.FCFS;
import planificacionSO.RoundRobin;
import planificacionSO.EDF;
import planificacionSO.PrioridadApropiativa;
import planificacionSO.PoliticaPlanificacion;
import losEnums.TipoProceso;

/**
 *
 * @author Miguel
 */
public class main {

     public static void main(String[] args) throws InterruptedException {

        System.out.println("===========================================");
        System.out.println("  AvilaOS - Demostracion Semanas 3 y 4");
        System.out.println("===========================================\n");

        probarCuatroPoliticas();
        probarClusterDosComputadores();
        probarProductorConsumidorLocal();
        probarDeadlineEnSemaforo();       // BUG 1 y 2
        probarBufferRemoto();             // BUG 3

        System.out.println("\n=== FIN DE LA DEMOSTRACION ===");
    }

    // -----------------------------------------------------------------
    // 1) Comparacion de las 4 politicas sobre 1 computador
    // -----------------------------------------------------------------
    private static void probarCuatroPoliticas() {
        System.out.println(">>> TEST 1: comparacion de las 4 politicas (1 computador)\n");

        correr("FCFS",              new FCFS());
        correr("Round Robin (q=2)", new RoundRobin(2));
        correr("EDF",               new EDF());
        correr("Prioridades",       new PrioridadApropiativa());
    }

    private static void correr(String nombre, PoliticaPlanificacion pol) {

        System.out.println("========== " + nombre + " ==========");

        Cluster cluster = new Cluster(1, 2048, 0);
        cluster.getComputador(0).setPolitica(pol);

        PCB p1 = new PCB("A", TipoProceso.CPU_BOUND, PCB.PRIORIDAD_BAJA,  256, 25, 4, 1);
        PCB p2 = new PCB("B", TipoProceso.CPU_BOUND, PCB.PRIORIDAD_ALTA,  256, 30, 6, 1);
        PCB p3 = new PCB("C", TipoProceso.CPU_BOUND, PCB.PRIORIDAD_MEDIA, 256, 15, 5, 1);
        PCB p4 = new PCB("D", TipoProceso.CPU_BOUND, PCB.PRIORIDAD_MEDIA, 256, 20, 3, 1);

        cluster.getComputador(0).getNucleo().admitir(p1);
        cluster.getComputador(0).getNucleo().admitir(p2);
        cluster.getComputador(0).getNucleo().admitir(p3);
        cluster.getComputador(0).getNucleo().admitir(p4);

        for (int i = 1; i <= 20; i++) {
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
    // 2) Cluster con 2 computadores, politicas distintas
    // -----------------------------------------------------------------
    private static void probarClusterDosComputadores() {
        System.out.println(">>> TEST 2: cluster con 2 computadores\n");

        Cluster cluster = new Cluster(2, 2048, 0);
        cluster.getComputador(0).setPolitica(new FCFS());
        cluster.getComputador(1).setPolitica(new RoundRobin(3));

        PCB a1 = new PCB("A1", TipoProceso.CPU_BOUND, PCB.PRIORIDAD_ALTA,  256, 30, 5, 1);
        PCB a2 = new PCB("A2", TipoProceso.CPU_BOUND, PCB.PRIORIDAD_MEDIA, 256, 30, 4, 1);
        PCB b1 = new PCB("B1", TipoProceso.CPU_BOUND, PCB.PRIORIDAD_ALTA,  256, 30, 7, 2);
        PCB b2 = new PCB("B2", TipoProceso.CPU_BOUND, PCB.PRIORIDAD_MEDIA, 256, 30, 5, 2);

        cluster.getComputador(0).getNucleo().admitir(a1);
        cluster.getComputador(0).getNucleo().admitir(a2);
        cluster.getComputador(1).getNucleo().admitir(b1);
        cluster.getComputador(1).getNucleo().admitir(b2);

        for (int i = 1; i <= 15; i++) {
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
    // 3) Productor-Consumidor local
    // -----------------------------------------------------------------
    private static void probarProductorConsumidorLocal() {
        System.out.println(">>> TEST 3: productor-consumidor local (buffer cap=4)\n");

        Cluster cluster = new Cluster(1, 4096, 0);
        cluster.getComputador(0).setPolitica(new RoundRobin(2));

        GestorBuffers gb = cluster.getGestorBuffers();
        Buffer b1 = gb.crearBuffer(4, 1);
        System.out.println("Buffer creado: " + b1);

        PCB prod = new PCB("PROD", TipoProceso.PRODUCTOR, PCB.PRIORIDAD_ALTA,
                           128, 100, 100, 1);
        prod.setBufferAsociado(b1.getId());
        prod.setCiclosPorElemento(2);
        prod.setElementosRestantes(6);

        PCB cons = new PCB("CONS", TipoProceso.CONSUMIDOR, PCB.PRIORIDAD_ALTA,
                           128, 100, 100, 1);
        cons.setBufferAsociado(b1.getId());
        cons.setCiclosPorElemento(3);
        cons.setElementosRestantes(6);

        cluster.getComputador(0).getNucleo().admitir(prod);
        cluster.getComputador(0).getNucleo().admitir(cons);

        for (int i = 1; i <= 35; i++) {
            cluster.getComputador(0).getNucleo().tic(i);

            PCB enCpu = cluster.getComputador(0).getCpu().getProcesoActual();
            System.out.println("Ciclo " + i
                    + " | CPU=" + (enCpu == null ? "(libre)" : enCpu)
                    + " | B1 elem=" + b1.getElementos()
                    + " | vacios=" + b1.getVacios().getValor()
                    + " llenos=" + b1.getLlenos().getValor()
                    + " mutex=" + b1.getMutex().getValor());
        }

        System.out.println("\n-- Terminados:");
        cluster.getComputador(0).getNucleo().getTerminados().imprimir();
        System.out.println();
    }

    // -----------------------------------------------------------------
    // 4) BUG 1 y 2: deadline que mata estando bloqueado en semaforo
    // -----------------------------------------------------------------
    private static void probarDeadlineEnSemaforo() {
        System.out.println(">>> TEST 4: deadline mata a un proceso bloqueado en semaforo\n");

        Cluster cluster = new Cluster(1, 4096, 0);
        cluster.getComputador(0).setPolitica(new RoundRobin(2));

        GestorBuffers gb = cluster.getGestorBuffers();
        Buffer b1 = gb.crearBuffer(2, 1);   // buffer chico, cap 2
        System.out.println("Buffer creado: " + b1);

        // Consumidor con deadline corto: se va a bloquear en llenos (buffer vacio)
        PCB cons = new PCB("CONS_MORIR", TipoProceso.CONSUMIDOR, PCB.PRIORIDAD_ALTA,
                           128, 3, 100, 1);   // ← deadline = 3
        cons.setBufferAsociado(b1.getId());
        cons.setCiclosPorElemento(1);
        cons.setElementosRestantes(5);

        // Productor lento: no produce hasta el ciclo 10
        PCB prod = new PCB("PROD_LENTO", TipoProceso.PRODUCTOR, PCB.PRIORIDAD_BAJA,
                           128, 100, 100, 1);
        prod.setBufferAsociado(b1.getId());
        prod.setCiclosPorElemento(10);
        prod.setElementosRestantes(3);

        cluster.getComputador(0).getNucleo().admitir(cons);
        cluster.getComputador(0).getNucleo().admitir(prod);

        System.out.println("Consumidor tiene deadline 3 y se bloqueara en 'llenos'.");
        System.out.println("Si el fix funciona, muere en el ciclo 3 y sale del semaforo.\n");

        for (int i = 1; i <= 8; i++) {
            cluster.getComputador(0).getNucleo().tic(i);
            System.out.println("Ciclo " + i
                    + " | Consumidor: " + cons.getEstado()
                    + " | deadline=" + cons.getDeadline()
                    + " | llenos.valor=" + b1.getLlenos().getValor()
                    + " | llenos.bloqueados=" + b1.getLlenos().getBloqueados().getTamano());
        }

        System.out.println("\n-- Terminados (el consumidor debe aparecer aqui):");
        cluster.getComputador(0).getNucleo().getTerminados().imprimir();
        System.out.println();
    }

    // -----------------------------------------------------------------
    // 5) BUG 3: buffer remoto con latencia de red
    // -----------------------------------------------------------------
    private static void probarBufferRemoto() {
        System.out.println(">>> TEST 5: buffer remoto (el buffer vive en C2, el proceso en C1)\n");

        Cluster cluster = new Cluster(2, 4096, 0);
        cluster.getComputador(0).setPolitica(new RoundRobin(2));
        cluster.getComputador(1).setPolitica(new RoundRobin(2));

        GestorBuffers gb = cluster.getGestorBuffers();
        Buffer bRemoto = gb.crearBuffer(4, 2);   // ← anfitrion = C2
        System.out.println("Buffer creado: " + bRemoto);

        // Proceso en C1 accede a buffer de C2: debe bloquearse 3 ciclos por RED
        PCB prod = new PCB("PROD_REMOTO", TipoProceso.PRODUCTOR, PCB.PRIORIDAD_ALTA,
                           128, 100, 100, 1);   // ← computador = 1
        prod.setBufferAsociado(bRemoto.getId());
        prod.setCiclosPorElemento(1);
        prod.setElementosRestantes(3);

        cluster.getComputador(0).getNucleo().admitir(prod);

        System.out.println("El productor corre en C1 pero el buffer vive en C2.");
        System.out.println("Cada acceso debe bloquearlo 3 ciclos por RED.\n");

        for (int i = 1; i <= 15; i++) {
            cluster.getComputador(0).getNucleo().tic(i);
            cluster.getComputador(1).getNucleo().tic(i);

            System.out.println("Ciclo " + i
                    + " | Estado=" + prod.getEstado()
                    + " | motivo=" + prod.getMotivoBloqueo()
                    + " | contadorRed=" + prod.getCiclosParaSatisfacer()
                    + " | B1 elem=" + bRemoto.getElementos());
        }

        System.out.println("\n-- Terminados:");
        cluster.getComputador(0).getNucleo().getTerminados().imprimir();
        System.out.println();
    }
    }
        
    

