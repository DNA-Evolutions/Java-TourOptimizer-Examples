# Territories with zone numbers and UK postcodes

`ZoneNumberConstraintExample` and `UKPostCodeExample` give each working day of a resource its own territory. A zone constraint on the `WorkingHours` names the zones of that day, and a qualification on each node names the zone of the node. The first class uses zone numbers for eight cities in Germany, the second the postcode districts of nine places around Birmingham, and both make the constraints hard. The page is for developers who model service districts or postal areas; the [ZoneCodes and territory management](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/zonecodes) feature guide describes the feature, with soft constraints and the JSON input.

## What the example shows

- **The resource side:** a `ZoneNumberConstraint` or a `UKPostCodeConstraint` collects the zones of one working day with `addZoneCode` and goes onto that `WorkingHours` with `addConstraint`. Each working day has a constraint of its own, so the territory changes from day to day.
- **The node side:** a `ZoneNumberQualification` or a `UKPostCodeQualification`, added to the node with `addQualification`, names the zone of the node.
- **A node in two zones:** one qualification with a second zone added by `addExtraCode`. A node keeps one qualification per class, so a second `ZoneNumberQualification` on the same node would replace the first. `Oberhausen-Z1-Z2` belongs to zones 1 and 2 this way.
- **Partial postcodes:** a postcode in a constraint compares only the parts it holds. `B37`, with the area and the district only, matches the postcodes `B37 2G` and `B37 2F` of the nodes.
- **Hard constraints:** both classes call `setIsHard(true)`. A node outside the zones of a working day is not planned on that day, and a node that matches no working day stays unassigned.
- **Soft constraints:** a zone constraint is soft unless `setIsHard(true)` is called. A soft constraint allows a visit outside the zones of the day; the result reports the visit as a violation, and the property `JOptWeight.ZoneCode` scales its cost. The two examples do not run the soft case; the feature guide describes it.

## Scenario

The dates and times are sample values, and so are the coordinates in Germany. In both classes, `addProperties` sets `JOptExitCondition.JOptGenerationCount` to 1000 and `JOpt.Algorithm.PreOptimization.SA.NumIterations` to 100000. Both are below their defaults, which keeps the run short. `addProperties` also sets `JOptWeight.ZoneCode` to 10.0, a sample weight that scales the cost of a visit outside the zones of a working day.

### Zone numbers in Germany

- **Resource:** `Jack` starts at 50.775346, 6.083887 (Aachen) and works on May 6 and 7, 2020, from 08:00 to 20:00 (Europe/Berlin). He may visit zones 1 and 2 on May 6 and zone 3 on May 7. His maximal working time is 13 hours, a sample value above what a day of this plan needs, and his maximal distance 1200 km.
- **Nodes:** eight `TimeWindowGeoNode` objects, open on May 6 and 7 from 08:00 to 17:00, each with a visit of 20 minutes and the importance 1. The name of each node ends with its zones:
  - zone 1: `Koeln-Z1`;
  - zones 1 and 2: `Oberhausen-Z1-Z2`;
  - zone 2: `Dueren-Z2`, `Stuttgart-Z2`, `Wuppertal-Z2` and `Aachen-Z2`;
  - zone 3: `Essen-Z3` and `Nuernberg-Z3`.

The zone numbers are sample values. A zone need not follow the map: Essen and Nuernberg share zone 3.

### Postcode districts around Birmingham

- **Resource:** `Jack` starts at 52.48, -1.9025, in the centre of Birmingham, and works on May 6 and 7, 2020, from 08:00 to 20:00 (Europe/London). He may visit the districts B37 and B48 on May 6 and B36 on May 7. His maximal working time is 13 hours, a sample value above what a day of this plan needs, and his maximal distance 1200 km.
- **Nodes:** nine `TimeWindowGeoNode` objects, open on May 6 and 7 from 08:00 to 17:00, each with a visit of 20 minutes and the importance 1. Each node is named after a place in its district, and the name ends with the district:
  - B31: `Northfield-B31` (postcode `B31 2G`);
  - B36: `CastleBromwich-B36` (`B36 2G`), `HodgeHill-B36` (`B36 2H`) and `SmithsWood-B36` (`B36 2F`);
  - B37: `ChelmsleyWood-B37` (`B37 2G`) and `MarstonGreen-B37` (`B37 2F`);
  - B48: `Alvechurch-B48` (`B48 2G`), `Hopwood-B48` (`B48 2H`) and `RowneyGreen-B48` (`B48 2F`).

