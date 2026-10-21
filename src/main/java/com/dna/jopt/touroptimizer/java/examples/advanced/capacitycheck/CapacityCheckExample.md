# Capacity check of the input

`CapacityCheckExample` builds an input whose visits need more than twice the time that the working hours of its only resource offer, and shows how the capacity check of JOpt rejects it before a run. The example prints the cause and ends without a run, on purpose. The page is for developers who want to see how JOpt reports such an input and how to switch the check off. The [First optimization example](../../basic/firstoptimization_01/FirstOptimizationExample.md) introduces the steps of the code.

## What the example shows

This example explains JOpt's **capacity plausibility check** and how to **deactivate it** if you *really* need to run with extreme or intentionally infeasible input.

Before a run, JOpt divides the total visit duration of all nodes by the total length of the working hours of all resources. Only visit durations count: travel times and the maximal working time of a resource are not part of the quotient. At a quotient of 2.0 or more, the capacity check rejects the input:

- The error event `ENTITY_CORRUPT_NOTENOUGHTWORKINGTIME` reports the rejection, with the quotient.
- No run starts. The wait for the result ends at once with an `InterruptedException`, which the example catches: it prints one line, sets the interrupt status of the thread again and returns.

The warning with the code 5000 reports the quotient as well. It appears from a lower quotient on, so a tight input gets the warning without the rejection.

The example also shows the switch: the property `JOpt.plausibility.doInputCheck.doCapacityCheck` set to `FALSE` turns the check off. With the check off and a quotient of 2.0 or more, JOpt starts the run and switches on performance mode.

## Scenario

Jack from Aachen works from 08:00 to 17:00 on May 6 and May 7, 2020 (Europe/Berlin), which gives 1080 minutes of working hours. He may work 9 hours and drive 1200 km per day. The dates are sample values.

Seven `TimeWindowGeoNode` objects need a visit of 400 minutes each: Koeln, Essen, Dueren, Nuernberg, Heilbronn, Wuppertal and Aachen. They are open from 08:00 to 20:00 on May 6.

The seven visits need 2800 minutes. 2800 divided by 1080 gives the quotient 2.59, above the limit of 2.0.

## How the code works

The source is [CapacityCheckExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/capacitycheck/CapacityCheckExample.java). `example()` sets the license, adds the properties, the nodes and the resource, subscribes to the events and starts the run, in the steps of the first optimization example.

### The input

The visit duration makes the input fail the check:

```java
// 400 minutes per visit: the seven visits need 2800 minutes, more than twice the 1080 minutes
// of Jack's working hours.
Duration visitDuration = Duration.ofMinutes(400);
```

Jack's limits do not take part in the check:

```java
// 9 h and 1200 km per day. The capacity check does not use these limits: it compares the
// visits with the length of the working hours.
Duration maxWorkingTime = Duration.ofHours(9);
Quantity<Length> maxDistanceKmW = Quantities.getQuantity(1200.0, KILO(METRE));
```

### The rejection

`attachToObservables` subscribes to the progress, warning, status and error events before the start. The error subscriber receives the rejection:

```java
// A rejection by the capacity check arrives here, with its error code and the quotient.
opti.getOptimizationEvents().errorSubject().subscribe(e -> System.out.println(e.toString()));
```

`startAndPresentResult` waits for the result as the other examples do. For a rejected input, the wait fails at once; the example catches the exception and prints one line instead of a result:

```java
CompletableFuture<IOptimizationResult> resultFuture = opti.startRunAsync();
try {
  // Wait for the result; without the wait the JVM would end before the run does.
  IOptimizationResult result = resultFuture.get(5, TimeUnit.MINUTES);
  System.out.println(result);
} catch (InterruptedException e) {
  // JOpt rejects this input before the run, so the wait ends at once with this exception
  // and no result. The first error event printed above names the cause.
  System.out.println(
      "Input rejected, the run did not start: the first error event names the cause.");
  // Keep the interrupt status of the thread for its caller.
  Thread.currentThread().interrupt();
}
```

### Switching the check off

A constant of the class decides whether `addProperties` switches the check off:

```java
// Set to true to switch the capacity check off. With this input the run then starts, and JOpt
// switches on performance mode.
private static final boolean DO_DEACTIVATE_CAPACITY_CHECK = false;
```

`addProperties` sets the property on every run, `TRUE` with the constant at `false` and `FALSE` with the constant at `true`:

```java
// TRUE lets the capacity check reject an input whose visits need at least twice the time of
// the working hours; FALSE switches the check off and lets the run start.
if (DO_DEACTIVATE_CAPACITY_CHECK) {
  props.setProperty("JOpt.plausibility.doInputCheck.doCapacityCheck", "FALSE");
} else {
  props.setProperty("JOpt.plausibility.doInputCheck.doCapacityCheck", "TRUE");
}
```

