/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
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
                                  String ruta) {
        if (cluster == null)
            return Resultado.error("El sistema no está inicializado.");

        // 1. Convertir el texto del combo a Enum (Validación)
        TipoProceso tipo;
        try {
            tipo = TipoProceso.valueOf(tipoTexto);
        } catch (IllegalArgumentException e) {
            return Resultado.error("Tipo de proceso inválido: " + tipoTexto);
        }

        // 2. Convertir la prioridad usando el método que crearemos en PCB
        int prioridad;
        try {
            prioridad = PCB.prioridadDesdeTexto(prioridadTexto);
        } catch (IllegalArgumentException e) {
            return Resultado.error(e.getMessage());
        }

        // 3. Convertir la operación de E/S
        TipoOperacionES operacion;
        try {
            operacion = TipoOperacionES.valueOf(operacionTexto);
        } catch (IllegalArgumentException e) {
            return Resultado.error("Operación inválida: " + operacionTexto);
        }

        // 4. Delegar la validación final y creación al Núcleo
        return cluster.getComputador(0).getNucleo().crearProcesoES(
                nombre, tipo, prioridad, memoria, deadline, instrucciones,
                operacion, ruta, 1, "admin");
    }
}
