# IncludeVisitDuration — “Arrive Within OpeningHours” vs. “Finish Within OpeningHours”

In most routing problems with time windows, there is a subtle but operationally critical distinction:

1. **Strict time windows (default behavior)**  
   A resource must **arrive and complete** the service within the node’s OpeningHours.

2. **Access windows (this feature)**  
   A resource must **arrive within** the OpeningHours, but the service may **continue after** the window ends.

The `IncludeVisitDurationExample` demonstrates how to switch a node from strict time-window semantics to “access window” semantics.

---

## References

- Example source: [IncludeVisitDurationExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/includevisitduration/IncludeVisitDurationExample.java)

---

## The key API: `setIsDutyHoursIncludesVisitDuration(...)`

The example uses:

- `koeln.setIsDutyHoursIncludesVisitDuration(false);`

### What it means

- **`true` (default)**  
  The node’s duty hours include the visit duration.  
  Operationally: the entire service must be performed inside the OpeningHours.

- **`false` (access window)**  
  The duty hours exclude the visit duration.  
  Operationally: the resource must **arrive during** OpeningHours, but the visit/service may finish afterwards.

In the example, only the node **Koeln** is configured as an access window, while all other nodes remain in the default strict mode.

---

## Why this matters in real operations

### Typical use cases for access windows
- **Site access restrictions**  
  You may only enter a facility during a guard shift, but once inside, you can continue working.
- **Key handover / check-in windows**  
  A concierge is available only during a certain window.
- **Start-of-service SLA**  
  “Technician must be on-site by 16:00” but the repair can continue after 16:00.
- **Noise or access regulations**  
  Certain actions require arrival before a cutoff, but completion can happen later.

### Typical use cases for strict windows (default)
- **Customer appointment windows**  
  The customer is only present during a time window; the job must be finished within it.
- **Dock slot scheduling**  
  The entire loading/unloading process must occur within the allocated slot.
- **Pickup windows for perishable goods**  
  Both arrival and completion must be within the window.

---

## How the example is constructed

### Time model
- OpeningHours for nodes: May 6–7, 2020, 08:00–17:00 (Europe/Berlin)
- WorkingHours for resources: May 6–7, 2020, 08:00–17:00 (Europe/Berlin)
- Visit duration: 20 minutes per node

### Nodes
The example creates several `TimeWindowGeoNode` instances (cities).  
For **Koeln**, it flips the semantic switch:

- Koeln is “arrive in window, finish after allowed”.

All other nodes remain strict by default.

### Resources
Two `CapacityResource` instances are created (Jack and John), both starting in Aachen, with:
- max distance: 1200 km
- max working time: 12 hours
- cost configuration: `setCost(0, 1, 1)`

This keeps the focus on the time-window semantics rather than resource differences.

### Optimization settings
The example uses a relatively large iteration budget:
- `JOptExitCondition.JOptGenerationCount = 20000`
- `JOpt.Algorithm.PreOptimization.SA.NumIterations = 1000000`
- `JOpt.Algorithm.PreOptimization.SA.NumRepetions = 1`

This ensures the solver has enough opportunity to exploit the relaxed constraint on Koeln.

---

## How to reason about feasibility with this setting

### Strict window feasibility (default)
A node is feasible only if:

- `arrivalTime >= windowStart`
- and `arrivalTime + visitDuration <= windowEnd`

### Access window feasibility (`setIsDutyHoursIncludesVisitDuration(false)`)
A node is feasible if:

- `arrivalTime >= windowStart`
- and `arrivalTime <= windowEnd`

Service completion time may exceed the window end.  
However, it still contributes to:
- resource working hours,
- downstream arrival times,
- and route feasibility.

Meaning:
- You are not “removing” the visit duration from time; you are only changing how it is validated against the node’s OpeningHours.

---

## Practical implications and best practices

### 1) You still need sufficient WorkingHours
Allowing a service to end after OpeningHours does not mean it can extend beyond WorkingHours.  
If WorkingHours are strict, the schedule must still finish inside working time limits.

### 2) Report semantics clearly to users
In UIs and reports, clarify whether:
- the “appointment window” is an arrival window, or
- a full-service window.

This prevents misunderstandings such as:
- “Why did the technician finish after the customer’s window?”

### 3) Do not apply globally by accident
This setting is per-node and should be used selectively:
- apply it only to nodes that truly represent “access windows”.

### 4) Be careful with multi-window nodes
If nodes have multiple OpeningHours windows, ensure your business interpretation is consistent:
- “arrive in any allowed window” vs “arrive in the last window”.

### 5) Combine with service-time modeling explicitly
If your service process has phases (e.g., check-in must happen inside the window, work can continue outside), you may:
- model check-in as a short access-window node,
- and model the main work as a subsequent event node or linked structure,
depending on your broader modeling approach.

---

## How to run and validate

1. Run `main(...)` in `IncludeVisitDurationExample`.
2. Inspect the produced KML (the example uses `EntityKMLExporter`) to visually verify ordering.
3. Print and review the result to confirm:
   - Koeln arrival occurs within the window,
   - Koeln completion may occur after the window end if the route timing dictates it.

---

## Summary

- Default time-window behavior requires **arrival and completion** within OpeningHours.
- `setIsDutyHoursIncludesVisitDuration(false)` changes a node to an **arrival-only** constraint (access window).
- This is essential for real cases where “access must start in the window” but work may finish afterwards.
- The example demonstrates the setting on Koeln while leaving other nodes in strict mode, making the behavioral difference easy to observe.
