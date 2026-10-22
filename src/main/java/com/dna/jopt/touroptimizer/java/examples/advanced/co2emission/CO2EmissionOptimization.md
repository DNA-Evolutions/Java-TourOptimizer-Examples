# CO2 emission as an optimization goal

`CO2EmissionOptimizationExample` plans seven visits with two vehicles from Aachen: Vehicle One emits more CO2 per kilometer, Vehicle Two less, but Vehicle Two costs a fix cost for each route it drives. With the weight `JOptWeight.CO2Emission`, the emission becomes part of the cost, and the run gives the long route to Vehicle Two and the short one to Vehicle One. The page is for developers who plan fleets with different vehicles. The [CO2 emission feature guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/co2_emission) explains the feature itself, and the [First optimization example](../../basic/firstoptimization_01/FirstOptimizationExample.md) the run pattern that this example follows.

## What the example shows

JOpt.TourOptimizer includes a built-in **CO2 emission assessment** and an optional **CO2-aware optimization goal**.  
This allows fleet operators to:
- calculate **per-route** and **overall** emissions,
- differentiate vehicles by their emission characteristics (diesel vs petrol, modern vs legacy, payload ratio effects), each expressed in one average emission factor per vehicle,
- and (optionally) steer the optimizer towards schedules with **lower overall CO2 emission**, while operational constraints such as time windows, capacities, and working times still count in the cost (in this example, the route of Vehicle Two goes over its maximal working time; see "Output and what to look for").

This document is based on:
* the official CO2 feature documentation, and
* the Java example `CO2EmissionOptimizationExample.java`.

- `setAverageCO2EmissionFactor(...)` sets the emission factor of a vehicle in kilograms of CO2 per kilometer. Each route of the result reports its emission in the line `Route Emission [kgCO2]`.
- The property `JOptWeight.CO2Emission` makes the emission part of the cost. Without it, the emission is only reported.
- `setFixCost(...)` gives Vehicle Two a cost for each route it drives. The fix cost competes with the CO2 cost, and the result shows which vehicle drives which route.

## Scenario

Two vehicles start at the same coordinates (Aachen) and visit seven nodes, one in each of the cities used: Koeln, Essen, Dueren, Nuernberg, Heilbronn, Wuppertal and Aachen. The nodes are open (`OpeningHours`) and both vehicles work (`WorkingHours`) from 08:00 to 17:00 on May 6 and May 7, 2020 (Europe/Berlin); the dates are sample values. Each visit takes 20 minutes, a visit duration that all nodes share; the nodes are defined as `TimeWindowGeoNode` with importance = 1. Each vehicle may work 12 hours (`maxWorkingTime`) and drive 1200 km per day (`maxDistanceKmW`).

The vehicles differ in their emission factor and their fix cost. The fuel consumptions and the CO2 values per litre are sample values:

| Vehicle | Fuel | Consumption | CO2 per litre | Emission factor | Fix cost per route |
|---|---|---|---|---|---|
| Vehicle One | diesel | 25 l per 100 km | 2.629 kg | 0.657 kg per km | not set |
| Vehicle Two | petrol | 8 l per 100 km | 2.362 kg | 0.189 kg per km | 2000 |

## How the code works

The source is [CO2EmissionOptimizationExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/co2emission/CO2EmissionOptimizationExample.java). `example()` sets the license and calls `addProperties`, `addNodes`, `addResources`, `attachToObservables` and `startAndPresentResult`, as the First optimization example does. The flow is clean and reusable.

### The CO2 weight

In `addProperties(...)`, the example enables CO2 optimization explicitly; the class does not set `JOpt.Algorithm.PreOptimization.SA.NumRepetitions`. It sets the weight next to the two values that keep the run short, the general run controls that the example also sets:

```java
// Fewer generations and iterations than the defaults keep the run short.
props.setProperty("JOptExitCondition.JOptGenerationCount", "1000");
props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumIterations", "100000");
// With a weight above zero the CO2 emission becomes part of the cost; without it the emission
// is only reported. With 10.0, the CO2 saving of Vehicle Two exceeds its fix cost only on the
// long route.
props.setProperty("JOptWeight.CO2Emission", "10.0");
```

Important: the comment in the code clarifies that this weight must be **higher than zero** for the emission to count in the cost. The [property reference](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties) lists the defaults of the three properties.

### The two vehicles

Then the example differentiates them: `addResources` computes each emission factor from the fuel consumption in litres per kilometer and the CO2 per litre. Vehicle One, with the emission factor set via `setAverageCO2EmissionFactor`:

```java
// 0.25 l/km x 2.629 kg/l: 0.657 kg CO2 per km.
vehicleOne.setAverageCO2EmissionFactor(fuelConsumptionVehicleOne * co2FactorDiesel);
opti.addElement(vehicleOne);
```

Vehicle Two, with a lower emission factor and the fix cost, an additional cost that is fixed for each route:

```java
// 0.08 l/km x 2.362 kg/l: 0.189 kg CO2 per km.
vehicleTwo.setAverageCO2EmissionFactor(fuelConsumptionVehicleTwo * co2FactorPetrol);
// Charged for each route of Vehicle Two: more than its CO2 saving on a short route, less than
// its saving on the long route.
vehicleTwo.setFixCost(2000.0);
opti.addElement(vehicleTwo);
```

A vehicle without `setAverageCO2EmissionFactor` uses the default factor that the CO2 emission feature guide gives.

The comments on the weight and on the fix cost rest on a short calculation. With the weight 10, each kilometer that Vehicle Two drives instead of Vehicle One saves 10 x (0.657 - 0.189), about 4.68, of CO2 cost. On the 889.7 km of the long route that is about 4164, more than the fix cost of 2000. On the 127.31 km of the short route it would be about 596, less than 2000.

### Events and the result

`attachToObservables` prints the progress, warning, status and error events, and `startAndPresentResult` starts the run, waits up to 5 minutes for the result and prints it. Both work as in the First optimization example.

## Output and what to look for

Two test runs gave the same split and the same cost. Both routes are on May 6; neither vehicle has a route on May 7.

- Vehicle One drives the short route with Dueren, Koeln and Aachen, in this order in both runs. In an earlier test run the order was Aachen, Dueren, Koeln, at the same cost.
- Vehicle Two drives the long route with Essen, Wuppertal, Nuernberg and Heilbronn.

The route of Vehicle One, trimmed to these lines:

```text
Resource               : Vehicle One
Route Distance   [km]  : 127.31
Route Emission [kgCO2] : 83.67
```

```text
Cost TotalSum: 977.0986925361068,Cost Distance: 964.0613925361068,Cost RouteTime: 13.037299999999998
```

The route of Vehicle Two, trimmed to these lines:

```text
Resource               : Vehicle Two
Effective Route Stop   : 06.05.2020 20:34:01 (Europe/Berlin)
Route time       [min] : 754.02
Route Distance   [km]  : 889.7
Route Emission [kgCO2] : 168.11
```

```text
[ CONSTRAINTVIOLATION/MAXTIME/EXCEEDED/ Overtime [h]: 0.5669699999999995 Value: 0.5669699999999995 ]
```

```text
Cost TotalSum: 4975.9003195940495,Cost Distance: 2570.888863925982,Cost RouteTime: 62.834849999999996,Cost MaxRouteTime: 342.1766056680677,Cost ResourceFixCost: 2000.0
```

What to look for:

- **Emission:** 83.67 kg for 127.31 km and 168.11 kg for 889.7 km match the two emission factors, 0.657 and 0.189 kg per km.
- **CO2 cost:** the result has no line of its own for the CO2 cost. It is part of `Cost Distance`, which is why the 127.31 km of Vehicle One cost 964.06 there and the 889.7 km of Vehicle Two only 2570.89.
- **Fix cost:** `Cost ResourceFixCost: 2000.0` is the fix cost of the one route of Vehicle Two.
- **Overtime:** the route of Vehicle Two takes 754.02 minutes, a little more than its 12 hours, so the result shows a MAXTIME violation with 0.57 hours of overtime. The route stops at 20:34:01, more than three hours after the end of its working hours at 17:00; the 0.57 hours count only the time beyond the 12 hours. Time after the end of the working hours counts against the maximal working time, and the working hours are a frame for matching resources with the opening hours of the nodes, so a route can end after its working hours without a violation. Only the time beyond the maximal working time is overtime and a violation.

In an earlier test run with `vehicleTwo.setFixCost(0.0)` instead, Vehicle Two drove a route on each of the two days, with every stop other than Aachen, which lies at the start of both vehicles.

## What the CO2 feature does in JOpt

### 1) Emission assessment (always available)
JOpt can compute CO2 emission:
- per route,
- and for the overall job: the result reports the emission per route, and their sum is the emission of the job.

With `JOptWeight.CO2Emission` at zero, this is a **reporting feature**: emissions are calculated and reported, but **they do not influence the optimization result** unless you explicitly activate a CO2 objective weight.

### 2) CO2 as an optimization goal (optional)
If you want the optimizer to prefer low-emission solutions, you must set a weight:

- `JOptWeight.CO2Emission` > 0 (the example sets it to `10.0`)

