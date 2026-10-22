# Custom node-level restrictions with the Open Assessor

Three example classes add a business rule of their own to the cost of each visited node: a deadline of 11:00 for nodes whose id starts with "M", a cost for each minute after the opening start of a node weighted by its importance, and a cost when a more important node follows a less important one. Each rule is a restriction class that a custom optimization scheme attaches in `postCreate()`. The page is for Java developers who model a rule that JOpt has no feature for; the [Open Assessor](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/open_assessor) feature guide describes the mechanism.

## What the example shows

- **A restriction class per rule:** `CustomNodeLetterMRestriction`, `CustomNodePriorityBasedEarlyVisitRestriction` and `CustomNodeImportanceOrderRestriction` extend `AbstractCustomNodeLevelRestriction` and override `invokeRestriction`, which the optimizer calls for each visited node of a route.
- **A scheme per restriction:** each `OpenCostAssessorOptimizationScheme...` class extends `DefaultOptimizationScheme` and attaches its restriction with `attachCustomNodeLevelRestriction` in `postCreate()`.
- **A runner per scheme:** `CustomNodeLevelRestrictionExample`, `CustomNodeLevelPriorityEarlyRestrictionExample` and `CustomNodeLevelImportanceOrderExample` set the scheme with `setOptimizationScheme` and plan nine nodes in the same nine places for Jack.
- **Cost and counters on the route:** each restriction adds its cost with `addCost` and books it like a late visit, with `setCostLate`, `setNumConstraintViolations` and `setNumViolationsNodeTimewindowLate`. The result therefore shows the custom cost as "Cost Late" and counts "LATE" violations, also for visits inside their opening hours.
- **A violation in the result:** only `CustomNodeLetterMRestriction` attaches a violation when a result is requested, so only its late nodes list a violation of their own.

## Scenario

The dates and times are sample values, and so are the coordinates in Germany. In all three classes, `addProperties` sets `JOptExitCondition.JOptGenerationCount` to 1000 and `JOpt.Algorithm.PreOptimization.SA.NumIterations` to 100000. Both are below their defaults, which keeps the run short.

- **Resource:** `Jack` starts at 50.775346, 6.083887 (Aachen) and works on May 6 and 7, 2020, from 08:00 to 17:00 (Europe/Berlin). His maximal working time is 13 hours per day, more than the longest day of this plan needs, and his maximal distance 1200 km.
- **Nodes:** nine `TimeWindowGeoNode` objects in Koeln, Oberhausen, Essen, Dueren, Nuernberg, Heilbronn, Stuttgart, Wuppertal and Aachen, open on May 6 and 7 from 08:00 to 17:00, each with a visit of 20 minutes.
- **`CustomNodeLevelRestrictionExample`:** four ids start with "M": `MOberhausen`, `MDueren`, `MHeilbronn` and `MWuppertal`. Every node has the importance 1. MHeilbronn is more than three hours from Aachen, so no plan reaches it before 11:00; the class keeps it on purpose, so that the result shows the violation of the rule.
- **`CustomNodeLevelPriorityEarlyRestrictionExample`:** the importances run from 1 (Koeln) to 9 (Aachen), in the order in which the nodes are added. They are sample values. No node has the importance 0: the importance also scales the built-in costs of a node, and with 0 a late visit would cost nothing.
- **`CustomNodeLevelImportanceOrderExample`:** the sample importances are 1 (Koeln), 3 (Oberhausen), 2 (Essen), 6 (Dueren), 5 (Nuernberg), 5 (Heilbronn), 10 (Stuttgart), 9 (Wuppertal) and 7 (Aachen).

## How the code works

The sources of the node-level examples in this package are on GitHub.

The example runners (scenario setup):
- [CustomNodeLevelRestrictionExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/openassessor/nodelevel/CustomNodeLevelRestrictionExample.java)
- [CustomNodeLevelPriorityEarlyRestrictionExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/openassessor/nodelevel/CustomNodeLevelPriorityEarlyRestrictionExample.java)
- [CustomNodeLevelImportanceOrderExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/openassessor/nodelevel/customimportance/CustomNodeLevelImportanceOrderExample.java)

