# ResourceConstraintAliasId — Treating Multiple Resources as One Logical Unit in Constraints

`constraintAliasId` is a small feature with large practical impact: it lets you **group multiple physical resources** (drivers, vehicles, technicians) under a **single logical identifier** that can be referenced by constraints.

This is especially useful when your business rule is formulated in terms of **teams** or **resource pools**, for example:
- “A member of *Team Aachen* must visit these customers.”
- “No one from *Team TempWorkers* may service this VIP account.”
- “Prefer *Fleet-EV* in the city center, but allow exceptions.”

Instead of duplicating constraints across all team members, you apply the constraint once to the **alias**, and each resource can “declare” membership by setting its alias.

---

## References

- Example source: https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/constraintaliasid/ResourceConstraintAliasIdExample.java  

This document is aligned with the repository’s broader feature notes (meeting transcript attached in this chat), where **alias resource IDs** are described as a way to “treat multiple resources as one logical unit” for constraints and dispatch rules.

---

## What `constraintAliasId` is (and what it is not)

### It *is*
- A **string identifier on the resource** used during constraint evaluation.
- A way to make constraints target a **group label** instead of a specific resource ID.
- A method to reduce modeling complexity when you have interchangeable team members.

### It is *not*
- A replacement for skills/qualifications.  
  Use skill/type constraints for capability (“can do”), and alias IDs for grouping (“belongs to team/pool”).
- A way to merge resources into a single route.  
  Each resource remains a separate entity with its own:
  - start location,
  - working hours,
  - maximum distance/time,
  - route, costs, and KPIs.

---

## How it works internally (conceptual)

When constraints decide whether “resource R may visit node N”, they need a stable identifier.

With `constraintAliasId`, the identifier is evaluated as:

1. If `resource.getConstraintAliasId()` is set → use it.
2. Otherwise → use `resource.getId()`.

Constraints that operate on resource IDs can therefore work against:
- a real resource ID (“Jack”), or
- an alias ID (“Team Aachen”).

This single indirection enables “team constraints” without changing the rest of the solver architecture.

---

## Core use cases

### 1) Team-based mandatory assignment
“Nodes A, B, C must be visited by a member of Team Aachen.”

- Put multiple resources into the team by setting:
  - `resource.setConstraintAliasId("Team Aachen")`
- Attach a **mandatory resource constraint** requiring `"Team Aachen"` to the node set.

The solver can then choose *which* team member actually serves the node, based on:
- feasibility,
- distance,
- time windows,
- and objective weights.

### 2) Prohibit a group from visiting specific customers
“Team TempWorkers must never visit VIP customers.”

- Set alias for all temporary staff to `"TempWorkers"`.
- Attach a **banned/excluding constraint** using `"TempWorkers"` to VIP nodes.

### 3) Preference for a pool (soft bias)
“Prefer the EV fleet downtown, but allow exceptions.”

- All EV resources: alias `"Fleet-EV"`.
- Add a **preferred resource constraint** referencing `"Fleet-EV"` to downtown nodes.

This provides a clean and explainable policy:
- EVs are preferred where possible,
- but the solver can still use other vehicles if constraints require it.

---

## Walkthrough of the provided example

The example models **two teams** and four resources:

- **Team Aachen**: Jack, Peter  
- **Team Heilbronn**: Carla, Jessi

The business rule is:

> Every node should be visited (mandatorily) by a member of either Team Aachen or Team Heilbronn (depending on the node).

### Step 1 — Declare team membership on resources
Each resource is created normally, then tagged with its team:

```java
jack.setConstraintAliasId("Team Aachen");
peter.setConstraintAliasId("Team Aachen");

carla.setConstraintAliasId("Team Heilbronn");
jessi.setConstraintAliasId("Team Heilbronn");
```

### Step 2 — Add a mandatory constraint to nodes using team names
Instead of referencing the four resources explicitly, the nodes reference team alias IDs:

- Nodes Koeln / Heilbronn / Wuppertal / Aachen require **Team Aachen**
- Nodes Essen / Dueren / Nuernberg require **Team Heilbronn**

Conceptually:

```java
IConstraintResource teamA = new MandatoryResourceConstraint();
teamA.addResource("Team Aachen", 10);
node.addConstraint(teamA);

IConstraintResource teamB = new MandatoryResourceConstraint();
teamB.addResource("Team Heilbronn", 10);
node.addConstraint(teamB);
```

### Resulting behavior
- The solver is free to choose *which team member* serves a node, as long as it is a member of the required team.
- This keeps the model small and stable even if you scale from 2 team members to 50.

---

## How to use alias IDs with the four resource-condition types

Alias IDs are compatible with resource constraints that match on resource identifiers.

### Binding constraints (who should do it)
- **Mandatory (hard)**: Only a resource whose ID or alias matches is allowed.
- **Preferred (soft)**: Matching resources are preferred; others are still allowed with an additional cost.

### Excluding constraints (who should not do it)
- **Banned (hard)**: Matching resources are not allowed.
- **UnPreferred (soft)**: Matching resources are allowed but penalized.

Operationally:
- Use **hard** when the rule is truly non-negotiable (compliance, legal, equipment).
- Use **soft** when the rule is a dispatch policy that can be violated in exceptional cases.

---

## Modeling patterns that scale well

### Pattern A — Team aliases + skill constraints
Use alias IDs for “team ownership” and skill constraints for “capability”.

Example:
- Team Aachen can serve the region (alias constraint),
- but only some resources have “plumbing level ≥ 3” (skill constraint).

This creates a robust and explainable model:
- assignment must satisfy both *group rule* and *capability rule*.

### Pattern B — Multiple aliases for the same resource (use with care)
A resource supports only one `constraintAliasId` value. If you need multiple group memberships:
- use skill/type constraints for one dimension,
- and alias IDs for another,
or
- implement a policy where alias is the “primary group” and skills cover additional membership.

### Pattern C — Alias IDs as stable integration keys
If your external systems use “team IDs” (e.g., `TEAM_42`), use those directly as alias IDs.
This avoids mapping and reduces integration errors.

---

## Performance and maintainability advantages

### Reduced constraint cardinality
Without alias IDs, a team rule needs to list every team member in constraints:
- 50 team members × 10,000 nodes is large and error-prone.

With alias IDs:
- the constraint references a single string (“Team Aachen”) regardless of team size.

### Stable documentation and stable APIs
You can modify team membership by changing only:
- `resource.setConstraintAliasId(...)`

No changes are needed on the node constraints, which is usually the larger and more dynamic set.

---

## Pitfalls and how to avoid them

### Pitfall 1 — Alias IDs not set consistently
If one team member forgets to set an alias, it will not match team constraints and may appear “unexpectedly unused”.

Mitigation:
- enforce alias assignment in your resource factory/builder,
- add a validation step before starting the run.

### Pitfall 2 — Confusing alias IDs with skills
Alias IDs express *group membership*, not capabilities.
If you need capability checks, use:
- type/skill constraints (including expertise levels),
- and cost models as needed.

### Pitfall 3 — Team members with very different start locations
If a team contains resources with very different depots, the solver may still assign the “wrong” depot if it is feasible and cheaper.
If you need geographic separation, combine:
- alias constraints with zone codes,
- or with explicit location-based constraints.

### Pitfall 4 — Reporting and explainability
In your UI/reporting, make it clear:
- which physical resource served the node,
- and which alias constraint was satisfied.

A simple pattern is to display:
- “Assigned Resource: Jack (Team Aachen)”.

---

## Recommendations for production use

1. Use alias IDs to express **teams/pools** and keep node constraints stable.
2. Use skills/types to express **capabilities**, not alias IDs.
3. Use hard constraints only when rules are truly non-negotiable.
4. Add input validation: ensure every resource has the expected alias when your model relies on it.
5. Treat alias IDs as part of your domain model: version and document them like any other business identifier.

---

## Summary

- `constraintAliasId` lets you reference a **logical group** of resources in constraints.
- Constraints can target either a resource’s **ID** or its **alias** (if set).
- This enables scalable and maintainable modeling of teams, pools, and fleet classes.
- The provided example demonstrates team membership via alias IDs and mandatory constraints using team names, allowing the solver to pick any member of the required team while optimizing the rest of the plan.
