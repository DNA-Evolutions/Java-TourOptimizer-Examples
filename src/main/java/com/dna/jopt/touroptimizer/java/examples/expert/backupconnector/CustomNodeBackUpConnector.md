# Custom backup connector for missing distances and driving times

`CustomNodeBackUpConnectorExample` and `CustomNodeBackUpConnectorHaversineExample` replace the calculation that fills in missing connections: the first makes every flat-earth distance 1.2 times longer and prints each connection it calculates, the second uses the haversine distance times 1.2. The page is for developers who run without a full distance and time matrix and want their own fallback model. It builds on [Custom distances and driving times with a node connector](../../basic/connection_04/ExternalNodeConnection.md) and on the [Backup Connector](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/backupconnector) feature guide.

## What the example shows

- **The backup connector of a node connector:** `setBackupElementConnector` on a `NodeEdgeConnector` sets the class that calculates a connection when none is given, and `setNodeConnector` gives the node connector to the optimization.
- **A subclass of the default:** `MyBackupElementConnector` extends `DefaultFlatEarthAverageSpeedBackupElementConnector` and overrides `getElement2ElementDistance` and `getElement2ElementDuration`.
- **The distance model:** the flat-earth distance (`NodeEdgeConnector.distancePlacePlaceFlatEarth`) times 1.2 in the first class, the class's own `haversineDistanceMeter` times 1.2 in the second.
- **The driving time:** the distance divided by the average speed of the resource (`getAvgSpeed`), the formula of the base class.
- **Which connections are calculated:** the first class prints one line `from=>to` per calculated connection.

## Scenario

The dates, times and coordinates are sample values. The time zone is Europe/Berlin.

- **Resource:** `Jack`, a `CapacityResource` at 50.775346, 6.083887 (Aachen), with working hours on May 6 and May 7, 2020, from 08:00 to 17:00. He may work up to 13 hours and drive up to 1200 km a day.
- **Nodes:** eight `TimeWindowGeoNode` objects (Koeln, Oberhausen, Essen, Dueren, Heilbronn, Stuttgart, Wuppertal and Aachen), open on the same two days from 08:00 to 17:00, each with a visit of 20 minutes and the same importance. Unlike other examples with these cities, there is no node in Nuernberg: with the 1.2 factor, the nine nodes with Nuernberg did not fit into two days of 13 hours. With a ninth node in Nuernberg, both days ran over the 13-hour maximum in two test runs of each class (794.85 and 794.19 minutes with the flat-earth distance).
- **Connections:** none are given, so the backup connector calculates all of them.
- **Run values:** `addProperties` sets `JOptExitCondition.JOptGenerationCount` and `JOpt.Algorithm.PreOptimization.SA.NumIterations` below their defaults, which keeps the run short; the defaults are in the [property reference](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties).

## How the code works

The sources are [CustomNodeBackUpConnectorExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/backupconnector/CustomNodeBackUpConnectorExample.java) and [CustomNodeBackUpConnectorHaversineExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/backupconnector/CustomNodeBackUpConnectorHaversineExample.java). In both, `main(...)` instantiates the class and calls `example()`, which calls the steps `addProperties`, `addNodes`, `addResources`, `setBackupConnector`, `attachToObservables` and `startAndPresentResult`. `attachToObservables` subscribes to the progress, warning, status and error events with the recommended event API, before the run starts, and `startAndPresentResult` waits up to 5 minutes for the result and prints it.

`setBackupConnector` sets the custom backup connector (the same code in both classes):

```java
// false: the connector calculates the driving time of a connection once, for the first
// resource that needs it. Jack is the only resource, so nothing is lost.
INodeEdgeConnector connector = new NodeEdgeConnector();
connector.setBackupElementConnector(new MyBackupElementConnector(false));
opti.setNodeConnector(connector);
```

The distance of `CustomNodeBackUpConnectorExample`:

