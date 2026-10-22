# Load and save optimization snapshots

The `io_03` examples on this page save a JOpt optimization as a snapshot, for example `myopti.json.bz2`, and load it again from a file or from a string; one of them saves a checkpoint while the genetic evolution runs. The page is for Java developers who want to continue, repeat or extend a run without building the model in code again. Start with the [First optimization example](../firstoptimization_01/FirstOptimizationExample.md), whose step methods these examples follow.

## What the example shows

A snapshot is the state of an optimization as JSON, in a file `*.json.bz2`, as most examples use it (for example `myopti.json.bz2` and `myoptiSavedDuringRun.json.bz2`). This is simply JSON content stored in a **BZip2-compressed stream**: the file is compressed with BZip2, which dramatically reduces size for realistic optimization states. The file that `SaveOptimizationToJsonExample` writes holds the nodes, the resources, their connections, the properties and the solution of the run. A loader reads such a file into its own optimization. It can continue from the saved solution, start without it, or add nodes to it.

- `SaveOptimizationToJsonExample` saves the optimization after the run to `myopti.json.bz2`.
- `SaveOptimizationDuringRunToJsonExample` saves it during the run with `requestExportState`, when the genetic evolution reaches 80 percent, to `myoptiSavedDuringRun.json.bz2`.
- `SaveOptimizationToJsonStringExample` prints the optimization as JSON after the run. The printed JSON holds no solution, so it is not a snapshot.
- `LoadOptimizationFromJsonExample` loads `myopti.json.bz2` and continues from its solution.
- `LoadOptimizationToFreshRunExample` loads `myopti.json.bz2` but ignores the saved solution (`ignoreLoadedSolution = true`), so the run builds its own.
- `LoadOptimizationFromJsonAndReassignNodes` loads `myopti.json.bz2` and adds three nodes to its solution with `addReassignNodes`.
- `LoadOptimizationFromJsonString` loads a snapshot from a JSON string. It reads the string from the class path resource `json/LoadOptimizationFromJsonString.json`.

### Why snapshotting matters (real-world use cases)

Snapshotting turns an optimization run into a **recoverable process**:

- **Crash recovery**: resume after JVM restarts.
- **Long-running runs**: persist intermediate best-known states.
- **Auditability**: store "what exactly was optimized" at a point in time.
- **Portability**: ship the problem state to another environment (dev to staging to production) or to another worker.
- **Operational control**: rerun with different properties without rebuilding the model from scratch.

### What a snapshot stores

A snapshot (as produced/consumed by these examples) is not merely "the result text". It represents the **optimization state**: model elements (nodes/resources/relations), properties, and the solution, which a snapshot always holds.

Two important implications:

1. **You can resume** from a known state or continue to improve it.
2. **You can rehydrate** an optimization without reconstructing the model programmatically.

The "fresh run" loading pattern (see below) exists because there are cases where you want the **model** but not the **previous solution**.

## Scenario

`SaveOptimizationToJsonExample` and `SaveOptimizationDuringRunToJsonExample` build the same scenario. Eight nodes in western and southern Germany (Koeln and Koeln1 at the same place, Oberhausen, Essen, Wuppertal, Aachen, Heilbronn, Stuttgart) are open on March 6 and 7, 2020, from 08:00 to 17:00, with a visit of 20 minutes each. Two resources, Jack and John, start in Aachen. They work on March 6 from 08:00 to 17:00 and on March 7 from 08:00 to 20:00. Jack may work 8 hours, less than either shift, and John 14 hours, more than either shift. The time zone is Europe/Berlin; the dates are sample values.

The loaders on this page that read `myopti.json.bz2` take this scenario from the file, with the properties and the solution of the save run, and add no properties of their own. `LoadOptimizationFromJsonString` loads a sample snapshot of the same scenario from its resource file. `LoadOptimizationFromJsonAndReassignNodes` adds Koeln2, Dueren2 and Essen2, open on March 6 and 7, 2020, from 08:00 to 17:00: the days on which the resources of the snapshot work.

