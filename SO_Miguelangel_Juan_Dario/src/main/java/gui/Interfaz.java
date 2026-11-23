package gui;

/**
 *
 * @author jmmor
 */
import sistemaOperativo.ProcesoESFactory ;
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




public class Interfaz extends javax.swing.JFrame {
   private int ultimaVersionLog = -1;
    public sistemaOperativo.Cluster cluster;
    private gui.PanelArbolFS panelArbolFS;
    private javax.swing.JLabel[] bloquesVisuales;
    private boolean fsCargado = false;
    private boolean procesosCargados = false; //crea la lógica para que se habilite solo cuando el usuario cargue los dos JSON.

    public Interfaz() {
        initComponents();
         
        // 1. CONFIGURACIÓN INICIAL Y PANELES
        panelArbolFS = new gui.PanelArbolFS(); 
        Tab.addTab("Sistema de Archivos", panelArbolFS); 
        
        spnMemoria.setModel(new javax.swing.SpinnerNumberModel(1, 1, 4096, 1));
        spnDeadline.setModel(new javax.swing.SpinnerNumberModel(1, 1, 9999, 1));
        spnInstrucciones.setModel(new javax.swing.SpinnerNumberModel(1, 1, 9999, 1));
        spnQuantum.setModel(new javax.swing.SpinnerNumberModel(1, 1, 1000, 1));
        
        jButton2.setEnabled(false); // PAUSAR deshabilitado
        jButton1.setEnabled(false); // INICIAR deshabilitado hasta cargar JSONs

        // 2. CREAR EL CLUSTER
        cluster = new sistemaOperativo.Cluster(1, 8192, 500);
             
        // 3. CONFIGURAR BARRAS Y SPINNERS
        jProgressBar1.setMaximum(8192); 
        jProgressBar1.setStringPainted(true);
        jProgressBar1.setString("RAM: 0 / 8192");

        jSpinner1.setModel(new javax.swing.SpinnerNumberModel(500, 50, 5000, 50));
        jSpinner1.addChangeListener((e) -> {
            if (cluster != null) {
                int ms = (int) jSpinner1.getValue();
                cluster.getReloj().setDuracionCicloMs(ms);
                jLabel2.setText("Velocidad (ms): " + ms);
            }
        });

        // 4. CREAR LOS 200 BLOQUES DEL DISCO
        jPanel3.setLayout(new java.awt.GridLayout(10, 20, 1, 1)); 
        for (int i = 0; i < 200; i++) {
            javax.swing.JLabel bloque = new javax.swing.JLabel(String.valueOf(i));
            bloque.setOpaque(true);
            bloque.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            bloque.setBackground(java.awt.Color.WHITE);
            bloque.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.GRAY));
            bloque.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 10));
            
           
            jPanel3.add(bloque);
        }
        
        
        
        // 5. EL TIMER (Actualiza la interfaz cada 100ms)
        javax.swing.Timer timer = new javax.swing.Timer(100, (e) -> {
            if (cluster == null || cluster.getCantidad() == 0) return;

            sistemaOperativo.Nucleo nucleo = cluster.getComputador(0).getNucleo();

            // --- A. ACTUALIZAR CICLO Y CPU ---
            jLabel1.setText("Ciclo Global: " + cluster.getReloj().getCiclo());
            sistemaOperativo.PCB enCpu = nucleo.getCpu().getProcesoActual();
            int ramUsada = nucleo.getRam().getUsada();
            
            if (enCpu != null) {
                jLabel3.setText("CPU: " + enCpu.getNombre()); 
                jLabel4.setText("PC: " + enCpu.getPc() + " / Deadline: " + enCpu.getDeadline() + " / Prio: " + enCpu.getPrioridad());
                String modoActual = nucleo.getCpu().getModo().toString();
                jLabel5.setText("Modo: " + modoActual);
                jLabel5.setForeground(modoActual.equals("SISTEMA") ? java.awt.Color.RED : java.awt.Color.BLACK);
            } else {
                jLabel3.setText("CPU: Libre");
                jLabel4.setText("PC: 0 / Deadline: 0");
                jLabel5.setText("Modo: Usuario");
                jLabel5.setForeground(java.awt.Color.BLACK); 
            }
           
            // --- B. ACTUALIZAR RAM ---
            jProgressBar1.setValue(ramUsada);
            jProgressBar1.setString("RAM: " + ramUsada + " / 8192");

            // --- C. ACTUALIZAR COLAS ---
            txtColaNuevos.setText("Nuevos: " + nucleo.getColaNuevos().getNombresProcesos() +
                   "\nListos: " + nucleo.getColaListos().getNombresProcesos() +
                   "\nBloqueados: " + nucleo.getColaBloqueados().getNombresProcesos() +
                   "\nTerminados: " + nucleo.getTerminados().getNombresProcesos());
            
            txtColaListosSusp.setText("Listos/Susp: " + nucleo.getColaListosSusp().getNombresProcesos());
            txtColaBloqSusp.setText("Bloq/Susp: " + nucleo.getColaBloqueadosSusp().getNombresProcesos());

            // --- D. ACTUALIZAR LOG ---
            int versionActual = cluster.getVersionLog();
            if (versionActual != ultimaVersionLog) {
                txtLog.setText(cluster.getLogEventos());
                if (cluster.getReloj().isCorriendo()) {
                    txtLog.setCaretPosition(txtLog.getDocument().getLength());
                }
                ultimaVersionLog = versionActual;
            }

            
            
            // --- E. ACTUALIZAR ÁRBOL DE ARCHIVOS ---
           if (cluster.getSistemaArchivos() != null) {
                panelArbolFS.actualizarArbol(cluster.getSistemaArchivos().getSnapshotArbol());
            }
             
           
                    // --- F. PINTAR EL DISCO (CABEZAL) ---
            if (cluster.getDisco() != null && bloquesVisuales != null) {
                int cabezalActual = cluster.getDisco().getCabezal();
                
                // Recorremos el arreglo dinámico de bloques
                for (int i = 0; i < bloquesVisuales.length; i++) {
                    if (i == cabezalActual) {
                        // Pintamos el bloque del cabezal de ROJO
                        bloquesVisuales[i].setBackground(java.awt.Color.RED);
                        bloquesVisuales[i].setForeground(java.awt.Color.WHITE);
                    } else {
                        // Los demás bloques vuelven a estar BLANCOS
                        bloquesVisuales[i].setBackground(java.awt.Color.WHITE);
                        bloquesVisuales[i].setForeground(java.awt.Color.BLACK);
                    }
                }
            }    
        }); 
        
        timer.start();
    }
    
  private void verificarInicio(){
      if(fsCargado && procesosCargados){
        jButton1.setEnabled(true);
        javax.swing.JOptionPane.showMessageDialog(this, "Archivos Cargados!!!, ya puedes iniciar la simulación");
      
      }
      
      
  }
      // Este método dibuja los bloques dependiendo del tamaño del disco
    public void generarMapaBloques(int totalBloques) {
        jPanel3.removeAll(); // Limpia si ya había bloques (por si cargan otro JSON)
        
        int columnas = 20; // Fijamos 20 columnas
        int filas = (int) Math.ceil((double) totalBloques / columnas); // Calcula las filas necesarias
        
        jPanel3.setLayout(new java.awt.GridLayout(filas, columnas, 1, 1));
        
        bloquesVisuales = new javax.swing.JLabel[totalBloques]; // Creamos el arreglo del tamaño correcto
        
        for (int i = 0; i < totalBloques; i++) {
            javax.swing.JLabel bloque = new javax.swing.JLabel(String.valueOf(i));
            bloque.setOpaque(true);
            bloque.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            bloque.setBackground(java.awt.Color.WHITE);
            bloque.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.GRAY));
            bloque.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 10));
            
            bloquesVisuales[i] = bloque;
            jPanel3.add(bloque);
        }
        
        jPanel3.revalidate(); // Refresca el panel
        jPanel3.repaint();
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
        Tab = new javax.swing.JTabbedPane();
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
        txtNombre = new javax.swing.JTextField();
        spnMemoria = new javax.swing.JSpinner();
        spnDeadline = new javax.swing.JSpinner();
        jLabel12 = new javax.swing.JLabel();
        spnInstrucciones = new javax.swing.JSpinner();
        jButton3 = new javax.swing.JButton();
        jLabel13 = new javax.swing.JLabel();
        cbxTipoProceso = new javax.swing.JComboBox<>();
        jLabel14 = new javax.swing.JLabel();
        cbxPrioridad = new javax.swing.JComboBox<>();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        spnQuantum = new javax.swing.JSpinner();
        jPanel3 = new javax.swing.JPanel();
        btnGrafico = new javax.swing.JButton();
        guardarJson = new javax.swing.JButton();
        cargarJson = new javax.swing.JButton();
        jScrollPane4 = new javax.swing.JScrollPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtLog = new javax.swing.JTextArea();
        jPanel4 = new javax.swing.JPanel();
        cbxPoliticaDisco = new javax.swing.JComboBox<>();
        comboUserAdmin = new javax.swing.JComboBox<>();

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
        Computador.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 30, -1, -1));

        jLabel4.setText("PC: 0 / Deadline: 0 ");
        Computador.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 90, 190, 20));

        jLabel5.setText("Modo: Usuario");
        Computador.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 200, -1, -1));

        jProgressBar1.setString("RAM: 0 /KB: 2048");
        jProgressBar1.setStringPainted(true);
        Computador.add(jProgressBar1, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 30, 130, 30));

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

        Tab.addTab("Simulación", jPanel2);

        jPanel3.setLayout(new java.awt.GridLayout(1, 0));
        Tab.addTab("Disco", jPanel3);

        jPanel1.add(Tab, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 110, 1110, 500));

        btnGrafico.setText("GRAFICOS");
        btnGrafico.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGraficoActionPerformed(evt);
            }
        });
        jPanel1.add(btnGrafico, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 80, -1, -1));

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

        jPanel4.setLayout(new java.awt.GridLayout(1, 0));
        jPanel1.add(jPanel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));

        cbxPoliticaDisco.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "FIFO", "SSTF", "SCAN", "CSCAN" }));
        cbxPoliticaDisco.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbxPoliticaDiscoActionPerformed(evt);
            }
        });
        jPanel1.add(cbxPoliticaDisco, new org.netbeans.lib.awtextra.AbsoluteConstraints(540, 30, -1, -1));

        comboUserAdmin.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Admin", "User1" }));
        comboUserAdmin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboUserAdminActionPerformed(evt);
            }
        });
        jPanel1.add(comboUserAdmin, new org.netbeans.lib.awtextra.AbsoluteConstraints(640, 30, -1, -1));

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
           jButton1.setEnabled(true);
           jButton2.setEnabled(true);
       }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        if (cluster != null) {
            cluster.detener();
            jButton1.setEnabled(true);
            jButton2.setEnabled(true);
        }
    }//GEN-LAST:event_jButton2ActionPerformed
   
    
    private void btnGraficoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGraficoActionPerformed

        VentanaGrafico vg = new VentanaGrafico();
        vg.cluster = this.cluster;        
        vg.setVisible(true);   // TODO add your handling code here:
    }//GEN-LAST:event_btnGraficoActionPerformed

    private void guardarJsonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_guardarJsonActionPerformed
           try {
            // 1. Creamos el objeto de configuración global
            sistemaOperativo.configuracion.GlobalConfig config = new sistemaOperativo.configuracion.GlobalConfig();
            
            // 2. Llenamos los datos globales
            config.cycleDurationMs = (int) jSpinner1.getValue();
            config.swapSizeBlocks = 100; // Valor por defecto para el Proyecto 2
            config.initialDiskPolicy = "FIFO"; // Valor por defecto para el Proyecto 2

            // 3. Llenamos la configuración de la computadora
            config.computer = new sistemaOperativo.configuracion.ComputerConfig();
            config.computer.ramCapacity = 8192; // RAM por defecto de la GUI
            config.computer.policy = "FCFS"; // O podrías leerlo del combo jComboBox1
            config.computer.quantum = (int) spnQuantum.getValue();

            // 4. Creamos el proceso que está en pantalla para guardarlo
            sistemaOperativo.configuracion.ProcessDTO pDto = new sistemaOperativo.configuracion.ProcessDTO();
            pDto.name = txtNombre.getText().isEmpty() ? "Proceso_Ejemplo" : txtNombre.getText();
            pDto.memory = (int) spnMemoria.getValue();
            pDto.deadline = (int) spnDeadline.getValue();
            pDto.instructions = (int) spnInstrucciones.getValue();
            
            // El Proyecto 2 pide targetPath y operation, le ponemos valores por defecto
            pDto.targetPath = "/sistema/archivo.txt"; 
            pDto.operation = cbxTipoProceso.getSelectedItem().toString(); // Usamos el combo como operación
            pDto.fileSizeBlocks = 0;

            // Lo metemos en el arreglo de procesos (de tamaño 1)
            config.processes = new sistemaOperativo.configuracion.ProcessDTO[1];
            config.processes[0] = pDto;

            // 5. Ventana para elegir el archivo JSON
            javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser();
            fileChooser.setDialogTitle("Guardar Procesos (Proyecto 2)");
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Archivos JSON (*.json)", "json"));
            
            if (fileChooser.showSaveDialog(this) == javax.swing.JFileChooser.APPROVE_OPTION) {
                java.io.File archivo = fileChooser.getSelectedFile();
                
                if (!archivo.getName().toLowerCase().endsWith(".json")) {
                    archivo = new java.io.File(archivo.getParentFile(), archivo.getName() + ".json");
                }

                // 6. Guardar con Gson
                com.google.gson.Gson gson = new com.google.gson.GsonBuilder().setPrettyPrinting().create();
                java.io.FileWriter writer = new java.io.FileWriter(archivo);
                gson.toJson(config, writer);
                writer.close();

                javax.swing.JOptionPane.showMessageDialog(this, "¡Procesos guardados exitosamente en:\n" + archivo.getAbsolutePath());
            }
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this, "Error al guardar: " + e.getMessage());
        }  // TODO add your handling code here:
    }//GEN-LAST:event_guardarJsonActionPerformed

    private void cargarJsonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cargarJsonActionPerformed
         try {
            // 1. PRIMERO: Cargar el JSON del Sistema de Archivos
            javax.swing.JFileChooser chooserFS = new javax.swing.JFileChooser();
            chooserFS.setDialogTitle("1. Selecciona el JSON del Sistema de Archivos");
            if (chooserFS.showOpenDialog(this) != javax.swing.JFileChooser.APPROVE_OPTION) return;
            
            java.io.File archivoFS = chooserFS.getSelectedFile();
            com.google.gson.Gson gson = new com.google.gson.Gson();
            java.io.FileReader readerFS = new java.io.FileReader(archivoFS);
            
               sistemaArchivo.FileSystemConfig configFS = gson.fromJson(readerFS, sistemaArchivo.FileSystemConfig.class);
            readerFS.close();

            // CAMBIA "nodos" POR "fileSystem"
            if (configFS != null && configFS.fileSystem != null) {
                cluster.getSistemaArchivos().inicializarDesdeJSON(configFS);
                
                int totalBloques = 200;
                int cabezalInicial = 50;
                
                if (configFS.diskConfig != null) {
                    totalBloques = configFS.diskConfig.totalBlocks;
                    cabezalInicial = configFS.diskConfig.initialHead;
                }
                
                // 3. Le dices al Disco de Juan cuál es su nuevo tamaño (debes crear este setter en Disco.java)
                cluster.getDisco().setTotalBloques(totalBloques);
                cluster.getDisco().setCabezal(cabezalInicial);
                
                // 4. Generas el mapa visual en la interfaz
                generarMapaBloques(totalBloques);
                
                fsCargado = true;
                verificarInicio();
            }

            // 2. SEGUNDO: Cargar el JSON de los Procesos
            javax.swing.JFileChooser chooserProc = new javax.swing.JFileChooser();
            chooserProc.setDialogTitle("2. Selecciona el JSON de Procesos");
            if (chooserProc.showOpenDialog(this) != javax.swing.JFileChooser.APPROVE_OPTION) return;
            
            java.io.File archivoProc = chooserProc.getSelectedFile();
            java.io.FileReader readerProc = new java.io.FileReader(archivoProc);
            
            sistemaOperativo.configuracion.GlobalConfig config = gson.fromJson(readerProc, sistemaOperativo.configuracion.GlobalConfig.class);
            readerProc.close();

            if (config == null || config.processes == null) return;

            if (config.cycleDurationMs > 0) jSpinner1.setValue(config.cycleDurationMs);
            if (config.computer != null && config.computer.policy != null) {
                 cluster.getComputador(0).setPolitica(planificacionSO.FabricaPoliticas.crear(config.computer.policy, config.computer.quantum));
            }

              for (sistemaOperativo.configuracion.ProcessDTO pDto : config.processes) {
                // 1. TIPO DE PROCESO
                losEnums.TipoProceso tipoProc = losEnums.TipoProceso.CPU_BOUND;
                if (pDto.targetPath != null && !pDto.targetPath.trim().isEmpty()) {
                    tipoProc = losEnums.TipoProceso.IO_BOUND;
                }

                // 2. LEEMOS LA OPERACIÓN (En inglés, porque así viene en el JSON)
                losEnums.TipoOperacionES op = losEnums.TipoOperacionES.LEER;
                if (pDto.operation != null) {
                    String opJson = pDto.operation.toUpperCase();
                    if (opJson.equals("READ")) op = losEnums.TipoOperacionES.LEER;
                    else if (opJson.equals("UPDATE")) op = losEnums.TipoOperacionES.ACTUALIZAR;
                    else if (opJson.equals("DELETE")) op = losEnums.TipoOperacionES.ELIMINAR;
                    else if (opJson.equals("CREATE")) op = losEnums.TipoOperacionES.CREAR;
                }

                // 3. CREAMOS EL PROCESO
                sistemaOperativo.PCB p = new sistemaOperativo.PCB(
                        pDto.name, 
                        tipoProc, 
                        sistemaOperativo.PCB.PRIORIDAD_MEDIA, 
                        pDto.memory, 
                        pDto.deadline, 
                        pDto.instructions, 
                        1
                );
                
                p.setOperacion(op);
                // Usamos targetPath
                p.setRutaArchivo(pDto.targetPath != null ? pDto.targetPath : "/sistema/archivo.txt");
                p.setBloques(pDto.fileSizeBlocks);
                p.setDueno("admin");
                
                cluster.getComputador(0).getNucleo().admitir(p);
            }
            
            procesosCargados = true;
            verificarInicio();

        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this, "Error al cargar: " + e.getMessage());
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

    private void cbxPrioridadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxPrioridadActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cbxPrioridadActionPerformed

    private void cbxTipoProcesoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxTipoProcesoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cbxTipoProcesoActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        try {
            // Leemos el combo que agregaste
            String usuarioSeleccionado = comboUserAdmin.getSelectedItem().toString();
            String dueno = usuarioSeleccionado.equals("Admin") ? "admin" : "user1";
            
            sistemaOperativo.Resultado r = sistemaOperativo.ProcesoESFactory.crear(
                    cluster,
                    txtNombre.getText(),
                    cbxTipoProceso.getSelectedItem().toString(),
                    cbxPrioridad.getSelectedItem().toString(),
                    (int) spnMemoria.getValue(),
                    (int) spnDeadline.getValue(),
                    (int) spnInstrucciones.getValue(),
                    "LEER",                       
                    "/sistema/archivo.txt",
                    dueno); // <-- AGREGADO AQUÍ

            if (!r.isOk()) {
                javax.swing.JOptionPane.showMessageDialog(this, "Rechazado: " + r.getMotivo());
                return;
            }
            javax.swing.JOptionPane.showMessageDialog(this, "¡Proceso creado!");
            txtNombre.setText("");
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this, "Error inesperado: " + e.getMessage());
        }

    }//GEN-LAST:event_jButton3ActionPerformed

    private void txtNombreActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtNombreActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtNombreActionPerformed

    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
        if (cluster == null) return;

        String seleccion = (String) jComboBox1.getSelectedItem();
        int q = (int) spnQuantum.getValue();
        cluster.getComputador(0).setPolitica(FabricaPoliticas.crear(seleccion, q));

    }//GEN-LAST:event_jComboBox1ActionPerformed

    private void cbxPoliticaDiscoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxPoliticaDiscoActionPerformed
       if (cluster == null || cluster.getDisco() == null) return;
        
        String seleccion = cbxPoliticaDisco.getSelectedItem().toString();
        
        switch (seleccion) {
            case "FIFO": cluster.getDisco().setPolitica(new planificacionSO.PoliticaFIFO()); break;
            case "SSTF": cluster.getDisco().setPolitica(new planificacionSO.PoliticaSSTF()); break;
            case "SCAN": cluster.getDisco().setPolitica(new planificacionSO.PoliticaSCAN()); break;
            case "CSCAN": cluster.getDisco().setPolitica(new planificacionSO.PoliticaCSCAN()); break;
        }
        
        cluster.registrarEvento("Política de disco cambiada a: " + seleccion); // TODO add your handling code here:
    }//GEN-LAST:event_cbxPoliticaDiscoActionPerformed

    private void comboUserAdminActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboUserAdminActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_comboUserAdminActionPerformed

   

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel Computador;
    private javax.swing.JTabbedPane Tab;
    private javax.swing.JButton btnGrafico;
    private javax.swing.JButton cargarJson;
    private javax.swing.JComboBox<String> cbxPoliticaDisco;
    private javax.swing.JComboBox<String> cbxPrioridad;
    private javax.swing.JComboBox<String> cbxTipoProceso;
    private javax.swing.JComboBox<String> comboUserAdmin;
    private javax.swing.JPanel crearProceso;
    private javax.swing.JButton guardarJson;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
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
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
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
    private javax.swing.JTextArea txtColaBloqSusp;
    private javax.swing.JTextArea txtColaListosSusp;
    private javax.swing.JTextArea txtColaNuevos;
    private javax.swing.JTextArea txtLog;
    private javax.swing.JTextField txtNombre;
    // End of variables declaration//GEN-END:variables



}
