# Open Assessor — Node-Level Customization (Custom Node-Level Restrictions)

The **Open Assessor** feature is the primary extensibility mechanism in JOpt.TourOptimizer for implementing customer-specific assessment logic without forking the solver.

At *node level*, Open Assessor lets you inject business logic that evaluates **each visited node inside its route context** and can:

- add **custom costs** (soft preferences),
- increment **violation counters** and attach explainable **violation details**,
- influence acceptance by aligning results with human intuition (“priority jobs should be early”, “certain jobs must not be late”, …),
- support rapid prototyping of customer rules.

Reference documentation:
- https://www.dna-evolutions.com/docs/learn-and-explore/special/special_features#open-assessor

---

## A note on examples vs real life

Some node-level examples are intentionally stylized (e.g., “nodes starting with `M` must be visited early”).  
The purpose is to showcase the architecture and prove a point: **no customer scenario is impossible**—if a rule can be expressed precisely, you can usually integrate it cleanly at the right level.

---

## References (node-level examples in this package)

### Example runners (scenario setup)

- [CustomNodeLevelRestrictionExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/openassessor/nodelevel/CustomNodeLevelRestrictionExample.java)
- [CustomNodeLevelPriorityEarlyRestrictionExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/openassessor/nodelevel/CustomNodeLevelPriorityEarlyRestrictionExample.java) 
- [CustomNodeLevelImportanceOrderExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/openassessor/nodelevel/customimportance/CustomNodeLevelImportanceOrderExample.java)
### Custom node-level restrictions

- [CustomNodeLetterMRestriction.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/openassessor/nodelevel/custom/CustomNodeLetterMRestriction.java)
- [CustomNodePriorityBasedEarlyVisitRestriction.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/openassessor/nodelevel/custom/CustomNodePriorityBasedEarlyVisitRestriction.java)
- [CustomNodeImportanceOrderRestriction.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/openassessor/nodelevel/customimportance/CustomNodeImportanceOrderRestriction.java)
### Custom optimization schemes (wiring)

- [OpenCostAssessorOptimizationSchemeWithMRestriction.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/openassessor/nodelevel/custom/OpenCostAssessorOptimizationSchemeWithMRestriction.java)
- [OpenCostAssessorOptimizationSchemeWithEarlyVisitRestiction.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/openassessor/nodelevel/custom/OpenCostAssessorOptimizationSchemeWithEarlyVisitRestiction.java)
- [OpenCostAssessorOptimizationSchemeWithImportanceRestriction.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/openassessor/nodelevel/customimportance/OpenCostAssessorOptimizationSchemeWithImportanceRestriction.java)

---

## What “node-level” means in the assessor architecture

Node-level restrictions evaluate *a single node* but with access to the **route context**, such as:

- chosen opening-hours index for the node,
- planned arrival time at the node,
- previous element (sequence context),
- route-level cost/violation controller,
- visitor/resource context via the entity cost assessor.

This is the right level for rules like:

- “high-priority jobs should be early (close to opening start)”
- “certain jobs must not be late beyond a threshold”
- “job ordering should follow importance”
- “penalize certain node types if they are visited too late/too early”
- “discourage specific sequences (A immediately after B)”

---

## Architectural integration pattern

Node-level Open Assessor is typically implemented in three layers:

### 1) Restriction implementation (the business rule)

You implement a class extending the node-level base restriction:

- `AbstractCustomNodeLevelRestriction`

and override the restriction callback (signature shown here conceptually):

- `invokeRestriction(Optional<ILogicEntityRoute> ..., ILogicEntityRoute route, INode node, ..., EvaluatedNodeDataHolder holder, IEntityCostAssessor ca, boolean resultRequested)`

This method is called repeatedly during evaluation.

Key inputs you typically use:
- `route`: current route context (including cost controller)
- `node`: the node being assessed
- `prevElement`: previous node/element (sequence context)
- `holder`: contains evaluated timing and opening-hours selection (e.g., arrival time)
- `resultRequested`: whether a structured violation result should be returned (for explainability/reporting)

### 2) Optimization scheme wiring (attach the restriction globally)

The cleanest integration point is the **optimization scheme** (`DefaultOptimizationScheme` subclass).

Each scheme example does the same thing in `postCreate()`:

- instantiate your restriction with the property provider
- attach it with `attachCustomNodeLevelRestriction(...)`

Example (pattern):
- `OpenCostAssessorOptimizationSchemeWithMRestriction`
- `OpenCostAssessorOptimizationSchemeWithEarlyVisitRestiction`
- `OpenCostAssessorOptimizationSchemeWithImportanceRestriction`

### 3) Scenario runner (a runnable example)

The runner sets the custom scheme and runs an instance to demonstrate behavior.

