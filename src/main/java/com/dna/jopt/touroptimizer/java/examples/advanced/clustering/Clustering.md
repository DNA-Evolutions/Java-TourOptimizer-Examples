# Clustering construction examples

These three examples need a full license: they have more elements than the public evaluation license allows, and with that license each run ends with a license error before the construction starts. `ClusteringCityToCityExample` and `ClusteringInnerCityExample` plan with the clustering construction alone, for cities across Germany and for 1000 nodes in the west of Koeln; `ClusteringCityToCityKeepClustersForOptimizationExample` keeps the clusters of the construction as hard zones while the optimization runs. The page is for developers who want to inspect the start solution of the construction on large inputs; the [clustering construction feature guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/clustering_construction) describes the algorithm.

## What the example shows

- **Construction only:** `ClusteringCityToCityExample` and `ClusteringInnerCityExample` set `JOptExitCondition.JOptGenerationCount` and `JOpt.Algorithm.PreOptimization.SA.NumIterations` to `0`. No genetic evolution and no simulated annealing follow the construction, so the routes of the result come from the construction.
- **Clusters kept as hard zones:** `ClusteringCityToCityKeepClustersForOptimizationExample` runs a short optimization after the construction and sets `JOpt.Clustering.AutoZoneCodeClusters` and `JOpt.Clustering.AutoZoneCodeClusters.isHard` to `True`. The clusters that the construction finds become zones, and as hard zones no node may leave its cluster during the optimization.
- **KML output:** each class writes its result to a KML file named after the class, for a map view of the routes.

## Scenario

The dates are sample values.

### The two city-to-city examples

`ClusteringCityToCityExample` and `ClusteringCityToCityKeepClustersForOptimizationExample` use the same data:

- **Resources:** eight `CapacityResource` objects, `JackKoeln`, `JackJena`, `JackHamburg`, `JackKoblenz`, `JackWuppertal`, `JackHeilbronn`, `PeterGoettingen` and `PeterDortmund`, each at the coordinates of its city. Each may work 12 hours, a sample value, and drive 2200 km.
- **Nodes:** 66 `TimeWindowGeoNode` objects in German cities, from Aachen to Goerlitz and from Kiel to Freiburg. Three of them, `Koeln1`, `Koeln2` and `Koeln3`, lie at the same place in Koeln. Each visit takes 10 minutes.
- **Times:** the nodes are open and the resources work on May 6, 2020, from 08:00 to 22:00 (Europe/Berlin).

### The inner-city example

`ClusteringInnerCityExample` creates its elements in a loop:

- **Nodes and resources:** 1000 nodes, `N_0` to `N_999`, and 50 resources, `R_0` to `R_49`, on two phyllotaxis patterns around a point in the west of Koeln (50.9333, 6.85), the area that the figures of this example in the feature guide show.
- **Pattern:** element i lies at the distance spacing x sqrt(i) from that point and at i times the golden angle. The spacing is 0.002 degrees for the nodes and 0.01 degrees for the resources, so the outermost node lies about 0.063 degrees from that point and the outermost resource 0.07 degrees: the resources spread over the area of the nodes.
- **Times:** the nodes are open and the resources work on May 6, 2023, from 08:00 to 22:00 (Europe/Berlin). Each visit takes 5 minutes; each resource may work 12 hours, a sample value, and drive 2200 km.
- **Two days:** with the constant `ADD_EVEN_ODD_SPLITTING` set to `true`, the nodes and resources with an even index move to May 7, and the plan splits into two days. The committed value is `false`.

## How the code works

The sources are [ClusteringCityToCityExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/clustering/ClusteringCityToCityExample.java), [ClusteringCityToCityKeepClustersForOptimizationExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/clustering/ClusteringCityToCityKeepClustersForOptimizationExample.java) and [ClusteringInnerCityExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/clustering/ClusteringInnerCityExample.java). In all three, `example()` runs the same steps:

```java
// Needs your own full license file where ExampleLicenseHelper looks for it: the public
// evaluation license does not allow this many elements.
ExampleLicenseHelper.setLicense(this);
addProperties(this);
addNodes(this);
addResources(this);
attachToObservables(this);
IOptimizationResult result = startAndPresentResult(this);
exportToKml(result);
```

`ExampleLicenseHelper.setLicense` sets the license from your license file if it finds one, and the public evaluation license otherwise. `attachToObservables` and `startAndPresentResult` work as in the [First optimization example](../../basic/firstoptimization_01/FirstOptimizationExample.md); `startAndPresentResult` also returns the result for the KML export.

### Construction only

`ClusteringCityToCityExample` and `ClusteringInnerCityExample` set the same two properties:

```java
// Zero generations and zero iterations: no genetic evolution and no simulated annealing after
// the construction.
props.setProperty("JOptExitCondition.JOptGenerationCount", "0");
props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumIterations", "0");
```

### Keeping the clusters

`ClusteringCityToCityKeepClustersForOptimizationExample` runs a short optimization and turns the clusters into hard zones:

```java
// Fewer generations and iterations than the defaults keep the run short.
props.setProperty("JOptExitCondition.JOptGenerationCount", "1000");
props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumIterations", "100000");
// Turns the clusters that the construction finds into zones.
props.setProperty("JOpt.Clustering.AutoZoneCodeClusters", "True");
// Hard zones: no node may leave the cluster of the construction during the optimization.
props.setProperty("JOpt.Clustering.AutoZoneCodeClusters.isHard", "True");
```

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

`exportToKml` writes the result to the working directory, to `ClusteringCityToCityExample.kml`, `ClusteringCityToCityKeepClustersForOptimizationExample.kml` or `ClusteringInnerCityExample.kml`. The block shows the file of `ClusteringCityToCityExample`:

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

The clustering construction feature guide shows figures of the construction results for the inner-city and the city-to-city data.

### With a full license

With your own full license file, each class prints the result and writes its KML file. Open the KML file in Google Earth or another KML viewer to see the routes on a map. For the effect of the hard zones, compare the KML file of `ClusteringCityToCityKeepClustersForOptimizationExample` with the one of `ClusteringCityToCityExample`, which has no optimization after the construction.

## Run it

1. Put your own full license file where `ExampleLicenseHelper` looks for it, `src/main/resources/license/YOUR_LICENSE.jli`. [Set a license from a file](../../basic/setlicense_08/SetLicense.md) shows how the examples read it, and the [license guide: element limits](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/license#free-license) lists what each license allows.
2. Run the `main(String[] args)` method of one of the three classes from your IDE, with the repository imported as a Maven project. You need a JDK in the version that the examples pom sets.
3. Open the KML file in the working directory.

## Related

- [Clustering construction feature guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/clustering_construction): the construction algorithm, the figures of these examples and the clustering properties.
- [ZoneCodes feature guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/zonecodes): zones, which `ClusteringCityToCityKeepClustersForOptimizationExample` creates from the clusters.
- [Set a license from a file](../../basic/setlicense_08/SetLicense.md): how the examples read your own license file.
- [Optimization properties](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties): the property reference, with the defaults.
- [Advanced examples](../README.md): the overview of all Advanced examples.
- [Recommended ways to run an optimization](../../basic/recommendedimplementation_02/RecommendedImplementation.md): the recommended run pattern, a synchronous run and the legacy callback methods.

---

Added: 2026-02-17 (JOpt core 7.5.3-j17) | Last updated: 2026-10-20
