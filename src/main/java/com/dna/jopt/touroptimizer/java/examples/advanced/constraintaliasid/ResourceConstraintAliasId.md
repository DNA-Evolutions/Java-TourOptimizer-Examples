# Teams of resources with constraint alias ids

Four examples give four resources the team names Team Aachen and Team Heilbronn as constraint alias ids, and name the teams instead of the resources in resource constraints: `ResourceConstraintAliasIdExample` with mandatory resources, `ResourceConstraintAliasIdPreferredExample` with preferred, `ResourceConstraintAliasIdBannedExample` with banned and `ResourceConstraintAliasIdUnPreferredExample` with unpreferred resources. One constraint per team then covers every member of the team. The page is for developers who decide which group of resources visits a node. The four constraint types themselves are explained on [Mandatory, preferred, banned and unpreferred resources for a node](../condition/BindingExcludingResourceCondition.md).

## What the example shows

- **The alias:** `setConstraintAliasId(String)` on a resource sets the name that resource constraints look up. Jack and Peter get the alias Team Aachen, Carla and Jessi the alias Team Heilbronn. A constraint that names Team Aachen with `addResource` applies to Jack and Peter alike, so a change of team members needs no change of the constraints.
- **The alias replaces the id:** when constraints decide whether "resource R may visit node N", they need a stable identifier. Once a resource has an alias, resource constraints look up the alias instead of the resource id. A constraint that names the id of such a resource, for example Jack, does not match it any more, and a resource without an alias is looked up by its id, so a team constraint does not match it. This single indirection enables "team constraints" without changing the rest of the solver architecture. "Output and what to look for" shows two test runs of the first case.
- **One alias per resource:** a resource holds one alias; a second call of `setConstraintAliasId` replaces the first.
- **The four constraint types,** one class each:
  - `MandatoryResourceConstraint` (hard): only the named teams may visit the node.
  - `PreferredResourceConstraint` (soft): the named teams should visit the node. A visit by another team is allowed, at an extra cost and with a `RESOURCE_NOT_ALLOWED` violation.
  - `BannedResourceConstraint` (hard): the named teams may not visit the node. A node that no resource may visit stays unassigned.
  - `UnPreferredResourceConstraint` (soft): the named teams should not visit the node. A visit is allowed, at an extra cost and with a `RESOURCE_NOT_ALLOWED` violation.
- **The extra cost:** the properties `JOptWeight.PreferableResource` and `JOptWeight.UnPreferableResource` set the weight of the extra cost of the two soft constraints.
- **Mirrored rules:** with two teams, banning one team from a node leaves the node to the other team. The banned and unpreferred classes name Team Heilbronn where the mandatory and preferred classes name Team Aachen, and the other way around. In the runs, all four classes gave each team the same nodes, apart from Nuernberg, which the banned and unpreferred classes name for both teams.
- **Long trips on purpose:** the rules send a member of Team Aachen to Heilbronn, where Carla and Jessi start, and a member of Team Heilbronn to Essen and Dueren, which lie near Aachen. The constraints force these long trips.
- **The priority:** `addResource` needs a priority above 0. The binding constraints of these examples name one team each, so the value of the priority does not matter there. A banned team is kept away whatever its priority. For an unpreferred team, the priority scales the extra cost of a visit.

## Scenario

The dates, times and coordinates are sample values. The time zone is Europe/Berlin.

- **Resources:** four `CapacityResource` objects with working hours on May 6, 2020, from 08:00 to 17:00. Each may work up to 9 hours, the length of the working hours, and drive up to 1200 km.
  - `Jack` and `Peter` at 50.775346, 6.083887 (Aachen), with the alias Team Aachen.
  - `Carla` and `Jessi` at 49.1403, 9.22 (Heilbronn), with the alias Team Heilbronn.
- **Nodes:** seven `TimeWindowGeoNode` objects: Koeln, Essen, Dueren, Nuernberg, Heilbronn, Wuppertal and Aachen. They are open on May 6 from 08:00 to 17:00, each visit takes 20 minutes, and all nodes have the same importance. The Aachen node lies at the start position of Jack and Peter, the Heilbronn node at the start position of Carla and Jessi.
- **Constraints:**

