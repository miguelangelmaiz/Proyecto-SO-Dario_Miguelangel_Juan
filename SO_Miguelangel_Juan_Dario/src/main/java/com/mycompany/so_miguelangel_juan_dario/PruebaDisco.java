package com.mycompany.so_miguelangel_juan_dario;


import hardware.Disco;
import sistemaOperativo.SolicitudES;
import losEnums.TipoOperacionES;
import planificacionSO.*;

/**
 *
 * @author jmmor
 */
public class PruebaDisco {
    
    
    public static void main(String[] args) {
        System.out.println("=== PRUEBA DE POLÍTICAS DE DISCO (ANEXO) ===");
        
        correrPolitica("FIFO", new PoliticaFIFO());
        correrPolitica("SSTF", new PoliticaSSTF());
        correrPolitica("SCAN", new PoliticaSCAN());
        correrPolitica("C-SCAN", new PoliticaCSCAN());
    }

    private static void correrPolitica(String nombre, PoliticaDisco pol) {
        Disco disco = new Disco();
        disco.setTotalBloques(200);
        disco.setCabezal(50); // Cabezal inicial en 50
        disco.setPolitica(pol);

        // Solicitudes del anexo: 82, 17, 43, 140, 24, 16, 190
        int[] solicitudes = {82, 17, 43, 140, 24, 16, 190};
        
        for (int bloque : solicitudes) {
            disco.encolarSolicitud(new SolicitudES(null, TipoOperacionES.LEER, bloque, 1, 1));
        }

        int ciclo = 1;
        while (disco.getSolicitudesAtendidas() < 7 && ciclo < 1000) {
            disco.tic(ciclo);
            ciclo++;
        }

        // Imprimimos el resultado
        System.out.println("Política: " + nombre);
        System.out.println("Orden de atención: " + disco.ordenAtencion.toString());
        System.out.println("Desplazamiento Total: " + disco.getDesplazamientoTotal() + " bloques");
        System.out.println("--------------------------------------------------");
    }
}