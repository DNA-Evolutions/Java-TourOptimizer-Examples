# First Optimization — `FirstOptimizationExample`

A compact “first run” example for **JOpt TourOptimizer (Java)**: define a small set of time-windowed geo nodes, add a single capacity resource, tune a few solver properties, subscribe to runtime events, and print the resulting plan.

## Links

- Example source (GitHub):  
  [FirstOptimizationExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic/firstoptimization_01/)

- Tutorial walk-through:  
  https://www.dna-evolutions.com/docs/learn-and-explore/base-examples/firstoptimizationexample

---

## What happens when you run it

Think of the execution flow as a short pipeline:

1. **License is applied** (example/free mode helper).
2. **Solver properties** are registered.
3. **Nodes** (places to visit) are registered.
4. **Resources** (people/vehicles) are registered.
5. **Observers** are attached (progress, warnings, status, errors).
6. The optimizer starts asynchronously and the code **waits for up to 5 minutes** for a result.
7. The **result object is printed** to stdout.

This is implemented in `example()` by calling five helper methods in order:
- `addProperties(...)`
- `addNodes(...)`
- `addResources(...)`
- `attachToObservables(...)`
- `startAndPresentResult(...)`

---

## A quick “code tour” (by method)

### `main(...)` and `example()`: the orchestration layer

- `main(...)` simply instantiates the class and calls `example()`.
- `example()` contains the full run sequence and is the best entry point when you want to modify the scenario.

Notable behavior:
- The example applies a license using `ExampleLicenseHelper.setLicense(this);` and notes that it is using a free/example mode approach.

### `addProperties(...)`: the runtime knobs

This example sets four properties:

- `JOptExitCondition.JOptGenerationCount = 1000`  
  Controls how many generations the evolutionary phase is allowed to run.

- `JOpt.Algorithm.PreOptimization.SA.NumIterations = 100000`  
  Controls the number of iterations for the simulated annealing pre-optimization.

- `JOpt.Algorithm.PreOptimization.SA.NumRepetions = 1`  
  Number of pre-optimization repetitions (note the spelling is exactly as in the source key).

- `JOpt.NumCPUCores = 4`  
  Provides a hint regarding CPU parallelism.

Practical reading: these values intentionally keep the example deterministic and “busy enough” to show progress updates, but not so heavy that it becomes inconvenient during development.

### `addNodes(...)`: the demand side (what must be visited)

The example builds a shared **opening-hours list** spanning two calendar days (Europe/Berlin timezone):

- May 6, 2020: 08:00–17:00  
- May 7, 2020: 08:00–17:00

All nodes share:
- `visitDuration = 20 minutes`
- `importance = 1`

Then seven nodes are created as `TimeWindowGeoNode` objects and added:

- Koeln (50.9333, 6.95)
- Essen (51.45, 7.01667)
- Dueren (50.8, 6.48333)
- Nuernberg (49.4478, 11.0683)
- Heilbronn (49.1403, 9.22)
- Wuppertal (51.2667, 7.18333)
- Aachen (50.775346, 6.083887)

Interpretation: this is a small, geographically distributed node set that is large enough to require trade-offs, but small enough to remain readable.

### `addResources(...)`: the supply side (who can perform visits)

Working hours mirror the node opening hours (two days, 08:00–17:00 in Europe/Berlin).

One `CapacityResource` is created:

- Name: **Jack from Aachen**
- Start/home location: Aachen (50.775346, 6.083887)
- `maxWorkingTime = 9 hours`
- `maxDistanceKmW = 1200 km` (configured as a unit-aware quantity)

Interpretation: one resource is often the simplest way to validate that your modeling is correct before introducing multi-resource and balancing effects.

### `attachToObservables(...)`: making the run “feel alive”

The example subscribes to four event streams and prints each event to the console:

- progress
- warnings
- status
- errors

This is helpful when you are learning:
- you can see whether the solver is still active,
- what the solver is trying to improve,
- and whether there are modeling or feasibility issues.

### `startAndPresentResult(...)`: run, wait, print

The optimizer is started asynchronously (`startRunAsync()`), then the code blocks:

- waits **up to 5 minutes** via `get(5, TimeUnit.MINUTES)`,
- prints the returned `IOptimizationResult`.

Why the blocking wait matters:
- if your process exits early, the asynchronous optimization run is terminated with it.
- a time-bounded wait also prevents “hanging forever” if something is misconfigured.

---

## How to run

Run the `main(String[] args)` method of `FirstOptimizationExample`.

Notes:
- The `main(...)` signature declares: `InterruptedException`, `ExecutionException`, `InvalidLicenceException`, `IOException`, `TimeoutException`.
- If the run exceeds the 5-minute wait window, you will receive a `TimeoutException`.

---

## Making it your own (common edits)

### Change problem size
- Add/remove nodes (more nodes typically increases runtime and solution complexity).
- Change visit durations (e.g., 5 min vs 60 min changes feasibility dramatically).

### Change constraints
- Tighten working hours or opening hours to test feasibility boundaries.
- Adjust `maxWorkingTime` and `maxDistanceKmW` to model operational limits.

### Change solver effort
- Lower `JOptExitCondition.JOptGenerationCount` for quicker feedback loops.
- Lower SA iterations to reduce pre-optimization time.

### Change observability
- Keep progress subscription during development.
- Consider disabling verbose output when embedding into an application or CI.

---

## Troubleshooting checklist

- **License errors**: review `ExampleLicenseHelper` used by the example.
- **Timeout**: increase the 5-minute wait (or reduce generations / SA iterations).
- **No meaningful result**: verify time windows overlap (resource working hours vs node opening hours).
- **Unexpected infeasibility**: check max working time and max distance relative to geography and visit durations.

---

## Next steps

Once you understand this example, the natural next progression is:
- multiple resources,
- different node types or constraints,
- capacity/skills/compatibility constraints,
- alternative objective weighting and more advanced properties.

The tutorial linked above provides the architectural narrative and explains the main modeling concepts in depth.
