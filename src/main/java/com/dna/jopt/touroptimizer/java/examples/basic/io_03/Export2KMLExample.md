# Export a result to a KML file

`Export2KMLExample` runs an optimization and writes its result to the KML file `myopti.kml`, with one line in its own color for each route. You can open the file in Google Earth or another KML viewer and see the routes on a map. The page is for developers who want to inspect a plan visually. If you have not built an optimization yet, start with the [First optimization example](../firstoptimization_01/FirstOptimizationExample.md).

## What the example shows

This example demonstrates a "last mile" step after solving a tour optimization: **exporting the resulting optimization container to a KML file** so you can visualize routes and locations in map tools such as **Google Earth** or other KML-capable viewers.

What you get when you run it:

1. The optimizer runs asynchronously.
2. The progress, warning, status and error events of the run are printed to stdout.
3. When the run finishes, the example prints the result.
4. The example exports the **result container** to `myopti.kml` via `EntityKMLExporter`.

The flow is: solve, receive the result, export, open in a map tool.

## Scenario

The dates, times and coordinates are sample values. The time zone is Europe/Berlin.

### Nodes

The example creates a shared list of opening hours for **two days in March 2020**: March 6 and March 7, each from 08:00 to 17:00.

All nodes share:
- `visitDuration = 20 minutes`
- importance is set to `1`

Nodes added (as `TimeWindowGeoNode`):
- Koeln
- Koeln1 (same coordinates as Koeln)
- Oberhausen
- Essen
- Heilbronn
- Stuttgart
- Wuppertal
- Aachen

### Resources

Two resources are created as `CapacityResource`, both starting at Aachen coordinates:

- **Jack**
  - max working time: 8 hours
- **John**
  - max working time: 14 hours

Shared constraints:
- max distance per route: **1200 km**
- working hours are defined by `createWorkingHours()` (two days; the second day extends to 20:00)

The 1200 km are more than the longest route of this plan needs. Jack's 8 hours are less than his shifts (9 hours and 12 hours); John's 14 hours are more than both.

### Properties

`addProperties` registers a small set of properties that define solver effort and runtime behavior: `JOptExitCondition.JOptGenerationCount` set to 2000 and `JOpt.Algorithm.PreOptimization.SA.NumIterations` set to 100000. Both are below their defaults, which keeps the run short; the defaults are in the [property reference](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties).

## How the code works

The source is [Export2KMLExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic/io_03/Export2KMLExample.java). `main(...)` instantiates the example and calls `example()`. The orchestration is compact:

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
- `addProperties`, `addNodes` and `addResources` register the properties and the problem elements.
- `attachToObservables` subscribes to the progress, warning, status and error events with the recommended event API, before the run starts. The event API is recommended. The legacy callback methods (`onProgress`, `onAsynchronousOptimizationResult` and the others) subscribe to the same events internally and stay supported; this example does not use them.
- `startAndPresentResult` starts the run with `startRunAsync()` and waits up to 5 minutes for the result; without the wait the JVM would end before the run does. It prints the result and returns it.
- `exportToKml` writes the result to the file.

### The export

```java
String kmlFile = "myopti.kml";

EntityKMLExporter kmlExporter = new EntityKMLExporter();

// export flushes the stream but does not close it; the try block closes it.
try (OutputStream out = new FileOutputStream(kmlFile)) {
  if (!kmlExporter.export(result, out)) {
    System.out.println("KML export failed: " + kmlFile);
  }
}
```

- `export(result, out)` takes the `IOptimizationResult` of the run and writes the routes of its entity container.
- `export` flushes the stream but does not close it, so the example opens the stream in a try-with-resources block, which closes it.
- The example checks the boolean that `export` returns and prints `KML export failed: myopti.kml` if it is `false`.
- The example sets no title. The exporter then names the KML document `Export_` followed by a number that can change between runs. `setTitle` sets a fixed name; [EventNode.md](../eventnode_06/EventNode.md) shows it.

The export uses the container that holds the solved state.

### Beyond the example

This example is minimal. In production or reusable tooling, consider:

1. **Make the output path explicit** (e.g., configurable output directory) rather than relying on the process working directory.
2. **Export on demand**:
   - e.g., export only when a run is "good enough", or export multiple snapshots (best-of) during progress callbacks.

## Output and what to look for

The run prints the status and progress lines, then the result. After the result, the file `myopti.kml` is in the working directory; the run prints no further line when the export works.

Once generated, open `myopti.kml` in a KML viewer (for example, Google Earth) to visually inspect:
- node locations,
- computed tours/routes (depending on exporter capabilities and result structure).

In the file, each route of the result is a line in its own color, and the start and end of each route and every node on it are placemarks with their names. Which resource visits which node can change between runs, so the lines on your map may differ from another run.

## Run it

Run the `main` method of `Export2KMLExample` from your IDE. You need a JDK in the version that the examples pom sets. You need no license file: without one, the example uses the public evaluation license (see the [license guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/license)). After completion, check your process working directory for `myopti.kml`.

## Related

- [Export2KMLExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic/io_03/Export2KMLExample.java): the source of this example.
- [LoadAndSaveOptimization.md](LoadAndSaveOptimization.md): the other examples of this package, which save and load a whole optimization as JSON.
- [EventNode.md](../eventnode_06/EventNode.md): a KML export with a title, and how the file shows a node without a position.
- [Pillar.md](../pillar_07/Pillar.md): an example that writes its result to a KML file as well.
- [Recommended ways to run an optimization](../recommendedimplementation_02/RecommendedImplementation.md): the recommended run pattern, a synchronous run and the legacy callback methods.
- [Base examples](../README.md): the overview of all Base examples.

---

Added: 2026-02-17 (JOpt core 7.5.3-j17) | Last updated: 2026-10-20
