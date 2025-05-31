# 🌧️ Rain Simulation - Puddle Tracker

## Problem Statement

You are the manager of a simulation world where it has started raining. The world is represented as an `N x N` grid, where each raindrop falls onto a tile. Each tile can either be **empty** or **contain water** from a raindrop.

When a raindrop falls into a tile, if it touches any existing raindrops **via an edge** (top, bottom, left, or right—not diagonally), it merges with those adjacent drops to form a larger **puddle**.

Your task is twofold:

---

### 📌 Part 1: Manage Rainfall and Merging Puddles

You will receive a list of raindrop placements on the grid. Each raindrop lands on a unique tile.

#### ✅ Input:
- An integer `N` representing the size of the grid (`N x N`).
- An integer `D` representing the number of raindrop placements.
- `D` lines each containing two integers `r` and `c` — the row and column where the raindrop lands.

#### 🧠 Task:
After each drop:
- Track the formation of puddles.
- Merge puddles when adjacent tiles connect them.

---

### 📌 Part 2: Answer Queries on Puddles

After all drops have fallen, you will receive queries asking whether two specific drops belong to the same puddle.

#### ✅ Input:
- An integer `Q` representing the number of queries.
- `Q` lines each containing four integers `r1, c1, r2, c2` representing two drop locations.

#### 🧠 Output:
- For each query, output `"true"` if the two drops belong to the same puddle, or `"false"` otherwise.

---

## 🔒 Constraints

- `1 <= N <= 10^4`
- `1 <= D, Q <= 10^5`
- No tile will receive more than one drop.
- All coordinates in the queries refer to tiles that had a raindrop.

---

## 🧪 Example Test Case

### Input
```
5
5
0 0
0 1
1 1
4 4
3 4
3
0 0 1 1
0 0 4 4
3 4 4 4
```

### Output
```
true
false
true

```
