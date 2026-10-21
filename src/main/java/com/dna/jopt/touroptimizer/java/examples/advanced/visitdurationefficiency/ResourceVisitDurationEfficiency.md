# Visit duration efficiency of a resource

`ResourceVisitDurationEfficiencyExample` gives the resource Clara the visit duration efficiency factor 0.5, so her visits take half the visit duration of a node where the node allows it. Of four visits of 30 minutes, one takes 15 minutes, two stop at their minimal visit duration of 20 minutes, and one keeps its 30 minutes, because its node does not let the visit change. The page is for developers whose resources need different times for the same job. If you have not built an optimization yet, start with the [First optimization example](../../basic/firstoptimization_01/FirstOptimizationExample.md).

## What the example shows

In many real-world tour optimization scenarios, resources are not equally fast:

- one technician is more experienced,
- one crew has better equipment,
- one vehicle requires longer on-site handling,
- some resources can execute certain tasks more efficiently than others.

JOpt.TourOptimizer supports this directly via a **resource visit duration efficiency factor**.  
This factor scales how long a resource needs to perform the "same job" at nodes whose visit duration is allowed to be modified.

- **The factor:** `setOverallVisitDurationEfficiencyFactor(double)` on a resource scales the visits of that resource: JOpt multiplies the visit duration of the node by the factor. A resource without the setting takes the visit duration of the node. The setter accepts a factor above 0 and returns `false` for any other value; the example checks the returned value.
- **Nodes that allow it:** the factor applies only at nodes with `setHasRouteDependentVisitDuration(true)`. A node without that call keeps its visit duration, whoever visits it. In the example this is the node `Essen - fixed duration`.
- **A lower limit:** `setMinimalVisitDuration(Duration)` sets the shortest visit that the factor can produce. At Koeln and Oberhausen, 30 minutes times 0.5 would be 15 minutes, and the minimal visit duration of 20 minutes applies instead.
- **The factor alone:** Dueren allows the change and sets no minimal visit duration of its own, so Clara's visit there takes 15 minutes.
- **Working time:** the visits count toward the working time, like the driving, so the shorter visits shorten Clara's route.

## Scenario

The dates, times and coordinates are sample values. The time zone is Europe/Berlin.

- **Resources:** `Clara` and `Jack`, two `CapacityResource` objects at 50.775346, 6.083887 (Aachen). Both have working hours on May 6 and May 7, 2020, from 08:00 to 17:00, may work up to 9 hours a day, the length of the working hours, and may drive up to 1200 km a day. Clara has the visit duration efficiency factor 0.5, Jack has none.
- **Nodes:** four `TimeWindowGeoNode` objects, open on May 6 and May 7 from 08:00 to 17:00, each with a visit duration of 30 minutes and the same importance:
  - `Koeln` and `Oberhausen` allow the visit to change with the resource, with a minimal visit duration of 20 minutes.
  - `Essen - fixed duration` does not allow it.
  - `Dueren` allows it and sets no minimal visit duration of its own.

`addProperties` sets `JOptExitCondition.JOptGenerationCount` to 1000 and `JOpt.Algorithm.PreOptimization.SA.NumIterations` to 100000. Both are below their defaults, which keeps the run short. The defaults are in the [property reference](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties).

## How the code works

The source is [ResourceVisitDurationEfficiencyExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/visitdurationefficiency/ResourceVisitDurationEfficiencyExample.java). `example()` runs the steps in order:

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

- `addProperties` sets the two properties.
- `addNodes` adds the four nodes, each with its own visit duration settings.
- `addResources` adds Clara, with the factor, and Jack.
- `attachToObservables` subscribes to the progress, warning, status and error events with the event API, before the run starts.
- `startAndPresentResult` starts the run, waits up to 5 minutes for the result and prints it.

### The nodes

Koeln and Oberhausen allow the change, with a lower limit:

```java
// Koeln and Oberhausen let the visit change with the resource, but not below 20 minutes.
TimeWindowGeoNode koeln =
    new TimeWindowGeoNode("Koeln", 50.9333, 6.95, openingHours, visitDuration, importance);
koeln.setHasRouteDependentVisitDuration(true);
koeln.setMinimalVisitDuration(minVisitDuration);
opti.addElement(koeln);
```

Essen keeps its visit duration, and Dueren allows the change without a lower limit of its own:

```java
// Without setHasRouteDependentVisitDuration(true), the visit keeps its 30 minutes, whoever
// the visitor is.
INode essen =
    new TimeWindowGeoNode(
        "Essen - fixed duration", 51.45, 7.01667, openingHours, visitDuration, importance);
opti.addElement(essen);

// Dueren lets the visit change and sets no minimal visit duration: Clara's factor takes the
// visit from 30 to 15 minutes.
TimeWindowGeoNode dueren =
    new TimeWindowGeoNode("Dueren", 50.8, 6.48333, openingHours, visitDuration, importance);
dueren.setHasRouteDependentVisitDuration(true);
opti.addElement(dueren);
```

### The factor

```java
IResource clara =
    new CapacityResource(
        "Clara", 50.775346, 6.083887, maxWorkingTime, maxDistanceKmW, createWorkingHours());
// Clara needs half the visit duration at nodes that let the visit change with the resource.
// The setter refuses a factor of 0 or below and returns false.
if (!clara.setOverallVisitDurationEfficiencyFactor(0.5)) {
  System.out.println("Visit duration efficiency factor not set for Clara");
}
opti.addElement(clara);
```

Jack is created the same way, without the factor.

## Output and what to look for

The run prints the status and progress lines, then the result. In both runs, Clara visited all four nodes on May 6, in a route of 256.83 minutes that ended at 12:16:49 and showed no violation, and Jack had no route. Clara's node lines were the same in both runs:

