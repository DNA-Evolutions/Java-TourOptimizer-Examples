# Pickup and delivery examples

The classes in the `pickupanddelivery` package show the pickup and delivery (PND) module of JOpt: node depots with the loads that a node requests or supplies, resource depots with load capacities, and the load types with which the optimizer sets amounts. Each class covers one load type, one depot setting or the reports, with one or two resources. The page is for Java developers who model the goods on board; the concepts and the depot reports are explained in the [pickup and delivery feature guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/pickup_and_delivery). If you have not built an optimization yet, start with the [First optimization example](../../basic/firstoptimization_01/FirstOptimizationExample.md).

| Example | What it shows |
|---|---|
| [PNDSimpleExample](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/pickupanddelivery/PNDSimpleExample.java) | A truck delivers Fruit to two customers and picks up Bread from them, with `SimpleLoad` and `SimpleLoadCapacity`; then the example prints the optimization as JSON, without a license. |
| [PNDSimpleFuzzyExample](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/pickupanddelivery/PNDSimpleFuzzyExample.java) | Fuzzy loads, which allow a partial delivery or pickup, next to loads that allow only a complete one. The plan cannot be served in full, so the result shows UNDERLOAD and OVERLOAD violations. |
| [PNDCapacityFactorExample](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/pickupanddelivery/PNDCapacityFactorExample.java) | `CargoSpace` and `CargoSpaceGood` turn the floor space of a truck, a piano and a pallet of cups into capacity factors, so that one piano uses more of the depot than one pallet. |
| [PNDDifferentTotalCapacityUtilization](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/pickupanddelivery/PNDDifferentTotalCapacityUtilization.java) | Two goods with different maximums share the total capacity of a depot, so that the goods on board limit what the truck can pick up. Its own page: [A depot capacity shared by two goods](PNDDifferentTotalCapacityUtilization.md). |
| [PNDMixedFlexLoadExample](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/pickupanddelivery/PNDMixedFlexLoadExample.java) | The `MixedFlexLoad` objects of an extra node act as a store of Fridge and TV; the optimizer sets their amounts and whether each one is a supply or a request. |
| [PNDRequestAndSupplyFlexLoadExample](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/pickupanddelivery/PNDRequestAndSupplyFlexLoadExample.java) | A `SupplyFlexLoad` only gives goods to the truck, a `RequestFlexLoad` only takes goods from it; the optimizer sets their amounts. |
| [PNDBakeryChainFlexLoadExample](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/pickupanddelivery/PNDBakeryChainFlexLoadExample.java) | Two bakeries with a `SupplyFlexLoad` of Bread each: the optimizer sets how much Bread each bakery makes for the stores on its route. Then the example prints the optimization as JSON, without a license. |
| [PNDOptionalUnloadAllLoadExample](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/pickupanddelivery/PNDOptionalUnloadAllLoadExample.java) | A truck picks up Trash and Garbage and unloads all of it at optional dumps with `UnloadAllLoad`, so that its depot does not overflow. |
| [PNDTimedLoadExample](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/pickupanddelivery/PNDTimedLoadExample.java) | Models a taxi, `JackTaxi`, that should bring passengers from one place to another, each within a maximal transport time of one hour; the example creates a `TimedLoad` for this limit. |
| [PNDTimedPizzaDeliveryExample](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/pickupanddelivery/PNDTimedPizzaDeliveryExample.java) | Models two pizza restaurants of one chain, each with one driver, that should deliver each pizza within 90 minutes; the example uses `TimedLoad` for this limit and `TimedSupplyFlexLoad` so that the optimizer can decide which restaurant prepares which pizza. |
| [PNDReportExtractionExample](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/pickupanddelivery/PNDReportExtractionExample.java) | Reads the ResourceDepot and NodeDepot reports out of the result with the getters of the depots, loads and load capacities, and prints them in its own format. |

## Why PND is different from classic CVRP/VRPTW

The PND module of JOpt tracks goods in depots, checks capacity, and drives routing decisions accordingly. A capacity violation is priced into the cost, a pseudo constraint: the result of `PNDSimpleFuzzyExample` shows UNDERLOAD and OVERLOAD violations together with a `Cost Capacity`.

Classic tour optimization focuses on:
- assigning nodes to resources,
- respecting time windows and working hours,
- minimizing distance/time/cost.

PND adds an additional, highly restrictive dimension:
- **what is inside the vehicle at any time**,
- and whether a planned sequence is feasible with respect to:
  - *available supply*,
  - *customer requests*,
  - *vehicle capacity*,
  - *time-limited transport* (perishable items, passengers, SLAs).

The JOpt PND module therefore introduces explicit objects for:
- **what the node wants to give or receive**, and
- **what the resource can carry and currently carries**.

