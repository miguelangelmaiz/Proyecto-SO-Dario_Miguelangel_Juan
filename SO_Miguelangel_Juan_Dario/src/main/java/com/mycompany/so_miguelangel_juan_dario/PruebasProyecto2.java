package com.mycompany.so_miguelangel_juan_dario;

/*
 * [AJUSTE 25-nov] Pruebas automáticas del Proyecto 2, sin interfaz.
 * Se corre como cualquier main (clic derecho > Run File en NetBeans). Usa los JSON de la
 * carpeta json-prueba/ del proyecto y revisa:
 *  - los casos P1–P4 del enunciado (orden de atención de FIFO, SSTF, SCAN y C-SCAN);
 *  - que el CRUD del JSON se aplique (UPDATE renombra, DELETE libera los bloques);
 *  - guardar y volver a cargar el sistema de archivos;
 *  - suspensión y reactivación con swap real;
 *  - que un proceso que muere por deadline no "reviva" cuando el disco termina;
 *  - permisos del modo usuario y CRUD completo (directorios, renombrar, borrado recursivo).
 * Al final imprime TODO OK o cuántas fallas hubo.
 */
import java.io.*;
import sistemaOperativo.*;
import losEnums.*;
import planificacionSO.*;
public class PruebasProyecto2 {
  static int fallas = 0;
  static void check(boolean c, String m) { System.out.println((c ? "  OK   " : "  FALLA ") + m); if (!c) fallas++; }
  static Cluster nuevo(String fs, String procs, TipoPoliticaDisco pol) throws Exception {
    Cluster c = new Cluster(1, 8192, 1);
    String a1 = CargadorJSON.cargarSistemaArchivos(c, new FileReader(fs));
    if (!a1.isEmpty()) System.out.println("  avisos FS: " + a1.trim());
    if (procs != null) System.out.println("  " + CargadorJSON.cargarProcesos(c, new FileReader(procs)).trim().replace("\n", " | "));
    c.getReloj().setDuracionCicloMs(1);
    if (pol != null) c.getDisco().setPolitica(FabricaPoliticasDisco.crear(pol));
    return c;
  }
  static void correr(Cluster c, int esperados) throws Exception {
    c.iniciar(); long t0 = System.currentTimeMillis();
    Nucleo n = c.getComputador(0).getNucleo();
    while (n.getTerminados().getTamano() < esperados && System.currentTimeMillis() - t0 < 20000) Thread.sleep(20);
    Thread.sleep(50); c.detener(); Thread.sleep(30);
  }
  public static void main(String[] a) throws Exception {
    String base = a.length > 0 ? a[0] : "json-prueba/";
    String fs = base + "fs_enunciado.json", pr = base + "procesos_enunciado.json", sw = base + "procesos_suspension.json";
    String[][] casos = {{"FIFO","95, 180, 34, 119, 11, 123, 62, 64"},{"SSTF","62, 64, 34, 11, 95, 119, 123, 180"},{"SCAN","62, 64, 95, 119, 123, 180, 34, 11"},{"C_SCAN","62, 64, 95, 119, 123, 180, 11, 34"}};
    for (String[] k : casos) {
      System.out.println("== Caso " + k[0] + " con los JSON del enunciado");
      Cluster c = nuevo(fs, pr, TipoPoliticaDisco.valueOf(k[0]));
      correr(c, 8);
      Nucleo n = c.getComputador(0).getNucleo();
      check(c.getDisco().ordenAtencion.toString().equals(k[1]), "orden " + c.getDisco().ordenAtencion + " (esperado " + k[1] + ")");
      check(n.getTerminados().getTamano() == 8 && n.getRam().getUsada() == 0, "8 terminados y RAM en 0 (usada=" + n.getRam().getUsada() + ")");
      if (k[0].equals("FIFO")) {
        String arbol = c.getSistemaArchivos().getSnapshotArbol();
        check(arbol.contains("notas.txt_mod") && arbol.contains("data.csv_mod") && !arbol.contains("img1.png"), "UPDATE renombró y DELETE borró img1.png");
        check(!c.getSistemaArchivos().getMapaBloques().estaOcupado(119), "bloque 119 liberado al borrar img1.png");
        check(c.getSistemaArchivos().getMapaBloques().totalSwap() == 100, "swap de 100 bloques reservado (globalConfig leído)");
        check(n.getRam().getTotal() == 1024, "RAM = ramCapacity del JSON (1024)");
        check(c.getDisco().getAtendidasPor(TipoPoliticaDisco.FIFO) == 8, "métrica: 8 solicitudes atendidas por FIFO");
        System.out.println("  espera promedio en cola: " + String.format("%.1f", c.getDisco().getEsperaPromedio()) + " ciclos; desplazamiento " + c.getDisco().getDesplazamientoTotal());
        // guardar y volver a cargar
        StringWriter w = new StringWriter(); CargadorJSON.guardarSistemaArchivos(c, w);
        Cluster c2 = new Cluster(1, 8192, 1);
        CargadorJSON.cargarSistemaArchivos(c2, new StringReader(w.toString()));
        check(c2.getSistemaArchivos().getSnapshotArbol().equals(arbol), "guardar + cargar el FS da el mismo árbol (sin carpeta raiz extra)");
        StringWriter w2 = new StringWriter(); CargadorJSON.guardarProcesos(c, w2, "FCFS", 0);
        check(w2.toString().contains("\"globalConfig\""), "guardarProcesos escribe globalConfig");
      }
    }

    System.out.println("== Suspensión y reactivación (RAM 100, swap 60)");
    {
      Cluster c = nuevo(fs, sw, null);
      Nucleo n = c.getComputador(0).getNucleo();
      int total = n.getColaNuevos().getTamano();
      c.iniciar(); long t0 = System.currentTimeMillis(); int maxSusp = 0;
      while (n.getTerminados().getTamano() < total && System.currentTimeMillis() - t0 < 20000) {
        maxSusp = Math.max(maxSusp, n.getColaBloqueadosSusp().getTamano() + n.getColaListosSusp().getTamano());
        Thread.sleep(2);
      }
      Thread.sleep(50); c.detener(); Thread.sleep(30);
      check(n.getTerminados().getTamano() == total, "terminaron los " + total + " procesos");
      check(n.getSuspensiones() > 0 && n.getReactivaciones() > 0, "suspensiones=" + n.getSuspensiones() + " reactivaciones=" + n.getReactivaciones() + " (se vieron hasta " + maxSusp + " suspendidos a la vez)");
      check(n.getRam().getUsada() == 0, "RAM en 0 al final");
      check(c.getSistemaArchivos().getMapaBloques().libresSwap() == c.getSistemaArchivos().getMapaBloques().totalSwap(), "swap vacío al final");
      String log = c.getLogEventos();
      check(log.contains("Se suspende al proceso") && log.contains("El disco atiende la solicitud"), "el log trae las decisiones del disco y del mediano plazo");
      for (String l : log.split("\n")) if (l.contains("suspend") || l.contains("reactiv") || l.contains("volvió")) System.out.println("     " + l);
    }

    System.out.println("== Deadline vencido mientras espera al disco");
    {
      Cluster c = nuevo(fs, null, TipoPoliticaDisco.FIFO);
      Nucleo n = c.getComputador(0).getNucleo();
      n.crearProcesoES("lejos", TipoProceso.IO_BOUND, 2, 10, 400, 2, TipoOperacionES.LEER, "/usuarios/video.mp4", 1, "admin", false, true, null);
      n.crearProcesoES("apurado", TipoProceso.IO_BOUND, 2, 10, 20, 2, TipoOperacionES.LEER, "/sistema/boot.bin", 1, "admin", false, true, null);
      correr(c, 2);
      check(n.getTerminados().getTamano() == 2 && n.getRam().getUsada() == 0, "los 2 terminan y la RAM queda en 0");
      check(c.getLogEventos().contains("ya había terminado"), "la solicitud del proceso muerto se descarta (no revive)");
    }

    System.out.println("== CRUD y permisos");
    {
      Cluster c = nuevo(fs, null, TipoPoliticaDisco.SSTF);
      Nucleo n = c.getComputador(0).getNucleo();
      sistemaArchivo.SistemaArchivos f = c.getSistemaArchivos();
      check(f.validar(TipoOperacionES.LEER, "/usuarios/notas.txt", "user2", false, false, 0, null) != null, "user2 no lee notas.txt (privado de user1): " + f.validar(TipoOperacionES.LEER, "/usuarios/notas.txt", "user2", false, false, 0, null));
      check(f.validar(TipoOperacionES.LEER, "/usuarios/notas.txt", "user1", false, false, 0, null) == null, "user1 sí lee su notas.txt");
      check(f.validar(TipoOperacionES.CREAR, "/sistema/x.txt", "user1", false, false, 2, null) != null, "user1 no crea en /sistema");
      check(f.validar(TipoOperacionES.ACTUALIZAR, "/usuarios/notas.txt", "user1", false, false, 0, "a.txt") != null, "user1 no renombra");
      check(f.validar(TipoOperacionES.CREAR, "/usuarios/grande.bin", "admin", true, false, 500, null) != null, "no se crea un archivo sin bloques libres suficientes");
      check(!n.crearProcesoES("malo", TipoProceso.IO_BOUND, 2, 10, 100, 2, TipoOperacionES.LEER, "/no/existe", 1, "admin", false, true, null).isOk(), "la interfaz recibe el rechazo de una ruta inexistente");
      n.crearProcesoES("mkdir", TipoProceso.IO_BOUND, 2, 10, 900, 1, TipoOperacionES.CREAR, "/proyectos", 0, "admin", true, true, null);
      correr(c, 1);
      n.crearProcesoES("crea", TipoProceso.IO_BOUND, 2, 10, 900, 1, TipoOperacionES.CREAR, "/proyectos/tesis.doc", 3, "admin", false, false, null);
      n.crearProcesoES("crea2", TipoProceso.IO_BOUND, 2, 10, 900, 1, TipoOperacionES.CREAR, "/usuarios/mio.txt", 2, "user1", false, true, null);
      correr(c, 3);
      String cad = f.getMapaBloques().cadenaDesde(f.primerBloqueReal("/proyectos/tesis.doc"));
      check(cad.split("→").length == 3, "tesis.doc quedó encadenado en 3 bloques: " + cad);
      check(f.primerBloqueReal("/usuarios/mio.txt") >= 0, "user1 creó /usuarios/mio.txt");
      n.crearProcesoES("ren", TipoProceso.IO_BOUND, 2, 10, 900, 1, TipoOperacionES.ACTUALIZAR, "/proyectos", 0, "admin", false, true, "trabajos");
      correr(c, 4);
      check(f.primerBloqueReal("/trabajos/tesis.doc") >= 0, "renombrar el directorio mueve la ruta de sus archivos");
      int libresAntes = f.getMapaBloques().libres();
      n.crearProcesoES("rm", TipoProceso.IO_BOUND, 2, 10, 900, 1, TipoOperacionES.ELIMINAR, "/trabajos", 0, "admin", false, true, null);
      correr(c, 5);
      check(f.getMapaBloques().libres() == libresAntes + 3 && !f.getSnapshotArbol().contains("trabajos"), "borrar el directorio libera sus 3 bloques");
    }
    System.out.println("== JSON de ejemplo del CRUD (json-prueba/procesos_crud.json)");
    {
      Cluster c = nuevo(fs, base + "procesos_crud.json", null);
      correr(c, 6);
      String arbol = c.getSistemaArchivos().getSnapshotArbol();
      check(arbol.contains("tesis.doc") && arbol.contains("apuntes.txt") && !arbol.contains("sistema"), "crea /proyectos/tesis.doc, renombra notas.txt y borra /sistema completo");
      check(c.getLogEventos().contains("es privado de user1"), "user2 no puede leer img1.png (queda en el log)");
    }
    System.out.println(fallas == 0 ? "\nTODO OK" : "\nFALLAS: " + fallas);
    System.exit(fallas == 0 ? 0 : 1);
  }
}
