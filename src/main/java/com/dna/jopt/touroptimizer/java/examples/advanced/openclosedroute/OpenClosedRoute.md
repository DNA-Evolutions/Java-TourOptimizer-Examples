# Open and closed routes

`OpenClosedRouteExample` plans two working days for Jack with open routes: each route ends at its last node, and the way back to Jack's start position is not part of the route. The setting is `setIsClosedRoute(false)` on each `WorkingHours` object; working hours are closed routes with `setIsClosedRoute(true)`, and the example sets both days open. The page is for developers whose resources do not return to their start at the end of a day, or whose return is not working time. If you have not built an optimization yet, start with the [First optimization example](../../basic/firstoptimization_01/FirstOptimizationExample.md).

## What the example shows

In routing, the term **closed route** typically means:

- the resource starts at its start location (depot),
- visits a sequence of nodes,
- and then **returns to the start location**.

An **open route** means:

- the resource starts at its start location,
- visits nodes,
- but **does not have to return to the start location**.

JOpt supports both behaviors. The choice changes:
* feasibility against the maximal working time,
* route timing and "end of day" arrival,
* total travel distance and travel time,
* and the realism of models where vehicles end at a different location (e.g., last job near home base or near a new shift start).

- **Closed route (`setIsClosedRoute(true)`):** the route ends at the resource's start position. The drive back from the last node is part of the route; the result reports it as the termination time and distance.
- **Open route:** with `setIsClosedRoute(false)`, the route ends at its last node. The route header shows the value `false` in the line `Closed route`, a termination time and distance of 0.0, and no termination id.
- **Per working hours:** each `WorkingHours` object has its own setting, so a resource can have closed and open days. The example makes both days open.
- **The next day starts at home again:** an open route does not move the start of the next route. Route 1 starts at Jack's position in Aachen, like route 0.

## Scenario

The dates, times and coordinates are sample values.

- **Resource:** `Jack` starts at 50.775346, 6.083887 (Aachen) and works on May 6 and 7, 2020, from 08:00 to 17:00 (Europe/Berlin). Both working hours are open routes. His maximal working time is 13 hours and his maximal distance 1200 km, more than any day of this plan needs.
- **Nodes:** nine `TimeWindowGeoNode` objects: Koeln, Oberhausen, Essen, Dueren, Nuernberg, Heilbronn, Stuttgart, Wuppertal and Aachen. They are open on May 6 and 7, 2020, from 08:00 to 17:00. Each visit takes 20 minutes, and all nodes have the importance 1. The Aachen node lies at Jack's start position.

`addProperties` sets two properties: `JOptExitCondition.JOptGenerationCount` to 1000 and `JOpt.Algorithm.PreOptimization.SA.NumIterations` to 100000. Both are below their defaults, which keeps the run short; the defaults are in the [property reference](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties).

## How the code works

The source is [OpenClosedRouteExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/openclosedroute/OpenClosedRouteExample.java). `example()` runs the steps in order:

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
- `addNodes` adds the nine nodes.
- `addResources` adds Jack, with open routes on both days.
- `attachToObservables` subscribes to the progress, warning, status and error events with the event API, before the run starts.
- `startAndPresentResult` starts the run, waits up to 5 minutes for the result, prints it and returns it.
- `exportToKml` writes the result to `OpenClosedRouteExample.kml` in the working directory and prints `KML export failed: OpenClosedRouteExample.kml` if the export returns `false`.

### The open routes

`addResources` switches both working hours to open routes before it creates Jack:

```java
// Working hours are closed routes with setIsClosedRoute(true); this example sets both days
// open with false. Open routes end at the last node: the way back to the start is not part of
// the route.
workingHours.forEach(w -> w.setIsClosedRoute(false));
```

To keep a day closed, call `setIsClosedRoute(true)` on its `WorkingHours` object instead of `setIsClosedRoute(false)`.

## Output and what to look for

The run prints the status and progress lines, then the result. In two test runs the result was the same:

- **Route 0, May 6:** Aachen, Dueren, Koeln, Wuppertal, Essen, Oberhausen. The route stops at 11:45:53, when Jack leaves Oberhausen.
- **Route 1, May 7:** Heilbronn, Stuttgart, Nuernberg. The route stops at 15:07:09, when Jack leaves Nuernberg.

