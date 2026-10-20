# Export a result to a KML file

`Export2KMLExample` runs an optimization and writes its result to the KML file `myopti.kml`, with one line in its own color for each route. You can open the file in Google Earth or another KML viewer and see the routes on a map. The page is for developers who want to inspect a plan visually. If you have not built an optimization yet, start with the [First optimization example](../firstoptimization_01/FirstOptimizationExample.md).

## What the example shows

The example runs an optimization, prints its events and the result, and writes the result with `EntityKMLExporter` to `myopti.kml`.

## Scenario

The dates, times and coordinates are sample values. The time zone is Europe/Berlin.

### Nodes

The example creates a shared list of opening hours for **two days in March 2020**: March 6 and March 7, each from 08:00 to 17:00.

All nodes have a visit duration of 20 minutes and the importance 1.

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

Two `CapacityResource` objects, Jack and John, start in Aachen. They work on March 6 from 08:00 to 17:00 and on March 7 from 08:00 to 20:00 (`createWorkingHours()`), and may drive 1200 km per route, more than any route of this plan needs. Jack may work 8 hours, less than either shift; John 14 hours, more than both.

### Properties

`addProperties` sets two properties: `JOptExitCondition.JOptGenerationCount` to 2000 and `JOpt.Algorithm.PreOptimization.SA.NumIterations` to 100000. Both are below their defaults, which keeps the run short; the defaults are in the [property reference](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties).

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
- `attachToObservables` subscribes to the progress, warning, status and error events before the run starts. It uses the event API; [Recommended ways to run an optimization](../recommendedimplementation_02/RecommendedImplementation.md) compares it with the legacy callback methods.
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
- The example sets no title. The exporter then names the KML document `Export_` followed by a number that can change between runs. `setTitle` sets a fixed name; [Event nodes for tasks without a location](../eventnode_06/EventNode.md) shows it.

### Beyond the example

This example is minimal. In production or reusable tooling, consider:

1. **Make the output path explicit** (e.g., configurable output directory) rather than relying on the process working directory.
2. **Export on demand**: write a KML file of an intermediate result that you request during the run.

## Output and what to look for

The run prints the status and progress lines, then the result. After the result, the file `myopti.kml` is in the working directory; the run prints no further line when the export works.

In the file, each route of the result is a line in its own color, and the start and end of each route and every node on it are placemarks with their names. Which resource visits which node can change between runs, so the lines on your map may differ from another run.

## Run it

Run the `main` method of `Export2KMLExample` from your IDE. You need a JDK in the version that the examples pom sets. You need no license file: without one, the example uses the public evaluation license (see the [license guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/license)).

## Related

- [Export2KMLExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic/io_03/Export2KMLExample.java): the source of this example.
- [Load and save optimization snapshots](LoadAndSaveOptimization.md): the other examples of this package, which save and load a whole optimization as JSON.
- [Event nodes for tasks without a location](../eventnode_06/EventNode.md): a KML export with a title, and how the file shows a node without a position.
- [Pillar nodes with hard time windows](../pillar_07/Pillar.md): an example that writes its result to a KML file as well.
- [Recommended ways to run an optimization](../recommendedimplementation_02/RecommendedImplementation.md): the recommended run pattern, a synchronous run and the legacy callback methods.
- [Basic examples](../README.md): the overview of all Basic examples.

---

Added: 2026-02-17 (JOpt core 7.5.3-j17) | Last updated: 2026-10-20
