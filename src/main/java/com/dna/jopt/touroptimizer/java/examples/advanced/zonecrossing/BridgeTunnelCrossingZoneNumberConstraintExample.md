# Zone Crossing Penalization Example

This example demonstrates how to use the **Zone Crossing Penalization** feature in **JOpt TourOptimizer** to discourage unnecessary zone transitions during routing—especially useful in cases where zone transitions represent real-world overhead (e.g., crossing bridges, tunnels, or entering restricted regions).

---

## Scenario: Bridge & Tunnel Crossings

In this example, we simulate routing across two zones representing **Manhattan** and **Jersey City**, where crossing between them should be minimized due to potential costs (e.g., tolls, traffic delays).

We use **ZoneNumberQualification** to assign nodes to zones and optionally penalize crossing between them using the zone connection configuration and global optimizer settings.

---

## Key Concepts

### ZoneNumbers
Zone numbers define **logical geographic partitions** (e.g., boroughs, districts). Each node can belong to one or more zones using a `ZoneNumberQualification`.

### Zone Crossing Penalty
When a resource crosses from one zone to another, an **optional cost penalty** can be applied. This encourages the optimizer to stay within zones where possible.

---

## Example Overview

| Feature                   | Implementation |
|---------------------------|----------------|
| Define zones              | `ZoneNumber`, `ZoneNumberQualification` |
| Assign zones to nodes     | `addManhattanNodes`, `addJerseyCityNodes` |
| Penalize crossings        | `ZoneConnection` + optimizer properties |
| Export result as KML      | `EntityKMLExporter` |
| Visual output             | `.kml` file for viewing in Google Earth |

---

## Optimizer Setup

### Enable Zone Crossing Penalization

```java
props.setProperty("JOpt.Clustering.PenlalizeZoneCodeCrossing", "true");
props.setProperty("JOpt.Clustering.PenlalizeZoneCodeCrossingMultiplier", "10.0");
```

### Define Zone Connections

```java
ZoneConnection.builder()
    .fromZoneId("1")      // Manhattan
    .toZoneId("3")        // Jersey City
    .crossingPenaltyMultiplier(5.0)  // Penalize this direction
    .build();
```

---

## Map & Data Setup

### Manhattan Nodes (Zone 1 + 2)

- 8 locations throughout Manhattan
- Assigned `ZoneNumberQualification` with Zone 1 and 2

### Jersey City Nodes (Zone 3 + 4)

- 8 locations throughout Jersey City
- Assigned `ZoneNumberQualification` with Zone 3 and 4

### Resources

- Two resources (`Jack`, `Mel`) start from a common depot
- Assigned working hours and capacity
- No fixed zone constraint; allowed to visit any zone

---

## Behavior Without Penalization

Without the penalty, the optimizer may **freely cross zones** multiple times, leading to routes that are:
- Logically correct
- But **costly or unrealistic** in real-world logistics

---

## Behavior With Penalization

With the penalty enabled:
- The optimizer **prefers intra-zone routing**
- Crosses only **when necessary** (e.g., once in the morning and once in the evening)

This results in **cleaner, cost-effective tours**, ideal for scenarios where:
- Toll costs exist
- Congestion zones apply
- Bridge/tunnel usage is limited

---

## Further Reading

- [Zone Crossing Docs](https://docs.dna-evolutions.com/overview_docs/zonecrossing/zonecrossing.html)
- [Territory Definition with ZoneCodes](https://docs.dna-evolutions.com/overview_docs/special_features/Special_Features.html#defining-territories-via-zonecodes)
- [KML Exporter Docs](https://docs.dna-evolutions.com)

---

## Benefits

- Reduces unnecessary zone transitions
- Encourages realistic routing decisions
- Offers fine-tuned control over zone-based penalties
- Compatible with other constraint layers (e.g., skills, costs, working hours)

---

## Package Location

```
com.dna.jopt.touroptimizer.java.examples.advanced.zonecrossing
```

---

## License

This example is distributed under the JOpt license terms.

More info: [https://www.dna-evolutions.com](https://www.dna-evolutions.com)

---
