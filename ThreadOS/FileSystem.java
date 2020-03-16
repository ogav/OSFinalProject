public class FileSystem {
    private SuperBlock superBlock;
    private Directory directory;
    private FileTable fileTable;

    public FileSystem(int diskBlocks) {
        superBlock = new SuperBlock(diskBlocks);
        directory = new Directory(superBlock.totalInodes);
        fileTable = new FileTable(directory);

        // read the '/' from disk
        FileTableEntry dirEnt = open("/", "r");
        int dirSize = fsize(dirEnt);
        if (dirSize > 0) {
            // directory has some data
            byte[] dirData = new byte[dirSize];
            read(dirEnt, dirData);
            directory.bytes2directory(dirData);
        }
        close(dirEnt);
    }

    void read(FileTableEntry entry, byte[] data) {

    }

    FileTableEntry open(String name, String mode) {
        return null;
    }

    int fsize(FileTableEntry entry) {
        return 0;
    }

    void close(FileTableEntry entry) {

    }
}