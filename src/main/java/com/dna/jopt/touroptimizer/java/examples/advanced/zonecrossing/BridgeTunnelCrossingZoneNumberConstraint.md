# Bridge and tunnel crossing between zones

`BridgeTunnelCrossingZoneNumberConstraintExample` plans two resources in New York whose nodes lie on both sides of the Hudson, in two zones, and adds a cost to every route step from a node of one zone to a node of the other. With the penalty on, each route stays on one side of the river. The page is for developers who model bridges, tunnels or toll borders between areas. The [zone crossing feature guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/zonecrossing) describes the feature.

## What the example shows

This example demonstrates how to **discourage unnecessary crossings between zones** using JOpt's **Zone Crossing Penalization**.

It is designed for real-world scenarios where a boundary crossing has a meaningful overhead, for example:
* toll bridges,
* tunnels,
* congestion zones,
* security checkpoints,
* ports / yards / restricted regions,
* **river crossing** city layouts where switching sides frequently is inefficient.

Instead of trying to approximate this behavior by manually tweaking distance matrices, JOpt provides an **architectural feature**:
* keep normal distance/time costs,
* and add an extra penalty when a route crosses from one zone into another.

- **The switch and the multiplier:** `JOpt.Clustering.PenalizeZoneCodeCrossing` switches the penalty on or off, and `JOpt.Clustering.PenalizeZoneCodeCrossingMultiplier` scales it.
- **Factors per direction:** a `ZoneConnection` sets a factor for one direction between two zones. The example charges steps from zone 1 to zone 3 with the sample factor 5.0 and switches the penalty off from zone 3 to zone 1 with the factor 0.0, like a toll that is charged in one direction only.
- **The extra cost of a crossing step:** its distance times the `JOptWeight.TotalDistance` and `JOptWeight.RouteDistance` weights, times the factor of its `ZoneConnection`, times the multiplier. The cost is added to the normal distance cost.
- **What counts as a crossing:** a step between two nodes whose zone codes do not match, or between a node with a zone and a node without one. The drive from the start of a resource to its first node, and back from the last node, never counts, because a resource is not a node.
- **The JSON of the optimization without a license:** the example prints the optimization as JSON with an extension that has no key setting.

## Scenario

Jack and Mel start and end each working day in Queens (40.742728, -73.870528), outside both zones. They work from 08:00 to 20:00 on May 6 and May 7, 2020 (America/New_York), and may work 13 hours and drive 1200 km per day. Their working hours carry no zone constraints, so both may visit both zones.

Sixteen `TimeWindowGeoNode` objects are open from 08:00 to 17:00 on both days, and each visit takes 60 minutes:

- `Manhattan-0` to `Manhattan-7`, in Manhattan: zone 1, with zone 2 as an extra code.
- `JerseyCity-0` to `JerseyCity-7`, across the Hudson from Manhattan: zone 3, with zone 4 as an extra code.

The two areas share no zone code, so every step between them is a crossing. The dates, coordinates, visit durations and limits are sample values; 13 hours and 1200 km are more than a day of this plan needs, and the 60-minute visits let one resource serve all nodes of a zone in one day.

## How the code works

The source is [BridgeTunnelCrossingZoneNumberConstraintExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/zonecrossing/BridgeTunnelCrossingZoneNumberConstraintExample.java). `example()` runs the steps of the [First optimization example](../../basic/firstoptimization_01/FirstOptimizationExample.md), with three extra steps: `addZoneConnections`, `exportToKml` and `printJson`:

```java
// Set to false to see the routes without the crossing penalty.
boolean doPenalizeZoneCrossings = true;

// The public evaluation license, or your own license if you put a license file where
// ExampleLicenseHelper looks for it.
ExampleLicenseHelper.setLicense(this);
addProperties(this, doPenalizeZoneCrossings);
addNodes(this);
addResources(this);
addZoneConnections(this);
attachToObservables(this);
IOptimizationResult result = startAndPresentResult(this);
exportToKml(result, doPenalizeZoneCrossings);
printJson(this);
```

### Properties

```java
props.setProperty(
    "JOpt.Clustering.PenalizeZoneCodeCrossing", String.valueOf(doPenalizeZoneCrossings));
// The extra cost of a step between two zones: its distance times the TotalDistance and
// RouteDistance weights, times the factor of its ZoneConnection, times this multiplier. With
// 10.0, no route of this plan crosses between the zones (see the output of the example).
props.setProperty("JOpt.Clustering.PenalizeZoneCodeCrossingMultiplier", "10.0");
```

