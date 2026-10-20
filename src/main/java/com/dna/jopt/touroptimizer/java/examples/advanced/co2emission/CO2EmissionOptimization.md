# CO2 emission as an optimization goal

`CO2EmissionOptimizationExample` plans seven visits with two vehicles from Aachen: Vehicle One emits more CO2 per kilometer, Vehicle Two less, but Vehicle Two costs a fix cost for each route it drives. With the weight `JOptWeight.CO2Emission`, the emission becomes part of the cost, and the run gives the long route to Vehicle Two and the short one to Vehicle One. The page is for developers who plan fleets with different vehicles. The [CO2 emission feature guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/co2_emission) explains the feature itself, and the [First optimization example](../../basic/firstoptimization_01/FirstOptimizationExample.md) the run pattern that this example follows.

## What the example shows

- `setAverageCO2EmissionFactor(...)` sets the emission factor of a vehicle in kilograms of CO2 per kilometer. Each route of the result reports its emission in the line `Route Emission [kgCO2]`.
- The property `JOptWeight.CO2Emission` makes the emission part of the cost. Without it, the emission is only reported.
- `setFixCost(...)` gives Vehicle Two a cost for each route it drives. The fix cost competes with the CO2 cost, and the result shows which vehicle drives which route.

## Scenario

Two vehicles start in Aachen and visit seven nodes: Koeln, Essen, Dueren, Nuernberg, Heilbronn, Wuppertal and Aachen. The nodes are open and both vehicles work from 08:00 to 17:00 on May 6 and May 7, 2020 (Europe/Berlin); the dates are sample values. Each visit takes 20 minutes. Each vehicle may work 12 hours and drive 1200 km per day.

The vehicles differ in their emission factor and their fix cost. The fuel consumptions and the CO2 values per litre are sample values:

| Vehicle | Fuel | Consumption | CO2 per litre | Emission factor | Fix cost per route |
|---|---|---|---|---|---|
| Vehicle One | diesel | 25 l per 100 km | 2.629 kg | 0.657 kg per km | not set |
| Vehicle Two | petrol | 8 l per 100 km | 2.362 kg | 0.189 kg per km | 2000 |

## How the code works

The source is [CO2EmissionOptimizationExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/co2emission/CO2EmissionOptimizationExample.java). `example()` sets the license and calls `addProperties`, `addNodes`, `addResources`, `attachToObservables` and `startAndPresentResult`, as the First optimization example does.

### The CO2 weight

`addProperties` sets the weight next to the two values that keep the run short:

```java
// Fewer generations and iterations than the defaults keep the run short.
props.setProperty("JOptExitCondition.JOptGenerationCount", "1000");
props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumIterations", "100000");
// With a weight above zero the CO2 emission becomes part of the cost; without it the emission
// is only reported. With 10.0, the CO2 saving of Vehicle Two exceeds its fix cost only on the
// long route.
props.setProperty("JOptWeight.CO2Emission", "10.0");
```

The [property reference](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties) lists the defaults of the three properties.

### The two vehicles

`addResources` computes each emission factor from the fuel consumption in litres per kilometer and the CO2 per litre. Vehicle One:

```java
// 0.25 l/km x 2.629 kg/l: 0.657 kg CO2 per km.
vehicleOne.setAverageCO2EmissionFactor(fuelConsumptionVehicleOne * co2FactorDiesel);
opti.addElement(vehicleOne);
```

Vehicle Two, with the fix cost:

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
- **Overtime:** the route of Vehicle Two takes 754.02 minutes, a little more than its 12 hours, so the result shows a MAXTIME violation with 0.57 hours of overtime. The route stops at 20:34:01, more than three hours after the end of its working hours at 17:00; the 0.57 hours count only the time beyond the 12 hours. Overtime counts against the maximal working time, and the working hours are a frame for matching resources with the opening hours of the nodes, so a route can end after its working hours without a violation.

In an earlier test run with `vehicleTwo.setFixCost(0.0)` instead, Vehicle Two drove a route on each of the two days, with every stop other than Aachen, which lies at the start of both vehicles.

## Run it

Run the `main(String[] args)` method of `CO2EmissionOptimizationExample` from your IDE, with the repository imported as a Maven project. You need a JDK in the version that the examples pom sets. No license file is needed: without one, the example uses the public evaluation license (see `ExampleLicenseHelper`). The [license guide: element limits](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/license#free-license) lists what each license allows.

## Related

- [CO2 emission feature guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/co2_emission): the default emission factor, the weight `JOptWeight.CO2Emission` and the CO2 fields of the REST input.
- [Optimization properties](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties): the property reference, with the defaults.
- [First optimization example](../../basic/firstoptimization_01/FirstOptimizationExample.md): the run pattern that this example follows, with one vehicle and the same seven nodes.
- [Advanced examples](../README.md): the overview of all Advanced examples.
- [Recommended ways to run an optimization](../../basic/recommendedimplementation_02/RecommendedImplementation.md): the recommended run pattern, a synchronous run and the legacy callback methods.

---

Added: 2026-02-17 (JOpt core 7.5.3-j17) | Last updated: 2026-10-20
