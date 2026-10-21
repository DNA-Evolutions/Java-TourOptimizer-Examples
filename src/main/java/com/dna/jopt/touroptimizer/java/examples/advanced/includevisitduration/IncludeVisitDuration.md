# Opening hours that bound only the arrival

`IncludeVisitDurationExample` gives the node Koeln opening hours of 15 minutes, from 09:00 to 09:15, and a visit of 20 minutes. With `setIsDutyHoursIncludesVisitDuration(false)`, these opening hours bound only the arrival: Jack must arrive between 09:00 and 09:15, and the visit may end after 09:15 without a violation. The page is for developers whose nodes have an arrival window rather than a window for the whole visit, for example a site that can be entered only at certain times. If you have not built an optimization yet, start with the [First optimization example](../../basic/firstoptimization_01/FirstOptimizationExample.md).

## What the example shows

In most routing problems with time windows, there is a subtle but operationally critical distinction:

1. **Strict time windows (the behavior the seven other nodes set)**  
   A resource must **arrive and complete** the service within the node's OpeningHours.

2. **Access windows (this feature)**  
   A resource must **arrive within** the OpeningHours, but the service may **continue after** the window ends.

The `IncludeVisitDurationExample` demonstrates how to switch a node from strict time-window semantics to "access window" semantics.

- **`setIsDutyHoursIncludesVisitDuration(true)`, set on the seven other nodes:** the opening hours of a node include its visit duration. This time-window behavior requires both: the visit must start and end inside them, **arrival and completion** within OpeningHours; a visit that ends later is late.
- **`setIsDutyHoursIncludesVisitDuration(false)`:** changes a node to an **arrival-only** constraint: the opening hours act as an access window. The visit must start inside them and may end after them. This is essential for real cases where "access must start in the window" but work may finish afterwards.
- **Per node:** only Koeln has the setting `false`, while the other seven nodes keep strict time windows with `true`: the example demonstrates the setting on Koeln, leaving the other nodes in strict mode and making the behavioral difference easy to observe.
- **The visit still takes its time:** the departure from Koeln, the arrival at the next node and Jack's working time all include the 20 minutes.

## Scenario

The dates, times and coordinates are sample values.

- **Resource:** `Jack` starts at 50.775346, 6.083887 (Aachen) and works on May 6 and May 7, 2020, from 08:00 to 17:00 (Europe/Berlin); his maximal working time is 13 hours, more than the 9 hours of the working hours, and his maximal distance 1200 km. He is created as one `CapacityResource` with these `WorkingHours` for resources, the max working time and the max distance, and the class does not call `setCost` for a cost configuration: starting in Aachen, this single resource keeps the focus on the time-window semantics rather than resource differences.
- **Koeln:** its own OpeningHours are open only on May 6, 2020, from 09:00 to 09:15, which is shorter than its visit of 20 minutes, and the node is set to `setIsDutyHoursIncludesVisitDuration(false)`. This flips the semantic switch for Koeln: it is "arrive in window, finish after allowed".
- **Seven other nodes:** Essen, Dueren, Nuernberg, Heilbronn, Stuttgart, Wuppertal and Aachen, all `TimeWindowGeoNode` objects, with the OpeningHours for nodes open on May 6 and 7, 2020, from 08:00 to 17:00 (Europe/Berlin). They are set to `setIsDutyHoursIncludesVisitDuration(true)` and remain strict.
- **All eight nodes:** the example creates several `TimeWindowGeoNode` instances (cities), one per city, with a visit of 20 minutes and the importance 1; the visit duration is set per node. Every node is set to `setWaitOnEarlyArrival(true)`: a resource that arrives before a node opens waits until it opens.

The example uses a small iteration budget: `addProperties` sets two properties, `JOptExitCondition.JOptGenerationCount` to 1000 and `JOpt.Algorithm.PreOptimization.SA.NumIterations` to 100000. Both are below their defaults, which keeps the run short; the defaults are in the [property reference](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties). The class does not set `JOpt.Algorithm.PreOptimization.SA.NumRepetitions`.

