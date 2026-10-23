# Flexible start time (FlexTime and StartReductionTime)

`PositiveFlexTimeExample` lets Jack start a route up to 2 hours after his working hours begin (FlexTime), so that he does not wait for his first node to open; `NegativeFlexTimeExample` lets him drive to his first node up to 2 hours before his working hours begin (StartReductionTime), so that he arrives when the node opens. The page is for developers who plan resources whose working day should start with work, not with waiting or driving. It builds on the [First optimization example](../../basic/firstoptimization_01/FirstOptimizationExample.md); the [Flexible start time](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/flextime) feature guide describes the feature.

## What the example shows

- **FlexTime:** `setFlexTime(Duration.ofHours(2))` on the resource. When a route would wait at its first node, the route starts later, by the waiting time and at most by 2 hours.
- **StartReductionTime:** `setMaxRouteStartReductionTime(Duration.ofHours(2), true)` on the resource. When the first node lies a drive away, the route starts earlier, by the driving time and at most by 2 hours. `true` allows the early time only for driving, not for working.
- **Working time:** `setReductionTimeIsIncludedInWorkingTime(true)` counts the early start toward the working time of the resource.
- **The result:** each route reports its start (`Effective Route Start`), the maximum of each kind (`Max Idle Reduc.`, `Max Early Redu.`) and the FlexTime or StartReductionTime it used (`Reduc. time usage[min]`, positive for a later start, negative for an earlier one).

## Scenario

The dates, times and coordinates are sample values. The time zone is Europe/Berlin.

- **Resource:** `Jack`, a `CapacityResource` at 50.775346, 6.083887 (Aachen), with working hours on May 6 and May 7, 2020, from 08:00 to 16:00. He may work up to 13 hours and drive up to 1200 km a day.
- **Nodes:** eight `TimeWindowGeoNode` objects (Koeln, Oberhausen, Essen, Dueren, Nuernberg, Heilbronn, Stuttgart and Wuppertal), open from 09:00 to 17:00 in `PositiveFlexTimeExample`, one hour after Jack starts working, and from 08:00 to 17:00 in `NegativeFlexTimeExample`. Unlike other examples with these cities, there is no node in Aachen, Jack's start position, so each route starts with a drive: StartReductionTime can move that drive before the working hours, and FlexTime removes the waiting at the first node. Test runs of `PositiveFlexTimeExample` with an Aachen node returned two plans of the same cost for May 6 (Aachen first at 09:00, or Dueren first at 08:38:38).
- **Visits:** 20 minutes each, with the same importance for every node.
- **Run values:** `addProperties` sets `JOptExitCondition.JOptGenerationCount` and `JOpt.Algorithm.PreOptimization.SA.NumIterations` below their defaults, which keeps the run short; the defaults are in the [property reference](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties).

## How the code works

The sources are [PositiveFlexTimeExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/flextime/PositiveFlexTimeExample.java) and [NegativeFlexTimeExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/flextime/NegativeFlexTimeExample.java). In both, `main(...)` instantiates the class and calls `example()`, which calls the steps `addProperties`, `addNodes`, `addResources`, `attachToObservables`, `startAndPresentResult` and `exportToKml`. `attachToObservables` subscribes to the progress, warning, status and error events with the recommended event API, before the run starts; `startAndPresentResult` waits up to 5 minutes for the result and prints it; `exportToKml` writes the result to a KML file named after the class, as in [Export a result to a KML file](../../basic/io_03/Export2KMLExample.md).

`addResources` of `PositiveFlexTimeExample` sets the FlexTime:

```java
// FlexTime: a route may start up to 2 hours after the working hours start, when it would
// otherwise wait at its first node.
jack.setFlexTime(Duration.ofHours(2));
```

`addResources` of `NegativeFlexTimeExample` sets the StartReductionTime and counts it as working time:

```java
// StartReductionTime: Jack may start up to 2 hours before his working hours. true: he may use
// the early time only for driving to the first node, not for working on it.
jack.setMaxRouteStartReductionTime(Duration.ofHours(2), true);
// true: the early start counts toward his working time and its 13 h maximum.
jack.setReductionTimeIsIncludedInWorkingTime(true);
```

