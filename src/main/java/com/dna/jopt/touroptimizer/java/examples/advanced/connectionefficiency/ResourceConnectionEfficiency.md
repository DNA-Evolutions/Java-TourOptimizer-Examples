# Resource Connection Efficiency — Modeling “Fast” and “Slow” Vehicles via Travel-Time Scaling

This document explains how to model **resource-specific travel-time behavior** using the *connection time efficiency factor*.

The key idea is simple and very effective:
- different resources (vehicles/technicians) can experience **different effective travel times** for the same geographical movement, while still using the same connection model (fallback connector or an external matrix).

This is especially useful when:
- one vehicle is significantly faster (highway capable, emergency privileges, optimized routing device),
- one vehicle is slower (heavy truck, restricted speed zones),
- one resource is a bike/walker in dense cities,
- a subset of vehicles has access to restricted roads/lanes and therefore travels faster in practice.

---

## References

- Example source (GitHub):  
  [ResourceConnectionEfficiencyExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/connectionefficiency/ResourceConnectionEfficiencyExample.java)


---

## What is “connection time efficiency”?

Every time the optimizer evaluates a move, it needs **travel time** between two locations (nodes, or resource start/end to nodes).

JOpt allows you to scale those travel times per resource via:

- `resource.setConnectionTimeEfficiencyFactor(factor)`

### Default behavior
- Default factor: `1.0`

Meaning:
- travel times are used “as-is” from the connection model.

### Factor semantics
- `factor < 1.0` → **faster** than baseline (needs less time to traverse a connection)  
- `factor > 1.0` → **slower** than baseline (needs more time)

In the provided example the factor is deliberately exaggerated to make the effect easy to observe.

---

## What the example does (`ResourceConnectionEfficiencyExample`)

The scenario defines:
- a small set of geo nodes (cities) with the same opening hours,
- two resources starting in Aachen,
- and then makes one resource “much faster” by scaling its travel time.

### Resources
The example creates two `CapacityResource` instances:

1) **Jack**
- max working time: **8 hours**
- max distance: **1200 km**
- same start position as John
- **connection time efficiency factor set to `0.2`**
  - meaning Jack needs only **20% of the baseline travel time** (very fast car)

2) **John**
- max working time: **14 hours**
- max distance: **1200 km**
- **default connection efficiency factor `1.0`** (normal travel speed)

The cost configuration is identical for both:

- `setCost(fixCost=0, perHourCost=1, perKilometerCost=1)`

So any behavioral difference is primarily caused by:
- different working-time limits, and
- Jack’s scaled travel time.

### Nodes
The example uses `TimeWindowGeoNode` instances, including:
- Koeln, Oberhausen, Essen, Heilbronn, Stuttgart, Wuppertal, Aachen

All nodes share the same opening hours:
- March 6–7, 2020, 08:00–17:00 (Europe/Berlin)
- visit duration: 20 minutes

### Run execution
The example runs asynchronously and blocks:
- `startRunAsync()`
- `future.get()`

This ensures the JVM remains alive until the optimization completes.

---

## How to interpret the result

### What should change when you scale connection time
Connection time efficiency directly impacts:
- arrival times,
- feasibility against node opening hours (time windows),
- feasibility against resource working hours,
- route ordering decisions (because timing feasibility changes),
- and time-based objective components (if enabled in your model).

### What usually does *not* change directly
In most models, scaling connection **time** does not inherently scale connection **distance**.

So, depending on the rest of your configuration:
- distance-related feasibility limits (maxDistance) may remain the binding factor,
- but time-related feasibility limits (WorkingHours, OpeningHours) may become much easier or harder.

### Why this example is pedagogically useful
The example makes the difference obvious:
- Jack has **less working time** (8h) but a **faster travel model** (0.2).
- John has **more working time** (14h) but normal travel times.

This creates a realistic “dispatch trade-off”:
- a fast resource can compensate for a shorter shift,
- or a slower resource may need a longer shift to cover the same workload.

---

## Where this feature belongs in real projects

### Use cases
1. **Vehicle classes**
   - vans vs trucks vs bikes
2. **Traffic privileges**
   - emergency / priority access routes
3. **Road restrictions or access zones**
   - vehicles restricted from certain streets (often modeled by external connections) plus different speed assumptions
4. **Operational differences**
   - local knowledge, better navigation tooling, “faster technician” due to experience (be careful with this interpretation)

### Recommended practice
- Use connection efficiency to model *systemic* differences (vehicle capabilities), not random noise.
- Keep the factor values explainable (e.g., 0.8, 1.0, 1.2) unless you are explicitly demonstrating the feature.

---

## Interaction with external connection data

Connection time efficiency is a *resource-level* modifier applied on top of travel time inputs.

This means it can be used with:
- fallback connectors (built-in distance/time approximations),
- or external connection matrices (your own distance/time data),
- or hybrid setups (partial external connections + fallback connector).

Practical pattern:
- Keep your connection data “baseline accurate” for an average vehicle.
- Use efficiency factors to reflect per-vehicle differences without maintaining multiple matrices.

---

## Calibration guidance (how to choose factors)

A reliable approach is to calibrate from observed speed ratios.

Example:
- baseline connector corresponds to ~50 km/h average,
- vehicle A behaves like ~60 km/h average,
- vehicle B behaves like ~40 km/h average.

Then:
- A factor ≈ 50/60 ≈ 0.83
- B factor ≈ 50/40 = 1.25

This gives you consistent and explainable scaling.

---

## Pitfalls and how to avoid them

### Pitfall 1 — Over-exaggerated factors create unrealistic feasibility
If you set a factor too small, you may unintentionally “solve” feasibility problems that are actually operational impossibilities.

### Pitfall 2 — Distance constraints still apply
If maxDistance is tight, reducing time alone may not allow additional work.

### Pitfall 3 — Reporting and KPI interpretation
Ensure your reporting clearly distinguishes:
- distance,
- transit time (possibly scaled),
- productive time,
- idle time,
- and whether the travel-time model differs by resource.

---

## Summary

- `setConnectionTimeEfficiencyFactor(...)` lets you model **resource-specific travel speed** by scaling travel times per resource.
- Default factor is `1.0`.
- Smaller values mean faster effective travel; larger values mean slower travel.
- This changes timing feasibility (WorkingHours / OpeningHours) and can materially alter route assignment decisions.
- The example contrasts:
  - a short-shift, very fast resource (Jack, factor 0.2),
  - with a long-shift, baseline-speed resource (John, factor 1.0),
  demonstrating how connection efficiency impacts dispatch feasibility and allocation.
