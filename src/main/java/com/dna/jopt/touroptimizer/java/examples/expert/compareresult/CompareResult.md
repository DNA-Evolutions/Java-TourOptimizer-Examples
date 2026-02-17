# Compare Result — The Optimization Solution Comparison Tool

Human planners often evaluate a route plan visually and intuitively (“this looks like an unnecessary crossing”).  
At the same time, an optimizer evaluates solutions against a large set of constraints (time windows, skills, visitor/resource rules, overtime prevention, etc.). These two perspectives can diverge.

The **Optimization Solution Comparison Tool** is designed to bridge that gap:

- **Debugging:** validate whether a “simple manual improvement” is truly better once all constraints and costs are considered.
- **End user acceptance:** enable planners to test their intuition (swap/move nodes) and then receive a structured explanation of trade-offs.

Official documentation:
- https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/comparison_tool

---

## References (examples)

This package contains a small suite of examples that all share the same comparison workflow:

- [CompareResultExampleOptimization.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/compareresult/CompareResultExampleOptimization.java) 
- [CompareResultsWithNodeExchangeExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/compareresult/CompareResultsWithNodeExchangeExample.java)
- [CompareResultsWithNodesMoveExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/compareresult/CompareResultsWithNodesMoveExample.java)

---

## The core workflow (conceptual)

The comparison tool implements a very practical 4-step loop:

1. **Compute an initial optimization result** (the “baseline”).
2. **Apply one or more modifications** to create a modified result:
   - swap two nodes in the visit order,
   - move nodes to a different route/day/resource,
   - or apply a list of combined modifications.
3. **Compare baseline vs modified**:
   - which jobs are better/worse,
   - which costs changed,
   - where constraints became violated (or improved).
4. **Generate a report** that is understandable for both engineers and planners.

This is exactly the “planner feedback loop” you want in production:
- “What if we do it like this?” → concrete cost/constraint impact.

---

## What “compare” actually means in JOpt

JOpt assigns every result a **figure of merit** (total cost) and performs fine-grained analysis on top of it.

The examples use:

- `JobAdvantagesController.compare(orgResult, modResult)`

This produces a `JobAdvantageResult` (if the results are structurally comparable).  
The report is then generated via:

- `ICostAdvantagesInterpreter myInterpreter = new CostAdvantageInterpreter();`
- `myInterpreter.generateTextReport(comparissonResult)`

Practical interpretation:
- the tool does not just output “modified is better/worse”,
- it attempts to explain **why**, per job and per cost/constraint dimension.

---

## Example 1: Node Exchange (swap visit order)

**CompareResultsWithNodeExchangeExample** showcases a classic acceptance situation:

- The initial plan visits *Duisburg* before *Krefeld*.
- Visually, this can look like a “geographical mistake” and triggers customer questions.
- The modified solution swaps those two nodes to remove the unnecessary detour.

Implementation pattern:

1. Create initial result
2. Export it to KML (visual inspection)
3. Apply a modification task:
   - `ExchangeOptimizableNodesModificationTask(nodeOneId, nodeTwoId)`
4. Export modified result to KML
5. Compare and print the report

Key benefit:
- You can objectively validate whether the “obvious” swap is truly beneficial after considering:
  - time window knock-on effects,
  - induced idle time,
  - late risks,
  - and cost model effects.

---

## Example 2: Node Move (move jobs between routes / days)

**CompareResultsWithNodesMoveExample** targets a different common question:

> “Can’t the optimizer keep all jobs in a single route?”

The example’s baseline produces **two separate routes**.  
Then it creates a modified result where it moves a chunk of nodes from route 2 into route 1, using:

- `MoveOptimizableNodeModificationTask(moveNodes, newResId, newResWorkingHoursIndex, afterNodeId)`

This is exactly the type of “manual consolidation” planners try.

The comparison report then makes the trade-offs explicit, for example:
- additional overtime risk,
- late arrivals,
- constraint violations introduced by the consolidation,
- or increased travel time.

---

## How result modification works (important for integration)

All modifications are modeled as a list of tasks:

- `List<IModificationTask> tasks = new ArrayList<>();`

Then the list is applied via:

- `OptimizationResultModificationWrapper.getNewResult(opti, tasks)`

This is a clean integration point for UIs:
- each user action becomes an `IModificationTask`,
- you can apply a sequence of UI edits,
- generate a modified result,
- and compare immediately.

Implementation note from the shared helper:
- calling the wrapper with an empty task list produces a shallow-copy style “recovered” result, which can be useful for safe handling in pipelines.

---

## Why this tool improves debugging

In engineering terms, this tool acts like a **counterfactual analyzer**:

- “If we swap these two nodes, what breaks?”
- “If we consolidate routes, which constraint becomes limiting?”
- “If we move jobs to a different day, do time windows remain feasible?”

This helps you identify:
- which constraint is the true driver of the optimizer’s choice,
- whether your cost weights match operational preferences,
- whether the model is missing an important business rule.

It also helps in regression testing:
- compare before/after changes in your modeling or cost model,
- and evaluate the delta in a structured way.

---

## Why this tool improves end user acceptance

Many acceptance challenges are not about correctness, but about explainability:

- “The plan looks weird.”
- “I would have done it differently.”
- “Why can’t we just reorder these two stops?”

The comparison tool turns subjective feedback into measurable facts:
- what improved,
- what got worse,
- and which constraints were violated.

This enables an “evidence-based” planning workflow:
- planners can propose changes,
- the system can immediately explain consequences,
- and the organization can align on a shared objective function.

---

## Recommended production patterns

### Pattern A — “Try my change” button in a planning UI
- User edits the plan (swap, drag-drop to another route, etc.).
- Backend converts the edit into `IModificationTask` entries.
- Backend generates a modified result and runs the comparison.
- UI displays:
  - a summary (“better overall / worse overall”),
  - plus the detailed text report for transparency.

### Pattern B — Explain optimizer decisions
Take a planner-proposed alternative and compare it to the optimizer result:
- if the optimizer is better, show why,
- if the planner change is better, you have actionable feedback:
  - missing constraint,
  - wrong weight calibration,
  - or a model gap.

### Pattern C — Acceptance-driven onboarding
During rollout, allow planners to interactively validate:
- consolidation desires,
- “geographical clean-up” swaps,
- region assignment assumptions,
and build trust quickly.

---

## Summary

- The Comparison Tool lets you modify an existing optimization result and compare the baseline vs the modified version.
- It is valuable for both:
  - engineering/debugging (counterfactual analysis),
  - and business adoption (transparent trade-off explanations).
- The included examples cover the two most common planner edits:
  - swapping nodes (Node Exchange),
  - consolidating/moving nodes between routes (Node Move).
- Reports are generated via `JobAdvantagesController` and interpreted into text via `CostAdvantageInterpreter`.

For background and conceptual explanation, see:
- https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/comparison_tool
