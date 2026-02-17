# Open Assessor — Route-Level Customization (Custom Route-Level Restrictions)

The **Open Assessor** feature is the primary extensibility mechanism in JOpt.TourOptimizer for implementing *customer-specific business logic* without forking the solver.

It provides a structured way to inject:

- additional **cost contributions** (soft preferences),
- additional **violations** and feasibility logic (hard rules when supported by architecture),
- custom scoring KPIs,
- and even domain-specific assessment hooks

at well-defined levels of the model.

This document focuses on the **route-level** open assessor mechanism.

Reference documentation:
- https://www.dna-evolutions.com/docs/learn-and-explore/special/special_features#open-assessor

---

## A note on examples vs real life

Some Open Assessor examples are intentionally “artificial” (e.g., “a route must have an odd number of nodes”).  
This is by design: the purpose is to demonstrate that **no customer scenario is impossible**—if you can express a rule precisely, you can typically integrate it cleanly.

The pattern is what matters:
- where to inject logic,
- how to attach it to the optimization scheme,
- how the cost/violation controller is updated.

---

## References (route-level examples in this package)

- [CustomRouteLevelOddNumberOfElementsRestrictionExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/openassessor/routelevel/custom/CustomRouteLevelOddNumberOfElementsRestrictionExample.java)
- [OpenCostAssessorOptimizationSchemeWithOddNumberOfElementsRestriction.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/openassessor/routelevel/custom/OpenCostAssessorOptimizationSchemeWithOddNumberOfElementsRestriction.java) 
- [CustomRouteWithOddNumberOfElementsRestriction.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/openassessor/routelevel/custom/CustomRouteWithOddNumberOfElementsRestriction.java)

---

## What “route-level” means in the assessor architecture

Route-level hooks evaluate properties of an entire route, such as:

- number of assigned nodes,
- route working-time consumption,
- travel distance,
- number of territory crossings,
- sequence patterns (e.g., “must contain at least one depot return”),
- compliance logic that is not naturally expressible as a built-in constraint.

The key advantage is *scope*:
- you can evaluate global route attributes without rewriting node-level constraints.

---

## The architectural integration points

The implementation is split into three clean layers:

### 1) **Restriction implementation**
A route-level restriction is a class implementing the route-level restriction contract (here: through a provided abstract base class):

- `CustomRouteWithOddNumberOfElementsRestriction`

Its job is to:
- evaluate the route,
- inject cost and/or violations,
- and return an `IEntityRestrictionResult` when asked.

### 2) **Optimization scheme wiring**
An optimization scheme is the correct place to attach assessor components globally.

The example uses a custom scheme:

- `OpenCostAssessorOptimizationSchemeWithOddNumberOfElementsRestriction`

In `postCreate()`, it creates the restriction and attaches it:

```java
ICustomRouteLevelRestriction restiction =
  new CustomRouteWithOddNumberOfElementsRestriction(this.getOptimization().getPropertyProvider());

this.attachCustomRouteLevelRestriction(restiction);
```

This ensures the restriction is integrated into the standard optimization lifecycle.

### 3) **Example / scenario**
The example class:
- sets the custom optimization scheme,
- defines nodes/resources,
- runs the optimization.

```java
this.setOptimizationScheme(
  new OpenCostAssessorOptimizationSchemeWithOddNumberOfElementsRestriction(this));
```

---

## Deep dive: the restriction logic

### The restriction contract entrypoint
The restriction is invoked via:

```java
public IEntityRestrictionResult invokeRestriction(
    Optional<ILogicEntityRoute> arg0,
    ILogicEntityRoute route,
    IEntityCostAssessor arg2,
    boolean resultRequested)
```

Important parameters:
- `route` is the route being evaluated.
- `resultRequested` indicates whether the caller wants a structured violation result (not always needed in every evaluation path).

