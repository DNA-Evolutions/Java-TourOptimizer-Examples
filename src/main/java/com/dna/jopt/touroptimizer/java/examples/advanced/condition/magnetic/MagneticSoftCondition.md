# Magnetic soft condition between nodes

`MagneticSoftConditionExample` attaches two `MagnetoNodeConstraint` objects to nodes: Stuttgart attracts Aachen onto its route, with Aachen after Stuttgart, and Koeln repels Wuppertal and Essen from its route. A magnet is always soft: a route that breaks it costs more, and the optimizer weighs that cost against the others. The page is for developers who want nodes kept together or apart without a hard rule. The [MagnetoCondition feature guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/magnetocondition) describes the condition in general.

## What the example shows

- **Attraction:** keep selected target nodes on the **same route** as the constrained node, the magnet node. `setIsAttractingMagnet(true)` with `addNodeMagnetId("Aachen")` on Stuttgart: Aachen should be on the route that holds Stuttgart.
- **Order:** `setOrder(NodeOrder.FRONT)` asks for the attracted nodes after the magnet node on the route, so Stuttgart comes first. `NodeOrder.BACK` asks for them before the magnet node, and `NodeOrder.NO_ORDER` asks only for the same route.
- **Repulsion:** keep selected target nodes on a **different route** than the constrained node. `setIsAttractingMagnet(false)` with the targets Wuppertal and Essen on Koeln: they should be on other routes than Koeln.
- **Soft only:** this example demonstrates `MagnetoNodeConstraint` ("magnetic" node constraint), which works in **soft** mode only. A magnetic constraint attaches to a single node and influences the optimizer via **preferences**. Important: a `MagnetoNodeConstraint` is not a hard constraint. `setIsHard(true)` does not make it hard; it only logs a warning. The example therefore does not call `setIsHard`.
- **Targets by id:** the targets are node ids, here the city names.

## Scenario

The dates, times and coordinates are sample values.

- **Resources:** `Jack` and `John` both start at 50.775346, 6.083887 (Aachen). Each works on May 6, 2020, from 08:00 to 17:00 and on May 7, 2020, from 08:00 to 20:00 (Europe/Berlin), with a maximal working time of 10 hours and a maximal distance of 1200 km per day.
- **Nodes:** eight `TimeWindowGeoNode` objects: Koeln, Oberhausen, Essen, Dueren, Heilbronn, Stuttgart, Wuppertal and Aachen. They are open on May 6 and 7, 2020, from 08:00 to 17:00. Each visit takes 60 minutes, and all nodes have the importance 1. The Aachen node lies at the start position of both resources.
- **Magnets:** an attracting magnet on Stuttgart with the target Aachen and the order `FRONT`; a repelling magnet on Koeln with the targets Wuppertal and Essen.

`addProperties` sets two properties: `JOptExitCondition.JOptGenerationCount` to 1000 and `JOpt.Algorithm.PreOptimization.SA.NumIterations` to 100000. Both are below their defaults, which keeps the run short; the defaults are in the [property reference](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties).

## How the code works

The source is [MagneticSoftConditionExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/condition/magnetic/MagneticSoftConditionExample.java). `example()` runs the steps in order:

```java
// The public evaluation license, or your own license if you put a license file where
// ExampleLicenseHelper looks for it.
ExampleLicenseHelper.setLicense(this);
addProperties(this);
addNodes(this);
addResources(this);
attachToObservables(this);
IOptimizationResult result = startAndPresentResult(this);
exportToKml(result);
```

- `ExampleLicenseHelper.setLicense(this)` uses your license file if you put one where the helper looks for it, and the public evaluation license otherwise.
- `addProperties` sets the two properties.
- `addNodes` adds the eight nodes and attaches the two magnets.
- `addResources` adds Jack and John; `createWorkingHours` builds the working hours of each.
- `attachToObservables` subscribes to the progress, warning, status and error events with the event API, before the run starts.
- `startAndPresentResult` starts the run, waits up to 5 minutes for the result, prints it and returns it.
- `exportToKml` writes the result to `MagneticSoftConditionExample.kml` in the working directory and prints `KML export failed: MagneticSoftConditionExample.kml` if the export returns `false`.

### The two magnets

A magnet is a constraint on one node, the magnet node, with the ids of its target nodes:

```java
// Stuttgart attracts Aachen: Aachen should be on Stuttgart's route. FRONT asks for the
// attracted node after the magnet node, so Stuttgart comes first.
MagnetoNodeConstraint attractingMagnet = new MagnetoNodeConstraint();
attractingMagnet.setIsAttractingMagnet(true);
attractingMagnet.addNodeMagnetId("Aachen");
attractingMagnet.setOrder(NodeOrder.FRONT);
stuttgart.addConstraint(attractingMagnet);

// Koeln repels Wuppertal and Essen: they should be on other routes than Koeln.
MagnetoNodeConstraint repellingMagnet = new MagnetoNodeConstraint();
repellingMagnet.setIsAttractingMagnet(false);
repellingMagnet.addNodeMagnetId("Wuppertal");
repellingMagnet.addNodeMagnetId("Essen");
koeln.addConstraint(repellingMagnet);
```

