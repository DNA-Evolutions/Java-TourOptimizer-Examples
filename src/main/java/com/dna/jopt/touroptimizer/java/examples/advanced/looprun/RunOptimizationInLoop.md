# Running the genetic evolution stage in a loop

`RunOptimizationInLoopExample` runs the genetic evolution stage in continuous mode, in which the stage starts again after each loop, and ends it from its own code after three loops with `jumpToNextStage()`. The code counts the loops from the progress events. The page is for developers who decide in their own code when a stage of the run ends. It builds on the event API that [Recommended ways to run an optimization](../../basic/recommendedimplementation_02/RecommendedImplementation.md) describes.

## What the example shows

In advanced scenarios you may want to run the optimizer in a **controlled loop**, for example:

* run a fixed number of loops in a specific stage (e.g., Genetic phase),
* inspect intermediate results or metrics after each loop,
* stop early once you reached a quality threshold,
* chain the run into an application-specific pipeline.

- **Start modes.** `setOptimizationAlgorithmStart(modeSA, modeGE)` takes a start mode for simulated annealing and one for the genetic evolution stage. A stage in `STATE_MODE_BATCH` runs for its number of iterations; a stage in `STATE_MODE_CONTINIOUS` runs until it is stopped. The example passes `STATE_MODE_BATCH` for simulated annealing and `STATE_MODE_CONTINIOUS` for the genetic evolution stage.
- **One loop.** In continuous mode, `JOptExitCondition.JOptGenerationCount` does not end the genetic evolution stage. It sets the length of one loop: the stage runs that many generations and then starts again.
- **The loop counter.** The progress subscription counts a loop each time an event of stage 2 reports 99.0 percent. Stage 2 is the genetic evolution stage: [Reading the full progress of a run](../readoutfullprogress/ReadOutFullProgress.md) prints the stage numbers of the same three stages, 0 for `SmartConstructionAlgo`, 1 for `SmartSimulatedAnnealingAlgo` and 2 for `GeneticEvolution`. In the runs of this class, the counter fired only on lines of `GeneticEvolution`.
- **The end of the stage.** After the third loop the code calls `jumpToNextStage()`, which ends the running stage. The genetic evolution stage is the last stage of this run, so the run then ends with its result. Without the call, the stage would run until it is stopped.

## Scenario

The dates, times and coordinates are sample values.

- **Resource:** `Jack` starts at 50.775346, 6.083887 (Aachen) and works on May 6 and 7, 2020, from 08:00 to 17:00 (Europe/Berlin). His maximal working time is 13 hours, so the day with Nuernberg (about 12 hours) needs no overtime, and his maximal distance 1200 km.
- **Nodes:** nine `TimeWindowGeoNode` objects: Koeln, Oberhausen, Essen, Dueren, Nuernberg, Heilbronn, Stuttgart, Wuppertal and Aachen. They are open on May 6 and 7, 2020, from 08:00 to 17:00. Each visit takes 20 minutes, and all nodes have the importance 1. The Aachen node lies at Jack's start position.

`addProperties` sets `JOptExitCondition.JOptGenerationCount` to 1000 and `JOpt.Algorithm.PreOptimization.SA.NumIterations` to 100000. Both are below their defaults, which keeps the run short; in continuous mode the 1000 generations are the length of one loop. The defaults are in the [property reference](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties).

## How the code works

The source is [RunOptimizationInLoopExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/looprun/RunOptimizationInLoopExample.java). `example()` runs the steps in order:

```java
// The public evaluation license, or your own license if you put a license file where
// ExampleLicenseHelper looks for it.
ExampleLicenseHelper.setLicense(this);
addProperties(this);
addNodes(this);
addResources(this);
setStartModes(this);
attachToObservables(this);
startAndPresentResult(this);
```

- `ExampleLicenseHelper.setLicense(this)` uses your license file if you put one where the helper looks for it, and the public evaluation license otherwise.
- `addProperties` sets the two properties, `addNodes` adds the nine nodes and `addResources` adds Jack.
- `setStartModes` passes the start modes for the two stages to `setOptimizationAlgorithmStart`.
- `attachToObservables` counts the loops and ends the stage, and prints the progress, warning, status and error events. It subscribes with the event API, before the run starts.
- `startAndPresentResult` starts the run, waits up to 5 minutes for the result and prints it.

### The start modes

