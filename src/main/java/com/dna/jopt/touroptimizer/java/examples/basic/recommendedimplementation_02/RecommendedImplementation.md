# Recommended Implementation Patterns (Java) — JOpt TourOptimizer Examples

This document consolidates the **recommended execution patterns** demonstrated by the following example classes:

- **Synchronous**: `RecommendedSynchImplementationExample`
- **Asynchronous (CompletableFuture)**: `RecommendedAsynchImplementationExample`
- **Reactive event consumption (ReactiveJava-style events + CompletableFuture result)**: `RecommendedImplementationReactiveJavaExample`

The intent is to help you choose the *right* integration style for your runtime (CLI tools, batch jobs, services, UI apps) and avoid common pitfalls such as premature JVM termination, missing events, or “silent” failures.

---

## Source links (GitHub)

These links point to the examples in the public repository:

- `RecommendedSynchImplementationExample.java`  
  [RecommendedSynchImplementationExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic/recommendedimplementation_02/RecommendedSynchImplementationExample.java)

- `RecommendedAsynchImplementationExample.java`  
  [RecommendedAsynchImplementationExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic/recommendedimplementation_02/RecommendedAsynchImplementationExample.java)

- `RecommendedImplementationReactiveJavaExample.java`  
  [RecommendedImplementationReactiveJavaExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic/recommendedimplementation_02/RecommendedImplementationReactiveJavaExample.java)

(If you mirror the repository or pin to a commit, consider replacing `master` with a commit hash for stable references.)

---

## Executive summary

If you only take away three points:

1. **Always keep the JVM alive until the optimization has finished** (block, await, or propagate the completion stage).  
2. **Choose a result-handling strategy**: get a returned `IOptimizationResult` directly, or handle it via callback/event streams.  
3. **Attach observability early** if you want progress/status/error output (especially in synchronous runs).

---

## Which pattern should you use?

### Use *Synchronous* when…
- you run a **CLI tool** or **batch job**,
- your application lifecycle is naturally blocking,
- you want a simple “run → print result → exit” control flow.

### Use *Asynchronous (CompletableFuture)* when…
- you run inside a **service** where blocking threads is undesirable,
- you need to **compose** optimization completion with other async work,
- you want a clean integration into `CompletionStage`-based application code.

### Use *Reactive events* when…
- you want **live progress/status/error streaming** (logging, telemetry, UI),
- you prefer “subscribe and react” over overriding callback hooks,
- you want a single place to wire all observability (progress + status + errors).

---

## Pattern 1 — Synchronous run with explicit timeout

**Example:** `RecommendedSynchImplementationExample`

### What it looks like in code (core line)

```java
IOptimizationResult result = this.startRunSync(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
System.out.println(result);
```

### Why this pattern exists

This pattern is designed for execution contexts where blocking is acceptable and often desirable:
- predictable application lifecycle,
- straightforward error handling (exceptions bubble up),
- no risk of “forgetting” to wait on the result.

### Practical guidance

- Prefer a **real timeout** in production (rather than `Long.MAX_VALUE`) so you can fail fast and recover gracefully.
- If you also want progress/status output, ensure your chosen event/callback approach is wired *before* calling `startRunSync(...)`.

---

## Pattern 2 — Asynchronous run via `CompletableFuture`

**Example:** `RecommendedAsynchImplementationExample`

### What it looks like in code (core lines)

```java
CompletableFuture<IOptimizationResult> resultFuture = this.startRunAsync();
resultFuture.get(); // blocks to keep the JVM alive
```

### Two valid ways to consume the result

This example demonstrates that you can handle results in two complementary ways:

#### A) “Future-driven” result consumption
- You get the returned `CompletableFuture<IOptimizationResult>`.
- You either block (CLI/batch) or chain continuations (services).

Typical service-style usage (non-blocking):

```java
this.startRunAsync()
    .thenAccept(result -> /* publish / persist / respond */)
    .exceptionally(ex -> { /* handle */ return null; });
```

#### B) “Callback-driven” result consumption
The class also overrides:

- `onAsynchronousOptimizationResult(IOptimizationResult result)`
- `requestedAsynchronousOptimizationResult(IOptimizationResult result)`

and prints the result from inside these hooks.

**Recommendation:** pick *one* primary result-consumption mechanism per integration to avoid duplicate handling.

### Practical guidance

- In server environments, avoid calling `get()` on request threads. Prefer chaining (`thenAccept`, `handle`, `whenComplete`) and propagating completion.
- Always ensure errors are surfaced (either via `exceptionally(...)` on the future, or via your chosen error observable/callback).

---

## Pattern 3 — Reactive events + async run (recommended for “observable” apps)

**Example:** `RecommendedImplementationReactiveJavaExample`

### What it looks like in code

1) Start asynchronously:

```java
CompletableFuture<IOptimizationResult> resultFuture = this.startRunAsync();
```

2) Subscribe to event streams:

```java
this.getOptimizationEvents().progress.subscribe(p -> System.out.println(p.getProgressString()));
this.getOptimizationEvents().error.subscribe(e -> System.out.println(e.getCause() + " " + e.getCode()));
this.getOptimizationEvents().status.subscribe(s -> System.out.println(s.getDescription() + " " + s.getCode()));
```

3) Retrieve and print the result (blocking here, but not required):

```java
IOptimizationResult result = resultFuture.get();
System.out.println(result);
```

### Why this pattern is often the “best default”

- You get **continuous visibility** into the run (progress/status/errors).
- You can route events to:
  - logs,
  - metrics/telemetry,
  - UI updates,
  - structured incident signals (alerts).
- You keep the result as a `CompletableFuture`, which integrates well with modern Java runtimes.

### Practical guidance

- If you run synchronously (`startRunSync(...)`) but still want event output, subscribe **before** starting the run (the example explicitly warns about this).
- Consider centralizing event subscriptions into a small “wiring” method so that:
  - your optimization model stays clean,
  - your logging/telemetry strategy remains consistent across examples.

---

## A “recommended default” integration blueprint

For most applications that need both **results** and **runtime insight**, the most robust approach is:

1. Build the optimization model (nodes/resources/properties).
2. Attach event subscribers (progress/status/error).
3. Start async (`startRunAsync()`).
4. Consume the result:
   - **CLI/batch**: block at the boundary (`get()` / timed get).
   - **service/UI**: chain continuations (`thenAccept`, `handle`) and return/propagate completion.

This yields a clean separation of concerns:
- modeling,
- observability,
- lifecycle management.

---

## Common pitfalls (and how these examples avoid them)

### Pitfall: JVM exits before optimization finishes
- Fix: block or await completion somewhere (examples use `get()` or sync start).

### Pitfall: no progress/status output (events “missed”)
- Fix: subscribe before running (especially relevant for synchronous start).

### Pitfall: result handled twice
- Fix: choose either “future-driven” *or* “callback-driven” result handling.

### Pitfall: swallowed exceptions
- Fix: connect error handling explicitly (reactive error stream and/or future exception handlers).

---

## Next steps

Once these patterns are clear, the most useful next enhancements tend to be:

- structured logging of events (JSON logs, correlation IDs),
- robust timeout/retry policies around long-running optimization runs,
- persisting intermediate checkpoints or partial results (if applicable in your architecture),
- multi-resource scenarios (to validate scaling behavior and dispatch policies).
