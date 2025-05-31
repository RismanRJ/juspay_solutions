# 💧 Problem: Minimum Number of Days to Collect Water

## 📝 Description

You are given `n` sprinklers. Each sprinkler can release a fixed amount of water per use and has a cooldown period (refill time) after each use.

Each sprinkler becomes available on **Day 0** and can only be used on days that are divisible by its **refill interval** (i.e., Day 0, refill[i], 2×refill[i], ...).

Your goal is to **collect at least `W` units of water** in the **minimum number of days** by using the sprinklers optimally.

---

## 🔢 Input

- `W`: `long`  
  The total units of water you need to collect.  
  _(1 ≤ W ≤ 10¹⁸)_

- `warr[]`: `int[]`  
  An array where `warr[i]` represents the **amount of water sprinkler `i` produces per use**.  
  _(1 ≤ warr[i] ≤ 10⁵)_

- `refill[]`: `int[]`  
  An array where `refill[i]` represents the **cooldown interval in days** for sprinkler `i`. It can only be used on days that are multiples of `refill[i]`.  
  _(1 ≤ refill[i] ≤ 10⁵)_

---

## 🎯 Objective

Determine the **minimum number of days `D`** such that the **total water collected by day `D` (inclusive)** is **at least `W`**.

---

## 🧠 Strategy

This is a **greedy + binary search** problem.

We observe:
- As days increase, the total water collected **never decreases**.
- So we can **binary search on the number of days `D`**.

For a given day `D`, we can compute how many times sprinkler `i` is used:

## 💡 Example

### Input
```
W = 20
warr = [3, 5]
refill = [1, 2]

```

### Output

``` 3  ```



