# Connection time efficiency of a resource

`ResourceConnectionEfficiencyExample` gives the resource Jack the connection time efficiency factor 0.2, so every connection that Jack drives takes a fifth of its driving time while its distance stays the same. In the result, Jack drives the 311.85 km from Aachen to Stuttgart in 47.25 minutes and visits seven of the eight nodes in a route of 258.35 minutes. The page is for developers whose resources drive faster or slower than the driving times of the connections assume. If you have not built an optimization yet, start with the [First optimization example](../../basic/firstoptimization_01/FirstOptimizationExample.md).

## What the example shows

This is especially useful when:
* one vehicle is significantly faster (highway capable, emergency privileges, optimized routing device),
* one vehicle is slower (heavy truck, restricted speed zones),
* one resource is a bike/walker in dense cities,
* a subset of vehicles has access to restricted roads/lanes and therefore travels faster in practice.

- **The factor:** every time the optimizer evaluates a move, it needs **travel time** between two locations (nodes, or resource start/end to nodes). `setConnectionTimeEfficiencyFactor(double)` on a resource scales the driving time of each connection that the resource drives. JOpt multiplies the time by the factor, so a factor below 1 makes the resource faster and a factor above 1 makes it slower. A resource with the factor 1.0, as John in the example, drives with the times of the connections. The setter throws an `IllegalStateException` for a factor below 0.
- **Time, not distance:** the factor changes the driving time only. The distance of a connection, and with it the distance cost, stays the same.
- **Where the time comes from:** the factor scales the time of the connection, wherever that time comes from. In this example JOpt calculates the time from the straight-line distance and the average speed of the resource (`setAvgSpeed`), as the [Backup connector feature guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/backupconnector) describes. The factor also scales the times of connections that you add yourself with a node connector. Connections with time-dependent data, as in [Time-dependent driving times with a connection store](../../expert/connectionstore/ConnectionStore.md), are the exception: where the time-dependent data hold a time for the moment of the drive, JOpt uses that time without the factor.
- **The effect on the plan:** both resources have the same costs, and the factor makes Jack's driving shorter. With the factor, Jack takes almost all nodes; in two test runs without it, John took them (see "Output and what to look for").
- **Choosing a value:** the factor is the ratio of the two speeds. A vehicle that drives at 60 km/h on connections whose times assume 50 km/h gets 50/60, about 0.83; a vehicle that drives at 40 km/h gets 50/40, that is 1.25. This gives you consistent and explainable scaling. The speeds are sample values. The example uses 0.2, a value exaggerated so that the effect is easy to see.

The example also writes its result to a KML file.

## Scenario

The dates, times and coordinates are sample values. The time zone is Europe/Berlin. The example uses the resources, nodes and dates of `MandatoryResourceConditionExample` ([Mandatory, preferred, banned and unpreferred resources for a node](../condition/BindingExcludingResourceCondition.md)), without its constraint.

- **Resources:** `Jack` and `John`, two `CapacityResource` objects at 50.775346, 6.083887 (Aachen). Both have working hours on March 6 and March 7, 2020, from 08:00 to 17:00, and may drive up to 1200 km a day. Jack may work up to 8 hours a day, John up to 14 hours. Jack has the connection time efficiency factor 0.2, John the factor 1.0. Neither sets costs, so both have the same costs.
- **Nodes:** eight `TimeWindowGeoNode` objects: Koeln, Koeln1, Oberhausen, Essen, Heilbronn, Stuttgart, Wuppertal and Aachen. They are open on March 6 and March 7 from 08:00 to 17:00, each visit takes 20 minutes, and all nodes have the same importance. Koeln and Koeln1 have the same position, 50.9333, 6.95. The Aachen node lies at the start position of both resources.

The class sets no properties, so the run uses the defaults of the [property reference](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties).

## How the code works

The source is [ResourceConnectionEfficiencyExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/connectionefficiency/ResourceConnectionEfficiencyExample.java). `example()` runs the steps in order:

```java
// The public evaluation license, or your own license if you put a license file where
// ExampleLicenseHelper looks for it.
ExampleLicenseHelper.setLicense(this);
addNodes(this);
addResources(this);
attachToObservables(this);
IOptimizationResult result = startAndPresentResult(this);
exportToKml(result);
```

