# Uncaught exception handling in a run and in event subscriptions

The three classes of this package throw an exception on purpose: in a custom node-level restriction during the run, in a legacy callback method and in a subscription of their own to the progress events. A custom uncaught exception handler completes the result future of the run with the exception, so the wait for the result ends with an `ExecutionException`; each class prints its cause and ends normally. The page is for developers who run JOpt in services and need a failed run to reach the caller; the [First optimization example](../../basic/firstoptimization_01/FirstOptimizationExample.md) introduces the steps of the code.

## What the example shows

Uncaught exceptions can be one of the most expensive classes of production failures in optimization pipelines:

- They often occur inside **background worker threads** (solver "slave threads").
- They can silently terminate a thread while the rest of the system keeps running.
- They may leave an optimization in an undefined state unless the run is **terminated cleanly** and the error is **propagated to the caller**.

JOpt provides a structured way to handle these failures using a dedicated **uncaught exception handler** mechanism:

1. JOpt's own handler can capture exceptions that escape any try/catch blocks, in the threads of the run and in the subscriptions of the legacy callback methods; it logs them and streams the error event `UNCAUGHT_EXCEPTION_IN_OPTIMIZATION`.
2. It then calls the handler that you set with `setSlaveUncaughtExceptionHandler`; it does not terminate the optimization itself.
3. And that handler can propagate the error through the optimization's result future, as `MyUncaughtExceptionHandler` of these examples does: the wait for the result then ends with the exception as its cause.

This document explains the architecture, the "why", and how to implement and operationalize it.

## Scenario

Every class creates its optimization with a setup whose slave handler is `MyUncaughtExceptionHandler`. The working hours and the opening hours run from 08:00 to 17:00 (Europe/Berlin); the dates in May 2020 are sample values.

- `UncaughtExceptionHandlingExample`: Jack from Aachen visits Koeln, Oberhausen, Essen, Dueren, Nuernberg, Heilbronn, Stuttgart, Wuppertal and Aachen on May 6 and May 7, with sample limits of 13 h and 1200 km per day. The custom restriction `FaultyInjectedRestriction` throws an `ArithmeticException` in about one of 1000 calls.
- `UncaughtExceptionHandlingRxJavaExample`: Jack from Aachen visits Koeln on May 6, with sample limits of 13 h and 1200 km. The overridden legacy callback method `onProgress(IOptimizationProgress)` throws in about one of three calls.
- `UncaughtExceptionHandlingRxJavaWithCustomErrorConsumer`: Jack from Aachen visits Koeln on May 6, with sample limits of 9 h and 1200 km. A subscription of the class to the progress events throws in about one of three events.

The faults are random on purpose, so where a run fails can change between runs. Every run ends at its fault, before a result, so the limits do not matter.

## How the code works

### References (example sources)

#### Main examples

- [UncaughtExceptionHandlingExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/uncaughtexception/UncaughtExceptionHandlingExample.java)
- [UncaughtExceptionHandlingRxJavaExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/uncaughtexception/UncaughtExceptionHandlingRxJavaExample.java)
- [UncaughtExceptionHandlingRxJavaWithCustomErrorConsumer.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/uncaughtexception/UncaughtExceptionHandlingRxJavaWithCustomErrorConsumer.java)

#### Internals used by the examples

- [MyUncaughtExceptionHandler.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/uncaughtexception/customhandler/MyUncaughtExceptionHandler.java)
- [OpenCostAssessorOptimizationSchemeWithFaultyRestriction.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/uncaughtexception/openassessorexception/OpenCostAssessorOptimizationSchemeWithFaultyRestriction.java)
- [FaultyInjectedRestriction.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/uncaughtexception/openassessorexception/FaultyInjectedRestriction.java)

#### Background reading (Reactive)