## How the code works

The source is [IncludeVisitDurationExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/includevisitduration/IncludeVisitDurationExample.java). `example()` runs the steps in order:

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
- `addNodes` adds Koeln with its access window and the seven other nodes.
- `addResources` adds Jack.
- `attachToObservables` subscribes to the progress, warning, status and error events with the event API, before the run starts.
- `startAndPresentResult` starts the run, waits up to 5 minutes for the result, prints it and returns it.
- `exportToKml` writes the result to `IncludeVisitDurationExample.kml` in the working directory and prints `KML export failed: IncludeVisitDurationExample.kml` if the export returns `false`.

### The access window of Koeln

Koeln gets its own opening hours, shorter than its visit:

```java
// Koeln is open for 15 minutes only, less than its visit: every visit there ends after 09:15.
List<IOpeningHours> koelnAccessHours = new ArrayList<>();
koelnAccessHours.add(
    new OpeningHours(
        ZonedDateTime.of(2020, MAY.getValue(), 6, 9, 0, 0, 0, ZoneId.of("Europe/Berlin")),
        ZonedDateTime.of(2020, MAY.getValue(), 6, 9, 15, 0, 0, ZoneId.of("Europe/Berlin"))));
```

The setting on the node makes them an access window:

```java
// With true, as on the seven other nodes, the visit must start and end inside the opening
// hours. With false, Jack only has to arrive inside them, and the visit may go on after they
// end. setWaitOnEarlyArrival(true), on every node: a resource that arrives before a node
// opens waits until it opens.
INode koeln =
    new TimeWindowGeoNode("Koeln", 50.9333, 6.95, koelnAccessHours, visitDuration, importance);
koeln.setIsDutyHoursIncludesVisitDuration(false);
koeln.setWaitOnEarlyArrival(true);
opti.addElement(koeln);
```

## Output and what to look for

The run prints the status and progress lines, then the result. In two test runs the result was the same. Route 0, on May 6, visits Dueren, Koeln, Wuppertal, Essen and Aachen. The line of Koeln:

```text
0.1 Koeln / Geo  / Arrival: 06.05.2020 09:08:36 (Europe/Berlin) , Departure: 06.05.2020 09:28:36 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 27.25 [min], 35.97 [km]
```

Print and review the result to confirm it: Jack arrives at 09:08:36, inside the opening hours, so Koeln arrival occurs within the window. He leaves at 09:28:36, after their end at 09:15: Koeln completion occurs after the window end, because its visit of 20 minutes is longer than its window of 15 minutes. The line shows no violation, and the violation summary of route 0 is empty.

In a test run with `setIsDutyHoursIncludesVisitDuration(true)` on Koeln, as the seven other nodes set it, the line of Koeln showed a LATE violation of 5 minutes. With 15 minutes of opening hours and a visit of 20 minutes, a visit that must end inside the opening hours is at least 5 minutes late.

Route 1, on May 7, visits Nuernberg, Stuttgart and Heilbronn. Its route header, trimmed to the three time lines:

```text
Defined WorkingHours   : 07.05.2020 08:00:00 - 07.05.2020 17:00:00 (Europe/Berlin)
Effective Route Start  : 07.05.2020 08:00:00 (Europe/Berlin)
Effective Route Stop   : 07.05.2020 19:58:18 (Europe/Berlin)
```

The route ends at 19:58:18, after 17:00, and its violation summary is empty. Overtime counts against the maximal working time, and the working hours are a frame for matching resources with the opening hours of the nodes, so a route can end after its working hours without a violation.

## Why this matters in real operations

### Typical use cases for access windows
- **Site access restrictions**  
  You may only enter a facility during a guard shift, but once inside, you can continue working.
