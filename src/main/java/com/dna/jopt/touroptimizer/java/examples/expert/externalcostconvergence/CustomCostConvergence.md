# Stop a run early with a cost converger

`CustomCostConvergenceExample` attaches a cost converger to a run: once six progress events in a row show the same driven distance of the best solution (the first sets the distance, the next five repeat it), the converger stops the run, and the run returns the best result it has. The page is for developers who want a run to end when its result stops improving, not at a fixed generation count. It builds on the [First optimization example](../../basic/firstoptimization_01/FirstOptimizationExample.md) and on the progress events described in the [Reactive events and streams](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/reactive_events) feature guide.

## What the example shows

- **A converger of your own:** `TransitionDistanceConverger` extends `AbstractCostConverger` from the core package `com.dna.jopt.util.costconverger` and overrides `getConvergenceCost` to return `getJobTransitionDistanceMeter()`, the driven distance of the best solution in meters.
- **The watched stages:** `addExecutionAlgorithm` names the stages whose progress events count, by their caller ids: `GeneticEvolution` and `SmartSimulatedAnnealingAlgo`.
- **The threshold:** `setConvergenceThreshold(5)` stops the run when five progress events in a row repeat the distance of the event before them. The class default of `AbstractCostConverger` is 10.
- **The stop:** the converger calls `stopOptimization()` on the optimization. The run ends with the best result found so far, and the status event `Optimization converged.` says so.
- **Created before the start:** the converger subscribes to the progress events when it is created, so `addConverger` runs before the run starts.

## Scenario

The dates, times and coordinates are sample values. The time zone is Europe/Berlin.

- **Resource:** `Jack`, a `CapacityResource` at 50.775346, 6.083887 (Aachen), with working hours on May 6 and May 7, 2020, from 08:00 to 17:00. He may work up to 13 hours and drive up to 1200 km a day.
- **Nodes:** nine `TimeWindowGeoNode` objects (Koeln, Oberhausen, Essen, Dueren, Nuernberg, Heilbronn, Stuttgart, Wuppertal and Aachen), open on the same two days from 08:00 to 17:00, each with a visit of 20 minutes and the same importance.
- **Run values:** `addProperties` sets `JOptExitCondition.JOptGenerationCount` and `JOpt.Algorithm.PreOptimization.SA.NumIterations` below their defaults, which keeps the run short; the defaults are in the [property reference](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties). The converger, not these exit conditions, ends the run. With 20000 generations and 1000000 iterations, far above the defaults, the converger stopped the run at the same point in four test runs of an earlier version of the class.

## How the code works

The source is [CustomCostConvergenceExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/externalcostconvergence/CustomCostConvergenceExample.java). `main(...)` instantiates the class and calls `example()`:

```java
// The public evaluation license, or your own license if you put a license file where
// ExampleLicenseHelper looks for it.
ExampleLicenseHelper.setLicense(this);
addProperties(this);
addNodes(this);
addResources(this);
addConverger(this);
attachToObservables(this);
IOptimizationResult result = startAndPresentResult(this);
exportToKml(result);
```

`addConverger` creates the converger and sets what it watches:

```java
// The converger subscribes to the progress events when it is created, so it is created
// before the start.
ICostConverger converger = new TransitionDistanceConverger(opti);
// Only progress events of these two stages count; the names are their caller ids.
converger.addExecutionAlgorithm("GeneticEvolution");
converger.addExecutionAlgorithm("SmartSimulatedAnnealingAlgo");
// 5 instead of the class default of 10, so that a short plateau stops the run: five progress
// events in a row that repeat the distance of the event before them.
converger.setConvergenceThreshold(5);
```

`TransitionDistanceConverger`, a nested class of the example, returns the metric that the converger compares:

```java
@Override
public double getConvergenceCost(IOptimizationProgress curP) {
  // The converger classes inside the core compare the job cost (getJobCost); this one
  // compares the distance. The distance is a whole number of meters, so equal values compare
  // exactly.
  return curP.getJobTransitionDistanceMeter();
}
```

`attachToObservables` subscribes to the progress, warning, status and error events with the recommended event API, before the run starts. `startAndPresentResult` starts the run, waits up to 5 minutes for the result, prints it and returns it. `exportToKml` writes the result to `CustomCostConvergenceExample.kml` in the working directory, as in [Export a result to a KML file](../../basic/io_03/Export2KMLExample.md).

## Output and what to look for

