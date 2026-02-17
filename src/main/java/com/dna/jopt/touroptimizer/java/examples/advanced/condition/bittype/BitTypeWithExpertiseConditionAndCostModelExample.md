# BitTypeWithExpertiseCondition

This example demonstrates how to model **hard skill eligibility** with **expertise levels** using **BitType**, and how to attach a **cost model** for preference/penalty behavior — while keeping evaluation fast enough for large, constraint-heavy instances.

- **Source (GitHub):** [/bittype/BitTypeWithExpertiseConditionAndCostModelExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/condition/bittype/BitTypeWithExpertiseConditionAndCostModelExample.java)

---

## Why BitType?

Skill and eligibility checks are evaluated *extremely often* during optimization (construction, swaps, reinsertions, exchanges).  
BitType accelerates this by mapping skill keys (strings) to a **dictionary of integer IDs** and representing the skill set as a **bitset**:

- `skillKey (String)` → `dictionaryId (int)` → `bit position`
- matching becomes: **requiredBits ⊆ offeredBits** using fast bit operations

This is especially relevant when you have:
- many nodes/resources
- many skills
- expertise levels
- frequent eligibility checks across many candidate moves

---

## Scenario in this example

The example builds a small optimization instance where:

- Nodes require **skills** (e.g., repair capability) and sometimes a **skill level** (expertise)
- Resources provide skills (and levels)
- The optimizer enforces **hard eligibility by architecture** (if a skill is required, only matching resources are considered feasible)
- Additionally, a **cost model** can influence *preference* decisions (e.g., “prefer better match”, “penalize weak match”), without weakening hard feasibility

> Important: In JOpt.TourOptimizer, **hard skill constraints are fulfilled by architecture — not by high costs**.  
> Costs are used for optimization trade-offs and preferences, not to “force feasibility”.

---

## Key API idea: `addDictType(...)`

In BitType, you typically attach requirements via `addDictType(...)`. Conceptually, this means:

1. register the string skill key in the dictionary (if not already present),
2. translate it into an integer ID,
3. set the corresponding bit inside the requirement bitset,
4. optionally attach expertise-level requirements.

Examples shown in this file:

- `repairConstraintMediumExpertise.addDictType(SKILL_TYPE_REPAIR, req);`
- `repairConstraintMediumExpertise.addDictType(SKILL_TYPE_WEIGHT, req2);`
- `repairConstraintMediumExpertise.addDictType(SKILL_TYPE_REPAIR,req3);`
- `weightConstraintMediumExpertise.addDictType(SKILL_TYPE_WEIGHT, req4);`
- `repairConstraintHighExpertise.addDictType(SKILL_TYPE_REPAIR, TypeLevelRequirement.of(minEpxertiseLevelHigh, true));`
- `repairConstraintHighExpertise.addDictType(SKILL_TYPE_WEIGHT, TypeLevelRequirement.of(maxWeightLevelHigh, !weightIsMaxLevel));`
- `repairConstraintMediumExpertise.addDictType(SKILL_TYPE_REPAIR, TypeLevelRequirement.of(minEpxertiseLevelMedium, true));`
- `weightConstraintMediumExpertise.addDictType(SKILL_TYPE_REPAIR, TypeLevelRequirement.of(maxWeightLevelMedium, !weightIsMaxLevel));`

---

## What to look for when you run it

When you run the `main()` method, focus on:

1. **Feasibility / eligibility**
   - Nodes that require a skill must not be assigned to resources missing that skill.
   - If expertise levels are required as *hard*, assignments must respect min/max level logic.

2. **Preference behavior (cost model)**
   - If multiple eligible resources exist, the cost model can steer selection (e.g., prefer stronger expertise match).
   - The result should show trade-offs (e.g., better match vs. travel time), depending on the configured cost weighting.

3. **Performance mindset**
   - Even though the instance is small, the pattern scales: skill checks remain fast because they use bitset inclusion.

---

## Customization patterns

### Add or rename skills
- Keep skill keys stable (constants) and reuse them across nodes/resources.
- Treat the skill catalog as part of your data contract (especially when using REST clients).

### Switch between hard and soft behavior
- **Hard requirement:** used when “must have” (certification, equipment, compliance).
- **Soft preference:** used when “should have” (best-fit dispatching, cost/quality trade-offs).

### Expertise levels (minimum / maximum)
BitType can represent expertise requirements such as:
- **minimum requirement**: resource level must be ≥ required level
- **maximum requirement**: resource level must be ≤ required level

Use this for:
- “only senior technicians”
- “avoid overqualified resources for simple jobs” (if desired)

---

## Related documentation

- [Skill cost model overview](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/skill_costmodel)
- [BitType Whitepaper (concept + benefits)](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/bittype_condition)