The custom node-level restrictions:
- [CustomNodeLetterMRestriction.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/openassessor/nodelevel/custom/CustomNodeLetterMRestriction.java)
- [CustomNodePriorityBasedEarlyVisitRestriction.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/openassessor/nodelevel/custom/CustomNodePriorityBasedEarlyVisitRestriction.java)
- [CustomNodeImportanceOrderRestriction.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/openassessor/nodelevel/customimportance/CustomNodeImportanceOrderRestriction.java)

The custom optimization schemes (wiring):
- [OpenCostAssessorOptimizationSchemeWithMRestriction.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/openassessor/nodelevel/custom/OpenCostAssessorOptimizationSchemeWithMRestriction.java)
- [OpenCostAssessorOptimizationSchemeWithEarlyVisitRestriction.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/openassessor/nodelevel/custom/OpenCostAssessorOptimizationSchemeWithEarlyVisitRestriction.java)
- [OpenCostAssessorOptimizationSchemeWithImportanceRestriction.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/openassessor/nodelevel/customimportance/OpenCostAssessorOptimizationSchemeWithImportanceRestriction.java)

The three runners call the same steps in `example()`. The block is from `CustomNodeLevelRestrictionExample`:

```java
// The public evaluation license, or your own license if you put a license file where
// ExampleLicenseHelper looks for it.
ExampleLicenseHelper.setLicense(this);
setScheme(this);
addProperties(this);
addNodes(this);
addResources(this);
attachToObservables(this);
IOptimizationResult result = startAndPresentResult(this);
exportToKml(result);
```

- `ExampleLicenseHelper.setLicense(this)` uses your license file if you put one where the helper looks for it, and the public evaluation license otherwise.
- `setScheme` sets the custom scheme of the class.
- `addProperties`, `addNodes` and `addResources` add the properties, the nodes and Jack.
- `attachToObservables` subscribes to the progress, warning, status and error events with the event API, before the run starts.
- `startAndPresentResult` starts the run, waits up to 5 minutes for the result, prints it and returns it.
- `exportToKml` writes the result to a KML file named after the class, in the working directory.

`setScheme` of `CustomNodeLevelRestrictionExample` sets the scheme with the M rule:

```java
opti.setOptimizationScheme(new OpenCostAssessorOptimizationSchemeWithMRestriction(opti));
```

The scheme creates the restriction and attaches it:

```java
@Override
public void postCreate() {
  // postCreate runs when the scheme has created its algorithms and its cost assessor, so the
  // restriction can be attached to the cost assessor here.
  ICustomNodeLevelRestriction restriction =
      new CustomNodeLetterMRestriction(this.getOptimization().getPropertyProvider());

  this.attachCustomNodeLevelRestriction(restriction);
}
```

`CustomNodeLetterMRestriction` compares the arrival with 11:00 of the same day:

```java
// The arrival as local time in Europe/Berlin, the time zone of the example data.
LocalDateTime arrival =
    LocalDateTime.ofInstant(
        Instant.ofEpochMilli(holder.getArrivalTime()), ZoneId.of("Europe/Berlin"));

// 11:00 on the day of the arrival.
LocalDateTime deadline =
    LocalDateTime.of(
        arrival.getYear(), arrival.getMonth(), arrival.getDayOfMonth(), 11, 0, 0, 0);

Duration delta = Duration.between(deadline, arrival);
```

`CustomNodePriorityBasedEarlyVisitRestriction` measures from the start of the opening hours in which the visit is planned:

```java
// The time between the start of the opening hours of the visit and the arrival.
long curNodeHoursStartMillis =
    node.getDutyHours()
        .get(holder.getChoosenOpeningHoursIndex())
        .getTimeWindow()
        .getHoursBeginInMillis();

long deltaMillis = holder.getArrivalTime() - curNodeHoursStartMillis;
```

