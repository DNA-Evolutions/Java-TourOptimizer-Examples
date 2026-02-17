# ZoneCodes — Defining Territories for Resources (and Why It Scales)

ZoneCodes are JOpt’s territory model. They allow you to restrict which resources may visit which nodes by attaching **territory identifiers** to:

- **WorkingHours of a resource** (what the resource is allowed to serve *on that day/shift*), and
- **Nodes** (which territory/territories the job belongs to).

Conceptually, ZoneCodes work similar to postcodes: a resource can serve certain “areas”, and each job belongs to one or more areas.

Reference documentation:
- https://www.dna-evolutions.com/docs/learn-and-explore/special/special_features#defining-territories-via-zonecodes

---

## References (examples)

Both examples are in the same package:

- UKPostCodeExample.java  
  [Source](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/zonecode/UKPostCodeExample.java)  


- ZoneNumberConstraintExample.java  
  [Source](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/zonecode/ZoneNumberConstraintExample.java)  

---

## Why ZoneCodes are a “construction capability” (not just another constraint)

In many real deployments, “territory logic” is one of the primary drivers for feasibility:

- legal or contractual territories,
- dispatch regions that change daily,
- customer-specific service areas,
- teams that temporarily cover for each other.

ZoneCodes model this **architecturally**:
- they reduce the assignment search space immediately,
- they eliminate infeasible allocations early,
- and they scale better than trying to approximate territory logic via costs.

You get fine-grained control without drawing polygons or maintaining complex geofences.

---

## The two building blocks

ZoneCodes are implemented through:

### 1) A WorkingHours constraint (resource side)
A resource is constrained by attaching a “zone constraint” to a specific WorkingHours instance.

Important: This means the allowed territories can differ **per day / per shift**.

The examples use two different implementations of the same concept:

- **UKPostCodeConstraint** (postcode-style)
- **ZoneNumberConstraint** (simple numeric areas)

Both follow the same pattern:
1. Create constraint
2. Set hard/soft
3. Add one or more ZoneCodes (allowed areas)
4. Attach constraint to a WorkingHours instance

### 2) A Node qualification (node side)
A node declares its ZoneCode(s) via qualifications.

Again, the two examples use matching qualification classes:

- **UKPostCodeQualification**
- **ZoneNumberQualification**

A node can have:
- exactly one ZoneCode (typical case), or
- multiple ZoneCodes (border case / shared territory).

---

## Hard vs soft ZoneCodes (and the cost weight)

Both examples demonstrate **hard constraint** behavior:

```java
postCodeConstraintWoh1.setIsHard(true);
zoneNumberConstraintWOHOne.setIsHard(true);
```

Interpretation:
- If a node’s ZoneCode does not match the resource’s allowed ZoneCodes for the current WorkingHours, the node **cannot** be assigned to that resource in that shift.

### Soft mode (when you want flexibility)
ZoneCodes can also be modeled as a *soft constraint* (not enforced as hard feasibility).  
In that case, violating the ZoneCode restriction creates a penalty cost.

Both examples show the standard weight property:

- `JOptWeight.ZoneCode` (default is `10.0`)

Example (in the source):
```java
props.setProperty("JOptWeight.ZoneCode", "10.0");
```

Practical guidance:
- Use **hard** ZoneCodes when territories are mandatory.
- Use **soft** ZoneCodes when territories are “preferred”, but cross-coverage is allowed at a measurable cost.

---

## Example 1 — UK Postcodes as ZoneCodes (UKPostCodeExample)

This example implements ZoneCodes using **UK Postcode identifiers**.

### What it demonstrates
- A resource can serve different postcodes on different days.
- Nodes carry postcode qualifications.
- A node with no matching postcode is **not assigned** when the constraint is hard.

### Resource side: daily postcode territories
The resource has two WorkingHours blocks (two days). Each day receives its own allowed postcodes:

- Day 1 (WorkingHours 1): allowed `B37` and `B48`
- Day 2 (WorkingHours 2): allowed `B36`

This is implemented by attaching different `UKPostCodeConstraint` instances to `woh1` and `woh2`.

### Node side: postcode qualifications
Each node receives a `UKPostCodeQualification` such as:

- `B37 2G`, `B48 2F`, etc.

The example includes a node explicitly designed to fail matching:

- `"KoelnNoMatchingPostCode"`

The source comment explains the intent:
- this node will not be assigned due to the hard postcode constraint.

### Why this pattern is powerful
If your territories naturally map to:
- postal codes,
- zip codes,
- service regions already maintained in CRM / ERP systems,
then ZoneCodes let you reuse that structure directly—without spatial polygons.

---

## Example 2 — Simple numbered territories (ZoneNumberConstraintExample)

This example is the “minimal” version of ZoneCodes:
- define a few integer zones (`1`, `2`, `3`)
- attach them to WorkingHours (resource side)
- attach them to nodes (node side)

### What it demonstrates

#### 1) Different territories per WorkingHours
- WorkingHours 1: allowed Zone 1 and Zone 2
- WorkingHours 2: allowed Zone 3

This models daily territory switching (e.g., on-call rotations, coverage plans).

#### 2) Border nodes (multi-zone qualification)
The example explicitly creates a border node:

- `"Oberhausen-Z1-Z2"`

It receives two qualifications:
- Zone 1 and Zone 2

Interpretation:
- it can be served by resources that are allowed to serve either zone.

This is the recommended modeling strategy for territory boundaries:
- assign multiple ZoneCodes to a border node,
- let the optimizer decide which resource assignment yields the smallest additional cost.

#### 3) Default behavior if no zone constraint is present
The source comment states:
- if a resource has no ZoneNumberConstraint, it can visit all nodes.

This is a useful design feature:
- you can introduce zones gradually (hybrid deployments),
- or define a “floating” resource that is allowed everywhere.

---

## Design patterns and best practices

### 1) Define ZoneCodes at the same “granularity” as dispatch decisions
If dispatch is performed by:
- city parts, ZIP codes, districts → use postcode-like ZoneCodes
- coarse regions, depots, teams → use numeric ZoneCodes (or any custom identifiers)

Avoid territory definitions that are too granular unless necessary.

### 2) Prefer hard ZoneCodes for feasibility and explainability
Hard ZoneCodes:
- reduce search space,
- prevent implausible assignments,
- and make results easier to explain operationally (“this technician does not cover that zone”).

If you need flexibility, introduce it explicitly via:
- soft ZoneCodes (penalty),
- or additional cross-coverage resources.

### 3) Model border jobs with multiple ZoneCodes
If a node is close to a boundary:
- add multiple ZoneCode qualifications.

This avoids arbitrary “polygon edge” decisions and lets the optimizer choose the best plan.

### 4) Use per-WorkingHours ZoneCodes for daily territory switching
This is the key scaling feature:
- “territories change daily” becomes a data update, not a model change.

### 5) Do not fake territories with distance penalties
Distance penalties cannot reliably enforce territory rules when:
- zones overlap,
- zones change daily,
- or service areas are not purely geometric.

ZoneCodes encode territory membership directly and are therefore the correct architectural tool.

---

## Summary

- ZoneCodes implement territories by matching:
  - **WorkingHours constraints** (what a resource may serve in that shift) and
  - **Node qualifications** (what territory the node belongs to).
- You can use hard constraints (mandatory territories) or soft constraints (penalty-based flexibility).
- Border nodes can carry multiple ZoneCodes to allow either neighboring territory to serve them.
- The included examples show both a “real-world” encoding (UK postcodes) and a minimal numeric encoding (ZoneNumbers), using the same concept and modeling workflow.
