
package sistemaOperativo;

/**
 *
 * @author jmmor
 */
public class configuracion {

   public static class GlobalConfig {
        public int cycleDurationMs;
        public int swapSizeBlocks;
        public String initialDiskPolicy;
        public ComputerConfig computer;
        public ProcessDTO[] processes;
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
        public String targetPath;
        public String operation;
        public int fileSizeBlocks;
    }
}
    
    

