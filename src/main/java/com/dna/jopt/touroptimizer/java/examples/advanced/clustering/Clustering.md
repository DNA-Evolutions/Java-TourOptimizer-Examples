# Clustering construction examples

These three examples need a full license: they have more elements than the public evaluation license allows, and with that license each run ends with a license error before the construction starts. `ClusteringCityToCityExample` and `ClusteringInnerCityExample` plan with the clustering construction alone, for cities across Germany and for 1000 nodes in the west of Koeln; `ClusteringCityToCityKeepClustersForOptimizationExample` keeps the clusters of the construction as hard zones while the optimization runs. The page is for developers who want to inspect the start solution of the construction on large inputs; the [clustering construction feature guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/clustering_construction) describes the algorithm.

## What the example shows

This document explains **JOpt.TourOptimizer's clustering construction capabilities** and how to reproduce and inspect them using the provided **Advanced** Java examples.

Clustering during construction is one of the most practical "make it work in production" features: it produces a **meaningful, feasible (or near-feasible) starting solution quickly**, which significantly improves convergence and solution quality for subsequent optimization stages.

- **Construction only:** `ClusteringCityToCityExample` and `ClusteringInnerCityExample` set `JOptExitCondition.JOptGenerationCount` and `JOpt.Algorithm.PreOptimization.SA.NumIterations` to `0`. No genetic evolution and no simulated annealing follow the construction, so the routes of the result come from the construction.
- **Clusters kept as hard zones:** `ClusteringCityToCityKeepClustersForOptimizationExample` runs a short optimization after the construction and sets `JOpt.Clustering.AutoZoneCodeClusters` and `JOpt.Clustering.AutoZoneCodeClusters.isHard` to `True`. The clusters that the construction finds become zones, and as hard zones no node may leave its cluster during the optimization.
- **KML output:** each class writes its result to a KML file named after the class, for a map view of the routes.

## Scenario

The dates are sample values.

### The two city-to-city examples

`ClusteringCityToCityExample` and `ClusteringCityToCityKeepClustersForOptimizationExample` use the same data:

- **Resources:** eight `CapacityResource` objects spread across Germany: `JackKoeln`, `JackJena`, `JackHamburg`, `JackKoblenz`, `JackWuppertal`, `JackHeilbronn`, `PeterGoettingen` and `PeterDortmund`, each at the coordinates of its city (examples: Koeln, Jena, Hamburg, Koblenz, Wuppertal, Heilbronn, Goettingen, Dortmund). Each may work 12 hours, a sample value, and drive 2200 km.
- **Nodes:** 66 time-window geo nodes (`TimeWindowGeoNode` objects) representing many German cities, from Aachen to Goerlitz and from Kiel to Freiburg. Three of them, `Koeln1`, `Koeln2` and `Koeln3`, lie at the same place in Koeln. Each visit takes 10 minutes, the visit duration per node.
- **Times:** the nodes are open (node `OpeningHours`) and the resources work (resource `WorkingHours`) on May 6, 2020, from 08:00 to 22:00 (Europe/Berlin).

### The inner-city example

`ClusteringInnerCityExample` creates its elements in a loop: a large synthetic "inner-city" instance in/around Koeln, using a **Phyllotaxis** distribution (spiral sampling) to place nodes and resources.

- **Nodes and resources:** 1000 nodes, `N_0` to `N_999`, and 50 resources, `R_0` to `R_49`, sampled using `samplePhyllotaxis(...)` on two phyllotaxis patterns around a point in the west of Koeln (50.9333, 6.85), the area of the phyllotaxis figures in the feature guide; those figures show a setup of their own, with 40 resources, while this class has 50 resources and chooses `SmartConstructionAlgo`. The resources also lie around the same center (multi-depot style).
- **Pattern:** element i lies at the distance spacing x sqrt(i) from that point and at i times the golden angle. The spacing is 0.002 degrees for the nodes and 0.01 degrees for the resources, so the outermost node lies about 0.063 degrees from that point and the outermost resource 0.07 degrees: the resources spread over the area of the nodes.
- **Times:** the nodes are open (node `OpeningHours`) and the resources work (resource `WorkingHours`) on May 6, 2023, from 08:00 to 22:00 (Europe/Berlin). Each visit takes 5 minutes, the visit duration per node; each resource may work 12 hours, a sample value, and drive 2200 km.
- **Two days:** with the constant `ADD_EVEN_ODD_SPLITTING` set to `true`, the nodes and resources with an even index move to May 7, and the plan splits into two days: the opening hours are optionally split by even/odd node id into different days (multi-dimensional clustering behavior), and the working hours are produced via `createWorkingHours(dayOfMay(ii))`, which supports the even/odd day split. The committed value is `false`.

