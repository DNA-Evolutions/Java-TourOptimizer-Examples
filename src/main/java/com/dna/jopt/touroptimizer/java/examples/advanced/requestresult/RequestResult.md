# Requesting a result during the run

`RequestResultExample` asks for the best solution so far while the optimization goes on: when a stage reports 20 percent, the code calls `requestResult()`, and the requested result arrives on `requestedResultSubject()`. The page is for developers who show or save an intermediate plan before a run ends. It builds on the event API that [Recommended ways to run an optimization](../../basic/recommendedimplementation_02/RecommendedImplementation.md) describes.

## What the example shows

For long-running optimizations, it is often valuable to fetch an **intermediate best-so-far solution** while the solver continues to run. Typical reasons include:

* show a usable route plan early in a UI,
* support "preview" or "save interim result" buttons,
* export a snapshot every N minutes,
* implement staged decision pipelines (e.g., early feasibility, later quality improvements).

JOpt supports this via **asynchronous result requests**:
* the optimization continues running,
* and you can request the current best solution to be delivered through a dedicated subject, `requestedResultSubject()` (in the legacy callback style through a dedicated callback).

- **The request.** `requestResult()` asks the running stage for a result. For each request, the stage builds a complete `IOptimizationResult` from its best solution so far and then goes on, so the run does not stop.
- **The requested results.** They arrive on `getOptimizationEvents().requestedResultSubject()`, a subject of its own. The final result still arrives through the future of `startRunAsync()`.
- **The trigger.** The example requests a result from the progress subscription, when `getProgress()` is 20.0, a sample value. In the runs each stage reported 20.0 once, so the run got three requests: one in `SmartConstructionAlgo`, one in `SmartSimulatedAnnealingAlgo` and one in `GeneticEvolution`.
- **The JSON level, which the example does not use.** `IJSONOptimization`, implemented by `JSONOptimization`, runs an optimization that is defined as an `OptimizationConfig<JSONConfig>`, and its `startAsynchConfigFuture` returns the final result in that form. Its method `requestAsynchConfigObservable(config, schemeOpt)` calls `requestResult()` and returns `requestedResultSubject()` as an `Observable<OptimizationConfig<JSONConfig>>`, with each requested result converted to the same form.

## Scenario

The dates, times and coordinates are sample values.

- **Resources:** `Jack` and `John` both start at 50.775346, 6.083887 (Aachen). Both work on March 6, 2020, from 08:00 to 17:00 and on March 7, 2020, from 08:00 to 20:00 (Europe/Berlin). Jack may work 8 hours per day, John 14 hours; both limits are sample values. The maximal distance is 1200 km for both.
- **Nodes:** eight `TimeWindowGeoNode` objects: Koeln, Koeln1 at the same position as Koeln, Oberhausen, Essen, Heilbronn, Stuttgart, Wuppertal and Aachen. They are open on March 6 and 7, 2020, from 08:00 to 17:00. Each visit takes 20 minutes, and all nodes have the importance 1. The Aachen node lies at the start position of both resources.

`addProperties` sets `JOptExitCondition.JOptGenerationCount` to 1000 and `JOpt.Algorithm.PreOptimization.SA.NumIterations` to 100000. Both are below their defaults, which keeps the run short. The defaults are in the [property reference](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties).

## How the code works

The source is [RequestResultExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/requestresult/RequestResultExample.java). `example()` runs the steps in order:

```java
// The public evaluation license, or your own license if you put a license file where
// ExampleLicenseHelper looks for it.
ExampleLicenseHelper.setLicense(this);
addProperties(this);
addNodes(this);
addResources(this);
attachToObservables(this);
startAndPresentResult(this);
```

- `ExampleLicenseHelper.setLicense(this)` uses your license file if you put one where the helper looks for it, and the public evaluation license otherwise.
- `addProperties` sets the two properties, `addNodes` adds the eight nodes and `addResources` adds Jack and John, with the working hours of `createWorkingHours`.
- `attachToObservables` requests the results and prints them, together with the progress, warning, status and error events.
- `startAndPresentResult` starts the run, waits up to 5 minutes for the final result and prints it.

### The request and the requested results

`attachToObservables` subscribes before the run starts. The progress subscription prints each progress line and requests a result at 20 percent; the subscription to `requestedResultSubject()` prints the overview of each requested result:

```java
// Subscribe before the start: the subjects replay only a few past events.
opti.getOptimizationEvents()
    .progressSubject()
    .subscribe(
        p -> {
          System.out.println(p.getProgressString());
          // 20 percent is a sample value. Each stage reports 20.0 percent once, so the run
          // gets one request per stage.
          if (p.getProgress() == 20.0) {
            System.out.println("Requesting a result at 20 percent of " + p.getCallerId());
            opti.requestResult();
          }
        });
// The requested results arrive here while the run goes on.
opti.getOptimizationEvents()
    .requestedResultSubject()
    .subscribe(
        r -> {
          System.out.println("Requested result:");
          System.out.println(r.toOverviewResult());
        });
```