## Output and what to look for

Each class gave the same result in four test runs.

### PositiveFlexTimeExample

Route 0 on May 6 starts at 08:38:38 instead of 08:00 and reaches Dueren, 21.36 minutes away, when it opens at 09:00, without idle time (trimmed to these lines of route 0):

```text
Defined WorkingHours   : 06.05.2020 08:00:00 - 06.05.2020 16:00:00 (Europe/Berlin)
Effective Route Start  : 06.05.2020 08:38:38 (Europe/Berlin)
Max Idle Reduc.  [min] : 120.0
Reduc. time usage[min] : 38.64
Idle time        [min] : 0.0
0.0 Dueren / Geo  / Arrival: 06.05.2020 09:00:00 (Europe/Berlin) , Departure: 06.05.2020 09:20:00 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 21.36 [min], 28.2 [km]
```

The 38.64 minutes are the time Jack would have waited at Dueren: he leaves at 08:38:38, drives 21.36 minutes and arrives at 09:00. The FlexTime used is the waiting time only. Route 1 on May 7 starts at 08:00 and uses no FlexTime: its first node, Heilbronn, is 216.37 minutes away and open on arrival. Route 1 ends at 20:01:57, after the end of its working hours at 16:00, and the result shows no violation. Time after the end of the working hours counts against the maximal working time, and the working hours are a frame for matching resources with the opening hours of the nodes, so a route can end after its working hours without a violation. Its 721.96 minutes stay below Jack's maximal working time of 13 hours.

### NegativeFlexTimeExample

Route 0 on May 6 starts at 07:38:38 and reaches Dueren, 21.36 minutes away, when it opens at 08:00 (trimmed to these lines of route 0):

```text
Defined WorkingHours   : 06.05.2020 08:00:00 - 06.05.2020 16:00:00 (Europe/Berlin)
Effective Route Start  : 06.05.2020 07:38:38 (Europe/Berlin)
Max Early Redu.  [min] : -120.0
Reduc. time usage[min] : -21.36
0.0 Dueren / Geo  / Arrival: 06.05.2020 08:00:00 (Europe/Berlin) , Departure: 06.05.2020 08:20:00 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 21.36 [min], 28.2 [km]
```

Route 1 on May 7 shows the limit. Its first node, Nuernberg, is 287.91 minutes away, more than the 2 hours of StartReductionTime. Jack starts at 06:00, uses the full 120 minutes and still arrives at 10:47:54, after the node opens at 08:00 (trimmed to these lines of route 1):

```text
Effective Route Start  : 07.05.2020 06:00:00 (Europe/Berlin)
Effective Route Stop   : 07.05.2020 17:58:18 (Europe/Berlin)
Reduc. time usage[min] : -120.0
Route time       [min] : 718.31
1.0 Nuernberg / Geo  / Arrival: 07.05.2020 10:47:54 (Europe/Berlin) , Departure: 07.05.2020 11:07:54 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 287.91 [min], 380.04 [km]
```

The route time of 718.31 minutes counts from 06:00, because the early start is part of the working time. Route 1 ends at 17:58:18, after the end of its working hours at 16:00, and the result shows no violation. The 718.31 minutes stay below Jack's maximal working time of 13 hours.

## The idea of FlexTime and StartReductionTime

FlexTime and StartReductionTime are resource-level features that make a WorkingHours start **flexible** rather than fixed.

Instead of "the route always starts at 08:00", they turn the start into a **bounded time window** that the optimizer can use to improve feasibility and realism.

They are particularly valuable in day-to-day operations because they solve two common issues without adding manual dispatcher rules:

- **Idle time at the beginning of the route** (first job opens later than shift start).
- **Late arrival at the first job** (first job opens early, but driving time delays the start).

