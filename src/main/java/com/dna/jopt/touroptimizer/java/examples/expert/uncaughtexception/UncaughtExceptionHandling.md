# Uncaught Exception Handling — Fail Fast, Fail Deterministically, and Still Return a Useful Signal

Uncaught exceptions are one of the most expensive classes of production failures in optimization pipelines:

- They often occur inside **background worker threads** (solver “slave threads”).
- They can silently terminate a thread while the rest of the system keeps running.
- They may leave an optimization in an undefined state unless the run is **terminated cleanly** and the error is **propagated to the caller**.

JOpt provides a structured way to handle these failures using a dedicated **uncaught exception handler** mechanism that can:

1. capture exceptions that escape any try/catch blocks,
2. terminate the optimization deterministically,
3. and propagate the error through the optimization’s result future.

This document explains the architecture, the “why”, and how to implement and operationalize it.

---

## References (example sources)

### Main examples

- [UncaughtExceptionHandlingExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/uncaughtexception/UncaughtExceptionHandlingExample.java)
- [UncaughtExceptionHandlingRxJavaExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/uncaughtexception/UncaughtExceptionHandlingRxJavaExample.java)
- [UncaughtExceptionHandlingRxJavaWithCustomErrorConsumer.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/uncaughtexception/UncaughtExceptionHandlingRxJavaWithCustomErrorConsumer.java)

### Internals used by the examples

- [MyUncaughtExceptionHandler.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/uncaughtexception/customhandler/MyUncaughtExceptionHandler.java)
- [OpenCostAssessorOptimizationSchemeWithFaultyRestiction.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/uncaughtexception/openassessorexception/OpenCostAssessorOptimizationSchemeWithFaultyRestiction.java)
- [FaultyInjectedRestriction.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/uncaughtexception/openassessorexception/FaultyInjectedRestriction.java)

### Background reading (Reactive)

- ReactiveX overview: https://reactivex.io/  
- ReplaySubject reference: https://introtorx.com/chapters/key-types#ReplaySubject

Open Assessor reference (because one failure source is a faulty injected restriction):
- https://www.dna-evolutions.com/docs/learn-and-explore/special/special_features#open-assessor

---

## What can go wrong (and why “uncaught” matters)

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
- or fail without a clean “result signal” to the caller.

---

## The architectural goal

A robust optimization service should guarantee:

1. **No silent failures**  
   Any uncaught exception becomes visible to the caller.

2. **Deterministic termination**  
   The optimization run is stopped cleanly (not “half-dead”).

3. **Actionable observability**  
   The error is logged with enough context to debug.

JOpt’s uncaught exception handling is designed for exactly that.

---

## How JOpt propagates “failure” to the caller

JOpt’s optimization run is typically started using:

```java
CompletableFuture<IOptimizationResult> resultFuture = opti.startRunAsync();
```

The decisive part is: **the result future is the single “truth source”** for success or failure.

The custom uncaught exception handler (`MyUncaughtExceptionHandler`) completes this future exceptionally:

- `opti.getOptimizationEvents().result.completeExceptionally(e);`

Operational impact:
- your caller receives a failing `CompletableFuture`,
- you can handle it in a single place (try/catch around `get()` or `future.exceptionally(...)`),
- and your job scheduler / microservice can mark the run as failed.

---

## Example A — Uncaught exception from injected Open Assessor code

### How the example triggers a fault

The example injects a node-level restriction (`FaultyInjectedRestriction`) through a custom optimization scheme:

- `OpenCostAssessorOptimizationSchemeWithFaultyRestiction`

The restriction randomly throws:

- `throw new ArithmeticException("Test Exception");`

This simulates a common real-world failure mode:
- customer logic is executed in the solver loop,
- and an unexpected edge case triggers an exception.

### How the handler is attached

In `main`, the example configures a custom setup:

```java
IOptimizationSetup currentSetup = new DefaultOptimizationSetup();
currentSetup.setSlaveUncaughtExceptionHandler(new MyUncaughtExceptionHandler());
```

Key idea:
- The handler is attached to solver slave threads created by the setup.

### Why the example blocks on `resultFuture.get()`

The example explicitly blocks:

```java
resultFuture.get();
```

This is not merely “for convenience”:
- in a standalone process, if the main thread exits early, the JVM may terminate while background tasks are still running.
- blocking ensures you see the failure signal in the calling thread.

In production services, you typically do not “block”, but you must still:
- keep the service request alive,
- or await completion in your job orchestration layer.

---

## Example B — Exceptions inside reactive subscriptions (RxJava)

### Why reactive event streams change the failure mode

In the reactive approach, progress/error/warning signals are typically emitted through `ReplaySubject`s and consumed via `subscribe(...)`.

That has a key implication:

> If your `onNext` handler throws, the exception occurs **inside the subscription** context, not at the call site.