`SaveOptimizationToJsonStringExample` builds a smaller scenario of its own. Koeln and Aachen are open on March 6, 2030, from 08:00 to 17:00. One resource, Jack, starts in Aachen and works on March 6 and 7, 2030. A relation between the two nodes makes Aachen follow Koeln. These dates are sample values as well.

The save examples set `JOptExitCondition.JOptGenerationCount` and `JOpt.Algorithm.PreOptimization.SA.NumIterations` below their defaults to keep the run short. The [optimization properties](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties) page lists the defaults.

## How the code works

The source files on GitHub:

- [SaveOptimizationToJsonExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic/io_03/SaveOptimizationToJsonExample.java)
- [SaveOptimizationDuringRunToJsonExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic/io_03/SaveOptimizationDuringRunToJsonExample.java)
- [SaveOptimizationToJsonStringExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic/io_03/SaveOptimizationToJsonStringExample.java)
- [LoadOptimizationFromJsonExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic/io_03/LoadOptimizationFromJsonExample.java)
- [LoadOptimizationToFreshRunExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic/io_03/LoadOptimizationToFreshRunExample.java)
- [LoadOptimizationFromJsonAndReassignNodes.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic/io_03/LoadOptimizationFromJsonAndReassignNodes.java)
- [LoadOptimizationFromJsonString.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic/io_03/LoadOptimizationFromJsonString.java)
- [json/LoadOptimizationFromJsonString.json](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/resources/json/LoadOptimizationFromJsonString.json), the resource that `LoadOptimizationFromJsonString` reads

Each class follows the steps of `FirstOptimizationExample`. `example()` sets the license and calls the step methods in order. `attachToObservables` subscribes to the event subjects, the recommended event API, before the run starts. `startAndPresentResult` starts the run with `startRunAsync()`, waits up to 5 minutes for the result and prints it. In the loaders, the load step takes the place of the property, node and resource steps.

The loaders that read `myopti.json.bz2` (`LoadOptimizationFromJsonExample`, `LoadOptimizationToFreshRunExample`, `LoadOptimizationFromJsonAndReassignNodes`, `LoadOptimizationFromJsonAndReassignPillar`) need that file in the working directory. Run `SaveOptimizationToJsonExample` first, in the same working directory. If the file is missing, each of them prints "myopti.json.bz2 not found. Run SaveOptimizationToJsonExample first." and does not start a run.

### SaveOptimizationToJsonExample

This is the save at the end of a run (the simplest baseline). Core idea (conceptually): "Result arrived -> export the optimization state." Operationally, this is a good default for:
- batch/CLI workflows,
- reproducibility,
- downstream tooling (e.g., later loading for re-optimization).

**Trigger point:** `saveToJson(...)`, which `example()` calls after `startAndPresentResult(...)`. When the run completes, the example:

1. prints the result to stdout,
2. writes a snapshot to `myopti.json.bz2` using `OptimizationJSONExporter`: after the result, the exporter is called with `exporter.export(opti, out)` in `saveToJson`, where `out` is a `FileOutputStream` on `JSON_FILE` (implementation notes).

The stream is opened in try-with-resources (in production code, prefer try-with-resources around the output stream, as the example does), and the example checks the boolean that `export` returns:

```java
private static void saveToJson(IOptimization opti) throws IOException {
  IOptimizationExporter exporter = new OptimizationJSONExporter();
  try (OutputStream out = new FileOutputStream(JSON_FILE)) {
    if (exporter.export(opti, out)) {
      System.out.println("Saved " + JSON_FILE);
    } else {
      System.out.println("JSON export failed: " + JSON_FILE);
    }
  }
}
```

### SaveOptimizationDuringRunToJsonExample

This example demonstrates **checkpointing** while the optimization (the solver) is still running, at a specific progress point, without blocking the thread that requests it: `requestExportState` returns at once, and the solver writes the file between two of its steps and waits until the write is done.