This adds CO2 emission as a goal in the cost function and the solver will trade it off against other objectives (distance, timing, resource costs, etc.), while operational constraints still count in the cost (in this example, the route of Vehicle Two goes over its maximal working time; see "Output and what to look for").

## Default emission factor and unit interpretation

The documentation states that, by default, a vehicle has a CO2 emission factor of:

- `0.377 [kg*CO2/km]` (see the [CO2 emission feature guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/co2_emission))

This corresponds approximately to an average fuel consumption of 14.3 liters diesel per 100 km, with the 2.629 kg CO2 per liter of diesel that the example uses (0.377 / 2.629 = 0.143 liters per km).

Practical example from the documentation:
- 500 km -> 188.5 kg CO2

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
- 25 L / 100 km -> `25.0 / 100 = 0.25 L/km`
- 8 L / 100 km -> `8.0 / 100 = 0.08 L/km`

2) Multiply by a CO2 factor (kg per liter):
- Diesel: `2.629 kg/l`
- Petrol: `2.362 kg/l`

3) Resulting emission factors:
- Diesel vehicle: `0.25 * 2.629 = 0.65725 kgCO2/km`
- Petrol vehicle: `0.08 * 2.362 = 0.18896 kgCO2/km`

These are exactly the kinds of computations shown in the CO2 documentation.

## The advanced example: `CO2EmissionOptimizationExample`

### What it demonstrates (high-level story)
The example creates a small tour optimization problem and defines **two vehicles**:

- **Vehicle One**: higher CO2 emission factor (diesel, high consumption)
- **Vehicle Two**: lower CO2 emission factor (petrol, low consumption) but with a higher **fixed cost**

The intent is to demonstrate a realistic decision:
- a "greener" vehicle might be operationally more expensive (or scarce),
- so whether it is used depends on:
  - the CO2 objective weight, and
  - the cost structure (fixed cost vs distance/time costs).

### Expected outcome (what the example explains)
The code comments explain the first two points of the intended result pattern:

- Vehicle One is used for a shorter trip.
- Vehicle Two is used for a longer trip.
- If you decrease Vehicle Two's fixed cost, **or** increase the CO2 objective weight, the optimizer will increasingly prefer Vehicle Two, potentially using it exclusively.

This is the key "business narrative":
- CO2 optimization is not a binary switch.
- It is a goal that competes with other goals (especially cost).

## How to interpret results

The CO2 value is part of the **route result header** (per the documentation).  
Therefore, when you print the result (`System.out.println(result)`), you should expect to see emissions reflected in the header of each route, not in the summary information.

Recommended practice:
- In production systems, do not rely only on `toString()` output.
- Extract structured route headers and store:
  - total distance,
  - total time,
  - total CO2,
  - and per-route equivalents.
This allows you to build dashboards and to run "what-if" comparisons.

## Operational guidance: using CO2 optimization correctly

### 1) Start by using CO2 as reporting only
Before steering the solver, you should ensure:
- emission factors are correct,
- units are correct,
- the reported numbers are plausible.

This builds trust and prevents "garbage in -> optimized garbage out".

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

## Summary

- JOpt includes CO2 emission assessment out of the box; with `JOptWeight.CO2Emission` at zero it is a reporting feature.
- You can make CO2 part of the optimization goal by setting `JOptWeight.CO2Emission` > 0.
- Emission factors are configured per resource using `setAverageCO2EmissionFactor(...)`.
- The advanced example demonstrates a realistic trade-off: a "greener" vehicle can have a higher fixed cost, and the optimizer chooses based on objective weights.
- Best practice is to validate reporting first, then increase CO2 weight gradually with clear monitoring of business KPIs.

## Run it

Run the `main(String[] args)` method of `CO2EmissionOptimizationExample` from your IDE, with the repository imported as a Maven project. You need a JDK in the version that the examples pom sets. No license file is needed: without one, the example uses the public evaluation license (see `ExampleLicenseHelper`). The [license guide: element limits](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/license#free-license) lists what each license allows.

## Related

- [CO2 emission feature guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/co2_emission): the default emission factor, the weight `JOptWeight.CO2Emission` and the CO2 fields of the REST input.
- [Optimization properties](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties): the property reference, with the defaults.
- [First optimization example](../../basic/firstoptimization_01/FirstOptimizationExample.md): the run pattern that this example follows, with one vehicle and the same seven nodes.
- [Advanced examples](../README.md): the overview of all Advanced examples.
- [Recommended ways to run an optimization](../../basic/recommendedimplementation_02/RecommendedImplementation.md): the recommended run pattern, a synchronous run and the legacy callback methods.

---

Added: 2026-02-17 (JOpt core 7.5.3-j17) | Last updated: 2026-10-23
