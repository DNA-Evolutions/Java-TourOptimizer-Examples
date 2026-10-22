# Skills with types and expertise levels

Five examples give resources skills, called types, and let nodes require them. `ResourceTypeConditionHardExample` and `ResourceTypeConditionSoftExample` match a plain type as a hard and as a soft constraint, `ResourceTypeWithExpertiseConditionExample` and `ResourceTypeWithExpertiseConditionAndCostModelExample` add expertise levels and cost models, and `SelectiveTypeConditionExample` gives one resource other types on each working day. The page is for developers who decide by skills which resource may or should visit a node, and it uses the nodes and resources of the [Basic elements tutorial](https://www.dna-evolutions.com/docs/getting-started/tutorials/basic-elements).

## What the example shows

The focus is practical implementation: you can use the linked examples to reproduce all behaviors.

- **Types:** a resource offers types in a qualification, and a node requires types in a constraint. `TypeQualification` and `TypeConstraint` hold types as strings; `TypeWithExpertiseQualification` and `TypeWithExpertiseConstraint` hold each type with a level. The resource gets its qualification with `addQualification`, the node its constraint with `addConstraint`. A constraint names a type, not a resource: any resource with the type meets it.
- **Hard and soft:** `setIsHard(true)` makes a constraint hard: a resource without the type, or outside the level, may not visit the node. Without it, the constraint is soft: such a resource may visit the node, and the result then shows a violation with an extra cost. The property `JOptWeight.NodeType` weights that cost.
- **Minimum and maximum levels:** `addType(type, level)` on a `TypeWithExpertiseConstraint` sets a minimum level, `addType(type, level, false)` a maximum level. A level equal to the limit meets it.
- **Cost models:** among the resources that meet a level, the cost model of a `TypeWithExpertiseConstraint`, set with `setSkillCostingModel`, adds a cost that ranks them:
  - `NO_PENALIZE_MATCHING_SKILL`: no cost. `ResourceTypeWithExpertiseConditionAndCostModelExample` sets it explicitly on the constraints that use it.
  - `PENALIZE_MATCHING_SKILL_LOW_DELTA`: the cost grows with the gap between the level of the resource and level 10, the top of the level scale, so the best qualified resource costs least.
  - `PENALIZE_MATCHING_SKILL_HIGH_DELTA`: the cost grows with the gap between the level of the resource and the required level, so the closest match costs least.

  The cost is the square of the gap, multiplied by the weights `JOptWeight.NodeType` and `JOptWeight.ExpertiseLevelPreference`, the importance of the node and the per-kilometer cost of the resource.
- **Types per working day:** `IWorkingHours` has an `addQualification` method as well. When the working hours of a day hold types, a route of that day uses them instead of the types of the resource, so a resource can have a type on some days and not on others.

`ResourceTypeConditionHardExample`, `ResourceTypeConditionSoftExample` and `SelectiveTypeConditionExample` also write their result to a KML file of their own. [Skill levels and cost models with BitType](bittype/BitTypeWithExpertiseConditionAndCostModelExample.md) shows the roof repair scenario of `ResourceTypeWithExpertiseConditionAndCostModelExample` with BitType skills.

## Scenario

The dates, times, levels, limits and coordinates are sample values. The time zone is Europe/Berlin.

### Plain types

`ResourceTypeConditionHardExample` and `ResourceTypeConditionSoftExample` use the same plan:

- **Resources:** `Jack` and `John`, two `CapacityResource` objects at 50.775346, 6.083887 (Aachen). Both have working hours on March 6 and March 7, 2020, from 08:00 to 17:00, and may drive up to 1200 km a day. Jack may work up to 8 hours a day, John up to 14 hours.
- **Nodes:** eight `TimeWindowGeoNode` objects, open on March 6 and March 7 from 08:00 to 17:00, each with a visit of 20 minutes and the same importance: Koeln, Oberhausen, Essen, Heilbronn, Stuttgart, Wuppertal and Aachen, plus Dueren in `ResourceTypeConditionHardExample` and Nuernberg in `ResourceTypeConditionSoftExample`.
- **Types:**
  - `ResourceTypeConditionHardExample`: Jack has the type `plumbing`, John has no type. Koeln needs `plumbing`, as a hard constraint.
  - `ResourceTypeConditionSoftExample`: Jack has the type `efficient`, John has no type. Koeln prefers `efficient`, as a soft constraint.

The two classes set no properties, so the runs use the defaults of the [property reference](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties).

### Expertise levels

`ResourceTypeWithExpertiseConditionExample` plans May 6, 2025. The resources work and the nodes are open from 08:00 to 17:00; each resource may work 10 hours and drive 1200 km. All three resources have the type `Repair`, at different levels:

| Resource | Start | Repair level |
|---|---|---|
| `Jack Aachen - Expertise 10` | Aachen | 10 |
| `John Oberhausen - Expertise 2` | Oberhausen | 2 |
| `Paula Aachen - Expertise 4` | Aachen | 4 |

Two nodes need `Repair`, each with a visit of 60 minutes: Koeln at level 8 or more, as a hard constraint, and Oberhausen at level 3 or more, as a soft constraint. John starts at the position of Oberhausen.

### Roof repair

`ResourceTypeWithExpertiseConditionAndCostModelExample` plans May 6, 2025, for a roofing company. The customers are open and the slaters work from 08:00 to 17:00; each slater may work 10 hours and drive 1200 km. The repair level stands for the expertise in roof repair and cleaning (`Roof_Repair_Clean`), the weight level for the weight of the slater with equipment (`Weight_Including_Equipment`).

| Resource | Start | Repair level | Weight level |
|---|---|---|---|
| `Jack Aachen - Repair: 10 Weight: 5` | Aachen | 10 | 5 |
| `John Oberhausen - Repair: 2 Weight: 8` | Oberhausen | 2 | 8 |
| `Paula Aachen - Repair: 5 Weight: 2` | Aachen | 5 | 2 |

The four customers, each a `TimeWindowGeoNode` whose name shows its limits:

| Node | Visit | Repair level | Weight level |
|---|---|---|---|
| `Oberhausen (R>=2, W<=10)` | 260 minutes | at least 2, soft, `PENALIZE_MATCHING_SKILL_LOW_DELTA` | at most 10, hard, `NO_PENALIZE_MATCHING_SKILL` |
| `Koeln (R>=8, W<=6)` | 280 minutes | at least 8, hard, `NO_PENALIZE_MATCHING_SKILL` | at most 6, hard, in the same constraint |
| `Wuppertal (R>=5, W<=4)` | 200 minutes | at least 5, hard, `PENALIZE_MATCHING_SKILL_HIGH_DELTA` | at most 4, hard, `NO_PENALIZE_MATCHING_SKILL` |
| `Aachen (R>=3, W<=6)` | 120 minutes | at least 3, hard, `PENALIZE_MATCHING_SKILL_HIGH_DELTA` | at most 6, hard, `NO_PENALIZE_MATCHING_SKILL` |

Only Jack meets the limits of Koeln, and only Paula those of Wuppertal. Jack and Paula both meet those of Aachen; all three slaters meet those of Oberhausen.

### Types per working day

`SelectiveTypeConditionExample` plans one resource, `Jack`, at 50.775346, 6.083887 (Aachen), on five days from May 6 to May 10, 2020. Jack works from 08:00 to 17:00 and may work up to 13 hours and drive up to 1200 km a day. The types sit on the working hours of each day:

| Day | Types of Jack |
|---|---|
| May 6 | `DANGEROUS_GOODS`, `COOLING`, `SECOND_PERSON`, `PALLET_LIFT_TRUCK` |
| May 7 | `DANGEROUS_GOODS`, `LOADING_RAMP`, `CHAIN_SAW` |
| May 8 | `PALLET_LIFT_TRUCK`, `SECOND_PERSON`, `CHAIN_SAW` |
| May 9 | `PALLET_LIFT_TRUCK`, `SECOND_PERSON`, `LOADING_RAMP` |
| May 10 | `COOLING`, `CHAIN_SAW`, `LOADING_RAMP`, `SECOND_PERSON` |

A type does not expire by itself: the certification for dangerous goods is missing from May 8 on because the working hours of those days do not list it, and cooling is back on May 10 because the working hours of that day list it again.

Nine nodes are open from 08:00 to 17:00 on all five days, each with a visit of 60 minutes and a hard type constraint:

| Node | Types it needs | Days with all its types |
|---|---|---|
| Koeln | `DANGEROUS_GOODS`, `PALLET_LIFT_TRUCK` | May 6 |
| Oberhausen | `SECOND_PERSON`, `COOLING` | May 6, May 10 |
| Essen | `DANGEROUS_GOODS`, `COOLING` | May 6 |
| Dueren | `SECOND_PERSON`, `CHAIN_SAW`, `LOADING_RAMP` | May 10 |
| Nuernberg | `PALLET_LIFT_TRUCK`, `LOADING_RAMP` | May 9 |
| Heilbronn | `DANGEROUS_GOODS`, `CHAIN_SAW` | May 7 |
| Stuttgart | `PALLET_LIFT_TRUCK`, `CHAIN_SAW` | May 8 |
| Wuppertal | `COOLING` | May 6, May 10 |
| Aachen | `LOADING_RAMP` | May 7, May 9, May 10 |

## How the code works

The sources are [ResourceTypeConditionHardExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/condition/ResourceTypeConditionHardExample.java), [ResourceTypeConditionSoftExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/condition/ResourceTypeConditionSoftExample.java), [ResourceTypeWithExpertiseConditionExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/condition/ResourceTypeWithExpertiseConditionExample.java), [ResourceTypeWithExpertiseConditionAndCostModelExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/condition/ResourceTypeWithExpertiseConditionAndCostModelExample.java) and [SelectiveTypeConditionExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/condition/SelectiveTypeConditionExample.java). Each class runs the steps of the [First optimization example](../../basic/firstoptimization_01/FirstOptimizationExample.md) in `example()`. The classes with a KML file keep the result for `exportToKml`; the block is from `ResourceTypeConditionHardExample`:

```java
// The public evaluation license, or your own license if you put a license file where
// ExampleLicenseHelper looks for it.
ExampleLicenseHelper.setLicense(this);
addNodes(this);
addResources(this);
attachToObservables(this);
IOptimizationResult result = startAndPresentResult(this);
exportToKml(result);
```

The three classes with levels or with types per day call `addProperties` first, and `ResourceTypeWithExpertiseConditionAndCostModelExample` adds each customer in a method of its own, from `addCustomerOberhausen` to `addCustomerAachen`.

### The hard type constraint

`ResourceTypeConditionHardExample` gives Jack the type in `addResources`:

```java
TypeQualification jackQualification = new TypeQualification();
jackQualification.addType("plumbing");
jack.addQualification(jackQualification);
```

and Koeln the hard constraint at the end of `addNodes`:

```java
// The type constraint names the type that a resource needs, not a resource: any resource with
// the type plumbing may visit Koeln.
TypeConstraint plumbingConstraint = new TypeConstraint();
plumbingConstraint.addType("plumbing");
// Hard: a resource without the type may not visit Koeln.
plumbingConstraint.setIsHard(true);
koeln.addConstraint(plumbingConstraint);
```

### The soft type constraint

`ResourceTypeConditionSoftExample` gives Jack the type `efficient` in the same way and leaves the constraint of Koeln soft:

```java
// A soft constraint: JOpt prefers a resource with the type efficient for Koeln, but a resource
// without it may still visit Koeln, at an extra cost. setIsHard(true) would forbid that.
TypeConstraint efficientConstraint = new TypeConstraint();
efficientConstraint.addType("efficient");
koeln.addConstraint(efficientConstraint);
```

Both classes give Jack and John different maximal working times:

```java
// Jack may work 8 hours a day and John 14, so a long day such as the one with Nuernberg,
// Stuttgart and Heilbronn fits John without overtime.
Duration maxWorkingTimeJack = Duration.ofHours(8);
Duration maxWorkingTimeJohn = Duration.ofHours(14);
```

The block is from `ResourceTypeConditionSoftExample`; `ResourceTypeConditionHardExample` names Heilbronn and Stuttgart in the comment.

### Hard and soft minimum levels

`ResourceTypeWithExpertiseConditionExample` gives each resource the type `Repair` with its level, for example Jack:

```java
TypeWithExpertiseQualification jackQualification = new TypeWithExpertiseQualification();
jackQualification.addType(SKILL_TYPE, jackLevel);
```

The two constraints differ in the level and in `setIsHard`:

```java
// addType(type, level) sets a minimum level; the levels 8 and 3 are sample values. Hard: no
// resource below level 8 may visit Koeln.
int minLevelKoeln = 8;
TypeWithExpertiseConstraint highLevelConstraint = new TypeWithExpertiseConstraint();
highLevelConstraint.addType(SKILL_TYPE, minLevelKoeln);
highLevelConstraint.setIsHard(true);

// A soft constraint: a resource below level 3 may still visit Oberhausen, at an extra cost.
int minLevelOberhausen = 3;
TypeWithExpertiseConstraint mediumLevelConstraint = new TypeWithExpertiseConstraint();
mediumLevelConstraint.addType(SKILL_TYPE, minLevelOberhausen);
```

### Minimum and maximum levels with cost models

`addCustomerOberhausen` in `ResourceTypeWithExpertiseConditionAndCostModelExample` adds a soft repair requirement with a cost model and a hard weight limit:

```java
// The levels of the customer are sample values. A soft constraint: a slater below the level
// may still visit, at an extra cost. LOW_DELTA adds a cost that grows with the gap between the
// level of the slater and the top of the level scale, so it prefers the best qualified slater.
int minRepairLevel = 2;
TypeWithExpertiseConstraint repairConstraint = new TypeWithExpertiseConstraint();
repairConstraint.setSkillCostingModel(
    SkillWithExpertiseCostModel.PENALIZE_MATCHING_SKILL_LOW_DELTA);
// addType(type, level) sets a minimum level.
repairConstraint.addType(SKILL_TYPE_REPAIR, minRepairLevel);

int maxWeightLevel = 10;
TypeWithExpertiseConstraint weightConstraint = new TypeWithExpertiseConstraint();
// Any slater within the limit is fine, at no extra cost.
weightConstraint.setSkillCostingModel(SkillWithExpertiseCostModel.NO_PENALIZE_MATCHING_SKILL);
// The third argument says whether the level is a minimum (true) or a maximum (false).
weightConstraint.addType(SKILL_TYPE_WEIGHT, maxWeightLevel, false);
// Hard: the skill and its level limit are a must, no slater above weight level 10.
weightConstraint.setIsHard(true);
```

Koeln holds both requirements in one constraint, because they share one cost model:

```java
TypeWithExpertiseConstraint constraint = new TypeWithExpertiseConstraint();
constraint.setSkillCostingModel(SkillWithExpertiseCostModel.NO_PENALIZE_MATCHING_SKILL);
constraint.addType(SKILL_TYPE_REPAIR, minRepairLevel);
constraint.addType(SKILL_TYPE_WEIGHT, maxWeightLevel, false);
// No slater below repair level 8 or above weight level 6.
constraint.setIsHard(true);
```

Wuppertal and Aachen use `PENALIZE_MATCHING_SKILL_HIGH_DELTA` for the repair level. The visit durations are chosen so that Jack cannot serve both Koeln and Oberhausen within his 10 hours:

```java
// 260 minutes: together with Koeln (280 minutes) and the drives, Jack's day would take longer
// than his maximal working time.
Duration visitDuration = Duration.ofMinutes(260);
```

### Types on the working hours of each day

`SelectiveTypeConditionExample` puts the types on the working hours, not on the resource:

```java
// The types sit on the working hours of each day, not on the resource: a route uses the types
// of its day. The certification for dangerous goods is valid on May 6 and May 7 and is not
// renewed after that; cooling is on the truck on May 6 and May 10 only.
List<IWorkingHours> workingHours = new ArrayList<>();
workingHours.add(
    createWorkingDay(6, DANGEROUS_GOODS, COOLING, SECOND_PERSON, PALLET_LIFT_TRUCK));
workingHours.add(createWorkingDay(7, DANGEROUS_GOODS, LOADING_RAMP, CHAIN_SAW));
workingHours.add(createWorkingDay(8, PALLET_LIFT_TRUCK, SECOND_PERSON, CHAIN_SAW));
workingHours.add(createWorkingDay(9, PALLET_LIFT_TRUCK, SECOND_PERSON, LOADING_RAMP));
workingHours.add(createWorkingDay(10, COOLING, CHAIN_SAW, LOADING_RAMP, SECOND_PERSON));
```

`createWorkingDay` adds the types of a day to its working hours:

```java
TypeQualification qualification = new TypeQualification();
for (String type : types) {
  qualification.addType(type);
}
hours.addQualification(qualification);
return hours;
```

Each node gets a hard type constraint with the types it needs, for example Nuernberg:

```java
nuernberg.addConstraint(createTypeConstraint(PALLET_LIFT_TRUCK, LOADING_RAMP));
```

`createTypeConstraint` adds the types to a `TypeConstraint` and calls `setIsHard(true)`. A route may visit a node only if its day has all the types of the constraint.

### Properties, events and the KML file

`ResourceTypeWithExpertiseConditionExample` and `ResourceTypeWithExpertiseConditionAndCostModelExample` set three properties:

```java
// Fewer generations and iterations than the defaults keep the run short.
props.setProperty("JOptExitCondition.JOptGenerationCount", "1000");
props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumIterations", "100000");
// A higher NodeType weight makes the costs of the cost models and of missed levels count more
// against the driving costs.
props.setProperty("JOptWeight.NodeType", "10.0");
```

The block is from `ResourceTypeWithExpertiseConditionAndCostModelExample`. `SelectiveTypeConditionExample` sets the first two only: all its type constraints are hard, so no route may break them, and there is no extra cost for `JOptWeight.NodeType` to weight. The [property reference](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties) lists the defaults.

`attachToObservables` subscribes to the progress, warning, status and error events with the recommended event API, before the run starts. `startAndPresentResult` starts the run, waits up to 5 minutes for the result and prints it. In the three classes with a KML file it also returns the result, and `exportToKml` writes it to a file named after the class (for example `SelectiveTypeConditionExample.kml`) in the working directory, as in [Export a result to a KML file](../../basic/io_03/Export2KMLExample.md).

## Output and what to look for

### ResourceTypeConditionHardExample

The result was the same in two test runs. Koeln is on Jack's route of March 6, together with Aachen, Dueren, Wuppertal, Essen and Oberhausen (trimmed to these lines):

```text
RouteId                : 0
Resource               : Jack
0.2 Koeln / Geo  / Arrival: 06.03.2020 09:28:36 (Europe/Berlin) , Departure: 06.03.2020 09:48:36 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 27.25 [min], 35.97 [km]
```

John visits Stuttgart and Heilbronn on March 6, and neither route shows a violation. In two test runs of a variant of this class in which John has the type `plumbing` instead of Jack, Koeln was on John's route, so the constraint decides who visits Koeln. In two test runs of a variant in which no resource has the type, Koeln was on no route, and the result listed it under the unassigned nodes.

### ResourceTypeConditionSoftExample

The result was the same in two test runs. Koeln is on Jack's route of March 6, together with Aachen, Wuppertal, Essen and Oberhausen, and the route shows no violation, so the preference holds (trimmed to these lines):

```text
RouteId                : 0
Resource               : Jack
0.1 Koeln / Geo  / Arrival: 06.03.2020 09:07:59 (Europe/Berlin) , Departure: 06.03.2020 09:27:59 (Europe/Berlin) / Duration: 20.0 [min] / Driving: 47.99 [min], 63.34 [km]
```

In two test runs of a variant of this class in which John has the type `efficient` instead of Jack, Koeln was on John's route, again without a violation. No run broke the soft constraint; if one does, Koeln is on the route of a resource without the type, and that route shows the violation and the extra cost.

John's route of March 6 visits Nuernberg, Stuttgart and Heilbronn and ends after his working hours, with no violation (trimmed to these lines):

```text
Defined WorkingHours   : 06.03.2020 08:00:00 - 06.03.2020 17:00:00 (Europe/Berlin)
Effective Route Stop   : 06.03.2020 19:58:18 (Europe/Berlin)
Route time       [min] : 718.31
```

Time after the end of the working hours counts against the maximal working time, and the working hours are a frame for matching resources with the opening hours of the nodes, so a route can end after its working hours without a violation. John may work 14 hours, and the route takes about 12.

### ResourceTypeWithExpertiseConditionExample

The result was the same in two test runs. Jack visits Koeln and Oberhausen; John and Paula have no stops (trimmed to Jack's stops):

```text
Resource               : Jack Aachen - Expertise 10
1.0 Koeln / Geo  / Arrival: 06.05.2025 08:47:59 (Europe/Berlin) , Departure: 06.05.2025 09:47:59 (Europe/Berlin) / Duration: 60.0 [min] / Driving: 47.99 [min], 63.34 [km]
1.1 Oberhausen / Geo  / Arrival: 06.05.2025 10:33:12 (Europe/Berlin) , Departure: 06.05.2025 11:33:12 (Europe/Berlin) / Duration: 60.0 [min] / Driving: 45.21 [min], 59.68 [km]
```

- **Koeln** needs level 8, which only Jack has.
- **Oberhausen** goes to Jack as well, although John starts there: John's level 2 is below the soft minimum of 3, and a visit by him would cost extra. In two test runs of a variant of this class with John at level 3, John visited Oberhausen. Paula's level 4 meets the minimum, but in both runs Jack took Oberhausen after Koeln.

### ResourceTypeWithExpertiseConditionAndCostModelExample

The result was the same in two test runs. Route 0 belongs to `John Oberhausen - Repair: 2 Weight: 8` and route 1 to `Jack Aachen - Repair: 10 Weight: 5` (trimmed to their stops):

```text
0.0 Oberhausen (R>=2, W<=10) / Geo  / Arrival: 06.05.2025 08:00:00 (Europe/Berlin) , Departure: 06.05.2025 12:20:00 (Europe/Berlin) / Duration: 260.0 [min] / Driving: 0.0 [min], 0.0 [km]
1.0 Koeln (R>=8, W<=6) / Geo  / Arrival: 06.05.2025 08:47:59 (Europe/Berlin) , Departure: 06.05.2025 13:27:59 (Europe/Berlin) / Duration: 280.0 [min] / Driving: 47.99 [min], 63.34 [km]
```

Route 2 belongs to `Paula Aachen - Repair: 5 Weight: 2` and visits `Wuppertal (R>=5, W<=4)` and `Aachen (R>=3, W<=6)`.

- **Koeln** goes to Jack, the only slater with repair level 8 or more and weight level 6 or less.
- **Wuppertal** goes to Paula, the only slater with repair level 5 or more and weight level 4 or less. Her level 5 matches exactly, so `PENALIZE_MATCHING_SKILL_HIGH_DELTA` adds nothing.
- **Aachen** goes to Paula as well. Under `PENALIZE_MATCHING_SKILL_HIGH_DELTA` her level 5 is closer to the required 3 than Jack's 10.
- **Oberhausen** goes to John. `PENALIZE_MATCHING_SKILL_LOW_DELTA` makes Jack the cheapest match, but Jack must take Koeln and Paula Wuppertal, and with Oberhausen as well either route would take longer than 10 hours. John meets the minimum level 2, and the cost model charges his low level. The weight limit 10 of Oberhausen excludes no one, as no slater is above weight level 8.

The costs of the cost models show up as `Cost NodeType` in the summaries of John's and Paula's routes (trimmed to these lines):

```text
Cost TotalSum: 661.6666666666666,Cost RouteTime: 21.666666666666668,Cost NodeType: 640.0
Cost TotalSum: 267.10952699653353,Cost Distance: 188.5400783854224,Cost RouteTime: 38.56944861111112,Cost NodeType: 40.0
```

The importance of every customer is 1, and the class sets neither `JOptWeight.ExpertiseLevelPreference` nor a per-kilometer cost for the slaters, so in both runs these factors leave the cost at the square of the gap times the `JOptWeight.NodeType` weight of 10. John's level 2 is 8 below level 10: 8 squared is 64, and 64 times 10 is 640. At Aachen, Paula's level 5 is 2 above the required level 3: 2 squared is 4, times 10 is 40. Jack's route has no `Cost NodeType`.

### SelectiveTypeConditionExample

The result was the same in two test runs. Each node is on the route of a day whose working hours list all of its types, and no route shows a violation. Koeln, Essen, Heilbronn, Stuttgart, Nuernberg and Dueren each have one such day (trimmed to these stops):

```text
0.0 Koeln / Geo  / Arrival: 06.05.2020 08:47:59 (Europe/Berlin) , Departure: 06.05.2020 09:47:59 (Europe/Berlin) / Duration: 60.0 [min] / Driving: 47.99 [min], 63.34 [km]
0.2 Essen / Geo  / Arrival: 06.05.2020 11:36:24 (Europe/Berlin) , Departure: 06.05.2020 12:36:24 (Europe/Berlin) / Duration: 60.0 [min] / Driving: 17.75 [min], 23.43 [km]
1.1 Heilbronn / Geo  / Arrival: 07.05.2020 12:36:22 (Europe/Berlin) , Departure: 07.05.2020 13:36:22 (Europe/Berlin) / Duration: 60.0 [min] / Driving: 216.37 [min], 285.61 [km]
2.0 Stuttgart / Geo  / Arrival: 08.05.2020 11:56:15 (Europe/Berlin) , Departure: 08.05.2020 12:56:15 (Europe/Berlin) / Duration: 60.0 [min] / Driving: 236.25 [min], 311.85 [km]
3.0 Nuernberg / Geo  / Arrival: 09.05.2020 12:47:54 (Europe/Berlin) , Departure: 09.05.2020 13:47:54 (Europe/Berlin) / Duration: 60.0 [min] / Driving: 287.91 [min], 380.04 [km]
4.0 Dueren / Geo  / Arrival: 10.05.2020 08:21:21 (Europe/Berlin) , Departure: 10.05.2020 09:21:21 (Europe/Berlin) / Duration: 60.0 [min] / Driving: 21.36 [min], 28.2 [km]
```

Oberhausen and Wuppertal, whose types are on May 6 and May 10, are on May 6. Aachen, whose type is on May 7, May 9 and May 10, is on May 7, before Heilbronn.

The routes of May 7 and May 9 end after the working hours, with no violation (trimmed to these lines):

```text
Effective Route Stop   : 07.05.2020 17:17:13 (Europe/Berlin)
Effective Route Stop   : 09.05.2020 18:42:42 (Europe/Berlin)
```

Time after the end of the working hours counts against the maximal working time, and the working hours are a frame for matching resources with the opening hours of the nodes, so a route can end after its working hours without a violation. The two routes take 557.22 and 642.71 minutes of the 13 hours that Jack may work.

## References

### Example sources (GitHub)
- This example is used to illustrate **BitTypeWithExpertise** and why it can be multiple orders of magnitude faster than string/list based matching:
  [bittype/BitTypeWithExpertiseConditionAndCostModelExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/condition/bittype/BitTypeWithExpertiseConditionAndCostModelExample.java)

## Why "ResourceType" matters in tour optimization

Real routing problems are rarely "any vehicle can serve any customer". Typical constraints:
- Only some technicians are certified for specific work.
- Only some vehicles have equipment (cooling, ramp, pallet lift truck).
- Some jobs require a minimum expertise level, and some additionally impose a maximum (e.g., weight limit with equipment).
- Some certifications can be valid only during certain shifts/days (or "expire" if not refreshed).

In JOpt, these requirements are modeled using **Qualifications (on resources)** and **Constraints (on nodes)**.

## Terminology and the building blocks

### Qualification (resource-side)
A **qualification** describes what a resource *can do / has* (skills, types, expertise levels).

Typical classes:
- `TypeQualification` (type only; no expertise)
- `TypeWithExpertiseQualification` (type + expertise level)
- `BitTypeWithExpertiseQualification` (integer dictionary types + bitset + expertise)

### Constraint (node-side)
A **constraint** describes what a node *requires* (hard) or *prefers* (soft).

Typical classes:
- `TypeConstraint` (type only)
- `TypeWithExpertiseConstraint` (type + minimum/maximum expertise requirement, plus optional cost model)
- `BitTypeWithExpertiseConstraint` (same semantics as above, but optimized)

## Layer 1: Simple type matching (no expertise)

This is the most common "skills and equipment" use-case.

### Hard type constraint (must have the type)

**Example:** `ResourceTypeConditionHardExample`

**Result**
- Only Jack can serve Koeln.
- This is a **feasibility rule**. It is not meant to be implemented via an "extremely high cost".
- If no eligible resource exists, the node cannot be served under the given model.

### Soft type constraint (prefer the type, but allow exceptions)

**Example:** `ResourceTypeConditionSoftExample`

**Result**
- The solver *tends* to assign Koeln to Jack.
- But if doing so would cause higher overall cost (distance/time/overlaps/other constraints), Koeln may be served by John.

**Important**
Soft type matching is a cost-based preference. It is appropriate for:
- "nice to have" equipment,
- customer affinity,
- optional dispatch guidelines.

## Layer 2: Type matching with expertise levels

Many organizations need more than "has skill / does not have skill".
They need:
- minimum expertise requirements ("at least level 5"),
- maximum requirements ("must not exceed level 6"),
- and "best-fit" selection between multiple eligible resources.

JOpt provides this with:
- `TypeWithExpertiseQualification`
- `TypeWithExpertiseConstraint`

### Expertise: minimum vs maximum requirement

In the examples and docs you will see two patterns:

- **Minimum requirement** (common):  
  "Repair skill must be **>= required level**"

- **Maximum requirement** (less common but important):  
  "Weight including equipment must be **<= required level**"

This is critical for problems where "too much" can also be disqualifying (e.g., weight class, access permissions, vehicle dimensions).

### Hard and soft expertise example

**Example:** `ResourceTypeWithExpertiseConditionExample`

Practical guidance:
- Raise `JOptWeight.NodeType` if skill compliance is a priority.
- Lower it if you want the optimizer to treat skills as "secondary" compared to distance/time.

## Layer 3: Expertise cost models (quality-of-match optimization)

Once you have multiple eligible resources, you often want to optimize "match quality".

Example: Customer requires **minimum level 5**.
Both level 5 and level 10 are feasible.
Now you need a decision rule:
- "Assign the most expert resource" (premium service)
- or "Assign the cheapest adequate expert" (cost control)
- or "Prefer a close match" (avoid wasting top-tier experts)

This is exactly what the **SkillWithExpertiseCostModel** is for.

### The cost models (conceptual behavior)

As the optimizer computes them:
- `NO_PENALIZE_MATCHING_SKILL`  
  If the resource meets/exceeds the requirement, there is no penalty for having "more than required".

- `PENALIZE_MATCHING_SKILL_LOW_DELTA`  
  Adds a penalty based on the delta between the actual level and level 10, the top of the level scale, growing with the square of the delta.  
  Useful when you want high expertise; only level 10 comes "free".

- `PENALIZE_MATCHING_SKILL_HIGH_DELTA`  
  Strongly penalizes having much higher expertise than required.  
  Useful when you want an exact match and want to protect top-tier resources.

In practice:
- **LOW_DELTA** often supports "premium service preference".
- **HIGH_DELTA** often supports "avoid over-qualification" and better resource utilization.

### Example with min/max requirements plus cost model

**Example:** `ResourceTypeWithExpertiseConditionAndCostModelExample`

A major benefit of this modeling approach is that it produces solutions that remain **explainable**:
- "This resource was chosen because the customer requested high expertise, and the cost model rewards that."
- "This resource was chosen because the customer required an upper weight limit."

In the runs of this example, `Oberhausen (R>=2, W<=10)`, the one customer with `PENALIZE_MATCHING_SKILL_LOW_DELTA`, goes to John with repair level 2, because the days of Jack and Paula have no room for it (see "Output and what to look for").

## SelectiveTypeCondition: time-dependent / expiring skills

In some businesses, a qualification may be valid only for:
- specific time windows,
- specific shifts,
- specific days (e.g., a certification that must be refreshed periodically).

JOpt supports this by attaching qualifications to **WorkingHours segments**, not only to the resource globally.

**Example:** `SelectiveTypeConditionExample`

### Operational use cases
- Dangerous goods certification valid only for certain shifts.
- Specialist equipment available only on certain days.
- Temporary authorizations.

## High-performance BitTypeWithExpertise: why it is dramatically faster

For many production scenarios, the performance bottleneck is not distance computation: it is **constraint checking**.

If you have:
- thousands of nodes,
- many skills,
- multiple constraints per node,
- and frequent reassignment during optimization iterations,

then "skill matching" can dominate runtime if implemented with strings, lists, or repeated hashing.

### Key idea: represent skills as bitsets
**BitTypeWithExpertise** represents the *presence of skills* as a **bitset**:
- each skill is mapped to an integer index,
- the resource holds a bitset of offered skills,
- the node holds a bitset of required skills.

Checking whether the resource satisfies all required skills becomes:
- a small number of CPU-word operations (`AND` / comparison),
- with early failure if any skill is missing.

This is fundamentally faster than:
- iterating strings,
- repeated map lookups,
- repeated hash computations,
- repeated allocations.

### Fast path + slow path (only if needed)
The most important performance principle is:

1) **Fast path:** Check pure skill presence with bitset operations.  
   If any required skill is missing, fail immediately.

2) **Slow path (optional):** Only if all required skills are present and expertise levels are relevant, evaluate levels.  
   Level checking is inherently more "branchy" and typically uses a map (skill -> level).

