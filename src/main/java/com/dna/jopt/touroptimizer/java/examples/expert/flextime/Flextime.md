# FlexTime — Flexible Start Time (Positive and Negative)

FlexTime is a resource-level feature that makes a WorkingHours start **flexible** rather than fixed.

Instead of “the route always starts at 08:00”, FlexTime turns the start into a **bounded time window** that the optimizer can use to improve feasibility and realism.

FlexTime is particularly valuable in day-to-day operations because it solves two common issues without adding manual dispatcher rules:

- **Idle time at the beginning of the route** (first job opens later than shift start).
- **Late arrival at the first job** (first job opens early, but driving time delays the start).

Reference documentation:
- https://www.dna-evolutions.com/docs/learn-and-explore/special/special_features#flexible-start-time-positive-and-negative

---

## Example sources

- Positive FlexTime:
  - [PositiveFlexTimeExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/flextime/PositiveFlexTimeExample.java)
- Negative FlexTime:
  - [NegativeFlexTimeExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/flextime/NegativeFlexTimeExample.java)
---

## The mental model

A resource has:

- **WorkingHours**: a fixed, contractual time window (e.g., 08:00–16:00).
- **Route start**: by default equals the WorkingHours start.
- **First-node timing**: depends on opening hours and travel time from the resource start.

FlexTime adjusts **when the route actually starts** within bounds:

- **Positive FlexTime** shifts the start later (reduce idle time).
- **Negative FlexTime** shifts the driving start earlier (arrive at the first node when it opens).

Both are **hard constrained**:
- the optimizer cannot exceed the configured maximum.

---

## Positive FlexTime (start later to reduce idle time)

### Business problem
A resource starts at 08:00, but the first node opens at 10:00.

Without FlexTime:
- the route starts at 08:00,
- and the resource accumulates idle time until it can begin productive work.

With positive FlexTime:
- the optimizer may shift the start later (within the allowed range),
- reducing idle time while still respecting WorkingHours and node opening hours.

### How it is configured in the example
The example uses:

```java
rep1.setFlexTime(Duration.ofHours(2));
```

Interpretation:
- the optimizer may start the route up to 2 hours later than the WorkingHours start.

### What changes operationally
Positive FlexTime helps your KPIs in several ways:

- less idle time at the beginning of the shift,
- more realistic schedules (no “waiting on the parking lot” for long time windows),
- better end-user acceptance (Gantt plots and timelines look cleaner),
- improved utilization (especially when first stops are opening-hour driven).

### When you should consider excluding driving time from FlexTime
The documentation notes that FlexTime can be configured so that:
- the allowed FlexTime is used only for “delaying work start”, not for “delaying work + driving”.

Operational interpretation:
- some organizations allow “start later” but still count “driving to the first job” as working time (or vice versa).

If your operation distinguishes between:
- paid working time,
- paid driving time,
- unpaid commute,
then this setting becomes important.

(Implementation details are documented on the FlexTime section of the Special Features page.)

---

## Negative FlexTime (start earlier to arrive when the first node opens)

### Business problem
A node opens at 08:00 and a resource starts at 08:00, but driving to the first job takes 30 minutes.

Without FlexTime:
- arrival is 08:30,
- the route begins “late” relative to the node opening (even if there is no violation, the schedule looks messy and may reduce downstream feasibility).

With negative FlexTime:
- the optimizer is allowed to start *driving* earlier so the first node can be served right when it opens.

### How it is configured in the example
The negative FlexTime example uses:

```java
rep1.setMaxRouteStartReductionTime(Duration.ofHours(2), true);
rep1.setReductionTimeIsIncludedInWorkingTime(true);
```

#### `setMaxRouteStartReductionTime(Duration, boolean)`
- The duration is the maximum allowed start-time reduction (how much earlier the route can begin).
- The boolean indicates a crucial policy:

  **“Only allowed to be used for driving (not working on the node).”**

Meaning:
- the early time can be used to move the vehicle toward the first node,
- but the resource should not start “working” before official WorkingHours / node opening hours.

This is an excellent fit for real operations:
- the employee may leave earlier to drive (commute / positioning),
- but productive work is still tied to contractual start times and opening hours.

#### `setReductionTimeIsIncludedInWorkingTime(boolean)`
This controls whether the early-start time is counted as part of the total working time.

In the example it is set to `true`, meaning:
- the early start contributes to total working time consumption,
- which is often the correct policy when early driving is paid and regulated.

If set to `false`, you model a policy where:
- early driving is outside working time (less common, but sometimes used in field-service scenarios).

### Why negative FlexTime is a major acceptance feature
Negative FlexTime has a well-known “visual effect”:
- route timelines and Gantt charts look more intuitive because first stops can align to opening times instead of starting with a long “drive segment” that shifts the whole day.

This reduces discussions like:
- “why does every route start with a long drive and then ‘work’ begins late?”

Even if feasibility is unchanged, perceived quality and trust typically improve.

---

## Combining Positive and Negative FlexTime

Positive and negative FlexTime can be used together.

Conceptually:
- the WorkingHours start becomes a flexible band:
  - it may shift later to avoid idle time,
  - or shift earlier (for driving) to align to early opening hours.

This gives the optimizer a powerful but controlled lever:
- it can pick the best start-time adjustment per day, per route, based on the actual first node(s) and travel time.

---

## Modeling guidance (what works well in production)

### 1) Be explicit about policy: “driving-only” vs “work allowed”
For negative FlexTime, the “driving-only” flag is critical.
It prevents unintended interpretation that:
- the resource is allowed to do productive work before official start.

### 2) Decide whether early-start counts toward working time
Use `setReductionTimeIsIncludedInWorkingTime(true/false)` based on:
- labor rules,
- payroll policy,
- and how you define maximum working time.

### 3) Use FlexTime to avoid brittle cost tuning
FlexTime is an architectural feature to expand the feasible region and improve realism.
It should not be simulated by cost manipulation.

If you need strict behavior (“must not start before …”, “must not start after …”), model it structurally (hard constraints or explicit time windows).

### 4) Pair FlexTime with realistic first-node handling
If your operation allows starting work immediately on early arrival (without waiting for node opening), consider the related feature:
- `isWaitOneEarlyArrival` (mentioned in the FlexTime documentation section).

This can be combined with FlexTime to match real practices:
- some organizations allow early work; others enforce waiting.

---

## Typical use cases

- Field service where first appointments open late (reduce idle time).
- Delivery operations where docks open early and early arrival is desirable.
- Any operation where “route looks wrong” because the start time is too rigid.
- Scenarios where you want better feasibility under tight time windows without increasing compute budgets.

---

## Summary

- **Positive FlexTime**: allows the route to start later to reduce idle time (`setFlexTime(...)`).
- **Negative FlexTime**: allows the route to start earlier (typically driving-only) to reach the first node when it opens (`setMaxRouteStartReductionTime(..., drivingOnly)`).
- You can choose whether the early-start time is counted as working time (`setReductionTimeIsIncludedInWorkingTime(...)`).
- Both are hard constrained and can be combined to make shift starts realistic, feasible, and easier to accept visually.
