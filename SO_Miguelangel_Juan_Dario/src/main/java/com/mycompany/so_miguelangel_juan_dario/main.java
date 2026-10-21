
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
import gui.Interfaz;
/**
 *
 * @author Miguel
 */
public class main {

     public static void main(String[] args) throws InterruptedException {
         sistemaOperativo.Cluster cluster; 
   try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(Interfaz.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                // 1. Creamos el Clúster (2 computadores, 2048 KB de RAM, 500ms de velocidad)
                sistemaOperativo.Cluster miCluster = new sistemaOperativo.Cluster(2, 2048, 500);
                
                // 2. Creamos la ventana
                Interfaz ventana = new Interfaz();
                
                // 3. Le inyectamos el Clúster a la ventana para que los botones funcionen
                ventana.cluster = miCluster; 
                
                // 4. Mostramos la ventana
                ventana.setVisible(true);
            }
        });
     }
    }
        
    

