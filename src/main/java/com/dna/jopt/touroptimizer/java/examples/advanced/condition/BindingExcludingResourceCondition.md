# Mandatory, preferred, banned and unpreferred resources for a node

Four examples decide which resource visits a node. `MandatoryResourceConditionExample` and `PreferredResourceConditionExample` bind a node to a resource, while `BannedResourceConditionExample` and `UnPreferredResourceConditionExample` keep a resource away from a node. Mandatory and banned resources are hard constraints, preferred and unpreferred resources are soft ones. The page is for developers who restrict or steer which resource visits a node, and it uses the nodes and resources of the [Basic elements tutorial](https://www.dna-evolutions.com/docs/getting-started/tutorials/basic-elements).

## What the example shows

- **A resource constraint** is an `IConstraintResource`. The examples create one, add a resource by its id with `addResource(id, priority)` and attach the constraint to a node with `addConstraint`.
- **Binding constraints** name the resources that should visit the node:
  - `MandatoryResourceConstraint` is hard: only the named resources may visit the node.
  - `PreferredResourceConstraint` is soft: a preferred resource is not guaranteed. The optimizer may give the node to another resource if the preferred one costs too much.
- **Excluding constraints** name the resources that should not visit the node:
  - `BannedResourceConstraint` is hard: the named resources may not visit the node.
  - `UnPreferredResourceConstraint` is soft: the optimizer avoids the named resources where it can.
- **Hard and soft:** A node that would break a hard resource condition stays unassigned, with a filter reason. In `UnPreferredResourceConditionExample` the soft constraint is broken for Stuttgart: the node is still planned, and the result shows a `RESOURCE_NOT_ALLOWED` violation and an extra cost. The properties `JOptWeight.PreferableResource` and `JOptWeight.UnPreferableResource` set the weight of that extra cost.
- **The priority:** `addResource` needs a priority above 0. The binding constraints of these examples name one resource each, so the value of the priority does not matter there. A banned resource is kept away whatever its priority. For an unpreferred resource, the priority scales the extra cost of a visit.

Each class writes its result to a KML file of its own.

## Scenario

The dates, times and coordinates are sample values. The time zone is Europe/Berlin.

- **Resources:** `Jack` and `John`, two `CapacityResource` objects at 50.775346, 6.083887 (Aachen). Both have working hours on March 6 and March 7, 2020, from 08:00 to 17:00, and may drive up to 1200 km a day. Jack may work up to 8 hours a day, John up to 14 hours.
- **Nodes:** eight `TimeWindowGeoNode` objects, open on March 6 and March 7 from 08:00 to 17:00, each with a visit of 20 minutes and the same importance. Koeln and Koeln1 have the same position, 50.9333, 6.95.
  - `MandatoryResourceConditionExample`, `BannedResourceConditionExample` and `UnPreferredResourceConditionExample`: Koeln, Koeln1, Oberhausen, Essen, Heilbronn, Stuttgart, Wuppertal and Aachen.
  - `PreferredResourceConditionExample`: Koeln, Koeln1, Oberhausen, Essen, Dueren, Nuernberg, Stuttgart and Aachen.
- **Constraints:**
  - `MandatoryResourceConditionExample`: a mandatory resource constraint on Koeln1 that names John. Koeln has no constraint.
  - `PreferredResourceConditionExample`: a preferred resource constraint on Koeln that names Jack, and one on Koeln1 that names John.
  - `BannedResourceConditionExample`: a banned resource constraint on Koeln1 that names John, and one on Stuttgart that names both Jack and John.
  - `UnPreferredResourceConditionExample`: an unpreferred resource constraint on Koeln1 that names John, and one on Stuttgart that names both Jack and John.

The classes set no properties, so the runs use the defaults of the [property reference](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties).

## How the code works

The sources are [MandatoryResourceConditionExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/condition/MandatoryResourceConditionExample.java), [PreferredResourceConditionExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/condition/PreferredResourceConditionExample.java), [BannedResourceConditionExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/condition/BannedResourceConditionExample.java) and [UnPreferredResourceConditionExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/condition/UnPreferredResourceConditionExample.java). All four call the same steps in `example()`:

```java
// The public evaluation license, or your own license if you put a license file where
// ExampleLicenseHelper looks for it.
ExampleLicenseHelper.setLicense(this);
addNodes(this);
addResources(this);
attachToObservables(this);
IOptimizationResult result = startAndPresentResult(this);
exportToKml(result);
```

Each class adds its constraints at the end of `addNodes`.

### The mandatory resource

```java
// The mandatory resource constraint is hard: Koeln1 is for John only. Koeln, at the same
// position, has no constraint.
IConstraintResource johnMandatory = new MandatoryResourceConstraint();
// addResource needs a priority above 0. The priority ranks the resources of one constraint,
// so with John alone its value does not matter.
johnMandatory.addResource("John", 10);
koeln1.addConstraint(johnMandatory);
```

### The preferred resources

```java
// A preferred resource constraint is soft: the optimizer may give the node to another
// resource if the preferred one costs too much. A mandatory resource constraint would not.
IConstraintResource jackPreferred = new PreferredResourceConstraint();
// addResource needs a priority above 0. The priority ranks the resources of one constraint,
// so with one resource its value does not matter.
jackPreferred.addResource("Jack", 10);
koeln.addConstraint(jackPreferred);

IConstraintResource johnPreferred = new PreferredResourceConstraint();
johnPreferred.addResource("John", 10);
koeln1.addConstraint(johnPreferred);
```

### The banned resources

```java
// The banned resource constraint is hard: John may not visit Koeln1. Koeln, at the same
// position, has no constraint.
IConstraintResource johnBanned = new BannedResourceConstraint();
// addResource needs a priority above 0. A banned resource is kept away whatever its
// priority.
johnBanned.addResource("John", 10);
koeln1.addConstraint(johnBanned);

// Both resources are banned from Stuttgart. The hard constraint leaves it no resource, so
// Stuttgart stays unassigned.
IConstraintResource bothBanned = new BannedResourceConstraint();
bothBanned.addResource("Jack", 10);
bothBanned.addResource("John", 10);
stuttgart.addConstraint(bothBanned);
```

### The unpreferred resources

```java
// The unpreferred resource constraint is soft: JOpt avoids John for Koeln1 where it can, but a
// visit by John is allowed at an extra cost. Koeln, at the same position, has no constraint.
IConstraintResource johnUnPreferred = new UnPreferredResourceConstraint();
// addResource needs a priority above 0. For an unpreferred resource the priority scales the
// extra cost of a visit; 1 is the lowest value.
johnUnPreferred.addResource("John", 1);
koeln1.addConstraint(johnUnPreferred);

// Both resources are unpreferred for Stuttgart. The soft constraint does not take the node out
// of the plan: one of them visits it, and the result shows the violation.
IConstraintResource bothUnPreferred = new UnPreferredResourceConstraint();
bothUnPreferred.addResource("Jack", 1);
bothUnPreferred.addResource("John", 1);
stuttgart.addConstraint(bothUnPreferred);
```

### The resources, the events and the KML file

`addResources` creates Jack and John with different maximal working times:

```java
// Jack may work 8 hours a day and John 14, so a long day such as the one with Heilbronn and
// Stuttgart fits John without overtime.
Duration maxWorkingTimeJack = Duration.ofHours(8);
Duration maxWorkingTimeJohn = Duration.ofHours(14);
```

The block is from `MandatoryResourceConditionExample`; `PreferredResourceConditionExample` names Nuernberg and Stuttgart in the comment. `BannedResourceConditionExample` and `UnPreferredResourceConditionExample` take the same limits as `MandatoryResourceConditionExample`, so that their results compare with its result.

`attachToObservables` subscribes to the progress, warning, status and error events with the recommended event API, before the run starts. `startAndPresentResult` starts the run, waits up to 5 minutes for the result, prints it and returns it. `exportToKml` writes the result to a KML file named after the class (for example `BannedResourceConditionExample.kml`) in the working directory, as in [Export a result to a KML file](../../basic/io_03/Export2KMLExample.md).

## Output and what to look for

### MandatoryResourceConditionExample

In both runs, Koeln1 was on John's route of March 6, together with Koeln and all other nodes apart from Aachen. Aachen lies at the start position of both resources. Whether Jack visits it, as his only node, or John takes it as well and Jack has no route can change between runs, at the same cost:

```text
 Cost                    : 847.033054487167
```

### PreferredResourceConditionExample

The result was the same in two test runs. Koeln is on Jack's route and Koeln1 on John's route, both on March 6, so both preferences hold (trimmed to these lines):

```text
RouteId                : 0
Resource               : Jack
0.2 Koeln / Geo  / Arrival: 06.03.2020 09:28:36 (Europe/Berlin) , Departure: 06.03.2020 09:48:36 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 27.25 [min], 35.97 [km]
RouteId                : 2
Resource               : John
2.0 Koeln1 / Geo  / Arrival: 06.03.2020 08:47:59 (Europe/Berlin) , Departure: 06.03.2020 09:07:59 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 47.99 [min], 63.34 [km]
```

Jack visits Aachen, Dueren, Koeln, Essen and Oberhausen. John visits Koeln1, Nuernberg and Stuttgart.

### BannedResourceConditionExample

The result was the same in two test runs. Koeln1 is on Jack's route of March 6, together with Aachen, Koeln, Wuppertal, Essen and Oberhausen. In `MandatoryResourceConditionExample`, Koeln1 is on John's route instead. In two test runs of a variant of this class without the constraint on Koeln1, Koeln1 was on John's route as well, so the banned resource constraint moves it to Jack.

Jack's route, trimmed to these lines:

```text
RouteId                : 0
Resource               : Jack
0.1 Koeln1 / Geo  / Arrival: 06.03.2020 09:07:59 (Europe/Berlin) , Departure: 06.03.2020 09:27:59 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 47.99 [min], 63.34 [km]
```

Stuttgart, banned for both resources, is not on any route. The result lists it as unassigned:

```text
Number of unassigned nodes: 1

Stuttgart
```

John visits only Heilbronn, and neither route shows a violation.

### UnPreferredResourceConditionExample

The result was the same in two test runs. Jack's route of March 6 is the same as in `BannedResourceConditionExample`, with Koeln1 on it. In two test runs of a variant of this class without the constraint on Koeln1, Koeln1 was on John's route, so here the unpreferred resource constraint keeps John away from Koeln1.

Stuttgart, unpreferred for both resources, is still planned: it is on John's route, with the violation and the extra cost (trimmed to these lines):

```text
RouteId                : 2
Resource               : John
2.0 Stuttgart / Geo  / Arrival: 06.03.2020 11:56:15 (Europe/Berlin) , Departure: 06.03.2020 12:16:15 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 236.25 [min], 311.85 [km][ CONSTRAINTVIOLATION/BINDING_EXCLUDING_TYPE/RESOURCE_NOT_ALLOWED/MatchingExcludingResource Value: [ "John", "Jack" ] ]
ViolationSumary: NumViolations: 1,TYV: 1
Cost TotalSum: 2689.028305914301,Cost Distance: 644.9767461920791,Cost RouteTime: 44.05155972222222,Cost UnPreferredRes: 2000.0
```

John also visits Heilbronn. With banned resources instead, in `BannedResourceConditionExample`, Stuttgart stays unassigned.

### John's long day

In `MandatoryResourceConditionExample` and `PreferredResourceConditionExample`, John's route of March 6 takes about 12 hours, so it ends hours after the end of his working hours at 17:00, and the result shows no violation. Overtime counts against the maximal working time, and the working hours are a frame for matching resources with the opening hours of the nodes, so a route can end after its working hours without a violation. John may work 14 hours; with Jack's 8 hours, a day this long would be overtime.

## Run it

Run the `main(String[] args)` method of one of the four classes from your IDE. You need a JDK in the version that the examples pom sets. You need no license file: without one, the examples use the public evaluation license (see the [license guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/license)). Each run writes its KML file into the working directory.

## Related

- [Basic elements tutorial](https://www.dna-evolutions.com/docs/getting-started/tutorials/basic-elements): the nodes and resources that these examples use.
- [Optimization properties](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties): the property reference, with `JOptWeight.PreferableResource` and `JOptWeight.UnPreferableResource`.
- [Export a result to a KML file](../../basic/io_03/Export2KMLExample.md): the KML export in general.
- [Recommended ways to run an optimization](../../basic/recommendedimplementation_02/RecommendedImplementation.md): the recommended run pattern, a synchronous run and the legacy callback methods.
- [Advanced examples](../README.md): the overview of all Advanced examples.

---

Added: 2026-02-17 (JOpt core 7.5.3-j17) | Last updated: 2026-10-20
