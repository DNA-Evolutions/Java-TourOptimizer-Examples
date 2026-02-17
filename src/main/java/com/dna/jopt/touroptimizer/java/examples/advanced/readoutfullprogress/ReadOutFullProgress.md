# Read Out Full Progress — Structured KPIs During Optimization

JOpt.TourOptimizer can emit much richer information than a simple “percent done” number.  
If you subscribe to the progress stream, you can read out **live KPIs** for the *current best (winner) solution* while the optimizer is still running.

This is essential for:
- live dashboards (web UIs, planning workbenches),
- observability and telemetry (metrics + logs),
- early stopping / loop control (“stop if improvement stalls”),
- exporting “best-so-far” solutions during long runs.

This document covers two approaches shown in the examples:
1. **Manual extraction** (compute a progress string yourself from `IOptimizationProgress` + winner `IEntity`).
2. **Using the helper utility** `ParsedProgress` (recommended for maintainability and consistency).

---

## References

### Examples
- ReadOutFullProgressExample.java:  
  https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/readoutfullprogress/ReadOutFullProgressExample.java  

- ReadOutFullProgressWithUtilExample.java:  
  https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/readoutfullprogress/ReadOutFullProgressWithUtilExample.java  

### Utility
- ParsedProgress.java:  
  https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/util/progressparser/ParsedProgress.java 

---

## What “full progress” means in this context

Progress updates are emitted as `IOptimizationProgress`.

From that progress object you can read:

### 1) Meta progress information
- **Progress** (`p.getProgress()`): the current stage progress percentage.
- **Caller ID** (`p.getCallerId()`): a stage/caller identifier for where the progress originates.

Practical use:
- show “SA 32%” / “GE 84%” in a UI,
- label metrics by stage,
- implement stage-specific logic (e.g., stop GE after N loops).

### 2) Winner (best-so-far) solution metrics
- **Winner entity** (`p.getResultEntity()`): the current best solution.
- From the winner’s detail controller (`winner.getJoinedDetailController()`), you can extract live KPIs such as:
  - transit time, idle time, productive time,
  - total distance,
  - termination transit metrics (see below).

These KPIs represent what you actually care about operationally:
- how much the plan improved,
- how efficient the tours are,
- whether the optimization is converging.

---

## Approach 1 — Manual KPI extraction (ReadOutFullProgressExample)

`ReadOutFullProgressExample` demonstrates the direct approach:
- subscribe to progress updates,
- read the winner entity,
- compute and format a KPI string yourself.

### The core pattern
1. Read the winner:
- `IEntity winner = p.getResultEntity();`

2. Build a “total route time excluding flex time”:
- transit time + idle time + productive time

3. Compute utilization:
- `productiveTime / (transit + idle + productive)`

4. Print a compact status line such as:

- PC: progress percent
- AL: caller id
- JC: joined cost
- RC: route count
- EC: optimizable element count
- TC: total element count (routeCount + elementCount)
- TT[h]: total time (hours)
- TU[%]: utilization
- TD[km]: total distance

### Why this is useful
This approach is:
- very transparent,
- easy to customize,
- and ideal if you want a very specific formatting for your UI or logs.

### Limitations
As soon as you need more KPIs (flex time, termination transit, etc.), manual extraction can become:
- repetitive,
- inconsistent across projects,
- and easy to get wrong.

That is why the repository also includes a utility.

---

## Approach 2 — Use the ParsedProgress helper (recommended)

`ReadOutFullProgressWithUtilExample` demonstrates a cleaner integration:

- create a `ParsedProgress` object from the progress event:
  - `ParsedProgress pp = new ParsedProgress(p);`

- print a richer KPI line assembled from typed getters.

### What ParsedProgress provides (as used in the example)

#### Primary meta information
- `pp.getProgress()` — progress percent
- `pp.getCallerId()` — caller/stage label

