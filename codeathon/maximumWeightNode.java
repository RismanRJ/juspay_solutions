import java.util.Scanner;

public class maximumWeightNode { // --> return the node which has maximum indegree edges
    public static int MaximumWeightNode(int n, int[] Edges, int[] wt) {
        int maxWeight = -1, node = 0;
        for (int i = 0; i < n; i++) {
            if (Edges[i] != -1)
                wt[Edges[i]] += i; // --> updating wt array with maximum weight for the ith Node
        }
        for (int i = 0; i < n; i++) {
            if (maxWeight <= wt[i]) { // check if weight array of ith node has more weight than maximum weight
                maxWeight = wt[i]; // --> update the maximum weight
                node = i; // --> update the node
            }
        }
        return node;
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int n = sc.nextInt();
        int Edges[] = new int[n];
        for (int i = 0; i < n; i++)
            Edges[i] = sc.nextInt();

        int wt = MaximumWeightNode(n, Edges, new int[n]);

        System.out.println("Maximum weighted Node -->" + wt);

        sc.close();
    }
}
