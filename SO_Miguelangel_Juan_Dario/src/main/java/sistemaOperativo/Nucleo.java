
package sistemaOperativo;

import estructuraDeDatos.Cola;
import estructuraDeDatos.Criterio;
import losEnums.EstadoProceso;
import losEnums.MotivoBloqueo;
import losEnums.TipoProceso;
import losEnums.TipoOperacionES;
import hardware.CPU;
import hardware.MemoriaPrincipal;
import planificacionSO.PoliticaPlanificacion;
import sistemaArchivo.MapaBloques;


/**
 *
 * @author Miguel
 */
public class Nucleo {
    
    private final int idComputador;
    private final CPU cpu;
    private final MemoriaPrincipal ram;
    private PoliticaPlanificacion politica;
    private Cluster cluster;

    private final Cola colaNuevos;   // cola a largo plazo
    private final Cola colaListos;
    private final Cola colaBloqueados;
    private final Cola terminados;

    private final Cola colaListosSusp;
    private final Cola colaBloqueadosSusp;

    private int ciclosEnCpu = 0;

    // [AJUSTE 25-nov] planificador a mediano plazo
    private int memoriaPorLiberar = 0;  // RAM de los procesos que el disco está escribiendo en el swap
    private int suspensiones = 0;
    private int reactivaciones = 0;
    private PCB ultimoSinVictima = null; // para no repetir el mismo aviso en el log cada ciclo

    public Nucleo(int idComputador, CPU cpu, MemoriaPrincipal ram,
                  PoliticaPlanificacion politica) {
        this.idComputador = idComputador;
        this.cpu = cpu;
        this.ram = ram;
        this.politica = politica;
        this.colaNuevos = new Cola();
        this.colaListos = new Cola();
        this.colaBloqueados = new Cola();
        this.terminados = new Cola();

        this.colaListosSusp = new Cola();
        this.colaBloqueadosSusp = new Cola();
    }

    // ---------- Getters ----------
    public int getIdComputador() { return idComputador; }

    public Cola getColaNuevos()     { return colaNuevos; }
    public Cola getColaListos()     { return colaListos; }
    public Cola getColaBloqueados() { return colaBloqueados; }
    public Cola getTerminados()     { return terminados; }
    public Cola getColaListosSusp()     { return colaListosSusp; }
    public Cola getColaBloqueadosSusp() { return colaBloqueadosSusp; }
    public int getSuspensiones()   { return suspensiones; }
    public int getReactivaciones() { return reactivaciones; }

    public CPU getCpu() { return cpu; }
    public MemoriaPrincipal getRam() { return ram; }
    public PoliticaPlanificacion getPolitica() { return politica; }
    public void setPolitica(PoliticaPlanificacion politica) { this.politica = politica; }

    public Cluster getCluster() { return cluster; }
    public void setCluster(Cluster cluster) { this.cluster = cluster; }

    private int ciclo() { return cluster.getReloj().getCiclo(); }
    private void log(String texto) { cluster.registrarEvento("Ciclo " + ciclo() + ": " + texto); }
    private MapaBloques mapa() { return cluster.getSistemaArchivos().getMapaBloques(); }
    private static boolean esAdmin(String dueno) { return "admin".equals(dueno); }

    // ---------- Admitir ----------
    public void admitir(PCB p) {
        p.setEstado(EstadoProceso.NUEVO);
        p.setCicloLlegada(cluster.getReloj().getCiclo());
        colaNuevos.encolar(p);
        cluster.registrarEvento("Ciclo " + cluster.getReloj().getCiclo() +
                ": Proceso " + p.getNombre() + " creado y admitido en cola NUEVOS.");
    }

    public Resultado crearProcesoES(String nombre, TipoProceso tipo, int prioridad,
                                    int memoria, int deadline, int restante,
                                    TipoOperacionES operacion, String ruta,
                                    int bloques, String dueno,
                                    boolean esDirectorio, boolean publico, String nuevoNombre) {
        return crearProcesoES(nombre, tipo, prioridad, memoria, deadline, restante, operacion, ruta,
                bloques, dueno, esDirectorio, publico, nuevoNombre, true);
    }