`toOverviewResult()` gives the counts, the cost and the totals of a result without the routes. `startAndPresentResult` prints the final result in full.

## Output and what to look for

In four test runs the output held the same three requests, each followed by the overview of the requested result, with the same costs:

- `Requesting a result at 20 percent of SmartConstructionAlgo`: the requested result costs 946.000137565664.
- `Requesting a result at 20 percent of SmartSimulatedAnnealingAlgo` and `Requesting a result at 20 percent of GeneticEvolution`: both requested results cost 847.033054487167, the cost of the final result.

The request in the simulated annealing stage, with the progress lines before and after it. The line with the number of scheduled routes is left out: it was 1 in two of the runs and 2 in the other two, because the routes can change between runs (see below).

```text
PC 20.0, RE 1, AL SmartSimulatedAnnealingAlgo, JC 847.033054487167, RC 4, EC 8, TC 12, TT[h] 12, TU[%] 21, TD[km] 784.192, T 29.81072970893301
Requesting a result at 20 percent of SmartSimulatedAnnealingAlgo
Requested result:

-----------------------------------------------------------
--------------------- RESULTS -----------------------------
-----------------------------------------------------------
 Number of Route         : 4
 Total Route Elements    : 10
 Cost                    : 847.033054487167
-----------------------------------------------------------
 Total time        [h]   : 12
 Total idle time   [h]   : 0
 Total prod. time  [h]   : 2
 Total tran. time  [h]   : 9
 Total utilization [%]   : 21
 Total distance    [km]  : 784
 Termi. time       [h]   : 4
 Termi. distance   [km]  : 318
PC 21.0, RE 1, AL SmartSimulatedAnnealingAlgo, JC 847.033054487167, RC 4, EC 8, TC 12, TT[h] 12, TU[%] 21, TD[km] 784.192, T 26.939728016002057
```

The progress line after the overview shows that the stage went on after the request. `StatusEvent(OPTIMIZATION_DONE, Optimization done)` follows the last request, and then the final result.

### The routes

The Aachen node lies at the start position of both resources, and which resource visits it can change between runs, at the same cost of 847.033054487167. The four test runs showed two outcomes on March 6:

- In two runs, Jack visited only Aachen and John the other seven nodes. John's route ended at 20:14:05, with a route time of 734.09 minutes.
- In the other two runs, John visited all eight nodes, starting with Aachen, and Jack had no route. John's route ended at 20:34:05, with a route time of 754.09 minutes.

In both outcomes John's route ends after his working hours end at 17:00, stays within his 14 hours and has no violation. Time after the end of the working hours counts against the maximal working time, and the working hours are a frame for matching resources with the opening hours of the nodes, so a route can end after its working hours without a violation.

## Two different result callbacks (critical distinction)

The legacy callback style overrides two result callbacks. The example overrides neither: it receives the requested results on `requestedResultSubject()` and the final result through the future of `startRunAsync()`. The legacy callback methods (`onProgress`, `onAsynchronousOptimizationResult` and the others) subscribe to the same events internally and stay supported; the event API is recommended.

### 1) `onAsynchronousOptimizationResult(IOptimizationResult result)`
This callback is the "standard" asynchronous result callback.  
It is called when the optimization has a result to report via its normal lifecycle.

A sketch, not code of the example classes:
```java
@Override
public void onAsynchronousOptimizationResult(IOptimizationResult rapoptResult) {
  System.out.println(rapoptResult);
}
```

### 2) `requestedAsynchronousOptimizationResult(IOptimizationResult result)`
This callback is invoked **only** when you explicitly request a result snapshot via `requestResult()`.

A sketch, not code of the example classes:
```java
@Override
public void requestedAsynchronousOptimizationResult(IOptimizationResult rapoptResult) {
  System.out.println(rapoptResult);
}
```

Practical guidance:
- Use `requestedResultSubject()`, or `requestedAsynchronousOptimizationResult(...)` in the legacy style, for "snapshot" handling (UI preview, periodic export).
- Use the future of `startRunAsync()`, or `onAsynchronousOptimizationResult(...)` in the legacy style, for the normal end-of-run or internal result reporting flow.

## How the example requests a snapshot

The snapshot request is triggered from the **progress subscription** (`progressSubject()`), which receives the structured progress object:

```java
if (p.getProgress() == 20.0) {
  System.out.println("Requesting a result at 20 percent of " + p.getCallerId());
  opti.requestResult();
}
```