The lines below, the cost and the route of May 7 were the same in four test runs; the order of the stops on May 6 changed between them. The construction stage prints its progress lines, and then the simulated annealing stage starts. Its progress lines all show the same distance, `TD[km] 1102.096` (trimmed to the progress lines of this stage and the status events among and after them; two log lines are left out):

```text
PC 0.0, RE 1, AL SmartSimulatedAnnealingAlgo, JC 1189.6735989443173, RC 2, EC 9, TC 11, TT[h] 16, TU[%] 17, TD[km] 1102.096, T 250.0
PC 0.0, RE 1, AL SmartSimulatedAnnealingAlgo, JC 1189.6735989443173, RC 2, EC 9, TC 11, TT[h] 16, TU[%] 17, TD[km] 1102.096, T 225.92308439810986
PC 1.0, RE 1, AL SmartSimulatedAnnealingAlgo, JC 1189.6735989443173, RC 2, EC 9, TC 11, TT[h] 16, TU[%] 17, TD[km] 1102.096, T 204.16496025582194
PC 2.0, RE 1, AL SmartSimulatedAnnealingAlgo, JC 1189.6735989443173, RC 2, EC 9, TC 11, TT[h] 16, TU[%] 17, TD[km] 1102.096, T 184.50231018805124
PC 3.0, RE 1, AL SmartSimulatedAnnealingAlgo, JC 1189.6735989443173, RC 2, EC 9, TC 11, TT[h] 16, TU[%] 17, TD[km] 1102.096, T 166.7333239850454
StatusEvent(EXTERNAL_STATUS_EVENT, Optimization converged.)
PC 4.0, RE 1, AL SmartSimulatedAnnealingAlgo, JC 1189.6735989443173, RC 2, EC 9, TC 11, TT[h] 16, TU[%] 17, TD[km] 1102.096, T 150.67562730660325
StatusEvent(OPTIMIZATION_ALGO_REPDONE, SmartSimulatedAnnealingAlgo)
StatusEvent(OPTIMIZATION_DONE, Optimization done)
```

The first of the six progress lines sets the distance, the next five repeat it, and the sixth line, `PC 4.0`, reaches the threshold of 5. The status event comes before that line in the output because the converger subscribes to the progress events in `addConverger`, before `attachToObservables` subscribes the printing: the converger receives each progress event first, converges on the sixth and sends its status event, and only then is the line printed. The log line "Converged" comes just before the status event, and the log line "Stop optimization was called." after the `PC 4.0` line. The run stops at 4 percent of the simulated annealing stage: the genetic evolution stage never starts, and the 1000 generations and 100000 iterations are not reached. The cost of the result, `1189.6735989443173`, is the cost at the start of the simulated annealing stage, so the result is the solution of the construction stage.

The route of May 7 visits Nuernberg, Stuttgart and Heilbronn (trimmed to these lines of route 1):

```text
RouteId                : 1
Defined WorkingHours   : 07.05.2020 08:00:00 - 07.05.2020 17:00:00 (Europe/Berlin)
Effective Route Stop   : 07.05.2020 19:58:18 (Europe/Berlin)
Route time       [min] : 718.31
```

It ends at 19:58:18, after the end of its working hours at 17:00, and the result shows no violation. Overtime counts against the maximal working time, and the working hours are a frame for matching resources with the opening hours of the nodes, so a route can end after its working hours without a violation. The 718.31 minutes stay below Jack's maximal working time of 13 hours.

## Why stop on convergence

In production planning, you typically want **predictable runtimes** while still extracting most of the value of iterative optimization.

A practical strategy is: **stop when the solution quality has stabilized**, not when an arbitrary generation count is reached.

JOpt's *cost converger* framework is designed for this. It listens to progress events and triggers a controlled shutdown when a chosen metric stays unchanged for a configurable number of progress updates.

This is useful for:
- runtime control in batch pipelines,
- interactive UIs ("stop when improvements stall"),
- and compute-cost reduction without sacrificing practical quality.

## The converger classes of the core

The convergence classes referenced in this document are in the core package `com.dna.jopt.util.costconverger`. The Javadoc of the core library documents all four under these names, although the library jar renames the last two:
- `ICostConverger`, the interface of a converger;
- `AbstractCostConverger`, the base class of a converger of your own;
- `JoinedCostConverger` and `JoinedCostConvergerWithFinalAutoFilterStep`, two converger classes inside the core. They are internal: the published library does not expose them under these names, so your code cannot reference them.

## What "convergence" means in this implementation

### Convergence is defined as "no metric change" for *N* progress events

`AbstractCostConverger` implements a simple and robust rule:

1. For every progress event `curP`, it computes a scalar metric: it calls `getConvergenceCost(curP)`, the method that a converger of your own overrides.

