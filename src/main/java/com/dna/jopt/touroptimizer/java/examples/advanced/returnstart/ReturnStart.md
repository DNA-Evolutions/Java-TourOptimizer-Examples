# Return to start after a node

`ReturnStartExample` sends Jack back to his start location after each of his four nodes, where he stays for 20 minutes before he drives on. The return is set per node with `setIsReturnStart`, and the result shows it as a stop of its own inside the route. The page is for developers who model a reload or another step at the depot after a visit. The [Return to start feature guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/return_to_start) describes the feature, with its field in the JSON input of the REST service.

## What the example shows

Return To Start (often referred to as **Return2Start**) is a routing feature that forces a resource to **return to its start location (home/depot)** between visits, and after the last visit of a route.

This is a highly effective architectural tool for problems where "direct chaining" between customer visits is not allowed or not realistic.

- **The setting:** `setIsReturnStart(Duration)` on a node. After a visit of that node, the resource drives back to its start location, stays there for the given duration and then drives on to the next node of the route.
- **The stay is required:** `setIsReturnStart` needs a stay of at least 2 seconds. With a shorter stay it enables no return and returns `false`. The example checks the boolean and prints a line for a node that does not take the return.
- **After every node, also the last one:** a return follows each node with the setting, also the last node of a route. The route goes on after a return; the return does not end it.
- **The returns in the result:** each return is a stop named after the node and the resource, for example `Koeln -> Jack from Aachen`, with the stay as its duration.
- **Open routes:** the example sets its routes open with `setIsClosedRoute(false)`, so a route has no drive back to the start at its end. The return after the last node comes from the return to start alone.
- **The returns take working time:** each return adds a drive back to the start and the stay. The four nodes with their returns take longer than Jack may work in one day, and the result spreads them over the two days (see "Output and what to look for").

## Scenario

The dates, times and coordinates are sample values.

- **Resource:** `Jack from Aachen` starts at 50.775346, 6.083887 (Aachen) and works on May 6 and 7, 2020, from 08:00 to 17:00 (Europe/Berlin), on open routes. His maximal working time is 9 hours, the length of the working hours, and his maximal distance 1200 km.
- **Nodes:** four `TimeWindowGeoNode` objects: Koeln, Essen, Dueren and Wuppertal. They are open on May 6 and 7, 2020, from 08:00 to 17:00. Each visit takes 20 minutes, and all nodes have the importance 1.
- **Return to start:** every node calls `setIsReturnStart` with a stay of 20 minutes, a sample value for a reload.

`addProperties` sets `JOptExitCondition.JOptGenerationCount` to 1000 and `JOpt.Algorithm.PreOptimization.SA.NumIterations` to 100000. Both are below their defaults, which keeps the run short; the defaults are in the [property reference](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties).

## How the code works

The source is [ReturnStartExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/returnstart/ReturnStartExample.java). `example()` runs the steps in order:

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
- `addNodes` adds the four nodes, each with the return to start.
- `addResources` adds Jack with open routes.
- `attachToObservables` subscribes to the progress, warning, status and error events with the event API, before the run starts.
- `startAndPresentResult` starts the run, waits up to 5 minutes for the result and prints it.

### The return after each node

`addNodes` sets the same stay on every node. The block shows Koeln; Essen, Dueren and Wuppertal follow the same pattern:

```java
// After each node Jack drives back to his start location and stays there for 20 minutes, a
// sample value for a reload, before he drives on. The return is a stop inside the route; it
// does not end the route.
Duration intermediateStayAtHomeDuration = Duration.ofMinutes(20);

INode koeln =
    new TimeWindowGeoNode("Koeln", 50.9333, 6.95, openingHours, visitDuration, importance);
enableReturnStart(koeln, intermediateStayAtHomeDuration);
opti.addElement(koeln);
```

`enableReturnStart` calls `setIsReturnStart` and checks its boolean:

```java
private static void enableReturnStart(INode node, Duration stayAtStartDuration) {
  // setIsReturnStart needs a stay of at least 2 seconds. With a shorter stay it enables no
  // return and returns false.
  if (!node.setIsReturnStart(stayAtStartDuration)) {
    System.out.println("Return to start not enabled for " + node.getId());
  }
}
```