    /**
     * validarConFS = false se usa al cargar el JSON de procesos: ahí el orden importa (un
     * proceso puede leer un archivo que otro del mismo JSON crea antes), así que la ruta se
     * revisa cuando el disco atiende la solicitud y el error queda en el log.
     */
    public Resultado crearProcesoES(String nombre, TipoProceso tipo, int prioridad,
                                    int memoria, int deadline, int restante,
                                    TipoOperacionES operacion, String ruta,
                                    int bloques, String dueno,
                                    boolean esDirectorio, boolean publico, String nuevoNombre,
                                    boolean validarConFS) {
        if (nombre == null || nombre.trim().isEmpty())
            return Resultado.error("El nombre no puede estar vacío.");
        if (memoria <= 0 || deadline <= 0 || restante <= 0)
            return Resultado.error("Memoria, deadline e instrucciones deben ser > 0.");
        if (memoria > ram.getTotal())
            return Resultado.error("RAM insuficiente: pide " + memoria
                    + " / total " + ram.getTotal() + ".");
        if (tipo == TipoProceso.IO_BOUND) {
            if (operacion == null)
                return Resultado.error("Debe indicar una operación de E/S.");
            if (ruta == null || ruta.trim().isEmpty())
                return Resultado.error("Debe indicar la ruta del archivo.");
            if (!ruta.trim().startsWith("/"))
                return Resultado.error("La ruta debe empezar con / : " + ruta);
            // [AJUSTE 25-nov] Se valida ANTES de crear el proceso: así la interfaz explica el rechazo
            String error = !validarConFS ? null : cluster.getSistemaArchivos().validar(operacion, ruta.trim(), dueno,
                    esAdmin(dueno), esDirectorio, bloques, nuevoNombre);
            if (error != null) {
                log("Se rechaza el proceso " + nombre + " (" + dueno + "): " + error);
                return Resultado.error(error);
            }
        }

        PCB p = new PCB(nombre, tipo, prioridad, memoria, deadline, restante, idComputador);
        p.setOperacion(tipo == TipoProceso.IO_BOUND ? operacion : null);
        p.setRutaArchivo(ruta == null ? null : ruta.trim());
        p.setBloques(bloques);
        p.setDueno(dueno);
        p.setEsDirectorio(esDirectorio);
        p.setPublico(publico);
        p.setNuevoNombre(nuevoNombre);
        admitir(p);
        return Resultado.ok(p);
    }

    // ---------- Ciclo ----------
    public void tic(int ciclo) {
        vencerPlazos(ciclo);
        contarCiclosBloqueado();
        admitirSiHayMemoria(ciclo);
        reactivar(ciclo);
        planificar(ciclo);
        ejecutarInstruccion(ciclo);
        if (!cpu.estaLibre()) ciclosEnCpu++; else ciclosEnCpu = 0;
        cerrarCiclo(ciclo);
    }

