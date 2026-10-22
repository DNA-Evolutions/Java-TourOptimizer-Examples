# Compare an optimization result with a modified result

`CompareResultsWithNodeExchangeExample` and `CompareResultsWithNodesMoveExample` change an optimized result the way a planner would, by swapping two nodes or by moving nodes to another route, and compare the changed result with the original. In both examples the original wins, and the comparison report shows why: a visit at Duisburg that ends after Duisburg closes in the first, late arrivals and overtime in the second. The page is for developers who explain results to planners or check a manual change; the [comparison tool feature guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/comparison_tool) describes the tool itself.

## What the example shows

Human planners often evaluate a route plan visually and intuitively ("this looks like an unnecessary crossing").
At the same time, an optimizer evaluates solutions against a large set of constraints (time windows, skills, visitor/resource rules, overtime prevention, etc.). These two perspectives can diverge.

The **Optimization Solution Comparison Tool** is designed to bridge that gap:

- **Debugging:** validate whether a "simple manual improvement" is truly better once all constraints and costs are considered.
- **End user acceptance:** enable planners to test their intuition (swap/move nodes) and then receive a structured explanation of trade-offs.

Official documentation: the [comparison tool feature guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/comparison_tool).

- **`CompareResultsWithNodeExchangeExample`** runs the optimization, swaps Krefeld and Duisburg in a copy of the result with an `ExchangeOptimizableNodesModificationTask`, and compares the two results.
- **`CompareResultsWithNodesMoveExample`** runs the optimization with two more nodes far away, moves the nodes of the second route to the end of the first route with a `MoveOptimizableNodeModificationTask`, and compares the two results.
- **Both** extend `CompareResultExampleOptimization`, which holds the shared data and steps. They print the events of the run through the event API, the recommended way, and write the original and the modified result to KML files.

## Scenario

Both classes use the same data; the node move example adds two nodes. The dates, times and coordinates are sample values. The time zone is Europe/Berlin.

- **Resource:** `Jack from Aachen`, a `CapacityResource` at 50.775346, 6.083887 (Aachen), with working hours on May 8 and May 9, 2030, from 08:00 to 17:00. He may work up to 9 hours and drive up to 1200 km a day.
- **Nodes:** `TimeWindowGeoNode` objects for Koeln, Essen, Dueren, Wuppertal, Aachen and Krefeld, open on both days from 08:00 to 17:00, and Duisburg, open only on May 8 from 08:00 to 09:45. The node move example adds Nuernberg and Heilbronn, open on both days. Each visit takes 30 minutes, and all nodes have the same importance.
- **Duisburg** closes early on purpose: a route that visits Krefeld first ends the visit at Duisburg after the closing time, which is why the optimizer visits Duisburg before Krefeld.

## How the code works

The sources are [CompareResultsWithNodeExchangeExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/compareresult/CompareResultsWithNodeExchangeExample.java), [CompareResultsWithNodesMoveExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/compareresult/CompareResultsWithNodesMoveExample.java) and their shared base class [CompareResultExampleOptimization.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/compareresult/CompareResultExampleOptimization.java). This package contains a small suite of examples that all share the same comparison workflow. `example()` of the node exchange example:

```java
// The public evaluation license, or your own license if you put a license file where
// ExampleLicenseHelper looks for it.
ExampleLicenseHelper.setLicense(this);
addProperties(this);
addNodes(this, false);
addResources(this);
attachToObservables(this);
IOptimizationResult orgResult = startAndPresentResult(this);
exportToKml(orgResult, "CompareResultsWithNodeExchangeExample_original.kml");
IOptimizationResult modResult = createModResult(this);
exportToKml(modResult, "CompareResultsWithNodeExchangeExample_modified.kml");
compareResults(orgResult, modResult);
```

The node move example differs in two places: `addNodes(this, true)` adds Nuernberg and Heilbronn, and its `createModResult` takes the original result, because it reads the nodes of the second route from it. Both set `JOptExitCondition.JOptGenerationCount` and `JOpt.Algorithm.PreOptimization.SA.NumIterations` below their defaults, which keeps the run short. `startAndPresentResult` starts the run, waits up to 5 minutes for the result, prints it and returns it.

### Swapping two nodes

`exchangeNodes` wraps an `ExchangeOptimizableNodesModificationTask` in a list of tasks; `createModResult` calls it with Krefeld and Duisburg:

```java
IModificationTask exchangeTask =
    new ExchangeOptimizableNodesModificationTask(nodeOneId, nodeTwoId);

List<IModificationTask> tasks = new ArrayList<>();
tasks.add(exchangeTask);

return applyTasksAndCreateModResult(opti, tasks);
```

### Moving the nodes of a route

`moveSecondRouteNodesToFirstRoute` reads the node ids of the second route in the order of arrival and moves them to Jack's first working day:

```java
// Target: Jack's first working day (index 0). Optional.empty() appends the moved nodes at
// the end of the target route; a node id instead puts them before that node.
IModificationTask moveTask =
    new MoveOptimizableNodeModificationTask(
        secondRouteItemIds, "Jack from Aachen", 0, Optional.empty());
```

### Creating the modified result

`applyTasksAndCreateModResult` in the base class hands the tasks to an `OptimizationResultModificationWrapper`:

```java
OptimizationResultModificationWrapper wrapper = new OptimizationResultModificationWrapper();
// The wrapper copies the optimization, applies the tasks to the copy and assesses the copy
// once, without a new search.
return wrapper.getNewResult(opti, tasks);
```

### Comparing the two results

`compareResults` compares the results with `JobAdvantagesController.compare` and turns the `JobAdvantageResult` into a text report with a `CostAdvantageInterpreter`:

```java
Optional<JobAdvantageResult> comparisonResultOptional =
    JobAdvantagesController.compare(orgResult, modResult);

if (comparisonResultOptional.isPresent()) {
  JobAdvantageResult comparisonResult = comparisonResultOptional.get();

  ICostAdvantagesInterpreter myInterpreter = new CostAdvantageInterpreter();
```

`exportToKml` writes a result to a KML file in a try-with-resources block and prints "KML export failed: " with the file name if the export returns false.

## Output and what to look for

Each class prints the original result under `RESULT ORG`, the modified result under `RESULT MOD`, and then the comparison report under `COMPARISON RESULT`. In two test runs each class gave the same routes and costs. The report has five numbered parts: the job result (the cost of each whole run), the violation summaries, the restrictions sorted by their cost difference, the restrictions with the largest positive and the largest negative difference, and the routes behind those two.

### CompareResultsWithNodeExchangeExample

The original route visits Duisburg before Krefeld, trimmed to the first two stops:

```text
0.0 Duisburg / Geo  / Arrival: 08.05.2030 09:09:22 (Europe/Berlin) , Departure: 08.05.2030 09:39:22 (Europe/Berlin) / Duration: 30.0 [min] / Driving: 69.37 [min], 91.57 [km]
0.1 Krefeld / Geo  / Arrival: 08.05.2030 09:55:31 (Europe/Berlin) , Departure: 08.05.2030 10:25:31 (Europe/Berlin) / Duration: 30.0 [min] / Driving: 16.16 [min], 21.33 [km]
```

In the modified route Krefeld comes first. Duisburg is reached at 09:39:27, before it closes at 09:45, but its 30-minute visit ends 24.46 minutes after the closing time:

```text
0.0 Krefeld / Geo  / Arrival: 08.05.2030 08:53:17 (Europe/Berlin) , Departure: 08.05.2030 09:23:17 (Europe/Berlin) / Duration: 30.0 [min] / Driving: 53.3 [min], 70.35 [km]
0.1 Duisburg / Geo  / Arrival: 08.05.2030 09:39:27 (Europe/Berlin) , Departure: 08.05.2030 10:09:27 (Europe/Berlin) / Duration: 30.0 [min] / Driving: 16.17 [min], 21.34 [km][ CONSTRAINTVIOLATION/TIMECONSTRAINT/LATE/ Late time [min]: 24.464716666666668 Value: 24.464716666666668 ]
```

The modified route is 237.71 km long instead of 272.04 km, but the late visit costs more than the distance saves. The job result of the report, without its two ident lines:

```text
Cost Base    : 306.71521617219287
Cost Compare : 316.3991459910722
Cost Diff    : -9.683929818879335
Result       : Base job has lower cost.
```

The route part of the report lists the distance as the restriction where the modified result has the lower cost (`Cost Diff    : 34.33032257339144`) and the lateness as the one where the original is better (`Cost Diff    : -46.18157322560417`).

### CompareResultsWithNodesMoveExample

The original result has two routes. The first, on May 8, is the route of the node exchange example; the second, on May 9, visits Nuernberg and Heilbronn and ends at 19:13:10 with 673.18 minutes, above the 9 hours of the day:

```text
[ CONSTRAINTVIOLATION/MAXTIME/EXCEEDED/ Overtime [h]: 2.2196599999999993 Value: 2.2196599999999993 ]
```

