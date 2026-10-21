# Shorter visits for nodes at the same place

`JointVisitDurationExample` plans two groups of three nodes, each group at one position in Koeln and in Essen. With `setJointVisitDuration`, a node that directly follows a node at the same place takes 15 minutes instead of its visit duration of 30 minutes. The page is for developers who model several jobs at one site as separate nodes. If you have not built an optimization yet, start with the [First optimization example](../../basic/firstoptimization_01/FirstOptimizationExample.md).

## What the example shows

In many routing problems, multiple tasks happen at the **same physical location**:
- multiple jobs at one customer site,
- multiple deliveries to the same warehouse gate,
- several maintenance tasks within one facility.

If these tasks are represented as separate nodes, a naive model applies the **full visit duration** to every node, which can significantly overestimate the on-site time.

JOpt supports a more realistic model using **joint visit duration**:
- the first visit at the location consumes the full visit duration,
- and immediately following co-located visits can consume a **shorter joint visit duration**.

This feature is designed to reflect operational reality:
* setup time happens once (check-in, parking, security),
* subsequent tasks at the same site are faster (walking distance is negligible, context already established).

- **`setJointVisitDuration(Duration)`** on a node sets the shorter visit that applies when the node directly follows a node at the same place.
- **The trigger is a distance:** the joint visit duration applies to a node whose predecessor on the route lies within `JOpt.JointVisitDuration.maxRadiusMeter`. The default of the property is in the [property reference](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties).
- **The first node of a group** takes its full visit duration, because its predecessor lies outside that radius. Each node of a group gets the setting, because any of them can follow another.
- **Without the setting,** a node takes its visit duration in every case.

## Scenario

The dates, times and coordinates are sample values.

- **Resource:** `Jack` starts at 50.775346, 6.083887 (Aachen) and works on May 6 and 7, 2020, from 08:00 to 17:00 (Europe/Berlin). His maximal working time is 13 hours and his maximal distance 1200 km, more than any day of this plan needs.
- **Nodes:** eight `TimeWindowGeoNode` objects, open on May 6 and 7, 2020, from 08:00 to 17:00, each with a visit duration of 30 minutes and the importance 1:
  - Koeln1, Koeln2 and Koeln3 at 50.9333, 6.95, with a joint visit duration of 15 minutes;
  - Essen1, Essen2 and Essen3 at 51.45, 7.01667, with a joint visit duration of 15 minutes;
  - Oberhausen and Dueren, each at a place of its own and without a joint visit duration.

`addProperties` sets two properties: `JOptExitCondition.JOptGenerationCount` to 1000 and `JOpt.Algorithm.PreOptimization.SA.NumIterations` to 100000. Both are below their defaults, which keeps the run short.

## How the code works

The source is [JointVisitDurationExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/jointvisitduration/JointVisitDurationExample.java). `example()` runs the steps in order:

```java
// The public evaluation license, or your own license if you put a license file where
// ExampleLicenseHelper looks for it.
ExampleLicenseHelper.setLicense(this);
addProperties(this);
addNodes(this);
addResources(this);
attachToObservables(this);
IOptimizationResult result = startAndPresentResult(this);
exportToKml(result);
```

- `ExampleLicenseHelper.setLicense(this)` uses your license file if you put one where the helper looks for it, and the public evaluation license otherwise.
- `addProperties` sets the two properties.
- `addNodes` adds the two groups and the two single nodes.
- `addResources` adds Jack.
- `attachToObservables` subscribes to the progress, warning, status and error events with the event API, before the run starts.
- `startAndPresentResult` starts the run, waits up to 5 minutes for the result, prints it and returns it.
- `exportToKml` writes the result to `JointVisitDurationExample.kml` in the working directory and prints `KML export failed: JointVisitDurationExample.kml` if the export returns `false`.

### The two durations

```java
// A visit takes 30 minutes. A visit directly after a node at the same place takes 15 minutes.
Duration visitDuration = Duration.ofMinutes(30);
Duration jointVisitDuration = Duration.ofMinutes(15);
```

### A group of nodes at one place

The first node of the Koeln group; Koeln2, Koeln3 and the Essen group follow the same pattern:

```java
// The joint visit duration applies to a node whose predecessor on the route lies within
// JOpt.JointVisitDuration.maxRadiusMeter. The first node of a group takes its full visit
// duration, because its predecessor lies outside that radius. Each node of a group gets the
// setting, because any of them can follow another.
INode koeln1 =
    new TimeWindowGeoNode("Koeln1", 50.9333, 6.95, openingHours, visitDuration, importance);
koeln1.setJointVisitDuration(jointVisitDuration);
opti.addElement(koeln1);
```

Oberhausen and Dueren get no joint visit duration, because no other node shares their place:

```java
// Oberhausen and Dueren share their place with no other node, so a joint visit duration would
// never apply to them.
INode oberhausen =
    new TimeWindowGeoNode(
        "Oberhausen", 51.4667, 6.85, openingHours, visitDuration, importance);
opti.addElement(oberhausen);
```

## Output and what to look for

The run prints the status and progress lines, then the result. In two test runs Jack visited all eight nodes on May 6, in this order of places: Dueren, the three Koeln nodes, the three Essen nodes, Oberhausen. The route of May 7 stayed empty.