This strategy can yield "several thousand percent" speedups in large instances because:
- most candidate assignments fail fast at the bitset level,
- level checks are only performed for candidates that already passed the fast eligibility filter.

### Coverage: expertise and non-expertise types
BitTypeWithExpertise can cover:
- classic "type only" matching (treat skills as present/absent),
- expertise-based matching (add level metadata only when needed),
- and mixed scenarios (some skills with levels, some without).

## Hard constraints are fulfilled by architecture, not by "high cost"

This point is crucial for correct modeling and solver behavior.

### What you should do
- Use **hard constraints** (`setIsHard(true)`) for non-negotiable rules:
  - required certification,
  - legal restrictions,
  - strict customer prohibitions.

### What you should not do
- Do not attempt to simulate a hard rule by:
  - leaving the rule soft, and
  - setting an extremely high penalty weight.

Hard constraints are handled as **feasibility rules** by the solver architecture: the optimizer does not let a resource that misses a hard type constraint visit the node. A hard `TypeConstraint` adds no cost. A `TypeWithExpertiseConstraint` adds the cost of its cost model whether it is hard or not, as `Cost NodeType: 40.0` on Paula's route shows for the hard repair constraint of `Aachen (R>=3, W<=6)`.  
"High cost" is appropriate for **preferences**, not for absolute eligibility.