`saveToJsonDuringRun` subscribes to the progress events before the start. How it decides *when* to save: it inspects progress updates and triggers at a specific milestone; when stage 2 reports 80.0 percent (`p.getOptimizationStage() == 2` and `p.getProgress() == 80.0`), both conditions match and it calls `requestSave`, which writes `myoptiSavedDuringRun.json.bz2`. The stages count from 0 (construction, simulated annealing, genetic evolution), so stage 2 is the genetic evolution. The method also subscribes to `requestCodeExecutionDoneSubject()`, which emits the execution id when the save is done:

```java
private static void saveToJsonDuringRun(IOptimization opti) {
  opti.getOptimizationEvents()
      .progressSubject()
      .subscribe(
          p -> {
            // Stages count from 0: construction, simulated annealing, genetic evolution.
            if (p.getOptimizationStage() == 2 && p.getProgress() == 80.0) {
              requestSave(opti);
            }
          });
  opti.getOptimizationEvents()
      .requestCodeExecutionDoneSubject()
      .subscribe(id -> System.out.println("Saved during the run: " + id));
}
```

How it saves safely: instead of calling an exporter directly, the example uses `opti.requestExportState(new FileOutputStream(JSON_FILE), "TestSaveAt80Percent");`, which asks the running optimization to save its state. This is a critical pattern: it requests the optimization engine to perform an export in a controlled manner. The core writes the file in the background and closes the stream after a successful write, so `requestSave` leaves the stream open:

```java
private static void requestSave(IOptimization opti) {
  try {
    // The core writes the file in the background and closes the stream after a successful
    // write, so the stream must stay open here (no try-with-resources).
    opti.requestExportState(new FileOutputStream(JSON_FILE), "TestSaveAt80Percent");
  } catch (FileNotFoundException e) {
    System.out.println("Cannot open " + JSON_FILE + ": " + e.getMessage());
  }
}
```

How you know the export finished: there are two acknowledgement routes; the example uses the event stream:

1. **Event stream** subscription:
   - `requestCodeExecutionDoneSubject().subscribe(...)`
2. **Callback** override:
   - `onRequestCodeExecutionDone(String executionId)`

The subscription prints an acknowledgement containing the execution id. The legacy callback methods (`onProgress`, `onAsynchronousOptimizationResult` and the others, `onRequestCodeExecutionDone` among them) subscribe to the same events internally and stay supported; the event API is recommended.

Use it for checkpoints in long runs, or to save when a progress value is reached. When this pattern is the right choice:
- you run long optimizations and want periodic checkpoints,
- you want "save on signal" behavior (e.g., on a metric threshold),
- you want to minimize risk of exporting an inconsistent in-memory state.

### SaveOptimizationToJsonStringExample

This example prints the optimization as a JSON string without writing a file. It focuses on JSON **as a string-oriented payload**, which is useful when you want to:

- store snapshots in a database,
- ship snapshots through a message queue,
- return snapshots through an API (with appropriate safeguards),
- embed snapshots into logs for debugging (usually not recommended in production due to size).

The JSON that this example prints holds no solution, so it is not a snapshot; a snapshot as a string is JSON with a `solution` entry, such as the resource that `LoadOptimizationFromJsonString` loads.

`addNodes` adds a `NegativeRelativeTimeWindow2RelatedNodeRelation` between Koeln and Aachen:

```java
// Penalizes Aachen starting -2000 to +10 min from Koeln's start, so Aachen follows Koeln.
INode2NodeRelation rel =
    new NegativeRelativeTimeWindow2RelatedNodeRelation(
        Duration.ofMinutes(-2000), Duration.ofMinutes(10));
rel.setMasterNode(koeln);
rel.setRelatedNode(aachen);
aachen.addNode2NodeRelation(rel);
koeln.addNode2NodeRelation(rel);
```

The relation adds a penalty when Aachen starts between 2000 minutes before and 10 minutes after the start of Koeln. Both nodes are open on one day only, so Aachen avoids the penalty only by starting more than 10 minutes after Koeln.

