# Plus Codes instead of latitude and longitude

JOpt converts a position to a Plus Code (an [Open Location Code](https://maps.google.com/pluscodes/)) and a Plus Code back to a position. Two examples show it: `LatLon2PlusCodeBackAndForthExample` converts the positions of nodes and resources to codes and back without an optimization, and `OptimizationWithOpenLocationCodesExample` builds and runs an optimization whose nodes and resources take their positions from codes. The page is for developers who store or exchange locations as Plus Codes. If you have not built an optimization yet, start with the [First optimization example](../../basic/firstoptimization_01/FirstOptimizationExample.md).

## What the example shows

- **Position to code:** `toOpenLocationCode()` on a `Position` returns its Plus Code, for example `9F28WXM2+82` for Koeln at 50.9333, 6.95. The codes in both examples have 11 characters.
- **Code to position:** `OpenLocation.of(code).toPos()`, or `OpenLocation.builder().code(code).build().toPos()`, returns a `Position`.
- **A code is an area:** the decoded position is the centre of the code's cell. It is close to the original position but not the same: 50.9333, 6.95 becomes 50.9333125, 6.9500625.
- **Codes as input:** the constructors of `TimeWindowGeoNode` and `CapacityResource` take a `Position`, so an optimization can be built from codes alone.

## Scenario

The dates, times, coordinates and codes are sample values.

### The conversion scenario

`LatLon2PlusCodeBackAndForthExample` uses three nodes, Koeln, Oberhausen and Essen, and two resources, JackKoeln and JackJena, each with a latitude and a longitude. The example runs no optimization and needs no license. A node needs opening hours and a visit duration, and a resource needs working hours and limits; the example builds them only to create the elements and reads nothing but their positions.

### The optimization scenario

`OptimizationWithOpenLocationCodesExample` builds these elements:

- **Nodes:** seven `TimeWindowGeoNode` objects, each given as an id and a Plus Code: Koeln, Oberhausen, Essen, Nuernberg, Heilbronn, Stuttgart and Wuppertal. They are open on May 6, 2020, from 08:00 to 22:00 (Europe/Berlin). Each visit takes 10 minutes, and all nodes have the importance 1.
- **Resources:** two `CapacityResource` objects, JackKoeln and JackHeilbronn, each given as an id and the Plus Code of its start position. Both work on May 6, 2020, from 08:00 to 22:00, with a maximal working time of 12 hours and a maximal distance of 2200 km, more than the day of this plan needs.

`addProperties` sets two properties: `JOptExitCondition.JOptGenerationCount` to 1000 and `JOpt.Algorithm.PreOptimization.SA.NumIterations` to 100000. Both are below their defaults, which keeps the run short; the defaults are in the [property reference](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties).

## How the code works

### The conversion code

The source is [LatLon2PlusCodeBackAndForthExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/openlocation/LatLon2PlusCodeBackAndForthExample.java). The class does not extend `Optimization`. `example()` creates the elements and converts their positions:

```java
List<IOptimizationElement> elements = new ArrayList<>();
elements.addAll(createNodes());
elements.addAll(createResources());
convertElements(elements);
```

`convertElements` converts the position of each element to a code and the code back to a position:

```java
Position position = element.getPosition();
String plusCode = position.toOpenLocationCode();

OpenLocation location = OpenLocation.builder().code(plusCode).build();
Position positionBack = location.toPos();
```

### The optimization code

The source is [OptimizationWithOpenLocationCodesExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/openlocation/OptimizationWithOpenLocationCodesExample.java). `example()` runs the steps in order:

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

- `ExampleLicenseHelper.setLicense(this)` uses your license file if you put one where the helper looks for it, and the public evaluation license otherwise.
- `addProperties` sets the two properties.
- `addNodes` and `addResources` create the elements from their ids and codes.
- `attachToObservables` subscribes to the progress, warning, status and error events with the event API, before the run starts.
- `startAndPresentResult` starts the run, waits up to 5 minutes for the result and prints it.

`addNodes` holds the node ids and their codes in a map and creates one node per entry, with the position decoded from the code:

```java
nodeCodes.forEach(
    (id, code) -> {
      INode node =
          new TimeWindowGeoNode(
              id, OpenLocation.of(code).toPos(), openingHours, visitDuration, importance);
      opti.addElement(node);
    });
```

`addResources` does the same for JackKoeln and JackHeilbronn with `CapacityResource`.

## Output and what to look for

### The conversion output

`LatLon2PlusCodeBackAndForthExample` prints four lines per element: its id, its position, its code and the decoded position. The output was the same in two test runs; the lines of the first node:

```text
Element: Koeln
 Lat/Long (initial): Position{latitude=50.9333, longitude=6.95}
 PlusCode: 9F28WXM2+82
 Lat/Long (back): Position{latitude=50.9333125, longitude=6.9500625}
```

The resource JackKoeln at the same position gets the same code, `9F28WXM2+82`. JackJena at 50.93297, 11.58297 gets `9F2HWHMM+55`.

### The optimization output

`OptimizationWithOpenLocationCodesExample` prints the status and progress lines, then the result. In two test runs the result was the same: JackKoeln visits Koeln, Oberhausen, Essen and Wuppertal, and JackHeilbronn visits Nuernberg, Stuttgart and Heilbronn. The first node line of JackKoeln:

```text
0.0 Koeln / Geo  / Arrival: 06.05.2020 08:00:00 (Europe/Berlin) , Departure: 06.05.2020 08:10:00 (Europe/Berlin) / Duration: 10.0 [min] / Driving: 0.0 [min], 0.0 [km]
```

Koeln and JackKoeln have the same code and therefore the same decoded position, so the route starts with a drive of 0.0 km.

## Run it

Run the `main(String[] args)` method of either class from your IDE, with the repository imported as a Maven project. You need a JDK in the version that the examples pom sets. `LatLon2PlusCodeBackAndForthExample` needs no license. `OptimizationWithOpenLocationCodesExample` needs no license file: without one, it uses the public evaluation license (see the [license guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/license)).

## Related

- [LatLon2PlusCodeBackAndForthExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/openlocation/LatLon2PlusCodeBackAndForthExample.java): the conversion without an optimization.
- [OptimizationWithOpenLocationCodesExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/openlocation/OptimizationWithOpenLocationCodesExample.java): the optimization built from codes.
- [Open Location Code package in the JOpt JavaDocs](https://public.javadoc.dna-evolutions.com/com/dna/jopt/member/unit/converter/openlocation/package-summary.html): the API documentation of `OpenLocation` and the converter.
- [Plus Codes](https://maps.google.com/pluscodes/): the public description of Open Location Codes.
- [Recommended ways to run an optimization](../../basic/recommendedimplementation_02/RecommendedImplementation.md): the recommended run pattern, a synchronous run and the legacy callback methods.
- [Advanced examples](../README.md): the overview of all Advanced examples.

---

Added: 2026-02-17 (JOpt core 7.5.3-j17) | Last updated: 2026-10-20
