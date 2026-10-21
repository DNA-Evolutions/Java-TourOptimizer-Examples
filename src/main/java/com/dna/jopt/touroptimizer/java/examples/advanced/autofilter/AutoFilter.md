# AutoFilter for late visits

`AutoFilterLateExample` and `AutoFilterParticularNodeExample` give one resource more visits than its two working days hold, and let the AutoFilter take nodes that are visited late too often out of the run. The first class lets the filter act on every node, the second only on the node Nuernberg. The page is for developers who try the AutoFilter on a small plan; the [AutoFilter feature guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/autofilter) explains how the filter decides and lists its properties.

## What the example shows

The **AutoFilter** is an important operational feature of **JOpt.TourOptimizer**: it enables the optimizer to **exclude ("filter out") nodes that repeatedly cause violations** across many candidate solutions.

In many real-world routing/scheduling instances, infeasibility is not global; it is often caused by a small subset of nodes:
- a customer is too far away to be reached within its SLA,
- too many tasks are packed into too little WorkingHours capacity,
- a subset of nodes require a specific resource (preferred/mandatory) and overload it,
- constraints interact in a way that makes a few nodes "structural outliers".

If those nodes remain in the problem throughout the run, the optimization can spend disproportionate effort exploring infeasible regions and may converge slowly, or not at all, to a useful schedule.

AutoFilter addresses this by changing the **effective problem instance during the run**: nodes that are systematically causing violations are removed so the optimizer can produce a high-quality plan for the remaining nodes.

- **`AutoFilterLateExample`** switches on the selective AutoFilter and its category for late visits with two properties, and sets the other AutoFilter values it uses explicitly. Every node can then be filtered when it is visited late too often.
- **`AutoFilterParticularNodeExample`** switches on the selective AutoFilter without a category and gives one node, Nuernberg, a `LateAutoFilterConstraint`. Only Nuernberg can be filtered; the other nodes stay in the plan, also when they are visited late.
- **Both** print each filter reason through the event API, with `nodeFilteringSubject()`. The event API is recommended. The legacy callback methods (`onProgress`, `onAsynchronousOptimizationResult` and the others, among them the two `onNodeFiltering` methods) subscribe to the same events internally and stay supported.

## Scenario

Both classes use the same data. The dates, times and coordinates are sample values. The time zone is Europe/Berlin.

- **Resource:** `Jack`, a `CapacityResource` at 50.775346, 6.083887 (Aachen), with working hours on May 6 and May 7, 2020, from 08:00 to 17:00. He may work up to 12 hours and drive up to 1200 km a day.
- **Nodes:** nine `TimeWindowGeoNode` objects (Koeln, Oberhausen, Essen, Dueren, Nuernberg, Heilbronn, Stuttgart, Wuppertal and Aachen), open on the same two days from 08:00 to 17:00, each with the same importance.
- **Visit duration:** 200 minutes per node. The nine visits take 30 hours, and the two working days hold 18 hours, so some nodes are visited late.

## How the code works

The sources are [AutoFilterLateExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/autofilter/AutoFilterLateExample.java) and [AutoFilterParticularNodeExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/autofilter/AutoFilterParticularNodeExample.java). Both call the same steps in `example()`:

```java
// The public evaluation license, or your own license if you put a license file where
// ExampleLicenseHelper looks for it.
ExampleLicenseHelper.setLicense(this);
addProperties(this);
addNodes(this);
addResources(this);
attachToObservables(this);
startAndPresentResult(this);
```

Both set `JOptExitCondition.JOptGenerationCount` and `JOpt.Algorithm.PreOptimization.SA.NumIterations` below their defaults, which keeps the run short. The [property reference](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties) lists the AutoFilter keys with their defaults.

### Filtering every node for late visits

`AutoFilterLateExample` switches on the selective AutoFilter and the category for late visits, keeps `JOpt.AutoFilter` off, and sets the strict last execution and the late margin:

```java
// The selective AutoFilter acts only on the violation categories that are switched on too.
props.setProperty("JOpt.SelectiveAutoFilter", "TRUE");
// Late visits are the only category here. JOpt.AutoFilter instead of the two keys would
// switch on all categories.
props.setProperty("JOpt.SelectiveAutoFilter.TimeWindow.Late", "TRUE");
// JOpt.AutoFilter stays off, so the late category above is the only one.
props.setProperty("JOpt.AutoFilter", "FALSE");
// The last AutoFilter execution of the run filters in strict mode.
props.setProperty("JOpt.AutoFilter.useStrictFilterOnLastExecution", "TRUE");
// No margin: a visit that is late by one second or more counts as late for the AutoFilter.
props.setProperty("JOpt.AutoFilter.LateArrivalMargin", "0");
```

### Filtering one particular node

`AutoFilterParticularNodeExample` switches on the selective AutoFilter but no category, which enables selective filtering only for a node with a filter constraint of its own; it keeps the late category and `JOpt.AutoFilter` off, and sets the strict last execution and the late margin as `AutoFilterLateExample` does:

```java
// No violation category is switched on, so the AutoFilter acts only on the node that carries
// a filter constraint of its own.
props.setProperty("JOpt.SelectiveAutoFilter", "TRUE");
// The late category stays off: only the late filter constraint of Nuernberg filters.
props.setProperty("JOpt.SelectiveAutoFilter.TimeWindow.Late", "FALSE");
// JOpt.AutoFilter stays off too; it would switch on all categories for the other nodes.
props.setProperty("JOpt.AutoFilter", "FALSE");
// The last AutoFilter execution of the run filters in strict mode.
props.setProperty("JOpt.AutoFilter.useStrictFilterOnLastExecution", "TRUE");
// No margin: a visit that is late by one second or more counts as late for the AutoFilter.
props.setProperty("JOpt.AutoFilter.LateArrivalMargin", "0");
```

In `addNodes` it creates a local constraint, a `LateAutoFilterConstraint` implementing `IAutoFilterNodeConstraint`, and attaches that constraint to a single node: it gives Nuernberg the constraint via `setAutoFilterConstraints`, with the effect that the AutoFilter logic is focused on that node, and other nodes are not filtered by that specific rule unless configured elsewhere:

```java
// Only Nuernberg carries a late filter constraint, so the AutoFilter may filter Nuernberg if
// it is visited late too often, and no other node.
List<IAutoFilterNodeConstraint> filterConstraints = new ArrayList<>();
IAutoFilterNodeConstraint lateConstraint = new LateAutoFilterConstraint();
filterConstraints.add(lateConstraint);
nuernberg.setAutoFilterConstraints(filterConstraints);
```

### Printing the filter reasons

`attachToObservables` subscribes to the progress, warning, status and error events, and to the node filtering events, before the run starts. A node filtering event carries a list of filter reasons, and the subscription prints each reason once:

```java
// Each filter reason names the node, its status and the reason.
opti.getOptimizationEvents()
    .nodeFilteringSubject()
    .subscribe(f -> f.getFilterReasons().forEach(r -> System.out.println(r.toString())));
```

`startAndPresentResult` starts the run, waits up to 5 minutes for the result and prints it.

## Output and what to look for

Both classes print the same warning at the start of the run, because the visits need more time than the working hours hold:

```text
WarningEvent(WarningEvent(5000, The provided workinghours are not sufficient to solve the problem. Quotient: ), 1.6666666666666667)
```

During the run, the AutoFilter executes several times and prints `StatusEvent(AUTOFILTER_EXECUTE, ...)` lines. After an execution that filters nodes, the filter reasons follow. Each reason also has a line `Violation quotient`, whose value can change between runs.

### AutoFilterLateExample

In two test runs the AutoFilter filtered five nodes for the reason `LATE`: Nuernberg, Stuttgart, then Essen, Oberhausen and Heilbronn in one execution. The first reason, trimmed to the lines without the quotient:

```text
 STATUS             : FILTERED
 Element            : Nuernberg
 Reason             : LATE
```

The result lists the five nodes as unassigned:

```text
Number of unassigned nodes: 5

Nuernberg
Stuttgart
Essen
Oberhausen
Heilbronn
```

The other four nodes are planned in two pairs without a violation: Aachen with Dueren, and Koeln with Wuppertal, one pair on each day. Which pair goes on which day can change between runs.

### AutoFilterParticularNodeExample

