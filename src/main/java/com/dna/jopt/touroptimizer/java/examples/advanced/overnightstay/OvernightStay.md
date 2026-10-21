# Overnight stays on a tour over several days

`OvernightStayExample` plans a tour from Aachen to Wien, Zagreb, Innsbruck and Stuttgart over four working days, and Jack spends three nights at nodes on the way instead of driving home each evening. The example sets stay nodes, allows a stay on every working day and adds a stay-out policy and limits for stays in a row. The page is for developers who plan tours that a resource cannot cover in day trips from home; the [overnight stay feature guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/overnight_stay) describes the feature, and the [First optimization example](../../basic/firstoptimization_01/FirstOptimizationExample.md) is the place to start if you have not built an optimization yet.

## What the example shows

Many real routing problems cannot be solved with "day trips only".
Examples:
- field service across large regions,
- multi-day tours (e.g., Europe-wide),
- long-haul deliveries,
- seasonal campaigns.

For these cases, JOpt supports **overnight stays** ("stay out") as part of the routing model.

- **Stay nodes:** `setIsStayNode(true)` marks a node at which a working day may end. The next working day of the resource then starts at that node, not at home. All five nodes of the example are stay nodes.
- **Working days that allow a stay:** `setIsAvailableForStay(true)` on a `WorkingHours` allows a stay at the end of that day. The example calls it on all six working hours of Jack. The feature guide explains this setting per day.
- **The stay-out policy:** `setStayOutPolicy(minDistanceForStayOut, minTimeForStayOut)` sets two thresholds for the way back from a stay node to the start location of the resource. Each threshold counts only when its flag is set: `setStayOutPolicyReturnDistanceActive(true)` for the distance, `setStayOutPolicyReturnTimeActive(true)` for the time. The example sets both, and then one of them is enough: a stay at a node is allowed when the way back is at least 100 km long or takes at least 4 hours. With neither flag set, the policy is not checked.
- **Limits for stays:** `setStaysOut(totalStaysOut, staysOutInRow, minRecoverHours)` limits the stays of the resource:
  - `totalStaysOut` is the number of stays over all working days; `-1` sets no limit.
  - `staysOutInRow` is the number of stays in a row that the resource may make; with `4`, a fifth stay in a row is not allowed.
  - `minRecoverHours` counts `WorkingHours`, here working days, not hours: with `2`, two working days that end at home have to pass before the resource stays out again.
- **How the optimizer applies them:** in this example the optimizer ends a working day at a stay node only where the policy and the limits allow it. Neither adds a cost to the plan.
- **An optional node:** `VenedigOptional` is a stay node and optional at the same time (`setIsOptional(true)`), so the optimizer may leave it out of the plan.

## Scenario

The dates, times and coordinates are sample values. The time zone is Europe/Berlin.

- **Resource:** `Jack`, a `CapacityResource` at 50.775346, 6.083887 (Aachen). He works on six days, May 6 to May 11, 2020, from 08:00 to 20:00, and every working day allows a stay. His maximal working time is 12 hours, the length of the working hours, and his maximal distance 1200 km per day, more than the longest day of this plan needs.
- **Nodes:** five `TimeWindowGeoNode` objects, each a stay node, with a visit of one hour and the same importance. Each node is open on one day only, from 08:00 to 20:00:
  - `Wien` on May 6;
  - `Zagreb` on May 7;
  - `Innsbruck` and the optional `VenedigOptional` on May 8;
  - `Stuttgart` on May 9.

  One day per node fixes the order of the tour, and the optimizer decides where each working day ends.
- **Policy:** a stay needs a way back to Aachen of at least 100 km or at least 4 hours; both thresholds are sample values. Stuttgart, the node nearest to Aachen, lies 317.6 km from it on the way back (see the last route of the output), so the policy allows a stay at every node of this example.
- **Limits:** no limit on the total number of stays, at most 4 stays in a row, and two working days that end at home before the next stay. The limits are sample values.