## How the code works

The sources are [ClusteringCityToCityExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/clustering/ClusteringCityToCityExample.java), [ClusteringCityToCityKeepClustersForOptimizationExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/clustering/ClusteringCityToCityKeepClustersForOptimizationExample.java) and [ClusteringInnerCityExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/clustering/ClusteringInnerCityExample.java). In all three, `example()` runs the same steps:

```java
// Needs your own full license file where ExampleLicenseHelper looks for it: the public
// evaluation license does not allow this many elements.
ExampleLicenseHelper.setLicense(this);
setConstructionAlgorithm(this);
addProperties(this);
addNodes(this);
addResources(this);
attachToObservables(this);
IOptimizationResult result = startAndPresentResult(this);
exportToKml(result);
```

`ExampleLicenseHelper.setLicense` sets the license from your license file if it finds one, and the public evaluation license otherwise. `setConstructionAlgorithm` chooses the construction algorithm (see "The construction algorithm" below). `attachToObservables` and `startAndPresentResult` work as in the [First optimization example](../../basic/firstoptimization_01/FirstOptimizationExample.md); `startAndPresentResult` also returns the result for the KML export.

### The construction algorithm

All three classes choose `SmartConstructionAlgo` as the construction algorithm, in `setConstructionAlgorithm`:

```java
// SmartConstructionAlgo builds the start solution by clustering. Without a list of heuristic
// algorithms (Optional.empty()), the scheme runs its own; the properties set their run values.
ConstructionOptimizationAlgorithmConfig construction =
    ConstructionOptimizationAlgorithmConfig.builder()
        .algorithm(ConstructionOptimizationAlgorithm.SMART_CONSTR_ALGO)
        .build();
opti.setOptimizationScheme(
    new DefaultOptimizationScheme(opti, Optional.of(construction), Optional.empty()));
```

### Construction only

To highlight clustering construction quality, the example disables the later optimization stages, and `ClusteringCityToCityExample` does so just like the inner-city example: both set the same two properties, zero generations and zero SA iterations. This means the output is pure construction (no SA, no GA), so it demonstrates what the construction stage can do on its own, at "country scale" for the city-to-city data. The block shows the two properties of `ClusteringCityToCityExample` and `ClusteringInnerCityExample`:

```java
// Zero generations and zero iterations: no genetic evolution and no simulated annealing after
// the construction.
props.setProperty("JOptExitCondition.JOptGenerationCount", "0");
props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumIterations", "0");
```

### Keeping the clusters

Unlike the pure-construction examples, `ClusteringCityToCityKeepClustersForOptimizationExample` enables optimization: this one runs a short optimization and turns the clusters into hard zones:

```java
// Fewer generations and iterations than the defaults keep the run short.
props.setProperty("JOptExitCondition.JOptGenerationCount", "1000");
props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumIterations", "100000");
// Turns the clusters that the construction finds into zones.
props.setProperty("JOpt.Clustering.AutoZoneCodeClusters", "True");
// Hard zones: no node may leave the cluster of the construction during the optimization.
props.setProperty("JOpt.Clustering.AutoZoneCodeClusters.isHard", "True");
```

Then it adds two key clustering properties, the last two lines of the block:

- `JOpt.Clustering.AutoZoneCodeClusters = True`, meaning: create zone codes from the clusters found by construction (automatic zoning).
- `JOpt.Clustering.AutoZoneCodeClusters.isHard = True`, meaning: these clusters become **hard constrained**. No node is allowed to leave its initial cluster during optimization.

With `JOpt.Clustering.AutoZoneCodeClusters.isHard` set to `False`, the zones are soft constrained. The [property reference](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties) lists the defaults of the four properties, and the [ZoneCodes feature guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/zonecodes) explains zones.

