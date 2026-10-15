# Different Total Capacity Utilization: `PNDDifferentTotalCapacityUtilization`

This pickup and delivery (PND) example shows a truck whose depot has a **total capacity** that is shared by two **load capacities** with different limits. One good may only use a part of the truck (a special compartment); the other good may use the whole truck, including that compartment.

For the PND building blocks (NodeDepot, ResourceDepot, Load, LoadCapacity), see [PND.md](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/pickupanddelivery/PND.md).

---

## References

- Example source: [PNDDifferentTotalCapacityUtilization.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/pickupanddelivery/PNDDifferentTotalCapacityUtilization.java)
- Pickup and delivery documentation: [ResourceDepot and LoadCapacity](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/pickup_and_delivery#resourcedepot-and-loadcapacity)

---

## Scenario

The truck transports two goods:

- pallets of `DelicateFruit`, which must be stored in a special compartment (temperature and humidity),
- pallets of `GlassVials`, which are well packed and can be stored in the normal part of the truck or in the compartment for the fruit.

### The truck and its depot

The resource `Truck` has one working day, March 11, 2020, 08:00 to 20:00 (Europe/Berlin), a max working time of 10 hours and a max distance of 2800 km.

`createResourceDepot()` gives the truck a `SimpleResourceDepot` with a total capacity of 70 and two `SimpleLoadCapacity` entries:

```java
IResourceDepot depot = new SimpleResourceDepot("truckDepot", 70);

ILoadCapacity delicateFruitLoadCapacity = new SimpleLoadCapacity("DelicateFruit", 30, 5);
ILoadCapacity glassVialCapacity = new SimpleLoadCapacity("GlassVials", 70, 10);
```

| Load capacity | Maximum | Initial load | Meaning in the example |
|---|---|---|---|
| `DelicateFruit` | 30 | 5 | fits only into the compartment, which holds 30 |
| `GlassVials` | 70 | 10 | fits everywhere, up to the total capacity of 70 |

Both load capacities share the total capacity of 70 of the depot. So the space left for `GlassVials` depends on how much `DelicateFruit` is on the truck, and the reverse.

### The customers

Two nodes, `CustomerOne` and `CustomerTwo`, both at 50.9333, 6.95, are open on March 11, 2020, 08:00 to 18:00 (Europe/Berlin), with a visit duration of 30 minutes. `createNodeDepot(...)` gives each of them a `SimpleNodeDepot` with two `SimpleLoad` entries:

| Node | `DelicateFruit` (request, delivered) | `GlassVials` (supply, picked up) |
|---|---|---|
| `CustomerOne` | 2 | 10 |
| `CustomerTwo` | 3 | 10 |

```java
ILoad deliverGoodLoad = new SimpleLoad("DelicateFruit", fruitRequest, true, true);
ILoad pickupGoodLoad = new SimpleLoad("GlassVials", glassRequest, false, true);
```

The third argument is the request flag (`true`: the node requests the good, `false`: the node supplies it). The fourth argument is the fuzzy flag; both loads are created with `true`.

---

## Solver properties

`setProperties()` registers:

- `JOptExitCondition.JOptGenerationCount = 1000`
- `JOpt.Algorithm.PreOptimization.SA.NumIterations = 1000`
- `JOptWeight.Capacity = 200`, to give capacity planning a high weight

---

## Execution flow

1. Set the license via `ExampleLicenseHelper.setLicense(this);`
2. `setProperties()`, `addNodes()`, `addRes()`
3. Start the run with `startRunAsync()` and subscribe to progress, error, warning and status events.
4. Wait for the result with `resultFuture.get(2, TimeUnit.MINUTES)` and print it.

---

## What to look for in the result

The printed result contains the depot information of the truck and of the two customers. Check:

- that both customers receive their `DelicateFruit` (2 and 3, from the initial load of 5),
- that both customers hand over their `GlassVials` (10 each, on top of the initial load of 10),
- that the load of the truck never exceeds the total capacity of 70, and `DelicateFruit` never exceeds 30.

---

## How to run

Run the `main(String[] args)` method of `PNDDifferentTotalCapacityUtilization`.
