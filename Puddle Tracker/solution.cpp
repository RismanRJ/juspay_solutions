#include <bits/stdc++.h>
using namespace std;

class Disjoint{
    vector<int> parent, size;
    public:
        Disjoint(int n){
            parent.resize(n+1);
            size.resize(n+1);
            
            for(int i=0;i<=n;i++){
                parent[i]=i;
                size[i]= 1;
            }
        }
        
    int findUltMtPar(int u){
        if(u == parent[u]) return u;
       return parent[u] = findUltMtPar(parent[u]);
        
    }
    
    
    void unionBySize(int u, int v){
        int ult_pu= findUltMtPar(u);
        int ult_pv= findUltMtPar(v);
        
        if(ult_pv== ult_pu) return;
        
        
        if(size[ult_pu]<size[ult_pv]){
            parent[ult_pu]= ult_pv;
            size[ult_pv]+= size[ult_pu];
        }
        else{
            parent[ult_pv]= ult_pu;
             size[ult_pu]+= size[ult_pv];
        }
    }
    
};

bool isValid(int row, int col, int n , int m){
    return row>=0 && row<n && col>=0 && col<m;
}

int main() {
	int n;
	int drops;
	cin>>n;
	cin>>drops;
	
	vector<vector<int>> arr(n, vector<int>(n,0));
	
    for(int i=0;i<drops;i++){
        int row,col;
        cin>>row>>col;
        arr[row][col]=1;
    }
    
    Disjoint ds(n*drops);
    
    
    int drow[]= {-1,1,0,0};
    int dcol[]={0,0,-1,1};
    
    for(int i=0;i<n;i++){
        for(int j=0;j<drops;j++){
            int dropId = i*n+j;
            if(arr[i][j]==1){
                for(int k=0;k<4;k++){
                int row = drow[k]+i;
                int col = dcol[k]+j;
                
                if(isValid(row,col,n,n) && arr[row][col]==1){
                       int currDropId = row*n + col;
                       
                       if(ds.findUltMtPar(dropId)==ds.findUltMtPar(currDropId)){
                           continue;
                       }
                       else{
                           ds.unionBySize(dropId,currDropId);
                       }
                }
            }
        }
           
            
        }
    }
    int queries;
    cin>>queries;
    
    while(queries--){
        int r1,c1,r2,c2;
        cin>>r1>>c1>>r2>>c2;
        
        
        int firstDropId = r1*n+c1;
        int secondDropId = r2*n+c2;
        
        if(ds.findUltMtPar(firstDropId)==ds.findUltMtPar(secondDropId)){
            cout<<"true"<<endl;
        }
        else{
            cout<<"false"<<endl;
        }
    }
}