Print the result and verify that co-located sequences show reduced service time: in each group, the first node takes `Duration: 30.0 [min]` and the two nodes after it take `Duration: 15.0 [min]` with `Driving: 0.0 [min], 0.0 [km]`. The three Koeln nodes arrive at 09:18:36, 09:48:36 and 10:03:36, and the last one leaves at 10:18:36: 60 minutes at the place instead of 90. The order inside each group can change between runs: in the two runs both the Koeln group and the Essen group had a different order. A practical validation method: compare total visit time in a route where (Koeln1, Koeln2, Koeln3) are consecutive, with joint duration enabled vs disabled.

The two single nodes take their full visit duration, the same in both runs:

```text
0.0 Dueren / Geo  / Arrival: 06.05.2020 08:21:21 (Europe/Berlin) , Departure: 06.05.2020 08:51:21 (Europe/Berlin) / Duration: 30.0 [min] / Driving: 21.36 [min], 28.2 [km]
```

```text
0.7 Oberhausen / Geo  / Arrival: 06.05.2020 12:11:06 (Europe/Berlin) , Departure: 06.05.2020 12:41:06 (Europe/Berlin) / Duration: 30.0 [min] / Driving: 8.86 [min], 11.69 [km]
```

Inspect the KML file that the example writes, `JointVisitDurationExample.kml`: it shows the same route on a map; the nodes of a group lie on one point. In the KML you can confirm that the solver naturally groups co-located tasks where beneficial.

## The key API: `setJointVisitDuration(...)`

Each node can define a joint duration:

- `node.setJointVisitDuration(Duration jointVisitDuration);`

### Interpretation
When nodes lie **within `JOpt.JointVisitDuration.maxRadiusMeter`** of each other and are visited **directly after each other**:

- the **first** node uses `visitDuration` (normal duration),
- subsequent nodes use `jointVisitDuration` (shortened duration).

### Default behavior
Without `setJointVisitDuration`, as for Oberhausen and Dueren in the example:
- a node takes its `visitDuration` in every case.

Meaning:
- you must set it explicitly if you want shortened follow-up tasks.

## Why this feature matters

### 1) Realistic time accounting
Without joint duration, co-located tasks often overestimate:
- setup time,
- parking/walking,
- check-in overhead.

JointVisitDuration prevents unnecessary infeasibility:
- especially under tight working hours or opening hours.

### 2) Better routing decisions
If the model correctly reflects that "stacking tasks at the same site is cheaper," the optimizer can:
- bundle tasks,
- reduce travel,
- and increase route density in dense areas.

### 3) Better service-level compliance
Accurate on-site time reduces:
- artificial lateness,
- unnecessary overtime,
- and "false infeasible" cases.

## Interaction with time windows and working hours

Joint visit duration affects the schedule time linearly:
- shorter on-site durations can improve feasibility against:
  - node opening hours (time windows),
  - resource working hours,
  - maximal working time constraints.

Important nuance:
- Joint duration only applies when the co-located nodes are visited **directly after each other**.
- If the solver interleaves another location between them, each visit is treated independently.

## Modeling guidance (how to use it correctly)

### 1) Use it only when tasks are truly co-located
JointVisitDuration assumes "same site, same arrival": the shorter duration applies when the predecessor on the route lies within `JOpt.JointVisitDuration.maxRadiusMeter`.  
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

## Performance and scalability considerations

JointVisitDuration is a modeling feature, not just an output decoration:
- it changes feasibility and objective values,
- therefore it influences search and route construction.

In large instances where many tasks share locations (warehouses, campuses), it can substantially reduce:
- unnecessary overtime violations,
- infeasibility caused by inflated service time.

## Summary

- `setJointVisitDuration(...)` lets you model **reduced service time for consecutive visits at the same place**, that is, within `JOpt.JointVisitDuration.maxRadiusMeter` of each other.
- The first node uses the full visit duration; subsequent co-located nodes can use a shorter joint duration.
- This improves realism, feasibility, and route quality in scenarios with multi-task locations (campuses, warehouses, multi-order customers).
- The example uses:
  - 30 minutes normal duration,
  - 15 minutes joint duration,
  across multiple co-located nodes in Koeln and Essen to make the effect observable.

## Run it

Run the `main(String[] args)` method of `JointVisitDurationExample` from your IDE, with the repository imported as a Maven project. You need a JDK in the version that the examples pom sets. You need no license file: without one, the example uses the public evaluation license (see the [license guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/license)). The run writes `JointVisitDurationExample.kml` into the working directory.

## Related

- [JointVisitDurationExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/jointvisitduration/JointVisitDurationExample.java): the source of this example.
- [Optimization properties](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties): the property reference, with `JOpt.JointVisitDuration.maxRadiusMeter` and its default.
- [Recommended ways to run an optimization](../../basic/recommendedimplementation_02/RecommendedImplementation.md): the recommended run pattern, a synchronous run and the legacy callback methods.
- [Advanced examples](../README.md): the overview of all Advanced examples.

---

Added: 2026-02-17 (JOpt core 7.5.3-j17) | Last updated: 2026-10-21
