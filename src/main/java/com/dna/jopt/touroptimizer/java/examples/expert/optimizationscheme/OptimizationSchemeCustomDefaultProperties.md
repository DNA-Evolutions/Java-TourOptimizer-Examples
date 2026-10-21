# Custom default properties of an optimization scheme

`OptimizationSchemeCustomDefaultPropertiesExample` gives an optimization scheme properties of its own, which apply to every optimization that uses the scheme, while the properties of the optimization override them key by key. The example sets the distance weight in the scheme and a generation count in both places, and prints which value the run uses. The page is for Java developers who keep solver defaults in one place for many jobs; it uses the data of the [First optimization example](../../basic/firstoptimization_01/FirstOptimizationExample.md).

## What the example shows

- **Custom default properties:** a `Properties` object set with `setCustomDefaultProperties` on a `DefaultOptimizationScheme`, before the scheme goes onto the optimization with `setOptimizationScheme`.
- **The effect of a default:** the scheme sets `JOptWeight.TotalDistance` to 10000.0, so distance becomes by far the most important cost.
- **The precedence:** the scheme and the optimization (with `addElement(Properties)`) both set `JOptExitCondition.JOptGenerationCount`, to 2000 and to 1000; the value of the optimization wins.
- **The merged set:** at the start the scheme merges its custom defaults into the properties of the optimization; `printEffectiveProperties` reads them with `getUserProperties()` after the run.

## Scenario

The dates and times are sample values, and so are the coordinates in Germany. The data is that of the first optimization example. `addProperties` sets `JOptExitCondition.JOptGenerationCount` to 1000 and `JOpt.Algorithm.PreOptimization.SA.NumIterations` to 100000. Both are below their defaults, which keeps the run short. The custom defaults of the scheme are sample values: `JOptWeight.TotalDistance` 10000.0 and `JOptExitCondition.JOptGenerationCount` 2000.

- **Resource:** `Jack from Aachen` starts at 50.775346, 6.083887 and works on May 6 and 7, 2020, from 08:00 to 17:00 (Europe/Berlin). His maximal working time is 9 hours per day, the length of the shift, and his maximal distance 1200 km.
- **Nodes:** seven `TimeWindowGeoNode` objects in Koeln, Essen, Dueren, Nuernberg, Heilbronn, Wuppertal and Aachen, open on May 6 and 7 from 08:00 to 17:00, each with a visit of 20 minutes and the importance 1.

## How the code works

The source is [OptimizationSchemeCustomDefaultPropertiesExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/optimizationscheme/customdefaultproperties/OptimizationSchemeCustomDefaultPropertiesExample.java). `example()` calls these steps:

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
printEffectiveProperties(this);
```

- `ExampleLicenseHelper.setLicense(this)` uses your license file if you put one where the helper looks for it, and the public evaluation license otherwise.
- `setScheme` creates the scheme with its custom defaults and sets it.
- `addProperties`, `addNodes` and `addResources` add the properties, the seven nodes and Jack.
- `attachToObservables` subscribes to the progress, warning, status and error events with the event API, before the run starts.
- `startAndPresentResult` starts the run, waits up to 5 minutes for the result and prints it.
- `printEffectiveProperties` prints the two keys of the custom defaults as the optimization holds them after the start.

`printEffectiveProperties` reads the merged set:

```java
// At the start the scheme merges its custom defaults into the properties of the
// optimization; a key that both set keeps the value of the optimization.
Properties effective = opti.getUserProperties().orElseGet(Properties::new);
```

## Output and what to look for

The run prints the status and progress lines, then the result and the two properties. The results were the same in two test runs. The last two lines show the distance weight from the scheme and the generation count of the optimization, not the 2000 of the scheme:

```text
JOptWeight.TotalDistance: 10000.0
JOptExitCondition.JOptGenerationCount: 1000
```

The distance weight moves Jack's plan. All nodes but Aachen go onto May 6, for 911 km in all, and the route of May 6 ends at 21:30:49. It takes 810.83 minutes, beyond the 9 hours of Jack's maximal working time, and Heilbronn is reached after it closes at 17:00. The result reports both as violations:

```text
[ CONSTRAINTVIOLATION/MAXTIME/EXCEEDED/ Overtime [h]: 4.513847777777778 Value: 4.513847777777778 ]
```

```text
 Total distance    [km]  : 911
```

Heilbronn's stop line ends with "Late time [min]: 49.98008333333333". May 7 holds only the Aachen node. With the same data and the default weight, the first optimization example plans 1036 km in two test runs, with 1.89 hours of overtime on May 7 and no late visit: the weight saves 125 km and costs more overtime and a late visit. The total cost of the result, 9128756.54, is that high because the weight 10000.0 multiplies the cost of the distance: the route of May 6 alone has a distance cost of 9118967.93.

## Why custom default properties

In a production environment, you often want **system-wide solver defaults** that apply automatically across many jobs and services, without forcing every call site to provide a full property set.

At the same time, you must preserve the ability to override defaults for:
- debugging and support cases,
- A/B experiments,
- customer-specific tuning,
- or special workloads.

JOpt supports this via **custom default properties on the Optimization Scheme**.

This approach provides:
- **centralized defaults** (attached once in the scheme),
- **local overrides** (per optimization via `addElement(Properties)`),
- and a deterministic precedence model.

## Key idea: two sources of properties

### Scheme-level default properties (global, reusable)
These are configured once when the scheme is installed:

```java
IOptimizationScheme scheme = new DefaultOptimizationScheme(opti);

// Custom default properties apply to every optimization that uses the scheme, unless the
// optimization sets the same key itself. This can add hooks to existing pipelines or help in
// debugging.
Properties customDefaultProps = new Properties();

