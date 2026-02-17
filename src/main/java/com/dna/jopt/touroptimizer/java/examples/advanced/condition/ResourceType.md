# ResourceType (Skills) — Hard/Soft Matching, Expertise Levels, Cost Models, and High-Performance Bitsets

This document explains how to model **resource skills / types** in JOpt TourOptimizer and how to control:
- **who is allowed** to serve a node (hard constraints),
- **who is preferred** to serve a node (soft constraints and cost shaping),
- **expertise levels** (minimum and maximum requirements),
- **time-dependent / expiring skills** (selective type availability),
- and the **high-performance BitTypeWithExpertise** construction (bitset-based matching).

The focus is practical implementation: you can use the linked examples to reproduce all behaviors.

---

## References

### Official documentation
- Skill with expertise and cost model (overview and cost model behavior):  
  https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/skill_costmodel

### Example sources (GitHub)
- Resource type (hard):  
  [ResourceTypeConditionHardExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/condition/ResourceTypeConditionHardExample.java)
- Resource type (soft):  
  [ResourceTypeConditionSoftExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/condition/ResourceTypeConditionSoftExample.java)
- Resource type with expertise (hard/soft levels):  
  [ResourceTypeWithExpertiseConditionExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/condition/ResourceTypeWithExpertiseConditionExample.java)
- Resource type with expertise + cost model (min/max and “match quality”):  
  [ResourceTypeWithExpertiseConditionAndCostModelExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/condition/ResourceTypeWithExpertiseConditionAndCostModelExample.java)
- Selective type availability (time-dependent, expiring skills):  
  [SelectiveTypeConditionExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/condition/SelectiveTypeConditionExample.java)
- This example is used to illustrate **BitTypeWithExpertise** and why it can be multiple orders of magnitude faster than string/list based matching:
  [bittype/BitTypeWithExpertiseConditionAndCostModelExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/condition/bittype/BitTypeWithExpertiseConditionAndCostModelExample.java)

---

## Why “ResourceType” matters in tour optimization

Real routing problems are rarely “any vehicle can serve any customer”. Typical constraints:
- Only some technicians are certified for specific work.
- Only some vehicles have equipment (cooling, ramp, pallet lift truck).
- Some jobs require a minimum expertise level, and some additionally impose a maximum (e.g., weight limit with equipment).
- Some certifications can be valid only during certain shifts/days (or “expire” if not refreshed).

In JOpt, these requirements are modeled using **Qualifications (on resources)** and **Constraints (on nodes)**.

---

## Terminology and the building blocks

### Qualification (resource-side)
A **qualification** describes what a resource *can do / has* (skills, types, expertise levels).

Typical classes:
- `TypeQualification` (type only; no expertise)
- `TypeWithExpertiseQualification` (type + expertise level)
- `BitTypeWithExpertiseQualification` (integer dictionary types + bitset + expertise)

### Constraint (node-side)
A **constraint** describes what a node *requires* (hard) or *prefers* (soft).

Typical classes:
- `TypeConstraint` (type only)
- `TypeWithExpertiseConstraint` (type + minimum/maximum expertise requirement, plus optional cost model)
- `BitTypeWithExpertiseConstraint` (same semantics as above, but optimized)

---

## Layer 1 — Simple type matching (no expertise)

This is the most common “skills and equipment” use-case.

### Resource side: `TypeQualification`
A resource becomes eligible for a type by adding it to a `TypeQualification` and attaching it to the resource.

Example (conceptually):
- Resource **Jack** has type `"plumbing"` or `"efficient"`

### Node side: `TypeConstraint`
A node requires or prefers a type by adding a `TypeConstraint`.

You then decide whether the type constraint is **hard** or **soft**.

---

### Hard type constraint (must have the type)

**Example:** `ResourceTypeConditionHardExample`

**Intent**
- Node `"Koeln"` must be visited by a resource that has `"plumbing"`.

**Mechanics**
- Jack is given `TypeQualification` containing `"plumbing"`.
- John has no `"plumbing"` qualification.
- Node Koeln gets a `TypeConstraint` requiring `"plumbing"`.
- The constraint is set to hard via `typeConstraint.setIsHard(true)`.

**Result**
- Only Jack can serve Koeln.
- This is a **feasibility rule**. It is not meant to be implemented via an “extremely high cost”.
- If no eligible resource exists, the node cannot be served under the given model.

---

### Soft type constraint (prefer the type, but allow exceptions)

**Example:** `ResourceTypeConditionSoftExample`