With the stay of 20 minutes, every node takes the return, so the line "Return to start not enabled for ..." does not appear in the output.

### Open routes

`addResources` sets both working days to open routes and gives Jack a maximal working time of 9 hours:

```java
// Open routes have no drive back to the start at the end, so the return after the last node
// comes from the return to start alone.
workingHours.forEach(w -> w.setIsClosedRoute(false));

// 9 h per day, the length of the working hours.
Duration maxWorkingTime = Duration.ofHours(9);
```

[Open and closed routes](../openclosedroute/OpenClosedRoute.md) shows the setting on its own.

## Output and what to look for

The run prints the status and progress lines, then the result. The result was the same in two test runs:

- **Route 0, May 6:** Wuppertal, Essen and Koeln, each followed by its return to Aachen.
- **Route 1, May 7:** Dueren, followed by its return.

The stops of route 0, the same in both runs:

```text
0.0 Wuppertal / Geo  / Arrival: 06.05.2020 09:11:40 (Europe/Berlin) , Departure: 06.05.2020 09:31:40 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 71.67 [min], 94.6 [km]
0.1 Wuppertal -> Jack from Aachen / Geo  / Arrival: 06.05.2020 10:42:50 (Europe/Berlin) , Departure: 06.05.2020 11:02:50 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 71.17 [min], 93.94 [km]
0.2 Essen / Geo  / Arrival: 06.05.2020 12:18:16 (Europe/Berlin) , Departure: 06.05.2020 12:38:16 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 75.44 [min], 99.58 [km]
0.3 Essen -> Jack from Aachen / Geo  / Arrival: 06.05.2020 13:53:14 (Europe/Berlin) , Departure: 06.05.2020 14:13:14 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 74.97 [min], 98.96 [km]
0.4 Koeln / Geo  / Arrival: 06.05.2020 15:01:13 (Europe/Berlin) , Departure: 06.05.2020 15:21:13 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 47.99 [min], 63.34 [km]
0.5 Koeln -> Jack from Aachen / Geo  / Arrival: 06.05.2020 16:09:03 (Europe/Berlin) , Departure: 06.05.2020 16:29:03 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 47.84 [min], 63.14 [km]
```

Each return stop lasts the 20 minutes of the stay, and its driving time is the drive back to Aachen. The last stop, `Koeln -> Jack from Aachen`, is the return after the last node. The route is open, so it has no termination and ends with that return (trimmed to these lines of the route information):

```text
Closed route           : false
Termination Id         : ---
```

Route 1 holds Dueren and its return:

```text
1.0 Dueren / Geo  / Arrival: 07.05.2020 08:21:21 (Europe/Berlin) , Departure: 07.05.2020 08:41:21 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 21.36 [min], 28.2 [km]
1.1 Dueren -> Jack from Aachen / Geo  / Arrival: 07.05.2020 09:02:43 (Europe/Berlin) , Departure: 07.05.2020 09:22:43 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 21.35 [min], 28.19 [km]
```

Every node is a round trip from Aachen, with the visit and the stay, so the time of a route is the sum of its round trips. Route 0 takes 509.06 minutes and route 1 82.72 minutes. One day with all four nodes would take 591.78 minutes, more than the 540 minutes (9 hours) that Jack may work; in both runs the plan puts Dueren on May 7 instead. Both routes end inside their working hours, and neither shows a violation.

## Typical use cases

Return2Start is commonly used when a resource must return to a base between tasks, for example:

- **Reloading / restocking** (goods, tools, spare parts)
- **Pickup & Delivery simplification** for "return after each delivery" patterns
- **Regulated operations** where vehicles must return to a controlled yard between jobs
- **Medical or lab logistics** with mandatory depot handling steps
- **Quality / paperwork / scanning** steps that must happen at the base
- **Battery charging** or equipment swap at the depot

In short: whenever your process requires an explicit depot loop that is not just "route termination".

## How to enable Return2Start

Return2Start is activated **per node**:

- `node.setIsReturnStart(intermediateStayAtHomeDuration);`