    /**
     * [AJUSTE 25-nov] Única salida hacia TERMINADO: libera la RAM y el swap una sola vez.
     */
    private void terminar(PCB p, int ciclo, String motivo) {
        if (p.isEnRam()) {
            ram.liberar(p.getMemoria());
            p.setEnRam(false);
        }
        if (p.isSuspensionPendiente()) {
            memoriaPorLiberar -= p.getMemoria();
            p.setSuspensionPendiente(false);
        }
        if (p.getPrimerBloqueSwap() >= 0) {
            try {
                mapa().liberarCadena(p.getPrimerBloqueSwap());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            p.setPrimerBloqueSwap(-1);
        }
        p.setEstado(EstadoProceso.TERMINADO);
        p.setCicloFin(ciclo);
        terminados.encolar(p);
        cluster.registrarEvento("Ciclo " + ciclo + ": Proceso " + p.getNombre() + " " + motivo);
    }

    // ---------- Paso 1: vencer plazos ----------
    private void vencerPlazos(int ciclo) {
        vencerEnCola(colaNuevos, ciclo);
        vencerEnCola(colaListos, ciclo);
        vencerEnCola(colaBloqueados, ciclo);
        vencerEnCola(colaListosSusp, ciclo);
        vencerEnCola(colaBloqueadosSusp, ciclo);

        PCB enCpu = cpu.getProcesoActual();
        if (enCpu != null) {
            enCpu.setDeadline(enCpu.getDeadline() - 1);
            if (enCpu.getDeadline() <= 0) {
                cpu.liberar();
                terminar(enCpu, ciclo, "terminó por DEADLINE en CPU.");
            }
        }
    }

    /**
     * [AJUSTE 25-nov] Recorre una copia de la cola y saca solo a los que vencen. Antes se
     * desencolaba y se volvía a encolar a todos: si la interfaz creaba un proceso en medio de esa
     * vuelta, el nuevo quedaba delante de otros que habían llegado antes.
     */
    private void vencerEnCola(Cola c, int ciclo) {
        for (Object o : c.aArreglo()) {
            PCB p = (PCB) o;
            p.setDeadline(p.getDeadline() - 1);
            if (p.getDeadline() <= 0) {
                c.eliminar(p);
                terminar(p, ciclo, "terminó por DEADLINE en cola " + p.getEstado() + ".");
            }
        }
    }

    // ---------- Paso 2: contar cuánto lleva cada bloqueado ----------
    /**
     * [AJUSTE 25-nov] Todos los bloqueados suman ciclos (antes los de E/S no sumaban y el
     * criterio "el que lleva más tiempo bloqueado" no distinguía a nadie). Los bloqueados por
     * E/S no se despiertan aquí: los despierta el disco al terminar su solicitud.
     */
    private void contarCiclosBloqueado() {
        for (Object o : colaBloqueados.aArreglo()) {
            PCB p = (PCB) o;
            p.setCiclosBloqueado(p.getCiclosBloqueado() + 1);
        }
    }

    // ---------- Paso 3: admisión (largo plazo) + mediano plazo ----------
    /**
     * Admite en orden de llegada todos los que quepan. Si el primero no cabe, se detiene
     * (así un proceso grande no muere de inanición detrás de los pequeños) y le pide al
     * planificador a mediano plazo que haga espacio.
     */
    private void admitirSiHayMemoria(int ciclo) {
        while (true) {
            PCB p = (PCB) colaNuevos.verFrente();
            if (p == null) return;

            if (ram.hayEspacio(p.getMemoria())) {
                colaNuevos.desencolar();
                ram.asignar(p.getMemoria());
                p.setEnRam(true);
                p.setEstado(EstadoProceso.LISTO);
                colaListos.encolar(p);
                if (p == ultimoSinVictima) ultimoSinVictima = null;
            } else {
                hacerEspacio(p, ciclo);
                return;
            }
        }
    }

    /**
     * Planificador a mediano plazo: suspende bloqueados hasta que quepa el proceso nuevo.
     * Criterio: el que lleva más ciclos bloqueado (empate: el de menor prioridad), porque es
     * el que menos probabilidad tiene de usar la CPU pronto.
     */
    private void hacerEspacio(PCB nuevoProceso, int ciclo) {
        int faltante = nuevoProceso.getMemoria() - ram.getLibre() - memoriaPorLiberar;
        if (faltante <= 0) return; // ya hay suspensiones en camino que alcanzan

        // [AJUSTE 25-nov] 1. Primero se PLANIFICA: se escogen víctimas por el criterio hasta cubrir
        // lo que falta. Si ni suspendiendo a todas alcanza, no se suspende a nadie (sería escribir
        // en el swap y leer de vuelta sin admitir a nadie).
        Object[] bloqueados = colaBloqueados.aArreglo();
        boolean[] elegido = new boolean[bloqueados.length];
        int swapLibre = mapa().libresSwap();
        int cubierto = 0;
        while (cubierto < faltante) {
            int mejor = -1;
            for (int i = 0; i < bloqueados.length; i++) {
                PCB c = (PCB) bloqueados[i];
                if (elegido[i] || !sirveDeVictima(c, swapLibre)) continue;
                if (mejor < 0 || esMejorVictima(c, (PCB) bloqueados[mejor])) mejor = i;
            }
            if (mejor < 0) break;
            elegido[mejor] = true;
            cubierto += ((PCB) bloqueados[mejor]).getMemoria();
            swapLibre -= ((PCB) bloqueados[mejor]).getMemoria();
        }
        if (cubierto < faltante) {
            if (ultimoSinVictima != nuevoProceso) {
                ultimoSinVictima = nuevoProceso;
                log(nuevoProceso.getNombre() + " no cabe en RAM (pide " + nuevoProceso.getMemoria()
                        + ", libre " + ram.getLibre() + ") y suspendiendo a los bloqueados que caben en el swap ("
                        + mapa().libresSwap() + " bloques libres) no alcanza: espera en la cola a largo plazo.");
            }
            return;
        }

        // 2. Se suspende a las víctimas escogidas
        for (int i = 0; i < bloqueados.length; i++) {
            if (!elegido[i]) continue;
            PCB victima = (PCB) bloqueados[i];
            int primer;
            try {
                primer = mapa().asignarSwap(victima.getMemoria(), "SWAP P" + victima.getId() + "-" + victima.getNombre());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            if (primer < 0) return;

            victima.setPrimerBloqueSwap(primer);
            victima.setSuspensionPendiente(true);
            memoriaPorLiberar += victima.getMemoria();

            // Suspender es una operación de E/S: va a la cola del disco y escribe un bloque por unidad de memoria
            cluster.getDisco().encolarSolicitud(
                    new SolicitudES(victima, TipoOperacionES.SUSPENDER, primer, victima.getMemoria(), ciclo));
            log("Se suspende al proceso " + victima.getNombre() + " para admitir a " + nuevoProceso.getNombre()
                    + " (lleva " + victima.getCiclosBloqueado() + " ciclos bloqueado; se escriben "
                    + victima.getMemoria() + " bloques en el swap desde el bloque " + primer + ").");
        }
    }

    /** Criterio: más ciclos bloqueado; si empatan, menor prioridad. */
    private static boolean esMejorVictima(PCB a, PCB b) {
        if (a.getCiclosBloqueado() != b.getCiclosBloqueado())
            return a.getCiclosBloqueado() > b.getCiclosBloqueado();
        return a.getPrioridad() < b.getPrioridad();
    }

    private boolean sirveDeVictima(PCB p, int swapLibre) {
        return p.isEnRam() && !p.isSuspensionPendiente() && p.getMemoria() <= swapLibre;
    }

    /**
     * [AJUSTE 25-nov] Se revisa en cada ciclo. Solo vuelven los Listo/Suspendido (los
     * Bloqueado/Suspendido esperan su evento). Si hay un proceso nuevo esperando una
     * suspensión en curso, se le guarda su memoria para no quitársela.
     */
    private void reactivar(int ciclo) {
        PCB frente = (PCB) colaNuevos.verFrente();
        int reservado = (frente != null && memoriaPorLiberar > 0) ? frente.getMemoria() : 0;

        for (Object o : colaListosSusp.aArreglo()) {
            PCB p = (PCB) o;
            if (!p.isReactivacionPendiente() && ram.getLibre() - reservado >= p.getMemoria()) {
                ram.asignar(p.getMemoria());   // se reserva ya: el disco va a copiar el proceso aquí
                p.setEnRam(true);
                p.setReactivacionPendiente(true);
                cluster.getDisco().encolarSolicitud(
                        new SolicitudES(p, TipoOperacionES.REACTIVAR, p.getPrimerBloqueSwap(), p.getMemoria(), ciclo));
                log("Se reactiva " + p.getNombre() + ": hay " + (ram.getLibre() + p.getMemoria())
                        + " de RAM libre; se leen " + p.getMemoria() + " bloques del swap.");
            }
            // sigue visible en Listo/Susp hasta que el disco termine de leerlo
        }
    }

    public void despertar(PCB p) {
        p.setEstado(EstadoProceso.LISTO);
        p.setMotivoBloqueo(null);
        colaBloqueados.eliminar(p);
        colaListos.encolar(p);
        if (cluster != null) {
            cluster.registrarEvento("Ciclo " + cluster.getReloj().getCiclo() +
                ": Proceso " + p.getNombre() + " DESPERTÓ y pasó a Listos.");
        }
    }

    /**
     * Llamado por el Disco al terminar una SolicitudES.
     * Distingue si el proceso estaba o no suspendido.
     */
    public void alTerminarES(SolicitudES s) {
        PCB p = s.getPcb();
        if (p == null) return;

        // [AJUSTE 25-nov] El proceso murió por deadline mientras esperaba al disco: no revive
        if (p.getEstado() == EstadoProceso.TERMINADO) {
            log("El disco terminó " + s.getOperacion() + " de " + p.getNombre()
                    + ", pero el proceso ya había terminado. Se descarta.");
            return;
        }

        if (s.getOperacion() == TipoOperacionES.SUSPENDER) {
            p.setSuspensionPendiente(false);
            memoriaPorLiberar -= p.getMemoria();
            ram.liberar(p.getMemoria());
            p.setEnRam(false);
            colaBloqueados.eliminar(p);
            suspensiones++;
            if (p.isEventoOcurrido()) {
                p.setEventoOcurrido(false);
                p.setEstado(EstadoProceso.LISTO_SUSPENDIDO);
                p.setMotivoBloqueo(null);
                colaListosSusp.encolar(p);
            } else {
                p.setEstado(EstadoProceso.BLOQUEADO_SUSPENDIDO);
                colaBloqueadosSusp.encolar(p);
            }
            log(p.getNombre() + " quedó suspendido en el swap (bloques "
                    + mapa().cadenaDesde(p.getPrimerBloqueSwap()) + "): libera " + p.getMemoria()
                    + " de RAM y pasa a " + p.getEstado() + ".");
            return;
        }

        if (s.getOperacion() == TipoOperacionES.REACTIVAR) {
            try {
                mapa().liberarCadena(p.getPrimerBloqueSwap());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            p.setPrimerBloqueSwap(-1);
            p.setReactivacionPendiente(false);
            colaListosSusp.eliminar(p);
            p.setEstado(EstadoProceso.LISTO);
            colaListos.encolar(p);
            reactivaciones++;
            log(p.getNombre() + " volvió a la RAM (reactivado) y pasa a LISTO.");
            return;
        }

        // LEER / CREAR / ACTUALIZAR / ELIMINAR: el disco ya hizo su trabajo, se aplica al árbol
        if (cluster.getSistemaArchivos() != null) {
            String error = cluster.getSistemaArchivos().aplicarCRUD(
                    s.getOperacion(), p.getRutaArchivo(), p.getDueno(), esAdmin(p.getDueno()),
                    p.getBloques(), p.isEsDirectorio(), p.isPublico(), p.getNuevoNombre(),
                    "P" + p.getId() + "-" + p.getNombre());
            if (error != null) {
                log("Error de FS (" + p.getNombre() + "): " + error);
            } else {
                log(p.getNombre() + " ejecutó " + s.getOperacion() + " en " + p.getRutaArchivo());
            }
        }

        // Ocurrió el evento que esperaba el proceso
        if (p.getRestante() <= 0) {
            // Ya no le quedan instrucciones: termina aquí, esté en RAM o suspendido
            // (no tiene sentido traerlo del swap solo para terminar)
            colaBloqueados.eliminar(p);
            colaBloqueadosSusp.eliminar(p);
            terminar(p, ciclo(), "TERMINÓ al completar su E/S.");
        } else if (p.getEstado() == EstadoProceso.BLOQUEADO_SUSPENDIDO) {
            // Bloqueado/Suspendido cuyo evento ocurre -> Listo/Suspendido (siete estados)
            colaBloqueadosSusp.eliminar(p);
            p.setEstado(EstadoProceso.LISTO_SUSPENDIDO);
            p.setMotivoBloqueo(null);
            colaListosSusp.encolar(p);
            log(p.getNombre() + " terminó su E/S estando suspendido. Pasa a Listo/Susp.");
        } else if (p.isSuspensionPendiente()) {
            // El disco todavía lo está escribiendo en el swap: quedará Listo/Suspendido al terminar
            p.setEventoOcurrido(true);
        } else {
            despertar(p);
        }
    }

    // ---------- Paso 4: planificación CPU ----------
    private void planificar(int ciclo) {
        if (!cpu.estaLibre()) {
            PCB enCpu = cpu.getProcesoActual();
            boolean desalojar = false;

            if (politica.getQuantum() > 0 && ciclosEnCpu >= politica.getQuantum()) {
                desalojar = true;
            } else if (politica.esApropiativa() && !colaListos.estaVacia()) {
                PCB candidato = politica.seleccionar(colaListos);
                if (candidato != null && politica.esMejor(candidato, enCpu)) {
                    desalojar = true;
                }
            }

            if (desalojar) {
                cpu.liberar();
                enCpu.setEstado(EstadoProceso.LISTO);
                colaListos.encolar(enCpu);
                ciclosEnCpu = 0;
            }
        }

        if (cpu.estaLibre() && !colaListos.estaVacia()) {
            PCB elegido = politica.seleccionar(colaListos);
            if (elegido != null) {
                colaListos.eliminar(elegido);
                if (elegido.getCicloInicio() < 0) {
                    elegido.setCicloInicio(ciclo);
                }
                cpu.cargar(elegido);
                ciclosEnCpu = 0;
            }
        }
    }

    // ---------- Paso 5: ejecución ----------
    private void ejecutarInstruccion(int ciclo) {
        PCB p = cpu.getProcesoActual();
        if (p == null) return;

        if (p.getTipo() == TipoProceso.IO_BOUND) {
            ejecutarIOBound(p, ciclo);
        } else {
            p.ejecutarInstruccion();
        }
    }

    /**
     * La primera vez que el proceso de E/S tiene la CPU genera su solicitud al disco
     * (cuenta como una instrucción en modo sistema), queda Bloqueado por E/S y libera la CPU.
     * Las instrucciones que le queden después son instrucciones normales.
     */
    private void ejecutarIOBound(PCB p, int ciclo) {
        if (p.getOperacion() == null || p.isIoSolicitada()) {
            p.ejecutarInstruccion();
            return;
        }

        p.setIoSolicitada(true);
        p.consumirInstruccionSistema();
        cpu.registrarOperacionSistema();
        cpu.entrarModoSistema();
        try {
            int bloque = cluster.getSistemaArchivos().bloqueDestino(
                    p.getOperacion(), p.getRutaArchivo(), p.isEsDirectorio(), cluster.getDisco().getCabezal());
            int cantidad = cluster.getSistemaArchivos().bloquesATransferir(
                    p.getOperacion(), p.getRutaArchivo(), p.getBloques());

            cluster.getDisco().encolarSolicitud(new SolicitudES(p, p.getOperacion(), bloque, cantidad, ciclo));

            cluster.registrarEvento("Ciclo " + ciclo + ": " + p.getNombre()
                    + " solicitó " + p.getOperacion()
                    + " sobre " + p.getRutaArchivo() + " (bloque " + bloque + ")");
        } finally {
            cpu.salirModoSistema();
        }

        p.setEstado(EstadoProceso.BLOQUEADO);
        p.setMotivoBloqueo(MotivoBloqueo.E_S);
        p.setCiclosBloqueado(0);
        cpu.liberar();
        colaBloqueados.encolar(p);
    }

    // ---------- Paso 6: cierre ----------
    private void cerrarCiclo(int ciclo) {
        PCB p = cpu.getProcesoActual();
        if (p == null) return;

        if (p.estaTerminado()) {
            cpu.liberar();
            terminar(p, ciclo, "TERMINÓ sus instrucciones.");
        }
    }
}