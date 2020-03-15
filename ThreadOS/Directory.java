public class Directory {
    // Return values
    public final static int OK = 0;
    public final static short ERROR = -1;

    private static int maxChars = 30; // max characters of each file name
 
    // Directory entries
    private int fsizes[];        // each element stores a different file size.
    private char fnames[][];    // each element stores a different file name.
 
    public Directory( int maxInumber ) { // directory constructor
       fsizes = new int[maxInumber];     // maxInumber = max files
       for ( int i = 0; i < maxInumber; i++ ) 
          fsizes[i] = 0;                 // all file size initialized to 0
       fnames = new char[maxInumber][maxChars];
       String root = "/";                // entry(inode) 0 is "/"
       fsizes[0] = root.length( );        // fsize[0] is the size of "/".
       root.getChars( 0, fsizes[0], fnames[0], 0 ); // fnames[0] includes "/"
    }
 
    public void bytes2directory( byte data[] ) {
       // assumes data[] received directory information from disk
       // initializes the Directory instance with this data[]
       int offset = 0;
       for (int i = 0; i <  fsizes.length; i++ ) {
           fsizes[i] = SysLib.bytes2int(data, offset);
           offset += 4; // int is 4 bytes
       }
       for (int i = 0; i < fnames.length; i ++) {
           String fname = new String(data, offset, maxChars * 2); // construct a new String by decoding data using the platform's default charset
           fname.getChars(0, fsizes[i], fnames[i], 0);
           offset += (maxChars * 2); // char is 2 bytes
       }
    }
 
    public byte[] directory2bytes( ) {
       // converts and return Directory information into a plain byte array
       // this byte array will be written back to disk
       // note: only meaningfull directory information should be converted
       // into bytes.
       int offset = 0;
       byte[] data = new byte[(fsizes.length * 4) + (fnames.length * maxChars * 2)];
       for (int i = 0; i < fsizes.length; i ++) {
           SysLib.int2bytes(fsizes[i], data, offset);
           offset += 4; // int is 4 bytes
       }
       for (int i = 0; i < fnames.length; i ++) {
           String fname = new String(fnames[i], 0, fsizes[i]);
           byte[] temp = fname.getBytes();
           System.arraycopy(temp, 0, data, offset, temp.length);
           offset += (maxChars * 2);
       }
       return data;
    }
 
    public short ialloc( String filename ) {
       // filename is the one of a file to be created.
       // allocates a new inode number for this filename
       for (int i = 0; i < fsizes.length; i ++) {
           if (fsizes[i] == 0) {
            fsizes[i] = (filename.length > 30) ? maxChars : filename.length();
            filename.getChars(0, fsizes[i], fnames[i], 0);
            return (short) i;
           }
       }
       return ERROR;
    }
 
    public boolean ifree( short iNumber ) {
       // deallocates this inumber (inode number)
       // the corresponding file will be deleted.
    }
 
    public short namei( String filename ) {
       // returns the inumber corresponding to this filename
    }
 }