`CustomNodeImportanceOrderRestriction` compares a node with the node before it:

```java
// A positive delta means that a more important node follows a less important one.
int deltaImportance = curNodeImportance - prevNodeImportance;
```

## Output and what to look for

Each run prints the status and progress lines, then the result. The results were the same in two test runs of each class. In all three classes the route of May 7 ends after 20:00, after the working hours: within the maximal working time of 13 hours (721.96 minutes for the first two classes, 727.6 minutes for the third). Time after the end of the working hours counts against the maximal working time, and the working hours are a frame for matching resources with the opening hours of the nodes, so a route can end after its working hours without a violation. In `CustomNodeLevelRestrictionExample` and `CustomNodeLevelPriorityEarlyRestrictionExample` the summary of the route of May 7 still counts LATE violations: they come from the M rule at MHeilbronn and from the priority rule, which book their cost like a late visit, not from the end of the route.

### CustomNodeLevelRestrictionExample

On May 6, MDueren (08:21:21), MOberhausen (09:40:46) and MWuppertal (10:47:22) are reached before 11:00. MHeilbronn is the first stop of May 7, after a drive of 216.37 minutes, and is reached at 11:36:22. The stops of May 7, with the violation that the restriction attaches, and the summary of the route:

```text
1.0 MHeilbronn / Geo  / Arrival: 07.05.2020 11:36:22 (Europe/Berlin) , Departure: 07.05.2020 11:56:22 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 216.37 [min], 285.61 [km][ CONSTRAINTVIOLATION/TIMECONSTRAINT/LATE/ Late M Violation time [min]: 36.36666666666667 Value: 36.36666666666667 ]
1.1 Stuttgart / Geo  / Arrival: 07.05.2020 12:27:53 (Europe/Berlin) , Departure: 07.05.2020 12:47:53 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 31.52 [min], 41.6 [km]
1.2 Nuernberg / Geo  / Arrival: 07.05.2020 14:47:09 (Europe/Berlin) , Departure: 07.05.2020 15:07:09 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 119.28 [min], 157.44 [km]
```

```text
ViolationSumary: NumViolations: 1,LATE: 1
Cost TotalSum: 1679.6392110582863,Cost Distance: 873.7888580887565,Cost RouteTime: 60.16343333333332,Cost Late: 742.6869196361965,Cost CompactInTime: 3.0
```

The "Cost Late" of 742.69 is the cost of the 36.37 minutes after 11:00 at MHeilbronn; no node of the plan is outside its opening hours.

### CustomNodeLevelPriorityEarlyRestrictionExample

The stops of May 6 show the importance of each node ("Import."; the result prints it only when it is above 1, so Koeln has none) and the arrival:

```text
0.0 Aachen / Geo  / Import.: 9 / Arrival: 06.05.2020 08:00:00 (Europe/Berlin) , Departure: 06.05.2020 08:20:00 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 0.0 [min], 0.0 [km]
0.1 Dueren / Geo  / Import.: 4 / Arrival: 06.05.2020 08:41:21 (Europe/Berlin) , Departure: 06.05.2020 09:01:21 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 21.36 [min], 28.2 [km]
0.2 Wuppertal / Geo  / Import.: 8 / Arrival: 06.05.2020 09:55:29 (Europe/Berlin) , Departure: 06.05.2020 10:15:29 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 54.14 [min], 71.46 [km]
0.3 Essen / Geo  / Import.: 3 / Arrival: 06.05.2020 10:33:15 (Europe/Berlin) , Departure: 06.05.2020 10:53:15 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 17.75 [min], 23.43 [km]
0.4 Oberhausen / Geo  / Import.: 2 / Arrival: 06.05.2020 11:02:06 (Europe/Berlin) , Departure: 06.05.2020 11:22:06 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 8.86 [min], 11.69 [km]
0.5 Koeln / Geo  / Arrival: 06.05.2020 12:07:18 (Europe/Berlin) , Departure: 06.05.2020 12:27:18 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 45.21 [min], 59.67 [km]
```

