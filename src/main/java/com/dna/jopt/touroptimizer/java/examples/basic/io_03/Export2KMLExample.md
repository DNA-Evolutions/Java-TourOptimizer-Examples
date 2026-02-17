# Export to KML — `Export2KMLExample`

This example demonstrates a practical “last mile” step after solving a tour optimization: **exporting the resulting optimization container to a KML file** so you can visualize routes and locations in map tools such as **Google Earth** or other KML-capable viewers.

Unlike purely console-based examples, this one produces a tangible artifact on disk: `myopti.kml`.

---

## Links

- Example source (GitHub):  
  [Export2KMLExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic/io_03/Export2KMLExample.java)

---

## What you get when you run it

1. The optimizer runs asynchronously.
2. Progress/status/error callbacks write messages to stdout.
3. When the run finishes, `onAsynchronousOptimizationResult(...)` is invoked.
4. The example exports the **result container** to `myopti.kml` via `EntityKMLExporter`.

In other words: **Solve → Receive result → Export → Open in a map tool**.

---

## Execution flow (read this like a story)

### 1) `main(...)` launches the scenario

`main(...)` simply instantiates the example and calls `example()`.

### 2) `example()` wires the run end-to-end

The orchestration is intentionally compact:

- Apply a license via `ExampleLicenseHelper.setLicense(this);`
- Register solver properties via `setProperties()`
- Register problem elements via `addNodes()` and `addResources()`
- Start the run via `startRunAsync().get()` (the blocking `get()` is there to keep the JVM alive until completion)

> Note: The class also overrides `onAsynchronousOptimizationResult(...)`, which is where the export happens. The `get()` call ensures the asynchronous run has time to finish and invoke that callback.

---

## What is being optimized

### Nodes (time-windowed geo points)

The example creates a shared list of opening hours for **two days in March 2020** (Europe/Berlin time zone):

- March 6: 08:00–17:00  
- March 7: 08:00–17:00

All nodes share:
- `visitDuration = 20 minutes`
- importance is set to `1`

Nodes added (as `TimeWindowGeoNode`):
- Koeln
- Koeln1 (same coordinates as Koeln; useful for testing repeated/duplicate locations)
- Oberhausen
- Essen
- Heilbronn
- Stuttgart
- Wuppertal
- Aachen

### Resources (capacity resources)

Two resources are created as `CapacityResource`, both starting at Aachen coordinates:

- **Jack**
  - max working time: 8 hours
- **John**
  - max working time: 14 hours

Shared constraints:
- max distance per working day: **1200 km**
- working hours are defined by `getDefaultWorkingHours()` (two days; the second day extends to 20:00)

Each resource is assigned a cost matrix entry via `setCost(0, 1, 1)`.

---

## Solver properties (what the run is “allowed” to do)

`setProperties()` registers a small set of properties that define solver effort and runtime behavior:

- `JOptExitCondition.JOptGenerationCount = 2000`
- `JOpt.Algorithm.PreOptimization.SA.NumIterations = 100000`
- `JOpt.Algorithm.PreOptimization.SA.NumRepetions = 1`
- `JOpt.NumCPUCores = 4`
- `JOptLicense.CheckAutoLicensce = FALSE`

These are typical “developer friendly” defaults: enough compute to produce a meaningful result, with bounded runtime behavior in common environments.

---

## Observability: progress, status, warnings, errors

This example shows two styles of progress reporting:

- `onProgress(String winnerProgressString)` prints a progress string
- `onProgress(IOptimizationProgress rapoptProgress)` is implemented but intentionally left empty

Additionally:
- `onStatus(...)` prints status code + message
- `onError(...)` prints error code + message
- `onWarning(...)` is present (stubbed)

Practical takeaway: you can scale observability from “just print progress” to “feed structured telemetry” without changing your modeling logic.

---

## The KML export (the point of this example)

### Where the export happens

The file is created in:

- `onAsynchronousOptimizationResult(IOptimizationResult rapoptResult)`

Core logic:

- Print the result (`System.out.println(rapoptResult);`)
- Create the output filename: `myopti.kml`
- Export via:
  - `IEntityExporter exporter = new EntityKMLExporter();`
  - `exporter.export(rapoptResult.getContainer(), new FileOutputStream(kmlFile));`

### What is exported

The exporter writes the **optimization container** from the result:

- `rapoptResult.getContainer()`

This is important: the export uses the container that holds the solved state, not just a textual summary.

### How to use the output

Once generated, open `myopti.kml` in a KML viewer (for example, Google Earth) to visually inspect:
- node locations,
- computed tours/routes (depending on exporter capabilities and result structure).

---

## Implementation notes (recommended improvements)

This example is intentionally minimal. In production or reusable tooling, consider:

1. **Use try-with-resources** for the output stream:
   - ensures the stream is closed even on exceptions.
2. **Make the output path explicit** (e.g., configurable output directory) rather than relying on the process working directory.
3. **Export on demand**:
   - e.g., export only when a run is “good enough”, or export multiple snapshots (best-of) during progress callbacks.

---

## Small curiosity

The class-level string returned by `toString()` says it saves to a JSON file, while the implementation exports to **KML**. Treat the Java source as authoritative; the string is likely a leftover from a similar “export” example variant.

---

## How to run

Run the `main(String[] args)` method of `Export2KMLExample`.

After completion, check your process working directory for:

- `myopti.kml`
