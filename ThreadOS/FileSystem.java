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
        if (entry != null && !entry.mode.equals("w") && !entry.mode.equals("a")) {
            synchronized (entry) {
                int fsize = fsize(entry);
                int read = 0;
                int bufleft = data.length;

                while (bufleft > 0 && entry.seekPtr < fsize) {
                    int id = entry.inode.findTargetBlock(entry.seekPtr);
                    if (id >= 0) {
                        byte[] block = new byte[Disk.blockSize];
                        SysLib.rawread(id, block);

                        int init = entry.seekPtr % Disk.blockSize;

                        int blcktoRead = Disk.blockSize - init;
                        int dataLeft = fsize - entry.seekPtr;
                        int toRead;

                        if (bufleft < blcktoRead && bufleft < dataLeft)
                            toRead = bufleft;
                        else if (dataLeft < blcktoRead && dataLeft < bufleft)
                            toRead = dataLeft;
                        else
                            toRead = blcktoRead;

                        System.arraycopy(block, init, data, read, toRead);

                        entry.seekPtr += toRead;
                        read += toRead;
                        bufleft -= toRead;
                    }
                }
            }
        }
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