`addProperties` also sets fewer generations and iterations than the defaults, which keeps a run short once the check is off.

## Output and what to look for

After the status events, three events report the check. These lines appear in two test runs:

```text
WarningEvent(WarningEvent(5000, The provided workinghours are not sufficient to solve the problem. Quotient: ), 2.5925925925925926)
ErrorEvent(ENTITY_CORRUPT_NOTENOUGHTWORKINGTIME, java.lang.IllegalStateException: ENTITY_CORRUPT_NOTENOUGHTWORKINGTIME 2.5925925925925926)
WarningEvent(WarningEvent(49991, The entity is not valid. Returning an empty result.), The provided entity is not valid and a correction attempt failed.)
```

The JOpt logger then reports the rejection as an `InvalidEntityException` with its stack trace, at the levels FATAL and ERROR, and a second error event, `UNCAUGHT_EXCEPTION_IN_OPTIMIZATION`, follows. These lines belong to the rejection; the example itself does not fail. Its own line comes last:

```text
Input rejected, the run did not start: the first error event names the cause.
```

The example ends normally, with the exit code 0.

In an earlier test run with `DO_DEACTIVATE_CAPACITY_CHECK` set to `true`, the warning 5000 came first, then the warnings 5100 (the input is not valid regarding the working hours, but the check is disabled) and 5101 (`Using performance mode.`). The run produced a result with all seven nodes planned and with overtime (MAXTIME) violations. Switching the check off does not make the input feasible; it only lets the run start.

## What "capacity check" means in this context

JOpt performs a basic plausibility check before starting optimization:

- If the **available working hours** are *far below* the **required working time** implied by the input (the visit durations; travel times are not part of the check), the run is typically rejected early.

In other words, it prevents you from running an optimization where the schedule is obviously impossible *by a factor of two or more*.  
This protects production systems from wasting time on runs that cannot produce meaningful schedules.

The example's `toString()` summarizes it clearly: the capacity check rejects an input whose visits need at least twice the time of the working hours. With `DO_DEACTIVATE_CAPACITY_CHECK` set to `false`, the class sets the check on (`JOpt.plausibility.doInputCheck.doCapacityCheck` set to `TRUE`).

## Why this check exists (enterprise view)

In real deployments, "bad input" happens:
- imported data with wrong visit durations (e.g., 400 min instead of 40)
- missing working hours (e.g., 1 hour defined for a full-day plan)
- unrealistic travel time assumptions for long-distance instances (not caught by this check: travel times are not part of its quotient)

The plausibility check:
- fails fast with a clear signal,
- prevents long-running jobs with no operational value,
- improves reliability of batch planning pipelines.

## How to deactivate the capacity check (not recommended unless necessary)

The example uses a boolean switch:

- `DO_DEACTIVATE_CAPACITY_CHECK`

When enabled, it sets this property:

- `JOpt.plausibility.doInputCheck.doCapacityCheck = FALSE`

This allows the optimizer to start even if the input looks clearly infeasible.

### Important note
Deactivating the check does **not** "fix" infeasibility. It only removes the early guardrail.  
Use this only when:
- you intentionally run partial/soft scenarios,
- you rely on other mechanisms to drop or relax elements,
- you want to analyze behavior on extreme data.

## How to use this safely in production

If you consider disabling the check, also consider:
- validating visit durations and working hours upstream
- using data-quality rules and thresholds
- logging warnings and run metadata for later auditing

A good pattern is:
1) keep the check enabled by default,  
2) allow disabling only for **explicit** "expert mode" runs with monitoring.

## Run it

Run the `main(String[] args)` method of `CapacityCheckExample` from your IDE, with the repository imported as a Maven project. You need a JDK in the version that the examples pom sets. No license file is needed: without one, the example uses the public evaluation license (see the [license guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/license)).

To see the run with the check off, set `DO_DEACTIVATE_CAPACITY_CHECK` to `true` and run the example again.

## Related

- [Performance mode](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/performance_mode): the feature guide of the mode that JOpt switches on when the check is off and the quotient reaches the limit.
- [Optimization properties](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties): the property reference, with `JOpt.plausibility.doInputCheck.doCapacityCheck`.
- [Recommended ways to run an optimization](../../basic/recommendedimplementation_02/RecommendedImplementation.md): the recommended run pattern, a synchronous run and the legacy callback methods.
- [Advanced examples](../README.md): the overview of all Advanced examples.

---

Added: 2026-02-17 (JOpt core 7.5.3-j17) | Last updated: 2026-10-21
