
public class LaboonHash {

    public static final int BLOCK_SIZE = 8;

    public static void main(String[] args) {
        boolean isVerbose = false;
        if (args.length == 2) {
            if (args[1].equals("-verbose"))
                isVerbose = true;
            else
                throw new IllegalArgumentException("\nTo see verbose output, use `-verbose` flag");
        } else if (args.length == 0 || args.length > 2)
            throw new IllegalArgumentException("\nUsage:\n" +
                    "java LaboonHash *string* *verbosity_flag*\n" +
                    "Verbosity flag can be omitted for hash output only\n" +
                    "Other options: -verbose");

        String toHash = args[0];
        String finalHash = hash(toHash, isVerbose);
        System.out.println("LaboonHash hash = " + finalHash);
    }

    public static String hash(String toHash, boolean isVerbose) {
        String toPad = pad(toHash).toUpperCase();
        String padded = toHash+toPad;

        if (isVerbose) {
            System.out.println("\tPadded string: " + padded);
            System.out.println("\tBlocks:");
            printBlocks(padded);
        }

        String iv = "1AB0";
        for (int i=0; i<calculateNumBlocks(padded); i++) {
            String hash = compress(iv, getNextBlock(padded, i)).toUpperCase();
            if (isVerbose)
                System.out.println("\tIterating with " + iv + " / " + getNextBlock(padded, i) + " = " + hash);
            iv = compress(iv, getNextBlock(padded, i)).toUpperCase();
        }
        return iv;
    }

    public static String hash(String toHash) {
        String toPad = pad(toHash).toUpperCase();
        String padded = toHash+toPad;

        String iv = "1AB0";
        for (int i=0; i<calculateNumBlocks(padded); i++) {
            String hash = compress(iv, getNextBlock(padded, i)).toUpperCase();
            iv = compress(iv, getNextBlock(padded, i)).toUpperCase();
        }
        return iv;
    }

    public static String compress(String lhs, String rhs) {
        // Phase 1 of compression
        char[] result = new char[4];
        for (int i=0; i<result.length; i++) {
            result[i] = (char)(lhs.charAt(i) + rhs.charAt((rhs.length()/2-1)-i));
        }

        // Phase 2 of compression
        for (int i=0; i<result.length; i++) {
            result[i] ^= rhs.charAt((rhs.length()-1)-i);
        }

        // Phase 3 of compression
        for (int i=0; i<result.length; i++) {
            result[i] ^= result[(result.length-1)-i];
        }
        return charArrayToHex(result);
    }

    private static String charArrayToHex(char[] arr) {
        StringBuilder hex = new StringBuilder();
        for (char c : arr) {
            hex.append(Integer.toHexString((int) c % 16));
        }

        return hex.toString();
    }

    public static int calculateNumBlocks(String s) {
        int numBlocks;
        int len = s.length();
        if (len % BLOCK_SIZE == 0) {
            numBlocks = len / BLOCK_SIZE;
        } else {
            numBlocks = (len / BLOCK_SIZE) + 1;
        }
        return numBlocks;
    }

    public static String getNextBlock(String s, int index) {
        return s.substring(index*BLOCK_SIZE, index*BLOCK_SIZE+BLOCK_SIZE);
    }

    public static void printBlocks(String s) {
        int index = 0;
        for (int i=0; i<calculateNumBlocks(s); i++) {
            String rhs = s.substring(index, index += BLOCK_SIZE);
            System.out.println("\t" + rhs);
        }
    }

    public static String pad(String s) {
        int sizeToPad = BLOCK_SIZE - s.length()%BLOCK_SIZE;
        if (sizeToPad == BLOCK_SIZE) return "";
        int modValue = (int) Math.pow(16, sizeToPad);
        int moddedLen = s.length() % modValue;
        return String.format("%0" + sizeToPad + "x", moddedLen);
    }

}
