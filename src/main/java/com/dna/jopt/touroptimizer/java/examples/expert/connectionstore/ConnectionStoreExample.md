# Time-Dependent Connection Store Example

This example demonstrates how to use a **time-dependent connection store** in [JOpt TourOptimizer](https://www.dna-evolutions.com/products/jopt-tour-optimizer/) to model realistic traffic scenarios where driving time (and optionally distance) varies based on the **time of day** and **day of the week**.

## Goal

To show how driving times between nodes can dynamically change over time, for example:
- Longer driving times during **weekday rush hours**
- Slightly increased travel times on **weekend mornings and evenings**
- Normal driving times during **off-peak hours**

By doing this, we allow the optimizer to consider **traffic congestion** and **time-based routing effects** during optimization.

---

## Concept Overview

In JOpt, each node-to-node connection can include:
- **Base driving time and distance**
- A **connection store** (`ConnectionBucket`) containing time-specific overrides

These overrides are defined using:
- `TimedConnectionData`: Specifies time-dependent driving durations
- `ConnectionBucket`: Maps time ranges (e.g., weekdays vs. weekends) to timed values

**Linear interpolation** is used between specified time points to simulate continuous time-dependent behavior.

---

## Traffic Modeling

### Weekday Profile (Monday–Friday)

| Time          | Multiplier | Traffic Description       |
|---------------|------------|---------------------------|
| 06:00         | 1.0        | Normal                    |
| 07:00–09:00   | 1.8        | Morning Rush Hour         |
| 13:00         | 1.0        | Normal Midday             |
| 16:00–19:00   | 1.6        | Evening Rush Hour         |
| 20:00         | 1.0        | Normal/Night              |

### Weekend Profile (Saturday–Sunday)

| Time          | Multiplier | Traffic Description       |
|---------------|------------|---------------------------|
| 06:00         | 1.0        | Normal                    |
| 07:00–09:00   | 1.05       | Slight morning delay      |
| 13:00         | 1.0        | Normal                    |
| 16:00–19:00   | 1.05       | Slight evening delay      |
| 20:00         | 1.0        | Normal/Night              |

---

## Structure of the Example

- `ConnectionStoreExample`: Main class extending `Optimization`
  - Sets up nodes, resources, and connection rules
  - Applies traffic-aware connection data
  - Executes the optimization and prints results (including a JSON export)
  
### Key Methods

| Method                                | Description |
|---------------------------------------|-------------|
| `addNodesAndResourcesWithConnectionStore` | Sets up resources, nodes, and time-dependent connections |
| `createConnectionBucket`             | Defines weekday/weekend traffic buckets per connection |
| `createTimedWeekdayConnections`      | Models rush hour on weekdays |
| `createTimedWeekendConnections`      | Models lighter weekend traffic variations |
| `element2ElementDuration`            | Converts distance to base duration using a fixed speed |
| `attachToObservables`                | Subscribes to optimization events for logging |

---

## Execution Flow

1. **Create resource** (e.g., "Jessi from Aachen") with working hours
2. **Create multiple nodes** with opening hours (some only open on weekdays or weekends)
3. **Generate connections** between nodes with:
   - Static driving time/distance
   - Overridden time-dependent durations using `TimedConnectionData`
4. **Start optimization**
5. **Print results and JSON export**

---

## JSON Export

After optimization, a full JSON export of the scenario (without build or solution data) is printed using:

```java
String json = prettySerialize(exportedConfig);
System.out.println(json);
