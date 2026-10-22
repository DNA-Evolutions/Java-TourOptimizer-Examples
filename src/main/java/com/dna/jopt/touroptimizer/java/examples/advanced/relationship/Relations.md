# Relations between nodes

Seven examples tie two nodes together with a relation. A visitor relation decides whether the two nodes share a route or a resource, and a time relation decides when the visit to the second node starts, relative to the visit to the first. A broken relation is a pseudo constraint: it adds a cost weighted by `JOptWeight.Relationships`, and the result lists it as a violation. The page is for developers who plan visits that belong together; if you have not built an optimization yet, start with the [First optimization example](../../basic/firstoptimization_01/FirstOptimizationExample.md).

## What the example shows

Relations are one of the most powerful modeling tools in **JOpt.TourOptimizer**.  
They let you express dependencies *between nodes* that cannot be captured by distance-based cost alone, such as:

* "These two stops must be served by the same technician."
* "These two stops must not be on the same route."
* "The work at the second stop must start within 20 minutes after the work at the first stop starts."
* "These two stops must be in the same route and occur in a defined time gap."

- **Master and related node:** every relation of the examples follows the same steps. The code creates the relation, sets the master node with `setMasterNode` and the related node with `setRelatedNode`, chooses the behaviour and adds the relation to both nodes with `addNode2NodeRelation`.
- **Visitor relations** (`RelativeVisitor2RelatedNodeRelation`, interface `INode2NodeVisitorRelation`). Its setters choose the behaviour:
  - `setIsForcedSameRoute()`: both nodes on the same route, that is, on the same working day of the same resource.
  - `setIsForcedSameVisitor()`: both nodes with the same resource; the routes, and so the days, may differ.
  - `setIsForcedDifferentRoute(boolean isForcedDifferentVisitor)`: the nodes on different routes. With `true` the routes also belong to different resources; with `false` the same resource is allowed, but not required.
  - `setIsForcedSameVisitorForcedDifferentRoute()`, which no example of this page uses, asks for the same resource on different routes.
- **Time relations** (`RelativeTimeWindow2RelatedNodeRelation`, interface `INode2NodeTempusRelation`). The constructor takes a window, `new RelativeTimeWindow2RelatedNodeRelation(minimalDeviation, maximalDeviation)`: the visit to the related node starts at least `minimalDeviation` and at most `maximalDeviation` after the start of the visit to the master node. The window is a range, not a fixed offset.
- **What a time relation compares:** the start of the work at both nodes, that is, the departure minus the visit duration. The start of the work is the arrival when the resource does not wait at the node; when it arrives early and waits, the work starts later than the arrival. `setTimeComparisonJuncture(boolean isMasterNodeWorkingStart, boolean isRelatedNodeWorkingStart)` switches a side to the end of the work, the departure, with `false`. The examples do not call it and compare the starts.
- **Induced idle time:** to keep a time window, the optimizer can let a resource wait before the related node, even when the node is already open. The result shows the wait as an `IDLE_EVENT_RELATION` element on the route and in the line `Ind. Idle time`. The property `JOpt.InducedIdleTime.max` limits the length of one such element.

### Relations are pseudo constraints

A broken visitor relation adds a fixed cost, multiplied by the weight `JOptWeight.Relationships`; a broken time relation adds a cost that grows with the minutes outside the window and with the same weight, up to a maximum. The optimizer weighs this cost against the other costs of the plan, so a relation can break when keeping it costs more. The result then lists a violation of the type `RELATIONSHIP`; for a time relation, the violation gives the minutes outside the window as the penalty time. A higher `JOptWeight.Relationships` makes broken relations more expensive. In two test runs of each example on this page, every relation held with the default weight of the [property reference](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties).

### Choosing a relation

