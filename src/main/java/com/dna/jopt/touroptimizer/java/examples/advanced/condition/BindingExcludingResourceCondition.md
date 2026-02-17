# Binding and Excluding Resource Conditions (Mandatory/Preferred vs. Banned/UnPreferred)

This document explains how to model **resource eligibility** for nodes in JOpt TourOptimizer using:

- **Binding** resource constraints  
  - **Mandatory** (hard)  
  - **Preferred** (soft)

- **Excluding** resource constraints  
  - **Banned** (hard)  
  - **UnPreferred** (soft)

These mechanisms are essential for real-world planning, where a task can be:
- restricted to specific technicians/vehicles,
- best served by certain resources (skills, customer preference),
- prohibited for certain resources (regulatory limits, missing certifications),
- possible but undesirable for certain resources (avoid overtime-skilled resource usage, soft dispatch rules).

---

## References

### Examples (TourOptimizer Examples repository)
- https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/condition/PreferredResourceConditionExample.java

- https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/condition/MandatoryResourceConditionExample.java


## The four concepts in one picture

### Binding (resource should visit the node)
- **Mandatory resource** (hard):  
  “This node must be visited by one of these resources.”
- **Preferred resource** (soft):  
  “Try to visit this node with one of these resources; if not possible, allow it but penalize it.”

### Excluding (resource should NOT visit the node)
- **Banned resource** (hard):  
  “This node must never be visited by these resources.”
- **UnPreferred resource** (soft):  
  “Avoid visiting this node with these resources; if needed, allow it but penalize it.”

---

## Hard constraints vs. soft constraints

### Hard constraints are enforced by architecture (not by “high cost”)
For **Mandatory** and **Banned** constraints, the solver treats the assignment as **not allowed** (infeasible) when violated.

This is critical:

- Hard constraints are fulfilled by **solver architecture** (feasibility enforcement and violation handling), not by setting a huge penalty.
- They represent real-world “must” and “must not” rules.

### Soft constraints are enforced by cost (preference shaping)
For **Preferred** and **UnPreferred** constraints, the assignment is still allowed, but the solver adds **extra cost** to make such assignments less attractive.

Soft constraints are the correct tool for:
- dispatch preferences,
- customer affinity rules,
- “use this resource if possible” skills.

---

## The API surface you use in modeling

### 1) Create a constraint object
You typically create a constraint via one of:

- `new MandatoryResourceConstraint()`
- `new PreferredResourceConstraint()`
- `new BannedResourceConstraint()`
- `new UnPreferredResourceConstraint()`

All of these implement the `IConstraintResource` contract.

### 2) Add one or more resources to the constraint
Each resource is added by **identifier** with a **priority**:

- `constraint.addResource("John", 10);`

You can add multiple resources if the node can be served by a set:
- “Either John or Jack must serve this customer”
- “Prefer John first; if not possible, Jack is still acceptable”

### 3) Attach the constraint to a node
In the examples, this is done via:

- `node.addConstraint(constraint);`

At runtime, the constraint is evaluated whenever the solver considers assigning/visiting the node with a given resource.

---

## BindingResourceConstraint (the shared base for Mandatory and Preferred)

### Semantics
A **binding** constraint defines a set of resources that are considered “matching” for a node.

- If the current visiting resource is in the list → constraint satisfied.
- If not in the list → depends on whether the constraint is hard or soft.

### Hard vs soft behavior in the implementation
The base class uses a boolean `isHard` flag:

- **Hard binding** (Mandatory): `isHard = true`  
  A non-matching resource triggers a violation and is treated as not allowed.

- **Soft binding** (Preferred): `isHard = false`  
  A non-matching resource is allowed but receives a cost penalty.

### Priority and cost shaping
In the binding constraint implementation, the cost penalty for mismatch is scaled by:

- the difference between `maxPriority` and the visitor’s priority, and
- a solver weight used for preferable resources.

Meaning:
- Higher priority → better fit → lower penalty.
- Lower priority (or not listed) → higher penalty.

This yields a very natural “ranking” behavior.

### Resource identifiers and aliases
Matching uses either:
- `resource.getConstraintAliasId()` (if present), otherwise
- `resource.getId()`

This allows a powerful pattern:
- multiple resources can share the same **constraint alias**, enabling group-based constraints without listing every physical resource ID.

---

## MandatoryResourceConstraint (Binding + hard)

### What it means
A node with a **mandatory** resource constraint can only be visited by the listed resources.

This is appropriate for:
- legally required certification,
- physical equipment constraints,
- customer-specific personnel requirements,
- strict skill qualification.

### Example: enforce “Koeln1 must be visited by John”
In `MandatoryResourceConditionExample` a mandatory constraint is added:

- create `MandatoryResourceConstraint`
- add resource `"John"`
- attach to node `koeln1`

Result:
- The solver must allocate `koeln1` to John’s route (if feasible).
- If it cannot, it will show up as a hard constraint issue (violations / infeasibility management).

---

## PreferredResourceConstraint (Binding + soft)

### What it means
A node with a **preferred** resource constraint should be visited by the listed resources if possible, but it is not forbidden to serve it with a different resource.

This is appropriate for:
- customer affinity,
- “avoid assigning inexperienced staff unless needed”,
- “prefer electric vehicles in certain zones”,
- dispatch quality preferences.

### Example: prefer Jack for Koeln and John for Koeln1
In `PreferredResourceConditionExample`, the code establishes:

- Preferred `"Jack"` for node `koeln`
- Preferred `"John"` for node `koeln1`

Result:
- The solver will bias towards those assignments.
- If the preferred allocation causes other constraints to become infeasible or too expensive, it can still assign a different resource, but the plan pays an additional preference cost.