Reference documentation:
- [Special features: flexible start time](https://www.dna-evolutions.com/docs/learn-and-explore/special/special_features#flexible-start-time-positive-and-negative)

## The mental model

A resource has:

- **WorkingHours**: a fixed, contractual time window (e.g., 08:00-16:00).
- **Route start**: by default equals the WorkingHours start.
- **First-node timing**: depends on opening hours and travel time from the resource start.

FlexTime and StartReductionTime adjust **when the route actually starts** within bounds:

- **FlexTime** shifts the start later (reduce idle time).
- **StartReductionTime** shifts the driving start earlier (arrive at the first node when it opens).

Both are **hard constrained**:
- the optimizer cannot exceed the configured maximum.

## FlexTime (start later to reduce idle time)

### Business problem of FlexTime
For example, a resource starts at 08:00, but the first node opens at 10:00.

Without FlexTime:
- the route starts at 08:00,
- and the resource accumulates idle time until it can begin productive work.

With FlexTime:
- the optimizer may shift the start later (within the allowed range),
- reducing idle time while still respecting WorkingHours and node opening hours.

### How FlexTime is configured in the example
The example uses:

```java
jack.setFlexTime(Duration.ofHours(2));
```

Interpretation:
- the optimizer may start the route up to 2 hours later than the WorkingHours start.

### What changes operationally
FlexTime helps your KPIs in several ways:

- less idle time at the beginning of the shift,
- more realistic schedules (no "waiting on the parking lot" for long time windows),
- better end-user acceptance (Gantt plots and timelines look cleaner),
- improved utilization (especially when first stops are opening-hour driven).

### Driving time and FlexTime
An operation may want FlexTime to be configured so that:
- the allowed FlexTime is used only for "delaying work start", not for "delaying work + driving".

The JOpt core that the examples pom references has no such setting for FlexTime: `setFlexTime` takes only a duration, and the FlexTime used is the waiting time at the first node (in the example, Jack leaves at 08:38:38, drives 21.36 minutes and arrives at Dueren at 09:00, so he uses 38.64 minutes). The driving-only setting belongs to StartReductionTime (`setMaxRouteStartReductionTime`, below).

Operational interpretation:
- some organizations allow "start later" but still count "driving to the first job" as working time (or vice versa).

If your operation distinguishes between paid working time, paid driving time and unpaid commute, this distinction becomes important. For FlexTime it is a modeling decision outside the core: the core of the examples has no setting for it.

(The FlexTime section of the Special Features page describes the idea.)

## StartReductionTime (start earlier to arrive when the first node opens)

### Business problem of StartReductionTime
For example, a node opens at 08:00 and a resource starts at 08:00, but driving to the first job takes 30 minutes.

Without StartReductionTime:
- arrival is 08:30,
- the route begins "late" relative to the node opening (even if there is no violation, the schedule looks messy and may reduce downstream feasibility).

With StartReductionTime:
- the optimizer is allowed to start *driving* earlier so the first node can be served right when it opens.

### How StartReductionTime is configured in the example
The StartReductionTime example, `NegativeFlexTimeExample`, uses:

```java
jack.setMaxRouteStartReductionTime(Duration.ofHours(2), true);
// true: the early start counts toward his working time and its 13 h maximum.
jack.setReductionTimeIsIncludedInWorkingTime(true);
```

#### `setMaxRouteStartReductionTime(Duration, boolean)`
- The duration is the maximum allowed start-time reduction (how much earlier the route can begin).
- The boolean indicates a crucial policy:

  **"Only allowed to be used for driving (not working on the node)."**

Meaning:
- the early time can be used to move the vehicle toward the first node,
- but the resource should not start "working" before official WorkingHours / node opening hours.

This is a fit for real operations:
- the employee may leave earlier to drive (commute / positioning),
- but productive work is still tied to contractual start times and opening hours.

#### `setReductionTimeIsIncludedInWorkingTime(boolean)`
This controls whether the early-start time is counted as part of the total working time.

In the example it is set to `true`, meaning:
- the early start contributes to total working time consumption,
- which is often the correct policy when early driving is paid and regulated.

If set to `false`, you model a policy where:
- early driving is outside working time (less common, but sometimes used in field-service scenarios).

### Why StartReductionTime is an acceptance feature
StartReductionTime has a "visual effect":
- route timelines and Gantt charts look more intuitive because first stops can align to opening times instead of starting with a long "drive segment" that shifts the whole day.

This reduces discussions like:
- "why does every route start with a long drive and then 'work' begins late?"

Even if feasibility is unchanged, perceived quality and trust typically improve.

## Combining FlexTime and StartReductionTime

FlexTime and StartReductionTime can be used together.

Conceptually:
- the WorkingHours start becomes a flexible band:
  - it may shift later to avoid idle time,
  - or shift earlier (for driving) to align to early opening hours.

This gives the optimizer a controlled lever:
- it can pick the best start-time adjustment per day, per route, based on the actual first node(s) and travel time.

## Modeling guidance (what works well in production)

### Be explicit about policy: "driving-only" vs "work allowed"
For StartReductionTime, the "driving-only" flag is critical.
It prevents unintended interpretation that:
- the resource is allowed to do productive work before official start.

### Decide whether early-start counts toward working time
Use `setReductionTimeIsIncludedInWorkingTime(true/false)` based on:
- labor rules,
- payroll policy,
- and how you define maximum working time.

### Use FlexTime and StartReductionTime to avoid brittle cost tuning
FlexTime and StartReductionTime are architectural features to expand the feasible region and improve realism.
They should not be simulated by cost manipulation.

If you need strict behavior ("must not start before ...", "must not start after ..."), model it structurally (hard constraints or explicit time windows).

The property `JOptWeight.TimeWindow.FlexTimePenalizeFactor` gives used FlexTime a cost, so that routes start closer to the start of the working hours; its default is in the property reference.

### Pair FlexTime with realistic first-node handling
If your operation allows starting work immediately on early arrival (without waiting for node opening), consider the related feature:
- `setWaitOnEarlyArrival(boolean)` on the node (and `setWaitOnEarlyArrivalFirstNode(boolean)` for the first node of a route); see the example `WaitOnEarlyArrivalFirstNodeExample` ([Wait on early arrival at the first node](../../advanced/waitonearlyarrival/WaitOnEarlyArrivalFirstNode.md)).

This can be combined with FlexTime to match real practices:
- some organizations allow early work; others enforce waiting.

## Typical use cases

- Field service where first appointments open late (reduce idle time).
- Delivery operations where docks open early and early arrival is desirable.
- Any operation where "route looks wrong" because the start time is too rigid.
- Scenarios where you want better feasibility under tight time windows without increasing compute budgets.

## Summary

- **FlexTime**: allows the route to start later to reduce idle time (`setFlexTime(...)`).
- **StartReductionTime**: allows the route to start earlier (typically driving-only) to reach the first node when it opens (`setMaxRouteStartReductionTime(..., drivingOnly)`).
- You can choose whether the early-start time is counted as working time (`setReductionTimeIsIncludedInWorkingTime(...)`).
- Both are hard constrained and can be combined to make shift starts realistic, feasible, and easier to accept visually.

## Run it

Run the `main(String[] args)` method of `PositiveFlexTimeExample` or `NegativeFlexTimeExample` from your IDE, with the repository imported as a Maven project. You need a JDK in the version that the examples pom sets. You need no license file: without one, the examples use the public evaluation license (see the [license guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/license)). Each run writes a KML file named after its class into the working directory.

## Related

- [Flexible start time](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/flextime): the feature guide, with FlexTime and StartReductionTime in Java.
- [Special features: flexible start time](https://www.dna-evolutions.com/docs/learn-and-explore/special/special_features#flexible-start-time-positive-and-negative): the FlexTime section of the special features page.
- [Wait on early arrival at the first node](../../advanced/waitonearlyarrival/WaitOnEarlyArrivalFirstNode.md): the early-arrival setting of the first node.
- [Optimization properties](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties): the property reference, with `JOptWeight.TimeWindow.FlexTimePenalizeFactor`.
- [Recommended ways to run an optimization](../../basic/recommendedimplementation_02/RecommendedImplementation.md): the recommended run pattern, a synchronous run and the legacy callback methods.
- [Expert examples](../README.md): the overview of all Expert examples.

---

Added: 2026-02-17 (JOpt core 7.5.3-j17) | Last updated: 2026-10-23
