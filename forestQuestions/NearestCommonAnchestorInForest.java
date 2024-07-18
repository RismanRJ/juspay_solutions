// You are given a forest (it may contain a single tree or more than one tree) with N nodes.

// Each node is given an integer value 0 to (N­-1).

// You have to find:

// ==================

// Nearest common ancestor of two given nodes x1 and x2.

// N can be very large. Aim for an algorithm with a time complexity of O(N).

// INPUT FORMAT

// -------------

// An integer T, denoting the number of testcases, followed by 3T lines, as each testcase will contain 3 lines.

// First line of each testcase has the value of N.

// Second line of each testcase has list of N values where the number at index i is the parent of node i. The parent of root is -1. ( The index has the range [0, N­-1] ).

// Third line for each testcase contains two integers within the range of [0,N­-1] whose common ancestor you have to find.

// OUTPUT FORMAT

// ==============

// For each testcase given, output a single line that has the nearest common ancestor to two given nodes x1 and x2. If a common ancestor is not present then output '-1'.

// SAMPLE INPUT

// -------------

// 2

// 6

// 5 -1 1 1 5 2

// 0 3

// 13

// 4 3 -1 -1 1 2 7 3 1 4 2 1 2

// 8 5

// SAMPLE OUTPUT

// ================

// 1

// -1

import java.util.*;

public class NearestCommonAnchestorInForest {

    static int MAX = 20; // This is enough for N up to around 10^6

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int T = sc.nextInt(); // Number of test cases

        while (T-- > 0) {
            int N = sc.nextInt(); // Number of nodes
            int[] parents = new int[N];
            for (int i = 0; i < N; i++) {
                parents[i] = sc.nextInt();
            }
            int x1 = sc.nextInt();
            int x2 = sc.nextInt();

            List<List<Integer>> forest = new ArrayList<>();
            for (int i = 0; i < N; i++) {
                forest.add(new ArrayList<>());
            }

            // Building the adjacency list representation of the forest
            for (int i = 0; i < N; i++) {
                if (parents[i] != -1) {
                    forest.get(parents[i]).add(i);
                }
            }

            // Find all root nodes
            List<Integer> roots = new ArrayList<>();
            for (int i = 0; i < N; i++) {
                if (parents[i] == -1) {
                    roots.add(i);
                }
            }

            // Depth array and Binary Lifting table
            int[] depth = new int[N];
            int[][] up = new int[N][MAX];

            // Initialize Binary Lifting table
            for (int i = 0; i < N; i++) {
                Arrays.fill(up[i], -1);
            }

            // Perform DFS for each root to fill depth and Binary Lifting table
            for (int root : roots) {
                dfs(root, -1, 0, depth, up, forest);
            }

            // Find LCA
            int lca = findLCA(x1, x2, depth, up);

            System.out.println(lca);
        }

        sc.close();
    }

    public static void dfs(int node, int parent, int d, int[] depth, int[][] up, List<List<Integer>> forest) {
        depth[node] = d;
        up[node][0] = parent;
        for (int i = 1; i < MAX; i++) {
            if (up[node][i - 1] != -1) {
                up[node][i] = up[up[node][i - 1]][i - 1];
            } else {
                up[node][i] = -1;
            }
        }
        for (int child : forest.get(node)) {
            if (child != parent) {
                dfs(child, node, d + 1, depth, up, forest);
            }
        }
    }

    public static int findLCA(int u, int v, int[] depth, int[][] up) {
        if (depth[u] < depth[v]) {
            int temp = u;
            u = v;
            v = temp;
        }

        int diff = depth[u] - depth[v];

        // Lift u to the same level as v
        for (int i = 0; i < MAX; i++) {
            if ((diff & (1 << i)) != 0) {
                u = up[u][i];
            }
        }

        if (u == v)
            return u;

        // Lift both u and v to their LCA
        for (int i = MAX - 1; i >= 0; i--) {
            if (up[u][i] != up[v][i]) {
                u = up[u][i];
                v = up[v][i];
            }
        }

        return up[u][0] != -1 ? up[u][0] : -1;
    }
}
