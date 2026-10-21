# Read out the default properties

`ReadOutDefaultPropertiesExample` prints the property catalog of the JOpt core: for each property its category, description, key, default value and allowed values, sorted by category, with a mark above each deprecated property. The page is for developers who configure runs with properties, build settings screens or compare core releases. The example runs no optimization and needs no license; the [Optimization properties](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties) reference describes the properties themselves.

## What the example shows

- **The catalog of a new optimization:** `getPropertyProvider().getPropertyItems()` returns one `PropertyItem` per property, with the values that the core uses when you set nothing.
- **The fields of an item:** `getCategoryIdent()`, `getDescription()`, `getValidatedPropertyName()`, `getDefaultValue()` and `getAllowedValues()`.
- **Categories by number:** `getCategoryIdent()` returns a number; the constants of `PropertyItem` give the numbers their names, and the example's list `CATEGORIES` holds the names in that order.
- **Deprecated properties:** the items of `PropertyItem.CATEGORY_OPTIMIZATION_DEPRECATED` get the line `-== DEPRECATED PROPERTY ==-`.

## Scenario

There is no optimization model: no nodes, no resources and no run. The example creates an `Optimization` only to read its property catalog.

## How the code works

The source is [ReadOutDefaultPropertiesExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/readoutproperties/ReadOutDefaultPropertiesExample.java). It is a plain class: it does not extend `Optimization`, because it runs nothing. `main(...)` instantiates the class and calls `example()`, which creates the optimization and passes it to `printPropertyItems`. `printPropertyItems` sorts the items by category and prints each one:

```java
System.out.print(
    "\nCategory: "
        + CATEGORIES.get(i.getCategoryIdent())
        + "\nDescription: "
        + i.getDescription()
        + "\nDefault Key: "
        + i.getValidatedPropertyName()
        + "\nDefault Value: "
        + i.getDefaultValue()
        + "\nAllowed Values: "
        + i.getAllowedValues()
        + "\n");
```

## Output and what to look for

The output was the same in four test runs. It starts with the only property of `CATEGORY_MISC`:

```text
Category: CATEGORY_MISC
Description: TRUE: Post Step Manager debug mode.
Default Key: JOpt.EntityValidation.Debug
Default Value: FALSE
Allowed Values: [TRUE, FALSE]
```

The deprecated properties come near the end, each below the mark (trimmed to the first one):

```text
-== DEPRECATED PROPERTY ==-
Category: CATEGORY_OPTIMIZATION_DEPRECATED
Description: Property for enabling and disabling the replace-functionality of wrong properties of the propertyChecker.
Default Key: JOpt.PropertyChecker.ReplaceWrongStatements
Default Value: TRUE
Allowed Values: [TRUE, FALSE]
```

The values belong to the core version that the examples pom references; the property reference is the place for the defaults of a release. Look also for:

- `Description: No description available` where a property has no description text.
- `Allowed Values: []` where a property has no list of allowed values; a numeric property can have a range instead (see "Understanding what you are reading" below).
- No block for `CATEGORY_SYSTEM` and `CATEGORY_OPTIMIZATION_2OPT`: the catalog holds no property of these two categories.

## Why the property catalog matters

JOpt exposes a large set of optimization properties controlling:
- algorithms and phases,
- weights and cost shaping,
- AutoFilter behavior,
- performance and parallelism,
- construction and repair strategies,
- continuous optimization behavior,
- and many additional internal tuning knobs.

In production, the ability to **read out the full default property catalog** is valuable for:

- configuration audits ("what are the defaults in this version?"),
- reproducibility ("which defaults changed between releases?"),
- debugging ("which property might explain this behavior?"),
- building user-facing "advanced settings" UIs,
- and support cases (providing a canonical list of valid keys and allowed values).

This example shows how to enumerate the default property definitions programmatically via the property provider.

Additional reference list and descriptions: the [Optimization properties](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties) reference.

## What the example does

The example creates a "dummy" optimization instance and reads its property catalog:

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

## Understanding what you are reading: `PropertyItem`

Each `PropertyItem` is a **schema entry** that includes:

- **Category**: where the property conceptually belongs (general setup, weights, AutoFilter, ...)
- **Description**: textual documentation embedded in the library
- **Key**: the canonical property key you can set (e.g., `JOpt.NumCPUCores`)
- **Default value**: what the library uses if you do not set anything
- **Allowed values**: an optional validation set (empty means "free-form" or validated differently)
- **Deprecated category flag**: used to mark keys you should avoid using in new projects

