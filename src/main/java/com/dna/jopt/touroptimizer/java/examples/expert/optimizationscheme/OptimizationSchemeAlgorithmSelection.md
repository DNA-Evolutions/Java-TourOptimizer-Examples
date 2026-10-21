# Algorithm selection with an optimization scheme

`OptimizationSchemeAlgorithmSelectionExample` chooses the algorithms of a run itself: the construction algorithm that builds the starting solution, and the optimization phases that follow it, in their order and each with its own settings. A `DefaultOptimizationScheme` built with these choices replaces the default pipeline. The page is for Java developers who want to control or compare the phases of a run; it uses the data of the [First optimization example](../../basic/firstoptimization_01/FirstOptimizationExample.md).

## What the example shows

- **The construction:** a `ConstructionOptimizationAlgorithmConfig` selects `SMART_CONSTR_ALGO`, the smart construction.
- **Three optimization phases:** a list of `HeuristicOptimizationAlgorithmConfig` objects: a short simulated annealing phase (`SMART_SIMULATED_ANNEALING_ALGO` with 1000 iterations), a longer one (the same algorithm with the iterations of the property) and the genetic phase (`GENETIC_EVOLUTION_ALGO`).
- **Settings per phase:** `simulatedAnnealingOverrideNumIterationsValue` replaces the iteration count of the property for one phase; `hasAutoFilter` attaches the AutoFilter to a phase or leaves it out.
- **The scheme:** `new DefaultOptimizationScheme(opti, Optional.of(constructionAlgo), Optional.of(heuristicAlgoConfigs))`, set with `setOptimizationScheme`.

## Scenario

The dates and times are sample values, and so are the coordinates in Germany. The data is that of the first optimization example. `addProperties` sets `JOptExitCondition.JOptGenerationCount` to 1000 and `JOpt.Algorithm.PreOptimization.SA.NumIterations` to 100000. Both are below their defaults, which keeps the run short. The generation count applies to the genetic phase, the iteration count to the second simulated annealing phase.

- **Resource:** `Jack from Aachen` starts at 50.775346, 6.083887 and works on May 6 and 7, 2020, from 08:00 to 17:00 (Europe/Berlin). His maximal working time is 9 hours per day, the length of the shift, and his maximal distance 1200 km.
- **Nodes:** seven `TimeWindowGeoNode` objects in Koeln, Essen, Dueren, Nuernberg, Heilbronn, Wuppertal and Aachen, open on May 6 and 7 from 08:00 to 17:00, each with a visit of 20 minutes and the importance 1.

## How the code works

The source is [OptimizationSchemeAlgorithmSelectionExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/optimizationscheme/selectalgorithms/OptimizationSchemeAlgorithmSelectionExample.java). `example()` calls these steps:

```java
// The public evaluation license, or your own license if you put a license file where
// ExampleLicenseHelper looks for it.
ExampleLicenseHelper.setLicense(this);
setScheme(this);
addProperties(this);
addNodes(this);
addResources(this);
attachToObservables(this);
startAndPresentResult(this);
```

- `ExampleLicenseHelper.setLicense(this)` uses your license file if you put one where the helper looks for it, and the public evaluation license otherwise.
- `setScheme` builds the scheme with the selected algorithms and sets it; the sections below show its parts.
- `addProperties`, `addNodes` and `addResources` add the properties, the seven nodes and Jack.
- `attachToObservables` subscribes to the progress, warning, status and error events with the event API, before the run starts.
- `startAndPresentResult` starts the run, waits up to 5 minutes for the result and prints it.

## Output and what to look for

The run prints the status and progress lines, then the result. The results were the same in two test runs. The status lines name the four algorithms of the scheme in their order:

```text
StatusEvent(OPTIMIZATION_ALGO_INITIALIZED, SmartConstructionAlgo)
StatusEvent(OPTIMIZATION_ALGO_INITIALIZED, SmartSimulatedAnnealingAlgo)
StatusEvent(OPTIMIZATION_ALGO_INITIALIZED, SmartSimulatedAnnealingAlgo)
StatusEvent(OPTIMIZATION_ALGO_INITIALIZED, GeneticEvolution)
```

