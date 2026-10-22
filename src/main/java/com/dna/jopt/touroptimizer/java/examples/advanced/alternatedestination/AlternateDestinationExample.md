# Alternate destination at the end of a route

This advanced example, `AlternateDestinationExample`, shows how to terminate a resource route at an alternate destination: it ends every route of the resource Jack in Essen instead of at his start position in Aachen, the resource's home/start location, while each working day starts in Aachen again. The page is for developers who plan resources that end their working day at another place than their start, for example a driver who starts every working day at home and ends it at a depot, with the way back home outside the working hours. It uses the nodes, resources and working hours of the [Basic elements tutorial](https://www.dna-evolutions.com/docs/getting-started/tutorials/basic-elements).

## What the example shows

Three setters on the resource define the alternate destination:

- `setDestinationLatitude` and `setDestinationLongitude` set the position where each route ends.
- `setAlternateDestination(true)` switches the alternate destination on.

Each route then ends at the alternate destination, a different location than the start (e.g., a hotel, a secondary depot, a handover location): in operational terms, the driver or technician starts at a home base, performs the work during the WorkingHours and ends the working day there. The next working day starts at the start position of the resource again; the drive from the alternate destination back to the start position lies outside the working hours and is not part of the plan: the resource "returns home" outside WorkingHours. For working days that start where the day before ended, see the [Overnight stay feature guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/overnight_stay).

## Scenario

The dates, times and coordinates are sample values. The time zone is Europe/Berlin.

- **Resource:** `Jack`, a `CapacityResource` at 50.775346, 6.083887 (Aachen), with working hours on May 6 and May 7, 2020, from 08:00 to 17:00. He may work up to 13 hours and drive up to 1200 km a day.
- **Alternate destination:** 51.45, 7.01667, the position of the node Essen.
- **Nodes:** nine `TimeWindowGeoNode` objects (Koeln, Oberhausen, Essen, Dueren, Nuernberg, Heilbronn, Stuttgart, Wuppertal and Aachen), open on the same two days from 08:00 to 17:00, each with a visit of 20 minutes and the same importance.

## How the code works

The source is [AlternateDestinationExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/alternatedestination/AlternateDestinationExample.java). `main(...)` instantiates the class and calls `example()`:

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

`addProperties` sets `JOptExitCondition.JOptGenerationCount` and `JOpt.Algorithm.PreOptimization.SA.NumIterations` below their defaults, which keeps the run short; the defaults are in the [property reference](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties).

`addResources` creates Jack and sets the alternate destination:

```java
// 13 h, more than the 9 h of the working hours: a day may run past 17:00 without overtime.
Duration maxWorkingTime = Duration.ofHours(13);
// 1200 km per day, more than the longest day of this plan needs.
Quantity<Length> maxDistanceKmW = Quantities.getQuantity(1200.0, KILO(METRE));

IResource jack =
    new CapacityResource(
        "Jack", 50.775346, 6.083887, maxWorkingTime, maxDistanceKmW, workingHours);

// Each route of Jack ends in Essen. The drive back to Aachen lies outside the working hours,
// and the next working day starts in Aachen again.
jack.setDestinationLatitude(51.45);
jack.setDestinationLongitude(7.01667);
jack.setAlternateDestination(true);

opti.addElement(jack);
```

`attachToObservables` subscribes to the progress, warning, status and error events with the recommended event API, before the run starts. `startAndPresentResult` starts the run, waits up to 5 minutes for the result, prints it and returns it. `exportToKml` writes the result to `AlternateDestinationExample.kml` in the working directory, as in [Export a result to a KML file](../../basic/io_03/Export2KMLExample.md).

## Output and what to look for

The result was the same in two test runs. Each route says `Alternate destination  : true`, and each starts in Aachen at 08:00.

Route 0 on May 6 visits Aachen, Dueren, Koeln, Wuppertal, Oberhausen and Essen. Its last stop is the node Essen, at the alternate destination, so the termination time and distance are 0 (trimmed to these lines of route 0):

