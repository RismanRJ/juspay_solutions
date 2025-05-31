#include <bits/stdc++.h>
using namespace std;

void solveTree(unordered_map<int, vector<int>>& mpp,
               unordered_map<int, int>& flags, int maxBlocks,
               int current, int node, int& cnt) {
    current = flags[node] == 1 ? current + 1 : 0;
    if (current > maxBlocks) return;

    if (mpp.find(node) == mpp.end()) { // leaf node
        cnt++;
        return;
    }

    for (int child : mpp[node]) {
        solveTree(mpp, flags, maxBlocks, current, child, cnt);
    }
}

int main() {
    int edgesCount;
    cin >> edgesCount;

    vector<vector<int>> edges;
    unordered_map<int, vector<int>> mpp;
    unordered_set<int> children;
    set<int> allNodes;

    for (int i = 0; i < edgesCount; i++) {
        int u, v;
        cin >> u >> v;
        edges.push_back({u, v});
        mpp[u].push_back(v);
        children.insert(v);
        allNodes.insert(u);
        allNodes.insert(v);
    }

    unordered_map<int, int> flags;
    int totalNodes = allNodes.size();

    for (int i = 0; i < totalNodes; i++) {
        int node, flag;
        cin >> node >> flag;
        flags[node] = flag;
    }

    int maxBlocks;
    cin >> maxBlocks;

    int root = -1;
    for (int node : allNodes) {
        if (children.find(node) == children.end()) {
            root = node;
            break;
        }
    }

    if (root == -1) {
        cout << "Invalid tree" << endl;
        return 0;
    }

    int cnt = 0;
    solveTree(mpp, flags, maxBlocks, 0, root, cnt);

    if (cnt == 0)
        cout << -1 << endl;
    else
        cout << cnt << endl;

    return 0;
}
