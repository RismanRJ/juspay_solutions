#include <bits/stdc++.h>
using namespace std;

class TreeNode {
public:
    string name;
    int lockedBy;
    int lockedCount; // replaces isLocked with count-based locking
    TreeNode* parent;
    vector<TreeNode*> childs;
    unordered_set<TreeNode*> lockedDescendents;

    TreeNode(TreeNode* Parent, string Name) {
        name = Name;
        parent = Parent;
        lockedBy = -1;
        lockedCount = 0;
    }

    void addChildren(const vector<string>& arr) {
        for (const auto& child : arr) {
            TreeNode* newChild = new TreeNode(this, child);
            childs.push_back(newChild);
        }
    }
};

class Tree {
public:
    TreeNode* root;
    unordered_map<string, TreeNode*> nameToNode;

    Tree(string rootName) {
        root = new TreeNode(nullptr, rootName);
    }

    void buildTree(vector<string>& arr, int m){
		queue<TreeNode*> q;
		q.push(root);
		int k =1;
		int i;
		int n = arr.size();

		while(!q.empty()){
			TreeNode* curr = q.front();
			q.pop();
			nameToNode[curr->name]= curr;

			vector<string> tempArr;


			for(i=k;i<min(n,k+m);i++){
				tempArr.push_back(arr[i]);
			}

			curr->addChildren(tempArr);


			for(auto child : curr->childs){
				q.push(child);
			}

			k =i;
		}
	}

    bool lock(string name, int id) {
        TreeNode* node = nameToNode[name];
        if (node->lockedCount > 0 || !node->lockedDescendents.empty()) return false;

        node->lockedCount++;
        if (node->lockedCount > 1) {
            node->lockedCount--;
            return false;
        }

        TreeNode* parent = node->parent;
        while (parent) {
            if (parent->lockedCount > 0 || !node->lockedDescendents.empty()) {
                node->lockedCount--;
                TreeNode* rollback = node->parent;
                while (rollback != parent) {
                    rollback->lockedDescendents.erase(node);
                    rollback = rollback->parent;
                }
                return false;
            }
            parent->lockedDescendents.insert(node);
            parent = parent->parent;
        }

        node->lockedBy = id;
        return true;
    }
    
        bool unlock(string name, int id) {
        TreeNode* node = nameToNode[name];
        if (node->lockedCount == 0 || node->lockedBy != id) return false;

        node->lockedCount = 0;
        node->lockedBy = -1;

        vector<TreeNode*> rollbackAncestors;
        TreeNode* parent = node->parent;

        while (parent) {
            if (parent->lockedDescendents.find(node) == parent->lockedDescendents.end()) {
                // Rollback
                node->lockedCount = 1;
                node->lockedBy = id;
                for (auto p : rollbackAncestors) {
                    p->lockedDescendents.insert(node);
                }
                return false;
            }
            rollbackAncestors.push_back(parent);
            parent->lockedDescendents.erase(node);
            parent = parent->parent;
        }

        return true;
    }

    bool upgrade(string name, int id) {
        TreeNode* node = nameToNode[name];

        if (node->lockedCount > 0 || node->lockedDescendents.empty()) return false;

        for (TreeNode* desc : node->lockedDescendents) {
            if (desc->lockedBy != id) return false;
        }

        TreeNode* ancestor = node->parent;
        while (ancestor) {
            if (ancestor->lockedCount > 0) return false;
            ancestor = ancestor->parent;
        }

        vector<TreeNode*> toUnlock(node->lockedDescendents.begin(), node->lockedDescendents.end());
        vector<TreeNode*> successfullyUnlocked;

        for (TreeNode* desc : toUnlock) {
            if (!unlock(desc->name, id)) {
                // Rollback
                for (TreeNode* relockNode : successfullyUnlocked) {
                    lock(relockNode->name, id);
                }
                return false;
            }
            successfullyUnlocked.push_back(desc);
        }

        if (!lock(name, id)) {
            // Re-lock previously unlocked nodes
            for (TreeNode* relockNode : successfullyUnlocked) {
                lock(relockNode->name, id);
            }
            return false;
        }

        return true;
    }


    bool performOperations(int operationId, string node, int userId) {
        switch (operationId) {
            case 1: return lock(node, userId);
            case 2: return unlock(node, userId);
            case 3: return upgrade(node, userId);
            default: return false;
        }
    }
};

int main() {
    int nodes, childrenPerNode, queries;
    cin >> nodes >> childrenPerNode >> queries;

    vector<string> nodeNames(nodes);
    for (int i = 0; i < nodes; ++i) {
        cin >> nodeNames[i];
    }

    Tree* tree = new Tree(nodeNames[0]);
    tree->buildTree(nodeNames, childrenPerNode);

    while (queries-- > 0) {
        int op;
        string nodeName;
        int userId;
        cin >> op >> nodeName >> userId;
        cout << (tree->performOperations(op, nodeName, userId) ? "true" : "false") << endl;
    }

    return 0;
}
