# Load and Save Optimization Snapshots (JSON / JSON.BZ2) — JOpt TourOptimizer (Java)

This document consolidates and explains the **load/save patterns** demonstrated by the `io_03` examples.  
It is written as a practical reference for building **restartable**, **recoverable**, and **portable** optimization runs.

The examples covered are:

## Save examples
- [SaveOptimizationToJsonExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic/io_03/SaveOptimizationToJsonExample.java)
- [SaveOptimizationDuringRunToJsonExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic/io_03/SaveOptimizationDuringRunToJsonExample.java)
- [SaveOptimizationToJsonStringExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic/io_03/SaveOptimizationToJsonStringExample.java)

## Load examples
- [LoadOptimizationFromJsonExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic/io_03/LoadOptimizationFromJsonExample.java)
- [LoadOptimizationToFreshRunExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic/io_03/LoadOptimizationToFreshRunExample.java)
- [LoadOptimizationFromJsonAndReassignNodes.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic/io_03/LoadOptimizationFromJsonAndReassignNodes.java)
- [LoadOptimizationFromJsonString.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic/io_03/LoadOptimizationFromJsonString.java)

---

## Why snapshotting matters (real-world use cases)

Snapshotting turns an optimization run into a **recoverable process**:

- **Crash recovery**: resume after JVM restarts.
- **Long-running runs**: persist intermediate best-known states.
- **Auditability**: store “what exactly was optimized” at a point in time.
- **Portability**: ship the problem state to another environment (dev → staging → production) or to another worker.
- **Operational control**: rerun with different properties without rebuilding the model from scratch.

In this example suite, snapshots are written as compressed JSON (`*.json.bz2`) or handled as JSON strings.

---

## What is stored in a snapshot?

A snapshot (as produced/consumed by these examples) is not merely “the result text”. It represents the **optimization state**: model elements (nodes/resources/relations), properties, and—depending on how you load it—potentially a previously computed solution state.

Two important implications:

1. **You can resume** from a known state or continue to improve it.
2. **You can rehydrate** an optimization without reconstructing the model programmatically.

The “fresh run” loading pattern (see below) exists precisely because there are cases where you want the **model** but not the **previous solution**.

---

## File format: `*.json.bz2`

Most examples use `*.json.bz2`, for example:

- `myopti.json.bz2`
- `myoptiSavedDuringRun.json.bz2`

This is simply JSON content stored in a **BZip2-compressed stream**, which dramatically reduces size for realistic optimization states.

---

## Saving patterns

### 1) Save at the end of a run (the simplest baseline)

**Example:** `SaveOptimizationToJsonExample`

**Trigger point:** `onAsynchronousOptimizationResult(...)`  
When the run completes, the example:

1. prints the result to stdout,
2. writes a snapshot to `myopti.json.bz2` using `OptimizationJSONExporter`.

Core idea (conceptually):

- “Result arrived → export the optimization state.”

Operationally, this is a good default for:
- batch/CLI workflows,
- reproducibility,
- downstream tooling (e.g., later loading for re-optimization).

#### Implementation notes
- The exporter is called with `exporter.export(this, new FileOutputStream(jsonFile));`.
- In production code, prefer try-with-resources around the output stream.

---

### 2) Save *during* a run (checkpointing at a specific progress point)

**Example:** `SaveOptimizationDuringRunToJsonExample`

This example demonstrates **checkpointing while the solver is still running**, without directly blocking the solving thread.

#### How it decides *when* to save
It inspects progress updates and triggers at a specific milestone:

- `progress.getOptimizationStage() == 2`
- `progress.getProgress() == 80.0`

When both conditions match, it writes `myoptiSavedDuringRun.json.bz2`.

#### How it saves safely
Instead of calling an exporter directly, the example uses:

- `this.requestExportState(new FileOutputStream(jsonFile), "TestSaveAt80Percent");`

This is a critical pattern: it requests the optimization engine to perform an export in a controlled manner.

#### How you know the export finished
The example demonstrates two acknowledgement routes:

1. **Event stream** subscription:
   - `requestCodeExecutionDoneSubject().subscribe(...)`
2. **Callback** override:
   - `onRequestCodeExecutionDone(String executionId)`

Both print an acknowledgement containing the execution id.

#### When this pattern is the right choice
- you run long optimizations and want periodic checkpoints,
- you want “save on signal” behavior (e.g., on a metric threshold),
- you want to minimize risk of exporting an inconsistent in-memory state.

---

### 3) Save (or print) as JSON without writing a file

**Example:** `SaveOptimizationToJsonStringExample`

This example focuses on JSON **as a string-oriented payload**, which is useful when you want to:

- store snapshots in a database,
- ship snapshots through a message queue,
- return snapshots through an API (with appropriate safeguards),
- embed snapshots into logs for debugging (usually not recommended in production due to size).

In `onAsynchronousOptimizationResult(...)`, the example calls:

- `ResultJsonPrinter.printResultAsJson(this, false);`

Practically, this produces JSON output (commonly to stdout) rather than writing `*.json.bz2`.

