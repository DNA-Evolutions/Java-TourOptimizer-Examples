# Recommended ways to run an optimization

JOpt delivers the events and the result of a run in three ways: through the event API with an asynchronous run, which we recommend, through the event API with a synchronous run, or through the legacy callback methods. The page is for Java developers who choose how their application starts a run and receives its events and result. Run the [First optimization example](../firstoptimization_01/FirstOptimizationExample.md) first: the three classes here use its steps.

## What the example shows

- **Recommended: the event API with an asynchronous run.** `RecommendedImplementationReactiveJavaExample` subscribes to the event subjects of `getOptimizationEvents()`, starts the run with `startRunAsync()` and waits for the result with a timeout.
- **The event API with a synchronous run.** `RecommendedSynchImplementationExample` subscribes to the same subjects and then calls `startRunSync(5, TimeUnit.MINUTES)`, which blocks until the result is there. If the run takes longer than the timeout, the call ends in a `TimeoutException`; the run is not stopped.
- **The legacy callback methods.** `RecommendedAsynchImplementationExample` extends `Optimization` and overrides its callback methods. The legacy callback methods (`onProgress`, `onAsynchronousOptimizationResult` and the others) subscribe to the same events internally and stay supported; the event API is recommended.

### Executive summary

If you only take away three points:

1. **Always keep the JVM alive until the optimization has finished** (block, await, or propagate the completion stage).
2. **Choose a result-handling strategy**: get a returned `IOptimizationResult` directly, or handle it via callback/event streams.
3. **Attach observability early** if you want progress/status/error output (especially in synchronous runs).

### Choosing a pattern

Use *Synchronous* (`RecommendedSynchImplementationExample`) when...
- you run a **CLI tool** or **batch job**,
- your application lifecycle is naturally blocking,
- you want a simple "run to print result to exit" control flow.

Use *Asynchronous (CompletableFuture)* (`RecommendedImplementationReactiveJavaExample`, the recommended pattern) when...
- you run inside a **service** where blocking threads is undesirable,
- you need to **compose** optimization completion with other async work,
- you want a clean integration into `CompletionStage`-based application code.

Use *Reactive events* (both event API classes) when...
- you want **live progress/status/error streaming** (logging, telemetry, UI),
- you prefer "subscribe and react" over overriding callback hooks,
- you want a single place to wire all observability (progress + status + errors).

### A recommended default integration blueprint

For most applications that need both **results** and **runtime insight**, the recommended and most robust approach is:

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

## Scenario

The three classes plan the same scenario. Jack, who starts in Aachen, visits nine nodes: Koeln, Oberhausen, Essen, Dueren, Nuernberg, Heilbronn, Stuttgart, Wuppertal and Aachen. The nodes are open and Jack works from 08:00 to 17:00 on May 6 and May 7, 2020 (Europe/Berlin); the dates are sample values. Each visit takes 20 minutes.

Jack may work 13 hours and drive 1200 km per day, so the day with Nuernberg, about 12 hours long, needs no overtime. The classes set two properties, `JOptExitCondition.JOptGenerationCount` and `JOpt.Algorithm.PreOptimization.SA.NumIterations`, to values lower than the core defaults, which keeps the run short; the [property reference](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties) describes them.

## How the code works

The three classes build the optimization with the same steps as the first optimization example. They differ in how they receive the events and the result. The sources (these links point to the examples in the public repository):

- [RecommendedImplementationReactiveJavaExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic/recommendedimplementation_02/RecommendedImplementationReactiveJavaExample.java)
- [RecommendedSynchImplementationExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic/recommendedimplementation_02/RecommendedSynchImplementationExample.java)
- [RecommendedAsynchImplementationExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic/recommendedimplementation_02/RecommendedAsynchImplementationExample.java)

(If you mirror the repository or pin to a commit, consider replacing `master` with a commit hash for stable references.)

### RecommendedImplementationReactiveJavaExample