`addProperties` sets `JOptExitCondition.JOptGenerationCount` to 1000 and `JOpt.Algorithm.PreOptimization.SA.NumIterations` to 100000. Both are below their defaults, which keeps the run short; the defaults are in the [property reference](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties).

## How the code works

The source is [OvernightStayExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/overnightstay/OvernightStayExample.java). `example()` runs the steps in order:

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

`attachToObservables` subscribes to the progress, warning, status and error events with the event API, before the run starts. `startAndPresentResult` starts the run, waits up to 5 minutes for the result and prints it.

### Stay nodes

`addNodes` creates the five nodes. Zagreb is a plain stay node, `VenedigOptional` is optional as well:

```java
TimeWindowGeoNode zagreb =
    new TimeWindowGeoNode(
        "Zagreb", 45.815011, 15.981919, createOpeningHours(7), visitDuration, importance);
// A stay node: a working day may end here, and the next working day starts here.
zagreb.setIsStayNode(true);
opti.addElement(zagreb);

TimeWindowGeoNode venedigOptional =
    new TimeWindowGeoNode(
        "VenedigOptional",
        45.440847,
        12.315515,
        createOpeningHours(8),
        visitDuration,
        importance);
// Optional: the optimizer may leave the node out of the plan.
venedigOptional.setIsOptional(true);
venedigOptional.setIsStayNode(true);
opti.addElement(venedigOptional);
```

`createOpeningHours(day)` returns the opening hours of one day in May 2020, from 08:00 to 20:00.

### Working days that allow a stay

`addResources` allows a stay on each of Jack's working days and sets his maximal working time:

```java
List<IWorkingHours> workingHours = createWorkingHours();
// Every working day may end at a stay node.
workingHours.forEach(w -> w.setIsAvailableForStay(true));

// 12 h per day, the length of the working hours. The drive from Aachen to Wien and back alone
// takes longer, so the tour needs stays.
Duration maxWorkingTime = Duration.ofHours(12);
```

The feature guide shows how to keep a resource at home on certain days.

### The stay-out policy

```java
// The stay-out policy, with sample thresholds: a stay at a node is allowed only if the way
// back from the node to Aachen is at least 100 km long or takes at least 4 hours. Each
// threshold counts only when its flag is set.
Quantity<Length> minDistanceForStayOut = Quantities.getQuantity(100, KILO(METRE));
Duration minTimeForStayOut = Duration.ofHours(4);
jack.setStayOutPolicy(minDistanceForStayOut, minTimeForStayOut);
jack.setStayOutPolicyReturnDistanceActive(true);
jack.setStayOutPolicyReturnTimeActive(true);
```

With only `setStayOutPolicyReturnDistanceActive(true)`, the 4 hours would not count, and a stay would need the 100 km.

### Limits for stays

```java
// Sample limits: -1 sets no limit on the total number of stays, at most 4 stays in a row, and
// at least 2 working days that end at home before the next stay.
int totalStaysOut = -1;
int staysOutInRow = 4;
int minRecoverHours = 2;
jack.setStaysOut(totalStaysOut, staysOutInRow, minRecoverHours);
```

## Output and what to look for

The run prints the status and progress lines, then the result. The result was the same in two test runs. Each route has a `Start Id`, where the working day begins, and a `Termination Id`, where it ends; `Jack` there means Jack's home. The routes, trimmed to these lines:

```text
Start Id               : Jack
Termination Id         : Wien
0.0 Wien / Geo  / Arrival: 06.05.2020 17:48:51 (Europe/Berlin) , Departure: 06.05.2020 18:48:51 (Europe/Berlin) / Duration: 60.0 [min] / Driving: 588.86 [min], 777.29 [km]
Start Id               : Wien
Termination Id         : Zagreb
1.0 Zagreb / Geo  / Arrival: 07.05.2020 11:22:39 (Europe/Berlin) , Departure: 07.05.2020 12:22:39 (Europe/Berlin) / Duration: 60.0 [min] / Driving: 202.66 [min], 267.51 [km]
Start Id               : Zagreb
Termination Id         : Innsbruck
2.0 Innsbruck / Geo  / Arrival: 08.05.2020 12:55:10 (Europe/Berlin) , Departure: 08.05.2020 13:55:10 (Europe/Berlin) / Duration: 60.0 [min] / Driving: 295.18 [min], 389.63 [km]
Start Id               : Innsbruck
Termination Id         : Jack
3.0 Stuttgart / Geo  / Arrival: 09.05.2020 10:59:23 (Europe/Berlin) , Departure: 09.05.2020 11:59:23 (Europe/Berlin) / Duration: 60.0 [min] / Driving: 179.4 [min], 236.81 [km]
```

