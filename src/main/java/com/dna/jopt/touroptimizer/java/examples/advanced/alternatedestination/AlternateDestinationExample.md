# Alternate Destination — `AlternateDestinationExample`

This advanced example shows how to **terminate a resource route at an alternate destination** instead of the resource’s home/start location.

In operational terms: the driver/technician **starts at a home base**, performs work during WorkingHours, and **ends the working day at a different location** (e.g., a hotel, a secondary depot, a handover location). The resource then “returns home” outside WorkingHours and starts the next day at home again.

---

## Clickable links

- Example source (GitHub):  
  [AlternateDestinationExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/alternatedestination/AlternateDestinationExample.java)

- Core modeling background (nodes/resources/properties):  
  [Basic elements tutorial](https://www.dna-evolutions.com/docs/getting-started/tutorials/basic-elements)

---

## What this example demonstrates

### The key feature: alternate destination on the resource

The example configures a single `CapacityResource` (“Jack”) with:

- a normal **home/start location** (Aachen: `50.775346, 6.083887`)
- and an **alternate end destination** (Essen: `51.45, 7.01667`)

The implementation is explicit and easy to reuse:

- `rep1.setDestinationLatitude(51.45);`
- `rep1.setDestinationLongitude(7.01667);`
- `rep1.setAlternateDestination(true);`

The code comment clarifies the intended semantics:

- The resource route **terminates** at the alternate destination **at the end of the day**.
- The resource is then assumed to travel back to its home location **outside** WorkingHours and starts working there again the next morning.

This is a strong pattern for realistic field operations where “end of day” is not necessarily “back at the depot”.

---

## Modeling details (what the solver sees)

### Working hours
Two working days (Europe/Berlin):

- May 6, 2020: 08:00–17:00  
- May 7, 2020: 08:00–17:00

### Resource constraints
- max working time: 13 hours
- max distance (per working day): 1200 km

### Nodes (visits)
This example uses `TimeWindowGeoNode` and a shared opening-hours schedule for all nodes (May 6–7, 08:00–17:00), with:

- visit duration: 20 minutes
- importance: 1

Nodes added:
- Koeln
- Oberhausen
- Essen
- Heilbronn
- Stuttgart
- Wuppertal
- Aachen

---

## Execution and output

### Run
The optimization is started via:

- `CompletableFuture<IOptimizationResult> resultFuture = this.startRunAsync();`
- `resultFuture.get();` (important: keeps the JVM alive until completion)

### Result print
The callback:

- `onAsynchronousOptimizationResult(IOptimizationResult rapoptResult)`

prints the result and exports a KML file:

- `./AlternateDestinationExample.kml`

This is convenient for visually inspecting how the last stop and termination are handled.

---

## Why alternate destinations are useful

Typical field-service and logistics scenarios:

- **Multi-day tours**: end near the last customer to reduce deadhead time.
- **Hotel/overnight logic**: terminate at a hotel location while still modeling a “home base” for the next day.
- **Shift handover**: terminate at a hub where another resource continues (advanced multi-resource patterns).
- **Regional depots**: start at one depot and end at another.

The key benefit is that the plan reflects operations as they happen, without forcing an artificial “return home within shift hours” pattern.

---

## Practical considerations

### 1) Validate your KPI interpretation
When you change termination behavior, route totals can be interpreted differently:
- distance/time within WorkingHours,
- termination distance/time,
- “outside WorkingHours” repositioning.

Ensure your reporting aligns with your business interpretation of:
- paid shift time,
- repositioning time,
- overnight travel policy.

### 2) Combine with external distances when accuracy matters
If you rely on fallback distance models, ensure they match your business. For road-based distances/times consider external connections or a customized fallback connector.

### 3) Consider multi-day consistency
This example demonstrates a conceptually “daily” alternate end point but assumes start is still home each morning.  
For true multi-day routing where day N ends at a place and day N+1 starts there, you typically need an extended modeling strategy (advanced/expert patterns).

---

## How to run

Run `main(String[] args)` in `AlternateDestinationExample`.

After completion, check the working directory for:

- `AlternateDestinationExample.kml`
