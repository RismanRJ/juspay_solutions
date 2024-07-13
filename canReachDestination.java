import java.util.*;

public class canReachDestination {

    private static class Graph {
        private Map<Node, List<Node>> adjList;

        public Graph() {
            adjList = new HashMap<>();
        }
    }

    public void addEdge(Node from, Node to ,Map<Node, List<Node>> adjList) {
        // Ensure both 'from' and 'to' nodes are in the adjacency list
        if (!adjList.containsKey(from)) {
            adjList.put(from, new ArrayList<>());
        }
        if (!adjList.containsKey(to)) {
            adjList.put(to, new ArrayList<>());
        }
        // Add the 'to' node to the list of neighbors for the 'from' node
        adjList.get(from).add(to);
    }

    public List<Node> getNeighbors(Node node ,Map<Node, List<Node>> adjList) {
        return adjList.getOrDefault(node, new ArrayList<>());
    }

    public boolean canReach(Node src, Node dest ,Map<Node, List<Node>> adjList) {
        if (src == dest) {
            return true;
        }

        Queue<Node> frontier = new LinkedList<>();
        Set<Node> visited = new HashSet<>();

        frontier.add(src);
        visited.add(src);

        while (!frontier.isEmpty()) {
            Node current = frontier.poll();
            for (Node neighbor : getNeighbors(current,adjList)) {
                if (neighbor.equals(dest)) {
                    return true;
                }
                if (!visited.contains(neighbor)) {
                    frontier.add(neighbor);
                    visited.add(neighbor);
                }
            }
        }
        return false;
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int nodes = sc.nextInt();

        canReachDestination graph = new canReachDestination();
        Node[] n = new Node[nodes];
        for (int i = 0; i < nodes; i++) {
            n[i] = new Node(sc.nextInt());
        }
        int possibleEdges = sc.nextInt();

        for (int i = 0; i < possibleEdges; i++) {
            graph.addEdge(new Node(sc.nextInt()), new Node(sc.nextInt()),new Graph().adjList);
        }

        Node src = new Node(sc.nextInt());
        Node des = new Node(sc.nextInt());

        System.out.println("Can reach " + des.label + " from " + src.label + "--> " + graph.canReach(src, des,new Graph().adjList));

        sc.close();
    }
}

class Node {
    int label;

    public Node(int label) {
        this.label = label;
    }

}
