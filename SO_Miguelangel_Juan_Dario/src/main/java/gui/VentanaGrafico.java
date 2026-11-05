
package gui;

/**
 *
 * @author jmmor
 */
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import java.awt.Color;
import java.awt.BorderLayout;
import javax.swing.JTabbedPane;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import estructuraDeDatos.Nodo;


public class VentanaGrafico extends javax.swing.JFrame {

    /**
     * Creates new form VentanaGrafico
     */
    
    public sistemaOperativo.Cluster cluster;
    private DefaultCategoryDataset datosCpu;
    private int tiempo = 0;

      // Datos para el gráfico 2 (RAM)
    private DefaultCategoryDataset datosRam;

    // Datos para el gráfico 3 (Estados de Procesos)
    private DefaultCategoryDataset datosEstados;
    
  
    public VentanaGrafico() {
           initComponents(); // Inicializa el diseño base de NetBeans
        
         setTitle("Dashboard de Métricas - ÁvilaOS");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        // Creamos las pestañas
        JTabbedPane pestanas = new JTabbedPane();

        // ==========================================
        // PESTAÑA 1: UTILIZACIÓN DE CPU
        // ==========================================
        datosCpu = new DefaultCategoryDataset();
        JFreeChart graficoCpu = ChartFactory.createLineChart(
                "Utilización del Procesador", "Tiempo (Ciclos)", "Ciclos Ocupados", 
                datosCpu, PlotOrientation.VERTICAL, true, true, false
        );
        ChartPanel panelCpu = new ChartPanel(graficoCpu);
        pestanas.addTab("Utilización de CPU", panelCpu);

        // ==========================================
        // PESTAÑA 2: MEMORIA RAM
        // ==========================================
        datosRam = new DefaultCategoryDataset();
        JFreeChart graficoRam = ChartFactory.createBarChart(
                "Estado de la Memoria RAM por Computador", "Computador", "Memoria (KB)", 
                datosRam, PlotOrientation.VERTICAL, true, true, false
        );
        ChartPanel panelRam = new ChartPanel(graficoRam);
        pestanas.addTab("Memoria RAM", panelRam);

        // ==========================================
        // PESTAÑA 3: ESTADOS DE LOS PROCESOS
        // ==========================================
        datosEstados = new DefaultCategoryDataset();
        JFreeChart graficoEstados = ChartFactory.createBarChart(
                "Cantidad de Procesos por Estado (Global)", "Estado", "Cantidad de Procesos", 
                datosEstados, PlotOrientation.VERTICAL, true, true, false
        );
        ChartPanel panelEstados = new ChartPanel(graficoEstados);
        pestanas.addTab("Estados de Procesos", panelEstados);

        // Agregamos las pestañas a la ventana
        this.setContentPane(pestanas);

        // ==========================================
        // TIMER DE ACTUALIZACIÓN
        // ==========================================
        javax.swing.Timer timer = new javax.swing.Timer(500, (e) -> {
            if (cluster == null) return;
            tiempo++; 
            int cicloActual = cluster.getReloj().getCiclo();
            
            if (cicloActual > 0) {
                // --- 1. ACTUALIZAR GRÁFICO DE CPU ---
                LineAndShapeRenderer renderer = (LineAndShapeRenderer) graficoCpu.getCategoryPlot().getRenderer();
                Color[] colores = {Color.RED, Color.BLUE, Color.GREEN, Color.ORANGE}; 
                
                for (int i = 0; i < cluster.getCantidad(); i++) {
                    int usoCpu = cluster.getComputador(i).getCpu().getCiclosOcupados();
                    String nombreComp = "Computador " + (i + 1);
                    datosCpu.addValue(usoCpu, nombreComp, String.valueOf(tiempo));
                    renderer.setSeriesPaint(i, colores[i % colores.length]);
                }

                // --- 2. ACTUALIZAR GRÁFICO DE RAM ---
                for (int i = 0; i < cluster.getCantidad(); i++) {
                    int ramUsada = cluster.getComputador(i).getRam().getUsada();
                    int ramLibre = cluster.getComputador(i).getRam().getLibre();
                    String nombreComp = "Comp " + (i + 1);
                    
                    // Se actualizan los valores (reemplazando el anterior)
                    datosRam.setValue(ramUsada, "Usada", nombreComp);
                    datosRam.setValue(ramLibre, "Libre", nombreComp);
                }

                // --- 3. ACTUALIZAR GRÁFICO DE ESTADOS ---
                int nuevos = 0, listos = 0, ejecucion = 0, bloqueados = 0, terminados = 0;
                
                for (int i = 0; i < cluster.getCantidad(); i++) {
                    sistemaOperativo.Nucleo nucleo = cluster.getComputador(i).getNucleo();
                    nuevos += nucleo.getColaNuevos().getTamano();
                    listos += nucleo.getColaListos().getTamano();
                    bloqueados += nucleo.getColaBloqueados().getTamano();
                    terminados += nucleo.getTerminados().getTamano();
                    
                    if (nucleo.getCpu().getProcesoActual() != null) {
                        ejecucion++;
                    }
                }
                
                datosEstados.setValue(nuevos, "Procesos", "Nuevos");
                datosEstados.setValue(listos, "Procesos", "Listos");
                datosEstados.setValue(ejecucion, "Procesos", "Ejecución");
                datosEstados.setValue(bloqueados, "Procesos", "Bloqueados");
                datosEstados.setValue(terminados, "Procesos", "Terminados");
            }
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

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jLabel1.setFont(new java.awt.Font("Segoe UI", 0, 36)); // NOI18N
        jLabel1.setText("Grafico");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(404, 404, 404)
                .addComponent(jLabel1)
                .addContainerGap(433, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addContainerGap(523, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

   
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables
}
