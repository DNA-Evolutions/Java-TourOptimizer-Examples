# API Schema and Snapshot Format (OpenAPI)

This document connects the `schema.json` of JOpt.TourOptimizer to a Snapshot created with our Java lib:
1) the OpenAPI contract of your REST service, and
2) the canonical JSON snapshot format that is compatible with snapshots created by the Java library.

The goal is to make it easy for developers and integrators to understand what they can exchange with the optimizer service and how to keep it compatible across environments and versions.

---

## 1. What is in `schema.json`

**Title:** DNA Evolutions - JOpt.TourOptimizer
**Version:** 1.3.2

**Short description (from OpenAPI `info.description`):**
This is DNA's JOpt.TourOptimizer service. A RESTful Spring Boot application using springdoc-openapi and OpenAPI 3. JOpt.TourOptimizer is a service that delivers route optimization and automatic scheduling features to be easily integrated into any third-party application. JOpt.TourOptimizer encapsulates all necessary...

### Standard OpenAPI sections
- `info` -- service metadata (title, version, description)
- `paths` -- REST endpoints and their operations
- `components.schemas` -- reusable data model definitions (this is also the snapshot model)

---

## 2. Snapshot compatibility (core concept)

A "snapshot" is a JSON serialization of the same model types defined under:

- `components.schemas`

That is why the REST API schema is also a portable file format definition:
- A snapshot produced by the Java library can be sent to the REST API.
- A snapshot retrieved from the REST API (or persistence endpoints) can be loaded back into Java workflows, provided it follows this schema.

In practice, the snapshot is usually represented by:

- `components.schemas.RestOptimization`

---

## 3. Snapshot root object: `RestOptimization`

`RestOptimization` is the practical snapshot root because it bundles:
- the optimization input (nodes, resources, relations, connections),
- solver configuration (properties, scheme options),
- and optionally a `solution`.

### Top-level properties in `RestOptimization`
- `coreBuildOptions`
- `createdTimeStamp`
- `creator`
- `elementConnections`
- `extension`
- `id`
- `ident`
- `nodeRelations`
- `nodes`
- `optimizationOptions`
- `optimizationStatus`
- `resources`
- `solution`
- `typeDictionaries`
- `zoneConnections`

### How to think about the top-level structure

1) Metadata and lifecycle
- identifiers (`id`, `ident`)
- timestamps
- status fields (optimization status, progress indicators)

2) Problem definition
- nodes and resources
- node relations
- explicit connections (element/zone connections)
- type dictionaries (types/skills classification data)

3) How the solver runs
- `optimizationOptions` including:
  - `properties` (key-value tuning surface)
  - `optimizationSchemeOptions` (execution pipeline architecture)

4) Solution payload (optional)
- `solution` holds the computed routes and related outputs

5) Extensibility
- `extension` is the standard space for custom fields without breaking compatibility

---

## 4. Solution payload: `Solution`

Snapshots may or may not contain a solution:
- Pre-run snapshots usually contain only the optimization input and options.
- Post-run snapshots typically contain input + solution.

### Top-level properties in `Solution`
- `createdTimeStamp`
- `creator`
- `header`
- `id`
- `ident`
- `optimizationStatus`
- `routes`

Recommendation:
- Treat `Solution` as an output payload that can be persisted and compared.
- Keep the original input snapshot as well to preserve full reproducibility.

---

## 5. Solver configuration: `OptimizationOptions`

`OptimizationOptions` is the portable way to carry runtime configuration inside the snapshot.

### Top-level properties in `OptimizationOptions`
- `optimizationSchemeOptions`
- `properties`

### The most important field: `OptimizationOptions.properties`
`properties` is the main tuning surface for portable configuration, such as:
- objective weights,
- algorithm parameters,
- AutoFilter settings,
- performance settings (CPU cores),
- exit conditions and convergence behavior,
- and other advanced knobs.

This design is a key reason snapshots are environment-independent:
- you do not need Java-only configuration objects to reproduce a run,
- the snapshot carries the properties as a stable key-value map.

Reference documentation for the property catalog:
- https://docs.dna-evolutions.com/overview_docs/optimizationproperties/Optimization_Properties.html

---

## 6. Endpoints and where snapshots are used

The schema defines **26** endpoints.

### Endpoint groups (by first path segment)
- `api`: 25 endpoints
- `healthStatus`: 1 endpoints

Common patterns:

### Pattern A: "Send snapshot, get result"
- Client POSTs a `RestOptimization` snapshot to an optimize endpoint (for example `/api/optimize/run`).
- Service returns a result payload (often includes a solution or result summary).

### Pattern B: "Persist snapshot, load later"
- Client stores snapshots via database endpoints under `/api/db/...`.
- Client retrieves stored snapshots later for review, comparison, or re-optimization.

### Pattern C: "Resume / continue"
Because the schema is the snapshot contract, you can:
- load a stored snapshot,
- re-create an optimization instance,
- and continue optimization after applying updates (new nodes, changed resources, new constraints).

---

## 7. How the Spring application produces the OpenAPI JSON

Your Spring application generates the OpenAPI schema using springdoc.

### Evidence from `pom.xml`
The `pom.xml` includes:
- `org.springdoc:springdoc-openapi-starter-webflux-ui`
- version: 2.8.13

### Typical springdoc endpoints
In a standard springdoc setup, OpenAPI is served at:
- `/v3/api-docs` (OpenAPI JSON)
- `/swagger-ui.html` (Swagger UI)

Your `schema.json` is typically exported from `/v3/api-docs`.

### Runtime context from `application.properties`
- server port: 8081



Note:
- These settings influence runtime behavior and access.
- They do not change the schema model itself, but can affect which endpoints are accessible and how the service is deployed.

---

## 8. Compatibility and versioning recommendations

Because this schema is also a snapshot file format, treat it as a contract.

### Recommended practices
1) Store schema version alongside each persisted snapshot
- Use `info.version` and/or your own build info metadata.

2) Prefer additive changes
- Add new optional fields and new schema types.
- Avoid changing the meaning of existing fields.

3) Use `extension` for custom additions
- Keep custom fields namespaced.
- Do not overload core fields for customer-specific needs.

4) Validate snapshots before import
- Validate JSON against this schema in CI and in ingestion pipelines.
- Reject incompatible snapshots early with clear error messages.

---

## 9. Minimal operational checklist for snapshot exchange

Before sending or loading a snapshot:
- Validate required fields (ids, nodes, resources, options).
- Ensure all IDs are unique and stable.
- Ensure connections are consistent with your distance/time strategy.
- Ensure properties are present for the intended execution mode.
- If loading a solution snapshot, ensure the solution references valid node/resource IDs from the input section.

---

## Quick links
- Docs hub: https://docs.dna-evolutions.com
- Examples repo: https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples
