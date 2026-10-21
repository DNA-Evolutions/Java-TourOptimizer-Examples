# First and last node of a route

`FirstLastNodeExample` asks for Koeln as the first node and Wuppertal as the last node of their route, with `setFirstNodeInRouteImportance` and `setLastNodeInRouteImportance`. Both settings are soft: a route that does not follow them is allowed, but it costs more. The page is for developers who want a preferred first or last stop on a route. If you have not built an optimization yet, start with the [First optimization example](../../basic/firstoptimization_01/FirstOptimizationExample.md).

## What the example shows

In many operational scenarios, planners want routes to begin or end with specific stops, for example:

- "Start the day with a key customer."
- "Finish the day at a handover location."
- "Visit a depot first for pickup, then execute deliveries."
- "End with a return-to-base inspection site."

JOpt supports this behavior with **first/last node importance**:
- `setFirstNodeInRouteImportance(int importance)`
- `setLastNodeInRouteImportance(int importance)`

These settings create **soft route anchoring**:
- the optimizer is incentivized (by penalty cost) to place the node first/last,
- but it may still choose a different first/last node if the global savings outweigh the penalty.

This makes the feature practical and safe:
* it guides the optimizer strongly,
* but it does not instantly destroy feasibility when reality prevents strict anchoring.

- `setFirstNodeInRouteImportance(int)` asks for a node as the first node of the route that holds it; `setLastNodeInRouteImportance(int)` asks for the last node. The example sets the value 10 on Koeln and on Wuppertal.
- The value scales the extra cost of a route in which the node is not first (or not last). The weights `JOptWeight.NodeImportance` and `JOptWeight.NodeType` scale the same cost; the example raises both.
- Each setting applies to the route that holds the node. Jack has two working days, and in the result one route holds Koeln and Wuppertal, the other neither of them.
- In this example the settings also change which nodes share a route; "Output and what to look for" compares a run without them.

## Scenario

The dates, times and coordinates are sample values.

- **Resource:** `Jack` starts at 50.775346, 6.083887 (Aachen) and works (resource `WorkingHours`) on May 6 and May 7, 2020, from 08:00 to 18:00 (Europe/Berlin). His maximal working time is 10 hours, the length of the working hours, and his maximal distance 1200 km. With a single resource, the first/last behavior is easy to interpret.
- **Nodes:** the example uses five `TimeWindowGeoNode` objects for five cities: Koeln, Essen, Dueren, Wuppertal and Aachen. They are open (node `OpeningHours`) on May 6 and May 7, 2020, from 08:00 to 12:00 (Europe/Berlin). Each visit takes 20 minutes, and all nodes have the importance 1. The Aachen node lies at Jack's start position.
- **First and last:** Koeln (first-node preference) with `setFirstNodeInRouteImportance(10)`, Wuppertal (last-node preference) with `setLastNodeInRouteImportance(10)`.

`addProperties` sets four properties:

- `JOptExitCondition.JOptGenerationCount` to 1000 and `JOpt.Algorithm.PreOptimization.SA.NumIterations` to 100000. Both are below their defaults, which keeps the run short.
- `JOptWeight.NodeImportance` to 5 and `JOptWeight.NodeType` to 5.0, which make the two settings stronger against the other costs.

The defaults are in the [property reference](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties).

## How the code works

The source is [FirstLastNodeExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/firstlastnode/FirstLastNodeExample.java). `example()` runs the steps in order:

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
- `addProperties` sets the four properties.
- `addNodes` adds the five nodes, with the first and last node settings on Koeln and Wuppertal. All nodes share a visit duration of 20 minutes and the importance 1 (base importance).
- `addResources` adds Jack, the one resource that is created, starting in Aachen, with a max working time of 10 hours, a max distance of 1200 km, no cost configuration of its own (the class does not call `setCost`) and two working days (see the time model in Scenario above).
- `attachToObservables` subscribes to the progress, warning, status and error events with the event API, before the run starts.
- `startAndPresentResult` starts the run, waits up to 5 minutes for the result and prints it.

### The weights

```java
// Both weights scale the cost of a node that is not first or last in its route, so higher
// values make the two settings stronger against the other costs.
props.setProperty("JOptWeight.NodeImportance", "5");
props.setProperty("JOptWeight.NodeType", "5.0");
```

### The first and the last node

Koeln asks to be the first node of its route:

```java
TimeWindowGeoNode koeln =
    new TimeWindowGeoNode("Koeln", 50.9333, 6.95, openingHours, visitDuration, importance);
// Koeln asks to be the first node of its route. A route with another first node costs more;
// the value 10 scales that cost.
koeln.setFirstNodeInRouteImportance(10);
opti.addElement(koeln);
```

Wuppertal asks to be the last node of its route:

```java
TimeWindowGeoNode wuppertal =
    new TimeWindowGeoNode(
        "Wuppertal", 51.2667, 7.18333, openingHours, visitDuration, importance);
// Wuppertal asks to be the last node of its route. A route with another last node costs more;
// the value 10 scales that cost.
wuppertal.setLastNodeInRouteImportance(10);
opti.addElement(wuppertal);
```

The other three nodes are plain `TimeWindowGeoNode` objects without these settings.

## Output and what to look for

The run prints the status and progress lines, then the result. In two test runs the result had the same two routes:

- **Route 0, May 6:** Koeln, Essen, Wuppertal. Koeln is the first node and Wuppertal the last.
- **Route 1, May 7:** Aachen, Dueren. This route holds neither Koeln nor Wuppertal; it starts with Aachen, the node at Jack's start position.

The node lines of route 0, the same in both runs:

```text
0.0 Koeln / Geo  / Arrival: 06.05.2020 08:47:59 (Europe/Berlin) , Departure: 06.05.2020 09:07:59 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 47.99 [min], 63.34 [km]
0.1 Essen / Geo  / Arrival: 06.05.2020 09:51:37 (Europe/Berlin) , Departure: 06.05.2020 10:11:37 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 43.64 [min], 57.61 [km]
0.2 Wuppertal / Geo  / Arrival: 06.05.2020 10:29:21 (Europe/Berlin) , Departure: 06.05.2020 10:49:21 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 17.74 [min], 23.41 [km]
```

The violation summaries of both routes are empty.

To see what the settings change, compare a run without them. In a test run with the two setter calls removed, all five nodes were on the route of May 6, in the order Aachen, Dueren, Koeln, Wuppertal, Essen. With the settings, Aachen and Dueren move to May 7, so that the route of May 6 can start with Koeln and end with Wuppertal.

## Hard vs soft interpretation (important)

First/last-node importance is a **soft constraint mechanism**:
- It is implemented as a penalty term (importance-driven) rather than as an architectural feasibility rule.

Meaning:
- The solver *can* violate it.
- You should not expect a guaranteed "first/last" if time windows, working hours, or hard constraints make it impossible or very expensive.

If you need a strict hard constraint (e.g., "pickup must happen first"), you typically model it differently (e.g., via explicit structure), depending on your overall architecture. Relationships do not give a strict hard constraint: they are pseudo constraints, and a broken relation adds a cost weighted by `JOptWeight.Relationships` (see [Relations between nodes](../relationship/Relations.md)).

## How "importance" works

### The importance value is the strength of the preference
Higher importance means:
- higher penalty cost if the node is not first/last,
- therefore stronger pull towards being first/last.

Lower importance means:
- the preference can be violated more easily when it improves the plan elsewhere.

The value is an integer, and the example uses `10` to make the effect visible.

### Importance interacts with weights
The example increases general weights related to node handling:

- `JOptWeight.NodeImportance = 5`
- `JOptWeight.NodeType = 5.0`

This is done to ensure penalties and node-related costs are strong enough to "matter" in the global objective.

Practical guidance:
- If first/last preferences appear too weak, increase:
  - first/last importance value, and/or
  - the relevant weights for node importance.
- If first/last preferences dominate too aggressively, reduce them.

## When to use first/last node importance in production

### Strong fit use cases
- "Try to start with a VIP customer" (preference, not a legal must)
- "Try to end with refueling location" (preference)
- "Try to end near a depot" (preference, not strict)
- "Try to start at a pickup location but allow exceptions" (preference)

### Cases where you should consider alternative modeling
If the "first/last" requirement is truly hard, such as:
- "Pickup must happen first, otherwise deliveries cannot happen."
- "End must be a mandatory depot return for compliance."

Then you usually want an architectural hard constraint model (pillars, etc.), rather than an importance-based preference. Relationships are pseudo constraints as well: a broken relation is a weighted cost, not a hard rule.

## How to tune and validate behavior

### Step 1: Start with moderate importance (e.g., 5-10)
Confirm:
- the preference is usually satisfied in normal instances.

### Step 2: Stress test with infeasible conditions
Deliberately create cases where:
- time windows force a different first/last order,
- working hours are tight.

Confirm:
- the solver violates the preference only when it produces a better global result or restores feasibility.

### Step 3: Validate downstream KPIs
If first/last anchoring is used for operational workflow reasons, ensure:
- route execution system agrees with "planned first/last" semantics,
- waiting times and arrival times remain sensible,
- the business understands when and why exceptions occur.

## Summary

- `setFirstNodeInRouteImportance(...)` and `setLastNodeInRouteImportance(...)` provide **soft** route anchoring.
- A higher importance increases the penalty for not being first/last, making the preference stronger.
- The optimizer can still violate the preference when the global savings outweigh the penalty or when constraints make it impractical.
- The example demonstrates:
  - Koeln as first node (importance 10),
  - Wuppertal as last node (importance 10),
  and uses higher node-related weights to make the effect clearly observable.

## Run it

Run the `main(String[] args)` method of `FirstLastNodeExample` from your IDE, with the repository imported as a Maven project. You need a JDK in the version that the examples pom sets. You need no license file: without one, the example uses the public evaluation license (see the [license guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/license)).

## Related

- [FirstLastNodeExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/firstlastnode/FirstLastNodeExample.java): the source of this example.
- [Optimization properties](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties): the property reference, with `JOptWeight.NodeImportance`, `JOptWeight.NodeType` and their defaults.
- [Recommended ways to run an optimization](../../basic/recommendedimplementation_02/RecommendedImplementation.md): the recommended run pattern, a synchronous run and the legacy callback methods.
- [Advanced examples](../README.md): the overview of all Advanced examples.

---

Added: 2026-02-17 (JOpt core 7.5.3-j17) | Last updated: 2026-10-21