"Validated differently" can be read from the item as well: `getExpectedValueType()` gives the expected type, and `hasIntRange()` with `getIntMin()` and `getIntMax()`, or `hasDoubleRange()` with `getDoubleMin()` and `getDoubleMax()`, give a range. `isHidden()` tells whether a property is marked as hidden; the catalog contains the hidden properties too, and the example prints them without a mark.

This matters because it is more than "a map of defaults":
- it is a validated *property surface definition*.

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

This list is used to print a human-readable category name for each property item. It also holds `CATEGORY_MISC` and `CATEGORY_SYSTEM`, the categories 0 and 1, so that the position of each name in the list matches the number of its category.

Operationally, categories are helpful for:
- building UI sections (General / Algorithms / Weights / AutoFilter / ...),
- filtering what you expose to end users,
- and organizing tuning discussions.

## Why reading default properties is helpful (use cases)

### Reproducibility across versions
Solver upgrades can change default behaviors.  
By exporting the full default property catalog for a version, you can:
- diff two catalogs across versions,
- identify exactly which defaults changed,
- and decide which properties you want to pin explicitly.

This is a best practice for regulated or audit-heavy environments.

### Support-ready bug reports
When reporting an issue, providing:
- the property keys you set,
- and the solver default list (or at least the relevant category subset)
greatly reduces ambiguity.

This is particularly important when:
- a behavior is caused by an unexpected default rather than your explicit configuration.

### Building "advanced settings" UI safely
If you build customer-facing configuration panels, you want:
- canonical keys,
- allowed value lists,
- and descriptions.

The property provider output is a "source of truth" for:
- auto-generating UI controls,
- validating user input,
- and displaying inline help text.

Some properties have no description, and hidden properties are in the list too, so a settings screen needs a filter of its own.

### Internal tuning and benchmarking
Property catalogs enable systematic experiments:
- choose a small set of candidate properties,
- vary them in controlled sweeps,
- store results with the exact property set.

This is much faster than hunting through documentation manually.

## Recommended integration patterns

### Pattern A: Export catalog at startup (service mode)
On service startup, export:
- the property catalog (or selected categories) to logs or a `/version` endpoint.

This helps operations answer:
- "what knobs exist in this build?"

### Pattern B: Create a "property snapshot" per optimization run
Store:
- all explicitly set properties,
- and optionally the solver defaults (or at least a version identifier).

This makes it easy to reconstruct why a result looked the way it did.

### Pattern C: Build a property diff tool
Capture default catalogs per release, then diff them:
- highlight new properties,
- highlight removed/deprecated properties,
- highlight default value changes.

This prevents "silent behavior changes" during upgrades.

## Deprecation handling

The example highlights deprecated keys in output. In real projects:

- avoid setting deprecated keys in new code,
- migrate to replacement keys where applicable,
- keep a compatibility layer only if required by legacy configurations.

A property-dump is also a good way to validate whether your current config still uses deprecated settings.

## Summary

- JOpt exposes a schema-driven catalog of optimization properties through `getPropertyProvider().getPropertyItems()`.
- This example prints each property's category, description, key, default value, and allowed values, and flags deprecated entries.
- Reading out default properties is a capability that supports:
  - auditing,
  - debugging,
  - reproducibility,
  - support,
  - and generating safe configuration UIs.

## Run it

Run the `main(String[] args)` method of `ReadOutDefaultPropertiesExample` from your IDE, with the repository imported as a Maven project. You need a JDK in the version that the examples pom sets. You need no license: the example runs no optimization.

## Related

- [Optimization properties](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties): the property reference, with the defaults and the allowed values by category.
- [Build information of the JOpt core](../buildinfo/ExtractBuildInfo.md): the other expert example that reads the core without a run, here its version number and build time.
- [Recommended ways to run an optimization](../../basic/recommendedimplementation_02/RecommendedImplementation.md): the recommended run pattern, a synchronous run and the legacy callback methods.
- [Expert examples](../README.md): the overview of all Expert examples.

---

Added: 2026-02-17 (JOpt core 7.5.3-j17) | Last updated: 2026-10-21
