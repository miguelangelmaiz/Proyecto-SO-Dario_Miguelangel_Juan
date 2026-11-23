
package gui;

/**
 *
 * @author jmmor
 */
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import javax.swing.JTabbedPane;
import javax.swing.JPanel;
import javax.swing.JLabel;
import java.awt.Font;
import java.awt.BorderLayout;
import sistemaOperativo.Cluster;
import sistemaOperativo.Metricas;


public class VentanaGrafico extends javax.swing.JFrame {

    /**
     * Creates new form VentanaGrafico
     */
    
      public sistemaOperativo.Cluster cluster; 
    
    private XYSeriesCollection datasetDisco;
    private XYSeries serieActual;
    private String ultimaPolitica = "";
    private int tiempo = 0;

    // Labels para las estadísticas del sistema
    private JLabel lblThroughput;
    private JLabel lblTiempoRetorno;
    private JLabel lblTasaPlazos;
    private JLabel lblProcesosTerminados;
    
    
  
    public VentanaGrafico() {
           initComponents(); // Inicializa el diseño base de NetBeans
        
         setTitle("Dashboard de Métricas - ÁvilaOS");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        // Creamos las pestañas
        JTabbedPane pestanas = new JTabbedPane();
        
      
           // ==========================================
        // PESTAÑA 1: DESPLAZAMIENTO DEL DISCO
        // ==========================================
        datasetDisco = new XYSeriesCollection();
        serieActual = new XYSeries("Sin política");
        datasetDisco.addSeries(serieActual);

        JFreeChart graficoDisco = ChartFactory.createXYLineChart(
                "Desplazamiento del Cabezal vs Tiempo",
                "Tiempo (Ciclos)",
                "Desplazamiento Acumulado",
                datasetDisco, PlotOrientation.VERTICAL, true, true, false
        );
        ChartPanel panelDisco = new ChartPanel(graficoDisco);
        pestanas.addTab("Métricas del Disco", panelDisco);

        // ==========================================
        // PESTAÑA 2: MÉTRICAS DEL SISTEMA
        // ==========================================
        JPanel panelSistema = new JPanel();
        panelSistema.setLayout(null); // Posicionamiento libre
        panelSistema.setBorder(javax.swing.BorderFactory.createTitledBorder("Rendimiento Global del Sistema"));

        // Creamos los Labels
        lblThroughput = new JLabel("Throughput: 0.000");
        lblThroughput.setFont(new Font("Arial", Font.BOLD, 16));
        lblThroughput.setBounds(30, 40, 400, 30);
        
        lblTiempoRetorno = new JLabel("Tiempo de Retorno Promedio: 0.00 ciclos");
        lblTiempoRetorno.setFont(new Font("Arial", Font.BOLD, 16));
        lblTiempoRetorno.setBounds(30, 90, 400, 30);
        
        lblTasaPlazos = new JLabel("Tasa de Plazos Cumplidos: 0.0%");
        lblTasaPlazos.setFont(new Font("Arial", Font.BOLD, 16));
        lblTasaPlazos.setBounds(30, 140, 400, 30);
        
        lblProcesosTerminados = new JLabel("Procesos Terminados: 0");
        lblProcesosTerminados.setFont(new Font("Arial", Font.BOLD, 16));
        lblProcesosTerminados.setBounds(30, 190, 400, 30);

        panelSistema.add(lblThroughput);
        panelSistema.add(lblTiempoRetorno);
        panelSistema.add(lblTasaPlazos);
        panelSistema.add(lblProcesosTerminados);

        pestanas.addTab("Estadísticas Sistema", panelSistema);
        this.setContentPane(pestanas);
        
       // ==========================================
        // TIMER DE ACTUALIZACIÓN (Cada 500ms)
        // ==========================================
        javax.swing.Timer timer = new javax.swing.Timer(500, (e) -> {
            if (cluster == null) return;
            
            tiempo++; 
            int cicloActual = cluster.getReloj().getCiclo();
            
            if (cicloActual > 0) {
                // 1. ACTUALIZAR GRÁFICO DEL DISCO
                if (cluster.getDisco() != null) {
                    int desplazamiento = cluster.getDisco().getDesplazamientoTotal();
                    String politicaActual = cluster.getDisco().getPolitica().getNombre();
                    
                    if (!politicaActual.equals(ultimaPolitica)) {
                        if (!ultimaPolitica.isEmpty()) {
                            serieActual = new XYSeries(politicaActual);
                            datasetDisco.addSeries(serieActual);
                        } else {
                            serieActual.setKey(politicaActual);
                        }
                        ultimaPolitica = politicaActual;
                    }
                    serieActual.add(tiempo, (double) desplazamiento);
                }

                  // 2. ACTUALIZAR ESTADÍSTICAS DIRECTAMENTE AQUÍ
                int totalTerminados = 0;
                int totalATiempo = 0;
                double sumaTiempos = 0;

                // Recorremos todos los computadores y sus procesos terminados
                for (int i = 0; i < cluster.getCantidad(); i++) {
                    estructuraDeDatos.Nodo aux = cluster.getComputador(i).getNucleo().getTerminados().getCabeza();
                    while (aux != null) {
                        sistemaOperativo.PCB p = (sistemaOperativo.PCB) aux.getDato();
                        sumaTiempos += (p.getCicloFin() - p.getCicloLlegada());
                        if (p.getDeadline() > 0) totalATiempo++;
                        totalTerminados++;
                        aux = aux.getpNext();
                    }
                }

                // Calculamos los promedios
                double throughput = (cicloActual > 0) ? (double) totalTerminados / cicloActual : 0;
                double tiempoRetorno = (totalTerminados > 0) ? sumaTiempos / totalTerminados : 0;
                double tasaPlazos = (totalTerminados > 0) ? ((double) totalATiempo / totalTerminados) * 100 : 0;

                // Pintamos en los Labels
                lblThroughput.setText("Throughput: " + String.format("%.3f", throughput));
                lblTiempoRetorno.setText("Tiempo de Retorno Promedio: " + String.format("%.2f", tiempoRetorno) + " ciclos");
                lblTasaPlazos.setText("Tasa de Plazos Cumplidos: " + String.format("%.1f", tasaPlazos) + "%");
                lblProcesosTerminados.setText("Procesos Terminados: " + totalTerminados);
        

        
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
