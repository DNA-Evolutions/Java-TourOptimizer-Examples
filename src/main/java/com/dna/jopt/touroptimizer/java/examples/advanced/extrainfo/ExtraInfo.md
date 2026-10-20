# Extra info on nodes and resources

`ExtraInfoExample` stores a small Java object as a JSON string in the extra info of two nodes and a resource, runs the optimization and reads the objects back from the result. The page is for developers who keep their own data, such as a contact person or an order number, with the nodes and resources of a plan. The run follows the pattern of the [First optimization example](../../basic/firstoptimization_01/FirstOptimizationExample.md).

## What the example shows

- Every node and every resource has an extra info string. `setExtraInfo(String)` sets it, and `getExtraInfo()` returns it as an `Optional<String>`. JOpt does not use the string in the optimization.
- The example writes a plain Java object into this string as JSON and reads it back after the run. Both steps use `ConfigSerialization.objectMapper()`, a Jackson `ObjectMapper` from the JOpt core (package `com.dna.jopt.config.serialize`).
- The objects come back from the result with the values the code wrote, including the `Instant` of the resource.

## Scenario

Jack from Aachen visits Koeln and Essen on May 6, 2020, from 08:00 to 17:00 (Europe/Berlin); the dates are sample values. Each visit takes 20 minutes. Jack may work 9 hours and drive 1200 km.

The extra info objects:

- Koeln: a `NodeExtraInfo` with the phone number `+00 000 0000001` and the contact person `Mrs. Einstein`.
- Essen: a `NodeExtraInfo` with the phone number `+00 000 0000002` and the contact person `Mrs. Kepler`.
- Jack from Aachen: a `ResourceExtraInfo` with the phone number `+00 000 0000003` and the birthday `1900-01-01T00:00:00Z`.

The phone numbers and the birthday are placeholders, not real data: `+00` is no country code, and the date stands for any birthday. The names are sample values. Real personal data in the extra info would also go into every JSON snapshot of the optimization.

## How the code works

The sources are [ExtraInfoExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/extrainfo/ExtraInfoExample.java), [NodeExtraInfo.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/extrainfo/NodeExtraInfo.java) and [ResourceExtraInfo.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/extrainfo/ResourceExtraInfo.java). `example()` runs these steps:

```java
// The public evaluation license, or your own license if you put a license file where
// ExampleLicenseHelper looks for it.
ExampleLicenseHelper.setLicense(this);
addProperties(this);
addNodes(this);
addResources(this);
attachToObservables(this);
IOptimizationResult result = startAndPresentResult(this);
printExtraInfo(result);
```

`startAndPresentResult` prints the result and returns it; `printExtraInfo` then reads the extra info back from it.

### The two helper classes

`NodeExtraInfo` holds a phone number and a contact person, `ResourceExtraInfo` a phone number and a birthday as an `Instant`. Both are plain Java classes with private fields, a constructor without arguments and getters and setters; Jackson uses them to write and read the JSON. Their `toString()` prints the fields.

### Writing the extra info

`addNodes` writes a `NodeExtraInfo` as a JSON string and sets it on the node. The block shows Koeln; Essen follows the same pattern:

```java
// The phone numbers are placeholders, not real data: +00 is no country code.
INode koeln =
    new TimeWindowGeoNode("Koeln", 50.9333, 6.95, openingHours, visitDuration, importance);
String extraInfoKoeln =
    ConfigSerialization.objectMapper()
        .writeValueAsString(new NodeExtraInfo("+00 000 0000001", "Mrs. Einstein"));
koeln.setExtraInfo(extraInfoKoeln);
opti.addElement(koeln);
```

`addResources` does the same for Jack with a `ResourceExtraInfo`:

```java
// Placeholders, not real data: +00 is no country code, and the date stands for any birthday.
String extraInfoJack =
    ConfigSerialization.objectMapper()
        .writeValueAsString(
            new ResourceExtraInfo("+00 000 0000003", Instant.parse("1900-01-01T00:00:00Z")));
jack.setExtraInfo(extraInfoJack);
opti.addElement(jack);
```

### Reading it back

`printExtraInfo` collects the elements of the routes of the result:

```java
// The elements come from the routes: a node that is on no route is not read back here.
List<IOptimizationElement> elements =
    result.getRoutes().stream()
        .map(ILogicEntityRoute::getAllElements)
        .flatMap(List::stream)
        .distinct()
        .toList();
```

It then reads the extra info of each node back into a `NodeExtraInfo` and prints the id and the object:

```java
for (IOptimizationElement element : elements) {
  Optional<String> extraInfo = element.getExtraInfo();
  if (element instanceof INode && extraInfo.isPresent()) {
    NodeExtraInfo info =
        ConfigSerialization.objectMapper().readValue(extraInfo.get(), NodeExtraInfo.class);
    System.out.println("\n" + element.getId());
    System.out.println(info);
  }
}
```

A second loop does the same for the resources, with `ResourceExtraInfo.class`.

## Output and what to look for

The output shows the events, then the result with one route: Jack visits Koeln and then Essen. After the result comes the extra info block, the same in two test runs:

```text
==== EXTRA INFO =====

Koeln
Phone: +00 000 0000001 / Contact: Mrs. Einstein

Essen
Phone: +00 000 0000002 / Contact: Mrs. Kepler
========

Jack from Aachen
Phone: +00 000 0000003 / Birthday: 1900-01-01T00:00:00Z
```

Each object prints the values that the code wrote before the run. The birthday comes back as the same `Instant`, so a field type beyond strings survives the JSON string and the run.

## Run it

Run the `main(String[] args)` method of `ExtraInfoExample` from your IDE, with the repository imported as a Maven project. You need a JDK in the version that the examples pom sets. No license file is needed: without one, the example uses the public evaluation license (see `ExampleLicenseHelper`). The [license guide: element limits](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/license#free-license) lists what each license allows.

## Related

- [First optimization example](../../basic/firstoptimization_01/FirstOptimizationExample.md): the run pattern that this example follows.
- [Load and save optimization snapshots](../../basic/io_03/LoadAndSaveOptimization.md): the JSON snapshot of an optimization, which also holds the extra info of its nodes and resources.
- [Advanced examples](../README.md): the overview of all Advanced examples.
- [Recommended ways to run an optimization](../../basic/recommendedimplementation_02/RecommendedImplementation.md): the recommended run pattern, a synchronous run and the legacy callback methods.

---

Added: 2026-02-17 (JOpt core 7.5.3-j17) | Last updated: 2026-10-20