Each of the four algorithms prints 100 progress lines with its name after "AL". On this small plan the three optimization phases do not change the result. In the test runs every progress line of the three phases shows 2835.64, the cost of the plan that the construction hands over, and the result is the same as that of the first optimization example with the default pipeline: one route per day, and on May 7 Nuernberg and Heilbronn, a route of 653.18 minutes that exceeds the 9 hours of Jack's maximal working time and ends at 18:53:10. The result reports it as a MAXTIME violation:

```text
[ CONSTRAINTVIOLATION/MAXTIME/EXCEEDED/ Overtime [h]: 1.8863266666666654 Value: 1.8863266666666654 ]
```

The AutoFilter that the genetic phase carries filters nothing in this run, because the example sets neither `JOpt.AutoFilter` nor `JOpt.SelectiveAutoFilter`.

## Optimization schemes and the execution pipeline

JOpt.TourOptimizer separates *what you want to optimize* (nodes/resources/constraints/cost model) from *how the solver runs* (construction strategies, heuristic phases, post-steps).

The **Optimization Scheme** is the architectural place where you can explicitly define:

- which **construction algorithm** generates the initial solution,
- which **heuristic algorithms** are executed and in which order,
- per-phase configuration overrides (e.g., SA iterations),
- per-phase feature toggles such as **AutoFilter**.

This is the recommended approach when you want fixed, transparent solver pipelines that can be explained, benchmarked, and tuned.

## Key concept: Scheme-driven pipeline

An optimization run generally has two layers:

### Construction phase (initial solution)
A construction algorithm creates a feasible (or near-feasible) starting entity.

In the example, the construction algorithm is explicitly selected:

```java
ConstructionOptimizationAlgorithmConfig constructionAlgo =
    ConstructionOptimizationAlgorithmConfig.builder()
        .algorithm(ConstructionOptimizationAlgorithm.SMART_CONSTR_ALGO)
        .build();
```

Notes:
- If you do not provide a construction config, the default construction algorithm is used; the [clustering construction](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/clustering_construction) feature guide describes it.
- Construction choice can substantially affect runtime and initial quality, especially for large instances.

### Heuristic phase(s) (improvement search)
One or more heuristic algorithms improve the constructed solution.

The example builds a list of heuristic phases:

- Simulated Annealing (quick)
- Simulated Annealing (longer, property-driven)
- Genetic Evolution

Each phase can be configured independently.

## Heuristic phase configuration (what the example demonstrates)

The example configures three heuristic algorithm configs:

### Phase 0: simulated annealing (explicit iteration override)
```java
HeuristicOptimizationAlgorithmConfig heuristicAlgoZero =
    HeuristicOptimizationAlgorithmConfig.builder()
        .algorithm(OptimizationAlgorithmConfig.SMART_SIMULATED_ANNEALING_ALGO)
        .simulatedAnnealingOverrideNumIterationsValue(1000)
        .hasAutoFilter(false)
        .build();
```

What this means:
- run SA with **1000 iterations** regardless of global properties,
- disable AutoFilter for this phase.

### Phase 1: simulated annealing (iterations from properties)
```java
HeuristicOptimizationAlgorithmConfig heuristicAlgoOne =
    HeuristicOptimizationAlgorithmConfig.builder()
        .algorithm(OptimizationAlgorithmConfig.SMART_SIMULATED_ANNEALING_ALGO)
        .hasAutoFilter(false)
        .build();
```

Here, the number of iterations comes from the property:

- `JOpt.Algorithm.PreOptimization.SA.NumIterations`

### Phase 2: genetic evolution (AutoFilter attached)
```java
HeuristicOptimizationAlgorithmConfig heuristicAlgoTwo =
    HeuristicOptimizationAlgorithmConfig.builder()
        .algorithm(OptimizationAlgorithmConfig.GENETIC_EVOLUTION_ALGO)
        .hasAutoFilter(true)
        .build();
```

What this means:
- run the genetic phase after the two SA phases,
- attach the AutoFilter to this phase explicitly; the filter acts only when the property `JOpt.AutoFilter` or `JOpt.SelectiveAutoFilter` is true, and the example sets neither.

## The "AutoFilter switch" is scheme-level authoritative

The example highlights an important rule:

> If `hasAutoFilter(false)` is set on a heuristic config, AutoFilter is disabled **even if properties enable it**.

With `hasAutoFilter(true)`, the properties decide whether the attached filter acts.

This is a production control:
- properties might be used broadly across deployments,
- but the scheme defines the authoritative execution pipeline for that specific run configuration.

