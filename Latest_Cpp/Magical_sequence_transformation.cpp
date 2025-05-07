#include <bits/stdc++.h>
using namespace std;

int main() {
   string s;
cin>>s;

stack<char> st; 
int cnt = 0; 

for(int i=0; i<s.size(); i++){

    if(st.empty())st.push(s[i]); 
    
    else if(s[i] == 'X'){
        
        if(st.top() == 'Y'){
            st.pop();
            st.push('Z');
            st.push('Y');
            cnt++;
        }
        else{
            st.push(s[i]);
        }
    }
    else if(s[i] == 'Y'){
        
        if(st.top() == 'X'){
            while(!st.empty() && st.top() == 'X'){
                st.pop(); 
                cnt++;
            }
            st.push('Y');
            st.push('Z');
        }
        else{
            st.push(s[i]);
        }
    }
}   
cout<<cnt; 
}
