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

public class nearestMeetingCell {

    public static void dijikstra(int cell, int[] edges, int nodes, int[] dt) {
        boolean vis[] = new boolean[nodes];

        PriorityQueue<pair> pq = new PriorityQueue<>((x, y) -> x.distance - y.distance);

        pq.add(new pair(cell, 0));
        vis[cell] = true;
        while (!pq.isEmpty()) {
            int node = pq.peek().node;
            int dist = pq.peek().distance;
            dt[node] = dist;
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

    public static int nearestMeetingNode(int nodes, int[] edges, int cell1, int cell2) {
        int nearestMeeting_NODE = -1;
        int maxDistance = Integer.MAX_VALUE;
        int c1[] = new int[nodes];
        int c2[] = new int[nodes];
        Arrays.fill(c1, -1);
        Arrays.fill(c2, -1);
        dijikstra(cell1, edges, nodes, c1);
        dijikstra(cell2, edges, nodes, c2);

        for (int i = 0; i < nodes; i++) {
            if (c1[i] == -1 || c2[i] == -1)
                continue;
            int dist = c1[i] + c2[i];

            if (dist < maxDistance) {
                maxDistance = dist;
                nearestMeeting_NODE = i;
            }
        }

        return nearestMeeting_NODE;
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int nodes = sc.nextInt();

        int edges[] = new int[nodes];
        for (int i = 0; i < nodes; i++)
            edges[i] = sc.nextInt();

        int cell1 = sc.nextInt();
        int cell2 = sc.nextInt();

        int nearestMeeting_NODE = nearestMeetingNode(nodes, edges, cell1, cell2);

        System.out.println("Nearest Meeting Node between " + cell1 + "and " + cell2 + " is " + nearestMeeting_NODE);

        sc.close();

    }
}
