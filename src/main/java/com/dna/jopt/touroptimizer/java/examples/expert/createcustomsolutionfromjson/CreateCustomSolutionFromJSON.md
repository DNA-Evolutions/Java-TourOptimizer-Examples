# Load a JSON snapshot by hand for a custom solution

`CreateCustomSolutionFromJSONExample` loads the snapshot that `SaveOptimizationToJsonExample` writes into a second optimization, takes over its nodes, resources, node connector and properties by hand, and runs the elements again from scratch. It is the manual way of what `LoadOptimizationToFreshRunExample` does with one importer call, and it marks the place where your own code builds a custom solution (a warm start) from the loaded elements. The page is for developers who reuse a saved plan; run `SaveOptimizationToJsonExample` first, as [Load and save optimization snapshots](../../basic/io_03/LoadAndSaveOptimization.md) describes.

## What the example shows

A production planning system rarely starts from "nothing".

Typical situations:
- You already have a plan from yesterday.
- A dispatcher produced a manual plan in an external UI.
- You saved an intermediate plan for auditability or recovery.
- You want to rerun an optimization with the same input for debugging.

For these scenarios, JOpt allows you to load an optimization snapshot from JSON, extract the contained elements (nodes/resources), and then **build a custom solution (warm start)** from that data.

This example focuses on the **loading and extraction** layer, and shows where you plug in your own custom-solution construction logic.

(If you already use JOpt JSON save/load flows in your project, this example is the natural bridge to "use loaded data as a warm start".)

- **The manual way:** the example imports the file into a second optimization, takes the parts it wants one by one, and adds them to its own optimization. `LoadOptimizationToFreshRunExample` gets the same kind of start with one call of the importer, `importer.update(in, opti, ignoreLoadedSolution)` with `ignoreLoadedSolution` set to true, which loads everything into one optimization and drops the saved solution.
- **The handoff point** for your own code is marked in the source, right after the loading.
- The example prints the events of the run through the event API, the recommended way.

## Scenario

The data come from the snapshot `myopti.json.bz2` that `SaveOptimizationToJsonExample` writes into the working directory: eight nodes and the two resources Jack and John on March 6 and 7, 2020, time zone Europe/Berlin, with the properties of that run. The dates, times and coordinates are sample values.

## How the code works

The source is [CreateCustomSolutionFromJSONExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/createcustomsolutionfromjson/CreateCustomSolutionFromJSONExample.java). `example()` loads the snapshot and runs only if the loading worked:

```java
// The public evaluation license, or your own license if you put a license file where
// ExampleLicenseHelper looks for it.
ExampleLicenseHelper.setLicense(this);
if (!loadFromJson(this)) {
  return;
}
attachToObservables(this);
startAndPresentResult(this);
```

`loadFromJson` opens the file in a try-with-resources block, takes over the node connector and the properties, and adds the loaded nodes and resources:

```java
// YOUR TODO HERE THE CUSTOM SOLUTION CAN BE CREATED BY USING THE LOADED ELEMENTS
List<INode> nodes = getNodes(en);
List<IResource> ress = getResources(en);

// For now we simply add the elements - But you could create your own custom solution
opti.addNodes(nodes);
opti.addResources(ress);
return true;
```

If the file is missing, it prints the hint "JsonFile not existing. Run example 'SaveOptimizationToJsonExample.java' first." and returns false; if the import fails, it prints "JSON import failed: myopti.json.bz2". The example adds no properties of its own: the run uses the properties of the snapshot. `startAndPresentResult` starts the run, waits up to 5 minutes for the result and prints it.

## Output and what to look for

The snapshot holds a solution, but the example adds the loaded elements without their routes, so the run builds its own start solution. In every test run the construction algorithm started:

```text
StatusEvent(OPTIMIZATION_ALGO_STARTING, SmartConstructionAlgo)
```

and the result had all 10 elements of the snapshot, at the cost of the run that saved it; the header, without the line with the number of scheduled routes:

```text
 Number of Route         : 4
 Total Route Elements    : 10
 Cost                    : 847.033054487167
```

