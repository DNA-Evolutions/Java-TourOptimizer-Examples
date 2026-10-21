# Build information of the JOpt core

`ExtractBuildInfoExample` reads the build properties of the JOpt core library with `getCoreVersionProperties()` and prints two of them: the version number and the build time. The page is for developers who need to tell which core build runs in a service, a batch job or a support case. The example runs no optimization and needs no license.

## What the example shows

- **The build properties:** one call of `getCoreVersionProperties()` on an `Optimization` is enough to obtain build metadata: a `Properties` object that the core library reads from its own jar.
- **Two keys:** the example prints `jopt.core.jopt.version.number` and `jopt.core.build.time.utc`, which identify the core build in a log or a support request.
- **No run, no license:** a new `Optimization` is enough; the example starts no run and sets no license.

## Scenario

There is no optimization model: no nodes, no resources and no run. The example creates an `Optimization` only to read the build properties of the core library that the examples pom references.

## How the code works

The source is [ExtractBuildInfoExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/buildinfo/ExtractBuildInfoExample.java). `main(...)` instantiates the class and calls `example()`, which creates the optimization and prints the build information:

```java
// A new optimization is enough: the build properties come from the core library, and nothing
// runs.
IOptimization opti = new Optimization();
printBuildInfo(opti);
```

`printBuildInfo` reads the properties and prints the two keys of `PRINTED_KEYS`:

```java
Properties coreVersionProperties = opti.getCoreVersionProperties();
// The properties hold more keys. The version number and the build time identify the core
// build in a log or in a support request.
for (String key : PRINTED_KEYS) {
  System.out.println(key + " = " + coreVersionProperties.getProperty(key));
}
```

## Output and what to look for

The output was the same in four test runs: two lines, `jopt.core.jopt.version.number = ` followed by the version of the core library that the examples pom references, and `jopt.core.build.time.utc = ` followed by the build time of that library in UTC. The values change with each core release, so this page does not show them.

Every optimization run also prints a short form of the build information as a status event (`OPTIMIZATION_VERSION`, "Core lib version"), with the closest Git tag and the build time among others. The run output of the other examples shows it at the start.

## What the build information answers

When you run JOpt.TourOptimizer in production (services, batch pipelines, planning UIs), you will eventually need to answer questions like:

- "Which exact JOpt build is running here?"
- "Are we on the expected release candidate or hotfix build?"
- "Did we deploy a build from a dirty Git state?"
- "Are we reproducing an issue on the same artifact that support uses?"

The **build info extraction** feature is designed to make these answers verifiable.

It exposes build and dependency metadata (version number, git commit time, tag, build time, and more) as a `Properties` structure that you can print, attach to logs, or include in telemetry.

## What the properties contain

In the source, this is essentially:

- `Properties coreVersionProperties = opti.getCoreVersionProperties();`
- print key/value pairs to stdout

The full property set contains a set of keys (the output of the example shows two of them), for example:
- `jopt.core.jopt.version.number`
- `jopt.core.git.commit.time`
- `jopt.core.git.build.time`
- `jopt.core.git.closest.tag.name`
- `jopt.core.git.dirty`
- plus the versions of the libraries and build plugins of the core build

The example prints only the first key and `jopt.core.build.time.utc`. Reading the properties needs no license and starts no run.

## Why this is helpful (practical scenarios)

### Faster support and troubleshooting
When reporting an issue, the single most useful context is:
- the exact library build + commit metadata.

Instead of "we run v7.x", you can provide:
- exact JOpt version string,
- commit time,
- tag,
- and dirty/clean state.

This removes ambiguity and accelerates root cause analysis.

### Reproducibility and experiment traceability
If you run optimizations as part of:
- analytics pipelines,
- scientific experiments,
- or A/B deployments,
you can store build info next to:
- run inputs,
- run outputs,
- and performance metrics.

This ensures you can later answer:
- "Which exact solver build produced this result?"

### Deployment verification
In CI/CD environments, it is easy to accidentally deploy:
- a wrong branch build,
- a locally built artifact,
- or a hotfix build into the wrong environment.

By emitting build info at startup (or per job), you can:
- verify the expected artifact is running in staging/prod,
- detect "shadow" or "drift" deployments.

### Debugging "it works on machine A but not on machine B"
If two environments show different behavior, build info helps you quickly confirm whether:
- they run the same JOpt build,
- or differ by a subtle dependency shift.

## Recommended integration patterns

### Pattern A: Print once at startup
For services, log the build info once during startup:
- it becomes part of your standard "boot banner".

### Pattern B: Add build info to optimization result records
If you persist optimization results (database, S3, report files), include:
- version number,
- commit time/tag,
- and build time.

This is helpful when you need to compare outcomes across releases.

### Pattern C: Expose build info via a health endpoint
If you operate the optimizer as a service, expose build info as part of:
- `/health` or `/version` endpoint.

This improves operational transparency for:
- DevOps,
- support,
- and customer success.

## Security and privacy note

Build info may include:
- build host IDs,
- user name metadata,
- repository state details.

If you expose build info externally (public endpoint), consider:
- limiting keys to a safe subset (version, tag, build time),
- or restricting access to authenticated operators.

The example follows the first advice: it prints the version number and the build time only. The JSON export of an optimization carries the core's build properties too, as `coreBuildOptions`, so the same care applies when you pass a JSON export on.

For internal logs, the full set is typically acceptable.

## Summary

- `getCoreVersionProperties()` returns a `Properties` map containing JOpt build and dependency metadata.
- This information is valuable for:
  - support-ready bug reports,
  - reproducibility,
  - deployment verification,
  - and operational diagnostics.
- The example prints the version number and the build time, not the full property set, which you can also persist or export into your telemetry system.

## Run it

Run the `main(String[] args)` method of `ExtractBuildInfoExample` from your IDE, with the repository imported as a Maven project. You need a JDK in the version that the examples pom sets. You need no license: the example runs no optimization.

## Related

- [Read out the default properties](../readoutproperties/ReadOutDefaultProperties.md): the other expert example that reads the core without a run, here its property catalog.
- [Recommended ways to run an optimization](../../basic/recommendedimplementation_02/RecommendedImplementation.md): the recommended run pattern, a synchronous run and the legacy callback methods.
- [Expert examples](../README.md): the overview of all Expert examples.

---

Added: 2026-02-17 (JOpt core 7.5.3-j17) | Last updated: 2026-10-21
