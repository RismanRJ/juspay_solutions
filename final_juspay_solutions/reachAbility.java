import java.util.Scanner;

public class reachAbility {

    public static boolean isReachableNode(int nodes, int[] edges, int src, int des, boolean[] vis) {

        vis[src] = true;

        if (edges[src] != -1) {
            int adj = edges[src];

            if (!vis[adj]) {
                vis[adj] = true;
                if (adj == des)
                    return true;
                if (isReachableNode(nodes, edges, adj, des, vis) == true)
                    return true;
            }
        }
        return false;
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int nodes = sc.nextInt();

        int edges[] = new int[nodes];
        for (int i = 0; i < nodes; i++)
            edges[i] = sc.nextInt();

        int src = sc.nextInt();
        int des = sc.nextInt();

        boolean vis[] = new boolean[nodes];

        boolean res = isReachableNode(nodes, edges, src, des, vis);

        System.out.println("The given two nodes are Reachable or Not -->" + res);

        sc.close();
    }
}