`addProperties` also sets fewer generations and iterations than the defaults, which keeps the run short. The [property reference](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties) lists the defaults of all these properties.

### Zones

Each node gets the zone qualification of its area:

```java
// Manhattan is zone 1; the JerseyCity nodes across the Hudson are zone 3. The extra codes 2
// and 4 show that a qualification can hold several codes; the two areas share none, so every
// step between them is a crossing.
ZoneNumberQualification manhattanQuali = new ZoneNumberQualification(new ZoneNumber(1));
manhattanQuali.addExtraCode(new ZoneNumber(2));

ZoneNumberQualification jerseyQuali = new ZoneNumberQualification(new ZoneNumber(3));
jerseyQuali.addExtraCode(new ZoneNumber(4));
```

### Factors per direction

`addZoneConnections` puts one `ZoneConnection` for each direction into the zone manager of the node connector:

```java
IZoneManager zm = opti.getNodeConnector().getZoneManager();

// The sample factor 5.0: a step from zone 1 to zone 3 costs five times the crossing penalty,
// like a toll that is charged in one direction only.
ZoneConnection zoneConnection13 =
    ZoneConnection.builder()
        .fromZoneId("1")
        .toZoneId("3")
        .crossingPenaltyMultiplier(5.0)
        .build();
zm.putZoneConnection(zoneConnection13);
```

The second connection, from zone 3 to zone 1, has the factor 0.0.

### KML and JSON

`exportToKml` writes the result to `BridgeTunnelCrossingZoneNumberConstraintExample-true.kml`, or `-false.kml` when the penalty is off, in the working directory. The two files let you compare the routes of both settings in a KML viewer.

`printJson` prints the optimization as JSON, without the solution and the node connections. Its extension has a timeout and no key setting, so the JSON contains no license:

```java
// The extension holds a timeout of 10 minutes, a sample value, and no key setting, so the
// printed JSON contains no license.
JSONConfig extension = JSONConfig.builder().timeOut(Duration.ofMinutes(10)).build();
```

## Output and what to look for

With the penalty on, the result has four routes, of which two are scheduled (trimmed to these lines, the same in two test runs):

```text
 Number of Route         : 4
 Number of Route (sched.): 2
 Total Route Elements    : 18
```

In both runs, one resource visits the eight `JerseyCity` nodes on May 6 and the other the eight `Manhattan` nodes; the routes of May 7 stay empty. No step of the result crosses between the zones, so the two `ZoneConnection` factors do not appear in its costs. Both runs had 67 km in total. In one run Jack visited the JerseyCity nodes and Mel the Manhattan nodes, in the other the sides were swapped, so which resource takes which side can change between runs.

In an earlier test run with `doPenalizeZoneCrossings` set to `false`, both routes had a mix of `Manhattan` and `JerseyCity` nodes: each route crossed the river twice, for 65 km in total. With the penalty, the plan is 2 km longer and has no crossing.

The JSON at the end lists the two connections, the same in both runs:

```json
"zoneConnections" : [ {
  "fromZoneId" : "1",
  "toZoneId" : "3",
  "crossingPenaltyMultiplier" : 5.0
}, {
  "fromZoneId" : "3",
  "toZoneId" : "1",
  "crossingPenaltyMultiplier" : 0.0
} ],
```

Its extension holds only `"timeOut" : "PT10M"`.

## What problem does this solve?

Without the crossing penalty (`doPenalizeZoneCrossings` set to `false`), the optimizer will select the best solution with respect to:
- travel distance/time,
- visit/service times,
- and any active constraints.

In dense areas split by a river, tunnel, or toll border, the "best" distance solution can still look unrealistic:
- a route zig-zags across zones multiple times, because each local step looks cheap,
- but the boundary crossings are operationally expensive.

Zone crossing penalization addresses this by making "crossing the border" itself expensive.

The intended behavior is:
- routes mostly stay inside one zone,
- crossing happens only when it truly improves the global solution; the drive from the start of a resource to its first node, and back from the last node, never counts as a crossing.

## Core mechanism (how penalization works)

When enabled, JOpt applies an additional cost whenever an edge in the tour goes from **Zone A -> Zone B**.

There are two layers:

### 1) Global multiplier (optimizer property)

The example sets:
- `JOpt.Clustering.PenalizeZoneCodeCrossing = true/false` (toggle)
- `JOpt.Clustering.PenalizeZoneCodeCrossingMultiplier = 10.0`

Interpretation:
- a zone-crossing edge becomes significantly more expensive than an intra-zone edge, unless its `ZoneConnection` sets the factor 0.0 (as from zone 3 to zone 1 in this example),
- which nudges the optimizer away from frequent crossings.

