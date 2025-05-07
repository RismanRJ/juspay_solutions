#include <bits/stdc++.h>
using namespace std;
#define INF INT_MAX

vector<long long> Dijkstra(vector<vector<int>>& adj, int s)
{
    priority_queue<pair<long, long>, vector<pair<long, long>>, greater<pair<long, long>>> pq;
    int v = adj.size();
    vector<long long> ans(v, INF);
    ans[s] = 0;
    pq.push({0, s});
    while (!pq.empty())
    {
        long long dist = pq.top().first;
        int node = pq.top().second;
        pq.pop();

        for (auto i : adj[node])
        {
            if (dist + 1 < ans[i])
            {
                ans[i] = dist + 1;
                pq.push({dist + 1, i});
            }
        }
    }
    return ans;
}

int minimumWeight(int n, vector<int>& edges, int C1, int C2)
{
    vector<vector<int>> graph(n);
    for (int i = 0; i < n; i++)
    {
        if (edges[i] != -1)
            graph[i].push_back(edges[i]);
    }

    // Create two arrays A and B for storing min distance from C1 and C2
    vector<long long> A(n, INF), B(n, INF);

    // Part 1 and Part 2 of Algo -> Implement a Dijkstra function and call it for both arrays A and B
    A = Dijkstra(graph, C1);
    B = Dijkstra(graph, C2);

    // Now comes Part 3 part of algo-> loop through and get node with min(A[i]+B[i])
    int node = -1;
    long long dist = INF;
    for (int i = 0; i < n; ++i)
    {
        // If node is not accessible from any of them, ignore it
        if (A[i] == INF || B[i] == INF)
            continue;
        if (dist > A[i] + B[i])
        {
            dist = A[i] + B[i];
            node = i;
        }
    }
    return node;
}

int main()
{
    int n;
    cin >> n;

    vector<int> edges(n);
    for (int i = 0; i < n; ++i)
    {
        cin >> edges[i];
    }

    int C1, C2;
    cin >> C1 >> C2;

    int nearestMeetingCell = minimumWeight(n, edges, C1, C2);
    cout << nearestMeetingCell << endl;

    return 0;
}
