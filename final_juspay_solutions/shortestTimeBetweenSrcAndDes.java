import java.util.Arrays;
import java.util.PriorityQueue;
import java.util.Scanner;

class pair {
    int node;
    int distance;

    pair(int node, int distance) {
        this.node = node;
        this.distance = distance;
    }
}

public class shortestTimeBetweenSrcAndDes {

    public static void dijikstra(int[] edges, int nodes, int src, int des, int[] wt) {

        boolean vis[] = new boolean[nodes];

        PriorityQueue<pair> pq = new PriorityQueue<>((x, y) -> x.distance - y.distance);

        pq.add(new pair(src, 0));
        vis[src] = true;

        while (!pq.isEmpty()) {
            int node = pq.peek().node;
            int dist = pq.peek().distance;
            wt[node] = dist;
            pq.remove();
            if (edges[node] != -1) {
                int adj = edges[node];
                if (!vis[edges[node]]) {
                    vis[adj] = true;
                    pq.add(new pair(adj, dist + 1));
                }
            }

        }
    }

    public static int shortestDistance(int nodes, int[] edges, int src, int des) {
        int shortestTime = 0;
        int[] wt = new int[nodes];
        Arrays.fill(wt, -1);
        dijikstra(edges, nodes, src, des, wt);
        shortestTime = wt[des];
        return shortestTime;
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int nodes = sc.nextInt();

        int edges[] = new int[nodes];
        for (int i = 0; i < nodes; i++)
            edges[i] = sc.nextInt();

        int src = sc.nextInt();
        int des = sc.nextInt();

        int shortestDistance = shortestDistance(nodes, edges, src, des);

        System.out.println("Shortest Time to reach src to Destination is -->" + shortestDistance);

        sc.close();
    }

}
