# Optimization Scheme — Algorithm Selection and Execution Pipeline

JOpt.TourOptimizer separates *what you want to optimize* (nodes/resources/constraints/cost model) from *how the solver runs* (construction strategies, heuristic phases, post-steps).

The **Optimization Scheme** is the architectural place where you can explicitly define:

- which **construction algorithm** generates the initial solution,
- which **heuristic algorithms** are executed and in which order,
- per-phase configuration overrides (e.g., SA iterations),
- per-phase feature toggles such as **AutoFilter**.

This is the recommended approach when you want deterministic, transparent solver pipelines that can be explained, benchmarked, and tuned.

---

## Reference (example)

- [OptimizationSchemeAlgorithmSelectionExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/optimizationscheme/selectalgorithms/OptimizationSchemeAlgorithmSelectionExample.java) 

---

## Key concept: Scheme-driven pipeline

An optimization run generally has two layers:

### 1) Construction phase (initial solution)
A construction algorithm creates a feasible (or near-feasible) starting entity.

In the example, the construction algorithm is explicitly selected:

```java
ConstructionOptimizationAlgorithmConfig constructionAlgo =
  ConstructionOptimizationAlgorithmConfig.builder()
    .algorithm(ConstructionOptimizationAlgorithm.SEQUENTIAL_SPACE_SAVINGS_ALGO)
    .build();
```

Notes:
- If you do not provide a construction config, the default construction algorithm is used.
- Construction choice can substantially affect runtime and initial quality, especially for large instances.

### 2) Heuristic phase(s) (improvement search)
One or more heuristic algorithms improve the constructed solution.

The example builds a list of heuristic phases:

- Simulated Annealing (quick)
- Simulated Annealing (longer, property-driven)
- Genetic Evolution

Each phase can be configured independently.

---

## Heuristic phase configuration (what the example demonstrates)

The example configures three heuristic algorithm configs:

### Phase 0 — Simulated Annealing (explicit iteration override)
```java
HeuristicOptimizationAlgorithmConfig heuristicAlgoZero =
  HeuristicOptimizationAlgorithmConfig.builder()
    .algorithm(OptimizationAlgorithmConfig.SIMULATED_ANNEALING_ALGO)
    .simulatedAnnealingOverrideNumIterationsValue(1000)
    .hasAutoFilter(false)
    .build();
```

What this means:
- run SA with **1000 iterations** regardless of global properties,
- disable AutoFilter for this phase.

### Phase 1 — Simulated Annealing (iterations from properties)
```java
HeuristicOptimizationAlgorithmConfig heuristicAlgoOne =
  HeuristicOptimizationAlgorithmConfig.builder()
    .algorithm(OptimizationAlgorithmConfig.SIMULATED_ANNEALING_ALGO)
    .hasAutoFilter(false)
    .build();
```

Here, the number of iterations comes from the property:

- `JOpt.Algorithm.PreOptimization.SA.NumIterations`

### Phase 2 — Genetic Evolution (AutoFilter enabled)
```java
HeuristicOptimizationAlgorithmConfig heuristicAlgoTwo =
  HeuristicOptimizationAlgorithmConfig.builder()
    .algorithm(OptimizationAlgorithmConfig.GENETIC_EVOLUTION_ALGO)
    .hasAutoFilter(true)
    .build();
```

What this means:
- run the genetic phase after the two SA phases,
- explicitly enable AutoFilter during this phase.

---

## The “AutoFilter switch” is scheme-level authoritative

The example highlights an important rule:

> If `hasAutoFilter(false)` is set on a heuristic config, AutoFilter is disabled **even if properties enable it**.

This is a crucial production control:
- properties might be used broadly across deployments,
- but the scheme defines the authoritative execution pipeline for that specific run configuration.

Practical use cases:
- disable AutoFilter in very small instances where overhead is unnecessary,
- enable AutoFilter only for the heavy phases,
- or disable it in a final “polishing phase” to avoid last-mile filtering effects.

---

## Building the scheme

After selecting the construction config and assembling the heuristic configs list, the example constructs a `DefaultOptimizationScheme`:

```java
IOptimizationScheme myScheme =
  new DefaultOptimizationScheme(opti, Optional.of(constructionAlgo), Optional.of(myHeursticAlgoConfigs));

opti.setOptimizationScheme(myScheme);
```

### Important behavior when optionals are empty
The example comments summarize the defaults:

- If construction config is not present → the default construction algorithm is used.
- If heuristic configs are not present → the default heuristic algorithms are used.
- If heuristic configs are present but the list is empty → **no heuristic algorithms** run (construction-only run).

This is very useful for:
- debugging construction quality,
- running “quick feasibility” generation,
- benchmarking individual phases.

---

## Properties vs scheme configuration (how they interact)

The example still sets properties, e.g.:

- `JOptExitCondition.JOptGenerationCount`
- `JOpt.Algorithm.PreOptimization.SA.NumIterations`
- `JOpt.Algorithm.PreOptimization.SA.NumRepetions`
- `JOpt.NumCPUCores`

Interpretation:
- **properties** remain the baseline configuration source,
- **scheme configs** selectively override phase-specific settings.

Recommended strategy:
- keep global knobs in properties (cores, global iteration defaults),
- pin your execution pipeline in the scheme,
- override only when you need phase-local behavior.

---

## Production guidance: how to choose a pipeline

### Pattern A — Fast “good-enough” results (interactive UI)
- construction algo tuned for speed,
- one short SA phase,
- early stop via convergence or strict time limit.

### Pattern B — Batch planning (nightly runs)
- construction algo tuned for quality,
- SA (medium) + Genetic Evolution (long),
- AutoFilter enabled for heavy phases,
- progress reporting enabled for telemetry.

### Pattern C — Debugging and explainability
- construction-only run (disable heuristics) to inspect baseline,
- then enable one heuristic at a time,
- measure KPI improvements per phase.

### Pattern D — “Do not change too much” (acceptance-sensitive rollouts)
- warm start (custom solution) + short heuristic,
- optionally disable certain aggressive phases,
- combine with comparison tooling to show deltas.

---

## Why this architecture matters

Explicit scheme pipelines provide:

- **repeatability**: the same pipeline behaves consistently across runs
- **benchmarkability**: you can compare phase contributions
- **tuning clarity**: local overrides are explicit, not hidden in property files
- **operational safety**: you can restrict which phases run in which environments
- **acceptance control**: control aggressiveness and runtime for end users

---

## Summary

- The Optimization Scheme defines the **execution pipeline**: construction algorithm + ordered heuristic phases.
- Each heuristic phase can override parameters (e.g., SA iterations) and toggle features such as AutoFilter.
- Scheme settings are authoritative for the pipeline; properties provide baseline defaults.
- This is the recommended approach for production-grade, transparent, and tunable optimization runs.
