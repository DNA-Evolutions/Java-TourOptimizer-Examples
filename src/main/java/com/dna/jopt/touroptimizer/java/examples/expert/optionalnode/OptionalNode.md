# Optional nodes as reload and drop stops

`ReloadBaseExample` and `ReloadBaseWithUnloadAllExample` give one truck optional nodes that the optimizer visits only where they help: reload stops for a truck that delivers, and drop stops, one of them unloading everything, for a truck that picks up. An optional node far away in Dresden is never visited. The page is for developers who model stopovers such as depots, dumps and reload bases; the [First optimization example](../../basic/firstoptimization_01/FirstOptimizationExample.md) introduces the steps of the code.

## What the example shows

- **Optional nodes:** both classes mark four nodes with `setIsOptional(true)`. The optimizer visits an optional node only where the visit helps; the far node DresdenFarAwayOptional is in no route of the test runs.
- **Reload stops:** in `ReloadBaseExample` the truck starts empty and delivers 10 units at each of five nodes. Its optional nodes reload 20 units each, and three of them are in the route.
- **Drop stops and an unload-all stop:** in `ReloadBaseWithUnloadAllExample` the truck picks up 65 units but holds 30. It unloads everything at the optional node KoelnOptional and drops 10 units at one of the two optional nodes in Oberhausen.
- **No capacity violation:** in the test runs both routes keep the load of the truck between empty and full.
- **A KML file:** `ReloadBaseExample` also writes its result to the file `ReloadBaseExample.kml`; `ReloadBaseWithUnloadAllExample` writes no file.