RxJava then applies its own error routing rules:
- If you do not provide an error consumer, errors may be wrapped (e.g., `OnErrorNotImplementedException`) and routed to the global RxJava error handler.

This can look like “random unexpected Rx exceptions” unless you handle errors intentionally.

### What the RxJava example demonstrates

The example overrides `onProgress(...)` and intentionally throws occasionally.  
The comment in the example is the critical part:

- errors inside subscription code can become `OnErrorNotImplementedException` **if not backed by an error consumer**,
- and by default JOpt can use the provided uncaught exception handler to create such an error consumer.

### Best practice: always attach an error consumer

The most production-safe pattern is shown in:

- `UncaughtExceptionHandlingRxJavaWithCustomErrorConsumer`

It explicitly constructs an error consumer based on the uncaught exception handler:

```java
MyUncaughtExceptionHandler h = new MyUncaughtExceptionHandler();
h.attachOptimization(opti);

Consumer<? super Throwable> errorConsumer =
    t -> h.uncaughtException(Thread.currentThread(), t);
```

And then subscribes with that error consumer:

```java
opti.getOptimizationEvents()
    .progressSubject()
    .subscribe(onNext, errorConsumer);
```

This guarantees:
- your subscription failures are bridged into the *same* uncaught exception handling policy,
- and the optimization terminates deterministically with an exceptional result future.

---

## Why ReplaySubject is used (and what that means operationally)

JOpt’s event bus (`OptimizationEvents`) uses multiple **ReplaySubject** instances, for example:
- progress
- errors
- warnings
- intermediate results

ReplaySubject semantics (high-level):
- it buffers a configurable number of recent events,
- and replays them to late subscribers.

This is extremely practical in optimization systems because:
- a UI subscriber may attach late,
- a monitoring component may restart,
- or your orchestration layer may subscribe after run start.

### JOpt’s buffer choices (from the internal implementation)
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

---

## Implementing your own UncaughtExceptionHandler (recommended template)

### 1) Extend the JOpt handler base class
`MyUncaughtExceptionHandler` extends a JOpt-provided base handler (which already supports attaching an optimization instance).

Your implementation should typically:
- log or stream the error,
- terminate the faulty thread / optimization,
- and complete the result future exceptionally.

### 2) Ensure the handler is thread-safe
Uncaught exceptions can happen concurrently.
The example uses `synchronized (this)` to serialize handler execution.

Production recommendation:
- keep handler logic short and deterministic,
- avoid blocking IO inside the handler,
- if you must do IO, do it in a non-blocking way or with timeouts.

### 3) Attach the optimization instance when you need “completion”
If your handler should complete the optimization result future exceptionally, it must know which optimization it belongs to:

- `myUncaughtExceptionHandler.attachOptimization(opti);`

### 4) Ensure you terminate the run, not just “log”
Logging alone is not sufficient.
If the solver thread is compromised, continuing can produce:
- inconsistent states,
- or a “stuck” optimization.

Completing the result future exceptionally provides a clean shutdown path.

---

## Operationalizing uncaught exception handling (production checklist)

### A) Always install a handler in your optimization setup
For any asynchronous usage, treat this as mandatory.

- Use `setSlaveUncaughtExceptionHandler(...)` in your setup (as in the examples).

### B) Bridge reactive subscription failures into the same policy
If you subscribe to event streams:
- always provide an `errorConsumer`,
- preferably one that routes into the uncaught handler (as shown).

### C) Make failures observable and actionable
Recommended logging payload:
- optimization id / job id,
- thread name,
- algorithm phase (if known),
- last known progress percentage,
- last known best cost/KPI,
- and a compact input summary.

### D) Decide on your failure policy
Not all customers want the same behavior:
- fail-fast (stop immediately),
- fail-safe (stop and return best-so-far),
- fail-continue (skip a restriction and continue) — this is rare and risky.

Uncaught exception handling is the “fail-fast” baseline.
If you want “fail-safe best-so-far”, implement it explicitly:
- store and persist intermediate best results,
- then stop cleanly and return the best snapshot when an error occurs.

### E) Test it intentionally
The provided examples are a good template:
- inject faults on purpose,
- verify that the result future fails,
- and verify that your orchestration layer records the failure reliably.

---

## Summary

- Uncaught exceptions are inevitable in extensible optimization systems; handling them is a core production concern.
- JOpt enables deterministic failure propagation by completing the optimization result future exceptionally from an uncaught exception handler.
- For reactive event subscriptions (RxJava), always attach an `errorConsumer` and preferably route subscription failures into the same uncaught exception policy.
- ReplaySubject-based event streams make late subscriptions practical, but they are not a durable log—forward events to your telemetry system when needed.
- Even if the example failure triggers are “artificial”, the architecture demonstrates the core promise: customer-specific logic can be added safely, and failures remain controllable and observable.
