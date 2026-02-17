# Create Custom Solution From JSON — Reconstructing a Warm Start from a Serialized Optimization

A production planning system rarely starts from “nothing”.

Typical situations:
- You already have a plan from yesterday.
- A dispatcher produced a manual plan in an external UI.
- You saved an intermediate plan for auditability or recovery.
- You want to rerun an optimization deterministically for debugging.

For these scenarios, JOpt allows you to load an optimization snapshot from JSON, extract the contained elements (nodes/resources), and then **build a custom solution (warm start)** from that data.

This example focuses on the **loading and extraction** layer, and shows where you plug in your own custom-solution construction logic.

---

## References

- Example source: [CreateCustomSolutionFromJSONExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/createcustomsolutionfromjson/CreateCustomSolutionFromJSONExample.java) 

Related background:
- Distance/time calculation and connectors: https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/backupconnector#description-how-jopttouroptimizer-calculates-distances-and-times

(If you already use JOpt JSON save/load flows in your project, this example is the natural bridge to “use loaded data as a warm start”.)

---

## What this example does (in plain language)

1. Load an optimization snapshot from a compressed JSON file:
   - `myopti.json.bz2`
2. Extract an `IEntity` from the loaded snapshot
3. Pull out all nodes and resources found in that entity
4. Add those elements into a new optimization instance and run again

The key “handoff point” is explicitly marked in the source:

> `// YOUR TODO HERE THE CUSTOM SOLUTION CAN BE CREATED BY USING THE LOADED ELEMENTS`

This is where a production system would:
- reconstruct a route plan (`IEntity`) and
- inject it via `setInitialEntity(...)` (see the CustomSolution examples).

---

## Loading: why the example uses a dummy optimization

The method:

- `loadEntityFromJson(FileInputStream jsonFile, IOptimization opti, boolean doLoadNodeConnector, boolean doLoadProperties)`

creates a temporary container:

- `IOptimization dummyOptimization = new Optimization();`

and then performs:

- `importer.update(jsonFile, dummyOptimization);`

Interpretation:
- The JSON importer restores data into the dummy optimization instance.
- You then extract the **work entity** from that dummy instance:

- `IEntity en = dummyOptimization.getWorkEntity();`

This pattern is important because it cleanly separates:
- **deserialization concerns** (handled by the dummy optimization),
- from your active run optimization container.

---

## Optionally restoring the environment: NodeConnector and Properties

A serialized optimization can include not only nodes and routes, but also parts of the runtime environment. The example allows you to decide whether you want to load those too.

### 1) NodeConnector restore (`doLoadNodeConnector`)
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

### 2) Properties restore (`doLoadProperties`)
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

---

## Extracting nodes and resources from the loaded entity

The example extracts elements from the loaded entity using:

- `en.getAllEntityElements()`

and filters by type:

- `instanceof INode`
- `instanceof IResource`

Then it calls:

```java
this.addNodes(nodes);
this.addResources(ress);
```

This “flat extraction” is useful for two reasons:

1. It makes the loaded data reusable even if you do not trust the loaded route order.
2. It enables a controlled rebuild where you may:
   - reassign nodes to different resources,
   - rebuild routes per day,
   - drop obsolete nodes,
   - insert new nodes,
   - or enforce additional constraints.

---

## How to turn loaded data into a custom solution (architecture)

The example intentionally stops short of building an `IEntity` warm start. In practice, you typically choose one of these strategies:

### Strategy A — “Re-run from scratch, but reuse environment”
You do this if you only want to reuse:
- node connector (distances/times),
- properties (solver configuration),
- and the set of nodes/resources.

Implementation:
- add nodes/resources (as in the example),
- do not set an initial entity.

This is the simplest “repeatable rerun” workflow.

### Strategy B — “Warm start from the loaded plan”
You do this when you want the solver to continue improving an existing plan.

Implementation approach:
1. Extract the planned routes from the loaded entity `en`.
2. Rebuild a new `IEntity`:
   - create routes,
   - bind each route to the right resource and working-hours index,
   - add nodes in the stored visit order.
3. Inject via:
   - `setInitialEntity(initialEntity)`
4. Add any *new* nodes/resources after that (incremental planning).

This is the standard production pattern for:
- “continue planning from current plan”,
- “insert late orders without rebuilding everything.”

See also: the CustomSolution examples for how to construct an entity.

### Strategy C — “Warm start + selective repair”
Use when the loaded plan is not guaranteed to be feasible under the current conditions.

Common cases:
- working hours changed,
- a resource is removed,
- constraints were tightened.

Implementation:
- rebuild entity but allow missing nodes to be unassigned initially,
- add them via reassign helpers,
- run optimization with a repair-friendly budget.

---

## Operational best practices

### 1) Treat IDs as a compatibility contract
When you load from JSON and then merge with live data:
- node IDs and resource IDs must remain stable and unique.

If an ID collision occurs (same ID, different object):
- you can get incorrect reassignments or rejected elements.

### 2) Decide upfront what must be “reproducible”
For debugging and acceptance workflows, define whether your rerun should reproduce:
- the same connector environment (distances/times),
- the same optimization properties,
- the same initial entity.

Then lock those decisions into your export/load process.

### 3) Keep JSON files as audit artifacts
Storing a compressed JSON snapshot per run is extremely valuable for:
- support cases,
- reproducible bug reports,
- and internal model tuning.

You can later load the snapshot and test:
- different properties,
- different constraints,
- or different solver versions.

---

## Summary

- This example loads an optimization snapshot from a compressed JSON file into a dummy optimization container.
- It extracts the `IEntity` and then optionally restores:
  - the NodeConnector (distance/time environment),
  - user Properties (solver configuration).
- It extracts nodes and resources from the entity and adds them to a new run.
- The explicit “TODO” marker in the example is where you reconstruct a true warm-start `IEntity` and inject it via `setInitialEntity(...)`.

This is the architectural foundation for production-grade workflows:
- reproducible reruns,
- warm-start continuation,
- incremental re-optimization with late orders.