The header of route 1, trimmed to the four lines of the open route:

```text
Closed route           : false
Termi. time      [min] : 0.0
Termi. distance  [km]  : 0.0
Termination Id         : ---
```

The termination time and distance are 0.0 and there is no termination id, because the route ends at Nuernberg. The first stop of route 1 shows that the day starts in Aachen again: Heilbronn comes after `Driving: 216.37 [min], 285.61 [km]`, the drive from Aachen.

When you inspect the exported KML: a closed route typically shows a final segment returning to the start, and an open route ends at the last node without the return leg. Because the example forces open routes, you should see the latter: in the KML file, the line of each route ends at its last node, without a line back to Aachen.

## How to interpret results and exports

### Closed route behavior (`setIsClosedRoute(true)`)
If you keep `isClosedRoute = true`:
- the route must contain an implied return leg back to the start location,
- which increases:
  - total distance,
  - total travel time,
  - and the risk of going over the maximal working time.

This is the correct model when:
- vehicles must return to depot,
- end-of-day parking is at the start location,
- unloading/closing procedures require depot return.

### Open route behavior
With `isClosedRoute = false`:
- the route ends at the last visited node,
- there is no mandatory return-to-start leg.

This is the correct model when:
- vehicles do not return (e.g., cross-docking, multi-shift operations),
- "end of day" is wherever the last job finishes,
- drivers can finish near home or at a different base.

## Mixing open and closed behavior across days (advanced, recommended)

A major advantage of storing this on WorkingHours is that you can model scenarios like:

- **Day 1**: closed route (must return to depot)
- **Day 2**: open route (ends where it ends)
- **Weekend**: different behavior (e.g., must return to depot due to parking constraints)

Implementation pattern:
- build WorkingHours blocks,
- set `setIsClosedRoute(true/false)` per block.

This yields realistic multi-day models without duplicating resources or building special-case routing logic.

## Operational guidance

### When open routes are usually correct
- field service technicians who end near the last customer,
- courier services with flexible end-of-day parking,
- multi-depot or "handover" operations where the next shift starts near the last stop (an open route alone does not move the start of the next route of the same resource: route 1 of the example starts in Aachen again).

### When closed routes are usually correct
- delivery fleets that must return for reloading, cleaning, charging,
- regulated fleets that must return to controlled yards,
- scenarios where shift end includes depot operations.

### Be explicit in downstream processes
If your execution system assumes:
- vehicles always return to depot,

then open-route planning must be reconciled, for example:
- by adding an explicit "end depot" node,
- or by choosing closed-route behavior.

## Pitfalls and how to avoid them

### Pitfall 1: Assuming open/closed is global
It is not global; it is per WorkingHours interval.  
If you add new WorkingHours blocks later, ensure you configure them consistently.

### Pitfall 2: Mixing open routes with strict depot KPIs
If your KPI dashboards assume depot return time, update the KPIs:
- "route end time" becomes last-node completion time,
- "return-to-depot" becomes optional or modeled explicitly.

### Pitfall 3: Unexpected long routes in open mode
Open routes can make long "one-way" travel appear cheaper, because the return leg disappears.
If this is undesirable, mitigate via:
- max distance constraints,
- working time constraints,
- or explicit "end location" requirements.

## Run it

Run the `main(String[] args)` method of `OpenClosedRouteExample` from your IDE, with the repository imported as a Maven project. You need a JDK in the version that the examples pom sets. You need no license file: without one, the example uses the public evaluation license (see the [license guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/license)). The run writes `OpenClosedRouteExample.kml` into the working directory.

## Related

- [OpenClosedRouteExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/openclosedroute/OpenClosedRouteExample.java): the source of this example.
- [Overnight stay](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/overnight_stay): the feature guide for multi-day routes in which a resource stays out overnight instead of driving home.
- [Alternate destination at the end of a route](../alternatedestination/AlternateDestinationExample.md): a route that ends at another place than the start.
- [Recommended ways to run an optimization](../../basic/recommendedimplementation_02/RecommendedImplementation.md): the recommended run pattern, a synchronous run and the legacy callback methods.
- [Advanced examples](../README.md): the overview of all Advanced examples.

---

Added: 2026-02-17 (JOpt core 7.5.3-j17) | Last updated: 2026-10-21
