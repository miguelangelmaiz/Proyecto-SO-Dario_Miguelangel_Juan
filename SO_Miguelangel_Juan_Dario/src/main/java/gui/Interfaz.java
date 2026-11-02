
package gui;

/**
 *
 * @author jmmor
 */

import estructuraDeDatos.Cola;
import estructuraDeDatos.Nodo;
import hardware.Computador;
import planificacionSO.*;
import sistemaOperativo.PCB;
import losEnums.TipoProceso;
import sistemaOperativo.Cluster;
import gui.VentanaGrafico;
import com.google.gson.Gson;
import java.io.FileReader;
import java.io.FileWriter;
import sistemaOperativo.configuracion;
import gui.MenuInicial;


public class Interfaz extends javax.swing.JFrame {
     

     public sistemaOperativo.Cluster cluster;

    public Interfaz(int numComputadores) {
         initComponents();
        spnMemoria.setModel(new javax.swing.SpinnerNumberModel(1, 1, 4096, 1));
        spnDeadline.setModel(new javax.swing.SpinnerNumberModel(1, 1, 5000, 1));
        spnInstrucciones.setModel(new javax.swing.SpinnerNumberModel(1, 1, 1000, 1));
        spnQuantum.setModel(new javax.swing.SpinnerNumberModel(1, 1, 1000, 1));
        
        jButton2.setEnabled(false); // PAUSAR deshabilitado al inicio
        
        // 1. CREAR EL CLUSTER
        cluster = new sistemaOperativo.Cluster(numComputadores, 2048, 500);
        
        // Ocultar o mostrar según el número elegido
        if (panelComp3 != null) {
            panelComp3.setVisible(numComputadores >= 3);
        }
        if (panelComp4 != null) {
            panelComp4.setVisible(numComputadores >= 4);
        }
        
        // Agregar las opciones al ComboBox de Crear Proceso
        OpComputadora.removeAllItems();
        OpComputadora.addItem("Computadora 1");
        OpComputadora.addItem("Computadora 2");
        if (numComputadores >= 3) OpComputadora.addItem("Computadora 3");
        if (numComputadores >= 4) OpComputadora.addItem("Computadora 4");
        
        if (panelComp3 != null) panelComp3.setVisible(numComputadores >= 3);
        if (panelComp4 != null) panelComp4.setVisible(numComputadores >= 4);
        
                // --- AJUSTE DINÁMICO DEL DISEÑO ---
        if (numComputadores == 2) {
            // 1 fila, 2 columnas
            jPanel2.setLayout(new java.awt.GridLayout(1, 2));
        } else if (numComputadores == 3) {
            // 1 fila, 3 columnas (se estiran para ocupar todo el ancho)
            jPanel2.setLayout(new java.awt.GridLayout(1, 3));
        } else if (numComputadores == 4) {
            // 2 filas, 2 columnas (cuadrícula perfecta)
            jPanel2.setLayout(new java.awt.GridLayout(2, 2));
String seleccion = (String) jComboBox3.getSelectedItem();
        }
        
        // ARREGLO VISUAL DE LAS BARRAS DE RAM
        jProgressBar1.setMaximum(2048); 
        jProgressBar1.setStringPainted(true);
        jProgressBar1.setString("RAM: 0 / 2048");

        jProgressBar2.setMaximum(2048);
        jProgressBar2.setStringPainted(true);
        jProgressBar2.setString("RAM: 0 / 2048");

        // 2. CONFIGURAR EL SPINNER DE VELOCIDAD
        jSpinner1.setModel(new javax.swing.SpinnerNumberModel(500, 50, 5000, 50));
        jSpinner1.addChangeListener((e) -> {
            if (cluster != null) {
                int ms = (int) jSpinner1.getValue();
                cluster.getReloj().setDuracionCicloMs(ms);
                jLabel2.setText("Velocidad (ms): " + ms);
            }
        });

        // 3. EL TIMER QUE ACTUALIZA LA PANTALLA
        javax.swing.Timer timer = new javax.swing.Timer(100, (e) -> {
            if (cluster == null) return;

            jLabel1.setText("Ciclo Global: " + cluster.getReloj().getCiclo());

            // --- COMPUTADOR 1 ---
            PCB p1 = cluster.getComputador(0).getCpu().getProcesoActual();
            int ramUsada1 = cluster.getComputador(0).getRam().getUsada();
            
            if (p1 != null) {
                jLabel3.setText("CPU: " + p1.getNombre()); 
                jLabel4.setText("PC: " + p1.getPc() + " / Deadline: " + p1.getDeadline() + " / Prio: " + p1.getPrioridad());
                String modoActual = cluster.getComputador(0).getCpu().getModo().toString();
                jLabel5.setText("Modo: " + modoActual);
                
                if (modoActual.equals("SISTEMA")) {
                    jLabel5.setForeground(java.awt.Color.RED);
                } else {
                    jLabel5.setForeground(java.awt.Color.BLACK);
                }
                
            } else {
                jLabel3.setText("CPU: Libre");
                jLabel4.setText("PC: 0 / Deadline: 0");
                jLabel5.setText("Modo: Usuario");
                jLabel5.setForeground(java.awt.Color.BLACK); 
            }
           
            jProgressBar1.setValue(ramUsada1);
            jProgressBar1.setString("RAM: " + ramUsada1 + " / 2048");

            jTextArea1.setText("Nuevos: " + cluster.getComputador(0).getNucleo().getColaNuevos().getNombresProcesos() +
                   "\nListos: " + cluster.getComputador(0).getNucleo().getColaListos().getNombresProcesos() +
                   "\nBloqueados: " + cluster.getComputador(0).getNucleo().getColaBloqueados().getNombresProcesos() +
                   "\nTerminados: " + cluster.getComputador(0).getNucleo().getTerminados().getNombresProcesos());

            // --- COMPUTADOR 2 ---
            PCB p2 = cluster.getComputador(1).getCpu().getProcesoActual();
            int ramUsada2 = cluster.getComputador(1).getRam().getUsada();
            
            if (p2 != null) {
                jLabel6.setText("CPU: " + p2.getNombre()); 
                jLabel7.setText("PC: " + p2.getPc() + " / Dead: " + p2.getDeadline() + " / Prio: " + p2.getPrioridad());
                jLabel8.setText("Modo: " + cluster.getComputador(1).getCpu().getModo().toString());
            } else {
                jLabel6.setText("CPU: Libre");
                jLabel7.setText("PC: 0 / Deadline: 0");
                jLabel8.setText("Modo: Usuario");
            }
            
            jProgressBar2.setValue(ramUsada2);
            jProgressBar2.setString("RAM: " + ramUsada2 + " / 2048");

            jTextArea2.setText("Nuevos: " + cluster.getComputador(1).getNucleo().getColaNuevos().getNombresProcesos() +
                   "\nListos: " + cluster.getComputador(1).getNucleo().getColaListos().getNombresProcesos() +
                   "\nBloqueados: " + cluster.getComputador(1).getNucleo().getColaBloqueados().getNombresProcesos() +
                   "\nTerminados: " + cluster.getComputador(1).getNucleo().getTerminados().getNombresProcesos());
        
            // --- ACTUALIZAR EL LOG DE EVENTOS ---
            txtLog.setText(cluster.getLogEventos());
            if (cluster.getReloj().isCorriendo()) {
                txtLog.setCaretPosition(txtLog.getDocument().getLength());
            }
            
                        // --- COMPUTADOR 3 ---
            if (cluster.getCantidad() >= 3) {
                PCB p3 = cluster.getComputador(2).getCpu().getProcesoActual();
                int ramUsada3 = cluster.getComputador(2).getRam().getUsada();
                
                if (p3 != null) {
                    jLabel17.setText("CPU: " + p3.getNombre()); 
                    jLabel18.setText("PC: " + p3.getPc() + " / Dead: " + p3.getDeadline() + " / Prio: " + p3.getPrioridad());
                    String modoActual3 = cluster.getComputador(2).getCpu().getModo().toString();
                    jLabel19.setText("Modo: " + modoActual3);
                    
                    if (modoActual3.equals("SISTEMA")) {
                        jLabel19.setForeground(java.awt.Color.RED);
                    } else {
                        jLabel19.setForeground(java.awt.Color.BLACK);
                    }
                } else {
                    jLabel17.setText("CPU: Libre");
                    jLabel18.setText("PC: 0 / Deadline: 0");
                    jLabel19.setText("Modo: Usuario");
                    jLabel19.setForeground(java.awt.Color.BLACK); 
                }
                
                jProgressBar3.setValue(ramUsada3);
                jProgressBar3.setString("RAM: " + ramUsada3 + " / 2048");

                jTextArea3.setText("Nuevos: " + cluster.getComputador(2).getNucleo().getColaNuevos().getNombresProcesos() +
                       "\nListos: " + cluster.getComputador(2).getNucleo().getColaListos().getNombresProcesos() +
                       "\nBloqueados: " + cluster.getComputador(2).getNucleo().getColaBloqueados().getNombresProcesos() +
                       "\nTerminados: " + cluster.getComputador(2).getNucleo().getTerminados().getNombresProcesos());
            }

            // --- COMPUTADOR 4 ---
            if (cluster.getCantidad() >= 4) {
                PCB p4 = cluster.getComputador(3).getCpu().getProcesoActual();
                int ramUsada4 = cluster.getComputador(3).getRam().getUsada();
                
                if (p4 != null) {
                    jLabel20.setText("CPU: " + p4.getNombre()); 
                    jLabel21.setText("PC: " + p4.getPc() + " / Dead: " + p4.getDeadline() + " / Prio: " + p4.getPrioridad());
                    String modoActual4 = cluster.getComputador(3).getCpu().getModo().toString();
                    jLabel22.setText("Modo: " + modoActual4);
                    
                    if (modoActual4.equals("SISTEMA")) {
                        jLabel22.setForeground(java.awt.Color.RED);
                    } else {
                        jLabel22.setForeground(java.awt.Color.BLACK);
                    }
                } else {
                    jLabel20.setText("CPU: Libre");
                    jLabel21.setText("PC: 0 / Deadline: 0");
                    jLabel22.setText("Modo: Usuario");
                    jLabel22.setForeground(java.awt.Color.BLACK); 
                }
                
                jProgressBar4.setValue(ramUsada4);
                jProgressBar4.setString("RAM: " + ramUsada4 + " / 2048");

                // Asumo que el JTextArea del Comp 4 se llama jTextArea4 (si se llama distinto, cámbialo aquí)
                jTextArea4.setText("Nuevos: " + cluster.getComputador(3).getNucleo().getColaNuevos().getNombresProcesos() +
                       "\nListos: " + cluster.getComputador(3).getNucleo().getColaListos().getNombresProcesos() +
                       "\nBloqueados: " + cluster.getComputador(3).getNucleo().getColaBloqueados().getNombresProcesos() +
                       "\nTerminados: " + cluster.getComputador(3).getNucleo().getTerminados().getNombresProcesos());
            }
            
            
        }); // <---- AQUÍ CIERRA EL TIMER CORRECTAMENTE
        
        timer.start(); // <---- ARRANCA EL TIMER UNA SOLA VEZ
    }
    
    
    

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jSpinner1 = new javax.swing.JSpinner();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        Simulación = new javax.swing.JTabbedPane();
        jPanel2 = new javax.swing.JPanel();
        panelComp1 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jProgressBar1 = new javax.swing.JProgressBar();
        jComboBox1 = new javax.swing.JComboBox<>();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        panelComp2 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jProgressBar2 = new javax.swing.JProgressBar();
        jComboBox2 = new javax.swing.JComboBox<>();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTextArea2 = new javax.swing.JTextArea();
        panelComp3 = new javax.swing.JPanel();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jProgressBar3 = new javax.swing.JProgressBar();
        jComboBox3 = new javax.swing.JComboBox<>();
        jScrollPane5 = new javax.swing.JScrollPane();
        jTextArea3 = new javax.swing.JTextArea();
        panelComp4 = new javax.swing.JPanel();
        jLabel20 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        jProgressBar4 = new javax.swing.JProgressBar();
        jComboBox4 = new javax.swing.JComboBox<>();
        jScrollPane6 = new javax.swing.JScrollPane();
        jTextArea4 = new javax.swing.JTextArea();
        crearProceso = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        txtNombre = new javax.swing.JTextField();
        spnMemoria = new javax.swing.JSpinner();
        spnDeadline = new javax.swing.JSpinner();
        jLabel12 = new javax.swing.JLabel();
        spnInstrucciones = new javax.swing.JSpinner();
        jButton3 = new javax.swing.JButton();
        jLabel13 = new javax.swing.JLabel();
        cbxTipoProceso = new javax.swing.JComboBox<>();
        OpComputadora = new javax.swing.JComboBox<>();
        jLabel14 = new javax.swing.JLabel();
        cbxPrioridad = new javax.swing.JComboBox<>();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        spnQuantum = new javax.swing.JSpinner();
        btnGrafico = new javax.swing.JButton();
        acBuffer = new javax.swing.JButton();
        guardarJson = new javax.swing.JButton();
        cargarJson = new javax.swing.JButton();
        jScrollPane4 = new javax.swing.JScrollPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtLog = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel1.setText("Ciclo Global: 0");
        jPanel1.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 0, 170, 30));

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel2.setText("Velocidad (ms): 0");
        jPanel1.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 50, -1, -1));
        jPanel1.add(jSpinner1, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 30, -1, -1));

        jButton1.setText("INICIAR");
        jButton1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 30, -1, -1));

        jButton2.setText("PAUSAR");
        jButton2.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(420, 30, -1, -1));

        jPanel2.setLayout(new java.awt.GridLayout(1, 0));

        panelComp1.setBorder(javax.swing.BorderFactory.createTitledBorder("Computador 1"));
        panelComp1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel3.setText("CPU: Libre");
        panelComp1.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 70, -1, -1));

        jLabel4.setText("PC: 0 / Deadline: 0 ");
        panelComp1.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 130, 130, 20));

        jLabel5.setText("Modo: Usuario");
        panelComp1.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 200, -1, -1));

        jProgressBar1.setString("RAM: 0 /KB: 2048");
        jProgressBar1.setStringPainted(true);
        panelComp1.add(jProgressBar1, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 60, 130, 30));

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "FCFS", "Round Robin", "EDF", "Prioridades" }));
        jComboBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox1ActionPerformed(evt);
            }
        });
        panelComp1.add(jComboBox1, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 130, 80, -1));

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jTextArea1.setText("Nuevos: [ ]\nListos: [ ]\nBloqueados: [ ]\nTerminados: [ ]");
        jTextArea1.setEnabled(false);
        jScrollPane2.setViewportView(jTextArea1);

        panelComp1.add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 240, 210, 180));

        jPanel2.add(panelComp1);

        panelComp2.setBorder(javax.swing.BorderFactory.createTitledBorder("Computador 2"));
        panelComp2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel6.setText("CPU: Libre");
        panelComp2.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 70, -1, -1));

        jLabel7.setText("PC: 0 / Deadline: 0");
        panelComp2.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 120, 170, 20));

        jLabel8.setText("Modo: Usuario");
        panelComp2.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 210, -1, -1));

        jProgressBar2.setString("RAM: 0 /KB: 2048");
        jProgressBar2.setStringPainted(true);
        panelComp2.add(jProgressBar2, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 70, 130, 30));

        jComboBox2.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "FCFS", "Round Robin", "EDF", "Prioridades" }));
        jComboBox2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox2ActionPerformed(evt);
            }
        });
        panelComp2.add(jComboBox2, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 150, 70, -1));

        jTextArea2.setColumns(20);
        jTextArea2.setRows(5);
        jTextArea2.setText("Nuevos: [ ]\nListos: [ ]\nBloqueados: [ ]\nTerminados: [ ]");
        jTextArea2.setEnabled(false);
        jScrollPane3.setViewportView(jTextArea2);

        panelComp2.add(jScrollPane3, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 250, 200, 180));

        jPanel2.add(panelComp2);

        panelComp3.setBorder(javax.swing.BorderFactory.createTitledBorder("Computador 2"));
        panelComp3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel17.setText("CPU: Libre");
        panelComp3.add(jLabel17, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 70, -1, -1));

        jLabel18.setText("PC: 0 / Deadline: 0");
        panelComp3.add(jLabel18, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 120, 160, 20));

        jLabel19.setText("Modo: Usuario");
        panelComp3.add(jLabel19, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 200, -1, -1));

        jProgressBar3.setString("RAM: 0 /KB: 2048");
        jProgressBar3.setStringPainted(true);
        panelComp3.add(jProgressBar3, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 60, 130, 30));

        jComboBox3.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "FCFS", "Round Robin", "EDF", "Prioridades" }));
        jComboBox3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox3ActionPerformed(evt);
            }
        });
        panelComp3.add(jComboBox3, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 120, 70, -1));

        jTextArea3.setColumns(20);
        jTextArea3.setRows(5);
        jTextArea3.setText("Nuevos: [ ]\nListos: [ ]\nBloqueados: [ ]\nTerminados: [ ]");
        jTextArea3.setEnabled(false);
        jScrollPane5.setViewportView(jTextArea3);

        panelComp3.add(jScrollPane5, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 230, 200, 180));

        jPanel2.add(panelComp3);
        panelComp3.getAccessibleContext().setAccessibleName("Computador 3");

        panelComp4.setBorder(javax.swing.BorderFactory.createTitledBorder("Computador 2"));
        panelComp4.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel20.setText("CPU: Libre");
        panelComp4.add(jLabel20, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 70, -1, -1));

        jLabel21.setText("PC: 0 / Deadline: 0");
        panelComp4.add(jLabel21, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 120, 150, 20));

        jLabel22.setText("Modo: Usuario");
        panelComp4.add(jLabel22, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 180, -1, -1));

        jProgressBar4.setString("RAM: 0 /KB: 2048");
        jProgressBar4.setStringPainted(true);
        panelComp4.add(jProgressBar4, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 60, 120, 30));

        jComboBox4.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "FCFS", "Round Robin", "EDF", "Prioridades" }));
        jComboBox4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox4ActionPerformed(evt);
            }
        });
        panelComp4.add(jComboBox4, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 120, 70, -1));

        jTextArea4.setColumns(20);
        jTextArea4.setRows(5);
        jTextArea4.setText("Nuevos: [ ]\nListos: [ ]\nBloqueados: [ ]\nTerminados: [ ]");
        jTextArea4.setEnabled(false);
        jScrollPane6.setViewportView(jTextArea4);

        panelComp4.add(jScrollPane6, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 220, 200, 180));

        jPanel2.add(panelComp4);
        panelComp4.getAccessibleContext().setAccessibleName("Computador 3");

        crearProceso.setBorder(javax.swing.BorderFactory.createTitledBorder("Crear Proceso"));
        crearProceso.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel9.setText("Nombre del Proceso:");
        crearProceso.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 30, -1, -1));

        jLabel10.setText("Memoria: ");
        crearProceso.add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 220, -1, -1));

        jLabel11.setText("Computadora:");
        crearProceso.add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 340, -1, -1));

        txtNombre.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtNombreActionPerformed(evt);
            }
        });
        crearProceso.add(txtNombre, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 30, 80, -1));

        spnMemoria.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        crearProceso.add(spnMemoria, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 220, 100, -1));

        spnDeadline.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        crearProceso.add(spnDeadline, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 260, 90, -1));

        jLabel12.setText("Deadline: ");
        crearProceso.add(jLabel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 260, -1, -1));

        spnInstrucciones.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        crearProceso.add(spnInstrucciones, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 300, 100, -1));

        jButton3.setText("Crear Proceso");
        jButton3.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });
        crearProceso.add(jButton3, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 390, -1, -1));

        jLabel13.setText("Tipo de proceso:");
        crearProceso.add(jLabel13, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 80, -1, -1));

        cbxTipoProceso.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "CPU_BOUND", "IO_BOUND", "PRODUCTOR", "CONSUMIDOR" }));
        cbxTipoProceso.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        cbxTipoProceso.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbxTipoProcesoActionPerformed(evt);
            }
        });
        crearProceso.add(cbxTipoProceso, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 80, 80, -1));

        OpComputadora.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Computadora 1", "Computadora 2" }));
        OpComputadora.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        OpComputadora.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                OpComputadoraActionPerformed(evt);
            }
        });
        crearProceso.add(OpComputadora, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 340, -1, -1));

        jLabel14.setText("Instrucciones: ");
        crearProceso.add(jLabel14, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 300, -1, -1));

        cbxPrioridad.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "ALTA", "MEDIA", "BAJA" }));
        cbxPrioridad.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbxPrioridadActionPerformed(evt);
            }
        });
        crearProceso.add(cbxPrioridad, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 170, 120, 30));

        jLabel15.setText("Prioridad:");
        crearProceso.add(jLabel15, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 180, -1, -1));

        jLabel16.setText("Quantum: ");
        crearProceso.add(jLabel16, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 130, -1, -1));

        spnQuantum.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spnQuantumStateChanged(evt);
            }
        });
        crearProceso.add(spnQuantum, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 130, 90, -1));

        jPanel2.add(crearProceso);

        Simulación.addTab("Simulación", jPanel2);

        jPanel1.add(Simulación, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 110, 1100, 500));

        btnGrafico.setText("GRAFICOS");
        btnGrafico.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGraficoActionPerformed(evt);
            }
        });
        jPanel1.add(btnGrafico, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 80, -1, -1));

        acBuffer.setText("BUFFER");
        acBuffer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                acBufferActionPerformed(evt);
            }
        });
        jPanel1.add(acBuffer, new org.netbeans.lib.awtextra.AbsoluteConstraints(540, 30, -1, -1));

        guardarJson.setText("GuardarJson");
        guardarJson.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                guardarJsonActionPerformed(evt);
            }
        });
        jPanel1.add(guardarJson, new org.netbeans.lib.awtextra.AbsoluteConstraints(530, 80, -1, -1));

        cargarJson.setText("CargarJson");
        cargarJson.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cargarJsonActionPerformed(evt);
            }
        });
        jPanel1.add(cargarJson, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 80, -1, -1));

        txtLog.setColumns(20);
        txtLog.setRows(5);
        jScrollPane1.setViewportView(txtLog);

        jScrollPane4.setViewportView(jScrollPane1);

        jPanel1.add(jScrollPane4, new org.netbeans.lib.awtextra.AbsoluteConstraints(750, 20, 320, 120));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    
    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
       if(cluster != null){
           cluster.iniciar();
           jLabel2.setText("Velocidad (ms): " + jSpinner1.getValue());
           jButton1.setEnabled(false);
           jButton2.setEnabled(true);
       }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        if (cluster != null) {
            cluster.detener();
            jButton1.setEnabled(true);
            jButton2.setEnabled(false);
        }
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
         if (cluster == null) return;
        String seleccion = (String) jComboBox1.getSelectedItem();
        PoliticaPlanificacion nuevaPolitica = null;
        
        switch (seleccion) {
            case "FCFS": nuevaPolitica = new FCFS(); break;
            case "Round Robin": 
                int q = (int) spnQuantum.getValue(); 
                nuevaPolitica = new RoundRobin(q); 
                break; 
            case "EDF": nuevaPolitica = new EDF(); break;
            case "Prioridades": nuevaPolitica = new PrioridadApropiativa(); break;
        }
        
        if (nuevaPolitica != null) {
            cluster.getComputador(0).setPolitica(nuevaPolitica);
        }
    }//GEN-LAST:event_jComboBox1ActionPerformed
   
    private void jComboBox2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox2ActionPerformed
         if (cluster == null) return;
        String seleccion = (String) jComboBox2.getSelectedItem();
        PoliticaPlanificacion nuevaPolitica = null;
        
        switch (seleccion) {
            case "FCFS": nuevaPolitica = new FCFS(); break;
            case "Round Robin": 
                // Leemos el quantum del Spinner
                int q = (int) spnQuantum.getValue(); 
                nuevaPolitica = new RoundRobin(q); 
                break; 
            case "EDF": nuevaPolitica = new EDF(); break;
            case "Prioridades": nuevaPolitica = new PrioridadApropiativa(); break;
        }
        
        if (nuevaPolitica != null) {
            cluster.getComputador(1).setPolitica(nuevaPolitica);
        } 
    }//GEN-LAST:event_jComboBox2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
         try {
            // 1. Atrapamos los datos
            String nombre = txtNombre.getText(); 
            int memoria = (int) spnMemoria.getValue();
            int deadline = (int) spnDeadline.getValue();
            int instrucciones = (int) spnInstrucciones.getValue();

            // 2. Validamos que el nombre no esté vacío
            if (nombre.trim().isEmpty()) {
                javax.swing.JOptionPane.showMessageDialog(this, "Escribe un nombre");
                return;
            }

            // 3. VALIDACIÓN: Evitar valores negativos o cero
            if (memoria <= 0 || deadline <= 0 || instrucciones <= 0) {
                javax.swing.JOptionPane.showMessageDialog(this, 
                    "Error: La memoria, el deadline y las instrucciones deben ser mayores a 0.",
                    "Valores Inválidos", 
                    javax.swing.JOptionPane.WARNING_MESSAGE);
                return; 
            }

            // 4. Determinamos el Tipo de Proceso
            losEnums.TipoProceso tipo = losEnums.TipoProceso.CPU_BOUND;
            String tipoSeleccionado = cbxTipoProceso.getSelectedItem().toString();
            
            if (tipoSeleccionado.equals("IO_BOUND")) {
                tipo = losEnums.TipoProceso.IO_BOUND;
            } else if (tipoSeleccionado.equals("PRODUCTOR")) {
                tipo = losEnums.TipoProceso.PRODUCTOR;
            } else if (tipoSeleccionado.equals("CONSUMIDOR")) {
                tipo = losEnums.TipoProceso.CONSUMIDOR;
            }

            // 5. Determinamos a qué computador va
            int indiceComp = OpComputadora.getSelectedIndex(); 
            int idComp = indiceComp + 1; 
            
            // Validación la cual el proceso tiene más memoria que la del computador
             int ramTotalComputador = cluster.getComputador(indiceComp).getRam().getTotal();
             
             if (memoria > ramTotalComputador) {
                javax.swing.JOptionPane.showMessageDialog(this, 
                    "Error: El proceso tiene " + memoria + " KB de memoria, pero el Computador " 
                    + idComp + " solo tiene " + ramTotalComputador + " KB en total.\n" +
                    "El proceso no sera admitido.",
                    "Memoria Insuficiente", 
                    javax.swing.JOptionPane.ERROR_MESSAGE);
                return; // Detenemos la ejecución para que no se cree el proceso
            }

            // 6. Determinamos la Prioridad
            int prioridad = 2; // Media por defecto
            String prioStr = cbxPrioridad.getSelectedItem().toString();
            if (prioStr.equals("ALTA")) {
                prioridad = sistemaOperativo.PCB.PRIORIDAD_ALTA; 
            } else if (prioStr.equals("BAJA")) {
                prioridad = sistemaOperativo.PCB.PRIORIDAD_BAJA; 
            }

            // 7. LÓGICA PARA SELECCIONAR EL BUFFER (FILTRADO POR COMPUTADORA)
            int idBufferElegido = 1; // Por defecto si no es productor/consumidor
            
            if (tipo == losEnums.TipoProceso.PRODUCTOR || tipo == losEnums.TipoProceso.CONSUMIDOR) {
                estructuraDeDatos.ListaSimple listaBuffers = cluster.getGestorBuffers().getLista();
                
                // 1. Primero contamos cuántos buffers pertenecen a ESTE computador
                int contadorLocal = 0;
                for (int i = 0; i < listaBuffers.getSize(); i++) {
                    sistemaOperativo.Buffer b = (sistemaOperativo.Buffer) listaBuffers.getValue(i);
                    if (b.getAnfitrion() == idComp) { // Si el dueño del buffer es el computador elegido
                        contadorLocal++;
                    }
                }

                // Si no hay buffers locales, no podemos crear el productor/consumidor aquí
                if (contadorLocal == 0) {
                    javax.swing.JOptionPane.showMessageDialog(this, 
                        "Error: No hay buffers creados en el Computador " + idComp + ".\n" +
                        "Crea un buffer primero o cambia el proceso a otro computador.", 
                        "Sin buffers locales", 
                        javax.swing.JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // 2. Creamos el arreglo solo con los buffers locales
                String[] opcionesBuffer = new String[contadorLocal];
                int[] idsBuffersReales = new int[contadorLocal]; // Para guardar el ID real
                
                int j = 0;
                for (int i = 0; i < listaBuffers.getSize(); i++) {
                    sistemaOperativo.Buffer b = (sistemaOperativo.Buffer) listaBuffers.getValue(i);
                    if (b.getAnfitrion() == idComp) {
                        opcionesBuffer[j] = "Buffer " + b.getId() + " (Local)";
                        idsBuffersReales[j] = b.getId(); // Guardamos el ID para usarlo luego
                        j++;
                    }
                }

                // 3. Mostramos la ventana con las opciones filtradas
                int seleccion = javax.swing.JOptionPane.showOptionDialog(
                        this,
                        "¿A qué buffer local del Computador " + idComp + " deseas asignar este " + tipoSeleccionado + "?",
                        "Seleccionar Buffer Local",
                        javax.swing.JOptionPane.DEFAULT_OPTION, 
                        javax.swing.JOptionPane.QUESTION_MESSAGE,
                        null,
                        opcionesBuffer,
                        opcionesBuffer[0]);

                if (seleccion == -1) {
                    return; // El usuario canceló
                }

                // 4. Obtenemos el ID real del buffer elegido usando el arreglo auxiliar
                idBufferElegido = idsBuffersReales[seleccion];
            }

            // 8. Creamos el proceso
            sistemaOperativo.PCB p = new sistemaOperativo.PCB(nombre, tipo, prioridad, memoria, deadline, instrucciones, idComp);
            
            if (tipo == losEnums.TipoProceso.IO_BOUND) {
                p.setCiclosPorElemento(3);    
                p.setCiclosParaSatisfacer(2); 
            }
            
            if (tipo == losEnums.TipoProceso.PRODUCTOR || tipo == losEnums.TipoProceso.CONSUMIDOR) {
                p.setBufferAsociado(idBufferElegido); // Asigna el buffer que el usuario eligió
                p.setCiclosPorElemento(3);    
                p.setElementosRestantes(5);   
            }
            
            // 9. Lo mandamos al Núcleo
            cluster.getComputador(indiceComp).getNucleo().admitir(p);
            
            // 10. Mensaje de éxito
            javax.swing.JOptionPane.showMessageDialog(this, "¡Proceso " + nombre + " creado en el Computador " + idComp + "!");
            txtNombre.setText("");
            
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
                

            


    }//GEN-LAST:event_jButton3ActionPerformed

    private void txtNombreActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtNombreActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtNombreActionPerformed

    private void cbxTipoProcesoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxTipoProcesoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cbxTipoProcesoActionPerformed

    private void OpComputadoraActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_OpComputadoraActionPerformed
        
    }//GEN-LAST:event_OpComputadoraActionPerformed

    private void btnGraficoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGraficoActionPerformed

        VentanaGrafico vg = new VentanaGrafico();
        vg.cluster = this.cluster;        
        vg.setVisible(true);   // TODO add your handling code here:
    }//GEN-LAST:event_btnGraficoActionPerformed

    private void acBufferActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_acBufferActionPerformed