The coordinates of Birmingham and of each place are those that its article on the English Wikipedia gives, and the postcode districts those that the Wikipedia article "B postcode area" lists, or that the article of the place gives. The sector and the unit of each postcode are sample values, because the constraints name only the area and the district.

## How the code works

The sources are [ZoneNumberConstraintExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/zonecode/ZoneNumberConstraintExample.java) and [UKPostCodeExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/zonecode/UKPostCodeExample.java). Both call the same steps in `example()`:

```java
// The public evaluation license, or your own license if you put a license file where
// ExampleLicenseHelper looks for it.
ExampleLicenseHelper.setLicense(this);
addProperties(this);
addNodes(this);
addResources(this);
attachToObservables(this);
startAndPresentResult(this);
```

- `ExampleLicenseHelper.setLicense(this)` uses your license file if you put one where the helper looks for it, and the public evaluation license otherwise.
- `addProperties` sets the three properties.
- `addNodes` adds the nodes with their qualifications.
- `addResources` adds Jack, with a zone constraint on each working day.
- `attachToObservables` subscribes to the progress, warning, status and error events with the event API, before the run starts.
- `startAndPresentResult` starts the run, waits up to 5 minutes for the result and prints it.

### The zones of the working days

In `ZoneNumberConstraintExample`, `addResources` gives each of the two `WorkingHours` its own constraint:

```java
// The zones of a working day are a ZoneNumberConstraint on its WorkingHours, so they can
// differ from day to day. The constraints are hard: on May 6 Jack visits only nodes of zone
// 1 or 2, on May 7 only nodes of zone 3.
ZoneNumberConstraint zoneNumberConstraintWOHOne = new ZoneNumberConstraint();
zoneNumberConstraintWOHOne.setIsHard(true);
zoneNumberConstraintWOHOne.addZoneCode(new ZoneNumber(1));
zoneNumberConstraintWOHOne.addZoneCode(new ZoneNumber(2));
woh1.addConstraint(zoneNumberConstraintWOHOne);

ZoneNumberConstraint zoneNumberConstraintWOHTwo = new ZoneNumberConstraint();
zoneNumberConstraintWOHTwo.setIsHard(true);
zoneNumberConstraintWOHTwo.addZoneCode(new ZoneNumber(3));
woh2.addConstraint(zoneNumberConstraintWOHTwo);
```

### The zones of the nodes

`addNodes` creates one qualification per zone and adds it to the nodes of that zone. The block shows Koeln:

```java
// The zone of a node is a ZoneNumberQualification. The zone numbers are sample values, and a
// zone need not follow the map: Essen and Nuernberg share zone 3.
IQualification zoneOneQuali = new ZoneNumberQualification(new ZoneNumber(1));
IQualification zoneTwoQuali = new ZoneNumberQualification(new ZoneNumber(2));
IQualification zoneThreeQuali = new ZoneNumberQualification(new ZoneNumber(3));

INode koeln =
    new TimeWindowGeoNode("Koeln-Z1", 50.9333, 6.95, openingHours, visitDuration, importance);
koeln.addQualification(zoneOneQuali);
opti.addElement(koeln);
```

Oberhausen belongs to two zones, with one qualification and an extra code:

```java
// A node on the border of two zones gets one qualification with an extra code, so a working
// day with either zone can include it. A node keeps one qualification per class: a second
// ZoneNumberQualification would replace the first.
ZoneNumberQualification borderQuali = new ZoneNumberQualification(new ZoneNumber(1));
borderQuali.addExtraCode(new ZoneNumber(2));
INode oberhausen =
    new TimeWindowGeoNode(
        "Oberhausen-Z1-Z2", 51.4667, 6.85, openingHours, visitDuration, importance);
oberhausen.addQualification(borderQuali);
opti.addElement(oberhausen);
```