- [ReactiveX overview](https://reactivex.io/)
- [ReplaySubject reference](https://introtorx.com/chapters/key-types#ReplaySubject)

Open Assessor reference (because one failure source is a faulty injected restriction):
- [Special features: Open Assessor](https://www.dna-evolutions.com/docs/learn-and-explore/special/special_features#open-assessor)

Each class sets the license, adds the properties, the nodes and the resource, subscribes to the events and starts the run, in the steps of the first optimization example. In all three classes, `addProperties` sets `JOptExitCondition.JOptGenerationCount` to 1000 and `JOpt.Algorithm.PreOptimization.SA.NumIterations` to 100000. Both are below their defaults, which keeps a run short.

### Example A: UncaughtExceptionHandlingExample, an uncaught exception from injected Open Assessor code

#### How the example triggers a fault

The example injects a node-level restriction (`FaultyInjectedRestriction`) through a custom optimization scheme:

- `OpenCostAssessorOptimizationSchemeWithFaultyRestriction`

The restriction randomly throws, in about one of 1000 calls:

- `throw new ArithmeticException("Test Exception");`

```java
// The fault: JOpt calls the restriction for every node it assesses, so one throw in about
// 1000 calls ends the run soon, at a point that changes between runs.
int randomNum = ThreadLocalRandom.current().nextInt(0, 1000);
if (randomNum == 0) {
  throw new ArithmeticException("Test Exception");
}

// An empty result: no cost and no violation.
return new EntityRestrictionResult();
```

This simulates a common real-world failure mode:
- customer logic is executed in the solver loop,
- and an unexpected edge case triggers an exception.

#### How the handler is attached

In `createSetup`, which `main` calls, the example configures a custom setup:

```java
IOptimizationSetup setup = new DefaultOptimizationSetup();
// JOpt wraps the handler in its own handler, which logs the exception, streams the error event
// UNCAUGHT_EXCEPTION_IN_OPTIMIZATION and then calls MyUncaughtExceptionHandler.
setup.setSlaveUncaughtExceptionHandler(new MyUncaughtExceptionHandler());
return setup;
```

Key idea:
- The handler is attached to the solver threads through the setup, as a slave handler: the setup wraps it in JOpt's own handler, which handles the uncaught exceptions of the threads created by the optimization for its run and of the subscriptions of the legacy callback methods.
- JOpt's handler logs the exception, streams the error event and then calls the slave handler, after it has attached the optimization and the error streamer to it, so `MyUncaughtExceptionHandler` needs no call of `attachOptimization` here.

#### Why the example blocks on resultFuture.get()

The example explicitly blocks, at most five minutes:

```java
// Wait for the result; without the wait the JVM would end before the run does.
IOptimizationResult result = resultFuture.get(5, TimeUnit.MINUTES);
```

The wait matters:
- in a standalone process, if the main thread exits early, the JVM may terminate while background tasks are still running.
- blocking ensures you see the failure signal in the calling thread.

`MyUncaughtExceptionHandler` completes the result future with the exception, so the wait ends with an `ExecutionException`. The example catches it, prints its cause and ends normally:

```java
CompletableFuture<IOptimizationResult> resultFuture = opti.startRunAsync();
try {
  // Wait for the result; without the wait the JVM would end before the run does.
  IOptimizationResult result = resultFuture.get(5, TimeUnit.MINUTES);
  System.out.println(result);
} catch (ExecutionException e) {
  // MyUncaughtExceptionHandler completed the result future with the exception, so the wait
  // ends with it. The example prints the cause and ends normally.
  System.out.println("The run failed: " + e.getCause());
}
```

In production services, you typically do not "block", but you must still:
- keep the service request alive,
- or await completion in your job orchestration layer.

### Example B: UncaughtExceptionHandlingRxJavaExample, exceptions inside reactive subscriptions (RxJava)

#### Why reactive event streams change the failure mode

In the reactive approach, progress/error/warning signals are typically emitted through `ReplaySubject`s and consumed via `subscribe(...)`.

That has a key implication: if your `onNext` handler throws, the exception occurs **inside the subscription** context, not at the call site.

RxJava then applies its own error routing rules:
- If you do not provide an error consumer, errors may be wrapped (e.g., `OnErrorNotImplementedException`) and routed to the global RxJava error handler.

This can look like "random unexpected Rx exceptions" unless you handle errors intentionally.

#### What the RxJava example demonstrates

The example overrides the legacy callback method `onProgress(IOptimizationProgress)` and intentionally throws occasionally, in about one of three calls:

```java
@Override
public void onProgress(IOptimizationProgress progress) {
  // The fault: a throw in about one of three progress events ends the run soon, after a number
  // of progress lines that changes between runs.
  int randomNum = ThreadLocalRandom.current().nextInt(0, 3);
  if (randomNum == 0) {
    throw new ArithmeticException("Test Exception from overridden onProgress()");
  }

  System.out.println(progress.getProgressString());
}
```

The comment in the example's Javadoc of the method is the critical part:
- errors inside subscription code can become `OnErrorNotImplementedException` **if not backed by an error consumer**,
- and by default JOpt can use the provided uncaught exception handler to create such an error consumer: it subscribes the legacy callback methods to the events with an error consumer that calls the uncaught exception handler of the setup.

The legacy callback methods (`onProgress`, `onAsynchronousOptimizationResult` and the others) subscribe to the same events internally and stay supported; the event API is recommended. The class prints the other events through the subjects `warningSubject()`, `statusSubject()` and `errorSubject()`.

### Example C: UncaughtExceptionHandlingRxJavaWithCustomErrorConsumer, a subscription with an error consumer

#### Best practice: always attach an error consumer

The pattern that we recommend for subscriptions of your own is shown in:

- `UncaughtExceptionHandlingRxJavaWithCustomErrorConsumer`

It explicitly constructs an error consumer based on the uncaught exception handler of the setup, and then subscribes with that error consumer and an `onNext` lambda, in the form `subscribe(onNext, errorConsumer)`:

```java
// The error consumer hands an exception of the subscriber to the uncaught exception handler of
// the setup, the same handler that JOpt uses for its own threads.
Consumer<? super Throwable> errorConsumer =
    t -> opti.getOptimizationSetup()
        .getUncaughtExceptionHandler()
        .uncaughtException(Thread.currentThread(), t);

// Subscribe before the start: the subjects replay only a few past events.
opti.getOptimizationEvents()
    .progressSubject()
    .subscribe(
        p -> {
          // The fault: a throw in about one of three progress events ends the run soon,
          // after a number of progress lines that changes between runs.
          int randomNum = ThreadLocalRandom.current().nextInt(0, 3);
          if (randomNum == 0) {
            throw new ArithmeticException("Test Exception from the progress subscription");
          }

          System.out.println(p.getProgressString());
        },
        errorConsumer);
```

This guarantees:
- your subscription failures are bridged into the *same* uncaught exception handling policy as the failures of the run: JOpt's handler logs them and streams the error event, and `MyUncaughtExceptionHandler` completes the result future,
- and the wait for the result of the optimization ends with an exceptional result future.

## Output and what to look for

Every class prints the status events first, then progress lines until the fault, if any come before it. JOpt's logger then reports the exception at the level ERROR with its stack trace, for example `Uncaught exception in (JOPT-main0): Test Exception.` The four lines after the stack trace were the same in four test runs of each class, and each class ends with the exit code 0.

### UncaughtExceptionHandlingExample

In four test runs the exception came right after the construction stage, after `StatusEvent(OPTIMIZATION_ALGO_REPDONE, SmartConstructionAlgo)`; the point can change between runs. The lines after the stack trace:

```text
ErrorEvent(UNCAUGHT_EXCEPTION_IN_OPTIMIZATION, java.lang.ArithmeticException: Test Exception)
We found an error: MyUncaughtExceptionHandler - Uncaught exception in JOPT-main0
Completing the result future exceptionally after the exception in thread: JOPT-main0
The run failed: java.lang.ArithmeticException: Test Exception
```

The error event comes from JOpt's handler, the next two lines from `MyUncaughtExceptionHandler`, the last from the example's catch block.

### UncaughtExceptionHandlingRxJavaExample

The overridden `onProgress` printed some progress lines before it threw in one test run and none in another; the number can change between runs. The lines after the stack trace:

```text
ErrorEvent(UNCAUGHT_EXCEPTION_IN_OPTIMIZATION, java.lang.ArithmeticException: Test Exception from overridden onProgress())
We found an error: MyUncaughtExceptionHandler - Uncaught exception in JOPT-main0
Completing the result future exceptionally after the exception in thread: JOPT-main0
The run failed: java.lang.ArithmeticException: Test Exception from overridden onProgress()
```

### UncaughtExceptionHandlingRxJavaWithCustomErrorConsumer

The subscription printed some progress lines before it threw in one test run and none in another; the number can change between runs. Because the error consumer calls the handler of the setup, the output has the same form as in the other two classes:

```text
ErrorEvent(UNCAUGHT_EXCEPTION_IN_OPTIMIZATION, java.lang.ArithmeticException: Test Exception from the progress subscription)
We found an error: MyUncaughtExceptionHandler - Uncaught exception in JOPT-main0
Completing the result future exceptionally after the exception in thread: JOPT-main0
The run failed: java.lang.ArithmeticException: Test Exception from the progress subscription
```

## What can go wrong (and why "uncaught" matters)

### Typical failure sources in customer solutions

In real projects, exceptions tend to come from:
- custom Open Assessor logic (node-level / route-level restrictions),
- custom distance/time backends,
- user-defined post-steps,
- progress callbacks and event listeners,
- malformed input transformations,
- and integration glue code.

Most of these code paths are executed:
- frequently,
- asynchronously,
- and on solver threads that you do not directly control.

If an exception escapes a callback and no handler is present, the system may:
- silently stop improving solutions (worker thread died),
- deadlock (some component waits for a signal that never arrives),
- or fail without a clean "result signal" to the caller.

## The architectural goal

A robust optimization service should guarantee:

1. **No silent failures**
   Any uncaught exception becomes visible to the caller.

2. **Clean termination**
   The optimization run is stopped cleanly (not "half-dead").

3. **Actionable observability**
   The error is logged with enough context to debug.

JOpt's uncaught exception handling, with a handler like `MyUncaughtExceptionHandler`, is designed for exactly that.

## How JOpt propagates "failure" to the caller

JOpt's optimization run is typically started using:

```java
CompletableFuture<IOptimizationResult> resultFuture = opti.startRunAsync();
```

The decisive part is: **the result future is the single "truth source"** for success or failure. The error event `UNCAUGHT_EXCEPTION_IN_OPTIMIZATION` on the error subject reports the same exception, for logging and monitoring.

The custom uncaught exception handler (`MyUncaughtExceptionHandler`) completes this future exceptionally:

- `opti.getOptimizationEvents().resultFuture().completeExceptionally(e);` (the result future of `getOptimizationEvents()`)

```java
IOptimization opti = this.getAttachedOptimization().get();
opti.getOptimizationEvents().resultFuture().completeExceptionally(e);
```

Operational impact:
- your caller receives a failing `CompletableFuture`,
- you can handle it in a single place (try/catch around `get()` or `future.exceptionally(...)`),
- and your job scheduler / microservice can mark the run as failed.

## Why ReplaySubject is used (and what that means operationally)

JOpt's event bus (`OptimizationEvents`) uses multiple **ReplaySubject** instances, for example:
- progress
- errors
- warnings
- intermediate results

ReplaySubject semantics (high-level):
- it buffers a bounded number of recent events (JOpt sets the number per subject; it is not configurable),
- and replays them to late subscribers.

This is practical in optimization systems because:
- a UI subscriber may attach late,
- a monitoring component may restart,
- or your orchestration layer may subscribe after run start.

### JOpt's buffer choices (from the internal implementation)
The implementation uses size-bounded subjects such as:
- progress: buffer 10
- error: buffer 20
- warnings: buffer 20
- node filtering: buffer 100

Operational guidance:
- size-bounded replay is a good default,
- but do not treat it as an infinite log (it is not a durable store).

If you need durable observability:
- forward events into your logging/telemetry pipeline (e.g., OpenTelemetry, ELK, Splunk),
- and keep replay as a convenience for late in-process subscribers.

## Implementing your own UncaughtExceptionHandler (recommended template)

### Step 1: Extend the JOpt handler base class
`MyUncaughtExceptionHandler` extends a JOpt-provided base handler (which already supports attaching an optimization instance).

Your implementation should typically:
- log or stream the error,
- terminate the faulty thread / optimization,
- and complete the result future exceptionally.

`MyUncaughtExceptionHandler` prints where the exception came from and completes the result future; JOpt's own handler, which calls it, has logged the exception and streamed the error event before.

### Step 2: Ensure the handler is thread-safe
Uncaught exceptions can happen concurrently.
The example uses `synchronized (this)` to serialize handler execution.

Production recommendation:
- keep handler logic short and predictable,
- avoid blocking IO inside the handler,
- if you must do IO, do it in a non-blocking way or with timeouts.

### Step 3: Attach the optimization instance when you need "completion"
If your handler should complete the optimization result future exceptionally, it must know which optimization it belongs to. For a handler set with `setSlaveUncaughtExceptionHandler`, JOpt's handler attaches the optimization before each call. A handler that you call yourself, outside the setup, needs:

- `myUncaughtExceptionHandler.attachOptimization(opti);`

### Step 4: Ensure you terminate the run, not just "log"
Logging alone is not sufficient.
If the solver thread is compromised, continuing can produce:
- inconsistent states,
- or a "stuck" optimization.

Completing the result future exceptionally provides a clean shutdown path.

## Operationalizing uncaught exception handling (production checklist)

### Always install a handler in your optimization setup
For any asynchronous usage, treat this as mandatory.

- Use `setSlaveUncaughtExceptionHandler(...)` in your setup (as in the examples).

### Bridge reactive subscription failures into the same policy
If you subscribe to event streams:
- always provide an `errorConsumer`,
- preferably one that routes into the uncaught handler (as shown).

### Make failures observable and actionable
Recommended logging payload:
- optimization id / job id,
- thread name,
- algorithm phase (if known),
- last known progress percentage,
- last known best cost/KPI,
- and a compact input summary.

### Decide on your failure policy
Not all customers want the same behavior:
- fail-fast (stop immediately),
- fail-safe (stop and return best-so-far),
- fail-continue (skip a restriction and continue): this is rare and risky.

Uncaught exception handling is the "fail-fast" baseline.
If you want "fail-safe best-so-far", implement it explicitly:
- store and persist intermediate best results,
- then stop cleanly and return the best snapshot when an error occurs.

### Test it intentionally
The provided examples are a good template:
- inject faults on purpose,
- verify that the result future fails,
- and verify that your orchestration layer records the failure reliably.

## Summary

- Uncaught exceptions are inevitable in extensible optimization systems; handling them is a core production concern.
- JOpt enables failure propagation by completing the optimization result future exceptionally from an uncaught exception handler, as `MyUncaughtExceptionHandler` does.
- For reactive event subscriptions (RxJava), always attach an `errorConsumer` and preferably route subscription failures into the same uncaught exception policy.
- ReplaySubject-based event streams make late subscriptions practical, but they are not a durable log: forward events to your telemetry system when needed.
- Even if the example failure triggers are "artificial", the architecture demonstrates the core promise: customer-specific logic can be added safely, and failures remain controllable and observable.

## Run it

Run the `main(String[] args)` method of `UncaughtExceptionHandlingExample`, `UncaughtExceptionHandlingRxJavaExample` or `UncaughtExceptionHandlingRxJavaWithCustomErrorConsumer` from your IDE, with the repository imported as a Maven project. You need a JDK in the version that the examples pom sets. No license file is needed: without one, the examples use the public evaluation license (see the [license guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/license)).

## Related

- [Reactive events and streams](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/reactive_events): the feature guide of the event subjects, with error handling in RxJava and uncaught exceptions.
- [Open Assessor](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/open_assessor): the feature guide of custom restrictions such as `FaultyInjectedRestriction`.
- [Recommended ways to run an optimization](../../basic/recommendedimplementation_02/RecommendedImplementation.md): the recommended run pattern, a synchronous run and the legacy callback methods.
- [Expert examples](../README.md): the overview of all Expert examples.

---

Added: 2026-02-17 (JOpt core 7.5.3-j17) | Last updated: 2026-10-21