```text
RouteId                : 0
Alternate destination  : true
Termi. time      [min] : 0.0
Termi. distance  [km]  : 0.0
0.5 Essen / Geo  / Arrival: 06.05.2020 11:32:27 (Europe/Berlin) , Departure: 06.05.2020 11:52:27 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 8.85 [min], 11.69 [km]
```

Route 1 on May 7 visits Heilbronn, Stuttgart and Nuernberg and then ends in Essen. The termination is the drive from Nuernberg to Essen (trimmed to these lines of route 1):

```text
RouteId                : 1
Alternate destination  : true
Defined WorkingHours   : 07.05.2020 08:00:00 - 07.05.2020 17:00:00 (Europe/Berlin)
Effective Route Stop   : 07.05.2020 19:45:41 (Europe/Berlin)
Route time       [min] : 705.69
Termi. time      [min] : 278.53
Termi. distance  [km]  : 367.66
1.2 Nuernberg / Geo  / Arrival: 07.05.2020 14:47:09 (Europe/Berlin) , Departure: 07.05.2020 15:07:09 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 119.28 [min], 157.44 [km]
```

The drive to Essen counts toward the route time, so route 1 ends at 19:45:41, after the end of its working hours at 17:00, and the result shows no violation. Time after the end of the working hours counts against the maximal working time, and the working hours are a frame for matching resources with the opening hours of the nodes, so a route can end after its working hours without a violation. The 705.69 minutes of route 1 stay below Jack's maximal working time of 13 hours.

The KML file draws the line of each route to Essen, but it places the `End - Jack` mark at Jack's start position in Aachen.

## Why alternate destinations are useful

Typical field-service and logistics scenarios:

- **Multi-day tours**: end near the last customer to reduce deadhead time.
- **Hotel/overnight logic**: terminate at a hotel location while still modeling a "home base" for the next day.
- **Shift handover**: terminate at a hub where another resource continues (advanced multi-resource patterns).
- **Regional depots**: start at one depot and end at another.

The key benefit is that the plan reflects operations as they happen, without forcing an artificial "return home within shift hours" pattern.

## Practical considerations

### 1) Validate your KPI interpretation
When you change termination behavior, route totals can be interpreted differently:
- distance/time within WorkingHours,
- termination distance/time,
- "outside WorkingHours" repositioning.

Ensure your reporting aligns with your business interpretation of:
- paid shift time,
- repositioning time,
- overnight travel policy.

### 2) Combine with external distances when accuracy matters
If you rely on fallback distance models, ensure they match your business. For road-based distances/times consider external connections or a customized fallback connector.

### 3) Consider multi-day consistency
This example demonstrates a conceptually "daily" alternate end point but assumes start is still home each morning.  
For true multi-day routing where day N ends at a place and day N+1 starts there, use overnight stays: a working day can end at a stay node, and the next working day starts there (see the [Overnight stay feature guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/overnight_stay)).

## Run it

Run the `main(String[] args)` method of `AlternateDestinationExample` from your IDE. You need a JDK in the version that the examples pom sets. You need no license file: without one, the example uses the public evaluation license (see the [license guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/license)). The run writes `AlternateDestinationExample.kml` into the working directory.

## Related

- [AlternateDestinationExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/alternatedestination/AlternateDestinationExample.java): the source of this example.
- [Basic elements tutorial](https://www.dna-evolutions.com/docs/getting-started/tutorials/basic-elements): the nodes, resources and working hours that this example uses.
- [Overnight stay feature guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/overnight_stay): working days that end at a stay node and start from there the next morning.
- [Export a result to a KML file](../../basic/io_03/Export2KMLExample.md): the KML export in general.
- [Recommended ways to run an optimization](../../basic/recommendedimplementation_02/RecommendedImplementation.md): the recommended run pattern, a synchronous run and the legacy callback methods.
- [Advanced examples](../README.md): the overview of all Advanced examples.

---

Added: 2026-02-17 (JOpt core 7.5.3-j17) | Last updated: 2026-10-23