- **Three stays:** the days of May 6, 7 and 8 end at Wien, Zagreb and Innsbruck, and each next day starts there. On May 9, Jack visits Stuttgart and drives home; the last route shows the way home with 317.6 km and 240.6 minutes (`Termi. distance` and `Termi. time`).
- **Why the tour needs stays:** the drive from Aachen to Wien alone takes 588.86 minutes, and with the visit the first day takes 648.86 minutes (`Route time`). A drive back to Aachen on the same day would not fit into the maximal working time of 12 hours.
- **No violation:** the violation summaries of all four routes are empty, and every route ends before 20:00.
- **The limits:** the plan has three stays in a row, so the limit of 4 stays in a row does not bind here, and with one series of stays the 2 working days at home do not come into play.
- **The optional node:** in both runs, `VenedigOptional` was not on any route.
- **Four of six days:** the plan uses the working days from May 6 to May 9.

## What the example does

### 1) Multi-day working hours
The resource is created with WorkingHours spanning multiple days:

- May 6-11, 2020
- each day from 08:00 to 20:00 (Europe/Berlin)

### 2) Explicitly enable overnight stays for all working days
Because `WorkingHours` are **not available for stay by default**, the example explicitly enables overnight stays across all six working days:

```java
// Every working day may end at a stay node.
workingHours.forEach(w -> w.setIsAvailableForStay(true));
```

Meaning:
- without this call, the resource would never be permitted to stay out, even if stay nodes exist and policy thresholds are met,
- enabling it per-day gives you fine-grained control: you can selectively call `setIsAvailableForStay(false)` on individual `WorkingHours` objects for days where the resource must return home (maintenance day, rest day, depot day).

This opt-in default is intentional: overnight stays are a significant operational decision and should be consciously enabled, not accidentally inherited.

### 3) Define a "stay out policy" threshold
The example sets a threshold that must be met before staying out is permitted:

- minimum distance from home: **100 km**, OR
- minimum travel time from home: **4 hours**

```java
jack.setStayOutPolicy(minDistanceForStayOut, minTimeForStayOut);
jack.setStayOutPolicyReturnDistanceActive(true);
jack.setStayOutPolicyReturnTimeActive(true);
```

Practical interpretation:
- do not allow overnight stays for "small" tours close to home,
- only allow stay outs once the route is sufficiently far away to justify it.

The `ReturnDistanceActive` flag switches on the distance threshold, and the `ReturnTimeActive` flag the time threshold. With both flags set, as in the example, one threshold is enough (the OR above); with neither flag set, the policy is not checked.

### 4) Add stay-out restrictions
The example restricts how staying out can be used across multiple days:

```java
// Sample limits: -1 sets no limit on the total number of stays, at most 4 stays in a row, and
// at least 2 working days that end at home before the next stay.
int totalStaysOut = -1;
int staysOutInRow = 4;
int minRecoverHours = 2;
jack.setStaysOut(totalStaysOut, staysOutInRow, minRecoverHours);
```

Operational interpretation:
- You may allow multi-day routes,
- but you still enforce employee wellbeing / regulatory compliance patterns:
  - limited consecutive nights away,
  - mandatory multi-night recovery at home.

(The code comment describes the intended logic in human terms.)

## How to interpret feasibility and results

### Without overnight stays
A Europe-wide tour would typically violate:
- maxWorkingTime per day,
- working hours windows,
- and would force unrealistic "teleporting" or infeasibility.

