/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
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

public class Interfaz extends javax.swing.JFrame {

     public sistemaOperativo.Cluster cluster;

    public Interfaz() {
        initComponents();
        spnMemoria.setModel(new javax.swing.SpinnerNumberModel(1, 1, 4096, 1));
        spnDeadline.setModel(new javax.swing.SpinnerNumberModel(1, 1, 1000, 1));
        spnInstrucciones.setModel(new javax.swing.SpinnerNumberModel(1, 1, 1000, 1));
        
         
        jButton2.setEnabled(false); // PAUSAR deshabilitado al inicio
        
        // 1. CREAR EL CLUSTER
        cluster = new sistemaOperativo.Cluster(2, 2048, 500);
        
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
                jLabel4.setText("PC: " + p1.getPc() + " / Deadline: " + p1.getDeadline());
                jLabel5.setText("Modo: " + cluster.getComputador(0).getCpu().getModo().toString());
            } else {
                jLabel3.setText("CPU: Libre");
                jLabel4.setText("PC: 0 / Deadline: 0");
                jLabel5.setText("Modo: Usuario");
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
                jLabel7.setText("PC: " + p2.getPc() + " / Deadline: " + p2.getDeadline());
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
        });
    
        timer.start();
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
        jButton4 = new javax.swing.JButton();
        acBuffer = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel1.setText("Ciclo Global: 0");
        jPanel1.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 40, 170, 30));

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel2.setText("Velocidad (ms): 0");
        jPanel1.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 50, -1, -1));
        jPanel1.add(jSpinner1, new org.netbeans.lib.awtextra.AbsoluteConstraints(380, 50, -1, -1));

        jButton1.setText("INICIAR");
        jButton1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(540, 50, -1, -1));

        jButton2.setText("PAUSAR");
        jButton2.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(700, 50, -1, -1));

        jPanel2.setLayout(new java.awt.GridLayout(1, 0));

        panelComp1.setBorder(javax.swing.BorderFactory.createTitledBorder("Computador 1"));
        panelComp1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel3.setText("CPU: Libre");
        panelComp1.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 100, -1, -1));

        jLabel4.setText("PC: 0 / Deadline: 0");
        panelComp1.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 160, -1, -1));

        jLabel5.setText("Modo: Usuario");
        panelComp1.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 230, -1, -1));

        jProgressBar1.setString("RAM: 0 /KB: 2048");
        jProgressBar1.setStringPainted(true);
        panelComp1.add(jProgressBar1, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 90, 170, 30));

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "FCFS", "Round Robin", "EDF", "Prioridades" }));
        jComboBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox1ActionPerformed(evt);
            }
        });
        panelComp1.add(jComboBox1, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 160, 110, -1));

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jTextArea1.setText("Nuevos: [ ]\nListos: [ ]\nBloqueados: [ ]\nTerminados: [ ]");
        jTextArea1.setEnabled(false);
        jScrollPane2.setViewportView(jTextArea1);

        panelComp1.add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 210, 210, 180));

        jPanel2.add(panelComp1);

        panelComp2.setBorder(javax.swing.BorderFactory.createTitledBorder("Computador 2"));
        panelComp2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel6.setText("CPU: Libre");
        panelComp2.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 90, -1, -1));

        jLabel7.setText("PC: 0 / Deadline: 0");
        panelComp2.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 160, -1, -1));

        jLabel8.setText("Modo: Usuario");
        panelComp2.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 230, -1, -1));

        jProgressBar2.setString("RAM: 0 /KB: 2048");
        jProgressBar2.setStringPainted(true);
        panelComp2.add(jProgressBar2, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 80, 180, 30));

        jComboBox2.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "FCFS", "Round Robin", "EDF", "Prioridades" }));
        jComboBox2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox2ActionPerformed(evt);
            }
        });
        panelComp2.add(jComboBox2, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 160, 110, -1));

        jTextArea2.setColumns(20);
        jTextArea2.setRows(5);
        jTextArea2.setText("Nuevos: [ ]\nListos: [ ]\nBloqueados: [ ]\nTerminados: [ ]");
        jTextArea2.setEnabled(false);
        jScrollPane3.setViewportView(jTextArea2);

        panelComp2.add(jScrollPane3, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 210, 200, 180));

        jPanel2.add(panelComp2);

        crearProceso.setBorder(javax.swing.BorderFactory.createTitledBorder("Crear Proceso"));
        crearProceso.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel9.setText("Nombre del Proceso:");
        crearProceso.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 50, -1, -1));

        jLabel10.setText("Memoria: ");
        crearProceso.add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 160, -1, -1));

        jLabel11.setText("Computadora:");
        crearProceso.add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 300, -1, -1));

        txtNombre.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtNombreActionPerformed(evt);
            }
        });
        crearProceso.add(txtNombre, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 50, 100, -1));

        spnMemoria.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        crearProceso.add(spnMemoria, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 160, 100, -1));

        spnDeadline.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        crearProceso.add(spnDeadline, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 210, 90, -1));

        jLabel12.setText("Deadline: ");
        crearProceso.add(jLabel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 210, -1, -1));

        spnInstrucciones.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        crearProceso.add(spnInstrucciones, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 260, 100, -1));

        jButton3.setText("Crear Proceso");
        jButton3.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });
        crearProceso.add(jButton3, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 340, -1, -1));

        jLabel13.setText("Tipo de proceso:");
        crearProceso.add(jLabel13, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 110, -1, -1));

        cbxTipoProceso.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "CPU_BOUND", "IO_BOUND", "PRODUCTOR", "CONSUMIDOR" }));
        cbxTipoProceso.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        cbxTipoProceso.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbxTipoProcesoActionPerformed(evt);
            }
        });
        crearProceso.add(cbxTipoProceso, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 110, -1, -1));

        OpComputadora.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Computadora 1", "Computadora 2" }));
        OpComputadora.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        OpComputadora.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                OpComputadoraActionPerformed(evt);
            }
        });
        crearProceso.add(OpComputadora, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 300, -1, -1));

        jLabel14.setText("Instrucciones: ");
        crearProceso.add(jLabel14, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 260, -1, -1));

        jPanel2.add(crearProceso);

        Simulación.addTab("Simulación", jPanel2);

        jPanel1.add(Simulación, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 90, 960, 440));

        jButton4.setText("SIMULACION");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton4, new org.netbeans.lib.awtextra.AbsoluteConstraints(860, 60, -1, -1));

        acBuffer.setText("BUFFER");
        acBuffer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                acBufferActionPerformed(evt);
            }
        });
        jPanel1.add(acBuffer, new org.netbeans.lib.awtextra.AbsoluteConstraints(870, 10, -1, -1));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 532, Short.MAX_VALUE)
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
            case "Round Robin": nuevaPolitica = new RoundRobin(3); break; 
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
            case "Round Robin": nuevaPolitica = new RoundRobin(3); break; 
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
                return; // Detiene la creación del proceso
            }

            // 4. Determinamos el Tipo de Proceso (Ahora atrapa los 4 tipos)
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

            // 6. Creamos el proceso
            sistemaOperativo.PCB p = new sistemaOperativo.PCB(nombre, tipo, 2, memoria, deadline, instrucciones, idComp);
            
            // Si es IO_BOUND, le configuramos la E/S
            if (tipo == losEnums.TipoProceso.IO_BOUND) {
                p.setCiclosPorElemento(3);    
                p.setCiclosParaSatisfacer(2); 
            }
            // NUEVO: Si es PRODUCTOR o CONSUMIDOR, asignarle el buffer
            if (tipo == losEnums.TipoProceso.PRODUCTOR || tipo == losEnums.TipoProceso.CONSUMIDOR) {
                p.setBufferAsociado(1);       // Usará el Buffer #1
                p.setCiclosPorElemento(3);    // Producirá/Consumirá un elemento cada 3 ciclos
                p.setElementosRestantes(5);   // Va a producir/consumir 5 elementos en total
                }
            
            // 7. Lo mandamos al Núcleo
            cluster.getComputador(indiceComp).getNucleo().admitir(p);
            
            // 8. Mostramos mensaje de éxito y limpiamos
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

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton4ActionPerformed

    private void acBufferActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_acBufferActionPerformed
ventanaBuffer vb = new ventanaBuffer();
vb.cluster = this.cluster; // Le pasas el mismo clúster
vb.setVisible(true);       // La muestras        // TODO add your handling code here:
    }//GEN-LAST:event_acBufferActionPerformed

   

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<String> OpComputadora;
    private javax.swing.JTabbedPane Simulación;
    private javax.swing.JButton acBuffer;
    private javax.swing.JComboBox<String> cbxTipoProceso;
    private javax.swing.JPanel crearProceso;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JComboBox<String> jComboBox2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel2;
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
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSpinner jSpinner1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextArea jTextArea2;
    private javax.swing.JPanel panelComp1;
    private javax.swing.JPanel panelComp2;
    private javax.swing.JSpinner spnDeadline;
    private javax.swing.JSpinner spnInstrucciones;
    private javax.swing.JSpinner spnMemoria;
    private javax.swing.JTextField txtNombre;
    // End of variables declaration//GEN-END:variables



}
