#include <iostream>
#include <vector>
#include <queue>
#include <limits>
using namespace std;

void dijkstra(const vector<int>& edges, int nodes, int src, vector<int>& wt) {
    vector<bool> vis(nodes, false);
    priority_queue<pair<int, int>, vector<pair<int, int>>, greater<pair<int, int>>> pq;

    pq.push(make_pair(src, 0));
    vis[src] = true;

    while (!pq.empty()) {
        pair<int, int> top = pq.top(); pq.pop();
        int node = top.first;
        int dist = top.second;
        wt[node] = dist;

        if (edges[node] != -1) {
            int adj = edges[node];
            if (adj >= 0 && adj < nodes && !vis[adj]) {
                vis[adj] = true;
                pq.push(make_pair(adj, dist + 1));
            }
        }
    }
}

int shortestDistance(int nodes, const vector<int>& edges, int src, int des) {
    vector<int> wt(nodes, -1);
    dijkstra(edges, nodes, src, wt);
    return wt[des];
}

int main() {
    int nodes;
    cin >> nodes;

    vector<int> edges(nodes);
    for (int i = 0; i < nodes; ++i)
        cin >> edges[i];

    int src, des;
    cin >> src >> des;

    int result = shortestDistance(nodes, edges, src, des);
    cout << "Shortest Time to reach src to Destination is -->" << result << endl;

    return 0;
}
