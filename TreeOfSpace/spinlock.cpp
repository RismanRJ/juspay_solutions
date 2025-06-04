#include <bits/stdc++.h>
using namespace std;

class Spinlock {
    volatile int flag = 0;
public:
    void lock() {
        while (__sync_lock_test_and_set(&flag, 1));
    }
    void unlock() {
        __sync_lock_release(&flag);
    }
};

class Node {
public:
    string name;
    Node* parent;
    int lockedBy;
    vector<Node*> children;
    bool isLocked;
    unordered_set<Node*> lockedDescendants;
    Spinlock spinlock;

    Node(Node* par, string nm) {
        name = nm;
        parent = par;
        lockedBy = -1;    // set default unlocked userId to -1
        isLocked = false;
    }

    void addChild(const vector<string>& arr) {
        for (auto& child : arr) {
            children.push_back(new Node(this, child));
        }
    }
};

class NaryTree {
    Node* root;
    unordered_map<string, Node*> nameToNodemapping;

public:
    NaryTree(string name) {
        root = new Node(nullptr, name);
    }

    void buildTree(const vector<string>& arr, int nodes, int m) {
        queue<Node*> q;
        q.push(root);
        int k = 1;  // root already taken
        int n = arr.size();
        while (!q.empty()) {
            Node* r = q.front();
            q.pop();

            nameToNodemapping[r->name] = r;  // map name to node

            vector<string> temp;
            for (int i = k; i < min(n, k + m); i++) {
                temp.push_back(arr[i]);
            }
            k += (int)temp.size();

            r->addChild(temp);

            for (Node* child : r->children) {
                q.push(child);
            }
        }
    }

       void updateParents(Node* curr, Node* par) {
        // Insert locked node curr into lockedDescendants of all its ancestors
        while (par) {
            par->spinlock.lock();
            par->lockedDescendants.insert(curr);  // **FIXED: insert curr, not par**
            par->spinlock.unlock();
            par = par->parent;
        }
    }


    bool lock(string node, int userId) {
        Node* curr = nameToNodemapping[node];
        curr->spinlock.lock();

        // Node must not be locked and no locked descendants
        if (curr->isLocked || !curr->lockedDescendants.empty()) {
            curr->spinlock.unlock();
            return false;
        }

        // Check if any ancestor is locked
        Node* par = curr->parent;
        while (par) {
            par->spinlock.lock();
            if (par->isLocked) {
                par->spinlock.unlock();
                curr->spinlock.unlock();
                return false;
            }
            par->spinlock.unlock();
            par = par->parent;
        }

        // Update lockedDescendants for ancestors
        updateParents(curr, curr->parent);

        curr->isLocked = true;
        curr->lockedBy = userId;
        curr->spinlock.unlock();
        return true;
    }

    bool unlock(string node, int userId) {
        Node* curr = nameToNodemapping[node];
        curr->spinlock.lock();

        if (!curr->isLocked || curr->lockedBy != userId) {
            curr->spinlock.unlock();
            return false;
        }

        // Remove curr from lockedDescendants of all ancestors
        Node* par = curr->parent;
        while (par) {
            par->spinlock.lock();
            par->lockedDescendants.erase(curr);
            par->spinlock.unlock();
            par = par->parent;
        }

        curr->isLocked = false;
        curr->lockedBy = -1;
        curr->spinlock.unlock();

        return true;
    }

   bool upgrade(string node, int userId) {
    Node* curr = nameToNodemapping[node];
    curr->spinlock.lock();

    // Node must be unlocked and have at least one locked descendant
    if (curr->isLocked || curr->lockedDescendants.empty()) {
        curr->spinlock.unlock();
        return false;
    }

    // Check all locked descendants belong to userId
    for (auto desc : curr->lockedDescendants) {
        if (desc->lockedBy != userId) {
            curr->spinlock.unlock();
            return false;
        }
    }

    // Check no ancestor is locked
    Node* par = curr->parent;
    while (par) {
        par->spinlock.lock();
        bool locked = par->isLocked;
        par->spinlock.unlock();

        if (locked) {
            curr->spinlock.unlock();
            return false;
        }
        par = par->parent;
    }

    // Copy locked descendants to unlock after releasing curr lock
    vector<Node*> descendantsToUnlock(curr->lockedDescendants.begin(), curr->lockedDescendants.end());

    curr->spinlock.unlock();

    // Unlock all locked descendants (without holding curr lock)
    for (Node* desc : descendantsToUnlock) {
        unlock(desc->name, userId);
    }

    // Lock current node (also no curr lock held)
    return lock(node, userId);
}


    bool performOperations(int operation, string node, int userId) {
        bool result;
        switch (operation) {
            case 1:
                result = lock(node, userId);
                break;
            case 2:
                result = unlock(node, userId);
                break;
            case 3:
                result = upgrade(node, userId);
                break;
            default:
                return false;
        }
        return result;
    }
};

int main() {
    ios::sync_with_stdio(false);
    cin.tie(nullptr);

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
        cout << (root->performOperations(operation, node, userId) ? "true\n" : "false\n");
    }
}
