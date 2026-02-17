# Open vs. Closed Route — Returning to Depot or Ending “Where It Makes Sense”

In routing, the term **closed route** typically means:

- the resource starts at its start location (depot),
- visits a sequence of nodes,
- and then **returns to the start location**.

An **open route** means:

- the resource starts at its start location,
- visits nodes,
- but **does not have to return to the start location**.

JOpt supports both behaviors. The choice changes:
- feasibility against WorkingHours,
- route timing and “end of day” arrival,
- total travel distance and travel time,
- and the realism of models where vehicles end at a different location (e.g., last job near home base or near a new shift start).

---

## References

- Example source: [OpenClosedRouteExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/openclosedroute/OpenClosedRouteExample.java)

---

## Where the setting lives: WorkingHours

Open/closed routing is configured **per WorkingHours interval**, not globally.

In the example, the key line is:

- `w.setIsClosedRoute(false);`

### Interpretation
- `isClosedRoute = true` (default)  
  The route is **closed** for that WorkingHours interval: the resource is modeled as returning to the start location.

- `isClosedRoute = false`  
  The route is **open** for that WorkingHours interval: the resource does **not** need to return to the start location.

This gives you fine control:
- You can mix open and closed days for the same resource (depending on your business model).

---

## What the example demonstrates

The example creates:

- 1 resource (“Jack”) starting in Aachen.
- Several time-window geo nodes (cities) including:
  - Koeln, Oberhausen, Essen, Heilbronn, Stuttgart, Wuppertal, Aachen.
- Matching OpeningHours and WorkingHours:
  - May 6–7, 2020, 08:00–17:00 (Europe/Berlin).
- Visit duration:
  - 20 minutes per node.

Then it explicitly makes all WorkingHours open-route:

```java
// All WorkingHours should result in a open Route
// Per default WorkingHours are closed
workingHours.stream().forEach(w -> w.setIsClosedRoute(false));
```

Finally, it runs the optimization and exports a KML:

- `OpenClosedRouteExample.kml`

---

## How to interpret results and exports

### Closed route behavior (default)
If you keep `isClosedRoute = true`:
- the route must contain an implied return leg back to the start location,
- which increases:
  - total distance,
  - total travel time,
  - and the risk of WorkingHours infeasibility.

This is the correct model when:
- vehicles must return to depot,
- end-of-day parking is at the start location,
- unloading/closing procedures require depot return.

### Open route behavior
With `isClosedRoute = false`:
- the route ends at the last visited node,
- there is no mandatory return-to-start leg.

This is the correct model when:
- vehicles do not return (e.g., cross-docking, multi-shift operations),
- “end of day” is wherever the last job finishes,
- drivers can finish near home or at a different base.

### KML inspection
When you inspect the exported KML:
- a closed route typically shows a final segment returning to the start.
- an open route ends at the last node without the return leg.

Because the example forces open routes, you should see the latter.

---

## Mixing open and closed behavior across days (advanced, recommended)

A major advantage of storing this on WorkingHours is that you can model scenarios like:

- **Day 1**: closed route (must return to depot)
- **Day 2**: open route (ends where it ends)
- **Weekend**: different behavior (e.g., must return to depot due to parking constraints)

Implementation pattern:
- build WorkingHours blocks,
- set `setIsClosedRoute(true/false)` per block.

This yields realistic multi-day models without duplicating resources or building special-case routing logic.

---

## Operational guidance

### When open routes are usually correct
- field service technicians who end near the last customer,
- courier services with flexible end-of-day parking,
- multi-depot or “handover” operations where the next shift starts near the last stop.

### When closed routes are usually correct
- delivery fleets that must return for reloading, cleaning, charging,
- regulated fleets that must return to controlled yards,
- scenarios where shift end includes depot operations.

### Be explicit in downstream processes
If your execution system assumes:
- vehicles always return to depot,

then open-route planning must be reconciled, for example:
- by adding an explicit “end depot” node,
- or by choosing closed-route behavior.

---

## Pitfalls and how to avoid them

### Pitfall 1 — Assuming open/closed is global
It is not global; it is per WorkingHours interval.  
If you add new WorkingHours blocks later, ensure you configure them consistently.

### Pitfall 2 — Mixing open routes with strict depot KPIs
If your KPI dashboards assume depot return time, update the KPIs:
- “route end time” becomes last-node completion time,
- “return-to-depot” becomes optional or modeled explicitly.

### Pitfall 3 — Unexpected long routes in open mode
Open routes can make long “one-way” travel appear cheaper, because the return leg disappears.
If this is undesirable, mitigate via:
- max distance constraints,
- working time constraints,
- or explicit “end location” requirements.

---

## Summary

- **Closed route** (default): route includes a return to the start location.
- **Open route**: no return-to-start is required; route ends at the last node.
- The setting is configured on **WorkingHours** via `setIsClosedRoute(false)`.
- The example demonstrates open routing by setting all WorkingHours to open-route mode and exporting a KML for inspection.

