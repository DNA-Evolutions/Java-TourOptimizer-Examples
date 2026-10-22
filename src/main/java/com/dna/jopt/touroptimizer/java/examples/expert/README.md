# Expert examples

The Expert examples focus on manipulating how the optimizer behaves: the algorithm pipeline, convergence criteria, injected assessors, dynamic connections, warm-start entities, and failure handling. They are for Java developers who work with JOpt.TourOptimizer for Java and know the [Basic examples](../basic/README.md) and the [Advanced examples](../advanced/README.md). If you are new to JOpt.TourOptimizer, start with the basic and advanced examples first, then return here once you are comfortable with the core modeling concepts.

**Danger zone (by design):** these features can fundamentally change performance characteristics and, if misused, can produce invalid problem definitions or misleading results. Use them when you have a concrete requirement and you can validate the behavior with tests and KPIs.

The table below lists the packages and recommended entry points. Each package contains one or more runnable examples plus one or two dedicated Markdown companion documents (two in `openassessor` and `optimizationscheme`) that explain the architecture and practical usage. The sources are in the [expert folder on GitHub](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert), one package per topic. Each row names the key documents / references of a package: its page and, where the Hub has one, its feature guide.

| Example | What you learn | Feature guide |
|---|---|---|
| [Custom backup connector for missing distances and driving times](backupconnector/CustomNodeBackUpConnector.md) (`backupconnector`) | BackupConnector and custom distance/time fallback logic: a backup connector of your own fills in the connections that no node connector provides, with flat-earth distances times 1.2 in `CustomNodeBackUpConnectorExample` and haversine distances times 1.2 in `CustomNodeBackUpConnectorHaversineExample`. The Basic page [Custom distances and driving times with a node connector](../basic/connection_04/ExternalNodeConnection.md) shows the connections that you set yourself. | [Backup connector](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/backupconnector) |
| [Build information of the JOpt core](buildinfo/ExtractBuildInfo.md) (`buildinfo`) | Read build metadata from the library: `ExtractBuildInfoExample` prints the version number and the build time of the JOpt core, for a service, a batch job or a support case. | |
| [Compare an optimization result with a modified result](compareresult/CompareResult.md) (`compareresult`) | Comparison tool for debugging and end-user acceptance: `CompareResultsWithNodeExchangeExample` and `CompareResultsWithNodesMoveExample` swap two nodes or move nodes the way a planner would, and compare the changed result with the optimized one. | [Comparison tool](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/comparison_tool) |
| [Time-dependent driving times with a connection store](connectionstore/ConnectionStore.md) (`connectionstore`) | Time-dependent (dynamic) connection store for distance/time: `ConnectionStoreExample` gives every connection between two places a weekday and a weekend traffic profile, so the same drive takes longer in a rush hour. | |
| [Load a JSON snapshot by hand for a custom solution](createcustomsolutionfromjson/CreateCustomSolutionFromJSON.md) (`createcustomsolutionfromjson`) | Load a stored optimization snapshot and take over its elements by hand, the starting point for a warm start of your own: `CreateCustomSolutionFromJSONExample` takes over by hand the elements of the snapshot that `SaveOptimizationToJsonExample` writes, the manual way of `LoadOptimizationToFreshRunExample`. | |
| [Custom solutions as a warm start](customsolution/CustomSolution.md) (`customsolution`) | Warm-start: inject your own initial entity/plan. `CustomSolutionExample` builds two routes by hand as the initial entity and lets the optimizer improve them; `CustomSolutionWithAdditionalNodesAndResourcesExample` adds nodes and resources to such a custom solution before the search starts. | |
| [Stop a run early with a cost converger](externalcostconvergence/CustomCostConvergence.md) (`externalcostconvergence`) | Graceful early-stop when a chosen KPI converges: `CustomCostConvergenceExample` stops the run once six progress events in a row show the same driven distance of the best solution (the first sets the distance, the next five repeat it). | [Reactive events and streams](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/reactive_events) |
| [Flexible start time (positive and negative FlexTime)](flextime/Flextime.md) (`flextime`) | Flexible route start time windows (positive and negative): `PositiveFlexTimeExample` lets a route start up to 2 hours after the working hours begin, so that it does not start with waiting; `NegativeFlexTimeExample` lets it start up to 2 hours before them, so that the resource arrives when its first node opens. | [Flexible start time](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/flextime), [Special features: flexible start time](https://www.dna-evolutions.com/docs/learn-and-explore/special/special_features#flexible-start-time-positive-and-negative) |
| [Custom node-level restrictions with the Open Assessor](openassessor/OpenAssessorNodeLevel.md) (`openassessor`) | Inject custom restrictions/costs on node level: a deadline of 11:00 for nodes whose id starts with "M" in `CustomNodeLevelRestrictionExample`, a cost for each minute after the opening start of a node, weighted by its importance, in `CustomNodeLevelPriorityEarlyRestrictionExample`, and a cost when a more important node follows a less important one in `CustomNodeLevelImportanceOrderExample`. | [Open Assessor](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/open_assessor), [Special features: Open Assessor](https://www.dna-evolutions.com/docs/learn-and-explore/special/special_features#open-assessor) |
| [Custom route-level restrictions with the Open Assessor](openassessor/OpenAssessorRouteLevel.md) (`openassessor`) | Inject custom restrictions/costs on route level: `CustomRouteLevelOddNumberOfElementsRestrictionExample` gives a route with an even number of nodes a penalty, an artificial rule that shows where a route-level rule goes. | [Open Assessor](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/open_assessor) |
| [Algorithm selection with an optimization scheme](optimizationscheme/OptimizationSchemeAlgorithmSelection.md) (`optimizationscheme`) | Define the algorithm pipeline: `OptimizationSchemeAlgorithmSelectionExample` chooses the construction algorithm and the optimization phases of a run, in their order and each with its own settings. | |
| [Custom default properties of an optimization scheme](optimizationscheme/OptimizationSchemeCustomDefaultProperties.md) (`optimizationscheme`) | Define default properties of a scheme: `OptimizationSchemeCustomDefaultPropertiesExample` gives a scheme properties of its own, which apply to every optimization that uses it, while the properties of the optimization override them key by key. | [Optimization properties](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties) |
| [Optional nodes as reload and drop stops](optionalnode/OptionalNode.md) (`optionalnode`) | Optional nodes (visited only if beneficial / necessary), with the array load model: `ReloadBaseExample` reloads an empty delivery truck at optional nodes, `ReloadBaseWithUnloadAllExample` drops goods at optional nodes, one of them unload-all; in the common context of pickup and delivery (PND). | [Pickup and delivery](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/pickup_and_delivery), [Special features: optional nodes](https://www.dna-evolutions.com/docs/learn-and-explore/special/special_features#optional-nodes) |
| [Read out the default properties](readoutproperties/ReadOutDefaultProperties.md) (`readoutproperties`) | Enumerate the full property catalog and defaults: `ReadOutDefaultPropertiesExample` prints each property with its category, description, key, default value and allowed values. | [Optimization properties](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties) |
| [Uncaught exception handling in a run and in event subscriptions](uncaughtexception/UncaughtExceptionHandling.md) (`uncaughtexception`) | Failure propagation for uncaught exceptions: the three classes throw on purpose, in a custom restriction, in a legacy callback method and in a subscription with an error consumer, and a custom handler ends the wait for the result with the exception. | [Reactive events and streams](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/reactive_events) |

## Run the examples

**Option A: run from your IDE (recommended).** Import the repository as a Maven project and run the `main(...)` method of the chosen example class under `.../examples/expert/...`. You need a JDK in the version that the examples pom sets.

**Option B: use the sandbox (Docker).** The repository provides a browser-based sandbox, described in the [repo-level README](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/README.md). The [sandbox quickstart](https://www.dna-evolutions.com/docs/getting-started/quickstart/jopt_sandboxes_quickstart) describes the sandbox images.

**The license.** Without a license file, the examples use the public evaluation license through `ExampleLicenseHelper`; `ExtractBuildInfoExample` and `ReadOutDefaultPropertiesExample` run no optimization and set no license. To use your own license, save it as `src/main/resources/license/YOUR_LICENSE.jli`; [Set a license from a file](../basic/setlicense_08/SetLicense.md) explains how, and the [license guide: element limits](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/license#free-license) lists what each license allows.

**Events and results.** Every example that runs an optimization subscribes to the progress, warning, status and error events of the run before it starts and waits for the result with a timeout. `UncaughtExceptionHandlingRxJavaExample` prints the progress through the overridden legacy callback method `onProgress`, which is its fault source, and the three uncaught exception examples end their runs with an exception on purpose and catch it. `ExtractBuildInfoExample` and `ReadOutDefaultPropertiesExample` run no optimization. [Recommended ways to run an optimization](../basic/recommendedimplementation_02/RecommendedImplementation.md) explains this pattern and the legacy callback methods.

## Choosing an example

### Choose a goal first

The expert section is easiest to approach goal-first. Typical goals include:

- **Runtime control**: stop earlier when improvements stall (`externalcostconvergence`), pick a different algorithm pipeline (`optimizationscheme`), or tune performance mode (the Advanced example [Performance mode examples](../advanced/performancemode/PerformanceMode.md)).
- **Custom business rules**: add restrictions/cost contributions that are specific to your domain and must be explainable (`openassessor`).
- **Realistic travel times**: plug in time-dependent connections (`connectionstore`) or a custom backup connector strategy (`backupconnector`).
- **Plan continuity**: start from an existing plan (warm start) and re-optimize incrementally as the world changes (`customsolution`, `createcustomsolutionfromjson`).
- **Operational robustness**: ensure uncaught exceptions are handled and surfaced to callers through the result future (`uncaughtexception`).

### Suggested exploration paths

#### Path 1: Custom business rules (customer-specific restrictions)

1. `openassessor` (route-level and node-level): implement rules, costs, and explainability
2. `compareresult`: show deltas to planners and debug "why"
3. `readoutproperties`: discover tuning knobs and verify defaults

#### Path 2: Runtime and pipeline control

1. `optimizationscheme`: select algorithms and order them explicitly
2. `externalcostconvergence`: stop when your KPI stabilizes (adaptive runtime control)
3. `flextime`: improve feasibility/realism without brittle tuning

#### Path 3: Realistic travel times and robust integration

1. `connectionstore`: time-dependent travel (rush hour / weekend effects)
2. `backupconnector`: define fallback distance/time strategy when no external connection is provided
3. `uncaughtexception`: failure propagation through the result future in asynchronous and reactive environments

#### Path 4: Plan continuity and incremental re-optimization

1. `customsolution`: inject a warm-start solution (`IEntity`) to continue from an existing plan
2. `createcustomsolutionfromjson`: load a stored optimization snapshot and take over its elements by hand, the starting point for a warm start of your own

#### Path 5: Operational stopovers

1. `optionalnode`: add operational stopovers (reload/unload) the solver may choose to use

## How to use this section effectively

### Keep feasibility architectural

A recurring source of production issues is mixing up feasibility with optimization:

- **Hard constraints must be satisfied by architecture**, i.e., by correct modeling constructs and hard feasibility logic. Pillar nodes are such a construct: a pillar that cannot be reached stays unassigned and is never served late.
- **Costs/weights are for optimizing within the feasible space**, not for "buying" infeasibility with a very high penalty.
- Many rules of JOpt are pseudo constraints: a violated capacity, relation or time window adds a violation and a cost weighted by a property such as `JOptWeight.Capacity`, so the weight decides how strictly the plan keeps the rule.

If a rule must always hold, implement it as a proper hard constraint / structural feature (or via a suitable architectural mechanism). Reserve penalties for preferences and acceptance shaping, and tune the weights of the pseudo constraints for the rules that may bend.

### Validate every expert feature with a comparison or KPI harness

When you introduce an expert feature, validate with:

- a baseline run vs modified run comparison,
- reproducible property snapshots,
- and at least one business KPI (distance, lateness, overtime, CO2, utilization, etc.).

The **CompareResult** tooling is particularly useful for demonstrating improvements and increasing end-user acceptance.

## Common production pitfalls (and how to avoid them)

1. **Over-tuning properties too early**: start with defaults; change as few knobs as possible; validate each change.
2. **Using huge penalties for hard rules**: model mandatory constraints architecturally; penalties are for preferences and for the weights of pseudo constraints.
3. **Unbounded event logging**: event streams are not durable storage; forward important events to telemetry.
4. **Ignoring algorithm phase identity**: when you filter by algorithm id (e.g., convergers), ensure caller ids match.
5. **ID collisions in warm starts**: node/resource IDs must be stable and unique when you load/merge plans.

## Contributing and maintaining documentation

When adding new expert examples, consider adding:

- a short runnable Java example,
- a Markdown companion document next to the example,
- and cross-links from this README (the package list above, in the table).

This structure keeps the example source concise while still providing a copy-paste friendly explanation layer.

## Related

- [Basic examples](../basic/README.md): the overview of all Basic examples.
- [Advanced examples](../advanced/README.md): the overview of all Advanced examples.
- [Expert examples root](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert): the folder of the Expert examples and their sources on GitHub.
- [Feature guides](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides): the guides to the features that these examples show.
- [Special features](https://www.dna-evolutions.com/docs/learn-and-explore/special/special_features): the Special Features overview.
- [AutoFilter](https://www.dna-evolutions.com/docs/learn-and-explore/special/special_features#autofilter): the AutoFilter section of the special features.
- [Backup connector](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/backupconnector): the feature guide of the BackupConnector.
- [Performance Mode](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/performance_mode): the feature guide of performance mode.
- [Zone crossing](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/zonecrossing): the feature guide of zone crossing penalization.
- [Optimization properties](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties): the Optimization Properties overview, the property reference.
- [License guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/license): the license JSON, the element limits and how to set a license.
- [Documentation Hub introduction](https://www.dna-evolutions.com/docs/getting-started/home/introduction): the introduction to the documentation.

---

Added: 2020-06-03 (JOpt core 7.4.4) | Last updated: 2026-10-23
