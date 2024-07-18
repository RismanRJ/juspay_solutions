// You are given a forest (it may contain a single tree or more than one tree) with N nodes.

// Each node is given an integer value 0 to (N­-1).

// You have to find:

// ==================

// Level order traversal: print nodes at every level of the forest.

// N can be very large. Aim for an algorithm with a time complexity of O(N).

// INPUT FORMAT

// ==============

// An integer T, denoting the number of testcases, followed by 2T lines, as each testcase will contain 2 lines.

// First line of each testcase has the value of N.

// Second line of each testcase has list of N values where the number at index i is the parent of node i. The parent of root is -1. ( The index has the range [0, N­-1] ).

// OUTPUT FORMAT

// ================

// For each testcase given, Suppose m is the height of tree, then next m lines must contain the nodes of that level in ascending order separated by space. After printing level order traversal of each testcase, print a new line.

// SAMPLE INPUT

// ================

// 2

// 6

// 5 -1 1 1 5 2

// 13

// 4 3 -1 -1 1 2 7 3 1 4 2 1 2

// SAMPLE OUTPUT

// ===============

// 1

// 2 3

// 5

// 0 4

// 2 3

// 1 5 7 10 12

// 4 6 8 11

// 0 9

import java.util.*;

public class levelOrderTraversalInForest {

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

            // Building the adjacency list representation of the forest
            for (int i = 0; i < N; i++) {
                if (parents[i] != -1) {
                    forest.get(parents[i]).add(i);
                }
            }

            // Finding root nodes
            List<Integer> roots = new ArrayList<>();
            for (int i = 0; i < N; i++) {
                if (parents[i] == -1) {
                    roots.add(i);
                }
            }

            // Performing BFS for each root to print nodes level by level
            for (int root : roots) {
                levelOrderTraversal(root, forest);
            }
            System.out.println(); // Print a new line after each test case
        }

        sc.close();
    }

    public static void levelOrderTraversal(int root, List<List<Integer>> forest) {
        Queue<Integer> queue = new LinkedList<>();
        queue.add(root);

        while (!queue.isEmpty()) {
            int levelSize = queue.size();
            List<Integer> currentLevel = new ArrayList<>();

            for (int i = 0; i < levelSize; i++) {
                int node = queue.poll();
                currentLevel.add(node);
                for (int child : forest.get(node)) {
                    queue.add(child);
                }
            }

            Collections.sort(currentLevel);
            for (int node : currentLevel) {
                System.out.print(node + " ");
            }
            System.out.println(); // Print nodes at current level
        }
    }
}
