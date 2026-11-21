package gui;

/**
 *
 * @author dariu
 */

/**
 * @author dariu
 */
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import estructuraDeDatos.ListaSimple; // Usamos nuestra propia lista

public class PanelArbolFS extends JPanel {
    private JTree arbol;
    private DefaultTreeModel modelo;

    public PanelArbolFS() {
        setLayout(new BorderLayout());
        DefaultMutableTreeNode raizVisible = new DefaultMutableTreeNode("Sistema de Archivos");
        modelo = new DefaultTreeModel(raizVisible);
        arbol = new JTree(modelo);
        add(new JScrollPane(arbol), BorderLayout.CENTER);
    }

    public void actualizarArbol(String snapshot) {
        if (snapshot == null || snapshot.isEmpty()) return;

        DefaultMutableTreeNode nuevaRaiz = new DefaultMutableTreeNode("Sistema de Archivos");
        String[] lineas = snapshot.split("\n");
        
        // Simulamos una Pila usando nuestra ListaSimple
        ListaSimple pila = new ListaSimple();
        pila.insertFinale(nuevaRaiz); // Push inicial

        for (String linea : lineas) {
            if (linea.trim().isEmpty()) continue;
            
            // Calcula el nivel basado en los espacios (2 espacios por nivel)
            int nivel = (linea.length() - linea.replaceAll("^\\s+", "").length()) / 2;
            String texto = linea.trim();
            
            DefaultMutableTreeNode nodoActual = new DefaultMutableTreeNode(texto);
            
            // Ajustar la "pila" al nivel actual (Pop)
            while (pila.getSize() > nivel + 1) {
                pila.deleteFinale();
            }
            
            if (pila.isEmpty()) {
                pila.insertFinale(nuevaRaiz); // Fallback
            }
            
            // Peek (obtener el padre) y agregar el nodo
            DefaultMutableTreeNode padre = (DefaultMutableTreeNode) pila.getValue(pila.getSize() - 1);
            padre.add(nodoActual);
            
            // Push del nodo actual
            pila.insertFinale(nodoActual);
        }
        
        modelo.setRoot(nuevaRaiz);
        // Expandir todo automáticamente
        for (int i = 0; i < arbol.getRowCount(); i++) {
            arbol.expandRow(i);
        }
    }
}