After the result, `printAsJson` prints the optimization with `ResultJsonPrinter`, a helper of this repository ([ResultJsonPrinter.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/util/jsonprinter/ResultJsonPrinter.java)): `printAsJson(...)` calls `ResultJsonPrinter.printResultAsJson(opti, false);`. The helper leaves out the solution and the core build options, and with `false` also the node connections:

```java
private static void printAsJson(IOptimization opti) throws IOException, ConvertException {
  // false: leave out the node connections (the distances and times between the elements).
  ResultJsonPrinter.printResultAsJson(opti, false);
}
```

Practically, this produces JSON output (commonly to stdout) rather than writing `*.json.bz2`.

#### Why this example is valuable
File output is not always the right integration. Many systems are "payload-first" (HTTP, Kafka, DB blobs), and this example shows how to shape an optimization state (without its solution) into such workflows.

### LoadOptimizationFromJsonExample

Load from `*.json.bz2` and continue. Sequence:

1. Apply license.
2. Load from `myopti.json.bz2` via:
   - `importer.update(in, opti);`, where `in` is a `FileInputStream` on `JSON_FILE`, opened in try-with-resources
3. Set (or override) properties (your own step; the example does not do this and keeps the properties of the snapshot).
4. Start a run asynchronously and wait (`get(5, TimeUnit.MINUTES)`).

This is the "resume / continue improving" pattern.

`loadFromJson` reads the file with `OptimizationJSONImporter` and checks the boolean that `update` returns. Without the file it prints a message and returns false, and `example()` then ends without a run:

```java
private static boolean loadFromJson(IOptimization opti) throws IOException {
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
}
```

The loaded optimization holds the properties and the solution of the save run, so the run starts from the saved solution.

Notes:
- Properties added **after** the snapshot is loaded replace those of the snapshot, which makes it explicit that:
  - you can preserve snapshot properties *or*
  - overwrite them for the new run (`LoadOptimizationFromJsonAndReassignPillar` adds its properties after loading; this example adds none).

### LoadOptimizationToFreshRunExample

Load the snapshot, but start a **fresh run** (ignore the previous solution): this example is the "I want the model, not the old solution" pattern.

The class differs from `LoadOptimizationFromJsonExample` in one argument: its `loadFromJson` passes `ignoreLoadedSolution` as true to `update`. The block shows the start of the method; the rest is the same as above:

```java
// With true, all parts of the snapshot are loaded (the elements, the node connector and the
// properties), but the run does not start from the saved solution.
boolean ignoreLoadedSolution = true;
IOptimizationImporter importer = new OptimizationJSONImporter();
try (InputStream in = new FileInputStream(JSON_FILE)) {
  boolean loaded = importer.update(in, opti, ignoreLoadedSolution);
```

The key switch and the load call are in the block above. The example explains the intent directly: when `ignoreLoadedSolution` is true, the snapshot elements (nodes, properties, connectors, etc.) are loaded, but the optimization starts without using the previous solution.

Use it to rerun the same model with new properties, or when the saved solution does not fit new constraints. When to use it:
- you want to rerun with new solver settings from the same base model,
- you suspect the prior solution is stale or incompatible with new constraints,
- you want a run "from scratch" while keeping model construction cheap.

### LoadOptimizationFromJsonAndReassignNodes

Load and then **extend the problem** via node reassignment: after loading `myopti.json.bz2`, the example adds additional nodes.

`example()` loads the file as `LoadOptimizationFromJsonExample` does and then adds the nodes:

```java
// The public evaluation license, or your own license if you put a license file where
// ExampleLicenseHelper looks for it.
ExampleLicenseHelper.setLicense(this);
if (!loadFromJson(this)) {
  return;
}
addReassignNodes(this);
attachToObservables(this);
startAndPresentResult(this);
```

The example's step `addReassignNodes(this)` builds the three nodes: it creates new time-window nodes (`Koeln2`, `Dueren2`, `Essen2`) with opening hours on March 6-7 (Europe/Berlin) and a 20-minute visit duration, and passes them to the core method `opti.addReassignNodes(newNodes)`, which the step calls. Unlike `addElement`, that method puts the nodes into the existing solution:

```java
List<INode> newNodes = new ArrayList<>();
newNodes.add(
    new TimeWindowGeoNode("Koeln2", 50.9333, 6.95, openingHours, visitDuration, importance));
newNodes.add(
    new TimeWindowGeoNode("Dueren2", 50.8, 6.48333, openingHours, visitDuration, importance));
newNodes.add(
    new TimeWindowGeoNode("Essen2", 51.45, 7.01667, openingHours, visitDuration, importance));
opti.addReassignNodes(newNodes);
```

Use it when new tasks arrive for a plan that is already optimized.

#### What "reassign" means in practice
This pattern is geared toward "live" operations:

- you already have an optimization state (and often a solution),
- new tasks arrive (new customer visits, new delivery points),
- you want to inject them into the existing optimization context and re-optimize.

This is a common operational requirement in routing: the world changes while you are planning.

### Load and then add a **pillar** via reassignment

**Example:** `LoadOptimizationFromJsonAndReassignPillar`

After loading `myopti.json.bz2`, the example adds one `PillarTimeWindowGeoNode` (`Koeln-Pillar`, at the coordinates of Koeln) through the same call:

- `opti.addReassignNodes(List.of(koelnPillar));`, in the example's step `addReassignPillar(this)`

The pillar has one opening hours window on March 7, 2020, 08:00 to 17:00 (Europe/Berlin), and a visit duration of 20 minutes. A pillar is a fixed appointment: its time window must be met. See [Load a snapshot and add a pillar](LoadOptimizationFromJsonAndReassignPillar.md).

### LoadOptimizationFromJsonString

Load from an **in-memory JSON string** (portable payloads). This example shows a robust trick: if you have JSON as a **string**, but your importer expects a compressed stream, you can:

1. compress the string into a BZip2 byte stream,
2. feed it to the importer as an `InputStream`.

`readJsonString` reads the resource from the class path into a string:

```java
private static String readJsonString() throws IOException {
  try (InputStream in = LoadOptimizationFromJsonString.class.getResourceAsStream(JSON_RESOURCE)) {
    if (in == null) {
      throw new FileNotFoundException("Resource not found: " + JSON_RESOURCE);
    }
    return new String(in.readAllBytes(), StandardCharsets.UTF_8);
  }
}
```

These are the key methods: `loadFromJsonString(IOptimization opti, String json)` loads the optimization from that string. `OptimizationJSONImporter` reads BZip2-compressed JSON, so `compressToBZip2(String text)` first compresses the string into an in-memory stream, which produces a `ByteArrayInputStream` backed by `BZip2CompressorOutputStream` output; `loadFromJsonString` then calls `importer.update(in, opti);` on the stream from `compressToBZip2(json)`:

```java
private static boolean loadFromJsonString(IOptimization opti, String json) throws IOException {
  IOptimizationImporter importer = new OptimizationJSONImporter();
  // OptimizationJSONImporter reads BZip2-compressed JSON, so the string is compressed first.
  try (InputStream in = compressToBZip2(json)) {
    boolean loaded = importer.update(in, opti);
    if (!loaded) {
      System.out.println("JSON import failed: " + JSON_RESOURCE);
    }
    return loaded;
  }
}
```

Use it when the snapshot comes from a database, a message or a test fixture; validate snapshots from sources you do not trust. When this pattern is useful:
- your snapshot is stored as a string in a DB,
- your snapshot arrives over the network as JSON text,
- you need to embed snapshots in test fixtures.

Operational cautions:
- Validate and trust your source: importing arbitrary snapshots can be risky if you accept untrusted input.
- Consider size limits: very large snapshots should be stored as compressed blobs, not as raw strings in logs.

### End-to-end workflows (recommended recipes)

