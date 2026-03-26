# Creating REST TourOptimizer JSON Input from a Java Optimization — JOpt TourOptimizer

This document explains how to translate a **Java-defined optimization problem** into a valid **JSON payload** for the JOpt.TourOptimizer REST API.

This bridge is particularly valuable when:

- **Debugging**: you build and inspect a problem in Java but want to submit it manually via the Swagger UI or a REST client.
- **Hybrid production setups**: Java is used for problem construction (nodes, resources, constraints) while the actual optimization is offloaded to a containerized REST server.
- **Snapshot seeding**: an already-optimized solution is embedded into the JSON payload so the REST server can use it as a warm start.

**Sources (GitHub):**
- [CreateRestTourOptimizerInputWithoutSolutionExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/restful/createinput/CreateRestTourOptimizerInputWithoutSolutionExample.java)
- [CreateRestTourOptimizerInputWithSolutionExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/restful/createinput/CreateRestTourOptimizerInputWithSolutionExample.java)

---

## Overview

- [Why translate Java to JSON?](#why-translate-java-to-json)
- [The two variants](#the-two-variants)
- [Core conversion pattern](#core-conversion-pattern)
- [Example 1 - Without solution (fresh start)](#example-1-without-solution-fresh-start)
- [Example 2 - With solution (warm start)](#example-2-with-solution-warm-start)
- [Compatible REST endpoints](#compatible-rest-endpoints)
- [Implementation notes](#implementation-notes)
- [End-to-end workflow recipes](#end-to-end-workflow-recipes)

---

## Why translate Java to JSON?

The JOpt core library is a Java API. The JOpt.TourOptimizer REST server accepts JSON payloads. These two examples demonstrate a **serialization bridge** that makes both worlds interoperable.

The benefits are significant in practice:

- **No re-implementation**: your Java problem definition is the single source of truth. You do not have to manually construct a JSON payload or maintain two versions.
- **Exact reproduction**: the JSON produced by these examples is fully compatible with the REST server. What you see from the Java run is exactly what the REST server will optimize.
- **Progressive workflows**: start with a Java-local run, capture the solution, then hand it off to a REST server as a warm-start seed — without changing the problem definition.
- **Manual debugging via Swagger**: paste the JSON output directly into the [Swagger UI](https://swagger.dna-evolutions.com/v3/api-docs/OptimizeConfig) to inspect or reproduce a specific scenario.

---

## The two variants

| Variant | What it serializes | Typical use case |
|---|---|---|
| `WithoutSolution` | Problem definition only (nodes, resources, properties) | First-time submission to the REST server; fresh optimization |
| `WithSolution` | Problem definition **plus** the result of a local run | Warm start: REST server continues from an already-found solution |

Both variants produce JSON that can be submitted to the same REST endpoints.

---

## Core conversion pattern

The key utility method is reusable and can be dropped into any project that needs to bridge Java models to REST payloads:

```java
public static String jsonFromOptimization(IOptimization opti)
        throws IOException, ConvertException, SerializationException {

    String licenseKey = "YOUR_JSON_LIC"; // Replace with your JSON license key

    Duration timeOut = Duration.ofMinutes(10);

    JSONConfig myExtension = JSONConfig.builder()
            .keySetting(OptimizationKeySetting.of(licenseKey))
            .timeOut(timeOut)
            .build();

    return JSONOptimization.asJSON(JSONOptimization.fromOptization(opti, Optional.of(myExtension)));
}
```

The `timeOut` value serves a dual purpose:
- It limits the serialization process on the Java side.
- It is embedded in the JSON payload and used by the REST server as the **maximum allowed optimization duration**.

> **Note:** Replace `"YOUR_JSON_LIC"` with a valid [JSON license key](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/license). Without a valid license, the optimizer runs in a limited mode.

---

## Example 1 - Without solution (fresh start)

**Class:** `CreateRestTourOptimizerInputWithoutSolutionExample`

This example constructs an optimization problem, serializes it to JSON, and prints the result. No local optimization run is performed — the JSON only encodes the **problem definition**.

### What it does (step by step)

1. Creates a new `Optimization` instance.
2. Adds seven nodes (German cities: Koeln, Essen, Dueren, Nuernberg, Heilbronn, Wuppertal, Aachen) with two-day opening hours and a 20-minute visit duration each.
3. Adds one resource ("Jack from Aachen") with two working days, a max working time of 9 hours, and a max travel distance of 1,200 km.
4. Assigns a run identifier: `"MyJOptRun"`.
5. Wraps the optimization in a `JSONConfig` (license + timeout).
6. Calls `JSONOptimization.fromOptization(...)` followed by `JSONOptimization.asJSON(...)` to produce the JSON string.
7. Prints the JSON to stdout.

### Skeleton

```java
IOptimization myOpti = new Optimization();

// Build the problem
CreateRestTourOptimizerInputWithoutSolutionExample.addNodes(myOpti);
CreateRestTourOptimizerInputWithoutSolutionExample.addResources(myOpti);
myOpti.setOptimizationRunIdent("MyJOptRun");

// Serialize to REST-compatible JSON
JSONConfig myExtension = JSONConfig.builder()
        .keySetting(OptimizationKeySetting.of(licenseKey))
        .timeOut(Duration.ofMinutes(10))
        .build();

String json = JSONOptimization.asJSON(JSONOptimization.fromOptization(myOpti, Optional.of(myExtension)));
System.out.println(json);
```

### When to use this variant

- You want to submit a **new, unsolved problem** to the REST server.
- You are generating test payloads for the Swagger UI or integration tests.
- You want to inspect what the REST server will receive before sending a real request.

---

## Example 2 — With solution (warm start)

**Class:** `CreateRestTourOptimizerInputWithSolutionExample`

This example extends the first by **running the optimization locally before serializing**. The resulting JSON snapshot includes the locally found solution, which the REST server can use as a **starting point** for further optimization instead of beginning from scratch.

### What it does (step by step)

1. Creates a new `Optimization` instance and sets the license via `ExampleLicenseHelper.setLicense(myOpti)`.
2. Adds the same nodes and resources as the first example.
3. Assigns a run identifier: `"MyJOptRunWithSolution"`.
4. **Runs the optimization locally** using `myOpti.startRunAsync()` and blocks until the result is available.
5. Serializes the post-run optimization state (problem + solution) using the same `JSONConfig` and `JSONOptimization.asJSON(...)` pattern.
6. Prints the JSON to stdout.

### Skeleton

```java
IOptimization myOpti = new Optimization();

// License and problem definition
ExampleLicenseHelper.setLicense(myOpti);
CreateRestTourOptimizerInputWithSolutionExample.addNodes(myOpti);
CreateRestTourOptimizerInputWithSolutionExample.addResources(myOpti);
myOpti.setOptimizationRunIdent("MyJOptRunWithSolution");

// Run locally first — the solution will be embedded in the JSON
CompletableFuture<IOptimizationResult> resultFuture = myOpti.startRunAsync();
System.out.println(resultFuture.get()); // Block until done

// Serialize the post-run state (problem + solution) to REST-compatible JSON
JSONConfig myExtension = JSONConfig.builder()
        .keySetting(OptimizationKeySetting.of(licenseKey))
        .timeOut(Duration.ofMinutes(10))
        .build();

String json = JSONOptimization.asJSON(JSONOptimization.fromOptization(myOpti, Optional.of(myExtension)));
System.out.println(json);
```

### Key difference from the `WithoutSolution` variant

The critical distinction is **when** `fromOptization(...)` is called:

- **Without solution**: called on a fresh, unrun `IOptimization` → JSON contains only the problem definition. The REST server will optimize it entirely from scratch.
- **With solution**: called on an already-run `IOptimization` → JSON embeds the current best solution. The REST server uses this as a seed and continues improving from there.

### When to use this variant

- You have already run locally and want the REST server to **continue improving** the result (e.g., with a longer timeout or different properties).
- You want to use Java as a **pre-solver** to generate a good initial solution, then hand off to the cloud.
- You want to **benchmark** local vs. REST results starting from the same initial solution.

---

## Compatible REST endpoints

The JSON produced by both examples is accepted by the following JOpt.TourOptimizer REST endpoints:

| Endpoint | Description |
|---|---|
| `/api/optimize/config/run` | Starts optimization and streams progress + final result |
| `/api/optimize/config/runOnlyResult` | Starts optimization and returns only the final result |

For production integrations, the recommended approach is to generate a typed client from the OpenAPI specification:

- **Swagger annotation / API schema:** [https://swagger.dna-evolutions.com/v3/api-docs/OptimizeConfig](https://swagger.dna-evolutions.com/v3/api-docs/OptimizeConfig)

---

## Implementation notes

### The `JSONConfig` builder

`JSONConfig` carries the **REST-specific metadata** that the core library itself does not have. The two most important fields are:

- **`keySetting`**: wraps your JSON license key via `OptimizationKeySetting.of(licenseKey)`. This is required for REST-server runs.
- **`timeOut`**: passed to `JSONConfig.builder().timeOut(...)`. This duration controls the maximum time the REST server will spend optimizing. Choose a value appropriate for your scenario — the REST server honours this as a hard upper bound.

### License key

The examples reference `ExampleLicenseHelper.PUBLIC_JSON_LICENSE` as a placeholder. In production:

- Replace this with your own [JSON license key](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/license).
- Never hardcode keys in shared repositories. Use environment variables or a secrets manager.

### Blocking the async call (WithSolution variant)

In the `WithSolution` example, `resultFuture.get()` is called explicitly before serialization. This is **mandatory**:

- The `IOptimization` object is only fully populated with a solution once the run completes.
- Calling `fromOptization(...)` on an incomplete run would produce a JSON without a valid solution, defeating the purpose of the warm-start variant.

### Reusable helper method

The `WithoutSolution` example exposes `jsonFromOptimization(IOptimization opti)` as a `public static` helper. This is the recommended entry point if you want to integrate the serialization bridge into your own code. It encapsulates the `JSONConfig` construction and `JSONOptimization` calls behind a clean single-method API.

---

## End-to-end workflow recipes

### Recipe A — Debug via Swagger UI

1. Construct your problem in Java.
2. Call `jsonFromOptimization(myOpti)` (from `CreateRestTourOptimizerInputWithoutSolutionExample`).
3. Copy the printed JSON.
4. Open [https://touroptimizer.dna-evolutions.com](https://touroptimizer.dna-evolutions.com) or your local Swagger UI.
5. Paste into `/api/optimize/config/runOnlyResult` and execute.

### Recipe B — Java pre-solver → REST refinement

1. Run the optimization locally using `CreateRestTourOptimizerInputWithSolutionExample`.
2. Capture the printed JSON (which contains the local solution).
3. Submit to the REST server at `/api/optimize/config/run` with a longer timeout to continue improving.

### Recipe C — CI/CD integration test fixture

1. Use `jsonFromOptimization(myOpti)` in a test setup method to produce a deterministic JSON payload.
2. Store the output as a test fixture file.
3. Use the fixture in REST integration tests against a locally running TourOptimizer container.

---

## References

### REST TourOptimizer
- [REST TourOptimizer documentation](https://www.dna-evolutions.com/docs/learn-and-explore/rest/rest-server-touroptimizer)
- [Docker REST TourOptimizer repository](https://github.com/DNA-Evolutions/Docker-REST-TourOptimizer)
- [Swagger / OpenAPI schema](https://swagger.dna-evolutions.com/v3/api-docs)

### REST Clients
- [Java REST Client Examples](https://github.com/DNA-Evolutions/Java-REST-Client-Examples)
- [Python REST Client Examples](https://github.com/DNA-Evolutions/Python-REST-Client-Examples)
- [C# REST Client Examples](https://github.com/DNA-Evolutions/C-Sharp-REST-Client-Examples)

### Related examples
- [Load JSON and run locally](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/restful/readinput/ReadJsonConfigAndRunExample.java)
- [Builder pattern for immutable config construction](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/builderpattern/BuilderPatternExample.java)
- [Load and save optimization snapshots](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic/io_03)

---

## Agreement

For reading our license agreement and for further information about license plans, please visit [www.dna-evolutions.com](https://www.dna-evolutions.com).

---

## Authors

A product by [dna-evolutions](https://www.dna-evolutions.com) &copy;
