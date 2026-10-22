# Performance mode examples

`PerformanceModeExample` switches on performance mode with the property `JOpt.PerformanceMode` and prints the run time, so that you can compare it with a run without performance mode on your machine. `PerformanceModeBigOptiExample` builds the setup of the evaluation in the [performance mode feature guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/performance_mode), 2000 nodes and 40 resources, and needs a full license. The page is for developers who want to try the switch; the feature guide describes what performance mode changes, when to use it and the results of its evaluation.

## What the example shows

Performance Mode is a built-in execution strategy in **JOpt.TourOptimizer** that aims to reduce runtime by simplifying parts of the optimization process.

It is designed for situations where you primarily need:
* **fast turnaround** (large instances, frequent re-optimizations, interactive planning),
* and you can accept a solution that may be **slightly less optimized** than the best outcome achievable in Standard Mode.

- **The switch:** the property `JOpt.PerformanceMode` set to `true` switches performance mode on. Both classes set it from the constant `USE_PERFORMANCE_MODE`; the nodes, the resources and the other properties stay the same.
- **The run time:** the output of a run shows no run time, so both classes measure the time from the start of the run to the result and print it as the last line.
- **The comparison:** set `USE_PERFORMANCE_MODE` to `false` and run the class again to see the run time without performance mode. Compare runs on the same machine, because the run time depends on it.
- **The large setup:** `PerformanceModeBigOptiExample` has switches for the skill matching of the evaluation and for a plan over two days, and writes its result to a KML file.

## Scenario

The dates, times and coordinates are sample values. The time zone is Europe/Berlin.

### The small plan of PerformanceModeExample

The class uses the data of the [First optimization example](../../basic/firstoptimization_01/FirstOptimizationExample.md):

- **Resource:** `Jack from Aachen`, a `CapacityResource` at 50.775346, 6.083887. He works on May 6 and May 7, 2020, from 08:00 to 17:00, may work up to 9 hours a day, the length of the working hours, and drive up to 1200 km, more than the longest day of this plan needs.
- **Nodes:** seven `TimeWindowGeoNode` objects, Koeln, Essen, Dueren, Nuernberg, Heilbronn, Wuppertal and Aachen, open on both days from 08:00 to 17:00, each with a visit of 20 minutes and the same importance.
- **Properties:** `JOptExitCondition.JOptGenerationCount` 10000, which makes the genetic evolution long enough that the run times with and without performance mode differ clearly; `JOpt.Algorithm.PreOptimization.SA.NumIterations` 100000, below its default, which keeps the simulated annealing short; and `JOpt.PerformanceMode`. The defaults are in the [property reference](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties).

### The large plan of PerformanceModeBigOptiExample

The class creates its elements in loops, in the same way as `ClusteringInnerCityExample` of the [clustering construction examples](../clustering/Clustering.md):

- **Nodes and resources:** 2000 nodes, `N_0` to `N_1999`, and 40 resources, `R_0` to `R_39`, on two phyllotaxis patterns around a point in the west of Koeln (50.9333, 6.85). Element i lies at the distance spacing x sqrt(i) from that point and at i times the golden angle. The spacing is 0.002 degrees for the nodes and 0.01 degrees for the resources, so the outermost node lies about 0.089 degrees from the point and the outermost resource about 0.062 degrees: the resources lie inside the area of the nodes.
- **Times:** the nodes are open and the resources work on May 6, 2023, from 08:00 to 22:00. Each visit takes 5 minutes; each resource may work 12 hours, a sample value, and drive 2200 km, more than a resource can drive in 24 hours at its average speed (`setAvgSpeed`).
- **Properties:** `JOptExitCondition.JOptGenerationCount` 100000, the smallest generation count of the evaluation in the feature guide, `JOpt.Algorithm.PreOptimization.SA.NumIterations` 100000 and `JOpt.PerformanceMode`.
- **Switches:** with `ADD_SKILL_CONSTRAINTS` set to `true`, 1000 of the 2000 nodes need the type `efficient` (a `TypeConstraint`), and 13 of the 40 resources have it (a `TypeQualification`), as in the evaluation of the feature guide. The fixed seed `SKILL_SEED` picks the same nodes and resources in every run. With `ADD_EVEN_ODD_SPLITTING` set to `true`, the nodes and resources with an even index move to May 7. Both switches are `false` in the committed code.

## How the code works

The sources are [PerformanceModeExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/performancemode/PerformanceModeExample.java) and [PerformanceModeBigOptiExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/performancemode/PerformanceModeBigOptiExample.java). `example()` of `PerformanceModeBigOptiExample` runs these steps:

```java
// Needs your own full license file where ExampleLicenseHelper looks for it: the public
// evaluation license does not allow this many elements.
ExampleLicenseHelper.setLicense(this);
addProperties(this);
addNodes(this);
addResources(this);
attachToObservables(this);
IOptimizationResult result = startAndPresentResult(this);
exportToKml(result);
```

`PerformanceModeExample` runs the same steps without `exportToKml`, and its comment above `setLicense` names the public evaluation license. `attachToObservables` subscribes to the progress, warning, status and error events with the event API, before the run starts.

### The switch

Both classes hold the switch in a constant:

```java
// Set to false and run the example again to compare the run time without performance mode.
private static final boolean USE_PERFORMANCE_MODE = true;
```

`addProperties` of `PerformanceModeExample` passes it on as the property:

```java
// 10000 generations make the genetic evolution long enough that the run times with and
// without performance mode differ clearly.
props.setProperty("JOptExitCondition.JOptGenerationCount", "10000");
// Fewer iterations than the default keep the simulated annealing short.
props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumIterations", "100000");
props.setProperty("JOpt.PerformanceMode", String.valueOf(USE_PERFORMANCE_MODE));
```

### The run time

`startAndPresentResult` measures the time from the start of the run to the result:

```java
long startNanos = System.nanoTime();
CompletableFuture<IOptimizationResult> resultFuture = opti.startRunAsync();
// Wait for the result; without the wait the JVM would end before the run does.
IOptimizationResult result = resultFuture.get(5, TimeUnit.MINUTES);
long runMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
System.out.println(result);
// The run time depends on the machine: compare runs on the same machine.
System.out.println(
    "JOpt.PerformanceMode " + USE_PERFORMANCE_MODE + ", run time [s]: " + runMillis / 1000.0);
```

`PerformanceModeBigOptiExample` waits up to 15 minutes instead of 5, because a run of its size can take several minutes; the feature guide shows the run times of its evaluation. It returns the result for the KML export.

### The KML export

`exportToKml` of `PerformanceModeBigOptiExample` writes the result to `PerformanceModeBigOptiExample.kml` in the working directory, as in [Export a result to a KML file](../../basic/io_03/Export2KMLExample.md).

## Output and what to look for

### PerformanceModeExample

In two test runs, the result was the same: Jack visits Aachen, Dueren, Koeln, Wuppertal and Essen on May 6, and Nuernberg and Heilbronn on May 7. The day of May 7 takes longer than the 9 hours that Jack may work, and the result shows the overtime as a `MAXTIME` violation (trimmed to the route of May 7):

```text
[ CONSTRAINTVIOLATION/MAXTIME/EXCEEDED/ Overtime [h]: 1.8863266666666654 Value: 1.8863266666666654 ]
1.0 Nuernberg / Geo  / Arrival: 07.05.2020 12:47:54 (Europe/Berlin) , Departure: 07.05.2020 13:07:54 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 287.91 [min], 380.04 [km]
1.1 Heilbronn / Geo  / Arrival: 07.05.2020 14:52:19 (Europe/Berlin) , Departure: 07.05.2020 15:12:19 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 104.42 [min], 137.83 [km]
ViolationSumary: NumViolations: 1,MTV: 1
```

The last line starts with `JOpt.PerformanceMode true, run time [s]:` and gives the run time in seconds, which can change between runs.

For the comparison, a variant of the class with `USE_PERFORMANCE_MODE` set to `false` ran three times, alternately with three runs of the class as it is, on one machine. Each run with performance mode took less time than each run without it, and all six runs ended with the same routes and the same cost. On this small plan, both settings found the same routes; the [feature guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/performance_mode) describes the trade-off on large plans.

### PerformanceModeBigOptiExample without a full license

With the public evaluation license, the class ended with exit code 1 after a few seconds in two test runs, with the same messages both times:

- **Element count:** the last status event before the error starts with `StatusEvent(STATUS_INFO, Execeed number of elemens. Allowed vs. actual:` and continues with two numbers: the number of elements that the license allows, and the number of elements of the example, 2040.
- **License error:** JOpt then logs the license error with this exception, and the example prints it once more in the error event `UNCAUGHT_EXCEPTION_IN_OPTIMIZATION`:

  ```text
  com.dna.jopt.framework.exception.caught.InvalidLicenceException: The provided license data is not valid or no license is available. Please provide a valid license.
  ```