Both classes use the array load model of JOpt: `setLoad(double[])` on a node, `setCapacity(double[])` and `setInitialLoad(double[])` on the resource, and `setUnloadAll(true)` for a node at which the resource unloads everything. The pickup and delivery (PND) model of JOpt plans the same kind of stop with an `UnloadAllLoad` in the depot of an optional node: [PNDOptionalUnloadAllLoadExample](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/pickupanddelivery/PNDOptionalUnloadAllLoadExample.java) shows it, and the pickup and delivery feature guide describes it in [TimedLoad, FlexLoad, and UnloadAllLoad](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/pickup_and_delivery#timedload-flexload-and-unloadallload).

## Scenario

Jack from Aachen drives a truck on May 7, 2020, from 08:00 to 17:00 (Europe/Berlin), with up to 10 hours of working time and 1200 km. The truck starts empty. Every node is open from 08:00 to 17:00 on that day and takes a visit of 20 minutes. The dates are sample values, and so are the coordinates in Germany. In both classes, `addProperties` sets `JOptExitCondition.JOptGenerationCount` to 1000 and `JOpt.Algorithm.PreOptimization.SA.NumIterations` to 100000. Both are below their defaults, which keeps the run short.

### Deliveries in ReloadBaseExample

The truck holds 20 units. Five nodes get a delivery of 10 units each: Koeln2 and Koeln3 in Koeln, Oberhausen2 and Oberhausen3 in Oberhausen, and Essen1 in Essen. Four optional nodes reload 20 units each: KoelnOptional, OberhausenOptional and OberhausenOptional2 at the places of the deliveries, and DresdenFarAwayOptional in Dresden.

### Pickups in ReloadBaseWithUnloadAllExample

The truck holds 30 units. Five nodes give 65 units to pick up: 15 each at Koeln2, Koeln3 and Essen1, and 10 each at Oberhausen2 and Oberhausen3. At the optional node KoelnOptional the truck unloads everything on board; the optional nodes OberhausenOptional, OberhausenOptional2 and DresdenFarAwayOptional take 10 units each. With one unload-all stop the truck carries at most 30 units before it and 30 after it, so the 65 units need at least one drop stop as well.

## How the code works

The example sources in this package are [ReloadBaseExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/optionalnode/ReloadBaseExample.java) and [ReloadBaseWithUnloadAllExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/optionalnode/ReloadBaseWithUnloadAllExample.java). `example()` sets the license, adds the properties, the nodes and the resource, subscribes to the events and starts the run, in the steps of the first optimization example. `ReloadBaseExample` also writes the result to the KML file `ReloadBaseExample.kml`; `ReloadBaseWithUnloadAllExample` writes no file.

These two examples demonstrate Optional Nodes as *reload bases / unload points*:
- Reload Base (the basic optional node concept, here with reload stops): `ReloadBaseExample`.
- Reload Base + `setUnloadAll(true)` (mix of partial unload + full unload): `ReloadBaseWithUnloadAllExample`.

Both examples are intentionally "didactic":
- they show the mechanism clearly,
- and demonstrate that optional nodes can be placed in locations that are clearly beneficial vs clearly not beneficial (to visualize the decision).

### The sign of a load

In the array load model, JOpt takes the load of a node off the load of the resource. `ReloadBaseExample` uses positive values for its deliveries and negative values for its reloads:

```java
// In the array load model, the load of a node is taken off the load of the truck: a positive
// value is a delivery, a negative value adds goods to the truck.
double[] deliveryPerNode = { 10 };
double[] reloadPerOptionalNode = { -20 };
```

`ReloadBaseWithUnloadAllExample` uses negative values for its pickups and positive values for its drops:

```java
// In the array load model, the load of a node is taken off the load of the truck: a negative
// value is a pickup, a positive value a drop.
double[] loadPerNode = { -10 }; // We pick up 10 units
double[] loadPerNodeHigh = { -15 }; // We pick up 15 units
double[] unloadPerOptionalNode = { 10 }; // We drop 10 units
```

### Marking a node as optional

A node becomes optional with `setIsOptional(true)`; its load gives it its meaning. In `ReloadBaseExample`:

```java
// Only the four nodes with "Optional" in their id are optional. The optimizer visits them
// where a reload avoids a delivery without goods on board.
INode koelnOptional =
    new TimeWindowGeoNode(
        "KoelnOptional", 50.9333, 6.95, openingHours, visitDuration, importance);
koelnOptional.setIsOptional(true);
koelnOptional.setLoad(reloadPerOptionalNode);
opti.addElement(koelnOptional);
```

The truck holds 20 units and starts empty:

```java
// 20 units fit on the truck. It starts empty, so it has to reload before the first delivery.
jack.setCapacity(new double[]{ 20 });
jack.setInitialLoad(new double[]{ 0 });
```

### The unload-all node

In `ReloadBaseWithUnloadAllExample`, KoelnOptional has no load of its own:

```java
// Only the four nodes with "Optional" in their id are optional. KoelnOptional has no load of
// its own: setUnloadAll(true) lets the truck drop everything it carries there.
INode koelnOptional =
    new TimeWindowGeoNode(
        "KoelnOptional", 50.9333, 6.95, openingHours, visitDuration, importance);
koelnOptional.setIsOptional(true);
koelnOptional.setUnloadAll(true);
opti.addElement(koelnOptional);
```

The pickup at Essen1 makes one fixed drop stop necessary:

```java
// 15 units at Essen make the pickups 65 in all: more than the truck holds with one
// unload-all stop (30 before it, 30 after it), so one drop stop of 10 is needed as well.
INode essen1 =
    new TimeWindowGeoNode("Essen1", 51.45, 7.01667, openingHours, visitDuration, importance);
essen1.setLoad(loadPerNodeHigh);
opti.addElement(essen1);
```

### Events and the result

`attachToObservables` subscribes to the progress, warning, status and error events before the start, and `startAndPresentResult` waits for the result with a timeout of five minutes and prints it. In `ReloadBaseExample`, `exportToKml` then writes the result to `ReloadBaseExample.kml`, in a try-with-resources block, and prints a line if the export fails.

## Output and what to look for

Each stop of the route ends with its load: `Target` is the load of the node, `Actual` the change of the load on the truck (positive when goods come on board). An unload-all stop prints `UnloadAll` with the amount it takes off the truck.

### The route of ReloadBaseExample

In four test runs the route has three reload stops, each with `Actual[0] 20.0`, and five deliveries, each with `Actual[0] -10.0`, in this order: a reload at one of the two optional nodes in Oberhausen (arrival 09:11:03), the deliveries Oberhausen2 and Oberhausen3, a reload at the other optional node in Oberhausen (arrival 10:11:03), the delivery Essen1, one delivery in Koeln, the reload KoelnOptional and the other delivery in Koeln. Nodes at the same place can swap: which optional node in Oberhausen comes first, the order of Oberhausen2 and Oberhausen3 and the order of Koeln2 and Koeln3 can change between runs, while the times and the cost stay the same. Two stops were the same in all four test runs:

```text
0.4 Essen1 / Geo  / Arrival: 07.05.2020 10:39:55 (Europe/Berlin) , Departure: 07.05.2020 10:59:55 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 8.85 [min], 11.69 [km][Load: Target[0] 10.0 / Actual[0] -10.0 ]
0.6 KoelnOptional / Geo  / Arrival: 07.05.2020 12:03:33 (Europe/Berlin) , Departure: 07.05.2020 12:23:33 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 0.0 [min], 0.0 [km][Load: Target[0] -20.0 / Actual[0] 20.0 ]
```

The cost has no capacity part, so the route has no overload and no underload:

```text
Cost TotalSum: 253.85325539844993,Cost Distance: 226.23726928733882,Cost RouteTime: 27.61598611111111
```

DresdenFarAwayOptional is not in the route. The truck reloads 60 units and delivers 50, so it ends the day with 10 units on board. The route ends at 13:31:23, inside the working hours.

### The route of ReloadBaseWithUnloadAllExample

In four test runs the truck picks up 30 units at Koeln2 and Koeln3, whose order can change between runs, unloads them at KoelnOptional and picks up 15 units at Essen1. The unload-all stop and Essen1 were the same in all four test runs:

```text
0.2 KoelnOptional / Geo  / Arrival: 07.05.2020 09:27:59 (Europe/Berlin) , Departure: 07.05.2020 09:47:59 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 0.0 [min], 0.0 [km][UnloadAll: Actual[0] 30.0]
0.3 Essen1 / Geo  / Arrival: 07.05.2020 10:31:37 (Europe/Berlin) , Departure: 07.05.2020 10:51:37 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 43.64 [min], 57.61 [km][Load: Target[0] -15.0 / Actual[0] 15.0 ]
```

In Oberhausen the route then has the pickups Oberhausen2 and Oberhausen3 and one drop stop with `Target[0] 10.0 / Actual[0] -10.0`, at OberhausenOptional or at OberhausenOptional2. Which of the two takes the drop and the order of the three stops can change between runs; the route ends at the same time.

The cost again has no capacity part:

```text
Cost TotalSum: 251.92021976875378,Cost Distance: 225.98671699097602,Cost RouteTime: 25.933502777777775
```

DresdenFarAwayOptional and the second fixed drop stop are not in the route. The truck ends the day with 25 units on board; the route ends at 13:11:12, inside the working hours.

## Optional nodes in JOpt

Optional Nodes are a modeling tool that lets the optimizer **choose** whether a node should be visited or skipped.

In classic tour optimization, every node must be visited, which means every additional node inevitably increases cost.
Optional Nodes break that limitation: they allow you to add *potential* stops that can **reduce** total cost or even help a route avoid capacity violations (e.g., an overload or an underload). They are not **required for feasibility**: capacity is a pseudo constraint in JOpt, and an overload or an underload adds a violation and a cost weighted by `JOptWeight.Capacity`.

Reference documentation:
- [Special features: optional nodes](https://www.dna-evolutions.com/docs/learn-and-explore/special/special_features#optional-nodes)

## Why optional nodes exist (the operational story)

In real operations, there are many "nodes" that are not customer jobs, but *operational stopovers*:

- waste deposits to dump collected waste (capacity relief),
- reload bases / depots to restock,
- intermediate drop points,
- optional "factory" or "supply" points for manufacturing planning,
- contingency stops ("use if needed"),
- optional tasks that are nice-to-have but not mandatory.

You usually do not want to hardcode these stopovers into every plan.
Instead, you want the optimizer to decide:

- Is this stop necessary to stay feasible (capacity/time windows)?
- Does this stop reduce overall cost enough to justify the detour?

Optional Nodes provide exactly this decision capability.

## The core semantics

### Mandatory node
- Must be visited exactly once (unless otherwise modeled).
- Always contributes to the plan.

### Optional node
- May or may not be visited.
- If it is visited, it behaves like a normal node:
  - opening hours/time windows apply,
  - travel time and travel distance apply,
  - visit duration applies,
  - and all other constraints apply.

Key implication:
- Optional Nodes do **not** bypass constraint logic.
  They only change the *decision* "visit or skip".

## When the optimizer will choose to visit an optional node

The optimizer tends to visit an optional node when one of these applies:

1. **Feasibility enabler**
   Visiting the optional node is necessary to avoid capacity violations (e.g., an overload or an underload). Capacity constraints are pseudo constraints in JOpt, so the visit does not satisfy a hard feasibility rule: a route that would otherwise need the visit does not become infeasible, but carries these violations and their cost (`JOptWeight.Capacity`).

2. **Cost reducer**
   Visiting the optional node decreases the overall objective cost more than the detour increases it.
   Classic example: unloading reduces load-related cost (overload penalties in these examples; fuel or risk where your own cost model, for example an open assessor, prices the load), so the optimizer inserts a dump/reload stop.

3. **Preference / policy shaping**
   If your cost model or open assessor introduces penalties for certain states (e.g., driving with high load, overtime, lateness), an optional node can become the economically best "escape hatch."

Optional nodes are therefore a structural modeling feature that creates additional degrees of freedom in the solution space.

## Implementation pattern

### Step 1: Create your normal nodes (jobs)
Use any node type you typically use (e.g., `TimeWindowGeoNode`).

### Step 2: Create the optional node
Create it like any other node and then mark it as optional. A sketch, not code of the example classes:

```java
node.setIsOptional(true);
```

### Step 3: Give the optional node its operational meaning
In capacity scenarios, this is typically done by assigning a load/unload vector. A sketch, not code of the example classes:

```java
node.setLoad(...);
```

Important:
- The sign convention of the load vector depends on the array load model, not on your scenario/modeling choices: JOpt takes the load of a node off the load of the resource. A positive value is a delivery (the load on the resource goes down), a negative value a pickup or a reload (it goes up).
- In the provided examples you will see both "pickup-like" and "unload-like" behavior represented through positive/negative values: `ReloadBaseExample` delivers 10 at its mandatory nodes and reloads 20 at its optional nodes, `ReloadBaseWithUnloadAllExample` picks up 10 or 15 at its mandatory nodes and drops 10 at its optional nodes.
- The relevant point is: the optional node is configured so that visiting it changes the resource's load state in a way that can improve feasibility/cost.

### Step 4, optional: Enable "unload all" semantics
If you want a node to empty the resource's current load (or drop as much as possible), enable it. A sketch, not code of the example classes:

```java
node.setUnloadAll(true);
```

This turns a node from "drop a fixed amount" into "drop everything / drop maximum possible": at the visit, the load on the resource goes to 0. Set it on a node without a load: on a node that already has a load, JOpt ignores `setUnloadAll(true)` and logs a warning.

This is especially useful for:
- waste management (empty truck),
- depot return (reset load),
- or end-of-route normalization (avoid returning with cargo).

## What the examples demonstrate

### Example A: ReloadBaseExample, deliveries with optional reload stops

High-level idea:
- A resource has a limited capacity (e.g., 20 units) and starts empty.
- Mandatory nodes represent deliveries.
- Optional nodes represent unload/reload points; here they reload 20 units each.
- The optimizer inserts a reload stop where the truck would otherwise deliver without goods on board (an underload).

The example also includes an optional node that is *far away* to demonstrate a key property:
- Optional nodes are not visited "because they exist".
- They are visited only if they are beneficial (feasibility or total cost).

This is a useful validation pattern for your own models:
- place a clearly-bad optional node in an unreasonable location,
- confirm it is not selected,
- thereby validating that the model is not biased toward "always visit optional nodes".

### Example B: ReloadBaseWithUnloadAllExample, partial unload and full unload

This example extends the idea:
- multiple optional nodes allow unloading a fixed amount,
- and **one special optional node** has `unloadAll` enabled.
- Mandatory nodes represent pickups/loads.
- The optimizer may insert an unload stop when the route would otherwise exceed capacity.

In four test runs the route uses the unload-all node and one of the fixed drop nodes.

This models a common real-world situation:
- small drop points reduce load incrementally,
- but a depot or transfer station can fully reset load, which may be ideal before continuing to pick up more goods.

This is also a useful pattern for mixed pickup/delivery planning:
- optional stopovers provide controllable "state reset points" without forcing a fixed schedule structure.

## Modeling guidance (what works well in production)

### Optional nodes are an architectural alternative to brittle cost tuning
If you need "sometimes a dump, sometimes not", optional nodes are a structural tool.
Avoid simulating this behavior purely via:
- extremely large penalty costs,
- or ad-hoc rules outside the optimizer.

Optional nodes create a clean and explainable decision:
- "the optimizer visited the dump because it reduced total cost / avoided capacity violations".

### Use optional nodes when state matters
Optional nodes are most valuable when visiting them changes some route state, such as:
- capacity/load,
- inventory,
- temperature chain constraints, where your own model tracks the temperature,
- or any other modeled resource state.

This is why they appear frequently in PND-related workflows.

### Keep the optional node realistic
Even though the node is optional, it should still reflect reality:
- opening hours,
- visit duration,
- and correct geo location (if applicable).

If the optional node is visited, it must be operationally feasible.

### Consider "useless visits" prevention
Some setups benefit from discouraging pointless unloadAll visits.
The global properties list includes a dedicated weight for this concept:

- [Optimization properties](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties) (search for `JOptWeight.UselessUnloadAllVisitation`)

This is relevant when you enable unloadAll and want to ensure it is used only when it provides real value. In the array load model of these examples, a visit of a node with `setUnloadAll(true)` counts as useful even when the truck is empty, so the weight adds no cost to such a visit.

## Summary

- Optional Nodes allow the optimizer to decide whether to schedule a node or skip it.
- They are ideal for modeling reload bases, dump sites, restocking points, and optional tasks.
- If visited, optional nodes behave like normal nodes (time windows, travel, duration, constraints).
- `setUnloadAll(true)` upgrades an optional node from "drop a fixed amount" to "drop everything / drop maximum possible".
- The provided examples demonstrate both:
  - optional nodes as capacity relief points (reload stops for deliveries, drop stops for pickups),
  - and why clearly non-beneficial optional nodes are naturally skipped.

## Run it

Run the `main(String[] args)` method of `ReloadBaseExample` or `ReloadBaseWithUnloadAllExample` from your IDE, with the repository imported as a Maven project. You need a JDK in the version that the examples pom sets. No license file is needed: without one, the examples use the public evaluation license (see the [license guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/license)). `ReloadBaseExample` writes `ReloadBaseExample.kml` into the working directory.

## Related

- [Special features](https://www.dna-evolutions.com/docs/learn-and-explore/special/special_features): the Special Features overview, with the section on optional nodes.
- [Pickup and delivery](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/pickup_and_delivery): the feature guide of the Pickup and Delivery module (optional nodes are often used in PND and manufacturing planning), with `UnloadAllLoad`.
- [Pickup and delivery examples](../../advanced/pickupanddelivery/PND.md): the PND examples, among them `PNDOptionalUnloadAllLoadExample` with optional dumps.
- [Optimization properties](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties): the property reference, with `JOptWeight.Capacity` and `JOptWeight.UselessUnloadAllVisitation`.
- [Recommended ways to run an optimization](../../basic/recommendedimplementation_02/RecommendedImplementation.md): the recommended run pattern, a synchronous run and the legacy callback methods.
- [Expert examples](../README.md): the overview of all Expert examples.

---

Added: 2026-02-17 (JOpt core 7.5.3-j17) | Last updated: 2026-10-23
