# Opening hours that bound only the arrival

`IncludeVisitDurationExample` gives the node Koeln opening hours of 15 minutes, from 09:00 to 09:15, and a visit of 20 minutes. With `setIsDutyHoursIncludesVisitDuration(false)`, these opening hours bound only the arrival: Jack must arrive between 09:00 and 09:15, and the visit may end after 09:15 without a violation. The page is for developers whose nodes have an arrival window rather than a window for the whole visit, for example a site that can be entered only at certain times. If you have not built an optimization yet, start with the [First optimization example](../../basic/firstoptimization_01/FirstOptimizationExample.md).

## What the example shows

- **The default:** the opening hours of a node include its visit duration. The visit must start and end inside them; a visit that ends later is late.
- **`setIsDutyHoursIncludesVisitDuration(false)`:** the opening hours act as an access window. The visit must start inside them and may end after them.
- **Per node:** only Koeln has the setting. The other seven nodes keep the default.
- **The visit still takes its time:** the departure from Koeln, the arrival at the next node and Jack's working time all include the 20 minutes.

## Scenario

The dates, times and coordinates are sample values.

- **Resource:** `Jack` starts at 50.775346, 6.083887 (Aachen) and works on May 6 and 7, 2020, from 08:00 to 17:00 (Europe/Berlin). His maximal working time is 13 hours, more than the 9 hours of the working hours, and his maximal distance 1200 km.
- **Koeln:** open only on May 6, 2020, from 09:00 to 09:15, which is shorter than its visit of 20 minutes, and set to `setIsDutyHoursIncludesVisitDuration(false)`.
- **Seven other nodes:** Essen, Dueren, Nuernberg, Heilbronn, Stuttgart, Wuppertal and Aachen, all `TimeWindowGeoNode` objects, open on May 6 and 7, 2020, from 08:00 to 17:00.
- **All eight nodes:** a visit of 20 minutes and the importance 1.

`addProperties` sets two properties: `JOptExitCondition.JOptGenerationCount` to 1000 and `JOpt.Algorithm.PreOptimization.SA.NumIterations` to 100000. Both are below their defaults, which keeps the run short; the defaults are in the [property reference](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties).

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
INode koeln =
    new TimeWindowGeoNode("Koeln", 50.9333, 6.95, koelnAccessHours, visitDuration, importance);
// By default the visit must start and end inside the opening hours. With false, Jack only has
// to arrive inside them, and the visit may go on after they end.
koeln.setIsDutyHoursIncludesVisitDuration(false);
opti.addElement(koeln);
```

## Output and what to look for

The run prints the status and progress lines, then the result. In two test runs the result was the same. Route 0, on May 6, visits Dueren, Koeln, Wuppertal, Essen and Aachen. The line of Koeln:

```text
0.1 Koeln / Geo  / Arrival: 06.05.2020 09:08:36 (Europe/Berlin) , Departure: 06.05.2020 09:28:36 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 27.25 [min], 35.97 [km]
```

Jack arrives at 09:08:36, inside the opening hours, and leaves at 09:28:36, after their end at 09:15. The line shows no violation, and the violation summary of route 0 is empty.

In a test run with `setIsDutyHoursIncludesVisitDuration(true)`, the default, the line of Koeln showed a LATE violation of 5 minutes. With 15 minutes of opening hours and a visit of 20 minutes, a visit that must end inside the opening hours is at least 5 minutes late.

Route 1, on May 7, visits Nuernberg, Stuttgart and Heilbronn. Its route header, trimmed to the three time lines:

```text
Defined WorkingHours   : 07.05.2020 08:00:00 - 07.05.2020 17:00:00 (Europe/Berlin)
Effective Route Start  : 07.05.2020 08:00:00 (Europe/Berlin)
Effective Route Stop   : 07.05.2020 19:58:18 (Europe/Berlin)
```

The route ends at 19:58:18, after 17:00, and its violation summary is empty. Overtime counts against the maximal working time, and the working hours are a frame for matching resources with the opening hours of the nodes, so a route can end after its working hours without a violation.

## Run it

Run the `main(String[] args)` method of `IncludeVisitDurationExample` from your IDE, with the repository imported as a Maven project. You need a JDK in the version that the examples pom sets. You need no license file: without one, the example uses the public evaluation license (see the [license guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/license)). The run writes `IncludeVisitDurationExample.kml` into the working directory.

## Related

- [IncludeVisitDurationExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/includevisitduration/IncludeVisitDurationExample.java): the source of this example.
- [Basic elements tutorial](https://www.dna-evolutions.com/docs/getting-started/tutorials/basic-elements): the opening hours of nodes and the working hours of resources.
- [Recommended ways to run an optimization](../../basic/recommendedimplementation_02/RecommendedImplementation.md): the recommended run pattern, a synchronous run and the legacy callback methods.
- [Advanced examples](../README.md): the overview of all Advanced examples.

---

Added: 2026-02-17 (JOpt core 7.5.3-j17) | Last updated: 2026-10-20
