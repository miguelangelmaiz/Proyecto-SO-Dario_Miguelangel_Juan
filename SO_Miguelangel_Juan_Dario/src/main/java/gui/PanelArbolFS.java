/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gui;

/**
 *
 * @author dariu
 */

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.util.Stack;

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
        
        Stack<DefaultMutableTreeNode> pila = new Stack<>();
        pila.push(nuevaRaiz);

        for (String linea : lineas) {
            if (linea.trim().isEmpty()) continue;
            
            // Calcula el nivel basado en los espacios (asumiendo 2 espacios por nivel)
            int nivel = (linea.length() - linea.replaceAll("^\\s+", "").length()) / 2;
            String texto = linea.trim();
            
            DefaultMutableTreeNode nodoActual = new DefaultMutableTreeNode(texto);
            
            // Ajustar la pila al nivel actual
            while (pila.size() > nivel + 1) {
                pila.pop();
            }
            
            if (pila.isEmpty()) {
                pila.push(nuevaRaiz); // Fallback
            }
            
            pila.peek().add(nodoActual);
            pila.push(nodoActual);
        }
        
        modelo.setRoot(nuevaRaiz);
        // Expandir todo automáticamente
        for (int i = 0; i < arbol.getRowCount(); i++) {
            arbol.expandRow(i);
        }
    }
}