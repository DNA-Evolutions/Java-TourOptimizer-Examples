# Custom Cost Convergence — Stop Optimization When Your Metric Has Stabilized

In production planning, you typically want **predictable runtimes** while still extracting most of the value of iterative optimization.

A practical strategy is: **stop when the solution quality has stabilized**—not when an arbitrary generation count is reached.

JOpt’s *cost converger* framework is designed for this. It listens to progress events and triggers a controlled shutdown when a chosen metric stays unchanged for a configurable number of progress updates.

This is extremely useful for:
- runtime control in batch pipelines,
- interactive UIs (“stop when improvements stall”),
- and compute-cost reduction without sacrificing practical quality.

---

## References

- Example source: [CustomCostConvergenceExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/externalcostconvergence/CustomCostConvergenceExample.java)  

The convergence utilities referenced in this document are core classes (provided in the attached source files):
- `ICostConverger`
- `AbstractCostConverger`
- `JoinedCostConverger`
- `JoinedCostConvergerWithFinalAutoFilterStep`

---

## What “convergence” means in this implementation

### Convergence is defined as “no metric change” for *N* progress events

`AbstractCostConverger` implements a simple and robust rule:

1. For every progress event `curP`, it computes a scalar metric:  
   `double cost2BeEvaluated = getConvergenceCost(curP);`

2. It compares the metric to the last metric value:

- if `lasCost != cost2BeEvaluated` → reset counter (`numCyclesConstantCost = 0`)
- else (exact equality) → increment counter

3. If the counter reaches the configured threshold, the run is considered converged:
- `numCyclesConstantCost >= numMaxProgressCyclesConstantCost`

**Important:** the comparison is an exact double comparison (`!=`).  
For stability, your convergence metric should be:
- an integer-like value,
- or a rounded value,
- or something that tends to repeat exactly across progress events.

For example:
- total distance meters as a `long` cast to `double`,
- or cost rounded to 2 decimals.

---

## What `setConvergenceThreshold(int)` really controls

Despite the name, `setConvergenceThreshold(...)` does **not** set a “cost delta threshold”.

It sets the number of consecutive progress updates for which the metric must remain unchanged:

```java
public void setConvergenceThreshold(int numProgressSteps) {
  numMaxProgressCyclesConstantCost = numProgressSteps;
}
```

So:

- `setConvergenceThreshold(5)` means:  
  “stop after 5 consecutive progress events where the convergence metric does not change.”

---

## Selecting which algorithms may trigger convergence

A converger does not necessarily apply to all algorithm phases.  
`AbstractCostConverger` filters progress events by algorithm identifier:

```java
if (execAlgos.stream().noneMatch(s -> s.equalsIgnoreCase(curP.getCallerId()))) {
  return false;
}
```

You must explicitly register allowed algorithms:

```java
converger.addExecutionAlgorithm("GeneticEvolution");
converger.addExecutionAlgorithm("SimulatedAnnealing");
```

Use the algorithm title strings (often available as constants in the algorithm classes) that match `curP.getCallerId()`.

Why this matters:
- large optimizations often chain multiple phases,
- you might want convergence only on the “main search” phase,
- or you might want different convergence definitions per phase.

---

## The lifecycle: `onConverged()` and `onDone()`

### 1) `onConverged(IOptimization opti)` returns a `CompletableFuture<Void>`
When convergence is detected, `AbstractCostConverger` calls:

- `CompletableFuture<Void> convergedFuture = onConverged(opti);`

This is a hook for *final actions* you want to perform when convergence is reached, such as:
- triggering a final post-step,
- exporting a snapshot,
- flushing telemetry,
- or running an explicit cleanup step.

By default (`AbstractCostConverger` implementation):
- `onConverged(...)` returns an already completed future (no-op).

### 2) After the future completes, `onDone(opti)` is called
`AbstractCostConverger` waits for the future with a configurable timeout:

- `convergedFuture.get(onConvergedtimeout, onConvergedTimeUnit);`

Then, if the future is done, it calls:

- `onDone(opti);`

By default:
- `onDone(...)` calls `opti.stopOptimization();`

This is the “graceful stop”: the solver stops normally and returns the current best result.

### 3) Timeout control for `onConverged(...)`
You can configure the wait:

- `setOnConvergedTimeOut(long, TimeUnit)`

This prevents a misbehaving finalization step from blocking shutdown indefinitely.

---

## Built-in convergers you can reuse

### JoinedCostConverger — converge on overall job cost
`JoinedCostConverger` is a standard implementation that converges on:

- `curP.getJobCost()`

Use it when:
- your overall cost function is what you care about,
- and you want to stop when global cost stops improving.

### JoinedCostConvergerWithFinalAutoFilterStep — converge, then run a final AutoFilter pass
This class demonstrates the “finalization hook” pattern:

- It still converges on `curP.getJobCost()`.
- Its `onConverged(...)` performs a manual AutoFilter step by retrieving an `IAutoNodeFilter` from the algorithm’s post-step manager and calling:
  - `performManual()`

After the future completes, `onDone(...)` stops the optimization.

Operational interpretation:
- you can use convergence as a trigger to run a last deterministic post-step (here: AutoFilter),
- and then stop cleanly.

This pattern is useful whenever you want to:
- “stop only after doing X final normalization step”,
- but still keep the main optimization runtime bounded.

---

## Implementing a custom convergence metric

To converge on a different KPI, you typically:

1. Extend `AbstractCostConverger`
2. Override `getConvergenceCost(IOptimizationProgress curP)`

Example pattern (conceptual):

- converge on total distance in meters:
  - return `curP.getJobTransitionDistanceMeter()`

- converge on lateness minutes:
  - return a lateness KPI if available in progress

- converge on a rounded cost:
  - return `Math.round(curP.getJobCost() * 100.0) / 100.0`

### Key recommendation: normalize to stable values
Because equality is strict, prefer a stable metric such as:
- integer meters,
- integer seconds,
- integer violation counts,
- or explicitly rounded values.

---

## How this differs from exit conditions

Exit conditions (time/generation count) are a hard upper bound:
- “stop no matter what.”

A cost converger is an *adaptive early stop*:
- “stop when improvements stall.”

Best practice:
- use both:
  - keep a hard upper bound (time or generation count),
  - add a converger to stop earlier in the typical case.

This improves:
- runtime predictability,
- cost control (compute),
- and UX for interactive planning.

---

## End-user acceptance and debugging benefits

### Acceptance
Convergence-based stopping aligns with how planners think:
- “This is good enough; improvements are marginal.”

In interactive UIs, this enables:
- fast, stable results,
- fewer “weird late changes” that occur in the tail of long runs.

### Debugging
A converger gives you a measurable signal:
- when did the KPI stop changing,
- which algorithm phase stabilized,
- and what the last improvements looked like.

This is valuable for:
- tuning operator sets and weights,
- comparing solver versions,
- and building reproducible performance baselines.

---

## Summary

- `AbstractCostConverger` stops optimization when a chosen metric remains **exactly unchanged** for **N consecutive progress events**.
- `setConvergenceThreshold(N)` sets the number of unchanged progress steps—not a cost delta.
- `addExecutionAlgorithm(...)` scopes convergence to specific algorithm phases (based on `curP.getCallerId()`).
- `onConverged(...)` is an asynchronous finalization hook; after its future completes, `onDone(...)` stops the optimization.
- Built-in implementations exist:
  - `JoinedCostConverger` (converge on overall job cost),
  - `JoinedCostConvergerWithFinalAutoFilterStep` (run a final AutoFilter pass before stopping).
- For custom KPIs, return stable, integer-like, or rounded metrics to avoid issues with strict double equality.
