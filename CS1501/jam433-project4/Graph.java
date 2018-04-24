import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class Graph {

    boolean containsCopper = true;  // Default sets to copper-only

    int numVertices;                // Holds the count of the vertices in the graph
    Vertex[] vertices;              // Stores vertices used to find a vertex by index
    ArrayList<Edge> edges;          // Holds edges used to determined MST
    ArrayList<Edge> MST;            // Stores the vertices of the MST

    // Constructor that builds graph via adjacency list
    public Graph(String file) throws FileNotFoundException {
        buildGraph(file);
    }

    // Finds the lowest latency path between v1 and v2
    // Using DFS and returning the lowest latency path
    void lowestLatencyPath(int v_id1, int v_id2) {
        if (v_id1 > numVertices || v_id2 > numVertices) {
            System.out.println("Invalid vertex. Please select vertices between 0 and " + (numVertices-1) + ".\n");
        }

        Vertex v_1 = vertices[v_id1];
        Vertex v_2 = vertices[v_id2];

        Object[] tokens = lowestLatencyDFS(v_1, v_2, "", 0, 0);

        System.out.println("\nLowest latency path between vertices " + v_1.v_id + " and " + v_2.v_id + ":");
        System.out.print(v_1.v_id);
        System.out.println(tokens[0]);
        System.out.print("Minimum bandwidth: " + tokens[2] + " Mbps\n");
    }

    // Recursive method to find the lowest latency path
    // Uses a DFS and returns an object array that stores path, time, and minimum bandwidth
    Object[] lowestLatencyDFS(Vertex v_1, Vertex v_2, String path, double time, int minBandwidth) {

        // Base case, target vertex has been reached
        if (v_1 == v_2) {
            return new Object[]{path, time, minBandwidth};
        }

        // Flags to initially set the minimum latency path
        double tempTime = -1.0;
        String minPath = "";

        for (Edge e : v_1.adjList) {

            Vertex neighborVertex = e.targetVertex;
            // Continue if vertex has already been visited
            if (path.contains("" + neighborVertex.v_id))
                continue;

            // Else add current vertex id into path list
            String tempPath = path + " -> " + neighborVertex.v_id;
            double minTime = time + e.travelTime;

            // Keeps track of lowest bandwidth
            int tempBandwidth = minBandwidth;
            if (minBandwidth == 0 || e.bandwidth < minBandwidth)
                tempBandwidth = e.bandwidth;

            Object[] latencyData = lowestLatencyDFS(neighborVertex, v_2, tempPath, minTime, tempBandwidth);
            if (latencyData == null)
                continue;

            String edgePath = (String)latencyData[0];
            double pathTime = (double)latencyData[1];
            int bandwidth = (int)latencyData[2];

            // If first lowest latency time or the latency time is less than the current latency time
            // We set the necessary information we need and return it
            if (tempTime == -1 || pathTime < tempTime) {
                tempTime = pathTime;
                minPath = edgePath;
                minBandwidth = bandwidth;
            }
        }

        // A path to the target vertex exists, then we can return the necessary data
        if (tempTime > 0) {
            return new Object[] {minPath, tempTime, minBandwidth };
        }

        // If reached here, no such path exists
        return null;
    }

    void copperConnectivity() {
        if (containsCopper) {
            System.out.println("\nThe graph consists of copper wires only.");

        } else {
            // Initially assume the graph can be connected with copper
            boolean isCopperConnected = true;

            // Iterate through the vertex neighbors
            for (Vertex v : vertices) {
                // Determine whether the edge is copper and dynamically update a boolean
                // to check if it can be connected via copper
                boolean hasCopperConnection = false;
                for (Edge e : v.adjList) {
                    if (e.material.equals("copper")) {
                        hasCopperConnection = true;
                        break;
                    }
                }

                if(!hasCopperConnection)
                    isCopperConnected = false;
            }

            if (isCopperConnected)
                System.out.println("\nThis graph can be considered connected with only copper wires but is not copper only.");
            else
                System.out.println("\nThis graph cannot be considered connected with only copper wires.");
        }
    }

    // Finds the maximum amount of bandwidth that can be sent between two vertices
    // It just takes the max and dynamically updates the max as it traverses through to the target vertex
    void maximumDataPath(int v_id1, int v_id2) {
        if (v_id1 > numVertices || v_id2 > numVertices) {
            System.out.println("Invalid vertex. Please select vertices between 0 and " + (numVertices-1) + ".\n");
        }

        Vertex v_1 = vertices[v_id1];
        Vertex v_2 = vertices[v_id2];

        int maxData = maxDataDFS(v_1, v_2, "", -1);
        System.out.println("Maximum data that can be transmitted between vertices " + v_id1 + " and " + v_id2 + ":");
        System.out.println(maxData + " Mbps");
    }

    // Recursive method for maximum data path
    // Uses a DFS to search for target vertex while dynamically updating max
    int maxDataDFS(Vertex v_1, Vertex v_2, String path, int maxBandwidth) {

        // Base case, we can return the max bandwidth
        if (v_1 == v_2) {
            return maxBandwidth;
        }

        // Initial flag setter for max to be empty
        int bandwidth = -1;

        for (Edge e : v_1.adjList) {
            Vertex neighborVertex = e.targetVertex;
            if (path.contains("" + neighborVertex.v_id))
                continue;

            // Here we set a temporary max value and
            // let that tempMax hold the new max for each recursion
            int tempMax = maxBandwidth;
            if (tempMax == -1 || e.bandwidth > tempMax)
                tempMax = e.bandwidth;

            // Update path and recurse using neighbor data
            String tempPath = path + neighborVertex.v_id;
            int newMax = maxDataDFS(neighborVertex, v_2, tempPath, tempMax);

            // If newMax is greater than the current bandwidth,
            // we set bandwidth to the new max and finally return the bandwidth
            if (newMax > bandwidth)
                bandwidth = newMax;
        }
        return bandwidth;
    }

    void lowestAverageST() {
        double avg = 0.0;
        if  (MST == null)
            avg = KruskalMST() / MST.size();

        for (Edge e : MST)
            System.out.println(e.sourceVertex.v_id + " -- " + e.targetVertex.v_id);

        System.out.printf("\nThe lowest average for the spanning tree is %.2f ns\n", avg);
    }

    // Returns the minimum spanning tree of the graph
    // in terms of the total travel time for all edges
    double KruskalMST() {

        int curr = 0;
        double minWeight = 0.0;
        MST = new ArrayList<>();

        int[] parents = new int[numVertices];
        byte[] rank = new byte[numVertices];
        for (int i=0; i<numVertices; i++) {
            parents[i] = i;
            rank[i] = 0;
        }

        // Sorts edges from min to max travel time
        edges.sort(Comparator.naturalOrder());

        while (curr != edges.size()-1 && MST.size() < numVertices-1) {
            Edge e = edges.get(curr);
            int v_1 = e.sourceVertex.v_id;
            int v_2 = e.targetVertex.v_id;

            if (!connected(v_1, v_2, parents)) {
                union(v_1, v_2, parents, rank);
                MST.add(e);
                minWeight += e.travelTime;
            }
            curr++;
        }
        return minWeight;
    }

    // HELPER METHODS VIA BOOK CODE IMPLEMENTED FOR THIS PROJECT

    // Function that does the union of two sets using rank
    void union(int p, int q, int[] parent, byte[] rank) {
        int pRoot = find(p, parent);
        int qRoot = find(q, parent);

        if (qRoot == pRoot)
            return;

        if (rank[pRoot] < rank[qRoot])
            parent[pRoot] = qRoot;
        else if (rank[pRoot] > rank[qRoot])
            parent[qRoot] = pRoot;
        else {
            parent[qRoot] = pRoot;
            rank[pRoot]++;
        }
    }

    // Checks whether current graph is connected (cycle)
    boolean connected(int p, int q, int[] parent) {
        return find(p, parent) == find(q, parent);
    }

    // Utility function to find set of an element p
    // Using path compression --parent[parent[p]];
    int find(int p, int[] parent) {
        while (p != parent[p]) {
            parent[p] = parent[parent[p]];
            p = parent[p];
        }   return p;
    }

    // Realized this isn't really finding articulation points but rather if a pair of vertices
    // acts like articulation points for a given graph.
    // Runtime is determined to be O(V(V+E)) since we're doing a DFS every pair of vertices "removed"
    boolean findArticulationPoints() {

        boolean canFail = false;

        // Permute through possible pairs of vertices
        for (int v1=0; v1<numVertices; v1++) {
            for (int v2=v1+1; v2<numVertices; v2++) {

                // The method here marks two vertices as visited
                // and does a DFS to see if the graph is connected
                Vertex curr = null;
                Vertex v_1 = vertices[v1];
                Vertex v_2 = vertices[v2];
                boolean[] visited = new boolean[numVertices];

                visited[v_1.v_id] = true;
                visited[v_2.v_id] = true;

                // Sets the current vertex
                // Set curr to vertex 0 first
                if (v1 != 0)
                    curr = vertices[0];
                // Else if we're checking vertex 0
                // Sets new current vertex
                else {
                    if (v2 != numVertices-1)
                        curr = vertices[v2+1];
                    else if (v2-v1 != 1)
                         curr = vertices[v2-1];
                }

                articulationPointsDFS(curr, visited);

                // Enumerate to check if any vertices were not visited
                for (boolean aVisited : visited) {
                    if (!aVisited) {
                        canFail = true;
                    }
                }
            }
        }
        return canFail;
    }

    void articulationPointsDFS(Vertex curr, boolean[] visited) {

        if (visited[curr.v_id])
            return;

        visited[curr.v_id] = true;

        for (Edge e : curr.adjList) {

            Vertex neighborVertex = e.targetVertex;
            if (visited[neighborVertex.v_id])
                continue;

            articulationPointsDFS(neighborVertex, visited);
        }
    }

    void buildGraph(String file) throws FileNotFoundException {

        if (file == null)
            throw new FileNotFoundException();

        Scanner stdin = new Scanner(new File(file));

        edges = new ArrayList<>();

        numVertices = stdin.nextInt();
        vertices = new Vertex[numVertices];
        stdin.nextLine();

        for (int i=0; i<vertices.length; i++)
            vertices[i] = new Vertex(i);

        while (stdin.hasNextLine()) {
            // Takes in the buffer and tokenizes the buffer to the data needed
            String buffer = stdin.nextLine();
            String[] tokens = buffer.split(" ");

            Vertex v_1 = vertices[Integer.parseInt(tokens[0])];
            Vertex v_2 = vertices[Integer.parseInt(tokens[1])];
            String material = tokens[2];
            if (material.equals("optical"))
                containsCopper = false;
            int bandwidth = Integer.parseInt(tokens[3]);
            int length = Integer.parseInt(tokens[4]);

            Edge forwardEdge = new Edge(v_1, v_2, material, bandwidth, length);
            Edge backwardEdge = new Edge(v_2, v_1, material, bandwidth, length);

            // Need to keep track of only one edge for MST
            edges.add(forwardEdge);

            v_1.adjList.addFirst(forwardEdge);
            v_2.adjList.addFirst(backwardEdge);
        }
    }

    // FOR DEBUGGING
    void printGraph() {
        for (Vertex v : vertices) {
            System.out.println("\nAdjacency list of vertex " + v.v_id + ":");
            System.out.print(v.v_id);
            for (Edge e : v.adjList) {
                System.out.print(" -> " + e.targetVertex.v_id);
            }   System.out.println();
        }
    }

    private class Edge implements Comparable<Edge> {

        // Speed of a single single data packet in m/s
        final int COPPER_CABLE = 230000000;
        final int FIBER_CABLE = 200000000;

        // Information that will be extracted from the text file
        Vertex sourceVertex;    // Source vertex
        Vertex targetVertex;    // Destination vertex
        String material;        // Copper or fiber
        int bandwidth;          // Bandwidth in megabits/s
        int length;             // Length in meters

        double travelTime;      // Time it takes to travel across a given wire in nanoseconds

        public Edge(Vertex sourceVertex, Vertex targetVertex, String material, int bandwidth, int length){
            this.sourceVertex = sourceVertex;
            this.targetVertex = targetVertex;
            this.material = material;
            this.bandwidth = bandwidth;
            this.length = length;

            if (material.equals("copper")) {
                travelTime = ((double)1/COPPER_CABLE)*length*1000000000;
            } else if (material.equals("optical")) {
                travelTime = ((double)1/FIBER_CABLE)*length*1000000000;
            }
        }

        public int compareTo(Edge edge){
            return Double.compare(travelTime, edge.travelTime);
        }
    }

    private class Vertex {
        int v_id;                   // Allows easier referencing for each vertex
        LinkedList<Edge> adjList;   // Holds the list of the neighbors for each vertex

        public Vertex(int v_id) {
            this.v_id = v_id;
            adjList = new LinkedList<>();
        }
    }
}