### With overnight stays enabled
The solver can:
- end a day at a stay node,
- continue the next day from that position,
- respecting WorkingHours and OpeningHours across days.

### What changes in the plan
When stay out is allowed, you should see:
- multi-day sequencing,
- an implicit "end-of-day" cut,
- the next day starting from the overnight position instead of the home depot.

## Recommended production modeling patterns

### Pattern A: Mark realistic overnight points as stay nodes
Do not mark every customer as a stay node unless it truly makes sense.
Typical stay nodes:
- hotels,
- depots,
- service bases,
- partner hubs,
- safe parking areas.

### Pattern B: Use policy thresholds to avoid unnecessary stays
The example's min distance/time thresholds are a best-practice:
- short routes should return home,
- staying out is for "far tours".

### Pattern C: Enforce consecutive-night limits and recovery
`setStaysOut(...)` is the key tool to express:
- "how many nights out are allowed",
- and "how much recovery is required".

### Pattern D: Control stay-out allowance per day using WorkingHours
Because `WorkingHours` are **not available for stay by default**, you must explicitly call `setIsAvailableForStay(true)` for each day on which overnight stays should be allowed. To then forbid staying out on specific days, simply omit the call (or explicitly call `setIsAvailableForStay(false)`) for those `WorkingHours` objects.

A sketch, not code of the example classes:

```java
// Enable stay for most days
workingHours.forEach(w -> w.setIsAvailableForStay(true));
// Then selectively forbid it for a specific day
workingHoursMonday.setIsAvailableForStay(false);
```

This is very clean and avoids complicated custom constraints.

## Pitfalls and how to avoid them

### Pitfall 1: No stay nodes defined
If no nodes are marked as stay nodes, the solver has nowhere to "place" an overnight.
Ensure at least some realistic stay nodes exist.

### Pitfall 2: Stay nodes conflict with time windows
Stay nodes still use OpeningHours. If the time windows are too strict, the solver may not be able to use them effectively as overnight positions.
Give stay nodes realistic windows.

### Pitfall 3: Policy thresholds too restrictive
If you set min distance/time thresholds too high, you may block stay outs even when the tour obviously needs them.
Calibrate thresholds based on real operations.

### Pitfall 4: Ambiguous interpretation in downstream systems
Multi-day routing needs downstream support:
- route execution apps must understand day boundaries,
- reporting must show per-day segments,
- dispatch must communicate overnight locations.

## Summary

- Overnight stays ("stay out") enable realistic multi-day routing.
- Nodes must be flagged as stay nodes via `setIsStayNode(true)` to serve as overnight positions.
- Resources can restrict stay outs:
  - per day via `WorkingHours.setIsAvailableForStay(false)`,
  - by policy thresholds (`setStayOutPolicy(minDistance, minTime)`, each threshold only with its flag, `setStayOutPolicyReturnDistanceActive(true)` or `setStayOutPolicyReturnTimeActive(true)`),
  - and by multi-day limits (`setStaysOut(total, inRow, minRecoverHours)`).
- The example demonstrates:
  - explicitly enabling overnight stays for all six working days (May 6-11) via `setIsAvailableForStay(true)`, required because the default is `false`,
  - permitting stay out only beyond 100 km or 4 hours from home,
  - limiting consecutive nights out and requiring recovery at home.

## Run it

Run the `main(String[] args)` method of `OvernightStayExample` from your IDE, with the repository imported as a Maven project. You need a JDK in the version that the examples pom sets. You need no license file: without one, the example uses the public evaluation license (see the [license guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/license)).

## Related

- [Overnight stay feature guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/overnight_stay): the feature, the setting per working day, the stay-out policy, the limits and guidance for modeling stays.
- [Optimization properties](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties): the property reference, with the defaults.
- [Advanced examples](../README.md): the overview of all Advanced examples.
- [Recommended ways to run an optimization](../../basic/recommendedimplementation_02/RecommendedImplementation.md): the recommended run pattern, a synchronous run and the legacy callback methods.

---

Added: 2026-02-17 (JOpt core 7.5.3-j17) | Last updated: 2026-10-21
