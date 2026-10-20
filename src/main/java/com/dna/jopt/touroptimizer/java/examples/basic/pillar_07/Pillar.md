# Pillar nodes with hard time windows

`PillarExample` adds three pillar nodes to a plan with two resources: two pillars are bound to one resource each, and the third may go to either. A pillar is a node whose time window must be met; a resource may not arrive late at it. The page is for developers who model fixed appointments. The concept in full is in the [pillar nodes feature guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/pillar_nodes).

## What the example shows

The example builds three pillars: a geo pillar bound to a resource, an event pillar bound to a resource, and a geo pillar without a resource. The result shows each of them inside its time window.

### Pillars as hard constraints

A **PillarNode** (also called a **Pillar**) models an **absolute, non-negotiable** constraint: its SLA (OpeningHours / time window) must be met.

A Pillar is a **hard constraint** that is fulfilled by the optimizer's **architecture**.

- **Soft constraints** are typically enforced by *cost* (penalty terms in the objective function).
- **Hard constraints** in JOpt are enforced by *design*: the algorithm is structured so that these constraints are not violated.
  A Pillar is an example: normal nodes "flow around" the pillar appointment in the timeline.

### A pillar as a fixed anchor

A pillar appointment acts like a **fixed anchor** in the route timeline:

- The optimizer treats the pillar time window as something that must be satisfied.
- The surrounding schedule is then adapted:
  - normal nodes may be reordered,
  - normal nodes may be shifted earlier/later,
  - normal nodes may be assigned to other resources,
  - or a pillar that cannot be reached stays unassigned; it is never served late.

This "nodes flow around the pillar" effect is a direct consequence of JOpt's architecture for hard constraints.

### What is hard about a pillar

A pillar's **OpeningHours** are treated as hard feasibility boundaries:
- arriving outside the pillar time window is not acceptable,
- waiting before the window is allowed (as with normal time windows),
- but the visit must be placed such that the SLA is matched.

This makes pillars suitable for:
- contractual appointment windows,
- compliance deadlines,
- "must happen at this time" commitments.

### Conflict handling

The Special Features documentation describes how the optimizer resolves conflicts between a pillar and "normal" nodes. The hierarchy is:

1. **Reorder / shift normal nodes** so the pillar SLA is met.
2. **Let normal nodes violate** their constraints if your model allows it (soft constraints/cost-driven).
3. **Move work to other resources** if available, to protect the pillar.
4. A pillar that cannot be reached stays unassigned; it is never served late.

The system prefers cancellation/removal to violating a captured SLA.

### Pillar node types

The pillar concept appears as dedicated node classes implementing `IPillarNode`, such as:

- `PillarTimeWindowGeoNode`: a geo-located pillar node (lat/long + strict opening hours)
- `PillarEventNode`: a non-geo pillar node (event-style pillar, strict opening hours)

A pillar can optionally be tied to a **mandatory resource**:

- `pillar.attachResource(resource);`

When attached, the node is time-critical and constrained to one specific resource.

This is valuable for:
- skills/certifications,
- legal requirements,
- named-person commitments.

### When to use a pillar

**Use pillars when violations are unacceptable.** Use pillars for contractual requirements:
- "must be serviced within this window"
- "must not be late"
- "must not be moved outside the SLA"

**Do not use pillars to "nudge" the solver.** If the time window is *preferred* but not mandatory, model it as a normal node and handle it via:
- soft constraint penalties,
- objective weighting,
- business rules.

That separation means:
- **architecture** for hard constraints,
- **cost function** for preferences and trade-offs.

### Summary

- **PillarNode**: strict SLA matching for OpeningHours (hard constraint).
- Pillars are enforced by **optimizer architecture**.
- Normal nodes "flow around" pillars; the solver rearranges the schedule to keep pillar SLAs intact.
- Pillars can optionally enforce a mandatory resource via `attachResource(...)`.
- A pillar that cannot be reached stays unassigned; it is never served late.

## Scenario

The dates, times and coordinates are sample values. The time zone is Europe/Berlin.

- **Resources:** `John` and `Jack` both start at 50.775346, 6.083887 (Aachen) and work on June 4, 5 and 6, 2019, from 08:00 to 17:00. Each may work up to 13 hours a day and drive up to 1200 km. The 13 hours are more than the 9 hours of the working hours, so a day may run past 17:00 without overtime; the 1200 km per day are more than the longest day of this plan needs.
- **Nodes:** five `TimeWindowGeoNode` objects (Koeln, Oberhausen, Nuernberg, Heilbronn and Aachen), open on the same three days from 08:00 to 17:00, each with a visit of 60 minutes and the same importance.
- **Pillars:**
  - `Cologne Plumbing`, a `PillarTimeWindowGeoNode` in Cologne, open on June 5 from 10:00 to 10:50, attached to John. The visit fills the whole window.
  - `Important call`, a `PillarEventNode`: Jack has to call a customer for 30 minutes between 10:30 and 12:00 on June 6. It is attached to Jack.
  - `Maintenance job`, a `PillarTimeWindowGeoNode` in Stuttgart: 90 minutes between 13:30 and 16:00 on June 4. No resource is attached, so the optimizer may select a resource while still treating the SLA as hard.

The run sets two properties: `JOptExitCondition.JOptGenerationCount` to 1000 and `JOpt.Algorithm.PreOptimization.SA.NumIterations` to 100000. Both are below their defaults, which keeps the run short; the defaults are in the [property reference](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties).