- **Same route or same resource:** use `setIsForcedSameRoute()` when both visits belong on one route, that is, on one working day of one resource. Use `setIsForcedSameVisitor()` when the same resource has to visit both nodes, on any of its working days. A resource with one working day has one route, so there the two settings lead to the same plan.
- **Visitor and time relations together:** a visitor relation does not set the order or the time of the visits on a route, and a time relation does not keep two nodes on one route. `SameRouteRelationAndRelativeTimeWindowExample` sets both on the same two nodes.
- **Relations change the routes:** a relation can make the plan cost more than a plan without it. In the variant runs without the relations (see "Output and what to look for"), each of the four visitor examples ended with a lower cost and with the two nodes placed against the relation.
- **Windows and waiting:** a time window that the routes cannot meet without waiting adds idle time, as in `RelativeTimeWindowRelationWithInducedIdleTimeExample`.

### Where each relation applies

Typical cases, as examples:

- **Same route:** two jobs that one resource does in one tour, for example a pickup and a follow-up service.
- **Same resource:** visits on several days for which a customer keeps the same technician. For the same technician on different days, use `setIsForcedSameVisitorForcedDifferentRoute()`.
- **Different routes, same resource allowed:** two visits that must not fall on the same route, for example two appointments of one customer on different days.
- **Different routes and different resources:** a check that another crew does, for example an inspection after an installation by a different team.
- **Time window:** a visit that follows another within a set time, for example an inspection within a window after an installation. A lower bound above 0 sets a minimal gap between the starts of the two visits, for example for drying or curing.
- **Same route and a time window:** steps of one process in one tour, in a set order, for example chained visits of a multi-step field service.

### Checking a relation

1. **Read the violations:** a broken relation shows in the result as a violation of the type `RELATIONSHIP`; for a time window, the violation gives the penalty time in minutes.
2. **Read the positions:** find the two nodes on the routes of the result, with their resource and day. For a time window, compare the starts of the work, the departures minus the visit durations.
3. **Compare with a variant:** run the same plan without the relation, with the two `addNode2NodeRelation` calls removed, and compare the routes and the cost, as the variant runs on this page do.
4. **Start small:** each example of this page checks one or two relations on two to eight nodes. Add nodes and constraints when the relation holds on the small plan.

## Scenario

The dates, times and coordinates are sample values. The time zone is Europe/Berlin. All resources start at 50.775346, 6.083887 (Aachen) and may drive up to 1200 km a day, more than the longest day of these plans needs; the node `Aachen` lies at the same position. All nodes are `TimeWindowGeoNode` objects with the same importance. Each class sets `JOptExitCondition.JOptGenerationCount` to 1000 and `JOpt.Algorithm.PreOptimization.SA.NumIterations` to 100000, both below their defaults, which keeps the run short.

### The visitor examples

The four visitor examples, and `SameRouteRelationAndRelativeTimeWindowExample`, use the nodes Koeln, Essen, Dueren, Wuppertal and Aachen. Each visit takes 150 minutes, so a resource cannot visit all five nodes in one day.

- `SameRouteRelationExample` and `SameRouteRelationAndRelativeTimeWindowExample`: `Jack` works on May 6, May 7 and May 8, 2020, from 08:00 to 16:00, and may work 8 hours a day, the length of the working hours. The nodes are open on the three days from 08:00 to 18:00. Essen is the master node and Aachen the related node of a same-route relation; the second class adds a time window of 0 to 1000 minutes between them.
- `SameVisitorRelationExample`: `Jack` and `John` work on May 6, 2020, from 08:00 to 19:00, and may work 11 hours, the length of the working hours. The nodes are open on May 6 from 08:00 to 18:00. Essen and Aachen have a same-visitor relation.
- `DifferentRouteSameVisitorRelationExample`: `Jack` works on May 6 and May 7, 2020, from 08:00 to 19:00, and may work 11 hours a day, the length of the working hours. The nodes are open on both days from 08:00 to 18:00. Dueren is the master node and Aachen the related node of a different-route relation that allows the same resource.
- `DifferentRouteDifferentVisitorRelationExample`: `Jack` and `John` work on May 6 and May 7, 2020, from 08:00 to 18:00, and may work 10 hours a day, the length of the working hours. The nodes are open on both days from 08:00 to 18:00. Dueren and Aachen have a different-route relation that asks for different resources.

