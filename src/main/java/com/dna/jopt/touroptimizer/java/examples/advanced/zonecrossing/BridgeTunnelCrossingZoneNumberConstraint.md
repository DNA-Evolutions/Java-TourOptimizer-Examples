# Bridge and tunnel crossing between zones

`BridgeTunnelCrossingZoneNumberConstraintExample` plans two resources in New York whose nodes lie on both sides of the Hudson, in two zones, and adds a cost to every route step from a node of one zone to a node of the other. With the penalty on, each route stays on one side of the river. The page is for developers who model bridges, tunnels or toll borders between areas. The [zone crossing feature guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/zonecrossing) describes the feature.

## What the example shows

- **The switch and the multiplier:** `JOpt.Clustering.PenalizeZoneCodeCrossing` switches the penalty on or off, and `JOpt.Clustering.PenalizeZoneCodeCrossingMultiplier` scales it.
- **Factors per direction:** a `ZoneConnection` sets a factor for one direction between two zones. The example charges steps from zone 1 to zone 3 with the sample factor 5.0 and switches the penalty off from zone 3 to zone 1 with the factor 0.0, like a toll that is charged in one direction only.
- **The extra cost of a crossing step:** its distance times the `JOptWeight.TotalDistance` and `JOptWeight.RouteDistance` weights, times the factor of its `ZoneConnection`, times the multiplier. The cost is added to the normal distance cost.
- **What counts as a crossing:** a step between two nodes whose zone codes do not match. The drive from the start of a resource to its first node, and back from the last node, never counts, because a resource is not a node.
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

In both runs, one resource visits the eight `JerseyCity` nodes on May 6 and the other the eight `Manhattan` nodes; the routes of May 7 stay empty. No step of the result crosses between the zones, so the two `ZoneConnection` factors do not appear in its costs. Both runs had 67 km in total. In both runs Jack visited the JerseyCity nodes and Mel the Manhattan nodes; in an earlier test run the sides were swapped, so which resource takes which side can change between runs.

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

## Run it

Run the `main(String[] args)` method of `BridgeTunnelCrossingZoneNumberConstraintExample` from your IDE, with the repository imported as a Maven project. You need a JDK in the version that the examples pom sets.

The example needs the public evaluation license, because it has more elements than JOpt allows without a license. You need no license file: without one, `ExampleLicenseHelper` sets the public evaluation license. The [license guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/license) lists what each license allows.

To compare both settings, run the example once as it is and once with `doPenalizeZoneCrossings` set to `false`, then open both KML files.

## Related

- [Zone crossing penalization](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/zonecrossing): the feature guide of the crossing penalty, its properties and the factors per direction.
- [ZoneCodes and territory management](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/zonecodes): zone codes on nodes and working hours, with hard or soft territories.
- [Optimization properties](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties): the property reference, with the defaults.
- [Recommended ways to run an optimization](../../basic/recommendedimplementation_02/RecommendedImplementation.md): the recommended run pattern, a synchronous run and the legacy callback methods.
- [Advanced examples](../README.md): the overview of all Advanced examples.

---

Added: 2026-02-17 (JOpt core 7.5.3-j17) | Last updated: 2026-10-20
