# Custom Solution — Architecture, Warm-Start Patterns, and Practical Integration

A **custom solution** (also called *warm start*, *seed solution*, or *initial entity*) is an explicitly constructed solution that you inject into the optimizer **before** the search starts.

This capability is central for production deployments where you want the solver to:

- continue from an existing plan (yesterday’s routes, dispatcher plan, manual plan),
- re-optimize partially after disruptions (new jobs, cancellations, resource changes),
- run controlled “what-if” scenarios without restarting from scratch,
- increase end-user acceptance by starting from a familiar plan and improving it incrementally.

In JOpt.TourOptimizer, a “solution” is an **`IEntity`** that contains one or more **routes** (`ILogicEntityRoute`), each route referencing a **resource** and a sequence of **optimizable elements** (nodes).

---

## References

### Examples in this package

- **CustomSolutionExample.java**  
  [Source](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/customsolution/CustomSolutionExample.java) · [Raw](https://raw.githubusercontent.com/DNA-Evolutions/Java-TourOptimizer-Examples/refs/heads/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/customsolution/CustomSolutionExample.java)

- **CustomSolutionWithAdditionalNodesAndResourcesExample.java**  
  [Source](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/customsolution/CustomSolutionWithAdditionalNodesAndResourcesExample.java) · [Raw](https://raw.githubusercontent.com/DNA-Evolutions/Java-TourOptimizer-Examples/refs/heads/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/customsolution/CustomSolutionWithAdditionalNodesAndResourcesExample.java)

---

## The solution architecture in JOpt (mental model)

Think of the optimizer as two layers:

### 1) Problem definition layer (static inputs)
You define:
- nodes (`INode`) and their constraints (opening hours, skills, relations, …),
- resources (`IResource`) with working hours and capacities,
- cost model and optimization properties,
- optional connection data (distance/time).

### 2) Solution layer (dynamic arrangement)
You define (or let the solver create):
- an `IEntity` consisting of
  - multiple `ILogicEntityRoute` routes,
  - each with a visiting resource + working-hours index,
  - and an ordered list of optimizable elements.

A simplified view:

```
Optimization (problem container)
└── IEntity (a solution candidate)
    ├── Route 0  (ILogicEntityRoute)
    │   ├── Visiting Resource: Jack (working day index 0)
    │   ├── Start / Termination (derived from resource)
    │   └── Optimizable elements: [NodeA, NodeB, NodeC, ...]
    └── Route 1  (ILogicEntityRoute)
        ├── Visiting Resource: Jack (working day index 1)
        └── Optimizable elements: [NodeX, NodeY, ...]
```

A **custom solution** means: you construct the right-hand side (the `IEntity`) explicitly and set it as the initial entity.

---

## How to inject a custom solution (the essential sequence)

Both examples follow the same high-level sequence:

1. Configure license and properties
2. Create an initial entity (`IEntity`)
3. Inject it via `setInitialEntity(entity)`
4. Start optimization

Minimal schematic:

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
- you are explicitly telling the optimizer: “do not build your own construction solution; I am providing the starting point.”
- this is typically what you want for true warm-start workflows.

---

## Building an `IEntity` correctly (what “valid” means)

The solver can repair certain inconsistencies, but a robust custom solution should be **structurally consistent**.

### Route must be bound to a resource and a working-hours index
In `CustomSolutionExample`, two routes are bound to the same resource but different working days:

```java
firstRoute.setCurrentVisitingResource(myRes, 0);
secondRoute.setCurrentVisitingResource(myRes, 1);
```

This is the recommended way to model:
- multi-day planning,
- or multiple shifts for the same person/vehicle.

### Route start and termination should be defined
The examples use:

```java
route.setRouteStart(myRes);
route.setRouteTermination(myRes);
```

This ensures the route has a valid start and end reference.  
(How “end” is interpreted depends on your open/closed route modeling; see the *OpenClosedRoute* example and documentation.)

### Add the optimizable elements in the planned order
You then insert nodes in the exact order you want to seed:

```java
route.addAllToOptimizableElements(nodesForThisRoute);
```

Finally:
```java
IEntity entity = new Entity();
entity.addRoute(firstRoute);
entity.addRoute(secondRoute);
```

---

## What a custom solution is (and what it is not)

### It is a starting point, not a hard constraint
A custom solution does **not** freeze the plan by default. It only provides the initial state from which search begins.

If you want parts of the solution to be immutable (e.g., “these nodes are fixed at the beginning”), use architectural features such as:
- Pillar/Captured nodes,
- specific relations,
- or other structural modeling tools (depending on the rule you need).

### Hard constraints are not enforced by “high cost”
A recurring mistake is to simulate feasibility constraints by “making violations very expensive”.
In JOpt, **hard constraints must be satisfied by architecture**—not by cost inflation.

- If a constraint is truly mandatory, it must be modeled as a hard constraint and/or via structural constructs.
- Costs are for optimizing within the feasible space, not for “buying” infeasibility.

This is particularly important when custom solutions are involved:
- you should seed a solution that is feasible (or close to feasible),
- and let the optimizer improve it within the valid constraint architecture.

---

## Adding new nodes/resources after injecting a custom solution

`CustomSolutionWithAdditionalNodesAndResourcesExample` demonstrates a critical production workflow:

- you start with a custom initial entity (your “current plan”),
- then you add *new elements* (late orders, new resources),
- and re-optimize.

After setting the initial entity, the example calls:

```java
addReassignResources(getAdditionalResources());
addReassignNodes(getAdditionalNodes());
```

### What `addReassignNodes(...)` / `addReassignResources(...)` are for
These helper methods are designed for **incremental plan evolution**:

- add a node/resource to the optimization container,
- ensure it becomes part of the search space,
- and “reassign” it into a state where the solver can place it appropriately.

This is the practical bridge between:
- “I already have a plan”
and
- “the world changed; optimize again”.

### Duplicate IDs are rejected (important!)
The example explicitly adds duplicates to demonstrate behavior:

- a node with an existing ID (“Essen”) is rejected,
- a resource with an existing ID (“Jack”) is rejected.

Production implication:
- **IDs must be globally unique** across nodes/resources in a run.
- When importing external plans, apply a deterministic ID strategy:
  - stable IDs for existing objects,
  - unique IDs for new objects,
  - never reuse an existing ID for a different object instance.

---

## Warm-start patterns used in real systems

### Pattern A — Continue yesterday’s plan
Use case:
- daily scheduling where most jobs repeat.

Workflow:
1. Load yesterday’s result (or store it as an entity snapshot)
2. Convert to an `IEntity` with routes + node order
3. `setInitialEntity(entity)`
4. Run a shorter optimization budget (because you already have a good baseline)

Benefits:
- faster convergence,
- higher similarity to previous plan,
- better planner trust.

### Pattern B — Insert late jobs (“re-optimization under change”)
Use case:
- new job arrives at 10:30 while routes are already planned.

Workflow:
1. Seed with current plan (`setInitialEntity`)
2. Add new nodes via `addReassignNodes(...)`
3. Run with limited compute budget
4. Optionally compare baseline vs modified plan (see *CompareResult* tooling)

Benefits:
- controlled deviation,
- clear “delta” to the plan,
- supports continuous planning.

### Pattern C — Add (or remove) resources
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

### Pattern D — Human-in-the-loop acceptance workflow
Use case:
- dispatcher wants to keep “most of my plan” but allow optimization.

Workflow:
1. Seed with dispatcher plan
2. Run short improvement phase
3. Show a diff report (comparison tool)
4. Iterate

This is one of the most effective adoption strategies in practice.

---

## Engineering best practices (high-leverage details)

### 1) Decide whether you want “repair” or “strict feasibility”
If your initial plan may be infeasible:
- you can still seed it, but you should expect repair dynamics and warnings.

If you require strict feasibility:
- validate feasibility before seeding (at least for obvious hard constraints),
- or seed a partially empty route structure and let the solver construct within constraints.

### 2) Keep your custom entity minimal and consistent
Avoid overfitting the seed with incidental details that can confuse search.

Good seed characteristics:
- correct resource/day assignment,
- sensible route order,
- realistic start/termination,
- no duplicated elements,
- consistent IDs.

### 3) Control similarity vs improvement
If end-user acceptance is key:
- use a shorter compute budget and/or a more conservative operator set,
- seed from the human plan,
- and evaluate deltas with the comparison tool.

If pure optimality is key:
- allow full optimization budget,
- still seed to accelerate convergence.

### 4) Instrument results and warnings
Custom seeds can surface modeling mismatches quickly.  
Always log:
- `onWarning(...)`,
- `onError(...)`,
- progress strings,
- final KPIs.

The examples demonstrate these hooks.

---

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

---

## Summary

- A **custom solution** in JOpt is an explicitly constructed `IEntity` containing routes and node order.
- You inject it via `setInitialEntity(...)` to warm-start optimization.
- This enables production-grade workflows: continuation, incremental re-optimization, human-in-the-loop planning, and improved acceptance.
- Additional nodes/resources can be integrated after seeding using `addReassignNodes(...)` and `addReassignResources(...)`, with strict ID uniqueness requirements.
- Mandatory feasibility rules must be enforced by **architecture (hard constraints / structural modeling)**, not by “high cost”.