### The time window examples

- `RelativeTimeWindowRelationExample`: `Jack` and `John` work on May 6 and May 7, 2020, from 08:00 to 17:00. They may work 13 hours a day, longer than the working hours, so a long day with Nuernberg and Stuttgart fits without overtime. Eight nodes, Koeln, Oberhausen, Essen, Dueren, Nuernberg, Stuttgart, Wuppertal and Aachen, are open on both days from 08:00 to 17:00; each visit takes 20 minutes. The visit to Aachen has to start 0 to 20 minutes after the start of the visit to Essen. The drive from Essen to Aachen alone takes longer than 20 minutes, so one resource cannot do both visits within the window.
- `RelativeTimeWindowRelationWithInducedIdleTimeExample`: `Jack` and `John` work on May 6 and May 7, 2020, from 08:00 to 17:00, and may work 9 hours a day, the length of the working hours. A `MandatoryResourceConstraint` gives Essen to John and Aachen to Jack. Essen is open from 09:30 to 16:00 and Aachen from 08:00 to 16:00, on both days. Each visit takes 60 minutes, and the visit to Aachen has to start 0 to 40 minutes after the start of the visit to Essen. The window is a sample value; its lower bound 0 makes Jack wait until the visit to Essen starts.

## How the code works

The sources are in the folder [relationship](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/relationship):

- [SameRouteRelationExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/relationship/SameRouteRelationExample.java)
- [SameRouteRelationAndRelativeTimeWindowExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/relationship/SameRouteRelationAndRelativeTimeWindowExample.java)
- [SameVisitorRelationExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/relationship/SameVisitorRelationExample.java)
- [DifferentRouteSameVisitorRelationExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/relationship/DifferentRouteSameVisitorRelationExample.java)
- [DifferentRouteDifferentVisitorRelationExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/relationship/DifferentRouteDifferentVisitorRelationExample.java)
- [RelativeTimeWindowRelationExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/relationship/RelativeTimeWindowRelationExample.java)
- [RelativeTimeWindowRelationWithInducedIdleTimeExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/relationship/RelativeTimeWindowRelationWithInducedIdleTimeExample.java)

All seven call the same steps in `example()`:

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

Each class creates its relations at the end of `addNodes`. `attachToObservables` subscribes to the progress, warning, status and error events with the event API, before the run starts. `startAndPresentResult` starts the run, waits up to 5 minutes for the result and prints it.

### Same route

`SameRouteRelationExample`:

```java
// Essen is the master node and Aachen the related node. setIsForcedSameRoute asks for the
// same route, which also means the same resource.
INode2NodeVisitorRelation sameRoute = new RelativeVisitor2RelatedNodeRelation();
sameRoute.setMasterNode(essen);
sameRoute.setRelatedNode(aachen);
sameRoute.setIsForcedSameRoute();

essen.addNode2NodeRelation(sameRoute);
aachen.addNode2NodeRelation(sameRoute);
```

### Same route and a time window

`SameRouteRelationAndRelativeTimeWindowExample` creates the same-route relation of `SameRouteRelationExample` and adds a time relation:

```java
// The visit to Aachen starts 0 to 1000 minutes after the start of the visit to Essen. 1000
// minutes are longer than a working day, so on one route the window only sets the order:
// Essen first.
INode2NodeTempusRelation essenBeforeAachen =
    new RelativeTimeWindow2RelatedNodeRelation(Duration.ofMinutes(0), Duration.ofMinutes(1000));
essenBeforeAachen.setMasterNode(essen);
essenBeforeAachen.setRelatedNode(aachen);

essen.addNode2NodeRelation(essenBeforeAachen);
aachen.addNode2NodeRelation(essenBeforeAachen);
```

### Same resource

`SameVisitorRelationExample`:

```java
// Essen is the master node and Aachen the related node. setIsForcedSameVisitor asks for the
// same resource; the route of that resource may differ.
INode2NodeVisitorRelation sameVisitor = new RelativeVisitor2RelatedNodeRelation();
sameVisitor.setMasterNode(essen);
sameVisitor.setRelatedNode(aachen);
sameVisitor.setIsForcedSameVisitor();

essen.addNode2NodeRelation(sameVisitor);
aachen.addNode2NodeRelation(sameVisitor);
```

### Different routes

`DifferentRouteSameVisitorRelationExample` allows the same resource on both routes:

```java
// Dueren is the master node and Aachen the related node. setIsForcedDifferentRoute asks for
// different routes; false allows the same resource on both routes. With Jack alone, the two
// routes are his two working days.
INode2NodeVisitorRelation differentRoute = new RelativeVisitor2RelatedNodeRelation();
differentRoute.setMasterNode(dueren);
differentRoute.setRelatedNode(aachen);
boolean isForcedDifferentVisitor = false;
differentRoute.setIsForcedDifferentRoute(isForcedDifferentVisitor);

dueren.addNode2NodeRelation(differentRoute);
aachen.addNode2NodeRelation(differentRoute);
```

`DifferentRouteDifferentVisitorRelationExample` asks for different resources as well:

```java
// Dueren is the master node and Aachen the related node. setIsForcedDifferentRoute asks for
// different routes; true also asks for different resources.
INode2NodeVisitorRelation differentRoute = new RelativeVisitor2RelatedNodeRelation();
differentRoute.setMasterNode(dueren);
differentRoute.setRelatedNode(aachen);
boolean isForcedDifferentVisitor = true;
differentRoute.setIsForcedDifferentRoute(isForcedDifferentVisitor);

dueren.addNode2NodeRelation(differentRoute);
aachen.addNode2NodeRelation(differentRoute);
```

### Relative time window

`RelativeTimeWindowRelationExample`:

```java
// Essen is the master node and Aachen the related node: the visit to Aachen starts 0 to 20
// minutes after the start of the visit to Essen. The drive from Essen to Aachen alone takes
// longer than 20 minutes, so the two visits need two resources.
INode2NodeTempusRelation timeWindow =
    new RelativeTimeWindow2RelatedNodeRelation(Duration.ofMinutes(0), Duration.ofMinutes(20));
timeWindow.setMasterNode(essen);
timeWindow.setRelatedNode(aachen);

essen.addNode2NodeRelation(timeWindow);
aachen.addNode2NodeRelation(timeWindow);
```

### Induced idle time

`RelativeTimeWindowRelationWithInducedIdleTimeExample` gives each node one resource and opens Essen later than Aachen:

```java
// Essen opens at 09:30, so its visit starts at 09:30 at the earliest, while Jack could start
// the visit to Aachen at 08:00. To keep the time window, Jack has to wait. The property
// JOpt.InducedIdleTime.max limits the length of one induced idle event.
TimeWindowGeoNode essen =
    new TimeWindowGeoNode(
        "Essen", 51.45, 7.01667, createOpeningHours(9, 30), visitDuration, importance);
// Only John may visit Essen.
IConstraintResource johnMandatory = new MandatoryResourceConstraint();
// addResource needs a priority above 0; with John alone its value does not matter.
johnMandatory.addResource("John", 10);
essen.addConstraint(johnMandatory);
opti.addElement(essen);
```

The time window between Essen and Aachen is 0 to 40 minutes:

```java
// Essen is the master node and Aachen the related node: the visit to Aachen starts 0 to 40
// minutes after the start of the visit to Essen, a sample window. Its lower bound 0 makes
// Jack wait until the visit to Essen starts.
INode2NodeTempusRelation timeWindow =
    new RelativeTimeWindow2RelatedNodeRelation(Duration.ofMinutes(0), Duration.ofMinutes(40));
```

## Output and what to look for

Every relation held in two test runs of each class, and no result showed a violation. For the four visitor examples, the sections below also name the result of variant runs: runs of the same class with the two `addNode2NodeRelation` calls removed, two runs per class.

