// Solution 2: Tarjan’s Algorithm for Articulation Points
#include <bits/stdc++.h>
using namespace std;

const int MAX = 2e5 + 5;
vector<int> adj[2 * MAX];
int tin[2 * MAX], low[2 * MAX], vis[2 * MAX], timer = 0;
int componentCount = 0;

void dfs(int node, int parent, vector<int>& articulationCount) {
    vis[node] = 1;
    tin[node] = low[node] = ++timer;
    int children = 0;

    for (int neigh : adj[node]) {
        if (neigh == parent) continue;
        if (!vis[neigh]) {
            dfs(neigh, node, articulationCount);
            low[node] = min(low[node], low[neigh]);

            if (low[neigh] >= tin[node] && parent != -1) {
                articulationCount[node]++;
            }

            children++;
        } else {
            low[node] = min(low[node], tin[neigh]);
        }
    }

    if (parent == -1 && children > 1) {
        articulationCount[node]++;
    }
}

int main() {
    ios::sync_with_stdio(false);
    cin.tie(nullptr);

    int n;
    cin >> n;
    string a, b;
    cin >> a >> b;

    auto getId = [&](int row, int col) {
        return row * n + col;
    };

    int totalNodes = 2 * n;
    int blocked = 0;
    for (int i = 0; i < n; i++) {
        if (a[i] == '.' && b[i] == '.') {
            int u = getId(0, i);
            int v = getId(1, i);
            adj[u].push_back(v);
            adj[v].push_back(u);
        }
        if (i + 1 < n) {
            if (a[i] == '.' && a[i + 1] == '.') {
                int u = getId(0, i);
                int v = getId(0, i + 1);
                adj[u].push_back(v);
                adj[v].push_back(u);
            }
            if (b[i] == '.' && b[i + 1] == '.') {
                int u = getId(1, i);
                int v = getId(1, i + 1);
                adj[u].push_back(v);
                adj[v].push_back(u);
            }
        }
        if (a[i] == 'x') blocked++;
        if (b[i] == 'x') blocked++;
    }

    vector<int> articulationCount(2 * n, 0);

    for (int i = 0; i < 2 * n; i++) {
        if (!vis[i] && ((i < n && a[i] == '.') || (i >= n && b[i - n] == '.'))) {
            dfs(i, -1, articulationCount);
            componentCount++;
        }
    }

    int result = 0;
    for (int i = 0; i < 2 * n; i++) {
        if (articulationCount[i] >= 1 && (componentCount + articulationCount[i] == 3)) {
            result++;
        }
    }

    cout << result << endl;
    return 0;
}



// // Solution 1: DFS approach to find critical blocks
// #include <bits/stdc++.h>
// using namespace std;

// const int MAXN = 200005;

// int n;
// string street[2];
// vector<vector<int>> dirs = {{0, 1}, {1, 0}, {-1, 0}, {0, -1}};

// // Unique ID for each cell
// int id(int r, int c) {
//     return r * n + c;
// }

// // Standard DFS to explore connected regions
// void dfs(int r, int c, vector<string>& grid, vector<bool>& vis) {
//     vis[id(r, c)] = true;
//     for (auto& d : dirs) {
//         int nr = r + d[0], nc = c + d[1];
//         if (nr >= 0 && nr < 2 && nc >= 0 && nc < n && grid[nr][nc] == '.' && !vis[id(nr, nc)]) {
//             dfs(nr, nc, grid, vis);
//         }
//     }
// }

// // Count connected components in current grid
// int count_components(vector<string>& grid) {
//     vector<bool> vis(2 * n, false);
//     int count = 0;
//     for (int r = 0; r < 2; ++r) {
//         for (int c = 0; c < n; ++c) {
//             if (grid[r][c] == '.' && !vis[id(r, c)]) {
//                 ++count;
//                 dfs(r, c, grid, vis);
//             }
//         }
//     }
//     return count;
// }

// int main() {
//     ios::sync_with_stdio(false);
//     cin.tie(nullptr);

//     cin >> n;
//     cin >> street[0];
//     cin >> street[1];

//     vector<string> original = {street[0], street[1]};
//     int base_components = count_components(original);

//     int critical_count = 0;
//     for (int r = 0; r < 2; ++r) {
//         for (int c = 0; c < n; ++c) {
//             if (original[r][c] == '.') {
//                 vector<string> copy = original;
//                 copy[r][c] = 'x'; // Temporarily block this cell
//                 int comps = count_components(copy);
//                 if (comps == 3) ++critical_count;
//             }
//         }
//     }

//     cout << critical_count << "\n";
//     return 0;
// }
