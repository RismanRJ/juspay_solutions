#include <iostream>
#include <vector>
using namespace std;

bool isReachableNode(int nodes, const vector<int>& edges, int src, int des, vector<bool>& vis) {
    if (src == des) return true;

    vis[src] = true;

    if (edges[src] != -1) {
        int adj = edges[src];

        // Safeguard against invalid indices
        if (adj >= 0 && adj < nodes && !vis[adj]) {
            if (adj == des) return true;
            if (isReachableNode(nodes, edges, adj, des, vis))
                return true;
        }
    }

    return false;
}

int main() {
    int nodes;
    cin >> nodes;

    vector<int> edges(nodes);
    for (int i = 0; i < nodes; ++i)
        cin >> edges[i];

    int src, des;
    cin >> src >> des;

    vector<bool> vis(nodes, false);
    bool result = isReachableNode(nodes, edges, src, des, vis);

    cout << "The given two nodes are Reachable or Not -->" << (result ? "true" : "false") << endl;

    return 0;
}