```text
ViolationSumary: NumViolations: 5,LATE: 5
Cost TotalSum: 3885.114758128202,Cost Distance: 257.6008067393133,Cost RouteTime: 26.26267361111111,Cost Late: 3601.251277777778
```

Aachen, the node with the highest importance, is served first, at 08:00, and costs nothing. The five nodes after it are inside their opening hours, yet the summary counts five LATE violations and the "Cost Late" holds the cost of the rule, because the restriction books its cost like a late visit. May 7 holds Heilbronn, Stuttgart and Nuernberg, with three more LATE counts.

### CustomNodeLevelImportanceOrderExample

Both routes run in falling importance: on May 6 Wuppertal (9), Aachen (7), Dueren (6), Oberhausen (3), Essen (2) and Koeln (1); on May 7 Stuttgart (10), Heilbronn (5) and Nuernberg (5). The rule costs nothing in this plan, and neither route shows a violation. To keep the order, Jack drives from Aachen to Wuppertal first and back to the Aachen node:

```text
0.0 Wuppertal / Geo  / Import.: 9 / Arrival: 06.05.2020 09:11:40 (Europe/Berlin) , Departure: 06.05.2020 09:31:40 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 71.67 [min], 94.6 [km]
0.1 Aachen / Geo  / Import.: 7 / Arrival: 06.05.2020 10:42:50 (Europe/Berlin) , Departure: 06.05.2020 11:02:50 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 71.17 [min], 93.94 [km]
```

## Open Assessor at node level

The **Open Assessor** feature is the primary extensibility mechanism in JOpt.TourOptimizer for implementing customer-specific assessment logic without forking the solver.

At *node level*, Open Assessor lets you inject business logic that evaluates **each visited node inside its route context** and can:

- add **custom costs** (soft preferences),
- increment **violation counters** and attach explainable **violation details**,
- influence acceptance by aligning results with human intuition ("priority jobs should be early", "certain jobs must not be late", ...),
- support rapid prototyping of customer rules.