#### Global objective and size
- `pp.getCost()` — joined (abstract) cost
- `pp.getRouteCount()` — number of routes
- `pp.getOptimizableElementsCount()` — number of optimizable elements
- `pp.getElementsCount()` — total elements (routes + elements)

#### Time breakdown
- `pp.getTime()` — total time (computed as transit + idle + productive; excludes flex time)
- `pp.getProductiveTime()` — productive time
- `pp.getIdleTime()` — idle time
- `pp.getFlexTime()` — flex time
- `pp.getTransitTime()` — transit time
- `pp.getTerminationTransitTime()` — termination transit time

#### Distance
- `pp.getDistance()` — total distance
- `pp.getTerminationTransitDistance()` — termination transit distance

#### Utilization
- `pp.getUtilization()` — `productiveTime / (productive + idle + transit)` (flex time excluded)

### What the example prints
The example constructs a line containing:

- progress + stage label
- cost and counts
- time breakdown (total, productive, idle, flex, transit, termination transit)
- utilization
- distance

This is an excellent “single-line telemetry output” for:
- logs,
- live terminals,
- and quick dashboards.

### Why this approach is usually better
- You get a consistent definition of KPIs across examples and projects.
- It is easier to add more KPIs later without duplicating logic.
- It avoids mixing formatting logic with metric extraction logic.

---

## Understanding the time components

While naming is self-explanatory, these distinctions matter in production:

### Transit Time
Time spent traveling between nodes (including start/termination legs, depending on model and metric definition).

### Productive Time
Time spent performing service/visit work at nodes (visit duration, service durations, etc.).

### Idle Time
Waiting time that happens due to constraints, for example:
- arriving early and waiting for OpeningHours,
- scheduling gaps created by time windows.

### Flex Time
Flex time is typically the time flexibility or slack that results from time windows and scheduling latitude.  
In dashboards, it is often useful because:
- high flex time can indicate robustness (schedule has buffer),
- or inefficiency (too much waiting due to poor sequencing).

### Termination Transit
Termination transit metrics relate to the “route termination” leg(s) in your model.  
This is relevant especially when:
- routes are closed (return to depot), or
- termination points are explicitly modeled.

In practice:
- if you care about “deadheading back to base”, termination transit is a key KPI.

---

## Subscribing to progress events (Observable pattern)

The util example demonstrates how to subscribe to optimization events using the optimizer’s event subjects:

- `progressSubject()` — progress stream
- `warningSubject()` — warnings
- `statusSubject()` — status updates
- `errorSubject()` — errors

This is a production-friendly pattern because it separates:
- the optimization engine,
- and the integration layer (UI/logging/monitoring).

---

## Best practices for production usage

### 1) Do not log too frequently
Progress callbacks can fire very often. Excessive output can slow down the run.

Combine this doc with the “OptimizationProgress” pattern:
- configure `OnProgressOutPercentage`,
- and/or request progress at a controlled wall-clock frequency.

### 2) Keep callback work minimal
In `progressSubject().subscribe(...)`, avoid heavy work such as:
- exporting KML on every update,
- large JSON serialization,
- database writes.

Instead:
- push to a queue,
- aggregate,
- and process asynchronously.

### 3) Track best-so-far improvements
If your UI needs “improvement over time” charts:
- store snapshots of (cost, distance, time) every N seconds,
- not every progress event.

### 4) Stage-aware monitoring
Use `callerId` to:
- segment metrics by stage,
- detect “we are stuck in a stage”,
- apply stage-specific early stopping rules.

---

## Summary

- Full progress in JOpt is read from `IOptimizationProgress`, including the current best winner solution.
- You can extract a compact KPI string manually (ReadOutFullProgressExample) or use `ParsedProgress` for a richer and more maintainable KPI set (recommended).
- The util example prints a detailed line including cost, counts, time breakdown (productive/idle/flex/transit/termination), utilization, and distance.
- This integration is central for observability, dashboards, and intelligent run control in long optimizations.
