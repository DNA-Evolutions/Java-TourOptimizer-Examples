# Magnetic Condition - Node-Node Soft Condition

This example demonstrates **MagnetoNodeConstraint** (“magnetic” node constraint) in **soft** mode.  
A magnetic constraint attaches to a single node and influences the optimizer via **preferences**:

- **Attraction:** keep selected target nodes on the **same route** as the constrained node.
- **Repulsion:** keep selected target nodes on a **different route** than the constrained node.

> Important: `MagnetoNodeConstraint` is **soft-only**. Setting `isHard=true` is not allowed.

- **Source (GitHub):** [/condition/magnetic/MagneticSoftConditionExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/condition/magnetic/MagneticSoftConditionExample.java)

---

## Typical business scenarios

Magnetic constraints are useful when you want *coordination preferences* without making the plan infeasible, for example:

- “These visits should be handled by the **same person/vehicle** if possible.”
- “These visits should be **separated** if possible (load risk, customer preference, operational separation).”
- “If we keep them together, this one should be **early** in the route” (ordering preference).

Because it is a *soft* constraint, the optimizer can still find a schedule when reality conflicts with the preference.

---

## What the example models

The example uses time-window geo nodes (German cities) and adds two magnets:

### 1) Attraction magnet on **Stuttgart**
- Stuttgart is configured as an **attracting magnet**
- It adds `"Aachen"` as a magnet target
- It also sets an ordering preference: **`NodeOrder.FRONT`**  
  (Stuttgart should be at the beginning of the stack of attracted nodes)

Expected effect:
- Prefer solutions where **Stuttgart and Aachen are on the same route**,
- and Stuttgart appears **early** on that route.

### 2) Repulsion magnet on **Koeln**
- Koeln is configured as a **repulsive magnet**
- It adds `"Wuppertal"` and `"Essen"` as magnet targets

Expected effect:
- Prefer solutions where **Koeln is not on the same route** as Wuppertal and Essen.

---

## Key configuration concepts

### Attraction vs. repulsion
- `setIsAttractingMagnet(true)` → attraction (same-route preference)
- `setIsAttractingMagnet(false)` → repulsion (different-route preference)

### Magnet targets by ID
Targets are added via:
- `addNodeMagnetId("...")`

In this example, the IDs match the node identifiers (city names). In production systems, you typically use stable business IDs.

### Optional ordering preference
For attraction magnets you can optionally guide where the constrained node should appear in the route reltive to its attracted nodes:
- `NodeOrder.FRONT`
- `NodeOrder.BACK`
- `NodeOrder.NO_ORDER`

---

## How to run

Run the `main()` method of:

- `MagneticSoftConditionExample.java`

The example starts an asynchronous optimization run and prints status/progress events to the console.

---

## What to look for in the result

1. **Route membership**
   - Check whether **Stuttgart + Aachen** end up on the same route (preferred).
   - Check whether **Koeln** is separated from **Wuppertal/Essen** (preferred).

2. **Ordering**
   - If Stuttgart shares a route with Aachen, verify Stuttgart is placed first (front preference).

3. **Trade-offs**
   - If constraints (time windows, travel times, resource limits), the optimizer may choose to violate them. This is expected for *soft* constraints.

---

## Notes for production use

- Magnetic constraints are great for expressing “*prefer together* / *prefer apart*” without making plans brittle.
- Use stable IDs for magnets (e.g., order IDs, customer IDs, task IDs), not human-readable labels.
- Combine magnets with other constraints (skills, territories, time windows) to capture real operational policies.

---

## Related topics

- Soft vs. hard constraints: hard constraints are fulfilled by architecture; soft constraints are optimized via cost trade-offs.
- Relations (same route / different route) can model hard linkage; magnets are the soft, preference-oriented counterpart.