- **End of the run:** the wait in `startAndPresentResult` ends with this exception, and `main` ends with it:

  ```text
  Exception in thread "main" java.lang.InterruptedException
  ```

  The license error above it is the cause. The run prints no result and no run time and writes no KML file.

### PerformanceModeBigOptiExample with a full license

With your own full license file, the class prints the result and the run time and writes its KML file. Open the KML file in Google Earth or another KML viewer to see the routes on a map. Run the class with `USE_PERFORMANCE_MODE` set to `true` and to `false`, and with `ADD_SKILL_CONSTRAINTS` set to `true` and to `false`, to repeat the four configurations of the evaluation in the feature guide on your machine.

## What Performance Mode changes internally

According to the official documentation, enabling Performance Mode changes two major aspects of the genetic phase:

### 1) Reduced population size
Performance Mode fixes the number of concurrent solutions evaluated during genetic evolution: it sets the **population size** to 50, whatever `JOpt.JOptPopulationSize` is set to (documented as "cut in half", which holds only for the default population size; the default is in the [property reference](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties)).

Practical effect:
- fewer candidate solutions are processed per evolutionary iteration,
- less CPU time spent per generation,
- but also less variation in the search space, which can reduce the optimizer's ability to escape local minima in difficult problems.

### 2) Several advanced operators are disabled
The following operators are disabled in Performance Mode; the documentation lists the first eight:

- `Single2OptOperator`
- `RecombineOptimizationElementsOperator2`
- `RecombineOptimizationElementsOperator3`
- `InversionOperator`
- `ReciprocalExchangeOperator`
- `RouteSeedOperator`
- `DoubleRouteRandomizeOperator`
- `DisplacementOperator`
- `ReciprocalExchangeMoveHighPriorityNodesOperator`

Practical effect:
- less "exploration and variation" per generation,
- higher speed,
- but fewer mechanisms to repair or refine complex route structures.

## Expected outcome and trade-offs

### Speed
The documentation reports that Performance Mode can make runs **up to ~50% faster** and demonstrates speedups from about **1.45x to 1.7x** in their evaluation setup.

### Solution quality
The same evaluation reports that solution quality remained around **~95%** of Standard Mode (in the tested scenario), while achieving the speed improvements.

Interpretation:
- Performance Mode is often a strong default for "large and relatively flexible" instances,
- but you should validate solution quality for your specific constraint landscape.

## When to use Performance Mode

Performance Mode is typically a good fit for:

- **Large-scale optimizations** (many nodes/resources) where runtime is a priority.
- **Low-restriction** problems (few time windows, few skills, few territory/zone constraints).
- **Long working hours / flexible schedules**, where the solution space is broad and simpler genetic exploration is still effective.
- **Operational re-optimization** loops where you run frequently (e.g., every few minutes) and want "good fast" rather than "best slow".

## When not to use Performance Mode

Avoid (or carefully validate) Performance Mode when:

- The problem is **highly constrained**, for example:
  - tight and strict time windows,
  - complex skill matching with limited qualified resources,
  - strong geographic territories / zone constraints,
  - many hard constraints that require sophisticated repair operators.
- You need **maximum optimality** (e.g., high-cost operations where small improvements matter significantly).
- You are diagnosing feasibility issues and require the most capable internal operator set to explore "hard" repairs.

## How to enable Performance Mode

Performance Mode is enabled using a single optimizer property. A sketch, not code of the example classes:

```text
JOpt.PerformanceMode = true
```

This is passed as part of the `Properties` element in the optimization instance.

### Minimal property snippet (conceptual)
- Create a `Properties` object
- Set:
  - `props.setProperty("JOpt.PerformanceMode", "true");`
- Add it to the optimization:
  - `opti.addElement(props);`

Both examples included in this package use exactly this approach.

## Example 1: `PerformanceModeExample`

This example is intentionally small and clear:

- It sets a boolean toggle:
  - `USE_PERFORMANCE_MODE = true`
- It adds properties with:
  - a high generation count (to make runtime differences visible),
  - the number of CPU cores at its default (the class does not set `JOpt.NumCPUCores`),
  - and `JOpt.PerformanceMode` set from the boolean.

Key learning:
- Performance Mode is a **configuration switch**: you do not need to change your modeling (nodes/resources/constraints).
- You can A/B test by flipping one boolean.

Recommended usage:
- Keep the boolean flag in your production configuration so you can turn Performance Mode on/off per scenario.

## Example 2: `PerformanceModeBigOptiExample`

This example is built to represent a **stress test** and to reflect the evaluation ideas described in the documentation.