## Core building blocks

### 1) NodeDepot (what a Node requests or supplies)

A `Node` may carry a **NodeDepot**.  
Think of it as the node's "warehouse interface": it describes which goods are exchanged at this stop.

Key characteristics:
- A NodeDepot holds one or more **Loads**.
- Each Load describes a single good type and, apart from an UnloadAllLoad, the direction (request vs supply) and the quantity. An UnloadAllLoad names only the good type and takes all of it that is on board.

Reference: [NodeDepot and Load](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/pickup_and_delivery#nodedepot-and-load)

### 2) ResourceDepot (what a Resource can carry and initially carries)

A `Resource` may carry a **ResourceDepot**.  
Think of it as the vehicle's cargo model.

Key characteristics:
- A ResourceDepot holds one or more **LoadCapacities**.
- A LoadCapacity declares:
  - which goods can be transported,
  - the maximum individual capacity for that good,
  - and the initial load amount before route start.

Reference: [ResourceDepot and LoadCapacity](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/pickup_and_delivery#resourcedepot-and-loadcapacity)

### 3) Load IDs must match (the most important modeling rule)

A Load and a LoadCapacity interact by **ID**.  
If the node requests `"Bread"`, then at least one resource must have a LoadCapacity for `"Bread"`.

Practical rule:
- If your loads are `"Bread"`, `"Fruit"`, `"Waste"`, your vehicle must declare capacities for these exact IDs (and your own "ID naming conventions" must be consistent across systems).

## Loads and exchange semantics (request vs supply)

A Load can represent either:

- **Request** (delivery): the node wants to receive goods from the resource.
- **Supply** (pickup): the node wants to give goods to the resource.

In addition, JOpt supports **fuzzy** exchanges:
- fuzzy = partial delivery/pickup is acceptable.
- non-fuzzy = the exchange must be complete.

This is a powerful real-world feature, because "partial fulfillment" is often allowed for some customers but not for others.

Reference: [At a glance](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/pickup_and_delivery#at-a-glance)

## Capacity violations and why they matter

In PND, an infeasible sequence is typically caused by one of these situations:

- **Underload**: the vehicle does not carry enough of the requested good to satisfy a request.
- **Overload**: the vehicle exceeds its available capacity when picking up supplies.

JOpt's PND system tracks these states explicitly.  
Depending on your modeling (especially fuzzy logic and FlexLoads), the optimizer can:
- avoid the violation by rerouting, reloading, or visiting dump/unload nodes,
- allow partial fulfillment (fuzzy),
- or keep the violation in the result, where it is priced into the cost (a pseudo constraint, not a hard failure: `PNDSimpleFuzzyExample` returns its result with UNDERLOAD and OVERLOAD violations and a `Cost Capacity`).

Reference: [Types of Capacity Violations](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/pickup_and_delivery#types-of-capacity-violations)

## Load types and when to use them

The PND module provides multiple load types to cover real industrial use cases.  
The key idea is: **choose the simplest load type that matches your reality**, because simpler load types are faster to evaluate and easier to explain.

Reference: [TimedLoad, FlexLoad, and UnloadAllLoad](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/pickup_and_delivery#timedload-flexload-and-unloadallload)

### A) SimpleLoad (fixed request or fixed supply)
Use when:
- quantities are fixed (or you handle flexibility outside the optimizer),
- you need classic pickup/delivery behavior.

Typical examples:
- deliver 3 pallets of fruit,
- pick up 4 bags of waste.

In code, you typically declare:
- load ID
- load value
- request flag
- fuzzy flag (partial allowed or not)

Examples:
- [PNDSimpleExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/pickupanddelivery/PNDSimpleExample.java)
- [PNDSimpleFuzzyExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/pickupanddelivery/PNDSimpleFuzzyExample.java)

### B) TimedLoad (time limit between pickup and delivery)
Use when:
- goods or entities must be transported within a maximum duration, e.g.:
  - passengers,
  - chilled products,
  - "pickup must be delivered within X minutes".

This introduces a strong precedence/time coupling that typical VRPTW modeling cannot express cleanly.

Example:
- [PNDTimedLoadExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/pickupanddelivery/PNDTimedLoadExample.java)

### C) FlexLoads (flexible amount and/or flexible role)
FlexLoads are the key that makes **manufacturing planning** and **dynamic inventory decisions** possible inside the optimizer.

FlexLoads allow the optimizer to decide:
- how much a node supplies (production),
- how much a node requests (consumption),
- and in some cases whether the load is treated as supply or request.

Use cases include:
- producing goods at factories (supply decided by optimizer),
- flexible replenishment,
- balancing initial loading and intermediate loading.

#### C1) SupplyFlexLoad (optimizer decides how much is supplied)
Use when:
- the optimizer must decide how much supply to create at a node.

This is the foundation for "production planning inside routing":
- factories produce goods only when needed (the two bakeries of the example below are not optional nodes: both are on the plan, and the optimizer sets how much Bread each one makes),
- the produced quantity is optimized to avoid underload without creating waste.

Example (Bakery Chain):
- [PNDBakeryChainFlexLoadExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/pickupanddelivery/PNDBakeryChainFlexLoadExample.java)

#### C2) RequestFlexLoad (optimizer decides how much is requested)
Use when:
- the node's request can be partially fulfilled or flexibly targeted by the optimizer,
- often used in combination with supply flexibility.

Example:
- [PNDRequestAndSupplyFlexLoadExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/pickupanddelivery/PNDRequestAndSupplyFlexLoadExample.java)

#### C3) MixedFlexLoad (optimizer may switch between request and supply behavior)
Use when:
- the load acts like a **reloading buffer / warehouse**, which can:
  - provide goods if the vehicle is underloaded, or
  - accept goods if the vehicle is overloaded.

This is particularly effective for:
- mixed pickup and delivery where initial load planning is hard,
- reloading nodes that can solve both underload and overload states.

Example:
- [PNDMixedFlexLoadExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/pickupanddelivery/PNDMixedFlexLoadExample.java)

#### C4) TimedSupplyFlexLoad (optimizer decides supply + delivery must meet an SLA)
Use when:
- supply decision and service promise are coupled, e.g.:
  - a chain decides which branch serves which customer,
  - but only if delivery is within a fixed SLA window.

The documentation's pizza scenario illustrates exactly this:
- restaurants decide which location prepares which pizza,
- and the optimizer enforces the delivery-within-time promise.

Example:
- [PNDTimedPizzaDeliveryExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/pickupanddelivery/PNDTimedPizzaDeliveryExample.java)

### D) UnloadAllLoad (unload everything of a load type)
Use when:
- it is operationally clear that a visit will unload a certain good type completely (e.g., landfill / dump),
- and you do not want the optimizer to spend time deciding "how much to unload".

This is a performance and modeling best practice:
- it reduces unnecessary search complexity.

Example:
- [PNDOptionalUnloadAllLoadExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/pickupanddelivery/PNDOptionalUnloadAllLoadExample.java)

## Capacity Factor (model different item sizes/weights)

Many PND problems are not "count-based".  
A pallet of cups and a piano do not consume the same cargo space.

JOpt supports this via **capacity factors**, allowing you to express:
- "one unit of load A consumes X capacity units"
- "one unit of load B consumes Y capacity units"

This is typically derived from a physical model:
- vehicle cargo space (length x width),
- effective floor-space consumption,
- stacking rules,
- weight limits (if modeled as a proxy via factors).

Reference: [Capacity Factor](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/pickup_and_delivery#capacity-factor)

Example:
- [PNDCapacityFactorExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/pickupanddelivery/PNDCapacityFactorExample.java)

## Total depot capacity shared by load capacities

A ResourceDepot can have a total capacity that is smaller than the sum of its LoadCapacities. Each LoadCapacity keeps its own maximum, and all of them share the total capacity of the depot.

This models a vehicle with a special compartment: one good may only use the compartment, another good may use the whole vehicle, including the compartment. In the example, a `SimpleResourceDepot` with a total capacity of 70 holds `DelicateFruit` (maximum 30, the compartment) and `GlassVials` (maximum 70).

Example:
- [PNDDifferentTotalCapacityUtilization.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/pickupanddelivery/PNDDifferentTotalCapacityUtilization.java) (document: [A depot capacity shared by two goods](PNDDifferentTotalCapacityUtilization.md))

## Optional nodes and "infrastructure" in PND

PND becomes significantly more powerful when you add infrastructure nodes that are not "customers", such as:
- dumps / landfills,
- intermediate warehouses,
- cross-docks,
- production sites,
- reloading points.

In JOpt, this is often modeled using **optional nodes**:
- the optimizer decides whether to visit them.

This is the foundation for patterns like:
- "visit a dump to avoid overload" (in two test runs of `PNDOptionalUnloadAllLoadExample`, the truck also unloads at a dump after its last pickup, before it drives home),
- "produce bread only if needed to satisfy supermarket demand."

Relevant examples:
- Unload points as optional nodes: [PNDOptionalUnloadAllLoadExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/pickupanddelivery/PNDOptionalUnloadAllLoadExample.java)
- Production sites (SupplyFlexLoad; the bakeries of the example are not optional nodes): [PNDBakeryChainFlexLoadExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/pickupanddelivery/PNDBakeryChainFlexLoadExample.java)

## Analyzing PND results (reports)

A PND solution is not only a set of routes.  
You also need to verify the **goods exchange timeline**:

- what was delivered to each node,
- what was picked up at each node,
- how the vehicle load evolved over time,
- whether any partial fulfillment happened,
- and whether any violations were avoided (or remain).

The official documentation describes:
- ResourceDepot report
- NodeDepot report

Reference: [Analyzing the Result of an Optimization-Run](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/pickup_and_delivery#analyzing-the-result-of-an-optimization-run)

Example that focuses on extracting and interpreting reports:
- [PNDReportExtractionExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/pickupanddelivery/PNDReportExtractionExample.java)

Recommended practice:
- treat depot reports as part of your operational audit trail,
- persist them alongside route results when PND is business-critical.

## Recommended modeling workflow (practical checklist)

1. **Define your goods taxonomy**
   - stable `loadId` strings (e.g., `"Bread"`, `"Waste"`, `"Pallet_1219x1016"`).
2. **Define resource depots**
   - total capacity and per-good capacities (LoadCapacities),
   - initial load levels.
3. **Define node depots**
   - per node: request/supply Loads, with fuzzy settings where appropriate.
4. **Choose the simplest adequate load types**
   - SimpleLoad for fixed quantities,
   - TimedLoad for SLA/precedence,
   - FlexLoads for production planning / dynamic quantities,
   - UnloadAllLoad for "dump everything" operations.
5. **Add infrastructure nodes as optional nodes**
   - dumps, factories, reload points, cross-docks.
6. **Run and analyze depot reports**
   - verify load timeline and fulfillment logic,
   - validate whether fuzzy behavior matches business expectations.
7. **Scale up**
   - add additional constraints (skills, zones, AutoFilter, etc.) only after the PND core is correct.

## Common pitfalls (and how to avoid them)

### Pitfall 1: Inconsistent load IDs
Symptom:
- requests are never satisfied, vehicles appear "unable" to carry goods.

Fix:
- enforce centralized ID constants/enums in your integration.

### Pitfall 2: Over-using FlexLoads
FlexLoads are powerful, but they expand the search space.

Recommendation:
- prefer SimpleLoad whenever quantity is truly fixed,
- use UnloadAllLoad when behavior is deterministic,
- reserve FlexLoads for cases where "the optimizer deciding the quantity" is truly a feature requirement.

### Pitfall 3: Missing infrastructure
Symptom:
- overload is unavoidable because there is no dump,
- underload is unavoidable because there is no replenishment.

Fix:
- add optional dump/reload/production nodes.

### Pitfall 4: Misinterpreting fuzzy
Fuzzy does not mean "always ignore violations".  
It means:
- partial exchange is allowed,
- and you must interpret the resulting partial fulfillment in your business process.

If your downstream system requires full fulfillment:
- set fuzzy to `false`.

## Closing note

PND is one of the most constraint-heavy and business-critical routing problem classes.  
For this reason, it is recommended to:
- start with a small instance and validate reports,
- establish strong naming conventions for goods IDs,
- and grow the model iteratively (constraints second, PND correctness first).

Additional context:
- [PND section in Special Features overview](https://www.dna-evolutions.com/docs/learn-and-explore/special/special_features#pickup-and-delivery-pnd-manufacturing-planning)
- [Basic elements (nodes/resources/time windows)](https://www.dna-evolutions.com/docs/getting-started/tutorials/basic-elements)

## Run the examples

Run the `main(String[] args)` method of a class from your IDE, with the repository imported as a Maven project. You need a JDK in the version that the examples pom sets. You need no license file: without one, the examples use the public evaluation license (see the [license guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/license)).

## Related

- [Pickup and delivery](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/pickup_and_delivery): the feature guide, with node depots and loads, resource depots and load capacities, the load types, capacity factors and the depot reports.
- [A depot capacity shared by two goods](PNDDifferentTotalCapacityUtilization.md): the example page of `PNDDifferentTotalCapacityUtilization`.
- [Manufacturing planning](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/manufacturing_planning): production quantities that the optimizer decides with flexible loads, as in `PNDBakeryChainFlexLoadExample`.
- [Optimization properties](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties): the property reference, with `JOptWeight.Capacity` and its default.
- [pickupanddelivery on GitHub](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/pickupanddelivery): the folder with the classes of this page.
- [Recommended ways to run an optimization](../../basic/recommendedimplementation_02/RecommendedImplementation.md): the recommended run pattern, a synchronous run and the legacy callback methods.
- [Advanced examples](../README.md): the overview of all Advanced examples.

---

Added: 2026-02-17 (JOpt core 7.5.3-j17) | Last updated: 2026-10-21
