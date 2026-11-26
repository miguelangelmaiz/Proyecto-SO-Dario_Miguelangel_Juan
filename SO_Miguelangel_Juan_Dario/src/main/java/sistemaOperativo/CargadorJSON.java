/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemaOperativo;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import java.io.Reader;
import java.io.Writer;
import losEnums.TipoOperacionES;
import losEnums.TipoPoliticaDisco;
import losEnums.TipoProceso;
import planificacionSO.FabricaPoliticas;
import planificacionSO.FabricaPoliticasDisco;
import sistemaArchivo.FileSystemConfig;
/**
 *
 * @author Miguel
 */
public class CargadorJSON {
     private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /** Devuelve los avisos de la carga ("" si todo salió bien). Lanza IllegalArgumentException si el JSON no sirve. */
    public static String cargarSistemaArchivos(Cluster cluster, Reader lector) {
        FileSystemConfig cfg;
        try {
            cfg = GSON.fromJson(lector, FileSystemConfig.class);
        } catch (JsonParseException e) {
            throw new IllegalArgumentException("El archivo no es un JSON válido: " + e.getMessage());
        }
        if (cfg == null || cfg.fileSystem == null) {
            throw new IllegalArgumentException("No es el JSON del sistema de archivos: falta \"fileSystem\".");
        }
        int total = 200, cabezal = 50;
        if (cfg.diskConfig != null) {
            if (cfg.diskConfig.totalBlocks > 0) total = cfg.diskConfig.totalBlocks;
            cabezal = cfg.diskConfig.initialHead;
        }
        if (total < 10 || total > 2000) throw new IllegalArgumentException("totalBlocks fuera de rango (10–2000): " + total);
        if (cabezal < 0 || cabezal >= total) throw new IllegalArgumentException("initialHead fuera del disco: " + cabezal);

        cluster.getDisco().setTotalBloques(total);
        cluster.getDisco().setCabezal(cabezal);
        cluster.getDisco().reiniciarArranque();
        String avisos = cluster.getSistemaArchivos().inicializarDesdeJSON(cfg, cluster.getSwapSizeBlocks());
        cluster.registrarEvento("Se cargó el sistema de archivos: " + total + " bloques, cabezal en "
                + cabezal + ", swap de " + cluster.getSistemaArchivos().getMapaBloques().totalSwap() + " bloques.");
        return avisos;
    }

