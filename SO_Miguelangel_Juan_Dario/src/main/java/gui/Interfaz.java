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




public class Interfaz extends javax.swing.JFrame {
   private int ultimaVersionLog = -1;
   private javax.swing.table.DefaultTableModel modeloTabla;
    public sistemaOperativo.Cluster cluster;
    private gui.PanelArbolFS panelArbolFS;
    private javax.swing.JLabel[] bloquesVisuales;
    private boolean fsCargado = false;
    private boolean procesosCargados = false; //crea la lógica para que se habilite solo cuando el usuario cargue los dos JSON.

    // ====================================================================================
    // [AJUSTE 25-nov] Componentes que se agregan por código, fuera de initComponents, para
    // no romper el .form de NetBeans. La interfaz solo OBSERVA al sistema: lee el estado en
    // el Timer y, cuando el usuario hace algo, llama a CargadorJSON o a ProcesoESFactory.
    // ====================================================================================
    private javax.swing.JSpinner spnBloques;
    private javax.swing.JCheckBox chkDirectorio;
    private javax.swing.JCheckBox chkPublico;
    private javax.swing.JTextField txtNuevoNombre;
    private javax.swing.JLabel lblModoUsuario;
    private javax.swing.JLabel lblInfoDisco;
    private javax.swing.JTextArea txtColaDisco;
    private javax.swing.JSpinner spnPosPorCiclo;
    private javax.swing.JSpinner spnArranque;
    private int versionFS = -1;
    private boolean sincronizando = false; // evita que los combos reaccionen cuando el JSON los cambia
    /** Para las pruebas automáticas: no abre cuadros de diálogo. */
    public boolean silencioso = false;

    private static final java.awt.Color COLOR_SWAP_LIBRE = new java.awt.Color(226, 216, 246);
    private static final java.awt.Color COLOR_SWAP_OCUPADO = new java.awt.Color(255, 160, 60);
    private static final javax.swing.border.Border BORDE_NORMAL = javax.swing.BorderFactory.createLineBorder(java.awt.Color.GRAY);
    private static final javax.swing.border.Border BORDE_OBJETIVO = javax.swing.BorderFactory.createLineBorder(new java.awt.Color(230, 180, 0), 3);