### SameRouteRelationExample

The result was the same in two test runs. Aachen and Essen are on Jack's route of May 6 (trimmed to that route):

```text
RouteId                : 0
Resource               : Jack
0.0 Aachen / Geo  / Arrival: 06.05.2020 08:00:00 (Europe/Berlin) , Departure: 06.05.2020 10:30:00 (Europe/Berlin) / Duration: 150.0 [min] / Driving: 0.0 [min], 0.0 [km]
0.1 Essen / Geo  / Arrival: 06.05.2020 11:45:26 (Europe/Berlin) , Departure: 06.05.2020 14:15:26 (Europe/Berlin) / Duration: 150.0 [min] / Driving: 75.44 [min], 99.58 [km]
```

Koeln and Wuppertal are on the route of May 7, Dueren on the route of May 8. Without the relation, Essen was on the route of May 6 together with Wuppertal, and Aachen alone on the route of May 8, in both variant runs.

### SameRouteRelationAndRelativeTimeWindowExample

The result was the same in two test runs. The routes are those of `SameRouteRelationExample`, but on May 6 Essen comes first. The visit to Aachen starts at 13:00:24, 224.97 minutes after the start of the visit to Essen at 09:15:26, inside the window of 0 to 1000 minutes:

```text
0.0 Essen / Geo  / Arrival: 06.05.2020 09:15:26 (Europe/Berlin) , Departure: 06.05.2020 11:45:26 (Europe/Berlin) / Duration: 150.0 [min] / Driving: 75.44 [min], 99.58 [km]
0.1 Aachen / Geo  / Arrival: 06.05.2020 13:00:24 (Europe/Berlin) , Departure: 06.05.2020 15:30:24 (Europe/Berlin) / Duration: 150.0 [min] / Driving: 74.97 [min], 98.96 [km]
```

### SameVisitorRelationExample

In both runs, one resource visited Aachen, Essen and Wuppertal, in this order, and the other resource Dueren and Koeln. Which resource took which route can change between runs. Without the relation, Aachen was on John's route and Essen on Jack's route in both variant runs.

### DifferentRouteSameVisitorRelationExample

The result was the same in two test runs. Aachen is on Jack's route of May 6 and Dueren on his route of May 7 (trimmed to the nodes):

```text
0.0 Aachen / Geo  / Arrival: 06.05.2020 08:00:00 (Europe/Berlin) , Departure: 06.05.2020 10:30:00 (Europe/Berlin) / Duration: 150.0 [min] / Driving: 0.0 [min], 0.0 [km]
0.1 Essen / Geo  / Arrival: 06.05.2020 11:45:26 (Europe/Berlin) , Departure: 06.05.2020 14:15:26 (Europe/Berlin) / Duration: 150.0 [min] / Driving: 75.44 [min], 99.58 [km]
0.2 Wuppertal / Geo  / Arrival: 06.05.2020 14:33:10 (Europe/Berlin) , Departure: 06.05.2020 17:03:10 (Europe/Berlin) / Duration: 150.0 [min] / Driving: 17.74 [min], 23.41 [km]
1.0 Dueren / Geo  / Arrival: 07.05.2020 08:21:21 (Europe/Berlin) , Departure: 07.05.2020 10:51:21 (Europe/Berlin) / Duration: 150.0 [min] / Driving: 21.36 [min], 28.2 [km]
1.1 Koeln / Geo  / Arrival: 07.05.2020 11:18:36 (Europe/Berlin) , Departure: 07.05.2020 13:48:36 (Europe/Berlin) / Duration: 150.0 [min] / Driving: 27.25 [min], 35.97 [km]
```

Without the relation, Aachen and Dueren were on the same route of May 7 in both variant runs.

### DifferentRouteDifferentVisitorRelationExample

