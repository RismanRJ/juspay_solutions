import java.util.*;

public class minimumNeighborsToBlock {
    static List<List<Integer>> graph;
    static List<List<Integer>> reverseGraph;
    static int enemy, person;
    static boolean[] visited;
    static boolean[] influenced;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Read the number of nodes
        int n = scanner.nextInt();

        Map<Integer, Integer> nodeToIndex = new HashMap<>();
        Map<Integer, Integer> indexToNode = new HashMap<>();
        int index = 0;

        // Read nodes and map them to indices
        for (int i = 0; i < n; i++) {
            int node = scanner.nextInt();
            nodeToIndex.put(node, index);
            indexToNode.put(index, node);
            index++;
        }

        int size = index;
        graph = new ArrayList<>(size);
        reverseGraph = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            graph.add(new ArrayList<>());
            reverseGraph.add(new ArrayList<>());
        }

        visited = new boolean[size];
        influenced = new boolean[size];

        // Read the number of edges
        int numEdges = scanner.nextInt();
        for (int i = 0; i < numEdges; i++) {
            int u = scanner.nextInt();
            int v = scanner.nextInt();
            graph.get(nodeToIndex.get(u)).add(nodeToIndex.get(v));
            reverseGraph.get(nodeToIndex.get(v)).add(nodeToIndex.get(u));
        }

        // Read enemy and person nodes
        int enemyNode = scanner.nextInt();
        int personNode = scanner.nextInt();
        enemy = nodeToIndex.get(enemyNode);
        person = nodeToIndex.get(personNode);

        // Mark enemy as influenced
        influenced[enemy] = true;

        // Perform DFS from the enemy node to mark all influenced nodes
        dfs(enemy);

        // Check which nodes can influence the person and are influenced
        boolean found = false;
        for (int influencingNode : reverseGraph.get(person)) {
            if (influenced[influencingNode]) {
                System.out.print(indexToNode.get(influencingNode) + " ");
                found = true;
            }
        }

        // If no influencing nodes are found, print -1
        if (!found) {
            System.out.println("-1");
        }

        scanner.close();
    }

    static void dfs(int node) {
        visited[node] = true;
        for (int neighbor : graph.get(node)) {
            if (!visited[neighbor]) {
                influenced[neighbor] = influenced[node];
                dfs(neighbor);
            }
        }
    }
}
