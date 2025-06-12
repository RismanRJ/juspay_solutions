#include<bits/stdc++.h>
using namespace std;

class Node{
public:
	string name;
	int lockedBy;
	int isLockedcnt;
	set<Node*> descendants;
	Node* parent;
	vector<Node*> children;

	Node(Node* Parent, string Name){
		name = Name;
		parent = Parent;
		lockedBy =-1;
		isLocked = 0;
       
	}

	 void add(vector<string>& arr){
		for(auto child : arr){
			children.push_back(new Node(this,child));
		}
	 }
};


class Tree{
public:
	Node* root;
	unordered_map<string, Node*> mpp;

	Tree(string name){
		root = new Node(nullptr,name);
	}

	void updateParents(Node*parent ,Node* node){
		
		while(parent){
			parent->descendants.insert(node);
			parent= parent->parent;
		}
	}
india, 1  || india, 2

        world 
   asia         america
india china    nyc chicago
	bool lockNode(string node, int userId){
		Node* curr = mpp[node];
      0 -> 1 ==> 1 =>>2
         curr->isLockedcnt++;

      if(curr->isLockedcnt>1) // there might be a two thread accessed it
      {
        return false;
      }
		if(curr->isLockedcnt || curr->descendants.size()) return false;

		Node* parentNode = curr->parent;

		while(parentNode){
			if(parentNode->isLockedCnt>0){ // might collision happen
              curr->isLockedCnt --;
              Node currPar = curr->parent;

              while(currPar!=parentNode){
                  currPar->descendants.erase(curr); // removing the child - roll back
                  currPar = currPar ->parent;
              }
      
               return false;
            }
            parertNode->descendants.insert(curr); // updating the parent with descendants;
			parentNode= parentNode->parent;
		}
1 & 2
		// updateParents(curr->parent,curr);

		curr->isLocked=true; // critical section
		curr->lockedBy= userId;  // 1 or 2  2 or 1

		return true;

	}

	bool unlockNode(string node , int userId){
		Node* curr = mpp[node];

		if(!curr->isLocked || curr->lockedBy!=userId) return false;

		Node* parentNode = curr->parent;

		while(parentNode){
			parentNode->descendants.erase(curr);
			parentNode= parentNode->parent;
		}

		curr->isLocked=false;
		curr->lockedBy =1;


		return true;

	}

	bool upgradNode(string node, int userId){
		Node* curr = mpp[node];

		if(curr->isLocked || curr->descendants.size()==0) return false;

		for(auto child : curr->descendants){
			if(child->lockedBy!=userId) return false;
		}

		Node* parentNode = curr->parent;

		while(parentNode){
			if(parentNode->isLocked) return false;

			parentNode = parentNode->parent;
		}


	auto tempSt = curr->descendants;

	for(auto it : tempSt){
		unlockNode(it->name,  userId);
	}

	return lockNode( node, userId);

	}


	void buildTree(vector<string>& arr, int m){
		queue<Node*> q;
		q.push(root);
		int k =1;
		int i;
		int n = arr.size();

		while(!q.empty()){
			Node* curr = q.front();
			q.pop();
			mpp[curr->name]= curr;

			vector<string> tempArr;


			for(i=k;i<min(n,k+m);i++){
				tempArr.push_back(arr[i]);
			}

			curr->add(tempArr);


			for(auto child : curr->children){
				q.push(child);
			}

			k =i;
		}
	}

	bool performOperations(int operationId, string node, int userId){
		bool res;
		switch (operationId) {
			case 1: 
				res = lockNode( node, userId);
				break;
			case 2:
				res = unlockNode(node, userId);
				break;
			case 3: 
				res = upgradNode(node, userId);
				break;
			default:
				res= false;
				break;
		}
		return res;
	}

};


int main(){
	int nodes;
	int child;
	int queries;
	cin>>nodes>>child>>queries;

	vector<string> arr(nodes);

	for(int i=0;i<nodes;i++) cin>> arr[i];

	Tree* rootTree = new Tree(arr[0]);

	rootTree->buildTree(arr, child);


	while(queries-->0){
		int operationId;
		string nodeName;
		int userId;
		cin>>operationId>>nodeName>>userId;
		if(rootTree->performOperations(operationId, nodeName, userId)){
			cout<<"true"<<endl;
		}
		else{
			cout<<"false"<<endl;
		}
	}
}


// exmaple:
     


// test case 1: 
lock-> (india, 1) - > true
lock -> (india, 2) -> false


expected result:
  yes , no  or no , yes

in a multi core -> yes, yes