**Intent**
- Node `"Koeln"` prefers an `"efficient"` resource but can accept a non-efficient one if needed.

**Mechanics**
- Jack has type `"efficient"`.
- John does not.
- Node Koeln receives a `TypeConstraint` for `"efficient"` without `setIsHard(true)`.

**Result**
- The solver *tends* to assign Koeln to Jack.
- But if doing so would cause higher overall cost (distance/time/overlaps/other constraints), Koeln may be served by John.

**Important**
Soft type matching is a cost-based preference. It is appropriate for:
- “nice to have” equipment,
- customer affinity,
- optional dispatch guidelines.

---

## Layer 2 — Type matching with expertise levels

Many organizations need more than “has skill / does not have skill”.
They need:
- minimum expertise requirements (“at least level 5”),
- maximum requirements (“must not exceed level 6”),
- and “best-fit” selection between multiple eligible resources.

JOpt provides this with:
- `TypeWithExpertiseQualification`
- `TypeWithExpertiseConstraint`

---

### Expertise: minimum vs maximum requirement

In the examples and docs you will see two patterns:

- **Minimum requirement** (common):  
  “Repair skill must be **>= required level**”

- **Maximum requirement** (less common but important):  
  “Weight including equipment must be **<= required level**”

This is critical for problems where “too much” can also be disqualifying (e.g., weight class, access permissions, vehicle dimensions).

---

### Hard vs soft: what is allowed vs what is preferred

You can use expertise requirements in two distinct ways:

1) **Hard expertise requirement** (feasibility)  
   The node must not be visited by resources below (or above) the required level.

2) **Soft expertise requirement** (preference)  
   Lower qualified resources can still be used, but are penalized.

---

### Hard and soft expertise example

**Example:** `ResourceTypeWithExpertiseConditionExample`

**Setup**
- Resources:
  - Jack expertise = 10
  - John expertise = 2
  - Paula expertise = 4
- Nodes:
  - Koeln requires a high minimum expertise (hard)
  - Oberhausen requires a medium minimum expertise (soft)

**Key point: `JOptWeight.NodeType`**
The example explicitly raises:
- `JOptWeight.NodeType = 10.0`

This weight influences how strongly the solver tries to avoid type/expertise mismatches when the constraint is modeled as soft.

Practical guidance:
- Raise `JOptWeight.NodeType` if skill compliance is a priority.
- Lower it if you want the optimizer to treat skills as “secondary” compared to distance/time.

---

## Layer 3 — Expertise cost models (quality-of-match optimization)

Once you have multiple eligible resources, you often want to optimize “match quality”.

Example: Customer requires **minimum level 5**.
Both level 5 and level 10 are feasible.
Now you need a decision rule:
- “Assign the most expert resource” (premium service)
- or “Assign the cheapest adequate expert” (cost control)
- or “Prefer a close match” (avoid wasting top-tier experts)

This is exactly what the **SkillWithExpertiseCostModel** is for.

---

### The cost models (conceptual behavior)

As described in the official documentation:
- `NO_PENALIZE_MATCHING_SKILL`  
  If the resource meets/exceeds the requirement, there is no penalty for having “more than required”.

- `PENALIZE_MATCHING_SKILL_LOW_DELTA`  
  Adds a penalty based on the delta between actual and required level, but with a relatively mild slope.  
  Useful when you want high expertise, but not “free”.

- `PENALIZE_MATCHING_SKILL_HIGH_DELTA`  
  Strongly penalizes having much higher expertise than required.  
  Useful when you want an exact match and want to protect top-tier resources.

In practice:
- **LOW_DELTA** often supports “premium service preference”.
- **HIGH_DELTA** often supports “avoid over-qualification” and better resource utilization.

---

### Example with min/max requirements plus cost model

**Example:** `ResourceTypeWithExpertiseConditionAndCostModelExample`

This example demonstrates a realistic “multi-dimensional” skill requirement:
- Skill type: roof repair / cleaning
- Expertise levels: minimum thresholds
- Additional constraint: maximum permissible weight level (including equipment)
- Cost models to steer the match quality between feasible candidates

A major benefit of this modeling approach is that it produces solutions that remain **explainable**:
- “This resource was chosen because the customer requested high expertise, and the cost model rewards that.”
- “This resource was chosen because the customer required an upper weight limit.”

---

## SelectiveTypeCondition — time-dependent / expiring skills

In some businesses, a qualification may be valid only for:
- specific time windows,
- specific shifts,
- specific days (e.g., a certification that must be refreshed periodically).

