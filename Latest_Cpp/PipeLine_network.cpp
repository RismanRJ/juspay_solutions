#include <iostream>
#include <vector>
#include <algorithm>
#include <cmath>

using namespace std;

const int MAXN = 100005;
const int LOG = 20; // Log base 2 of MAXN (for binary lifting)

int n, root, q;
vector<pair<int, int>> adj[MAXN]; // adjacency list for the tree
int depth[MAXN], parent[MAXN][LOG], cost_from_root[MAXN];
int edge_id[MAXN]; // maps nodes to edge id

// Segment Tree for edge costs
vector<int> segtree(4 * MAXN, 0);

void dfs(int node, int par, int dep, int cost) {
    parent[node][0] = par;
    depth[node] = dep;
    cost_from_root[node] = cost;
    for (auto &[next, c] : adj[node]) {
        if (next != par) {
            dfs(next, node, dep + 1, cost + c);
        }
    }
}

// Binary lifting for LCA
void preprocess_lca(int n) {
    for (int j = 1; j < LOG; ++j) {
        for (int i = 1; i <= n; ++i) {
            if (parent[i][j - 1] != -1) {
                parent[i][j] = parent[parent[i][j - 1]][j - 1];
            }
        }
    }
}

// LCA Function
int lca(int u, int v) {
    if (depth[u] < depth[v]) swap(u, v);
    
    // Lift u to same depth as v
    for (int i = LOG - 1; i >= 0; --i) {
        if (depth[u] - (1 << i) >= depth[v]) {
            u = parent[u][i];
        }
    }
    
    if (u == v) return u;
    
    // Lift both u and v
    for (int i = LOG - 1; i >= 0; --i) {
        if (parent[u][i] != parent[v][i]) {
            u = parent[u][i];
            v = parent[v][i];
        }
    }
    
    return parent[u][0];
}

int query_cost(int u, int v) {
    int common_ancestor = lca(u, v);
    return cost_from_root[u] + cost_from_root[v] - 2 * cost_from_root[common_ancestor];
}

void update_edge(int i, int new_cost) {
    // Placeholder: Update the segment tree or similar structure to reflect new cost
}

int main() {
    // Read input and build the graph
    cin >> n >> root >> q;
    
    for (int i = 0; i < n - 1; i++) {
        int u, v, cost;
        cin >> u >> v >> cost;
        adj[u].push_back({v, cost});
        adj[v].push_back({u, cost});
    }
    
    // Preprocess LCA and initial DFS
    dfs(root, -1, 0, 0);
    preprocess_lca(n);
    
    // Process queries
    while (q--) {
        int query_type;
        cin >> query_type;
        
        if (query_type == 1) {
            int a, b;
            cin >> a >> b;
            cout << query_cost(a, b) << endl;
        } else if (query_type == 2) {
            int i, j, new_cost;
            cin >> i >> j >> new_cost;
            update_edge(i, new_cost); // Update the maintenance cost
        }
    }
    
    return 0;
}
