# Mandatory, preferred, banned and unpreferred resources for a node

Four examples decide which resource visits a node. `MandatoryResourceConditionExample` and `PreferredResourceConditionExample` bind a node to a resource, while `BannedResourceConditionExample` and `UnPreferredResourceConditionExample` keep a resource away from a node. Mandatory and banned resources are hard constraints, preferred and unpreferred resources are soft ones. The page is for developers who restrict or steer which resource visits a node, and it uses the nodes and resources of the [Basic elements tutorial](https://www.dna-evolutions.com/docs/getting-started/tutorials/basic-elements).

## What the example shows

These mechanisms are essential for real-world planning, where a task can be:
* restricted to specific technicians/vehicles,
* best served by certain resources (skills, customer preference),
* prohibited for certain resources (regulatory limits, missing certifications),
* possible but undesirable for certain resources (avoid overtime-skilled resource usage, soft dispatch rules).

- **A resource constraint** is an `IConstraintResource`. The examples create one, add a resource by its id with `addResource(id, priority)` and attach the constraint to a node with `addConstraint`.
- **Binding constraints** name the resources that should visit the node; each one defines this set of resources, which are considered "matching" for the node. If the current visiting resource is in the list, the constraint is satisfied; if not in the list, it depends on whether the constraint is hard or soft:
  - `MandatoryResourceConstraint` is hard: only the named resources may visit the node.
  - `PreferredResourceConstraint` is soft: a preferred resource is not guaranteed. The optimizer may give the node to another resource if the preferred one costs too much.
- **Excluding constraints** name the resources that should not visit (or serve) the node; each one defines this set of resources. If the current visiting resource is not in the list, the constraint is satisfied; if the resource is listed, it depends on whether the constraint is hard or soft:
  - `BannedResourceConstraint` is hard: the named resources may not visit the node.
  - `UnPreferredResourceConstraint` is soft: the optimizer avoids the named resources where it can.
- **Hard and soft:** A node that would break a hard resource condition stays unassigned, with a filter reason. In `UnPreferredResourceConditionExample` the soft constraint is broken for Stuttgart: the node is still planned, and the result shows a `RESOURCE_NOT_ALLOWED` violation and an extra cost. The properties `JOptWeight.PreferableResource` and `JOptWeight.UnPreferableResource` set the weight of that extra cost.
- **The priority:** `addResource` needs a priority above 0. The binding constraints of these examples name one resource each, so the value of the priority does not matter there. A banned resource is kept away whatever its priority. For an unpreferred resource, the priority scales the extra cost of a visit.

Each class writes its result to a KML file of its own.

## Scenario

The dates, times and coordinates are sample values. The time zone is Europe/Berlin.

- **Resources:** `Jack` and `John`, two `CapacityResource` objects at 50.775346, 6.083887 (Aachen). Both have working hours on March 6 and March 7, 2020, from 08:00 to 17:00, and may drive up to 1200 km a day. Jack may work up to 8 hours a day, John up to 14 hours.
- **Nodes:** eight `TimeWindowGeoNode` objects, open on March 6 and March 7 from 08:00 to 17:00, each with a visit of 20 minutes and the same importance. Koeln and Koeln1 have the same position, 50.9333, 6.95.
  - `MandatoryResourceConditionExample`, `BannedResourceConditionExample` and `UnPreferredResourceConditionExample`: Koeln, Koeln1, Oberhausen, Essen, Heilbronn, Stuttgart, Wuppertal and Aachen.
  - `PreferredResourceConditionExample`: Koeln, Koeln1, Oberhausen, Essen, Dueren, Nuernberg, Stuttgart and Aachen.
- **Constraints:**
  - `MandatoryResourceConditionExample`: a mandatory resource constraint on Koeln1 that names John. Koeln has no constraint.
  - `PreferredResourceConditionExample`: a preferred resource constraint on Koeln that names Jack, and one on Koeln1 that names John.
  - `BannedResourceConditionExample`: a banned resource constraint on Koeln1 that names John, and one on Stuttgart that names both Jack and John.
  - `UnPreferredResourceConditionExample`: an unpreferred resource constraint on Koeln1 that names John, and one on Stuttgart that names both Jack and John.

The classes set no properties, so the runs use the defaults of the [property reference](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties).

## How the code works

The sources are [MandatoryResourceConditionExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/condition/MandatoryResourceConditionExample.java), [PreferredResourceConditionExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/condition/PreferredResourceConditionExample.java), [BannedResourceConditionExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/condition/BannedResourceConditionExample.java) and [UnPreferredResourceConditionExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/condition/UnPreferredResourceConditionExample.java). All four call the same steps in `example()`:

```java
// The public evaluation license, or your own license if you put a license file where
// ExampleLicenseHelper looks for it.
ExampleLicenseHelper.setLicense(this);
addNodes(this);
addResources(this);
attachToObservables(this);
IOptimizationResult result = startAndPresentResult(this);
exportToKml(result);
```

Each class adds its constraints at the end of `addNodes`.

### The mandatory resource

In `MandatoryResourceConditionExample` a mandatory constraint is added: create a `MandatoryResourceConstraint`, add the resource `"John"` and attach it to the node `koeln1`:

```java
// The mandatory resource constraint is hard: Koeln1 is for John only. Koeln, at the same
// position, has no constraint.
IConstraintResource johnMandatory = new MandatoryResourceConstraint();
// addResource needs a priority above 0. The priority ranks the resources of one constraint,
// so with John alone its value does not matter.
johnMandatory.addResource("John", 10);
koeln1.addConstraint(johnMandatory);
```

### The preferred resources

In `PreferredResourceConditionExample`, the code establishes preferred `"Jack"` for node `koeln` and preferred `"John"` for node `koeln1`:

```java
// A preferred resource constraint is soft: the optimizer may give the node to another
// resource if the preferred one costs too much. A mandatory resource constraint would not.
IConstraintResource jackPreferred = new PreferredResourceConstraint();
// addResource needs a priority above 0. The priority ranks the resources of one constraint,
// so with one resource its value does not matter.
jackPreferred.addResource("Jack", 10);
koeln.addConstraint(jackPreferred);

IConstraintResource johnPreferred = new PreferredResourceConstraint();
johnPreferred.addResource("John", 10);
koeln1.addConstraint(johnPreferred);
```

### The banned resources

```java
// The banned resource constraint is hard: John may not visit Koeln1. Koeln, at the same
// position, has no constraint.
IConstraintResource johnBanned = new BannedResourceConstraint();
// addResource needs a priority above 0. A banned resource is kept away whatever its
// priority.
johnBanned.addResource("John", 10);
koeln1.addConstraint(johnBanned);

// Both resources are banned from Stuttgart. The hard constraint leaves it no resource, so
// Stuttgart stays unassigned.
IConstraintResource bothBanned = new BannedResourceConstraint();
bothBanned.addResource("Jack", 10);
bothBanned.addResource("John", 10);
stuttgart.addConstraint(bothBanned);
```

### The unpreferred resources

```java
// The unpreferred resource constraint is soft: JOpt avoids John for Koeln1 where it can, but a
// visit by John is allowed at an extra cost. Koeln, at the same position, has no constraint.
IConstraintResource johnUnPreferred = new UnPreferredResourceConstraint();
// addResource needs a priority above 0. For an unpreferred resource the priority scales the
// extra cost of a visit; 1 is the lowest value.
johnUnPreferred.addResource("John", 1);
koeln1.addConstraint(johnUnPreferred);

// Both resources are unpreferred for Stuttgart. The soft constraint does not take the node out
// of the plan: one of them visits it, and the result shows the violation.
IConstraintResource bothUnPreferred = new UnPreferredResourceConstraint();
bothUnPreferred.addResource("Jack", 1);
bothUnPreferred.addResource("John", 1);
stuttgart.addConstraint(bothUnPreferred);
```

### The resources, the events and the KML file

`addResources` creates Jack and John with different maximal working times:

```java
// Jack may work 8 hours a day and John 14, so a long day such as the one with Heilbronn and
// Stuttgart fits John without overtime.
Duration maxWorkingTimeJack = Duration.ofHours(8);
Duration maxWorkingTimeJohn = Duration.ofHours(14);
```

The block is from `MandatoryResourceConditionExample`; `PreferredResourceConditionExample` names Nuernberg and Stuttgart in the comment. `BannedResourceConditionExample` and `UnPreferredResourceConditionExample` take the same limits as `MandatoryResourceConditionExample`, so that their results compare with its result.

`attachToObservables` subscribes to the progress, warning, status and error events with the recommended event API, before the run starts. `startAndPresentResult` starts the run, waits up to 5 minutes for the result, prints it and returns it. `exportToKml` writes the result to a KML file named after the class (for example `BannedResourceConditionExample.kml`) in the working directory, as in [Export a result to a KML file](../../basic/io_03/Export2KMLExample.md).

## Output and what to look for

### MandatoryResourceConditionExample

In both runs, Koeln1 was on John's route of March 6, together with Koeln and all other nodes apart from Aachen. Aachen lies at the start position of both resources. Whether Jack visits it, as his only node, or John takes it as well and Jack has no route can change between runs, at the same cost:

```text
 Cost                    : 847.033054487167
```

Result: the solver must allocate Koeln1 to John's route (if feasible). If it cannot, it will show up as a hard constraint issue: the node stays unassigned, with a filter reason, and the result shows no violation for it.

### PreferredResourceConditionExample

The result was the same in two test runs. Koeln is on Jack's route and Koeln1 on John's route, both on March 6, so both preferences hold (trimmed to these lines):

```text
RouteId                : 0
Resource               : Jack
0.2 Koeln / Geo  / Arrival: 06.03.2020 09:28:36 (Europe/Berlin) , Departure: 06.03.2020 09:48:36 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 27.25 [min], 35.97 [km]
RouteId                : 2
Resource               : John
2.0 Koeln1 / Geo  / Arrival: 06.03.2020 08:47:59 (Europe/Berlin) , Departure: 06.03.2020 09:07:59 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 47.99 [min], 63.34 [km]
```

Jack visits Aachen, Dueren, Koeln, Essen and Oberhausen. John visits Koeln1, Nuernberg and Stuttgart.

Result: the solver will bias towards those assignments. If the preferred allocation causes other constraints to become infeasible or too expensive, it can still assign a different resource, but the plan pays an additional preference cost.

### BannedResourceConditionExample

The result was the same in two test runs. Koeln1 is on Jack's route of March 6, together with Aachen, Koeln, Wuppertal, Essen and Oberhausen. In `MandatoryResourceConditionExample`, Koeln1 is on John's route instead. In two test runs of a variant of this class without the constraint on Koeln1, Koeln1 was on John's route as well, so the banned resource constraint moves it to Jack.

Jack's route, trimmed to these lines:

```text
RouteId                : 0
Resource               : Jack
0.1 Koeln1 / Geo  / Arrival: 06.03.2020 09:07:59 (Europe/Berlin) , Departure: 06.03.2020 09:27:59 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 47.99 [min], 63.34 [km]
```

Stuttgart, banned for both resources, is not on any route. The result lists it as unassigned:

```text
Number of unassigned nodes: 1

Stuttgart
```

John visits only Heilbronn, and neither route shows a violation.

### UnPreferredResourceConditionExample

The result was the same in two test runs. Jack's route of March 6 is the same as in `BannedResourceConditionExample`, with Koeln1 on it. In two test runs of a variant of this class without the constraint on Koeln1, Koeln1 was on John's route, so here the unpreferred resource constraint keeps John away from Koeln1.

Stuttgart, unpreferred for both resources, is still planned: it is on John's route, with the violation and the extra cost (trimmed to these lines):

```text
RouteId                : 2
Resource               : John
2.0 Stuttgart / Geo  / Arrival: 06.03.2020 11:56:15 (Europe/Berlin) , Departure: 06.03.2020 12:16:15 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 236.25 [min], 311.85 [km][ CONSTRAINTVIOLATION/BINDING_EXCLUDING_TYPE/RESOURCE_NOT_ALLOWED/MatchingExcludingResource Value: [ "John", "Jack" ] ]
ViolationSumary: NumViolations: 1,TYV: 1
Cost TotalSum: 2689.028305914301,Cost Distance: 644.9767461920791,Cost RouteTime: 44.05155972222222,Cost UnPreferredRes: 2000.0
```

John also visits Heilbronn. With banned resources instead, in `BannedResourceConditionExample`, Stuttgart stays unassigned.

### John's long day

In `MandatoryResourceConditionExample` and `PreferredResourceConditionExample`, John's route of March 6 takes about 12 hours, so it ends hours after the end of his working hours at 17:00, and the result shows no violation. Time after the end of the working hours counts against the maximal working time, and the working hours are a frame for matching resources with the opening hours of the nodes, so a route can end after its working hours without a violation. John may work 14 hours; with Jack's 8 hours, a day this long would be overtime.

## Hard constraints vs. soft constraints

### Hard constraints are enforced by architecture (not by "high cost")
For **Mandatory** and **Banned** constraints, the solver treats the assignment as **not allowed** (infeasible) when violated.

This is critical:

- Hard constraints are fulfilled by **solver architecture** (feasibility enforcement and violation handling), not by setting a huge penalty.
- They represent real-world "must" and "must not" rules.

### Soft constraints are enforced by cost (preference shaping)
For **Preferred** and **UnPreferred** constraints, the assignment is still allowed, but the solver adds **extra cost** to make such assignments less attractive.

Soft constraints are the correct tool for:
- dispatch preferences,
- customer affinity rules,
- "use this resource if possible" skills.

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
- "Either John or Jack must serve this customer"
- "Prefer John first; if not possible, Jack is still acceptable"

### 3) Attach the constraint to a node
In the examples, this is done via:

- `node.addConstraint(constraint);`

At runtime, the constraint is evaluated whenever the solver considers assigning/visiting the node with a given resource.

## BindingResourceConstraint (the shared base for Mandatory and Preferred)

### Hard vs soft behavior in the implementation
The base class uses a boolean `isHard` flag:

- **Hard binding** (Mandatory): `isHard = true`  
  A non-matching resource is treated as not allowed: it does not get the node, and the result shows no violation for it.

- **Soft binding** (Preferred): `isHard = false`  
  A non-matching resource is allowed but receives a cost penalty.

### Priority and cost shaping
In the binding constraint implementation, the cost penalty of a listed resource is scaled by:

- the difference between `maxPriority` and the visitor's priority, and
- a solver weight used for preferable resources.

A resource that is not listed is a mismatch: it costs a fixed penalty, scaled by the same weight, and counts as a violation (a mandatory resource constraint does not let such a resource visit the node).

Meaning:
- Higher priority -> better fit -> lower penalty.
- Lower priority (or not listed) -> higher penalty.

This yields a very natural "ranking" behavior.

### Resource identifiers and aliases
Matching uses either:
- `resource.getConstraintAliasId()` (if present), otherwise
- `resource.getId()`

This allows a powerful pattern:
- multiple resources can share the same **constraint alias**, enabling group-based constraints without listing every physical resource ID.

## MandatoryResourceConstraint (Binding + hard)

### What it means
A node with a **mandatory** resource constraint can only be visited by the listed resources.

This is appropriate for:
- legally required certification,
- physical equipment constraints,
- customer-specific personnel requirements,
- strict skill qualification.

## PreferredResourceConstraint (Binding + soft)

### What it means (Preferred)
A node with a **preferred** resource constraint should be visited by the listed resources if possible, but it is not forbidden to serve it with a different resource.

This is appropriate for:
- customer affinity,
- "avoid assigning inexperienced staff unless needed",
- "prefer electric vehicles in certain zones",
- dispatch quality preferences.

### How to control how strongly "preferred" is enforced
Preferred constraints are controlled via:
- priority values (per resource; they rank the listed resources, while the cost of a resource that is not listed does not depend on them), and
- the solver's weight for preferable resources (internally used by the cost computation).

Operationally:
- increase the weight if you want stronger preference adherence,
- lower the weight if preferences should "yield" easily to operational feasibility.

## ExcludingResourceConstraint (the shared base for Banned and UnPreferred)

### Hard vs soft behavior in the implementation (Excluding)
- **Hard excluding** (Banned): `isHard = true`  
  Listed resources are not allowed to visit the node.

- **Soft excluding** (UnPreferred): `isHard = false`  
  Listed resources are allowed but incur a penalty cost.

### Why excluding is not the same as binding
Binding answers:
- "Who *should* do it?"

Excluding answers:
- "Who *must not* do it?" (hard) or "Who *should ideally not* do it?" (soft)

In practice you will often use both:
- bind a node to a skill group,
- exclude one specific technician due to a compliance issue.

## BannedResourceConstraint (Excluding + hard)

### What it means (Banned)
A node with a **banned** resource constraint cannot be visited by listed resources.

Use this for:
- safety restrictions (resource must not enter a zone),
- legal restrictions (driver hours classification),
- customer bans ("never again this technician"),
- incompatible equipment.

Hard banning is often the safest modeling choice when the restriction is real.

## UnPreferredResourceConstraint / UnPreferredResource (Excluding + soft)

### What it means (UnPreferred)
A node with an **unpreferred** resource constraint should *avoid* certain resources, but can still use them if needed.

Use this for:
- reserving premium technicians for premium work,
- avoiding high-cost resources for low-value tasks,
- soft "districting" rules,
- soft skill matching ("prefer specialized staff, but allow others").

### How strength is controlled
Just like preferred constraints, unpreferred constraints apply a cost that is scaled by:
- the resource's priority, and
- a solver weight for unpreferable resources.

Operationally:
- increase the unpreferred weight to avoid those resources more strongly,
- decrease it to allow the solver to use them more freely.

## Priority: what does the number mean?

All resource constraints use:

- `addResource(resourceId, priority)`

### A practical interpretation
- Higher priority means "more preferred / more important" in binding constraints.
- In excluding constraints, "priority" can be used as severity ranking (how strongly we want to avoid this resource). For an unpreferred resource the priority scales the extra cost; a banned resource is kept away whatever its priority.

### Recommended conventions
To keep your model consistent across teams:
- choose a small integer scale (e.g., 1-10),
- document it internally ("10 = strongest preference or strongest unpreferred severity"),
- avoid mixing "severity" and "preference" semantics within the same constraint type.

## How AutoFilter interacts with resource mismatch constraints

The constraint implementations call into a node's AutoFilter collector for resource-mismatch statistics:
- one path for "mismatch violation"
- another path for "mismatch but no violation" (a visit that keeps the constraint)

Practical implication:
- If you enable AutoFilter selective categories such as resource mismatch, these constraints can feed into filtering decisions.
- This can be useful for "optional tasks" that systematically cannot be served by allowed resources.

If you use AutoFilter in production, ensure you:
- log filter reasons,
- surface unscheduled tasks to dispatchers with actionable explanations.

## Recommended modeling patterns (production-grade)

### Pattern 1: Skills as "mandatory binding"
If a node requires a certification:
- use `MandatoryResourceConstraint`
- list all certified resources (or use a constraint alias to represent the certified group)

### Pattern 2: Customer affinity as "preferred binding"
If a customer prefers a technician but it is not mandatory:
- use `PreferredResourceConstraint`
- set a meaningful priority and tune the preferable-resource weight

### Pattern 3: Compliance and prohibitions as "banned excluding"
If a resource must never visit a node:
- use `BannedResourceConstraint`
- do not model this via a preference (it must be hard)

### Pattern 4: Soft territory rules as "unpreferred excluding"
If you want stable territories but allow exceptions:
- use `UnPreferredResourceConstraint` for out-of-territory assignments
- tune the unpreferred-resource weight to control "leakage"

## Pitfalls and how to avoid them

### Pitfall A: Overconstraining (everything mandatory + banned)
If you combine many hard rules, feasibility can collapse quickly.
Recommended mitigation:
- use hard constraints only for truly non-negotiable rules,
- convert "preferences" to soft constraints,
- add resources or relax time windows when infeasibility is real.

### Pitfall B: Using soft constraints for true prohibitions
If a prohibition is real (legal or physical), do not use UnPreferred. Use Banned.

### Pitfall C: IDs not matching
Constraints match resources by:
- `constraintAliasId` if present, else `id`

Therefore:
- if you use aliases, ensure they are consistent across your resource creation pipeline,
- if you do not use aliases, use stable unique resource IDs.

### Pitfall D: "Why doesn't the solver always choose the preferred resource?"
Because preferences are *trade-offs*:
- time windows, working hours, distance, capacity, and other constraints can dominate.
If you want stronger adherence:
- increase the preferable weight,
- increase priority separation (e.g., 10 vs 2),
- reduce conflicting constraints.

## Summary

- **BindingResourceConstraint** governs "who should serve" a node.
  - **Mandatory** = hard (architectural feasibility)
  - **Preferred** = soft (cost shaping)

- **ExcludingResourceConstraint** governs "who should not serve" a node.
  - **Banned** = hard (architectural feasibility)
  - **UnPreferred** = soft (cost shaping)

Together, these four mechanisms are the foundation for realistic dispatch rules:
- strict compliance as hard constraints,
- operational preferences as weighted soft constraints,
- and transparent, explainable outcomes in the result and (optionally) AutoFilter diagnostics.

## Run it

Run the `main(String[] args)` method of one of the four classes from your IDE. You need a JDK in the version that the examples pom sets. You need no license file: without one, the examples use the public evaluation license (see the [license guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/license)). Each run writes its KML file into the working directory.

## Related

- [Basic elements tutorial](https://www.dna-evolutions.com/docs/getting-started/tutorials/basic-elements): the nodes and resources that these examples use.
- [Optimization properties](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties): the property reference, with `JOptWeight.PreferableResource` and `JOptWeight.UnPreferableResource`.
- [Export a result to a KML file](../../basic/io_03/Export2KMLExample.md): the KML export in general.
- [Recommended ways to run an optimization](../../basic/recommendedimplementation_02/RecommendedImplementation.md): the recommended run pattern, a synchronous run and the legacy callback methods.
- [Advanced examples](../README.md): the overview of all Advanced examples.

---

Added: 2026-02-17 (JOpt core 7.5.3-j17) | Last updated: 2026-10-23
