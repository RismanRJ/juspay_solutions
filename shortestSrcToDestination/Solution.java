package shortestSrcToDestination;

import java.util.*;

class pair {
    int a;
    int b;
    int c;

    pair(int a, int b, int c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }
}

class Solution {
    int shortestDistance(int N, int M, int A[][], int X, int Y) {
        if (A[0][0] == 0)
            return -1;
        Queue<pair> q = new ArrayDeque<pair>();

        q.add(new pair(0, 0, 0));
        int ROW[] = { 0, 0, -1, 1 };
        int COL[] = { -1, 1, 0, 0 };

        while (!q.isEmpty()) {
            int row = q.peek().a;
            int col = q.peek().b;
            int steps = q.peek().c;
            q.remove();
            if (row == X && col == Y)
                return steps;
            for (int i = 0; i < 4; i++) {
                int r = row + ROW[i];
                int c = col + COL[i];

                if (r >= 0 && r < N && c >= 0 && c < M && A[r][c] == 1) {
                    A[r][c] = 0;
                    q.add(new pair(r, c, steps + 1));
                }

            }
        }
        return -1;
    }
};