In both runs, one resource visited Dueren and Koeln on May 6, and the other resource visited Essen and Wuppertal on May 6 and Aachen on May 7. Which resource took which nodes can change between runs. Without the relation, John visited Aachen, Dueren and Koeln on the same route in both variant runs.

### RelativeTimeWindowRelationExample

In both runs, one resource visited Essen and the other Aachen, on the same day, and both visits started at 09:15:26, so the window of 0 to 20 minutes holds. The route with Aachen holds an `IDLE_EVENT_RELATION` element before Aachen: the optimizer let that resource wait, so that its visit falls into the window. Which resource takes which node, the day of the two visits, May 6 or May 7, and the other routes can change between runs.

In both runs, the route with Nuernberg and Stuttgart ends at 19:27:07, after the working hours, and takes 687.12 minutes, within the maximal working time of 13 hours. Time after the end of the working hours counts against the maximal working time, and the working hours are a frame for matching resources with the opening hours of the nodes, so a route can end after its working hours without a violation.

### RelativeTimeWindowRelationWithInducedIdleTimeExample

The result was the same in two test runs. John arrives in Essen at 09:15:26, waits until Essen opens and starts the visit at 09:30, one hour before he leaves at 10:30. Jack's route starts with an induced idle time of 90 minutes, and his visit to Aachen starts at 09:30 as well, inside the window of 0 to 40 minutes (trimmed to these lines):

```text
Ind. Idle time   [min] : 90.0
0.1 Aachen / Geo  / Arrival: 06.05.2020 09:30:00 (Europe/Berlin) , Departure: 06.05.2020 10:30:00 (Europe/Berlin) / Duration: 60.0 [min] / Driving: 0.0 [min], 0.0 [km]
2.0 Essen / Geo  / Idle: 14.56 [min] / Arrival: 06.05.2020 09:15:26 (Europe/Berlin) , Departure: 06.05.2020 10:30:00 (Europe/Berlin) / Duration: 60.0 [min] / Driving: 75.44 [min], 99.58 [km]
```

The line before Aachen on Jack's route is the induced idle time itself, an element named `IDLE_EVENT_RELATION_(Essen -> Aachen)` from 08:00:00 to 09:30:00; its name also carries a number that changes with every run.

## Conceptual model

### Master node and related node
Most relations are expressed as:

- **Master node**: the anchor/reference point
- **Related node**: the node whose route/resource/time is constrained relative to the master

Attaching the relation to both nodes is important because:
- it makes the relationship explicit on both elements,
- and ensures the solver's internal evaluation sees a consistent relation graph.

## Visitor relations (route/resource coupling)

### A) Force Same Route

Example: `SameRouteRelationExample`

Typical use cases:
- jobs that must be executed in one continuous tour,
- "keep these two stops together" bundling,
- service sequences that must remain in one route for operational continuity.

Practical note:
- This type of relation is often used to prevent natural geographic clustering from splitting critical pairs across routes.

### B) Force Same Visitor (Same Resource)

Example: `SameVisitorRelationExample`

Typical use cases:
- "The same technician must do both visits" (customer continuity),
- compliance requirements,
- skill continuity beyond formal skill constraints (e.g., "this customer wants their usual technician").

When to prefer this over skill constraints:
- Skills model *capability*; same-visitor models *continuity* and *identity*.

### C) Force Different Routes (same visitor allowed or forbidden)

Examples:
- `DifferentRouteSameVisitorRelationExample`
- `DifferentRouteDifferentVisitorRelationExample`

This captures two common business policies:

#### C1) Different routes, same visitor allowed
Use when:
- tasks must not be in the same route (e.g., require separation into different days),
- but continuity of the same resource is still acceptable (when it is desired, `setIsForcedSameVisitorForcedDifferentRoute()` asks for it).

Example scenario:
- a customer accepts the same technician, but not two visits by that technician in the same day (the relation allows the same technician, it does not require one).

#### C2) Different routes, different visitors required
Use when:
- tasks must be separated and also require independent execution by different resources.

Example scenario:
- separation-of-duties compliance,
- independent verification steps,
- safety rules ("two different crews must inspect independently").

