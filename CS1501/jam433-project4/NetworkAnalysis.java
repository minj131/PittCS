import java.io.FileNotFoundException;
import java.util.Scanner;

public class NetworkAnalysis {
    public static void main(String[] args) throws FileNotFoundException {
        if (args.length != 1) {
            System.out.println("Invalid argument type. Please check your input and try again.");
            return;
        }
        Scanner sc = new Scanner(System.in);
        Graph graph = new Graph(args[0]);

        String cont;

        System.out.println("\n=======================================" +
                "\n= Welcome to the Network Analysis App =" +
                "\n=======================================");

        graph.printGraph();

        while(true) {
            System.out.println("\n(1) Find the lowest latency path");
            System.out.println("(2) Determine copper-only connectivity");
            System.out.println("(3) Find the maximum amount of data that can be transmitted between two vertices");
            System.out.println("(4) Find the lowest average latency spanning tree");
            System.out.println("(5) Determine if the graph fails when two vertices are removed");
            System.out.println("(6) Exit program");
            System.out.print(">");

            int input = sc.nextInt();

            if (input == 1) {
                System.out.println("\nPlease enter the ID of two vertices separated by a space.");
                System.out.print(">");
                sc.nextLine();

                String[] tokens = sc.nextLine().split(" ");

                if (tokens.length != 2) {
                    System.out.println("Invalid format. See usage example: '1 3'\n");
                    continue;
                }

                int v_id1 = Integer.parseInt(tokens[0]);
                int v_id2 = Integer.parseInt(tokens[1]);

                graph.lowestLatencyPath(v_id1, v_id2);

                System.out.print("\nWould you like to continue (y/n)? ");
                cont = sc.next();
                if (cont.equalsIgnoreCase("y"))
                    continue;
                else if (cont.equalsIgnoreCase("n"))
                    System.exit(0);

            } else if (input == 2) {
                graph.copperConnectivity();

                System.out.print("\nWould you like to continue (y/n)? ");
                cont = sc.next();
                if (cont.equalsIgnoreCase("y"))
                    continue;
                else if (cont.equalsIgnoreCase("n"))
                    System.exit(0);

            } else if (input == 3) {
                System.out.println("\nPlease enter the ID of two vertices separated by a space.");
                System.out.print(">");
                sc.nextLine();

                String[] token = sc.nextLine().split(" ");

                if (token.length != 2) {
                    System.out.println("Invalid format. See example usage: '1 3'\n");
                    continue;
                }

                int v_id1 = Integer.parseInt(token[0]);
                int v_id2 = Integer.parseInt(token[1]);

                graph.maximumDataPath(v_id1, v_id2);

                System.out.print("\nWould you like to continue (y/n)? ");
                cont = sc.next();
                if (cont.equalsIgnoreCase("y"))
                    continue;
                else if (cont.equalsIgnoreCase("n"))
                    System.exit(0);

            } else if (input == 4) {
                System.out.println("Lowest Average Latency Spanning Tree Edges:");
                graph.lowestAverageST();

                System.out.print("\nWould you like to continue (y/n)? ");
                cont = sc.next();
                if (cont.equalsIgnoreCase("y"))
                    continue;
                else if (cont.equalsIgnoreCase("n"))
                    System.exit(0);

            } else if (input == 5) {
                boolean canFail = graph.findArticulationPoints();
                if (canFail)
                    System.out.println("The graph would not remain connected if any two vertices in the graph were to fail.");
                else
                    System.out.println("The graph would remain connected if any two vertices in the graph were to fail.");

                System.out.print("\nWould you like to continue (y/n)? ");
                cont = sc.next();
                if (cont.equalsIgnoreCase("y"))
                    continue;
                else if (cont.equalsIgnoreCase("n"))
                    System.exit(0);

            } else if (input == 6) {
                System.exit(0);
            } else {
                System.out.println("Please pick an option between 1 and 6.");
            }   System.out.println();
        }
    }
}
