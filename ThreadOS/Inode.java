
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
    int toDisk(short iNumber) {
        if (iNumber > 0) {
            byte[] data = new byte[Disk.blockSize];
            int blockNumber = (iNumber / 16) + 1;

            SysLib.rawread(blockNumber, data);
            int offset = (iNumber % 16) * 32;

            SysLib.bytes2int(length, data, offset);
            SysLib.bytes2short(count, data, offset + 4);
            SysLib.bytes2short(flag, data, offset + 6);
            SysLib.bytes2short(indirect, data, offset + 30);

            for (int i = 0; i < directSize; i++) {
                int value = offset + 8 + (2 * i);
                SysLib.bytes2short(direct[i], data, value);
            }

            SysLib.rawwrite(blockNumber, data);
            return  1;
        }
        return 0;
    }

    // Return found value on success and -1 upon failure
    short getIndexBlockNumber(int seekPointer) {
        int offset = seekPointer / Disk.blockSize;

        if (offset < directSize) {
            return directSize[offset];
        } else if (indirect != -1) {
            byte[] data = new byte[Disk.blockSize];
            
            return
        }
        return -1;
    }

    boolean setIndexBlock(short indexBlockNumber) {

    }

    short findTargetBlock(int offset) {

    }
}