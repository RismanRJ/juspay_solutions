#include <bits/stdc++.h>
#include <mutex>
using namespace std;

class Node {
public:
    string name;
    int lockedBy;
    bool isLocked;
    set<Node*> descendants;
    Node* parent;
    vector<Node*> children;
    mutex mtx;

    Node(Node* Parent, string Name) {
        name = Name;
        parent = Parent;
        lockedBy = -1;
        isLocked = false;
    }

    void add(vector<string>& arr) {
        for (auto& child : arr) {
            children.push_back(new Node(this, child));
        }
    }
};

class Tree {
public:
    Node* root;
    unordered_map<string, Node*> mpp;

    Tree(string name) {
        root = new Node(nullptr, name);
    }

    void updateParents(Node* parent, Node* node) {
        while (parent) {
            parent->mtx.lock();
            parent->descendants.insert(node);
            parent->mtx.unlock();
            parent = parent->parent;
        }
    }

    bool lockNode(string node, int userId) {
        Node* curr = mpp[node];
        curr->mtx.lock();
        if (curr->isLocked || curr->descendants.size()) {
            curr->mtx.unlock();
            return false;
        }

        Node* parentNode = curr->parent;
        while (parentNode) {
            parentNode->mtx.lock();
            if (parentNode->isLocked) {
                parentNode->mtx.unlock();
                curr->mtx.unlock();
                return false;
            }
            parentNode->mtx.unlock();
            parentNode = parentNode->parent;
        }

        updateParents(curr->parent, curr);

        curr->isLocked = true;
        curr->lockedBy = userId;
        curr->mtx.unlock();
        return true;
    }

    bool unlockNode(string node, int userId) {
        Node* curr = mpp[node];
        curr->mtx.lock();
        if (!curr->isLocked || curr->lockedBy != userId) {
            curr->mtx.unlock();
            return false;
        }

        Node* parentNode = curr->parent;
        while (parentNode) {
            parentNode->mtx.lock();
            parentNode->descendants.erase(curr);
            parentNode->mtx.unlock();
            parentNode = parentNode->parent;
        }

        curr->isLocked = false;
        curr->lockedBy = -1;
        curr->mtx.unlock();
        return true;
    }

    bool upgradNode(string node, int userId) {
        Node* curr = mpp[node];
        curr->mtx.lock();
        if (curr->isLocked || curr->descendants.size() == 0) {
            curr->mtx.unlock();
            return false;
        }
        curr->mtx.unlock();

        for (auto child : curr->descendants) {
            child->mtx.lock();
            if (child->lockedBy != userId) {
                child->mtx.unlock();
                return false;
            }
            child->mtx.unlock();
        }

        Node* parentNode = curr->parent;
        while (parentNode) {
            parentNode->mtx.lock();
            if (parentNode->isLocked) {
                parentNode->mtx.unlock();
                return false;
            }
            parentNode->mtx.unlock();
            parentNode = parentNode->parent;
        }

        auto tempSt = curr->descendants;
        for (auto it : tempSt) {
            unlockNode(it->name, userId);
        }

        return lockNode(node, userId);
    }

    void buildTree(vector<string>& arr, int m) {
        queue<Node*> q;
        q.push(root);
        int k = 1;
        int i;
        int n = arr.size();

        while (!q.empty()) {
            Node* curr = q.front();
            q.pop();
            mpp[curr->name] = curr;

            vector<string> tempArr;
            for (i = k; i < min(n, k + m); i++) {
                tempArr.push_back(arr[i]);
            }

            curr->add(tempArr);

            for (auto child : curr->children) {
                q.push(child);
            }

            k = i;
        }
    }

    bool performOperations(int operationId, string node, int userId) {
        bool res;
        switch (operationId) {
            case 1:
                res = lockNode(node, userId);
                break;
            case 2:
                res = unlockNode(node, userId);
                break;
            case 3:
                res = upgradNode(node, userId);
                break;
            default:
                res = false;
                break;
        }
        return res;
    }
};

int main() {
    int nodes;
    int child;
    int queries;
    cin >> nodes >> child >> queries;

    vector<string> arr(nodes);

    for (int i = 0; i < nodes; i++) cin >> arr[i];

    Tree* rootTree = new Tree(arr[0]);

    rootTree->buildTree(arr, child);

    while (queries-- > 0) {
        int operationId;
        string nodeName;
        int userId;
        cin >> operationId >> nodeName >> userId;
        if (rootTree->performOperations(operationId, nodeName, userId)) {
            cout << "true" << endl;
        } else {
            cout << "false" << endl;
        }
    }
}
