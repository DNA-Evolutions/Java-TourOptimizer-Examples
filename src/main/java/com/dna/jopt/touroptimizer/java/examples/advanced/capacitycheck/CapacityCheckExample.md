# CapacityCheck - Input plausibility: capacity check for working time vs required workload

This example explains JOpt’s **capacity plausibility check** and how to **deactivate it** if you *really* need to run with extreme or intentionally infeasible input.

**Source (GitHub):** https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/capacitycheck/CapacityCheckExample.java

---

## What “capacity check” means in this context

JOpt performs a basic plausibility check before starting optimization:

- If the **available working hours** are *far below* the **required working time** implied by the input (travel + visit durations), the run is typically rejected early.

In other words, it prevents you from running an optimization where the schedule is obviously impossible *by orders of magnitude*.  
This protects production systems from wasting time on runs that cannot produce meaningful schedules.

The example’s `toString()` summarizes it clearly: by default, JOpt does not accept if the available working hours are drastically smaller than the needed working time.

---

## Why this check exists (enterprise view)

In real deployments, “bad input” happens:
- imported data with wrong visit durations (e.g., 400 min instead of 40)
- missing working hours (e.g., 1 hour defined for a full-day plan)
- unrealistic travel time assumptions for long-distance instances

The plausibility check:
- fails fast with a clear signal,
- prevents long-running jobs with no operational value,
- improves reliability of batch planning pipelines.

---

## What the example does

1. Creates multiple **TimeWindowGeoNode** stops with long visit durations (`Duration.ofMinutes(400)`).
2. Creates a single **CapacityResource** with working hours and max working time.
3. Runs the optimization and prints progress/warnings/status/errors.
4. Shows how to toggle the plausibility capacity check via a property.

---

## How to deactivate the capacity check (not recommended unless necessary)

The example uses a boolean switch:

- `DO_DEACTIVATE_CAPACITY_CHECK`

When enabled, it sets this property:

- `JOpt.plausibility.doInputCheck.doCapacityCheck = FALSE`

This allows the optimizer to start even if the input looks extremely infeasible.

### Important note
Deactivating the check does **not** “fix” infeasibility. It only removes the early guardrail.  
Use this only when:
- you intentionally run partial/soft scenarios,
- you rely on other mechanisms to drop or relax elements,
- you want to analyze behavior on extreme data.

---

## How to use this safely in production

If you consider disabling the check, also consider:
- validating visit durations and working hours upstream
- using data-quality rules and thresholds
- logging warnings and run metadata for later auditing

A good pattern is:
1) keep the check enabled by default,  
2) allow disabling only for **explicit** “expert mode” runs with monitoring.

---

## Related topics
- [Optimization properties](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties)
- [Performance mode](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/performance_mode)

