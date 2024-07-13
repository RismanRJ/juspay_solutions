import java.util.Scanner;

public class largestSumCycle{
    static int mx=-1;
    public static void findLargeSumCycle(int n , 
    int[] Edges,boolean vis[],boolean visPath[],int node){

            vis[node]=true;
            visPath[node]=true;
        if(Edges[node]!=-1){  // Check --> if it is an exit point
            int adj= Edges[node];  // get the next connected node
            if(!vis[adj]){  //--> check if it is visited or not
                    findLargeSumCycle(n, Edges, vis, visPath, adj);  // recursive call 
            }
                else if(visPath[adj]==true){  //--> if already visited and also if 
                                             //it is in path ==> the check for loop exist or not
                    int sum=0;
                    int parent= adj; // track the parent
                    do{
                        sum++;
                        parent= Edges[parent];  // update the parent with --> next connected node
                    }while(parent!=adj);  // loop until parent and curr node matches

                    mx= Math.max(sum,mx);  //update the max count
                }
            }
        
        visPath[node]= false;  // backtrack all the visited path --> for future recursions
            

        

    }


    public static void main(String[] args) {
        Scanner sc= new Scanner(System.in);
        int n = sc.nextInt();
        int Edges[] = new int[n];
        for(int i=0;i<n;i++)
            Edges[i]= sc.nextInt();

        boolean vis[] = new boolean[n];
        boolean visPath[] = new boolean[n];

        
        for(int i=0;i<n;i++){
            if(Edges[i]!=-1){
                if(!vis[i])
                    findLargeSumCycle(n, Edges,vis,visPath,i);
            }
        }
        System.out.println("Maximum Largest sum cycle -->"+ mx);
            sc.close();
    }
}