```java
// 1.2: roads are longer than the straight line, for example through detours and terrain.
double distance =
    NodeEdgeConnector.distancePlacePlaceFlatEarth(
        fromElementLon, fromElementLat, toElementLon, toElementLat)
        * 1.2;

// Shows which connections the connector calculates.
System.out.println(fromElementId + "=>" + toElementId);
```

The distance of `CustomNodeBackUpConnectorHaversineExample`, which uses its own method `haversineDistanceMeter`:

```java
// The core has the same formula (NodeEdgeConnector.distancePlacePlaceHaversine); the own
// method shows where a distance model of your own goes.
double distance =
    CustomNodeBackUpConnectorHaversineExample.haversineDistanceMeter(
        fromElementLon, fromElementLat, toElementLon, toElementLat)
        * 1.2;
```

## Output and what to look for

In both classes, the cost, the route of May 7 and the lines below were the same in four test runs; the order of the stops on May 6 changed between them. The node Aachen lies at Jack's start position, so a route can visit it first or last at the same cost.

`CustomNodeBackUpConnectorExample` prints 80 lines `from=>to`, each pair once. The connector receives the location ids of the elements, which are the element ids when no location id is set. Eight of the lines come from an element named `Deputy-c::0`, which the example does not create (trimmed to the first lines):

```text
Deputy-c::0=>Dueren
Deputy-c::0=>Aachen
Aachen=>Dueren
Dueren=>Aachen
Deputy-c::0=>Koeln
Aachen=>Koeln
Koeln=>Aachen
```

Both classes plan the near nodes on May 6 and Heilbronn and Stuttgart on May 7. Route 1 of `CustomNodeBackUpConnectorExample` (trimmed to these lines):

```text
Defined WorkingHours   : 07.05.2020 08:00:00 - 07.05.2020 17:00:00 (Europe/Berlin)
Effective Route Stop   : 07.05.2020 18:26:20 (Europe/Berlin)
Route time       [min] : 626.34
Route Distance   [km]  : 773.97
1.0 Stuttgart / Geo  / Arrival: 07.05.2020 12:43:30 (Europe/Berlin) , Departure: 07.05.2020 13:03:30 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 283.5 [min], 374.22 [km]
1.1 Heilbronn / Geo  / Arrival: 07.05.2020 13:41:19 (Europe/Berlin) , Departure: 07.05.2020 14:01:19 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 37.82 [min], 49.92 [km]
```

Route 1 of `CustomNodeBackUpConnectorHaversineExample` visits the two nodes in the other order (trimmed to these lines):

```text
Effective Route Stop   : 07.05.2020 18:26:55 (Europe/Berlin)
Route time       [min] : 626.93
Route Distance   [km]  : 774.75
1.0 Heilbronn / Geo  / Arrival: 07.05.2020 12:22:28 (Europe/Berlin) , Departure: 07.05.2020 12:42:28 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 262.48 [min], 346.47 [km]
1.1 Stuttgart / Geo  / Arrival: 07.05.2020 13:20:19 (Europe/Berlin) , Departure: 07.05.2020 13:40:19 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 37.84 [min], 49.95 [km]
```

The two distance models give route distances within 1 km of each other on this day (773.97 km and 774.75 km). Both routes end after 18:00, after the end of their working hours at 17:00, and the results show no violation. Time after the end of the working hours counts against the maximal working time, and the working hours are a frame for matching resources with the opening hours of the nodes, so a route can end after its working hours without a violation. The 626.34 and 626.93 minutes stay below Jack's maximal working time of 13 hours.

## When the Backup Connector is used

JOpt.TourOptimizer can operate with *externally provided* connections (distance + driving time) **or** it can auto-generate connections when they are missing.  
The mechanism responsible for filling missing `NodeEdgeConnectorItem`s is the **Backup Connector**.

A custom Backup Connector is the right tool when you need to:

- run without external routing data (or only partial routing data),
- apply a systematic correction factor (terrain, detours, traffic proxies),
- switch the underlying distance model (flat-earth vs haversine),
- implement custom business logic per edge (e.g., "crossing the river adds 10 minutes"),
- make travel time depend on the resource (different average speeds).

