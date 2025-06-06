#include <bits/stdc++.h>
#include <thread>

using namespace std;

class Node {
public:
    string name;
    int lockedBy;
    int lockedCount; // number of locked descendants
    vector<Node*> children;
    Node* parent;
    bool isLocked;

    Node(string name, Node* parent = nullptr)
        : name(name), parent(parent), lockedBy(-1), lockedCount(0), isLocked(false) {}
};

class LockingTree {
public:
    Node* root;
    unordered_map<string, Node*> nodeMap;

    LockingTree(const vector<string>& names, int m) {
        root = new Node(names[0]);
        nodeMap[root->name] = root;

        queue<Node*> q;
        q.push(root);
        int idx = 1;
        int n = (int)names.size();

        while (!q.empty() && idx < n) {
            Node* cur = q.front();
            q.pop();
            for (int i = idx; i < idx + m && i < n; i++) {
                Node* child = new Node(names[i], cur);
                cur->children.push_back(child);
                nodeMap[child->name] = child;
                q.push(child);
            }
            idx += m;
        }
    }

    // Helper: check if node or any descendant is locked
    bool hasLockedDescendant(Node* node) {
        if (node->lockedCount > 0) return true;
        return false;
    }

    // Check if any ancestor is locked
    bool hasLockedAncestor(Node* node) {
        Node* cur = node->parent;
        while (cur) {
            if (cur->isLocked) return true;
            cur = cur->parent;
        }
        return false;
    }

    bool lock(string nodeName, int userId) {
        
        Node* node = nodeMap[nodeName];
        if (node->isLocked || hasLockedDescendant(node) || hasLockedAncestor(node))
            return false;

        node->isLocked = true;
        node->lockedBy = userId;

        // update lockedCount of ancestors
        Node* cur = node->parent;
        while (cur) {
            cur->lockedCount++;
            cur = cur->parent;
        }
        return true;
    }

    bool unlock(string nodeName, int userId) {
        
        Node* node = nodeMap[nodeName];
        if (!node->isLocked || node->lockedBy != userId)
            return false;

        node->isLocked = false;
        node->lockedBy = -1;

        // update lockedCount of ancestors
        Node* cur = node->parent;
        while (cur) {
            cur->lockedCount--;
            cur = cur->parent;
        }
        return true;
    }

    bool upgrade(string nodeName, int userId) {
      
        Node* node = nodeMap[nodeName];
        if (node->isLocked || node->lockedCount == 0)
            return false;

        // Check all locked descendants are locked by userId
        vector<Node*> lockedDescendants;
        getLockedDescendants(node, userId, lockedDescendants);

        if (lockedDescendants.empty()) return false;

        if (hasLockedAncestor(node))
            return false;

        // Unlock all locked descendants
        for (Node* desc : lockedDescendants) {
            desc->isLocked = false;
            desc->lockedBy = -1;

            Node* cur = desc->parent;
            while (cur) {
                cur->lockedCount--;
                cur = cur->parent;
            }
        }

        // Lock current node
        node->isLocked = true;
        node->lockedBy = userId;

        Node* cur = node->parent;
        while (cur) {
            cur->lockedCount++;
            cur = cur->parent;
        }

        return true;
    }

private:
    void getLockedDescendants(Node* node, int userId, vector<Node*>& lockedDescendants) {
        for (Node* child : node->children) {
            if (child->isLocked && child->lockedBy == userId) {
                lockedDescendants.push_back(child);
            }
            getLockedDescendants(child, userId, lockedDescendants);
        }
    }
};

// Check if two nodes are siblings (share same parent)
bool areSiblings(Node* a, Node* b) {
    if (!a || !b) return false;
    return a->parent && b->parent && (a->parent == b->parent);
}

int main() {
    ios::sync_with_stdio(false);
    cin.tie(nullptr);

    int n, m, q;
    cin >> n >> m >> q;

    vector<string> names(n);
    for (int i = 0; i < n; i++) cin >> names[i];

    LockingTree tree(names, m);

    struct Query {
        int op;
        string nodeName;
        int userId;
    };

    queue<Query> queries;
    for (int i = 0; i < q; i++) {
        int op; string nodeName; int userId;
        cin >> op >> nodeName >> userId;
        queries.push({op, nodeName, userId});
    }

    vector<pair<thread, Node*>> activeThreads;

    while (!queries.empty()) {
        Query cur = queries.front();
        queries.pop();

        Node* nodePtr = tree.nodeMap[cur.nodeName];
        bool canRun = true;

           
        for (auto& [t, activeNode] : activeThreads) {
            if (t.joinable()) {
                if (!areSiblings(nodePtr, activeNode)) {
                    canRun = false;
                    break;
                }
            }
        }
        

        if (!canRun) {
            queries.push(cur);
            continue;
        }
        
        //creating a thread to process the query
        thread th([&tree, cur]() {
            bool res = false;
            if (cur.op == 1) res = tree.lock(cur.nodeName, cur.userId);
            else if (cur.op == 2) res = tree.unlock(cur.nodeName, cur.userId);
            else if (cur.op == 3) res = tree.upgrade(cur.nodeName, cur.userId);
            cout << (res ? "true" : "false") << "\n";
        });

        activeThreads.emplace_back(move(th), nodePtr);
        
        // clear the finished threads
            auto it = activeThreads.begin();
            while (it != activeThreads.end()) {
                if (it->first.joinable()) {
                    it->first.join();
                    it = activeThreads.erase(it);
                } else {
                    ++it;
                }
            }
        
    }

    // Join remaining threads before exit
    for (auto& [t, _] : activeThreads) {
        if (t.joinable()) t.join();
    }

    return 0;
}
