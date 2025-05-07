Problem 1 Solution:
#include <bits/stdc++.h>
using namespace std;

set<int> result;

bool dfs(vector<vector<int>>& adj, vector<int>& c, int ele) {
    bool hasCompliant = false;

    for (int child : adj[ele]) {
        if (dfs(adj, c, child)) {
            hasCompliant = true;
        }
    }

    if (c[ele] == 0 || hasCompliant) return true; // This node is compliant → propagate true

    if (!hasCompliant) {
        result.insert(ele); // Only if this is non-compliant and no compliant in subtree
    }

    return false; // This node is non-compliant, and no compliant subtree
}

int main() {
    int n;
    cin >> n;

    vector<vector<int>> grph(n + 1);
    vector<int> p_id(n + 1);
    vector<int> c_id(n + 1);

    int root = -1;

    for (int i = 1; i <= n; i++) {
        int p, c;
        cin >> p >> c;
        p_id[i] = p;
        c_id[i] = c;

        if (p == -1) {
            root = i;
        } else {
            grph[p].push_back(i);
        }
    }

    dfs(grph, c_id, root);

    if (result.empty()) {
        cout << -1 << endl;
    } else {
        if(result.size()==n){
            for(auto it = result.rbegin();it!=result.rend();it++){
                cout<<*it<<" ";
            }
        }
        else{
            for (int id : result) {
            cout << id << " ";
        }
        }
        
        cout << endl;
    }

    return 0;
}
