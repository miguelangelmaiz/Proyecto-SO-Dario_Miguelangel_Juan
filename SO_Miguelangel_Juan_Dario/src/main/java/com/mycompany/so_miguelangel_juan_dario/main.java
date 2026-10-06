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
        PCB p = new PCB("tarea", TipoProceso.CPU_BOUND,
                        2, 256, 20, 5, 1);

        CPU cpu = new CPU();
        System.out.println("inicio: " + cpu.estaLibre());     // true

        cpu.cargar(p);
        System.out.println("cargado: " + cpu.estaLibre());    // false
        System.out.println("estado: " + p.getEstado());       // EJECUCION
        System.out.println("modo: " + cpu.getModo());         // USUARIO

        for (int c = 1; c <= 6; c++) {
            cpu.tic(c);
            System.out.println("ciclo " + c
                    + " | pc=" + p.getPc()
                    + " mar=" + p.getMar()
                    + " restante=" + p.getRestante()
                    + " estado=" + p.getEstado()
                    + " ocupada=" + cpu.getCiclosOcupados());
        }

        cpu.liberar();
        System.out.println("fin libre? " + cpu.estaLibre());  // true
}
}