| Class | Koeln, Heilbronn, Wuppertal, Aachen | Essen, Dueren | Nuernberg |
|---|---|---|---|
| `ResourceConstraintAliasIdExample` | mandatory: Team Aachen | mandatory: Team Heilbronn | mandatory: Team Heilbronn |
| `ResourceConstraintAliasIdPreferredExample` | preferred: Team Aachen | preferred: Team Heilbronn | preferred: Team Heilbronn |
| `ResourceConstraintAliasIdBannedExample` | banned: Team Heilbronn | banned: Team Aachen | banned: both teams |
| `ResourceConstraintAliasIdUnPreferredExample` | unpreferred: Team Heilbronn | unpreferred: Team Aachen | unpreferred: both teams |

`addProperties` sets `JOptExitCondition.JOptGenerationCount` to 1000 and `JOpt.Algorithm.PreOptimization.SA.NumIterations` to 100000. Both are below their defaults, which keeps the run short. The defaults are in the [property reference](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties).

## How the code works

The sources are [ResourceConstraintAliasIdExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/constraintaliasid/ResourceConstraintAliasIdExample.java), [ResourceConstraintAliasIdPreferredExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/constraintaliasid/ResourceConstraintAliasIdPreferredExample.java), [ResourceConstraintAliasIdBannedExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/constraintaliasid/ResourceConstraintAliasIdBannedExample.java) and [ResourceConstraintAliasIdUnPreferredExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/constraintaliasid/ResourceConstraintAliasIdUnPreferredExample.java). All four call the same steps in `example()`:

```java
// The public evaluation license, or your own license if you put a license file where
// ExampleLicenseHelper looks for it.
ExampleLicenseHelper.setLicense(this);
addProperties(this);
addNodes(this);
addResources(this);
attachToObservables(this);
startAndPresentResult(this);
```

- `addProperties` sets the two properties.
- `addNodes` adds the seven nodes and, at its end, the constraints of the class.
- `addResources` adds the four resources with their aliases. Each resource gets its own working hours from `createWorkingHours()`; JOpt warns when resources share working hours objects.
- `attachToObservables` subscribes to the progress, warning, status and error events with the event API, before the run starts.
- `startAndPresentResult` starts the run, waits up to 5 minutes for the result and prints it.

### The aliases

`addResources` creates the same four resources in all four classes:

```java
// Once set, the alias replaces the resource id when a resource constraint looks up the
// resource: a constraint that names Team Aachen matches Jack, one that names Jack does not.
IResource jack =
    new CapacityResource(
        "Jack", 50.775346, 6.083887, maxWorkingTime, maxDistanceKmW, createWorkingHours());
jack.setConstraintAliasId("Team Aachen");
opti.addElement(jack);
```

The block is from `ResourceConstraintAliasIdExample`; the other three classes create Jack the same way, without the comment. Peter gets the alias Team Aachen, Carla and Jessi get Team Heilbronn.

### The mandatory teams

```java
// The constraint names the alias Team Aachen, not a resource id: Jack and Peter, who carry the
// alias, may visit these nodes, and no other resource may.
IConstraintResource teamAachenMandatory = new MandatoryResourceConstraint();
// addResource needs a priority above 0. The priority ranks the entries of one constraint, so
// with one alias its value does not matter.
teamAachenMandatory.addResource("Team Aachen", 10);
koeln.addConstraint(teamAachenMandatory);
wuppertal.addConstraint(teamAachenMandatory);
aachen.addConstraint(teamAachenMandatory);
// Carla and Jessi start at Heilbronn, but the rule sends a member of Team Aachen there: a long
// trip that shows the constraint at work.
heilbronn.addConstraint(teamAachenMandatory);

// Essen and Dueren lie near Aachen, but the rule sends a member of Team Heilbronn there as
// well.
IConstraintResource teamHeilbronnMandatory = new MandatoryResourceConstraint();
teamHeilbronnMandatory.addResource("Team Heilbronn", 10);
essen.addConstraint(teamHeilbronnMandatory);
dueren.addConstraint(teamHeilbronnMandatory);
nuernberg.addConstraint(teamHeilbronnMandatory);
```

