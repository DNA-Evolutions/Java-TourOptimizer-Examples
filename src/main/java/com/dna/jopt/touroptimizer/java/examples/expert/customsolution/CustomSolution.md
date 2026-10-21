# Custom solutions as a warm start

`CustomSolutionExample` builds two routes by hand, sets them as the initial entity and lets the optimizer improve them; `CustomSolutionWithAdditionalNodesAndResourcesExample` adds more nodes and resources to such a custom solution before the search starts. In both test runs the optimizer changed the given order of the nodes, so the custom solution is a starting point that the optimizer may change. The page is for developers who start a run from an existing plan; it covers the architecture, warm-start patterns and practical integration. [Load a JSON snapshot by hand for a custom solution](../createcustomsolutionfromjson/CreateCustomSolutionFromJSON.md) shows how to take the elements of a saved plan.

## What the example shows

A **custom solution** (also called *warm start*, *seed solution*, or *initial entity*) is an explicitly constructed solution that you inject into the optimizer **before** the search starts.

This capability is central for production deployments where you want the solver to:

- continue from an existing plan (yesterday's routes, dispatcher plan, manual plan),
- re-optimize partially after disruptions (new jobs, cancellations, resource changes),
- run controlled "what-if" scenarios without restarting from scratch,
- increase end-user acceptance by starting from a familiar plan and improving it incrementally.

In JOpt.TourOptimizer, a "solution" is an **`IEntity`** that contains one or more **routes** (`ILogicEntityRoute`), each route referencing a **resource** and a sequence of **optimizable elements** (nodes).

- **`CustomSolutionExample`** builds an `IEntity` with two routes of the resource Jack, one per working day, and sets it with `setInitialEntity`.
- **`CustomSolutionWithAdditionalNodesAndResourcesExample`** builds the same entity and then adds a resource, three nodes and a pillar with `addReassignResources` and `addReassignNodes`, plus a resource and a node whose ids the custom solution already uses, on purpose.
- **Both** print the events of the run through the event API, the recommended way, and write the result to a KML file named after the class.

## Scenario

The dates, times and coordinates are sample values. The time zone is Europe/Berlin.

- **Resource:** `Jack`, a `CapacityResource` at 50.775346, 6.083887 (Aachen), with working hours on May 6 and May 7, 2020, from 08:00 to 17:00. He may work up to 13 hours and drive up to 1200 km a day.
- **Custom solution:** the first route (May 6) visits Koeln, Dueren and Essen in that order, the second (May 7) Nuernberg, Heilbronn and Stuttgart. The six `TimeWindowGeoNode` objects are open on both days from 08:00 to 17:00; each visit takes 20 minutes.
- **Additions of the second class:** the resource `JackNew` at the same point, working on May 10 and May 11 from 08:00 to 17:00 with the same limits; the nodes Koeln2, Dueren2 and Essen2, open on those two days; the pillar `testPillarKoeln` at the Koeln point, on May 11 from 09:00 to 10:00; and a second resource `Jack` and a second node `Essen`, both with the dates of the additions.
- **License:** the second class has more elements than JOpt runs without a license; `ExampleLicenseHelper` sets the public evaluation license, which covers it (see the [license guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/license)).

## How the code works

The examples in this package, each with its source on GitHub: [CustomSolutionExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/customsolution/CustomSolutionExample.java) and [CustomSolutionWithAdditionalNodesAndResourcesExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/customsolution/CustomSolutionWithAdditionalNodesAndResourcesExample.java). `example()` of the second class; the first has the same steps without `addReassignElements`:

```java
// The public evaluation license, or your own license if you put a license file where
// ExampleLicenseHelper looks for it.
ExampleLicenseHelper.setLicense(this);
addProperties(this);
addInitialSolution(this);
addReassignElements(this);
attachToObservables(this);
startAndPresentResult(this);
```

`addProperties` sets the run values and switches off the construction:

```java
// Fewer generations and iterations than the defaults keep the run short.
props.setProperty("JOptExitCondition.JOptGenerationCount", "1000");
props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumIterations", "100000");
// No construction of a start solution: the custom solution is the start. A custom solution
// skips the construction anyway; FALSE skips it also when no solution is set.
props.setProperty("JOpt.Assisted", "FALSE");
```

`addInitialSolution` builds the entity with `createInitialSolution` and sets it with `opti.setInitialEntity(initialEntity)`. `startAndPresentResult` starts the run, waits up to 5 minutes for the result, prints it and writes it to the KML file. The sections below show the entity and the additions.

## Output and what to look for

### CustomSolutionExample

In two test runs the run used the custom solution and started only the simulated annealing and the genetic evolution, no construction:

```text
StatusEvent(OPTIMIZATION_USING_PROVIDED_SOLUTION, Using a provided solution as starting point for the optimization.)
StatusEvent(OPTIMIZATION_ALGO_STARTING, SmartSimulatedAnnealingAlgo)
```

The result keeps each node on its day but changes the order on both days:

```text
0.0 Dueren / Geo  / Arrival: 06.05.2020 08:21:21 (Europe/Berlin) , Departure: 06.05.2020 08:41:21 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 21.36 [min], 28.2 [km]
0.1 Koeln / Geo  / Arrival: 06.05.2020 09:08:36 (Europe/Berlin) , Departure: 06.05.2020 09:28:36 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 27.25 [min], 35.97 [km]
0.2 Essen / Geo  / Arrival: 06.05.2020 10:12:15 (Europe/Berlin) , Departure: 06.05.2020 10:32:15 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 43.64 [min], 57.61 [km]
```

```text
1.0 Nuernberg / Geo  / Arrival: 07.05.2020 12:47:54 (Europe/Berlin) , Departure: 07.05.2020 13:07:54 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 287.91 [min], 380.04 [km]
1.1 Stuttgart / Geo  / Arrival: 07.05.2020 15:05:56 (Europe/Berlin) , Departure: 07.05.2020 15:25:56 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 118.03 [min], 155.8 [km]
1.2 Heilbronn / Geo  / Arrival: 07.05.2020 15:57:27 (Europe/Berlin) , Departure: 07.05.2020 16:17:27 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 31.52 [min], 41.6 [km]
```

The job cost (`JC`) in the progress lines is 1171.49, the cost of the result, from the first line of the simulated annealing to the end. The route of May 7 ends at 19:58:18, after Jack's working hours, with 718.31 minutes of his 13 hours. Overtime counts against the maximal working time, and the working hours are a frame for matching resources with the opening hours of the nodes, so a route can end after its working hours without a violation.

### CustomSolutionWithAdditionalNodesAndResourcesExample

In two test runs the core added the further elements to the custom solution and reported both duplicates:

```text
StatusEvent(ADDITIONAL_ELEMENT, Adding additional elements to be reassigned to optimization. Reassigning Resources: JackNew, Jack)
WarningEvent(WarningEvent(5557, The element is already present in the solution.), The element is already present in the solution. Duplicate res ignored: Jack)
StatusEvent(ADDITIONAL_ELEMENT, Adding additional elements to be reassigned to optimization. Reassigning Nodes: Koeln2, Dueren2, Essen2, testPillarKoeln, Essen)
WarningEvent(WarningEvent(5557, The element is already present in the solution.), The element is already present in the solution. Duplicate node ignored: Essen)
```

The result plans Jack on May 6 with Dueren, Koeln, Nuernberg, Heilbronn and Stuttgart, and leaves his route of May 7 empty. The route of May 6 ends at 20:57:28, with 777.47 minutes of his 13 hours, without a violation. Overtime counts against the maximal working time, and the working hours are a frame for matching resources with the opening hours of the nodes, so a route can end after its working hours without a violation. JackNew works on May 11; the route without the two lines of `Essen` and `Essen2`:

```text
3.0 testPillarKoeln / Pillar  / Geo  / Idle: 12.01 [min] / Arrival: 11.05.2020 08:47:59 (Europe/Berlin) , Departure: 11.05.2020 10:00:00 (Europe/Berlin) / Duration: 60.0 [min] / Driving: 47.99 [min], 63.34 [km]
3.1 Koeln2 / Geo  / Arrival: 11.05.2020 10:00:00 (Europe/Berlin) , Departure: 11.05.2020 10:20:00 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 0.0 [min], 0.0 [km]
3.4 Dueren2 / Geo  / Arrival: 11.05.2020 12:45:05 (Europe/Berlin) , Departure: 11.05.2020 13:05:05 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 61.46 [min], 81.12 [km]
```

Between Koeln2 and Dueren2 JackNew visits `Essen` and `Essen2`, which share a place, so their order can change between runs: the first of the two is reached at 11:03:38, the second at 11:23:38. The node `Essen` is visited on May 11, without a violation, and no route of Jack has a node `Essen`, although the `Essen` of the custom solution is open only on May 6 and May 7. The pillar takes its whole window of 60 minutes; JackNew arrives early and waits 12.01 minutes.

## The solution architecture in JOpt (mental model)

Think of the optimizer as two layers:

### Problem definition layer (static inputs)
You define:
- nodes (`INode`) and their constraints (opening hours, skills, relations, ...),
- resources (`IResource`) with working hours and capacities,
- cost model and optimization properties,
- optional connection data (distance/time).

### Solution layer (dynamic arrangement)
You define (or let the solver create):
- an `IEntity` consisting of
  - multiple `ILogicEntityRoute` routes,
  - each with a visiting resource + working-hours index,
  - and an ordered list of optimizable elements.

A simplified view. A sketch, not code of the example classes:

```text
Optimization (problem container)
+-- IEntity (a solution candidate)
    +-- Route 0  (ILogicEntityRoute)
    |   +-- Visiting Resource: Jack (working day index 0)
    |   +-- Start / Termination (derived from resource)
    |   +-- Optimizable elements: [NodeA, NodeB, NodeC, ...]
    +-- Route 1  (ILogicEntityRoute)
        +-- Visiting Resource: Jack (working day index 1)
        +-- Optimizable elements: [NodeX, NodeY, ...]
```

A **custom solution** means: you construct the right-hand side (the `IEntity`) explicitly and set it as the initial entity.

## How to inject a custom solution (the essential sequence)

Both examples follow the same high-level sequence:

1. Configure license and properties
2. Create an initial entity (`IEntity`)
3. Inject it via `setInitialEntity(entity)`
4. Start optimization

The second example adds its further nodes and resources between steps 3 and 4.

Minimal schematic. A sketch, not code of the example classes:

```java
IEntity initial = createInitialSolution();
setInitialEntity(initial);
startRunAsync().get();
```

### Why the properties matter
In the examples, assisted construction is disabled:

```java
props.setProperty("JOpt.Assisted", "FALSE");
```

Interpretation:
- you are explicitly telling the optimizer: "do not build your own construction solution; I am providing the starting point."
- this is typically what you want for true warm-start workflows.
- With a custom solution the optimizer skips the construction anyway: `setInitialEntity` marks the entity as set by the user, and in both test runs of each class only the simulated annealing and the genetic evolution started. `JOpt.Assisted` set to FALSE skips the construction also when no solution is set.

## Building an `IEntity` correctly (what "valid" means)

The solver can repair certain inconsistencies, but a robust custom solution should be **structurally consistent**.

### Route must be bound to a resource and a working-hours index
In `CustomSolutionExample`, two routes are bound to the same resource but different working days. The first route:

```java
firstRoute.setCurrentVisitingResource(myRes, 0);
firstRoute.setRouteStart(myRes);
firstRoute.setRouteTermination(myRes);
firstRoute.addAllToOptimizableElements(getNodeSet1());
firstRoute.setRouteId(0);
```

The second route does the same with `secondRoute.setCurrentVisitingResource(myRes, 1)`, `getNodeSet2()` and `setRouteId(1)`. Both examples give each route its working-day index as route id. A run of the second class without `setRouteId` printed warning 5004 ("The indexing of the dutyhours of the route ids is wrong") and the status `ENTITY_CORRECTION_ATTEMPT_SUCESS`: the core corrected the entity before the run.

This is the recommended way to model:
- multi-day planning,
- or multiple shifts for the same person/vehicle.

### Route start and termination should be defined
Each route of the examples uses `setRouteStart(myRes)` and `setRouteTermination(myRes)`, as in the block above.

This ensures the route has a valid start and end reference.
(How "end" is interpreted depends on your open/closed route modeling; see the *OpenClosedRoute* example, [Open and closed routes](../../advanced/openclosedroute/OpenClosedRoute.md), and documentation.)

### Add the optimizable elements in the planned order
You then insert nodes in the exact order you want to seed, with `addAllToOptimizableElements` and the list of nodes for this route (`getNodeSet1()` for the first route).

Finally:
```java
IEntity entity = new Entity();
entity.addRoute(firstRoute);
entity.addRoute(secondRoute);
```

## What a custom solution is (and what it is not)

### It is a starting point, not a hard constraint
A custom solution does **not** freeze the plan by default. It only provides the initial state from which search begins. In the example runs the optimizer reorders both routes of `CustomSolutionExample`, and in the second class it moves all nodes of Jack's second day to his first.

If you want parts of the solution to be immutable (e.g., "these nodes are fixed at the beginning"), use architectural features such as:
- Pillar/Captured nodes,
- specific relations,
- or other structural modeling tools (depending on the rule you need).

### Hard constraints are not enforced by "high cost"
A recurring mistake is to simulate feasibility constraints by "making violations very expensive".
In JOpt, many violations, such as late arrivals and overtime, add a weighted cost: they are pseudo constraints. **Hard constraints must be satisfied by architecture**, not by cost inflation.

- If a constraint is truly mandatory, it must be modeled as a hard constraint and/or via structural constructs.
- Costs are for optimizing within the feasible space, not for "buying" infeasibility.

This is particularly important when custom solutions are involved:
- you should seed a solution that is feasible (or close to feasible),
- and let the optimizer improve it within the valid constraint architecture.

## Adding new nodes/resources after injecting a custom solution

`CustomSolutionWithAdditionalNodesAndResourcesExample` demonstrates a production workflow:

- you start with a custom initial entity (your "current plan"),
- then you add *new elements* (late orders, new resources),
- and re-optimize.

After setting the initial entity, the example calls, in `addReassignElements`:

```java
opti.addReassignResources(getAdditionalResources());
opti.addReassignNodes(getAdditionalNodes());
```

### What `addReassignNodes(...)` / `addReassignResources(...)` are for
These helper methods are designed for **incremental plan evolution**:

- add a node/resource to the optimization container,
- ensure it becomes part of the search space,
- and "reassign" it into a state where the solver can place it appropriately.

The core puts the reassigned elements into the custom solution when the run starts (the `ADDITIONAL_ELEMENT` status lines above).

This is the practical bridge between:
- "I already have a plan"
and
- "the world changed; optimize again".

### Duplicate IDs are reported (important!)
The example explicitly adds duplicates to demonstrate behavior:

- a node with an existing ID ("Essen") is reported with warning 5557, "Duplicate node ignored: Essen",
- a resource with an existing ID ("Jack") is reported with warning 5557, "Duplicate res ignored: Jack".

What the result shows for the node `Essen` is in the output section above.

Production implication:
- **IDs must be globally unique** across nodes/resources in a run.
- When importing external plans, apply a deterministic ID strategy:
  - stable IDs for existing objects,
  - unique IDs for new objects,
  - never reuse an existing ID for a different object instance.

## Warm-start patterns used in real systems

### Pattern A: continue yesterday's plan
Use case:
- daily scheduling where most jobs repeat.

Workflow:
1. Load yesterday's result (or store it as an entity snapshot)
2. Convert to an `IEntity` with routes + node order
3. `setInitialEntity(entity)`
4. Run a shorter optimization budget (because you already have a good baseline)

Benefits:
- faster convergence,
- higher similarity to previous plan,
- better planner trust.

### Pattern B: insert late jobs ("re-optimization under change")
Use case:
- new job arrives at 10:30 while routes are already planned.

Workflow:
1. Seed with current plan (`setInitialEntity`)
2. Add new nodes via `addReassignNodes(...)`
3. Run with limited compute budget
4. Optionally compare baseline vs modified plan (see the CompareResult tooling: [Compare an optimization result with a modified result](../compareresult/CompareResult.md))

Benefits:
- controlled deviation,
- clear "delta" to the plan,
- supports continuous planning.

### Pattern C: add (or remove) resources
Use case:
- a driver calls in sick; an extra driver becomes available.

Workflow:
1. Seed with existing plan
2. Add/remove resources
3. Let the optimizer rebalance

Note:
- If resources change, always validate:
  - working hours coverage,
  - skill constraints,
  - territory constraints,
  - and feasibility of previously assigned jobs.

### Pattern D: human-in-the-loop acceptance workflow
Use case:
- dispatcher wants to keep "most of my plan" but allow optimization.

Workflow:
1. Seed with dispatcher plan
2. Run short improvement phase
3. Show a diff report (comparison tool)
4. Iterate

## Engineering best practices (high-leverage details)

### Decide whether you want "repair" or "strict feasibility"
If your initial plan may be infeasible:
- you can still seed it, but you should expect repair dynamics and warnings.

If you require strict feasibility:
- validate feasibility before seeding (at least for obvious hard constraints),
- or seed a partially empty route structure and let the solver construct within constraints.

### Keep your custom entity minimal and consistent
Avoid overfitting the seed with incidental details that can confuse search.

Good seed characteristics:
- correct resource/day assignment,
- sensible route order,
- realistic start/termination,
- no duplicated elements,
- consistent IDs.

### Control similarity vs improvement
If end-user acceptance is key:
- use a shorter compute budget and/or a more conservative operator set,
- seed from the human plan,
- and evaluate deltas with the comparison tool.

If pure optimality is key:
- allow full optimization budget,
- still seed to accelerate convergence.

### Instrument results and warnings
Custom seeds can surface modeling mismatches quickly.
Always log:
- the warnings (`warningSubject()`),
- the errors (`errorSubject()`),
- progress strings,
- final KPIs.

The examples demonstrate these subscriptions in `attachToObservables`. The legacy callback methods (`onProgress(IOptimizationProgress)`, `onWarning(...)`, `onError(...)` and the others) subscribe to the same events internally and stay supported; the event API is recommended.

## Practical checklist for implementing custom solutions

1. **IDs**
   - Node IDs and Resource IDs must be unique and stable.

2. **Resource/WorkingHours assignment**
   - Every route must have a resource and a correct working-hours index.

3. **Start/Termination**
   - Set route start and termination (or use your chosen open/closed route rules consistently).

4. **Node order**
   - Add nodes in the exact sequence you want as seed.

5. **Properties**
   - Decide whether assisted construction should be disabled.
   - Decide compute budget and operator aggressiveness.

6. **Incremental changes**
   - Add new nodes/resources via reassign helpers rather than rebuilding everything.

7. **Explainability**
   - If users propose changes, compare baseline vs modified solutions and provide a report.

## Summary

- A **custom solution** in JOpt is an explicitly constructed `IEntity` containing routes and node order.
- You inject it via `setInitialEntity(...)` to warm-start optimization.
- This enables production workflows: continuation, incremental re-optimization, human-in-the-loop planning, and improved acceptance.
- Additional nodes/resources can be integrated after seeding using `addReassignNodes(...)` and `addReassignResources(...)`, with strict ID uniqueness requirements.
- Mandatory feasibility rules must be enforced by **architecture (hard constraints / structural modeling)**, not by "high cost".

## Run it

Run the `main(String[] args)` method of `CustomSolutionExample` or `CustomSolutionWithAdditionalNodesAndResourcesExample` from your IDE. You need a JDK in the version that the examples pom sets. You need no license file: without one, the examples use the public evaluation license (see the [license guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/license)). Each class writes a KML file named after the class into the working directory.

## Related

- [Load a JSON snapshot by hand for a custom solution](../createcustomsolutionfromjson/CreateCustomSolutionFromJSON.md): the elements of a saved plan as the material for a custom solution.
- [Compare an optimization result with a modified result](../compareresult/CompareResult.md): the comparison tool for a baseline and a modified plan.
- [Pillar nodes feature guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/pillar_nodes): nodes whose time window must be met, such as the pillar of the second class.
- [Reactive events and streams](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/reactive_events): the feature guide of the event API that `attachToObservables` uses.
- [Recommended ways to run an optimization](../../basic/recommendedimplementation_02/RecommendedImplementation.md): the recommended run pattern, a synchronous run and the legacy callback methods.
- [Expert examples](../README.md): the overview of all Expert examples.

---

Added: 2026-02-17 (JOpt core 7.5.3-j17) | Last updated: 2026-10-21
