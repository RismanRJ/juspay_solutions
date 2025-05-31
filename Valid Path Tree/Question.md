# Valid Root-to-Leaf Paths in a Binary Tree with Obstacles

## Problem Description

You are given a binary tree described via a list of parent-child relationships and a flag map indicating whether each node is passable (`0`) or blocked by an obstacle (`1`).

Your task is to determine how many root-to-leaf paths exist such that no path contains more than `m` consecutive obstacles.

---

## Input

- `edges`: A list of parent-child pairs representing the tree structure. Each element is a pair `[parent, child]`.
- `flags`: A map from node values to integers `0` or `1`, where:
  - `0` indicates the node is open (passable)
  - `1` indicates the node is blocked (obstacle)
- `m`: An integer representing the maximum allowed consecutive obstacles on any root-to-leaf path.

---

## Output

- Return the number of valid root-to-leaf paths that respect the constraint of at most `m` consecutive obstacles.
- If no such path exists, return `-1`.

---

## Constraints

- Number of nodes: `1 <= n <= 1000`
- Maximum consecutive obstacles: `0 <= m <= 10`
- The input always describes a connected tree with exactly one root.

---

## Example

### Input

```
edges = [
  [1, 2],
  [1, 3],
  [2, 4],
  [2, 5],
  [3, 6]
]

flags = {
  1: 0,
  2: 0,
  3: 0,
  4: 1,
  5: 0,
  6: 1
}

m = 1 
```

### Tree Structure

        1
       / \
      2   3
     / \   \
    4   5   6

### Output

``` 3 ```

