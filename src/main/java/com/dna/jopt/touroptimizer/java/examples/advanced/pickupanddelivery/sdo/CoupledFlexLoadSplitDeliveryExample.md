# Coupled Split Delivery — `CoupledFlexLoadSplitDeliveryExample` - SDO-Opimization

> **Experimental feature.** `CoupledRequestFlexLoad` is an experimental capability of JOpt.TourOptimizer. Behavior, API, and defaults may change in future releases without prior notice.

---

## What this example shows

This example demonstrates how JOpt handles **split delivery** using `CoupledRequestFlexLoad`. A split delivery means that a single customer order is fulfilled by more than one truck visit — each truck delivers a portion, and the sum of all portions equals the total ordered quantity.

Two scenarios appear side by side:

| Group | Location | Total demand | Split? |
|---|---|---|---|
| Cologne | Cologne city centre | 30 pallets | **Mandatory** — 30 > truck capacity (20) |
| Leverkusen | Leverkusen | 10 pallets | Optional — 10 fits in one truck |
| Dortmund | Dortmund city centre | 20 pallets | Optional — exactly fills one truck |

The Cologne order **cannot** be fulfilled by a single truck. The optimizer is forced to split it across two trucks. The Leverkusen and Dortmund orders fit on a single truck, so the optimizer consolidates them to one stop each.


## References

- Example source: [CoupledFlexLoadSplitDeliveryExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/pickupanddelivery/sdo/CoupledFlexLoadSplitDeliveryExample.java)


---

## Setup

### Customer groups

Each group consists of **two split-nodes** (S0 and S1) placed at the same geographic location. A `CoupledRequestFlexLoad` ties the two nodes together. The optimizer may freely redistribute the total demand between S0 and S1, subject to one constraint:

```
S0 + S1 = total demand  (coupling invariant)
```

Any split-node assigned zero pallets is automatically suppressed from routing via `isIgnoreOnZeroLoad = true`, so it does not appear as an unnecessary stop.

### Supply nodes

Four `SupplyFlexLoad` nodes are placed in Aachen, each starting with 20 pallets. Trucks start empty and must reload at a supply node before serving customers. Once a supply node's load reaches zero it disappears from routing (`isIgnoreOnZeroLoad = true`).

### Resources

Two trucks, both based in Aachen (same coordinates as the supply nodes). Each truck carries a maximum of 20 pallets.

---

## Why `isFuzzyVisit = true` is required

`CoupledRequestFlexLoad` is created with `isFuzzyVisit = true`:

```java
new CoupledRequestFlexLoad("Pallets", requestPerSplit, /* isFuzzyVisit */ true);
```

Without fuzzy visits the PND evaluator locks the desired load exchange amount at the value it sees during route assessment. This prevents `FlexLoadOperator` from moving load freely between the two split-nodes during the optimization search, and the optimizer gets stuck at the initial distribution (e.g. always 15 + 15) rather than finding the cost-optimal ratio (e.g. 20 + 10 for Cologne).

With fuzzy visits enabled, the truck may deliver any amount up to its available capacity at each stop. This is the mechanism that makes dynamic load redistribution work correctly.

---

## How the coupling map works

The `attachCoupledLoad` helper builds the coupling map for each node. The map tells a node's load object which depot IDs belong to its sibling nodes in the same group. `FlexLoadOperator` uses this map to maintain the invariant `S0 + S1 = coupledTotal` while exploring load redistributions:

```java
// Full node-id -> depot-id map for the group
Map<String, List<String>> allDepots = group.stream()
    .collect(Collectors.toMap(INode::getId, n -> List.of(depotLabel.apply(n))));

// Each node's coupling map = all entries except its own
Map<String, List<String>> siblingDepots = new HashMap<>(allDepots);
siblingDepots.entrySet().removeIf(e -> e.getValue().contains(ownDepotId));
```

Both split-nodes must use the same load ID (`"Pallets"` here), and the resource depot must declare a capacity for that same ID.

---

## Result

```
Truck_Aachen_0:
  Supply_Aachen_0  →  load 20 pallets
  Cologne_S0       →  deliver 20 pallets   (Cologne split, part 1)
  Supply_Aachen_2  →  reload 20 pallets
  Dortmund_S0      →  deliver 20 pallets   (Dortmund, consolidated)

Truck_Aachen_1:
  Supply_Aachen_1  →  load 20 pallets
  Cologne_S1       →  deliver 10 pallets   (Cologne split, part 2)
  Leverkusen_S1    →  deliver 10 pallets   (Leverkusen, consolidated)
```

**Cologne:** split across two trucks — 20 + 10 = 30. The mandatory split is correctly enforced: no single truck visit exceeds 20 pallets.

**Leverkusen:** consolidated to S1 (10 pallets). S0 is suppressed.

**Dortmund:** consolidated to S0 (20 pallets). S1 is suppressed.

Two of the four supply nodes (`Supply_Aachen_0` and `Supply_Aachen_2` for truck 0; `Supply_Aachen_1` for truck 1) are used. `Supply_Aachen_3` is unused and suppressed.

---

## Key parameters

| Parameter | Value | Notes |
|---|---|---|
| `MAX_TRUCK_CAPACITY` | 20 | Cologne total (30) exceeds this — split is mandatory |
| `INITIAL_SUPPLY_LOAD` | 20 | Starting value per supply node; optimizer adjusts freely |
| `isFuzzyVisit` | `true` | Required for effective load redistribution |
| `isIgnoreOnZeroLoad` | `true` | On all split-nodes and supply nodes |
| `JOptWeight.Capacity` | 10 | See note below |
| SA iterations | 500,000 | Pre-optimization phase |
| GE generations | 20,000 | Genetic evolution phase |

---

## Choosing `JOptWeight.Capacity`

The capacity penalty weight controls how strongly the optimizer penalises constraint violations during the search. For split delivery problems, this setting has a significant effect on solution quality and requires careful tuning.

**Why not simply set it as high as possible?**

The optimizer explores the solution space by temporarily allowing violations and then repairing them. A very high capacity weight makes any route containing a violation so expensive that the optimizer refuses to enter that region of the search space at all — even when passing through it would lead to a better feasible solution. The optimizer effectively becomes quasi-static: it cannot move nodes around freely, gets trapped in local optima, and converges slowly or not at all.

**The problem-size effect**

This is especially pronounced in small problems. With a large number of nodes and routes, many candidate moves exist that do not touch the violated nodes, so the optimizer can still explore freely. With only a handful of nodes, as in this example, nearly every candidate move involves the nodes that carry a coupling constraint. A high penalty then freezes the search almost entirely.

In large-scale problems with hundreds of nodes the optimizer behaves more like a continuum: the search landscape is broad, violations are diluted across many routes, and a higher weight is tolerable. In small problems the landscape is sparse and the weight must be kept low enough to allow the optimizer to move through temporary violations on its way to a better solution.

**Practical guidance**

A value of `10` works well for this example. As a starting point for your own problems: begin low (around `10`) and increase gradually while monitoring whether the coupling invariant is reliably satisfied in the final result. If violations remain in the output despite a long run, increase the weight. If the optimizer converges too slowly or gets stuck, lower it.

---

## Related examples

- `PNDBakeryChainFlexLoadExample` — SupplyFlexLoad with a chain of bakery supply stops.
- `PNDRequestAndSupplyFlexLoadExample` — basic RequestFlexLoad and SupplyFlexLoad setup.
- `CoupledFlexLoadMandatorySplitExample` — large-scale benchmark with 250 customer groups and mandatory splits.
