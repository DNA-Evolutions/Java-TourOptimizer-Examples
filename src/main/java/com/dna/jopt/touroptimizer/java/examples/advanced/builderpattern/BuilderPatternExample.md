# Build an optimization config with builders

`BuilderPatternExample` creates the whole input of an optimization as one immutable `OptimizationConfig` with builders and runs it with `JSONOptimization`. The result comes back as a config as well, and the example prints it as JSON, as the text solution and in its parts. The page is for developers who build the input as data, for example to send it as JSON to JOpt.TourOptimizer. The [First optimization example](../../basic/firstoptimization_01/FirstOptimizationExample.md) builds a similar input from elements added one by one.

## What the example shows

This example demonstrates the **builder pattern** approach to configure and execute an optimization run with **immutable input objects**.  
Instead of wiring a large number of constructor calls, you create a complete optimization configuration using fluent builders and then run it with the **JSONOptimization** engine.

- **One config object:** the builders of `OptimizationConfig`, `Node`, `Resource`, `WorkingHours`, `OpeningHours` and `JSONConfig` create the nodes, the resource, the extension and the properties. `build()` returns an immutable object.
- **The JSON engine:** `JSONOptimization` runs the config with `startAsynchConfigFuture`. The future returns a config again: the input plus the solution.
- **Three forms of the result:** `JSONOptimization.asJSON` prints the result config as JSON, the extension holds the text solution, and `solution()` gives the solution header, the route headers and the element details.
- **Limits on two levels:** a `WorkingHours` object can carry its own `maxTime` and `maxDistance`. For that working day they take precedence over the limits of the resource.

## Scenario

Jack from Aachen (50.775346, 6.083887) visits two nodes on May 6, 2020:

- Koeln (50.9333, 6.95)
- Essen (51.45, 7.01667)

The nodes are open and Jack works from 08:00 to 17:00 (Europe/Berlin). Each visit takes 20 minutes. The dates are sample values.

Jack's working hours of May 6 allow 10 hours and 1000 km. The resource itself allows 9 hours and 1000 km; these limits apply to working hours without limits of their own, and the one working day of this example has its own. The day stays far below both limits.

The example sets no license, so JOpt runs in its free mode; the three elements fit into it.

## How the code works

The source is [BuilderPatternExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/builderpattern/BuilderPatternExample.java). The class does not extend `Optimization`. `example()` builds the config, creates the engine, subscribes to its events, runs the config and prints the parts of the solution:

```java
OptimizationConfig<JSONConfig> config = createConfig();

// JSONOptimization runs a config object instead of elements added one by one.
IJSONOptimization opti = new JSONOptimization();
attachToObservables(opti);

OptimizationConfig<JSONConfig> result = startAndPresentResult(opti, config);
printSolution(result);
```

### The config

`createConfig` puts the parts together:

```java
return OptimizationConfig.<JSONConfig>builder()
    .addAllNodes(createNodes())
    .addAllResources(createResources())
    .extension(createJSONExtension())
    .optimizationOptions(createOptimizationOptions())
    .build();
```

The extension, a `JSONConfig`, holds the timeout of the config and, if you add one, your license:

```java
// To use your own license, put its JSON string here: Optional.of("YOUR JSON LICENSE").
// Without a license JOpt runs in its free mode; an invalid license makes the run fail.
Optional<String> myJSONLicenseOpt = Optional.empty();

if (myJSONLicenseOpt.isPresent()) {
  OptimizationKeySetting keySetting = OptimizationKeySetting.of(myJSONLicenseOpt.get());
  return JSONConfig.builder().keySetting(keySetting).timeOut(TIMEOUT_OPTIMIZATION).build();
}
return JSONConfig.builder().timeOut(TIMEOUT_OPTIMIZATION).build();
```

`TIMEOUT_OPTIMIZATION` is 10 minutes, a sample value. The printed JSON carries it in its extension; the run of this example ends long before.

The properties go into the config as `OptimizationOptions`:

```java
// Fewer generations and iterations than the defaults keep the run short.
propMap.put("JOptExitCondition.JOptGenerationCount", "1000");
propMap.put("JOpt.Algorithm.PreOptimization.SA.NumIterations", "100000");
```

The [property reference](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties) lists the defaults.

### Working hours with their own limits

`createWorkingHours` sets limits on the working hours themselves:

```java
// Limits set on the working hours take precedence over the limits of the resource for this
// day: 10 h instead of the resource's 9 h. The 1000 km are more than this day needs.
return WorkingHours.builder()
    .begin(begin.toInstant())
    .end(end.toInstant())
    .zoneId(begin.getZone())
    .maxTime(Duration.ofHours(10))
    .maxDistance(Quantities.getQuantity(1000, KILO(METRE)))
    .build();
```

The resource needs limits of its own as well:

```java
// The resource needs its own limits. They apply to working hours without limits of their
// own; the one working day of this example sets its own (see createWorkingHours).
Duration maxWorkingTime = Duration.ofHours(9);
Quantity<Length> maxDistance = Quantities.getQuantity(1000, KILO(METRE));
```

### Running the config and reading the result

`attachToObservables` subscribes to the progress, warning, status and error events of the engine before the start. `startAndPresentResult` runs the config, waits up to 5 minutes and prints the JSON and the text solution:

```java
CompletableFuture<OptimizationConfig<JSONConfig>> resultFuture =
    opti.startAsynchConfigFuture(config, Optional.empty());
// Wait for the result; without the wait the JVM would end before the run does.
OptimizationConfig<JSONConfig> result = resultFuture.get(5, TimeUnit.MINUTES);

System.out.println(JSONOptimization.asJSON(result));
System.out.println();

result
    .extension()
    .flatMap(JSONConfig::textSolution)
    .ifPresent(t -> System.out.println(t.textSolution()));
System.out.println();
```

`printSolution` then prints the `SolutionHeader`, each `RouteHeader` and each `RouteElementDetail` of the result: the parts an application reads to show the result in its own layer.

## Output and what to look for

The output starts with the status events and the progress lines of the run. In two test runs, the JOpt log also shows the line `Limiting run time` near the start.

Then the result config follows as JSON. It holds the nodes, the resource, the connections between the elements, the properties, the solution and the extension. Times in the JSON are in UTC: `2020-05-06T06:00:00Z` is 08:00 in Europe/Berlin. The resource part shows both levels of limits, trimmed to these lines in two test runs:

```json
"workingHours" : [ {
  "begin" : "2020-05-06T06:00:00Z",
  "end" : "2020-05-06T15:00:00Z",
  "zoneId" : "Europe/Berlin",
  "maxTime" : "PT10H",
  "maxDistance" : "1000 km"
} ],
"maxTime" : "PT9H",
"maxDistance" : "1000 km",
```

The extension at the end of the JSON holds the text solution and `"timeOut" : "PT10M"`, but no license.

The text solution follows, in the form known from the other examples. Its route visits Koeln first, then Essen (trimmed to the two stops):

```text
0.0 Koeln / Geo  / Arrival: 06.05.2020 08:47:59 (Europe/Berlin) , Departure: 06.05.2020 09:07:59 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 47.99 [min], 63.34 [km]
0.1 Essen / Geo  / Arrival: 06.05.2020 09:51:37 (Europe/Berlin) , Departure: 06.05.2020 10:11:37 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 43.64 [min], 57.61 [km]
```

The route takes 206.59 minutes and 219.9 km, far below both limits. The last lines are the `SolutionHeader`, the `RouteHeader` and one `RouteElementDetail` for each of the two nodes.

## Why this example matters

The builder pattern is a good fit for enterprise integrations where you want:

- **Strong readability:** configuration reads like a specification ("add nodes", "add resources", "set extension", "set options")
- **Immutability:** one final `build()` produces a **stable config snapshot**
- **Reproducibility:** the resulting config can be serialized to **JSON** and used in other environments (for example REST / OpenAPI)
- **Separation of concerns:** *build input* -> *run engine* -> *consume results*