JOpt supports this by attaching qualifications to **WorkingHours segments**, not only to the resource globally.

**Example:** `SelectiveTypeConditionExample`

### What the example proves
- A resource can “have” a type on day 1 and 2, and no longer have it on day 3 if it is not refreshed.
- Nodes with a hard `TypeConstraint` must be scheduled in the time interval where the type is actually available.

### What to look for in the code
- Several `WorkingHours` blocks (per day).
- `addQualification(...)` called on those `WorkingHours`.
- A specific qualification is deliberately not refreshed on later days, effectively expiring.
- Nodes requiring that qualification must be placed earlier.

### Operational use cases
- Dangerous goods certification valid only for certain shifts.
- Specialist equipment available only on certain days.
- Temporary authorizations.

---

## High-performance BitTypeWithExpertise — why it is dramatically faster

For many production scenarios, the performance bottleneck is not distance computation—it is **constraint checking**.

If you have:
- thousands of nodes,
- many skills,
- multiple constraints per node,
- and frequent reassignment during optimization iterations,

then “skill matching” can dominate runtime if implemented with strings, lists, or repeated hashing.

### Key idea: represent skills as bitsets
**BitTypeWithExpertise** represents the *presence of skills* as a **bitset**:
- each skill is mapped to an integer index,
- the resource holds a bitset of offered skills,
- the node holds a bitset of required skills.

Checking whether the resource satisfies all required skills becomes:
- a small number of CPU-word operations (`AND` / comparison),
- with early failure if any skill is missing.

This is fundamentally faster than:
- iterating strings,
- repeated map lookups,
- repeated hash computations,
- repeated allocations.

### Fast path + slow path (only if needed)
The most important performance principle is:

1) **Fast path:** Check pure skill presence with bitset operations.  
   If any required skill is missing, fail immediately.

2) **Slow path (optional):** Only if all required skills are present and expertise levels are relevant, evaluate levels.  
   Level checking is inherently more “branchy” and typically uses a map (skill → level).

This strategy can yield “several thousand percent” speedups in large instances because:
- most candidate assignments fail fast at the bitset level,
- level checks are only performed for candidates that already passed the fast eligibility filter.

### Coverage: expertise and non-expertise types
BitTypeWithExpertise can cover:
- classic “type only” matching (treat skills as present/absent),
- expertise-based matching (add level metadata only when needed),
- and mixed scenarios (some skills with levels, some without).

---

## Hard constraints are fulfilled by architecture, not by “high cost”

This point is crucial for correct modeling and solver behavior.

### What you should do
- Use **hard constraints** (`setIsHard(true)`) for non-negotiable rules:
  - required certification,
  - legal restrictions,
  - strict customer prohibitions.

### What you should not do
- Do not attempt to simulate a hard rule by:
  - leaving the rule soft, and
  - setting an extremely high penalty weight.

Hard constraints are handled as **feasibility rules** by the solver architecture.  
“High cost” is appropriate for **preferences**, not for absolute eligibility.

---

## Practical recommendations

### 1) Use stable skill identifiers
Prefer constants and centralized skill catalogs.
Avoid accidental mismatches due to typos or inconsistent naming.

### 2) Start with simple TypeConstraint, then introduce expertise
- If you only need “has equipment yes/no”, use `TypeConstraint`.
- If you need level thresholds or utilization control, use `TypeWithExpertiseConstraint`.

### 3) Use cost models to protect expensive experts
If you have many highly skilled resources, use cost models to prevent:
- over-qualification waste,
- or to enforce a “premium service” policy consistently.

### 4) For very large instances: prefer BitTypeWithExpertise
If you have large-scale optimization with many skills, BitTypeWithExpertise can dramatically reduce runtime by:
- eliminating repeated string matching,
- making presence checks CPU-efficient,
- and running level checks only when required.

---

## Summary

- **TypeConstraint / TypeQualification**: simplest and most common skill model.
  - hard: eligibility
  - soft: preference

- **TypeWithExpertiseConstraint / TypeWithExpertiseQualification**: adds expertise levels (min/max), supports soft violations and can be weighted via `JOptWeight.NodeType`.

- **SkillWithExpertiseCostModel**: controls “quality of match” among feasible candidates (exact match vs best expert vs adequate match).

- **SelectiveTypeCondition**: skills can be time-dependent by attaching qualifications to working hours segments.

- **BitTypeWithExpertise**: high-performance skill matching using bitsets; extremely fast for large problems due to CPU-efficient set inclusion checks with early failure.