### The phyllotaxis pattern

`ClusteringInnerCityExample` places its nodes and resources with `samplePhyllotaxis`:

```java
for (int i = 0; i < n; i++) {
  double radius = spacing * Math.sqrt(i);
  double angle = i * goldenAngle;
  positions.add(
      Position.of(
          origin.latitude() + radius * Math.sin(angle),
          origin.longitude() + radius * Math.cos(angle)));
}
```

The spacing is in degrees on both axes. A degree of longitude is shorter than a degree of latitude at Koeln, so on a map the pattern is narrower from west to east than from north to south.

### The KML export

The example exports a KML file named after the class: `exportToKml` writes the result to the working directory, to `ClusteringCityToCityExample.kml`, `ClusteringCityToCityKeepClustersForOptimizationExample.kml` or `ClusteringInnerCityExample.kml`. The block shows the file of `ClusteringCityToCityExample`:

```java
String kmlFile = "ClusteringCityToCityExample.kml";

EntityKMLExporter kmlExporter = new EntityKMLExporter();
kmlExporter.setTitle("ClusteringCityToCityExample");

// export flushes the stream but does not close it; the try block closes it.
try (OutputStream out = new FileOutputStream(kmlFile)) {
  if (!kmlExporter.export(result, out)) {
    System.out.println("KML export failed: " + kmlFile);
  }
}
```

Each run writes the same file, so keep a copy of it before you generate another instance (other numbers of resources/nodes or other spacing parameters) and compare construction behavior.

## Output and what to look for

### Without a full license

With the public evaluation license, each of the three classes ended with exit code 1 after a few seconds in two test runs, with the same messages both times.

- **Element count:** the last status event before the error starts with `StatusEvent(STATUS_INFO, Execeed number of elemens. Allowed vs. actual:` and continues with two numbers: the number of elements that the license allows, and the number of elements of the example, 74 for the two city-to-city examples and 1050 for `ClusteringInnerCityExample`.
- **License error:** JOpt then logs the license error with this exception, and the example prints it once more in the error event `UNCAUGHT_EXCEPTION_IN_OPTIMIZATION`:

  ```text
  com.dna.jopt.framework.exception.caught.InvalidLicenceException: The provided license data is not valid or no license is available. Please provide a valid license.
  ```

- **End of the run:** the wait in `startAndPresentResult` ends with this exception, and `main` ends with it:

  ```text
  Exception in thread "main" java.lang.InterruptedException
  ```

  The license error above it is the cause. The run prints no result and writes no KML file.

The clustering construction feature guide shows figures of construction results for phyllotaxis data of its own, with 40 resources, and for city-to-city data across Germany.

### With a full license

With your own full license file, each class prints the result and writes its KML file. Open the KML file in Google Earth or another KML viewer to see the routes on a map. For the effect of the hard zones, compare the KML file of `ClusteringCityToCityKeepClustersForOptimizationExample` with the one of `ClusteringCityToCityExample`, which has no optimization after the construction.

## Why clustering during construction exists

JOpt uses a **multi-algorithm solving strategy**: multiple algorithms are executed in succession, where the output of one stage is used as input to the next.

A key observation is:

- The quality of a solver's final result strongly depends on the quality of the **starting ("working") solution**.
- A deterministic, domain-specific construction stage can produce a better starting point than random initialization.
- With the same compute budget, a better starting point typically yields a **better final solution**.

Therefore, JOpt runs a deterministic construction algorithm first; the three examples set it explicitly to `SmartConstructionAlgo`. In many cases, you can already use this constructed solution directly, for example to show an approximate plan to end users while deeper optimization runs in the background.

## What "clustering during construction" means in routing terms

In tour optimization, "clustering" is not only a geographic concept. A "good cluster" must typically be consistent with:

- **distance / travel-time proximity**,
- **WorkingHours** of resources,
- **OpeningHours** of nodes (time windows),
- **constraints** that exclude specific resources from specific nodes (skills, types, mandatory resource); preferred resources and relationships steer by cost and do not exclude (see [Mandatory, preferred, banned and unpreferred resources for a node](../condition/BindingExcludingResourceCondition.md)).

