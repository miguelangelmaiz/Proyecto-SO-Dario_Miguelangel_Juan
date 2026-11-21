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
                                  String dueno) { // <-- NUEVO PARÁMETRO
        if (cluster == null) return Resultado.error("El sistema no está inicializado.");

        TipoProceso tipo;
        try {
            tipo = TipoProceso.valueOf(tipoTexto);
        } catch (IllegalArgumentException e) {
            return Resultado.error("Tipo de proceso inválido: " + tipoTexto);
        }

        int prioridad;
        try {
            prioridad = PCB.prioridadDesdeTexto(prioridadTexto);
        } catch (IllegalArgumentException e) {
            return Resultado.error(e.getMessage());
        }

        TipoOperacionES operacion;
        try {
            operacion = TipoOperacionES.valueOf(operacionTexto);
        } catch (IllegalArgumentException e) {
            return Resultado.error("Operación inválida: " + operacionTexto);
        }

        // Delegar al Núcleo (ya con el dueño)
        return cluster.getComputador(0).getNucleo().crearProcesoES(
                nombre, tipo, prioridad, memoria, deadline, instrucciones,
                operacion, ruta, 1, dueno); // <-- ENVIAMOS EL DUEÑO
    }
}
