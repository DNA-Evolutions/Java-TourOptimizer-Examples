# Connection Store — Time-Dependent Driving Times (Traffic Profiles)

Many routing problems are not only “where are the nodes?” but also “**when** do we drive between them?”.  
Traffic patterns can dominate route quality:

- morning and evening rush hours,
- weekend vs weekday profiles,
- time-of-day dependent congestion,
- predictable slowdowns around schools, industrial zones, or city centers.

JOpt supports this using a **time-dependent connection store**: a connection (edge) can carry a **bucket of timed overrides** that adjust driving time (and optionally distance) based on:
- **time of day**
- **day of week / day ranges**

This example shows a pragmatic way to approximate traffic using a few time grid points and **linear interpolation** between them.

---

## References

- Example source: [ConnectionStoreExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/connectionstore/ConnectionStoreExample.java)

Additional conceptual background on how JOpt computes distances and durations:
- https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/backupconnector#description-how-jopttouroptimizer-calculates-distances-and-times

---

## What a Connection Store is (conceptual)

In JOpt, the optimizer reasons about **node-to-node connections** (distance + driving duration).  
A connection store extends this by allowing a single edge to expose *multiple* driving durations depending on departure time.

Think of it as a “traffic overlay” on top of a base distance/time:

- Base connection: “A → B is 12.3 km and normally 18 minutes”
- Timed overrides: “At 08:30 on weekdays it behaves like 32 minutes”

The benefit is that the optimizer can:
- schedule visits *and* choose sequencing while being aware that travel time changes over the day,
- and therefore avoid routes that look good at 03:00 but fail at 08:00.

---

## Structure used by the example

The example builds time-dependent connections using:

- `NodeEdgeConnectorItem` — the actual edge (distance + base driving time)
- `ConnectionBucket` — a container holding timed profiles
- `TimedConnectionData` — a “time grid point” (time → distance/time override)

Two day-range profiles are attached per connection:

1. **Weekend profile** (Saturday–Sunday)
2. **Weekday profile** (Monday–Friday)

Between specified times (e.g., 07:00 and 09:00), durations are **interpolated linearly**, which yields a smooth “traffic curve” without requiring a full minute-by-minute table.

---

## Traffic profiles in this example

The example models a base driving time, then applies multipliers by time of day.

### Weekday profile (Mon–Fri)

| Time | Multiplier | Interpretation |
|---|---:|---|
| 06:00 | 1.0 | normal traffic |
| 07:00–09:00 | 1.8 | morning rush hour |
| 13:00 | 1.0 | midday normal |
| 16:00–19:00 | 1.6 | evening rush hour |
| 20:00 | 1.0 | night normal |

### Weekend profile (Sat–Sun)

| Time | Multiplier | Interpretation |
|---|---:|---|
| 06:00 | 1.0 | normal traffic |
| 07:00–09:00 | 1.05 | light morning delay |
| 13:00 | 1.0 | midday normal |
| 16:00–19:00 | 1.05 | light evening delay |
| 20:00 | 1.0 | night normal |

These profiles are created in:
- `createTimedWeekdayConnections(...)`
- `createTimedWeekendConnections(...)`

---

## How the bucket is attached to a connection

At a high level, the example does:

1. Create a base connection between nodes (distance + base duration)
2. Create a `ConnectionBucket` for that connection
3. Fill the bucket with timed profiles for day ranges
4. Attach the bucket to the connection

This is implemented using:
- `createConnectionBucket(connection)`  
  which constructs the buckets for weekday and weekend ranges.

The day ranges are expressed with:
- `RangeDef.of(DayDef.of(DayOfWeek.MONDAY), DayDef.of(DayOfWeek.FRIDAY))`
- `RangeDef.of(DayDef.of(DayOfWeek.SATURDAY), DayDef.of(DayOfWeek.SUNDAY))`

---

## Practical modeling guidance

### 1) Start with a small number of time grid points
You generally do not need hundreds of time entries. A few grid points per profile are often enough:
- pre-rush,
- rush start,
- rush end,
- midday,
- evening rush start,
- evening rush end,
- night.

The optimizer benefits from the signal (“rush hour is expensive”) without being burdened by ultra-detailed data.

### 2) Calibrate multipliers to match your region
Start with conservative multipliers and adjust based on observed delays:
- urban cores can easily exceed 2.0 during peak times,
- weekend multipliers can be near 1.0 in business districts but higher near leisure destinations.

### 3) Decide what is time-dependent: duration only vs duration + distance
In most cases:
- distance stays constant,
- duration varies.

Some deployments also vary distance (e.g., rerouting around city centers).  
The API supports both; this example demonstrates adjusting the duration (and keeps the concept extendable).

### 4) Combine with OpeningHours and WorkingHours
Traffic-aware edges become especially valuable when used with time constraints:
- morning peak can push you into lateness,
- evening peak can create overtime risk,
- weekend hours may create different feasible plans.

The example includes nodes with weekday-only and weekend-only opening hours to illustrate this interplay.

---

## Operational benefits

### Debugging and model verification
Time-dependent connections make it easier to explain “why the optimizer avoided that route”:
- a plan that crosses the city at 08:30 is objectively slower than at 11:00.

### End user acceptance
Planners often distrust optimizers when they “ignore traffic reality”.  
A traffic-aware connection store makes results much more intuitive:
- less “impossible timing”,
- fewer routes that look feasible only on paper,
- fewer ad-hoc manual corrections post-optimization.

### Incremental adoption
You can start with:
- one profile (weekday), then extend to weekend,
- a small set of critical corridors (city center edges), then expand.

---

## Exporting and inspection

The example prints a JSON export of the configuration (without solution/build data).  
This is useful for:
- verifying that your connection bucket is present,
- checking that timed points are serialized as expected,
- and creating test fixtures for regression testing.

---

## Summary

- A **Connection Store** makes driving time depend on **time of day** and **day of week**, enabling realistic traffic-aware routing.
- The example uses a few time grid points and linear interpolation to model weekday rush hours and mild weekend effects.
- This improves feasibility, solution realism, debugging transparency, and planner acceptance.
