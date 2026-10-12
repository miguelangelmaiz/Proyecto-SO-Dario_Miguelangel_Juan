/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package SO.Sincronizacion;

import Enums.EstadoProceso;
import Enums.MotivoBloqueo;
import SO.Nucleo;
import SO.PCB;
import SO.Planificacion.Semaforo;

/**
 *
 * @author dariu
 */
public class Buffer {

    private final int id;
    private final int capacidad;
    private final int anfitrion;      // id del computador dueño
    private int elementos;

    private final Semaforo mutex;
    private final Semaforo vacios;
    private final Semaforo llenos;

    public Buffer(int id, int capacidad, int anfitrion) {
        this.id = id;
        this.capacidad = capacidad;
        this.anfitrion = anfitrion;
        this.elementos = 0;
        this.mutex  = new Semaforo(1, "mutex");
        this.vacios = new Semaforo(capacidad, "vacios");
        this.llenos = new Semaforo(0, "llenos");
    }

    public int getId() { return id; }
    public int getCapacidad() { return capacidad; }
    public int getAnfitrion() { return anfitrion; }
    public int getElementos() { return elementos; }
    public Semaforo getMutex()  { return mutex; }
    public Semaforo getVacios() { return vacios; }
    public Semaforo getLlenos() { return llenos; }

    /**
     * Intenta producir. Devuelve true si se añadió el elemento.
     * Devuelve false si el proceso quedó BLOQUEADO en algún semáforo.
     */
    public boolean producir(PCB p, Nucleo k) {
        // 1) ¿hay hueco?
        vacios.semWait(p, k, MotivoBloqueo.SEMAFORO_LLENO);
        if (p.getEstado() == EstadoProceso.BLOQUEADO) return false;

        // 2) entro a región crítica
        mutex.semWait(p, k, MotivoBloqueo.REGION_CRITICA);
        if (p.getEstado() == EstadoProceso.BLOQUEADO) {
            // No obtuve mutex: devuelvo el hueco que ya había reservado
            vacios.semSignal(k);
            return false;
        }

        // 3) añado y salgo
        elementos++;
        mutex.semSignal(k);
        llenos.semSignal(k);
        return true;
    }

    /**
     * Intenta consumir. Devuelve true si se extrajo el elemento.
     */
    public boolean consumir(PCB p, Nucleo k) {
        llenos.semWait(p, k, MotivoBloqueo.SEMAFORO_VACIO);
        if (p.getEstado() == EstadoProceso.BLOQUEADO) return false;

        mutex.semWait(p, k, MotivoBloqueo.REGION_CRITICA);
        if (p.getEstado() == EstadoProceso.BLOQUEADO) {
            llenos.semSignal(k);
            return false;
        }

        elementos--;
        mutex.semSignal(k);
        vacios.semSignal(k);
        return true;
    }

    @Override
    public String toString() {
        return "Buffer B" + id + "[cap=" + capacidad + ", elem=" + elementos +
               ", anfitrion=C" + anfitrion + "]";
    }
}