2. It compares the metric to the last metric value:

- if the metric differs from the last metric value, it resets the counter to 0;
- else (exact equality), it increments the counter by 1.

3. If the counter reaches the configured threshold, the run is considered converged:
- the counter is equal to or greater than the threshold of `setConvergenceThreshold(...)` (the class default is 10; the example sets 5).

**Important:** the comparison is an exact double comparison: two values count as the same only if they are equal.  
For stability, your convergence metric should be:
- an integer-like value,
- or a rounded value,
- or something that tends to repeat exactly across progress events.

For example:
- total distance meters as a `long` cast to `double`,
- or cost rounded to 2 decimals.

The counter is reset only by a changed metric and after `onDone(...)` (see below). While the metric stays the same and the run goes on, every further progress event counts as converged again.

## What `setConvergenceThreshold(int)` really controls

Despite the name, `setConvergenceThreshold(...)` does **not** set a "cost delta threshold".

It sets the number of consecutive progress updates for which the metric must remain unchanged: the converger counts the progress events that repeat the metric of the event before them, and converges when that count reaches the number. Until you call it, the number is 10, the class default; the example sets 5. The core refuses a value below 1 with an `IllegalStateException`.

So:

- `setConvergenceThreshold(5)` means:  
  "stop after 5 consecutive progress events where the convergence metric does not change."

## Selecting which algorithms may trigger convergence

A converger does not necessarily apply to all algorithm phases.  
`AbstractCostConverger` filters progress events by algorithm identifier: it looks only at the progress events whose caller id (`curP.getCallerId()`) matches one of the names registered with `addExecutionAlgorithm(...)`. Every other progress event is skipped: it neither converges nor changes the counter.

You must explicitly register allowed algorithms:

```java
converger.addExecutionAlgorithm("GeneticEvolution");
converger.addExecutionAlgorithm("SmartSimulatedAnnealingAlgo");
```

Use the algorithm title strings that match `curP.getCallerId()` (the comparison ignores case), and write them as strings: the published library does not expose the algorithm classes and their title constants. The progress line of each stage shows its caller id after `AL`, for example `AL SmartSimulatedAnnealingAlgo`.

Why this matters:
- large optimizations often chain multiple phases,
- you might want convergence only on the "main search" phase,
- or you might want different convergence definitions per phase.

## The lifecycle: `onConverged()` and `onDone()`

### `onConverged(IOptimization opti)` returns a `CompletableFuture<Void>`
When convergence is detected, `AbstractCostConverger` calls:

- `onConverged(opti)`, which returns a `CompletableFuture<Void>`.

This is a hook for *final actions* you want to perform when convergence is reached, such as:
- triggering a final post-step,
- exporting a snapshot,
- flushing telemetry,
- or running an explicit cleanup step.

By default (`AbstractCostConverger` implementation):
- `onConverged(...)` returns an already completed future (no-op).

### After the future completes, `onDone(opti)` is called
`AbstractCostConverger` waits for the future with a configurable timeout, in a thread of its own:

- the future returned by `onConverged(...)`, with the timeout set by `setOnConvergedTimeOut(...)`.

Then, if the future is done, it calls:

- `onDone(opti);`

By default:
- `onDone(...)` calls `opti.stopOptimization();`

This is the "graceful stop": the solver stops normally and returns the current best result.

### Timeout control for `onConverged(...)`
You can configure the wait:

- `setOnConvergedTimeOut(long, TimeUnit)`

Until you set it, the wait has no practical limit. Setting a timeout prevents a misbehaving finalization step from blocking shutdown indefinitely: the converger stops waiting. On a timeout, though, the converger does not call `onDone(...)`, so that converged event does not stop the run. It passes the `TimeoutException` to the uncaught exception handler of the optimization, if one is set, and otherwise only logs it. The counter is not reset either, because the converger resets it only after `onDone(...)`: each further progress event with the same metric counts as converged again and calls `onConverged(...)` again.

## Built-in convergers inside the core

The two converger classes inside the core show two ways to finish a run. The published library does not expose them, so a converger of your own extends `AbstractCostConverger`, as the example does, and returns the metric you want.

### JoinedCostConverger: converge on overall job cost
`JoinedCostConverger` is a standard implementation that converges on:

- `curP.getJobCost()`

Converge on the job cost when:
- your overall cost function is what you care about,
- and you want to stop when global cost stops improving.

### JoinedCostConvergerWithFinalAutoFilterStep: converge, then run a final AutoFilter pass
This class demonstrates the "finalization hook" pattern:

