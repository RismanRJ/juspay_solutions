import java.util.Scanner;

public class maximumWeightNode {

    public static int maximumWeightNode(int[] edges, int nodes) {

        int maxWt = 0;
        int maxWtNode = 0;
        int[] wt = new int[nodes];

        for (int i = 0; i < nodes; i++) {
            if (edges[i] != -1)
                wt[edges[i]] += i;
        }
        for (int i = 0; i < nodes; i++) {
            if (maxWt <= wt[i]) {
                maxWt = wt[i];
                maxWtNode = i;
            }
        }
        return maxWtNode;
    }

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        int nodes = sc.nextInt();

        int edges[] = new int[nodes];
        for (int i = 0; i < nodes; i++)
            edges[i] = sc.nextInt();

        int maxWtNode = maximumWeightNode(edges, nodes);

        System.out.println("maximum Weight Node -->" + maxWtNode);

        sc.close();
    }
}
