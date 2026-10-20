# Custom distances and driving times with a node connector

A node connector gives JOpt the distance and the driving time between two elements from your own source, and JOpt fills in every pair that you leave out. In these examples, a connection of 5 km between Koeln and Oberhausen, two cities about 60 km apart in a straight line, makes the given values easy to find in the result. The page is for developers who bring in values from their own routing source. If you have not run an example yet, start with the [First optimization example](../firstoptimization_01/FirstOptimizationExample.md).

## What the example shows

Two classes show the node connector:

- [ExternalNodeConnectionExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic/connection_04/ExternalNodeConnectionExample.java) sets one connection in each direction between Koeln and Oberhausen. All other pairs of elements get their values from the default backup connector.
- [ExternalNodeConnectionWithLocationIdExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic/connection_04/ExternalNodeConnectionWithLocationIdExample.java) does the same with location ids: Koeln and Koeln1 share a location id, so the connection given for Koeln also serves Koeln1.

### Why external connections matter

For many problems, JOpt's built-in "auto connection" behavior is good enough: you provide nodes/resources with geo-coordinates and time windows and the optimizer can infer missing connections. However, in real routing use-cases you often need:

- **Road network** distances instead of straight-line distances
- Travel times depending on **traffic**, **vehicle type**, **terrain**, or **service policies**
- A precomputed matrix coming from:
  - OSRM / Valhalla / GraphHopper,
  - Google Distance Matrix,
  - internal GIS systems,
  - company-specific routing engines.

In those cases, you can inject **external edge data** by defining explicit connections for selected (or all) pairs of elements.

### ExternalNodeConnectionExample

**Intent (from `toString()`):** "Sets the distance and the driving time between two nodes with a node connector."

The example builds the nodes and the resources, creates two connections between Koeln and Oberhausen (one in each direction), puts them into a `NodeEdgeConnector` and hands the connector to the optimization. Everything else (all other pairs) is computed via fallback behavior. After the run, the example prints the result and writes it to a KML file.

#### Why this is a good starter pattern

- "Here are the edges I care about or got from an external system."
- "For all other edges, do something reasonable."

#### Typical real-world usage

- Provide external edges for:
  - depot to customer and customer to depot (one item for each direction),
  - high-priority customers,
  - cross-city edges (where straight-line is especially misleading),
  - known restricted segments.
- Leave the rest to fallback to reduce integration complexity.

### ExternalNodeConnectionWithLocationIdExample

**Intent (from `toString()`):** "Sets the distance and the driving time between two locations with a node connector and location ids, so that every node at a location uses the connections of that location."

#### Why you would use location IDs

Location IDs are a practical tool when your "logical nodes" are not all distinct physical points, for example:

- multiple tasks at the same customer site,
- multiple deliveries at one facility,
- clustered tasks that should share travel-time semantics.

In addition, location IDs can reduce the number of distinct "places" your external matrix must cover (depending on how you structure your integration), because multiple nodes map to one physical location identity.

#### When to be careful

If you assign the same `locationId` to nodes that do **not** share the same latitude/longitude, you create ambiguity:

- explicit connections might be applied unexpectedly,
- fallback distances might be computed from an inconsistent geo position.

The class comment of the example warns about this, and the example only shares a location id between nodes with identical coordinates.

## Scenario

Both classes use the same data:

- **Nodes:** eight nodes in Germany: Koeln, Koeln1, Oberhausen, Essen, Heilbronn, Stuttgart, Wuppertal and Aachen. Koeln and Koeln1 have the same coordinates. Every node is open on March 6 and 7, 2020, from 08:00 to 17:00, a visit takes 20 minutes, and all nodes have the same importance.
- **Resources:** Jack and John start in Aachen. Both work on March 6 from 08:00 to 17:00 and on March 7 from 08:00 to 20:00. They differ only in their maximal working time: 8 hours for Jack and 14 hours for John. The maximal distance of 1200 km does not limit any route of the example.
- **Connections:** one connection in each direction between Koeln and Oberhausen, with 5 km and 10 minutes. The connection is artificial on purpose: in a straight line the two cities are about 60 km apart, so the given values are easy to spot in the result.
- **Time zone and data:** Europe/Berlin. The dates, the coordinates and the connection values are sample values. The examples set no optimization properties.