    /** Devuelve un resumen ("8 procesos cargados..."). Lanza IllegalArgumentException si el JSON no sirve. */
    public static String cargarProcesos(Cluster cluster, Reader lector) {
        configuracion.GlobalConfig cfg;
        try {
            cfg = GSON.fromJson(lector, configuracion.GlobalConfig.class);
        } catch (JsonParseException e) {
            throw new IllegalArgumentException("El archivo no es un JSON válido: " + e.getMessage());
        }
        if (cfg == null || cfg.processes == null) {
            throw new IllegalArgumentException("No es el JSON de procesos: falta \"processes\".");
        }
        StringBuilder resumen = new StringBuilder();
        Nucleo nucleo = cluster.getComputador(0).getNucleo();

        // globalConfig
        if (cfg.getCycleDurationMs() > 0) cluster.getReloj().setDuracionCicloMs(cfg.getCycleDurationMs());
        if (cfg.getSwapSizeBlocks() > 0) {
            cluster.setSwapSizeBlocks(cfg.getSwapSizeBlocks());
            try {
                int r = cluster.getSistemaArchivos().getMapaBloques().redefinirSwap(cfg.getSwapSizeBlocks());
                if (r < cfg.getSwapSizeBlocks()) resumen.append("Solo cupieron ").append(r).append(" bloques de swap.\n");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        TipoPoliticaDisco pd = TipoPoliticaDisco.desdeTexto(cfg.getInitialDiskPolicy());
        cluster.getDisco().setPolitica(FabricaPoliticasDisco.crear(pd));

        // computer
        if (cfg.computer != null) {
            if (cfg.computer.ramCapacity > 0 && !nucleo.getRam().setTotal(cfg.computer.ramCapacity)) {
                resumen.append("No se pudo cambiar la RAM a ").append(cfg.computer.ramCapacity)
                       .append(" (hay procesos usando más).\n");
            }
            if (cfg.computer.policy != null) {
                cluster.getComputador(0).setPolitica(FabricaPoliticas.crear(cfg.computer.policy, cfg.computer.quantum));
            }
        }

        // processes
        int cargados = 0;
        for (configuracion.ProcessDTO d : cfg.processes) {
            if (d == null) continue;
            String nombre = d.name == null ? "" : d.name;
            boolean esES = d.targetPath != null && !d.targetPath.trim().isEmpty();
            TipoOperacionES op = esES ? TipoOperacionES.desdeTexto(d.operation) : null;
            if (esES && op == null) {
                resumen.append("Se omite ").append(nombre).append(": operación desconocida \"").append(d.operation).append("\"\n");
                continue;
            }
            // Si no lo dice el JSON: CREATE con fileSizeBlocks = 0 crea un directorio
            boolean esDir = d.isDirectory != null ? d.isDirectory : (op == TipoOperacionES.CREAR && d.fileSizeBlocks <= 0);
            Resultado r = nucleo.crearProcesoES(nombre,
                    esES ? TipoProceso.IO_BOUND : TipoProceso.CPU_BOUND,
                    PCB.PRIORIDAD_MEDIA, d.memory, d.deadline, d.instructions,
                    op, esES ? d.targetPath : null, d.fileSizeBlocks,
                    d.owner == null ? "admin" : d.owner,
                    esDir, d.isPublic == null || d.isPublic, d.newName, false);
            if (r.isOk()) cargados++;
            else resumen.append("Se omite ").append(nombre).append(": ").append(r.getMotivo()).append('\n');
        }
        resumen.insert(0, cargados + " procesos cargados. Disco: " + pd + ", swap " + cluster.getSwapSizeBlocks()
                + " bloques, RAM " + nucleo.getRam().getTotal() + ".\n");
        cluster.registrarEvento("Se cargó el JSON de procesos: " + cargados + " procesos, política de disco " + pd + ".");
        return resumen.toString();
    }

    public static void guardarSistemaArchivos(Cluster cluster, Writer escritor) {
        FileSystemConfig cfg = cluster.getSistemaArchivos().exportarAJSON(
                cluster.getDisco().getTotalBloques(), cluster.getDisco().getCabezal());
        GSON.toJson(cfg, escritor);
    }

    /** Guarda la configuración actual y los procesos que todavía no terminaron (con lo que les queda). */
    public static void guardarProcesos(Cluster cluster, Writer escritor, String politicaCpu, int quantum) {
        Nucleo n = cluster.getComputador(0).getNucleo();
        configuracion.GlobalConfig cfg = new configuracion.GlobalConfig();
        cfg.globalConfig = new configuracion.GlobalSection();
        cfg.globalConfig.cycleDurationMs = cluster.getReloj().getDuracionCicloMs();
        cfg.globalConfig.swapSizeBlocks = cluster.getSwapSizeBlocks();
        cfg.globalConfig.initialDiskPolicy = cluster.getDisco().getPolitica().getTipo().getNombre();
        cfg.computer = new configuracion.ComputerConfig();
        cfg.computer.ramCapacity = n.getRam().getTotal();
        cfg.computer.policy = politicaCpu;
        cfg.computer.quantum = quantum;

        Object[][] colas = {
            n.getColaNuevos().aArreglo(), n.getColaListos().aArreglo(), n.getColaBloqueados().aArreglo(),
            n.getColaListosSusp().aArreglo(), n.getColaBloqueadosSusp().aArreglo(),
            n.getCpu().getProcesoActual() == null ? new Object[0] : new Object[]{n.getCpu().getProcesoActual()}
        };
        int total = 0;
        for (Object[] c : colas) total += c.length;
        cfg.processes = new configuracion.ProcessDTO[total];
        int k = 0;
        for (Object[] c : colas) {
            for (Object o : c) {
                PCB p = (PCB) o;
                configuracion.ProcessDTO d = new configuracion.ProcessDTO();
                d.name = p.getNombre();
                d.memory = p.getMemoria();
                d.deadline = p.getDeadline();
                d.instructions = Math.max(1, p.getRestante());
                // Si ya hizo su E/S no se vuelve a pedir al recargar (repetiría un CREATE o un DELETE)
                boolean conES = p.getOperacion() != null && !p.isIoSolicitada();
                d.targetPath = conES ? p.getRutaArchivo() : "";
                d.operation = conES ? p.getOperacion().enIngles() : "";
                d.isPublic = p.isPublico() ? null : Boolean.FALSE;
                d.fileSizeBlocks = p.getBloques();
                d.owner = p.getDueno();
                d.newName = p.getNuevoNombre();
                d.isDirectory = p.isEsDirectorio() ? Boolean.TRUE : null;
                cfg.processes[k++] = d;
            }
        }
        GSON.toJson(cfg, escritor);
    }
}
