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

/**
 *
 * @author Miguel
 */
public class main {

    public static void main(String[] args) {
        Criterio porPrioridad = (a, b) ->
                ((PCB) a).getPrioridad() > ((PCB) b).getPrioridad();

        ColaPrioridad cp = new ColaPrioridad(porPrioridad);
cp.insertarObjeto(new PCB("A", TipoProceso.CPU_BOUND, 2, 256, 20, 5, 1));
cp.insertarObjeto(new PCB("B", TipoProceso.CPU_BOUND, 5, 256, 20, 5, 1));
cp.insertarObjeto(new PCB("C", TipoProceso.CPU_BOUND, 1, 256, 20, 5, 1));

System.out.println("--- ColaPrioridad ---");
Nodo aux = cp.getCabeza();
while (aux != null) {
    System.out.print(aux.getDato() + " -> ");
    aux = aux.getpNext();
}
System.out.println("NULL");}
}