Practical use cases:
- disable AutoFilter in very small instances where overhead is unnecessary,
- enable AutoFilter only for the heavy phases,
- or disable it in a final "polishing phase" to avoid last-mile filtering effects.

## Building the scheme

After selecting the construction config and assembling the heuristic configs list, the example constructs a `DefaultOptimizationScheme`:

```java
IOptimizationScheme scheme =
    new DefaultOptimizationScheme(
        opti, Optional.of(constructionAlgo), Optional.of(heuristicAlgoConfigs));

opti.setOptimizationScheme(scheme);
```

### Important behavior when optionals are empty
The example comments summarize the defaults:

- If construction config is not present: the default construction algorithm is used.
- If heuristic configs are not present: the default heuristic algorithms are used.
- If heuristic configs are present but the list is empty: **no heuristic algorithms** run (construction-only run).

This is very useful for:
- debugging construction quality,
- running "quick feasibility" generation,
- benchmarking individual phases.

## Properties vs scheme configuration (how they interact)

The example still sets properties, e.g.:

- `JOptExitCondition.JOptGenerationCount`
- `JOpt.Algorithm.PreOptimization.SA.NumIterations`

Other keys, such as `JOpt.Algorithm.PreOptimization.SA.NumRepetitions` and `JOpt.NumCPUCores`, keep their defaults in this example; the [property reference](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties) lists them.

Interpretation:
- **properties** remain the baseline configuration source,
- **scheme configs** selectively override phase-specific settings.

Recommended strategy:
- keep global knobs in properties (cores, global iteration defaults),
- pin your execution pipeline in the scheme,
- override only when you need phase-local behavior.

## Production guidance: how to choose a pipeline

### Pattern A: fast "good-enough" results (interactive UI)
- construction algo tuned for speed,
- one short SA phase,
- early stop via convergence or strict time limit.

### Pattern B: batch planning (nightly runs)
- construction algo tuned for quality,
- SA (medium) + Genetic Evolution (long),
- AutoFilter enabled for heavy phases,
- progress reporting enabled for telemetry.

### Pattern C: debugging and explainability
- construction-only run (disable heuristics) to inspect baseline,
- then enable one heuristic at a time,
- measure KPI improvements per phase.

### Pattern D: "Do not change too much" (acceptance-sensitive rollouts)
- warm start (custom solution) + short heuristic,
- optionally disable certain aggressive phases,
- combine with comparison tooling to show deltas.

## Why this architecture matters

Explicit scheme pipelines provide:

- **repeatability**: the same pipeline behaves consistently across runs
- **benchmarkability**: you can compare phase contributions
- **tuning clarity**: local overrides are explicit, not hidden in property files
- **operational safety**: you can restrict which phases run in which environments
- **acceptance control**: control aggressiveness and runtime for end users

## Summary

- The Optimization Scheme defines the **execution pipeline**: construction algorithm + ordered heuristic phases.
- Each heuristic phase can override parameters (e.g., SA iterations) and toggle features such as AutoFilter.
- Scheme settings are authoritative for the pipeline; properties provide baseline defaults.
- This is the recommended approach for transparent and tunable optimization runs in production.

## Run it

Run the `main(String[] args)` method of `OptimizationSchemeAlgorithmSelectionExample` from your IDE, with the repository imported as a Maven project. You need a JDK in the version that the examples pom sets. You need no license file: without one, the example uses the public evaluation license (see the [license guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/license)).

## Related

- [Clustering construction](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/clustering_construction): the feature guide of the default construction algorithm.
- [AutoFilter feature guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/autofilter): how the filter decides and the properties that switch it on.
- [Optimization properties](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties): the property reference, with the generation count, the SA iterations and their defaults.
- [Custom default properties of an optimization scheme](OptimizationSchemeCustomDefaultProperties.md): the other optimization scheme example, default properties that a scheme brings along.
- [First optimization example](../../basic/firstoptimization_01/FirstOptimizationExample.md): the same data with the default pipeline.
- [Reactive events and streams](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/reactive_events): the feature guide of the event API that `attachToObservables` uses.
- [Recommended ways to run an optimization](../../basic/recommendedimplementation_02/RecommendedImplementation.md): the recommended run pattern, a synchronous run and the legacy callback methods.
- [Expert examples](../README.md): the overview of all Expert examples.

---

Added: 2026-02-17 (JOpt core 7.5.3-j17) | Last updated: 2026-10-21