Official documentation:
- [Backup Connector](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/backupconnector)

### You provide a full matrix of connections
If you provide distances and driving times for all required element pairs, the Backup Connector will rarely be used.

This is typical when you integrate a routing engine:
- OSRM / GraphHopper / commercial APIs / internal road-network services.

### You provide **no** connections or only a partial set
If some connections are missing, the optimizer asks the Backup Connector to compute them on demand.

This is typical when:
- you prototype quickly,
- you only have raw lat/lon,
- you intentionally do not compute a full matrix (performance),
- you want a fallback if your routing service does not return a result for some pairs.

Related background (element connections):
- [Basic Elements, Element connections](https://www.dna-evolutions.com/docs/getting-started/tutorials/basic-elements)

## Default behavior (what gets computed)
The default concept is:

1. Compute a distance between two coordinates using a geometric formula (flat-surface projection; the default backup connector uses this one, and a custom one can use haversine).
2. Convert distance -> driving time using the resource's average speed.

The documentation provides the background and tradeoffs:
- flat-surface is fast and sufficiently accurate for typical "short hops" in optimized tours,
- haversine is more precise for longer distances but usually not needed for city-scale neighbor edges.  
  See: [Backup Connector](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/backupconnector)

## The implementation pattern (what you actually do)

### Step 1: Assign a custom backup connector to the optimizer's node connector

In `CustomNodeBackUpConnectorExample`, `setBackupConnector` does it (code above).

Conceptually:
- The `NodeEdgeConnector` is responsible for producing/holding element connections.
- The Backup Connector is the fallback used when a required connection is missing.

### Step 2: Extend an existing default backup connector class
Both examples implement:

- `MyBackupElementConnector extends DefaultFlatEarthAverageSpeedBackupElementConnector`

This gives you:
- a ready-made baseline implementation,
- the ability to override only what you need.

### Step 3: Override the two key methods

1) **Distance** (coordinates -> meters):
- `getElement2ElementDistance(...)`

2) **Duration** (meters + resource -> travel time):
- `getElement2ElementDuration(...)`

That is the "contract":
- distance is a function of coordinates,
- duration is a function of distance *and* the visiting resource (to support different speeds).

## The constructor flag: `doRecalculateElement2ElementDuration`

The default base class constructor takes:

- `doRecalculateElement2ElementDuration`

Meaning:

### `false`: duration can be reused across resources
Use this when:
- all resources have the same average speed, or
- you intentionally want a uniform travel-time model (resource-independent).

Result:
- once duration is computed for an edge, it does not need to be recomputed per resource. The duration of the first resource that needs the edge stays.

Both examples pass `false`. With Jack as the only resource, this loses nothing.

### `true`: duration must be recomputed per resource
Use this when:
- resources have different `avgSpeed`, and travel time must reflect that.

Result:
- the optimizer will re-evaluate duration for an edge that the backup connector created each time the edge is looked up, so that the visiting resource of the lookup counts.

This is a key scaling decision:
- resource-dependent durations are more realistic,
- but they also increase the amount of duration computation.

Without a custom backup connector, the node connector uses a `DefaultFlatEarthAverageSpeedBackupElementConnector` created with `true`.

## Example 1: Flat-earth distance + terrain correction factor (1.2x)

**CustomNodeBackUpConnectorExample** uses:

- `NodeEdgeConnector.distancePlacePlaceFlatEarth(...)`
- multiplied by `1.2`

Interpretation:
- "straight line distance is too optimistic; roads are twisty / terrain is difficult"
- every missing connection becomes 20% longer than the default geometric estimate.

This is a common and pragmatic approach when you do not have a routing engine but still want to:
- avoid underestimating travel distance and time,
- prevent overly optimistic schedules.

The example also prints the edge IDs when the connector is used, which is a good debugging technique to verify:
- which connections were auto-generated,
- and whether you are accidentally missing a large portion of your matrix.

## Example 2: Haversine distance + correction factor (1.2x)