### How to control how strongly “preferred” is enforced
Preferred constraints are controlled via:
- priority values (per resource), and
- the solver’s weight for preferable resources (internally used by the cost computation).

Operationally:
- increase the weight if you want stronger preference adherence,
- lower the weight if preferences should “yield” easily to operational feasibility.

---

## ExcludingResourceConstraint (the shared base for Banned and UnPreferred)

### Semantics
An **excluding** constraint defines a set of resources that should not serve a node.

- If the current visiting resource is not in the list → satisfied.
- If the resource is listed → depends on whether the constraint is hard or soft.

### Hard vs soft behavior in the implementation
- **Hard excluding** (Banned): `isHard = true`  
  Listed resources are not allowed to visit the node.

- **Soft excluding** (UnPreferred): `isHard = false`  
  Listed resources are allowed but incur a penalty cost.

### Why excluding is not the same as binding
Binding answers:
- “Who *should* do it?”

Excluding answers:
- “Who *must not* do it?” (hard) or “Who *should ideally not* do it?” (soft)

In practice you will often use both:
- bind a node to a skill group,
- exclude one specific technician due to a compliance issue.

---

## BannedResourceConstraint (Excluding + hard)

### What it means
A node with a **banned** resource constraint cannot be visited by listed resources.

Use this for:
- safety restrictions (resource must not enter a zone),
- legal restrictions (driver hours classification),
- customer bans (“never again this technician”),
- incompatible equipment.

Hard banning is often the safest modeling choice when the restriction is real.

---

## UnPreferredResourceConstraint / UnPreferredResource (Excluding + soft)

### What it means
A node with an **unpreferred** resource constraint should *avoid* certain resources, but can still use them if needed.

Use this for:
- reserving premium technicians for premium work,
- avoiding high-cost resources for low-value tasks,
- soft “districting” rules,
- soft skill matching (“prefer specialized staff, but allow others”).

### How strength is controlled
Just like preferred constraints, unpreferred constraints apply a cost that is scaled by:
- the resource’s priority, and
- a solver weight for unpreferable resources.

Operationally:
- increase the unpreferred weight to avoid those resources more strongly,
- decrease it to allow the solver to use them more freely.

---

## Priority: what does the number mean?

All resource constraints use:

- `addResource(resourceId, priority)`

### A practical interpretation
- Higher priority means “more preferred / more important” in binding constraints.
- In excluding constraints, “priority” can be used as severity ranking (how strongly we want to avoid this resource).

### Recommended conventions
To keep your model consistent across teams:
- choose a small integer scale (e.g., 1–10),
- document it internally (“10 = strongest preference or strongest ban severity”),
- avoid mixing “severity” and “preference” semantics within the same constraint type.

---

## How AutoFilter interacts with resource mismatch constraints

The constraint implementations call into a node’s AutoFilter collector for resource-mismatch statistics:
- one path for “mismatch violation”
- another path for “mismatch but no violation” (soft mismatch)

Practical implication:
- If you enable AutoFilter selective categories such as resource mismatch, these constraints can feed into filtering decisions.
- This can be useful for “optional tasks” that systematically cannot be served by allowed resources.

If you use AutoFilter in production, ensure you:
- log filter reasons,
- surface unscheduled tasks to dispatchers with actionable explanations.

---

## Recommended modeling patterns (production-grade)

### Pattern 1 — Skills as “mandatory binding”
If a node requires a certification:
- use `MandatoryResourceConstraint`
- list all certified resources (or use a constraint alias to represent the certified group)

### Pattern 2 — Customer affinity as “preferred binding”
If a customer prefers a technician but it is not mandatory:
- use `PreferredResourceConstraint`
- set a meaningful priority and tune the preferable-resource weight

### Pattern 3 — Compliance and prohibitions as “banned excluding”
If a resource must never visit a node:
- use `BannedResourceConstraint`
- do not model this via a preference (it must be hard)

### Pattern 4 — Soft territory rules as “unpreferred excluding”
If you want stable territories but allow exceptions:
- use `UnPreferredResourceConstraint` for out-of-territory assignments
- tune the unpreferred-resource weight to control “leakage”

---

## Pitfalls and how to avoid them

### Pitfall A — Overconstraining (everything mandatory + banned)
If you combine many hard rules, feasibility can collapse quickly.
Recommended mitigation:
- use hard constraints only for truly non-negotiable rules,
- convert “preferences” to soft constraints,
- add resources or relax time windows when infeasibility is real.

### Pitfall B — Using soft constraints for true prohibitions
If a prohibition is real (legal or physical), do not use UnPreferred. Use Banned.

### Pitfall C — IDs not matching
Constraints match resources by:
- `constraintAliasId` if present, else `id`

Therefore:
- if you use aliases, ensure they are consistent across your resource creation pipeline,
- if you do not use aliases, use stable unique resource IDs.

### Pitfall D — “Why doesn’t the solver always choose the preferred resource?”
Because preferences are *trade-offs*:
- time windows, working hours, distance, capacity, and other constraints can dominate.
If you want stronger adherence:
- increase the preferable weight,
- increase priority separation (e.g., 10 vs 2),
- reduce conflicting constraints.

---

## Summary

- **BindingResourceConstraint** governs “who should serve” a node.
  - **Mandatory** = hard (architectural feasibility)
  - **Preferred** = soft (cost shaping)

- **ExcludingResourceConstraint** governs “who should not serve” a node.
  - **Banned** = hard (architectural feasibility)
  - **UnPreferred** = soft (cost shaping)

Together, these four mechanisms are the foundation for realistic dispatch rules:
- strict compliance as hard constraints,
- operational preferences as weighted soft constraints,
- and transparent, explainable outcomes in the result and (optionally) AutoFilter diagnostics.