```text
0.0 Dueren / Geo  / Arrival: 06.05.2020 08:21:21 (Europe/Berlin) , Departure: 06.05.2020 08:36:21 (Europe/Berlin) / Duration: 15.0 [min] / Driving: 21.36 [min], 28.2 [km]
0.1 Koeln / Geo  / Arrival: 06.05.2020 09:03:36 (Europe/Berlin) , Departure: 06.05.2020 09:23:36 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 27.25 [min], 35.97 [km]
0.2 Essen - fixed duration / Geo  / Arrival: 06.05.2020 10:07:15 (Europe/Berlin) , Departure: 06.05.2020 10:37:15 (Europe/Berlin) / Duration: 30.0 [min] / Driving: 43.64 [min], 57.61 [km]
0.3 Oberhausen / Geo  / Arrival: 06.05.2020 10:46:06 (Europe/Berlin) , Departure: 06.05.2020 11:06:06 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 8.86 [min], 11.69 [km]
```

The `Duration` values show the three cases: 15 minutes at Dueren, where the factor alone applies; 20 minutes at Koeln and Oberhausen, where the minimal visit duration applies; 30 minutes at `Essen - fixed duration`, where the visit does not change.

The assignment alone does not show the factor. In two test runs of a variant of this class with Clara's factor set to 1.0, so that her visits take the visit duration of the node as Jack's do, Clara again visited all four nodes. Every visit took 30 minutes, and her route took 291.83 minutes, 35 minutes longer than with the factor.

## The key idea

Nodes typically have a default visit duration, for example:
- 30 minutes per visit

A resource can then declare an efficiency factor:

- efficiency factor **1.0** -> use the visit duration "as is"
- efficiency factor **0.5** -> the resource needs **only 50%** of the time
- efficiency factor **2.0** -> the resource needs **200%** (twice the time)

In the example:
- Both resources start in Aachen.
- Default node visit duration is **30 minutes**.
- Resource **Clara** is configured as "more efficient":
  - `clara.setOverallVisitDurationEfficiencyFactor(0.5);`

Expected effect:
- Clara should be preferred for assignments, because she can complete the same workload in less time (and therefore potentially visit more nodes and/or reduce idle/late risk). The assignment of this example does not show it: in two test runs of a variant with Clara's factor set to 1.0, Clara took all four nodes as well.

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

The example leaves route-dependent visit duration disabled for one node:

- "Essen - fixed duration"
- no `setHasRouteDependentVisitDuration(true)` call for `essen`

Meaning:
- even if Clara is faster, this job stays at the full duration.

## Minimum visit duration (why "faster" does not always mean "arbitrarily fast")

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
- Clara's "true potential of 15 minutes" can only be realized at the node that sets **no minimal visit duration of its own** ("Dueren"; JOpt keeps a minimal visit duration of 2 seconds for a node that sets none, far below 15 minutes).

## Interpreting the results

The example subscribes to progress (and to the warning, status and error events) and waits for the final result:

- `progressSubject().subscribe(p -> println(p.getProgressString()))`
- `resultFuture.get(5, TimeUnit.MINUTES)`, then `System.out.println(result)`

In practice, you would verify:
- assignment decisions (Clara should dominate if constraints allow),
- route time metrics (Clara's tour should be shorter),
- feasibility margins (less risk of exceeding WorkingHours or time windows).

If your scenario includes tight time windows, this feature is particularly impactful:
- faster resources can make otherwise infeasible schedules feasible without changing the plan structure.

## Recommended production usage

### 1) Start with an "overall" factor
If your data model does not yet include per-task efficiency, start with:
- a single overall efficiency factor per resource (as in the example).

This already captures many real operational differences.

### 2) Use minimum visit durations to preserve realism
Always ask:
- "What is the absolute minimum for this task type?"

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
Do not attempt to "simulate efficiency" by adding artificial costs.
Visit-duration efficiency affects:
- feasibility,
- schedule tightness,
- and route time in a structurally correct way.

## Summary

- Resources can have different execution speeds via `setOverallVisitDurationEfficiencyFactor(...)`.
- Efficiency only applies to nodes that allow route-dependent visit durations (`setHasRouteDependentVisitDuration(true)`).
- Minimum visit durations cap efficiency effects, ensuring realism.
- Fixed-duration nodes ignore efficiency (useful for breaks or strictly timed operations).
- The example demonstrates all three cases in a compact scenario. Its assignment does not show why an experienced resource tends to dominate assignments when time matters: in two test runs of a variant with Clara's factor set to 1.0, Clara took all four nodes as well (see "Output and what to look for").

## Run it

Run the `main(String[] args)` method of `ResourceVisitDurationEfficiencyExample` from your IDE, with the repository imported as a Maven project. You need a JDK in the version that the examples pom sets. You need no license file: without one, the example uses the public evaluation license (see the [license guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/license)).

## Related

- [Advanced examples](../README.md): the overview of all Advanced examples.
- [Recommended ways to run an optimization](../../basic/recommendedimplementation_02/RecommendedImplementation.md): the recommended run pattern, a synchronous run and the legacy callback methods.
- [Optimization properties](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties): the property reference, with the defaults of the two properties.
- [Connection time efficiency of a resource](../connectionefficiency/ResourceConnectionEfficiency.md): the same kind of factor for the driving times.
- [Shorter visits for nodes at the same place](../jointvisitduration/JointVisitDuration.md): another way to shorten a visit, for nodes at the same position.

---

Added: 2026-02-17 (JOpt core 7.5.3-j17) | Last updated: 2026-10-21