### UK postcodes

`UKPostCodeExample` follows the same pattern with postcodes. The constraint of May 6:

```java
// A UKPostCode takes the area, the district, the sector and the unit. A part that the code of
// a constraint leaves empty is not compared, so B37 matches every postcode of the district
// B37. The constraints are hard: on May 6 Jack visits only nodes in B37 or B48, on May 7
// only nodes in B36.
UKPostCodeConstraint postCodeConstraintWoh1 = new UKPostCodeConstraint();
postCodeConstraintWoh1.setIsHard(true);
postCodeConstraintWoh1.addZoneCode(
    new UKPostCode("B", Optional.of(37), Optional.empty(), Optional.empty()));
postCodeConstraintWoh1.addZoneCode(
    new UKPostCode("B", Optional.of(48), Optional.empty(), Optional.empty()));
woh1.addConstraint(postCodeConstraintWoh1);
```

Each node gets a `UKPostCodeQualification` with its full postcode. Northfield lies in B31, which no working day allows:

```java
// B31 is in no constraint of Jack's working days, so the hard constraints leave this node
// unassigned.
INode northfield =
    new TimeWindowGeoNode(
        "Northfield-B31", 52.408, -1.963, openingHours, visitDuration, importance);
northfield.addQualification(
    new UKPostCodeQualification(
        new UKPostCode("B", Optional.of(31), Optional.of(2), Optional.of("G"))));
opti.addElement(northfield);
```

## Output and what to look for

Each run prints the status and progress lines, then the result. The results were the same in two test runs of each class.

### ZoneNumberConstraintExample

- **Route 0, May 6:** Aachen-Z2, Dueren-Z2, Oberhausen-Z1-Z2, Wuppertal-Z2, Koeln-Z1 and Stuttgart-Z2, the nodes of zones 1 and 2.
- **Route 1, May 7:** Essen-Z3 and Nuernberg-Z3, the nodes of zone 3.

The line "WOH Constraints" of each route names the zones of its working day (trimmed to these lines):

```text
RouteId                : 0
WOH Constraints        : [ZONENUMBER [ZoneNumber: 1, ZoneNumber: 2]]
RouteId                : 1
WOH Constraints        : [ZONENUMBER [ZoneNumber: 3]]
```

The stops of route 1:

```text
1.0 Essen-Z3 / Geo  / Arrival: 07.05.2020 09:15:26 (Europe/Berlin) , Departure: 07.05.2020 09:35:26 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 75.44 [min], 99.58 [km]
1.1 Nuernberg-Z3 / Geo  / Arrival: 07.05.2020 14:06:42 (Europe/Berlin) , Departure: 07.05.2020 14:26:42 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 271.27 [min], 358.08 [km]
```

Route 0 takes 714.37 minutes of the 13 hours that Jack may work and ends at 19:54:22, before his working hours end at 20:00. Neither route shows a violation.

### UKPostCodeExample

Northfield-B31 matches no district of Jack's working days and stays unassigned:

```text
Number of unassigned nodes: 1

Northfield-B31
```

- **Route 0, May 6:** ChelmsleyWood-B37, MarstonGreen-B37, RowneyGreen-B48, Alvechurch-B48 and Hopwood-B48, the nodes of B37 and B48.
- **Route 1, May 7:** HodgeHill-B36, CastleBromwich-B36 and SmithsWood-B36, the nodes of B36.

The stops of route 0:

```text
0.0 ChelmsleyWood-B37 / Geo  / Arrival: 06.05.2020 08:08:53 (Europe/London) , Departure: 06.05.2020 08:28:53 (Europe/London) / Duration: 20.0 [min] / Driving: 8.9 [min], 11.75 [km]
0.1 MarstonGreen-B37 / Geo  / Arrival: 06.05.2020 08:29:59 (Europe/London) , Departure: 06.05.2020 08:49:59 (Europe/London) / Duration: 20.0 [min] / Driving: 1.09 [min], 1.44 [km]
0.2 RowneyGreen-B48 / Geo  / Arrival: 06.05.2020 09:04:20 (Europe/London) , Departure: 06.05.2020 09:24:20 (Europe/London) / Duration: 20.0 [min] / Driving: 14.35 [min], 18.94 [km]
0.3 Alvechurch-B48 / Geo  / Arrival: 06.05.2020 09:25:51 (Europe/London) , Departure: 06.05.2020 09:45:51 (Europe/London) / Duration: 20.0 [min] / Driving: 1.51 [min], 2.0 [km]
0.4 Hopwood-B48 / Geo  / Arrival: 06.05.2020 09:47:42 (Europe/London) , Departure: 06.05.2020 10:07:42 (Europe/London) / Duration: 20.0 [min] / Driving: 1.86 [min], 2.45 [km]
```

In this class the line "WOH Constraints" names the class `UKPostCodeConstraint` and an object number, not the postcodes. Route 0 ends at 10:17:12 and route 1 at 09:16:31, inside the working hours, and neither shows a violation.

## Why ZoneCodes are a "construction capability" (not just another constraint)

In many real deployments, "territory logic" is one of the primary drivers for feasibility:

- legal or contractual territories,
- dispatch regions that change daily,
- customer-specific service areas,
- teams that temporarily cover for each other.

ZoneCodes model this **architecturally**:
- they reduce the assignment search space immediately,
- they eliminate infeasible allocations early,
- and they scale better than trying to approximate territory logic via costs.

You get fine-grained control without drawing polygons or maintaining complex geofences.

## Hard vs soft ZoneCodes (and the cost weight)

Both examples demonstrate **hard constraint** behavior, `UKPostCodeExample` with:

```java
postCodeConstraintWoh1.setIsHard(true);
```

and `ZoneNumberConstraintExample` with:

```java
zoneNumberConstraintWOHOne.setIsHard(true);
```

Interpretation:
- If a node's ZoneCode does not match the resource's allowed ZoneCodes for the current WorkingHours, the node **cannot** be assigned to that resource in that shift.

### Soft mode (when you want flexibility)
ZoneCodes can also be modeled as a *soft constraint* (not enforced as hard feasibility).  
In that case, violating the ZoneCode restriction creates a penalty cost.

The standard weight property is:

- `JOptWeight.ZoneCode` (both examples set it to `10.0`)

`ZoneNumberConstraintExample` and `UKPostCodeExample` set it in `addProperties`:

```java
props.setProperty("JOptWeight.ZoneCode", "10.0");
```

Practical guidance:
- Use **hard** ZoneCodes when territories are mandatory.
- Use **soft** ZoneCodes when territories are "preferred", but cross-coverage is allowed at a measurable cost.

## Example 1: UK Postcodes as ZoneCodes (UKPostCodeExample)

This example implements ZoneCodes using **UK Postcode identifiers**.

### What it demonstrates
- A resource can serve different postcodes on different days.
- Nodes carry postcode qualifications.
- A node with no matching postcode is **not assigned** when the constraint is hard.

### Resource side: daily postcode territories
The resource has two WorkingHours blocks (two days). Each day receives its own allowed postcodes:

- Day 1 (WorkingHours 1): allowed `B37` and `B48`
- Day 2 (WorkingHours 2): allowed `B36`

This is implemented by attaching different `UKPostCodeConstraint` instances to `woh1` and `woh2`.

### Node side: postcode qualifications
Each node receives a `UKPostCodeQualification` such as:

- `B37 2G`, `B48 2F`, etc.

The example includes a node explicitly designed to fail matching:

- `"Northfield-B31"`

The source comment explains the intent:
- this node will not be assigned due to the hard postcode constraint.

### Why this pattern is powerful
If your territories naturally map to:
- postal codes,
- zip codes,
- service regions already maintained in CRM / ERP systems,
then ZoneCodes let you reuse that structure directly, without spatial polygons.

## Example 2: Simple numbered territories (ZoneNumberConstraintExample)

This example is the "minimal" version of ZoneCodes:
- define a few integer zones (`1`, `2`, `3`)
- attach them to WorkingHours (resource side)
- attach them to nodes (node side)

### What it demonstrates (ZoneNumberConstraintExample)

