# CO2-Emission Optimization — From Reporting to an Explicit Optimization Goal

JOpt TourOptimizer includes a built-in **CO2 emission assessment** and an optional **CO2-aware optimization goal**.  
This allows fleet operators to:
- calculate **per-route** and **overall** emissions,
- differentiate vehicles by their emission characteristics (diesel vs petrol, modern vs legacy, payload ratio effects),
- and (optionally) steer the optimizer towards schedules with **lower overall CO2 emission**, while still satisfying operational constraints such as time windows, capacities, and working times.

This document is based on:
- the official CO2 feature documentation, and
- the Java example `CO2EmissionOptimizationExample.java`.

---

## References

- CO2 feature overview (official docs):  
  https://docs.dna-evolutions.com/overview_docs/co2_emission/CO2_emission.html

- Example source (GitHub):  
  https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/co2emission/CO2EmissionOptimizationExample.java

- Example source (raw):  
  https://raw.githubusercontent.com/DNA-Evolutions/Java-TourOptimizer-Examples/refs/heads/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/co2emission/CO2EmissionOptimizationExample.java

---

## What the CO2 feature does in JOpt

### 1) Emission assessment (always available)
JOpt can compute CO2 emission:
- per route,
- and for the overall job.

By default, this is a **reporting feature**: emissions are calculated and reported, but **they do not influence the optimization result** unless you explicitly activate a CO2 objective weight.

### 2) CO2 as an optimization goal (optional)
If you want the optimizer to prefer low-emission solutions, you must set a weight:

- `JOptWeight.CO2Emission` > 0

This adds CO2 emission as a goal in the cost function and the solver will trade it off against other objectives (distance, timing, resource costs, etc.), respecting feasibility constraints.

---

## Default emission factor and unit interpretation

The documentation states that, by default, a vehicle has a CO2 emission factor of:

- `0.377 [kg*CO2/km]`

This corresponds approximately to an average fuel consumption of 12 liters diesel per 100 km.

Practical example from the documentation:
- 500 km → 188.5 kg CO2

---

## How to set emission factors per vehicle/resource

The emission factor is vehicle-specific and depends on:
- vehicle type,
- fuel type,
- consumption,
- payload effects and usage patterns.

In JOpt, you set it per resource (vehicle) via:

- `resource.setAverageCO2EmissionFactor(...)`

### Example calculation (as used in the docs and reflected in the Java example style)

1) Convert fuel consumption to **liters per km**:
- 25 L / 100 km → `25.0 / 100 = 0.25 L/km`
- 8 L / 100 km → `8.0 / 100 = 0.08 L/km`

2) Multiply by a CO2 factor (kg per liter):
- Diesel: `2.629 kg/l`
- Petrol: `2.362 kg/l`

3) Resulting emission factors:
- Diesel vehicle: `0.25 * 2.629 = 0.65725 kgCO2/km`
- Petrol vehicle: `0.08 * 2.362 = 0.18896 kgCO2/km`

These are exactly the kinds of computations shown in the CO2 documentation.

---

## The advanced example: `CO2EmissionOptimizationExample`

### What it demonstrates (high-level story)
The example creates a small tour optimization problem and defines **two vehicles**:

- **Vehicle One**: higher CO2 emission factor (diesel, high consumption)
- **Vehicle Two**: lower CO2 emission factor (petrol, low consumption) but with a higher **fixed cost**

The intent is to demonstrate a realistic decision:
- a “greener” vehicle might be operationally more expensive (or scarce),
- so whether it is used depends on:
  - the CO2 objective weight, and
  - the cost structure (fixed cost vs distance/time costs).

### Properties: enabling CO2 as an optimization goal
In `addProperties(...)`, the example enables CO2 optimization explicitly:

- `props.setProperty("JOptWeight.CO2Emission", "10.0");`

Important: the comment in the code clarifies that this must be **higher than zero** (the default).

The example also sets general run controls:
- `JOptExitCondition.JOptGenerationCount = 2000`
- `JOpt.Algorithm.PreOptimization.SA.NumIterations = 10000`
- `JOpt.Algorithm.PreOptimization.SA.NumRepetions = 1`

