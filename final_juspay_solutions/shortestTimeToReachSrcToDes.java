import java.util.*;

class Pair {
    int node;
    int distance;

    Pair(int node, int distance) {
        this.node = node;
        this.distance = distance;
    }
}

public class shortestTimeToReachSrcToDes {
    static Map<Integer, Integer> nodeToIndex = new HashMap<>();
    static Map<Integer, Integer> indexToNode = new HashMap<>();

    public static void dijkstra(int size, int[] dist, int src, List<List<Pair>> graph) {
        boolean[] vis = new boolean[size];
        PriorityQueue<Pair> pq = new PriorityQueue<>((x, y) -> x.distance - y.distance);
        pq.add(new Pair(src, 0));

        while (!pq.isEmpty()) {
            Pair current = pq.poll();
            int node = current.node;
            int distance = current.distance;

            if (vis[node])
                continue;
            vis[node] = true;
            dist[node] = distance;

            for (Pair neighbor : graph.get(node)) {
                if (!vis[neighbor.node]) {
                    pq.add(new Pair(neighbor.node, distance + neighbor.distance));
                }
            }
        }
    }

    public static int shortestTime(int src, int des, List<List<Pair>> graph, int size) {
        int[] dist = new int[size];
        Arrays.fill(dist, Integer.MAX_VALUE);
        dist[src] = 0;

        dijkstra(size, dist, src, graph);

        return dist[des] == Integer.MAX_VALUE ? -1 : dist[des];
    }

    public static boolean canReach(int src, int des, List<List<Pair>> graph, int size, boolean vis[]) {

        vis[src] = true;

        for (Pair p : graph.get(src)) {
            int node = p.node;
            if (node == des)
                return true;
            else if (!vis[node]) {
                if (canReach(node, des, graph, size, vis))
                    return true;
            }
        }

        return false;
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int nodes = sc.nextInt();

        int index = 0;
        for (int i = 0; i < nodes; i++) {
            int member = sc.nextInt();
            nodeToIndex.put(member, index);
            indexToNode.put(index, member);
            index++;
        }

        int size = index;
        List<List<Pair>> graph = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            graph.add(new ArrayList<>());
        }

        int edges = sc.nextInt();
        while (edges-- > 0) {
            int u = sc.nextInt();
            int v = sc.nextInt();
            int wt = sc.nextInt();
            graph.get(nodeToIndex.get(u)).add(new Pair(nodeToIndex.get(v), wt));
        }

        int src = sc.nextInt();
        int des = sc.nextInt();
        int shortestTime = shortestTime(nodeToIndex.get(src), nodeToIndex.get(des), graph, size);
        System.out.println("shortest Time --> " + shortestTime);
        boolean vis[] = new boolean[size];
        boolean res = canReach(nodeToIndex.get(src), nodeToIndex.get(des), graph, size, vis);

        System.out.println("IS able to reach A to B ? --> " + res);
        sc.close();
    }
}
