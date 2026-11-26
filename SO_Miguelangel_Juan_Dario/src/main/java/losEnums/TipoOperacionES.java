
package losEnums;

/**
 *
 * @author Miguel
 */
public enum TipoOperacionES {
    LEER,
    ESCRIBIR,
    ACTUALIZAR,
    ELIMINAR,
    CREAR,
    SUSPENDER,
    REACTIVAR;

    /**
     * [AJUSTE 25-nov] El JSON del enunciado trae las operaciones en inglés
     * (READ, CREATE, UPDATE, DELETE). Este método las traduce y también acepta
     * los nombres en español. Devuelve null si el texto no es una operación válida,
     * para que quien llama rechace el proceso en vez de convertirlo en LEER en silencio.
     */
    public static TipoOperacionES desdeTexto(String texto) {
        if (texto == null) return null;
        switch (texto.trim().toUpperCase()) {
            case "READ":   case "LEER":       return LEER;
            case "CREATE": case "CREAR":      return CREAR;
            case "UPDATE": case "ACTUALIZAR": return ACTUALIZAR;
            case "DELETE": case "ELIMINAR":   return ELIMINAR;
            default: return null;
        }
    }

    /** Nombre en inglés, para guardar los procesos en el formato del enunciado. */
    public String enIngles() {
        switch (this) {
            case LEER: return "READ";
            case CREAR: return "CREATE";
            case ACTUALIZAR: return "UPDATE";
            case ELIMINAR: return "DELETE";
            default: return name();
        }
    }
}
