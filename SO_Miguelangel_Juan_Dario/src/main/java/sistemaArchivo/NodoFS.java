/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemaArchivo;

import estructuraDeDatos.ListaSimple;

/**
 *
 * @author dariu
 */
public class NodoFS {
    private String nombre;
    private boolean esDirectorio;
    private String dueno;
    private boolean permisoLectura;
    private boolean permisoEscritura;
    private int bloqueInicio; // -1 si es directorio o archivo sin bloques
    private int cantidadBloques;
    private ListaSimple hijos; // Solo se usa si es directorio

    public NodoFS(String nombre, boolean esDirectorio, String dueno) {
        this.nombre = nombre;
        this.esDirectorio = esDirectorio;
        this.dueno = dueno;
        this.permisoLectura = true;
        this.permisoEscritura = true;
        this.bloqueInicio = -1;
        this.cantidadBloques = 0;
        this.hijos = new ListaSimple();
    }

    // Getters y Setters
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public boolean isEsDirectorio() { return esDirectorio; }
    public String getDueno() { return dueno; }
    public boolean isPermisoLectura() { return permisoLectura; }
    public void setPermisoLectura(boolean permisoLectura) { this.permisoLectura = permisoLectura; }
    public boolean isPermisoEscritura() { return permisoEscritura; }
    public void setPermisoEscritura(boolean permisoEscritura) { this.permisoEscritura = permisoEscritura; }
    public int getBloqueInicio() { return bloqueInicio; }
    public void setBloqueInicio(int bloqueInicio) { this.bloqueInicio = bloqueInicio; }
    public int getCantidadBloques() { return cantidadBloques; }
    public void setCantidadBloques(int cantidadBloques) { this.cantidadBloques = cantidadBloques; }
    public ListaSimple getHijos() { return hijos; }
}