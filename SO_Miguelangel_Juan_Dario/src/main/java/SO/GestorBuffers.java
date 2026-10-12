/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package SO;

import EDD.ListaSimple;
/**
 *
 * @author Miguel
 */
public class GestorBuffers {
    private final ListaSimple buffers;
    private int siguienteId;

    public GestorBuffers() {
        this.buffers = new ListaSimple();
        this.siguienteId = 1;
    }

    /** Crea un buffer nuevo con id autogenerado. */
    public Buffer crearBuffer(int capacidad, int anfitrion) {
        Buffer b = new Buffer(siguienteId++, capacidad, anfitrion);
        buffers.insertFinale(b);
        return b;
    }

    /** Crea un buffer con id explícito (útil para leer JSON). */
    public Buffer crearBuffer(int id, int capacidad, int anfitrion) {
        Buffer b = new Buffer(id, capacidad, anfitrion);
        buffers.insertFinale(b);
        if (id >= siguienteId) siguienteId = id + 1;
        return b;
    }

    /** Devuelve null si no existe. */
    public Buffer getBuffer(int id) {
        for (int i = 0; i < buffers.getSize(); i++) {
            Buffer b = (Buffer) buffers.getValue(i);
            if (b.getId() == id) return b;
        }
        return null;
    }

    public int getCantidad() { return buffers.getSize(); }
    public ListaSimple getLista() { return buffers; }
}
