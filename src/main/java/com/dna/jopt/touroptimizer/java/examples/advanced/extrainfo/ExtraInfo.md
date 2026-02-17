# ExtraInfo — Attaching Domain Metadata to Nodes and Resources

In real-world optimization systems you almost always need to carry **domain metadata** alongside routing entities, for example:
- customer contact information,
- service instructions,
- order identifiers,
- technician phone numbers,
- compliance notes,
- UI labels or external system keys.

JOpt TourOptimizer supports this directly via an **`extraInfo` string** on both:
- `INode`
- `IResource`

The `extraInfo` value is intentionally modeled as a **plain string** so you can store:
- a compact text note, or
- structured data (typically JSON) that your application can parse.

This document explains the recommended pattern and how the provided example implements it.

---

## References

### Example sources
- [ExtraInfoExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/extrainfo/ExtraInfoExample.java)
- [NodeExtraInfo.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/extrainfo/NodeExtraInfo.java)
- [ResourceExtraInfo.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/extrainfo/ResourceExtraInfo.java)

---

## The concept: a domain payload attached to solver entities

### Where it lives
Both nodes and resources support:
- `setExtraInfo(String extraInfo)`
- `Optional<String> getExtraInfo()`

### What it is used for
The solver treats `extraInfo` as **opaque**—it does not interpret the content.  
That is by design.

Your application uses it to:
- reconstruct domain objects after optimization,
- enrich reporting and UI,
- pass identifiers into downstream systems (dispatch, ERP, CRM, WFM).

---

## Recommended storage formats

### Option A — Simple string
Use when you only need a note or label:
- “Gate code: 1947”
- “Deliver to back entrance”
- “VIP customer — call before arrival”

### Option B — JSON (recommended)
Use when you need structured metadata.

Advantages:
- stable schema,
- easy parsing,
- supports backward compatibility strategies,
- easy to version and extend.

The provided example uses JSON serialization for exactly this reason.

---

## The example: storing POJOs as JSON in `extraInfo`

The example `ExtraInfoExample` demonstrates a complete round-trip:

1. Create nodes/resources
2. Create a POJO for each entity
3. Serialize POJO → JSON string
4. Store JSON string via `setExtraInfo(...)`
5. After the run, read all entities with extraInfo
6. Deserialize JSON string → POJO and print it

### NodeExtraInfo (node-side POJO)
The `NodeExtraInfo` POJO contains:
- `phone`
- `contactPerson`

It provides a readable `toString()` so it prints nicely after deserialization.

### ResourceExtraInfo (resource-side POJO)
The `ResourceExtraInfo` POJO contains:
- `phone`
- `birthday` (`Instant`)

This demonstrates that you can store non-trivial types as long as your JSON mapper can serialize/deserialize them.

---

## How the example stores extraInfo

### On nodes
When creating a node, the example builds a JSON payload:

- Create a `NodeExtraInfo(phone, contactPerson)`
- Serialize it with the repository’s `ConfigSerialization.objectMapper()`
- Store it:

- `node.setExtraInfo(jsonString);`

This is shown for nodes such as “Koeln” and “Essen” with different contact data.

### On resources
When creating a resource, the example does the same:

- Create a `ResourceExtraInfo(phone, birthdayInstant)`
- Serialize
- Store via `resource.setExtraInfo(jsonString)`

---

## How the example reads extraInfo back

After the run (in the result presentation flow), the example:

1. Collects all nodes that have extraInfo:
   - `n.getExtraInfo().isPresent()`

2. For each, reads the JSON back into a POJO:
   - `readValue(extra, NodeExtraInfo.class)`

3. Prints the decoded POJO.

It repeats the same for resources:
- `readValue(extra, ResourceExtraInfo.class)`

This is the critical point:
- JOpt does not force any schema.
- You control the schema and decoding.

---

## Best practices for production systems

### 1) Treat extraInfo as a versioned contract
Because extraInfo often ends up persisted (results, logs, exports), treat it like an API:
- Add a version field in your JSON, e.g. `"schemaVersion": 1`
- Use backward compatible decoding logic
- Avoid breaking changes (rename/remove fields) without migration

### 2) Prefer IDs over copying large payloads
A very robust pattern is:
- store only stable domain keys (orderId, customerId),
- and look up full objects in your database.

This avoids:
- duplicating sensitive data,
- large result payloads,
- schema drift.

### 3) Keep it small and intentional
ExtraInfo is attached to every entity and can be copied around in:
- optimization snapshots,
- JSON exports,
- logging output.

Therefore:
- avoid huge payloads,
- avoid embedding binary data,
- avoid verbose nested structures unless necessary.

### 4) Security and privacy
Do not store sensitive PII in extraInfo unless:
- you have a strong reason,
- and you have a secure storage/transport policy.

If you must store personal data:
- encrypt the payload before setting it,
- or store only references and resolve at runtime.

### 5) Use a consistent mapper configuration
If you serialize/deserialize JSON:
- ensure all services use the same ObjectMapper settings,
- especially for time types (`Instant`), field naming, and null handling.

The example uses `ConfigSerialization.objectMapper()` to keep this consistent.

---

## Typical application patterns

### UI enrichment
Display node cards with:
- contact person,
- phone,
- delivery notes.

### Dispatch export
When exporting routes, include:
- order IDs,
- external system references.

### Post-run analytics
Store:
- customer segment,
- planned SLA type,
- business priority,
in extraInfo so analytics does not need to rejoin on external systems.

---

## Summary

- `extraInfo` is an **opaque string field** available on nodes and resources.
- It is ideal for attaching domain metadata that the solver should not interpret.
- JSON is the recommended format for structured metadata.
- The provided example demonstrates end-to-end:
  - POJO → JSON → setExtraInfo → run → getExtraInfo → JSON → POJO.
- In production, treat extraInfo as a small, versioned contract and be mindful of privacy and payload size.
