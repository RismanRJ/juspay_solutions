#include <bits/stdc++.h>
using namespace std;

class Node {
public:
    string name;
    Node* parent;
    int lockedBy;
    vector<Node*> children;
    bool isLocked;
    unordered_set<Node*> lockedDescendants;

    Node(Node* par, string nm) {
        name = nm;
        parent = par;
        lockedBy = -1;
        isLocked = false;
    }

    void addChild(vector<string>& arr, unordered_map<string, Node*>& nameToNode) {
        for (auto child : arr) {
            children.push_back(new Node(this, child));
            nameToNode[child] = children.back();
        }
    }
};

class NaryTree {
    Node* root;
    unordered_map<string, Node*> nameToNodemapping;

public:
    NaryTree(string name) {
        root = new Node(nullptr, name);
        nameToNodemapping[name] = root;
    }

    void buildTree(vector<string>& arr, int nodes, int m) {
        queue<Node*> q;
        q.push(root);
        int k = 1; // because already first element is taken as root
        int n = arr.size();

        while (!q.empty()) {
            Node* r = q.front();
            q.pop();

            nameToNodemapping[r->name] = r; // mapping the name to the node
            vector<string> temp;

            for (int i = k; i < min(n, k + m); i++) {
                temp.push_back(arr[i]);
            }

            r->addChild(temp, nameToNodemapping);

            for (Node* child : r->children) {
                q.push(child);
            }

            k += m;
        }
    }

    void updateParents(Node* curr) {
        // updating the parent by who are all the descendants are locked
        Node* par = curr->parent;
        while (par) {
            par->lockedDescendants.insert(curr);
            par = par->parent;
        }
    }

    bool lock(string node, int userId) {
        Node* curr = nameToNodemapping[node];

        if (curr->isLocked || !curr->lockedDescendants.empty()) return false; // ensure that node is not locked and has no descendants locked

        Node* par = curr->parent;

        while (par) { // checking for all the parents to ensure no ancestors are locked
            if (par->isLocked) return false;
            par = par->parent;
        }

        updateParents(curr);

        curr->isLocked = true;
        curr->lockedBy = userId;

        return true;
    }

    bool unlock(string node, int userId) {
        Node* curr = nameToNodemapping[node];
        if (!curr->isLocked || curr->lockedBy != userId) return false;

        Node* par = curr->parent;

        while (par) {
            par->lockedDescendants.erase(curr); // removing all the descendants from the parent
            par = par->parent;
        }

        curr->isLocked = false;
        curr->lockedBy = -1;

        return true;
    }

    bool upgrade(string node, int userId) {
        Node* curr = nameToNodemapping[node];

        if (curr->isLocked || curr->lockedDescendants.size() == 0) return false;

        for (auto descendants : curr->lockedDescendants) {
            if (descendants->lockedBy != userId) return false;
        }

        Node* par = curr->parent;

        while (par) {
            if (par->isLocked) return false;
            par = par->parent;
        }

        unordered_set<Node*> tempSt = curr->lockedDescendants;

        for (auto descendants : tempSt) {
            unlock(descendants->name, userId); // unlock all the descendants' lock
        }

        lock(node, userId); // lock the current node

        return true;
    }

    bool performOperations(int operation, string node, int userId) {
        bool result;
        switch (operation) {
            case 1:
                result = lock(node, userId);
                break;
            case 2:
                // unlock
                result = unlock(node, userId);
                break;
            case 3:
                // upgrade
                result = upgrade(node, userId);
                break;
            default:
                return false;
        }

        return result;
    }
};

int main() {
    int nodes, child, queries;

    cin >> nodes >> child >> queries;

    vector<string> arr(nodes);

    for (int i = 0; i < nodes; i++) cin >> arr[i];

    NaryTree* root = new NaryTree(arr[0]);

    root->buildTree(arr, nodes, child);

    for (int i = 0; i < queries; i++) {
        int operation;
        string node;
        int userId;
        cin >> operation >> node >> userId;
        bool result =root->performOperations(operation, node, userId);
        cout<<result<<endl;
    }
}