```java
OptimizationAlgorithmStart modeSA = OptimizationAlgorithmStart.STATE_MODE_BATCH;
OptimizationAlgorithmStart modeGE = OptimizationAlgorithmStart.STATE_MODE_CONTINIOUS;
opti.setOptimizationAlgorithmStart(modeSA, modeGE);
```

`STATE_MODE_CONTINIOUS` is the name of the constant in the API.

### The loop counter

The counter is an `AtomicInteger`, because the subscription is a lambda and cannot change a local `int`. The subscription prints each progress line and counts the loops:

```java
opti.getOptimizationEvents()
    .progressSubject()
    .subscribe(
        p -> {
          System.out.println(p.getProgressString());
          // Stage 2 is the genetic evolution stage. The code counts a loop each time the
          // stage reports 99.0 percent.
          if (p.getOptimizationStage() == 2 && p.getProgress() == 99.0) {
            int done = loopCount.incrementAndGet();
            System.out.println("Done with loop: " + done + " of " + MAX_LOOP_COUNT_GE);
            if (done == MAX_LOOP_COUNT_GE) {
              System.out.println("Max loop count reached. Stopping stage...");
              // Ends the running stage; the run then continues with the next stage.
              opti.jumpToNextStage();
            }
          }
        });
```

`MAX_LOOP_COUNT_GE` is 3. The test compares the progress with exactly 99.0; in the runs, the genetic evolution stage reported that value three times.

## Output and what to look for

The progress lines were the same in four test runs. The construction and the simulated annealing stage ran once each, and the genetic evolution stage started three times. The cost of the best solution was 1189.6735989443173 in every progress line of the genetic evolution stage and in the result.

The first loop reports its progress up to 98.0 percent. The second loop starts with a line at 99.0 percent, so the first count follows the second start of the stage:

```text
StatusEvent(OPTIMIZATION_ALGO_REPDONE, GeneticEvolution)
StatusEvent(OPTIMIZATION_ALGO_STARTING, GeneticEvolution)
PC 99.0, RE 1, AL GeneticEvolution, JC 1189.6735989443173, RC 2, EC 9, TC 11, TT[h] 16, TU[%] 17, TD[km] 1102.096,
Done with loop: 1 of 3
PC 0.0, RE 1, AL GeneticEvolution, JC 1189.6735989443173, RC 2, EC 9, TC 11, TT[h] 16, TU[%] 17, TD[km] 1102.096,
```

The second loop ends with a line at 99.0 percent and the second count. The third loop starts with a line at 100.0 percent and ends with the third count, after which the code ends the stage and the run finishes:

```text
PC 98.0, RE 1, AL GeneticEvolution, JC 1189.6735989443173, RC 2, EC 9, TC 11, TT[h] 16, TU[%] 17, TD[km] 1102.096,
PC 99.0, RE 1, AL GeneticEvolution, JC 1189.6735989443173, RC 2, EC 9, TC 11, TT[h] 16, TU[%] 17, TD[km] 1102.096,
Done with loop: 3 of 3
Max loop count reached. Stopping stage...
StatusEvent(OPTIMIZATION_ALGO_REPDONE, GeneticEvolution)
StatusEvent(OPTIMIZATION_DONE, Optimization done)
```

### The routes

The result has one route per day. Route 1 on May 7 visits Nuernberg, Stuttgart and Heilbronn and has no violation. It ends at 19:58:18, after Jack's working hours end at 17:00, and its route time of 718.31 minutes stays within his 13 hours. Time after the end of the working hours counts against the maximal working time, and the working hours are a frame for matching resources with the opening hours of the nodes, so a route can end after its working hours without a violation.

Route 0 on May 6 visits the six other nodes. The Aachen node was its first stop in some test runs and its last stop in others, at the same cost, so the times of route 0 can change between runs.

## Why this pattern is useful

### 1) Deterministic compute budgeting
Instead of relying on:
- wall clock time,
- or a global generation count,
you can budget compute per stage, e.g.:
- "Run GE exactly 3 cycles."
- "Run SA exactly 1 cycle."
- "Stop after N construction attempts."

### 2) Stage-specific control
Some instances respond well to:
- a short SA run,
- then more GE,
or vice versa.

Stage loop control allows you to align compute with:
- problem structure,
- business deadlines,
- or runtime SLAs.

### 3) Hook point for custom monitoring and decision rules
Once you have a loop boundary, you can:
- capture intermediate KPIs,
- track best-so-far improvements,
- stop early if improvement stalls,
- implement escalation logic (e.g., enable AutoFilter, change weights, add more SA).

## Understanding "stage" and "progress" in this example

