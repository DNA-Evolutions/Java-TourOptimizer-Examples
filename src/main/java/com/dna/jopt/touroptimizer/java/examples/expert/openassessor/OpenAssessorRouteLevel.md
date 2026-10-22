# Custom route-level restrictions with the Open Assessor

`CustomRouteLevelOddNumberOfElementsRestrictionExample` adds a business rule of its own to the cost of each route: a route with an even number of nodes costs a penalty of 1000. The rule is artificial on purpose; it shows where a route-level rule goes, how a custom optimization scheme attaches it and how it books its cost and its violation. The page is for Java developers who model a rule about whole routes; the [Open Assessor](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/open_assessor) feature guide describes the mechanism.

## What the example shows

- **The restriction:** `CustomRouteWithOddNumberOfElementsRestriction` extends `AbstractCustomRouteLevelRestriction` and overrides `invokeRestriction`, which the optimizer calls for each route.
- **The scheme:** `OpenCostAssessorOptimizationSchemeWithOddNumberOfElementsRestriction` extends `DefaultOptimizationScheme` and attaches the restriction with `attachCustomRouteLevelRestriction` in `postCreate()`.
- **The runner:** `CustomRouteLevelOddNumberOfElementsRestrictionExample` sets the scheme with `setOptimizationScheme` and plans 19 nodes for one resource on five days.
- **Cost and violation:** a route with an even number of nodes gets a constraint violation, the penalty as injected restriction cost, and the penalty in its total cost. With a requested result, the restriction attaches the violation "NOT ODD NUMBER OF ELEMENTS" with the node count.
- **Empty routes:** the optimizer runs route-level restrictions only for routes that are not empty, so an unused working day does not count as a route with zero nodes, an even number.

## Scenario

The dates and times are sample values, and so are the coordinates in Germany. `addProperties` sets `JOptExitCondition.JOptGenerationCount` to 1000 and `JOpt.Algorithm.PreOptimization.SA.NumIterations` to 100000. Both are below their defaults, which keeps the run short.

- **Resource:** `Jack` starts at 50.775346, 6.083887 (Aachen) and works on five days, May 6 to 10, 2020, from 08:00 to 17:00 (Europe/Berlin). His maximal working time is 13 hours per day, more than the longest day of this plan needs, and his maximal distance 1200 km.
- **Nodes:** 19 `TimeWindowGeoNode` objects in nine places, open on the same five days from 08:00 to 17:00, each with a visit of 20 minutes and the importance 1. The nodes of one place share its coordinates and stand for several jobs there: `Koeln1` to `Koeln3`, `Oberhausen1` and `Oberhausen2`, `Essen1` to `Essen3`, `Dueren1` and `Dueren2`, `Nuernberg1`, `Heilbronn1` to `Heilbronn3`, `Stuttgart1`, `Wuppertal1` to `Wuppertal3` and `Aachen1`.
- **License:** the example runs with the public evaluation license that `ExampleLicenseHelper` sets; the [license guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/license) gives the element limits.

## How the code works

The sources of the route-level examples in this package are on GitHub:
- [CustomRouteLevelOddNumberOfElementsRestrictionExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/openassessor/routelevel/CustomRouteLevelOddNumberOfElementsRestrictionExample.java)
- [OpenCostAssessorOptimizationSchemeWithOddNumberOfElementsRestriction.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/openassessor/routelevel/custom/OpenCostAssessorOptimizationSchemeWithOddNumberOfElementsRestriction.java)
- [CustomRouteWithOddNumberOfElementsRestriction.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/openassessor/routelevel/custom/CustomRouteWithOddNumberOfElementsRestriction.java)

`example()` calls these steps:

```java
// The public evaluation license, or your own license if you put a license file where
// ExampleLicenseHelper looks for it.
ExampleLicenseHelper.setLicense(this);
setScheme(this);
addProperties(this);
addNodes(this);
addResources(this);
attachToObservables(this);
startAndPresentResult(this);
```

- `ExampleLicenseHelper.setLicense(this)` uses your license file if you put one where the helper looks for it, and the public evaluation license otherwise.
- `setScheme` sets the custom scheme.
- `addProperties`, `addNodes` and `addResources` add the properties, the 19 nodes and Jack.
- `attachToObservables` subscribes to the progress, warning, status and error events with the event API, before the run starts.
- `startAndPresentResult` starts the run, waits up to 5 minutes for the result and prints it.

The sections "The architectural integration points" and "Deep dive: the restriction logic" below show the scheme and the restriction in detail.

## Output and what to look for

The run prints the status and progress lines, then the result. In six test runs the cost was the same, and so were the routes of May 6 and May 7 with their times. The nodes of one place share its coordinates, so their order within a route can change between runs (for example `Koeln1` to `Koeln3`). The result counts 20 elements, the 19 nodes and Jack:

```text
 Number of Route         : 5
 Number of Route (sched.): 5
 Total Route Elements    : 20
```

Each of the five routes has an odd number of nodes, so none shows a violation:

- **May 6:** 11 nodes, the three nodes of Koeln, Wuppertal and Essen and the two of Oberhausen.
- **May 7:** 5 nodes, Nuernberg1, Stuttgart1 and the three nodes of Heilbronn.
- **May 8, 9 and 10:** one node each, `Aachen1`, `Dueren1` and `Dueren2`, in an order that can change between runs, at the same cost.

The rule shapes this plan. In two test runs of a copy of the class without the `setScheme` call, the same data gave two routes in both runs, 14 nodes on May 6 and 5 on May 7, without a violation, for a total cost of 1208.34 against 1336.36 with the rule.

The route of May 7 ends at 20:38:18, after the working hours, within the maximal working time: it takes 758.31 of 780 minutes. Time after the end of the working hours counts against the maximal working time, and the working hours are a frame for matching resources with the opening hours of the nodes, so a route can end after its working hours without a violation.

## Open Assessor at route level

The **Open Assessor** feature is the primary extensibility mechanism in JOpt.TourOptimizer for implementing *customer-specific business logic* without forking the solver.

It provides a structured way to inject:

- additional **cost contributions** (soft preferences),
- additional **violations**, which steer softly like the costs (a rule that must always hold needs a hard constraint; see "Soft vs hard" below),
- custom scoring KPIs,
- and even domain-specific assessment hooks

at well-defined levels of the model.

This document focuses on the **route-level** open assessor mechanism.

