# Reading values from an optimization result

An `IOptimizationResult` gives access to the job values, the routes, the route items and the violations of a run, and these examples read them out and print them as a report with headers. In the scenario, a maximal working time of 4 hours and opening hours that end at 12:00 produce overtime and late arrivals, so the violation part of the result has content to read. The page is for developers who process the result in code: for a console report, for automated checks or for your own export. If you have not run an example yet, start with the [First optimization example](../firstoptimization_01/FirstOptimizationExample.md); the [First optimization tutorial](https://www.dna-evolutions.com/docs/getting-started/tutorials/first-optimization#analyzing-the-result) explains the printed result.

## What the example shows

Two classes show the techniques:

- [ReadOutResultExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic/readoutresult_05/ReadOutResultExample.java) reads job values from the result, and costs and violations from the cost and violation controller of each route.
- [ReadOutResultWithHeadersExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic/readoutresult_05/ReadOutResultWithHeadersExample.java) prints a report: a header with the job totals, a header for each route, the route items and the violations.

`System.out.println(result)` prints the whole result as text, as in the [First optimization example](../firstoptimization_01/FirstOptimizationExample.md). These two examples read the values through the API instead.

The printed result has a job header, a header per route, the route items in the order of the visits and the violations; the [First optimization tutorial](https://www.dna-evolutions.com/docs/getting-started/tutorials/first-optimization#analyzing-the-result) explains each part.

`ReadOutResultExample` prints the job values and the items of each route; `ReadOutResultWithHeadersExample` prints the job header and a header per route.

## Scenario

Both classes use the same data:

- **Nodes:** eight nodes in Germany: Koeln, Essen, Dueren, Nuernberg, Heilbronn, Stuttgart, Wuppertal and Aachen. Every node is open on May 6 and 7, 2020, from 08:00 to 12:00, a visit takes 20 minutes, and all nodes have the same importance.
- **Resource:** Jack starts in Aachen and works on May 6 and 7 from 08:00 to 18:00. The maximal distance of 1200 km does not limit any route of the example.
- **Violations on purpose:** Jack's maximal working time is 4 hours, shorter than both of his routes, so the result shows overtime. The nodes close at 12:00, so the result shows late arrivals on May 7.
- **Properties:** the examples set `JOptExitCondition.JOptGenerationCount` to 2000 and `JOpt.Algorithm.PreOptimization.SA.NumIterations` to 100000, fewer than the defaults, to keep the run short. The [property reference](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties) lists the defaults.
- **Time zone and data:** Europe/Berlin. The dates and the coordinates are sample values.

## How the code works

The code is in [ReadOutResultExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic/readoutresult_05/ReadOutResultExample.java) and [ReadOutResultWithHeadersExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic/readoutresult_05/ReadOutResultWithHeadersExample.java).

### The steps

`example()` of `ReadOutResultExample`; `ReadOutResultWithHeadersExample` has the same steps:

```java
public void example()
    throws InterruptedException, ExecutionException, TimeoutException, InvalidLicenceException,
    IOException {
  // The public evaluation license, or your own license if you put a license file where
  // ExampleLicenseHelper looks for it.
  ExampleLicenseHelper.setLicense(this);
  addProperties(this);
  addNodes(this);
  addResources(this);
  attachToObservables(this);
  startAndPresentResult(this);
}
```

`attachToObservables` subscribes to the progress, warning, status and error events before the run starts, because the subjects replay only a few past events. `startAndPresentResult` starts the run with `startRunAsync()`, waits for the result for up to 5 minutes and reads the values from it. `attachToObservables` uses the event API; [Recommended ways to run an optimization](../recommendedimplementation_02/RecommendedImplementation.md) compares it with the legacy callback methods.

### Job values and routes in ReadOutResultExample

Before looking at single routes, the example reads two job-level values from the result: the unassigned nodes (`result.getUnassignedNodes()`, of which it prints the number) and the relationship cost (`result.getJobRelationshipCost()`):

```java
CompletableFuture<IOptimizationResult> resultFuture = opti.startRunAsync();
// Wait for the result; without the wait the JVM would end before the run does.
IOptimizationResult result = resultFuture.get(5, TimeUnit.MINUTES);

System.out.println("\nUnassigned nodes: " + result.getUnassignedNodes().size());
System.out.println("\nRelationship cost: " + result.getJobRelationshipCost());
```

The example then loops over all routes in the result (`result.getRoutes()`). For each route, it reads the route cost and violation controller (`route.getRouteCostAndViolationController()`) and prints costs from it: `controller.getCost()` (route cost) and `controller.getCostDistance()` (distance cost of the route). The controller also has a cost for each kind of cost, for example `getCostLate()`.

```java
for (ILogicEntityRoute route : result.getRoutes()) {
  ILogicRouteCostAndViolationController controller =
      route.getRouteCostAndViolationController();

  System.out.println("\nRouteCost: " + controller.getCost());

  List<ILogicRouteElementDetailItem> details = result.getOrderedRouteItems(route);
  details.forEach(System.out::println);

  controller.getRouteViolations().forEach(System.out::println);

  // Besides the distance cost the controller has a cost per kind, for example getCostLate().
  System.out.println("\nRouteDistanceCost: " + controller.getCostDistance());

  for (ILogicRouteElementDetailItem detail : details) {
    List<IViolation> nodeViolations = controller.getNodeViolations(detail.getElement().getId());
    if (!nodeViolations.isEmpty()) {
      System.out.println(detail.getElement().getId() + ": " + nodeViolations);
    }
  }
}
```

The route's visit sequence comes from `result.getOrderedRouteItems(route)`, in the order of arrival. This returns a list of `ILogicRouteElementDetailItem`. Each item is one stop. The example prints the items as they are; the output below shows their fields: the element id, arrival, departure, visit duration, and the driving time and distance from the previous element.

### The report of ReadOutResultWithHeadersExample

This example takes the same extraction and turns it into a **human-friendly console report**, modeled after the tutorial's result header and route header breakdown.

The job header uses the job getters of the result:

```java
// The job getters return seconds and meters.
StringBuilder jobHeader = new StringBuilder();
jobHeader.append("\n-------------------------------- --------------------------");
jobHeader.append("\n--------------------- RESULTS -----------------------------");
jobHeader.append("\n----------------- -----------------------------------------");
jobHeader.append("\n Number of Route         : " + result.getJobRouteCount());
jobHeader.append("\n Total Route Elements    : " + result.getJobElementCount());
jobHeader.append("\n Total cost              : " + result.getJobCost());
jobHeader.append("\n-----------------------------------------------------------");
jobHeader.append("\n Total time        [h]   : " + toHours(result.getJobTimeSeconds()));
jobHeader.append(
    "\n Total idle time   [h]   : " + toHours(result.getJobIdleTimeSeconds()));
jobHeader.append(
    "\n Total prod. time  [h]   : " + toHours(result.getJobProductiveTimeSeconds()));
jobHeader.append(
    "\n Total tran. time  [h]   : " + toHours(result.getJobTransitionTimeSeconds()));
jobHeader.append(
    "\n Total distance    [km]  : " + result.getJobTransitionDistanceMeter() / 1000);
jobHeader.append(
    "\n Termi. time       [h]   : "
        + toHours(result.getJobTerminationTransitionTimeSeconds()));
jobHeader.append(
    "\n Termi. distance   [km]  : "
        + result.getJobTerminationTransitionDistanceMeter() / 1000);
System.out.println(jobHeader.toString());
```

The job getters return seconds (the getters that end in `Seconds`) and meters (the getters that end in `Meter`). `toHours` turns seconds into hours with two decimals. It formats with `Locale.ROOT`, so the decimal separator is a dot on every machine:

```java
private static String toHours(int seconds) {
  return String.format(Locale.ROOT, "%.2f", seconds / 3600.0);
}
```

The distances are divided by 1000 in integer arithmetic, so they print as whole kilometers.

For each route, the example prints a route header from the joined detail controller of the route (`route.getJoinedDetailController()`), which returns milliseconds and meters. The excerpt stops after the idle time:

```java
for (ILogicEntityRoute route : result.getRoutes()) {
  // The joined detail controller of a route returns milliseconds and meters.
  StringBuilder routeHeader = new StringBuilder();
  routeHeader.append("\n-----------------------------------------------------------");
  routeHeader.append("\nRoute information");
  routeHeader.append("\nRouteId                : " + route.getRouteId());
  routeHeader.append(
      "\nResource               : " + route.getCurrentVisitingResource().getId());
  routeHeader.append(
      "\nTransit time     [min] : "
          + (int) (route.getJoinedDetailController().getCurTransitTime() / 1000.0 / 60.0));
  routeHeader.append(
      "\nProductive. time [min] : "
          + (int) (route.getJoinedDetailController().getCurProductiveTime() / 1000.0 / 60.0));
  routeHeader.append(
      "\nIdle time        [min] : "
          + (int) (route.getJoinedDetailController().getCurIdleTime() / 1000.0 / 60.0));
```

The route header holds:

- the route id (`route.getRouteId()`) and the resource id (`route.getCurrentVisitingResource().getId()`);
- the transit, productive and idle time (`getCurTransitTime()`, `getCurProductiveTime()`, `getCurIdleTime()`), and the two parts of the idle time, `getCurWhitSpaceIdleTime()` (printed as "White Idle time") and `getCurInducedIdleTime()` (printed as "Ind. Idle time");
- the route distance (`getCurDistance()`);
- the termination (return-to-home) time and distance (`getTerminationTransitTime()`, `getTerminationTransitDistance()`).

Times print as whole minutes, distances as kilometers.

Conceptually, this corresponds to what the tutorial calls:
- route time,
- transit time,
- productive time,
- idle time,
- route distance,
- termination time/distance.

After the route header, the example prints the route cost (`result.getRouteCost(route)`), the ordered route items (`result.getOrderedRouteItems(route)`), the route violations (`result.getRouteViolations(route)`) and the node violations (`route.getRouteCostAndViolationController().getNodeViolations(...)`).

### Common pitfalls

- **Not waiting for completion**: if you run asynchronously and the JVM exits before the run ends, you never get the result, and the legacy `onAsynchronousOptimizationResult(...)` is never called. The examples wait with `resultFuture.get(5, TimeUnit.MINUTES)`.
- **Unit confusion**: always normalize time and distance units (milliseconds vs seconds; meters vs kilometers) consistently across your report.
- **Duplicate printing**: decide whether you want "pretty printed" headers, raw detail item prints, or both, otherwise logs can become noisy.

## Output and what to look for

### Output of ReadOutResultExample

After the event lines, the example prints these lines, the same in two test runs:

```text
Unassigned nodes: 0

Relationship cost: 0.0

RouteCost: 567.4941601558551
 Aachen / Geo  / Arrival: 06.05.2020 08:00:00 (Europe/Berlin) , Departure: 06.05.2020 08:20:00 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 0.0 [min], 0.0 [km]
 Dueren / Geo  / Arrival: 06.05.2020 08:41:21 (Europe/Berlin) , Departure: 06.05.2020 09:01:21 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 21.36 [min], 28.2 [km]
 Koeln / Geo  / Arrival: 06.05.2020 09:28:36 (Europe/Berlin) , Departure: 06.05.2020 09:48:36 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 27.25 [min], 35.97 [km]
 Wuppertal / Geo  / Arrival: 06.05.2020 10:19:17 (Europe/Berlin) , Departure: 06.05.2020 10:39:17 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 30.67 [min], 40.49 [km]
 Essen / Geo  / Arrival: 06.05.2020 10:57:02 (Europe/Berlin) , Departure: 06.05.2020 11:17:02 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 17.75 [min], 23.43 [km]
[ CONSTRAINTVIOLATION/MAXTIME/EXCEEDED/ Overtime [h]: 0.5334627777777783 Value: 0.5334627777777783 ]

RouteDistanceCost: 227.05030559418685

RouteCost: 56433.494065366285
 Heilbronn / Geo  / Arrival: 07.05.2020 11:36:22 (Europe/Berlin) , Departure: 07.05.2020 11:56:22 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 216.37 [min], 285.61 [km]
 Stuttgart / Geo  / Arrival: 07.05.2020 12:27:53 (Europe/Berlin) , Departure: 07.05.2020 12:47:53 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 31.52 [min], 41.6 [km]
 Nuernberg / Geo  / Arrival: 07.05.2020 14:47:09 (Europe/Berlin) , Departure: 07.05.2020 15:07:09 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 119.28 [min], 157.44 [km]
[ CONSTRAINTVIOLATION/MAXTIME/EXCEEDED/ Overtime [h]: 8.032686666666667 Value: 8.032686666666667 ]

RouteDistanceCost: 873.7888580887565
Stuttgart: [[ CONSTRAINTVIOLATION/TIMECONSTRAINT/LATE/ Late time [min]: 47.88531666666667 Value: 47.88531666666667 ]]
Nuernberg: [[ CONSTRAINTVIOLATION/TIMECONSTRAINT/LATE/ Late time [min]: 187.16055 Value: 187.16055 ]]
```

Look for:

- no unassigned nodes and no relationship cost;
- the route of May 6 with five nodes and about 0.53 hours of overtime as a route violation;
- the route of May 7 with three nodes, about 8.03 hours of overtime, and the late arrivals at Stuttgart and Nuernberg as node violations.

### Output of ReadOutResultWithHeadersExample

The report, trimmed to the job header and the header of the first route, is the same in two test runs:

```text
-------------------------------- --------------------------
--------------------- RESULTS -----------------------------
----------------- -----------------------------------------
 Number of Route         : 2
 Total Route Elements    : 9
 Total cost              : 57000.98822552214
-----------------------------------------------------------
 Total time        [h]   : 16.57
 Total idle time   [h]   : 0.00
 Total prod. time  [h]   : 2.67
 Total tran. time  [h]   : 13.90
 Total distance    [km]  : 1100
 Termi. time       [h]   : 6.16
 Termi. distance   [km]  : 488
```

```text
-----------------------------------------------------------
Route information
RouteId                : 0
Resource               : Jack
Transit time     [min] : 172
Productive. time [min] : 100
Idle time        [min] : 0
White Idle time  [min] : 0
Ind. Idle time   [min] : 0
Route Distance   [km]  : 227.05
Termi. time      [min] : 74
Termi. distance  [km]  : 98.957
```

Look for the hours with two decimals. They agree with the route headers: the transit times of the two routes, 172 and 661 minutes, add up to about 13.9 hours (the route headers cut the minutes to whole numbers). After each route header, the report prints the same route cost, route items and violations as `ReadOutResultExample`.

### Beyond console output

To go further, use one or more of these if you want to go beyond console output:

- Write the per-stop rows to CSV (a per-stop table for analysts).
- Serialize a compact "route plan JSON" for APIs/clients.
- Write the result to KML for map visualization, as in [Export a result to a KML file](../io_03/Export2KMLExample.md), to see the routes on a map.
- Build a UI timeline using arrival/departure timestamps.

## Two complementary ways to "read" a result

### 1) Use `result.toString()` for debugging and reproducibility
The tutorial prints the textual representation produced by `OptimizationResult.toString()`; saving it is practical for later debugging and for comparing runs.

Use this when:
- you want a quick sanity check,
- you store logs for support/replay,
- you need a compact output without writing your own report logic.

### 2) Use the API for structured, machine-readable extraction
When you need more than a diagnostic dump (e.g., a table of planned arrivals, or a UI timeline), you should read the result object directly.

This document focuses on the structured approach, while still encouraging you to also store the `toString()` output in logs.

## Basic structured extraction (`ReadOutResultExample`)

`ReadOutResultExample` shows the core technique without "pretty headers".

### Collect violations
The example demonstrates two layers of violations:

**Route-level**:
- `route.getRouteCostAndViolationController().getRouteViolations()`

**Node-level (per element id)**:
- `route.getRouteCostAndViolationController().getNodeViolations(detail.getElement().getId())`

This pattern is ideal for:
- feasibility checks,
- SLA validation (late arrivals),
- reporting constraint breaches per stop.

## How to build your own "result reader" (recommended blueprint)

If you want a robust implementation you can reuse across environments, the following structure works well:

### Step 1: Always persist `toString()` for debugging
Even if you parse the result structurally, keep a copy of the textual dump:
- it accelerates incident triage,
- it helps with comparing runs.

### Step 2: Extract a job summary object
Populate a DTO with:
- total routes,
- scheduled routes (if you use that concept),
- total time/distance,
- cost figure-of-merit,
- termination totals.

This is the data your UI/dashboard typically wants "up top".

### Step 3: Extract per-route summaries
For each route:
- resource id,
- working-hours index (if applicable in your scenario),
- route start/stop (effective),
- transit/productive/idle time,
- distance,
- termination metrics.

### Step 4: Extract per-stop details
From `getOrderedRouteItems(route)` build per-stop rows:
- element id,
- visiting index,
- arrival/departure,
- service duration,
- travel time/distance from previous element.

This is the data you want for:
- customer notifications,
- driver instructions,
- Gantt/timeline renderings.

### Step 5: Extract violations and classify them
- route-level violations: "this route violates constraints overall"
- node-level violations: "this stop violates constraints specifically"

Then decide how to present them:
- as hard errors,
- as warnings,
- or as "exceptions requiring review".

## Practical "interesting" checks you can implement immediately

These checks produce high value with minimal work:

1. **Late arrival audit**  
   Print any node violations, but group them by route and sort by severity.

2. **Idle-time hotspots**  
   Identify routes/stops with high idle time (often caused by tight time windows).

3. **Distance vs. productive ratio**  
   Compare transit time vs productive time per route; this quickly reveals unbalanced planning.

4. **Termination cost control**  
   Watch termination distance/time; it's an easy indicator that the last stop is far from the home base.

## Run it

Run the `main` method of `ReadOutResultExample` or `ReadOutResultWithHeadersExample` from your IDE. You need a JDK in the version that the examples pom sets. You need no license file: without one, the examples use the public evaluation license (see the [license guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/license)). No other example needs to run first, and the examples write no files.

## Related

- [First optimization tutorial, analyzing the result](https://www.dna-evolutions.com/docs/getting-started/tutorials/first-optimization#analyzing-the-result): the printed result of a run, with its header and its routes.
- [Property reference](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties): the property keys of the examples and their defaults.
- [Export a result to a KML file](../io_03/Export2KMLExample.md): the KML example, which writes a result to a KML file for a map.
- [Recommended ways to run an optimization](../recommendedimplementation_02/RecommendedImplementation.md): the recommended run pattern, a synchronous run and the legacy callback methods.
- [Reactive events and streams](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/reactive_events): the event API that `attachToObservables` uses.
- [Basic examples](../README.md): the overview of all Basic examples.

---

Added: 2026-02-17 (JOpt core 7.5.3-j17) | Last updated: 2026-10-21