- `addNodes` adds the eight nodes.
- `addResources` adds Jack, with the factor 0.2, and John, with the factor 1.0.
- `attachToObservables` subscribes to the progress, warning, status and error events with the event API, before the run starts.
- `startAndPresentResult` starts the run, waits up to 5 minutes for the result, prints it and returns it. The wait ensures the JVM remains alive until the optimization completes.
- `exportToKml` writes the result to `ResourceConnectionEfficiencyExample.kml` in the working directory, as in [Export a result to a KML file](../../basic/io_03/Export2KMLExample.md).

### The factor

`addResources` sets the factor 0.2 on Jack and the factor 1.0 on John:

```java
IResource jack =
    new CapacityResource(
        "Jack", 50.775346, 6.083887, maxWorkingTimeJack, maxDistanceKmW, createWorkingHours());
// The factor scales Jack's driving times, not the distances: 0.2 means a fifth of the time.
// The value is exaggerated so that the effect is easy to see in the result.
jack.setConnectionTimeEfficiencyFactor(0.2);
opti.addElement(jack);

IResource john =
    new CapacityResource(
        "John", 50.775346, 6.083887, maxWorkingTimeJohn, maxDistanceKmW, createWorkingHours());
// The factor 1.0 keeps John's driving times at the times of the connections.
john.setConnectionTimeEfficiencyFactor(1.0);
opti.addElement(john);
```

The maximal working times are those of `MandatoryResourceConditionExample`, so that the results compare:

```java
// The limits of MandatoryResourceConditionExample, so that the results compare: Jack may work
// 8 hours a day, John 14.
Duration maxWorkingTimeJack = Duration.ofHours(8);
Duration maxWorkingTimeJohn = Duration.ofHours(14);
```

## Output and what to look for

The run prints the status and progress lines, then the result. In both runs, Jack visited seven of the eight nodes on March 6, in a route of 258.35 minutes that ended at 12:18:21, and John visited only Aachen, the node at the start position of both resources. The violation summaries of both routes were empty. Koeln and Koeln1, at the same position, can change places in Jack's route between runs. Jack's first two node lines were the same in both runs:

```text
0.0 Stuttgart / Geo  / Arrival: 06.03.2020 08:47:15 (Europe/Berlin) , Departure: 06.03.2020 09:07:15 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 47.25 [min], 311.85 [km]
0.1 Heilbronn / Geo  / Arrival: 06.03.2020 09:13:33 (Europe/Berlin) , Departure: 06.03.2020 09:33:33 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 6.3 [min], 41.6 [km]
```

The driving times are short for the distances because of the factor; the visits still take 20 minutes each.

To see what the factor changes, compare a run without it. Four test runs of a variant of this class without the `setConnectionTimeEfficiencyFactor` calls showed two outcomes on March 6, at the same cost: in two runs, Jack visited only Aachen, and John visited the other seven nodes, in a route of 734.09 minutes; in the other two, John visited all eight nodes, starting with Aachen, in a route of 754.09 minutes, and Jack had no route. The drive between Heilbronn and Stuttgart, 41.6 km, took John 31.52 minutes in the variant; Jack drives the same 41.6 km, in the other direction, in 6.3 minutes, a fifth of that time.

In the variant, John's route ended at 20:14:05, or at 20:34:05 with Aachen, after the end of his working hours at 17:00, and showed no violation in any of the four runs. Time after the end of the working hours counts against the maximal working time, and the working hours are a frame for matching resources with the opening hours of the nodes, so a route can end after its working hours without a violation. John may work 14 hours; with Jack's 8 hours, a day this long would be overtime.

## How to interpret the result

### What should change when you scale connection time
Connection time efficiency directly impacts:
- arrival times,
- feasibility against node opening hours (time windows),
- feasibility against resource working hours,
- route ordering decisions (because timing feasibility changes),
- and time-based objective components (if enabled in your model).

### What usually does *not* change directly
Scaling connection **time** does not scale connection **distance**.

So, depending on the rest of your configuration:
- distance-related feasibility limits (maxDistance) may remain the binding factor,
- but time-related feasibility limits (WorkingHours, OpeningHours) may become much easier or harder.

