
package gui;
import sistemaOperativo.Cluster;
import estructuraDeDatos.Cola;
/**
 *
 * @author jmmor
 */
public class ventanaBuffer extends javax.swing.JFrame {
public sistemaOperativo.Cluster cluster;
    /**
     * Creates new form ventanaBuffer
     */
    public ventanaBuffer() {
        
        initComponents();
        Capacidad.setModel(new javax.swing.SpinnerNumberModel(1, 1, 1000, 1));

        javax.swing.Timer timer = new javax.swing.Timer(100, (e) -> {
            if (cluster == null) return;
            
            estructuraDeDatos.ListaSimple lista = cluster.getGestorBuffers().getLista();
            StringBuilder texto = new StringBuilder();
            
            int totalProd = 0;
            int totalCons = 0;

            if (lista.getSize() == 0) {
                texto.append("No hay buffers creados aún.");
                barraBuffer.setValue(0);
                cbxSelectorBuffer.removeAllItems();
            } else {
                // --- LÓGICA PARA LLENAR EL COMBOBOX DINÁMICAMENTE ---
                if (cbxSelectorBuffer.getItemCount() != lista.getSize()) {
                    cbxSelectorBuffer.removeAllItems();
                    for (int i = 0; i < lista.getSize(); i++) {
                        sistemaOperativo.Buffer b = (sistemaOperativo.Buffer) lista.getValue(i);
                        cbxSelectorBuffer.addItem("Buffer " + b.getId() + " (Comp " + b.getAnfitrion() + ")");
                    }
                    if (cbxSelectorBuffer.getItemCount() > 0) {
                        cbxSelectorBuffer.setSelectedIndex(0); 
                    }
                }

                // --- IMPRIMIR TODOS LOS BUFFERS EN EL TEXTO DE ABAJO ---
                for (int i = 0; i < lista.getSize(); i++) {
                    sistemaOperativo.Buffer b = (sistemaOperativo.Buffer) lista.getValue(i);
                    
                    texto.append("BUFFER #").append(b.getId()).append(" (En Comp ").append(b.getAnfitrion()).append(")\n");
                    texto.append("Capacidad: ").append(b.getCapacidad()).append(" | Elementos: ").append(b.getElementos()).append("\n");
                    
                    texto.append("Semáforos -> Mutex: ").append(b.getMutex().getValor())
                         .append(" | Vacíos: ").append(b.getVacios().getValor())
                         .append(" | Llenos: ").append(b.getLlenos().getValor()).append("\n");
                         
                    texto.append("   * En Vacíos: ").append(b.getVacios().getBloqueados().getNombresProcesos()).append("\n");
                    texto.append("   * En Llenos: ").append(b.getLlenos().getBloqueados().getNombresProcesos()).append("\n");
                    texto.append("   * En Mutex: ").append(b.getMutex().getBloqueados().getNombresProcesos()).append("\n");
                    
                    texto.append("--------------------------------------------------\n");
                    
                    totalProd += b.getTotalProducidos();
                    totalCons += b.getTotalConsumidos();
                }

                // --- ACTUALIZAR LA BARRA DE PROGRESO SEGÚN EL COMBOBOX ---
                int indiceSeleccionado = cbxSelectorBuffer.getSelectedIndex();
                if (indiceSeleccionado >= 0 && indiceSeleccionado < lista.getSize()) {
                    sistemaOperativo.Buffer bSeleccionado = (sistemaOperativo.Buffer) lista.getValue(indiceSeleccionado);
                    barraBuffer.setMaximum(bSeleccionado.getCapacidad());
                    barraBuffer.setValue(bSeleccionado.getElementos());
                    barraBuffer.setString(bSeleccionado.getElementos() + " / " + bSeleccionado.getCapacidad());
                    barraBuffer.setStringPainted(true);
                }
            }
            txtEstadoBuffer.setText(texto.toString());

            // --- ACTUALIZAR LAS MÉTRICAS ---
            int cicloActual = cluster.getReloj().getCiclo();
            int indiceSeleccionado = cbxSelectorBuffer.getSelectedIndex();
            
            if (indiceSeleccionado >= 0 && indiceSeleccionado < lista.getSize()) {
                // CASO 1: Hay un buffer seleccionado. Mostramos métricas SOLO de su computador.
                sistemaOperativo.Buffer bSeleccionado = (sistemaOperativo.Buffer) lista.getValue(indiceSeleccionado);
                int idCompAnfitrion = bSeleccionado.getAnfitrion(); 
                
                int terminadosComp = cluster.getComputador(idCompAnfitrion - 1).getNucleo().getTerminados().getTamano();
                
                if (cicloActual > 0) {
                    double throughputComp = (double) terminadosComp / cicloActual;
                    jLabel8.setText("Throughput (C" + idCompAnfitrion + "): " + String.format("%.2f", throughputComp));
                }
                
                jLabel6.setText("Producidos (C" + idCompAnfitrion + "): " + bSeleccionado.getTotalProducidos());
                jLabel5.setText("Consumidos (C" + idCompAnfitrion + "): " + bSeleccionado.getTotalConsumidos());
                
                double tr = cluster.getComputador(idCompAnfitrion - 1).getNucleo().getTiempoRespuestaPromedio();
                jLabel4.setText("Tiempo Respuesta (C" + idCompAnfitrion + "): " + String.format("%.2f", tr));
                
            } else {
                // CASO 2: No hay selección. Mostramos los totales.
                int terminadosC1 = cluster.getComputador(0).getNucleo().getTerminados().getTamano();
                int terminadosC2 = cluster.getComputador(1).getNucleo().getTerminados().getTamano();
                int totalTerminados = terminadosC1 + terminadosC2;
                
                if (cicloActual > 0) {
                    double throughput = (double) totalTerminados / cicloActual;
                    jLabel8.setText("Throughput Total: " + String.format("%.2f", throughput));
                }
                
                jLabel6.setText("Total Producidos: " + totalProd);
                jLabel5.setText("Total Consumidos: " + totalCons);
                
                double tr1 = cluster.getComputador(0).getNucleo().getTiempoRespuestaPromedio();
                double tr2 = cluster.getComputador(1).getNucleo().getTiempoRespuestaPromedio();
                double promedioReal = 0;
                int pcsConDatos = 0;
                if (terminadosC1 > 0) { promedioReal += tr1; pcsConDatos++; }
                if (terminadosC2 > 0) { promedioReal += tr2; pcsConDatos++; }

                if (pcsConDatos > 0) {
                    jLabel4.setText("Tiempo Respuesta Promedio: " + String.format("%.2f", promedioReal / pcsConDatos));
                } else {
                    jLabel4.setText("Tiempo Respuesta Promedio: 0");
                }
            }
            jLabel7.setText("Tasa Plazos Cumplidos: 100%");
        });
        
        timer.start();
        
        
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        Capacidad = new javax.swing.JSpinner();
        jLabel3 = new javax.swing.JLabel();
        Host = new javax.swing.JComboBox<>();
        crearBuffer = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtEstadoBuffer = new javax.swing.JTextArea();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        barraBuffer = new javax.swing.JProgressBar();
        jLabel6 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        cbxSelectorBuffer = new javax.swing.JComboBox<>();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        jLabel1.setText("VENTANA DE BUFFERS");
        jPanel1.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 10, -1, -1));

        jLabel2.setText("Computador Anfitrion:");
        jPanel1.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 120, -1, -1));
        jPanel1.add(Capacidad, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 70, 80, -1));

        jLabel3.setText("Capacidad: ");
        jPanel1.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 70, -1, -1));

        Host.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Computador 1", "Computador 2" }));
        jPanel1.add(Host, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 120, -1, -1));

        crearBuffer.setText("Crear Buffer");
        crearBuffer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                crearBufferActionPerformed(evt);
            }
        });
        jPanel1.add(crearBuffer, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 160, -1, -1));

        txtEstadoBuffer.setColumns(20);
        txtEstadoBuffer.setRows(5);
        jScrollPane1.setViewportView(txtEstadoBuffer);

        jPanel1.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 240, 820, 250));

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel4.setText("Tiempo Respuesta Promedio: 0");
        jPanel1.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(280, 60, -1, -1));

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel5.setText("Elementos Consumido: 0");
        jPanel1.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(580, 140, -1, -1));
        jPanel1.add(barraBuffer, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 200, -1, 30));

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel6.setText("Elementos Producido: 0");
        jPanel1.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(580, 100, -1, -1));

        jLabel8.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel8.setText("Throughput: 0");
        jPanel1.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(280, 100, -1, -1));

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel7.setText("Tasa Plazos Cumplidos: 0%");
        jPanel1.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(280, 140, -1, -1));

        cbxSelectorBuffer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbxSelectorBufferActionPerformed(evt);
            }
        });
        jPanel1.add(cbxSelectorBuffer, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 200, 110, 30));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 821, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 12, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 6, Short.MAX_VALUE))
        );

        jPanel1.getAccessibleContext().setAccessibleDescription("");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void crearBufferActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_crearBufferActionPerformed
