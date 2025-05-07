#include <iostream>
#include <vector>
#include <unordered_map>
#include <unordered_set>
using namespace std;

vector<vector<int>> graph, reverseGraph;
vector<bool> visited, influenced;
int enemyIndex, personIndex;

void dfs(int node) {
    visited[node] = true;
    for (int neighbor : graph[node]) {
        if (!visited[neighbor]) {
            influenced[neighbor] = true;
            dfs(neighbor);
        }
    }
}

int main() {
    int n;
    cin >> n;

    unordered_map<int, int> nodeToIndex;
    vector<int> indexToNode(n);

    for (int i = 0; i < n; ++i) {
        int node;
        cin >> node;
        nodeToIndex[node] = i;
        indexToNode[i] = node;
    }

    graph.assign(n, vector<int>());
    reverseGraph.assign(n, vector<int>());
    visited.assign(n, false);
    influenced.assign(n, false);

    int m;
    cin >> m;
    for (int i = 0; i < m; ++i) {
        int u, v;
        cin >> u >> v;
        int uIdx = nodeToIndex[u];
        int vIdx = nodeToIndex[v];
        graph[uIdx].push_back(vIdx);
        reverseGraph[vIdx].push_back(uIdx);
    }

    int enemyNode, personNode;
    cin >> enemyNode >> personNode;
    enemyIndex = nodeToIndex[enemyNode];
    personIndex = nodeToIndex[personNode];

    influenced[enemyIndex] = true;
    dfs(enemyIndex);

    bool found = false;
    for (int influencingNode : reverseGraph[personIndex]) {
        if (influenced[influencingNode]) {
            cout << indexToNode[influencingNode] << " ";
            found = true;
        }
    }

    if (!found) {
        cout << -1 << endl;
    }

    return 0;
}
