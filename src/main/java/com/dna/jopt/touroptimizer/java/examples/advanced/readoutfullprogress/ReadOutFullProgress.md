# Reading the full progress of a run

Each progress event of a run carries the best solution found so far, and two examples print its cost, counts, times and distance as one line per event. `ReadOutFullProgressExample` reads the values from the solution itself, and `ReadOutFullProgressWithUtilExample` reads them with `ParsedProgress`, a helper class of this repository. The page is for developers who log or display more of a run than its percentage. How often the events arrive is the topic of [Progress output frequency and progress requests](../progressfrequency/OptimizationProgress.md).

## What the example shows

JOpt.TourOptimizer can emit much richer information than a simple "percent done" number.  
If you subscribe to the progress stream, you can read out **live KPIs** for the *current best (winner) solution* while the optimizer is still running.

This is essential for:
* live dashboards (web UIs, planning workbenches),
* observability and telemetry (metrics + logs),
* early stopping / loop control ("stop if improvement stalls"),
* exporting "best-so-far" solutions during long runs.

- **The progress event.** `getOptimizationEvents().progressSubject()` delivers an `IOptimizationProgress` for each progress event. The classes read four things from it:
  - `getProgress()`: the progress in percent of the running stage.
  - `getCallerId()`: the name of the algorithm of the stage, for example `SmartSimulatedAnnealingAlgo`.
  - `getOptimizationStage()`: the number of the stage. In the runs, 0 is `SmartConstructionAlgo`, 1 is `SmartSimulatedAnnealingAlgo` and 2 is `GeneticEvolution`. Only `ReadOutFullProgressExample` prints it.
  - `getResultEntity()`: the best solution so far, an `IEntity`.
- **The best solution.** `ReadOutFullProgressExample` reads the cost (`getJoinedCost()`), the route count (`getRouteCount()`) and the element count (`getTotalRoutesOptimizableElementsCount()`) from the entity, and the transit, idle and productive time and the distance of all routes from `getJoinedDetailController()`.
- **The helper class.** `ParsedProgress` gives the same values through its own getters and adds the flex time and the termination transit time. `ReadOutFullProgressWithUtilExample` prints them all.
- **The construction stage.** While `SmartConstructionAlgo` runs, the time, the utilization and the distance print as 0 in both classes; the cost does not.

## Scenario

The dates, times and coordinates are sample values.

- **Resource:** `Jack` starts at 50.775346, 6.083887 (Aachen) and works on May 6 and 7, 2020, from 08:00 to 18:00 (Europe/Berlin). His maximal working time is 10 hours, the length of the working hours, and his maximal distance 1200 km.
- **Nodes:** five `TimeWindowGeoNode` objects: Koeln, Essen, Dueren, Wuppertal and Aachen. They are open on May 6 and 7, 2020, from 08:00 to 12:00. Each visit takes 20 minutes, and all nodes have the importance 1. The Aachen node lies at Jack's start position.

Both classes set `JOptExitCondition.JOptGenerationCount` to 1000 and `JOpt.Algorithm.PreOptimization.SA.NumIterations` to 100000. Both are below their defaults, which keeps the run short. The defaults are in the [property reference](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties).

## How the code works

The sources are [ReadOutFullProgressExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/readoutfullprogress/ReadOutFullProgressExample.java), [ReadOutFullProgressWithUtilExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/readoutfullprogress/ReadOutFullProgressWithUtilExample.java) and the helper class [ParsedProgress.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/util/progressparser/ParsedProgress.java). `example()` runs the same steps in both classes:

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
- `addProperties` sets the two properties, `addNodes` adds the five nodes and `addResources` adds Jack.
- `attachToObservables` subscribes to the progress, warning, status and error events with the event API, before the run starts. For each progress event it prints the line that `toProgressString` builds:

  ```java
  opti.getOptimizationEvents()
      .progressSubject()
      .subscribe(p -> System.out.println(toProgressString(p)));
  ```

- `startAndPresentResult` starts the run, waits up to 5 minutes for the result and prints it.

### The line of ReadOutFullProgressExample

`toProgressString` takes the best solution from the event and adds up the time of all routes:

```java
// The best solution so far.
IEntity winner = p.getResultEntity();

// The time of all routes: driving, waiting and visits.
long routeTimeMillis =
    winner.getJoinedDetailController().getCurTransitTime()
        + winner.getJoinedDetailController().getCurIdleTime()
        + winner.getJoinedDetailController().getCurProductiveTime();
```

The line holds these values:

- `PC`: `getProgress()`; `ST`: `getOptimizationStage()`; `AL`: `getCallerId()`.
- `JC`: the cost of the best solution, `getJoinedCost()`.
- `RC`: the route count, `getRouteCount()`. `EC`: the element count, `getTotalRoutesOptimizableElementsCount()`. `TC`: the sum of the two, so it counts routes, not resources.
- `TT[h]`: the route time in whole hours, `TU[%]`: the share of the visits in the route time, `TD[km]`: the distance of all routes.

### The line of ReadOutFullProgressWithUtilExample

`toProgressString` creates a `ParsedProgress` from the event and prints its getters:

```java
ParsedProgress pp = new ParsedProgress(p);

StringBuilder line = new StringBuilder();
line.append("PC " + pp.getProgress());
line.append(", AL " + pp.getCallerId());
line.append(", JC " + pp.getCost());
line.append(", RC " + pp.getRouteCount());
line.append(", EC " + pp.getOptimizableElementsCount());
line.append(", TC " + pp.getElementsCount());
line.append(", TT " + pp.getTime());
line.append(", TT PROD " + pp.getProductiveTime());
line.append(", TT IDLE " + pp.getIdleTime());
line.append(", TT FLEX " + pp.getFlexTime());
line.append(", TT TRAN " + pp.getTransitTime());
line.append(", TT TTRAN " + pp.getTerminationTransitTime());
line.append(", TU[%] " + (int) (pp.getUtilization() * 100.0));
line.append(", TD " + pp.getDistance());
```

The times are `Duration` values and print in the ISO form, for example `PT1H40M`; the distance is a length in meters.

- `getTime()`: the route time, the sum of the transit, the idle and the productive time, as in the other class.
- `getProductiveTime()`: the time of the visits. `getIdleTime()`: the waiting time, for example at a node that is not open yet.
- `getFlexTime()`: the flex time used, that is the shift of a route start at its first node. Positive flex time (`setFlexTime` on the resource) lets a route start later to cut the idle time at its first node; negative flex time (`setMaxRouteStartReductionTime`) lets it start earlier. Jack has neither, so the flex time is zero in every line. The [flexible start time feature guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/flextime) explains both.
- `getTransitTime()`: the driving time of all routes. `getTerminationTransitTime()`: the part of it from the last node to the end of the route, here the drive back to Aachen.
- `getUtilization()`: the productive time divided by the route time. `getDistance()`: the distance of all routes.

`ParsedProgress` also has `getTerminationTransitDistance()`, which the example does not print.

## Output and what to look for

Both classes print 100 progress lines per stage, the status events and the result. The progress lines and the routes were the same in four test runs.

### ReadOutFullProgressExample

The first line of each stage, with the status line before it:

```text
StatusEvent(OPTIMIZATION_ALGO_STARTING, SmartConstructionAlgo)
PC 0.0, ST 0, AL SmartConstructionAlgo, JC 262.55150148275936, RC 2, EC 5, TC 7, TT[h] 0, TU[%] 0, TD[km] 0.0
StatusEvent(OPTIMIZATION_ALGO_STARTING, SmartSimulatedAnnealingAlgo)
PC 0.0, ST 1, AL SmartSimulatedAnnealingAlgo, JC 249.71761948307574, RC 2, EC 5, TC 7, TT[h] 4, TU[%] 36, TD[km] 227.05
StatusEvent(OPTIMIZATION_ALGO_STARTING, GeneticEvolution)
PC 0.0, ST 2, AL GeneticEvolution, JC 249.71761948307574, RC 2, EC 5, TC 7, TT[h] 4, TU[%] 36, TD[km] 227.05
```

