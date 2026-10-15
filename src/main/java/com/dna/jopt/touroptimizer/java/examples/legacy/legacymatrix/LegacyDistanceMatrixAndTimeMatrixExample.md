# Legacy Distance and Time Matrix: `LegacyDistanceMatrixAndTimeMatrixExample`

This example shows the **legacy way** to pass precomputed distances and driving times to the optimizer: as a distance matrix and a time matrix, with a matrix index on each node. The methods it uses are deprecated in the current core. For new code, use a node connector instead (see `ExternalNodeConnectionExample`); the `toString()` of the example says the same: "Defining a time and distance matrix. (Legacy - use NodeConnector instead)".

The example is kept so that existing integrations that still use matrices can be understood and migrated.

---

## References

- Example source: [LegacyDistanceMatrixAndTimeMatrixExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/legacy/legacymatrix/LegacyDistanceMatrixAndTimeMatrixExample.java)
- The replacement, a node connector with explicit connections: [ExternalNodeConnection.md](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic/connection_04/ExternalNodeConnection.md)

---

## What the example does

### 1) Pass the two matrices

```java
// Set Matrix in [km]
double[][] distanceMatrix = {
  {0.0, 11.0, 21.0}, {13.0, 0.0, 42.0}, {14.0, 30.0, 0.0}, {16.0, 20.0, 47.0}
};
this.addDistanceMatrix(distanceMatrix);

// Set Matrix in [seconds]
double[][] timeMatrix = {
  {0.0, 2100.0, 2200.0}, {4300.0, 0.0, 4200.0}, {2400.0, 3000.0, 0.0}, {2600.0, 8000.0, 4700.0}
};
this.addTimeMatrix(timeMatrix);
```

- `addDistanceMatrix(double[][])` takes the distances; the code comment gives the unit as kilometers.
- `addTimeMatrix(double[][])` takes the driving times; the code comment gives the unit as seconds.
- Both matrices in the example have four rows and three columns.

### 2) Give each node its matrix index

```java
koeln.setDistMatrixId(0);
heilbronn.setDistMatrixId(1);
```

The two nodes `Koeln` and `Heilbronn` (`TimeWindowGeoNode`) get the matrix indices 0 and 1. They are open on March 6 and March 7, 2020, 08:00 to 17:00 (Europe/Berlin), with a visit duration of 20 minutes.

### 3) Resources

Two `CapacityResource` instances, `Jack` (max working time 8 hours) and `John` (14 hours), both start at 50.775346, 6.083887, with a max distance of 1200 km and two working days: March 6, 2020, 08:00 to 17:00 and March 7, 2020, 08:00 to 20:00 (Europe/Berlin). The code does not set a matrix index for the resources.

### 4) Run and export

The example starts the run with `startRunAsync().get()`. In `onAsynchronousOptimizationResult(...)` it prints the result and writes `LegacyDistanceMatrixAndTimeMatrixExample.kml` to the working directory with `EntityKMLExporter`.

---

## Deprecated API

`addDistanceMatrix(double[][])`, `addTimeMatrix(double[][])` and `setDistMatrixId(int)` are deprecated in the core. The example suppresses the compiler warnings with `@SuppressWarnings("deprecation")` on `example()` and `addNodes()`.

To migrate, create one `NodeEdgeConnectorItem` per pair of elements with its distance and driving time, put the items into a `NodeEdgeConnector` and set it with `setNodeConnector(...)`, as shown in `ExternalNodeConnectionExample`.

---

## How to run

Run the `main(String[] args)` method of `LegacyDistanceMatrixAndTimeMatrixExample`. After the run, the working directory holds `LegacyDistanceMatrixAndTimeMatrixExample.kml`.
