# Pillar Nodes (Captured Nodes) — Hard Constraints Fulfilled by Architecture

A **CapturedNode** (also called a **Pillar** / **PillarNode**) models an **absolute, non-negotiable** constraint: its SLA (OpeningHours / time window) must be met.

This is a crucial point in JOpt:

> A Pillar is a **hard constraint** that is fulfilled by the optimizer’s **architecture**, not by assigning an extremely high cost/penalty.

In other words:
- **Soft constraints** are typically enforced by *cost* (penalty terms in the objective function).
- **Hard constraints** in JOpt are enforced by *design*: the algorithm is structured so that these constraints are not violated.  
  A Pillar is a prime example: normal nodes “flow around” the pillar appointment in the timeline.

---

## Clickable links

### Concept documentation
- [Special Features — CapturedNode / Pillar conflict resolution](https://www.dna-evolutions.com/docs/learn-and-explore/special/special_features#case-four-conflict-of-a-capturednode-with-a-node-solved-by-removing-a-capturednode)

### Example implementation
- [PillarExample.java (GitHub)](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic/pillar_07/PillarExample.java) 

---

## Why this is not “just a high penalty”

Many optimization engines approximate “must-not-violate” requirements by setting a huge penalty in the objective function.  
That is **not** how pillars work in JOpt.

### Pillar behavior in plain language
A pillar appointment acts like a **fixed anchor** in the route timeline:

- The optimizer treats the pillar time window as something that must be satisfied.
- The surrounding schedule is then adapted:
  - normal nodes may be reordered,
  - normal nodes may be shifted earlier/later,
  - normal nodes may be assigned to other resources,
  - or (in extreme cases) the optimizer may decide to drop a pillar rather than violate it.

This “nodes flow around the pillar” effect is a direct consequence of JOpt’s architecture for hard constraints, not cost tuning.

---

## What exactly is “hard” about a pillar?

A pillar’s **OpeningHours** are treated as hard feasibility boundaries:
- arriving outside the pillar time window is not acceptable,
- waiting before the window is allowed (as with normal time windows),
- but the visit must be placed such that the SLA is matched.

This makes pillars suitable for:
- contractual appointment windows,
- compliance deadlines,
- “must happen at this time” commitments.

---

## Conflict handling (what happens when things don’t fit)

The Special Features documentation describes how the optimizer resolves conflicts between a pillar and “normal” nodes. The practical hierarchy is:

1. **Reorder / shift normal nodes** so the pillar SLA is met.
2. **Let normal nodes violate** their constraints if your model allows it (soft constraints/cost-driven).
3. **Move work to other resources** if available, to protect the pillar.
4. **Remove a pillar** (rare / extreme) if there is no feasible schedule that keeps the pillar intact and no alternative resource exists.

That last point is a key differentiator: the system prefers cancellation/removal to violating a captured SLA.

---

## Java implementation: pillar node types

The pillar concept appears as dedicated node classes implementing `IPillarNode`, such as:

- `PillarTimeWindowGeoNode` — a geo-located pillar node (lat/long + strict opening hours)
- `PillarEventNode` — a non-geo pillar node (event-style pillar, strict opening hours)

A pillar can optionally be tied to a **mandatory resource**:

- `pillar.attachResource(resource);`

When attached:
- the node is not only time-critical,
- it is also constrained to one specific resource.

This is particularly valuable for:
- skills/certifications,
- legal requirements,
- named-person commitments.

---

## What `PillarExample` demonstrates

The example contains three pillar variants.

### 1) Geo pillar with mandatory resource
A “Plumbing” task in Cologne has a strict time window (June 5, 10:00–10:50, Europe/Berlin) and is attached to **John**.

### 2) Event pillar with mandatory resource
An “Important call” has a strict time window (June 6, 10:30–12:00) and is attached to **Jack**.

### 3) Geo pillar without mandatory resource
A “Maintenance job” in Stuttgart has a strict time window (June 4, 13:30–16:00) and 90 minutes duration.  
No resource is attached, so the optimizer may select the best-fitting resource while still treating the SLA as hard.

The example also exports the result to:

- `./PillarExample.kml`

---

## How to run

Run the `main(String[] args)` method of `PillarExample`.

Behavior highlights:
- The run is started asynchronously (`startRunAsync()`).
- The example blocks with `get()` to prevent premature termination.
- The result is printed to stdout.
- A KML file is written for visual inspection.

---

## Recommended modeling guidance

### Use pillars when violations are unacceptable
Use pillars for truly contractual requirements:
- “must be serviced within this window”
- “must not be late”
- “must not be moved outside the SLA”

### Do not use pillars to “nudge” the solver
If the time window is *preferred* but not mandatory, model it as a normal node and handle it via:
- soft constraint penalties,
- objective weighting,
- business rules.

That separation keeps your model honest:
- **architecture** for hard constraints,
- **cost function** for preferences and trade-offs.

---

## Summary

- **CapturedNode = PillarNode**: strict SLA matching for OpeningHours (hard constraint).
- Pillars are enforced by **optimizer architecture**, not by high penalty costs.
- Normal nodes “flow around” pillars; the solver rearranges the schedule to keep pillar SLAs intact.
- Pillars can optionally enforce a mandatory resource via `attachResource(...)`.
- Extreme infeasibility may result in a pillar being removed rather than violated (per Special Features docs).