## How the code works

The code is in [ExternalNodeConnectionExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic/connection_04/ExternalNodeConnectionExample.java) and [ExternalNodeConnectionWithLocationIdExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic/connection_04/ExternalNodeConnectionWithLocationIdExample.java).

### The steps

`example()` of `ExternalNodeConnectionExample`:

```java
public void example()
    throws InterruptedException, ExecutionException, TimeoutException, InvalidLicenceException,
    IOException {
  // The public evaluation license, or your own license if you put a license file where
  // ExampleLicenseHelper looks for it.
  ExampleLicenseHelper.setLicense(this);
  addNodes(this);
  addResources(this);
  attachToObservables(this);
  IOptimizationResult result = startAndPresentResult(this);
  exportToKml(result);
}
```

`addNodes` adds the eight nodes and then calls `addNodeConnections` with the nodes Koeln and Oberhausen. `ExternalNodeConnectionWithLocationIdExample` has the same steps without the KML file.

### Connections are directed edges

A `NodeEdgeConnectorItem` (via `INodeConnectorItem`) represents:

- a directed edge from **element A to element B**
- **distance** (unit-aware quantity)
- **driving time** (`Duration`)

Important: connections are generally **directional** (A to B is not assumed to equal B to A). The Basic Elements tutorial explicitly demonstrates creating both directions.
Practical implication: if you want symmetry, you must provide both edges yourself.

### The connection items

`addNodeConnections` creates one item for each direction. Each item names the from element and the to element, the distance as a quantity and the driving time as a `Duration`. The method then puts both items into a `NodeEdgeConnector` and hands the connector to the optimization with `setNodeConnector`:

```java
private static void addNodeConnections(IOptimization opti, INode koeln, INode oberhausen) {
  // Artificial on purpose: the straight line is about 60 km, so the given edge is easy to spot.
  Quantity<Length> distance = Quantities.getQuantity(5, KILO(METRE));
  Duration drivingTime = Duration.ofMinutes(10);

  INodeConnectorItem koelnToOberhausen = new NodeEdgeConnectorItem();
  koelnToOberhausen.setFromOptimizationElement(koeln);
  koelnToOberhausen.setToOptimizationElement(oberhausen);
  koelnToOberhausen.setDistance(distance);
  koelnToOberhausen.setDrivingTime(drivingTime);

  // A connection has a direction, so the way back needs an item of its own.
  INodeConnectorItem oberhausenToKoeln = new NodeEdgeConnectorItem();
  oberhausenToKoeln.setFromOptimizationElement(oberhausen);
  oberhausenToKoeln.setToOptimizationElement(koeln);
  oberhausenToKoeln.setDistance(distance);
  oberhausenToKoeln.setDrivingTime(drivingTime);

  // Every other pair gets its values from the default backup connector: the straight-line
  // distance of a flat-earth approximation, and the driving time at the average speed of the
  // resource (setAvgSpeed).
  INodeEdgeConnector connector = new NodeEdgeConnector();
  connector.putNodeConnections(List.of(koelnToOberhausen, oberhausenToKoeln));
  opti.setNodeConnector(connector);
}
```

### Explicit connections and the fallback

When you set a node connector, the optimizer uses a layered strategy:

#### Layer 1: explicit connections you provide

If a connection for a pair exists in your `NodeEdgeConnector`, it is used as-is.

This is how you "induce" external travel times/distances.

#### Layer 2: fallback for missing connections

If a connection is **missing**, the optimizer can auto-calculate it.

By default (per docs), the fallback distance uses a flat-surface earth approximation (projected model), and driving time is derived using an average speed; the BackupConnector documentation explains both the default model and how to override it.

#### The default backup connector

A `NodeEdgeConnector` created with `new NodeEdgeConnector()` uses a `DefaultFlatEarthAverageSpeedBackupElementConnector`, an implementation of `IBackupElementConnector`. For every pair without an item, it calculates the straight-line distance of a flat-earth approximation from the coordinates of the two elements, and the driving time from that distance and the average speed of the resource. The average speed is a property of the resource, set with `setAvgSpeed`. A different backup connector can be passed to the constructor `NodeEdgeConnector(IBackupElementConnector)`.

When you do **not** supply all connections, fallback kicks in. The BackupConnector documentation describes:

