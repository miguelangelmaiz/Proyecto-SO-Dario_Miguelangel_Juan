
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
import hardware.Computador;

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
        probarDeadlineEnSemaforo();
        probarBufferRemoto();
        probarIOBound();
        probarC1();
        probarC2();
        probarC3();
        probarM4();

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
    // 4) Deadline mata a un proceso bloqueado en semaforo
    // -----------------------------------------------------------------
    private static void probarDeadlineEnSemaforo() {
        System.out.println(">>> TEST 4: deadline mata a un proceso bloqueado en semaforo\n");

        Cluster cluster = new Cluster(1, 4096, 0);
        cluster.getComputador(0).setPolitica(new RoundRobin(2));

        GestorBuffers gb = cluster.getGestorBuffers();
        Buffer b1 = gb.crearBuffer(2, 1);
        System.out.println("Buffer creado: " + b1);

        PCB cons = new PCB("CONS_MORIR", TipoProceso.CONSUMIDOR, PCB.PRIORIDAD_ALTA,
                           128, 3, 100, 1);
        cons.setBufferAsociado(b1.getId());
        cons.setCiclosPorElemento(1);
        cons.setElementosRestantes(5);

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
    // 5) Buffer remoto con latencia de red
    // -----------------------------------------------------------------
    private static void probarBufferRemoto() {
        System.out.println(">>> TEST 5: buffer remoto (el buffer vive en C2, el proceso en C1)\n");

        Cluster cluster = new Cluster(2, 4096, 0);
        cluster.getComputador(0).setPolitica(new RoundRobin(2));
        cluster.getComputador(1).setPolitica(new RoundRobin(2));

        GestorBuffers gb = cluster.getGestorBuffers();
        Buffer bRemoto = gb.crearBuffer(4, 2);
        System.out.println("Buffer creado: " + bRemoto);

        PCB prod = new PCB("PROD_REMOTO", TipoProceso.PRODUCTOR, PCB.PRIORIDAD_ALTA,
                           128, 100, 100, 1);
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

    // -----------------------------------------------------------------
    // 6) I/O bound se bloquea por E/S (C-4)
    // -----------------------------------------------------------------
    private static void probarIOBound() {
        System.out.println(">>> TEST 6: I/O bound se bloquea por E/S\n");

        Cluster cluster = new Cluster(1, 4096, 0);
        cluster.getComputador(0).setPolitica(new FCFS());

        PCB p = new PCB("IO1", TipoProceso.IO_BOUND, PCB.PRIORIDAD_MEDIA,
                        128, 100, 50, 1);
        p.setCiclosPorElemento(4);
        p.setCiclosParaSatisfacer(3);

        cluster.getComputador(0).getNucleo().admitir(p);

        for (int i = 1; i <= 20; i++) {
            cluster.getComputador(0).getNucleo().tic(i);
            System.out.println("Ciclo " + i
                    + " | Estado=" + p.getEstado()
                    + " | motivo=" + p.getMotivoBloqueo()
                    + " | contador=" + p.getContadorCiclos()
                    + " | E/S restante=" + p.getCiclosParaSatisfacer());
        }
        System.out.println();
    }

    // -----------------------------------------------------------------
    // C-1: deadline se descuenta UNA vez por ciclo
    // -----------------------------------------------------------------
    private static void probarC1() {
        System.out.println(">>> TEST C-1: deadline se descuenta 1 vez por ciclo (2 computadores)\n");

        Cluster cluster = new Cluster(2, 4096, 0);
        GestorBuffers gb = cluster.getGestorBuffers();
        Buffer b1 = gb.crearBuffer(4, 1);

        PCB cons = new PCB("CONS_C1", TipoProceso.CONSUMIDOR, PCB.PRIORIDAD_ALTA,
                           128, 100, 100, 1);
        cons.setBufferAsociado(b1.getId());
        cons.setCiclosPorElemento(1);
        cons.setElementosRestantes(5);

        cluster.getComputador(0).getNucleo().admitir(cons);

        System.out.println("Deadline inicial = 100");
        System.out.println("Si el fix funciona, baja de 1 en 1. Si no, baja de 2 en 2.\n");

        for (int i = 1; i <= 6; i++) {
            cluster.getComputador(0).getNucleo().tic(i);
            cluster.getComputador(1).getNucleo().tic(i);
            System.out.println("Ciclo " + i + " | deadline=" + cons.getDeadline());
        }
        System.out.println();
    }

    // -----------------------------------------------------------------
    // C-2: proceso de C2 no se muda a C1
    // -----------------------------------------------------------------
    private static void probarC2() {
        System.out.println(">>> TEST C-2: proceso de C2 NO aparece en cola de C1\n");

        Cluster cluster = new Cluster(2, 4096, 0);
        GestorBuffers gb = cluster.getGestorBuffers();
        Buffer b1 = gb.crearBuffer(4, 1);

        PCB prod = new PCB("PROD_C1", TipoProceso.PRODUCTOR, PCB.PRIORIDAD_ALTA,
                           128, 100, 100, 1);
        prod.setBufferAsociado(b1.getId());
        prod.setCiclosPorElemento(1);
        prod.setElementosRestantes(3);

        PCB cons = new PCB("CONS_C2", TipoProceso.CONSUMIDOR, PCB.PRIORIDAD_ALTA,
                           128, 100, 100, 2);
        cons.setBufferAsociado(b1.getId());
        cons.setCiclosPorElemento(1);
        cons.setElementosRestantes(3);

        cluster.getComputador(0).getNucleo().admitir(prod);
        cluster.getComputador(1).getNucleo().admitir(cons);

        for (int i = 1; i <= 15; i++) {
            cluster.getComputador(0).getNucleo().tic(i);
            cluster.getComputador(1).getNucleo().tic(i);

            int enC1 = cluster.getComputador(0).getNucleo().getColaListos().getTamano();
            int enC2 = cluster.getComputador(1).getNucleo().getColaListos().getTamano();
            System.out.println("Ciclo " + i
                    + " | colaListos C1=" + enC1
                    + " | colaListos C2=" + enC2
                    + " | PC del cons=" + cons.getComputador());
        }
        System.out.println();
    }

    // -----------------------------------------------------------------
    // C-3: modo SISTEMA durante operaciones de buffer
    // -----------------------------------------------------------------
       private static void probarC3() {
        System.out.println(">>> TEST C-3: contador de instrucciones en modo SISTEMA\n");

        Cluster cluster = new Cluster(1, 4096, 0);
        GestorBuffers gb = cluster.getGestorBuffers();
        Buffer b1 = gb.crearBuffer(4, 1);

        PCB prod = new PCB("PROD", TipoProceso.PRODUCTOR, PCB.PRIORIDAD_ALTA,
                           128, 100, 100, 1);
        prod.setBufferAsociado(b1.getId());
        prod.setCiclosPorElemento(1);
        prod.setElementosRestantes(3);

        cluster.getComputador(0).getNucleo().admitir(prod);

        System.out.println("Cada produccion debe incrementar el contador SISTEMA.");
        System.out.println("Se espera: 1 por cada elemento producido.\n");

        for (int i = 1; i <= 6; i++) {
            cluster.getComputador(0).getNucleo().tic(i);
            System.out.println("Ciclo " + i
                    + " | elem=" + b1.getElementos()
                    + " | instrSistema=" + cluster.getComputador(0).getCpu().getInstruccionesSistema()
                    + " | estadoProd=" + prod.getEstado());
        }
        System.out.println();
    }
           private static void probarM4() {
        System.out.println(">>> TEST M-4: agregar computador en caliente\n");

        // Arrancar con 2 computadores
        Cluster cluster = new Cluster(2, 2048, 0);
        cluster.getComputador(0).setPolitica(new FCFS());
        cluster.getComputador(1).setPolitica(new FCFS());

        System.out.println("Cluster inicial: " + cluster.getCantidad() + " computadores");
        System.out.println("Reloj suscritos: " + cluster.getReloj().getCantidad());

        // Agregar un TERCER computador en caliente
        Computador c3 = new Computador(3, 2048, new FCFS(),
                                       cluster.getGestorBuffers());
        cluster.agregarComputador(c3);

        System.out.println("Despues de agregar: " + cluster.getCantidad() + " computadores");
        System.out.println("Reloj suscritos: " + cluster.getReloj().getCantidad());
        System.out.println();

        // Meter un proceso SOLO en el computador 3
        PCB p = new PCB("NUEVO", TipoProceso.CPU_BOUND, PCB.PRIORIDAD_MEDIA,
                        256, 30, 3, 3);
        cluster.getComputador(2).getNucleo().admitir(p);

        System.out.println("Proceso en C3. Si el reloj lo notifica, se ejecutara.");
        System.out.println("Si M-4 no esta arreglado, C3 nunca recibe tic y el proceso se queda quieto.\n");

        for (int i = 1; i <= 10; i++) {
            cluster.getComputador(0).getNucleo().tic(i);
            cluster.getComputador(1).getNucleo().tic(i);
            cluster.getComputador(2).getNucleo().tic(i);   // ← el nuevo

            System.out.println("Ciclo " + i
                    + " | CPU C3=" + (cluster.getComputador(2).getCpu().getProcesoActual() == null
                        ? "(libre)"
                        : cluster.getComputador(2).getCpu().getProcesoActual())
                    + " | PC=" + p.getPc()
                    + " | restante=" + p.getRestante()
                    + " | estado=" + p.getEstado());
        }
        System.out.println();
    }
    }
        
    