## Output and what to look for

The run prints the status and progress lines, then the result. In two test runs the routes held the same nodes in the same order, with the same times and distances:

- **Attraction:** Stuttgart and Aachen share the route of May 7, Stuttgart first.
- **Repulsion:** Koeln is on a route of May 6 with Heilbronn. Wuppertal and Essen are on the other route of May 6, with Dueren and Oberhausen.

Which resource drives which route, and the route numbers, can change between runs: the route with Stuttgart and Aachen was route 3 of John in one run and route 1 of Jack in the other. Its two stops, without the route and stop numbers at the start of each line:

```text
Stuttgart / Geo  / Arrival: 07.05.2020 11:56:15 (Europe/Berlin) , Departure: 07.05.2020 12:56:15 (Europe/Berlin) / Duration: 60.0 [min] / Driving: 236.25 [min], 311.85 [km]
Aachen / Geo  / Arrival: 07.05.2020 16:57:25 (Europe/Berlin) , Departure: 07.05.2020 17:57:25 (Europe/Berlin) / Duration: 60.0 [min] / Driving: 241.18 [min], 318.36 [km][ CONSTRAINTVIOLATION/TIMECONSTRAINT/LATE/ Late time [min]: 57.43245 Value: 57.43245 ]
```

Aachen comes after the long drive back from Stuttgart, and its visit ends 57 minutes after the end of its opening hours. The result keeps Aachen on the route of Stuttgart and shows this LATE violation.

The route with Heilbronn and Koeln ends at 17:40:11, after the end of its working hours at 17:00, and its violation summary is empty. Time after the end of the working hours counts against the maximal working time, and the working hours are a frame for matching resources with the opening hours of the nodes, so a route can end after its working hours without a violation.

To see what the magnets change, compare a run without them. In a test run with the two `addConstraint` calls removed, one route visited Aachen, Dueren, Koeln, Wuppertal, Essen and Oberhausen, starting at Aachen, and another route visited Stuttgart and Heilbronn.

## Typical business scenarios

Magnetic constraints are useful when you want *coordination preferences* without making the plan infeasible, for example:

- "These visits should be handled by the **same person/vehicle** if possible."
- "These visits should be **separated** if possible (load risk, customer preference, operational separation)."
- "If we keep them together, this one should be **early** in the route" (ordering preference).

Because it is a *soft* constraint, the optimizer can still find a schedule when reality conflicts with the preference.

## Key configuration concepts

### Attraction vs. repulsion
- `setIsAttractingMagnet(true)` -> attraction (same-route preference)
- `setIsAttractingMagnet(false)` -> repulsion (different-route preference)

### Magnet targets by ID
Targets are added via:
- `addNodeMagnetId("...")`

In this example, the IDs match the node identifiers (city names). In production systems, you typically use stable business IDs.

### Optional ordering preference
For attraction magnets you can optionally guide where the constrained node should appear in the route relative to its attracted nodes:
- `NodeOrder.FRONT`
- `NodeOrder.BACK`
- `NodeOrder.NO_ORDER`

## Notes for production use

- Magnetic constraints are great for expressing "*prefer together* / *prefer apart*" without making plans brittle.
- Use stable IDs for magnets (e.g., order IDs, customer IDs, task IDs), not human-readable labels.
- Combine magnets with other constraints (skills, territories, time windows) to capture real operational policies.

## Soft, hard and pseudo constraints (related topics)

- Soft vs. hard constraints: hard constraints are fulfilled by architecture; soft constraints are optimized via cost trade-offs.
- Relations (same route / different route) can model linkage between two nodes; they are pseudo constraints too: a broken relation adds a cost weighted by `JOptWeight.Relationships` (see [Relations between nodes](../../relationship/Relations.md)). Magnets are the preference-oriented counterpart.

## Run it

Run the `main(String[] args)` method of `MagneticSoftConditionExample` from your IDE, with the repository imported as a Maven project. You need a JDK in the version that the examples pom sets. You need no license file: without one, the example uses the public evaluation license (see the [license guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/license)). The run writes `MagneticSoftConditionExample.kml` into the working directory.

## Related

- [MagneticSoftConditionExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/condition/magnetic/MagneticSoftConditionExample.java): the source of this example.
- [MagnetoCondition feature guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/magnetocondition): what the condition does, when to use it and how to set it up.
- [Recommended ways to run an optimization](../../../basic/recommendedimplementation_02/RecommendedImplementation.md): the recommended run pattern, a synchronous run and the legacy callback methods.
- [Advanced examples](../../README.md): the overview of all Advanced examples.

---

Added: 2026-02-17 (JOpt core 7.5.3-j17) | Last updated: 2026-10-23
