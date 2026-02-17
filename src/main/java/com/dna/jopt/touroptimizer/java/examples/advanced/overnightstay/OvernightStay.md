# OvernightStay — Allowing Multi-Day Routes With “Stay Out” Policies

Many real routing problems cannot be solved with “day trips only”.
Examples:
- field service across large regions,
- multi-day tours (e.g., Europe-wide),
- long-haul deliveries,
- seasonal campaigns.

For these cases, JOpt supports **overnight stays** (“stay out”) as part of the routing model.

The `OvernightStayExample` demonstrates how to:
- enable nodes as **stay nodes** (eligible end-of-day / overnight positions),
- control on which days a resource is **allowed to stay out**,
- define **policy thresholds** when staying out becomes allowed,
- and apply **restrictions** such as maximum consecutive nights out and minimum recovery time at home.

---

## References

- Example source: [OvernightStayExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/overnightstay/OvernightStayExample.java)

---

## The concept in JOpt terms

### Stay nodes (`setIsStayNode(true)`)
A **stay node** is a regular node that is additionally marked as:
- eligible for “overnight positioning”.

In the example:
- multiple cities are marked as stay nodes by calling:
  - `node.setIsStayNode(true)`

This indicates to the solver:
- if the tour spans multiple days, the resource may end a day at such a node.

### Resource “stay out” capability (WorkingHours + policy)
Overnight behavior is controlled on the **resource side** using two mechanisms:

1) **Per-day allowance**: WorkingHours can be configured as *available for stay* or not.
2) **Policy thresholds and restrictions**: the resource can define when staying out is allowed and how it is limited across consecutive days.

---

## What the example does

### 1) Multi-day working hours
The resource is created with WorkingHours spanning multiple days:

- May 6–11, 2020
- each day from 08:00 to 20:00 (Europe/Berlin)

### 2) Disallow overnight on a specific day
The example explicitly forbids staying out on **May 6**:

```java
forbiddenStayOutWOH.setIsAvailableForStay(false); // Default is true
```

Meaning:
- the route may operate that day,
- but it must not end the day “out” (i.e., an overnight stay is not allowed for that day’s WorkingHours block).

This is a powerful control lever in practice:
- forbid staying out on days where a resource must return (maintenance day, rest day, depot day).

### 3) Define a “stay out policy” threshold
The example sets a threshold that must be met before staying out is permitted:

- minimum distance from home: **100 km**, OR
- minimum travel time from home: **4 hours**

```java
rep1.setStayOutPolicy(minDistanceForStayOut, minTimeForStayOut);
rep1.setStayOutPolicyReturnDistanceActive(true);
```

Practical interpretation:
- do not allow overnight stays for “small” tours close to home,
- only allow stay outs once the route is sufficiently far away to justify it.

The `ReturnDistanceActive` flag indicates that the policy should consider the return-distance logic as part of the decision (i.e., the economics of returning vs staying out).

### 4) Add stay-out restrictions
The example restricts how staying out can be used across multiple days:

```java
int totalStaysOut = -1;      // unlimited total nights out
int staysOutInRow = 4;       // after (effectively) 3 nights out in a row, return is required
int minRecoverHours = 2;     // recovery at home must last at least 2 nights
rep1.setStaysOut(totalStaysOut, staysOutInRow, minRecoverHours);
```

Operational interpretation:
- You may allow multi-day routes,
- but you still enforce employee wellbeing / regulatory compliance patterns:
  - limited consecutive nights away,
  - mandatory multi-night recovery at home.

(The code comment describes the intended logic in human terms.)

---

## The nodes in the example (why they are chosen)

The example uses a path across Europe to make “stay out” behavior meaningful:

- Zagreb (day 0)
- VenedigOptional (day 0, optional)
- Innsbruck (day 1)
- Wien (day 2)
- Mannheim (day 2)

Key modeling signals:
- all major nodes are marked as stay nodes,
- one node is optional (`VenedigOptional.setIsOptional(true)`), illustrating that optionality and stay capability can coexist.

This setup encourages the solver to:
- split the tour across multiple days,
- and use eligible overnight points where needed.

---

## How to interpret feasibility and results

### Without overnight stays
A Europe-wide tour would typically violate:
- maxWorkingTime per day,
- working hours windows,
- and would force unrealistic “teleporting” or infeasibility.

### With overnight stays enabled
The solver can:
- end a day at a stay node,
- continue the next day from that position,
- respecting WorkingHours and OpeningHours across days.

### What changes in the plan
When stay out is allowed, you should see:
- multi-day sequencing,
- an implicit “end-of-day” cut,
- the next day starting from the overnight position instead of the home depot.

---

## Recommended production modeling patterns

### Pattern A — Mark realistic overnight points as stay nodes
Do not mark every customer as a stay node unless it truly makes sense.
Typical stay nodes:
- hotels,
- depots,
- service bases,
- partner hubs,
- safe parking areas.

### Pattern B — Use policy thresholds to avoid unnecessary stays
The example’s min distance/time thresholds are a best-practice:
- short routes should return home,
- staying out is for “far tours”.

### Pattern C — Enforce consecutive-night limits and recovery
`setStaysOut(...)` is the key tool to express:
- “how many nights out are allowed”,
- and “how much recovery is required”.

### Pattern D — Control stay-out allowance per day using WorkingHours
If certain days must end at home:
- set `workingHours.setIsAvailableForStay(false)` for those days.

This is very clean and avoids complicated custom constraints.

---

## Pitfalls and how to avoid them

### Pitfall 1 — No stay nodes defined
If no nodes are marked as stay nodes, the solver has nowhere to “place” an overnight.
Ensure at least some realistic stay nodes exist.

### Pitfall 2 — Stay nodes conflict with time windows
Stay nodes still use OpeningHours. If the time windows are too strict, the solver may not be able to use them effectively as overnight positions.
Give stay nodes realistic windows.

### Pitfall 3 — Policy thresholds too restrictive
If you set min distance/time thresholds too high, you may block stay outs even when the tour obviously needs them.
Calibrate thresholds based on real operations.

### Pitfall 4 — Ambiguous interpretation in downstream systems
Multi-day routing needs downstream support:
- route execution apps must understand day boundaries,
- reporting must show per-day segments,
- dispatch must communicate overnight locations.

---

## Summary

- Overnight stays (“stay out”) enable realistic multi-day routing.
- Nodes must be flagged as stay nodes via `setIsStayNode(true)` to serve as overnight positions.
- Resources can restrict stay outs:
  - per day via `WorkingHours.setIsAvailableForStay(false)`,
  - by policy thresholds (`setStayOutPolicy(minDistance, minTime)`),
  - and by multi-day limits (`setStaysOut(total, inRow, minRecoverHours)`).
- The example demonstrates:
  - forbidding stay out on May 6,
  - allowing multi-day work May 7–11,
  - permitting stay out only beyond 100 km or 4 hours from home,
  - limiting consecutive nights out and requiring recovery at home.