### The preferred teams

`ResourceConstraintAliasIdPreferredExample` attaches its constraints to the same nodes:

```java
// A preferred resource constraint is soft: a visit by another team is allowed at an extra
// cost. The constraint names the alias Team Aachen, so it prefers Jack and Peter.
IConstraintResource teamAachenPreferred = new PreferredResourceConstraint();
// addResource needs a priority above 0. The priority ranks the entries of one constraint, so
// with one alias its value does not matter.
teamAachenPreferred.addResource("Team Aachen", 10);
```

### The banned teams

```java
// A banned resource constraint is hard. It names the alias Team Heilbronn, so Carla and Jessi
// may not visit these nodes. With two teams, that leaves the nodes to Team Aachen, as the
// mandatory resource constraints of ResourceConstraintAliasIdExample do.
IConstraintResource teamHeilbronnBanned = new BannedResourceConstraint();
// addResource needs a priority above 0. A banned resource is kept away whatever its
// priority.
teamHeilbronnBanned.addResource("Team Heilbronn", 10);
```

The constraint goes to Koeln, Wuppertal, Aachen and Heilbronn; a second one bans Team Aachen from Essen and Dueren. Nuernberg gets a constraint with both teams:

```java
// Both teams are banned from Nuernberg. The hard constraint leaves it no resource, so
// Nuernberg stays unassigned.
IConstraintResource bothTeamsBanned = new BannedResourceConstraint();
bothTeamsBanned.addResource("Team Aachen", 10);
bothTeamsBanned.addResource("Team Heilbronn", 10);
nuernberg.addConstraint(bothTeamsBanned);
```

### The unpreferred teams

`ResourceConstraintAliasIdUnPreferredExample` follows the layout of the banned class, with priority 1:

```java
// Both teams are unpreferred for Nuernberg. The soft constraint does not take the node out of
// the plan: one of the four resources visits it, and the result shows the violation.
IConstraintResource bothTeamsUnPreferred = new UnPreferredResourceConstraint();
bothTeamsUnPreferred.addResource("Team Aachen", 1);
bothTeamsUnPreferred.addResource("Team Heilbronn", 1);
nuernberg.addConstraint(bothTeamsUnPreferred);
```

## Output and what to look for

Each run prints the status and progress lines, then the result. Every route of these runs ended within its working hours.

### ResourceConstraintAliasIdExample

In both runs, Koeln, Heilbronn, Wuppertal and Aachen were on the routes of Jack and Peter, Essen and Dueren on Carla's route, and Nuernberg on Jessi's route. No route showed a violation. A member of Team Aachen drove the 285.61 km from Aachen to Heilbronn, and Carla drove from Heilbronn to Essen and Dueren. Her node lines were the same in both runs:

```text
2.0 Essen / Geo  / Arrival: 06.05.2020 11:49:11 (Europe/Berlin) , Departure: 06.05.2020 12:09:11 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 229.19 [min], 302.53 [km]
2.1 Dueren / Geo  / Arrival: 06.05.2020 13:10:38 (Europe/Berlin) , Departure: 06.05.2020 13:30:38 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 61.46 [min], 81.12 [km]
```

Which of Jack and Peter drives to Heilbronn, and which of them takes the Aachen node at their common start position, can change between runs. The total cost was the same in all runs:

```text
 Cost                    : 1820.4777678596943
```

### ResourceConstraintAliasIdPreferredExample

The result was the same in two test runs: Jack visited Aachen, Koeln and Wuppertal, Peter Heilbronn, Carla Essen and Dueren, and Jessi Nuernberg. Every node was on a route of its preferred team, no route showed a violation, and the total cost was the one of `ResourceConstraintAliasIdExample`:

```text
 Cost                    : 1820.4777678596943
```

### ResourceConstraintAliasIdBannedExample

