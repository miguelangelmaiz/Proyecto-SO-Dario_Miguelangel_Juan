
package sistemaOperativo;

/**
 *
 * 
 * @author jmmor
 */

public class configuracion {

   public static class GlobalConfig {
        public GlobalSection globalConfig;   // formato del enunciado
        public ComputerConfig computer;
        public ProcessDTO[] processes;

        // Formato viejo (campos sueltos en la raíz), se mantiene por compatibilidad
        public int cycleDurationMs;
        public int swapSizeBlocks;
        public String initialDiskPolicy;

        public int getCycleDurationMs() {
            return (globalConfig != null && globalConfig.cycleDurationMs > 0) ? globalConfig.cycleDurationMs : cycleDurationMs;
        }
        public int getSwapSizeBlocks() {
            return (globalConfig != null && globalConfig.swapSizeBlocks > 0) ? globalConfig.swapSizeBlocks : swapSizeBlocks;
        }
        public String getInitialDiskPolicy() {
            return (globalConfig != null && globalConfig.initialDiskPolicy != null) ? globalConfig.initialDiskPolicy : initialDiskPolicy;
        }
    }

    public static class GlobalSection {
        public int cycleDurationMs;
        public int swapSizeBlocks;
        public String initialDiskPolicy;
    }

    public static class ComputerConfig {
        public int ramCapacity;
        public String policy;
        public int quantum;
    }

    public static class ProcessDTO {
        public String name;
        public int memory;
        public int deadline;
        public int instructions;
        public String targetPath; // <--- Nombre en inglés
        public String operation;  // <--- Nombre en inglés
        public int fileSizeBlocks;
        // Opcionales (no están en el ejemplo del enunciado)
        public String owner;
        public String newName;
        public Boolean isDirectory;
        public Boolean isPublic;
    }
    
}
    
    

