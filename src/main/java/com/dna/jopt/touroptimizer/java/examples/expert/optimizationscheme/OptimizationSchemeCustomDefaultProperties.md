# Optimization Scheme — Custom Default Properties (Pipeline Defaults With Safe Overrides)

In a production environment, you often want **system-wide solver defaults** that apply automatically across many jobs and services—without forcing every call site to provide a full property set.

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

---

## Reference (example)

- [OptimizationSchemeCustomDefaultPropertiesExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/optimizationscheme/customdefaultproperties/OptimizationSchemeCustomDefaultPropertiesExample.java) 

---

## Key idea: two sources of properties

### 1) Scheme-level default properties (global, reusable)
These are configured once when the scheme is installed:

```java
IOptimizationScheme myScheme = new DefaultOptimizationScheme(opti);

Properties customDefaultProps = new Properties();
customDefaultProps.setProperty("JOptWeight.TotalDistance", "10000.0");

myScheme.setCustomDefaultProperties(customDefaultProps);
opti.setOptimizationScheme(myScheme);
```

### 2) Optimization-level properties (local, explicit)
These are set directly on the optimization instance:

```java
Properties props = new Properties();
props.setProperty("JOptExitCondition.JOptGenerationCount", "2000");
props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumIterations", "20000");
props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumRepetions", "1");
props.setProperty("JOpt.NumCPUCores", "4");

opti.addElement(props);
```

---

## Precedence model (critical)

The example explicitly documents the precedence rule:

> Properties set directly on the optimization via `addElement(Properties)` take priority over custom default properties set via the scheme.

In other words:

1. **Local properties** (`opti.addElement(props)`) override
2. **Scheme custom defaults** (`myScheme.setCustomDefaultProperties(...)`)

This design is intentional and high leverage because it enables:

- safe “baseline tuning” via the scheme,
- targeted overrides without modifying the default pipeline,
- support workflows where you can reproduce a customer’s case with a modified property set.

---

## Why this matters in real systems

### 1) Central defaults for many entry points
In enterprise deployments, optimizations can be triggered by:
- batch schedulers,
- microservices,
- UI workflows,
- integration pipelines.

A scheme-level default allows you to guarantee consistent baseline settings without copying property files everywhere.

### 2) Cleaner DevOps and configuration management
Instead of managing multiple sources of truth for properties:
- define system defaults once,
- keep per-environment overrides in a controlled config layer,
- allow request-level overrides only when needed.

### 3) Debugging and support
When investigating issues, you often want to:
- temporarily bias the solver (e.g., emphasize distance, emphasize lateness, disable a feature),
- reproduce results with known settings.

If defaults are injected centrally by the scheme, engineers can:
- override just one or two keys at the call site (or test harness),
- without breaking production default behavior.

### 4) Controlled behavior changes across releases
When you update a solver version, you may want to ship:
- updated recommended defaults,
- new weights,
- or new performance settings.

A scheme-level default property block is an ideal place to enforce these changes consistently.

---

## What the example is demonstrating (and why it is intentionally extreme)

The example sets:

```java
customDefaultProps.setProperty("JOptWeight.TotalDistance", "10000.0"); // Default is 1.0 (!)
```

The comment explains the intended effect:
- distance becomes *by far* the most important objective,
- which can push solutions into overtime (if distance savings dominate other considerations).

This is not necessarily “good” in operations—it is a demonstration of influence and precedence:
- scheme-level defaults can meaningfully reshape solver behavior,
- but can still be overridden locally when needed.

---

## Recommended production patterns

### Pattern A — Company-wide baseline defaults
Use scheme-level defaults to set:
- preferred exit conditions (time/generation caps),
- core objective weights,
- performance mode defaults,
- AutoFilter defaults (if applicable),
- standard algorithm pipelines (via scheme configuration).

### Pattern B — Per-customer tuning layer
If you operate multiple customers with different preferences:
- keep a per-customer scheme configuration (or scheme default property set),
- inject it when building the optimization instance,
- still allow emergency overrides via request-level properties.

### Pattern C — Safe experimental overrides (A/B tests)
For controlled experiments:
- keep the scheme defaults stable,
- override a small set of keys via `addElement(Properties)` for the experimental branch,
- record the effective property set alongside result outputs.

### Pattern D — Debug harness and reproducibility tooling
In a debugging tool:
- load the same optimization model and inputs,
- replay with modified local properties,
- confirm whether a behavioral issue is caused by property tuning or model structure.

---

## Implementation checklist

1. Define baseline defaults in the scheme via `setCustomDefaultProperties(Properties)`.
2. Ensure you apply the scheme early (`opti.setOptimizationScheme(...)`) so defaults are known for the run.
3. Override selectively per run via `opti.addElement(Properties)`.
4. Record the effective property set for:
   - reproducibility,
   - audits,
   - and support cases.

---

## Summary

- Custom default properties on an Optimization Scheme provide centralized solver defaults that apply automatically.
- Properties added directly to the optimization instance override those defaults.
- This architecture is ideal for production pipelines: consistent baseline behavior with safe, controlled overrides for debugging and experiments.
- The example demonstrates this using an intentionally extreme distance weight to make the effect obvious.