### Example restriction: “odd number of elements”
The restriction counts route elements:

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
setNumConstraintViolations(getNumConstraintViolations() + 1);
```

2. **Injected restriction cost**
```java
setCostInjectedRestriction(getCostInjectedRestriction() + penaltyCost);
```

3. **Total route cost**
```java
addCost(penaltyCost);
```

This triple update is important:
- it makes the violation visible in reporting,
- keeps injected-cost accounting separated,
- and still contributes to the total optimization objective.

### Producing a structured violation report (optional)
If the caller requests a result (`resultRequested`) and a penalty was applied, the restriction creates a violation:

- `VIOLATION_NOT_ODD_NUMBER_OF_ELEMENTS`

and attaches additional context (the count) to the violation payload.

This is extremely valuable in real deployments:
- it gives you an explainable “why” that can be surfaced to end users, logs, audits, or comparison tools.

---

## Soft vs hard: what route-level restrictions are (and are not)

### Route-level restrictions are naturally “soft” unless you architect them as hard feasibility
The example injects a **penalty cost** (`1000.0`) and increments violation counts. This is a **soft pressure**:
- the optimizer is encouraged to satisfy the rule,
- but it may violate it if forced by other priorities.

If your business rule must be strictly satisfied, you should:

- model it as a native hard constraint when possible, or
- implement architectural constructs that prevent infeasible configurations from being constructed.

As a guiding principle (also relevant for other features):
- **Hard constraints must be fulfilled by architecture, not by high cost.**
- Costs are the optimization objective inside the feasible space.

Route-level open assessor is most effective for:
- sophisticated “soft policy rules”,
- custom KPIs,
- additional preference logic,
- and explainability (reporting, acceptance).

---

## Why this is so important for customer solutions

Open Assessor is the mechanism that typically unlocks the “last 10%”:

- company-specific policy rules that are too niche for a generic solver,
- operational preferences that change over time,
- rollout and acceptance logic (“make the solution look like how planners expect it”).

With a route-level assessor, you can implement rules such as:

- “a route must not contain more than 2 long-haul legs”
- “if a route enters territory X, it must end in territory X”
- “a route must visit at least one replenishment point if load > threshold”
- “prefer routes that keep the same technician–customer pairing”
- “penalize routes with too many ‘direction changes’ or ‘zig-zags’”

Even if your exact rule is unique:
- you still implement it using the same stable integration pattern shown in this example.

---

## Recommended production practices

### 1) Keep the restriction deterministic and fast
Restrictions can be evaluated often. Avoid expensive operations inside `invokeRestriction(...)`:
- no network calls,
- no heavy IO,
- cache derived features if needed.

### 2) Make violation reporting high quality
Use `resultRequested` to provide:
- clear violation type identifiers,
- contextual values (counts, thresholds, route id),
- human-readable messages.

This directly improves:
- debugging speed,
- end-user acceptance,
- auditability.

### 3) Avoid “giant penalty numbers” as a substitute for feasibility
If a rule must always be satisfied, invest in the correct architecture (hard constraints / modeling constructs).
Reserve route-level penalty cost for:
- preferences,
- prioritization,
- and explainability.

### 4) Integrate in the optimization scheme, not ad-hoc
The example attaches the restriction in `postCreate()` of the scheme.
This is the recommended location because:
- it is centralized,
- reproducible,
- and applied consistently for all runs that use the scheme.

---

## Summary

- **Open Assessor (route-level)** lets you inject custom restriction logic evaluated at the route scope.
- The example shows a route-level restriction that penalizes routes with an even number of elements.
- Integration is cleanly separated into:
  1) restriction implementation (`CustomRouteWithOddNumberOfElementsRestriction`)
  2) scheme wiring (`OpenCostAssessorOptimizationScheme...` in `postCreate()`)
  3) scenario execution (the example class sets the scheme and runs)
- Even if an example rule is artificial, the architecture proves that customer-specific requirements can be implemented without making the solver brittle.
