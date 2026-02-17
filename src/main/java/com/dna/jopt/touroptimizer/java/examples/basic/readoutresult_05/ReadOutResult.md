# Read Out an Optimization Result — `IOptimizationResult` in Practice

This document explains how to **extract structured information** from a JOpt TourOptimizer run result (`IOptimizationResult`) and turn it into something useful for:

- console diagnostics,
- human-readable reports (route headers, per-stop tables),
- automated checks (violations / feasibility),
- downstream export (CSV/JSON/KML).

It is based on these example implementations:

- [ReadOutResultExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic/readoutresult_05/ReadOutResultExample.java)
- [ReadOutResultWithHeadersExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic/readoutresult_05/ReadOutResultWithHeadersExample.java)

Additional background (recommended):

- **Analyzing the result** section of the beginner tutorial:  
  https://www.dna-evolutions.com/docs/getting-started/tutorials/first-optimization#analyzing-the-result

---

## Two complementary ways to “read” a result

### 1) Use `result.toString()` for debugging and reproducibility
The tutorial recommends saving the textual representation produced by `OptimizationResult.toString()` because it is extremely practical for later debugging and for comparing runs.

Use this when:
- you want a quick sanity check,
- you store logs for support/replay,
- you need a compact output without writing your own report logic.

### 2) Use the API for structured, machine-readable extraction
When you need more than a diagnostic dump—e.g., a table of planned arrivals, or a UI timeline—you should read the result object directly.

This document focuses on the structured approach, while still encouraging you to also store the `toString()` output in logs.

---

## Mental model: Result → Routes → Route items → Violations

The tutorial describes the result as:

- a **header** (job-level summary across all routes),
- multiple **route headers** (per-resource/per-working-hours summaries),
- an ordered list of **route elements** (the actual visit sequence with timestamps),
- optional **violations** (route-level and node-level).

The two Java examples implement exactly that extraction pattern.

---

## Example A: Basic structured extraction (`ReadOutResultExample`)

`ReadOutResultExample` shows the core technique without “pretty headers”.

### 1) Iterate routes
The example loops over all routes in the result:

- `result.getRoutes()`

### 2) Get route cost
For each route `r`, it prints a route cost:

- `result.getRouteCost(r)`

### 3) Read the ordered route items (your “stop list”)
The route’s visit sequence is retrieved via:

- `result.getOrderedRouteItems(r)`

This returns a list of `ILogicRouteElementDetailItem`. You can treat each item as “the next stop” in the route plan.

Typical data contained in those items (conceptually aligned with the tutorial’s per-node analysis):
- element id,
- arrival/departure timestamps,
- visit duration,
- travel time / travel distance from the previous element.

(Exact getters depend on the type of the detail item; the example prints each item directly.)

### 4) Collect violations
The example demonstrates two layers of violations:

**Route-level**:
- `result.getRouteViolations(r)`

**Node-level (per element id)**:
- `r.getRouteCostAndViolationController().getNodeViolations(d.getElement().getId())`

This pattern is ideal for:
- feasibility checks,
- SLA validation (late arrivals),
- reporting constraint breaches per stop.

---

## Example B: A readable report (`ReadOutResultWithHeadersExample`)

This example takes the same extraction and turns it into a **human-friendly console report**, modeled after the tutorial’s result header and route header breakdown.

### 1) Job header (“Result header”)
The example builds a result header using these job-level metrics:

- `result.getJobRouteCount()`
- `result.getJobElementCount()`
- `result.getJobCost()`

…and time/distance aggregates:

- `result.getJobTimeSeconds()`
- `result.getJobIdleTimeSeconds()`
- `result.getJobProductiveTimeSeconds()`
- `result.getJobTransitTimeSeconds()`
- `result.getJobDistance()`
- `result.getJobTerminationTime()`
- `result.getJobTerminationDistance()`

Important: The example converts these values by dividing by `1000` and `60/3600`, implying the underlying unit is **milliseconds** in that runtime context. If you integrate this into production code, verify the units in your JOpt version and normalize consistently.

### 2) Per-route header (“Route information”)
For each route, the example prints:

- route id (`r.getRouteId()`),
- assigned resource id (`r.getCurrentVisitingResource().getId()`),
- transit/productive/idle times via `r.getJoinedDetailController()`:
  - `getCurTransitTime()`
  - `getCurProductiveTime()`
  - `getCurIdleTime()`
  - plus an additional “White Idle time” field (also from the joined detail controller),
- route distance via:
  - `r.getJoinedDetailController().getCurDistance()`,
- termination (return-to-home) metrics:
  - `getTerminationTransitTime()`
  - `getTerminationTransitDistance()`

Conceptually, this corresponds to what the tutorial calls:
- route time,
- transit time,
- productive time,
- idle time,
- route distance,
- termination time/distance.

### 3) Route elements (the “stop list”)
It then prints the ordered route detail items:

- `List<ILogicRouteElementDetailItem> details = result.getOrderedRouteItems(r);`

and the violations (route-level and per-node), same as in the basic example.

---

## How to build your own “result reader” (recommended blueprint)

If you want a robust implementation you can reuse across environments, the following structure works well:

### Step 1 — Always persist `toString()` for debugging
Even if you parse the result structurally, keep a copy of the textual dump:
- it accelerates incident triage,
- it helps with comparing runs.

### Step 2 — Extract a job summary object
Populate a DTO with:
- total routes,
- scheduled routes (if you use that concept),
- total time/distance,
- cost figure-of-merit,
- termination totals.

This is the data your UI/dashboard typically wants “up top”.

### Step 3 — Extract per-route summaries
For each route:
- resource id,
- working-hours index (if applicable in your scenario),
- route start/stop (effective),
- transit/productive/idle time,
- distance,
- termination metrics.

### Step 4 — Extract per-stop details
From `getOrderedRouteItems(r)` build per-stop rows:
- element id,
- visiting index,
- arrival/departure,
- service duration,
- travel time/distance from previous element.

This is the data you want for:
- customer notifications,
- driver instructions,
- Gantt/timeline renderings.

### Step 5 — Extract violations and classify them
- route-level violations: “this route violates constraints overall”
- node-level violations: “this stop violates constraints specifically”

Then decide how to present them:
- as hard errors,
- as warnings,
- or as “exceptions requiring review”.

---

## Practical “interesting” checks you can implement immediately

These checks produce high value with minimal work:

1. **Late arrival audit**  
   Print any node violations, but group them by route and sort by severity.

2. **Idle-time hotspots**  
   Identify routes/stops with high idle time (often caused by tight time windows).

3. **Distance vs. productive ratio**  
   Compare transit time vs productive time per route; this quickly reveals unbalanced planning.

4. **Termination cost control**  
   Watch termination distance/time; it’s an easy indicator that the last stop is far from the home base.

---

## Common pitfalls

- **Not waiting for completion**: if you run asynchronously and the JVM exits, you will never see `onAsynchronousOptimizationResult(...)`. The examples explicitly block (`get()`).
- **Unit confusion**: always normalize time and distance units (milliseconds vs seconds; meters vs kilometers) consistently across your report.
- **Duplicate printing**: decide whether you want “pretty printed” headers, raw detail item prints, or both—otherwise logs can become noisy.

---

## Next steps

If you want to go beyond console output:

- Export the per-stop table to CSV (for analysts).
- Serialize a compact “route plan JSON” for APIs/clients.
- Export to KML (see the KML example) for map visualization.
- Build a UI timeline using arrival/departure timestamps.