### Why this example is pedagogically useful
The example makes the difference obvious:
- Jack has **less working time** (8h) but a **faster travel model** (0.2).
- John has **more working time** (14h) but normal travel times.

This creates a realistic "dispatch trade-off":
- a fast resource can compensate for a shorter shift,
- or a slower resource may need a longer shift to cover the same workload.

## Where this feature belongs in real projects

### Use cases
1. **Vehicle classes**
   - vans vs trucks vs bikes
2. **Traffic privileges**
   - emergency / priority access routes
3. **Road restrictions or access zones**
   - vehicles restricted from certain streets (often modeled by external connections) plus different speed assumptions
4. **Operational differences**
   - local knowledge, better navigation tooling, "faster technician" due to experience (be careful with this interpretation)

### Recommended practice
- Use connection efficiency to model *systemic* differences (vehicle capabilities), not random noise.
- Keep the factor values explainable (e.g., 0.8, 1.0, 1.2) unless you are explicitly demonstrating the feature.

## Interaction with external connection data

Connection time efficiency is a *resource-level* modifier applied on top of travel time inputs.

This means it can be used with:
- fallback connectors (built-in distance/time approximations),
- or external connection matrices (your own distance/time data),
- or hybrid setups (partial external connections + fallback connector).

Connections with time-dependent data are the exception, as in [Time-dependent driving times with a connection store](../../expert/connectionstore/ConnectionStore.md): where those data hold a time for the moment of the drive, JOpt uses that time without the factor.

Practical pattern:
- Keep your connection data "baseline accurate" for an average vehicle.
- Use efficiency factors to reflect per-vehicle differences without maintaining multiple matrices.

## Pitfalls and how to avoid them

### Pitfall 1: Over-exaggerated factors create unrealistic feasibility
If you set a factor too small, you may unintentionally "solve" feasibility problems that are actually operational impossibilities.

### Pitfall 2: Distance constraints still apply
If maxDistance is tight, reducing time alone may not allow additional work.

### Pitfall 3: Reporting and KPI interpretation
Ensure your reporting clearly distinguishes:
- distance,
- transit time (possibly scaled),
- productive time,
- idle time,
- and whether the travel-time model differs by resource.

## Summary

- `setConnectionTimeEfficiencyFactor(...)` lets you model **resource-specific travel speed** by scaling travel times per resource.
- The example sets John's factor to `1.0`: he drives with the times of the connections.
- Smaller values mean faster effective travel; larger values mean slower travel.
- This changes timing feasibility (WorkingHours / OpeningHours) and can materially alter route assignment decisions.
- The example contrasts:
  - a short-shift, very fast resource (Jack, factor 0.2),
  - with a long-shift, baseline-speed resource (John, factor 1.0),
  demonstrating how connection efficiency impacts dispatch feasibility and allocation.

## Run it

Run the `main(String[] args)` method of `ResourceConnectionEfficiencyExample` from your IDE, with the repository imported as a Maven project. You need a JDK in the version that the examples pom sets. You need no license file: without one, the example uses the public evaluation license (see the [license guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/license)). The run writes `ResourceConnectionEfficiencyExample.kml` into the working directory.

## Related

- [Advanced examples](../README.md): the overview of all Advanced examples.
- [Recommended ways to run an optimization](../../basic/recommendedimplementation_02/RecommendedImplementation.md): the recommended run pattern, a synchronous run and the legacy callback methods.
- [Backup connector feature guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/backupconnector): how JOpt calculates the distance and the driving time of a connection, and the average speed of a resource.
- [Custom distances and driving times with a node connector](../../basic/connection_04/ExternalNodeConnection.md): connections that you add yourself.
- [Time-dependent driving times with a connection store](../../expert/connectionstore/ConnectionStore.md): connections with time-dependent data, whose times the factor does not scale.
- [Visit duration efficiency of a resource](../visitdurationefficiency/ResourceVisitDurationEfficiency.md): the same kind of factor for the visits.
- [Mandatory, preferred, banned and unpreferred resources for a node](../condition/BindingExcludingResourceCondition.md): the examples whose resources, nodes and dates this example uses.

---

Added: 2026-02-17 (JOpt core 7.5.3-j17) | Last updated: 2026-10-23
