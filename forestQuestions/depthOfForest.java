// You are given a forest (it may contain a single tree or more than one tree) with N nodes.

// Each node is given an integer value 0 to (N­-1).

// You have to find:

// ===================

// The depth of forest at which maximum number of nodes are present.

// N can be very large. Aim for an algorithm with a time complexity of O(N).

// INPUT FORMAT

// =================

// An integer T, denoting the number of testcases, followed by 2T lines, as each testcase will contain 2 lines.

// First line of each testcase has the value of N.

// Second line of each testcase has list of N values where the number at index i is the parent of node i. The parent of root is -1. ( The index has the range [0, N­-1] ).

// OUTPUT FORMAT

// ===============

// For each testcase given, output a single line that has the depth of forest at which maximum number of nodes are present. If multiple depths has same number of nodes, then deepest depth should be selected.

// SAMPLE INPUT

// ==============

// 2

// 6

// 5 -1 1 1 5 2

// 13

// 4 3 -1 -1 1 2 7 3 1 4 2 1 2

// SAMPLE OUTPUT

// ====================

// 3

// 1

import java.util.*;

public class depthOfForest {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int T = sc.nextInt(); // Number of test cases

        while (T-- > 0) {
            int N = sc.nextInt(); // Number of nodes
            int[] parents = new int[N];
            for (int i = 0; i < N; i++) {
                parents[i] = sc.nextInt();
            }

            List<List<Integer>> forest = new ArrayList<>();
            for (int i = 0; i < N; i++) {
                forest.add(new ArrayList<>());
            }

            int[] inDegree = new int[N];
            for (int i = 0; i < N; i++) {
                if (parents[i] != -1) {
                    forest.get(parents[i]).add(i);
                    inDegree[i]++;
                }
            }

            int maxDepth = 0;
            int maxNodesAtDepth = 0;

            for (int i = 0; i < N; i++) {
                if (parents[i] == -1) {
                    Queue<Integer> queue = new LinkedList<>();
                    queue.add(i);
                    int depth = 0;
                    while (!queue.isEmpty()) {
                        int size = queue.size();
                        if (size >= maxNodesAtDepth) {
                            maxNodesAtDepth = size;
                            maxDepth = depth;
                        }
                        for (int j = 0; j < size; j++) {
                            int node = queue.poll();
                            for (int child : forest.get(node)) {
                                queue.add(child);
                            }
                        }
                        depth++;
                    }
                }
            }

            System.out.println(maxDepth);
        }

        sc.close();
    }
}
