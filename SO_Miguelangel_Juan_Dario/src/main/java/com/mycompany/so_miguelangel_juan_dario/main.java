/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.so_miguelangel_juan_dario;
import EDD.Cola;
import EDD.ColaPrioridad;
import SO.PCB;
import EDD.Nodo;
import Enums.TipoProceso;
import EDD.Criterio;
import hardware.CPU;
import hardware.ComponenteReloj;
import hardware.MemoriaPrincipal;

/**
 *
 * @author Miguel
 */
public class main {

    public static void main(String[] args) {
        MemoriaPrincipal ram = new MemoriaPrincipal(1024);

System.out.println("libre inicial: " + ram.getLibre());   // 1024
System.out.println("hay 512? " + ram.hayEspacio(512));    // true

ram.asignar(512);
System.out.println("usada: " + ram.getUsada());           // 512
System.out.println("libre: " + ram.getLibre());           // 512

System.out.println("hay 700? " + ram.hayEspacio(700));    // false
System.out.println("asignar 700: " + ram.asignar(700));   // false

ram.liberar(512);
System.out.println("libre tras liberar: " + ram.getLibre());
        
        
    }
}
