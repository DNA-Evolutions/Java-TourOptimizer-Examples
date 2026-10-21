# Time-dependent driving times with a connection store

`ConnectionStoreExample` gives every connection between two places of a small plan two traffic profiles, one for weekdays and one for the weekend, so the same drive takes longer in the Friday rush hour than on a Sunday morning. The core turns the few time points of a profile into one driving time per hour. The page is for developers who model traffic by day and time of day; how JOpt computes distances and durations without such data is in the [backup connector feature guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/backupconnector#description-how-jopttouroptimizer-calculates-distances-and-times).

## What the example shows

Many routing problems are not only "where are the nodes?" but also "**when** do we drive between them?".
Traffic patterns can dominate route quality:

- morning and evening rush hours,
- weekend vs weekday profiles,
- time-of-day dependent congestion,
- predictable slowdowns around schools, industrial zones, or city centers.

JOpt supports this using a **time-dependent connection store**: a connection (edge) can carry a **bucket of timed overrides** that adjust driving time (and optionally distance) based on:
- **time of day**
- **day of week / day ranges**

This example shows a pragmatic way to approximate traffic using a few time grid points; the core fills the hours between two points with the **mean** of the two.

- **`ConnectionStoreExample`** builds the connections between all elements itself: a flat-earth distance and a base driving time, and two buckets per connection, one for Monday to Friday and one for Saturday and Sunday.
- It plans one resource on a Friday and on a Sunday, so each profile applies on one of the two days, and prints the optimization as JSON after the result, with the stored driving times of every connection.
- It prints the events of the run through the event API, the recommended way.

## Scenario

The dates, times and coordinates are sample values. The time zone is Europe/Berlin.

- **Resource:** `Jessi from Aachen`, a `CapacityResource` at 50.775346, 6.083887 (Aachen), with working hours on Friday, February 19, and Sunday, February 21, 2100, from 08:00 to 17:00. She may work up to 9 hours and drive up to 1200 km a day.
- **Nodes:** five `TimeWindowGeoNode` objects with 60-minute visits and the same importance. Koeln, Essen and Dueren are open on both days from 08:00 to 17:00; `KoelnWeek` is open only on the Friday and `KoelnWeekend` only on the Sunday. The three Koeln nodes share one point.
- **Connections:** every pair of elements gets a connection with the flat-earth distance and a base driving time at a fixed sample speed (`speedMeterPerSecond`). Every connection between two different points carries the two traffic profiles.

## How the code works

The source is [ConnectionStoreExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/connectionstore/ConnectionStoreExample.java). `example()` adds the connections after the nodes and the resource, and prints the JSON after the result:

```java
// The public evaluation license, or your own license if you put a license file where
// ExampleLicenseHelper looks for it.
ExampleLicenseHelper.setLicense(this);
addProperties(this);
List<IOptimizationElement> elements = new ArrayList<>();
elements.addAll(addNodes(this));
elements.addAll(addResources(this));
addConnections(this, elements);
attachToObservables(this);
startAndPresentResult(this);
printAsJson(this);
```

`addProperties` sets `JOptExitCondition.JOptGenerationCount` and `JOpt.Algorithm.PreOptimization.SA.NumIterations` below their defaults, which keeps the run short. `addConnections` puts the connections of `createConnections` into a `NodeEdgeConnector` and sets it on the optimization. For each connection between two different points, `createConnections` attaches the buckets:

```java
// The core turns the points of both buckets into one driving time per hour, and it
// sets the connection's own distance and driving time to the mean of all points.
TimedNodeConnectorItem.setTimedBucketData(connection,
    createConnectionBucket(connection));
```

`createConnectionBucket` builds the two buckets from the base distance and driving time of the connection:

```java
ConnectionBucket weekendBucket =
    new ConnectionBucket(
        RangeDef.of(DayDef.of(DayOfWeek.SATURDAY), DayDef.of(DayOfWeek.SUNDAY)),
        createTimedWeekendConnections(distance, driving));

ConnectionBucket weekBucket =
    new ConnectionBucket(
        RangeDef.of(DayDef.of(DayOfWeek.MONDAY), DayDef.of(DayOfWeek.FRIDAY)),
        createTimedWeekdayConnections(distance, driving));
```

Each point of a profile is a `TimedConnectionData` with a time of day, the base distance and the base driving time times a multiplier:

```java
return new TimedConnectionData(
    time, distance, Duration.ofMillis(Math.round(driving.toMillis() * multiplier)));
```

How the core uses the points:

- It keeps one driving time and one distance per hour of each bucket. A point sets the values of its hour; two points in the same hour are averaged.
- The hours between two points get the mean of the two points, the same value for each of those hours.
- The hours before the first point and after the last point of a bucket get the mean of the first and the last point.
- A drive takes the value of the hour in which it departs, on the day of the bucket that contains the departure day, in the time zone of the first opening or working hours of the element where the drive starts.
- `setTimedBucketData` also sets the connection's own distance and driving time to the mean of all points of all its buckets.

`printAsJson` calls `ResultJsonPrinter.printResultAsJson(opti, true)`: the JSON leaves out the solution and the build options and keeps the connections.

## Output and what to look for

In two test runs the result had two routes, both inside the working hours: the Friday route ends at 13:25:46, the Sunday route at 12:53:02. The Friday route starts with the two nodes at the Koeln point, `KoelnWeek` and `Koeln`, whose order can change between runs. The drive from Aachen at 08:00 to the first of them takes 86.37 minutes for 63.34 km, 1.8 times the base driving time of 47.98 minutes. The Friday route continues to Dueren, departing in the 11:00 hour:

```text
0.2 Dueren / Geo  / Arrival: 19.02.2100 12:04:25 (Europe/Berlin) , Departure: 19.02.2100 13:04:25 (Europe/Berlin) / Duration: 60.0 [min] / Driving: 38.06 [min], 35.88 [km]
```

The 38.06 minutes are about 1.4 times the base of 27.18 minutes: the mean of 1.8 (09:00) and 1.0 (13:00). On the Sunday the same first drive takes 50.38 minutes, 1.05 times the base:

```text
1.0 KoelnWeekend / Geo  / Arrival: 21.02.2100 08:50:23 (Europe/Berlin) , Departure: 21.02.2100 09:50:23 (Europe/Berlin) / Duration: 60.0 [min] / Driving: 50.38 [min], 63.34 [km]
1.1 Essen / Geo  / Arrival: 21.02.2100 10:36:12 (Europe/Berlin) , Departure: 21.02.2100 11:36:12 (Europe/Berlin) / Duration: 60.0 [min] / Driving: 45.82 [min], 57.61 [km]
```

The JSON after the result lists every connection. The connection from Dueren to Essen, trimmed to its first lines:

```json
    "fromElementId" : "Dueren",
    "toElementId" : "Essen",
    "type" : {
      "distance" : "81362.85877779142 m",
      "time" : "PT1H14M50.806S",
```

Its base driving time is 3698.311 seconds; the `time` of 4490.806 seconds is the mean of all 14 points of the two buckets. Below the positions, `connectionByTime` holds the two day ranges (`timeDefinitions`) and 48 values in `timeMillis` and in `distanceMeters`: 24 hours of the weekend bucket, then 24 hours of the weekday bucket. The weekday hours of this connection, as factors of the base driving time:

| Hours | Factor | Where it comes from |
|---|---:|---|
| 00:00 to 06:00 | 1.0 | before the first point: the mean of 06:00 and 20:00, and the 06:00 point |
| 07:00 to 09:00 | 1.8 | the points at 07:00 and 09:00 and the hour between them |
| 10:00 to 12:00 | 1.4 | the mean of 09:00 and 13:00 |
| 13:00 | 1.0 | the point at 13:00 |
| 14:00 and 15:00 | 1.3 | the mean of 13:00 and 16:00 |
| 16:00 to 19:00 | 1.6 | the points at 16:00 and 19:00 and the hours between them |
| 20:00 to 23:00 | 1.0 | the point at 20:00, after it the mean of 06:00 and 20:00 |

The distances stay at the base distance in every hour.

## What a Connection Store is (conceptual)

In JOpt, the optimizer reasons about **node-to-node connections** (distance + driving duration).
A connection store extends this by allowing a single edge to expose *multiple* driving durations depending on departure time.

Think of it as a "traffic overlay" on top of a base distance/time:

- Base connection: "A to B is 12.3 km and normally 18 minutes"
- Timed overrides: "At 08:30 on weekdays it behaves like 32 minutes"

The benefit is that the optimizer can:
- schedule visits *and* choose sequencing while being aware that travel time changes over the day,
- and therefore avoid routes that look good at 03:00 but fail at 08:00.

## Structure used by the example

The example builds time-dependent connections using:

- `NodeEdgeConnectorItem`: the actual edge (distance + base driving time)
- `ConnectionBucket`: a container holding timed profiles
- `TimedConnectionData`: a "time grid point" (time to distance/time override)

Two day-range profiles are attached per connection:

1. **Weekend profile** (Saturday to Sunday)
2. **Weekday profile** (Monday to Friday)

Between specified times, the core stores one value per hour and gives the hours between two points the mean of the two. Between 07:00 and 09:00 both points are 1.8, so these hours are 1.8; between 09:00 (1.8) and 13:00 (1.0) the hours 10:00 to 12:00 are 1.4. This yields a stepped "traffic curve" without requiring a full minute-by-minute table.

## Traffic profiles in this example

The example models a base driving time, then applies multipliers by time of day. The multipliers are sample values.

### Weekday profile (Mon to Fri)

| Time | Multiplier | Interpretation |
|---|---:|---|
| 06:00 | 1.0 | normal traffic |
| 07:00 and 09:00 | 1.8 | morning rush hour |
| 13:00 | 1.0 | midday normal |
| 16:00 and 19:00 | 1.6 | evening rush hour |
| 20:00 | 1.0 | night normal |

### Weekend profile (Sat to Sun)

| Time | Multiplier | Interpretation |
|---|---:|---|
| 06:00 | 1.0 | normal traffic |
| 07:00 and 09:00 | 1.05 | light morning delay |
| 13:00 | 1.0 | midday normal |
| 16:00 and 19:00 | 1.05 | light evening delay |
| 20:00 | 1.0 | night normal |

These profiles are created in:
- `createTimedWeekdayConnections(...)`
- `createTimedWeekendConnections(...)`

## How the bucket is attached to a connection

At a high level, the example does:

1. Create a base connection between nodes (distance + base duration)
2. Create a `ConnectionBucket` for that connection
3. Fill the bucket with timed profiles for day ranges
4. Attach the bucket to the connection

This is implemented using:
- `createConnectionBucket(connection)`
  which constructs the buckets for weekday and weekend ranges,
- and `TimedNodeConnectorItem.setTimedBucketData(connection, ...)`, which attaches them.

The day ranges are expressed with:
- `RangeDef.of(DayDef.of(DayOfWeek.MONDAY), DayDef.of(DayOfWeek.FRIDAY))`
- `RangeDef.of(DayDef.of(DayOfWeek.SATURDAY), DayDef.of(DayOfWeek.SUNDAY))`

## Practical modeling guidance

### Start with a small number of time grid points
You generally do not need hundreds of time entries. A few grid points per profile are often enough:
- pre-rush,
- rush start,
- rush end,
- midday,
- evening rush start,
- evening rush end,
- night.

The optimizer benefits from the signal ("rush hour is expensive") without being burdened by ultra-detailed data. The core keeps one value per hour in any case: points in the same clock hour are averaged.

### Calibrate multipliers to match your region
Start with conservative multipliers and adjust based on observed delays:
- urban cores can exceed the multipliers of this example during peak times,
- weekend multipliers can be near 1.0 in business districts but higher near leisure destinations.

### Decide what is time-dependent: duration only vs duration + distance
In most cases:
- distance stays constant,
- duration varies.

Some deployments also vary distance (e.g., rerouting around city centers).
The API supports both; this example demonstrates adjusting the duration (and keeps the concept extendable).

### Combine with OpeningHours and WorkingHours
Traffic-aware edges become especially valuable when used with time constraints:
- morning peak can push you into lateness,
- evening peak can create overtime risk,
- weekend hours may create different feasible plans.

The example includes nodes with weekday-only and weekend-only opening hours to illustrate this interplay.

## Operational benefits

### Debugging and model verification
Time-dependent connections make it easier to explain "why the optimizer avoided that route":
- a plan that crosses the city at 08:30 is objectively slower than at 11:00.

### End user acceptance
Planners often distrust optimizers when they "ignore traffic reality".
A traffic-aware connection store makes results much more intuitive:
- less "impossible timing",
- fewer routes that look feasible only on paper,
- fewer ad-hoc manual corrections post-optimization.

### Incremental adoption
You can start with:
- one profile (weekday), then extend to weekend,
- a small set of critical corridors (city center edges), then expand.

## Exporting and inspection

The example prints a JSON export of the configuration (without solution/build data).
This is useful for:
- verifying that your connection bucket is present,
- checking how the timed points are serialized: as one driving time and one distance per hour of each bucket (`timeMillis`, `distanceMeters`) with the day ranges (`timeDefinitions`),
- and creating test fixtures for regression testing.

## Summary

- A **Connection Store** makes driving time depend on **time of day** and **day of week**, enabling realistic traffic-aware routing.
- The example uses a few time grid points, which the core turns into one value per hour with the mean between two points, to model weekday rush hours and mild weekend effects.
- This improves feasibility, solution realism, debugging transparency, and planner acceptance.

## Run it

Run the `main(String[] args)` method of `ConnectionStoreExample` from your IDE. You need a JDK in the version that the examples pom sets. You need no license file: without one, the example uses the public evaluation license (see the [license guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/license)). The JSON after the result is long: one block per connection.

## Related

- [Backup connector feature guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/backupconnector#description-how-jopttouroptimizer-calculates-distances-and-times): additional conceptual background on how JOpt computes distances and durations when no connection is given.
- [Custom distances and driving times with a node connector](../../basic/connection_04/ExternalNodeConnection.md): connections from your own source, without time profiles.
- [Reactive events and streams](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/reactive_events): the feature guide of the event API that `attachToObservables` uses.
- [Recommended ways to run an optimization](../../basic/recommendedimplementation_02/RecommendedImplementation.md): the recommended run pattern, a synchronous run and the legacy callback methods.
- [Expert examples](../README.md): the overview of all Expert examples.

---

Added: 2026-02-17 (JOpt core 7.5.3-j17) | Last updated: 2026-10-21