**CustomNodeBackUpConnectorHaversineExample** demonstrates how to swap the distance model:
- it uses a haversine implementation (`haversineDistanceMeter(...)`) and applies the same correction factor.

The core offers the same formula as `NodeEdgeConnector.distancePlacePlaceHaversine(...)`; the example's own method shows where a distance model of your own goes.

Use this approach when:
- your nodes can be far apart (regional routing),
- or you have a use case where great-circle precision matters.

For most routing problems where consecutively visited nodes are near neighbors, the practical difference is small, which is also discussed in the documentation:
- [Backup Connector](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/backupconnector)

## Travel time calculation and `avgSpeed`

Both examples compute driving time using the resource's `avgSpeed`:

```java
long travelTimeMillis = (long) (distanceMeter / visitor.getAvgSpeed() * 1000L);
```

This ties travel time to the resource configuration.

To change average speed per resource, configure it on the resource with `setAvgSpeed` (see documentation for details):
- [Backup Connector](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/backupconnector)
- [Basic Elements](https://www.dna-evolutions.com/docs/getting-started/tutorials/basic-elements)

Practical modeling notes:
- If you use `doRecalculateElement2ElementDuration = true`, `avgSpeed` differences become effective.
- If you keep it `false`, speed differences will not be reflected in travel times (unless you implement your own logic): the time of the first resource that needs an edge applies to all.

## Recommended production practices

### Use a routing engine for "final-mile accuracy" and the Backup Connector as fallback
A robust architecture is:
- primary connections from an external routing service,
- backup connector for missing pairs or error cases.

This prevents failures when:
- the routing service cannot compute a route for a pair,
- network calls time out,
- a node is outside the supported region.

### Add caching if you compute many edges repeatedly
If your custom connector computes expensive logic (API calls, terrain checks):
- cache by (fromId, toId, resourceId) depending on whether duration is resource-dependent.

The node connector keeps each connection that the backup connector creates, so the distance of a pair is calculated once (80 lines for 80 pairs in the example).

### Keep the model internally consistent
If you apply correction factors, apply them systematically:
- do not scale distance but forget to scale duration (or vice versa),
- unless that mismatch is intentionally part of your cost model.

### Validate with a small instance first
Before scaling up, run a tiny instance and log:
- which edges were generated by the backup connector,
- whether the computed values align with expectations.

## Summary

- The Backup Connector fills in missing `NodeEdgeConnectorItem` connections.
- A custom connector is ideal for custom distance/time models, correction factors, and fallback behavior.
- Implement it by extending `DefaultFlatEarthAverageSpeedBackupElementConnector` and overriding:
  - `getElement2ElementDistance(...)`
  - `getElement2ElementDuration(...)`
- Use `doRecalculateElement2ElementDuration` to control whether duration depends on the visiting resource.
- The included examples demonstrate:
  - flat-earth distance with a 1.2x correction factor,
  - haversine distance with the same correction factor.

## Run it

Run the `main(String[] args)` method of `CustomNodeBackUpConnectorExample` or `CustomNodeBackUpConnectorHaversineExample` from your IDE, with the repository imported as a Maven project. You need a JDK in the version that the examples pom sets. You need no license file: without one, the examples use the public evaluation license (see the [license guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/license)).

## Related

- [Backup Connector](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/backupconnector): the feature guide, with the flat-surface and the haversine formula and the average speed.
- [Custom distances and driving times with a node connector](../../basic/connection_04/ExternalNodeConnection.md): connections given by your own code, with the backup connector for the rest.
- [Basic elements tutorial](https://www.dna-evolutions.com/docs/getting-started/tutorials/basic-elements): the nodes, resources and element connections.
- [Recommended ways to run an optimization](../../basic/recommendedimplementation_02/RecommendedImplementation.md): the recommended run pattern, a synchronous run and the legacy callback methods.
- [Expert examples](../README.md): the overview of all Expert examples.

---

Added: 2026-02-17 (JOpt core 7.5.3-j17) | Last updated: 2026-10-23
