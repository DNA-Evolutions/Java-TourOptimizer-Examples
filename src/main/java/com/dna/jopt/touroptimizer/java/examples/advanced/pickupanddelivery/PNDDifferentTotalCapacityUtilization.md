# A depot capacity shared by two goods

`PNDDifferentTotalCapacityUtilization` gives a truck a depot of 70 that two goods share: `DelicateFruit` may use at most 30 of it, `GlassVials` all 70. Because the truck starts with 30 `DelicateFruit` on board, it has to deliver fruit before it can pick up a large supply of `GlassVials`. The page is for developers who give goods their own limits inside one shared capacity. The building blocks of pickup and delivery (PND) are explained in the [pickup and delivery feature guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/pickup_and_delivery), and [Pickup and delivery examples](PND.md) lists the other PND examples.

## What the example shows

- A `SimpleResourceDepot` has a total capacity, here 70, that all its load capacities share. Each `SimpleLoadCapacity` has its own maximum and an initial load: 30 and 30 for `DelicateFruit`, 70 and 10 for `GlassVials`.
- The space left for one good depends on the other goods on board. With 30 `DelicateFruit` and 10 `GlassVials`, 30 more fit, although `GlassVials` alone may use all 70.
- In this example the shared capacity decides the order of the visits: the truck delivers the `DelicateFruit` of `CustomerOne` before it picks up the `GlassVials` of `CustomerTwo`.

## Scenario

The dates, times, coordinates and amounts are sample values.

- **Resource:** `Truck` starts at 50.1167, 7.68333 and works on March 11, 2020, from 08:00 to 20:00 (Europe/Berlin). Its maximal working time is 10 hours and its maximal distance 1200 km.
- **Nodes:** `CustomerOne` and `CustomerTwo`, two `TimeWindowGeoNode` objects at one point in Koeln (50.9333, 6.95). They are open on March 11, 2020, from 08:00 to 18:00, and each visit takes 30 minutes. Both nodes are at one point, so the order of the visits changes no distance; only the loads on board decide it.

The depot of the truck, `truckDepot`, is a `SimpleResourceDepot` with a total capacity of 70 and two load capacities:

| Load capacity | Maximum | Initial load |
|---|---|---|
| `DelicateFruit` | 30 | 30 |
| `GlassVials` | 70 | 10 |

Each node has a `SimpleNodeDepot` with a request of `DelicateFruit` and a supply of `GlassVials`. Both loads are fuzzy, so a part of them can be exchanged:

| Node | `DelicateFruit` (request) | `GlassVials` (supply) |
|---|---|---|
| `CustomerOne` | 25 | 5 |
| `CustomerTwo` | 5 | 40 |

`addProperties` sets three properties:

- `JOptExitCondition.JOptGenerationCount` to 1000 and `JOpt.Algorithm.PreOptimization.SA.NumIterations` to 100000. Both are below their defaults, which keeps the run short.
- `JOptWeight.Capacity` to 200, above its default, so that a capacity violation costs more against the route time and distance.

The defaults are in the [property reference](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties).

## How the code works

The source is [PNDDifferentTotalCapacityUtilization.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/pickupanddelivery/PNDDifferentTotalCapacityUtilization.java). `example()` runs the steps in order:

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
- `addProperties` sets the three properties.
- `addNodes` adds the two customers with their depots.
- `addResources` adds `Truck` with its depot.
- `attachToObservables` subscribes to the progress, warning, status and error events with the event API, before the run starts.
- `startAndPresentResult` starts the run, waits up to 5 minutes for the result and prints it.

### The depot of the truck

`createResourceDepot` builds the depot with its total capacity and the two load capacities:

```java
// The total capacity of 70, which DelicateFruit and GlassVials share.
IResourceDepot depot = new SimpleResourceDepot("truckDepot", 70);

// DelicateFruit may use at most 30 of the 70; Truck starts with 30 on board.
ILoadCapacity fruitCapacity = new SimpleLoadCapacity("DelicateFruit", 30, 30);

// GlassVials may use all 70; Truck starts with 10 on board.
ILoadCapacity glassCapacity = new SimpleLoadCapacity("GlassVials", 70, 10);

depot.add(fruitCapacity);
depot.add(glassCapacity);
return depot;
```

`addResources` attaches the depot to `Truck` with `setResourceDepot`.

### The customers

`addNodes` gives each customer its depot:

```java
INode customerOne =
    new TimeWindowGeoNode("CustomerOne", 50.9333, 6.95, openingHours, visitDuration, 1);
customerOne.setNodeDepot(createNodeDepot("CustomerOneDepot", 25, 5));
opti.addElement(customerOne);

INode customerTwo =
    new TimeWindowGeoNode("CustomerTwo", 50.9333, 6.95, openingHours, visitDuration, 1);
customerTwo.setNodeDepot(createNodeDepot("CustomerTwoDepot", 5, 40));
opti.addElement(customerTwo);
```

