/*************************************************************************
 *  MODIFIED VERSION: Jamie Min
 *
 *  USAGE:
 *  Be careful for correct usage
 *  Compression: java MyLZW - mode < file.txt > file.lzw
 *  Expansion:   java MyLZW + < file.lzw > file.txt
 *
 *  Compress or expand binary input from standard input using LZW.
 *
 *  WARNING: STARTING WITH ORACLE JAVA 6, UPDATE 7 the SUBSTRING
 *  METHOD TAKES TIME AND SPACE LINEAR IN THE SIZE OF THE EXTRACTED
 *  SUBSTRING (INSTEAD OF CONSTANT SPACE AND TIME AS IN EARLIER
 *  IMPLEMENTATIONS).
 *
 *  See <a href = "http://java-performance.info/changes-to-string-java-1-7-0_06/">this article</a>
 *  for more details.
 *
 *************************************************************************/

public class MyLZW {

    private static final double MAX_RATIO = 1.1;
    private static final int MAX_WIDTH = 16;

    private static int R = 256;        // number of input chars
    private static int W = 9;          // initial codeword width
    private static int L = 512;        // initial number of codewords = 2^W

    private static char mode = 'n';   // current mode is set to "do (n)othing"

    public static void compress() {

        int readData = 0;           // input uncompressed data size
        int writeData = 0;          // output compressed data size
        double oldRatio = 0.0;      // old compression ratio
        double currRatio = 0.0;     // new compression ratio

        String input = BinaryStdIn.readString();

        TST<Integer> st = new TST<Integer>();
        initSymTable(st);

        int code = R+1;  // R is codeword for EOF (end of file)

        BinaryStdOut.write((byte) mode);

        while (input.length() > 0) {
            String s = st.longestPrefixOf(input);  // Find max prefix match s.
            BinaryStdOut.write(st.get(s), W);      // Print s's encoding.
            int t = s.length();

            readData += (t*16); // uncompressed data size is simply t * size of char in bits
            writeData += W;     // compressed data size would be W

            if (t < input.length() && code < L) {   // Add s to symbol table and codebook is not full
                st.put(input.substring(0, t + 1), code++);
                oldRatio = (double) readData / writeData;

            } else if (t < input.length() && code >= L) {     // else if there are more symbols and the code book is full
                if (W < MAX_WIDTH) {                // codeword width can still be expanded
                    W++;
                    L *= 2;                          // set new number of codewords = 2^(W+1)
                    st.put(input.substring(0, t+1), code++);

                } else if (W == MAX_WIDTH) {        // symbol table is full
                    if (mode == 'r') {              // reset mode
                        st = new TST<>();   // create new symbol table
                        initSymTable(st);

                        W = 9;      // reinitialize codeword width to 9
                        L = 512;    // reinitialize num of codewords to 2^W
                        code = R+1;

                        st.put(input.substring(0, t+1), code++);

                    } else if (mode == 'n') {       // monitor mode
                        currRatio = (double) readData / writeData;
                        double RCR = oldRatio / currRatio; // ratio of compression ratios = RCR
                        if (RCR > MAX_RATIO) {  // reset codebook if ratio is too high
                            st = new TST<>();   // create new symbol table
                            initSymTable(st);

                            W = 9;      // reinitialize codeword width to 9
                            L = 512;    // reinitialize num of codewords to 2^W
                            code = R+1;

                            st.put(input.substring(0, t+1), code++);
                        }
                    }
                }
            }
            input = input.substring(t);             // Scan past s in input.
        }
        BinaryStdOut.write(R, W);
        BinaryStdOut.close();
    } 


    public static void expand() {
        String[] st = initSymArray();
        int i = R+1;                             // next available codeword (257)

        mode = BinaryStdIn.readChar();       // read in first char to signify mode to use

        int codeword = BinaryStdIn.readInt(W);
        if (codeword == R) return;           // expanded message is empty string
        String val = st[codeword];

        int readData = 0;                    // input uncompressed data size
        int writeData = 0;                   // output compressed data size
        double oldRatio = 0.0;               // old compression ratio
        double currRatio = 0.0;              // new compression ratio

        while (true) {
            readData += (val.length()*16); // uncompressed data size is the curr string length
            writeData += W;     // compressed data size would be W

            BinaryStdOut.write(val);

            if (i >= L) {                    // if i is greater than the range of the number of available codewords
                if (W < MAX_WIDTH) {
                    W++;    // increment W
                    L *= 2; // set new num of codewords
                    oldRatio = (double) readData / writeData; // determine old compression ratio

                } else if (W == MAX_WIDTH) {
                    if (mode == 'r') {       // reset codebook
                        st = initSymArray(); // init new symbol table

                        W = 9;      // reinitialize codeword width to 9
                        L = 512;    // reinitialize num of codewords to 2^W
                        i = R+1;    // next available codeword (257)

                    } else if (mode == 'm') {
                        currRatio = (double) readData / writeData; // determine new comrpession ratio
                        double RCR = oldRatio / currRatio; // ratio of compression ratios = RCR

                        if (RCR > MAX_RATIO) {
                            st = initSymArray(); // init new symbol table

                            W = 9;      // reinitialize codeword width to 9
                            L = 512;    // reinitialize num of codewords to 2^W
                            i = R+1;    // next available codeword (257)
                            oldRatio = 0.0; // reinit compression ratio
                        }
                    }
                }
            }

            // proceed as normal
            codeword = BinaryStdIn.readInt(W);

            if (codeword == R) break;
            String s = st[codeword];

            if (i == codeword) s = val + val.charAt(0);   // special case hack

            if (i < L) {
                st[i++] = val + s.charAt(0);
                oldRatio = (double) readData / writeData;
            }   val = s;
        }
        BinaryStdOut.close();
    }

    public static void initSymTable(TST<Integer> st) {
        for (int i = 0; i < R; i++)
            st.put("" + (char) i, i);
    }

    public static String[] initSymArray() {
        String[] st = new String[65536];      // 2^MAX_W         // next available codeword value
        int i;
        // initialize symbol table with all 1-character strings
        for (i = 0; i < R; i++)
            st[i] = "" + (char) i;
        st[i++] = "";                        // (unused) lookahead for EOF
        return st;
    }



    public static void main(String[] args) {

        if (args.length >= 2)
            if (args[1].equals("n") || args[1].equals("r") || args[1].equals("m"))
                mode = args[1].charAt(0);
            else throw new IllegalArgumentException("Invalid mode type. See usage format.");

        if      (args[0].equals("-")) compress();
        else if (args[0].equals("+")) expand();
        else throw new IllegalArgumentException("Illegal command line argument");
    }

}
