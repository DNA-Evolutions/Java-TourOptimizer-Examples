# External Node Connection — Custom Distances and Driving Times

This document explains how to integrate **external distance/time information** into **JOpt TourOptimizer (Java)** by supplying explicit **element-to-element connections** (“edge data”) via a `NodeEdgeConnector`.

It is based on the example implementations:

- [ExternalNodeConnectionExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic/connection_04/ExternalNodeConnectionExample.java)
- [ExternalNodeConnectionWithLocationIdExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic/connection_04/ExternalNodeConnectionWithLocationIdExample.java)

…and it connects the examples to the official documentation sections:

- **Basic Elements** (especially “Element connections”):  
  https://www.dna-evolutions.com/docs/getting-started/tutorials/basic-elements
- **BackupConnector** (how default distances/times are calculated and how to modify them):  
  https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/backupconnector#description-how-jopttouroptimizer-calculates-distances-and-times

---

## Why this matters

For many problems, JOpt’s built-in “auto connection” behavior is good enough: you provide nodes/resources with geo-coordinates and time windows and the optimizer can infer missing connections. However, in real routing use-cases you often need:

- **Road network** distances instead of straight-line distances
- Travel times depending on **traffic**, **vehicle type**, **terrain**, or **service policies**
- A precomputed matrix coming from:
  - OSRM / Valhalla / GraphHopper,
  - Google Distance Matrix,
  - internal GIS systems,
  - company-specific routing engines.

In those cases, you can inject **external edge data** by defining explicit connections for selected (or all) pairs of elements.

---

## Concept: “connections” are edges between optimization elements

A `NodeEdgeConnectorItem` (via `INodeConnectorItem`) represents:

- a directed edge from **element A → element B**
- **distance** (unit-aware quantity)
- **driving time** (`Duration`)

Important: connections are generally **directional** (A→B is not assumed to equal B→A). The Basic Elements tutorial explicitly demonstrates creating both directions.  
Practical implication: if you want symmetry, you must provide both edges yourself.

---

## Two layers of behavior

When you set a node connector, the optimizer uses a layered strategy:

### Layer 1 — Explicit connections you provide
If a connection for a pair exists in your `NodeEdgeConnector`, it is used as-is.

This is how you “induce” external travel times/distances.

### Layer 2 — Fallback for missing connections
If a connection is **missing**, the optimizer can auto-calculate it.

By default (per docs), the fallback distance uses a flat-surface earth approximation (projected model), and driving time is derived using an average speed; the BackupConnector documentation explains both the default model and how to override it.

This is a powerful design: you can provide a **partial matrix** (only where you have high-quality external data or need special handling) and rely on fallback for everything else.

---

## Pattern 1 — Partial external matrix (ExternalNodeConnectionExample)

**File:** `ExternalNodeConnectionExample.java`  
**Intent (from `toString()`):** “Setting custom node driving times and distances by using a node connector.”

### What it does

1. Builds nodes/resources as usual.
2. Creates **two explicit connections** between Cologne and Oberhausen:
   - Koeln → Oberhausen
   - Oberhausen → Koeln
3. Adds only those edges to a `NodeEdgeConnector`.
4. Assigns the connector to the optimization:
   - `myNodeConnector.putNodeConnections(nodeConnectionItems);`
   - `this.setNodeConnector(myNodeConnector);`
5. Starts a run.

Everything else (all other pairs) is computed via fallback behavior.

### Why this is a good “starter” pattern

It is the cleanest mental model:

- “Here are the edges I care about or got from an external system.”
- “For all other edges, do something reasonable.”

### Typical real-world usage

- Provide external edges for:
  - depot ↔ customer,
  - high-priority customers,
  - cross-city edges (where straight-line is especially misleading),
  - known restricted segments.
- Leave the rest to fallback to reduce integration complexity.

---

## Pattern 2 — Location IDs (ExternalNodeConnectionWithLocationIdExample)

