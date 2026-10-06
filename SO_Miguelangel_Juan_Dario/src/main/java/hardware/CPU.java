/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hardware;

/**
 *
 * @author jmmor
 */
import Enums.ModoEjecucion;
import Enums.EstadoProceso;
import SO.PCB;
public class CPU implements ComponenteReloj{


    
    private PCB procesoActual;
    private ModoEjecucion modo;
    private int ciclosOcupados;
    
    public CPU(){
    this.procesoActual = null;
    this.ciclosOcupados = 0;
    this.modo = ModoEjecucion.USUARIO;
    }
    
    //GETTERS AND SETTERS

    /**
     * @return the procesoActual
     */
    public PCB getProcesoActual() {
        return procesoActual;
    }

    /**
     * @param procesoActual the procesoActual to set
     */
    public void setProcesoActual(PCB procesoActual) {
        this.procesoActual = procesoActual;
    }

    /**
     * @return the modo
     */
    public ModoEjecucion getModo() {
        return modo;
    }

    /**
     * @param modo the modo to set
     */
    public void setModo(ModoEjecucion modo) {
        this.modo = modo;
    }

    /**
     * @return the ciclosOcupados
     */
    public int getCiclosOcupados() {
        return ciclosOcupados;
    }

    /**
     * @param ciclosOcupados the ciclosOcupados to set
     */
    public void setCiclosOcupados(int ciclosOcupados) {
        this.ciclosOcupados = ciclosOcupados;
    }
    
    //METODOS DE LA CPU
    
    public boolean estaLibre(){
        return procesoActual == null;
    }
    
    /** El proceso pide una operación privilegiada (semWait, semSignal, buffer). */
    public void entrarModoSistema(){
        this.modo = ModoEjecucion.SISTEMA;
    }
    
    public void salirModoSistema(){
        this.modo = ModoEjecucion.USUARIO;
    }
    /** El Núcleo llama a esto para poner un proceso en CPU. */
    public void cargar(PCB p){
     this.procesoActual = p;
    this.modo = ModoEjecucion.USUARIO;
    if (p != null) {
        p.setEstado(EstadoProceso.EJECUCION);
    }
    }
    
    /** El Núcleo llama a esto para vaciar la CPU. Devuelve el proceso saliente. */
public PCB liberar() {
    PCB saliente = this.procesoActual;
    this.procesoActual = null;
    this.modo = ModoEjecucion.USUARIO;
    return saliente;
}
    
    public void reset(){
    this.procesoActual = null;
    this.ciclosOcupados = 0;
    this.modo = ModoEjecucion.USUARIO;
    }

    @Override
public void tic(int ciclo) {
    if (procesoActual == null) {
        return;   // CPU ociosa, no cuenta como ocupada
    }
    ciclosOcupados++;
    procesoActual.ejecutarInstruccion();
}
    

}
