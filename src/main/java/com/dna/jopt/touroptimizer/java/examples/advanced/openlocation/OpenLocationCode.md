# OpenLocationCode — Using Plus Codes Instead of Latitude/Longitude

JOpt TourOptimizer supports **Open Location Codes (Plus Codes)** as an alternative to raw latitude/longitude coordinates.

This is useful when you want:
- **copy/paste friendly location identifiers** (short strings instead of two floating-point values),
- a location format that is **human-shareable** and easy to store in CSV / JSON / databases,
- a standardized, offline-capable way to represent locations without requiring street addresses.

Plus Codes are a public standard originally popularized by Google Maps.  
Reference: https://maps.google.com/pluscodes/

---

## References

### Examples
- Lat/Lon ↔ Plus Code conversion (GitHub):  
  https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/openlocation/LatLon2PlusCodeBackAndForthExample.java

- Optimization built from Plus Codes (GitHub):  
  https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/openlocation/OptimizationWithOpenLocationCodesExample.java

### JOpt API reference
- Open Location Converter package (Javadoc):  
  https://public.javadoc.dna-evolutions.com/com/dna/jopt/member/unit/converter/openlocation/package-summary.html

---

## What an Open Location Code is (practical view)

A Plus Code is a compact string (typically 10–12 characters) representing a **geographic area**.
Example codes from the optimization example:

- `9F28WXM2+82` (Cologne)
- `8FXF46RC+42` (Heilbronn)

In practice:
- You store and exchange the code.
- JOpt converts it into a `Position` (lat/lon) when creating nodes/resources.
- JOpt can also convert a `Position` back into a Plus Code for reporting/export.

---

## Two supported workflows (as shown in the examples)

### Workflow A — Convert existing coordinates to Plus Codes (and back)

Example: `LatLon2PlusCodeBackAndForthExample`

This example demonstrates a **round trip** for both nodes and resources:
1. Start with entities that already have `Position` (lat/lon).
2. Convert the position to a Plus Code via:

- `String olc = e.getPosition().toOpenLocationCode();`

3. Convert the Plus Code back to a Position via:

- `OpenLocation loc = OpenLocation.builder().code(olc).build();`
- `Position posBack = loc.toPos();`

4. Print original position, code, and decoded position.

**Why this is valuable**
- You can standardize on Plus Codes in your data layer even if your initial source is lat/lon.
- You can display Plus Codes in logs and reports as stable identifiers.

---

### Workflow B — Build an optimization directly from Plus Codes

Example: `OptimizationWithOpenLocationCodesExample`

This example shows the “pure Plus Code” approach:
- You never provide lat/lon in the input layer.
- Instead, you maintain a map:

- Node ID → Plus Code  
- Resource ID → Plus Code

Then each element is created by decoding the Plus Code:

- `OpenLocation.of(code).toPos()`

In the code, this is used for:
- `TimeWindowGeoNode(...)`
- `CapacityResource(...)`

**Why this is valuable**
- Your integration can store locations as a single, copy/paste friendly string.
- You eliminate parsing issues and locale problems common with decimal separators.
- It becomes easy to share locations in support tickets and tests.

---

## Practical advantages in production systems

### 1) Cleaner interfaces and fewer parsing errors
A Plus Code is a single string:
- easy to validate,
- easy to log,
- easy to store.

In contrast, lat/lon frequently suffers from:
- swapped coordinates,
- rounding mistakes,
- locale formatting differences,
- JSON precision issues.

### 2) A convenient “location key” for external systems
When you must exchange locations with:
- a dispatch UI,
- a customer portal,
- a partner system,

Plus Codes are often easier than raw coordinates and do not require full address geocoding.

### 3) Reproducible test data
Plus Codes make test cases extremely easy to maintain:
- no floating point formatting noise,
- deterministic copy/paste from reference sources.

---

## Important modeling notes

### Codes represent areas, not perfect points
A Plus Code represents a **cell/area**; decoding yields a representative position (commonly the cell center).

Practical impact:
- For high-precision problems (building entrance routing, indoor logistics), ensure the code resolution is sufficient.

### Prefer full (global) Plus Codes in optimizer inputs
The optimization example uses full codes such as `9F28WXM2+82`.  
These are globally unambiguous.

(Shortened Plus Codes exist in the broader standard, but require a reference locality. For routing optimization inputs, full codes are generally the safest and most reproducible choice.)

### Plus Codes do not replace road-network distances
Plus Codes are a location representation.  
Distance/time quality still depends on your chosen connector strategy (fallback, matrices, external routing).

---

## Recommended usage pattern

1. Store Plus Codes as your canonical location identifier in your domain model.
2. Convert Plus Codes to `Position` at the boundary where you build:
   - nodes (`TimeWindowGeoNode`), and
   - resources (`CapacityResource`).
3. Optionally convert positions back to Plus Codes for:
   - debugging,
   - support,
   - exports and logs.

This yields a clean separation:
- your system speaks Plus Codes,
- the optimizer operates on positions.

---

## Summary

- JOpt supports Open Location Codes (Plus Codes) as a location representation.
- You can convert:
  - `Position → Plus Code` via `toOpenLocationCode()`,
  - `Plus Code → Position` via `OpenLocation.of(code).toPos()` (or builder form).
- The two examples demonstrate:
  - conversion round trip, and
  - building a full optimization instance purely from Plus Codes.
- In production, Plus Codes are an excellent choice for robust, copy/paste friendly location handling.
