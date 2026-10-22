# Wait on early arrival at the first node

`WaitOnEarlyArrivalFirstNodeExample` lets Jack start work at once when he reaches a node before it opens, except at the first node of his route: there he waits until the node opens. Two node settings do this, `setWaitOnEarlyArrival(false)` and `setWaitOnEarlyArrivalFirstNode(true)`. An early start later in the route is allowed, but the result reports it as an EARLY violation with a cost. The page is for developers who allow early service on the way but not at the start of a route; if you have not built an optimization yet, start with the [First optimization example](../../basic/firstoptimization_01/FirstOptimizationExample.md).

## What the example shows

In time-window routing, there are two common interpretations when a resource arrives early:

1. **Wait** until the opening/working time begins (classic VRPTW behavior).
2. **Start work immediately** (common in field service when "arrival earlier than planned" is operationally acceptable).

In practice, many operations combine both:
* Starting work at the first stop of a route before the stop opens is not allowed (labor rules, depot opening, dispatch rules),
* but once the route has started, early arrivals at subsequent stops can be handled immediately.

This example demonstrates exactly that behavior; its route starts at the start of the working hours:
* allow immediate service at a node upon arrival (**no waiting**),
* but **if the node is the first node of the route**, enforce waiting so the work does not start before the node opens.

- **`setWaitOnEarlyArrival(boolean)`:** with `true`, a resource that reaches a node before it opens waits until the opening time, and the wait shows as idle time. With `false`, work starts at once on arrival.
- **`setWaitOnEarlyArrivalFirstNode(boolean)`:** the same decision for the node when it is the first node of a route. It counts only when `setWaitOnEarlyArrival` is `false`.
- **Both settings together:** at the first node of a route, Jack waits until the node opens; at every later node, he starts at once. The wait ends at the opening time of the node, not at the start of the working hours: Jack starts work at 08:00 and waits at the first node until it opens at 10:00.
- **An early start has a cost:** when work starts before a node opens, the result reports an EARLY violation with the early time in minutes, and the route cost holds a `Cost Early` part. The weight `JOptWeight.TimeWindow` scales the cost of time window violations, early starts included; its default is in the [property reference](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties).
- **Per node:** both settings belong to the node, and the example sets both on every node: `setWaitOnEarlyArrival(false)` and `setWaitOnEarlyArrivalFirstNode(true)`. A node that must not be served before it opens is given `setWaitOnEarlyArrival(true)`.

## Scenario

The dates, times and coordinates are sample values.

- **Resource:** `Jack` starts at 50.775346, 6.083887 (Aachen) and works on May 6, 2020, from 08:00 to 18:00 (Europe/Berlin). His maximal working time is 10 hours, the length of the working hours, and his maximal distance 1200 km.
- **Nodes:** five `TimeWindowGeoNode` objects. Koeln, Essen, Dueren and Aachen are open from 10:00 to 16:00, Wuppertal from 14:00 to 19:00. Each visit takes 20 minutes, and all nodes have the importance 1. The Aachen node lies at Jack's start position.
- **Early arrival:** every node sets `setWaitOnEarlyArrival(false)` and `setWaitOnEarlyArrivalFirstNode(true)`.

`addProperties` sets `JOptExitCondition.JOptGenerationCount` to 1000 and `JOpt.Algorithm.PreOptimization.SA.NumIterations` to 100000. Both are below their defaults, which keeps the run short.

## How the code works

The source is [WaitOnEarlyArrivalFirstNodeExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/waitonearlyarrival/WaitOnEarlyArrivalFirstNodeExample.java). `example()` runs the steps in order:

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
- `addProperties` sets the two properties.
- `addNodes` adds the five nodes with both settings.
- `addResources` adds Jack.
- `attachToObservables` subscribes to the progress, warning, status and error events with the event API, before the run starts.
- `startAndPresentResult` starts the run, waits up to 5 minutes for the result and prints it.

