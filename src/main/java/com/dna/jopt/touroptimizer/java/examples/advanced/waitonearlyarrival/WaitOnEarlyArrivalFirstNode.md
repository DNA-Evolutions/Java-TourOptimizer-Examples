# Wait On Early Arrival (First Node) — Prevent “Working Before Shift Start” Only at Route Start

In time-window routing, there are two common interpretations when a resource arrives early:

1. **Wait** until the opening/working time begins (classic VRPTW behavior).
2. **Start work immediately** (common in field service when “arrival earlier than planned” is operationally acceptable).

In practice, many operations combine both:
- Starting a route earlier than shift start is not allowed (labor rules, depot opening, dispatch rules),
- but once the route has started, early arrivals at subsequent stops can be handled immediately.

This example demonstrates exactly that behavior:
- allow immediate service at a node upon arrival (**no waiting**),
- but **if the node is the first node of the route**, enforce waiting so the work does not start before WorkingHours.

---

## References

- Example source: [WaitOnEarlyArrivalFirstNodeExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/waitonearlyarrival/WaitOnEarlyArrivalFirstNodeExample.java)

---

## The two key switches

The behavior is configured per node using two flags:

### 1) `setWaitOnEarlyArrival(false)`
Meaning:
- If the resource arrives before the node’s opening time, it is allowed to start service immediately.
- This suppresses the typical “arrive early → idle until open” behavior.

In the example, all nodes use:
- `node.setWaitOnEarlyArrival(false);`

### 2) `setWaitOnEarlyArrivalFirstNode(true)`
Meaning:
- If the node becomes the **first node of a route**, then waiting rules apply (even if `setWaitOnEarlyArrival(false)` is set).
- This prevents the optimizer from exploiting early arrivals to “start the shift earlier than allowed” by simply choosing an early-open node first.

In the example, all nodes use:
- `node.setWaitOnEarlyArrivalFirstNode(true);`

Combined interpretation:
- **First node**: waiting is enforced (route start cannot begin “too early”).
- **All subsequent nodes**: no waiting; service may start immediately on arrival.

This is a clean architectural way to model:
- “Start work only within WorkingHours, but once you are out, finish early if you can.”

---

## How the example creates a clear test case

### WorkingHours
The resource (“Jack”) has WorkingHours on May 6, 2020:
- 08:00 to 18:00 (Europe/Berlin)

### OpeningHours
Two opening-hour profiles are used:

- “Early” nodes open from **10:00–16:00**
- “Late” node opens from **14:00–19:00** (Wuppertal)

### Why Wuppertal is important
The source comment explains the intent:

- Wuppertal is open 14–19.
- The solver will reach it early.
- If it is **not** the first node, the model allows immediate work anyway (no waiting).
- If it **were** the first node, waiting would be enforced (because of the first-node rule).

This makes the example easy to reason about:
- you can compare “first-node behavior” vs “mid-route behavior” without changing global settings.

---

## When this feature is operationally correct

This pattern is realistic in many field service and delivery settings:

- A technician cannot start before shift begins (HR / dispatch rule),
- but once the technician is in the field, early arrivals can be served immediately, because:
  - the customer is happy to be served early,
  - service can begin as soon as the technician arrives,
  - the strict “time window” is more a planning guideline than a hard gate.

---

## When you should not use it

Do not use this behavior if your opening hours are hard operational constraints, for example:

- access gates that are physically closed,
- regulated slots (medical appointments),
- warehouse docks with strict appointment times,
- customers that must not be served early.

In those cases:
- keep `setWaitOnEarlyArrival(true)` (default waiting),
- and do not relax mid-route waiting.

---

## Recommended production guidance

### 1) Use it selectively
You can enable this only for nodes where “early service is acceptable”:
- same-day service visits,
- flexible customers,
- tasks without strict appointment schedules.

Keep strict nodes in standard waiting mode.

### 2) Keep the first-node rule enabled when shift start must be respected
If WorkingHours represent legal/contractual time boundaries, it is recommended to:
- keep `setWaitOnEarlyArrivalFirstNode(true)` for applicable nodes.

This prevents subtle schedule artifacts where the optimizer effectively starts work before the shift.

### 3) Make sure your downstream execution system aligns
If your dispatch or mobile app assumes:
- “do not start before OpeningHours”,
then relaxing early-arrival waiting may produce plans that the execution system rejects.

Align planning and execution semantics.

---

## Summary

- `setWaitOnEarlyArrival(false)` allows immediate service even when arriving “early”.
- `setWaitOnEarlyArrivalFirstNode(true)` re-enables waiting *only* when the node is the first node of the route.
- The combination is a practical way to enforce “no work before shift start” while still exploiting early arrivals later in the route.
- The example uses Wuppertal’s late opening hours to illustrate the difference between first-node and mid-route behavior.