Interpretation:
- As soon as the solver reports **20% progress** (for the current stage/caller), the code requests an intermediate result.
- The optimizer then responds by publishing the requested result on:
  - `requestedResultSubject()` (in the legacy style it invokes `requestedAsynchronousOptimizationResult(IOptimizationResult ...)`)

This is a clean pattern because:
- you request snapshots based on solver state,
- without stopping the optimization run.

## Running asynchronously (and why `resultFuture.get(5, TimeUnit.MINUTES)` matters)

The example starts the solver asynchronously:

```java
CompletableFuture<IOptimizationResult> resultFuture = opti.startRunAsync();
// Wait for the result; without the wait the JVM would end before the run does.
IOptimizationResult result = resultFuture.get(5, TimeUnit.MINUTES);
```

Important:
- the `.get(...)` call blocks the main thread.
- Without blocking (or without another mechanism keeping the JVM alive), the program can terminate and the optimization would be aborted.

In production systems (services, UIs), you usually keep the process alive anyway; the key takeaway is:
- snapshot requests occur while the run is active.

## Recommended production patterns

### Pattern A: UI "Preview now" button
- The UI calls into your backend.
- Your backend triggers `requestResult()`.
- Handle the snapshot in the subscription to `requestedResultSubject()` (in the legacy style in `requestedAsynchronousOptimizationResult(...)`) by:
  - serializing the current best solution,
  - returning it to the UI,
  - continuing optimization.

### Pattern B: Periodic snapshots (every N seconds)
Instead of requesting at "progress == 20.0", you can request on a timer:
- every 2 to 10 seconds for interactive UIs,
- every 30 to 120 seconds for long-running batch jobs.

Important best practice:
- debounce requests (avoid multiple concurrent snapshot requests too close together).

### Pattern C: Event-based snapshot conditions
Request snapshots when:
- cost improves by more than X%,
- constraint violations drop to 0,
- a stage finishes,
- a maximum compute budget is reached.

This creates "meaningful snapshots" instead of purely periodic ones.

## Performance and safety considerations

### 1) Snapshot creation has cost
Delivering a full `IOptimizationResult` snapshot requires work:
- packaging the best entity,
- generating result data structures.

Therefore:
- do not request snapshots too frequently in very large instances.

### 2) Keep snapshot handlers lightweight
Inside the subscription to `requestedResultSubject()` (or `requestedAsynchronousOptimizationResult(...)` in the legacy style), avoid heavy work on the optimizer thread.
Preferred pattern:
- enqueue the result or a small derived summary,
- process exports or persistence asynchronously.

### 3) Be explicit about stage/context
In the example, the trigger condition uses:
- `p.getProgress() == 20.0`

In production, you often want to refine this:
- check stage/caller (if available),
- ensure you only request once per stage,
- avoid "progress == 20" triggering repeatedly (depending on progress reporting behavior).

## Summary

- `requestResult()` lets you fetch an intermediate "best-so-far" solution **without stopping the optimization**.
- Requested snapshots arrive via:
  - `requestedResultSubject()` (in the legacy style `requestedAsynchronousOptimizationResult(IOptimizationResult ...)`)
- Normal asynchronous results arrive via:
  - the future of `startRunAsync()` (in the legacy style `onAsynchronousOptimizationResult(IOptimizationResult ...)`)
- The example requests a snapshot at **20% progress** of each stage (three in the runs) and prints its overview, while keeping the run alive via `startRunAsync()` and `resultFuture.get(5, TimeUnit.MINUTES)`.

This feature is a strong foundation for interactive planning UIs and operational "good solution now, better solution later" workflows.

## Run it

Run the `main(String[] args)` method of `RequestResultExample` from your IDE, with the repository imported as a Maven project. You need a JDK in the version that the examples pom sets. You need no license file: without one, the example uses the public evaluation license (see the [license guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/license)).

## Related

- [Reactive events and streams](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/reactive_events): the feature guide of the event API, with `requestedResultSubject()` among the event subjects.
- [Progress output frequency and progress requests](../progressfrequency/OptimizationProgress.md): requests a progress event instead of a result, from a timer.
- [Reading the full progress of a run](../readoutfullprogress/ReadOutFullProgress.md): reads the cost, the counts, the times and the distance of the best solution so far from each progress event, without a request.
- [ReadJsonConfigAndRunExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/restful/readinput/ReadJsonConfigAndRunExample.java): a run of JSON input with `JSONOptimization` and `startAsynchConfigFuture`.
- [Recommended ways to run an optimization](../../basic/recommendedimplementation_02/RecommendedImplementation.md): the recommended run pattern, a synchronous run and the legacy callback methods.
- [Advanced examples](../README.md): the overview of all Advanced examples.

---

Added: 2026-02-17 (JOpt core 7.5.3-j17) | Last updated: 2026-10-23
