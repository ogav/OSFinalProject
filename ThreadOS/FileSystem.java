public class FileSystem {
    private SuperBlock superBlock;
    private Directory directory;
    private FileTable fileTable;

    public FileSystem(int diskBlocks) {
        superBlock = new SuperBlock(diskBlocks);
        directory = new Directory(superBlock.inodeBlocks);
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

    synchronized int fsize(FileTableEntry entry) {
        if (entry != null) {            // delete this later
            synchronized (entry) {
                return entry.inode.length;
            }
        }
        return 0;
    }

    void close(FileTableEntry entry) {

    }

    boolean format(int numFiles) {
        superBlock.format(numFiles);
        directory = new Directory(superBlock.inodeBlocks);
        fileTable = new FileTable(directory);
        return true;
    }

}