- **Key handover / check-in windows**  
  A concierge is available only during a certain window.
- **Start-of-service SLA**  
  "Technician must be on-site by 16:00" but the repair can continue after 16:00.
- **Noise or access regulations**  
  Certain actions require arrival before a cutoff, but completion can happen later.

### Typical use cases for strict windows (`true`)
- **Customer appointment windows**  
  The customer is only present during a time window; the job must be finished within it.
- **Dock slot scheduling**  
  The entire loading/unloading process must occur within the allocated slot.
- **Pickup windows for perishable goods**  
  Both arrival and completion must be within the window.

## How to reason about feasibility with this setting

### Strict window feasibility (`setIsDutyHoursIncludesVisitDuration(true)`)
A visit has no time window violation only if:

- `arrivalTime >= windowStart`
- and `arrivalTime + visitDuration <= windowEnd`

Here `arrivalTime` is the start of the visit: every node sets `setWaitOnEarlyArrival(true)`, so a resource that arrives before `windowStart` waits until then. A visit that ends after `windowEnd` is not infeasible: the result shows it as a LATE violation.

### Access window feasibility (`setIsDutyHoursIncludesVisitDuration(false)`)
A visit has no time window violation if:

- `arrivalTime >= windowStart`
- and `arrivalTime <= windowEnd`

Service completion time may exceed the window end.  
However, it still contributes to:
- resource working hours,
- downstream arrival times,
- and route feasibility.

Meaning:
- You are not "removing" the visit duration from time; you are only changing how it is validated against the node's OpeningHours.

## Practical implications and best practices

### 1) You still need sufficient working time: WorkingHours are a frame
Allowing a service to end after OpeningHours does not take it out of the working time: the visit still counts against the maximal working time of the resource. A route can end after its WorkingHours without a violation, as route 1 in "Output and what to look for" does.  
A route that takes longer than the maximal working time gets a MAXTIME violation and a penalty cost for its overtime. If WorkingHours are strict in your operation, the schedule must still finish inside the working time limits; the optimizer treats these limits as a penalty, not as a hard rule.

### 2) Report semantics clearly to users
In UIs and reports, clarify whether:
- the "appointment window" is an arrival window, or
- a full-service window.

This prevents misunderstandings such as:
- "Why did the technician finish after the customer's window?"

### 3) Do not apply globally by accident
This setting is per-node and should be used selectively:
- apply it only to nodes that truly represent "access windows".

### 4) Be careful with multi-window nodes
If nodes have multiple OpeningHours windows, ensure your business interpretation is consistent:
- "arrive in any allowed window" vs "arrive in the last window".

### 5) Combine with service-time modeling explicitly
If your service process has phases (e.g., check-in must happen inside the window, work can continue outside), you may:
- model check-in as a short access-window node,
- and model the main work as a subsequent event node or linked structure,
depending on your broader modeling approach.

## Run it

Run the `main(String[] args)` method of `IncludeVisitDurationExample` from your IDE, with the repository imported as a Maven project. You need a JDK in the version that the examples pom sets. You need no license file: without one, the example uses the public evaluation license (see the [license guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/license)). The run writes `IncludeVisitDurationExample.kml` into the working directory; inspect the produced KML (the example uses `EntityKMLExporter`) to visually verify ordering.

## Related

- [IncludeVisitDurationExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/includevisitduration/IncludeVisitDurationExample.java): the source of this example.
- [Basic elements tutorial](https://www.dna-evolutions.com/docs/getting-started/tutorials/basic-elements): the opening hours of nodes and the working hours of resources.
- [Recommended ways to run an optimization](../../basic/recommendedimplementation_02/RecommendedImplementation.md): the recommended run pattern, a synchronous run and the legacy callback methods.
- [Advanced examples](../README.md): the overview of all Advanced examples.

---

Added: 2026-02-17 (JOpt core 7.5.3-j17) | Last updated: 2026-10-21
