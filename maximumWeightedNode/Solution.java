package maximumWeightedNode;

class Solution {
    public int maxWeightCell(int N, int Edge[]) {
        int weight = -1, node = -1;
        int[] wt = new int[N];

        for (int i = 0; i < N; i++) {
            if (Edge[i] == -1)
                continue;
            wt[Edge[i]] += i; // --> accumulates the "sum of the indices of nodes pointing to each node",
                              // thereby calculating the weight correctly as per the problem definition.
        }

        for (int i = 0; i < N; i++) {
            if (wt[i] >= weight) {
                weight = wt[i]; // update the maximum weight
                node = i; // --> updating the node which has maximum weight or maximum indegree
            }
        }

        return node;
    }

}
