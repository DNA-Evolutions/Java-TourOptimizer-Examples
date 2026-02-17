# PerformanceMode — Faster Genetic Optimization With Reduced Operators

Performance Mode is a built-in execution strategy in **JOpt.TourOptimizer** that aims to reduce runtime by simplifying parts of the optimization process.

It is designed for situations where you primarily need:
- **fast turnaround** (large instances, frequent re-optimizations, interactive planning),
- and you can accept a solution that may be **slightly less optimized** than the best outcome achievable in Standard Mode.

---

## References

### Documentation
- Performance Mode: https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/performance_mode
- Optimization Properties (includes `JOpt.PerformanceMode`): https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties

### Example sources
- [PerformanceModeExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/performancemode/PerformanceModeExample.java)  

- [PerformanceModeBigOptiExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/performancemode/PerformanceModeBigOptiExample.java)  


---

## What Performance Mode changes internally

According to the official documentation, enabling Performance Mode changes two major aspects of the genetic phase:

### 1) Reduced population size
Performance Mode reduces the number of concurrent solutions evaluated during genetic evolution by reducing the **population size** (documented as “cut in half”).

Practical effect:
- fewer candidate solutions are processed per evolutionary iteration,
- less CPU time spent per generation,
- but also less variation in the search space, which can reduce the optimizer’s ability to escape local minima in difficult problems.

### 2) Several advanced operators are disabled
The following operators are listed as disabled in Performance Mode:

- `Single2OptOperator`
- `RecombineOptimizationElementsOperator2`
- `RecombineOptimizationElementsOperator3`
- `InversionOperator`
- `ReciprocalExchangeOperator`
- `RouteSeedOperator`
- `DoubleRouteRandomizeOperator`
- `DisplacementOperator`

Practical effect:
- less “exploration and variation” per generation,
- higher speed,
- but fewer mechanisms to repair or refine complex route structures.

---

## Expected outcome and trade-offs

### Speed
The documentation reports that Performance Mode can make runs **up to ~50% faster** and demonstrates speedups around **1.4×–1.8×** in their evaluation setup.

### Solution quality
The same evaluation reports that solution quality remained around **~95%** of Standard Mode (in the tested scenario), while achieving the speed improvements.

Interpretation:
- Performance Mode is often a strong default for “large and relatively flexible” instances,
- but you should validate solution quality for your specific constraint landscape.

---

## When to use Performance Mode

Performance Mode is typically a good fit for:

- **Large-scale optimizations** (many nodes/resources) where runtime is a priority.
- **Low-restriction** problems (few time windows, few skills, few territory/zone constraints).
- **Long working hours / flexible schedules**, where the solution space is broad and simpler genetic exploration is still effective.
- **Operational re-optimization** loops where you run frequently (e.g., every few minutes) and want “good fast” rather than “best slow”.

---

## When not to use Performance Mode

Avoid (or carefully validate) Performance Mode when:

- The problem is **highly constrained**, for example:
  - tight and strict time windows,
  - complex skill matching with limited qualified resources,
  - strong geographic territories / zone constraints,
  - many hard constraints that require sophisticated repair operators.
- You need **maximum optimality** (e.g., high-cost operations where small improvements matter significantly).
- You are diagnosing feasibility issues and require the most capable internal operator set to explore “hard” repairs.

---

## How to enable Performance Mode

Performance Mode is enabled using a single optimizer property:

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

---

## Example 1 — `PerformanceModeExample`

This example is intentionally small and clear:

- It sets a boolean toggle:
  - `usePerformanceMode = true`
- It adds properties with:
  - high iteration counts (to make runtime differences visible),
  - CPU cores set via `JOpt.NumCPUCores`,
  - and `JOpt.PerformanceMode` set from the boolean.

Key learning:
- Performance Mode is a **configuration switch** — you do not need to change your modeling (nodes/resources/constraints).
- You can A/B test by flipping one boolean.

Recommended usage:
- Keep the boolean flag in your production configuration so you can turn Performance Mode on/off per scenario.

---

## Example 2 — `PerformanceModeBigOptiExample`

This example is built to represent a **stress test** and to reflect the evaluation ideas described in the documentation.

### What makes it “big”
The example programmatically generates:
- a large set of nodes around a center position,
- a set of resources around a center position,
- and optional complexity toggles such as skill constraints and “odd day splitting”.

It uses a *phyllotaxis sampling* strategy to distribute positions deterministically around a center location, producing dense but structured spatial instances.

### Complexity toggles (important for performance testing)
Inside `addElements(...)`, the example defines switches such as:
- `addSkillConstraints` (adds a skill requirement to ~half of the nodes and provides that skill only to a subset of resources)
- `addEventOddSplitting` (allows visitations on different days, making results less geographically compact)

This is a recommended pattern for serious performance evaluation:
- benchmark the same instance under multiple “constraint intensities,”
- then decide if Performance Mode remains acceptable.

### What to watch for when running it
- Runtime difference between Standard vs Performance Mode.
- Route quality differences (distance/time/constraint violations).
- Whether constraints (skills/time windows) remain well satisfied.
- Whether the solution changes structure (more fragmented routes vs compact routes).

---

## Practical testing strategy

A reliable way to decide if Performance Mode should be default in your environment:

1. Select 3–5 representative instance types:
   - “easy” (few constraints),
   - “typical” (moderate constraints),
   - “hard” (tight time windows / scarce skills).
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
   - Use Performance Mode for “easy/typical” segments,
   - and Standard Mode for “hard” segments.

---

## Recommended positioning in production systems

Many teams use Performance Mode as part of a two-tier pipeline:

### Tier 1 — Fast solution
- Performance Mode ON
- Lower compute budget
- Used for:
  - interactive planning,
  - frequent re-optimization,
  - “quick feasibility + decent quality”

### Tier 2 — Refinement solution
- Performance Mode OFF (Standard Mode)
- Higher compute budget
- Used for:
  - final dispatch freeze,
  - overnight planning,
  - high-value deliveries

This approach provides a pragmatic balance:
- fast response during operations,
- higher quality when it matters.

---

## Summary

- Performance Mode accelerates optimization by reducing population size and disabling several advanced genetic operators.
- It can deliver significant runtime improvements (documented up to ~50%, and ~1.4×–1.8× in an evaluation), with an observed quality retention around ~95% in the tested setup.
- It is best for large, flexible, lightly constrained instances and operational re-optimization.
- For tight, complex constraint landscapes, validate carefully or prefer Standard Mode.
- Enable it via: `JOpt.PerformanceMode = true` in the optimizer properties.
