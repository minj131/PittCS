import java.io.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;

public class ac_test {

    public ac_test() throws Exception {

        DLBDict DLB = new DLBDict();

        ArrayList<String> list = new ArrayList<>();
        HashSet<String> ref = new HashSet<>();

        int count = 0;

        double avgTime = 0;
        int avgCount = 0;

        BufferedReader stdin = new BufferedReader(new FileReader("dictionary.txt"));

        try {
            while (stdin.ready()) DLB.add(new StringBuilder(stdin.readLine()));
            System.out.println("DLB has been loaded!");
        } catch (Exception e) {
            System.out.println("Problem occurred when trying to load DLB. Exception: " + e);
        }

        try {
            BufferedReader inref = new BufferedReader(new FileReader("user_history.txt"));
            while (inref.ready()) ref.add(inref.readLine());
            System.out.println("\nUser history has been loaded!");
            for (String s : ref) System.out.println(s);
        } catch (Exception e) {
            System.out.println("\nNo user history text file found. A new one will be created.\n");
        }

        System.out.print("\nEnter your first character: ");

        Scanner sc = new Scanner(System.in);
        String input = sc.next();

        while (!input.equals("!")) {

            StringBuilder key = new StringBuilder(input);

            switch(input.charAt(input.length()-1)) {
                case '1':
                    ref.add(list.get(0));
                    System.out.println("WORD COMPLETED: " + list.get(0));
                    input = "";
                    break;
                case '2':
                    ref.add(list.get(1));
                    System.out.println("WORD COMPLETED: " + list.get(1));
                    input = "";
                    break;
                case '3':
                    ref.add(list.get(2));
                    System.out.println("WORD COMPLETED: " + list.get(2));
                    input = "";
                    break;
                case '4':
                    ref.add(list.get(3));
                    System.out.println("WORD COMPLETED: " + list.get(3));
                    input = "";
                    break;
                case '5':
                    ref.add(list.get(4));
                    System.out.println("WORD COMPLETED: " + list.get(4));
                    input = "";
                    break;
                case '$':
                    ref.add(input.substring(0, input.length()-1));
                    System.out.println("WORD COMPLETED: " + input.substring(0, input.length()-1));
                    input = "";
            }

            list.clear();

            long start = System.nanoTime();

            // consults user history table before searching trie to populate list
            for (String s : ref) {
                if (DLB.isBiggestPrefix(key, s))
                    list.add(s);
            }

            if (DLB.autoComplete(key, list, ref)) {

                long end = System.nanoTime();
                double time = (end - start)/1000000000.0;
                avgTime+=time;
                avgCount++;
                NumberFormat formatter = new DecimalFormat("#0.000000");

                System.out.println("\n(" + (formatter.format(time) + " s)"));

                System.out.println("Predictions:");
                for (String s : list) {
                    System.out.print("(" + (++count) + "): " + s +"\t");
                    if (count == 5) break;
                }
                count = 0;
            }

            System.out.print("\n\nEnter the next character: ");
            input+=sc.next();
        }

        NumberFormat formatter = new DecimalFormat("#0.000000");
        avgTime = avgTime/avgCount;
        System.out.println("\nAverage time: " + (formatter.format(avgTime)) + " (s)\nBye!");

        try {
            FileWriter stdout = new FileWriter("user_history.txt", true);
            for (String s : ref) {
                stdout.write(s);
                stdout.write("\n");
            }
            stdout.close();
        } catch (IOException e) {
            System.out.println("An error has occurred. Please try again.");
            System.exit(0);
        }
    }



    public static void main(String[] args) throws Exception {new ac_test();}
}
