# Reading a REST JSON Config and Running It Locally — JOpt TourOptimizer

This document explains how to take a **REST-compatible JSON payload** — as produced by the JOpt.TourOptimizer REST server or by the Java serialization bridge — and execute it **locally** using the JOpt core library.

This is the complement to the [create REST input examples](https://www.dna-evolutions.com/docs/learn-and-explore/rest-examples/createresttouroptimizerinput): where those examples convert a Java optimization into JSON, this example takes JSON and runs it as a local optimization.

**Sources (GitHub):**
- [ReadJsonConfigAndRunExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/restful/readinput/ReadJsonConfigAndRunExample.java)
- [JSONInputProvider.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/restful/readinput/JSONInputProvider.java)

---

## Overview

- [Why run JSON locally?](#why-run-json-locally)
- [How it works (high level)](#how-it-works-high-level)
- [Key classes and their roles](#key-classes-and-their-roles)
- [Step-by-step walkthrough](#step-by-step-walkthrough)
- [The JSONInputProvider: embedded test payload](#the-jsoninputprovider-embedded-test-payload)
- [JSON payload structure explained](#json-payload-structure-explained)
- [Modifying the config before running](#modifying-the-config-before-running)
- [Implementation notes](#implementation-notes)
- [End-to-end workflow recipes](#end-to-end-workflow-recipes)

---

## Why run JSON locally?

The JOpt.TourOptimizer REST server accepts a JSON payload and returns an optimized result. However, there are many situations where you want to **replay, test, or debug** that same JSON payload using the local JOpt core library directly:

- **Local debugging**: reproduce a REST-server run exactly on your machine without needing a running REST server instance.
- **Integration testing**: load a known JSON fixture and verify the optimization result in a unit or integration test.
- **Config inspection**: deserialize a JSON payload, inspect or modify its structure programmatically, then re-run it.
- **Offline development**: develop and test optimization logic without a network connection to a REST server.
- **Round-trip validation**: confirm that the JSON produced by the serialization bridge (see `CreateRestTourOptimizerInputWithoutSolutionExample`) produces the same result when run locally.

---

## How it works (high level)

The flow is straightforward:

```
JSON string
    → RestOptimization  (deserialization via ObjectMapper)
    → OptimizationConfig<JSONConfig>  (via opti.asConfig())
    → [optional: modify the config immutably]
    → JSONOptimization.startAsynchConfigFuture(config)
    → OptimizationConfig<JSONConfig> result
    → JSONOptimization.asJSON(result)  (print or use)
```

The key insight is that `RestOptimization` is a wrapper around `OptimizationConfig<JSONConfig>`. Once you have a config object, the local `JSONOptimization` engine can run it identically to how the REST server would.

---

## Key classes and their roles

| Class / Interface | Role |
|---|---|
| `JSONInputProvider` | Holds the embedded JSON test payload as a `static final String` constant |
| `RestOptimization` | Deserialized form of the JSON; wraps and extends `OptimizationConfig` |
| `ConfigSerialization.objectMapper()` | Jackson `ObjectMapper` preconfigured for JOpt JSON structures |
| `OptimizationConfig<JSONConfig>` | Immutable config object accepted by the `JSONOptimization` engine |
| `IJSONOptimization` / `JSONOptimization` | The local engine that runs configs and is fully compatible with the REST server's logic |
| `CompletableFuture<OptimizationConfig<JSONConfig>>` | Async result handle; call `.get()` to block until the run completes |

---

## Step-by-step walkthrough

### 1) Load the JSON string

The example uses the embedded constant from `JSONInputProvider`:

```java
String myInput = JSONInputProvider.JSON_INOUT_WITHOUT_SOLUTION;
```

In practice, replace this with a string loaded from a file, database, message queue, or HTTP response body — any source that provides a valid JOpt REST JSON payload.

---

### 2) Deserialize to `RestOptimization`

```java
RestOptimization opti = ReadJsonConfigAndRunExample.jsonToRestOptimization(
        myInput,
        ConfigSerialization.objectMapper()
);
```

The static helper wraps a standard Jackson deserialization:

```java
public static RestOptimization jsonToRestOptimization(String src, ObjectMapper mapper)
        throws IOException {
    return mapper.readValue(src, new TypeReference<RestOptimization>() {});
}
```

`RestOptimization` is a typed wrapper that extends `OptimizationConfig` and adds REST-specific metadata. It knows how to convert itself into the immutable config that the local engine requires.

---

### 3) Create the engine and attach observables

```java
IJSONOptimization myOpti = new JSONOptimization();
attachToObservables(myOpti);
```

Attaching to observables is optional but strongly recommended — it gives you visibility into the run as it progresses:

```java
private static void attachToObservables(IOptimization opti) {
    opti.getOptimizationEvents().progressSubject().subscribe(p -> System.out.println(p.getProgressString()));
    opti.getOptimizationEvents().warningSubject().subscribe(w -> System.out.println(w.toString()));
    opti.getOptimizationEvents().statusSubject().subscribe(s -> System.out.println(s.toString()));
    opti.getOptimizationEvents().errorSubject().subscribe(e -> System.out.println(e.toString()));
}
```

These four subjects cover the full lifecycle: progress ticks, warning events, status transitions, and error events.

---

### 4) Convert to `OptimizationConfig` and optionally modify

```java
OptimizationConfig<JSONConfig> config = opti.asConfig();
config = dummyModify(config);
```

`asConfig()` extracts the immutable config from the `RestOptimization` wrapper. From this point, the config is a standard `OptimizationConfig<JSONConfig>` object that can be inspected and — because it is immutable — modified only by creating a new copy with updated fields:

```java
public static OptimizationConfig<JSONConfig> dummyModify(OptimizationConfig<JSONConfig> config) {
    return config.withIdent("MyNewModifiedIdent");
}
```

The `withIdent(...)` call (and similar `with*` methods) produce a new config object with the specified field changed, leaving all other fields intact. This is the standard pattern for making targeted changes to a deserialized payload without re-building the entire config from scratch.

---

### 5) Run the optimization

```java
CompletableFuture<OptimizationConfig<JSONConfig>> resultFuture =
        myOpti.startAsynchConfigFuture(config, Optional.empty());

OptimizationConfig<JSONConfig> result = resultFuture.get();
```

`startAsynchConfigFuture` starts the optimization engine asynchronously. `resultFuture.get()` blocks the calling thread until the run finishes. The returned `result` is a new `OptimizationConfig` that contains the solution embedded inside it.

---

### 6) Print or use the result

```java
System.out.println(JSONOptimization.asJSON(result, true));
```

`JSONOptimization.asJSON(result, true)` serializes the result (including the solution) back to a JSON string. The boolean flag controls pretty-printing. This output can be:

- printed to stdout for inspection,
- stored as a fixture for tests,
- submitted to the REST server as a warm-start payload (since it now includes a solution).

---

## The JSONInputProvider: embedded test payload

`JSONInputProvider` is a utility class that holds a **hardcoded JSON string** representing a complete, self-contained optimization input. It mirrors the same seven-city problem (Koeln, Essen, Dueren, Nuernberg, Heilbronn, Wuppertal, Aachen) and one resource ("Jack from Aachen") used in the `CreateRest*` examples — making the two example families directly comparable.

The constant is named `JSON_INOUT_WITHOUT_SOLUTION`, reflecting that the payload carries the problem definition only (no pre-existing solution). The embedded status field confirms this:

```json
"optimizationStatus": {
  "statusDescription": "SUCCESS_WITHOUT_SOLUTION",
  "status": "SUCCESS_WITHOUT_SOLUTION"
}
```

Using an embedded constant makes the example **self-contained and runnable with zero setup** — no files, no REST server, no external dependencies. For production code, replace this constant with your own JSON source.

---

## JSON payload structure explained

The JSON accepted by this example follows the standard JOpt REST schema. The key top-level fields are:

| Field | Description |
|---|---|
| `optimizationStatus` | Status metadata from when the JSON was created (not the run you are about to start) |
| `createdTimeStamp` | Unix timestamp of when the payload was generated |
| `ident` | Human-readable identifier for this optimization run |
| `nodes` | Array of visit nodes, each with `id`, geo `position`, `openingHours`, `visitDuration`, and `priority` |
| `resources` | Array of resources (vehicles/employees), each with `id`, `position`, `workingHours`, `maxTime`, and `maxDistance` |
| `extension` | REST-specific metadata: `keySetting` (license key) and `timeOut` (maximum optimization duration) |

A minimal node entry looks like this:

```json
{
  "id": "Koeln",
  "type": {
    "position": { "latitude": 50.9333, "longitude": 6.95, "locationId": "Koeln" },
    "typeName": "Geo"
  },
  "openingHours": [
    { "begin": "2020-05-06T06:00:00Z", "end": "2020-05-06T15:00:00Z", "zoneId": "Europe/Berlin" },
    { "begin": "2020-05-07T06:00:00Z", "end": "2020-05-07T15:00:00Z", "zoneId": "Europe/Berlin" }
  ],
  "visitDuration": "PT20M",
  "priority": 1
}
```

A minimal resource entry looks like this:

```json
{
  "id": "Jack from Aachen",
  "type": { "typeName": "Capacity" },
  "position": { "latitude": 50.775346, "longitude": 6.083887, "locationId": "Jack from Aachen" },
  "workingHours": [
    { "begin": "2020-05-06T06:00:00Z", "end": "2020-05-06T15:00:00Z", "zoneId": "Europe/Berlin", "isAvailableForStay": false }
  ],
  "maxTime": "PT9H",
  "maxDistance": "1200.0 km"
}
```

Durations follow the ISO-8601 duration format: `PT20M` = 20 minutes, `PT9H` = 9 hours, `PT10M` = 10 minutes.

---

## Modifying the config before running

Because `OptimizationConfig` is immutable, all modifications produce a new copy. The `with*` method family provides surgical updates without rebuilding from scratch:

| Method | Effect |
|---|---|
| `config.withIdent("newId")` | Changes the run identifier |
| `config.withSolution(Optional.empty())` | Strips an embedded solution (forces a fresh run) |
| `config.withExtension(Optional.of(newExt))` | Replaces the `JSONConfig` extension (e.g., new timeout or license) |
| `config.withElementConnections(new ArrayList<>())` | Removes cached node-connection data |

This pattern is useful when you receive a JSON payload from an external system and need to make targeted adjustments (override the timeout, inject a different license, clear a stale solution) before running.

---

## Implementation notes

### `RestOptimization` vs `OptimizationConfig`

`RestOptimization` is specifically designed to mirror the JSON structure produced and consumed by the REST server. It carries the same fields as `OptimizationConfig<JSONConfig>` but adds REST-specific lifecycle metadata (creation timestamp, creator, status). Once you call `.asConfig()`, you get a plain `OptimizationConfig` that the local engine understands natively.

### `ConfigSerialization.objectMapper()`

Do not use a plain `new ObjectMapper()` for deserialization. `ConfigSerialization.objectMapper()` returns a pre-configured instance with all the necessary mixins, modules, and type resolvers registered for JOpt's polymorphic type hierarchy (nodes, resources, conditions, etc.). Using a plain mapper will cause deserialization failures for most real payloads.

### Blocking with `.get()`

The call `resultFuture.get()` blocks indefinitely. In production code, prefer `resultFuture.get(timeout, TimeUnit.MINUTES)` to enforce an upper bound, or use the timeout already embedded in the `JSONConfig` extension of the payload, which the engine honours as a hard stop.

### The public license key

The `JSONInputProvider` constant embeds a **public evaluation license key** (`PUBLIC-bc799ef350fe...`) valid for up to 15 elements. This is sufficient for the example's seven nodes and one resource. For production runs with more elements, replace the `keySetting` in the `extension` block with a valid full license key.

---

## End-to-end workflow recipes

### Recipe A — Local round-trip test

1. Run `CreateRestTourOptimizerInputWithoutSolutionExample` and copy the printed JSON.
2. Paste it into `JSONInputProvider` as a new constant (or load from a file).
3. Run `ReadJsonConfigAndRunExample` with that JSON to verify the local result matches expectations.

### Recipe B — Debug a failed REST server run

1. Capture the JSON payload that was sent to the REST server (from your client code or Swagger UI).
2. Pass it as `myInput` in `ReadJsonConfigAndRunExample`.
3. Attach a debugger and step through the local run to identify the issue.

### Recipe C — Modify and re-run

1. Deserialize a stored JSON payload via `jsonToRestOptimization(...)`.
2. Call `opti.asConfig()` to get the immutable config.
3. Apply targeted changes with `with*` methods (e.g., override the timeout, update the ident).
4. Run via `startAsynchConfigFuture(config, Optional.empty())`.
5. Serialize the result back to JSON with `JSONOptimization.asJSON(result, true)` for storage or forwarding.

### Recipe D — Integration test fixture

1. Store `JSONInputProvider.JSON_INOUT_WITHOUT_SOLUTION` (or a variant) as a `.json` test resource file.
2. Load it in your test `@BeforeEach` or `@BeforeAll` using `Files.readString(...)`.
3. Deserialize and run as shown in this example.
4. Assert on the returned `result` — solution status, number of visited nodes, total distance, etc.

---

## References

### REST TourOptimizer
- [REST TourOptimizer documentation](https://www.dna-evolutions.com/docs/learn-and-explore/rest/rest-server-touroptimizer)
- [Docker REST TourOptimizer repository](https://github.com/DNA-Evolutions/Docker-REST-TourOptimizer)
- [Swagger / OpenAPI schema](https://swagger.dna-evolutions.com/v3/api-docs)

### Related examples
- [Create REST JSON input (without solution)](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/restful/createinput/CreateRestTourOptimizerInputWithoutSolutionExample.java)
- [Create REST JSON input (with solution)](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/restful/createinput/CreateRestTourOptimizerInputWithSolutionExample.java)
- [Builder pattern for immutable config construction](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/builderpattern/BuilderPatternExample.java)
- [Load and save optimization snapshots](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic/io_03)

### REST Clients
- [Java REST Client Examples](https://github.com/DNA-Evolutions/Java-REST-Client-Examples)
- [Python REST Client Examples](https://github.com/DNA-Evolutions/Python-REST-Client-Examples)
- [C# REST Client Examples](https://github.com/DNA-Evolutions/C-Sharp-REST-Client-Examples)

---

## Agreement

For reading our license agreement and for further information about license plans, please visit [www.dna-evolutions.com](https://www.dna-evolutions.com).

---

## Authors

A product by [dna-evolutions](https://www.dna-evolutions.com) &copy;
