# Progress output frequency and progress requests

Two examples control when a run reports its progress. `ChangeOnProgressDefaultFrequencyExample` sets how often the simulated annealing stage and the genetic evolution stage report, with one property for each stage, and `RequestOnProgressViaExternalTimeExample` asks the running stage for a progress event every 500 milliseconds from a timer. The page is for developers who show or log the progress of a run. Both classes read the progress with the event API that [Recommended ways to run an optimization](../../basic/recommendedimplementation_02/RecommendedImplementation.md) describes.

## What the example shows

JOpt.TourOptimizer exposes **progress events** during an optimization run so you can:
* stream status into a UI,
* log intermediate improvements,
* implement early stopping or stage switching,
* export intermediate "best so far" snapshots.

- **Progress events.** `getOptimizationEvents().progressSubject()` delivers an `IOptimizationProgress` for each progress event. Both classes print its `getProgressString()`, a line that starts with the progress in percent of the running stage (`PC`) and names the algorithm of the stage (`AL`).
- **A step per stage.** `JOpt.Algorithm.PreOptimization.SA.OnProgressOutPercentage` sets the step between two progress events of the simulated annealing stage, and `JOpt.Algorithm.GE.OnProgressOutPercentage` the step of the genetic evolution stage, both in percent of the stage. `ChangeOnProgressDefaultFrequencyExample` sets 0.1 and 0.01. The construction stage keeps its own step.
- **At most one event per generation.** The genetic evolution stage reports at most once per generation. With 1000 generations, a step of 0.01 percent would be a tenth of a generation, so `ChangeOnProgressDefaultFrequencyExample` runs 10000 generations.
- **Requests from outside the run.** `requestProgress()` asks the running stage for a progress event. The event arrives on the same `progressSubject()`, so the subscription prints it like the others. A request while no stage runs does nothing, so the timer starts after `startRunAsync()`.

## Scenario

The dates, times and coordinates are sample values.

- **Resource:** `Jack` starts at 50.775346, 6.083887 (Aachen) and works on May 6 and 7, 2020, from 08:00 to 17:00 (Europe/Berlin). His maximal working time is 13 hours, so the day with Nuernberg (about 12 hours) needs no overtime, and his maximal distance 1200 km.
- **Nodes:** nine `TimeWindowGeoNode` objects: Koeln, Oberhausen, Essen, Dueren, Nuernberg, Heilbronn, Stuttgart, Wuppertal and Aachen. They are open on May 6 and 7, 2020, from 08:00 to 17:00. Each visit takes 20 minutes, and all nodes have the importance 1. The Aachen node lies at Jack's start position.

`ChangeOnProgressDefaultFrequencyExample` sets four properties:

- `JOpt.Algorithm.PreOptimization.SA.NumIterations` to 100000, below its default, which keeps the run short.
- `JOptExitCondition.JOptGenerationCount` to 10000, so that 0.01 percent of the genetic evolution stage is one generation.
- `JOpt.Algorithm.PreOptimization.SA.OnProgressOutPercentage` to 0.1 and `JOpt.Algorithm.GE.OnProgressOutPercentage` to 0.01.

`RequestOnProgressViaExternalTimeExample` sets `JOptExitCondition.JOptGenerationCount` to 1000 and `JOpt.Algorithm.PreOptimization.SA.NumIterations` to 100000. Both are below their defaults, which keeps the run short. The class sets no progress step, so each stage reports at its default step.

The defaults are in the [property reference](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties).

## How the code works

The sources are [ChangeOnProgressDefaultFrequencyExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/progressfrequency/ChangeOnProgressDefaultFrequencyExample.java) and [RequestOnProgressViaExternalTimeExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/progressfrequency/RequestOnProgressViaExternalTimeExample.java). `example()` runs the same steps in both classes:

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
- `addProperties` sets the properties of the class.
- `addNodes` adds the nine nodes, and `addResources` adds Jack.
- `attachToObservables` subscribes to the progress, warning, status and error events with the event API, before the run starts.
- `startAndPresentResult` starts the run, waits up to 5 minutes for the result and prints it. In `RequestOnProgressViaExternalTimeExample` it also starts and stops the timer.

### The progress steps

`addProperties` of `ChangeOnProgressDefaultFrequencyExample` sets the length of the run and the two steps:

```java
// Fewer iterations than the default keep the run short.
props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumIterations", "100000");
// 10000 generations, so that 0.01 percent of the genetic stage is one generation. The stage
// reports at most once per generation, so with fewer generations the step below would be
// coarser than 0.01 percent.
props.setProperty("JOptExitCondition.JOptGenerationCount", "10000");

// A progress event every 0.1 percent of the simulated annealing stage.
props.setProperty("JOpt.Algorithm.PreOptimization.SA.OnProgressOutPercentage", "0.1");
// A progress event every 0.01 percent of the genetic evolution stage.
props.setProperty("JOpt.Algorithm.GE.OnProgressOutPercentage", "0.01");
```

`attachToObservables` prints every progress event:

```java
// Subscribe before the start: the subjects replay only a few past events.
opti.getOptimizationEvents()
    .progressSubject()
    .subscribe(p -> System.out.println(p.getProgressString()));
```

### The progress requests

`startAndPresentResult` of `RequestOnProgressViaExternalTimeExample` starts the run, then the timer, and stops the timer after the wait, also when the wait fails:

```java
CompletableFuture<IOptimizationResult> resultFuture = opti.startRunAsync();
// The timer starts once startRunAsync() has returned, because a request while no stage of
// the run is running does nothing. The first request can still come before the first stage
// starts.
Timer progressRequestTimer = startProgressRequests(opti);
try {
  // Wait for the result; without the wait the JVM would end before the run does.
  IOptimizationResult result = resultFuture.get(5, TimeUnit.MINUTES);
  System.out.println(result);
} finally {
  // Stop the timer also when the wait fails: its thread would keep the JVM running.
  progressRequestTimer.cancel();
}
```

`startProgressRequests` schedules a `ProgressRequestTask` on a `java.util.Timer`, without a delay and then every 500 milliseconds, a sample value. Each time, the task prints a line and asks for a progress event:

```java
@Override
public void run() {
  System.out.println("Requesting progress");
  this.opti.requestProgress();
}
```

The requested events arrive on `progressSubject()`, so `attachToObservables` prints them with the others.

## Output and what to look for

Both classes print the status events, the progress lines and the result.

### ChangeOnProgressDefaultFrequencyExample

In four test runs, the stages printed the same progress lines:

- `SmartConstructionAlgo`: 100 lines, in steps of 1 percent.
- `SmartSimulatedAnnealingAlgo`: 1000 lines, in steps of 0.1 percent.
- `GeneticEvolution`: 10000 lines, in steps of 0.01 percent.

The start of the simulated annealing stage, trimmed to its first three progress lines:

```text
StatusEvent(OPTIMIZATION_ALGO_STARTING, SmartSimulatedAnnealingAlgo)
PC 0.0, RE 1, AL SmartSimulatedAnnealingAlgo, JC 1189.6735989443173, RC 2, EC 9, TC 11, TT[h] 16, TU[%] 17, TD[km] 1102.096, T 250.0
PC 0.1, RE 1, AL SmartSimulatedAnnealingAlgo, JC 1189.6735989443173, RC 2, EC 9, TC 11, TT[h] 16, TU[%] 17, TD[km] 1102.096, T 247.481117645864
PC 0.2, RE 1, AL SmartSimulatedAnnealingAlgo, JC 1189.6735989443173, RC 2, EC 9, TC 11, TT[h] 16, TU[%] 17, TD[km] 1102.096, T 244.9876143649839
```

The start of the genetic evolution stage, trimmed in the same way:

```text
StatusEvent(OPTIMIZATION_ALGO_STARTING, GeneticEvolution)
PC 0.0, RE 1, AL GeneticEvolution, JC 1189.6735989443173, RC 2, EC 9, TC 11, TT[h] 16, TU[%] 17, TD[km] 1102.096,
PC 0.01, RE 1, AL GeneticEvolution, JC 1189.6735989443173, RC 2, EC 9, TC 11, TT[h] 16, TU[%] 17, TD[km] 1102.096,
PC 0.02, RE 1, AL GeneticEvolution, JC 1189.6735989443173, RC 2, EC 9, TC 11, TT[h] 16, TU[%] 17, TD[km] 1102.096,
```

In a test run with 1000 generations instead of 10000, the genetic evolution stage printed 1000 lines in steps of 0.1 percent: one line per generation, although the step was still 0.01 percent.

### RequestOnProgressViaExternalTimeExample

