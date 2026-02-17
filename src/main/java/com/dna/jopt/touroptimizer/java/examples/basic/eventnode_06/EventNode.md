# Event Nodes — Non-Geographical Tasks in JOpt TourOptimizer

This document explains how to model **work items without a physical geo-location** using an `EventNode`.  
A typical example is a **customer call** or **administrative task** that must happen within a time window but does not require travel.

It is based on:

- [EventNodeExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic/eventnode_06/EventNodeExample.java)

---

## What is an `EventNode`?

In JOpt, many nodes represent **geo locations** (e.g., `TimeWindowGeoNode` with latitude/longitude).  
An `EventNode` is different:

- it participates in routing/scheduling like any other node (visit duration, opening hours, importance),
- but it has **no geo coordinates**.

Conceptually, it represents “work that must be done” rather than “a place to visit”.

---

## Why use an `EventNode`?

Event nodes are useful when your planning problem includes tasks such as:

- phone calls,
- paperwork / compliance steps,
- remote support sessions,
- internal meetings,
- follow-ups that can be done anywhere (within working hours).

These tasks still consume time and must be placed in the route timeline, but they do not consume travel distance.

---

## How the example is built (walkthrough)

### 1) License and lifecycle

The example applies a license via the repository helper and starts the run asynchronously:

- `ExampleLicenseHelper.setLicense(this);`
- `CompletableFuture<IOptimizationResult> resultFuture = this.startRunAsync();`
- `resultFuture.get();` (keeps the JVM alive until completion)

---

### 2) Solver properties (high effort configuration)

`setProperties()` sets relatively high effort parameters:

- `JOptExitCondition.JOptGenerationCount = 20000`
- `JOpt.Algorithm.PreOptimization.SA.NumIterations = 1000000`
- `JOpt.Algorithm.PreOptimization.SA.NumRepetions = 1`
- `JOpt.NumCPUCores = 4`

Practical meaning:
- this example is configured to run “long enough” to produce stable behavior for demonstration,
- in typical tutorials you may start with smaller values and scale up once modeling is correct.

---

### 3) Resources

`addResources()` creates one `CapacityResource`:

- “Jack” based in Aachen (lat/long provided),
- max working time: 13 hours,
- max distance: 1200 km,
- working hours: May 6–7, 2020, 08:00–17:00 (Europe/Berlin).

Even though the example includes an `EventNode`, resources still have geo coordinates because they define:
- start/end location,
- travel constraints.

---

### 4) Nodes: mixing geo nodes and event nodes

`addNodes()` defines:
- opening hours on May 6–7, 2020 (08:00–17:00),
- visit duration: 20 minutes.

Then it adds a standard list of geo nodes (e.g., Koeln, Essen, Dueren, Nuernberg, Heilbronn, Wuppertal, Aachen) using `TimeWindowGeoNode`.

**Key part:** it inserts an `EventNode` into the same optimization:

- `new EventNode("Event", weeklyOpeningHours, visitDuration, 1)`

This shows that an event node is not a special “other system” object; it is a first-class node that can be scheduled into the route plan.

---

## What to expect in the result

When the optimizer places the `EventNode` into the plan:

- it consumes **service time** (visit duration) like any other node,
- it is bounded by **opening hours** like any other node,
- but it should not introduce travel distance to reach it (it is not a physical location).

This makes event nodes ideal for representing remote tasks that must happen between “real” visits.

---

## Export: Visualizing the route (KML)

The example exports the result container to a KML file in:

- `onAsynchronousOptimizationResult(IOptimizationResult rapoptResult)`

Export behavior:
- creates a `EntityKMLExporter`,
- sets the title to the class simple name,
- writes a file: `./EventNodeExample.kml`

This is valuable for quick inspection of route geometry and ordering. Note that event nodes may be visualized differently depending on how the exporter represents non-geo elements.

---

## Recommended modeling guidance (production use)

### Use event nodes when the task is location-independent
A good rule: if the task can be performed “from anywhere” and the only constraint is **time**, it belongs as an `EventNode`.

### Use geo nodes when travel is part of the cost/constraints
If there is any dependency on distance/time-to-reach, keep it as a geo node (or supply connection data).

### Be explicit about time windows
Event nodes are frequently used for “call the customer between 10–12” type constraints. Ensure the opening hours reflect the business requirement (not just broad working hours), otherwise the optimizer will schedule it wherever convenient.

### Watch idle time
Adding event nodes can reduce idle time (fill gaps between tight windows), but it can also increase complexity. Use result analysis to validate:
- whether the event nodes are scheduled where intended,
- whether they create unexpected waiting time due to time-window interaction.

---

## Practical extensions

If you want to make event nodes operationally powerful:

- Add multiple event nodes representing calls, each with its own time window.
- Give them different importance values to influence selection/ordering.
- Combine with result analysis to generate a “call schedule” alongside the physical route.
- Export both route and event tasks to a downstream execution system (driver app / dispatcher UI).

