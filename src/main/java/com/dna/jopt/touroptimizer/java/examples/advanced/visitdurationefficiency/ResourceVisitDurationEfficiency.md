# Resource Visit Duration Efficiency — Modeling Faster (or Slower) Resources

In many real-world tour optimization scenarios, resources are not equally fast:

- one technician is more experienced,
- one crew has better equipment,
- one vehicle requires longer on-site handling,
- some resources can execute certain tasks more efficiently than others.

JOpt.TourOptimizer supports this directly via a **resource visit duration efficiency factor**.  
This factor scales how long a resource needs to perform the “same job” at nodes whose visit duration is allowed to be modified.

---

## References

- Example source: [ResourceVisitDurationEfficiencyExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/visitdurationefficiency/ResourceVisitDurationEfficiencyExample.java)

---

## The key idea

Nodes typically have a default visit duration, for example:
- 30 minutes per visit

A resource can then declare an efficiency factor:

- efficiency factor **1.0** → use the visit duration “as is”
- efficiency factor **0.5** → the resource needs **only 50%** of the time
- efficiency factor **2.0** → the resource needs **200%** (twice the time)

In the example:
- Both resources start in Aachen.
- Default node visit duration is **30 minutes**.
- Resource **Clara** is configured as “more efficient”:
  - `resClara.setOverallVisitDurationEfficiencyFactor(0.5);`

Expected effect:
- Clara should be preferred for assignments, because she can complete the same workload in less time (and therefore potentially visit more nodes and/or reduce idle/late risk).

---

## Where efficiency applies (and where it does not)

Efficiency only has an effect when the node allows **route-dependent visit duration**.

### Route-dependent visit duration (enabled)
The example enables visit duration adjustment for certain nodes:

- `node.setHasRouteDependentVisitDuration(true);`

Meaning:
- the solver may scale the effective visit duration (within limits), depending on which resource visits the node.

### Fixed duration (disabled)
Some jobs are not compressible:
- breaks,
- fixed service slots,
- regulated handling time.

The example explicitly disables route-dependent visit duration for one node:

- “Essen - fixed duration”
- `essen.setHasRouteDependentVisitDuration(false);`

Meaning:
- even if Clara is faster, this job stays at the full duration.

---

## Minimum visit duration (why “faster” does not always mean “arbitrarily fast”)

In practice, even an expert cannot perform some tasks below a hard lower bound:
- safety protocols,
- mandatory inspection steps,
- physical constraints.

The example sets a minimum visit duration of **20 minutes** for two nodes:

- `node.setMinimalVisitDuration(Duration.ofMinutes(20));`

Applied to:
- Koeln
- Oberhausen

Practical interpretation with Clara (efficiency 0.5):
- Default is 30 minutes.
- Scaled would be 15 minutes.
- But the minimum is 20 minutes.
- Therefore, Clara is capped at 20 minutes for those nodes.

The example explicitly calls this out:
- Clara’s “true potential of 15 minutes” can only be realized at the node that has **no minimum limit** (“Dueren - unlimited”).

---

## What the example is designed to show

The nodes are intentionally chosen to cover all important cases in one run:

1. **Koeln**  
   - route-dependent visit duration enabled  
   - minimum visit duration = 20 min  
   ⇒ Clara improves time, but only down to 20 min

2. **Oberhausen**  
   - same behavior as Koeln

3. **Essen - fixed duration**  
   - route-dependent visit duration disabled  
   ⇒ no efficiency impact

4. **Dueren - unlimited**  
   - route-dependent visit duration enabled  
   - no minimum visit duration  
   ⇒ Clara can reach the full 0.5 factor: 30 min → 15 min

With these four nodes, you can validate:
- whether efficiency scaling is applied,
- whether minimum duration caps are honored,
- whether fixed-duration nodes remain fixed.

---

## Interpreting the results

The example subscribes to progress and the final result:

- `progressSubject().subscribe(p -> println(p.getProgressString()))`
- `resultFuture().thenAccept(println)`

In practice, you would verify:
- assignment decisions (Clara should dominate if constraints allow),
- route time metrics (Clara’s tour should be shorter),
- feasibility margins (less risk of exceeding WorkingHours or time windows).

If your scenario includes tight time windows, this feature is particularly impactful:
- faster resources can make otherwise infeasible schedules feasible without changing the plan structure.

---

## Recommended production usage

### 1) Start with an “overall” factor
If your data model does not yet include per-task efficiency, start with:
- a single overall efficiency factor per resource (as in the example).

This already captures many real operational differences.

### 2) Use minimum visit durations to preserve realism
Always ask:
- “What is the absolute minimum for this task type?”

Then encode it as:
- `setMinimalVisitDuration(...)`

This prevents unrealistic solutions where:
- efficiency compresses tasks below operational reality.

### 3) Treat fixed-duration nodes as hard process steps
If a node represents:
- a break,
- a mandatory procedure,
- a non-negotiable service slot,
disable route-dependent visit duration:
- `setHasRouteDependentVisitDuration(false)`

### 4) Prefer architectural modeling over cost hacks
Do not attempt to “simulate efficiency” by adding artificial costs.
Visit-duration efficiency affects:
- feasibility,
- schedule tightness,
- and route time in a structurally correct way.

---

## Summary

- Resources can have different execution speeds via `setOverallVisitDurationEfficiencyFactor(...)`.
- Efficiency only applies to nodes that allow route-dependent visit durations (`setHasRouteDependentVisitDuration(true)`).
- Minimum visit durations cap efficiency effects, ensuring realism.
- Fixed-duration nodes ignore efficiency (useful for breaks or strictly timed operations).
- The example demonstrates all three cases in a compact scenario and shows why an experienced resource tends to dominate assignments when time matters.
