# Pickup & Delivery (PND) in JOpt.TourOptimizer

This document explains how to model **Pickup & Delivery (PND)** problems with JOpt.TourOptimizer using the built-in PND module.  
PND in JOpt is not “just an extra cost term” — it is a **first-class feasibility system** that tracks goods in depots, checks capacity, and drives routing decisions accordingly.

Primary reference: [Pickup and delivery documentation](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/pickup_and_delivery)

---

## Why PND is different from classic CVRP/VRPTW

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

---

## Core building blocks

### 1) NodeDepot (what a Node requests or supplies)

A `Node` may carry a **NodeDepot**.  
Think of it as the node’s “warehouse interface”: it describes which goods are exchanged at this stop.

Key characteristics:
- A NodeDepot holds one or more **Loads**.
- Each Load describes a single good type, the direction (request vs supply), and the quantity.

Reference: [NodeDepot and Load](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/pickup_and_delivery#nodedepot-and-load)

### 2) ResourceDepot (what a Resource can carry and initially carries)

A `Resource` may carry a **ResourceDepot**.  
Think of it as the vehicle’s cargo model.

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
- If your loads are `"Bread"`, `"Fruit"`, `"Waste"`, your vehicle must declare capacities for these exact IDs (and your own “ID naming conventions” must be consistent across systems).

---

## Loads and exchange semantics (request vs supply)

A Load can represent either:

- **Request** (delivery): the node wants to receive goods from the resource.
- **Supply** (pickup): the node wants to give goods to the resource.

In addition, JOpt supports **fuzzy** exchanges:
- fuzzy = partial delivery/pickup is acceptable.
- non-fuzzy = the exchange must be complete.

This is a powerful real-world feature, because “partial fulfillment” is often allowed for some customers but not for others.

Reference: [At a glance](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/pickup_and_delivery#at-a-glance)

---

## Capacity violations and why they matter

In PND, an infeasible sequence is typically caused by one of these situations:

- **Underload**: the vehicle does not carry enough of the requested good to satisfy a request.
- **Overload**: the vehicle exceeds its available capacity when picking up supplies.

JOpt’s PND system tracks these states explicitly.  
Depending on your modeling (especially fuzzy logic and FlexLoads), the optimizer can:
- avoid the violation by rerouting, reloading, or visiting dump/unload nodes,
- allow partial fulfillment (fuzzy),
- or declare an infeasible violation (hard failure).

Reference: [Types of Capacity Violations](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/pickup_and_delivery#types-of-capacity-violations)

---

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
  - “pickup must be delivered within X minutes”.

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

This is the foundation for “production planning inside routing”:
- optional factories produce goods only when needed,
- the produced quantity is optimized to avoid underload without creating waste.

Example (Bakery Chain):
- [PNDBakeryChainFlexLoadExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/pickupanddelivery/PNDBakeryChainFlexLoadExample.java)

#### C2) RequestFlexLoad (optimizer decides how much is requested)
Use when:
- the node’s request can be partially fulfilled or flexibly targeted by the optimizer,
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

The documentation’s pizza scenario illustrates exactly this:
- restaurants decide which location prepares which pizza,
- and the optimizer enforces the delivery-within-time promise.

Example:
- [PNDTimedPizzaDeliveryExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/pickupanddelivery/PNDTimedPizzaDeliveryExample.java)

### D) UnloadAllLoad (unload everything of a load type)
Use when:
- it is operationally clear that a visit will unload a certain good type completely (e.g., landfill / dump),
- and you do not want the optimizer to spend time deciding “how much to unload”.

This is a performance and modeling best practice:
- it reduces unnecessary search complexity.

Example:
- [PNDOptionalUnloadAllLoadExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/pickupanddelivery/PNDOptionalUnloadAllLoadExample.java)

---

## Capacity Factor (model different item sizes/weights)

Many PND problems are not “count-based”.  
A pallet of cups and a piano do not consume the same cargo space.

JOpt supports this via **capacity factors**, allowing you to express:
- “one unit of load A consumes X capacity units”
- “one unit of load B consumes Y capacity units”

This is typically derived from a physical model:
- vehicle cargo space (length × width),
- effective floor-space consumption,
- stacking rules,
- weight limits (if modeled as a proxy via factors).

Reference: [Capacity Factor](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/pickup_and_delivery#capacity-factor)

Example:
- [PNDCapacityFactorExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/pickupanddelivery/PNDCapacityFactorExample.java)

---

## Optional nodes and “infrastructure” in PND

PND becomes significantly more powerful when you add infrastructure nodes that are not “customers”, such as:
- dumps / landfills,
- intermediate warehouses,
- cross-docks,
- production sites,
- reloading points.

In JOpt, this is often modeled using **optional nodes**:
- the optimizer decides whether to visit them.

This is the foundation for patterns like:
- “visit a dump only if needed to avoid overload,”
- “produce bread only if needed to satisfy supermarket demand.”

Relevant examples:
- Unload points as optional nodes: [PNDOptionalUnloadAllLoadExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/pickupanddelivery/PNDOptionalUnloadAllLoadExample.java)
- Production sites (SupplyFlexLoad): [PNDBakeryChainFlexLoadExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/pickupanddelivery/PNDBakeryChainFlexLoadExample.java)

---

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

---

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
   - UnloadAllLoad for “dump everything” operations.
5. **Add infrastructure nodes as optional nodes**
   - dumps, factories, reload points, cross-docks.
6. **Run and analyze depot reports**
   - verify load timeline and fulfillment logic,
   - validate whether fuzzy behavior matches business expectations.
7. **Scale up**
   - add additional constraints (skills, zones, AutoFilter, etc.) only after the PND core is correct.

---

## Common pitfalls (and how to avoid them)

### Pitfall 1: Inconsistent load IDs
Symptom:
- requests are never satisfied, vehicles appear “unable” to carry goods.

Fix:
- enforce centralized ID constants/enums in your integration.

### Pitfall 2: Over-using FlexLoads
FlexLoads are powerful, but they expand the search space.

Recommendation:
- prefer SimpleLoad whenever quantity is truly fixed,
- use UnloadAllLoad when behavior is deterministic,
- reserve FlexLoads for cases where “the optimizer deciding the quantity” is truly a feature requirement.

### Pitfall 3: Missing infrastructure
Symptom:
- overload is unavoidable because there is no dump,
- underload is unavoidable because there is no replenishment.

Fix:
- add optional dump/reload/production nodes.

### Pitfall 4: Misinterpreting fuzzy
Fuzzy does not mean “always ignore violations”.  
It means:
- partial exchange is allowed,
- and you must interpret the resulting partial fulfillment in your business process.

If your downstream system requires full fulfillment:
- set fuzzy to `false`.

---

## Example index (quick navigation)

PND example folder:
- [src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/pickupanddelivery on GitHub](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/pickupanddelivery)

| Example | Focus |
|---|---|
| [PNDSimpleExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/pickupanddelivery/PNDSimpleExample.java) | Simple PND setup |
| [PNDSimpleFuzzyExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/pickupanddelivery/PNDSimpleFuzzyExample.java) | Fuzzy acceptance |
| [PNDTimedLoadExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/pickupanddelivery/PNDTimedLoadExample.java) | TimedLoad (pickup→delivery time limit) |
| [PNDTimedPizzaDeliveryExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/pickupanddelivery/PNDTimedPizzaDeliveryExample.java) | TimedSupplyFlexLoad (SLA + production planning) |
| [PNDMixedFlexLoadExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/pickupanddelivery/PNDMixedFlexLoadExample.java) | MixedFlexLoad (dynamic request/supply) |
| [PNDRequestAndSupplyFlexLoadExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/pickupanddelivery/PNDRequestAndSupplyFlexLoadExample.java) | RequestFlexLoad + SupplyFlexLoad |
| [PNDOptionalUnloadAllLoadExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/pickupanddelivery/PNDOptionalUnloadAllLoadExample.java) | Optional UnloadAllLoad dumps |
| [PNDCapacityFactorExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/pickupanddelivery/PNDCapacityFactorExample.java) | Capacity factors |
| [PNDReportExtractionExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/pickupanddelivery/PNDReportExtractionExample.java) | Report extraction and analysis |
| [PNDBakeryChainFlexLoadExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/pickupanddelivery/PNDBakeryChainFlexLoadExample.java) | Bakery production planning |

---

## Closing note

PND is one of the most constraint-heavy and business-critical routing problem classes.  
For this reason, it is recommended to:
- start with a small instance and validate reports,
- establish strong naming conventions for goods IDs,
- and grow the model iteratively (constraints second, PND correctness first).

Additional context:
- [PND section in Special Features overview](https://www.dna-evolutions.com/docs/learn-and-explore/special/special_features#pickup-and-delivery-pnd-manufacturing-planning)
- [Basic elements (nodes/resources/time windows)](https://www.dna-evolutions.com/docs/getting-started/tutorials/basic-elements)