---

## How node-level restrictions inject cost and violations

All three restrictions use the route’s **RouteCostAndViolationController**:

- `route.getRouteCostAndViolationController()`

Typical updates include:

- adding cost (e.g., `addCost(...)`)
- incrementing generic violation counts (e.g., `setNumConstraintViolations(...)`)
- incrementing specific violation counters (e.g., node lateness counters)
- optionally returning an `IEntityRestrictionResult` containing a structured violation entry when `resultRequested == true`

### Why “structured violations” matter
Structured violations are not just for internal debugging:
- they can be surfaced in a planning UI,
- included in acceptance reports,
- logged for audits,
- or used in comparison tooling (“why is solution A better than B?”).

In other words: node-level open assessor is often an **explainability feature**, not only an optimization feature.

---

## Example 1 — Deadline-style rule: “M-nodes should not be late” (CustomNodeLetterMRestriction)

**Intent:** nodes whose IDs start with `M` should be served before a deadline (in the example: “before 11pm (CET)”).

Key mechanics shown:
- the restriction filters nodes by ID prefix (`startsWith("M")`)
- it computes time deviation (`delta`) using evaluation data from `holder`
- it assigns *lateness cost* when the node violates the rule
- it uses an **exponential multiplier** so that “more late” becomes disproportionately more expensive
- it passes the computed cost through a **cost adjuster** (`EntityCostAdjuster`) so cost scaling remains consistent across the optimization environment

Why this pattern is valuable:
- many real policies behave like “deadlines” (service-level requirements, cutoffs, contractual constraints),
- exponential scaling is a good model for sharply increasing operational pain beyond a certain point.

---

## Example 2 — Priority/importance-driven early placement (CustomNodePriorityBasedEarlyVisitRestriction)

**Intent:** high-importance nodes should be visited *close to the start of their opening hours*.

This is not the same as “late vs not late” feasibility.
Instead it creates a preference profile like:
- “visit important jobs early in their feasible window.”

Mechanics shown:
- it reads node importance via `node.getImportance()`
- it computes deviation between planned arrival and the **begin of the chosen opening window**
- it assigns penalty cost when arrival occurs later than opening start
- the penalty is scaled by importance and adjusted via the standard cost adjuster

Why this pattern is valuable:
- It aligns strongly with planner intuition (“do the important tasks first”).
- It increases end-user acceptance because the schedule tends to “look right” even when multiple feasible sequences exist.

---

## Example 3 — Order-by-importance rule (CustomNodeImportanceOrderRestriction)

**Intent:** within a route, nodes should appear in an order consistent with importance.

Mechanics shown:
- compare the current node importance to the previous element’s importance
- if a higher-importance node appears *after* a lower-importance node, assign penalty
- cost is proportional to the “importance delta” (in the example: `100 * deltaImportance`)

Why this pattern is valuable:
- It provides a simple but effective mechanism for enforcing “importance monotonicity” without forcing a rigid schedule.
- It is especially useful when many nodes share the same location/cluster and sequencing differences are otherwise arbitrary.

---

## Soft vs hard: critical modeling guidance

The examples inject penalty costs and increment violation counters. This is a **soft steering mechanism**:
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

---

## Production best practices

### 1) Keep restriction evaluation fast and deterministic
Node-level restrictions can be evaluated frequently.
Avoid:
- IO / network calls,
- heavy parsing,
- non-deterministic logic.

If you need expensive computations:
- precompute features,
- cache derived values by node id / route id.

### 2) Make costs comparable and stable
Because costs interact with other parts of the objective:
- keep penalty magnitudes reasonable,
- prefer using cost adjusters and consistent units.

If you need non-linear behavior (deadlines), prefer explicit non-linear scaling (as shown in the exponential example) rather than arbitrary “huge constants”.

### 3) Produce high-quality violation payloads
When `resultRequested` is true:
- return a violation with a clear identifier,
- include meaningful numeric values (minutes late, delta importance, threshold, etc.).

This is one of the best levers for:
- debugging speed,
- customer trust,
- and rollout success.

### 4) Wire via optimization schemes
Attach restrictions in the optimization scheme (`postCreate()`), not ad-hoc.
Benefits:
- centralized configuration,
- consistent application,
- easy reuse across different runs/environments.

---

## Summary

- Node-level Open Assessor lets you inject custom business logic that evaluates each node in route context.
- It is implemented by extending `AbstractCustomNodeLevelRestriction` and wiring the restriction via a custom optimization scheme.
- The included examples demonstrate three valuable patterns:
  - deadline-style penalties with non-linear scaling,
  - importance-driven early placement,
  - importance-order enforcement.
- Even when the example rules are stylized, they demonstrate a core point: **customer scenarios are implementable when modeled precisely**.
