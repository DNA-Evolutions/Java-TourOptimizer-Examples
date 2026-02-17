# OptimizationProgress — Controlling Progress Callbacks and Forcing Progress Snapshots

JOpt.TourOptimizer exposes **progress events** during an optimization run so you can:
- stream status into a UI,
- log intermediate improvements,
- implement early stopping or stage switching,
- export intermediate “best so far” snapshots.

This document covers two complementary mechanisms, each demonstrated by an example:

1. **Change the default progress output frequency** (property-driven, per stage)  
2. **Request progress updates externally at arbitrary times** (imperative `requestProgress()`)

---

## References

### Change default progress output frequency
- Source: https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/progressfrequency/ChangeOnProgressDefaultFrequencyExample.java

### Request progress via external time
- Source: https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/progressfrequency/RequestOnProgressViaExternalTimeExample.java

---

## The two progress callback signatures

Both examples implement two callbacks:

### 1) `onProgress(String winnerProgressString)`
This callback provides a **human-readable progress string** (suitable for logs).

The examples print this string directly:
- `System.out.println(winnerProgressString);`

This is often the easiest integration point for:
- quick logging,
- CLI tools,
- early debugging.

### 2) `onProgress(IOptimizationProgress rapoptProgress)`
This callback provides a **structured progress object**, typically used for:
- UI progress bars,
- stage-based logic (e.g., “if we are in Genetic stage and progress is 99%…”),
- telemetry and metrics.

In the provided examples the structured callback is present but intentionally left empty, because the focus is on *when* progress events occur and how to request them.

---

## Part 1 — Changing the default progress update frequency (per stage)

Example: `ChangeOnProgressDefaultFrequencyExample`

### What it solves
Some optimizations run for minutes or hours. If you emit progress updates too frequently, you may:
- slow down the optimizer (logging overhead),
- flood a UI or message bus,
- generate excessive storage.

If you emit progress too rarely, a UI feels “stuck” and you lose observability.

This example shows how to configure **different progress output percentages** per stage.

### The key properties
The example sets:

- `JOpt.Algorithm.PreOptimization.SA.OnProgressOutPercentage = 0.1`
- `JOpt.Algorithm.GE.OnProgressOutPercentage = 0.01`

Interpretation:
- In the **SA pre-optimization stage**, emit progress output every **0.1%**.
- In the **GE genetic stage**, emit progress output every **0.01%**.

This is a practical pattern:
- SA can be very iteration-heavy, so a moderate rate is often enough.
- GE progress can be more “visible” to users (e.g., improving winner), so a finer rate may be desirable.

### Why “per stage” matters
Different stages have different dynamics:
- SA often iterates many small changes and can produce extremely frequent progress events.
- GE typically runs generations, and users often want more granular insight into improvements.

Per-stage configuration avoids the “one size fits all” problem.

---

## Part 2 — Requesting progress snapshots on demand (external timer)

Example: `RequestOnProgressViaExternalTimeExample`

### What it solves
Sometimes you do not want progress to be tied to the solver’s internal loop counters.
Instead, you want progress at a **wall-clock frequency**, e.g.:
- every 500 ms,
- every 2 seconds,
- on a user action (“refresh progress now”),
- on an external orchestrator tick.

This example shows exactly that: it attaches a `Timer` that calls:

- `opti.requestProgress();`

every **500 ms**.

### How it is implemented (high level)
1. Start optimization asynchronously:
   - `CompletableFuture<IOptimizationResult> resultFuture = this.startRunAsync();`

2. Schedule a timer task:
   - `timer.schedule(task, 0L, 500L);`

3. Each timer tick triggers:
   - `this.opti.requestProgress();`

4. Block on the run to keep the JVM alive:
   - `resultFuture.get();`

5. Cancel the timer after completion:
   - `progressRequestTimer.cancel();`

### What `requestProgress()` does (practical interpretation)
`requestProgress()` asks the optimizer to **publish a progress update** at the next safe opportunity.
It is not meant to “interrupt” the solver; it is meant to:
- surface the most recent progress state,
- without waiting for the next internally scheduled output boundary.

---

## Recommended usage patterns (production-grade)

### Pattern A — UI progress: combine both mechanisms
A robust pattern for UIs is:

- set a conservative internal progress output percentage (property-driven),
- additionally request progress every 0.5–2 seconds from the UI layer.

This ensures:
- stable baseline updates even if the UI timer fails,
- user-visible smooth progress even if the solver’s internal stage loop is coarse.

### Pattern B — Logging: keep internal output less frequent
For server environments, logging is expensive. Consider:
- `OnProgressOutPercentage` of 0.1–1.0 depending on the stage,
- and avoid printing very frequently in high-CPU scenarios.

### Pattern C — Early stopping or stage control using structured progress
If you want to implement logic such as:
- “stop after N loops”,
- “switch stage if improvement stalls”,
use the structured callback:
- `onProgress(IOptimizationProgress ...)`

and implement policy-based logic there, rather than parsing the string output.

(See also the pattern used in `RunOptimizationInLoopExample`, where progress stage and percentage are used to count loops.)

---

## Performance and stability considerations

### 1) Progress events are not free
Every progress event can trigger:
- string construction,
- logging,
- UI updates,
- and in some integrations network calls.

Set your frequencies consciously.

### 2) Avoid heavy work in `onProgress(...)`
If you do heavy processing (exports, DB writes) inside progress callbacks, you can slow down optimization.

Best practice:
- push minimal events to a queue,
- process them asynchronously in your application layer.

### 3) Timer threads and lifecycle
If you use external timers:
- always cancel them after completion,
- ensure they are not creating unbounded background threads in long-lived services.

The example cancels the timer explicitly:
- `progressRequestTimer.cancel();`

### 4) Thread safety
Progress callbacks may be invoked from optimization threads. Therefore:
- treat `onProgress(...)` as multi-threaded code,
- use thread-safe structures or synchronization if you update shared state.

---

## Summary

- JOpt exposes progress via:
  - `onProgress(String winnerProgressString)` for human-readable updates,
  - `onProgress(IOptimizationProgress ...)` for structured, stage-aware integration.

- You can control default progress output frequency per stage using properties such as:
  - `JOpt.Algorithm.PreOptimization.SA.OnProgressOutPercentage`
  - `JOpt.Algorithm.GE.OnProgressOutPercentage`

- You can request progress at arbitrary times (e.g., wall-clock interval) using:
  - `opti.requestProgress()`

Together, these mechanisms provide precise control over observability, UI responsiveness, and runtime overhead.