Reference documentation: the [Open Assessor section of the special features page](https://www.dna-evolutions.com/docs/learn-and-explore/special/special_features#open-assessor) and the [Open Assessor feature guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/open_assessor).

## A note on examples vs real life

Some node-level examples are intentionally stylized (e.g., "nodes whose id starts with `M` should be reached by 11:00").
The purpose is to showcase the architecture and prove a point: if a rule can be expressed precisely, you can usually integrate it cleanly at the right level.

## What "node-level" means in the assessor architecture

Node-level restrictions evaluate *a single node* but with access to the **route context**, such as:

- chosen opening-hours index for the node,
- planned arrival time at the node,
- previous element (sequence context),
- route-level cost/violation controller,
- visitor/resource context via the entity cost assessor.

This is the right level for rules like:

- "high-priority jobs should be early (close to opening start)"
- "certain jobs must not be late beyond a threshold"
- "job ordering should follow importance"
- "penalize certain node types if they are visited too late/too early"
- "discourage specific sequences (A immediately after B)"

## Architectural integration pattern

Node-level Open Assessor is typically implemented in three layers:

### Restriction implementation (the business rule)

You implement a class extending the node-level base restriction:

- `AbstractCustomNodeLevelRestriction`

and override the restriction callback (signature as in the examples):

- `invokeRestriction(IEntity en, ILogicEntityRoute route, INode node, IOptimizationElement lastGeoLocationElement, IOptimizationElement prevElement, EvaluatedNodeDataHolder holder, IEntityCostAssessor ca, boolean resultRequested)`

This method is called repeatedly during evaluation.

Key inputs you typically use:
- `route`: current route context (including cost controller)
- `node`: the node being assessed
- `prevElement`: previous node/element (sequence context)
- `holder`: contains evaluated timing and opening-hours selection (e.g., arrival time)
- `resultRequested`: whether a structured violation result should be returned (for explainability/reporting)

### Optimization scheme wiring (attach the restriction globally)

The cleanest integration point is the **optimization scheme** (`DefaultOptimizationScheme` subclass).

Each scheme example does the same thing in `postCreate()`:

- instantiate your restriction with the property provider
- attach it with `attachCustomNodeLevelRestriction(...)`

Example (pattern):
- `OpenCostAssessorOptimizationSchemeWithMRestriction`
- `OpenCostAssessorOptimizationSchemeWithEarlyVisitRestriction`
- `OpenCostAssessorOptimizationSchemeWithImportanceRestriction`

### Scenario runner (a runnable example)

The runner sets the custom scheme and runs an instance to demonstrate behavior.

## How node-level restrictions inject cost and violations

All three restrictions use the route's **RouteCostAndViolationController**:

- `route.getRouteCostAndViolationController()`

Typical updates include:

- adding cost (e.g., `addCost(...)`)
- incrementing generic violation counts (e.g., `setNumConstraintViolations(...)`)
- incrementing specific violation counters (e.g., node lateness counters)
- returning an `IEntityRestrictionResult`. `CustomNodeLetterMRestriction` adds a structured violation to it when `resultRequested == true`; the other two restrictions return it without a violation.

The three examples book their cost as late cost (`setCostLate`) and count it with the node lateness counter (`setNumViolationsNodeTimewindowLate`), so the result reports the cost of the rule as "Cost Late" and counts a LATE violation for each costed visit, even inside the opening hours.

### Why "structured violations" matter
Structured violations are not just for internal debugging:
- they can be surfaced in a planning UI,
- included in acceptance reports,
- logged for audits,
- or used in comparison tooling ("why is solution A better than B?").

In other words: node-level open assessor is often an **explainability feature**, not only an optimization feature.

## Example 1: deadline-style rule, M nodes should not be late (CustomNodeLetterMRestriction)

**Intent:** nodes whose IDs start with `M` should be served before a deadline (in the example: before 11:00, that is 11 am, Europe/Berlin time).

Key mechanics shown:
- the restriction filters nodes by ID prefix (`startsWith("M")`)
- it computes time deviation (`delta`) using evaluation data from `holder`
- it assigns *lateness cost* when the node violates the rule: ten times the late-arrival weight (the property `JOptWeight.TimeWindow.TimeWindowLateFactor`) per minute late
- it uses an **exponential multiplier** so that "more late" becomes disproportionately more expensive; the class computes e^x with a bit-level approximation (Schraudolph's method), which for delays up to six hours stays within about 4 percent of e^x
- it passes the computed cost through a **cost adjuster** (`EntityCostAdjuster`), which turns the minute value into cost with the per-hour cost of the visiting resource

Why this pattern is valuable:
- many real policies behave like "deadlines" (service-level requirements, cutoffs, contractual constraints),
- exponential scaling is a good model for sharply increasing operational pain beyond a certain point.

## Example 2: priority and importance-driven early placement (CustomNodePriorityBasedEarlyVisitRestriction)

**Intent:** high-importance nodes should be visited *close to the start of their opening hours*.

This is not the same as "late vs not late" feasibility.
Instead it creates a preference profile like:
- "visit important jobs early in their feasible window."

Mechanics shown:
- it reads node importance via `node.getImportance()`
- it computes deviation between planned arrival and the **begin of the chosen opening window**
- it assigns penalty cost when arrival occurs later than opening start
- the penalty is scaled by importance and adjusted via the standard cost adjuster

Why this pattern is valuable:
- It aligns strongly with planner intuition ("do the important tasks first").
- It increases end-user acceptance because the schedule tends to "look right" even when multiple feasible sequences exist.

## Example 3: order-by-importance rule (CustomNodeImportanceOrderRestriction)

**Intent:** within a route, nodes should appear in an order consistent with importance.

Mechanics shown:
- compare the current node importance to the previous element's importance
- if a higher-importance node appears *after* a lower-importance node, assign penalty
- cost is proportional to the "importance delta" (in the example: `100 * deltaImportance`)

Why this pattern is valuable:
- It provides a simple but effective mechanism for enforcing "importance monotonicity" without forcing a rigid schedule.
- It is especially useful when many nodes share the same location/cluster and sequencing differences are otherwise arbitrary.

## Soft vs hard: critical modeling guidance

The examples inject penalty costs and increment violation counters. This is a **soft steering mechanism**, a pseudo constraint:
- the optimizer prefers satisfying the rule,
- but it may violate the preference if other objectives dominate.

If a rule must be strictly satisfied:
- model it as a true hard constraint when supported, and/or
- enforce it structurally via the appropriate architectural feature.

Guiding principle:
- **Hard constraints must be fulfilled by architecture, not by high cost.**
  Costs optimize within the feasible space; they are not a substitute for feasibility.

Node-level Open Assessor is strongest for:
- sophisticated preferences,
- acceptance-driven shaping,
- explainable KPI contributions,
- and domain-specific scoring logic.

## Production best practices

### Keep restriction evaluation fast and deterministic
Node-level restrictions can be evaluated frequently.
Avoid:
- IO / network calls,
- heavy parsing,
- non-deterministic logic.

If you need expensive computations:
- precompute features,
- cache derived values by node id / route id.

### Make costs comparable and stable
Because costs interact with other parts of the objective:
- keep penalty magnitudes reasonable,
- prefer using cost adjusters and consistent units.

If you need non-linear behavior (deadlines), prefer explicit non-linear scaling (as shown in the exponential example) rather than arbitrary "huge constants".

### Produce high-quality violation payloads
When `resultRequested` is true:
- return a violation with a clear identifier,
- include meaningful numeric values (minutes late, delta importance, threshold, etc.).

This is one of the best levers for:
- debugging speed,
- customer trust,
- and rollout success.

### Wire via optimization schemes
Attach restrictions in the optimization scheme (`postCreate()`), not ad-hoc.
Benefits:
- centralized configuration,
- consistent application,
- easy reuse across different runs/environments.

## Summary

- Node-level Open Assessor lets you inject custom business logic that evaluates each node in route context.
- It is implemented by extending `AbstractCustomNodeLevelRestriction` and wiring the restriction via a custom optimization scheme.
- The included examples demonstrate three valuable patterns:
  - deadline-style penalties with non-linear scaling,
  - importance-driven early placement,
  - importance-order enforcement.
- Even when the example rules are stylized, they demonstrate a core point: **customer scenarios are implementable when modeled precisely**.

## Run it

Run the `main(String[] args)` method of `CustomNodeLevelRestrictionExample`, `CustomNodeLevelPriorityEarlyRestrictionExample` or `CustomNodeLevelImportanceOrderExample` from your IDE, with the repository imported as a Maven project. You need a JDK in the version that the examples pom sets. You need no license file: without one, the examples use the public evaluation license (see the [license guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/license)).

## Related

- [Open Assessor](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/open_assessor): the feature guide, with both levels of restrictions and the limits of the mechanism.
- [Special features: Open Assessor](https://www.dna-evolutions.com/docs/learn-and-explore/special/special_features#open-assessor): the short overview of the Open Assessor on the special features page.
- [Custom route-level restrictions with the Open Assessor](OpenAssessorRouteLevel.md): the route-level example, a rule on the number of nodes of a route.
- [Optimization properties](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties): the property reference, with `JOptWeight.TimeWindow.TimeWindowLateFactor` and its default.
- [Reactive events and streams](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/reactive_events): the feature guide of the event API that `attachToObservables` uses.
- [Recommended ways to run an optimization](../../basic/recommendedimplementation_02/RecommendedImplementation.md): the recommended run pattern, a synchronous run and the legacy callback methods.
- [Expert examples](../README.md): the overview of all Expert examples.

---

Added: 2026-02-17 (JOpt core 7.5.3-j17) | Last updated: 2026-10-23
