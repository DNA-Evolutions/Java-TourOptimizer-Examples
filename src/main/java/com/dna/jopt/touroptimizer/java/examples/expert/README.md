# Expert Examples

The **expert** examples focus on manipulating *how the optimizer behaves* (algorithm pipeline, convergence criteria, injected assessors, dynamic connections, warm-start entities, and failure handling).

> **Danger zone (by design):** these features can fundamentally change performance characteristics and, if misused, can produce invalid problem definitions or misleading results. Use them when you have a concrete requirement and you can validate the behavior with tests and KPIs.

If you are new to JOpt.TourOptimizer, start with the **basic** and **advanced** examples first, then return here once you are comfortable with the core modeling concepts.

- Expert examples root: [https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert)
- Special Features overview: [https://www.dna-evolutions.com/docs/learn-and-explore/special/special_features](https://www.dna-evolutions.com/docs/learn-and-explore/special/special_features)
- Optimization Properties overview: [https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties)

## How to use this section effectively

### Choose a goal first

The expert section is easiest to approach goal-first. Typical goals include:

- **Runtime control**: stop earlier when improvements stall, pick a different algorithm pipeline, or tune performance mode.
- **Custom business rules**: add restrictions/cost contributions that are specific to your domain and must be explainable.
- **Realistic travel times**: plug in time-dependent connections or a custom backup connector strategy.
- **Plan continuity**: start from an existing plan (warm start) and re-optimize incrementally as the world changes.
- **Operational robustness**: ensure uncaught exceptions are handled deterministically and surfaced to callers.

### Keep feasibility architectural

A recurring source of production issues is mixing up feasibility with optimization:

- **Hard constraints must be satisfied by architecture**, i.e., by correct modeling constructs and hard feasibility logic.
- **Costs/weights are for optimizing within the feasible space**, not for "buying" infeasibility with a very high penalty.

If a rule must always hold, implement it as a proper hard constraint / structural feature (or via a suitable architectural mechanism). Reserve penalties for preferences and acceptance shaping.

### Validate every expert feature with a comparison or KPI harness

When you introduce an expert feature, validate with:

- a baseline run vs modified run comparison,
- reproducible property snapshots,
- and at least one business KPI (distance, lateness, overtime, CO₂, utilization, etc.).

The **CompareResult** tooling is particularly useful for demonstrating improvements and increasing end-user acceptance.

## Packages and recommended entry points

Each package below contains one or more runnable examples plus a dedicated Markdown companion document that explains the architecture and practical usage.

> Links are absolute so they remain usable when this README is viewed in different environments.

### `backupconnector` — BackupConnector and custom distance/time fallback logic

- Package: [https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/backupconnector](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/backupconnector)
- Key documents / references:
  - [CustomNodeBackUpConnector.md](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/backupconnector/CustomNodeBackUpConnector.md)
  - [ExternalNodeConnection.md](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic/externalnodeconnection/ExternalNodeConnection.md)

### `buildinfo` — Read build/runtime metadata from the library

- Package: [https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/buildinfo](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/buildinfo)
- Key documents / references:
  - [ExtractBuildInfo.md](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/buildinfo/ExtractBuildInfo.md)

### `compareresult` — Comparison tool for debugging and end-user acceptance

- Package: [https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/compareresult](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/compareresult)
- Key documents / references:
  - [CompareResult.md](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/compareresult/CompareResult.md)
  - [Comparison Tool docs](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/comparison_tool)

### `connectionstore` — Time-dependent (dynamic) connection store for distance/time

- Package: [https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/connectionstore](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/connectionstore)
- Key documents / references:
  - [ConenctionStore.md](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/connectionstore/ConenctionStore.md)

### `customsolution` — Warm-start: inject your own initial entity/plan

- Package: [https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/customsolution](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/customsolution)
- Key documents / references:
  - [CustomSolution.md](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/customsolution/CustomSolution.md)
  - [CreateCustomSolutionFromJSON.md](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/createcustomsolutionfromjson/CreateCustomSolutionFromJSON.md)

### `externalcostconvergence` — Graceful early-stop when a chosen KPI converges

- Package: [https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/externalcostconvergence](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/externalcostconvergence)
- Key documents / references:
  - [CustomCostConvergence.md](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/externalcostconvergence/CustomCostConvergence.md)

### `flextime` — Flexible route start time windows (positive and negative)

- Package: [https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/flextime](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/flextime)
- Key documents / references:
  - [Flextime.md](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/flextime/Flextime.md)

### `openassessor` — Inject custom restrictions/costs on node and route level

- Package: [https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/openassessor](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/openassessor)
- Key documents / references:
  - [OpenAssessorRouteLevel.md](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/openassessor/routelevel/custom/OpenAssessorRouteLevel.md)
  - [OpenAssessorNodeLevel.md](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/openassessor/nodelevel/OpenAssessorNodeLevel.md)
  - [Open Assessor docs](https://www.dna-evolutions.com/docs/learn-and-explore/special/special_features#open-assessor)

### `optimizationscheme` — Define the algorithm pipeline and default properties

- Package: [https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/optimizationscheme](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/optimizationscheme)
- Key documents / references:
  - [OptimizationSchemeAlgorithmSelection.md](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/optimizationscheme/selectalgorithms/OptimizationSchemeAlgorithmSelection.md)
  - [OptimizationSchemeCustomDefaultProperties.md](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/optimizationscheme/customdefaultproperties/OptimizationSchemeCustomDefaultProperties.md)

### `optionalnode` — Optional nodes (visited only if beneficial / necessary)

- Package: [https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/optionalnode](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/optionalnode)
- Key documents / references:
  - [OptionalNode.md](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/optionalnode/OptionalNode.md)
  - [PND docs (common context)](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/pickup_and_delivery)

### `readoutproperties` — Enumerate the full property catalog and defaults

- Package: [https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/readoutproperties](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/readoutproperties)
- Key documents / references:
  - [ReadOutDefaultProperties.md](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/readoutproperties/ReadOutDefaultProperties.md)
  - [Optimization Properties docs](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties)

### `uncaughtexception` — Deterministic failure propagation for uncaught exceptions

- Package: [https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/uncaughtexception](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/uncaughtexception)
- Key documents / references:
  - [UncaughtExceptionHandling.md](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/uncaughtexception/UncaughtExceptionHandling.md)

## Suggested exploration paths

### Path 1 — Custom business rules (customer-specific restrictions)

1. `openassessor` (route-level and node-level) — implement rules, costs, and explainability
2. `compareresult` — show deltas to planners and debug "why"
3. `readoutproperties` — discover tuning knobs and verify defaults

### Path 2 — Runtime and pipeline control

1. `optimizationscheme` — select algorithms and order them explicitly
2. `externalcostconvergence` — stop when your KPI stabilizes (adaptive runtime control)
3. `flextime` — improve feasibility/realism without brittle tuning

### Path 3 — Realistic travel times and robust integration

1. `connectionstore` — time-dependent travel (rush hour / weekend effects)
2. `backupconnector` — define fallback distance/time strategy when no external connection is provided
3. `uncaughtexception` — deterministic failure propagation in asynchronous and reactive environments

### Path 4 — Plan continuity and incremental re-optimization

1. `customsolution` — inject a warm-start solution (`IEntity`) to continue from an existing plan
2. `createcustomsolutionfromjson` — load a stored optimization snapshot and rebuild a warm start
3. `optionalnode` — add operational stopovers (reload/unload) the solver may choose to use

## Additional recommended reading

- AutoFilter: [https://www.dna-evolutions.com/docs/learn-and-explore/special/special_features#autofilter](https://www.dna-evolutions.com/docs/learn-and-explore/special/special_features#autofilter)
- BackupConnector: [https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/backupconnector](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/backupconnector)
- Performance Mode: [https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/performance_mode](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/performance_mode)
- Zone crossing: [https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/zonecrossing](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/zonecrossing)

## Common production pitfalls (and how to avoid them)

1. **Over-tuning properties too early**: start with defaults; change as few knobs as possible; validate each change.
2. **Using huge penalties for hard rules**: model mandatory constraints architecturally; penalties are for preferences.
3. **Unbounded event logging**: event streams are not durable storage; forward important events to telemetry.
4. **Ignoring algorithm phase identity**: when you filter by algorithm id (e.g., convergers), ensure caller ids match.
5. **ID collisions in warm starts**: node/resource IDs must be stable and unique when you load/merge plans.

## Contributing and maintaining documentation

When adding new expert examples, consider adding:

- a short runnable Java example,
- a Markdown companion document next to the example,
- and cross-links from this README (package list above).

This structure keeps the example source concise while still providing a copy-paste friendly explanation layer.