## Key sections in the code

### 1) Creating the full config using builders
The central idea is to construct everything via builders and "compose" the final config:

- `OptimizationConfig.builder() ... build()`
- `Node.builder() ... build()`
- `Resource.builder() ... build()`
- `WorkingHours.builder() ... build()`
- `OpeningHours.builder() ... build()`
- `JSONConfig.builder() ... build()`

This yields a complete, immutable config object that can be executed and serialized.

### 2) Running with JSONOptimization (and why JSON matters)
The engine used is **JSONOptimization**, which enables:

- **Direct JSON serialization** of the config
- Interoperability with **REST / Swagger/OpenAPI** endpoints that accept a JSON snapshot-like structure

In the example, the config is printed as JSON:

- `JSONOptimization.asJSON(result)`

This is a practical bridge between:
- SDK-style configuration (Java builders)
- API-style execution (JSON payloads)

**Related docs:**
- REST / OpenAPI usage: [Try the API and get the Schema](https://www.dna-evolutions.com/api)
- Snapshot / schema compatibility: [Load and save optimization snapshots](../../basic/io_03/LoadAndSaveOptimization.md); the schema is behind the link above

### 3) Attaching to observables (progress, warnings, status, errors)
The example subscribes to event streams so you can:

- report progress in a UI
- log warnings and status updates
- capture errors centrally

This is the recommended approach for production systems because it:
- improves observability
- makes runs debuggable
- supports "explainability" and operational acceptance

**Related docs:** [Progress output frequency and progress requests](../progressfrequency/OptimizationProgress.md) (progress events and how often they are sent)

### 4) Parsing the output for your application layer
Beyond printing a solution, the example shows how to consume structured result objects:

- solution header (global KPIs / meta)
- route headers (per route stats)
- route element details (visit sequence and per-stop details)

This is typically how you map optimization output into:
- dispatch UIs
- reports
- downstream systems (TMS/ERP/WFM)

## Implementation notes

### License handling (optional)
The example demonstrates how to attach a JSON license via `OptimizationKeySetting`.  
If no license is provided, JOpt runs in its free mode, as the code comment says.

### OptimizationOptions (properties)
The example sets a few optimization properties (generation count and pre-optimization settings).  
This is the entry point for production tuning.

**Related docs:**
- [Optimization properties](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties)
- [Performance mode](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/performance_mode)

## When to choose the builder approach

Use the builder approach when:
- your config has many optional parts (opening hours, constraints, metadata)
- you want clean code and fewer constructor overloads
- you want to reliably serialize the config into a portable JSON representation
- you build configs dynamically (from DB, UI, API calls)

## Next steps

- Connect this pattern to your **REST workflow** (build config -> serialize JSON -> call API)
- Extend nodes/resources with additional constraints (skills, zones, PND, relations)
- Add progress streaming to your UI or monitoring pipeline

## Run it

Run the `main(String[] args)` method of `BuilderPatternExample` from your IDE, with the repository imported as a Maven project. You need a JDK in the version that the examples pom sets. The example sets no license and needs no license file; the [license guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/license) lists what the free mode allows. To run a larger config, put your license JSON into `myJSONLicenseOpt` in `createJSONExtension`.

## Related

- [Creating REST TourOptimizer JSON Input from a Java Optimization](../../restful/createinput/CreateRestTourOptimizerInput.md): an example that turns an optimization built from elements into the JSON input of JOpt.TourOptimizer.
- [API reference](https://www.dna-evolutions.com/api): the schema of the JSON input of JOpt.TourOptimizer.
- [Optimization properties](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties): the property reference, with the defaults.
- [Recommended ways to run an optimization](../../basic/recommendedimplementation_02/RecommendedImplementation.md): the recommended run pattern, a synchronous run and the legacy callback methods.
- [Advanced examples](../README.md): the overview of all Advanced examples.

---

Added: 2026-02-17 (JOpt core 7.5.3-j17) | Last updated: 2026-10-23
