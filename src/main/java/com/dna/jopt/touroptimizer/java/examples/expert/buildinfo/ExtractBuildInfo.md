# Extract Build Info — Diagnostics, Reproducibility, and Support-Ready Bug Reports

When you run JOpt.TourOptimizer in production (services, batch pipelines, planning UIs), you will eventually need to answer questions like:

- “Which exact JOpt build is running here?”
- “Are we on the expected release candidate or hotfix build?”
- “Did we deploy a build from a dirty Git state?”
- “Are we reproducing an issue on the same artifact that support uses?”

The **build info extraction** feature is designed to make these answers trivial and verifiable.

It exposes build and dependency metadata (version number, git commit time, tag, build time, and more) as a `Properties` structure that you can print, attach to logs, or include in telemetry.

---

## References

- Example source: [ExtractBuildInfoExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/buildinfo/ExtractBuildInfoExample.java)
---

## What the example does

The example executes three core steps:

1. Configure the license (so the optimizer can initialize cleanly).
2. Call `getCoreVersionProperties()` to obtain build metadata.
3. Print all property entries.

In the source, this is essentially:

- `Properties coreVersionProperties = this.getCoreVersionProperties();`
- print key/value pairs to stdout

The output contains a rich set of keys, for example (excerpt shown in the source comment):
- `jopt.core.jopt.version.number`
- `jopt.core.git.commit.time`
- `jopt.core.git.build.time`
- `jopt.core.git.closest.tag.name`
- `jopt.core.git.dirty`
- plus versions of bundled dependencies

---

## Why this is helpful (practical scenarios)

### 1) Faster support and troubleshooting
When reporting an issue, the single most useful context is:
- the exact library build + commit metadata.

Instead of “we run v7.x”, you can provide:
- exact JOpt version string,
- commit time,
- tag,
- and dirty/clean state.

This removes ambiguity and accelerates root cause analysis.

### 2) Reproducibility and experiment traceability
If you run optimizations as part of:
- analytics pipelines,
- scientific experiments,
- or A/B deployments,
you can store build info next to:
- run inputs,
- run outputs,
- and performance metrics.

This ensures you can later answer:
- “Which exact solver build produced this result?”

### 3) Deployment verification
In CI/CD environments, it is easy to accidentally deploy:
- a wrong branch build,
- a locally built artifact,
- or a hotfix build into the wrong environment.

By emitting build info at startup (or per job), you can:
- verify the expected artifact is running in staging/prod,
- detect “shadow” or “drift” deployments.

### 4) Debugging “it works on machine A but not on machine B”
If two environments show different behavior, build info helps you quickly confirm whether:
- they run the same JOpt build,
- or differ by a subtle dependency shift.

---

## Recommended integration patterns

### Pattern A — Print once at startup
For services, log the build info once during startup:
- it becomes part of your standard “boot banner”.

### Pattern B — Add build info to optimization result records
If you persist optimization results (database, S3, report files), include:
- version number,
- commit time/tag,
- and build time.

This is extremely helpful when you need to compare outcomes across releases.

### Pattern C — Expose build info via a health endpoint
If you operate the optimizer as a service, expose build info as part of:
- `/health` or `/version` endpoint.

This improves operational transparency for:
- DevOps,
- support,
- and customer success.

---

## Security and privacy note

Build info may include:
- build host IDs,
- user name metadata,
- repository state details.

If you expose build info externally (public endpoint), consider:
- limiting keys to a safe subset (version, tag, build time),
- or restricting access to authenticated operators.

For internal logs, the full set is typically acceptable.

---

## Summary

- `getCoreVersionProperties()` returns a `Properties` map containing JOpt build and dependency metadata.
- This information is valuable for:
  - support-ready bug reports,
  - reproducibility,
  - deployment verification,
  - and operational diagnostics.
- The example prints the full property set, which you can also persist or export into your telemetry system.
