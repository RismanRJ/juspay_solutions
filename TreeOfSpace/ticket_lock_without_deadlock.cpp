#include <bits/stdc++.h>
using namespace std;

class TicketLock {
    atomic<int> next_ticket{0};
    atomic<int> now_serving{0};
public:
    void lock() {
        int my_ticket = next_ticket.fetch_add(1);
        while (now_serving.load() != my_ticket);
    }

    void unlock() {
        now_serving.fetch_add(1);
    }
};

class Node {
public:
    string name;
    Node* parent;
    vector<Node*> children;
    int lockedBy;
    bool isLocked;
    int lockedDescendantCount;
    TicketLock lockObj;

    Node(Node* par, string nm) {
        name = nm;
        parent = par;
        lockedBy = -1;
        isLocked = false;
        lockedDescendantCount = 0;
    }

    void addChild(const vector<string>& arr) {
        for (auto& child : arr) {
            children.push_back(new Node(this, child));
        }
    }
};

class NaryTree {
    Node* root;
    unordered_map<string, Node*> nameToNode;

public:
    NaryTree(string name) {
        root = new Node(nullptr, name);
        nameToNode[name] = root;
    }

    void buildTree(const vector<string>& arr, int nodes, int m) {
        queue<Node*> q;
        q.push(root);
        int k = 1;
        int n = arr.size();

        while (!q.empty()) {
            Node* r = q.front(); q.pop();
            vector<string> temp;
            for (int i = k; i < min(n, k + m); i++) {
                temp.push_back(arr[i]);
            }
            k += temp.size();
            r->addChild(temp);
            for (Node* child : r->children) {
                nameToNode[child->name] = child;
                q.push(child);
            }
        }
    }

    void updateAncestors(Node* curr, int delta) {
        Node* par = curr->parent;
        while (par) {
            par->lockObj.lock();
            par->lockedDescendantCount += delta;
            par->lockObj.unlock();
            par = par->parent;
        }
    }

    bool lock(string node, int userId) {
        Node* curr = nameToNode[node];
        curr->lockObj.lock();
        if (curr->isLocked || curr->lockedDescendantCount > 0) {
            curr->lockObj.unlock();
            return false;
        }
        Node* par = curr->parent;
        while (par) {
            par->lockObj.lock();
            if (par->isLocked) {
                par->lockObj.unlock();
                curr->lockObj.unlock();
                return false;
            }
            par->lockObj.unlock();
            par = par->parent;
        }
        curr->isLocked = true;
        curr->lockedBy = userId;
        curr->lockObj.unlock();

        updateAncestors(curr, 1);
        return true;
    }

    bool unlock(string node, int userId) {
        Node* curr = nameToNode[node];
        curr->lockObj.lock();
        if (!curr->isLocked || curr->lockedBy != userId) {
            curr->lockObj.unlock();
            return false;
        }
        curr->isLocked = false;
        curr->lockedBy = -1;
        curr->lockObj.unlock();

        updateAncestors(curr, -1);
        return true;
    }

    void collectLockedDescendants(Node* node, vector<Node*>& out) {
        node->lockObj.lock();
        if (node->isLocked) out.push_back(node);
        node->lockObj.unlock();

        for (Node* child : node->children) {
            collectLockedDescendants(child, out);
        }
    }

    bool upgrade(string node, int userId) {
        Node* curr = nameToNode[node];
        curr->lockObj.lock();
        if (curr->isLocked || curr->lockedDescendantCount == 0) {
            curr->lockObj.unlock();
            return false;
        }
        curr->lockObj.unlock();

        vector<Node*> lockedNodes;
        collectLockedDescendants(curr, lockedNodes);
        sort(lockedNodes.begin(), lockedNodes.end());

        for (Node* n : lockedNodes) {
            n->lockObj.lock();
            if (n->lockedBy != userId) {
                n->lockObj.unlock();
                return false;
            }
            n->lockObj.unlock();
        }

        for (Node* n : lockedNodes) unlock(n->name, userId);

        return lock(node, userId);
    }

    bool performOperations(int operation, string node, int userId) {
        switch (operation) {
            case 1: return lock(node, userId);
            case 2: return unlock(node, userId);
            case 3: return upgrade(node, userId);
        }
        return false;
    }
};

int main() {
    ios::sync_with_stdio(false);
    cin.tie(nullptr);

    int nodes, child, queries;
    cin >> nodes >> child >> queries;
    vector<string> arr(nodes);
    for (int i = 0; i < nodes; i++) cin >> arr[i];

    NaryTree* tree = new NaryTree(arr[0]);
    tree->buildTree(arr, nodes, child);

    for (int i = 0; i < queries; i++) {
        int op, user;
        string node;
        cin >> op >> node >> user;
        cout << (tree->performOperations(op, node, user) ? "true\n" : "false\n");
    }
}
