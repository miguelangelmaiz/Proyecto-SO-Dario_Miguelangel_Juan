/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package losEnums;

/**
 *
 * @author Miguel
 */
public enum TipoPoliticaDisco {
     FIFO("FIFO"),
    SSTF("SSTF"),
    SCAN("SCAN"),
    C_SCAN("C-SCAN");

    private final String nombre;

    TipoPoliticaDisco(String nombre) { this.nombre = nombre; }

    public String getNombre() { return nombre; }

    @Override
    public String toString() { return nombre; }

    /** Acepta "C-SCAN", "CSCAN", "c_scan"... Si no la reconoce devuelve FIFO. */
    public static TipoPoliticaDisco desdeTexto(String texto) {
        if (texto == null) return FIFO;
        String t = texto.trim().toUpperCase().replace("-", "").replace("_", "").replace(" ", "");
        for (TipoPoliticaDisco p : values()) {
            if (p.nombre.replace("-", "").equals(t)) return p;
        }
        return FIFO;
    }
}