**File:** `ExternalNodeConnectionWithLocationIdExample.java`  
**Intent (from `toString()`):** “Setting custom node driving times and distances by using a node connector and location ids.”

### What “locationId” changes

This example assigns the **same `locationId`** to multiple nodes:

- `koeln.setLocationId("TestLocationId1");`
- `koeln1.setLocationId("TestLocationId1");`
- `oberhausen.setLocationId("TestLocationId1");`
- …and so on

The file comment explains the key semantics:

- Elements sharing a `locationId` effectively share the same position.
- You must ensure that all elements sharing a `locationId` also share the same geo-coordinates—this becomes important when fallback calculations rely on geo coordinates.

### Why you would use location IDs

Location IDs are a practical tool when your “logical nodes” are not all distinct physical points, for example:

- multiple tasks at the same customer site,
- multiple deliveries at one facility,
- clustered tasks that should share travel-time semantics.

In addition, location IDs can reduce the number of distinct “places” your external matrix must cover (depending on how you structure your integration), because multiple nodes map to one physical location identity.

### When to be careful

If you assign the same `locationId` to nodes that do **not** share the same latitude/longitude, you create ambiguity:

- explicit connections might be applied unexpectedly,
- fallback distances might be computed from an inconsistent geo position.

The example explicitly warns about this.

---

## How fallback distances and times work (and why BackupConnector matters)

When you do **not** supply all connections, fallback kicks in. The BackupConnector documentation describes:

- Default distance calculation: a flat-surface earth approximation (efficient; usually accurate enough for short edges in optimized tours).
- Default driving time: derived from an assumed average speed (the docs mention a default average speed and show how to set a per-resource average speed).
- How to override/adjust the calculation by providing a custom `BackupElementConnector` implementation.

Practical recommendation:

- If you only provide a partial external matrix, consider whether fallback should be:
  1) “good enough” straight-line + average speed, or
  2) replaced with a custom BackupConnector (e.g., correction factor, haversine, custom terrain model).

---

## A recommended implementation strategy (production-grade)

### Step 1 — Decide your external data scope
Choose one:

- **Full matrix** (best accuracy; highest integration cost).
- **Partial matrix** + fallback (excellent ROI for many scenarios).
- **No matrix** but custom BackupConnector (low integration; better than default).

### Step 2 — Generate directed edges
Even if your external system returns symmetric values, explicitly create both directions unless you are certain JOpt is configured to interpret them otherwise.

### Step 3 — Use stable identifiers
If you use `locationId`, ensure it is:
- stable,
- consistent across runs,
- consistent with your geo-coordinate assignment.

### Step 4 — Validate and harden
Before running optimization:
- Verify every external edge has:
  - non-negative distance,
  - non-negative driving time,
  - correct units,
  - correct “from/to” direction.

### Step 5 — Observe and test
Attach observables (progress/status/errors) early during development so you can immediately see:
- if the connector is applied,
- if a missing connection forces fallback,
- if infeasible time windows emerge due to travel-time inflation.

---

## Practical “interesting” thought experiments (to validate your integration)

1. **Asymmetry test**  
   Make A→B short and B→A long; verify the resulting tour order changes accordingly.

2. **Fallback boundary test**  
   Provide external edges for only 10% of pairs. Compare results with:
   - default fallback,
   - a custom BackupConnector (e.g., scaled distances),
   to measure sensitivity.

3. **LocationId consolidation test**  
   Create multiple nodes at one physical location (same `locationId`) and observe whether:
   - the run becomes faster (smaller effective geography),
   - the solution becomes more “grouped” around those clustered tasks.

---

## Summary

- Use `NodeEdgeConnectorItem` to inject external **distance** and **driving time** data.
- Treat connections as **directed** (define both directions when needed).
- Missing connections fall back to **auto-calculated** values; this fallback can be customized via **BackupConnector**.
- `locationId` is a powerful concept to represent multiple tasks at the same physical position—use it carefully and consistently.

