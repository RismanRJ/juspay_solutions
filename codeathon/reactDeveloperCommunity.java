import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

public class reactCommunity {

    private static class Graph {

        int val = 0;
        HashMap<Integer, ArrayList<Integer>> adj; // --> storing adj list with correspponding to the members

        Graph() {
            this.adj = new HashMap<>();
        }

        public void addEdge(int edge1, int edge2) {
            if (!adj.containsKey(edge1)) { // --> whether is key is present or not
                adj.put(edge1, new ArrayList<>()); // --> if it is not present update the key with edg1 and assign a
                                                   // empty List for adding edge 2
            }

            adj.get(edge1).add(edge2); // --> add edge1 and edge 2 [ edge1 -->{edge2}]
        }

        public boolean isReachable(int src, int des, Graph g) {
            Queue<Integer> q = new LinkedList<>();
            HashSet<Integer> vis = new HashSet<>(); // visting check
            vis.add(src);
            q.add(src);
            while (!q.isEmpty()) {
                int node = q.poll();
                if (node == des) // --> if A reaches B
                    return true;
                for (int it : g.adj.getOrDefault(node, new ArrayList<>())) { // else traverse through the list
                    // If there is no list it will throw error --> so use getOrDefault
                    if (!vis.contains(it)) {
                        vis.add(it);
                        q.add(it);
                    }
                }
            }

            return false;
        }
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int nodes = sc.nextInt();
        Graph g = new Graph();
        for (int i = 0; i < nodes; i++) {
            int u = sc.nextInt();
            int v = sc.nextInt();
            g.addEdge(u, v);
        }
        System.out.println(g.adj);
        int src = sc.nextInt();
        int des = sc.nextInt();

        System.out.println(g.isReachable(src, des, g));

        sc.close();
    }

}
