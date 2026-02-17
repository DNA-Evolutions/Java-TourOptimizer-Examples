# Build an OptimizationConfig fluently and run it via the JSONOptimization engine

This example demonstrates the **builder pattern** approach to configure and execute an optimization run with **immutable input objects**.  
Instead of wiring a large number of constructor calls, you create a complete optimization configuration using fluent builders and then run it with the **JSONOptimization** engine.

**Source (GitHub):** https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/builderpattern/BuilderPatternExample.java  

---

## Why this example matters

The builder pattern is ideal for enterprise integrations where you want:

- **Strong readability:** configuration reads like a specification (“add nodes”, “add resources”, “set extension”, “set options”)
- **Immutability:** one final `build()` produces a **stable config snapshot**
- **Reproducibility:** the resulting config can be serialized to **JSON** and used in other environments (for example REST / OpenAPI)
- **Separation of concerns:** *build input* → *run engine* → *consume results*

---

## What the example does (high level)

1. **Build an immutable `OptimizationConfig<JSONConfig>`**
   - Nodes (with opening hours and visit duration)
   - Resources (with working hours, max time, max distance, start position)
   - Extension (`JSONConfig`) including license (optional) and timeout
   - Optimization properties (`OptimizationOptions`) for algorithm behavior

2. **Run the config using `JSONOptimization`**
   - Starts asynchronously and returns a `CompletableFuture`

3. **Subscribe to optimization events**
   - progress, warnings, status, errors (reactive event streams)

4. **Print and parse the result**
   - JSON output (useful as REST input)
   - human-readable text solution
   - structured parsing (solution header, route headers, element details)

---

## Key sections in the code

### 1) Creating the full config using builders
The central idea is to construct everything via builders and “compose” the final config:

- `OptimizationConfig.builder() ... build()`
- `Node.builder() ... build()`
- `Resource.builder() ... build()`
- `WorkingHours.builder() ... build()`
- `OpeningHours.builder() ... build()`
- `JSONConfig.builder() ... build()`

This yields a complete, immutable config object that can be executed and serialized.

---

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
- REST / OpenAPI usage: [Try the API and get the Schema](https://www.dna-evolutions.com/api/)
- Snapshot / schema compatibility

---

### 3) Attaching to observables (progress, warnings, status, errors)
The example subscribes to event streams so you can:

- report progress in a UI
- log warnings and status updates
- capture errors centrally

This is the recommended approach for production systems because it:
- improves observability
- makes runs debuggable
- supports “explainability” and operational acceptance

**Related docs (placeholder):** {LINK_DOC_OPTIMIZATION_PROGRESS}

---

### 4) Parsing the output for your application layer
Beyond printing a solution, the example shows how to consume structured result objects:

- solution header (global KPIs / meta)
- route headers (per route stats)
- route element details (visit sequence and per-stop details)

This is typically how you map optimization output into:
- dispatch UIs
- reports
- downstream systems (TMS/ERP/WFM)

---

## Implementation notes

### License handling (optional)
The example demonstrates how to attach a JSON license via `OptimizationKeySetting`.  
If no license is provided, the optimizer may run in a limited/free mode (as described in the comments).

### OptimizationOptions (properties)
The example sets a few optimization properties (generation count and pre-optimization settings).  
This is the entry point for production tuning.

**Related docs:**
- [Optimization properties](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties)
- [Performance mode](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/performance_mode)

---

## When to choose the builder approach

Use the builder approach when:
- your config has many optional parts (open hours, constraints, metadata)
- you want clean code and fewer constructor overloads
- you want to reliably serialize the config into a portable JSON representation
- you build configs dynamically (from DB, UI, API calls)

---

## Next steps

- Connect this pattern to your **REST workflow** (build config → serialize JSON → call API)
- Extend nodes/resources with additional constraints (skills, zones, PND, relations)
- Add progress streaming to your UI or monitoring pipeline