In two test runs the AutoFilter filtered only Nuernberg, the node with the late filter constraint:

```text
 STATUS             : FILTERED
 Element            : Nuernberg
 Reason             : LATE
```

```text
Number of unassigned nodes: 1

Nuernberg
```

The other eight nodes stay on the two routes, although some of them are visited late: the result shows `LATE` and overtime (`MAXTIME`) violations on both routes.

## AutoFilter in one sentence

**AutoFilter continuously analyzes which nodes caused violations in each generated solution and filters out nodes whose violation pattern becomes dominant.**

## Why AutoFilter is fundamentally different from "high penalty costs"

To our knowledge, the AutoFilter handles infeasibility in a way of its own: during the run, it removes the nodes that keep causing violations, judged per node by the share of the evaluated solutions in which the node violates, for the violation categories that are switched on, or for the filter constraints set on a node, beyond the margins and except for protected nodes. Attaching huge penalty weights to violations can "force feasibility". This is useful for soft constraints, but it has limitations:
- if the instance is infeasible, the algorithm can keep oscillating between different sets of violations,
- a small set of infeasible outliers can prevent a good plan for the feasible majority,
- penalty tuning becomes fragile, scenario-specific, and operationally risky.

AutoFilter does **not** primarily work by cost. Instead, it:
1. evaluates violations across many candidate schedules,
2. identifies nodes that are systematically responsible, and
3. **removes them from the optimization** so the solver can converge on a feasible/strong plan for the rest.

Think of AutoFilter as **infeasibility management** and "best-effort scheduling under constraints," not as a cost-function tweak.

## Conceptual model: penalty points vs. non-violation occurrences