Reference documentation: the [Open Assessor section of the special features page](https://www.dna-evolutions.com/docs/learn-and-explore/special/special_features#open-assessor) and the [Open Assessor feature guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/open_assessor).

## A note on examples vs real life

Some Open Assessor examples are intentionally "artificial" (e.g., "a route must have an odd number of nodes").
This is by design: the purpose is to demonstrate that if you can express a rule precisely, you can typically integrate it cleanly.

The pattern is what matters:
- where to inject logic,
- how to attach it to the optimization scheme,
- how the cost/violation controller is updated.

## What "route-level" means in the assessor architecture

Route-level hooks evaluate properties of an entire route, such as:

- number of assigned nodes,
- route working-time consumption,
- travel distance,
- number of territory crossings,
- sequence patterns (e.g., "must contain at least one depot return"),
- compliance logic that is not naturally expressible as a built-in constraint.

The key advantage is *scope*:
- you can evaluate global route attributes without rewriting node-level constraints.

## The architectural integration points

The implementation is split into three clean layers:

### Restriction implementation
A route-level restriction is a class implementing the route-level restriction contract (here: through a provided abstract base class):

- `CustomRouteWithOddNumberOfElementsRestriction`

Its job is to:
- evaluate the route,
- inject cost and/or violations,
- and return an `IEntityRestrictionResult` when asked.

### Optimization scheme wiring
An optimization scheme is the correct place to attach assessor components globally.

The example uses a custom scheme:

- `OpenCostAssessorOptimizationSchemeWithOddNumberOfElementsRestriction`

In `postCreate()`, it creates the restriction and attaches it:

```java
ICustomRouteLevelRestriction restriction =
    new CustomRouteWithOddNumberOfElementsRestriction(
        this.getOptimization().getPropertyProvider());

this.attachCustomRouteLevelRestriction(restriction);
```

This ensures the restriction is integrated into the standard optimization lifecycle.

### Example and scenario
The example class:
- sets the custom optimization scheme,
- defines nodes/resources,
- runs the optimization.

In `setScheme`, this is the call:

```java
opti.setOptimizationScheme(
    new OpenCostAssessorOptimizationSchemeWithOddNumberOfElementsRestriction(opti));
```

## Deep dive: the restriction logic

### The restriction contract entrypoint
The restriction is invoked via:

```java
@Override
public IEntityRestrictionResult invokeRestriction(
    Optional<ILogicEntityRoute> prevRouteOpt,
    ILogicEntityRoute route,
    IEntityCostAssessor ca,
    boolean resultRequested) {
```

Important parameters:
- `route` is the route being evaluated.
- `resultRequested` indicates whether the caller wants a structured violation result (not always needed in every evaluation path).
- `prevRouteOpt` is the previous route, if there is one, and `ca` the cost assessor; the example uses neither.

### Example restriction: "odd number of elements"
The restriction counts route elements, the nodes of the route:

```java
int numOptimizableRouteElements = route.getRouteOptimizableElements().size();
```

Then checks parity:

- if **even**, it injects penalty cost and increments violation counters
- if **odd**, it does nothing

### How the restriction injects cost and violations
The route has an internal controller:

- `route.getRouteCostAndViolationController()`

The example updates three things when the rule is violated:

1. **Constraint violation count**
```java
route
    .getRouteCostAndViolationController()
    .setNumConstraintViolations(
        route.getRouteCostAndViolationController().getNumConstraintViolations() + 1);
```

2. **Injected restriction cost**
```java
route
    .getRouteCostAndViolationController()
    .setCostInjectedRestriction(
        route.getRouteCostAndViolationController().getCostInjectedRestriction()
            + penaltyCost);
```

3. **Total route cost**
```java
route.getRouteCostAndViolationController().addCost(penaltyCost);
```

This triple update is important:
- it makes the violation visible in reporting,
- keeps injected-cost accounting separated,
- and still contributes to the total optimization objective.

### Producing a structured violation report (optional)
If the caller requests a result (`resultRequested`) and a penalty was applied, the restriction creates a violation:

- `VIOLATION_NOT_ODD_NUMBER_OF_ELEMENTS`

and attaches additional context (the count) to the violation payload.

This is valuable in real deployments:
- it gives you an explainable "why" that can be surfaced to end users, logs, audits, or comparison tools.

## Soft vs hard: what route-level restrictions are (and are not)

### Route-level restrictions are naturally "soft" unless you architect them as hard feasibility
The example injects a **penalty cost** (`1000.0`) and increments violation counts. This is a **soft pressure**, a pseudo constraint:
- the optimizer is encouraged to satisfy the rule,
- but it may violate it if forced by other priorities.

If your business rule must be strictly satisfied, you should:

- model it as a native hard constraint when possible, or
- implement architectural constructs that prevent infeasible configurations from being constructed.

As a guiding principle (also relevant for other features):
- **Hard constraints must be fulfilled by architecture, not by high cost.**
- Costs are the optimization objective inside the feasible space.

Route-level open assessor is most effective for:
- sophisticated "soft policy rules",
- custom KPIs,
- additional preference logic,
- and explainability (reporting, acceptance).

## Why this is so important for customer solutions

Open Assessor is the mechanism that typically unlocks the last part of a customer's requirements:

- company-specific policy rules that are too niche for a generic solver,
- operational preferences that change over time,
- rollout and acceptance logic ("make the solution look like how planners expect it").

With a route-level assessor, you can implement rules such as:

- "a route must not contain more than 2 long-haul legs"
- "if a route enters territory X, it must end in territory X"
- "a route must visit at least one replenishment point if load > threshold"
- "prefer routes that keep the same technician-customer pairing"
- "penalize routes with too many 'direction changes' or 'zig-zags'"

Even if your exact rule is unique:
- you still implement it using the same stable integration pattern shown in this example.

## Recommended production practices

### Keep the restriction deterministic and fast
Restrictions can be evaluated often. Avoid expensive operations inside `invokeRestriction(...)`:
- no network calls,
- no heavy IO,
- cache derived features if needed.

### Make violation reporting high quality
Use `resultRequested` to provide:
- clear violation type identifiers,
- contextual values (counts, thresholds, route id),
- human-readable messages.

This directly improves:
- debugging speed,
- end-user acceptance,
- auditability.

### Avoid "giant penalty numbers" as a substitute for feasibility
If a rule must always be satisfied, invest in the correct architecture (hard constraints / modeling constructs).
Reserve route-level penalty cost for:
- preferences,
- prioritization,
- and explainability.

### Integrate in the optimization scheme, not ad-hoc
The example attaches the restriction in `postCreate()` of the scheme.
This is the recommended location because:
- it is centralized,
- reproducible,
- and applied consistently for all runs that use the scheme.

## Summary

- **Open Assessor (route-level)** lets you inject custom restriction logic evaluated at the route scope.
- The example shows a route-level restriction that penalizes routes with an even number of elements.
- Integration is cleanly separated into:
  1) restriction implementation (`CustomRouteWithOddNumberOfElementsRestriction`)
  2) scheme wiring (`OpenCostAssessorOptimizationScheme...` in `postCreate()`)
  3) scenario execution (the example class sets the scheme and runs)
- Even if an example rule is artificial, the architecture proves that customer-specific requirements can be implemented without making the solver brittle.

## Run it

Run the `main(String[] args)` method of `CustomRouteLevelOddNumberOfElementsRestrictionExample` from your IDE, with the repository imported as a Maven project. You need a JDK in the version that the examples pom sets. You need no license file: without one, the example uses the public evaluation license (see the [license guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/license)).

## Related

- [Open Assessor](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/open_assessor): the feature guide, with both levels of restrictions and the limits of the mechanism.
- [Special features: Open Assessor](https://www.dna-evolutions.com/docs/learn-and-explore/special/special_features#open-assessor): the short overview of the Open Assessor on the special features page.
- [Custom node-level restrictions with the Open Assessor](OpenAssessorNodeLevel.md): three node-level examples: a deadline, an early visit by importance and an order by importance.
- [Reactive events and streams](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/reactive_events): the feature guide of the event API that `attachToObservables` uses.
- [Recommended ways to run an optimization](../../basic/recommendedimplementation_02/RecommendedImplementation.md): the recommended run pattern, a synchronous run and the legacy callback methods.
- [Expert examples](../README.md): the overview of all Expert examples.

---

Added: 2026-02-17 (JOpt core 7.5.3-j17) | Last updated: 2026-10-23
