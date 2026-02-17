# Clustering During Construction — High-Quality Starting Solutions at Scale

This document explains **JOpt TourOptimizer’s clustering construction capabilities** and how to reproduce and inspect them using the provided **Advanced** Java examples.

Clustering during construction is one of the most practical “make it work in production” features: it produces a **meaningful, feasible (or near-feasible) starting solution quickly**, which significantly improves convergence and solution quality for subsequent optimization stages.

---

## References

- Clustering during construction (overview and theory):  
  https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/clustering_construction

- Examples (GitHub):
  - https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/clustering/ClusteringInnerCityExample.java  
  - https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/clustering/ClusteringCityToCityExample.java  
  - https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/clustering/ClusteringCityToCityKeepClustersForOptimizationExample.java

---

## Why clustering during construction exists

JOpt uses a **multi-algorithm solving strategy**: multiple algorithms are executed in succession, where the output of one stage is used as input to the next.

A key observation from production use is:

- The quality of a solver’s final result strongly depends on the quality of the **starting (“working”) solution**.
- A deterministic, domain-specific construction stage can produce a better starting point than random initialization.
- With the same compute budget, a better starting point typically yields a **better final solution**.

Therefore, JOpt runs a deterministic construction algorithm first (by default). In many cases, you can already use this constructed solution directly—for example to show an approximate plan to end users while deeper optimization runs in the background.

---

## What “clustering during construction” means in routing terms

In tour optimization, “clustering” is not only a geographic concept. A “good cluster” must typically be consistent with:

- **distance / travel-time proximity**,
- **WorkingHours** of resources,
- **OpeningHours** of nodes (time windows),
- **constraints** that exclude specific resources from specific nodes (skills, types, mandatory/preferred resource, relationships).

A construction algorithm that only clusters by distance (classic K-Means usage) can fail badly when time windows and working hours dominate feasibility.

JOpt’s clustering construction is explicitly designed to be **multi-dimensional**.

---

## The construction algorithm (as described in the documentation)

The documentation describes the **HybridMultiDimensionalClusteringAlgorithm** (internal name: `MultiAttemptParallelMultiDimensionalHybridSavingsAlgo`) as the construction algorithm used to create the starting solution.

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

1. Collect resources and nodes and estimate “free path lengths” to derive cluster scale and potential cluster counts.
2. Let resources collect nodes in parallel and compete for nodes acquired by multiple resource clusters.
3. Re-adjust clusters using a parallel, savings-like algorithm to build an initial solution.
4. Apply “disturbance-theory” strategies to place elements that could not be positioned earlier.

Operationally, this means the construction stage is not “one clustering pass”; it is a multi-stage attempt to produce a route plan that is immediately useful.

---

## How to interpret a “good construction result”

Even when you run *only* construction (no optimization iterations), a good result typically exhibits:

- low route crossings,
- geographically coherent routes,
- roughly balanced workload across resources (when such balance is feasible),
- consistent handling of time windows (e.g., odd/even day splitting),
- reasonable “ownership” of node groups by resources without pathological oscillation.

The examples below are designed to make those outcomes visible via KML export.

---

## The Advanced clustering examples (what each one is proving)

### 1) `ClusteringInnerCityExample` — Inner-city multi-depot clustering using a Phyllotaxis pattern

**What it showcases**
- A large synthetic “inner-city” instance in/around Cologne using a **Phyllotaxis** distribution (spiral sampling) to place nodes and resources.
- A visually inspectable cluster/route structure where distance dominates, but time windows can be used as a second dimension.

**How the input is generated**
- Nodes: sampled around a Cologne center position using `samplePhyllotaxis(...)`.
- Resources: also sampled via `samplePhyllotaxis(...)` around the same center (multi-depot style).
- Node OpeningHours:
  - 08:00–22:00 (Europe/Berlin)
  - optionally split by even/odd node id into different days (multi-dimensional clustering behavior).
- Visit duration:
  - 5 minutes per node.
- Resource WorkingHours:
  - produced via `getWorkingHours(addDay)` (supports the even/odd day split).

**How it runs**
To highlight clustering construction quality, the example disables later optimization stages:

- `JOptExitCondition.JOptGenerationCount = 0`
- `JOpt.Algorithm.PreOptimization.SA.NumIterations = 0`

This means:
- The output is **pure construction** (no SA, no GA).

**How you inspect the result**
The example exports a KML file with a parameterized name including:
- number of resources/nodes,
- spacing parameters.

That makes it convenient to generate multiple instances and compare construction behavior.

---

### 2) `ClusteringCityToCityExample` — Germany-wide “effective single depot” behavior (city-to-city)

**What it showcases**
- A country-scale setup (Germany) where:
  - resources are positioned far apart,
  - nodes represent many cities across the country.
- The construction algorithm must assign geographically and temporally coherent “city clusters” to appropriate resources.

