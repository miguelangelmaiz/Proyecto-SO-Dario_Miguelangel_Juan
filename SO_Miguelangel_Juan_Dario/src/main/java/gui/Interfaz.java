
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
      private int ultimaVersionLog = -1;

    public Interfaz(int numComputadores) {
         initComponents();
        spnMemoria.setModel(new javax.swing.SpinnerNumberModel(1, 1, 4096, 1));
        spnDeadline.setModel(new javax.swing.SpinnerNumberModel(1, 1, 5000, 1));
        spnInstrucciones.setModel(new javax.swing.SpinnerNumberModel(1, 1, 1000, 1));
        spnQuantum.setModel(new javax.swing.SpinnerNumberModel(1, 1, 1000, 1));
        
        jButton2.setEnabled(false); // PAUSAR deshabilitado al inicio
        
        // 1. CREAR EL CLUSTER
        cluster = new sistemaOperativo.Cluster(numComputadores, 2048, 500);
         OpComputadora.removeAllItems();
        for (int i = 0; i < numComputadores; i++) {
            OpComputadora.addItem("Computadora " + (i + 1));
        }
        
        // Ocultar o mostrar según el número elegido
        
        
        // Agregar las opciones al ComboBox de Crear Proceso
        OpComputadora.removeAllItems();
        OpComputadora.addItem("Computadora 1");
        OpComputadora.addItem("Computadora 2");
        if (numComputadores >= 3) OpComputadora.addItem("Computadora 3");
        if (numComputadores >= 4) OpComputadora.addItem("Computadora 4");
        
        
      
        // ARREGLO VISUAL DE LAS BARRAS DE RAM
        jProgressBar1.setMaximum(2048); 
        jProgressBar1.setStringPainted(true);
        jProgressBar1.setString("RAM: 0 / 2048");

        

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
            if (cluster == null || cluster.getCantidad() == 0) return;

            // Obtenemos el único núcleo del sistema
            sistemaOperativo.Nucleo nucleo = cluster.getComputador(0).getNucleo();

            jLabel1.setText("Ciclo Global: " + cluster.getReloj().getCiclo());

            // --- CPU ---
            sistemaOperativo.PCB enCpu = nucleo.getCpu().getProcesoActual();
            int ramUsada = nucleo.getRam().getUsada();
            
            if (enCpu != null) {
                jLabel3.setText("CPU: " + enCpu.getNombre()); 
                jLabel4.setText("PC: " + enCpu.getPc() + " / Deadline: " + enCpu.getDeadline() + " / Prio: " + enCpu.getPrioridad());
                String modoActual = nucleo.getCpu().getModo().toString();
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
           
            // --- RAM ---
            jProgressBar1.setValue(ramUsada);
            jProgressBar1.setString("RAM: " + ramUsada + " / 2048");

            // --- LAS COLAS DEL COMPUTADOR ---
            // Nota: Si renombraste jTextArea1 a txtColaNuevos, usa ese nombre aquí.
            txtColaNuevos.setText("Nuevos: " + nucleo.getColaNuevos().getNombresProcesos() +
                               "\nListos: " + nucleo.getColaListos().getNombresProcesos() +
                               "\nBloqueados: " + nucleo.getColaBloqueados().getNombresProcesos() +
                               "\nTerminados: " + nucleo.getTerminados().getNombresProcesos());
            
            // NUEVAS COLAS DEL PROYECTO 2 (Suspensión)
            txtColaListosSusp.setText("Listos/Susp: " + nucleo.getColaListosSusp().getNombresProcesos());
            txtColaBloqSusp.setText("Bloq/Susp: " + nucleo.getColaBloqueadosSusp().getNombresProcesos());

            // --- LOG DE EVENTOS ---
            int versionActual = cluster.getVersionLog();
            if (versionActual != ultimaVersionLog) {
                txtLog.setText(cluster.getLogEventos());
                if (cluster.getReloj().isCorriendo()) {
                    txtLog.setCaretPosition(txtLog.getDocument().getLength());
                }
                ultimaVersionLog = versionActual;
            }
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
        tabbedPanePrincipal = new javax.swing.JTabbedPane();
        jPanel2 = new javax.swing.JPanel();
        Computador = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jProgressBar1 = new javax.swing.JProgressBar();
        jComboBox1 = new javax.swing.JComboBox<>();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtColaNuevos = new javax.swing.JTextArea();
        jScrollPane3 = new javax.swing.JScrollPane();
        txtColaListosSusp = new javax.swing.JTextArea();
        jScrollPane5 = new javax.swing.JScrollPane();
        txtColaBloqSusp = new javax.swing.JTextArea();
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

        Computador.setBorder(javax.swing.BorderFactory.createTitledBorder("Computador 1"));
        Computador.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel3.setText("CPU: Libre");
        Computador.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 60, -1, -1));

        jLabel4.setText("PC: 0 / Deadline: 0 ");
        Computador.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 110, 130, 20));

        jLabel5.setText("Modo: Usuario");
        Computador.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 200, -1, -1));

        jProgressBar1.setString("RAM: 0 /KB: 2048");
        jProgressBar1.setStringPainted(true);
        Computador.add(jProgressBar1, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 50, 130, 30));

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "FCFS", "Round Robin", "EDF", "Prioridades" }));
        jComboBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox1ActionPerformed(evt);
            }
        });
        Computador.add(jComboBox1, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 150, 80, -1));

        txtColaNuevos.setColumns(20);
        txtColaNuevos.setRows(5);
        txtColaNuevos.setText("Nuevos: [ ]\nListos: [ ]\nBloqueados: [ ]\nTerminados: [ ]");
        txtColaNuevos.setEnabled(false);
        jScrollPane2.setViewportView(txtColaNuevos);

        Computador.add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 240, 210, 180));

        txtColaListosSusp.setColumns(20);
        txtColaListosSusp.setRows(5);
        jScrollPane3.setViewportView(txtColaListosSusp);

        Computador.add(jScrollPane3, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 240, 210, 180));

        txtColaBloqSusp.setColumns(20);
        txtColaBloqSusp.setRows(5);
        jScrollPane5.setViewportView(txtColaBloqSusp);

        Computador.add(jScrollPane5, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 80, 190, 150));

        jPanel2.add(Computador);

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
        crearProceso.add(spnInstrucciones, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 300, 100, -1));

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
        crearProceso.add(cbxTipoProceso, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 80, 100, -1));

        OpComputadora.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        OpComputadora.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                OpComputadoraActionPerformed(evt);
            }
        });
        crearProceso.add(OpComputadora, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 340, -1, -1));

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

        tabbedPanePrincipal.addTab("Simulación", jPanel2);

        jPanel1.add(tabbedPanePrincipal, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 110, 1110, 500));

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
int q = (int) spnQuantum.getValue();
cluster.getComputador(0).setPolitica(FabricaPoliticas.crear(seleccion, q));
        
       
    }//GEN-LAST:event_jComboBox1ActionPerformed
   
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

            // 3. Validaciones
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
            }

            // 5. Determinamos la Prioridad
            int prioridad = 2; // Media por defecto
            String prioStr = cbxPrioridad.getSelectedItem().toString();
            if (prioStr.equals("ALTA")) {
                prioridad = sistemaOperativo.PCB.PRIORIDAD_ALTA; 
            } else if (prioStr.equals("BAJA")) {
                prioridad = sistemaOperativo.PCB.PRIORIDAD_BAJA; 
            }

            // 6. Validación de RAM
            int ramTotalComputador = cluster.getComputador(0).getRam().getTotal();
            if (memoria > ramTotalComputador) {
                javax.swing.JOptionPane.showMessageDialog(this, 
                    "Error: El proceso tiene " + memoria + " KB de memoria, pero el Computador " 
                    + " solo tiene " + ramTotalComputador + " KB en total.",
                    "Memoria Insuficiente", 
                    javax.swing.JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 7. Creamos el proceso (Solo con los parámetros del P2)
            sistemaOperativo.PCB p = new sistemaOperativo.PCB(nombre, tipo, prioridad, memoria, deadline, instrucciones, 1);
            
            // NOTA: Aquí irá la lógica de asignar "operacion" y "rutaArchivo" cuando Darío termine su parte.
            
            // 8. Lo mandamos al Núcleo
            cluster.getComputador(0).getNucleo().admitir(p);
            
            // 9. Mensaje de éxito
            javax.swing.JOptionPane.showMessageDialog(this, "¡Proceso " + nombre + " creado!");
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
         // TODO add your handling code here:
    }//GEN-LAST:event_spnQuantumStateChanged

   

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel Computador;
    private javax.swing.JComboBox<String> OpComputadora;
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
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JProgressBar jProgressBar1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JSpinner jSpinner1;
    private javax.swing.JSpinner spnDeadline;
    private javax.swing.JSpinner spnInstrucciones;
    private javax.swing.JSpinner spnMemoria;
    private javax.swing.JSpinner spnQuantum;
    private javax.swing.JTabbedPane tabbedPanePrincipal;
    private javax.swing.JTextArea txtColaBloqSusp;
    private javax.swing.JTextArea txtColaListosSusp;
    private javax.swing.JTextArea txtColaNuevos;
    private javax.swing.JTextArea txtLog;
    private javax.swing.JTextField txtNombre;
    // End of variables declaration//GEN-END:variables



}