- During `SmartConstructionAlgo`, `TT[h]`, `TU[%]` and `TD[km]` stay at 0 while `JC` shows a cost.
- `RC 2`: Jack has two working days, so the solution has two routes. With five elements, `TC` is 7.
- `TT[h] 4`: the route time of 272.01 minutes, in whole hours. `TU[%] 36`: 100 minutes of visits in 272.01 minutes. `TD[km] 227.05`: the route distance of the result.
- The cost of 249.71761948307574 does not change during simulated annealing and genetic evolution.

### ReadOutFullProgressWithUtilExample

A line of the simulated annealing stage:

```text
PC 50.0, AL SmartSimulatedAnnealingAlgo, JC 249.71761948307574, RC 2, EC 5, TC 7, TT PT4H32M0.466S, TT PROD PT1H40M, TT IDLE PT0S, TT FLEX PT0S, TT TRAN PT2H52M0.466S, TT TTRAN PT1H14M58.08S, TU[%] 36, TD 227050.30559418685 m
```

- `TT` is the sum of `TT TRAN` and `TT PROD`; the idle time is zero.
- `TT TTRAN` is the drive from Essen, the last node, back to Aachen: the 74.97 minutes of the `Termi. time [min]` line of the result. The node drives of the result (21.36, 27.25, 30.67 and 17.75 minutes) and this drive add up to the transit time of 172.01 minutes, within rounding.
- `TT FLEX` is `PT0S` in every line.

### The result

The route of May 6 visits Aachen, Dueren, Koeln, Wuppertal and Essen and ends at 12:32:00, within Jack's working hours, without a violation. The route of May 7 is not scheduled. The route lines, the same in both classes:

```text
0.0 Aachen / Geo  / Arrival: 06.05.2020 08:00:00 (Europe/Berlin) , Departure: 06.05.2020 08:20:00 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 0.0 [min], 0.0 [km]
0.1 Dueren / Geo  / Arrival: 06.05.2020 08:41:21 (Europe/Berlin) , Departure: 06.05.2020 09:01:21 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 21.36 [min], 28.2 [km]
0.2 Koeln / Geo  / Arrival: 06.05.2020 09:28:36 (Europe/Berlin) , Departure: 06.05.2020 09:48:36 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 27.25 [min], 35.97 [km]
0.3 Wuppertal / Geo  / Arrival: 06.05.2020 10:19:17 (Europe/Berlin) , Departure: 06.05.2020 10:39:17 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 30.67 [min], 40.49 [km]
0.4 Essen / Geo  / Arrival: 06.05.2020 10:57:02 (Europe/Berlin) , Departure: 06.05.2020 11:17:02 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 17.75 [min], 23.43 [km]
```

## What "full progress" means in this context

Progress updates are emitted as `IOptimizationProgress`.

From that progress object you can read:

### 1) Meta progress information
- **Progress** (`p.getProgress()`): the current stage progress percentage.
- **Caller ID** (`p.getCallerId()`): a stage/caller identifier for where the progress originates.

Practical use:
- show "SA 32%" / "GE 84%" in a UI,
- label metrics by stage,
- implement stage-specific logic (e.g., stop GE after N loops).

### 2) Winner (best-so-far) solution metrics
- **Winner entity** (`p.getResultEntity()`): the current best solution.
- From the winner's detail controller (`winner.getJoinedDetailController()`), you can extract live KPIs such as:
  - transit time, idle time, productive time,
  - total distance,
  - termination transit metrics (see below).

These KPIs represent what you actually care about operationally:
- how much the plan improved,
- how efficient the tours are,
- whether the optimization is converging.

## Approach 1: Manual KPI extraction (ReadOutFullProgressExample)

`ReadOutFullProgressExample` demonstrates the direct approach:
- subscribe to progress updates,
- read the winner entity,
- compute and format a KPI string yourself.

### Why this is useful
This approach is:
- very transparent,
- easy to customize,
- and ideal if you want a very specific formatting for your UI or logs.

### Limitations
As soon as you need more KPIs (flex time, termination transit, etc.), manual extraction can become:
- repetitive,
- inconsistent across projects,
- and easy to get wrong.

That is why the repository also includes a utility.

## Approach 2: Use the ParsedProgress helper (recommended)

`ReadOutFullProgressWithUtilExample` demonstrates a cleaner integration:

- create a `ParsedProgress` object from the progress event:
  - `ParsedProgress pp = new ParsedProgress(p);`

- print a richer KPI line assembled from typed getters.

### What the example prints
The example constructs a line containing:

- progress + stage label
- cost and counts
- time breakdown (total, productive, idle, flex, transit, termination transit)
- utilization
- distance

This is an excellent "single-line telemetry output" for:
- logs,
- live terminals,
- and quick dashboards.

### Why this approach is usually better
- You get a consistent definition of KPIs across examples and projects.
- It is easier to add more KPIs later without duplicating logic.
- It avoids mixing formatting logic with metric extraction logic.

## Understanding the time components

While naming is self-explanatory, these distinctions matter in production:

### Transit Time
Time spent traveling between nodes (including start/termination legs, depending on model and metric definition).

### Flex Time
Flex time is the shift of a route start at its first node, as described for `getFlexTime()` above: positive flex time lets a route start later to cut the idle time at its first node, negative flex time lets it start the drive earlier.  
In dashboards, it is often useful because:
- high positive flex time shows routes that start later instead of waiting at their first node,
- or high negative flex time shows routes that drive to their first node before their working hours begin.

### Termination Transit
Termination transit metrics relate to the "route termination" leg(s) in your model.  
This is relevant especially when:
- routes are closed (return to depot), or
- termination points are explicitly modeled.

In practice:
- if you care about "deadheading back to base", termination transit is a key KPI.

## Subscribing to progress events (Observable pattern)

Both examples demonstrate how to subscribe to optimization events using the optimizer's event subjects:

- `progressSubject()`: progress stream
- `warningSubject()`: warnings
- `statusSubject()`: status updates
- `errorSubject()`: errors

This is a production-friendly pattern because it separates:
- the optimization engine,
- and the integration layer (UI/logging/monitoring).

## Best practices for production usage

### 1) Do not log too frequently
Progress callbacks can fire very often. Excessive output can slow down the run.

Combine this doc with the [Progress output frequency and progress requests](../progressfrequency/OptimizationProgress.md) pattern:
- configure `OnProgressOutPercentage`,
- and/or request progress at a controlled wall-clock frequency.

### 2) Keep callback work minimal
In `progressSubject().subscribe(...)`, avoid heavy work such as:
- exporting KML on every update,
- large JSON serialization,
- database writes.

Instead:
- push to a queue,
- aggregate,
- and process asynchronously.

### 3) Track best-so-far improvements
If your UI needs "improvement over time" charts:
- store snapshots of (cost, distance, time) every N seconds,
- not every progress event.

### 4) Stage-aware monitoring
Use `callerId` to:
- segment metrics by stage,
- detect "we are stuck in a stage",
- apply stage-specific early stopping rules.

## Summary

- Full progress in JOpt is read from `IOptimizationProgress`, including the current best winner solution.
- You can extract a compact KPI string manually (ReadOutFullProgressExample) or use `ParsedProgress` for a richer and more maintainable KPI set (recommended).
- The util example prints a detailed line including cost, counts, time breakdown (productive/idle/flex/transit/termination), utilization, and distance.
- This integration is central for observability, dashboards, and intelligent run control in long optimizations.

## Run it

Run the `main(String[] args)` method of either class from your IDE, with the repository imported as a Maven project. You need a JDK in the version that the examples pom sets. You need no license file: without one, the examples use the public evaluation license (see the [license guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/license)).

## Related

- [Progress output frequency and progress requests](../progressfrequency/OptimizationProgress.md): sets how often a stage reports its progress and requests a progress event from outside the run.
- [Running the genetic evolution stage in a loop](../looprun/RunOptimizationInLoop.md): uses the stage number and the progress of the events to count loops.
- [Flexible start time](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/flextime): the feature guide of positive and negative flex time.
- [Reactive events and streams](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/reactive_events): the feature guide of the event API.
- [Recommended ways to run an optimization](../../basic/recommendedimplementation_02/RecommendedImplementation.md): the recommended run pattern, a synchronous run and the legacy callback methods.
- [Advanced examples](../README.md): the overview of all Advanced examples.

---

Added: 2026-02-17 (JOpt core 7.5.3-j17) | Last updated: 2026-10-21