**Model highlights**
- Resources: 8 resources spread across Germany (examples: Cologne, Jena, Hamburg, Koblenz, Wuppertal, Heilbronn, Goettingen, Dortmund).
- Nodes: 66 time-window geo nodes representing many cities.
- Node OpeningHours:
  - May 6, 2020, 08:00–22:00 (Europe/Berlin).
- Resource WorkingHours:
  - May 6, 2020, 08:00–22:00 (Europe/Berlin).
- Visit duration:
  - 10 minutes per node.

**Pure construction execution**
Just like the inner-city example, it disables the optimization iterations:

- `JOptExitCondition.JOptGenerationCount = 0`
- `JOpt.Algorithm.PreOptimization.SA.NumIterations = 0`

So the output demonstrates what the construction stage can do on its own at “country scale”.

**KML output**
The example writes:
- `ClusteringCityToCityExample.kml`

This is intended for visual inspection of cluster ownership, route coherence, and “outlier handling” (where the algorithm deliberately assigns borderline cities to avoid overtime).

---

### 3) `ClusteringCityToCityKeepClustersForOptimizationExample` — Keep construction clusters as hard constraints (“districting”)

**What it showcases**
This example is the bridge between:
- construction clustering (creating good initial clusters), and
- optimization (improving the solution), **without destroying cluster ownership**.

This pattern is highly relevant in production when:
- you want stable service territories,
- you want predictable resource ownership of customer groups,
- you want to optimize *within* a cluster but not “move nodes across districts”.

**How it runs**
Unlike the pure-construction examples, this one enables optimization:

- `JOptExitCondition.JOptGenerationCount = 5000`
- `JOpt.Algorithm.PreOptimization.SA.NumIterations = 500000`

Then it adds two key clustering properties:

- `JOpt.Clustering.AutoZoneCodeClusters = True`  
  Meaning: create zone codes from the clusters found by construction (automatic zoning).

- `JOpt.Clustering.AutoZoneCodeClusters.isHard = True`  
  Meaning: these clusters become **hard constrained**. No node is allowed to leave its initial cluster during optimization.

**Why this matters**
This is not a cost “preference” strategy. With `isHard = True`, the solver is architecturally restricted:
- optimization may improve routes, order, timing, and distance **within the cluster**,
- but it may not break the cluster allocation.

This enables robust “cluster-first, optimize-second” workflows for territory-driven operations.

**KML output**
The example writes:
- `ClusteringCityToCityKeepClustersForOptimizationExample.kml`

This allows you to compare:
- the constructed solution,
- vs. the optimized solution that respects hard cluster boundaries.

---

## Recommended way to demonstrate clustering capability (for customers, demos, and benchmarks)

### A. Construction-only demonstration (fast and persuasive)
Use:
- `ClusteringInnerCityExample` and `ClusteringCityToCityExample`

Why:
- they show immediate route structure without “solver magic” from later stages,
- results are easy to explain (“this is what the constructor can do before optimization”).

### B. Construction + optimization with stable ownership (production narrative)
Use:
- `ClusteringCityToCityKeepClustersForOptimizationExample`

Why:
- it demonstrates that JOpt can deliver both:
  - strong initial territories, and
  - improved route quality,
  while keeping ownership stable via hard constraints.

---

## Practical notes for running the examples

### License requirement
These examples contain more than 10 elements (nodes/resources), therefore a **valid license is required** (the source files explicitly warn about this).

### Output artifacts
All examples export KML files to the working directory.  
Use Google Earth (or a KML-capable GIS viewer) to inspect:

- cluster coherence,
- route crossing behavior,
- territory separation (especially in the “keep clusters hard” example).

### Keep the examples “construction-only” when you want to explain clustering
If your goal is to showcase clustering construction itself, do not add later optimization stages.  
The examples show you the canonical way to do this:

- set generation count and SA iterations to zero.

---

## Common questions and the correct answer

### “Is clustering just a post-processing visualization step?”
No. In JOpt, clustering during construction is a solver stage that produces the initial working solution. It directly influences:
- feasibility,
- runtime-to-quality,
- final plan quality.

### “Is keeping clusters for optimization done by cost weights?”
Not when `isHard = True`. In that mode, cluster boundaries are enforced as a **hard constraint**.  
Costs and weights are used for soft trade-offs, but hard cluster boundaries are a structural restriction.

### “Why does JOpt care about OpeningHours/WorkingHours in clustering?”
Because in real routing problems, feasibility and quality are dominated by time.  
Distance-only clustering can produce routes that look compact but are impossible to execute within time windows.

---

## Summary

- Clustering during construction creates high-quality starting solutions quickly and robustly.
- The construction algorithm is designed to be fast, robust, multi-dimensional, and dynamic.
- The Advanced examples show three distinct narratives:
  1. **Inner-city phyllotaxis** (distance-driven, scalable, visually convincing),
  2. **Germany city-to-city** (macro clustering and outlier handling),
  3. **Keep clusters for optimization** (territory stability enforced as hard constraints).

If you want a single “wow moment” for customers, start with the KML output of the construction-only examples, then show how hard cluster preservation keeps territories stable while optimization improves route quality.