## Practical recommendations

### 1) Use stable skill identifiers
Prefer constants and centralized skill catalogs.
Avoid accidental mismatches due to typos or inconsistent naming.

### 2) Start with simple TypeConstraint, then introduce expertise
- If you only need "has equipment yes/no", use `TypeConstraint`.
- If you need level thresholds or utilization control, use `TypeWithExpertiseConstraint`.

### 3) Use cost models to protect expensive experts
If you have many highly skilled resources, use cost models to prevent:
- over-qualification waste,
- or to enforce a "premium service" policy consistently.

### 4) For very large instances: prefer BitTypeWithExpertise
If you have large-scale optimization with many skills, BitTypeWithExpertise can dramatically reduce runtime by:
- eliminating repeated string matching,
- making presence checks CPU-efficient,
- and running level checks only when required.

## Summary

- **TypeConstraint / TypeQualification**: simplest and most common skill model.
  - hard: eligibility
  - soft: preference

- **TypeWithExpertiseConstraint / TypeWithExpertiseQualification**: adds expertise levels (min/max), supports soft violations and can be weighted via `JOptWeight.NodeType`.

- **SkillWithExpertiseCostModel**: controls "quality of match" among feasible candidates (exact match vs best expert vs adequate match).

- **SelectiveTypeCondition**: skills can be time-dependent by attaching qualifications to working hours segments.

