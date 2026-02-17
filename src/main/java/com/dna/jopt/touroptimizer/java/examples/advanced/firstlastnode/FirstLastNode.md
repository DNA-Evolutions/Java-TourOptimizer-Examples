# First/Last Node in Route — Softly Steering Route Anchors via Importance

In many operational scenarios, planners want routes to begin or end with specific stops, for example:

- “Start the day with a key customer.”
- “Finish the day at a handover location.”
- “Visit a depot first for pickup, then execute deliveries.”
- “End with a return-to-base inspection site.”

JOpt supports this behavior with **first/last node importance**:
- `setFirstNodeInRouteImportance(int importance)`
- `setLastNodeInRouteImportance(int importance)`

These settings create **soft route anchoring**:
- the optimizer is incentivized (by penalty cost) to place the node first/last,
- but it may still choose a different first/last node if the global savings outweigh the penalty.

This makes the feature practical and safe:
- it guides the optimizer strongly,
- but it does not instantly destroy feasibility when reality prevents strict anchoring.

---

## References

- Example source: [FirstLastNodeExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/firstlastnode/FirstLastNodeExample.java)

---

## What the example demonstrates

The example `FirstLastNodeExample` models two “route anchor” preferences:

- **Koeln** should be visited as the **first** node in the route.
- **Wuppertal** should be visited as the **last** node in the route.

Implementation (as shown in the example):

- Koeln:
  - `koeln.setFirstNodeInRouteImportance(10);`

- Wuppertal:
  - `wuppertal.setLastNodeInRouteImportance(10);`

The comment in the source explains the core logic:

- The optimizer considers alternatives only if the cost savings of violating the preference exceed the penalty for not using the intended node as first/last.

---

## Hard vs soft interpretation (important)

First/last-node importance is a **soft constraint mechanism**:
- It is implemented as a penalty term (importance-driven) rather than as an architectural feasibility rule.

Meaning:
- The solver *can* violate it.
- You should not expect a guaranteed “first/last” if time windows, working hours, or other hard constraints make it impossible or extremely expensive.

If you need a strict hard constraint (e.g., “pickup must happen first”), you typically model it differently (e.g., via relationships or explicit structure), depending on your overall architecture.

---

## How “importance” works

### The importance value is the strength of the preference
Higher importance means:
- higher penalty cost if the node is not first/last,
- therefore stronger pull towards being first/last.

Lower importance means:
- the preference can be violated more easily when it improves the plan elsewhere.

The value is an integer, and the example uses `10` to make the effect visible.

### Importance interacts with weights
The example increases general weights related to node handling:

- `JOptWeight.NodeImportance = 5`
- `JOptWeight.NodeType = 5.0`

This is done to ensure penalties and node-related costs are strong enough to “matter” in the global objective.

Practical guidance:
- If first/last preferences appear too weak, increase:
  - first/last importance value, and/or
  - the relevant weights for node importance.
- If first/last preferences dominate too aggressively, reduce them.

---

## Scenario structure in the example

### Time model
- Node OpeningHours: March 6–7, 2020, 08:00–17:00 (Europe/Berlin)
- Resource WorkingHours: March 6–7, 2020, 08:00–17:00 (Europe/Berlin)

### Nodes
The example uses `TimeWindowGeoNode` for multiple cities, including:
- Koeln (first-node preference)
- Wuppertal (last-node preference)
- Essen, Dueren, Aachen (and potentially others in the file)

All nodes share:
- visit duration: 20 minutes
- importance: 1 (base importance)

### Resources
Two resources are created (Jack and John), both starting in Aachen, with identical:
- max distance: 1200 km
- cost configuration
- working hours

This makes the “first/last” behavior easier to interpret because resources are not artificially different.

---

## When to use first/last node importance in production

### Strong fit use cases
- “Try to start with a VIP customer” (preference, not a legal must)
- “Try to end with refueling location” (preference)
- “Try to end near a depot” (preference, not strict)
- “Try to start at a pickup location but allow exceptions” (preference)

### Cases where you should consider alternative modeling
If the “first/last” requirement is truly hard, such as:
- “Pickup must happen first, otherwise deliveries cannot happen.”
- “End must be a mandatory depot return for compliance.”

Then you usually want an architectural hard constraint model (relationships, mandatory nodes, pillars, etc.), rather than an importance-based preference.

---

## How to tune and validate behavior

### Step 1 — Start with moderate importance (e.g., 5–10)
Confirm:
- the preference is usually satisfied in normal instances.

### Step 2 — Stress test with infeasible conditions
Deliberately create cases where:
- time windows force a different first/last order,
- working hours are tight.

Confirm:
- the solver violates the preference only when it produces a better global result or restores feasibility.

### Step 3 — Validate downstream KPIs
If first/last anchoring is used for operational workflow reasons, ensure:
- route execution system agrees with “planned first/last” semantics,
- waiting times and arrival times remain sensible,
- the business understands when and why exceptions occur.

---

## Summary

- `setFirstNodeInRouteImportance(...)` and `setLastNodeInRouteImportance(...)` provide **soft** route anchoring.
- A higher importance increases the penalty for not being first/last, making the preference stronger.
- The optimizer can still violate the preference when the global savings outweigh the penalty or when constraints make it impractical.
- The example demonstrates:
  - Koeln as first node (importance 10),
  - Wuppertal as last node (importance 10),
  and uses higher node-related weights to make the effect clearly observable.
