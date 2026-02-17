# Optional Nodes — Make “Stopovers” and Optional Tasks First-Class Citizens

Optional Nodes are a modeling tool that lets the optimizer **choose** whether a node should be visited or skipped.

In classic tour optimization, every node must be visited, which means every additional node inevitably increases cost.  
Optional Nodes break that limitation: they allow you to add *potential* stops that can **reduce** total cost or even be **required for feasibility** (e.g., capacity).

Reference documentation:
- https://www.dna-evolutions.com/docs/learn-and-explore/special/special_features#optional-nodes

---

## Why Optional Nodes exist (the operational story)

In real operations, there are many “nodes” that are not customer jobs, but *operational stopovers*:

- waste deposits to dump collected waste (capacity relief),
- reload bases / depots to restock,
- intermediate drop points,
- optional “factory” or “supply” points for manufacturing planning,
- contingency stops (“use if needed”),
- optional tasks that are nice-to-have but not mandatory.

You usually do not want to hardcode these stopovers into every plan.  
Instead, you want the optimizer to decide:

- Is this stop necessary to stay feasible (capacity/time windows)?
- Does this stop reduce overall cost enough to justify the detour?

Optional Nodes provide exactly this decision capability.

---

## The core semantics

### Mandatory node
- Must be visited exactly once (unless otherwise modeled).
- Always contributes to the plan.

### Optional node
- May or may not be visited.
- If it is visited, it behaves like a normal node:
  - opening hours/time windows apply,
  - travel time and travel distance apply,
  - visit duration applies,
  - and all other constraints apply.

Key implication:
- Optional Nodes do **not** bypass constraint logic.  
  They only change the *decision* “visit or skip”.

---

## When the optimizer will choose to visit an optional node

The optimizer tends to visit an optional node when one of these applies:

1. **Feasibility enabler**  
   Visiting the optional node is necessary to satisfy hard feasibility (e.g., capacity constraints), otherwise the route would become infeasible.

2. **Cost reducer**  
   Visiting the optional node decreases the overall objective cost more than the detour increases it.  
   Classic example: unloading reduces load-related cost (fuel, risk, overload penalties), so the optimizer inserts a dump/reload stop.

3. **Preference / policy shaping**  
   If your cost model or open assessor introduces penalties for certain states (e.g., driving with high load, overtime, lateness), an optional node can become the economically best “escape hatch.”

Optional nodes are therefore not a trick—this is a structural modeling feature that creates additional degrees of freedom in the solution space.

---

## Example sources in this package

These two examples demonstrate Optional Nodes as *reload bases / unload points*.

- Reload Base (basic optional unloading concept):
  - [ReloadBaseExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/optionalnode/ReloadBaseExample.java)

- Reload Base + `unloadAll()` (mix of partial unload + full unload):
  - [ReloadBaseWithUnloadAllExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/optionalnode/ReloadBaseWithUnloadAllExample.java)

Both examples are intentionally “didactic”:
- they show the mechanism clearly,
- and demonstrate that optional nodes can be placed in locations that are clearly beneficial vs clearly not beneficial (to visualize the decision).

---

## Implementation pattern

### 1) Create your normal nodes (jobs)
Use any node type you typically use (e.g., `TimeWindowGeoNode`).

### 2) Create the optional node
Create it like any other node and then mark it as optional:

```java
node.setIsOptional(true);
```

### 3) Give the optional node its operational meaning
In capacity scenarios, this is typically done by assigning a load/unload vector:

```java
node.setLoad(...);
```

Important:
- The sign convention of the load vector depends on your scenario/modeling choices.
- In the provided examples you will see both “pickup-like” and “unload-like” behavior represented through positive/negative values.
- The relevant point is: the optional node is configured so that visiting it changes the resource’s load state in a way that can improve feasibility/cost.

### 4) (Optional) Enable “unload all” semantics
If you want a node to empty the resource’s current load (or drop as much as possible), enable:

```java
node.setUnloadAll(true);
```

This turns a node from “drop a fixed amount” into “drop everything / drop maximum possible”.

This is especially useful for:
- waste management (empty truck),
- depot return (reset load),
- or end-of-route normalization (avoid returning with cargo).

---

## What the examples demonstrate

### Example A — ReloadBaseExample: capacity relief with optional unload nodes

High-level idea:
- A resource has a limited capacity (e.g., 20 units).
- Mandatory nodes represent pickups/loads.
- Optional nodes represent unload/reload points.
- The optimizer may insert an unload stop when the route would otherwise exceed capacity.

The example also includes an optional node that is *far away* to demonstrate a key property:
- Optional nodes are not visited “because they exist”.
- They are visited only if they are beneficial (feasibility or total cost).

This is a powerful validation pattern for your own models:
- place a clearly-bad optional node in an unreasonable location,
- confirm it is not selected,
- thereby validating that the model is not biased toward “always visit optional nodes”.

### Example B — ReloadBaseWithUnloadAllExample: partial unload + full unload

This example extends the idea:
- multiple optional nodes allow unloading a fixed amount,
- and **one special optional node** has `unloadAll` enabled.

This models a common real-world situation:
- small drop points reduce load incrementally,
- but a depot or transfer station can fully reset load, which may be ideal before continuing to pick up more goods.

This is also a strong pattern for mixed pickup/delivery planning:
- optional stopovers provide controllable “state reset points” without forcing a fixed schedule structure.

---

## Modeling guidance (what works well in production)

### 1) Optional nodes are an architectural alternative to brittle cost tuning
If you need “sometimes a dump, sometimes not”, optional nodes are a structural tool.
Avoid simulating this behavior purely via:
- extremely large penalty costs,
- or ad-hoc rules outside the optimizer.

Optional nodes create a clean and explainable decision:
- “the optimizer visited the dump because it reduced total cost / enabled feasibility”.

### 2) Use optional nodes when state matters
Optional nodes are most valuable when visiting them changes some route state, such as:
- capacity/load,
- inventory,
- temperature chain constraints,
- or any other modeled resource state.

This is why they appear frequently in PND-related workflows.

### 3) Keep the optional node realistic
Even though the node is optional, it should still reflect reality:
- opening hours,
- visit duration,
- and correct geo location (if applicable).

If the optional node is visited, it must be operationally feasible.

### 4) Consider “useless visits” prevention
Some setups benefit from discouraging pointless unloadAll visits.  
The global properties list includes a dedicated weight for this concept:

- https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties (search for `JOptWeight.UselessUnloadAllVisitation`)

This is relevant when you enable unloadAll and want to ensure it is used only when it provides real value.

---

## Related topics

- Special Features overview: https://www.dna-evolutions.com/docs/learn-and-explore/special/special_features
- Pickup and Delivery module (optional nodes are often used in PND and manufacturing planning): https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/pickup_and_delivery

---

## Summary

- Optional Nodes allow the optimizer to decide whether to schedule a node or skip it.
- They are ideal for modeling reload bases, dump sites, restocking points, and optional tasks.
- If visited, optional nodes behave like normal nodes (time windows, travel, duration, constraints).
- `setUnloadAll(true)` upgrades an optional node from “drop a fixed amount” to “drop everything / drop maximum possible”.
- The provided examples demonstrate both:
  - optional nodes as capacity relief points,
  - and why clearly non-beneficial optional nodes are naturally skipped.