A construction algorithm that only clusters by distance (classic K-Means usage) can fail badly when time windows and working hours dominate feasibility.

JOpt's clustering construction is explicitly designed to be **multi-dimensional**.

## The construction algorithm (as described in the documentation)

The documentation describes the **HybridMultiDimensionalClusteringAlgorithm**, a clustering construction algorithm; without a choice of construction algorithm, JOpt runs `SmartConstructionAlgo`. The examples of this page choose `SmartConstructionAlgo` as the construction algorithm (`ConstructionOptimizationAlgorithm.SMART_CONSTR_ALGO`), which also builds clusters. The two test runs of each class with the public evaluation license show it in a status event before the license error: `StatusEvent(OPTIMIZATION_ALGO_INITIALIZED, SmartConstructionAlgo)`. The algorithm of the documentation runs when you choose it as the construction algorithm instead (`ConstructionOptimizationAlgorithm.CLUSTERED_SEQUENTIAL_SPACE_SAVINGS_ALGO`).

### Requirements the algorithm is designed to meet
The algorithm is designed to be:

1. **Fast**  
   Runtime on seconds to low-minute scale, even for very large problem sets (> 1000 elements).

2. **Robust**  
   A meaningful result should be produced no matter how the input data looks.

3. **Multi-dimensional**  
   Uses more than distance; it includes time windows, working hours, and resource eligibility constraints when forming clusters.

4. **Dynamic**  
   The number of clusters cannot be known a priori; it is derived from the problem instance (resources, nodes, constraints).

### Summarized working principle (high level)
The documentation summarizes the working principle in four phases:

1. Collect resources and nodes and estimate "free path lengths" to derive cluster scale and potential cluster counts.
2. Let resources collect nodes in parallel and compete for nodes acquired by multiple resource clusters.
3. Re-adjust clusters using a parallel, savings-like algorithm to build an initial solution.
4. Apply "disturbance-theory" strategies to place elements that could not be positioned earlier.

Operationally, this means the construction stage is not "one clustering pass"; it is a multi-stage attempt to produce a route plan that is immediately useful.

## How to interpret a "good construction result"

Even when you run *only* construction (no optimization iterations), a good result typically exhibits:

- low route crossings,
- geographically coherent routes,
- roughly balanced workload across resources (when such balance is feasible),
- consistent handling of time windows (e.g., odd/even day splitting),
- reasonable "ownership" of node groups by resources without pathological oscillation.

The examples below are designed to make those outcomes visible via KML export.

## The Advanced clustering examples (what each one is proving)

### 1) `ClusteringInnerCityExample`: Inner-city multi-depot clustering using a Phyllotaxis pattern

A visually inspectable cluster/route structure where distance dominates, but time windows can be used as a second dimension.
How the input is generated, how it runs and how you inspect the result: see "The inner-city example" under "Scenario", and "How the code works" above.

### 2) `ClusteringCityToCityExample`: Germany-wide "effective single depot" behavior (city-to-city)

**What it showcases**
- A country-scale setup (Germany) where:
  - resources are positioned far apart,
  - nodes represent many cities across the country.
- The construction algorithm must assign geographically and temporally coherent "city clusters" to appropriate resources.

**KML output**
The example writes:
- `ClusteringCityToCityExample.kml`