The result was the same in two test runs. The routes of Jack, Peter and Carla were those of `ResourceConstraintAliasIdPreferredExample`, Jessi had no route, and Nuernberg, banned for both teams, was not on any route. The result lists it as unassigned:

```text
Number of unassigned nodes: 1

Nuernberg
```

### ResourceConstraintAliasIdUnPreferredExample

The result was the same in two test runs. The routes were those of `ResourceConstraintAliasIdPreferredExample`, and Nuernberg, unpreferred for both teams, was still planned: it was on Jessi's route, with the violation and the extra cost (trimmed to these lines):

```text
3.0 Nuernberg / Geo  / Arrival: 06.05.2020 09:45:01 (Europe/Berlin) , Departure: 06.05.2020 10:05:01 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 105.03 [min], 138.64 [km][ CONSTRAINTVIOLATION/BINDING_EXCLUDING_TYPE/RESOURCE_NOT_ALLOWED/MatchingExcludingResource Value: [ "Team Aachen", "Team Heilbronn" ] ]
ViolationSumary: NumViolations: 1,TYV: 1
Cost TotalSum: 2295.5912519273497,Cost Distance: 276.4706324829054,Cost RouteTime: 19.120619444444444,Cost UnPreferredRes: 2000.0
```

The other constraints held: Carla and Jessi stayed away from Koeln, Heilbronn, Wuppertal and Aachen, and Jack and Peter from Essen and Dueren.

### An id that no longer matches

In two test runs of a variant of `ResourceConstraintAliasIdExample` whose constraint on Koeln named Jack instead of Team Aachen, Koeln stayed unassigned, although Jack had a route. Jack's alias had replaced his id in the lookup, so no resource matched the mandatory resource constraint on Koeln.

## What `constraintAliasId` is (and what it is not)

### It *is*
- A **string identifier on the resource** used during constraint evaluation.
- A way to make constraints target a **group label** instead of a specific resource ID.
- A method to reduce modeling complexity when you have interchangeable team members.

### It is *not*
- A replacement for skills/qualifications.  
  Use skill/type constraints for capability ("can do"), and alias IDs for grouping ("belongs to team/pool").
- A way to merge resources into a single route.  
  Each resource remains a separate entity with its own:
  - start location,
  - working hours,
  - maximum distance/time,
  - route, costs, and KPIs.

## Core use cases

### 1) Team-based mandatory assignment
"Nodes A, B, C must be visited by a member of Team Aachen."

- Put multiple resources into the team by setting:
  - `resource.setConstraintAliasId("Team Aachen")`
- Attach a **mandatory resource constraint** requiring `"Team Aachen"` to the node set.

The solver can then choose *which* team member actually serves the node, based on:
- feasibility,
- distance,
- time windows,
- and objective weights.

### 2) Prohibit a group from visiting specific customers
"Team TempWorkers must never visit VIP customers."

- Set alias for all temporary staff to `"TempWorkers"`.
- Attach a **banned/excluding constraint** using `"TempWorkers"` to VIP nodes.

### 3) Preference for a pool (soft bias)
"Prefer the EV fleet downtown, but allow exceptions."

- All EV resources: alias `"Fleet-EV"`.
- Add a **preferred resource constraint** referencing `"Fleet-EV"` to downtown nodes.

This provides a clean and explainable policy:
- EVs are preferred where possible,
- but the solver can still use other vehicles if constraints require it.

## How to use alias IDs with the four resource-condition types

Alias IDs are compatible with resource constraints that match on resource identifiers.

### Binding constraints (who should do it)
- **Mandatory (hard)**: Only a resource whose alias matches, or whose ID matches if it has no alias, is allowed.
- **Preferred (soft)**: Matching resources are preferred; others are still allowed with an additional cost.

### Excluding constraints (who should not do it)
- **Banned (hard)**: Matching resources are not allowed.
- **UnPreferred (soft)**: Matching resources are allowed but penalized.

Operationally:
- Use **hard** when the rule is truly non-negotiable (compliance, legal, equipment).
- Use **soft** when the rule is a dispatch policy that can be violated in exceptional cases.

