# AutoFilter for late visits

`AutoFilterLateExample` and `AutoFilterParticularNodeExample` give one resource more visits than its two working days hold, and let the AutoFilter take nodes that are visited late too often out of the run. The first class lets the filter act on every node, the second only on the node Nuernberg. The page is for developers who try the AutoFilter on a small plan; the [AutoFilter feature guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/autofilter) explains how the filter decides and lists its properties.

## What the example shows

- **`AutoFilterLateExample`** switches on the selective AutoFilter and its category for late visits with two properties. Every node can then be filtered when it is visited late too often.
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

`AutoFilterLateExample` switches on the selective AutoFilter and the category for late visits:

```java
// The selective AutoFilter acts only on the violation categories that are switched on too.
props.setProperty("JOpt.SelectiveAutoFilter", "TRUE");
// Late visits are the only category here. JOpt.AutoFilter instead of the two keys would
// switch on all categories.
props.setProperty("JOpt.SelectiveAutoFilter.TimeWindow.Late", "TRUE");
```

### Filtering one particular node

`AutoFilterParticularNodeExample` switches on the selective AutoFilter but no category:

```java
// No violation category is switched on, so the AutoFilter acts only on the node that carries
// a filter constraint of its own.
props.setProperty("JOpt.SelectiveAutoFilter", "TRUE");
```

In `addNodes` it gives Nuernberg a `LateAutoFilterConstraint`:

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

## Run it

Run the `main(String[] args)` method of `AutoFilterLateExample` or `AutoFilterParticularNodeExample` from your IDE. You need a JDK in the version that the examples pom sets. You need no license file: without one, the examples use the public evaluation license (see the [license guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/license)).

## Related

- [AutoFilter feature guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/autofilter): how the filter decides, the violation categories, the margins and the protection of nodes.
- [Optimization properties](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties): the property reference, with every AutoFilter key and its default.
- [Reactive events and streams](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/reactive_events): the feature guide of the event API that `attachToObservables` uses.
- [Recommended ways to run an optimization](../../basic/recommendedimplementation_02/RecommendedImplementation.md): the recommended run pattern, a synchronous run and the legacy callback methods.
- [Advanced examples](../README.md): the overview of all Advanced examples.

---

Added: 2026-02-17 (JOpt core 7.5.3-j17) | Last updated: 2026-10-20