    public Interfaz() {
        initComponents();
        setTitle("ÁvilaOS — Proyecto 2: sistema de archivos, disco y suspensión");

        // 1. TABLA DE ASIGNACIÓN DE ARCHIVOS (JTable)
        String[] columnas = {"Color", "Nombre", "Ruta", "Bloques", "Primer bloque", "Dueño", "Permiso", "Creado por", "Cadena de bloques"};
        modeloTabla = new javax.swing.table.DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) { return false; }
        };
        tablaDatos.setModel(modeloTabla);
        tablaDatos.getColumnModel().getColumn(0).setMaxWidth(50);
        tablaDatos.getColumnModel().getColumn(8).setPreferredWidth(260);
        tablaDatos.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(javax.swing.JTable t, Object valor,
                    boolean sel, boolean foco, int fila, int col) {
                super.getTableCellRendererComponent(t, col == 0 ? "" : valor, sel, foco, fila, col);
                if (col == 0) setBackground(colorPara((String) t.getModel().getValueAt(fila, 2)));
                else if (!sel) setBackground(java.awt.Color.WHITE);
                return this;
            }
        });

        // 2. JTREE DEL SISTEMA DE ARCHIVOS
        panelArbolFS = new gui.PanelArbolFS();
        tablaArchivos.addTab("Sistema de Archivos", panelArbolFS);

        spnMemoria.setModel(new javax.swing.SpinnerNumberModel(16, 1, 4096, 1));
        spnDeadline.setModel(new javax.swing.SpinnerNumberModel(300, 1, 9999, 1));
        spnInstrucciones.setModel(new javax.swing.SpinnerNumberModel(2, 1, 9999, 1));
        spnQuantum.setModel(new javax.swing.SpinnerNumberModel(3, 1, 1000, 1));

        // Los buffers productor-consumidor ya no se usan en el Proyecto 2 (lo dice el enunciado)
        cbxTipoProceso.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "IO_BOUND", "CPU_BOUND" }));
        comboUserAdmin.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Admin", "User1", "User2" }));
        String[] politicasDisco = new String[losEnums.TipoPoliticaDisco.values().length];
        for (int i = 0; i < politicasDisco.length; i++) politicasDisco[i] = losEnums.TipoPoliticaDisco.values()[i].getNombre();
        cbxPoliticaDisco.setModel(new javax.swing.DefaultComboBoxModel<>(politicasDisco));

        jButton2.setEnabled(false); // PAUSAR deshabilitado
        jButton1.setEnabled(false); // INICIAR deshabilitado hasta cargar JSONs

        // 3. CREAR EL CLUSTER (una sola instancia del computador; el JSON trae la RAM real)
        cluster = new sistemaOperativo.Cluster(1, 1024, 500);

        jProgressBar1.setStringPainted(true);
        jSpinner1.setModel(new javax.swing.SpinnerNumberModel(500, 50, 5000, 50));
        jSpinner1.addChangeListener((e) -> {
            if (cluster != null) {
                int ms = (int) jSpinner1.getValue();
                cluster.getReloj().setDuracionCicloMs(ms);
                jLabel2.setText("Velocidad (ms): " + ms);
            }
        });

        // 4. ETIQUETAS QUE FALTABAN ARRIBA, FORMULARIO DE ARCHIVOS Y PESTAÑA DEL DISCO
        javax.swing.JLabel lblPol = new javax.swing.JLabel("Política de disco");
        lblPol.setFont(lblPol.getFont().deriveFont(11f));
        jPanel1.add(lblPol, new org.netbeans.lib.awtextra.AbsoluteConstraints(548, 8, -1, -1));
        javax.swing.JLabel lblMod = new javax.swing.JLabel("Modo");
        lblMod.setFont(lblMod.getFont().deriveFont(11f));
        jPanel1.add(lblMod, new org.netbeans.lib.awtextra.AbsoluteConstraints(650, 8, -1, -1));
        for (javax.swing.JTextArea t : new javax.swing.JTextArea[]{txtColaNuevos, txtColaListosSusp, txtColaBloqSusp, txtLog}) {
            t.setLineWrap(true);
            t.setWrapStyleWord(true);
        }
        armarFormularioArchivos();
        armarPestanaDisco();
        generarMapaBloques(cluster.getDisco().getTotalBloques());
        actualizarModo();

        // 5. EL TIMER: la interfaz observa al sistema cada 150 ms
        javax.swing.Timer timer = new javax.swing.Timer(150, (e) -> refrescar());
        timer.start();
    }

    /** Bloque de controles para las operaciones de archivo (columna derecha de "Crear Proceso"). */
    private void armarFormularioArchivos() {
        crearProceso.remove(cbxOperacion);
        crearProceso.remove(txtRuta);
        javax.swing.JLabel titulo = new javax.swing.JLabel("<html><b>Operación de E/S (procesos IO_BOUND)</b></html>");
        crearProceso.add(titulo, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 22, -1, -1));
        crearProceso.add(new javax.swing.JLabel("Operación:"), new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 58, -1, -1));
        crearProceso.add(cbxOperacion, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 52, 150, -1));
        crearProceso.add(new javax.swing.JLabel("Ruta:"), new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 96, -1, -1));
        crearProceso.add(txtRuta, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 92, 180, -1));
        txtRuta.setText("/usuarios/nuevo.txt");
        crearProceso.add(new javax.swing.JLabel("Bloques (crear):"), new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 134, -1, -1));
        spnBloques = new javax.swing.JSpinner(new javax.swing.SpinnerNumberModel(1, 1, 500, 1));
        crearProceso.add(spnBloques, new org.netbeans.lib.awtextra.AbsoluteConstraints(370, 130, 70, -1));
        chkDirectorio = new javax.swing.JCheckBox("Crear un directorio (no un archivo)");
        crearProceso.add(chkDirectorio, new org.netbeans.lib.awtextra.AbsoluteConstraints(246, 166, -1, -1));
        chkPublico = new javax.swing.JCheckBox("Archivo público (si no, privado)", true);
        crearProceso.add(chkPublico, new org.netbeans.lib.awtextra.AbsoluteConstraints(246, 194, -1, -1));
        crearProceso.add(new javax.swing.JLabel("Nombre nuevo (renombrar):"), new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 230, -1, -1));
        txtNuevoNombre = new javax.swing.JTextField();
        crearProceso.add(txtNuevoNombre, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 252, 200, -1));
        lblModoUsuario = new javax.swing.JLabel();
        crearProceso.add(lblModoUsuario, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 290, 280, 90));
        chkDirectorio.addActionListener(e -> spnBloques.setEnabled(!chkDirectorio.isSelected()));
    }

    /** Pestaña "Disco": datos del cabezal arriba, mapa de bloques al centro, cola de E/S abajo. */
    private void armarPestanaDisco() {
        int idx = tablaArchivos.indexOfComponent(jPanel3);
        tablaArchivos.remove(jPanel3);

        javax.swing.JPanel envoltura = new javax.swing.JPanel(new java.awt.BorderLayout(4, 4));
        javax.swing.JPanel arriba = new javax.swing.JPanel(new java.awt.GridLayout(2, 1));
        javax.swing.JPanel fila1 = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 2));
        javax.swing.JPanel fila2 = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 0));
        arriba.add(fila1);
        arriba.add(fila2);
        lblInfoDisco = new javax.swing.JLabel("Cabezal: -");
        fila1.add(lblInfoDisco);
        fila2.add(new javax.swing.JLabel("Posiciones del cabezal por ciclo:"));
        spnPosPorCiclo = new javax.swing.JSpinner(new javax.swing.SpinnerNumberModel(cluster.getDisco().getPosPorCiclo(), 1, 50, 1));
        spnPosPorCiclo.addChangeListener(e -> cluster.getDisco().setPosPorCiclo((int) spnPosPorCiclo.getValue()));
        fila2.add(spnPosPorCiclo);
        fila2.add(new javax.swing.JLabel("   Arranque del motor (ciclos):"));
        spnArranque = new javax.swing.JSpinner(new javax.swing.SpinnerNumberModel(cluster.getDisco().getCiclosArranque(), 0, 100, 1));
        spnArranque.addChangeListener(e -> cluster.getDisco().setCiclosArranque((int) spnArranque.getValue()));
        fila2.add(spnArranque);
        envoltura.add(arriba, java.awt.BorderLayout.NORTH);

        envoltura.add(new javax.swing.JScrollPane(jPanel3), java.awt.BorderLayout.CENTER);

        javax.swing.JPanel abajo = new javax.swing.JPanel(new java.awt.BorderLayout());
        abajo.add(new javax.swing.JLabel("<html>&nbsp;<span style='background:#ff0000;color:white'>&nbsp;N&nbsp;</span> cabezal &nbsp; "
                + "<span style='border:2px solid #e6b400'>&nbsp;N&nbsp;</span> próxima solicitud &nbsp; "
                + "<span style='background:#ffffff'>&nbsp;N&nbsp;</span> libre &nbsp; "
                + "<span style='background:#bfe3ff'>&nbsp;N&nbsp;</span> archivo (un color por archivo, igual que en la tabla) &nbsp; "
                + "<span style='background:#e2d8f6'>&nbsp;N&nbsp;</span> swap libre &nbsp; "
                + "<span style='background:#ffa03c'>&nbsp;N&nbsp;</span> proceso suspendido &nbsp; (pasa el ratón por un bloque para ver quién lo ocupa)</html>"),
                java.awt.BorderLayout.NORTH);
        txtColaDisco = new javax.swing.JTextArea(3, 80);
        txtColaDisco.setEditable(false);
        txtColaDisco.setLineWrap(true);
        txtColaDisco.setWrapStyleWord(true);
        abajo.add(new javax.swing.JScrollPane(txtColaDisco), java.awt.BorderLayout.CENTER);
        envoltura.add(abajo, java.awt.BorderLayout.SOUTH);

        tablaArchivos.insertTab("Disco", null, envoltura, "Mapa de bloques, cabezal y cola de E/S", idx);
    }

  private void verificarInicio(){
      if(fsCargado && procesosCargados){
        jButton1.setEnabled(true);
      }
  }

    /** Dibuja un bloque por cada posición del disco (20 columnas). */
    public void generarMapaBloques(int totalBloques) {
        jPanel3.removeAll(); // Limpia si ya había bloques (por si cargan otro JSON)

        int columnas = 20; // Fijamos 20 columnas
        int filas = (int) Math.ceil((double) totalBloques / columnas); // Calcula las filas necesarias

        jPanel3.setLayout(new java.awt.GridLayout(filas, columnas, 1, 1));
        jPanel3.setPreferredSize(new java.awt.Dimension(1060, Math.max(300, filas * 20)));

        bloquesVisuales = new javax.swing.JLabel[totalBloques]; // Creamos el arreglo del tamaño correcto

        for (int i = 0; i < totalBloques; i++) {
            javax.swing.JLabel bloque = new javax.swing.JLabel(String.valueOf(i));
            bloque.setOpaque(true);
            bloque.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            bloque.setBackground(java.awt.Color.WHITE);
            bloque.setBorder(BORDE_NORMAL);
            bloque.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 10));

            bloquesVisuales[i] = bloque;
            jPanel3.add(bloque);
        }

        jPanel3.revalidate(); // Refresca el panel
        jPanel3.repaint();
    }

    /** Un color fijo por archivo (el mismo en el mapa del disco y en la tabla). */
    static java.awt.Color colorPara(String etiqueta) {
        if (etiqueta == null) return java.awt.Color.WHITE;
        float tono = (etiqueta.hashCode() & 0xffff) / 65535f;
        return java.awt.Color.getHSBColor(tono, 0.30f, 1.0f);
    }

    /** "P3-Proc_95 (E/S)": nombres de una cola, con el motivo del bloqueo si se pide. */
    private static String formatear(estructuraDeDatos.Cola c, boolean conMotivo) {
        Object[] ps = c.aArreglo();
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < ps.length; i++) {
            sistemaOperativo.PCB p = (sistemaOperativo.PCB) ps[i];
            if (i > 0) sb.append(", ");
            sb.append("P").append(p.getId()).append("-").append(p.getNombre());
            if (p.isSuspensionPendiente()) sb.append(" →swap");
            if (p.isReactivacionPendiente()) sb.append(" ←swap");
            if (conMotivo && p.getMotivoBloqueo() != null) {
                sb.append(" (").append(p.getMotivoBloqueo() == losEnums.MotivoBloqueo.E_S ? "E/S" : p.getMotivoBloqueo().toString()).append(")");
            }
        }
        return sb.append("]").toString();
    }

    /** Lo que hace el Timer: leer el estado del sistema y pintarlo. */
    private void refrescar() {
        if (cluster == null || cluster.getCantidad() == 0) return;

        sistemaOperativo.Nucleo nucleo = cluster.getComputador(0).getNucleo();

        // --- A. CICLO Y CPU ---
        jLabel1.setText("Ciclo Global: " + cluster.getReloj().getCiclo());
        sistemaOperativo.PCB enCpu = nucleo.getCpu().getProcesoActual();
        if (enCpu != null) {
            jLabel3.setText("CPU: " + enCpu.getNombre());
            jLabel4.setText("PC: " + enCpu.getPc() + " / Deadline: " + enCpu.getDeadline() + " / Prio: " + enCpu.getPrioridad());
            String modoActual = nucleo.getCpu().getModo().toString();
            jLabel5.setText("Modo CPU: " + modoActual);
            jLabel5.setForeground(modoActual.equals("SISTEMA") ? java.awt.Color.RED : java.awt.Color.BLACK);
        } else {
            jLabel3.setText("CPU: Libre");
            jLabel4.setText("PC: 0 / Deadline: 0");
            jLabel5.setText("Modo CPU: Usuario");
            jLabel5.setForeground(java.awt.Color.BLACK);
        }

        // --- B. RAM ---
        int ramUsada = nucleo.getRam().getUsada();
        int ramTotal = nucleo.getRam().getTotal();
        jProgressBar1.setMaximum(ramTotal);
        jProgressBar1.setValue(ramUsada);
        jProgressBar1.setString("RAM: " + ramUsada + " / " + ramTotal);

        // --- C. COLAS (siete estados) ---
        txtColaNuevos.setText("Nuevos (largo plazo): " + formatear(nucleo.getColaNuevos(), false)
                + "\nListos: " + formatear(nucleo.getColaListos(), false)
                + "\nBloqueados: " + formatear(nucleo.getColaBloqueados(), true)
                + "\nTerminados: " + formatear(nucleo.getTerminados(), false));
        txtColaListosSusp.setText("Listos/Suspendidos:\n" + formatear(nucleo.getColaListosSusp(), false));
        txtColaBloqSusp.setText("Bloqueados/Suspendidos:\n" + formatear(nucleo.getColaBloqueadosSusp(), true));

        // --- D. LOG ---
        int versionActual = cluster.getVersionLog();
        if (versionActual != ultimaVersionLog) {
            txtLog.setText(cluster.getLogEventos());
            if (cluster.getReloj().isCorriendo()) {
                txtLog.setCaretPosition(txtLog.getDocument().getLength());
            }
            ultimaVersionLog = versionActual;
        }

        // --- E. JTREE Y TABLA: solo se rearman cuando el sistema de archivos cambia ---
        sistemaArchivo.SistemaArchivos fs = cluster.getSistemaArchivos();
        if (fs.getVersion() != versionFS) {
            versionFS = fs.getVersion();
            panelArbolFS.actualizarArbol(fs.getSnapshotArbol());
            modeloTabla.setRowCount(0);
            for (Object[] fila : fs.getFilasTabla()) {
                Object[] conColor = new Object[fila.length + 1];
                conColor[0] = "";
                System.arraycopy(fila, 0, conColor, 1, fila.length);
                modeloTabla.addRow(conColor);
            }
        }

        // --- F. DISCO: bloques, cabezal, dirección y cola de E/S ---
        pintarDisco(fs.getMapaBloques());
    }

    private void pintarDisco(sistemaArchivo.MapaBloques mapa) {
        hardware.Disco disco = cluster.getDisco();
        if (bloquesVisuales == null || bloquesVisuales.length != mapa.getTotal()) generarMapaBloques(mapa.getTotal());
        int cabezal = disco.getCabezal();
        sistemaOperativo.SolicitudES objetivo = disco.getObjetivo();
        sistemaOperativo.SolicitudES actual = disco.getSolicitudActual();
        int bloqueObjetivo = objetivo != null ? objetivo.getBloqueDestino() : -1;

        for (int i = 0; i < bloquesVisuales.length; i++) {
            javax.swing.JLabel b = bloquesVisuales[i];
            String dueno = mapa.getDuenoBloque(i);
            java.awt.Color fondo;
            String quien;
            if (mapa.esSwap(i)) {
                fondo = mapa.estaOcupado(i) ? COLOR_SWAP_OCUPADO : COLOR_SWAP_LIBRE;
                quien = mapa.estaOcupado(i) ? dueno : "área de swap (libre)";
            } else if (mapa.estaOcupado(i)) {
                fondo = colorPara(dueno);
                quien = dueno;
            } else {
                fondo = java.awt.Color.WHITE;
                quien = "libre";
            }
            if (i == cabezal) {
                b.setBackground(java.awt.Color.RED);
                b.setForeground(java.awt.Color.WHITE);
            } else {
                b.setBackground(fondo);
                b.setForeground(java.awt.Color.BLACK);
            }
            b.setBorder(i == bloqueObjetivo ? BORDE_OBJETIVO : BORDE_NORMAL);
            int sig = mapa.getSiguiente(i);
            b.setToolTipText("Bloque " + i + ": " + quien + (sig >= 0 ? " · siguiente bloque: " + sig : ""));
        }

        String estado = !disco.isArrancado() && disco.getTamanoCola() > 0 ? " · <b>motor arrancando</b>" : "";
        lblInfoDisco.setText("<html>Cabezal: <b>" + cabezal + "</b> " + (disco.getDireccion() == losEnums.DireccionCabezal.SUBE ? "↑ sube" : "↓ baja")
                + " · Política: <b>" + disco.getPolitica().getNombre() + "</b>"
                + " · Atendiendo: " + (actual != null ? actual : "-")
                + " · Desplazamiento: " + disco.getDesplazamientoTotal()
                + " · Swap libre: " + mapa.libresSwap() + "/" + mapa.totalSwap() + estado + "</html>");
        txtColaDisco.setText("Cola de E/S del disco (" + disco.getTamanoCola() + " solicitudes): " + disco.getResumenCola()
                + (objetivo != null ? "\nPróxima según " + disco.getPolitica().getNombre() + ": " + objetivo : ""));
    }

    /** Dueño de los procesos que se crean según el modo escogido arriba. */
    private String duenoActual() {
        String sel = comboUserAdmin.getSelectedItem().toString();
        return sel.equals("Admin") ? "admin" : sel.toLowerCase();
    }

    private boolean esAdmin() { return "admin".equals(duenoActual()); }

    /** Modo administrador / usuario: qué se puede tocar y qué se explica. */
    private void actualizarModo() {
        boolean admin = esAdmin();
        cbxPoliticaDisco.setEnabled(admin);
        jComboBox1.setEnabled(admin);
        spnPosPorCiclo.setEnabled(admin);
        spnArranque.setEnabled(admin);
        chkDirectorio.setEnabled(admin);
        if (!admin) chkDirectorio.setSelected(false);
        lblModoUsuario.setText(admin
                ? "<html><b>Modo administrador</b>: todas las operaciones.<br>Los procesos que crees serán de <b>admin</b>.</html>"
                : "<html><b>Modo usuario (" + duenoActual() + ")</b>: lee sus archivos o los públicos, crea archivos en sus "
                  + "directorios o en /usuarios y elimina solo los suyos. No renombra ni cambia políticas.</html>");
    }

    private void avisar(String texto) {
        if (silencioso) {
            System.out.println("[aviso] " + texto);
        } else {
            javax.swing.JOptionPane.showMessageDialog(this, texto);
        }
    }

    /** Carga los dos JSON (lo usa el botón CargarJson; queda público para las pruebas). */
    public void cargarDesdeArchivos(java.io.File archivoFS, java.io.File archivoProc) {
        // [AJUSTE 25-nov] Cargar los JSON REINICIA la simulación con un sistema nuevo. Antes se
        // reemplazaba el mapa de bloques con procesos vivos y un proceso podía liberar el swap
        // de otro. El enunciado lo plantea así: "reiniciar la simulación con nuevos procesos
        // preservando el estado del disco" (guardado en el JSON del sistema de archivos).
        cluster.detener();
        cluster = new sistemaOperativo.Cluster(1, 1024, 500);
        cluster.getDisco().setPosPorCiclo((int) spnPosPorCiclo.getValue());
        cluster.getDisco().setCiclosArranque((int) spnArranque.getValue());
        ultimaVersionLog = -1;
        versionFS = -1;
        fsCargado = false;
        procesosCargados = false;
        jButton1.setEnabled(false);
        jButton2.setEnabled(false);

        StringBuilder mensaje = new StringBuilder();
        try (java.io.Reader r = new java.io.InputStreamReader(new java.io.FileInputStream(archivoFS), java.nio.charset.StandardCharsets.UTF_8)) {
            String avisos = sistemaOperativo.CargadorJSON.cargarSistemaArchivos(cluster, r);
            generarMapaBloques(cluster.getDisco().getTotalBloques());
            versionFS = -1;
            fsCargado = true;
            mensaje.append("Sistema de archivos: ").append(cluster.getDisco().getTotalBloques()).append(" bloques, cabezal en ")
                   .append(cluster.getDisco().getCabezal()).append(".\n").append(avisos);
        } catch (Exception e) {
            avisar("No se pudo cargar el JSON del sistema de archivos:\n" + e.getMessage());
            return;
        }
        if (archivoProc != null) {
            try (java.io.Reader r = new java.io.InputStreamReader(new java.io.FileInputStream(archivoProc), java.nio.charset.StandardCharsets.UTF_8)) {
                mensaje.append(sistemaOperativo.CargadorJSON.cargarProcesos(cluster, r));
                procesosCargados = true;
                sincronizarControles();
            } catch (Exception e) {
                avisar("No se pudo cargar el JSON de procesos:\n" + e.getMessage());
                return;
            }
        }
        verificarInicio();
        avisar(mensaje.toString() + (jButton1.isEnabled() ? "\nYa puedes iniciar la simulación." : ""));
    }

    /** Pone los combos y spinners como quedaron después de cargar el JSON. */
    private void sincronizarControles() {
        sincronizando = true;
        try {
            cbxPoliticaDisco.setSelectedItem(cluster.getDisco().getPolitica().getTipo().getNombre());
            planificacionSO.PoliticaPlanificacion pc = cluster.getComputador(0).getNucleo().getPolitica();
            String nombre = pc instanceof planificacionSO.RoundRobin ? "Round Robin"
                    : pc instanceof planificacionSO.EDF ? "EDF"
                    : pc instanceof planificacionSO.PrioridadApropiativa ? "Prioridades" : "FCFS";
            if (pc.getQuantum() > 0) spnQuantum.setValue(pc.getQuantum());
            jComboBox1.setSelectedItem(nombre);
            int ms = cluster.getReloj().getDuracionCicloMs();
            ms = Math.max(50, Math.min(5000, (ms / 50) * 50));
            jSpinner1.setValue(ms);
        } finally {
            sincronizando = false;
        }
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
        tablaArchivos = new javax.swing.JTabbedPane();
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
        cbxOperacion = new javax.swing.JComboBox<>();
        txtRuta = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jScrollPane6 = new javax.swing.JScrollPane();
        tablaDatos = new javax.swing.JTable();
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
        jPanel1.add(jSpinner1, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 10, -1, -1));

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

        cbxOperacion.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "LEER", "CREAR", "ACTUALIZAR", "ELIMINAR" }));
        crearProceso.add(cbxOperacion, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 330, -1, -1));

        txtRuta.setText("/sistema/archivo.txt");
        crearProceso.add(txtRuta, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 360, -1, -1));

        jPanel2.add(crearProceso);

        tablaArchivos.addTab("Simulación", jPanel2);

        jPanel3.setLayout(new java.awt.GridLayout(1, 0));
        tablaArchivos.addTab("Disco", jPanel3);

        tablaDatos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane6.setViewportView(tablaDatos);

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 1110, Short.MAX_VALUE)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 465, Short.MAX_VALUE)
        );

        tablaArchivos.addTab("Tabla Archivos ", jPanel5);

        jPanel1.add(tablaArchivos, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 110, 1110, 500));

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
       if (cluster == null) return;
       if (!fsCargado || !procesosCargados) {
           avisar("Primero carga los dos JSON (sistema de archivos y procesos).");
           return;
       }
       cluster.iniciar();
       jLabel2.setText("Velocidad (ms): " + jSpinner1.getValue());
       jButton1.setEnabled(false);
       jButton2.setEnabled(true);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
         if (cluster != null) {
            cluster.detener();
            jButton1.setEnabled(true);
            jButton2.setEnabled(false);
        }
    }//GEN-LAST:event_jButton2ActionPerformed
   
    
    private void btnGraficoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGraficoActionPerformed

        VentanaGrafico vg = new VentanaGrafico();
        vg.cluster = this.cluster;        
        vg.setVisible(true);
    }//GEN-LAST:event_btnGraficoActionPerformed

    private void guardarJsonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_guardarJsonActionPerformed
           // [AJUSTE 25-nov] Guarda el sistema de archivos (persistencia pedida) y los procesos que no han terminado
        try {
            javax.swing.JFileChooser chooserFS = new javax.swing.JFileChooser();
            chooserFS.setDialogTitle("1. Guardar el sistema de archivos (JSON)");
            if (chooserFS.showSaveDialog(this) != javax.swing.JFileChooser.APPROVE_OPTION) return;
            java.io.File archivoFS = conExtension(chooserFS.getSelectedFile());
            try (java.io.Writer w = new java.io.OutputStreamWriter(new java.io.FileOutputStream(archivoFS), java.nio.charset.StandardCharsets.UTF_8)) {
                sistemaOperativo.CargadorJSON.guardarSistemaArchivos(cluster, w);
            }

            javax.swing.JFileChooser chooserProc = new javax.swing.JFileChooser();
            chooserProc.setDialogTitle("2. Guardar configuración y procesos pendientes (JSON) — opcional");
            if (chooserProc.showSaveDialog(this) == javax.swing.JFileChooser.APPROVE_OPTION) {
                java.io.File archivoProc = conExtension(chooserProc.getSelectedFile());
                try (java.io.Writer w = new java.io.OutputStreamWriter(new java.io.FileOutputStream(archivoProc), java.nio.charset.StandardCharsets.UTF_8)) {
                    sistemaOperativo.CargadorJSON.guardarProcesos(cluster, w,
                            jComboBox1.getSelectedItem().toString(), (int) spnQuantum.getValue());
                }
            }
            avisar("Guardado. El JSON del sistema de archivos se puede volver a cargar en otra ejecución.");
        } catch (Exception e) {
            avisar("Error al guardar: " + e.getMessage());
        }
    }//GEN-LAST:event_guardarJsonActionPerformed

    private void cargarJsonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cargarJsonActionPerformed
         // [AJUSTE 25-nov] La lectura del JSON se movió a sistemaOperativo.CargadorJSON:
        // la ventana solo escoge los archivos.
        if (cluster.getReloj().isCorriendo()) {
            avisar("Pausa la simulación antes de cargar otros JSON.");
            return;
        }
        javax.swing.JFileChooser chooserFS = new javax.swing.JFileChooser();
        chooserFS.setDialogTitle("1. Selecciona el JSON del Sistema de Archivos");
        if (chooserFS.showOpenDialog(this) != javax.swing.JFileChooser.APPROVE_OPTION) return;
        java.io.File archivoFS = chooserFS.getSelectedFile();

        javax.swing.JFileChooser chooserProc = new javax.swing.JFileChooser(archivoFS.getParentFile());
        chooserProc.setDialogTitle("2. Selecciona el JSON de Procesos");
        java.io.File archivoProc = chooserProc.showOpenDialog(this) == javax.swing.JFileChooser.APPROVE_OPTION
                ? chooserProc.getSelectedFile() : null;
        cargarDesdeArchivos(archivoFS, archivoProc);
    }//GEN-LAST:event_cargarJsonActionPerformed

    private void spnQuantumStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spnQuantumStateChanged
        if (cluster == null || sincronizando) return;
        int q = (int) spnQuantum.getValue();

        // Actualiza el Computador 1 si está en Round Robin
        if (jComboBox1.getSelectedItem().toString().equals("Round Robin")) {
            cluster.getComputador(0).setPolitica(new planificacionSO.RoundRobin(q));
        }
    }//GEN-LAST:event_spnQuantumStateChanged

    private void cbxPrioridadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxPrioridadActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cbxPrioridadActionPerformed

    private void cbxTipoProcesoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxTipoProcesoActionPerformed
        boolean es = "IO_BOUND".equals(cbxTipoProceso.getSelectedItem());
        if (cbxOperacion != null) cbxOperacion.setEnabled(es);
        if (txtRuta != null) txtRuta.setEnabled(es);
    }//GEN-LAST:event_cbxTipoProcesoActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
         try {
            String dueno = duenoActual();
            sistemaOperativo.Resultado r = sistemaOperativo.ProcesoESFactory.crear(
                    cluster,
                    txtNombre.getText().trim(),
                    cbxTipoProceso.getSelectedItem().toString(),
                    cbxPrioridad.getSelectedItem().toString(),
                    (int) spnMemoria.getValue(),
                    (int) spnDeadline.getValue(),
                    (int) spnInstrucciones.getValue(),
                    cbxOperacion.getSelectedItem().toString(),
                    txtRuta.getText(),
                    dueno,
                    (int) spnBloques.getValue(),
                    chkDirectorio.isSelected(),
                    chkPublico.isSelected(),
                    txtNuevoNombre.getText().trim().isEmpty() ? null : txtNuevoNombre.getText().trim());

            if (!r.isOk()) {
                avisar("Operación rechazada" + (esAdmin() ? "" : " (modo usuario: " + dueno + ")") + ":\n" + r.getMotivo());
                return;
            }
            avisar("Proceso " + r.getPcb().getNombre() + " creado (P" + r.getPcb().getId() + "). Pasa a la cola de Nuevos.");
            txtNombre.setText("");
        } catch (Exception e) {
            avisar("Error inesperado: " + e.getMessage());
        }

    }//GEN-LAST:event_jButton3ActionPerformed

    private void txtNombreActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtNombreActionPerformed
        jButton3.doClick();
    }//GEN-LAST:event_txtNombreActionPerformed

    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
        if (cluster == null || sincronizando) return;

        String seleccion = (String) jComboBox1.getSelectedItem();
        int q = (int) spnQuantum.getValue();
        cluster.getComputador(0).setPolitica(FabricaPoliticas.crear(seleccion, q));
        cluster.registrarEvento("Política de CPU cambiada a: " + seleccion);

    }//GEN-LAST:event_jComboBox1ActionPerformed

    private void cbxPoliticaDiscoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxPoliticaDiscoActionPerformed
      // [AJUSTE 25-nov] Se crea desde el enum: el Disco no sabe qué política es, solo usa la interfaz
        if (cluster == null || cluster.getDisco() == null || sincronizando) return;
        losEnums.TipoPoliticaDisco tipo = losEnums.TipoPoliticaDisco.desdeTexto(cbxPoliticaDisco.getSelectedItem().toString());
        cluster.getDisco().setPolitica(planificacionSO.FabricaPoliticasDisco.crear(tipo));
        cluster.registrarEvento("Ciclo " + cluster.getReloj().getCiclo() + ": Política de disco cambiada a " + tipo);
    }//GEN-LAST:event_cbxPoliticaDiscoActionPerformed

    private void comboUserAdminActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboUserAdminActionPerformed
        if (lblModoUsuario == null) return; // todavía se está construyendo la ventana
        actualizarModo();
        cluster.registrarEvento("Ciclo " + cluster.getReloj().getCiclo() + ": Modo cambiado a "
                + (esAdmin() ? "ADMINISTRADOR" : "USUARIO (" + duenoActual() + ")"));
    }//GEN-LAST:event_comboUserAdminActionPerformed

  private static java.io.File conExtension(java.io.File f) {
        return f.getName().toLowerCase().endsWith(".json") ? f : new java.io.File(f.getParentFile(), f.getName() + ".json");
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel Computador;
    private javax.swing.JButton btnGrafico;
    private javax.swing.JButton cargarJson;
    private javax.swing.JComboBox<String> cbxOperacion;
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
    private javax.swing.JPanel jPanel5;
    private javax.swing.JProgressBar jProgressBar1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JSpinner jSpinner1;
    private javax.swing.JSpinner spnDeadline;
    private javax.swing.JSpinner spnInstrucciones;
    private javax.swing.JSpinner spnMemoria;
    private javax.swing.JSpinner spnQuantum;
    private javax.swing.JTabbedPane tablaArchivos;
    private javax.swing.JTable tablaDatos;
    private javax.swing.JTextArea txtColaBloqSusp;
    private javax.swing.JTextArea txtColaListosSusp;
    private javax.swing.JTextArea txtColaNuevos;
    private javax.swing.JTextArea txtLog;
    private javax.swing.JTextField txtNombre;
    private javax.swing.JTextField txtRuta;
    // End of variables declaration//GEN-END:variables



}
