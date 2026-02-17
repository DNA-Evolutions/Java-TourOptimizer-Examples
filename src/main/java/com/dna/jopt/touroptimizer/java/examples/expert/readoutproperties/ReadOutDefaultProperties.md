# Read Out Default Properties — Discover, Audit, and Explain the Solver Configuration Surface

JOpt exposes a large set of optimization properties controlling:
- algorithms and phases,
- weights and cost shaping,
- AutoFilter behavior,
- performance and parallelism,
- construction and repair strategies,
- continuous optimization behavior,
- and many additional internal tuning knobs.

In production, the ability to **read out the full default property catalog** is valuable for:

- configuration audits (“what are the defaults in this version?”),
- reproducibility (“which defaults changed between releases?”),
- debugging (“which property might explain this behavior?”),
- building user-facing “advanced settings” UIs,
- and support cases (providing a canonical list of valid keys and allowed values).

This example shows how to enumerate the default property definitions programmatically via the property provider.

---

## References

- Example source: [ReadOutDefaultPropertiesExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/readoutproperties/ReadOutDefaultPropertiesExample.java)

Additional reference list and descriptions:
- https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/zonecrossing

---

## What the example does

The example creates a “dummy” optimization instance and reads its property catalog:

```java
IOptimization myDummyOptimization = new Optimization();
```

Then it:

1. Retrieves all property definitions from the provider:
   - `getPropertyProvider().getPropertyItems()`

2. Sorts them by category:
   - `sorted((o1, o2) -> Integer.compare(o1.getCategoryIdent(), o2.getCategoryIdent()))`

3. Prints a structured view of each property item:
   - Category name
   - Description
   - Default key (validated property name)
   - Default value
   - Allowed values

Additionally, it explicitly flags deprecated property items by printing:

- `-== DEPRECATED PROPERTY ==-`

when `categoryIdent == CATEGORY_OPTIMIZATION_DEPRECATED`.

---

## Understanding what you are reading: `PropertyItem`

Each `PropertyItem` is a **schema entry** that includes:

- **Category**: where the property conceptually belongs (general setup, weights, AutoFilter, …)
- **Description**: textual documentation embedded in the library
- **Key**: the canonical property key you can set (e.g., `JOpt.NumCPUCores`)
- **Default value**: what the library uses if you do not set anything
- **Allowed values**: an optional validation set (empty means “free-form” or validated differently)
- **Deprecated category flag**: used to mark keys you should avoid using in new projects

This matters because it is more than “a map of defaults”:
- it is a validated *property surface definition*.

---

## Category mapping used by the example

The example creates a local mapping list of categories, including:

- `CATEGORY_OPTIMIZATION_GENERAL_SETUP`
- `CATEGORY_OPTIMIZATION_CONSTRUCTION`
- `CATEGORY_OPTIMIZATION_PRE_OPTIMIZATION_SETUP`
- `CATEGORY_OPTIMIZATION_GENETIC_SETUP`
- `CATEGORY_OPTIMIZATION_2OPT`
- `CATEGORY_OPTIMIZATION_WEIGHTS`
- `CATEGORY_OPTIMIZATION_AUTOFILTER`
- `CATEGORY_OPTIMIZATION_CO_SETUP`
- `CATEGORY_OPTIMIZATION_DEPRECATED`
- `CATEGORY_INJECTION`

This list is used to print a human-readable category name for each property item.

Operationally, categories are helpful for:
- building UI sections (General / Algorithms / Weights / AutoFilter / …),
- filtering what you expose to end users,
- and organizing tuning discussions.

---

## Why reading default properties is helpful (high-impact use cases)

### 1) Reproducibility across versions
Solver upgrades can change default behaviors.  
By exporting the full default property catalog for a version, you can:
- diff two catalogs across versions,
- identify exactly which defaults changed,
- and decide which properties you want to pin explicitly.

This is a best practice for regulated or audit-heavy environments.

### 2) Support-ready bug reports
When reporting an issue, providing:
- the property keys you set,
- and the solver default list (or at least the relevant category subset)
greatly reduces ambiguity.

This is particularly important when:
- a behavior is caused by an unexpected default rather than your explicit configuration.

### 3) Building “advanced settings” UI safely
If you build customer-facing configuration panels, you want:
- canonical keys,
- allowed value lists,
- and descriptions.

The property provider output is an ideal “source of truth” for:
- auto-generating UI controls,
- validating user input,
- and displaying inline help text.

### 4) Internal tuning and benchmarking
Property catalogs enable systematic experiments:
- choose a small set of candidate properties,
- vary them in controlled sweeps,
- store results with the exact property set.

This is much faster than hunting through documentation manually.

---

## Recommended integration patterns

### Pattern A — Export catalog at startup (service mode)
On service startup, export:
- the property catalog (or selected categories) to logs or a `/version` endpoint.

This helps operations answer:
- “what knobs exist in this build?”

### Pattern B — Create a “property snapshot” per optimization run
Store:
- all explicitly set properties,
- and optionally the solver defaults (or at least a version identifier).

This makes it easy to reconstruct why a result looked the way it did.

### Pattern C — Build a property diff tool (highly recommended)
Capture default catalogs per release, then diff them:
- highlight new properties,
- highlight removed/deprecated properties,
- highlight default value changes.

This prevents “silent behavior changes” during upgrades.

---

## Deprecation handling

The example highlights deprecated keys in output. In real projects:

- avoid setting deprecated keys in new code,
- migrate to replacement keys where applicable,
- keep a compatibility layer only if required by legacy configurations.

A property-dump is also a good way to validate whether your current config still uses deprecated settings.

---

## Summary

- JOpt exposes a schema-driven catalog of optimization properties through `getPropertyProvider().getPropertyItems()`.
- This example prints each property’s category, description, key, default value, and allowed values, and flags deprecated entries.
- Reading out default properties is a high-leverage capability for:
  - auditing,
  - debugging,
  - reproducibility,
  - support,
  - and generating safe configuration UIs.
