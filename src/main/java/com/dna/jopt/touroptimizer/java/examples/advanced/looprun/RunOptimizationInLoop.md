# RunOptimizationInLoop — Stopping a Solver Stage After N Loops

In advanced scenarios you may want to run the optimizer in a **controlled loop**, for example:

- run a fixed number of loops in a specific stage (e.g., Genetic phase),
- inspect intermediate results or metrics after each loop,
- stop early once you reached a quality threshold,
- chain the run into an application-specific pipeline.

`RunOptimizationInLoopExample` demonstrates a clean technique: **use progress callbacks to count loops and programmatically jump to the next stage** after a chosen number of loops.

---

## References

- Example source: [RunOptimizationInLoopExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/looprun/RunOptimizationInLoopExample.java)

---

## What the example does

### Goal
The example stops the **Genetic Algorithm stage** after **three loops**, then continues with the default flow.

Implementation intent in the code:

- `MAX_LOOP_COUNT_GE = 3`

### How it counts loops
The example increments a counter based on **progress events**:

- It listens to `onProgress(IOptimizationProgress rapoptProgress)`.
- It checks for:
  - `optimizationStage == 2` (genetic phase),
  - `progress == 99.0` (near end of a loop).

When both conditions match:

- `currentLoopCount++`

This effectively means:
- “Count one loop each time the genetic stage reports 99.0% progress.”

### How it stops the stage
When the counter reaches the configured threshold, the example calls:

- `jumpToNextStage();`

This instructs the solver pipeline to:
- stop the current stage and proceed to the next stage.

The output prints:
- “Done with loop: X of 3”
- “Max loop count reached. Stopping stage...”

---

## Why this pattern is useful

### 1) Deterministic compute budgeting
Instead of relying on:
- wall clock time,
- or a global generation count,
you can budget compute per stage, e.g.:
- “Run GE exactly 3 cycles.”
- “Run SA exactly 1 cycle.”
- “Stop after N construction attempts.”

### 2) Stage-specific control
Some instances respond well to:
- a short SA run,
- then more GE,
or vice versa.

Stage loop control allows you to align compute with:
- problem structure,
- business deadlines,
- or runtime SLAs.

### 3) Hook point for custom monitoring and decision rules
Once you have a loop boundary, you can:
- capture intermediate KPIs,
- track best-so-far improvements,
- stop early if improvement stalls,
- implement escalation logic (e.g., enable AutoFilter, change weights, add more SA).

---

## Understanding “stage” and “progress” in this example

### `rapoptProgress.getOptimizationStage()`
Stages are indexed numerically in progress events.  
In this example:
- stage `2` corresponds to the genetic phase (as indicated by the comment in the code).

### `rapoptProgress.getProgress()`
Progress is reported as a percentage-like number.  
The example uses:
- `99.0` to detect “end of loop”.

Important nuance:
- This is an implementation coupling to the solver’s progress reporting behavior.
- It is intentionally simple and works well for examples and many production cases.
- For mission-critical systems, you may want to additionally guard against:
  - repeated 99.0 events,
  - stage transitions,
  - or use a more explicit “loop end” indicator if your architecture provides one.

---

## Relationship to standard exit conditions

The example still configures normal exit conditions:

- `JOptExitCondition.JOptGenerationCount = 5000`
- `JOpt.Algorithm.PreOptimization.SA.NumIterations = 100000`
- `JOpt.Algorithm.PreOptimization.SA.NumRepetions = 1`

These remain relevant because:
- you are only stopping the stage earlier than its default exit,
- the solver still needs global stop rules for other stages.

Think of “loop run” as an additional control layer:
- ExitCondition limits the maximum,
- Loop control can stop earlier under your logic.

---

## Recommended production patterns

### Pattern A — Stop after N loops and export intermediate results
After each loop boundary:
- export `toOverviewResult()` for telemetry,
- optionally export KML for debugging,
- persist “best so far” for fail-safe.

### Pattern B — Adaptive stopping based on improvement
Replace the hard loop limit with:
- “Stop if best cost improved less than X% over the last N loops.”

You can implement this by:
- capturing `rapoptProgress` data,
- reading best result snapshots (if available),
- and calling `jumpToNextStage()` once the improvement stalls.

### Pattern C — Progressive tightening
You can run early loops with:
- relaxed weights,
- relaxed AutoFilter settings,
then tighten them after N loops.

This is often effective when you want:
- quick feasibility first,
- then quality improvements.

---

## Pitfalls and how to avoid them

### Pitfall 1 — Using a fragile loop boundary signal
The example uses `(stage == 2 && progress == 99.0)`.

If you find in your environment that:
- progress reports multiple times at 99.0,
- or progress granularity changes,
then add a guard, for example:
- remember last seen progress per stage,
- increment only on transition into 99.0,
- or require a time delta.

### Pitfall 2 — Stopping too early
Stopping GE too early may yield:
- a feasible but low-quality plan.

Mitigation:
- combine loop control with KPI-based stopping,
- or allow fallback: if result quality is poor, run additional loops.

### Pitfall 3 — Misinterpreting stages
Stage numbering is framework-specific.  
Do not hard-code stage indices without confirming which stage you are controlling in your integration.

In this example, stage `2` is explicitly commented as the genetic phase.

---

## Summary

- The example shows how to run optimization in a controlled loop by using progress callbacks.
- It counts “loops” of the genetic stage by watching for `stage == 2` and `progress == 99.0`.
- After `MAX_LOOP_COUNT_GE` loops (3), it calls `jumpToNextStage()` to exit the stage early.
- This pattern is valuable for deterministic compute budgeting, stage-specific control, and adaptive optimization pipelines.