This is the recommended pattern for "observable" apps: reactive events plus an async run. The class follows three steps: 1) subscribe to event streams, before the start (the subjects replay only a few past events); 2) start asynchronously; 3) retrieve and print the result (blocking here, but not required).

`attachToObservables` subscribes to the progress, warning, status and error subjects before the run starts and prints each event:

```java
// Subscribe before the start: the subjects replay only a few past events.
opti.getOptimizationEvents()
    .progressSubject()
    .subscribe(p -> System.out.println(p.getProgressString()));
opti.getOptimizationEvents().warningSubject().subscribe(w -> System.out.println(w.toString()));
opti.getOptimizationEvents().statusSubject().subscribe(s -> System.out.println(s.toString()));
opti.getOptimizationEvents().errorSubject().subscribe(e -> System.out.println(e.toString()));
```

`startAndPresentResult` starts the run and waits up to 5 minutes for the result. An application that must not block chains on the future instead, as the comment says:

```java
CompletableFuture<IOptimizationResult> resultFuture = opti.startRunAsync();
// Wait for the result; without the wait the JVM would end before the run does. An
// application that must not block can chain on the future instead, for example with
// thenAccept.
IOptimizationResult result = resultFuture.get(5, TimeUnit.MINUTES);
System.out.println(result);
```

**Why this pattern is often the "best default":**

- You get **continuous visibility** into the run (progress/status/errors).
- You can route events to:
  - logs,
  - metrics/telemetry,
  - UI updates,
  - structured incident signals (alerts).
- You keep the result as a `CompletableFuture`, which integrates well with modern Java runtimes.

Practical guidance:

- In server environments, avoid calling `get()` on request threads. Prefer chaining (`thenAccept`, `handle`, `whenComplete`) and propagating completion.
- Always ensure errors are surfaced (either via `exceptionally(...)` on the future, or via your chosen error observable/callback).
- Consider centralizing event subscriptions into a small "wiring" method so that:
  - your optimization model stays clean,
  - your logging/telemetry strategy remains consistent across examples.

### RecommendedSynchImplementationExample

This is the synchronous run with an explicit timeout. `attachToObservables` makes the same four subscriptions as in the recommended class. `startAndPresentResult` runs the optimization synchronously:

```java
// Blocks until the result is there; after 5 minutes without a result it throws a
// TimeoutException.
IOptimizationResult result = opti.startRunSync(5, TimeUnit.MINUTES);
System.out.println(result);
```

**Why this pattern exists:** it is designed for execution contexts where blocking is acceptable and often desirable:
- predictable application lifecycle,
- straightforward error handling (exceptions bubble up),
- no risk of "forgetting" to wait on the result.

Practical guidance:

- Prefer a **real timeout** in production (rather than `Long.MAX_VALUE`) so you can fail fast and recover gracefully.
- If you run synchronously (`startRunSync(...)`) but still want event output, also the progress/status output, ensure your chosen event/callback approach is wired *before* calling `startRunSync(...)`: subscribe **before** starting the run, that is before you call it, because the subjects replay only a few past events (the example explicitly warns about this).

### RecommendedAsynchImplementationExample

This class has no `attachToObservables` step. `example()` ends with `startAndWait`:

```java
// The public evaluation license, or your own license if you put a license file where
// ExampleLicenseHelper looks for it.
ExampleLicenseHelper.setLicense(this);
addProperties(this);
addNodes(this);
addResources(this);
startAndWait(this);
```

`startAndWait` starts the run and waits for it. It does not print the result:

```java
CompletableFuture<IOptimizationResult> resultFuture = opti.startRunAsync();
// Wait for the run; without the wait the JVM would end before the run does. The future
// completes after onAsynchronousOptimizationResult has returned, so the result is printed
// before the JVM ends.
resultFuture.get(5, TimeUnit.MINUTES);
```

