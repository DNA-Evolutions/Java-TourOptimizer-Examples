# Event nodes for tasks without a location

`EventNodeExample` plans a task without a geographical location into a tour of geo nodes: the `EventNode` with the id `Event` takes 20 minutes on Jack's route but adds no driving. A typical example is a customer call or an administrative task that must happen within a time window but does not require travel. The page is for developers who model such work. If you have not built an optimization yet, start with the [First optimization example](../firstoptimization_01/FirstOptimizationExample.md).

## What the example shows

In JOpt, many nodes represent geo locations, for example a `TimeWindowGeoNode` with latitude and longitude. An `EventNode` is different:

- it takes part in the plan like any other node, with opening hours, a visit duration and an importance,
- but it has no geo coordinates.

Conceptually, it represents "work that must be done" rather than "a place to visit".

Typical event nodes are phone calls, paperwork / compliance steps, remote support sessions and internal meetings, and follow-ups that can be done anywhere (within working hours): event nodes are useful when your planning problem includes tasks such as these. These tasks still take time on the route and must be placed in the route timeline, but they add no travel: they do not consume travel distance. This makes event nodes ideal for representing remote tasks that must happen between "real" visits.

### When to use an event node

- **If only the time of a task matters, not its place, model it as an `EventNode`.**
- **Use geo nodes when travel is part of the cost/constraints.** If there is any dependency on distance/time-to-reach, keep it as a geo node (or supply connection data).
- **Be explicit about time windows**, for example: call the customer between 10 and 12. Ensure the opening hours reflect the business requirement (not just broad working hours), otherwise the optimizer places it anywhere inside the opening hours.
- **Watch idle time.** Adding event nodes can reduce idle time (fill gaps between tight windows), but it can also increase complexity. Use result analysis to validate:
  - whether the event nodes are scheduled where intended,
  - whether they create unexpected waiting time due to time-window interaction.

## Scenario

The example builds one resource and nine nodes. The dates, times and coordinates are sample values.

- **Resource:** `Jack`, the one `CapacityResource` that `addResources()` creates, is based in Aachen and starts at 50.775346, 6.083887 (lat/long provided). He works on May 6 and 7, 2020, from 08:00 to 17:00 (Europe/Berlin): these are his working hours. His max working time is 13 hours and his maximal distance 1200 km.
- **Geo nodes:** eight `TimeWindowGeoNode` objects: Koeln, Essen, Dueren, Nuernberg, Heilbronn, Stuttgart, Wuppertal and Aachen.
- **Event node:** one `EventNode` with the id `Event`.
- **All nine nodes** are open on May 6 and 7, 2020, from 08:00 to 17:00, take a visit of 20 minutes and have the same importance.

The maximal working time of 13 hours is longer than the 9 hours of the working hours, so a day may run past 17:00 without overtime. The 1200 km per day are more than the longest day of this plan needs.

The resource still has coordinates, even though the example includes an `EventNode`: resources have geo coordinates because they define the start/end location, so each route starts and ends at Jack's position, and the driving to the first node and back from the last node starts and ends there.

`addProperties` (`addProperties()`) sets two properties, relatively low effort parameters: `JOptExitCondition.JOptGenerationCount` to 1000 and `JOpt.Algorithm.PreOptimization.SA.NumIterations` to 100000; the class does not set `JOpt.Algorithm.PreOptimization.SA.NumRepetitions` and `JOpt.NumCPUCores`. Both are below their defaults, which keeps the run short: this example is configured for demonstration, to keep it quick; the defaults are in the [property reference](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties). Practical meaning: in typical tutorials you may start with smaller values and scale up once modeling is correct.

## How the code works

The source is [EventNodeExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic/eventnode_06/EventNodeExample.java). `example()` runs the steps in order:

```java
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
- `addNodes` adds the eight geo nodes and the event node.
- `addResources` adds Jack.
- `attachToObservables` subscribes to the events of the run.
- `startAndPresentResult` starts the run, waits for the result and prints it.
- `exportToKml` writes the result to a KML file.

### The event node

The event node takes the same opening hours, visit duration and importance as the geo nodes. Only the coordinates are missing:

```java
// The EventNode has opening hours and a visit duration like the other nodes, but no position.
INode event = new EventNode("Event", openingHours, visitDuration, importance);
opti.addElement(event);
```

A geo node of the same example, for comparison:

```java
INode koeln =
    new TimeWindowGeoNode(
        "Koeln", 50.9333, 6.95, openingHours, visitDuration, importance);
