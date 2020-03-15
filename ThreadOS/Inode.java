
public class Inode {
    private final static int iNodeSize = 32;
    private final static int directSize = 11;

    public int length;
    public short count;
    public short flag;
    public short direct[] = new short[directSize];
    public short indirect;

    Inode() {
        length = 0;
        count = 0;
        flag = 1;
        indirect = -1;

        for (int i = 0; i < directSize; i++)
            direct[i] = -1;
    }

    // Taken from the 'CSS430FinalProject.pdf" file provided in Program 5 assignment description
    Inode(short iNumber) {
        byte[] data = new byte[Disk.blockSize];
        int blockNumber = (iNumber / 16) + 1;

        SysLib.rawread(blockNumber, data);
        int offset = (iNumber % 16) * 32;

        length = SysLib.bytes2int(data, offset);
        count = SysLib.bytes2short(data, offset + 4);
        flag = SysLib.bytes2short(data, offset + 6);
        indirect = SysLib.bytes2short(data, offset + 30);

        for (int i = 0; i < directSize; i++) {
            int value = offset + 8 + (2 * i);
            direct[i] = SysLib.bytes2short(data, value);
        }
    }

    // Return 1 on success and 0 upon failure
    void toDisk(short iNumber) {
        //System.out.println("IN TODISK METHOD");
        if (iNumber > 0) {
            byte[] data = new byte[Disk.blockSize];
            int blockNumber = (iNumber / 16) + 1;

            SysLib.rawread(blockNumber, data);
            int offset = (iNumber % 16) * 32;

            SysLib.int2bytes(length, data, offset);
            SysLib.short2bytes(count, data, offset + 4);
            SysLib.short2bytes(flag, data, offset + 6);
            SysLib.short2bytes(indirect, data, offset + 30);

            for (int i = 0; i < directSize; i++) {
                int value = offset + 8 + (2 * i);
                SysLib.short2bytes(direct[i], data, value);
            }

            SysLib.rawwrite(blockNumber, data);
        }
    }

    // Return found value on success and -1 upon failure
    short getIndexBlockNumber(int seekPointer) {
        int offset = seekPointer / Disk.blockSize;

        if (offset < directSize) {
            return direct[offset];
        } else if (indirect != -1) {
            byte[] data = new byte[Disk.blockSize];

            return 1;
        }
        return -1;
    }

    boolean setIndexBlock(short indexBlockNumber) {
        //System.out.println("IN SETINDEXBLOCK METHOD");
        return true;
    }

    int findTargetBlock(int offset) {
        int targetBlock = offset / Disk.blockSize;

        if (indirect < 0)
            return -1;
        else if (targetBlock < 11)
            return direct[offset];
        else {
            int retVal = 0;
            byte[] block = new byte[Disk.blockSize];
            int location = offset - 11;
            location *= 2;

            SysLib.rawread(indirect, block);
            retVal = SysLib.bytes2int(block, location);

            return retVal;
        }
    }

    int registerTargetBlock(int i, short j) {
        return 0;
    }

    byte[] unregisterIndexBlock() {
        if (indirect > 0) {
            indirect = -1;
            byte[] block = new byte[Disk.blockSize];
            SysLib.rawread(indirect, block);
            return block;
        }
        return null;
    }
}