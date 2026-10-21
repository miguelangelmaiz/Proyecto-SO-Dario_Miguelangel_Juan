
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
    public static void main(String[] args) {
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

        java.awt.EventQueue.invokeLater(() -> {
            new Interfaz().setVisible(true);
        });
    }
}
        
    

