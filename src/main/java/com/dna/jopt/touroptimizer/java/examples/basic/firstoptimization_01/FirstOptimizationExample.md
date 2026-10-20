# First optimization example

`FirstOptimizationExample` plans two working days of one resource, Jack from Aachen, who visits seven cities in Germany: the class sets the license and the properties, adds the nodes and the resource, prints the events of the run and then the result. It is for Java developers who start with JOpt. The [First optimization tutorial](https://www.dna-evolutions.com/docs/getting-started/tutorials/first-optimization) covers the same code step by step.

## What the example shows

`FirstOptimizationExample` extends `Optimization` and builds a complete run in one method, `example()`. Each step is a method of its own: `addProperties`, `addNodes`, `addResources`, `attachToObservables` and `startAndPresentResult`. Most other Base examples use the same names for `attachToObservables` and `startAndPresentResult`; their other steps depend on what they show.

The run starts asynchronously, and the example waits for the result with a timeout. It prints the progress, warning, status and error events while the run is going, and the result at the end.

## Scenario

Jack from Aachen visits seven nodes. The Aachen node lies at Jack's start position. The nodes are open and Jack works from 08:00 to 17:00 on May 6 and May 7, 2020 (Europe/Berlin); the dates are sample values. Each visit takes 20 minutes.

Then seven nodes are created as `TimeWindowGeoNode` objects and added:

- Koeln (50.9333, 6.95)
- Essen (51.45, 7.01667)
- Dueren (50.8, 6.48333)
- Nuernberg (49.4478, 11.0683)
- Heilbronn (49.1403, 9.22)
- Wuppertal (51.2667, 7.18333)
- Aachen (50.775346, 6.083887)

Jack may work 9 hours and drive 1200 km per working day. The day with Nuernberg takes longer than 9 hours, so the result reports overtime. No day of this plan needs 1200 km.

Interpretation: this is a geographically distributed node set that is small, but it already shows a trade-off: the second day runs into overtime. It stays small enough to remain readable.

## How the code works

The source is [FirstOptimizationExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic/firstoptimization_01/FirstOptimizationExample.java).

### The steps of the run

`main(...)` instantiates the class and calls `example()`. `example()` contains the full run sequence: start here when you want to modify the scenario.

`example()` sets the license first. `ExampleLicenseHelper.setLicense` uses your license file if it finds one, otherwise the public evaluation license. Then it calls the steps in order:

```java
public void example()
    throws InterruptedException, ExecutionException, TimeoutException, InvalidLicenceException,
    IOException {
  // The public evaluation license, or your own license if you put a license file where
  // ExampleLicenseHelper looks for it.
  ExampleLicenseHelper.setLicense(this);
  addProperties(this);
  addNodes(this);
  addResources(this);
  attachToObservables(this);
  startAndPresentResult(this);
}
```

### Properties

`addProperties` sets two properties:

- `JOptExitCondition.JOptGenerationCount`: controls how many generations the evolutionary phase is allowed to run.
- `JOpt.Algorithm.PreOptimization.SA.NumIterations`: controls the number of iterations for the simulated annealing pre-optimization.

Both values are lower than the core defaults, which keeps the run short. The [property reference](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties) lists the defaults.

```java
// Fewer generations and iterations than the defaults keep the run short.
props.setProperty("JOptExitCondition.JOptGenerationCount", "1000");
props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumIterations", "100000");
```

Practical reading: our runs gave the same result each time; JOpt has no seed setting, so this is not guaranteed. These values keep the example "busy enough" to show progress updates, but not so heavy that it becomes inconvenient during development.

### Nodes and the resource

`addNodes` gives all seven nodes the same opening hours, a visit duration of 20 minutes and the importance 1. The visit time counts toward Jack's working time, like the driving. The block shows the first node; the other six follow the same pattern:

```java
// Each visit takes 20 minutes and counts toward Jack's working time, like the driving.
Duration visitDuration = Duration.ofMinutes(20);

// The same importance for every node, so no node is preferred over another.
int importance = 1;

INode koeln =
    new TimeWindowGeoNode("Koeln", 50.9333, 6.95, openingHours, visitDuration, importance);
opti.addElement(koeln);
```

`addResources` creates Jack with the working hours of both days, a maximal working time of 9 hours and a maximal distance of 1200 km per day:

```java
// 9 h per day, the length of the shift: the day with Nuernberg exceeds it (overtime).
Duration maxWorkingTime = Duration.ofHours(9);
// 1200 km per day, more than the longest day of this plan needs.
Quantity<Length> maxDistanceKmW = Quantities.getQuantity(1200.0, KILO(METRE));

IResource jack =
    new CapacityResource(
        "Jack from Aachen", 50.775346, 6.083887, maxWorkingTime, maxDistanceKmW, workingHours);
opti.addElement(jack);
```

### Events and the result

`attachToObservables` subscribes to the progress, warning, status and error events and prints each one. It subscribes before the run starts, because the subjects replay only a few past events:

```java
// Subscribe before the start: the subjects replay only a few past events.
opti.getOptimizationEvents()
    .progressSubject()
    .subscribe(p -> System.out.println(p.getProgressString()));
opti.getOptimizationEvents().warningSubject().subscribe(w -> System.out.println(w.toString()));
opti.getOptimizationEvents().statusSubject().subscribe(s -> System.out.println(s.toString()));
opti.getOptimizationEvents().errorSubject().subscribe(e -> System.out.println(e.toString()));
```

This is helpful when you are learning:
- you can see whether the solver is still active,
- what the solver is trying to improve,
- and whether there are modeling or feasibility issues.

`startAndPresentResult` starts the run with `startRunAsync()` and waits up to 5 minutes for the result.

```java
CompletableFuture<IOptimizationResult> resultFuture = opti.startRunAsync();
// Wait for the result; without the wait the JVM would end before the run does.
IOptimizationResult result = resultFuture.get(5, TimeUnit.MINUTES);
System.out.println(result);
```

Why the blocking wait matters:
- if your process exits early, the asynchronous optimization run is terminated with it.
- without a timeout, the call can wait indefinitely if something is misconfigured.

## Output and what to look for

The output starts with status events and progress lines. Then comes the result, with one route per day. Route 0 on May 6 visits Aachen, Dueren, Koeln, Wuppertal and Essen. Route 1 on May 7 goes to Nuernberg and Heilbronn.

The route time of route 1 is its transit time plus 20 minutes for each of its two visits (trimmed to these lines of route 1):

```text
Route time       [min] : 653.18
Transit time     [min] : 613.18
Productive. time [min] : 40.0
```

653.18 minutes are more than the 9 hours that Jack may work, so route 1 shows a MAXTIME violation with the overtime in hours, followed by its two stops:

```text
[ CONSTRAINTVIOLATION/MAXTIME/EXCEEDED/ Overtime [h]: 1.8863266666666654 Value: 1.8863266666666654 ]

1.0 Nuernberg / Geo  / Arrival: 07.05.2020 12:47:54 (Europe/Berlin) , Departure: 07.05.2020 13:07:54 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 287.91 [min], 380.04 [km]
1.1 Heilbronn / Geo  / Arrival: 07.05.2020 14:52:19 (Europe/Berlin) , Departure: 07.05.2020 15:12:19 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 104.42 [min], 137.83 [km]
```

The distance of route 1, 809.4 km, stays below the 1200 km limit, so there is no distance violation.

## Run it

Run the `main(String[] args)` method of `FirstOptimizationExample` from your IDE, with the repository imported as a Maven project. You need a JDK in the version that the examples pom sets. No license file is needed: without one, the example uses the public evaluation license (see `ExampleLicenseHelper`). The [license guide: element limits](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/license#free-license) lists what each license allows.

The `main(...)` signature declares: `InterruptedException`, `ExecutionException`, `TimeoutException`, `InvalidLicenceException`, `IOException`. If the run exceeds the 5-minute wait window, you will receive a `TimeoutException`.

### Making it your own

Change problem size:
- Add/remove nodes (more nodes typically increases runtime and solution complexity).
- Change visit durations (e.g., 5 min vs 60 min changes feasibility).

Change constraints:
- Tighten working hours or opening hours to test feasibility boundaries.
- Adjust `maxWorkingTime` and `maxDistanceKmW` to model operational limits.

Change solver effort:
- Lower `JOptExitCondition.JOptGenerationCount` for quicker feedback loops.
- Lower SA iterations to reduce pre-optimization time.

Change observability:
- Keep progress subscription during development.
- Consider disabling verbose output when embedding into an application or CI.

### Troubleshooting checklist

- **License errors**: review `ExampleLicenseHelper` used by the example.
- **Timeout**: increase the 5-minute wait (or reduce generations / SA iterations).
- **No meaningful result**: verify time windows overlap (resource working hours vs node opening hours).
- **Unexpected infeasibility**: check max working time and max distance relative to geography and visit durations.

### Next steps

Once you understand this example, the next progression is:
- multiple resources,
- different node types or constraints,
- capacity/skills/compatibility constraints,
- alternative objective weighting and more advanced properties.

The tutorial linked above explains the main modeling concepts in depth.

## Related

- [Basic elements tutorial](https://www.dna-evolutions.com/docs/getting-started/tutorials/basic-elements): the nodes, resources and other elements that this example uses.
- [Reactive events and streams](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/reactive_events): the feature guide of the event API that `attachToObservables` uses.
- [Recommended ways to run an optimization](../recommendedimplementation_02/RecommendedImplementation.md): the recommended run pattern, a synchronous run and the legacy callback methods.
- [Base examples](../README.md): the overview of all Base examples.
- [Optimization properties](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties): the property reference, with the defaults.

---

Added: 2026-02-17 (JOpt core 7.5.3-j17) | Last updated: 2026-10-20