### 2) Direction-specific multipliers (ZoneConnection)

For more control, the example also configures **directional zone connections**:

- Zone `1` -> Zone `3` has an additional multiplier of `5.0`
- Zone `3` -> Zone `1` has an additional multiplier of `0.0`

This illustrates an important modeling capability:
- you can penalize crossing **asymmetrically**, for example:
  - "going into the city is expensive (toll), leaving the city is cheap", or
  - "crossing east->west is often congested, west->east rarely".

In the code, this is configured via the zone manager:

- `opti.getNodeConnector().getZoneManager().putZoneConnection(...)`

## Zone modeling in this example

This example uses **ZoneNumber** (integer ZoneCodes).

### Zone sets (two "macro areas" with two codes each)
To demonstrate multi-zone membership, the nodes are assigned as:

- Manhattan-like area:
  - Zone 1 (primary)
  - Zone 2 (extra code on the same qualification)

- Jersey City-like area:
  - Zone 3 (primary)
  - Zone 4 (extra code on the same qualification)

This is done using:
- `new ZoneNumberQualification(new ZoneNumber(1))` plus `addExtraCode(new ZoneNumber(2))` (and analogous for Zone 3/4)

Practical note:
- Multi-zone membership is the recommended way to model border areas or "shared service areas".
- A job on a boundary can carry multiple zones, allowing either side to serve it without "hard polygon edges".

## How to validate the effect (what to look for)

### 1) Toggle penalization on/off
The example defines:
- `boolean doPenalizeZoneCrossings = true;`, with the comment "Set to false to see the routes without the crossing penalty." in the line above it

Run twice:
- once with `true`,
- once with `false`.

You should observe:
- `false`: routes can switch zones (distance-optimal but unrealistic)
- `true`: routes tend to cluster by zone and reduce crossings

### 2) Visual inspection via KML export
The example exports the solution to KML:

- `BridgeTunnelCrossingZoneNumberConstraintExample-<true|false>.kml`

Open the KML in Google Earth (or another KML viewer) and compare:
- how often the route crosses the implicit zone boundary,
- how "clean" the tours stay inside each zone.

This is a quick, practical way to communicate the feature to end users and stakeholders.

## Recommended modeling patterns

### Pattern A: Penalize only specific borders
Use ZoneConnection entries for:
- bridge/tunnel edges,
- controlled border crossings,
- congestion-zone boundaries.

Leave other zone-to-zone transitions unpenalized: give them a `ZoneConnection` with the factor 0.0, as the example does from zone 3 to zone 1, because a crossing between two zones without a `ZoneConnection` is penalized as well.

### Pattern B: Use asymmetry intentionally
Directional penalties allow you to model:
- toll directionality,
- one direction that is congested more often than the other,
- one-way physical constraints.

### Pattern C: Combine with hard territories when required
If territories are mandatory:
- enforce via ZoneCode constraints on WorkingHours (hard),
- and still apply zone crossing penalties for "allowed but undesirable" transitions between adjacent territories.

This provides a clean separation:
- feasibility (hard territory rules),
- realism/efficiency (crossing penalization).

## Run it

Run the `main(String[] args)` method of `BridgeTunnelCrossingZoneNumberConstraintExample` from your IDE, with the repository imported as a Maven project. You need a JDK in the version that the examples pom sets.

The example needs the public evaluation license, because it has more elements than JOpt allows without a license. You need no license file: without one, `ExampleLicenseHelper` sets the public evaluation license. The [license guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/license) lists what each license allows.

To compare both settings, run the example once as it is and once with `doPenalizeZoneCrossings` set to `false`, then open both KML files.

## Related

- [Zone crossing penalization](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/zonecrossing): the feature guide of the crossing penalty, with its concept and properties and the factors per direction.
- [ZoneCodes and territory management](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/zonecodes): zone codes on nodes and working hours, with hard or soft territories.
- [Defining territories via ZoneCodes](https://www.dna-evolutions.com/docs/learn-and-explore/special/special_features#defining-territories-via-zonecodes): the foundation concept used by this feature.
- [Optimization properties](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties): the property reference, with the defaults.
- [Recommended ways to run an optimization](../../basic/recommendedimplementation_02/RecommendedImplementation.md): the recommended run pattern, a synchronous run and the legacy callback methods.
- [Advanced examples](../README.md): the overview of all Advanced examples.

---

Added: 2026-02-17 (JOpt core 7.5.3-j17) | Last updated: 2026-10-23