## Modeling patterns that scale well

### Pattern A: Team aliases + skill constraints
Use alias IDs for "team ownership" and skill constraints for "capability".

Example:
- Team Aachen can serve the region (alias constraint),
- but only some resources have "plumbing level >= 3" (skill constraint).

This creates a robust and explainable model:
- assignment must satisfy both *group rule* and *capability rule*.

### Pattern B: Multiple aliases for the same resource (use with care)
A resource supports only one `constraintAliasId` value. If you need multiple group memberships:
- use skill/type constraints for one dimension,
- and alias IDs for another,
or
- implement a policy where alias is the "primary group" and skills cover additional membership.

### Pattern C: Alias IDs as stable integration keys
If your external systems use "team IDs" (e.g., `TEAM_42`), use those directly as alias IDs.
This avoids mapping and reduces integration errors.

## Performance and maintainability advantages

### Reduced constraint cardinality
Without alias IDs, a team rule needs to list every team member in constraints:
- 50 team members x 10,000 nodes is large and error-prone.

With alias IDs:
- the constraint references a single string ("Team Aachen") regardless of team size.

### Stable documentation and stable APIs
You can modify team membership by changing only:
- `resource.setConstraintAliasId(...)`

No changes are needed on the node constraints, which is usually the larger and more dynamic set.

## Pitfalls and how to avoid them

### Pitfall 1: Alias IDs not set consistently
If one team member forgets to set an alias, it will not match team constraints and may appear "unexpectedly unused".

Mitigation:
- enforce alias assignment in your resource factory/builder,
- add a validation step before starting the run.

### Pitfall 2: Confusing alias IDs with skills
Alias IDs express *group membership*, not capabilities.
If you need capability checks, use:
- type/skill constraints (including expertise levels),
- and cost models as needed.

### Pitfall 3: Team members with very different start locations
If a team contains resources with very different depots, the solver may still assign the "wrong" depot if it is feasible and cheaper.
If you need geographic separation, combine:
- alias constraints with zone codes,
- or with explicit location-based constraints.

### Pitfall 4: Reporting and explainability
In your UI/reporting, make it clear:
- which physical resource served the node,
- and which alias constraint was satisfied.

A simple pattern is to display:
- "Assigned Resource: Jack (Team Aachen)".

## Recommendations for production use

1. Use alias IDs to express **teams/pools** and keep node constraints stable.
2. Use skills/types to express **capabilities**, not alias IDs.
3. Use hard constraints only when rules are truly non-negotiable.
4. Add input validation: ensure every resource has the expected alias when your model relies on it.
5. Treat alias IDs as part of your domain model: version and document them like any other business identifier.

## Summary

- `constraintAliasId` lets you reference a **logical group** of resources in constraints.
- Constraints target a resource's **alias** if it is set, and its **ID** otherwise: once set, the alias replaces the ID.
- This enables scalable and maintainable modeling of teams, pools, and fleet classes.
- The provided example (`ResourceConstraintAliasIdExample`) demonstrates team membership via alias IDs and mandatory constraints using team names, allowing the solver to pick any member of the required team while optimizing the rest of the plan.

## Run it

Run the `main(String[] args)` method of one of the four classes from your IDE, with the repository imported as a Maven project. You need a JDK in the version that the examples pom sets. You need no license file: without one, the examples use the public evaluation license (see the [license guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/license)).

## Related

- [Advanced examples](../README.md): the overview of all Advanced examples.
- [Recommended ways to run an optimization](../../basic/recommendedimplementation_02/RecommendedImplementation.md): the recommended run pattern, a synchronous run and the legacy callback methods.
- [Mandatory, preferred, banned and unpreferred resources for a node](../condition/BindingExcludingResourceCondition.md): the four constraint types with resource ids instead of aliases.
- [Optimization properties](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties): the property reference, with `JOptWeight.PreferableResource` and `JOptWeight.UnPreferableResource`.

---

Added: 2026-02-17 (JOpt core 7.5.3-j17) | Last updated: 2026-10-21
