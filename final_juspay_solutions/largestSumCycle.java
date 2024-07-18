import java.util.Scanner;

public class largestSumCycle {
    static int largeSum = 0;
    static int largeSumCycleWeight = 0;

    public static void largeSumCycle(int nodes, int start, int parent, int[] edges, boolean[] vis, boolean vispath[]) {

        vis[start] = true;
        vispath[start] = true;
        if (edges[start] != -1) {
            int adj = edges[start];
            if (!vis[adj]) {
                largeSumCycle(nodes, adj, parent, edges, vis, vispath);
            } else if (vispath[adj] == true) {
                int curr = adj;
                int sum = 0;
                int sumWeight = 0;
                do {
                    sumWeight += curr = edges[curr];
                    sum++;
                } while (curr != adj);
                largeSum = Math.max(sum, largeSum);
                largeSumCycleWeight = Math.max(largeSumCycleWeight, sumWeight);
            }
        }
        vispath[start] = false;
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int nodes = sc.nextInt();

        int edges[] = new int[nodes];
        for (int i = 0; i < nodes; i++)
            edges[i] = sc.nextInt();
        boolean vis[] = new boolean[nodes];
        boolean visPath[] = new boolean[nodes];
        for (int i = 0; i < nodes; i++) {
            if (!vis[i]) {
                if (edges[i] != -1) {

                    largeSumCycle(nodes, 0, -1, edges, vis, visPath);
                }
            }
        }

        if (largeSum > 0)
            System.out.println("largest sum cycle --> " + largeSum);
        else
            System.out.println("largest sum cycle --> " + 0);

        if (largeSumCycleWeight > 0)
            System.out.println("largest sum cycle weight --> " + largeSumCycleWeight);
        else
            System.out.println("largest sum cycle weight --> " + 0);
        sc.close();

    }
}