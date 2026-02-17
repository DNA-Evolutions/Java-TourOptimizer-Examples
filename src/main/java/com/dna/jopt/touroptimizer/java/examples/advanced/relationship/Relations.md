# Relations — Coupling Nodes Across Routes, Resources, and Time

Relations are one of the most powerful modeling tools in **JOpt.TourOptimizer**.  
They let you express dependencies *between nodes* that cannot be captured by distance-based cost alone, such as:

- “These two stops must be served by the same technician.”
- “These two stops must not be on the same route.”
- “The second stop must start 20 minutes after arriving at the first stop.”
- “These two stops must be in the same route and occur in a defined time gap.”

In JOpt, relations are **architectural constraints**. They influence feasibility and assignment decisions directly, rather than being “just a high cost penalty”.

This package focuses on **node-to-node relations**, split into two main categories:

- **Visitor Relations**: constrain *who* visits which nodes and whether nodes share a route.
- **Tempus Relations**: constrain *when* nodes are visited relative to each other.

---

## References (examples)

All examples are in:
- https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/relationship

### Visitor relations (route/resource coupling)
- Same route:  
  [SameRouteRelationExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/relationship/SameRouteRelationExample.java)  

- Same visitor (same resource):  
  [SameVisitorRelationExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/relationship/SameVisitorRelationExample.java)  

- Different routes, same visitor allowed:  
  [DifferentRouteSameVisitorRelationExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/relationship/DifferentRouteSameVisitorRelationExample.java)  

- Different routes, different visitors required:  
  [DifferentRouteDifferentVisitorRelationExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/relationship/DifferentRouteDifferentVisitorRelationExample.java)  

### Tempus relations (relative time constraints)
- Relative time window (basic):  
  [RelativeTimeWindowRelationExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/relationship/RelativeTimeWindowRelationExample.java)  

- Relative time window that induces idle/waiting:  
  [RelativeTimeWindowRelationWithInducedIdleTimeExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/relationship/RelativeTimeWindowRelationWithInducedIdleTimeExample.java)  

### Combined visitor + time coupling
- Same route + relative time window (sequencing inside the same route):  
  [SameRouteRelationAndRelativeTimeWindowExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/relationship/SameRouteRelationAndRelativeTimeWindowExample.java)  

---

## Conceptual model

### Master node and related node
Most relations are expressed as:

- **Master node**: the anchor/reference point
- **Related node**: the node whose route/resource/time is constrained relative to the master

Every relation in these examples follows the same pattern:

1. Create a relation object.
2. Set `masterNode`.
3. Set `relatedNode`.
4. Configure relation behavior (same route, same visitor, time window, etc.).
5. Attach the relation to **both** nodes.

Attaching the relation to both nodes is important because:
- it makes the relationship explicit on both elements,
- and ensures the solver’s internal evaluation sees a consistent relation graph.

You will see this pattern in every example.

---

## Visitor relations (route/resource coupling)

Visitor relations are about **assignment structure**:

- Are two nodes visited by the same resource?
- Must they be in the same route?
- Must they be separated into different routes?
- If separated into different routes, must they also be visited by different resources?

In the examples, visitor relations are implemented using:

- `INode2NodeVisitorRelation`
- `RelativeVisitor2RelatedNodeRelation`

Even though the class name contains “RelativeVisitor…”, the relation can enforce several distinct behaviors via configuration methods.

### Important distinction: “same route” vs “same visitor”

- **Same visitor** means: the *same resource* must visit both nodes.
- **Same route** means: the nodes must be on the *same route* of that resource.

Why this matters:
- In multi-day or multi-route scenarios, a single resource can potentially have more than one route (e.g., different WorkingHours blocks).
- “Same visitor” can allow two nodes to be served by the same resource but on different days/routes.
- “Same route” is stricter: the solver must place both stops into the same route instance.

---

### A) Force Same Route

Example: `SameRouteRelationExample`

Purpose:
- Ensure two nodes are visited **in the same route** (and therefore by the same resource as well).

Key call:
- `rel.setIsForcedSameRoute();`

Typical use cases:
- jobs that must be executed in one continuous tour,
- “keep these two stops together” bundling,
- service sequences that must remain in one route for operational continuity.

Practical note:
- This type of relation is often used to prevent natural geographic clustering from splitting critical pairs across routes.

---

### B) Force Same Visitor (Same Resource)

Example: `SameVisitorRelationExample`

Purpose:
- Ensure two nodes are visited by the **same resource**, but not necessarily in the same route.

Key call:
- `rel.setIsForcedSameVisitor();`

Typical use cases:
- “The same technician must do both visits” (customer continuity),
- compliance requirements,
- skill continuity beyond formal skill constraints (e.g., “this customer wants their usual technician”).

When to prefer this over skill constraints:
- Skills model *capability*; same-visitor models *continuity* and *identity*.

---

### C) Force Different Routes (same visitor allowed or forbidden)

Examples:
- `DifferentRouteSameVisitorRelationExample`
- `DifferentRouteDifferentVisitorRelationExample`

Purpose:
- Ensure two nodes are **not in the same route**.

In both examples the relation is configured via:
- `rel.setIsForcedDifferentRoute(isForcedDifferentVisitor);`

Where:
- `isForcedDifferentVisitor = false` → **different routes**, but the **same resource is allowed**  
- `isForcedDifferentVisitor = true`  → **different routes**, and also **different resources are required**

This captures two common business policies:

#### C1) Different routes, same visitor allowed
Use when:
- tasks must not be in the same route (e.g., require separation into different days),
- but continuity of the same resource is still acceptable or desired.

Example scenario:
- a customer wants the same technician, but not two visits in the same day.

#### C2) Different routes, different visitors required
Use when:
- tasks must be separated and also require independent execution by different resources.

Example scenario:
- separation-of-duties compliance,
- independent verification steps,
- safety rules (“two different crews must inspect independently”).

---

## Tempus relations (relative time constraints)

Tempus relations express **relative timing rules** between two nodes.

In the examples they are implemented using:

- `INode2NodeTempusRelation`
- `RelativeTimeWindow2RelatedNodeRelation`

### What a “relative time window” expresses
A relative time window constrains when the related node may occur **relative to a reference timestamp on the master node**.

In the examples, the relative time window is defined as:

- `new RelativeTimeWindow2RelatedNodeRelation(minDelta, maxDelta)`

Conceptually:
- The related node must occur between:
  - masterTimestamp + minDelta
  - masterTimestamp + maxDelta

### Choosing the timestamp: `setTimeComparisonJuncture(...)`

The examples use:

- `rel.setTimeComparisonJuncture(true, true);`

The intent (as stated in the code comments) is:
- compare **arrival timestamps** for master and related nodes.

Practical interpretation:
- the booleans select which “juncture” to use (arrival vs another relevant timestamp such as start-of-service/departure), for each side of the relation.

You should keep this explicit in your model because:
- “arrival-to-arrival” constraints behave differently than “end-of-service-to-start-of-service” constraints, especially when visit durations are non-trivial.

---

### A) Relative time window (tight sequencing)

Example: `RelativeTimeWindowRelationExample`

Purpose:
- Enforce that the related node’s task starts within a defined time window after the master.

In the example:
- master: `Essen`
- related: `Aachen`
- relation window: 0 to 20 minutes after master arrival
- visit duration is 20 minutes, so the model implies:
  - the related task must begin immediately after the master task completes (tight coupling)

Typical use cases:
- follow-up appointments right after a first visit,
- chained jobs with little tolerance,
- handover processes (“visit B shortly after A”).

---

### B) Relative time window that induces idle time (controlled waiting)

Example: `RelativeTimeWindowRelationWithInducedIdleTimeExample`

Purpose:
- Demonstrate that relative timing constraints can intentionally create **waiting**.

In the example:
- relation window: 0 to 40 minutes after master arrival
- visit durations are long enough that this introduces idle time on the related node side (as described in the source comments)

Typical use cases:
- “wait at least X minutes after event A before starting event B,”
- cooling/curing/waiting processes (industrial service),
- regulated delays (e.g., mandatory waiting periods),
- real-world synchronization where one party must wait for another.

Operational implication:
- You should expect idle time to rise (by design).
- This is not “inefficiency”; it is model fidelity.

---

## Combining visitor + tempus relations

Example: `SameRouteRelationAndRelativeTimeWindowExample`

This example demonstrates the most common real-world pattern:

1. Ensure two nodes are on the **same route** (so they are executed by the same resource in one tour)
2. Enforce a **relative time window** between them (so their ordering/timing is controlled)

In the example:
- Visitor relation:
  - `setIsForcedSameRoute()`
- Time relation:
  - `RelativeTimeWindow2RelatedNodeRelation(Duration.ofMinutes(0), Duration.ofMinutes(1000))`
  - time comparison uses arrival timestamps (`setTimeComparisonJuncture(true, true)`)

Why this matters:
- Visitor relations alone do not guarantee the *relative timing* inside a route.
- Tempus relations alone do not guarantee the nodes end up in the *same route*.
- Combining them gives you a clean architectural model for “paired tasks in a controlled sequence”.

Typical use cases:
- pickup and follow-up service in one tour with a time bound,
- chained medical visits,
- equipment install then inspection within a window,
- multi-step field service processes.

---

## Design guidance and best practices

### 1) Decide whether you need “same route” or “same visitor”
Use **same visitor** when:
- identity continuity matters across days/routes.

Use **same route** when:
- the tasks must be executed as one continuous tour segment.

### 2) Keep relative-time windows realistic
A relative-time window that is too tight may:
- cause infeasibility,
- force excessive detours,
- increase idle time,
- or push the optimizer into pathological schedules.

Start with realistic tolerance intervals, then tighten if needed.

### 3) Understand that relations can override natural clustering
Relations can intentionally work *against* pure geographic clustering.  
This is the point: relations encode business reality, not geometry.

If you see longer travel, do not assume it is “worse”:
- it may be the necessary outcome of a business rule.

### 4) Relations are usually “hard by architecture”
In most cases, relations are not modeled as “high cost”.  
They represent feasibility structure and are expected to be fulfilled unless you explicitly model them as soft in a different way.

### 5) Debugging: verify relations early
A recommended workflow:
1. Build a tiny instance (5–10 nodes) with one relation.
2. Verify that the relation is respected.
3. Add more nodes and constraints.
4. Only then scale up.

This avoids debugging “everything at once”.

---

## Summary

- **Visitor relations** constrain *who* visits nodes and whether nodes share a route:
  - forced same route
  - forced same visitor
  - forced different routes (optionally with forced different visitors)

- **Tempus relations** constrain *when* one node is served relative to another:
  - relative time windows based on master/related timestamps

- Relations are attached using a consistent master/related pattern and should be added to **both** nodes.

- For real-world workflows, combining visitor + tempus relations is often the cleanest architectural way to encode multi-step processes in routes.
