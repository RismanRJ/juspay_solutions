import java.util.Arrays;
import java.util.PriorityQueue;
import java.util.Scanner;

class pair {
    int node;
    int distance;

    pair(int node, int distance) {
        this.node = node;
        this.distance = distance;
    }
}

public class nearestMeetingCell {
    public static void dijikstra(int n, int[] Edges, int c, int[] wt) {

        PriorityQueue<pair> q = new PriorityQueue<pair>((x, y) -> x.distance - y.node);
        boolean vis[] = new boolean[n];

        vis[c] = true; // --> marking that cell as visited

        q.add(new pair(c, 0));  //--> starting cell has always weight ==> 0
      
        while (!q.isEmpty()) {
            int dist = q.peek().distance; //--> calculatin the distance
            int node = q.peek().node; // --> getting the node
            q.remove();
            wt[node] = dist; // update the distance to weighted array of given cell

            // System.out.println("For cell-->"+ c);
            // System.out.println(node +"-->" + wt[node]);

            if (Edges[node] != -1) {   //--> check if is not an exit point
                int adj = Edges[node];  // --> get the next connected node
                if (!vis[adj]) {
                    vis[adj]=true;
                    q.add(new pair(adj, dist + 1));  // update the distance by 1 becaue it is an unweighted graph
                }
            }
        }
    }

    public static int findNearestCell(int n, int[] Edges, int c1, int c2) {
        int node = -1;
        int[] cellWt1 = new int[n];
        int[] cellWt2 = new int[n];

        Arrays.fill(cellWt1, -1);
        Arrays.fill(cellWt2, -1);

        dijikstra(n, Edges, c1, cellWt1);
        dijikstra(n, Edges, c2, cellWt2);

        int maxDist= Integer.MAX_VALUE;
        for(int i=0;i<n;i++){
            if(cellWt1[i]==-1|| cellWt2[i]==-1) continue;

            int dist =  cellWt1[i]+cellWt2[i];
            if(maxDist>dist){
                maxDist=dist;  // --> update the max Distance
                node=i;  // update the node
            }
        }
        return node;
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int n = sc.nextInt();
        int Edges[] = new int[n];
        for (int i = 0; i < n; i++)
            Edges[i] = sc.nextInt();

        int cell1 = sc.nextInt();
        int cell2 = sc.nextInt();

        int nearestNode = findNearestCell(n, Edges, cell1, cell2);

        System.out.println("Nearest Meeting point between cell1 and cell 2 is -->" + nearestNode);
        sc.close();
    }
}


// Time complexity ==>  O(n)+ O(n) + O(n) --> O(n)
//space complexity ==>  O(n)+ O(n) + O(n)  --> O(n)