### `p.getProgress()`
Progress is reported as a percentage-like number.  
The example uses:
- `99.0` to detect "end of loop".

Important nuance:
- This is an implementation coupling to the solver's progress reporting behavior.
- It is intentionally simple; in the runs of this page, the first count came on the first line of the second loop (see "Output and what to look for").
- For mission-critical systems, you may want to additionally guard against:
  - repeated 99.0 events,
  - stage transitions,
  - or use a more explicit "loop end" indicator if your architecture provides one.

## Relationship to standard exit conditions

The example still configures normal exit conditions:

- `JOptExitCondition.JOptGenerationCount = 1000`
- `JOpt.Algorithm.PreOptimization.SA.NumIterations = 100000`

These remain relevant because:
- the generation count and the SA settings still configure the two algorithms,
- the simulated annealing stage runs in batch mode, for its configured number of iterations.

Because the genetic stage runs in continuous mode, the loop counter and `jumpToNextStage()` end it in this example.

Think of "loop run" as an additional control layer:
- ExitCondition limits the maximum in batch mode; in continuous mode it sets the length of one loop,
- Loop control can stop earlier under your logic; in continuous mode it is what ends the stage in this example.

## Recommended production patterns

### Pattern A: Stop after N loops and export intermediate results
After each loop boundary:
- export `toOverviewResult()` for telemetry,
- optionally export KML for debugging,
- persist "best so far" for fail-safe.

### Pattern B: Adaptive stopping based on improvement
Replace the hard loop limit with:
- "Stop if best cost improved less than X% over the last N loops."

You can implement this by:
- capturing `p` (progress event) data,
- reading best result snapshots (if available),
- and calling `jumpToNextStage()` once the improvement stalls.

### Pattern C: Progressive tightening
You can run early loops with:
- relaxed weights,
- relaxed AutoFilter settings,
then tighten them after N loops.

This is often effective when you want:
- quick feasibility first,
- then quality improvements.

## Pitfalls and how to avoid them

### Pitfall 1: Using a fragile loop boundary signal
The example uses `p.getOptimizationStage() == 2 && p.getProgress() == 99.0`.

If you find in your environment that:
- progress reports multiple times at 99.0,
- or progress granularity changes,
then add a guard, for example:
- remember last seen progress per stage,
- increment only on transition into 99.0,
- or require a time delta.

### Pitfall 2: Stopping too early
Stopping GE too early may yield:
- a feasible but low-quality plan.

Mitigation:
- combine loop control with KPI-based stopping,
- or allow fallback: if result quality is poor, run additional loops.

### Pitfall 3: Misinterpreting stages
Stage numbering is framework-specific.  
Do not hard-code stage indices without confirming which stage you are controlling in your integration.

In this example, stage `2` is explicitly commented as the genetic phase.

## Summary

- The example shows how to run optimization in a controlled loop by using progress events (`progressSubject()`).
- It counts "loops" of the genetic stage by watching for `p.getOptimizationStage() == 2` and `p.getProgress() == 99.0`.
- After `MAX_LOOP_COUNT_GE` loops (3), it calls `jumpToNextStage()` to exit the stage, which in continuous mode would otherwise run until it is stopped.
- This pattern is valuable for deterministic compute budgeting, stage-specific control, and adaptive optimization pipelines.

## Run it

Run the `main(String[] args)` method of `RunOptimizationInLoopExample` from your IDE, with the repository imported as a Maven project. You need a JDK in the version that the examples pom sets. You need no license file: without one, the example uses the public evaluation license (see the [license guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/license)).

## Related

- [CustomCostConvergenceExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/externalcostconvergence/CustomCostConvergenceExample.java): an Expert example that defines a cost convergence criterion, which stops the optimization once it is reached.
- [Progress output frequency and progress requests](../progressfrequency/OptimizationProgress.md): sets how often a stage reports its progress.
- [Reading the full progress of a run](../readoutfullprogress/ReadOutFullProgress.md): prints the stage number and the values of the best solution from each progress event.
- [Requesting a result during the run](../requestresult/RequestResult.md): asks the running stage for the best solution so far.
- [Reactive events and streams](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/reactive_events): the feature guide of the event API.
- [Recommended ways to run an optimization](../../basic/recommendedimplementation_02/RecommendedImplementation.md): the recommended run pattern, a synchronous run and the legacy callback methods.
- [Advanced examples](../README.md): the overview of all Advanced examples.

---

Added: 2026-02-17 (JOpt core 7.5.3-j17) | Last updated: 2026-10-23
