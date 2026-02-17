# JointVisitDuration — Modeling “Co-located” Stops with Reduced Service Time

In many routing problems, multiple tasks happen at the **same physical location**:
- multiple jobs at one customer site,
- multiple deliveries to the same warehouse gate,
- several maintenance tasks within one facility.

If these tasks are represented as separate nodes, a naive model applies the **full visit duration** to every node — which can significantly overestimate the on-site time.

JOpt supports a more realistic model using **joint visit duration**:
- the first visit at the location consumes the full visit duration,
- and immediately following co-located visits can consume a **shorter joint visit duration**.

This feature is designed to reflect operational reality:
- setup time happens once (check-in, parking, security),
- subsequent tasks at the same site are faster (walking distance is negligible, context already established).

---

## References

- Example source: [JointVisitDurationExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/jointvisitduration/JointVisitDurationExample.java)

---

## The key API: `setJointVisitDuration(...)`

Each node can define a joint duration:

- `node.setJointVisitDuration(Duration jointVisitDuration);`

### Interpretation
When nodes share the **same geo location** and are visited **directly after each other**:

- the **first** node uses `visitDuration` (normal duration),
- subsequent nodes use `jointVisitDuration` (shortened duration).

### Default behavior
By default:
- `jointVisitDuration == visitDuration`

Meaning:
- you must set it explicitly if you want shortened follow-up tasks.

---

## What the example demonstrates

The example `JointVisitDurationExample` constructs a scenario with repeated nodes at identical coordinates:

### Visit durations
- Normal visit duration: **30 minutes**
- Joint visit duration: **15 minutes**

The example then assigns the joint duration to each relevant node:

- `koeln1.setJointVisitDuration(jointVisitDuration);`
- `koeln2.setJointVisitDuration(jointVisitDuration);`
- `koeln3.setJointVisitDuration(jointVisitDuration);`

and similarly for Essen nodes (Essen1/2/3), plus Dueren.

### Co-located node groups
The example intentionally creates multiple nodes with identical coordinates:

- **Koeln1, Koeln2, Koeln3** share `(50.9333, 6.95)`
- **Essen1, Essen2, Essen3** share `(51.45, 7.01667)`

This makes the effect visible:
- if the solver schedules Koeln1 → Koeln2 → Koeln3 consecutively, the total on-site time becomes:
  - 30 minutes + 15 minutes + 15 minutes = **60 minutes**
instead of:
  - 30 + 30 + 30 = **90 minutes**

---

## Why this feature matters

### 1) Realistic time accounting
Without joint duration, co-located tasks often overestimate:
- setup time,
- parking/walking,
- check-in overhead.

JointVisitDuration prevents unnecessary infeasibility:
- especially under tight working hours or opening hours.

### 2) Better routing decisions
If the model correctly reflects that “stacking tasks at the same site is cheaper,” the optimizer can:
- bundle tasks,
- reduce travel,
- and increase route density in dense areas.

### 3) Better service-level compliance
Accurate on-site time reduces:
- artificial lateness,
- unnecessary overtime,
- and “false infeasible” cases.

---

## Interaction with time windows and working hours

Joint visit duration affects the schedule time linearly:
- shorter on-site durations can improve feasibility against:
  - node opening hours (time windows),
  - resource working hours,
  - maximal working time constraints.

Important nuance:
- Joint duration only applies when the co-located nodes are visited **directly after each other**.
- If the solver interleaves another location between them, each visit is treated independently.

---

## Modeling guidance (how to use it correctly)

### 1) Use it only when tasks are truly co-located
JointVisitDuration assumes “same site, same arrival”.  
If two tasks are near each other but not actually co-located, do not use joint duration.

### 2) Choose joint duration based on operational process
A common breakdown:
- full visit duration = setup + execution
- joint duration = execution only (setup assumed already done)

Example:
- setup: 15 minutes (check-in, gear, parking)
- execution: 15 minutes
- total: 30 minutes
- joint: 15 minutes

This is exactly the type of parameterization shown in the example.

### 3) Avoid over-optimistic values
If joint duration is unrealistically small, you may create:
- plans that cannot be executed,
- SLA issues in the field.

Calibrate from real service logs where possible.

### 4) Use separate nodes when tasks must be distinguishable
Even if co-located, you may still need separate nodes for:
- different service windows,
- different priorities,
- different constraints (skills/resources),
- different external identifiers.

JointVisitDuration supports this without losing realism.

---

## Performance and scalability considerations

JointVisitDuration is a modeling feature, not just an output decoration:
- it changes feasibility and objective values,
- therefore it influences search and route construction.

In large instances where many tasks share locations (warehouses, campuses), it can substantially reduce:
- unnecessary overtime violations,
- infeasibility caused by inflated service time.

---

## How to run and validate

1. Run `main(...)` in `JointVisitDurationExample`.
2. Print the result and verify that co-located sequences show reduced service time.
3. Export and inspect KML if you are using additional exporters:
   - confirm the solver naturally groups co-located tasks where beneficial.

A practical validation method:
- compare total visit time in a route where (Koeln1, Koeln2, Koeln3) are consecutive:
  - with joint duration enabled vs disabled.

---

## Summary

- `setJointVisitDuration(...)` lets you model **reduced service time for consecutive visits at the same geo location**.
- The first node uses the full visit duration; subsequent co-located nodes can use a shorter joint duration.
- This improves realism, feasibility, and route quality in scenarios with multi-task locations (campuses, warehouses, multi-order customers).
- The example uses:
  - 30 minutes normal duration,
  - 15 minutes joint duration,
  across multiple co-located nodes in Koeln and Essen to make the effect observable.
