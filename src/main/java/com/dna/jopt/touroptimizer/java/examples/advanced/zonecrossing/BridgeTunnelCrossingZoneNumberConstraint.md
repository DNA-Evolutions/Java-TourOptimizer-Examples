# Bridge & Tunnel Crossing Penalization with ZoneNumbers

This example demonstrates how to **discourage unnecessary crossings between zones** using JOpt’s **Zone Crossing Penalization**.

It is designed for real-world scenarios where a boundary crossing has a meaningful overhead, for example:
- toll bridges,
- tunnels,
- congestion zones,
- security checkpoints,
- ports / yards / restricted regions,
- “river crossing” city layouts where switching sides frequently is inefficient.

Instead of trying to approximate this behavior by manually tweaking distance matrices, JOpt provides an **architectural feature**:
- keep normal distance/time costs,
- and add an extra penalty when a route crosses from one zone into another.

---

## References

- Zone Crossing Penalization (concept and properties):  
  https://docs.dna-evolutions.com/overview_docs/zonecrossing/zonecrossing.html

- Defining territories via ZoneCodes (foundation concept used by this feature):  
  https://docs.dna-evolutions.com/overview_docs/special_features/Special_Features.html#defining-territories-via-zonecodes

- Example source:  
  [BridgeTunnelCrossingZoneNumberConstraintExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/zonecrossing/BridgeTunnelCrossingZoneNumberConstraintExample.java)  
  [BridgeTunnelCrossingZoneNumberConstraintExample.java (raw)](https://raw.githubusercontent.com/DNA-Evolutions/Java-TourOptimizer-Examples/refs/heads/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/zonecrossing/BridgeTunnelCrossingZoneNumberConstraintExample.java)

---

## What problem does this solve?

By default, the optimizer will select the best solution with respect to:
- travel distance/time,
- visit/service times,
- and any active constraints.

In dense areas split by a river, tunnel, or toll border, the “best” distance solution can still look unrealistic:
- a route zig-zags across zones multiple times, because each local step looks cheap,
- but the boundary crossings are operationally expensive.

Zone crossing penalization addresses this by making “crossing the border” itself expensive.

The intended behavior is:
- routes mostly stay inside one zone,
- crossing happens only when it truly improves the global solution (typically once early and once late in the shift).

---

## Core mechanism (how penalization works)

When enabled, JOpt applies an additional cost whenever an edge in the tour goes from **Zone A → Zone B**.

There are two layers:

### 1) Global multiplier (optimizer property)

The example sets:
- `JOpt.Clustering.PenlalizeZoneCodeCrossing = true/false` (toggle)
- `JOpt.Clustering.PenlalizeZoneCodeCrossingMultiplier = 10.0`

Interpretation:
- a zone-crossing edge becomes significantly more expensive than an intra-zone edge,
- which nudges the optimizer away from frequent crossings.

### 2) Direction-specific multipliers (ZoneConnection)

For more control, the example also configures **directional zone connections**:

- Zone `1` → Zone `3` has an additional multiplier of `5.0`
- Zone `3` → Zone `1` has an additional multiplier of `0.0`

This illustrates an important modeling capability:
- you can penalize crossing **asymmetrically**, for example:
  - “going into the city is expensive (toll), leaving the city is cheap”, or
  - “crossing east→west is congested in the morning, west→east is congested in the evening”.

In the code, this is configured via the zone manager:

- `opti.getNodeConnector().getZoneManager().putZoneConnection(...)`

---

## Zone modeling in this example

This example uses **ZoneNumber** (integer ZoneCodes).

### Zone sets (two “macro areas” with two codes each)
To demonstrate multi-zone membership, the nodes are assigned as:

- Manhattan-like area:
  - Zone 1 (primary)
  - Zone 2 (extra code on the same qualification)

- Jersey City-like area:
  - Zone 3 (primary)
  - Zone 4 (extra code on the same qualification)

This is done using:
- `ZoneNumberQualification(zoneOne)` plus `addExtraCode(zoneTwo)` (and analogous for Zone 3/4)

Practical note:
- Multi-zone membership is the recommended way to model border areas or “shared service areas”.
- A job on a boundary can carry multiple zones, allowing either side to serve it without “hard polygon edges”.

---

## Resource setup (intentional modeling choice)

In this scenario, resources are **not** restricted to a particular zone.

The point of the example is not “territory enforcement”, but “crossing discouragement”.

This is aligned with the documentation behavior:
- if a resource has no explicit zone restriction, it can visit all zones,
- but crossings will be penalized if the feature is enabled.

---

## How to validate the effect (what to look for)

### 1) Toggle penalization on/off
The example defines:
- `boolean doPenalizeZoneCrossings = true; // Modify me`

Run twice:
- once with `true`,
- once with `false`.

You should observe:
- `false`: routes can switch zones frequently (distance-optimal but unrealistic)
- `true`: routes tend to cluster by zone and reduce crossings

### 2) Visual inspection via KML export
The example exports the solution to KML:

- `BridgeCrossingZoneNumberConstraintExample-<true|false>.kml`

Open the KML in Google Earth (or another KML viewer) and compare:
- how often the route crosses the implicit zone boundary,
- how “clean” the tours stay inside each zone.

This is the fastest practical way to communicate the feature to end users and stakeholders.

---

## Recommended modeling patterns

### Pattern A — Penalize only specific borders
Use ZoneConnection entries for:
- bridge/tunnel edges,
- controlled border crossings,
- congestion-zone boundaries.

Leave other zone-to-zone transitions unpenalized.

### Pattern B — Use asymmetry intentionally
Directional penalties allow you to model:
- toll directionality,
- “morning inbound / evening outbound” congestion,
- one-way physical constraints.

### Pattern C — Combine with hard territories when required
If territories are mandatory:
- enforce via ZoneCode constraints on WorkingHours (hard),
- and still apply zone crossing penalties for “allowed but undesirable” transitions between adjacent territories.

This provides a clean separation:
- feasibility (hard territory rules),
- realism/efficiency (crossing penalization).

---

## Summary

- Zone Crossing Penalization adds an extra cost when traveling from one zone to another.
- It is enabled via optimizer properties and can be refined with per-direction `ZoneConnection` multipliers.
- This example models two macro areas (Manhattan vs Jersey City) using ZoneNumbers and demonstrates asymmetric crossing costs.
- KML export makes it straightforward to verify that penalization reduces unnecessary “zig-zag” tours across bridges/tunnels.