try {
    
            if (cluster == null) {
                javax.swing.JOptionPane.showMessageDialog(this, "Error: No hay conexión con el simulador.");
                return;
            }

            // 1. Atrapamos los datos de tu diseño
            int capacidad = (int) Capacidad.getValue();
            // El índice 0 es Comp 1, el índice 1 es Comp 2. Le sumamos 1 para que el ID sea 1 o 2.
            int host = Host.getSelectedIndex() + 1; 

            // 2. Le pedimos al GestorBuffers que cree el buffer
            cluster.getGestorBuffers().crearBuffer(capacidad, host);
            
            javax.swing.JOptionPane.showMessageDialog(this, "¡Buffer creado exitosamente en el Computador " + host + "!");
            
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this, "Error al crear buffer: " + e.getMessage());
        }   // TODO add your handling code here:
    }//GEN-LAST:event_crearBufferActionPerformed

    private void cbxSelectorBufferActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxSelectorBufferActionPerformed
        
    }//GEN-LAST:event_cbxSelectorBufferActionPerformed

    /**
     * @param args the command line arguments
     */
   

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSpinner Capacidad;
    private javax.swing.JComboBox<String> Host;
    private javax.swing.JProgressBar barraBuffer;
    private javax.swing.JComboBox<String> cbxSelectorBuffer;
    private javax.swing.JButton crearBuffer;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea txtEstadoBuffer;
    // End of variables declaration//GEN-END:variables
}
