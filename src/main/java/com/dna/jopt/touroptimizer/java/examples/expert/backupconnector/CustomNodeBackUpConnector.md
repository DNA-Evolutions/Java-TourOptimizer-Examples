# Custom Node Backup Connector — Custom Distance/Time Calculation for Missing Connections

JOpt.TourOptimizer can operate with *externally provided* connections (distance + driving time) **or** it can auto-generate connections when they are missing.  
The mechanism responsible for filling missing `NodeEdgeConnectorItem`s is the **Backup Connector**.

A custom Backup Connector is the right tool when you need to:

- run without external routing data (or only partial routing data),
- apply a systematic correction factor (terrain, detours, traffic proxies),
- switch the underlying distance model (flat-earth vs haversine),
- implement custom business logic per edge (e.g., “crossing the river adds 10 minutes”),
- make travel time depend on the resource (different average speeds).

Official documentation:
- [Backup Connector](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/backupconnector)

---

## References (examples)

- [CustomNodeBackUpConnectorExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/backupconnector/CustomNodeBackUpConnectorExample.java)  
- [CustomNodeBackUpConnectorHaversineExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/backupconnector/CustomNodeBackUpConnectorHaversineExample.java)

---

## When the Backup Connector is used

### A) You provide a full matrix of connections
If you provide distances and driving times for all required element pairs, the Backup Connector will rarely be used.

This is typical when you integrate a routing engine:
- OSRM / GraphHopper / commercial APIs / internal road-network services.

### B) You provide **no** connections or only a partial set
If some connections are missing, the optimizer asks the Backup Connector to compute them on demand.

This is typical when:
- you prototype quickly,
- you only have raw lat/lon,
- you intentionally do not compute a full matrix (performance),
- you want a fallback if your routing service does not return a result for some pairs.

Related background (element connections):
- [Basic Elements — Element connections](https://www.dna-evolutions.com/docs/getting-started/tutorials/basic-elements)

---

## Default behavior (what gets computed)
The default concept is:

1. Compute a distance between two coordinates using a geometric formula (flat-surface projection or haversine).
2. Convert distance → driving time using the resource’s average speed.

The documentation provides the background and tradeoffs:
- flat-surface is fast and sufficiently accurate for typical “short hops” in optimized tours,
- haversine is more precise for longer distances but usually not needed for city-scale neighbor edges.  
  See: [Backup Connector](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/backupconnector)

---

## The implementation pattern (what you actually do)

### Step 1 — Assign a custom backup connector to the optimizer’s node connector

In `CustomNodeBackUpConnectorExample`:

```java
INodeEdgeConnector connector = new NodeEdgeConnector();
connector.setBackupElementConnector(new MyBackupElementConnector(false));
this.setNodeConnector(connector);
```

Conceptually:
- The `NodeEdgeConnector` is responsible for producing/holding element connections.
- The Backup Connector is the fallback used when a required connection is missing.

### Step 2 — Extend an existing default backup connector class
Both examples implement:

- `MyBackupElementConnector extends DefaultFlatEarthAverageSpeedBackupElementConnector`

This gives you:
- a ready-made baseline implementation,
- the ability to override only what you need.

### Step 3 — Override the two key methods

1) **Distance** (coordinates → meters):
- `getElement2ElementDistance(...)`

2) **Duration** (meters + resource → travel time):
- `getElement2ElementDuration(...)`

That is the “contract”:
- distance is a function of coordinates,
- duration is a function of distance *and* the visiting resource (to support different speeds).

---

## The constructor flag: `doRecalculateElement2ElementDuration`

The default base class constructor takes:

- `doRecalculateElement2ElementDuration`

Meaning:

### `false` — duration can be reused across resources
Use this when:
- all resources have the same average speed, or
- you intentionally want a uniform travel-time model (resource-independent).

Result:
- once duration is computed for an edge, it does not need to be recomputed per resource.

### `true` — duration must be recomputed per resource
Use this when:
- resources have different `avgSpeed`, and travel time must reflect that.

Result:
- the optimizer will re-evaluate duration for the edge whenever the visiting resource changes.

This is a key scaling decision:
- resource-dependent durations are more realistic,
- but they also increase the amount of duration computation.

---

## Example 1 — Flat-earth distance + terrain correction factor (1.2x)

**CustomNodeBackUpConnectorExample** uses:

- `NodeEdgeConnector.distancePlacePlaceFlatEarth(...)`
- multiplied by `1.2`

Interpretation:
- “straight line distance is too optimistic; roads are twisty / terrain is difficult”
- every missing connection becomes 20% longer than the default geometric estimate.

This is a very common and pragmatic approach when you do not have a routing engine but still want to:
- avoid underestimating travel distance and time,
- prevent overly optimistic schedules.

The example also prints the edge IDs when the connector is used, which is a good debugging technique to verify:
- which connections were auto-generated,
- and whether you are accidentally missing a large portion of your matrix.

---

## Example 2 — Haversine distance + correction factor (1.2x)

**CustomNodeBackUpConnectorHaversineExample** demonstrates how to swap the distance model:
- it uses a haversine implementation (`haversineDistanceMeter(...)`) and applies the same correction factor.

Use this approach when:
- your nodes can be far apart (regional routing),
- or you have a use case where great-circle precision matters.

For most routing problems where consecutively visited nodes are near neighbors, the practical difference is small—this is also discussed in the documentation:
- [Backup Connector](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/backupconnector)

---

## Travel time calculation and `avgSpeed`

Both examples compute driving time using the resource’s `avgSpeed`:

```java
long travelTimeMillis = (long) (distanceMeter / visitor.getAvgSpeed() * 1000L);
```

This ties travel time to the resource configuration.

To change average speed per resource, configure it when creating the resource (see documentation for details):
- [Backup Connector](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/backupconnector)
- [Basic Elements](https://www.dna-evolutions.com/docs/getting-started/tutorials/basic-elements)

Practical modeling notes:
- If you use `doRecalculateElement2ElementDuration = true`, `avgSpeed` differences become effective.
- If you keep it `false`, speed differences will not be reflected in travel times (unless you implement your own logic).

---

## Recommended production practices

### 1) Use a routing engine for “final-mile accuracy” and the Backup Connector as fallback
A robust architecture is:
- primary connections from an external routing service,
- backup connector for missing pairs or error cases.

This prevents failures when:
- the routing service cannot compute a route for a pair,
- network calls time out,
- a node is outside the supported region.

### 2) Add caching if you compute many edges repeatedly
If your custom connector computes expensive logic (API calls, terrain checks):
- cache by (fromId, toId, resourceId) depending on whether duration is resource-dependent.

### 3) Keep the model internally consistent
If you apply correction factors, apply them systematically:
- do not scale distance but forget to scale duration (or vice versa),
- unless that mismatch is intentionally part of your cost model.

### 4) Validate with a small instance first
Before scaling up, run a tiny instance and log:
- which edges were generated by the backup connector,
- whether the computed values align with expectations.

---

## Summary

- The Backup Connector fills in missing `NodeEdgeConnectorItem` connections.
- A custom connector is ideal for custom distance/time models, correction factors, and fallback behavior.
- Implement it by extending `DefaultFlatEarthAverageSpeedBackupElementConnector` and overriding:
  - `getElement2ElementDistance(...)`
  - `getElement2ElementDuration(...)`
- Use `doRecalculateElement2ElementDuration` to control whether duration depends on the visiting resource.
- The included examples demonstrate:
  - flat-earth distance with a 1.2x correction factor,
  - haversine distance with the same correction factor.
