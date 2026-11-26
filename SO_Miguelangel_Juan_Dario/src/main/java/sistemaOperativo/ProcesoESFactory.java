package sistemaOperativo;

import losEnums.TipoProceso;
import losEnums.TipoOperacionES;
/**
 *
 * @author Miguel
 */
public class ProcesoESFactory {
    public static Resultado crear(Cluster cluster,
                                  String nombre,
                                  String tipoTexto,
                                  String prioridadTexto,
                                  int memoria,
                                  int deadline,
                                  int instrucciones,
                                  String operacionTexto,
                                  String ruta,
                                  String dueno,
                                  int bloques,
                                  boolean esDirectorio,
                                  boolean publico,
                                  String nuevoNombre) {
        if (cluster == null) return Resultado.error("El sistema no está inicializado.");

        TipoProceso tipo;
        try {
            tipo = TipoProceso.valueOf(tipoTexto);
        } catch (IllegalArgumentException | NullPointerException e) {
            return Resultado.error("Tipo de proceso inválido: " + tipoTexto);
        }

        int prioridad;
        try {
            prioridad = PCB.prioridadDesdeTexto(prioridadTexto);
        } catch (IllegalArgumentException e) {
            return Resultado.error(e.getMessage());
        }

        TipoOperacionES operacion = TipoOperacionES.desdeTexto(operacionTexto);
        if (tipo == TipoProceso.IO_BOUND && operacion == null) {
            return Resultado.error("Operación inválida: " + operacionTexto);
        }

        return cluster.getComputador(0).getNucleo().crearProcesoES(
                nombre, tipo, prioridad, memoria, deadline, instrucciones,
                operacion, ruta, bloques, dueno, esDirectorio, publico, nuevoNombre);
    }
}
