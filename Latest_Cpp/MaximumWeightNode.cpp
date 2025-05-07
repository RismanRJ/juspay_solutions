int maxWeightCell(int n, vector<int> edge){
    vector<int> cnt(n,0);
    for(int i=0; i<n; i++){
        if(edge[i] != -1)
            cnt[edge[i]] += i;
    }
    int ans = -1;
    int maxi = INT_MIN;
    for(int i=0; i<n; i++){
        if(maxi <= cnt[i]){
            maxi = cnt[i];
            ans = i;
        }
    }
    return ans;
  }
