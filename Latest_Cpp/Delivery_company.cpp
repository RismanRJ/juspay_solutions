#include <bits/stdc++.h>
using namespace std;

int solution1(vector<int> arr) {
    int cnt = 0;
    int n = arr.size();
    unordered_set<int> st;
    for (int i = 0; i < arr.size(); i++) {
        if (arr[i] < n) st.insert(arr[i]);
    }

    return st.size();
}

int solution2(vector<int> arr) {
    int n = arr.size();
    sort(arr.begin(), arr.end());
    unordered_map<int, int> mpp;
    int key = n - arr[n - 1];
    arr[n - 1] += key;
    bool flag =false;
    for (int i = n; i >= 1; i--) {
        if (i != key && flag ==false) {
            arr[n - i] += i;
        }
        else{
            arr[n-i]+= (i-1);
            flag=true;
        }
        mpp[arr[n - i]]++;
    }
    int max_Freq = INT_MIN;
    for (auto [ele, cnt] : mpp) max_Freq = max(cnt, max_Freq);

    return max_Freq;
}

int solution3(vector<int>& arr) {
    int n = arr.size();
    
    // Sort the array of parcels
    sort(arr.begin(), arr.end());
    
    unordered_map<int, int> freq_map;
    // Try adding each permutation element (1 to n) to each element of arr
    for (int i = 0; i < n; i++) {
        int new_val = arr[i] - (i + 1); // Applying the permutation q[i] = i + 1
        freq_map[new_val]++;
    }

    // Find the maximum frequency of any sum
    int max_freq = 0;
    for (auto& entry : freq_map) {
        max_freq = max(max_freq, entry.second);
    }

    return max_freq;
}

int main() {
    int n;
    cin >> n;
    
    vector<int> p(n);

    int maxFreq = 0;

    for (int i = 0; i < n; ++i) {
        cin >> p[i];
    }
    
    maxFreq = solution1(p);
    cout << "solution1 Max Freq " << maxFreq << endl;
    
    maxFreq = solution2(p);
    cout << "solution2 Max Freq " << maxFreq << endl;
    
    maxFreq = solution3(p);
    cout << "solution3 Max Freq " << maxFreq << endl;
    
    return 0;
}
