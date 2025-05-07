#include <iostream>
#include <vector>
#include <algorithm>
using namespace std;

bool place(const vector<vector<long long>>& arr, long long val, int n) {
    long long cur = arr[0][0]; // Place the first stall at the start of the first interval
    for (int i = 1; i < n; i++) {
        // Place the stall at the maximum of current position + distance or the start of the next interval
        cur = max(cur + val, arr[i][0]);

        // If the stall is outside the current interval, return false
        if (cur > arr[i][1]) {
            return false;
        }
    }
    return true; // All stalls placed successfully
}

int main() {
    int n;
    cin >> n;

    vector<vector<long long>> arr(n, vector<long long>(2));

    long long low = LLONG_MAX, high = LLONG_MIN;

    for (int i = 0; i < n; i++) {
        cin >> arr[i][0] >> arr[i][1];

        low = min(low, min(arr[i][0], arr[i][1]));
        high = max(high, max(arr[i][0], arr[i][1]));
    }

    // Sort intervals by the starting position
    sort(arr.begin(), arr.end());

    while (low < high) {
        long long mid = (low + high + 1) / 2; // Binary search mid value

        if (place(arr, mid, n)) {
            low = mid; // If it's possible to place all stalls, try for a larger minimum distance
        } else {
            high = mid - 1; // Otherwise, reduce the distance
        }
    }

    cout << low << endl;

    return 0;
}