The node Aachen lies at the start point of Jack and John, so either of them can visit it at no extra driving, and the runs differ in that: in some, Jack visits Aachen alone and John the other seven nodes, so two routes are scheduled; in others, John visits all eight nodes and one route is scheduled. The cost is the same in every run. John's route on March 6 ends after his working hours (08:00 to 17:00) in every run, within his 14 hours: at 20:14:05 with 734.09 minutes when Jack visits Aachen, at 20:34:05 with 754.09 minutes when John visits it too. Time after the end of the working hours counts against the maximal working time, and the working hours are a frame for matching resources with the opening hours of the nodes, so a route can end after its working hours without a violation.

## What this example does (in plain language)

1. Load an optimization snapshot from a compressed JSON file:
   - `myopti.json.bz2`
   - The file must exist in the working directory first: run `SaveOptimizationToJsonExample` (package `basic.io_03`) before this example. If the file is missing, the example prints the hint "JsonFile not existing. Run example 'SaveOptimizationToJsonExample.java' first." and stops.
2. Extract an `IEntity` from the loaded snapshot
3. Pull out all nodes and resources found in that entity
4. Add those elements into the example's own optimization and run again

The key "handoff point" is explicitly marked in the source:

> `// YOUR TODO HERE THE CUSTOM SOLUTION CAN BE CREATED BY USING THE LOADED ELEMENTS`

This is where a production system would:
- reconstruct a route plan (`IEntity`) and
- inject it via `setInitialEntity(...)` (see the CustomSolution examples, [Custom solutions as a warm start](../customsolution/CustomSolution.md)).

## Loading: why the example uses a dummy optimization

The method:

- `loadEntityFromJson(FileInputStream jsonFile, IOptimization opti, boolean doLoadNodeConnector, boolean doLoadProperties)`

creates a temporary container:

- `IOptimization dummyOptimization = new Optimization();`

and then performs:

- `importer.update(jsonFile, dummyOptimization)`, which returns false if the import fails; the method then returns an empty `Optional`.

Interpretation:
- The JSON importer restores data into the dummy optimization instance.
- You then extract the **work entity** from that dummy instance:

- `IEntity en = dummyOptimization.getWorkEntity();`

This pattern is important because it cleanly separates the **deserialization concerns** (handled by the dummy optimization) from your active run optimization container.

## Optionally restoring the environment: NodeConnector and Properties

A serialized optimization can include not only nodes and routes, but also parts of the runtime environment. The example allows you to decide whether you want to load those too.

### NodeConnector restore (`doLoadNodeConnector`)
If enabled, the example copies the connector:

```java
opti.setNodeConnector(dummyOptimization.getNodeConnector());
```

Why this can matter:
- If the saved snapshot contains a precomputed connection store / matrix / connector configuration, restoring it can:
  - avoid recomputation,
  - ensure consistent distance/time values across re-runs,
  - preserve time-dependent connection logic.

When you might disable it:
- You want to re-evaluate distances/times using a new connector or updated routing data.
- You want to force recalculation to reflect changed travel conditions.

### Properties restore (`doLoadProperties`)
If enabled, the example copies user properties:

```java
Optional<Properties> propertiesOpt = dummyOptimization.getUserProperties();
if (doLoadProperties && propertiesOpt.isPresent()) {
  opti.addElement(propertiesOpt.get());
}
```

Why this can matter:
- Properties control optimization behavior (operators, weights, performance mode, etc.).
- Loading them ensures the rerun uses the same solver configuration as the original run, which is essential for:
  - reproducibility,
  - debugging,
  - regression tests.

When you might disable it:
- You intentionally want to rerun with a different property set (e.g., tuning, new weights).

## Extracting nodes and resources from the loaded entity

The example extracts elements from the loaded entity using:

- `en.getAllEntityElements()`

and filters by type:

- `instanceof INode`
- `instanceof IResource`

Then it calls:

```java
opti.addNodes(nodes);
opti.addResources(ress);
```

This "flat extraction" is useful for two reasons:

1. It makes the loaded data reusable even if you do not trust the loaded route order.
2. It enables a controlled rebuild where you may:
   - reassign nodes to different resources,
   - rebuild routes per day,
   - drop obsolete nodes,
   - insert new nodes,
   - or enforce additional constraints.

## How to turn loaded data into a custom solution (architecture)

