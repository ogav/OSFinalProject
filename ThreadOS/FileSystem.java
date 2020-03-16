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

    int read(FileTableEntry entry, byte[] data) {
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
                return read;
            }
        }
        return -1;
    }

    FileTableEntry open(String name, String mode) {
        FileTableEntry e = fileTable.falloc(name, mode);
        boolean i = deallocAllBlocks(e);
        if (mode == "w" && !i) {
            return null;
        }
        return e;
    }

    synchronized int fsize(FileTableEntry entry) {
        if (entry != null) {            // delete this later
            synchronized (entry) {
                return entry.inode.length;
            }
        }
        return 0;
    }

    // update seek pointer
    int seek(FileTableEntry e, int offset, int whence) {
        if (e == null)
            return -1;
        synchronized (e) {
            if (whence == 0) {
                // file's seek pointer is set to offset bytes from the beginning of the file
                e.seekPtr = offset;
            } else if (whence == 1) {
                // the file's seek pointer is set to its current value plus the offset
                e.seekPtr += offset;
            } else if (whence == 2) {
                // the file's seek pointer is set to the size of the file plus the offset
                e.seekPtr = fsize(e) + offset;
            }
            // If the user attempts to set the seek pointer to a negative number you must clamp it to zero.
            if (e.seekPtr > fsize(e))
                e.seekPtr = fsize(e);
            // user attempts to set the pointer to beyond the file size, you must set the seek pointer to end of file
            if (e.seekPtr < 0)
                e.seekPtr = 0;

            return e.seekPtr;
        }
    }

    boolean close(FileTableEntry entry) {
        synchronized (entry) {
            entry.count = entry.count - 1;
            if (entry.count == 0)
                return fileTable.ffree(entry);
            return true;
        }
    }

    boolean format(int numFiles) {
        superBlock.format(numFiles);
        directory = new Directory(superBlock.inodeBlocks);
        fileTable = new FileTable(directory);
        return true;
    }

    boolean delete(String fileName) {
        FileTableEntry e = open(fileName, "w");
        boolean i = directory.ifree(e.iNumber);
        boolean j = close(e);

        return i && j;
    }

    boolean deallocAllBlocks(FileTableEntry e) {
        return true;
    }

    int write(FileTableEntry e, byte[] data) {
        return 0;
    }

}