// A sample weight that makes distance by far the most important cost, so the plan saves
// distance at the price of more overtime.
customDefaultProps.setProperty("JOptWeight.TotalDistance", "10000.0");

// A sample value that addProperties sets too: the value of the optimization wins.
customDefaultProps.setProperty("JOptExitCondition.JOptGenerationCount", "2000");

scheme.setCustomDefaultProperties(customDefaultProps);

opti.setOptimizationScheme(scheme);
```

### Optimization-level properties (local, explicit)
These are set directly on the optimization instance:

```java
Properties props = new Properties();
// Fewer generations and iterations than the defaults keep the run short.
props.setProperty("JOptExitCondition.JOptGenerationCount", "1000");
props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumIterations", "100000");
opti.addElement(props);
```

The example sets neither `JOpt.Algorithm.PreOptimization.SA.NumRepetitions` nor `JOpt.NumCPUCores`; both keep their defaults, which the [property reference](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties) lists.

## Precedence model (critical)

The example explicitly documents the precedence rule, and its output shows it:

> Properties set directly on the optimization via `addElement(Properties)` take priority over custom default properties set via the scheme.

In other words:

1. **Local properties** (`opti.addElement(props)`) override
2. **Scheme custom defaults** (`scheme.setCustomDefaultProperties(...)`)

This design is intentional and gives you leverage because it enables:

- safe "baseline tuning" via the scheme,
- targeted overrides without modifying the default pipeline,
- support workflows where you can reproduce a customer's case with a modified property set.

## Why this matters in real systems

### Central defaults for many entry points
In enterprise deployments, optimizations can be triggered by:
- batch schedulers,
- microservices,
- UI workflows,
- integration pipelines.

A scheme-level default allows you to guarantee consistent baseline settings without copying property files everywhere.

### Cleaner DevOps and configuration management
Instead of managing multiple sources of truth for properties:
- define system defaults once,
- keep per-environment overrides in a controlled config layer,
- allow request-level overrides only when needed.

### Debugging and support
When investigating issues, you often want to:
- temporarily bias the solver (e.g., emphasize distance, emphasize lateness, disable a feature),
- reproduce results with known settings.

If defaults are injected centrally by the scheme, engineers can:
- override just one or two keys at the call site (or test harness),
- without breaking production default behavior.

### Controlled behavior changes across releases
When you update a solver version, you may want to ship:
- updated recommended defaults,
- new weights,
- or new performance settings.

A scheme-level default property block is an ideal place to enforce these changes consistently.

## What the example is demonstrating (and why it is intentionally extreme)

The example sets:

```java
customDefaultProps.setProperty("JOptWeight.TotalDistance", "10000.0");
```

The comment explains the intended effect:
- distance becomes *by far* the most important objective,
- which can push solutions into overtime (if distance savings dominate other considerations).

This is not necessarily "good" in operations; it is a demonstration of influence and precedence:
- scheme-level defaults can meaningfully reshape solver behavior,
- but can still be overridden locally when needed.

## Recommended production patterns

### Pattern A: company-wide baseline defaults
Use scheme-level defaults to set:
- preferred exit conditions (time/generation caps),
- core objective weights,
- performance mode defaults,
- AutoFilter defaults (if applicable),
- standard algorithm pipelines (via scheme configuration).

### Pattern B: per-customer tuning layer
If you operate multiple customers with different preferences:
- keep a per-customer scheme configuration (or scheme default property set),
- inject it when building the optimization instance,
- still allow emergency overrides via request-level properties.

### Pattern C: safe experimental overrides (A/B tests)
For controlled experiments:
- keep the scheme defaults stable,
- override a small set of keys via `addElement(Properties)` for the experimental branch,
- record the effective property set alongside result outputs.

### Pattern D: debug harness and reproducibility tooling
In a debugging tool:
- load the same optimization model and inputs,
- replay with modified local properties,
- confirm whether a behavioral issue is caused by property tuning or model structure.

## Implementation checklist

1. Define baseline defaults in the scheme via `setCustomDefaultProperties(Properties)`.
2. Ensure you apply the scheme before the run starts (`opti.setOptimizationScheme(...)`) so defaults are known for the run: the scheme merges them when the run is prepared.
3. Override selectively per run via `opti.addElement(Properties)`.
4. Record the effective property set for:
   - reproducibility,
   - audits,
   - and support cases.

## Summary

- Custom default properties on an Optimization Scheme provide centralized solver defaults that apply automatically.
- Properties added directly to the optimization instance override those defaults.
- This architecture is ideal for production pipelines: consistent baseline behavior with safe, controlled overrides for debugging and experiments.
- The example demonstrates this using an intentionally extreme distance weight to make the effect obvious.

## Run it

Run the `main(String[] args)` method of `OptimizationSchemeCustomDefaultPropertiesExample` from your IDE, with the repository imported as a Maven project. You need a JDK in the version that the examples pom sets. You need no license file: without one, the example uses the public evaluation license (see the [license guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/license)).

## Related

- [Optimization properties](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties): the property reference, with `JOptWeight.TotalDistance`, the generation count and their defaults.
- [Algorithm selection with an optimization scheme](OptimizationSchemeAlgorithmSelection.md): the other optimization scheme example, the construction algorithm and the phases of a run.
- [First optimization example](../../basic/firstoptimization_01/FirstOptimizationExample.md): the same data with the default properties.
- [Reactive events and streams](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/reactive_events): the feature guide of the event API that `attachToObservables` uses.
- [Recommended ways to run an optimization](../../basic/recommendedimplementation_02/RecommendedImplementation.md): the recommended run pattern, a synchronous run and the legacy callback methods.
- [Expert examples](../README.md): the overview of all Expert examples.

---

Added: 2026-02-17 (JOpt core 7.5.3-j17) | Last updated: 2026-10-21