opti.addElement(koeln);
```

### Events and the run

`attachToObservables` uses the event API; [Recommended ways to run an optimization](../recommendedimplementation_02/RecommendedImplementation.md) compares it with the legacy callback methods. It subscribes to the progress, warning, status and error subjects of `getOptimizationEvents()` before the run starts, because the subjects replay only a few past events:

```java
// Subscribe before the start: the subjects replay only a few past events.
opti.getOptimizationEvents()
    .progressSubject()
    .subscribe(p -> System.out.println(p.getProgressString()));
opti.getOptimizationEvents().warningSubject().subscribe(w -> System.out.println(w.toString()));
opti.getOptimizationEvents().statusSubject().subscribe(s -> System.out.println(s.toString()));
opti.getOptimizationEvents().errorSubject().subscribe(e -> System.out.println(e.toString()));
```

`startAndPresentResult` starts the run asynchronously and waits up to 5 minutes for the result:

```java
CompletableFuture<IOptimizationResult> resultFuture = opti.startRunAsync();
// Wait for the result; without the wait the JVM would end before the run does.
IOptimizationResult result = resultFuture.get(5, TimeUnit.MINUTES);
System.out.println(result);
return result;
```

### The KML export

The example exports the result container to a KML file in `exportToKml(IOptimizationResult result)`. Its export behavior: it creates an `EntityKMLExporter`, sets the title to `EventNodeExample` (the simple name of the class, written as a literal string) and writes the result, with that title, to a file, `EventNodeExample.kml` in the working directory:

```java
String kmlFile = "EventNodeExample.kml";

EntityKMLExporter kmlExporter = new EntityKMLExporter();
kmlExporter.setTitle("EventNodeExample");

// export flushes the stream but does not close it; the try block closes it.
try (OutputStream out = new FileOutputStream(kmlFile)) {
  if (!kmlExporter.export(result, out)) {
    System.out.println("KML export failed: " + kmlFile);
  }
}
```

The example checks the boolean that `export` returns and prints `KML export failed: EventNodeExample.kml` if it is `false`.

This is valuable for quick inspection of route geometry and ordering. Note that an event node has no position of its own: in the KML file it gets a placemark at the position of the element before it on the route, with its own icon style.

## Output and what to look for

The run prints the status and progress lines, then the result.

In the result, find the line of the node `Event`. It is marked `NonGeo` and shows `Duration: 20.0 [min] / Driving: 0.0 [min], 0.0 [km]`. Its place in the route and its times can change between runs. When it is placed into the plan:

- it consumes **service time** (visit duration) like any other node,
- it is bounded by **opening hours** like any other node,
- but it adds no travel: the node after the event shows the same driving time as when it follows the node before the event directly.

The route of May 7 visits Nuernberg, Stuttgart and Heilbronn. Its route header, trimmed to the three time lines:

```text
Defined WorkingHours   : 07.05.2020 08:00:00 - 07.05.2020 17:00:00 (Europe/Berlin)
Effective Route Start  : 07.05.2020 08:00:00 (Europe/Berlin)
Effective Route Stop   : 07.05.2020 19:58:18 (Europe/Berlin)
```

The route ends after 17:00, and its violation summary is empty: Jack may work up to 13 hours a day.

## Run it

Run the `main` method of `EventNodeExample` from your IDE. You need a JDK in the version that the examples pom sets. You need no license file: without one, the example uses the public evaluation license (see the [license guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/license)). The run writes `EventNodeExample.kml` into the working directory.

To extend the example:

- Add multiple event nodes representing calls, each with its own time window.
- Give them different importance values to influence selection/ordering.
- Combine with result analysis to generate a "call schedule" alongside the physical route.
- Export both route and event tasks to a downstream execution system (driver app / dispatcher UI).

## Related

- [EventNodeExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic/eventnode_06/EventNodeExample.java): the source of this example.
- [Nodes](https://www.dna-evolutions.com/docs/getting-started/tutorials/nodes): the node types and their constructors, `EventNode` among them.
- [Pillar nodes with hard time windows](../pillar_07/Pillar.md): pillar nodes, including the `PillarEventNode`, an event node whose time window must be met.
- [Export a result to a KML file](../io_03/Export2KMLExample.md): the KML export as the subject of its own example.
- [Reactive events and streams](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/reactive_events): the event subjects that `attachToObservables` subscribes to.
- [Recommended ways to run an optimization](../recommendedimplementation_02/RecommendedImplementation.md): the recommended run pattern, a synchronous run and the legacy callback methods.
- [Basic examples](../README.md): the overview of all Basic examples.

---

Added: 2026-02-17 (JOpt core 7.5.3-j17) | Last updated: 2026-10-21