This tells the optimizer:
- before continuing from this node to the next node, return to the resource start location,
- stay there for the given duration,
- then proceed to the next visit (a return also follows the last node of a route).

### The "stay at home" duration (intermediate depot dwell time)

In `ReturnStartExample`, this is configured once and applied to multiple nodes:

- `Duration intermediateStayAtHomeDuration = Duration.ofMinutes(20);`

Interpretation:
- after each affected visit, the resource must "spend" 20 minutes at the start location
  (e.g., reload, paperwork, handover, charging).

Why this matters:
- without the dwell time, `setIsReturnStart` enables no return at all: a stay of less than 2 seconds returns `false`. A stay of a few seconds represents "drive home and immediately leave again".
- with dwell time, you get a more realistic model and better time-window feasibility behavior.

## Why this can be better than modeling full PND (and when it is not)

The documentation lists a core advantage:
- Return2Start can **relax the modeling and optimization effort** when your business rule is simply "return to start between visits".

This is often simpler than implementing a full pickup-and-delivery capacity model when:
- the only reason you go home is to reload/reset between visits,
- and you do not need fine-grained load tracking.

However, use full PND if you need:
- multiple goods types,
- partial fulfillment rules,
- timed transport SLAs (TimedLoad),
- flexible production/reloading (FlexLoads),
- or detailed depot reports.

Return2Start is best understood as:
- a direct architectural feature for depot-loop routing,
- not a substitute for full logistics inventory modeling.

## Modeling patterns and best practices

### Pattern A: Only some nodes require return to start
You do not have to apply Return2Start to every node.  
You can apply it selectively, for example:
- hazardous deliveries require depot return,
- normal deliveries can chain directly.

### Pattern B: Calibrate dwell time realistically
Set `intermediateStayAtHomeDuration` based on real operations:
- 5-10 minutes (quick restock),
- 15-30 minutes (loading paperwork, equipment reset),
- 60+ minutes (charging cycles).

Dwell time is a critical parameter because it influences:
- time-window feasibility,
- how many visits fit into a day,
- and whether the solver chooses certain nodes or not (if optional nodes exist).

### Pattern C: Keep routes open unless you explicitly need end-of-day return
If your operation does not require returning home at the end of the day, model:
- open routes (as the example does),
- and rely on Return2Start for depot loops between visits.

If you do require depot return at day end:
- set closed routes for the relevant WorkingHours blocks.

## Common pitfalls

### Pitfall 1: Confusing Return2Start with route termination
Return2Start inserts intermediate return legs; it is not "end the route".

### Pitfall 2: Over-constraining the plan
Forcing depot return after many nodes can drastically increase travel time and reduce throughput.
Use it only where it reflects a real business rule.

### Pitfall 3: Dwell time too small or too large
- Too small: unrealistic; can appear to "create time" at the depot.
- Too large: makes instances infeasible or reduces plan quality unnecessarily.

## Summary

- Return2Start forces the resource to return to its start location between visits, and after the last visit of a route.
- It is enabled per node via `setIsReturnStart(Duration)`.
- The duration represents mandatory time spent at the start location before continuing.
- Return2Start is not route termination; it inserts intermediate "return-to-depot" steps.
- It is particularly useful for depot-loop processes such as reloading and reset operations, and can simplify certain pickup/delivery patterns.

## Run it

Run the `main(String[] args)` method of `ReturnStartExample` from your IDE, with the repository imported as a Maven project. You need a JDK in the version that the examples pom sets. You need no license file: without one, the example uses the public evaluation license (see the [license guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/license)).

## Related

- [Return to start](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/return_to_start): the feature guide, with the JSON field, the use cases and the limits.
- [Open and closed routes](../openclosedroute/OpenClosedRoute.md): routes that end at their last node, and routes that drive back to the start.
- [Optimization properties](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties): the property reference, with the defaults.
- [Recommended ways to run an optimization](../../basic/recommendedimplementation_02/RecommendedImplementation.md): the recommended run pattern, a synchronous run and the legacy callback methods.
- [Advanced examples](../README.md): the overview of all Advanced examples.

---

Added: 2026-02-17 (JOpt core 7.5.3-j17) | Last updated: 2026-10-21