This is intended for visual inspection of cluster ownership, route coherence, and "outlier handling" (in the feature guide's city-to-city figure, Eberswalde is not on the route it would usually be assigned to, which would cause overtime).

### 3) `ClusteringCityToCityKeepClustersForOptimizationExample`: Keep construction clusters as hard constraints ("districting")

**What it showcases**
This example is the bridge between:
- construction clustering (creating good initial clusters), and
- optimization (improving the solution), **without destroying cluster ownership**.

This pattern is highly relevant in production when:
- you want stable service territories,
- you want predictable resource ownership of customer groups,
- you want to optimize *within* a cluster but not "move nodes across districts".

**Why this matters**
This is not a cost "preference" strategy. With `isHard = True`, the solver is architecturally restricted:
- optimization may improve routes, order, timing, and distance **within the cluster**,
- but it may not break the cluster allocation.

This enables robust "cluster-first, optimize-second" workflows for territory-driven operations.

**KML output**
The example writes:
- `ClusteringCityToCityKeepClustersForOptimizationExample.kml`

It holds the optimized solution only. Together with `ClusteringCityToCityExample.kml`, the constructed solution of the same data, it allows you to compare:
- the constructed solution,
- vs. the optimized solution that respects hard cluster boundaries.

## Recommended way to demonstrate clustering capability (for customers, demos, and benchmarks)

### A. Construction-only demonstration (fast and persuasive)
Use:
- `ClusteringInnerCityExample` and `ClusteringCityToCityExample`

Why:
- they show immediate route structure without the effect of later optimization stages,
- results are easy to explain ("this is what the constructor can do before optimization").

### B. Construction + optimization with stable ownership (production narrative)
Use:
- `ClusteringCityToCityKeepClustersForOptimizationExample`

Why:
- it demonstrates that JOpt can deliver both:
  - strong initial territories, and
  - improved route quality,
  while keeping ownership stable via hard constraints.

## Practical notes for running the examples

### License requirement
These examples contain more nodes and more resources than the public evaluation license in `ExampleLicenseHelper` and the extended free license allow (see the [license guide: element limits](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/license#free-license)). Therefore a **full license is required** (the source files explicitly warn about this).

### Output artifacts
All examples export KML files to the working directory.  
Use Google Earth (or a KML-capable GIS viewer) to inspect:

- cluster coherence,
- route crossing behavior,
- territory separation (especially in the "keep clusters hard" example).

### Keep the examples "construction-only" when you want to explain clustering
If your goal is to showcase clustering construction itself, do not add later optimization stages.  
The examples show you the canonical way to do this:

- set generation count and SA iterations to zero.

## Common questions and the correct answer

### "Is clustering just a post-processing visualization step?"
No. In JOpt, clustering during construction is a solver stage that produces the initial working solution. It directly influences:
- feasibility,
- runtime-to-quality,
- final plan quality.

### "Is keeping clusters for optimization done by cost weights?"
Not when `isHard = True`. In that mode, cluster boundaries are enforced as a **hard constraint**.  
Costs and weights are used for soft trade-offs, but hard cluster boundaries are a structural restriction.

### "Why does JOpt care about OpeningHours/WorkingHours in clustering?"
Because in real routing problems, feasibility and quality are dominated by time.  
Distance-only clustering can produce routes that look compact but are impossible to execute within time windows.

## Summary

- Clustering during construction creates high-quality starting solutions quickly and robustly.
- The construction algorithm is designed to be fast, robust, multi-dimensional, and dynamic.
- The Advanced examples show three distinct narratives:
  1. **Inner-city phyllotaxis** (distance-driven, scalable, visually convincing),
  2. **Germany city-to-city** (macro clustering and outlier handling),
  3. **Keep clusters for optimization** (territory stability enforced as hard constraints).

If you want a single clear demonstration for customers, start with the KML output of the construction-only examples, then show how hard cluster preservation keeps territories stable while optimization improves route quality.

## Run it

1. Put your own full license file where `ExampleLicenseHelper` looks for it, `src/main/resources/license/YOUR_LICENSE.jli`. [Set a license from a file](../../basic/setlicense_08/SetLicense.md) shows how the examples read it, and the [license guide: element limits](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/license#free-license) lists what each license allows.
2. Run the `main(String[] args)` method of one of the three classes from your IDE, with the repository imported as a Maven project. You need a JDK in the version that the examples pom sets.
3. Open the KML file in the working directory.

## Related

- [Clustering construction feature guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/clustering_construction): the construction algorithm, figures of construction results and the clustering properties.
- [ZoneCodes feature guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/zonecodes): zones, which `ClusteringCityToCityKeepClustersForOptimizationExample` creates from the clusters.
- [Set a license from a file](../../basic/setlicense_08/SetLicense.md): how the examples read your own license file.
- [Optimization properties](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties): the property reference, with the defaults.
- [Advanced examples](../README.md): the overview of all Advanced examples.
- [Recommended ways to run an optimization](../../basic/recommendedimplementation_02/RecommendedImplementation.md): the recommended run pattern, a synchronous run and the legacy callback methods.

---

Added: 2026-02-17 (JOpt core 7.5.3-j17) | Last updated: 2026-10-23