`createNodeDepot` creates the two loads of a customer and adds them to a `SimpleNodeDepot`:

```java
// The third argument makes the load a request (true) or a supply (false); the fourth allows a
// partial exchange (fuzzy).
ILoad fruitLoad = new SimpleLoad("DelicateFruit", fruitRequest, true, true);
ILoad glassLoad = new SimpleLoad("GlassVials", glassSupply, false, true);
```

## Output and what to look for

The run prints the status and progress lines, then the result. In two test runs the result had the same route, `CustomerOne` and then `CustomerTwo`, and an empty violation summary.

The ResourceDepot report of `Truck`, the same in both runs:

```text
------- ResourceDepot Report (Truck)-------
START:
---
Id: truckDepot / Type: SimpleResourceDepot  / MaxTotalCapacity: 70.0 / UsedCapacity: 40.0
(0) LoadCapacity:
 Id: DelicateFruit / Type: SimpleLoadCapacity / Load: 30.0 / maxCapacity: 30.0 /

(1) LoadCapacity:
 Id: GlassVials / Type: SimpleLoadCapacity / Load: 10.0 / maxCapacity: 70.0 /

TERMINATION:
---
Id: truckDepot / Type: SimpleResourceDepot  / MaxTotalCapacity: 70.0 / UsedCapacity: 55.0
(0) LoadCapacity:
 Id: DelicateFruit / Type: SimpleLoadCapacity / Load: 0.0 / maxCapacity: 30.0 /

(1) LoadCapacity:
 Id: GlassVials / Type: SimpleLoadCapacity / Load: 55.0 / maxCapacity: 70.0 /
------- ResourceDepot Report END -------
```

At the start, 40 of the 70 are in use. `CustomerOne` takes 25 `DelicateFruit` and gives 5 `GlassVials`, which leaves 20 in use. `CustomerTwo` takes the last 5 `DelicateFruit` and gives 40 `GlassVials`, so the truck ends with 55 `GlassVials` and no `DelicateFruit`. The visit lines of both customers, the same in both runs:

```text
0.0 CustomerOne / Geo  / Arrival: 11.03.2020 09:19:19 (Europe/Berlin) , Departure: 11.03.2020 09:49:19 (Europe/Berlin) / Duration: 30.0 [min] / Driving: 79.33 [min], 104.71 [km]
0.1 CustomerTwo / Geo  / Arrival: 11.03.2020 09:49:19 (Europe/Berlin) , Departure: 11.03.2020 10:19:19 (Europe/Berlin) / Duration: 30.0 [min] / Driving: 0.0 [min], 0.0 [km]
```

After each visit, the depot line of the customer shows a desired load exchange of 0.0 for both goods: each load was exchanged in full.

The order matters because of the shared capacity. Visited first, `CustomerTwo` would meet a truck that still carries 25 `DelicateFruit` and 10 `GlassVials` after the fruit delivery, 35 of 70 in use. Its 40 `GlassVials` do not fit into the 35 that are left, although 50 `GlassVials` would stay below their own maximum of 70. In a test run with `CustomerOne` left out, the truck picked up 35 of the 40 `GlassVials` at `CustomerTwo`. The result reported a total overload of 5 (reason `TOTAL_OVERLOAD`) and no overload with the reason `OVERLOAD`, which marks a load capacity above its own maximum.

## Run it

Run the `main(String[] args)` method of `PNDDifferentTotalCapacityUtilization` from your IDE, with the repository imported as a Maven project. You need a JDK in the version that the examples pom sets. You need no license file: without one, the example uses the public evaluation license (see the [license guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/license)).

## Related

- [PNDDifferentTotalCapacityUtilization.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/pickupanddelivery/PNDDifferentTotalCapacityUtilization.java): the source of this example.
- [Pickup and delivery examples](PND.md): the other PND examples, one line each.
- [Pickup and delivery](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/pickup_and_delivery): the feature guide, with resource depots, load capacities and the types of capacity violations.
- [Optimization properties](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties): the property reference, with `JOptWeight.Capacity` and its default.
- [Recommended ways to run an optimization](../../basic/recommendedimplementation_02/RecommendedImplementation.md): the recommended run pattern, a synchronous run and the legacy callback methods.
- [Advanced examples](../README.md): the overview of all Advanced examples.

---

Added: 2026-10-15 (JOpt core 7.5.3-rc5-j17) | Last updated: 2026-10-21
