# AutoFilter — Infeasibility Management by Excluding Violation-Prone Nodes

The **AutoFilter** is one of the most important operational features of **JOpt TourOptimizer**: it enables the optimizer to **exclude (“filter out”) nodes that repeatedly cause violations** across many candidate solutions.

In many real-world routing/scheduling instances, infeasibility is not global; it is often caused by a small subset of nodes:
- a customer is too far away to be reached within its SLA,
- too many tasks are packed into too little WorkingHours capacity,
- a subset of nodes require a specific resource (preferred/mandatory) and overload it,
- constraints interact in a way that makes a few nodes “structural outliers”.

If those nodes remain in the problem throughout the run, the optimization can spend disproportionate effort exploring infeasible regions and may converge slowly—or not at all—to a useful schedule.

AutoFilter addresses this by changing the **effective problem instance during the run**: nodes that are systematically causing violations are removed so the optimizer can produce a high-quality plan for the remaining nodes.

---

## References

### Core concept documentation
- [Special Features — AutoFilter](https://www.dna-evolutions.com/docs/learn-and-explore/special/special_features#autofilter)

### Property reference (authoritative)
- [Optimization Properties — AutoFilter category](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties)

### Java implementation examples
- [AutoFilterLateExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/autofilter/AutoFilterLateExample.java)
- [AutoFilterParticularNodeExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/autofilter/AutoFilterParticularNodeExample.java)

---

## AutoFilter in one sentence

**AutoFilter continuously analyzes which nodes caused violations in each generated solution and filters out nodes whose violation pattern becomes dominant.**

---

## Why AutoFilter is fundamentally different from “high penalty costs”

Many optimization systems attempt to “force feasibility” by attaching huge penalty weights to violations. This is useful for soft constraints, but it has limitations:
- if the instance is infeasible, the algorithm can keep oscillating between different sets of violations,
- a small set of infeasible outliers can prevent a good plan for the feasible majority,
- penalty tuning becomes fragile, scenario-specific, and operationally risky.

AutoFilter does **not** primarily work by cost. Instead, it:
1. evaluates violations across many candidate schedules,
2. identifies nodes that are systematically responsible, and
3. **removes them from the optimization** so the solver can converge on a feasible/strong plan for the rest.

Think of AutoFilter as **infeasibility management** and “best-effort scheduling under constraints,” not as a cost-function tweak.

---

## Conceptual model: penalty points vs. non-violation occurrences

The Special Features documentation describes the underlying idea:

- JOpt creates a **massive number of parallel solutions**; each solution is a complete schedule of all nodes and resources.
- For each solution, the AutoFilter identifies the nodes that caused violations and assigns **penalty points** to those nodes.
- Over time, nodes accumulate penalty points.
- If the ratio of “penalty points” relative to the number of solutions where the node did **not** show violations becomes too high, the node is filtered out.

The key operational implication:

> AutoFilter does not need a node to violate in every solution.
> If a node repeatedly “shows up as the violator” across many solutions, it becomes a candidate for exclusion.

This prevents cases where infeasibility “moves around” across alternative schedules:
- Solution A violates Node D late,
- Solution B violates Node A late,
- both nodes accumulate penalty points,
- the system can then filter the “worst offenders” and stabilize the planning problem.

---

## What counts as a “violation” for AutoFilter?

AutoFilter can be configured to react to specific violation categories. The official properties list defines **SelectiveAutoFilter** categories that you can enable or disable.

Common categories include:
- time window violations: early / late,
- working hours exceeded,
- route distance exceeded,
- capacity overload,
- resource mismatch (preferred/mandatory mismatch),
- relationship violations,
- node type mismatch,
- double booking.

The exact categories and names are property keys (see the configuration section below).

---

## Two modes: AutoFilter vs. SelectiveAutoFilter

JOpt supports two primary activation modes:

### Mode A — `JOpt.AutoFilter` (full AutoFilter switch)
- Property: `JOpt.AutoFilter`
- Default: `FALSE`

This is the “turn it on” switch. It enables AutoFilter and lets the system use the AutoFilter configuration.

### Mode B — `JOpt.SelectiveAutoFilter` (explicit category selection)
- Property: `JOpt.SelectiveAutoFilter`
- Default: `FALSE`

This mode is intentionally conservative: you enable the mechanism, and then explicitly enable the violation categories that should contribute to filtering decisions.

**Recommended operational default:** start with **SelectiveAutoFilter**, enable only the categories you can explain to the business, and expand as you gain confidence.

---

## Configuration reference: AutoFilter-related properties (authoritative)

The following properties are listed under **Category: AutoFilter** in the official Optimization Properties documentation.

### Switches

- `JOpt.AutoFilter` (default `FALSE`)
  Turns AutoFilter on/off.

- `JOpt.SelectiveAutoFilter` (default `FALSE`)
  Turns selective AutoFilter on/off. If enabled, you must enable the desired violation categories explicitly.

- `JOpt.AutoFilter.useStrictFilterOnLastExecution` (default `TRUE`)
  Enables strict mode for the last execution of the AutoFilter.

**Interpretation (practical):** strict mode at the end is commonly used to ensure the final solution is stabilized and the “worst” offenders have been filtered if needed.

---

### Margin properties (tolerance windows)

Margins define **how much violation is tolerated** before counting it as violation for filtering purposes.

- `JOpt.AutoFilter.LateArrivalMargin` (default `0`)
- `JOpt.AutoFilter.EarlyArrivalMargin` (default `0`)
- `JOpt.AutoFilter.WorkingHoursExceedMargin` (default `0`, unit: **seconds**)
- `JOpt.AutoFilter.RouteDistanceExceedMargin` (default `0`, unit: **meter**)

**Operational guidance:**
- Set margins to avoid filtering nodes for negligible “seconds late” or “meters over” deviations that are operationally acceptable.
- If your domain can accept slight lateness, a late margin can prevent unnecessary exclusions.
- When you need strict SLA adherence, keep margins small/zero.

---

### Selective categories (each default `FALSE`)

Enable any of the following only if you want them to contribute to filtering decisions.

#### Time window categories
- `JOpt.SelectiveAutoFilter.TimeWindow.Early`
  Filters nodes that are visited too early in too many optimization steps.
- `JOpt.SelectiveAutoFilter.TimeWindow.Late`
  Filters nodes that are visited too late in too many optimization steps.

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

---

## “Instant filtering”, custom reasons, and node protection (feature-level capabilities)

The Special Features documentation highlights additional capabilities beyond the raw property list:

- **Custom filter reasons** can be defined.
- **Margins** can be set for being filtered out (covered above).
- Certain reasons (e.g., late/early) can be set such that nodes are filtered **instantly**.
- **Individual nodes can be protected** from being filtered out at all.

The Java examples demonstrate how to hook into filtering events and, in the “particular node” scenario, how to attach filtering logic to a single node.

---

## Java implementation patterns in the Advanced examples

### Example 1 — Global (Selective) filtering for lateness

**Example:** `AutoFilterLateExample`

Core configuration intent:
- enable selective filtering,
- enable late time window filtering.

This is the most common “first rollout”:
- it targets a category that is easy to explain,
- it tends to isolate outliers (very distant nodes or very tight windows).

#### Recommended operational interpretation
If a node is filtered for lateness, it typically means one of:
- travel time + service time cannot fit within the time window given current resources,
- the node is an outlier relative to geography and shift structure,
- the instance is over-capacitated (too much work for the available working hours).

---

### Example 2 — Filter only a particular node (local constraint)

**Example:** `AutoFilterParticularNodeExample`

This example is strategically important: it demonstrates “targeted optionality”.

#### Why you would do this
Not all nodes are equal. In real operations you often have:
- mandatory tasks (must be served),
- optional tasks (nice-to-have, can be deferred),
- tasks that are uncertain (unconfirmed appointments).

Targeted AutoFilter allows you to declare:
> “Only this node (or group of nodes) is allowed to be filtered if it destabilizes feasibility.”

#### What the example does
- Enables selective filtering (`JOpt.SelectiveAutoFilter = TRUE`).
- Creates a local constraint (`LateAutoFilterConstraint`) implementing `IAutoFilterNodeConstraint`.
- Attaches that constraint to a single node (Nuernberg) via:
  - `nuernberg.setAutoFilterConstraints(nodeConstraints);`

Effect:
- the AutoFilter logic is focused on that node,
- other nodes are not filtered by that specific rule unless configured elsewhere.

This pattern is highly recommended for:
- staged adoption of AutoFilter,
- “drop candidate” nodes,
- operationally optional tasks.

---

## Observability: detecting when and why nodes were filtered

In production, AutoFilter is only safe if it is transparent:
- you must be able to report *which* tasks were filtered,
- and *why* they were filtered,
- so the business workflow can respond.

The examples provide two observability surfaces:

### 1) Callback-style
- `onNodeFiltering(int code, String message, List<INodeFilterReason> filterReasons)`

This is suitable for:
- structured logging,
- error reporting pipelines,
- post-processing analysis.

### 2) Event-style
- `onNodeFiltering(NodeFilteringEvent nodeFilteringEvent)`

This supports:
- event streaming,
- telemetry integration,
- real-time UI status.

**Recommended practice:**
- map `INodeFilterReason` to your domain identifiers (order id, customer id),
- persist filtered nodes as “unplanned tasks” with reason codes,
- trigger re-dispatch workflows (manual review, next-day planning, add a resource).

---

## How AutoFilter interacts with other concepts

### AutoFilter vs. Pillar/Captured nodes
- Pillars (CapturedNodes) enforce **hard constraints by architecture** (SLAs must be matched).
- AutoFilter manages infeasibility by **removing** nodes that repeatedly cause violations.

These two are complementary:
- use pillars for “must meet SLA” contracts,
- use AutoFilter when you accept “best possible plan” under infeasibility.

### AutoFilter vs. cost weights (JOptWeight.*)
Cost weights determine preference among feasible/near-feasible solutions.
AutoFilter changes which nodes are even considered.

Operationally:
- tune weights to improve quality (e.g., reduce lateness cost, reduce distance),
- enable AutoFilter when you must handle infeasible inputs robustly.

---

## Practical rollout strategy (recommended)

### Step 1 — Decide policy: “drop optional tasks” or “fail hard”
AutoFilter is appropriate when your business can accept:
- a feasible plan for most nodes,
- plus an explicit list of unscheduled tasks.

If you must schedule *all* nodes, AutoFilter is typically the wrong tool; fix feasibility instead:
- add resources,
- relax windows,
- adjust capacity,
- fix travel times/distances.

### Step 2 — Start with SelectiveAutoFilter + one category
Recommended first configuration:
- `JOpt.SelectiveAutoFilter = TRUE`
- `JOpt.SelectiveAutoFilter.TimeWindow.Late = TRUE`

Then observe:
- which nodes get filtered,
- how often,
- and whether the result becomes operationally better.

### Step 3 — Add margins that reflect operational tolerance
If “2 minutes late” is acceptable, set a late margin; if it isn’t, leave it at `0`.

### Step 4 — Decide whether strict filtering on last execution matches your workflow
`JOpt.AutoFilter.useStrictFilterOnLastExecution` defaults to `TRUE`.
If you want more conservative filtering, you can disable it, but only after careful testing.

### Step 5 — Use local constraints for optionality and controlled behavior
Use node-level constraints (like the “particular node” example) to define:
- which nodes are filterable,
- which nodes are protected.

### Step 6 — Operationalize the result
Build a standard post-processing contract:
- Planned tasks (routes),
- Filtered tasks (with reasons),
- Violations that remain (if any soft constraints are allowed).

---

## Interpreting AutoFilter outcomes (what they mean)

If AutoFilter filters a node, it is almost always a signal that one of these is true:

1. **The node is infeasible under the current constraint set**
   (travel time + service time cannot fit into any resource’s WorkingHours + node OpeningHours).

2. **The instance is globally over-constrained**
   (too much work, too few resources, too tight windows), and filtering is acting as a “pressure valve”.

3. **Resource assignment constraints are too tight**
   (mandatory/preferred resource restrictions eliminate feasible assignments).

4. **Data quality problems**
   - incorrect coordinates,
   - unrealistic travel times,
   - missing or wrong time windows.

AutoFilter is valuable precisely because it converts these situations into:
- a usable plan,
- plus a diagnostic list of problematic nodes.

---

## Recommended “business-facing” explanation (for customers/dispatchers)

When nodes are filtered, it should be communicated as:

- “These tasks could not be scheduled within the given constraints.”
- “They were excluded because they consistently caused violations while the optimizer evaluated many alternative schedules.”
- “Here are the reasons and the constraint type (late window, overtime, distance exceed, etc.).”
- “Suggested remediation: relax time window, add resources, or defer to a future day.”

This makes the feature transparent and defensible.

---

## Example configuration templates

### Template A — Conservative late filtering with tolerance
Use when slight lateness is acceptable and you want to avoid dropping nodes for marginal reasons:

- Enable selective filtering
- Enable late
- Set a late margin in seconds (domain-specific)
- Keep strict last execution enabled (default)

### Template B — Strict SLA adherence under infeasibility
Use when you must avoid late arrivals and prefer dropping infeasible tasks:

- Enable selective filtering
- Enable late (and possibly WorkingHoursExceeded, DistanceExceeded)
- Margins = 0
- Strict last execution enabled

### Template C — Optional task filtering only (controlled exclusion)
Use when only some tasks may be dropped:

- Enable selective filtering
- Attach node-level AutoFilter constraints only to optional nodes
- Keep mandatory nodes protected

---

## Troubleshooting checklist

### “AutoFilter never filters anything”
- AutoFilter might be disabled (`JOpt.AutoFilter = FALSE` and `JOpt.SelectiveAutoFilter = FALSE`).
- SelectiveAutoFilter enabled but no categories enabled (all defaults are `FALSE`).
- Violations exist but are within margins (margins too large).

### “It filters too aggressively”
- Reduce categories (start with one).
- Increase margins slightly (late/early/overdistance/overtime).
- Disable strict mode on last execution only after careful testing.
- Prefer node-level constraints for optional nodes only.

### “Filtered nodes are actually feasible”
- Check travel-time/distance model (fallback connector vs external connection).
- Verify OpeningHours / WorkingHours time zones and dates.
- Check preferred/mandatory resource restrictions.

---

## Summary

- AutoFilter is an infeasibility-management feature that excludes nodes that repeatedly cause violations across many solutions.
- It is not merely “high penalties”; it changes the effective problem instance by removing outlier tasks.
- Use **SelectiveAutoFilter** for controlled rollout; enable only well-understood violation categories.
- Tune **margins** to reflect real operational tolerance (seconds/meters).
- Use node-level constraints to make only specific nodes filterable.
- Always capture filtering events/reasons and expose them to your business workflow.
