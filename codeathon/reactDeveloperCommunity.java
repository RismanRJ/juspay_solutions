import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Scanner;


public class reactDeveloperCommunity {

    private static class Graph {

        @SuppressWarnings("unused")
        int val=0;
        Map<Node,List<Node>> adj;

        Graph() {
           this.val =0;
            this.adj = new HashMap<>();
        }

        public void addEdge(Node v1, Node v2) {
            if (!adj.containsKey(v1)) {
                adj.put(v1, new ArrayList<>());
            }
            if (!adj.containsKey(v2)) {
                adj.put(v2, new ArrayList<>());
            }
            // Add the 'to' node to the list of neighbors for the 'from' node
            adj.get(v1).add(v2);
        }
    }

    public static boolean isHeReacheB(Node src, Node des){

        Queue<Node> q = new LinkedList<>();
        HashSet<Node> vis = new HashSet<>();
        q.add(src);

        while (!q.isEmpty()) {
            Node n = q.poll();
            vis.add(n);

            for(Node it : new Graph().adj.getOrDefault(n,new ArrayList<>())){
                if(it==des)
                return true;
                if(!vis.contains(it)){
                    vis.add(it);
                    q.add(it);
                }
            }

        }

        return false;
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int members = sc.nextInt();
        Graph g = new Graph();
        Node membersID[] = new Node[members];
        for (int i = 0; i < members; i++)
            membersID[i] = new Node(sc.nextInt());
        int noOfEdges = sc.nextInt();
        for (int i = 0; i < noOfEdges; i++) {
            g.addEdge(new Node(sc.nextInt()), new Node(sc.nextInt()));
        }

        Node src = new Node(sc.nextInt());
        Node des = new Node(sc.nextInt());

       boolean res = isHeReacheB(src, des);
       System.out.println("A can reach B --> "+res);

        sc.close();

    }
}

class Node {
    int val;

    Node(int val) {
        this.val = val;
    }
}
