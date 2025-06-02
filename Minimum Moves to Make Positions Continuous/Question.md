# Minimum Moves to Make Positions Continuous

## Problem Description

You are given an integer array `positions` of length `n`. Your goal is to transform the array such that all elements form a sequence of **consecutive distinct integers** (e.g., `[4, 5, 6, 7]`).

You can perform the following operation any number of times:

- **Move an element** by changing its value to any integer you want.

Your task is to find the **minimum number of moves** required to make the `positions` array continuous.

---

## Constraints

- `1 <= positions.length <= 10^5`
- `1 <= positions[i] <= 10^5`

---

### Input:

```
positions = [1, 7, 11, 8 ]
```

### Output :

``` 2 ```

### Explnation:

```
One possible solution is to move:
- 7 → 9 (positions become [1, 8, 9, 11])
- 1 → 10 (positions become [8, 9, 10, 11])
Now all positions are continuous.

```