The [Special Features documentation](https://www.dna-evolutions.com/docs/learn-and-explore/special/special_features#autofilter) describes the underlying idea:

- JOpt creates **many parallel solutions**, the individuals of its population (`JOpt.JOptPopulationSize`); each solution is a complete schedule of all nodes and resources.
- For each solution, the AutoFilter identifies the nodes that caused violations and assigns **penalty points** to those nodes.
- Over time, nodes accumulate penalty points.
- If the ratio of "penalty points" relative to the number of solutions where the node did **not** show violations becomes too high, the node is filtered out.

The key operational implication:

> AutoFilter does not need a node to violate in every solution.
> If a node repeatedly "shows up as the violator" across many solutions, it becomes a candidate for exclusion.

This prevents cases where infeasibility "moves around" across alternative schedules:
- Solution A violates Node D late,
- Solution B violates Node A late,
- both nodes accumulate penalty points,
- the system can then filter the "worst offenders" and stabilize the planning problem.

## What counts as a "violation" for AutoFilter?

AutoFilter can be configured to react to specific violation categories. The official properties list defines **SelectiveAutoFilter** categories that you can enable or disable.

Common categories include:
- time window violations: early / late,
- working hours exceeded,
- route distance exceeded,
- capacity overload,
- resource mismatch (preferred/mandatory mismatch),
- relationship violations,
- node type mismatch,
- double booking,
- nodes positioned after an end anchor.

The exact categories and names are property keys (see the configuration section below).

## Two modes: AutoFilter vs. SelectiveAutoFilter

JOpt supports two primary activation modes:

### Mode A: `JOpt.AutoFilter` (full AutoFilter switch)
- Property: `JOpt.AutoFilter`
- Both example classes set it to `FALSE`: they use Mode B.

This is the "turn it on" switch. It enables AutoFilter and lets the system use the AutoFilter configuration.

### Mode B: `JOpt.SelectiveAutoFilter` (explicit category selection)
- Property: `JOpt.SelectiveAutoFilter`
- Both example classes set it to `TRUE`.

This mode is intentionally conservative: you enable the mechanism, and then explicitly enable the violation categories that should contribute to filtering decisions.

**Recommended operational default:** start with **SelectiveAutoFilter**, enable only the categories you can explain to the business, and expand as you gain confidence.

## Configuration reference: AutoFilter-related properties (authoritative)

The following properties are listed under **Category: AutoFilter** in the official Optimization Properties documentation.

### Switches

- `JOpt.AutoFilter` (both example classes set it to `FALSE`)
  Turns AutoFilter on/off.

- `JOpt.SelectiveAutoFilter` (both example classes set it to `TRUE`)
  Turns selective AutoFilter on/off. If enabled, you must enable the desired violation categories explicitly.

- `JOpt.AutoFilter.useStrictFilterOnLastExecution` (both example classes set it to `TRUE`)
  Enables strict mode for the last execution of the AutoFilter.

**Interpretation (practical):** strict mode at the end is commonly used to ensure the final solution is stabilized and the "worst" offenders have been filtered if needed.

### Margin properties (tolerance windows)

Margins define **how much violation is tolerated** before counting it as violation for filtering purposes.

- `JOpt.AutoFilter.LateArrivalMargin` (both example classes set it to `0`)
- `JOpt.AutoFilter.EarlyArrivalMargin` (both example classes set it to `0`)
- `JOpt.AutoFilter.WorkingHoursExceedMargin` (both example classes set it to `0`, unit: **seconds**)
- `JOpt.AutoFilter.RouteDistanceExceedMargin` (both example classes set it to `0`, unit: **meter**)

**Operational guidance:**
- Set margins to avoid filtering nodes for negligible "seconds late" or "meters over" deviations that are operationally acceptable.
- If your domain can accept slight lateness, a late margin can prevent unnecessary exclusions.
- When you need strict SLA adherence, keep margins small/zero.

### Selective categories (the example classes set each to `FALSE`, except `TimeWindow.Late` in `AutoFilterLateExample`)

Enable any of the following only if you want them to contribute to filtering decisions.

#### Time window categories
- `JOpt.SelectiveAutoFilter.TimeWindow.Early`
  Filters nodes that are visited too early in too many optimization steps.
- `JOpt.SelectiveAutoFilter.TimeWindow.Late`
  Filters nodes that are visited too late in too many optimization steps. `AutoFilterLateExample` sets it to `TRUE`, `AutoFilterParticularNodeExample` to `FALSE`.

#### Allocation / feasibility categories
- `JOpt.SelectiveAutoFilter.DoubleBooking`
  Filters nodes that are potentially double booked too often.
- `JOpt.SelectiveAutoFilter.ResourceMismatch`
  Filters nodes visited too often by wrong resources (preferred or mandatory).
- `JOpt.SelectiveAutoFilter.NodeType`
  Filters nodes visited by the wrong type of resource.
- `JOpt.SelectiveAutoFilter.RelationShip`
  Filters nodes with wrong relations to other nodes.

#### Capacity / route-limit categories
- `JOpt.SelectiveAutoFilter.CapacityOverload`
  Filters nodes that cause capacity overload.
- `JOpt.SelectiveAutoFilter.WorkingHoursExceeded`
  Filters nodes causing working-hours exceedance.
- `JOpt.SelectiveAutoFilter.MaximalRouteDistanceExceeded`
  Filters nodes causing maximal route distance exceedance.
- `JOpt.SelectiveAutoFilter.AfterEndAnchor`
  Filters nodes which are positioned after an end anchor.

## "Instant filtering", custom reasons, and node protection (feature-level capabilities)

The [Special Features documentation](https://www.dna-evolutions.com/docs/learn-and-explore/special/special_features#autofilter) highlights additional capabilities beyond the raw property list:

- **Custom filter reasons** can be defined.
- **Margins** can be set for being filtered out (covered above).
- Certain reasons (e.g., late/early) can be set such that nodes are filtered **instantly** (in the strict mode of the last execution, without the ratio of violations).
- **Individual nodes can be protected** from being filtered out at all.

The Java examples demonstrate how to hook into filtering events and, in the "particular node" scenario, how to attach filtering logic to a single node.

## Java implementation patterns in the Advanced examples

### Example 1: Global (Selective) filtering for lateness

**Example:** `AutoFilterLateExample`

Core configuration intent:
- enable selective filtering,
- enable late time window filtering.

This is the most common "first rollout":
- it targets a category that is easy to explain,
- it tends to isolate outliers (very distant nodes or very tight windows).

#### Recommended operational interpretation
If a node is filtered for lateness, it typically means one of:
- travel time + service time cannot fit within the time window given current resources,
- the node is an outlier relative to geography and shift structure,
- the instance is over-capacitated (too much work for the available working hours).

### Example 2: Filter only a particular node (local constraint)

**Example:** `AutoFilterParticularNodeExample`

This example is strategically important: it demonstrates "targeted optionality".

#### Why you would do this
Not all nodes are equal. In real operations you often have:
- mandatory tasks (must be served),
- optional tasks (nice-to-have, can be deferred),
- tasks that are uncertain (unconfirmed appointments).

Targeted AutoFilter allows you to declare:
> "Only this node (or group of nodes) is allowed to be filtered if it destabilizes feasibility."

This pattern is highly recommended for:
- staged adoption of AutoFilter,
- "drop candidate" nodes,
- operationally optional tasks.

## Observability: detecting when and why nodes were filtered

In production, AutoFilter is only safe if it is transparent:
- you must be able to report *which* tasks were filtered,
- and *why* they were filtered,
- so the business workflow can respond.

JOpt provides two observability surfaces, and the examples use the second:

### 1) Callback-style
- `onNodeFiltering(int code, String message, List<INodeFilterReason> filterReasons)`
- `onNodeFiltering(NodeFilteringEvent nodeFilteringEvent)`

This is suitable for:
- structured logging,
- error reporting pipelines,
- post-processing analysis.

### 2) Event-style
- `getOptimizationEvents().nodeFilteringSubject()`, a stream of `NodeFilteringEvent` objects

This supports:
- event streaming,
- telemetry integration,
- real-time UI status.

**Recommended practice:**
- map `INodeFilterReason` to your domain identifiers (order id, customer id),
- persist filtered nodes as "unplanned tasks" with reason codes,
- trigger re-dispatch workflows (manual review, next-day planning, add a resource).

## How AutoFilter interacts with other concepts

### AutoFilter vs. Pillar/Captured nodes
- Pillars (CapturedNodes) enforce **hard constraints by architecture** (SLAs must be matched).
- AutoFilter manages infeasibility by **removing** nodes that repeatedly cause violations.

These two are complementary:
- use pillars for "must meet SLA" contracts,
- use AutoFilter when you accept "best possible plan" under infeasibility.

### AutoFilter vs. cost weights (JOptWeight.*)
Cost weights determine preference among feasible/near-feasible solutions.
AutoFilter changes which nodes are even considered.

Operationally:
- tune weights to improve quality (e.g., reduce lateness cost, reduce distance),
- enable AutoFilter when you must handle infeasible inputs robustly.

## Practical rollout strategy (recommended)

### Step 1: Decide policy: "drop optional tasks" or "fail hard"
AutoFilter is appropriate when your business can accept:
- a feasible plan for most nodes,
- plus an explicit list of unscheduled tasks.

If you must schedule *all* nodes, AutoFilter is typically the wrong tool; fix feasibility instead:
- add resources,
- relax windows,
- adjust capacity,
- fix travel times/distances.

### Step 2: Start with SelectiveAutoFilter + one category
Recommended first configuration:
- `JOpt.SelectiveAutoFilter = TRUE`
- `JOpt.SelectiveAutoFilter.TimeWindow.Late = TRUE`

Then observe:
- which nodes get filtered,
- how often,
- and whether the result becomes operationally better.

### Step 3: Add margins that reflect operational tolerance
If "2 minutes late" is acceptable, set a late margin; if it isn't, leave it at `0`, as both example classes set it.

### Step 4: Decide whether strict filtering on last execution matches your workflow
Both example classes set `JOpt.AutoFilter.useStrictFilterOnLastExecution` to `TRUE`.
If you want more conservative filtering, you can disable it, but only after careful testing.

### Step 5: Use local constraints for optionality and controlled behavior
Use node-level constraints (like the "particular node" example) to define:
- which nodes are filterable,
- which nodes are protected.

### Step 6: Operationalize the result
Build a standard post-processing contract:
- Planned tasks (routes),
- Filtered tasks (with reasons),
- Violations that remain (if any soft constraints are allowed).

## Interpreting AutoFilter outcomes (what they mean)

If AutoFilter filters a node, it is almost always a signal that one of these is true:

1. **The node is infeasible under the current constraint set**
   (travel time + service time cannot fit into any resource's WorkingHours + node OpeningHours).

2. **The instance is globally over-constrained**
   (too much work, too few resources, too tight windows), and filtering is acting as a "pressure valve".

3. **Resource assignment constraints are too tight**
   (mandatory/preferred resource restrictions eliminate feasible assignments).

4. **Data quality problems**
   - incorrect coordinates,
   - unrealistic travel times,
   - missing or wrong time windows.

AutoFilter is valuable precisely because it converts these situations into:
- a usable plan,
- plus a diagnostic list of problematic nodes.

## Recommended "business-facing" explanation (for customers/dispatchers)

When nodes are filtered, it should be communicated as:

- "These tasks could not be scheduled within the given constraints."
- "They were excluded because they consistently caused violations while the optimizer evaluated many alternative schedules."
- "Here are the reasons and the constraint type (late window, overtime, distance exceed, etc.)."
- "Suggested remediation: relax time window, add resources, or defer to a future day."

This makes the feature transparent and defensible.

## Example configuration templates

### Template A: Conservative late filtering with tolerance
Use when slight lateness is acceptable and you want to avoid dropping nodes for marginal reasons:

- Enable selective filtering
- Enable late
- Set a late margin in seconds (domain-specific)
- Keep strict last execution enabled (as both example classes set it)

### Template B: Strict SLA adherence under infeasibility
Use when you must avoid late arrivals and prefer dropping infeasible tasks:

- Enable selective filtering
- Enable late (and possibly WorkingHoursExceeded, MaximalRouteDistanceExceeded)
- Margins = 0
- Strict last execution enabled

### Template C: Optional task filtering only (controlled exclusion)
Use when only some tasks may be dropped:

- Enable selective filtering
- Attach node-level AutoFilter constraints only to optional nodes
- Keep mandatory nodes protected

## Troubleshooting checklist

### "AutoFilter never filters anything"
- AutoFilter might be disabled (`JOpt.AutoFilter = FALSE` and `JOpt.SelectiveAutoFilter = FALSE`).
- SelectiveAutoFilter enabled but no categories enabled (both example classes set all categories they do not switch on to `FALSE`).
- Violations exist but are within margins (margins too large).

### "It filters too aggressively"
- Reduce categories (start with one).
- Increase margins slightly (late/early/overdistance/overtime).
- Disable strict mode on last execution only after careful testing.
- Prefer node-level constraints for optional nodes only.

### "Filtered nodes are actually feasible"
- Check travel-time/distance model (fallback connector vs external connection).
- Verify OpeningHours / WorkingHours time zones and dates.
- Check preferred/mandatory resource restrictions.

## Summary

- AutoFilter is an infeasibility-management feature that excludes nodes that repeatedly cause violations across many solutions.
- It is not merely "high penalties"; it changes the effective problem instance by removing outlier tasks.
- Use **SelectiveAutoFilter** for controlled rollout; enable only well-understood violation categories.
- Tune **margins** to reflect real operational tolerance (seconds/meters).
- Use node-level constraints to make only specific nodes filterable.
- Always capture filtering events/reasons and expose them to your business workflow.

## Run it

Run the `main(String[] args)` method of `AutoFilterLateExample` or `AutoFilterParticularNodeExample` from your IDE. You need a JDK in the version that the examples pom sets. You need no license file: without one, the examples use the public evaluation license (see the [license guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/license)).

## Related

- [AutoFilter feature guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/autofilter): how the filter decides, the violation categories, the margins and the protection of nodes.
- [Special Features: AutoFilter](https://www.dna-evolutions.com/docs/learn-and-explore/special/special_features#autofilter): the short overview of the AutoFilter on the special features page.
- [Optimization properties](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties): the property reference, with every AutoFilter key and its default.
- [Reactive events and streams](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/reactive_events): the feature guide of the event API that `attachToObservables` uses.
- [Recommended ways to run an optimization](../../basic/recommendedimplementation_02/RecommendedImplementation.md): the recommended run pattern, a synchronous run and the legacy callback methods.
- [Advanced examples](../README.md): the overview of all Advanced examples.

---

Added: 2026-02-17 (JOpt core 7.5.3-j17) | Last updated: 2026-10-21