### What makes it "big"
The example programmatically generates:
- a large set of nodes around a center position,
- a set of resources around a center position,
- and optional complexity toggles such as skill constraints and "even-odd splitting".

It uses a *phyllotaxis sampling* strategy to distribute positions deterministically around a center location, producing dense but structured spatial instances.

### Complexity toggles (important for performance testing)
As constants of the class, the example defines switches such as:
- `ADD_SKILL_CONSTRAINTS` (adds a skill requirement to ~half of the nodes and provides that skill only to a subset of resources)
- `ADD_EVEN_ODD_SPLITTING` (moves the nodes and resources with an even index to May 7, which splits the plan into two days, making results less geographically compact)

This is a recommended pattern for serious performance evaluation:
- benchmark the same instance under multiple "constraint intensities,"
- then decide if Performance Mode remains acceptable.

### What to watch for when running it
- Runtime difference between Standard vs Performance Mode.
- Route quality differences (distance/time/constraint violations).
- Whether constraints (skills/time windows) remain well satisfied.
- Whether the solution changes structure (more fragmented routes vs compact routes).

## Practical testing strategy

A reliable way to decide if Performance Mode should be default in your environment:

1. Select 3-5 representative instance types:
   - "easy" (few constraints),
   - "typical" (moderate constraints),
   - "hard" (tight time windows / scarce skills).
2. Run Standard Mode vs Performance Mode with identical settings:
   - same exit conditions,
   - same number of cores,
   - same random seed strategy (if applicable).
3. Compare:
   - total runtime,
   - objective value / route cost,
   - feasibility metrics (hard constraints satisfied),
   - operational KPIs (lateness, overtime, total distance).
4. Decide per segment:
   - Use Performance Mode for "easy/typical" segments,
   - and Standard Mode for "hard" segments.

## Recommended positioning in production systems

Many teams use Performance Mode as part of a two-tier pipeline:

### Tier 1: Fast solution
- Performance Mode ON
- Lower compute budget
- Used for:
  - interactive planning,
  - frequent re-optimization,
  - "quick feasibility + decent quality"

### Tier 2: Refinement solution
- Performance Mode OFF (Standard Mode)
- Higher compute budget
- Used for:
  - final dispatch freeze,
  - overnight planning,
  - high-value deliveries

This approach provides a pragmatic balance:
- fast response during operations,
- higher quality when it matters.

## Summary

- Performance Mode accelerates optimization by setting a fixed population size of 50 and disabling several advanced genetic operators.
- It can deliver significant runtime improvements (documented up to ~50%, and ~1.45x-1.7x in an evaluation), with an observed quality retention around ~95% in the tested setup.
- It is best for large, flexible, lightly constrained instances and operational re-optimization.
- For tight, complex constraint landscapes, validate carefully or prefer Standard Mode.
- Enable it via: `JOpt.PerformanceMode = true` in the optimizer properties.

## Run it

- **PerformanceModeExample:** run its `main(String[] args)` method from your IDE, with the repository imported as a Maven project. You need a JDK in the version that the examples pom sets. You need no license file: without one, the example uses the public evaluation license (see the [license guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/license)). Then set `USE_PERFORMANCE_MODE` to `false`, run it again and compare the last lines.
- **PerformanceModeBigOptiExample:**
  1. Put your own full license file where `ExampleLicenseHelper` looks for it, `src/main/resources/license/YOUR_LICENSE.jli`. [Set a license from a file](../../basic/setlicense_08/SetLicense.md) shows how the examples read it, and the [license guide: element limits](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/license#free-license) lists what each license allows.
  2. Run its `main(String[] args)` method from your IDE.
  3. Open the KML file in the working directory.

## Related

- [Performance mode feature guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/performance_mode): what performance mode changes, when to use it and the evaluation with the setup of `PerformanceModeBigOptiExample`.
- [Optimization properties](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties): the property reference, with `JOpt.PerformanceMode` and its default.
- [First optimization example](../../basic/firstoptimization_01/FirstOptimizationExample.md): the data of `PerformanceModeExample`.
- [Clustering construction examples](../clustering/Clustering.md): the same phyllotaxis pattern for a large plan.
- [Set a license from a file](../../basic/setlicense_08/SetLicense.md): how the examples read your own license file.
- [Advanced examples](../README.md): the overview of all Advanced examples.
- [Recommended ways to run an optimization](../../basic/recommendedimplementation_02/RecommendedImplementation.md): the recommended run pattern, a synchronous run and the legacy callback methods.

---

Added: 2026-02-17 (JOpt core 7.5.3-j17) | Last updated: 2026-10-23
