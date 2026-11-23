package sistemaArchivo;

/**
 *
 * @author dariu
 */


public class FileSystemConfig {

    public static class DiskConfigDTO {
        public int totalBlocks;
        public int initialHead;
    }
    public DiskConfigDTO diskConfig; 
    public FileSystemDTO[] fileSystem; 

    public static class FileSystemDTO {
        public String path;
        public String type; // "DIRECTORY" o "FILE"
        public String owner;
        public String permissions;
        public int startBlock;
        public int blocks;
    }
}