#### Recipe A: Save at end, load, continue
1. Run `SaveOptimizationToJsonExample` to create `myopti.json.bz2`.
2. Run `LoadOptimizationFromJsonExample` to load that snapshot and continue optimizing.

#### Recipe B: Save at 80%, recover from checkpoint
1. Run `SaveOptimizationDuringRunToJsonExample` to create `myoptiSavedDuringRun.json.bz2`.
2. Point a loader example to that file to resume/continue from that intermediate state (your own step; the example does not do this).

#### Recipe C: Load snapshot but re-optimize from scratch
1. Load via `LoadOptimizationToFreshRunExample` with `ignoreLoadedSolution = true`.
2. Change solver properties (your own step; the example does not do this).
3. Start a clean run without inheriting the previous solution.

#### Recipe D: Load snapshot and inject new work
1. Load via `LoadOptimizationFromJsonAndReassignNodes`.
2. Add new nodes through reassignment.
3. Run optimization to incorporate changes.

### Production hardening checklist

If you copy these patterns into production code, consider:

1. **Stream management**
   - Use try-with-resources for `FileInputStream` / `FileOutputStream`, as the save and load classes do. The stream of `requestExportState` stays open, because the core writes the file in the background and closes the stream after a successful write; if the write fails, the stream stays open, so close the stream once the run has finished.
2. **Snapshot naming**
   - Include timestamps / correlation ids (e.g., `opti_<runId>_<ts>.json.bz2`).
3. **Timeouts**
   - Avoid unbounded waits in services; use timeouts and cancellation strategies.
4. **Versioning**
   - Treat snapshots as versioned artifacts. Store solver/library versions alongside the snapshot metadata.
5. **Security**
   - Do not import snapshots from untrusted sources without validation and governance.
6. **Observability**
   - Log export/import success, duration, and size. For checkpointing, log which progress milestone triggered the export.

## Output and what to look for

Every run prints the progress lines, the status events and the result. The lines below were the same in two test runs. The run id (`Optimization-IDENT` in the result header), the order of Koeln and Koeln1 (they share a place) and the routes that get a node can change between runs, so they are not shown.

### Output of SaveOptimizationToJsonExample

The result header shows the cost of the solution:

```text
 Cost                    : 847.033054487167
```

The last line confirms the file:

```text
Saved myopti.json.bz2
```

### Output of SaveOptimizationDuringRunToJsonExample

Look for the progress line that starts with `PC 80.0` and names `AL GeneticEvolution`. The acknowledgement follows it, before the result:

```text
Saved during the run: TestSaveAt80Percent
```

`myoptiSavedDuringRun.json.bz2` has the same format as `myopti.json.bz2`. No example of this package reads it.

With the core version that the examples pom sets, the JVM may not exit after the run: the result and the file are complete, but the program keeps running until you stop it.

### Output of SaveOptimizationToJsonStringExample

Jack visits Koeln before Aachen. The output is trimmed to the two visits:

```text
0.0 Koeln / Geo  / Arrival: 06.03.2030 08:47:59 (Europe/Berlin) , Departure: 06.03.2030 09:07:59 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 47.99 [min], 63.34 [km]
0.1 Aachen / Geo  / Arrival: 06.03.2030 09:55:49 (Europe/Berlin) , Departure: 06.03.2030 10:15:49 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 47.84 [min], 63.14 [km]
```

The JSON follows the result. It holds the nodes, the resources, the relation and the properties, and no `solution` entry. The relation shows the -2000 and 10 minutes as `PT-33H-20M` and `PT10M` (trimmed after the two values):

```json
  "nodeRelations" : [ {
    "masterNodeId" : "Koeln",
    "relatedNodeIds" : [ "Aachen" ],
    "type" : {
      "minTimeDeviation" : "PT-33H-20M",
      "maxTimeDeviation" : "PT10M",
```

### Output of LoadOptimizationFromJsonExample

The status events show that the run starts from the saved solution and skips the construction:

```text
StatusEvent(OPTIMIZATION_USING_PROVIDED_SOLUTION, Using a provided solution as starting point for the optimization.)
StatusEvent(OPTIMIZATION_ALGO_STARTING, SmartSimulatedAnnealingAlgo)
```

