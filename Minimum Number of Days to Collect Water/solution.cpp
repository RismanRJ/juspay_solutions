#include <iostream>
#include <vector>
using namespace std;

typedef long long ll;

bool canCollectInDays(ll days, ll W, const vector<int>& warr, const vector<int>& refill) {
    ll total = 0;
    int n = warr.size();
    
    for (int i = 0; i < n; ++i) {
        ll uses = days / refill[i] + 1;
        total += uses * (ll)warr[i];
        if (total >= W) return true; // Early exit if already enough
    }

    return total >= W;
}

ll minDaysToCollectWater(ll W, const vector<int>& warr, const vector<int>& refill) {
    ll low = 0, high = 1e18, ans = -1;

    while (low <= high) {
        ll mid = low + (high - low) / 2;
        
        if (canCollectInDays(mid, W, warr, refill)) {
            ans = mid;
            high = mid - 1; // Try smaller day
        } else {
            low = mid + 1;  // Need more days
        }
    }

    return ans;
}

int main() {
    ll W = 20;
    vector<int> warr = {3, 5};
    vector<int> refill = {1, 2};

    cout << "Minimum days: " << minDaysToCollectWater(W, warr, refill) << endl;
    return 0;
}