## Tempus relations (relative time constraints)

### Choosing the timestamp: `setTimeComparisonJuncture(...)`

You should keep this explicit in your model because:
- "start-of-service-to-start-of-service" constraints behave differently than "end-of-service-to-start-of-service" constraints, especially when visit durations are non-trivial.

### A) Relative time window (tight sequencing)

Example: `RelativeTimeWindowRelationExample`

Typical use cases:
- follow-up appointments right after a first visit,
- chained jobs with little tolerance,
- handover processes ("visit B shortly after A").

### B) Relative time window that induces idle time (controlled waiting)

Example: `RelativeTimeWindowRelationWithInducedIdleTimeExample`

Typical use cases:
- "wait at least X minutes after event A before starting event B,"
- cooling/curing/waiting processes (industrial service),
- regulated delays (e.g., mandatory waiting periods),
- real-world synchronization where one party must wait for another.

Operational implication:
- You should expect idle time to rise (by design).
- This is not "inefficiency"; it is model fidelity.

## Combining visitor + tempus relations

Example: `SameRouteRelationAndRelativeTimeWindowExample`

This example demonstrates the most common real-world pattern:

1. Ensure two nodes are on the **same route** (so they are executed by the same resource in one tour)
2. Enforce a **relative time window** between them (so their ordering/timing is controlled)

Why this matters:
- Visitor relations alone do not guarantee the *relative timing* inside a route.
- Tempus relations alone do not guarantee the nodes end up in the *same route*.
- Combining them gives you a clean architectural model for "paired tasks in a controlled sequence".

Typical use cases:
- pickup and follow-up service in one tour with a time bound,
- chained medical visits,
- equipment install then inspection within a window,
- multi-step field service processes.

## Design guidance and best practices

### 1) Keep relative-time windows realistic
A relative-time window that is too tight may:
- make the window impossible to keep, so that the relation breaks and adds its cost,
- force excessive detours,
- increase idle time,
- or push the optimizer into pathological schedules.

Start with realistic tolerance intervals, then tighten if needed.

### 2) Understand that relations can override natural clustering
Relations can intentionally work *against* pure geographic clustering.  
This is the point: relations encode business reality, not geometry.

If you see longer travel, do not assume it is "worse":
- it may be the necessary outcome of a business rule.

### 3) Debugging: verify relations early
A recommended workflow:
1. Build a tiny instance (5-10 nodes) with one relation.
2. Verify that the relation is respected.
3. Add more nodes and constraints.
4. Only then scale up.

This avoids debugging "everything at once".

## Summary

- **Visitor relations** constrain *who* visits nodes and whether nodes share a route:
  - forced same route
  - forced same visitor
  - forced different routes (optionally with forced different visitors)

- **Tempus relations** constrain *when* one node is served relative to another:
  - relative time windows based on master/related timestamps

- Relations are attached using a consistent master/related pattern and should be added to **both** nodes.

- For real-world workflows, combining visitor + tempus relations is often the cleanest architectural way to encode multi-step processes in routes.

## Run it

Run the `main(String[] args)` method of one of the seven classes from your IDE, with the repository imported as a Maven project. You need a JDK in the version that the examples pom sets. You need no license file: without one, the examples use the public evaluation license (see the [license guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/license)).

## Related

- [Optimization properties](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties): the property reference, with `JOptWeight.Relationships` and its default.
- [Mandatory, preferred, banned and unpreferred resources for a node](../condition/BindingExcludingResourceCondition.md): the `MandatoryResourceConstraint` that `RelativeTimeWindowRelationWithInducedIdleTimeExample` uses.
- [Advanced examples](../README.md): the overview of all Advanced examples.
- [Recommended ways to run an optimization](../../basic/recommendedimplementation_02/RecommendedImplementation.md): the recommended run pattern, a synchronous run and the legacy callback methods.

---

Added: 2026-02-17 (JOpt core 7.5.3-j17) | Last updated: 2026-10-23