- Default distance calculation: a flat-surface earth approximation (efficient; usually accurate enough for short edges in optimized tours).
- Default driving time: derived from an assumed average speed (the docs mention a default average speed and show how to set a per-resource average speed).
- How to override/adjust the calculation by providing a custom `IBackupElementConnector` implementation.

Practical recommendation:

- If you only provide a partial external matrix, consider whether fallback should be:
  1) "good enough" straight-line + average speed, or
  2) replaced with a custom BackupConnector (e.g., correction factor, haversine, custom terrain model).

### Location ids

`ExternalNodeConnectionWithLocationIdExample` gives Koeln and Koeln1 the location id `TestLocationId1`:

```java
// Koeln and Koeln1 share their position and therefore a location id. Every other node keeps
// its id as its location id.
INode koeln =
    new TimeWindowGeoNode(
        "Koeln", 50.9333, 6.95, openingHours, visitDuration, importance);
koeln.setLocationId("TestLocationId1");
opti.addElement(koeln);

INode koeln1 =
    new TimeWindowGeoNode(
        "Koeln1", 50.9333, 6.95, openingHours, visitDuration, importance);
koeln1.setLocationId("TestLocationId1");
opti.addElement(koeln1);
```

All other nodes have no explicit location id; for them, the element id is used as location id.

The class Javadoc explains the key semantics:

- Elements sharing a `locationId` effectively share the same position.
- You must ensure that all elements sharing a `locationId` also share the same geo-coordinates. This becomes important when fallback calculations rely on geo coordinates.

Connections are stored by location id. The explicit connections Koeln to Oberhausen and Oberhausen to Koeln are therefore also used for Koeln1 to Oberhausen and Oberhausen to Koeln1.

### The KML file

`startAndPresentResult` returns the result, and `example()` passes it to `exportToKml`:

```java
private static void exportToKml(IOptimizationResult result) throws IOException {
  String name = ExternalNodeConnectionExample.class.getSimpleName();
  File file = new File(name + ".kml");

  EntityKMLExporter kmlExporter = new EntityKMLExporter();
  kmlExporter.setTitle(name);

  try (OutputStream out = new FileOutputStream(file)) {
    if (!kmlExporter.export(result, out)) {
      System.out.println("KML export failed: " + file);
    }
  }
}
```

The file is named after the class and written to the working directory. The stream is opened in try-with-resources, so it is closed after the export. `export` returns a boolean, and the example prints a line if it is false.

### Events and the wait

`attachToObservables` subscribes to the progress, warning, status and error events before the run starts, because the subjects replay only a few past events. `startAndPresentResult` starts the run with `startRunAsync()` and waits for the result for up to 5 minutes; without the wait, the JVM would end before the run does. The event API is recommended. The legacy callback methods (`onProgress`, `onAsynchronousOptimizationResult` and the others) subscribe to the same events internally and stay supported.

### A recommended implementation strategy

#### Step 1: decide your external data scope

Choose one:

- **Full matrix** (highest integration cost).
- **Partial matrix** + fallback.
- **No matrix** but custom BackupConnector (low integration).

#### Step 2: generate directed edges

Even if your external system returns symmetric values, explicitly create both directions.

#### Step 3: use stable identifiers

If you use `locationId`, ensure it is:

- stable,
- consistent across runs,
- consistent with your geo-coordinate assignment.

#### Step 4: validate and harden

Before running optimization:

- Verify every external edge has:
  - non-negative distance,
  - non-negative driving time,
  - correct units,
  - correct "from/to" direction.

### Summary

- Use `NodeEdgeConnectorItem` to inject external **distance** and **driving time** data.
- Treat connections as **directed** (define both directions when needed).
- Missing connections fall back to **auto-calculated** values; this fallback can be customized via **BackupConnector**.
- `locationId` is a concept to represent multiple tasks at the same physical position: use it carefully and consistently.

## Output and what to look for

### Output of ExternalNodeConnectionExample

On March 6, Jack's route holds only Aachen (20 minutes), and John's route holds the seven other nodes (705.96 minutes, 738.86 km). The routes of March 7 stay empty. The long route fits into John's 14 hours. The route items of John, trimmed from the printed result, are the same in both runs:

```text
2.0 Koeln1 / Geo  / Arrival: 06.03.2020 08:47:59 (Europe/Berlin) , Departure: 06.03.2020 09:07:59 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 47.99 [min], 63.34 [km]
2.1 Koeln / Geo  / Arrival: 06.03.2020 09:07:59 (Europe/Berlin) , Departure: 06.03.2020 09:27:59 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 0.0 [min], 0.0 [km]
2.2 Oberhausen / Geo  / Arrival: 06.03.2020 09:37:59 (Europe/Berlin) , Departure: 06.03.2020 09:57:59 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 10.0 [min], 5.0 [km]
2.3 Essen / Geo  / Arrival: 06.03.2020 10:06:50 (Europe/Berlin) , Departure: 06.03.2020 10:26:50 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 8.85 [min], 11.69 [km]
2.4 Wuppertal / Geo  / Arrival: 06.03.2020 10:44:34 (Europe/Berlin) , Departure: 06.03.2020 11:04:34 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 17.74 [min], 23.41 [km]
2.5 Heilbronn / Geo  / Arrival: 06.03.2020 14:33:15 (Europe/Berlin) , Departure: 06.03.2020 14:53:15 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 208.69 [min], 275.47 [km]
2.6 Stuttgart / Geo  / Arrival: 06.03.2020 15:24:46 (Europe/Berlin) , Departure: 06.03.2020 15:44:46 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 31.52 [min], 41.6 [km]
```

Look for the leg into Oberhausen: 10.0 minutes and 5.0 km, the values of the connection from Koeln. In both runs, Koeln comes right before Oberhausen. All other legs carry fallback values, for example 63.34 km and 47.99 minutes from Aachen to Koeln1. Koeln and Koeln1 have the same coordinates, so the leg between them is 0.0 km.

The run also writes `ExternalNodeConnectionExample.kml`, which shows the routes on a map in a KML viewer.

### Output of ExternalNodeConnectionWithLocationIdExample

The route is the same as in `ExternalNodeConnectionExample`, except that the order of Koeln and Koeln1 can change between runs. The leg into Oberhausen shows the given values in either order, because the connection is stored for the location id that both nodes share. The leg between Koeln and Koeln1 is 0.0 minutes and 0.0 km. The line for Oberhausen is the same in both runs:

```text
2.2 Oberhausen / Geo  / Arrival: 06.03.2020 09:37:59 (Europe/Berlin) , Departure: 06.03.2020 09:57:59 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 10.0 [min], 5.0 [km]
```

### Practical thought experiments to validate your integration

1. **Asymmetry test**  
   Make A to B short and B to A long; verify the resulting tour order changes accordingly.

2. **Fallback boundary test**  
   Provide external edges for only 10% of pairs. Compare results with:
   - default fallback,
   - a custom BackupConnector (e.g., scaled distances),
   to measure sensitivity.

3. **LocationId consolidation test**  
   Create multiple nodes at one physical location (same `locationId`) and observe whether:
   - the solution becomes more "grouped" around those clustered tasks.

## Run it

- Open the project in your IDE and run the `main` method of `ExternalNodeConnectionExample` or `ExternalNodeConnectionWithLocationIdExample`.
- You need a JDK in the version that the examples pom sets.
- You need no license: without a license file, the examples use the public evaluation license. The [license guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/license) explains the license and its element limits.
- No other example needs to run first. `ExternalNodeConnectionExample` writes `ExternalNodeConnectionExample.kml` to the working directory.

## Related

- [BackupConnector feature guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/backupconnector#description-how-jopttouroptimizer-calculates-distances-and-times): how JOpt calculates the distances and driving times of missing connections, and how to change the calculation.
- [Basic elements tutorial](https://www.dna-evolutions.com/docs/getting-started/tutorials/basic-elements): nodes, resources and element connections, with a connection in each direction.
- [Export2KMLExample](../io_03/Export2KMLExample.md): writing a result to a KML file.
- [Recommended ways to run an optimization](../recommendedimplementation_02/RecommendedImplementation.md): the recommended run pattern, a synchronous run and the legacy callback methods.
- [Reactive events and streams](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/reactive_events): the event API that `attachToObservables` uses.
- [Base examples](../README.md): the overview of all Base examples.

---

Added: 2026-02-17 (JOpt core 7.5.3-j17) | Last updated: 2026-10-20