#### 1) Different territories per WorkingHours
- WorkingHours 1: allowed Zone 1 and Zone 2
- WorkingHours 2: allowed Zone 3

This models daily territory switching (e.g., on-call rotations, coverage plans).

#### 2) Border nodes (multi-zone qualification)
The example explicitly creates a border node:

- `"Oberhausen-Z1-Z2"`

It receives one qualification with an extra code (`addExtraCode`):
- Zone 1 and Zone 2

Interpretation:
- it can be served by resources that are allowed to serve either zone.

This is the recommended modeling strategy for territory boundaries:
- assign multiple ZoneCodes to a border node,
- let the optimizer decide which resource assignment yields the smallest additional cost.

#### 3) Default behavior if no zone constraint is present
JOpt checks a zone constraint only on the working hours that hold it:
- if a resource has no ZoneNumberConstraint, it can visit all nodes.

This is a useful design feature:
- you can introduce zones gradually (hybrid deployments),
- or define a "floating" resource that is allowed everywhere.

## Design patterns and best practices

### 1) Define ZoneCodes at the same "granularity" as dispatch decisions
If dispatch is performed by:
- city parts, ZIP codes, districts -> use postcode-like ZoneCodes
- coarse regions, depots, teams -> use numeric ZoneCodes (or any custom identifiers)

Avoid territory definitions that are too granular unless necessary.

### 2) Prefer hard ZoneCodes for feasibility and explainability
Hard ZoneCodes:
- reduce search space,
- prevent implausible assignments,
- and make results easier to explain operationally ("this technician does not cover that zone").

If you need flexibility, introduce it explicitly via:
- soft ZoneCodes (penalty),
- or additional cross-coverage resources.

### 3) Model border jobs with multiple ZoneCodes
If a node is close to a boundary:
- add multiple ZoneCodes to its qualification (`addExtraCode`).

This avoids arbitrary "polygon edge" decisions and lets the optimizer choose the best plan.

### 4) Use per-WorkingHours ZoneCodes for daily territory switching
This is the key scaling feature:
- "territories change daily" becomes a data update, not a model change.

### 5) Do not build pseudo territories with distance penalties
Distance penalties cannot reliably enforce territory rules when:
- zones overlap,
- zones change daily,
- or service areas are not purely geometric.

ZoneCodes encode territory membership directly and are therefore the correct architectural tool.

## Summary

- ZoneCodes implement territories by matching:
  - **WorkingHours constraints** (what a resource may serve in that shift) and
  - **Node qualifications** (what territory the node belongs to).
- You can use hard constraints (mandatory territories) or soft constraints (penalty-based flexibility).
- Border nodes can carry multiple ZoneCodes to allow either neighboring territory to serve them.
- The included examples show both a "real-world" encoding (UK postcodes) and a minimal numeric encoding (ZoneNumbers), using the same concept and modeling workflow.

## Run it

Run the `main(String[] args)` method of `ZoneNumberConstraintExample` or `UKPostCodeExample` from your IDE, with the repository imported as a Maven project. You need a JDK in the version that the examples pom sets. You need no license file: without one, the examples use the public evaluation license (see the [license guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/license)).

## Related

- [ZoneCodes and territory management](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/zonecodes): the feature guide, with soft constraints, the JSON input and modeling guidance.
- [Defining territories via ZoneCodes](https://www.dna-evolutions.com/docs/learn-and-explore/special/special_features#defining-territories-via-zonecodes): the short overview of zone codes on the special features page.
- [Bridge and tunnel crossing between zones](../zonecrossing/BridgeTunnelCrossingZoneNumberConstraint.md): a cost for route steps from a node of one zone to a node of another.
- [Optimization properties](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties): the property reference, with `JOptWeight.ZoneCode` and its default.
- [Recommended ways to run an optimization](../../basic/recommendedimplementation_02/RecommendedImplementation.md): the recommended run pattern, a synchronous run and the legacy callback methods.
- [Advanced examples](../README.md): the overview of all Advanced examples.

---

Added: 2026-02-17 (JOpt core 7.5.3-j17) | Last updated: 2026-10-21