The events and the result arrive in five overridden callback methods: `onProgress(IOptimizationProgress)`, `onWarning(IWarningEvent)`, `onStatus(IStatusEvent)`, `onError(IErrorEvent)` and `onAsynchronousOptimizationResult(IOptimizationResult)`. Each of them prints what it receives, so no warning goes unseen. Two of them:

```java
@Override
public void onProgress(IOptimizationProgress progress) {
  System.out.println(progress.getProgressString());
}
```

```java
@Override
public void onAsynchronousOptimizationResult(IOptimizationResult result) {
  System.out.println(result);
}
```

### How each class receives the result

`RecommendedImplementationReactiveJavaExample` waits on the future that `startRunAsync()` returns; a service chains `thenAccept` and `exceptionally` on it instead. `RecommendedSynchImplementationExample` takes the result that `startRunSync` returns. `RecommendedAsynchImplementationExample` prints it once in `onAsynchronousOptimizationResult`; it does not override `requestedAsynchronousOptimizationResult`, which receives only results requested during the run with `requestResult()`.

## Output and what to look for

The three classes print the same status events, the same progress lines, the same route of May 7 and the same cost. The result has one route per day. Route 1 on May 7 visits Nuernberg, Stuttgart and Heilbronn and has no violation (trimmed to the stops of route 1):

```text
1.0 Nuernberg / Geo  / Arrival: 07.05.2020 12:47:54 (Europe/Berlin) , Departure: 07.05.2020 13:07:54 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 287.91 [min], 380.04 [km]
1.1 Stuttgart / Geo  / Arrival: 07.05.2020 15:05:56 (Europe/Berlin) , Departure: 07.05.2020 15:25:56 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 118.03 [min], 155.8 [km]
1.2 Heilbronn / Geo  / Arrival: 07.05.2020 15:57:27 (Europe/Berlin) , Departure: 07.05.2020 16:17:27 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 31.52 [min], 41.6 [km]
```

Route 0 on May 6 visits the six other nodes. The Aachen node can be the first or the last stop of route 0, at the same cost, so the times of route 0 and the termination values can change between runs.

The position of the last status event shows the difference between the patterns. The two event API classes print it before the result, which `startAndPresentResult` prints after the wait:

```text
StatusEvent(OPTIMIZATION_ALGO_REPDONE, GeneticEvolution)
StatusEvent(OPTIMIZATION_DONE, Optimization done)
```

The legacy class prints the result in `onAsynchronousOptimizationResult`, and `StatusEvent(OPTIMIZATION_DONE, Optimization done)` follows as the last line of the output in two test runs.

## Next steps

Once these patterns are clear, the most useful next enhancements tend to be:

- structured logging of events (JSON logs, correlation IDs),
- robust timeout/retry policies around long-running optimization runs,
- persisting intermediate checkpoints or partial results (if applicable in your architecture),
- multi-resource scenarios (to validate scaling behavior and dispatch policies).

## Run it

Run the `main` method of each class from your IDE, with the repository imported as a Maven project. You need a JDK in the version that the examples pom sets. No license file is needed: without one, the classes use the public evaluation license (see `ExampleLicenseHelper`).

### Common pitfalls

- **The JVM ends before the run.** Wait for the result, as the classes do with a timed `get(...)` or `startRunSync(...)`.
- **No progress or status output.** Subscribe before the run starts, especially before a synchronous start.
- **The result is handled twice.** Handle it in one place: on the returned future or result, or in `onAsynchronousOptimizationResult`.
- **Exceptions are swallowed.** Connect the error handling explicitly: the error events, and the exception handlers of the future.

## Related

- [First optimization example](../firstoptimization_01/FirstOptimizationExample.md): the steps that these classes share, explained one by one.
- [Reactive events and streams](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/reactive_events): the feature guide of the event API.
- [Basic examples](../README.md): the overview of all Basic examples.
- [Optimization properties](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties): the property reference.

---

Added: 2026-02-17 (JOpt core 7.5.3-j17) | Last updated: 2026-10-21