The example intentionally stops short of building an `IEntity` warm start. In practice, you typically choose one of these strategies:

### Strategy A: "Re-run from scratch, but reuse environment"
You do this if you only want to reuse:
- node connector (distances/times),
- properties (solver configuration),
- and the set of nodes/resources.

Implementation:
- add nodes/resources (as in the example),
- do not set an initial entity.

This is the simplest "repeatable rerun" workflow. `LoadOptimizationToFreshRunExample` does it in one importer call.

### Strategy B: "Warm start from the loaded plan"
You do this when you want the solver to continue improving an existing plan.

Implementation approach:
1. Extract the planned routes from the loaded entity `en`.
2. Rebuild a new `IEntity`:
   - create routes,
   - bind each route to the right resource and working-hours index,
   - add nodes in the stored visit order.
3. Inject via:
   - `setInitialEntity(initialEntity)`
4. Add any *new* nodes/resources after that (incremental planning), for example with `addReassignNodes` and `addReassignResources`.

This is a common production pattern for:
- "continue planning from current plan",
- "insert late orders without rebuilding everything."

See also: the CustomSolution examples, [Custom solutions as a warm start](../customsolution/CustomSolution.md), for how to construct an entity.

### Strategy C: "Warm start + selective repair"
Use when the loaded plan is not guaranteed to be feasible under the current conditions.

Common cases:
- working hours changed,
- a resource is removed,
- constraints were tightened.

Implementation:
- rebuild entity but allow missing nodes to be unassigned initially,
- add them via reassign helpers,
- run optimization with a repair-friendly budget.

## Operational best practices

### Treat IDs as a compatibility contract
When you load from JSON and then merge with live data:
- node IDs and resource IDs must remain stable and unique.

If an ID collision occurs (same ID, different object):
- you can get incorrect reassignments or rejected elements.

### Decide upfront what must be "reproducible"
For debugging and acceptance workflows, define whether your rerun should reproduce:
- the same connector environment (distances/times),
- the same optimization properties,
- the same initial entity.

Then lock those decisions into your export/load process.

### Keep JSON files as audit artifacts
Storing a compressed JSON snapshot per run is valuable for:
- support cases,
- reproducible bug reports,
- and internal model tuning.

You can later load the snapshot and test:
- different properties,
- different constraints,
- or different solver versions.

## Summary

- This example loads an optimization snapshot from a compressed JSON file into a dummy optimization container.
- It extracts the `IEntity` and then optionally restores:
  - the NodeConnector (distance/time environment),
  - user Properties (solver configuration).
- It extracts nodes and resources from the entity and adds them to a new run.
- The explicit "TODO" marker in the example is where you reconstruct a true warm-start `IEntity` and inject it via `setInitialEntity(...)`.

This is the foundation for production workflows:
- reproducible reruns,
- warm-start continuation,
- incremental re-optimization with late orders.

## Run it

Run the `main(String[] args)` method of `SaveOptimizationToJsonExample` (package `basic.io_03`) first, then the `main(String[] args)` method of `CreateCustomSolutionFromJSONExample`, from your IDE and in the same working directory. You need a JDK in the version that the examples pom sets. You need no license file: without one, the examples use the public evaluation license (see the [license guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/license)).

## Related

- [Load and save optimization snapshots](../../basic/io_03/LoadAndSaveOptimization.md): `SaveOptimizationToJsonExample`, which writes the snapshot, and `LoadOptimizationToFreshRunExample`, the same fresh run in one importer call.
- [Custom solutions as a warm start](../customsolution/CustomSolution.md): how to build the `IEntity` and add more nodes and resources to it.
- [Backup connector feature guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/backupconnector#description-how-jopttouroptimizer-calculates-distances-and-times): related background on distance/time calculation and connectors.
- [Reactive events and streams](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/reactive_events): the feature guide of the event API that `attachToObservables` uses.
- [Recommended ways to run an optimization](../../basic/recommendedimplementation_02/RecommendedImplementation.md): the recommended run pattern, a synchronous run and the legacy callback methods.
- [Expert examples](../README.md): the overview of all Expert examples.

---

Added: 2026-02-17 (JOpt core 7.5.3-j17) | Last updated: 2026-10-23