### Nodes and time windows
Nodes are defined as `TimeWindowGeoNode` with:
- OpeningHours on May 6 and May 7, 2020 (08:00–17:00, Europe/Berlin),
- a visit duration of 20 minutes,
- importance = 1.

Cities used:
- Koeln, Essen, Dueren, Nuernberg, Heilbronn, Wuppertal, Aachen

### Resources (vehicles) and CO2 setup
Both vehicles start at the same coordinates (Aachen) and share:
- maxWorkingTime = 12 hours
- maxDistance = 1200 km
- WorkingHours on May 6 and May 7, 2020 (08:00–17:00, Europe/Berlin)

Then the example differentiates them:

#### Vehicle One
- emission factor set via:
  - `vehicleOne.setAverageCO2EmissionFactor(fuelConsumptionVehicleOne * co2FactorDiesel);`

#### Vehicle Two
- additional fixed cost:
  - `vehicleTwo.setFixCost(1000.0);`
- lower emission factor:
  - `vehicleTwo.setAverageCO2EmissionFactor(fuelConsumptionVehicleTwo * co2FactorPetrol);`

### Expected outcome (what the example explains)
The code comments explain the intended result pattern:

- Vehicle One is used for a shorter trip.
- Vehicle Two is used for a longer trip.
- If you decrease Vehicle Two’s fixed cost, **or** increase the CO2 objective weight, the optimizer will increasingly prefer Vehicle Two—potentially using it exclusively.

This is the key “business narrative”:
- CO2 optimization is not a binary switch.
- It is a goal that competes with other goals (especially cost).

---

## How to interpret results

The CO2 value is part of the **route result header** (per the documentation).  
Therefore, when you print the result (`System.out.println(result)`), you should expect to see emissions reflected in the summary information.

Recommended practice:
- In production systems, do not rely only on `toString()` output.
- Extract structured route headers and store:
  - total distance,
  - total time,
  - total CO2,
  - and per-route equivalents.
This allows you to build dashboards and to run “what-if” comparisons.

---

## Operational guidance: using CO2 optimization correctly

### 1) Start by using CO2 as reporting only
Before steering the solver, you should ensure:
- emission factors are correct,
- units are correct,
- the reported numbers are plausible.

This builds trust and prevents “garbage in → optimized garbage out”.

### 2) Introduce `JOptWeight.CO2Emission` gradually
CO2 optimization is an additional objective. Use staged rollout:

1. Set weight to a small value (e.g., 1).
2. Validate that routes still meet SLA constraints and business cost expectations.
3. Increase weight while monitoring:
   - how much CO2 improves,
   - how much cost/time/distance changes.

### 3) Be explicit about fixed and variable costs
CO2-aware routing can select a low-emission vehicle even if it increases distance slightly, depending on weights.

If you want realistic decisions, model both:
- **fixed costs** (vehicle activation cost, driver cost, rental cost),
- and **variable costs** (distance/time cost, toll cost, etc.).

The example demonstrates exactly this by adding a fixed cost to the lower-emission vehicle.

### 4) Combine with accurate distance/time (optional but recommended)
If your distances are approximated (fallback connector), CO2 computations may be directionally correct but not exact.  
For operational-grade reporting and optimization, consider:
- external road-network distances,
- travel-time matrices,
- or domain-specific distance connectors.

---

## How to run the example

Run `main(String[] args)` in `CO2EmissionOptimizationExample`.

The flow is clean and reusable:

1. add properties (including CO2 weight)
2. add nodes
3. add resources (including per-vehicle CO2 factor)
4. attach to observables (progress/status/warnings/errors)
5. start asynchronously and block with `future.get()`
6. print result

---

## Summary

- JOpt includes CO2 emission assessment out of the box; by default it is a reporting feature.
- You can make CO2 part of the optimization goal by setting `JOptWeight.CO2Emission` > 0.
- Emission factors are configured per resource using `setAverageCO2EmissionFactor(...)`.
- The advanced example demonstrates a realistic trade-off: a “greener” vehicle can have a higher fixed cost, and the optimizer chooses based on objective weights.
- Best practice is to validate reporting first, then increase CO2 weight gradually with clear monitoring of business KPIs.