- It still converges on `curP.getJobCost()`.
- Its `onConverged(...)` performs a manual AutoFilter step: one more AutoFilter pass on the current algorithm, run by hand.

If the current algorithm has no AutoFilter step, its `onConverged(...)` throws an `IllegalStateException`.

After the future completes, `onDone(...)` stops the optimization.

Operational interpretation:
- you can use convergence as a trigger to run a last deterministic post-step (here: AutoFilter),
- and then stop cleanly.

This pattern is useful whenever you want to:
- "stop only after doing X final normalization step",
- but still keep the main optimization runtime bounded.

## Implementing a custom convergence metric

To converge on a different KPI, you typically:

1. Extend `AbstractCostConverger`
2. Override `getConvergenceCost(IOptimizationProgress curP)`

Example pattern (conceptual):

- converge on total distance in meters (what the example does):
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

`getJobTransitionDistanceMeter()` already returns whole meters (an `int`).

## Convergence in a JSON optimization

A JSON optimization can carry the same kind of converger. Its `OptimizationOptions` take an optional `optimizationConvergenceOptions` with `convergenceThreshold` (at least 1), `watchedAlgorithms` (the values `SIMULATED_ANNEALING_ALGO`, `PARALLEL_SIMULATED_ANNEALING_ALGO`, `SMART_SIMULATED_ANNEALING_ALGO` and `GENETIC_EVOLUTION_ALGO`), `costMetric` (`JOB_COST` or `JOB_TRANSITION_DISTANCE`, the metric of this example) and an optional `onConvergedTimeout`. When the JSON optimization is loaded, the core creates a converger from these options and subscribes it to the progress events.

## How this differs from exit conditions

Exit conditions (time/generation count) are a hard upper bound:
- "stop no matter what."

A cost converger is an *adaptive early stop*:
- "stop when improvements stall."

Best practice:
- use both:
  - keep a hard upper bound (time or generation count),
  - add a converger to stop earlier in the typical case.

The example does both: its generation count and iterations stay as the upper bound.

This improves:
- runtime predictability,
- cost control (compute),
- and UX for interactive planning.

## End-user acceptance and debugging benefits

### Acceptance
Convergence-based stopping aligns with how planners think:
- "This is good enough; improvements are marginal."

In interactive UIs, this enables:
- fast, stable results,
- fewer "weird late changes" that occur in the tail of long runs.

### Debugging
A converger gives you a measurable signal:
- when did the KPI stop changing,
- which algorithm phase stabilized,
- and what the last improvements looked like.

In the example, the signal is the status event `Optimization converged.`; the progress lines before it show the stage and the distance.

This is valuable for:
- tuning operator sets and weights,
- comparing solver versions,
- and building reproducible performance baselines.

## Summary

- `AbstractCostConverger` stops optimization when a chosen metric remains **exactly unchanged** for **N consecutive progress events**.
- `setConvergenceThreshold(N)` sets the number of unchanged progress steps, not a cost delta.
- `addExecutionAlgorithm(...)` scopes convergence to specific algorithm phases (based on `curP.getCallerId()`).
- `onConverged(...)` is an asynchronous finalization hook; after its future completes, `onDone(...)` stops the optimization.
- Built-in implementations exist inside the core, internal and not exposed by the published library:
  - `JoinedCostConverger` (converge on overall job cost),
  - `JoinedCostConvergerWithFinalAutoFilterStep` (run a final AutoFilter pass before stopping).
- For custom KPIs, return stable, integer-like, or rounded metrics to avoid issues with strict double equality.

## Run it

Run the `main(String[] args)` method of `CustomCostConvergenceExample` from your IDE, with the repository imported as a Maven project. You need a JDK in the version that the examples pom sets. You need no license file: without one, the example uses the public evaluation license (see the [license guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/license)). The run writes `CustomCostConvergenceExample.kml` into the working directory.

## Related

- [Reactive events and streams](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/reactive_events): the progress events that a converger reads.
- [Running the genetic evolution stage in a loop](../../advanced/looprun/RunOptimizationInLoop.md): a run in continuous mode that a loop counter ends.
- [Optimization properties](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties): the property reference, with the defaults of the generation count and the iterations.
- [Recommended ways to run an optimization](../../basic/recommendedimplementation_02/RecommendedImplementation.md): the recommended run pattern, a synchronous run and the legacy callback methods.
- [Expert examples](../README.md): the overview of all Expert examples.

---

Added: 2026-02-17 (JOpt core 7.5.3-j17) | Last updated: 2026-10-21
