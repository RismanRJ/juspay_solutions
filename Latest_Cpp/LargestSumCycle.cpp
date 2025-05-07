#include <iostream>
#include <vector>
#include <algorithm>
using namespace std;

class Solution {
public:
    long long largestSumCycleWeight = -1;

    void dfs(int node, vector<int>& edges, vector<bool>& visited, vector<bool>& pathVisited) {
        visited[node] = true;
        pathVisited[node] = true;

        int adj = edges[node];
        if (adj != -1) {
            if (!visited[adj]) {
                dfs(adj, edges, visited, pathVisited);
            } else if (pathVisited[adj]) {
                // Cycle detected
                int curr = adj;
                long long sumWeight = 0;
                do {
                    sumWeight += curr;
                    curr = edges[curr];
                } while (curr != adj);
                sumWeight += curr;

                largestSumCycleWeight = max(largestSumCycleWeight, sumWeight);
            }
        }

        pathVisited[node] = false;
    }

    long long findLargestSumCycle(int n, vector<int>& edges) {
        vector<bool> visited(n, false), pathVisited(n, false);
        for (int i = 0; i < n; ++i) {
            if (!visited[i]) {
                dfs(i, edges, visited, pathVisited);
            }
        }
        return largestSumCycleWeight;
    }
};

int main() {
    int n;
    cin >> n;
    vector<int> edges(n);
    for (int i = 0; i < n; ++i) {
        cin >> edges[i];
    }

    Solution sol;
    long long result = sol.findLargestSumCycle(n, edges);
    cout << result << endl;

    return 0;
}
