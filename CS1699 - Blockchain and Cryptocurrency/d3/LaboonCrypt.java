
public class LaboonCrypt {
    public static void main(String[] args) {
        // 0 = not verbose, 1 = verbose, 2 = very verbose, 3 = ultra verbose
        int verbose = 0;
        if (args.length == 2) {
            if (args[1].equals("-verbose"))
                verbose = 1;
            else if (args[1].equals("-veryverbose"))
                verbose = 2;
            else if (args[1].equals("-ultraverbose"))
                verbose = 3;
            else
                throw new IllegalArgumentException("\nTo see verbose output, use `-verbose` flag");
        } else if (args.length == 0 || args.length > 2)
            throw new IllegalArgumentException("\nUsage:\n" +
                    "java LaboonCrypt *string* *verbosity_flag*\n" +
                    "Verbosity flag can be omitted for hash output only\n" +
                    "Other options: -verbose -veryverbose -ultraverbose");

        String toHash = args[0];
        String[][] result = new String[12][12];

        if (verbose == 3)
            result[0][0] = LaboonHash.hash(toHash, true);
        else
            result[0][0] = LaboonHash.hash(toHash);

        String prev = result[0][0];
        for (int i=0; i<result.length; i++) {
            for (int j=0; j<result[i].length; j++) {
                if (j==0 && i==0) continue;
                if (verbose == 3)
                    result[i][j] = LaboonHash.hash(prev, true);
                else
                    result[i][j] = LaboonHash.hash(prev);
                prev = result[i][j];
            }
        }

        if (verbose > 0) {
            System.out.println("Initial Array:");
            print2dArray(result);
        }

        char[] input = toHash.toCharArray();
        int dindex = 0, rindex = 0;
        for (char c : input) {
            int d = (c * 11);
            int r = ((c + 3) * 7);
            int down = (dindex + (c * 11))%12;
            int right = (rindex + ((c + 3) * 7))%12;

            if (verbose == 3)
                result[down][right] = LaboonHash.hash(result[down][right], true);
            else
                result[down][right] = LaboonHash.hash(result[down][right]);

            if (verbose > 1) {
                System.out.println("Moving " + d + " down and " + r + " right " +
                        "- modifying [" + down + ", " + right + "] from " + result[down][right]
                        + " to " + LaboonHash.hash(result[down][right]));
            }
            dindex = down; rindex = right;
        }

        if (verbose > 0) {
            System.out.println("Final Array:");
            print2dArray(result);
        }
        String finalToHash = join2dArray(result);
        String hash;
        if (verbose == 3)
            hash = LaboonHash.hash(finalToHash, true);
        else
            hash = LaboonHash.hash(finalToHash);

        System.out.println("LaboonCrypt hash: " + hash);
    }

    public static void print2dArray(String[][] arr) {
        for (int i=0; i<arr.length; i++) {
            for (int j=0; j<arr[i].length; j++) {
                System.out.print(arr[i][j] + " ");
            }
            System.out.println();
        }
    }

    public static String join2dArray(String[][] arr) {
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<arr.length; i++) {
            for (int j=0; j<arr[i].length; j++) {
                sb.append(arr[i][j]);
            }
        }
        return sb.toString();
    }
}