ventanaBuffer vb = new ventanaBuffer();
vb.cluster = this.cluster; // Le pasas el mismo clúster
vb.setVisible(true);       // La muestras        // TODO add your handling code here:
    }//GEN-LAST:event_acBufferActionPerformed

    private void cbxPrioridadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxPrioridadActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cbxPrioridadActionPerformed

    private void guardarJsonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_guardarJsonActionPerformed
          try {
            sistemaOperativo.configuracion config = new sistemaOperativo.configuracion();
            
            config.duracionCicloMs = (int) jSpinner1.getValue();
            config.latenciaRed = 3;

            sistemaOperativo.configuracion.ProcesoDTO pDto = new sistemaOperativo.configuracion.ProcesoDTO();
            pDto.nombre = txtNombre.getText().isEmpty() ? "Proceso_Ejemplo" : txtNombre.getText();
            pDto.tipo = cbxTipoProceso.getSelectedItem().toString();
            pDto.memoria = (int) spnMemoria.getValue();
            pDto.deadline = (int) spnDeadline.getValue();
            pDto.instrucciones = (int) spnInstrucciones.getValue();
            pDto.computador = OpComputadora.getSelectedIndex() + 1;

            String prioStr = cbxPrioridad.getSelectedItem().toString();
            if (prioStr.equals("ALTA")) pDto.prioridad = sistemaOperativo.PCB.PRIORIDAD_ALTA;
            else if (prioStr.equals("BAJA")) pDto.prioridad = sistemaOperativo.PCB.PRIORIDAD_BAJA;
            else pDto.prioridad = sistemaOperativo.PCB.PRIORIDAD_MEDIA;

            config.procesosIniciales = new sistemaOperativo.configuracion.ProcesoDTO[1];
            config.procesosIniciales[0] = pDto;

            // Guardar buffers
            estructuraDeDatos.ListaSimple listaBuffers = cluster.getGestorBuffers().getLista();
            config.buffersIniciales = new sistemaOperativo.configuracion.BufferDTO[listaBuffers.getSize()];
            for (int i = 0; i < listaBuffers.getSize(); i++) {
                sistemaOperativo.Buffer b = (sistemaOperativo.Buffer) listaBuffers.getValue(i);
                sistemaOperativo.configuracion.BufferDTO bDto = new sistemaOperativo.configuracion.BufferDTO();
                bDto.capacidad = b.getCapacidad();
                bDto.anfitrion = b.getAnfitrion();
                config.buffersIniciales[i] = bDto;
            }

            // --- VENTANA PARA ELEGIR EL ARCHIVO ---
            javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser();
            fileChooser.setDialogTitle("Guardar Escenario");
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Archivos JSON (*.json)", "json"));
            
            // Si el usuario elige una ruta y le da a Guardar
            if (fileChooser.showSaveDialog(this) == javax.swing.JFileChooser.APPROVE_OPTION) {
                java.io.File archivo = fileChooser.getSelectedFile();
                
                // Asegurarnos de que termine en .json
                if (!archivo.getName().toLowerCase().endsWith(".json")) {
                    archivo = new java.io.File(archivo.getParentFile(), archivo.getName() + ".json");
                }

                com.google.gson.Gson gson = new com.google.gson.GsonBuilder().setPrettyPrinting().create();
                java.io.FileWriter writer = new java.io.FileWriter(archivo);
                gson.toJson(config, writer);
                writer.close();

                javax.swing.JOptionPane.showMessageDialog(this, "¡Escenario guardado exitosamente en:\n" + archivo.getAbsolutePath());
            }
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this, "Error al guardar: " + e.getMessage());
        } // TODO add your handling code here:
    }//GEN-LAST:event_guardarJsonActionPerformed

    private void cargarJsonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cargarJsonActionPerformed
        try {
            // --- VENTANA PARA ELEGIR EL ARCHIVO A CARGAR ---
            javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser();
            fileChooser.setDialogTitle("Cargar Escenario");
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Archivos JSON (*.json)", "json"));

            if (fileChooser.showOpenDialog(this) == javax.swing.JFileChooser.APPROVE_OPTION) {
                java.io.File archivo = fileChooser.getSelectedFile();

                com.google.gson.Gson gson = new com.google.gson.Gson();
                java.io.FileReader reader = new java.io.FileReader(archivo);
                sistemaOperativo.configuracion config = gson.fromJson(reader, sistemaOperativo.configuracion.class);
                reader.close();

                if (config == null) return;

                // Aplicar la velocidad al sistema
                jSpinner1.setValue(config.duracionCicloMs);

                // Rellenar los campos de la interfaz con el proceso guardado
                if (config.procesosIniciales != null && config.procesosIniciales.length > 0) {
                    sistemaOperativo.configuracion.ProcesoDTO pDto = config.procesosIniciales[0];
                    
                    txtNombre.setText(pDto.nombre);
                    spnMemoria.setValue(pDto.memoria);
                    spnDeadline.setValue(pDto.deadline);
                    spnInstrucciones.setValue(pDto.instrucciones);
                    OpComputadora.setSelectedIndex(pDto.computador - 1);
                    cbxTipoProceso.setSelectedItem(pDto.tipo);
                    
                    if (pDto.prioridad == sistemaOperativo.PCB.PRIORIDAD_ALTA) cbxPrioridad.setSelectedItem("ALTA");
                    else if (pDto.prioridad == sistemaOperativo.PCB.PRIORIDAD_BAJA) cbxPrioridad.setSelectedItem("BAJA");
                    else cbxPrioridad.setSelectedItem("MEDIA");

                    javax.swing.JOptionPane.showMessageDialog(this, "¡Escenario cargado!\nRevisa los datos y presiona 'Crear Proceso'.");
                }
            }
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this, "Error al cargar el archivo: " + e.getMessage());
        } 
         // TODO add your handling code here:
    }//GEN-LAST:event_cargarJsonActionPerformed

    private void spnQuantumStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spnQuantumStateChanged
      if (cluster == null) return;
        int q = (int) spnQuantum.getValue();
        
        // Actualiza el Computador 1 si está en Round Robin
        if (jComboBox1.getSelectedItem().toString().equals("Round Robin")) {
            cluster.getComputador(0).setPolitica(new planificacionSO.RoundRobin(q));
        }
        // Actualiza el Computador 2 si está en Round Robin
        if (jComboBox2.getSelectedItem().toString().equals("Round Robin")) {
            cluster.getComputador(1).setPolitica(new planificacionSO.RoundRobin(q));
            
            
        }   // TODO add your handling code here:
    }//GEN-LAST:event_spnQuantumStateChanged

    private void jComboBox3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox3ActionPerformed
          if (cluster == null) return;
        String seleccion = (String) jComboBox3.getSelectedItem();
        PoliticaPlanificacion nuevaPolitica = null;
        switch (seleccion) {
            case "FCFS": nuevaPolitica = new FCFS(); break;
            case "Round Robin": nuevaPolitica = new RoundRobin((int) spnQuantum.getValue()); break; 
            case "EDF": nuevaPolitica = new EDF(); break;
            case "Prioridades": nuevaPolitica = new PrioridadApropiativa(); break;
        }
        if (nuevaPolitica != null) cluster.getComputador(2).setPolitica(nuevaPolitica);
     
    }//GEN-LAST:event_jComboBox3ActionPerformed

    private void jComboBox4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox4ActionPerformed
        if (cluster == null) return;
        
        // ¡OJO AQUÍ! Ahora sí lee el ComboBox 4
        String seleccion = (String) jComboBox4.getSelectedItem();
        PoliticaPlanificacion nuevaPolitica = null;
        
        switch (seleccion) {
            case "FCFS": nuevaPolitica = new FCFS(); break;
            case "Round Robin": nuevaPolitica = new RoundRobin((int) spnQuantum.getValue()); break; 
            case "EDF": nuevaPolitica = new EDF(); break;
            case "Prioridades": nuevaPolitica = new PrioridadApropiativa(); break;
        }
        
        if (nuevaPolitica != null) {
            cluster.getComputador(3).setPolitica(nuevaPolitica); // Índice 3 = Computador 4
        }
     // <--- SOLO UNA LLAVE AQUÍ para cerrar el método
    }//GEN-LAST:event_jComboBox4ActionPerformed

   

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<String> OpComputadora;
    private javax.swing.JTabbedPane Simulación;
    private javax.swing.JButton acBuffer;
    private javax.swing.JButton btnGrafico;
    private javax.swing.JButton cargarJson;
    private javax.swing.JComboBox<String> cbxPrioridad;
    private javax.swing.JComboBox<String> cbxTipoProceso;
    private javax.swing.JPanel crearProceso;
    private javax.swing.JButton guardarJson;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JComboBox<String> jComboBox2;
    private javax.swing.JComboBox<String> jComboBox3;
    private javax.swing.JComboBox<String> jComboBox4;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JProgressBar jProgressBar1;
    private javax.swing.JProgressBar jProgressBar2;
    private javax.swing.JProgressBar jProgressBar3;
    private javax.swing.JProgressBar jProgressBar4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JSpinner jSpinner1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextArea jTextArea2;
    private javax.swing.JTextArea jTextArea3;
    private javax.swing.JTextArea jTextArea4;
    private javax.swing.JPanel panelComp1;
    private javax.swing.JPanel panelComp2;
    private javax.swing.JPanel panelComp3;
    private javax.swing.JPanel panelComp4;
    private javax.swing.JSpinner spnDeadline;
    private javax.swing.JSpinner spnInstrucciones;
    private javax.swing.JSpinner spnMemoria;
    private javax.swing.JSpinner spnQuantum;
    private javax.swing.JTextArea txtLog;
    private javax.swing.JTextField txtNombre;
    // End of variables declaration//GEN-END:variables



}
