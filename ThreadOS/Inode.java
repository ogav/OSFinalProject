public class Inode {
    private final static int iNodeSize = 32;       // fix to 32 bytes
    private final static int directSize = 11;      // # direct pointers
 
    public int length;                             // file size in bytes
    public short count;                            // # file-table entries pointing to this
    public short flag;                             // 0 = unused, 1 = used, ...
    public short direct[] = new short[directSize]; // direct pointers
    public short indirect;                         // a indirect pointer
 
    Inode( ) {                                     // a default constructor
       length = 0;
       count = 0;
       flag = 1;
       for ( int i = 0; i < directSize; i++ )
          direct[i] = -1;
       indirect = -1;
    }

    Inode(short iNumber) {
        byte[] data = new byte[Disk.blockSize];
        int blockNumber = (iNumber / 16) + 1;
        SysLib.rawread(blockNumber, data);
        int offset = (iNumber % 16) * 32;
        length = SysLib.bytes2int(data, offset);
        offset += 4;
        count = SysLib.bytes2short(data, offset);
        offset += 2;
        flag = SysLib.bytes2short(data, offset);
        offset += 2;

        for (int i = 0; i < directSize; i++) {
            direct[i] = SysLib.bytes2short(data, offset);
            offset += 2;
        }
        indirect = SysLib.bytes2short(data, offset);
    }

    void toDisk(short iNumber){
        if (iNumber < 0) {
            return;
        }
        byte[] data = new byte[Disk.blockSize];
        int offset = 0;
        SysLib.int2bytes(length, data, offset);
        offset += 4;
        SysLib.short2bytes(count, data, offset);

        offset += 2;
        SysLib.short2bytes(flag, data, offset);
        offset += 2;

        for (int i = 0; i < directSize; i++) {
            SysLib.short2bytes(direct[i], data, offset);
            offset += 2;
        }
        SysLib.short2bytes(indirect, data, offset);

        int blockNumber = (iNumber / 16) + 1;
        byte[] buffer = new byte[Disk.blockSize];
        SysLib.rawread(blockNumber, buffer);
        offset = (iNumber % 16) * 32;
        System.arraycopy(data, 0, buffer, offset, iNodeSize);
        SysLib.rawwrite(blockNumber, buffer);
    }

    int findTargetBlock(int offset) {
        int index = offset / Disk.blockSize;
        if (index < directSize) {
            return direct[index];
        } else if (indirect == -1) {
            return -1;
        } else {
            byte[] buffer = new byte[Disk.blockSize];
            SysLib.rawread(indirect, buffer);
            int difference = index - directSize;
            return SysLib.bytes2short(buffer, difference * 2);
        }
    }

    int registerTargetBlock(int offset, short iNumber) {
        int target = offset / Disk.blockSize;
        if (target < directSize) {
            if (direct[target] >= 0) {
                return -1;
            } else if (target > 0 && direct[target - 1] == -1) {
                return -2;
            } else {
                direct[target] = iNumber;
                return 0;
            }
        }

        if (indirect < 0) {
            return -3;
        }
        else {
            byte[] buffer = new byte[Disk.blockSize];
            SysLib.rawread(indirect, buffer);
            int index = target - directSize;
            if (SysLib.bytes2short(buffer, index * 2) > 0) {
                return -1;
            }
            else {
                SysLib.short2bytes(iNumber, buffer, index * 2);
                SysLib.rawwrite(indirect, buffer);
                return 0;
            }
        }
    }

    boolean registerIndexBlock(short iNumber) {
        for (int i = 0; i < directSize; i++) {
            if (direct[i] == -1) {
                return false;
            }
        }
        if (indirect != -1) {
            return false;
        } else {
            indirect = iNumber;
            byte[] data = new byte[Disk.blockSize];

            for (int i = 0; i < (Disk.blockSize)/2; ++ i) {
                SysLib.short2bytes((short) - 1, data, i * 2);
            }
            SysLib.rawwrite(iNumber, data);
            return true;
        }

    }

    byte[] unregisterIndexBlock() {
        if (indirect >= 0) {
            indirect = -1;
            byte[] data = new byte[Disk.blockSize];
            SysLib.rawread( indirect, data );
            return data;
        }
        return null;
    }
}