### Output of LoadOptimizationToFreshRunExample

The run starts with the construction and has no `OPTIMIZATION_USING_PROVIDED_SOLUTION` line:

```text
StatusEvent(OPTIMIZATION_ALGO_STARTING, SmartConstructionAlgo)
```

### Output of LoadOptimizationFromJsonAndReassignNodes

The status events name the added nodes:

```text
StatusEvent(OPTIMIZATION_USING_PROVIDED_SOLUTION, Using a provided solution as starting point for the optimization.)
StatusEvent(ADDITIONAL_ELEMENT, Adding additional elements to be reassigned to optimization. Reassigning Nodes: Koeln2, Dueren2, Essen2)
```

Koeln2, Dueren2 and Essen2 then appear in the routes of the result. The routes that take them can change between runs, and so can the violations of the result.

### Output of LoadOptimizationFromJsonString

As in `LoadOptimizationFromJsonExample`, the run starts from the loaded solution:

```text
StatusEvent(OPTIMIZATION_USING_PROVIDED_SOLUTION, Using a provided solution as starting point for the optimization.)
StatusEvent(OPTIMIZATION_ALGO_STARTING, SmartSimulatedAnnealingAlgo)
```

## Quick decision guide

- Want the simplest persistence?  
  Use **SaveOptimizationToJsonExample** (end-of-run export).

- Want safety checkpoints in long runs?  
  Use **SaveOptimizationDuringRunToJsonExample** (`requestExportState` + done acknowledgement).

- Want to transport state through APIs/DB?  
  Use the "string payload" approach from **SaveOptimizationToJsonStringExample** (print/emit JSON; the printed JSON holds no solution, so it is not a snapshot) and **LoadOptimizationFromJsonString** (rehydrate from string).

- Want to reuse the model but not the solution?  
  Use **LoadOptimizationToFreshRunExample** (`ignoreLoadedSolution = true`).

- Want to add new tasks into an existing plan?  
  Use **LoadOptimizationFromJsonAndReassignNodes** (`addReassignNodes(...)`).

- Want to add a fixed appointment (pillar) into an existing plan?  
  Use **LoadOptimizationFromJsonAndReassignPillar** (`addReassignNodes(...)` with a `PillarTimeWindowGeoNode`).

## Run it

Run the `main` method of a class from your IDE, with the root of the project as the working directory. The examples need a JDK in the version that the examples pom sets. They need no license: without a license file they use the public evaluation license (see the [license guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/license)).

Run `SaveOptimizationToJsonExample` first. It writes `myopti.json.bz2` into the working directory, where `LoadOptimizationFromJsonExample`, `LoadOptimizationToFreshRunExample`, `LoadOptimizationFromJsonAndReassignNodes` and `LoadOptimizationFromJsonAndReassignPillar` read it. Without the file, the code prints that `myopti.json.bz2` is not found and that `SaveOptimizationToJsonExample` must run first, and the example starts no run. `LoadOptimizationFromJsonString` needs no file in the working directory, because its JSON resource is on the class path.

## Related

- [First optimization example](../firstoptimization_01/FirstOptimizationExample.md): the step methods that these examples follow.
- [Load a snapshot and add a pillar](LoadOptimizationFromJsonAndReassignPillar.md): loads `myopti.json.bz2` and adds a pillar node with `addReassignNodes`.
- [Export a result to a KML file](Export2KMLExample.md): exports the result of a run to a KML file.
- [Recommended ways to run an optimization](../recommendedimplementation_02/RecommendedImplementation.md): the recommended run pattern, a synchronous run and the legacy callback methods.
- [Basic examples](../README.md): the overview of all Basic examples.
- [Optimization properties](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties): the property keys and their defaults.
- [License guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/license): the public evaluation license and its element limits.

---

Added: 2026-02-17 (JOpt core 7.5.3-j17) | Last updated: 2026-10-23
