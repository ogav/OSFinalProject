/* I need more explanation about the disk structure and the superblock.
A: The disk consists of 1000 block and the block#0 is the superblock.
The disk consists of 1000 blocks and you can consider those blocks are numbered from 0 to 999. Use the block #0 as the
superblock. For accessing this block, you should call SysLib.rawread( 0, data ) where data is a 512-byte array.
 */
import java.lang.*;

class SuperBlock {
    private final int defaultINodeBlocks = 64;
    public int totalBlocks; // the number of disk blocks
    public int totalInodes; // the number of inodes
    public int freeList;    // the block number of the free list's head

    // Taken from the 'CSS430FinalProject.pdf" file provided in Program 5 assignment description
    public SuperBlock(int diskSize) {
        byte[] superBlock = new byte[Disk.blockSize];
        SysLib.rawread(0, superBlock);
        totalBlocks = SysLib.bytes2int(superBlock, 0);
        totalInodes = SysLib.bytes2int(superBlock, 4);
        freeList = SysLib.bytes2int(superBlock, 8);

        if (totalBlocks == diskSize && totalInodes > 0 && freeList >= 2)
            return;     // disk contents are valid
        else {
            // need to format disk
            totalBlocks = diskSize;
            format(defaultINodeBlocks);
        }
    }

    public void format(int numBlocks) {
        totalInodes = numBlocks;

        for (short i = 0; i < totalInodes; i++) {
            Inode n = new Inode();
            n.flag = 0;
            n.toDisk(i);
        }

        // format the freeList blocks
        byte[] block = new byte[Disk.blockSize];
        for (int i = freeList; i < totalBlocks; i++) {
            if (i == totalBlocks - 1) {     // to be able to identify the last block
                SysLib.int2bytes(-1, block, 0);
                SysLib.rawwrite(i, block);
            } else {
                SysLib.rawread(i, block);
                SysLib.int2bytes(i, block, 0);
                SysLib.rawwrite(i, block);
            }
        }

        sync();
    }

    // Write back totalBlocks, inode blocks, and freeList to disk
    public void sync() {
        byte[] block = new byte[Disk.blockSize];
        SysLib.int2bytes(totalBlocks, block, 0);
        SysLib.int2bytes(totalInodes, block, 4);
        SysLib.int2bytes(freeList, block, 8);
        SysLib.rawwrite(0, block);
    }

    // Dequeue the top block from the free list and return the int of the next free block
    public int getFreeBlock() {
        // check that the freeList value is valid and not the superblock val and not greater than the total # of blocks
        if (freeList > 0 && freeList < totalBlocks) {
            int freeBlockValue = freeList;
            byte[] block = new byte[Disk.blockSize];
            SysLib.rawread(freeList, block);

            // update freeList
            freeList = SysLib.bytes2int(block, 0);

            SysLib.int2bytes(0, block, 0);
            SysLib.rawwrite(freeBlockValue, block);
            return freeList;
        }
        // free list value not valid, return err value
        return freeList;
    }

    // Enqueue a given block to the end of the free list
    public void returnBlock(int blockNum) {
        if (blockNum > 0 && blockNum < totalBlocks) {
            byte[] block = new byte[Disk.blockSize];

            SysLib.int2bytes(blockNum, block, 0);
            SysLib.rawwrite(blockNum, block);


            if (freeList < 0) {     // nothing in the freelist, add to the begininng
                freeList = blockNum;
                SysLib.rawwrite(blockNum, block);
            } else {                // freelist has multiple values in it, find the end of the list to append free block
                int nextFreeBlock = freeList;
                int previous = freeList;
                byte[] nextFreeBlockVal = new byte[Disk.blockSize];
                byte[] newBlockToAdd = new byte[Disk.blockSize];

                SysLib.int2bytes(-1, newBlockToAdd, 0);

                while (nextFreeBlock != -1) {
                    SysLib.rawread(nextFreeBlock, nextFreeBlockVal);
                    previous = SysLib.bytes2int(nextFreeBlockVal, 0);
                    // maybe concatenate these two lines
                    nextFreeBlock = previous;
                }

                SysLib.int2bytes(blockNum, nextFreeBlockVal, 0);
                SysLib.rawwrite(nextFreeBlock, nextFreeBlockVal);
                SysLib.rawwrite(blockNum, newBlockToAdd);

            }
        }
    }
}