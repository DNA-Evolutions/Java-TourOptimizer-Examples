# Load a snapshot and add a pillar

This example loads a saved optimization state from `myopti.json.bz2` and adds one **pillar node** (a fixed appointment) to it before the next run. It shows how a planned state can take a new appointment with a strict time window without rebuilding the model: the pillar `Koeln-Pillar` goes into the loaded solution through `addReassignNodes`. The page is for developers who add fixed appointments to a plan that is already optimized. The snapshot comes from `SaveOptimizationToJsonExample`, which [Load and save optimization snapshots](LoadAndSaveOptimization.md) describes; run it first.

## What the example shows

- **Load:** the importer reads the nodes, the resources, the properties and the solution of the snapshot into the optimization. The next run starts from the loaded solution.
- **Reassign:** `addReassignNodes(...)` hands the pillar to the loaded optimization, so the next run plans it together with the elements from the snapshot. Unlike `addElement`, it puts the pillar into the loaded solution.
- **Pillar:** a pillar is a fixed appointment: its time window must be met.

## Scenario

The dates, times and coordinates are sample values.

- **The snapshot:** `SaveOptimizationToJsonExample` writes the two resources Jack and John, who start in Aachen, and eight `TimeWindowGeoNode` objects for March 6 and 7, 2020 (Europe/Berlin), together with the solution of its run.
- **The added pillar:** one `PillarTimeWindowGeoNode`:
  - id: `Koeln-Pillar`
  - position: 50.9333, 6.95 (the coordinates the other examples use for Koeln)
  - opening hours: March 7, 2020, 08:00 to 17:00 (Europe/Berlin)
  - visit duration: 20 minutes

The code does not attach the pillar to a resource (there is no `attachResource(...)` call, unlike in `PillarExample`).

The next run sets two properties: `JOptExitCondition.JOptGenerationCount` to 2000 and `JOpt.Algorithm.PreOptimization.SA.NumIterations` to 100000. Both are below their defaults, which keeps the run short; the defaults are in the [property reference](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties).

## How the code works

The source is [LoadOptimizationFromJsonAndReassignPillar.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic/io_03/LoadOptimizationFromJsonAndReassignPillar.java). `example()` does the following:

```java
ExampleLicenseHelper.setLicense(this);
if (!loadFromJson(this)) {
  return;
}
addReassignPillar(this);
// The snapshot brings its own properties, and addElement(Properties) replaces them. The
// properties of the new run are therefore added after the load.
addProperties(this);
attachToObservables(this);
startAndPresentResult(this);
```

1. Sets the license: your license file if you put one where `ExampleLicenseHelper` looks for it, the public evaluation license otherwise.
2. Loads the snapshot. If that fails, `example()` returns without a run.
3. Adds the pillar through reassignment.
4. Sets the properties of the next run, after the load (see the comment in the code).
5. Subscribes to the progress, warning, status and error events with the recommended event API, before the run starts.
6. Starts the run, waits up to 5 minutes for the result and prints it.

### Loading the snapshot

The file name is a constant of the class:

```java
private static final String JSON_FILE = "myopti.json.bz2";
```

`loadFromJson` reads it:

```java
IOptimizationImporter importer = new OptimizationJSONImporter();
try (InputStream in = new FileInputStream(JSON_FILE)) {
  boolean loaded = importer.update(in, opti);
  if (!loaded) {
    System.out.println("JSON import failed: " + JSON_FILE);
  }
  return loaded;
} catch (FileNotFoundException e) {
  System.out.println(JSON_FILE + " not found. Run SaveOptimizationToJsonExample first.");
  return false;
}
```

`update` returns `false` if it cannot read the snapshot; the example then prints `JSON import failed: myopti.json.bz2`. The try-with-resources block closes the file in every case.

### Adding the pillar

```java
IPillarNode koelnPillar =
    new PillarTimeWindowGeoNode("Koeln-Pillar", 50.9333, 6.95, openingHours, visitDuration);

// Unlike addElement, addReassignNodes puts the pillar into the loaded solution.
opti.addReassignNodes(List.of(koelnPillar));
```

## Output and what to look for

The status lines show that the run starts from the loaded solution and reassigns the pillar. These two lines appear in two test runs:

```text
StatusEvent(OPTIMIZATION_USING_PROVIDED_SOLUTION, Using a provided solution as starting point for the optimization.)
StatusEvent(ADDITIONAL_ELEMENT, Adding additional elements to be reassigned to optimization. Reassigning Nodes: Koeln-Pillar)
```

The run starts with the simulated annealing step; no construction algorithm starts, because the loaded solution is the starting point.

In the result, the line of `Koeln-Pillar` is marked `Pillar`. It lies on a route of March 7, inside its time window, directly after the nodes Koeln and Koeln1, which have the same coordinates (`Driving: 0.0 [min], 0.0 [km]`). Which resource takes the pillar and the cost of the result can change between runs.

If `myopti.json.bz2` is missing, `loadFromJson` prints `myopti.json.bz2 not found. Run SaveOptimizationToJsonExample first.` and the example starts no run.

## Run it

The example reads `myopti.json.bz2` from the working directory. Run `SaveOptimizationToJsonExample` first, in the same working directory; it writes that file at the end of its run.

1. Run the `main(String[] args)` method of `SaveOptimizationToJsonExample`.
2. Run the `main(String[] args)` method of `LoadOptimizationFromJsonAndReassignPillar` in the same working directory.

You need a JDK in the version that the examples pom sets. You need no license file: without one, both examples use the public evaluation license (see the [license guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/license)).

## Related

- [LoadOptimizationFromJsonAndReassignPillar.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic/io_03/LoadOptimizationFromJsonAndReassignPillar.java): the source of this example.
- [Load and save optimization snapshots](LoadAndSaveOptimization.md): overview of all load and save examples in this package, including `SaveOptimizationToJsonExample`, which writes the snapshot.
- [LoadOptimizationFromJsonAndReassignNodes.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic/io_03/LoadOptimizationFromJsonAndReassignNodes.java): the same pattern with three ordinary time-window nodes instead of a pillar.
- [LoadOptimizationToFreshRunExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic/io_03/LoadOptimizationToFreshRunExample.java): loads the snapshot but ignores its solution.
- [Pillar nodes with hard time windows](../pillar_07/Pillar.md): pillar nodes in general; `PillarExample` builds pillars attached to resources, from scratch.
- [Recommended ways to run an optimization](../recommendedimplementation_02/RecommendedImplementation.md): the recommended run pattern, a synchronous run and the legacy callback methods.
- [Basic examples](../README.md): the overview of all Basic examples.

---

Added: 2026-10-15 (JOpt core 7.5.3-rc5-j17) | Last updated: 2026-10-20