The timer printed `Requesting progress` every 500 milliseconds. The number of requests depends on the wall-clock time and can change between runs. During `SmartSimulatedAnnealingAlgo` and `GeneticEvolution`, most requests added a progress line whose percentage lies between the whole-percent lines of the stage, right after the request line or after one regular line. Whether a request adds a line, and its percentage, can change between runs.

Requests outside these two stages added no progress line. In four test runs, the first request came before the construction stage started:

```text
Requesting progress
StatusEvent(OPTIMIZATION_ALGO_STARTING, SmartConstructionAlgo)
```

One or more requests came after the end of the construction stage and before the start of simulated annealing; their number depends on the wall-clock time as well.

### The routes

The result of both classes has one route per day. In four test runs of each class, route 1 on May 7 visits Nuernberg, Stuttgart and Heilbronn and has no violation. It ends at 19:58:18, after Jack's working hours end at 17:00, and its route time of 718.31 minutes stays within his 13 hours. Time after the end of the working hours counts against the maximal working time, and the working hours are a frame for matching resources with the opening hours of the nodes, so a route can end after its working hours without a violation.

Route 0 on May 6 visits the six other nodes. In both classes the Aachen node was the first stop of route 0 in some test runs and the last stop in others, at the same cost, so the times of route 0 can change between runs.

## The two progress callback signatures

The legacy callback style implements two progress callbacks. The examples of this page implement neither: they subscribe to `progressSubject()` of the event API, which delivers the same structured progress object (the legacy style is in [Recommended ways to run an optimization](../../basic/recommendedimplementation_02/RecommendedImplementation.md)). The legacy callback methods (`onProgress`, `onAsynchronousOptimizationResult` and the others) subscribe to the same events internally and stay supported; the event API is recommended.

### 1) `onProgress(String winnerProgressString)`
This callback provides a **human-readable progress string** (suitable for logs). It is deprecated: the same string is the `getProgressString()` of the progress event.

The examples print this string from the progress event:
- `System.out.println(p.getProgressString())`

This is often the easiest integration point for:
- quick logging,
- CLI tools,
- early debugging.

### 2) `onProgress(IOptimizationProgress rapoptProgress)`
This callback provides a **structured progress object**, typically used for:
- UI progress bars,
- stage-based logic (e.g., "if we are in Genetic stage and progress is 99%..."),
- telemetry and metrics.

In the provided examples the structured progress object arrives through `progressSubject()`, and the subscription prints only its `getProgressString()`, because the focus is on *when* progress events occur and how to request them.

## Part 1: Changing the default progress update frequency (per stage)

Example: `ChangeOnProgressDefaultFrequencyExample`

### What it solves
Some optimizations run for minutes or hours. If you emit progress updates too frequently, you may:
- slow down the optimizer (logging overhead),
- flood a UI or message bus,
- generate excessive storage.

If you emit progress too rarely, a UI feels "stuck" and you lose observability.

This example shows how to configure **different progress output percentages** per stage.

### The key properties
The example sets:

- `JOpt.Algorithm.PreOptimization.SA.OnProgressOutPercentage = 0.1`
- `JOpt.Algorithm.GE.OnProgressOutPercentage = 0.01`

Interpretation:
- In the **SA pre-optimization stage**, emit progress output every **0.1%**.
- In the **GE genetic stage**, emit progress output every **0.01%**.

This is a practical pattern:
- SA can be very iteration-heavy, so a moderate rate is often enough.
- GE progress can be more "visible" to users (e.g., improving winner), so a finer rate may be desirable.

### Why "per stage" matters
Different stages have different dynamics:
- SA often iterates many small changes and can produce extremely frequent progress events.
- GE typically runs generations, and users often want more granular insight into improvements.

Per-stage configuration avoids the "one size fits all" problem.

## Part 2: Requesting progress snapshots on demand (external timer)

Example: `RequestOnProgressViaExternalTimeExample`

### What it solves (Part 2)
Sometimes you do not want progress to be tied to the solver's internal loop counters.
Instead, you want progress at a **wall-clock frequency**, e.g.:
- every 500 ms,
- every 2 seconds,
- on a user action ("refresh progress now"),
- on an external orchestrator tick.

This example shows exactly that: it attaches a `Timer` that calls:

- `opti.requestProgress();`

every **500 ms**.

### What `requestProgress()` does (practical interpretation)
`requestProgress()` asks the optimizer to **publish a progress update** at the next safe opportunity.
It is not meant to "interrupt" the solver; it is meant to:
- surface the most recent progress state,
- without waiting for the next internally scheduled output boundary.

## Recommended usage patterns (production-grade)

### Pattern A (UI progress): combine both mechanisms
A robust pattern for UIs is:

- set a conservative internal progress output percentage (property-driven),
- additionally request progress every 0.5 to 2 seconds from the UI layer.

This ensures:
- stable baseline updates even if the UI timer fails,
- user-visible smooth progress even if the solver's internal stage loop is coarse.

### Pattern B (Logging): keep internal output less frequent
For server environments, logging is expensive. Consider:
- `OnProgressOutPercentage` of 0.1 to 1.0 depending on the stage,
- and avoid printing very frequently in high-CPU scenarios.

### Pattern C: Early stopping or stage control using structured progress
If you want to implement logic such as:
- "stop after N loops",
- "switch stage if improvement stalls",
use the structured progress object:
- `progressSubject()`, or in the legacy style the callback `onProgress(IOptimizationProgress ...)`

and implement policy-based logic there, rather than parsing the string output.

(See also the pattern used in `RunOptimizationInLoopExample`, where progress stage and percentage are used to count loops.)

## Performance and stability considerations

### 1) Progress events are not free
Every progress event can trigger:
- string construction,
- logging,
- UI updates,
- and in some integrations network calls.

Set your frequencies consciously.

### 2) Avoid heavy work in `progressSubject().subscribe(...)`
If you do heavy processing (exports, DB writes) inside progress callbacks, you can slow down optimization.

Best practice:
- push minimal events to a queue,
- process them asynchronously in your application layer.

### 3) Timer threads and lifecycle
If you use external timers:
- always cancel them after completion,
- ensure they are not creating unbounded background threads in long-lived services.

The example cancels the timer explicitly:
- `progressRequestTimer.cancel();`

### 4) Thread safety
Progress callbacks may be invoked from optimization threads. Therefore:
- treat `progressSubject().subscribe(...)` (and `onProgress(...)`) as multi-threaded code,
- use thread-safe structures or synchronization if you update shared state.

## Summary

- JOpt exposes progress via:
  - `progressSubject()`, which delivers an `IOptimizationProgress` for structured, stage-aware integration, with its `getProgressString()` for human-readable updates,
  - in the legacy callback style, `onProgress(String winnerProgressString)` (deprecated) for human-readable updates and `onProgress(IOptimizationProgress ...)` for structured, stage-aware integration.

- You can control default progress output frequency per stage using properties such as:
  - `JOpt.Algorithm.PreOptimization.SA.OnProgressOutPercentage`
  - `JOpt.Algorithm.GE.OnProgressOutPercentage`

- You can request progress at arbitrary times (e.g., wall-clock interval) using:
  - `opti.requestProgress()`

  In the runs, a request added a progress line only during simulated annealing and genetic evolution.

Together, these mechanisms provide precise control over observability, UI responsiveness, and runtime overhead.

## Run it

Run the `main(String[] args)` method of either class from your IDE, with the repository imported as a Maven project. You need a JDK in the version that the examples pom sets. You need no license file: without one, the examples use the public evaluation license (see the [license guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/license)). `ChangeOnProgressDefaultFrequencyExample` prints one line per progress event, more than 11000 lines in all.

## Related

- [Reactive events and streams](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/reactive_events): the feature guide of the event API, with the progress requests and the two progress properties.
- [Reading the full progress of a run](../readoutfullprogress/ReadOutFullProgress.md): reads the cost, the counts, the times and the distance of the best solution so far from each progress event.
- [Running the genetic evolution stage in a loop](../looprun/RunOptimizationInLoop.md): counts the loops of a stage from its progress events.
- [Requesting a result during the run](../requestresult/RequestResult.md): asks the running stage for the best solution so far instead of a progress event.
- [Optimization properties](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties): the property reference, with the two progress properties and their defaults.
- [Recommended ways to run an optimization](../../basic/recommendedimplementation_02/RecommendedImplementation.md): the recommended run pattern, a synchronous run and the legacy callback methods.
- [Advanced examples](../README.md): the overview of all Advanced examples.

---

Added: 2026-02-17 (JOpt core 7.5.3-j17) | Last updated: 2026-10-23