The modified result appends Nuernberg and Heilbronn to the route of May 8. That route ends at 02:09:16 on the next day, with 9.15 hours of overtime and two late arrivals:

```text
0.7 Nuernberg / Geo  / Arrival: 08.05.2030 19:44:00 (Europe/Berlin) , Departure: 08.05.2030 20:14:00 (Europe/Berlin) / Duration: 30.0 [min] / Driving: 287.91 [min], 380.04 [km][ CONSTRAINTVIOLATION/TIMECONSTRAINT/LATE/ Late time [min]: 194.00333333333336 Value: 194.00333333333336 ]
0.8 Heilbronn / Geo  / Arrival: 08.05.2030 21:58:25 (Europe/Berlin) , Departure: 08.05.2030 22:28:25 (Europe/Berlin) / Duration: 30.0 [min] / Driving: 104.42 [min], 137.83 [km][ CONSTRAINTVIOLATION/TIMECONSTRAINT/LATE/ Late time [min]: 328.42036666666667 Value: 328.42036666666667 ]
```

The job result of the report, without its two ident lines:

```text
Cost Base    : 3371.1834522956833
Cost Compare : 99054.57026575043
Cost Diff    : -95683.38681345475
Result       : Base job has lower cost.
```

In both results the job has the same distance (1081.44 km) and the same route time (1089.27 minutes); the difference comes from the late arrivals and the overtime. Both reports print the same `Base Ident` and `Compare Ident`: the modified result keeps the ident of the run.

## The core workflow (conceptual)

The comparison tool implements a very practical 4-step loop:

1. **Compute an initial optimization result** (the "baseline").
2. **Apply one or more modifications** to create a modified result:
   - swap two nodes in the visit order,
   - move nodes to a different route/day/resource,
   - or apply a list of combined modifications.
3. **Compare baseline vs modified**:
   - whether the original or the modified job (the whole run) is better or worse,
   - which costs changed,
   - where constraints became violated (or improved).
4. **Generate a report** that is understandable for both engineers and planners.

This is the "planner feedback loop" of a production system:
- "What if we do it like this?" gives the concrete cost/constraint impact.

## What "compare" actually means in JOpt

JOpt assigns every result a **figure of merit** (total cost) and performs fine-grained analysis on top of it.

The examples use:

- `JobAdvantagesController.compare(orgResult, modResult)`

This produces a `JobAdvantageResult` (if the results are structurally comparable). If the two results do not have the same routes (the number of routes, the resource and the working-hours index of each route), `compare` throws a `ResultStructureNotMatchingException`. It returns an empty `Optional`, and `compareResults` prints "Comparison failed", when none of the restrictions gives a result across the routes while the violation summaries of the two results differ.
The report is then generated via:

- `ICostAdvantagesInterpreter myInterpreter = new CostAdvantageInterpreter();`
- `myInterpreter.generateTextReport(comparisonResult)`

Practical interpretation:
- the tool does not just output "modified is better/worse",
- it attempts to explain **why**, for the job as a whole, per route and per cost/constraint dimension.

## Example 1: node exchange (swap visit order)

**CompareResultsWithNodeExchangeExample** showcases a classic acceptance situation:

- The initial plan visits *Duisburg* before *Krefeld*.
- Visually, this can look like a "geographical mistake" and triggers customer questions.
- The modified solution swaps those two nodes to remove what looks like an unnecessary detour.

Implementation pattern:

1. Create initial result
2. Export it to KML (visual inspection)
3. Apply a modification task:
   - `ExchangeOptimizableNodesModificationTask(nodeOneId, nodeTwoId)`
4. Export modified result to KML
5. Compare and print the report

Key benefit:
- You can objectively validate whether the "obvious" swap is truly beneficial after considering:
  - time window knock-on effects,
  - induced idle time,
  - late risks,
  - and cost model effects.

In the example the swap is not beneficial: it saves 34.33 km, and the visit at Duisburg ends 24.46 minutes after Duisburg closes.

## Example 2: node move (move jobs between routes / days)

**CompareResultsWithNodesMoveExample** targets a different common question:

> "Can't the optimizer keep all jobs in a single route?"

The example's baseline produces **two separate routes**; the second already has overtime.
Then it creates a modified result where it moves all nodes of route 2 to the end of route 1, using:

- `MoveOptimizableNodeModificationTask(moveNodes, newResId, newResWorkingHoursIndex, afterNodeId)`

This is exactly the type of "manual consolidation" planners try.