### The two node settings

Every node sets both. The block shows Koeln; the other four nodes follow the same pattern:

```java
// setWaitOnEarlyArrival(false): on an early arrival, work starts at once instead of waiting
// for the opening time; the result reports the early start as an EARLY violation with a
// cost. setWaitOnEarlyArrivalFirstNode(true): as the first node of a route, the node makes
// Jack wait until it opens. The first-node setting counts only together with
// setWaitOnEarlyArrival(false).
TimeWindowGeoNode koeln =
    new TimeWindowGeoNode("Koeln", 50.9333, 6.95, openingHoursEarly, visitDuration, importance);
koeln.setWaitOnEarlyArrival(false);
koeln.setWaitOnEarlyArrivalFirstNode(true);
opti.addElement(koeln);
```

### The opening hours

Wuppertal opens later than the other nodes:

```java
// Wuppertal opens at 14:00, four hours after the other nodes, so Jack can reach it before it
// opens in the middle of his route.
List<IOpeningHours> openingHoursLate = new ArrayList<>();
openingHoursLate.add(
    new OpeningHours(
        ZonedDateTime.of(2020, MAY.getValue(), 6, 14, 0, 0, 0, ZoneId.of("Europe/Berlin")),
        ZonedDateTime.of(2020, MAY.getValue(), 6, 19, 0, 0, 0, ZoneId.of("Europe/Berlin"))));
```

The other four nodes open at 10:00, two hours after Jack starts work.

## Output and what to look for

The run prints the status and progress lines, then the result. The result was the same in two test runs: one route on May 6 with Aachen, Dueren, Koeln, Essen and Wuppertal, in this order. The stops of the route:

```text
0.0 Aachen / Geo  / Idle: 120.0 [min] / Arrival: 06.05.2020 08:00:00 (Europe/Berlin) , Departure: 06.05.2020 10:20:00 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 0.0 [min], 0.0 [km]
0.1 Dueren / Geo  / Arrival: 06.05.2020 10:41:21 (Europe/Berlin) , Departure: 06.05.2020 11:01:21 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 21.36 [min], 28.2 [km]
0.2 Koeln / Geo  / Arrival: 06.05.2020 11:28:36 (Europe/Berlin) , Departure: 06.05.2020 11:48:36 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 27.25 [min], 35.97 [km]
0.3 Essen / Geo  / Arrival: 06.05.2020 12:32:15 (Europe/Berlin) , Departure: 06.05.2020 12:52:15 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 43.64 [min], 57.61 [km]
0.4 Wuppertal / Geo  / Arrival: 06.05.2020 13:09:59 (Europe/Berlin) , Departure: 06.05.2020 13:29:59 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 17.74 [min], 23.41 [km][ CONSTRAINTVIOLATION/TIMECONSTRAINT/EARLY/ Early time [min]: 50.01213333333333 Value: 50.01213333333333 ]
```

- **Aachen, the first node:** Jack arrives at 08:00, because the node lies at his start position. Aachen opens at 10:00, so he waits 120 minutes (`Idle: 120.0 [min]`) and works from 10:00 to 10:20.
- **Dueren, Koeln and Essen:** Jack arrives after 10:00, when they are open, so no setting acts on them.
- **Wuppertal:** Jack arrives at 13:09:59, about 50 minutes before Wuppertal opens at 14:00, and starts at once. The result reports this as an EARLY violation with the early time in minutes.

The violation summary and the cost of the route show the early start:

```text
ViolationSumary: NumViolations: 1,EARLY: 1
Cost TotalSum: 283.65117253702726,Cost Distance: 239.12207926053716,Cost RouteTime: 33.42942222222222,Cost Early: 11.099671054267882
```

The route ends at 14:41:09, inside the working hours, and takes 401.15 minutes of the 10 hours that Jack may work.

## The two key switches

The behavior is configured per node using two flags:

### 1) `setWaitOnEarlyArrival(false)`
Meaning:
- If the resource arrives before the node's opening time, it is allowed to start service immediately; the result reports the early start as an EARLY violation with a cost.
- This suppresses the typical "arrive early -> idle until open" behavior.

In the example, all nodes use:
- `node.setWaitOnEarlyArrival(false);`

### 2) `setWaitOnEarlyArrivalFirstNode(true)`
Meaning:
- If the node becomes the **first node of a route**, then waiting rules apply (even if `setWaitOnEarlyArrival(false)` is set).
- This prevents the optimizer from exploiting early arrivals to start work at the first node before it opens.

In the example, all nodes use:
- `node.setWaitOnEarlyArrivalFirstNode(true);`

Combined interpretation:
- **First node**: waiting is enforced (work at the first node cannot begin before the node opens).
- **All subsequent nodes**: no waiting; service may start immediately on arrival, with an EARLY violation and its cost.

This is a clean architectural way to model:
- "Start work at the first node only within its opening hours, but once you are out, finish early if you can."

## When this feature is operationally correct

This pattern is realistic in many field service and delivery settings:

- A technician cannot start work at the first customer before that customer opens (dispatch rule),
- but once the technician is in the field, early arrivals can be served immediately, because:
  - the customer is happy to be served early,
  - service can begin as soon as the technician arrives,
  - the strict "time window" is more a planning guideline than a hard gate.

## When you should not use it

Do not use this behavior if your opening hours are hard operational constraints, for example:

- access gates that are physically closed,
- regulated slots (medical appointments),
- warehouse docks with strict appointment times,
- customers that must not be served early.

In those cases:
- keep `setWaitOnEarlyArrival(true)` (the node waits until it opens),
- and do not relax mid-route waiting.

## Recommended production guidance

### 1) Use it selectively
You can enable this only for nodes where "early service is acceptable":
- same-day service visits,
- flexible customers,
- tasks without strict appointment schedules.

Keep strict nodes in standard waiting mode.

### 2) Keep the first-node rule enabled when the opening time of the first node must be respected
If the opening hours of the first node of a route are binding, it is recommended to:
- keep `setWaitOnEarlyArrivalFirstNode(true)` for applicable nodes.

This prevents subtle schedule artifacts where the optimizer effectively starts work at the first node before it opens.

### 3) Make sure your downstream execution system aligns
If your dispatch or mobile app assumes:
- "do not start before OpeningHours",
then relaxing early-arrival waiting may produce plans that the execution system rejects.

Align planning and execution semantics.

## Summary

- `setWaitOnEarlyArrival(false)` allows immediate service even when arriving "early".
- `setWaitOnEarlyArrivalFirstNode(true)` re-enables waiting *only* when the node is the first node of the route.
- The combination is a practical way to enforce "no work at the first node before it opens" while still exploiting early arrivals later in the route.
- The example uses Wuppertal's late opening hours, and the wait at Aachen, the first node in two test runs, to illustrate the difference between first-node and mid-route behavior.

## Run it

Run the `main(String[] args)` method of `WaitOnEarlyArrivalFirstNodeExample` from your IDE, with the repository imported as a Maven project. You need a JDK in the version that the examples pom sets. You need no license file: without one, the example uses the public evaluation license (see the [license guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/license)).

## Related

- [Flexible start time (FlexTime)](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/flextime): the feature guide of a flexible start of the working hours; its section on early arrival at the first node names both settings.
- [Optimization properties](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties): the property reference, with `JOptWeight.TimeWindow` and its default.
- [Recommended ways to run an optimization](../../basic/recommendedimplementation_02/RecommendedImplementation.md): the recommended run pattern, a synchronous run and the legacy callback methods.
- [Advanced examples](../README.md): the overview of all Advanced examples.

---

Added: 2026-02-17 (JOpt core 7.5.3-j17) | Last updated: 2026-10-23
