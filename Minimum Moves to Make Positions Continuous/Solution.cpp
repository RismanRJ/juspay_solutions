#include <iostream>
#include <vector>
#include <unordered_set>
#include <algorithm>
using namespace std;

int minMovesToMakeContinuous(vector<int>& positions) {
    int n = positions.size();

    // Step 1: Sort and remove duplicates
    sort(positions.begin(), positions.end());
    vector<int> unique_positions;
    unique_positions.push_back(positions[0]);
    
    for (int i = 1; i < n; ++i) {
        if (positions[i] != positions[i - 1]) {
            unique_positions.push_back(positions[i]);
        }
    }

    int maxWindow = 0;
    int left = 0;

    // Step 2: Sliding window over unique_positions
    for (int right = 0; right < unique_positions.size(); ++right) {
        while (unique_positions[right] - unique_positions[left] >= n) {
            left++;
        }
        maxWindow = max(maxWindow, right - left + 1);
    }

    return n - maxWindow;
}

int main() {
    vector<int> positions = {4, 2, 7, 7, 3};
    cout << minMovesToMakeContinuous(positions) << endl; // Output: 2
    return 0;
}
