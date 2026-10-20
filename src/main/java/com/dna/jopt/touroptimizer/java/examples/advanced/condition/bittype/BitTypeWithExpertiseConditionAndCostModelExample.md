# Skill levels and cost models with BitType

`BitTypeWithExpertiseConditionAndCostModelExample` assigns four roofing customers to three slaters by two BitType skills with levels: a repair level and a weight level. Each requirement has a cost model that ranks the slaters who meet it, and all level limits but one are hard. The page is for developers who model skills with levels. The [BitType feature guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/bittype_condition) explains how BitType matches skills.

## What the example shows

- **Requirements and offers:** a node states a requirement with a `BitTypeWithExpertiseConstraint` and `addDictType(key, TypeLevelRequirement.of(level, isMinimum))`. A resource offers a skill with a `BitTypeWithExpertiseQualification` and `addDictType(key, TypeLevelOffering.of(level))`. The key is a string, here `Roof_Repair_Clean` and `Weight_Including_Equipment`.
- **Minimum and maximum levels:** the second argument of `TypeLevelRequirement.of` is `true` for a minimum level and `false` for a maximum level. The repair level is a minimum, the weight level a maximum.
- **Hard and soft:** `setIsHard(true)` makes the skill a must, and `setIsHardLevel(true)` makes the level limit a must as well. Without `setIsHardLevel`, a slater outside the level limit stays allowed and costs extra. The repair requirement of Oberhausen is the one soft requirement of the example.
- **Cost models:** among the slaters who meet a requirement, the cost model of the constraint decides which one costs least.
  - `NO_PENALIZE_MATCHING_SKILL`: every slater within the limit, at no extra cost.
  - `PENALIZE_MATCHING_SKILL_LOW_DELTA`: the extra cost grows with the gap between the offered level and the top of the level scale, so the best qualified slater costs least.
  - `PENALIZE_MATCHING_SKILL_HIGH_DELTA`: the extra cost grows with the gap between the offered and the required level, so the closest match costs least.

## Scenario

A roofing company plans May 6, 2025. The customers are open and the slaters work from 08:00 to 17:00 (Europe/Berlin); each slater may work 10 hours and drive 1200 km. The dates, levels, visit durations, limits and coordinates are sample values.

The repair level stands for the expertise in roof repair and cleaning (`Roof_Repair_Clean`), the weight level for the weight of the slater with equipment (`Weight_Including_Equipment`).

The three slaters:

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

## How the code works

The source is [BitTypeWithExpertiseConditionAndCostModelExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/condition/bittype/BitTypeWithExpertiseConditionAndCostModelExample.java). `example()` adds each customer in a method of its own, then the slaters, and runs the steps of the [First optimization example](../../../basic/firstoptimization_01/FirstOptimizationExample.md):

```java
// The public evaluation license, or your own license if you put a license file where
// ExampleLicenseHelper looks for it.
ExampleLicenseHelper.setLicense(this);
addProperties(this);
addCustomerOberhausen(this);
addCustomerKoeln(this);
addCustomerWuppertal(this);
addCustomerAachen(this);
addResources(this);
attachToObservables(this);
startAndPresentResult(this);
```

### A soft and a hard requirement

`addCustomerOberhausen` adds a soft repair requirement with a minimum level:

```java
int minRepairLevel = 2;
BitTypeWithExpertiseConstraint repairConstraint = new BitTypeWithExpertiseConstraint();
repairConstraint.setSkillCostingModel(SkillInfoCostModel.PENALIZE_MATCHING_SKILL_LOW_DELTA);
// The second argument says whether the level is a minimum (true) or a maximum (false).
repairConstraint.addDictType(SKILL_TYPE_REPAIR, TypeLevelRequirement.of(minRepairLevel, true));
```

and a hard weight limit with a maximum level:

```java
// Hard: setIsHard makes the skill a must, setIsHardLevel the level limit as well. Without
// setIsHardLevel, a level outside the limit only costs extra.
int maxWeightLevel = 10;
BitTypeWithExpertiseConstraint weightConstraint = new BitTypeWithExpertiseConstraint();
// Any slater within the limit is fine, at no extra cost.
weightConstraint.setSkillCostingModel(SkillInfoCostModel.NO_PENALIZE_MATCHING_SKILL);
weightConstraint.addDictType(SKILL_TYPE_WEIGHT, TypeLevelRequirement.of(maxWeightLevel, false));
weightConstraint.setIsHard(true);
weightConstraint.setIsHardLevel(true);
```

### Two requirements in one constraint

Koeln holds both requirements in one constraint, because they share one cost model:

```java
constraint.setSkillCostingModel(SkillInfoCostModel.NO_PENALIZE_MATCHING_SKILL);
constraint.addDictType(SKILL_TYPE_REPAIR, TypeLevelRequirement.of(minRepairLevel, true));
constraint.addDictType(SKILL_TYPE_WEIGHT, TypeLevelRequirement.of(maxWeightLevel, false));
// No slater below repair level 8 or above weight level 6.
constraint.setIsHard(true);
constraint.setIsHardLevel(true);
```

### The offers of the slaters

`addResources` gives each slater a qualification with both levels, for example Jack:

```java
BitTypeWithExpertiseQualification jackQualification = new BitTypeWithExpertiseQualification();
jackQualification.addDictType(SKILL_TYPE_REPAIR, TypeLevelOffering.of(jackRepairLevel));
jackQualification.addDictType(SKILL_TYPE_WEIGHT, TypeLevelOffering.of(jackWeightLevel));
```

### Properties

```java
// Fewer generations and iterations than the defaults keep the run short.
props.setProperty("JOptExitCondition.JOptGenerationCount", "1000");
props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumIterations", "100000");
// A higher NodeType weight makes the costs of the cost models and of missed levels count more
// against the driving costs.
props.setProperty("JOptWeight.NodeType", "10.0");
```

The [property reference](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties) lists the defaults.

## Output and what to look for

The result has one route per slater. In two test runs the assignment and the costs were the same. Route 0 belongs to `John Oberhausen - Repair: 2 Weight: 8` and route 1 to `Jack Aachen - Repair: 10 Weight: 5` (trimmed to their stops):

```text
0.0 Oberhausen (R>=2, W<=10) / Geo  / Arrival: 06.05.2025 08:00:00 (Europe/Berlin) , Departure: 06.05.2025 12:20:00 (Europe/Berlin) / Duration: 260.0 [min] / Driving: 0.0 [min], 0.0 [km]
1.0 Koeln (R>=8, W<=6) / Geo  / Arrival: 06.05.2025 08:47:59 (Europe/Berlin) , Departure: 06.05.2025 13:27:59 (Europe/Berlin) / Duration: 280.0 [min] / Driving: 47.99 [min], 63.34 [km]
```

Route 2 belongs to `Paula Aachen - Repair: 5 Weight: 2` and visits `Aachen (R>=3, W<=6)` and `Wuppertal (R>=5, W<=4)`. The order of her two stops can change between runs; the cost of the route stays the same.

- **Koeln** goes to Jack, the only slater with repair level 8 or more and weight level 6 or less.
- **Wuppertal** goes to Paula, the only slater with repair level 5 or more and weight level 4 or less. Her level 5 matches exactly, so `PENALIZE_MATCHING_SKILL_HIGH_DELTA` adds nothing.
- **Aachen** goes to Paula as well. Under `PENALIZE_MATCHING_SKILL_HIGH_DELTA` her level 5 is closer to the required 3 than Jack's 10.
- **Oberhausen** goes to John. `PENALIZE_MATCHING_SKILL_LOW_DELTA` prefers Jack, but Koeln and Oberhausen together, with the drives, take longer than Jack's 10 hours, and Aachen, Wuppertal and Oberhausen together take longer than Paula's: either of them would work overtime. John meets the minimum level 2, and the cost model charges his low level with an extra cost.

The cost models show up as `Cost NodeType` in the violation summaries of the routes: 640.0 on John's route for his repair level 2 at Oberhausen, and 40.0 on Paula's route for her level 5 at Aachen. Jack's route has none.

```text
Cost TotalSum: 661.6666666666666,Cost RouteTime: 21.666666666666668,Cost NodeType: 640.0
Cost TotalSum: 267.10952699653353,Cost Distance: 188.5400783854224,Cost RouteTime: 38.56944861111112,Cost NodeType: 40.0
```

In an earlier test run without `setIsHardLevel`, the assignment was the same, because the chosen assignment breaks no level limit. With soft limits, a slater outside a limit would be allowed at a high cost; with `setIsHardLevel(true)`, the limit itself holds.

## Run it

Run the `main(String[] args)` method of `BitTypeWithExpertiseConditionAndCostModelExample` from your IDE, with the repository imported as a Maven project. You need a JDK in the version that the examples pom sets. No license file is needed: without one, the example uses the public evaluation license (see the [license guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/license)).

## Related

- [BitType skills with expertise levels and cost models](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/bittype_condition): the feature guide of BitType, with the bitset matching and the cost models.
- [Skill with expertise and cost model](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/skill_costmodel): the feature guide of the cost models of skills with expertise levels.
- [Optimization properties](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties): the property reference, with `JOptWeight.NodeType` and its default.
- [Recommended ways to run an optimization](../../../basic/recommendedimplementation_02/RecommendedImplementation.md): the recommended run pattern, a synchronous run and the legacy callback methods.
- [Advanced examples](../../README.md): the overview of all Advanced examples.

---

Added: 2026-02-17 (JOpt core 7.5.3-j17) | Last updated: 2026-10-20
