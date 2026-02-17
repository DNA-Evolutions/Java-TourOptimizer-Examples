# RequestResult — Pulling an Intermediate Solution While Optimization Is Still Running

For long-running optimizations, it is often valuable to fetch an **intermediate best-so-far solution** while the solver continues to run. Typical reasons include:

- show a usable route plan early in a UI,
- support “preview” or “save interim result” buttons,
- export a snapshot every N minutes,
- implement staged decision pipelines (e.g., early feasibility, later quality improvements).

JOpt supports this via **asynchronous result requests**:
- the optimization continues running,
- and you can request the current best solution to be delivered through a dedicated callback.

---

## References

- Example source: [RequestResultExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/requestresult/RequestResultExample.java)

---

## Two different result callbacks (critical distinction)

The example overrides two callbacks:

### 1) `onAsynchronousOptimizationResult(IOptimizationResult result)`
This callback is the “standard” asynchronous result callback.  
It is called when the optimization has a result to report via its normal lifecycle.

In the example:
```java
@Override
public void onAsynchronousOptimizationResult(IOptimizationResult rapoptResult) {
  System.out.println(rapoptResult);
}
```

### 2) `requestedAsynchronousOptimizationResult(IOptimizationResult result)`
This callback is invoked **only** when you explicitly request a result snapshot via `requestResult()`.

In the example:
```java
@Override
public void requestedAsynchronousOptimizationResult(IOptimizationResult rapoptResult) {
  System.out.println(rapoptResult);
}
```

Practical guidance:
- Use `requestedAsynchronousOptimizationResult(...)` for “snapshot” handling (UI preview, periodic export).
- Use `onAsynchronousOptimizationResult(...)` for the normal end-of-run or internal result reporting flow.

---

## How the example requests a snapshot

The snapshot request is triggered from the **structured progress callback**:

```java
@Override
public void onProgress(IOptimizationProgress winner) {

  if (winner.getProgress() == 20.0) {
    System.out.println("Requesting result ...");
    this.requestResult();
  }
}
```

Interpretation:
- As soon as the solver reports **20% progress** (for the current stage/caller), the code requests an intermediate result.
- The optimizer then responds by invoking:
  - `requestedAsynchronousOptimizationResult(IOptimizationResult ...)`

This is a clean pattern because:
- you request snapshots based on solver state,
- without stopping the optimization run.

---

## Running asynchronously (and why `resultFuture.get()` matters)

The example starts the solver asynchronously:

```java
CompletableFuture<IOptimizationResult> resultFuture = this.startRunAsync();
resultFuture.get();
```

Important:
- the `.get()` call blocks the main thread.
- Without blocking (or without another mechanism keeping the JVM alive), the program can terminate and the optimization would be aborted.

In production systems (services, UIs), you usually keep the process alive anyway; the key takeaway is:
- snapshot requests occur while the run is active.

---

## Recommended production patterns

### Pattern A — UI “Preview now” button
- The UI calls into your backend.
- Your backend triggers `requestResult()`.
- Handle the snapshot in `requestedAsynchronousOptimizationResult(...)` by:
  - serializing the current best solution,
  - returning it to the UI,
  - continuing optimization.

### Pattern B — Periodic snapshots (every N seconds)
Instead of requesting at “progress == 20.0”, you can request on a timer:
- every 2–10 seconds for interactive UIs,
- every 30–120 seconds for long-running batch jobs.

Important best practice:
- debounce requests (avoid multiple concurrent snapshot requests too close together).

### Pattern C — Event-based snapshot conditions
Request snapshots when:
- cost improves by more than X%,
- constraint violations drop to 0,
- a stage finishes,
- a maximum compute budget is reached.

This creates “meaningful snapshots” instead of purely periodic ones.

---

## Performance and safety considerations

### 1) Snapshot creation has cost
Delivering a full `IOptimizationResult` snapshot requires work:
- packaging the best entity,
- generating result data structures.

Therefore:
- do not request snapshots too frequently in very large instances.

### 2) Keep snapshot handlers lightweight
Inside `requestedAsynchronousOptimizationResult(...)`, avoid heavy work on the optimizer thread.
Preferred pattern:
- enqueue the result or a small derived summary,
- process exports or persistence asynchronously.

### 3) Be explicit about stage/context
In the example, the trigger condition uses:
- `winner.getProgress() == 20.0`

In production, you often want to refine this:
- check stage/caller (if available),
- ensure you only request once per stage,
- avoid “progress == 20” triggering repeatedly (depending on progress reporting behavior).

---

## Summary

- `requestResult()` lets you fetch an intermediate “best-so-far” solution **without stopping the optimization**.
- Requested snapshots arrive via:
  - `requestedAsynchronousOptimizationResult(IOptimizationResult ...)`
- Normal asynchronous results arrive via:
  - `onAsynchronousOptimizationResult(IOptimizationResult ...)`
- The example requests a snapshot at **20% progress** and prints it, while keeping the run alive via `startRunAsync().get()`.

This feature is a strong foundation for interactive planning UIs and operational “good solution now, better solution later” workflows.