#### Why this example is valuable
File output is not always the right integration. Many systems are “payload-first” (HTTP, Kafka, DB blobs), and this example shows how to shape an optimization state into such workflows.

---

## Loading patterns

### 1) Load from `*.json.bz2` and continue

**Example:** `LoadOptimizationFromJsonExample`

Sequence:

1. Apply license.
2. Load from `myopti.json.bz2` via:
   - `importer.update(new FileInputStream(jsonFile), opti);`
3. Set (or override) properties.
4. Start a run asynchronously and wait (`get()`).

This is the “resume / continue improving” pattern.

#### Notes
- `setProperties()` happens **after** the snapshot is loaded, which makes it explicit that:
  - you can preserve snapshot properties *or*
  - overwrite them for the new run (the code sets properties after loading).

---

### 2) Load the snapshot, but start a **fresh run** (ignore the previous solution)

**Example:** `LoadOptimizationToFreshRunExample`

This example is the “I want the model, not the old solution” pattern.

Key switch:

- `boolean ignoreLoadedSolution = true;`

Load call:

- `importer.update(jsonFileStream, opti, ignoreLoadedSolution);`

The example explains the intent directly: when `ignoreLoadedSolution` is true, the snapshot elements (nodes, properties, connectors, etc.) are loaded, but the optimization starts without using the previous solution.

#### When to use it
- you want to rerun with new solver settings from the same base model,
- you suspect the prior solution is stale or incompatible with new constraints,
- you want reproducible “from scratch” behavior while keeping model construction cheap.

---

### 3) Load and then **extend the problem** via node reassignment

**Example:** `LoadOptimizationFromJsonAndReassignNodes`

After loading `myopti.json.bz2`, the example adds additional nodes:

- `this.addReassignNodes(getAdditionalNodes());`

`getAdditionalNodes()` creates new time-window nodes (`Koeln2`, `Dueren2`, `Essen2`) with opening hours on March 7–8 (Europe/Berlin) and a 20-minute visit duration.

#### What “reassign” means in practice
This pattern is geared toward “live” operations:

- you already have an optimization state (and often a solution),
- new tasks arrive (new customer visits, new delivery points),
- you want to inject them into the existing optimization context and re-optimize.

This is a common operational requirement in routing: the world changes while you are planning.

---

### 4) Load from an **in-memory JSON string** (portable payloads)

**Example:** `LoadOptimizationFromJsonString`

This example shows a robust trick: if you have JSON as a **string**, but your importer expects a compressed stream, you can:

1. compress the string into a BZip2 byte stream,
2. feed it to the importer as an `InputStream`.

The key methods are:

- `invokeFromJson(String json, IOptimization opti)`  
  which calls `importer.update(compressStringToBZip2Stream(json), opti);`

- `compressStringToBZip2Stream(String input)`  
  which produces a `ByteArrayInputStream` backed by `BZip2CompressorOutputStream` output.

#### When this pattern is useful
- your snapshot is stored as a string in a DB,
- your snapshot arrives over the network as JSON text,
- you need to embed snapshots in test fixtures.

#### Operational cautions
- Validate and trust your source: importing arbitrary snapshots can be risky if you accept untrusted input.
- Consider size limits: very large snapshots should be stored as compressed blobs, not as raw strings in logs.

---

## End-to-end workflows (recommended recipes)

### Recipe A — Save at end → Load → Continue
1. Run `SaveOptimizationToJsonExample` to create `myopti.json.bz2`.
2. Run `LoadOptimizationFromJsonExample` to load that snapshot and continue optimizing.

### Recipe B — Save at 80% → Recover from checkpoint
1. Run `SaveOptimizationDuringRunToJsonExample` to create `myoptiSavedDuringRun.json.bz2`.
2. Point a loader example to that file to resume/continue from that intermediate state.

### Recipe C — Load snapshot but re-optimize from scratch
1. Load via `LoadOptimizationToFreshRunExample` with `ignoreLoadedSolution = true`.
2. Change solver properties.
3. Start a clean run without inheriting the previous solution.

### Recipe D — Load snapshot and inject new work
1. Load via `LoadOptimizationFromJsonAndReassignNodes`.
2. Add new nodes through reassignment.
3. Run optimization to incorporate changes.

---

## Production hardening checklist

If you copy these patterns into production code, consider:

1. **Stream management**
   - Use try-with-resources for `FileInputStream` / `FileOutputStream`.
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

---

## Quick decision guide

- Want the simplest persistence?  
  Use **SaveOptimizationToJsonExample** (end-of-run export).

- Want safety checkpoints in long runs?  
  Use **SaveOptimizationDuringRunToJsonExample** (`requestExportState` + done acknowledgement).

- Want to transport state through APIs/DB?  
  Use the “string payload” approach from **SaveOptimizationToJsonStringExample** (print/emit JSON) and **LoadOptimizationFromJsonString** (rehydrate from string).

- Want to reuse the model but not the solution?  
  Use **LoadOptimizationToFreshRunExample** (`ignoreLoadedSolution = true`).

- Want to add new tasks into an existing plan?  
  Use **LoadOptimizationFromJsonAndReassignNodes** (`addReassignNodes(...)`).