The comparison report then makes the trade-offs explicit, for example:
- additional overtime risk,
- late arrivals,
- constraint violations introduced by the consolidation,
- or increased travel time.

In the example the consolidation brings late arrivals and more overtime, at the same distance and route time.

## How result modification works (important for integration)

All modifications are modeled as a list of tasks:

- `List<IModificationTask> tasks = new ArrayList<>();`

Then the list is applied via:

- `getNewResult(opti, tasks)` of an `OptimizationResultModificationWrapper`

This is a clean integration point for UIs:
- each user action becomes an `IModificationTask`,
- you can apply a sequence of UI edits,
- generate a modified result,
- and compare immediately.

Implementation note from the shared helper:
- the wrapper copies the optimization, applies the tasks to the copy and assesses the copy once, without a new search; calling the wrapper with an empty task list produces a "recovered" result, a new assessment of the unchanged copy, which can be useful for safe handling in pipelines.
- a task that the core cannot apply, for example because a node id is unknown or the target route does not exist, leaves the copy unchanged without an error; the core writes only a debug log line, so check the modified result.

## Why this tool improves debugging

In engineering terms, this tool acts like a **counterfactual analyzer**:

- "If we swap these two nodes, what breaks?"
- "If we consolidate routes, which constraint becomes limiting?"
- "If we move jobs to a different day, do time windows remain feasible?"

This helps you identify:
- which constraint is the true driver of the optimizer's choice,
- whether your cost weights match operational preferences,
- whether the model is missing an important business rule.

It also helps in regression testing:
- compare before/after changes in your modeling or cost model,
- and evaluate the delta in a structured way.

## Why this tool improves end user acceptance

Many acceptance challenges are not about correctness, but about explainability:

- "The plan looks weird."
- "I would have done it differently."
- "Why can't we just reorder these two stops?"

The comparison tool turns subjective feedback into measurable facts:
- what improved,
- what got worse,
- and which constraints were violated.

This enables an "evidence-based" planning workflow:
- planners can propose changes,
- the system can immediately explain consequences,
- and the organization can align on a shared objective function.

## Recommended production patterns

### Pattern A: a "Try my change" button in a planning UI
- User edits the plan (swap, drag-drop to another route, etc.).
- Backend converts the edit into `IModificationTask` entries.
- Backend generates a modified result and runs the comparison.
- UI displays:
  - a summary ("better overall / worse overall"),
  - plus the detailed text report for transparency.

### Pattern B: explain optimizer decisions
Take a planner-proposed alternative and compare it to the optimizer result:
- if the optimizer is better, show why,
- if the planner change is better, you have actionable feedback:
  - missing constraint,
  - wrong weight calibration,
  - or a model gap.

### Pattern C: acceptance-driven onboarding
During rollout, allow planners to interactively validate:
- consolidation desires,
- "geographical clean-up" swaps,
- region assignment assumptions,
and build trust.

## Summary

- The Comparison Tool lets you modify an existing optimization result and compare the baseline vs the modified version.
- It is valuable for both:
  - engineering/debugging (counterfactual analysis),
  - and business adoption (transparent trade-off explanations).
- The included examples cover the two most common planner edits:
  - swapping nodes (Node Exchange),
  - consolidating/moving nodes between routes (Node Move).
- Reports are generated via `JobAdvantagesController` and interpreted into text via `CostAdvantageInterpreter`.

For background and conceptual explanation, see the [comparison tool feature guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/comparison_tool).

## Run it

Run the `main(String[] args)` method of `CompareResultsWithNodeExchangeExample` or `CompareResultsWithNodesMoveExample` from your IDE. You need a JDK in the version that the examples pom sets. You need no license file: without one, the examples use the public evaluation license (see the [license guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/license)). Each class writes two KML files, `<class name>_original.kml` and `<class name>_modified.kml`, into the working directory.

## Related

- [Comparison tool feature guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/comparison_tool): the comparison tool, its report and its limits.
- [Custom solutions as a warm start](../customsolution/CustomSolution.md): a start from a plan built by hand, the other way to bring a planner's plan into a run.
- [Reactive events and streams](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/reactive_events): the feature guide of the event API that `attachToObservables` uses.
- [Recommended ways to run an optimization](../../basic/recommendedimplementation_02/RecommendedImplementation.md): the recommended run pattern, a synchronous run and the legacy callback methods.
- [Expert examples](../README.md): the overview of all Expert examples.

---

Added: 2026-02-17 (JOpt core 7.5.3-j17) | Last updated: 2026-10-23
