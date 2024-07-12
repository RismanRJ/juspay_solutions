
import java.util.Scanner;

public class Solution {

    public static void dfs(int node, int[] Edge, boolean[] vis, boolean[] path, long[] ans) {

        vis[node] = true;
        path[node] = true;

        if (Edge[node] != -1) {
            int adj = Edge[node];
            if (!vis[adj]) {
                dfs(adj, Edge, vis, path, ans);
            } else if (path[adj]) {
                int curr = adj;
                long sum = 0;
                do {
                    sum++;
                    curr = Edge[curr];
                } while (curr != adj);
                ans[0] = Math.max(ans[0], sum);
            }
        }
        path[node] = false;
    }

    public static long largesSumCycle(int N, int Edge[]) {

        boolean[] vis = new boolean[N];
        boolean[] path = new boolean[N];
        long[] ans = { -1 };
        for (int i = 0; i < N; i++) {

            if (!vis[i]) {

                dfs(i, Edge, vis, path, ans);
            }
        }

        return ans[0];
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int n = sc.nextInt();
        int Edges[] = new int[n];
        for (int i = 0; i < n; i++)
            Edges[i] = sc.nextInt();

        System.out.println(largesSumCycle(n, Edges));
        sc.close();

    }
}