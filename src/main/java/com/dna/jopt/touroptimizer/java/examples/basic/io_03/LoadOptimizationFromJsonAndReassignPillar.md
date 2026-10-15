# Load a Snapshot and Add a Pillar: `LoadOptimizationFromJsonAndReassignPillar`

This example loads a saved optimization state from `myopti.json.bz2` and adds one **pillar node** (a fixed appointment) to it before the next run. It shows how a planned state can take a new appointment with a strict time window without rebuilding the model.

---

## Links

- Example source (GitHub):  
  [LoadOptimizationFromJsonAndReassignPillar.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic/io_03/LoadOptimizationFromJsonAndReassignPillar.java)
- Overview of all load and save examples in this package:  
  [LoadAndSaveOptimization.md](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic/io_03/LoadAndSaveOptimization.md)
- Pillar nodes in general:  
  [Pillar.md](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic/pillar_07/Pillar.md)

---

## Before you run it

The example reads `myopti.json.bz2` from the working directory. Run `SaveOptimizationToJsonExample` first, in the same working directory; it writes that file at the end of its run.

If the file is missing, the example prints:

```
JsonFile not existing. Run example 'SaveOptimizationToJsonExample.java' first.
```

followed by the stack trace of the `FileNotFoundException`, and does not start a run.

---

## Execution flow

`example()` does the following:

1. Sets the license via `ExampleLicenseHelper.setLicense(this);`
2. Loads the snapshot into this optimization:
   - `IOptimizationImporter importer = new OptimizationJSONImporter();`
   - `importer.update(jsonFile, opti);`
3. Adds the pillar through reassignment:
   - `this.addReassignNodes(getAdditionalNodes());`
4. Sets the properties for the new run via `setProperties()`.
5. Starts the run with `startRunAsync().get()` and prints the result in `onAsynchronousOptimizationResult(...)`.

---

## The added pillar

`getAdditionalNodes()` creates one `PillarTimeWindowGeoNode`:

- id: `Koeln-Pillar`
- position: 50.9333, 6.95 (the coordinates the other examples use for Koeln)
- opening hours: March 7, 2020, 08:00 to 17:00 (Europe/Berlin)
- visit duration: 20 minutes

```java
IPillarNode koeln =
    new PillarTimeWindowGeoNode("Koeln-Pillar", 50.9333, 6.95, weeklyOpeningHours.get(0), visitDuration);
```

The code does not attach the pillar to a resource (there is no `attachResource(...)` call, unlike in `PillarExample`).

A pillar is a fixed appointment: its time window must be met. `addReassignNodes(...)` hands the pillar to the loaded optimization, so the next run plans it together with the elements from the snapshot.

---

## Solver properties

`setProperties()` registers:

- `JOptExitCondition.JOptGenerationCount = 2000`
- `JOpt.Algorithm.PreOptimization.SA.NumIterations = 100000`
- `JOpt.Algorithm.PreOptimization.SA.NumRepetitions = 1`
- `JOpt.NumCPUCores = 4`

---

## Related examples

- `LoadOptimizationFromJsonAndReassignNodes`: the same pattern with three ordinary time-window nodes instead of a pillar.
- `LoadOptimizationToFreshRunExample`: loads the snapshot but ignores its solution.
- `PillarExample`: pillars attached to resources, built from scratch.

---

## How to run

1. Run the `main(String[] args)` method of `SaveOptimizationToJsonExample`.
2. Run the `main(String[] args)` method of `LoadOptimizationFromJsonAndReassignPillar` in the same working directory.