## How the code works

The source is [PillarExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic/pillar_07/PillarExample.java). `example()` creates the resources before the pillars, because `addPillars` needs John and Jack to attach two of the pillars to them:

```java
ExampleLicenseHelper.setLicense(this);
addProperties(this);
// addPillars attaches pillars to John and Jack, so the resources are created first.
IResource john = addResource(this, "John");
IResource jack = addResource(this, "Jack");
addNodes(this);
addPillars(this, john, jack);
attachToObservables(this);
IOptimizationResult result = startAndPresentResult(this);
exportToKml(result);
```

`addResource(opti, name)` creates one resource with the shared working hours and limits, adds it, and returns it. `attachResource` takes that resource object.

### A geo pillar for John

The constructor without a visit duration makes the visit as long as the time window, here 50 minutes:

```java
IPillarNode colognePlumbing =
    new PillarTimeWindowGeoNode(
        "Cologne Plumbing", 50.9333, 6.95, plumbingOpeningHours);
// Only John may visit this pillar.
colognePlumbing.attachResource(john);
opti.addElement(colognePlumbing);
```

### An event pillar for Jack

A `PillarEventNode` has no position. Its visit of 30 minutes must fit into the window from 10:30 to 12:00:

```java
IPillarNode callJack =
    new PillarEventNode(
        "Important call", callOpeningHours, visitDurationPillarCallJack);
// Only Jack may visit this pillar.
callJack.attachResource(jack);
opti.addElement(callJack);
```

### A geo pillar without a resource

The maintenance job has no `attachResource` call:

```java
IPillarNode maintenanceJob =
    new PillarTimeWindowGeoNode(
        "Maintenance job",
        48.7667,
        9.18333,
        maintenanceOpeningHours,
        visitDurationPillarMaintenance);
opti.addElement(maintenanceJob);
```

`attachToObservables`, `startAndPresentResult` and `exportToKml` work as in [EventNode.md](../eventnode_06/EventNode.md): the example subscribes to the events with the recommended event API, waits up to 5 minutes for the result, prints it and writes it to `PillarExample.kml` in the working directory.

## Output and what to look for

The result lists the pillars with the marker `Pillar`. The three pillar lines, trimmed from the result (each line belongs to a different route):

```text
0.2 Maintenance job / Pillar  / Geo  / Arrival: 04.06.2019 14:07:53 (Europe/Berlin) , Departure: 04.06.2019 15:37:53 (Europe/Berlin) / Duration: 90.0 [min] / Driving: 31.52 [min], 41.6 [km]
1.1 Cologne Plumbing / Pillar  / Geo  / Idle: 12.01 [min] / Arrival: 05.06.2019 09:47:59 (Europe/Berlin) , Departure: 05.06.2019 10:50:00 (Europe/Berlin) / Duration: 50.0 [min] / Driving: 0.0 [min], 0.0 [km]
5.1 Important call / Pillar  / NonGeo  / Idle: 18.93 [min] / Arrival: 06.06.2019 10:11:03 (Europe/Berlin) , Departure: 06.06.2019 11:00:00 (Europe/Berlin) / Duration: 30.0 [min] / Driving: 0.0 [min], 0.0 [km]
```

- `Maintenance job` is on John's route of June 4. Its visit from 14:07:53 to 15:37:53 lies inside 13:30 to 16:00.
- `Cologne Plumbing` is on John's route of June 5. John arrives early and waits (`Idle: 12.01 [min]`), so the visit runs from 10:00 to 10:50.
- `Important call` is on Jack's route of June 6. Jack waits 18.93 minutes, so the call runs from 10:30 to 11:00.

No route of the result shows a violation.

## Run it

Run the `main(String[] args)` method of `PillarExample` from your IDE.

Behavior highlights:
- The run is started asynchronously (`startRunAsync()`).
- The example blocks with `get(5, TimeUnit.MINUTES)` to prevent premature termination.
- The result is printed to stdout.
- A KML file is written for visual inspection.

You need a JDK in the version that the examples pom sets. You need no license file: without one, the example uses the public evaluation license (see the [license guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/license)). The run writes `PillarExample.kml` into the working directory.

## Related

- [PillarExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic/pillar_07/PillarExample.java): the source of this example.
- [Pillar nodes feature guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/pillar_nodes): why pillars are hard constraints, the conflict cases and when to use them.
- [Special features: conflict of a pillar node with a node](https://www.dna-evolutions.com/docs/learn-and-explore/special/special_features#case-five-conflict-of-a-capturednode-with-a-node-solved-by-removing-a-capturednode): the case in which a pillar stays unassigned.
- [Nodes](https://www.dna-evolutions.com/docs/getting-started/tutorials/nodes): the constructors of `PillarTimeWindowGeoNode` and `PillarEventNode`.
- [EventNode.md](../eventnode_06/EventNode.md): event nodes without a hard time window, and the event API and KML export that this example uses too.
- [LoadOptimizationFromJsonAndReassignPillar.md](../io_03/LoadOptimizationFromJsonAndReassignPillar.md): a pillar added to a loaded solution.
- [Recommended ways to run an optimization](../recommendedimplementation_02/RecommendedImplementation.md): the recommended run pattern, a synchronous run and the legacy callback methods.
- [Base examples](../README.md): the overview of all Base examples.

---

Added: 2026-02-17 (JOpt core 7.5.3-j17) | Last updated: 2026-10-20