- **BitTypeWithExpertise**: high-performance skill matching using bitsets; extremely fast for large problems due to CPU-efficient set inclusion checks with early failure.

## Run it

Run the `main(String[] args)` method of one of the five classes from your IDE. You need a JDK in the version that the examples pom sets. You need no license file: without one, the examples use the public evaluation license (see the [license guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/license)). `ResourceTypeConditionHardExample`, `ResourceTypeConditionSoftExample` and `SelectiveTypeConditionExample` write their KML file into the working directory.

## Related

- [Advanced examples](../README.md): the overview of all Advanced examples.
- [Recommended ways to run an optimization](../../basic/recommendedimplementation_02/RecommendedImplementation.md): the recommended run pattern, a synchronous run and the legacy callback methods.
- [Skill with expertise and cost model](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/skill_costmodel): the feature guide of the cost models of skills with expertise levels.
- [BitType skills with expertise levels and cost models](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/bittype_condition): the feature guide of BitType, skills as bitsets, with the cost models.
- [Skill levels and cost models with BitType](bittype/BitTypeWithExpertiseConditionAndCostModelExample.md): the roof repair example with BitType skills.
- [Mandatory, preferred, banned and unpreferred resources for a node](BindingExcludingResourceCondition.md): constraints that name resources instead of types.
- [Basic elements tutorial](https://www.dna-evolutions.com/docs/getting-started/tutorials/basic-elements): the nodes and resources that these examples use.
- [Optimization properties](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties): the property reference, with `JOptWeight.NodeType` and `JOptWeight.ExpertiseLevelPreference`.
- [Export a result to a KML file](../../basic/io_03/Export2KMLExample.md): the KML export in general.

---

Added: 2026-02-17 (JOpt core 7.5.3-j17) | Last updated: 2026-10-23
