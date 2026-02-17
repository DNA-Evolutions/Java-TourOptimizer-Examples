package com.dna.jopt.touroptimizer.java.examples.advanced.condition.bittype;
/*-
 * #%L
 * JOpt TourOptimizer Examples
 * %%
 * Copyright (C) 2017 - 2020 DNA Evolutions GmbH
 * %%
 * This file is subject to the terms and conditions defined in file 'src/main/resources/LICENSE.txt',
 * which is part of this repository.
 *
 * If not, see <https://www.dna-evolutions.com/>.
 * #L%
 */

import static javax.measure.MetricPrefix.KILO;
import static tech.units.indriya.unit.Units.METRE;

import java.io.IOException;
import java.io.PrintStream;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.time.Month.MAY;

import javax.measure.Quantity;
import javax.measure.quantity.Length;

import com.dna.jopt.framework.body.IOptimization;
import com.dna.jopt.framework.body.Optimization;
import com.dna.jopt.framework.exception.caught.InvalidLicenceException;
import com.dna.jopt.framework.outcomewrapper.IOptimizationResult;
import com.dna.jopt.member.unit.hours.IWorkingHours;
import com.dna.jopt.member.unit.condition.bittypewithexpertise.BitTypeWithExpertiseConstraint;
import com.dna.jopt.member.unit.condition.bittypewithexpertise.BitTypeWithExpertiseQualification;
import com.dna.jopt.member.unit.condition.bittypewithexpertise.TypeLevelOffering;
import com.dna.jopt.member.unit.condition.bittypewithexpertise.TypeLevelRequirement;
import com.dna.jopt.member.unit.condition.bittypewithexpertise.TypeLevelRequirement.SkillInfoCostModel;
import com.dna.jopt.member.unit.hours.IOpeningHours;
import com.dna.jopt.member.unit.hours.WorkingHours;
import com.dna.jopt.member.unit.hours.OpeningHours;
import com.dna.jopt.member.unit.node.INode;
import com.dna.jopt.member.unit.node.geo.TimeWindowGeoNode;
import com.dna.jopt.member.unit.resource.CapacityResource;
import com.dna.jopt.member.unit.resource.IResource;
import com.dna.jopt.touroptimizer.java.examples.ExampleLicenseHelper;

import tech.units.indriya.quantity.Quantities;

/**
 * 
 * Updated with BitType. BitType covers both, skills with and without levels and even mixed modes.
 * 
 * This example delves into a comprehensive scenario where the Resource-type-Expertise condition is
 * applied alongside a cost-model that accommodates both minimum and maximum expertise levels.
 * 
 * Consider a company that specializes in roof repair and cleaning services for various customers.
 * Customers have the option to specify the expertise level required for the slater assigned to their
 * job. The expertise levels are categorized as follows: a level from 1 to 5 signifies a journeyman, 
 * whereas a level from 5 to 10 indicates a master-level expertise. Moreover, certain customers have 
 * specifications regarding the maximum permissible weight for the slater, inclusive of their equipment.
 * In this context, a weight level from 1 to 3 corresponds to a maximum of 100 kg, a level from 3 to 6 
 * allows for up to 125 kg, and any level above 6 is capped at 150 kg.
 * 
 * The company employs three slaters, each with distinct levels of expertise: Jack boasts a top-tier 
 * expertise level of 10, John is at level 2, reflecting a novice skillset, and Paula is at an intermediate 
 * level of 5.
 * 
 * The task at hand involves catering to four distinct customers, each with unique service requirements
 * and expectations.
 * 
 * 
 * Cost-Models:
 * ====
 * ====
 * 
 * The cost model in the example is a key part of the optimization algorithm
 * used to determine the most suitable resource (in this case, a slater) for a
 * job based on expertise levels. The cost model dictates how the optimization
 * process should consider the match between the slater's expertise level and
 * the customer's requirements, impacting the overall cost of assigning a
 * particular slater to a job. Here's how each cost model affects the
 * assignment:
 * 
 * 
 * SkillInfoCostModel.NO_PENALIZE_MATCHING_SKILL:
 * ===
 * 
 * Situation: The customer requires a slater with at least level 5 expertise.
 * 
 * Resource Provided: Slater with level 8 expertise. 
 * 
 * Cost Implication: No
 * additional cost is incurred for assigning a slater with higher expertise than
 * required. The main goal is to meet or exceed the minimum requirement.
 * 
 * 
  * SkillInfoCostModel.PENALIZE_MATCHING_SKILL_LOW_DELTA:
 * ===
 *
 * Situation: The customer requires a slater with at least level 5 expertise (minimum mode).
 *
 * Intention: Prefer the <em>highest qualified</em> feasible resource. Assigning a more qualified slater is
 * considered better and should reduce the penalty.
 *
 * Cost Implication: The penalty is computed relative to the global maximum level, i.e. it is proportional to
 * (<code>GLOBAL_MAX_LEVEL - ACTUAL_LEVEL</code>). Example with <code>GLOBAL_MAX_LEVEL = 10</code>:
 * a slater with level 7 yields a higher penalty (10 - 7 = 3) than a slater with level 9 (10 - 9 = 1).
 *
 * Note on the name: "low delta" refers to a small delta between <em>actual</em> and <em>required</em> level
 * (e.g. 7 - 5 = 2) which is typically a <em>lower</em> qualification and is therefore penalized more than a
 * higher delta (e.g. 9 - 5 = 4).
 *
 *
 * SkillInfoCostModel.PENALIZE_MATCHING_SKILL_HIGH_DELTA:
 * ===
 * 
 * Situation: The customer is willing to pay for the slater's services and seeks
 * an exact or close match to their required expertise level of 5. 
 * 
 * Resource Provided: Slater with level 8 expertise. 
 * 
 * Cost Implication: There is a higher
 * penalty cost for the difference between the slater's level and the required
 * level because the customer is looking for the best match possible. In this
 * case, a slater with level 5 expertise would be the ideal candidate, and
 * assigning one with level 8 incurs extra costs, reflecting the customer's
 * preference for a precise match. 
 * 
 * 
 * In optimization terms, these cost models are
 * used to calculate the "cost" or "penalty" for resource assignments during the
 * optimization process. These costs influence the optimization algorithm's
 * decision-making process to find the most cost-effective match between
 * resources and jobs based on the defined constraints and preferences.
 * 
 *
 * @author DNA
 * @version Feb 11, 2026
 * @since Feb 11, 2026
 */
public class BitTypeWithExpertiseConditionAndCostModelExample extends Optimization {

    /** Dictionary key for the roof repair / cleaning skill used in this example. */
    private static final String SKILL_TYPE_REPAIR = "Roof_Repair_Clean";

    /** Dictionary key for the worker weight (incl. equipment) skill used in this example. */
    private static final String SKILL_TYPE_WEIGHT = "Weight_Including_Equimpment";

    /**
     * The main method.
     *
     * @param args the arguments
     * @throws InterruptedException    the interrupted exception
     * @throws ExecutionException      the execution exception
     * @throws InvalidLicenceException the invalid licence exception
     * @throws IOException             Signals that an I/O exception has occurred.
     * @throws TimeoutException        the timeout exception
     */
    public static void main(String[] args)
	    throws InterruptedException, ExecutionException, InvalidLicenceException, IOException, TimeoutException {
	new BitTypeWithExpertiseConditionAndCostModelExample().example();
    }


    /**
     * Example.
     *
     * @throws InterruptedException    the interrupted exception
     * @throws ExecutionException      the execution exception
     * @throws InvalidLicenceException the invalid licence exception
     * @throws IOException             Signals that an I/O exception has occurred.
     * @throws TimeoutException        the timeout exception
     */
    public void example()
	    throws InterruptedException, ExecutionException, InvalidLicenceException, IOException, TimeoutException {

	// Set license via helper
	ExampleLicenseHelper.setLicense(this);

	// Set the Properties
	this.setProperties(this);

	// Add the different customers with different requirements and cost models.
	BitTypeWithExpertiseConditionAndCostModelExample.addCustomerOberhausen(this);
	BitTypeWithExpertiseConditionAndCostModelExample.addCustomerCologne(this);
	BitTypeWithExpertiseConditionAndCostModelExample.addCustomerWuppertal(this);
	BitTypeWithExpertiseConditionAndCostModelExample.addCustomerAachen(this);

	BitTypeWithExpertiseConditionAndCostModelExample.addResources(this);

	// Start the Optimization
	CompletableFuture<IOptimizationResult> resultFuture = this.startRunAsync();

	// Subscribe to events
	subscribeToEvents(this);

	// It is important to block the call, otherwise the optimization will be
	// terminated
	resultFuture.get(1, TimeUnit.MINUTES);
    }

    /**
     * Configures algorithm and weighting properties for the run.
     * <p>
     * In particular, {@code JOptWeight.NodeType} influences how strongly the optimizer prefers not to violate
     * soft skill constraints (i.e., when {@code setIsHard(false)} is used).
     *
     * @param opti optimization instance to which properties are added
     */
    private void setProperties(IOptimization opti) {

	Properties props = new Properties();

	props.setProperty("JOptExitCondition.JOptGenerationCount", "2000");
	props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumIterations", "100000");
	props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumRepetions", "1");

	// The NodeType setting also influences how important it is not to violate the
	// minimal
	// required Expertise level - In case we use a soft constraint.
	props.setProperty("JOptWeight.NodeType", "10.0"); // Default is 1.0
	props.setProperty("JOpt.NumCPUCores", "4");

	opti.addElement(props);
    }

    /**
     * Adds the customer node for <em>Oberhausen</em> with mixed requirements:
     * <ul>
     *   <li><strong>Repair skill</strong> with a <em>minimum</em> expertise level (soft).</li>
     *   <li><strong>Weight</strong> with a <em>maximum</em> allowed level (hard / legal).</li>
     * </ul>
     * The repair constraint uses {@link SkillInfoCostModel#PENALIZE_MATCHING_SKILL_LOW_DELTA} to prefer the
     * most qualified feasible resource (closest to the global maximum expertise), while still allowing lower
     * (but acceptable) expertise when needed because the constraint is soft.
     *
     * @param opti optimization instance to which the node is added
     */
    private static void addCustomerOberhausen(IOptimization opti) {

	/*
	 * 
	 * Oberhausen needs: - At least a repair qualification at level 3 but should be
	 * as high as possible (soft constraint) - A slater with a weight level of
	 * maximal 4 (hard constraint)
	 * 
	 */

	/*
	 * OpeningHours and duration
	 */
	List<IOpeningHours> weeklyOpeningHours = new ArrayList<>();
	weeklyOpeningHours
		.add(new OpeningHours(ZonedDateTime.of(2025, MAY.getValue(), 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
			ZonedDateTime.of(2025, MAY.getValue(), 6, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

	Duration visitDuration = Duration.ofMinutes(260);

	// The customer wants a resource that is highly qualified -
	// LOW_DELTA prefers the highest feasible expertise level; penalty ∝ (GLOBAL_MAX_LEVEL - ACTUAL_LEVEL).
	SkillInfoCostModel acceptedModelRepair = SkillInfoCostModel.PENALIZE_MATCHING_SKILL_LOW_DELTA;

	// This Expertise constraint is implemented as a soft Constraint.
	int minEpxertiseLevelMedium = 2;

	BitTypeWithExpertiseConstraint repairConstraintMediumExpertise = new BitTypeWithExpertiseConstraint();
	repairConstraintMediumExpertise.setSkillCostingModel(acceptedModelRepair);
	
	
	// If a node does not have a min requirment, do not provide it to save performance. If a node does not have a level requirment, any resource
	// with the right skill is applicable.
	TypeLevelRequirement req = TypeLevelRequirement.of(minEpxertiseLevelMedium, true);
	
	repairConstraintMediumExpertise.addDictType(SKILL_TYPE_REPAIR, req);

	/*
	 * 
	 */

	int maxWeightLevelMedium = 10;
	boolean weightIsMaxLevel = true;

	// We just want to fulfil the weight requirement
	SkillInfoCostModel acceptedModelWeight = SkillInfoCostModel.NO_PENALIZE_MATCHING_SKILL;

	BitTypeWithExpertiseConstraint weightConstraintMediumExpertise = new BitTypeWithExpertiseConstraint();
	
	weightConstraintMediumExpertise.setSkillCostingModel(acceptedModelWeight);
	
	TypeLevelRequirement req2 = TypeLevelRequirement.of(maxWeightLevelMedium, !weightIsMaxLevel);
	repairConstraintMediumExpertise.addDictType(SKILL_TYPE_WEIGHT, req2);
	
	weightConstraintMediumExpertise.setIsHard(true); // Try to send matching expertise level

	INode oberhausen = new TimeWindowGeoNode(
		"Oberhausen (R>=" + minEpxertiseLevelMedium + ", W<=" + maxWeightLevelMedium + ")", 51.4667, 6.85,
		weeklyOpeningHours, visitDuration, 1);
	oberhausen.addConstraint(repairConstraintMediumExpertise);
	oberhausen.addConstraint(weightConstraintMediumExpertise);
	opti.addElement(oberhausen);
    }

    /**
     * Adds the customer node for <em>Wuppertal</em>. This customer demonstrates a repair requirement with
     * expertise handling and a cost model to steer the optimizer towards a desired match quality.
     *
     * @param opti optimization instance to which the node is added
     */
    private static void addCustomerWuppertal(IOptimization opti) {

	/*
	 * 
	 * Wuppertal needs: - At least a repair qualification at level 5 a perfect match
	 * is desirable (hard constraint) - A slater with a weight level of maximal 6
	 * (hard constraint)
	 * 
	 */

	/*
	 * OpeningHours and duration
	 */
	List<IOpeningHours> weeklyOpeningHours = new ArrayList<>();
	weeklyOpeningHours
		.add(new OpeningHours(ZonedDateTime.of(2025, MAY.getValue(), 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
			ZonedDateTime.of(2025, MAY.getValue(), 6, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

	Duration visitDuration = Duration.ofMinutes(200);

	// The customer pays the resource itself, therefore the customer wants a perfect
	// match - We penalize a high delta
	SkillInfoCostModel acceptedModelRepair = SkillInfoCostModel.PENALIZE_MATCHING_SKILL_HIGH_DELTA;

	// This Expertise constraint is implemented as a soft Constraint.
	int minEpxertiseLevelMedium = 5;

	BitTypeWithExpertiseConstraint repairConstraintMediumExpertise = new BitTypeWithExpertiseConstraint();
	repairConstraintMediumExpertise.setSkillCostingModel(acceptedModelRepair);
	
	TypeLevelRequirement req3 = TypeLevelRequirement.of(minEpxertiseLevelMedium, true);
	
	repairConstraintMediumExpertise.addDictType(SKILL_TYPE_REPAIR,req3);
	
	repairConstraintMediumExpertise.setIsHard(true); // Try to send matching expertise level

	/*
	 * 
	 */

	int maxWeightLevelMedium = 4;
	boolean weightIsMaxLevel = true;

	// We just want to fulfil the weight requirement
	SkillInfoCostModel acceptedModelWeight = SkillInfoCostModel.NO_PENALIZE_MATCHING_SKILL;

	BitTypeWithExpertiseConstraint weightConstraintMediumExpertise = new BitTypeWithExpertiseConstraint();
	
	weightConstraintMediumExpertise.setSkillCostingModel(acceptedModelWeight);
	
	
	TypeLevelRequirement req4 = TypeLevelRequirement.of(maxWeightLevelMedium, !weightIsMaxLevel);
	
	weightConstraintMediumExpertise.addDictType(SKILL_TYPE_WEIGHT, req4);
	weightConstraintMediumExpertise.setIsHard(true); // Try to send matching expertise level

	INode wuppertal = new TimeWindowGeoNode(
		"Wuppertal (R>=" + minEpxertiseLevelMedium + ", W<=" + maxWeightLevelMedium + ")", 51.2667, 7.18333,
		weeklyOpeningHours, visitDuration, 1);
	wuppertal.addConstraint(repairConstraintMediumExpertise);
	wuppertal.addConstraint(weightConstraintMediumExpertise);
	opti.addElement(wuppertal);
    }

    /**
     * Adds the customer node for <em>Cologne</em>. This customer is configured to illustrate how a different
     * combination of expertise requirement (min/max) and cost model affects assignment preferences.
     *
     * @param opti optimization instance to which the node is added
     */
    private static void addCustomerCologne(IOptimization opti) {

	/*
	 * 
	 * Cologne needs: - At least a repair qualification at level 8 (hard constraint)
	 * - A slater with a weight level of maximal 6 (hard constraint)
	 * 
	 */

	/*
	 * OpeningHours and duration
	 */
	List<IOpeningHours> weeklyOpeningHours = new ArrayList<>();
	weeklyOpeningHours
		.add(new OpeningHours(ZonedDateTime.of(2025, MAY.getValue(), 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
			ZonedDateTime.of(2025, MAY.getValue(), 6, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

	Duration visitDuration = Duration.ofMinutes(280);

	// The customer accepts that any resource fulfilling the expertise is accepted.
	// Therefore, we can even cover both constraints in a single Constraint object
	// as they will share the same cost-model
	SkillInfoCostModel acceptedModel = SkillInfoCostModel.NO_PENALIZE_MATCHING_SKILL;

	// Defining the type Expertise Constraints for the Nodes
	int minEpxertiseLevelHigh = 8;
	int maxWeightLevelHigh = 6;

	// The weight is a max level and not the default min level constraint
	boolean weightIsMaxLevel = true;

	BitTypeWithExpertiseConstraint repairConstraintHighExpertise = new BitTypeWithExpertiseConstraint();
	repairConstraintHighExpertise.setSkillCostingModel(acceptedModel);

	
	repairConstraintHighExpertise.addDictType(SKILL_TYPE_REPAIR, TypeLevelRequirement.of(minEpxertiseLevelHigh, true));
	repairConstraintHighExpertise.addDictType(SKILL_TYPE_WEIGHT, TypeLevelRequirement.of(maxWeightLevelHigh, !weightIsMaxLevel));
	
	repairConstraintHighExpertise.setIsHard(true); // Forbidden to send Resource with low level

	// Define some Nodes and adding the Constraints to the Nodes
	INode koeln = new TimeWindowGeoNode("Koeln (R>=" + minEpxertiseLevelHigh + ", W<=" + maxWeightLevelHigh + ")",
		50.9333, 6.95, weeklyOpeningHours, visitDuration, 1);
	koeln.addConstraint(repairConstraintHighExpertise);
	opti.addElement(koeln);

    }

    /**
     * Adds the customer node for <em>Aachen</em>. This customer provides another variation of skill + expertise
     * constraints (hard/soft) to showcase how feasibility and scoring interact during optimization.
     *
     * @param opti optimization instance to which the node is added
     */
    private static void addCustomerAachen(IOptimization opti) {

	/*
	 * 
	 * Aachen needs: - At least a repair qualification at level 5 a perfect match is
	 * desirable (hard constraint) - A slater with a weight level of maximal 6 (hard
	 * constraint)
	 * 
	 */

	/*
	 * OpeningHours and duration
	 */
	List<IOpeningHours> weeklyOpeningHours = new ArrayList<>();
	weeklyOpeningHours
		.add(new OpeningHours(ZonedDateTime.of(2025, MAY.getValue(), 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
			ZonedDateTime.of(2025, MAY.getValue(), 6, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

	Duration visitDuration = Duration.ofMinutes(120);

	// The customer pays the resource itself, therefore the customer wants a perfect
	// match - We penalize a high delta
	SkillInfoCostModel acceptedModelRepair = SkillInfoCostModel.PENALIZE_MATCHING_SKILL_HIGH_DELTA;

	// This Expertise constraint is implemented as a soft Constraint.
	int minEpxertiseLevelMedium = 3;

	BitTypeWithExpertiseConstraint repairConstraintMediumExpertise = new BitTypeWithExpertiseConstraint();
	
	repairConstraintMediumExpertise.setSkillCostingModel(acceptedModelRepair);
	
	repairConstraintMediumExpertise.addDictType(SKILL_TYPE_REPAIR, TypeLevelRequirement.of(minEpxertiseLevelMedium, true));
	
	repairConstraintMediumExpertise.setIsHard(true); // Try to send matching expertise level

	/*
	 * 
	 */

	int maxWeightLevelMedium = 6;
	boolean weightIsMaxLevel = true;

	// We just want to fulfil the weight requirement
	SkillInfoCostModel acceptedModelWeight = SkillInfoCostModel.NO_PENALIZE_MATCHING_SKILL;

	BitTypeWithExpertiseConstraint weightConstraintMediumExpertise = new BitTypeWithExpertiseConstraint();
	
	weightConstraintMediumExpertise.setSkillCostingModel(acceptedModelWeight);
	
	weightConstraintMediumExpertise.addDictType(SKILL_TYPE_REPAIR, TypeLevelRequirement.of(maxWeightLevelMedium, !weightIsMaxLevel));
	
	
	weightConstraintMediumExpertise.setIsHard(true); // Try to send matching expertise level

	INode aachen = new TimeWindowGeoNode(
		"Aachen (R>=" + minEpxertiseLevelMedium + ", W<=" + maxWeightLevelMedium + ")", 50.775346, 6.083887,
		weeklyOpeningHours, visitDuration, 1);
	aachen.addConstraint(repairConstraintMediumExpertise);
	aachen.addConstraint(weightConstraintMediumExpertise);
	opti.addElement(aachen);
    }

    /*
     * 
     * 
     * 
     */

    /**
     * Adds example resources (slaters) including their {@link BitTypeWithExpertiseQualification} sets.
     * Each resource offers one or more dictionary-backed skills with an associated expertise level.
     * These qualifications are later matched against node requirements using fast bitset inclusion checks,
     * followed by (optional) expertise scoring via the configured cost model.
     *
     * @param opti optimization instance to which the resources are added
     */
    private static void addResources(IOptimization opti) {

	// Setting Qualifications with Expertise
	int jackExpertiseLevel = 10;
	int jackWeightLevel = 5;

	int johnExpertiseLevel = 2;
	int johnWeightLevel = 8;

	int paulaExpertiseLevel = 5;
	int paulaWeightLevel = 2;

	BitTypeWithExpertiseQualification jackQualification = new BitTypeWithExpertiseQualification();
	jackQualification.addDictType(SKILL_TYPE_REPAIR, TypeLevelOffering.of(jackExpertiseLevel));
	jackQualification.addDictType(SKILL_TYPE_WEIGHT, TypeLevelOffering.of(jackWeightLevel));

	BitTypeWithExpertiseQualification johnQualification = new BitTypeWithExpertiseQualification();
	johnQualification.addDictType(SKILL_TYPE_REPAIR, TypeLevelOffering.of(johnExpertiseLevel));
	johnQualification.addDictType(SKILL_TYPE_WEIGHT, TypeLevelOffering.of(johnWeightLevel));

	BitTypeWithExpertiseQualification paulaQualification = new BitTypeWithExpertiseQualification();
	paulaQualification.addDictType(SKILL_TYPE_REPAIR, TypeLevelOffering.of(paulaExpertiseLevel));
	paulaQualification.addDictType(SKILL_TYPE_WEIGHT, TypeLevelOffering.of(paulaWeightLevel));

	/*
	 * Defining Resources
	 */

	// Max Time and max Distance
	Duration maxWorkingTime = Duration.ofHours(10);
	Quantity<Length> maxDistanceKmW = Quantities.getQuantity(1200.0, KILO(METRE));

	// Add Resources and assign Qualification
	IResource resJack = new CapacityResource(
		"Jack Aachen - Repair: " + jackExpertiseLevel + " Weight: " + jackWeightLevel, 50.775346, 6.083887,
		maxWorkingTime, maxDistanceKmW, getDefaultWorkingHours());

	IResource resJohn = new CapacityResource(
		"John Oberhausen - Repair " + johnExpertiseLevel + " Weight: " + johnWeightLevel, 51.4667, 6.85,
		maxWorkingTime, maxDistanceKmW, getDefaultWorkingHours());

	IResource resPaula = new CapacityResource(
		"Paula Cologne - Repair " + paulaExpertiseLevel + " Weight: " + paulaWeightLevel, 50.775346, 6.083887,
		maxWorkingTime, maxDistanceKmW, getDefaultWorkingHours());

	// Adding the Qualifications to the Resources
	resJack.addQualification(jackQualification);
	resJohn.addQualification(johnQualification);
	resPaula.addQualification(paulaQualification);

	opti.addElement(resJack);
	opti.addElement(resJohn);
	opti.addElement(resPaula);
    }

    /**
     * Builds a default weekly working-hours template for resources used in this example.
     *
     * @return list of working-hours windows applied to example resources
     */
    private static List<IWorkingHours> getDefaultWorkingHours() {

	List<IWorkingHours> workingHours = new ArrayList<>();
	workingHours
		.add(new WorkingHours(ZonedDateTime.of(2025, MAY.getValue(), 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
			ZonedDateTime.of(2025, MAY.getValue(), 6, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

	return workingHours;
    }

    /**
     * Subscribes to optimization lifecycle events and prints relevant information to {@link System#out}.
     * This is purely for demonstration/debugging and does not influence the optimization logic.
     *
     * @param opti optimization instance whose event bus is used for subscriptions
     */
    private static void subscribeToEvents(IOptimization opti) {

	PrintStream myOut = System.out;

	// Subscribe to events
	opti.getOptimizationEvents().progressSubject().subscribe(p -> myOut.println(p.getProgressString()));

	opti.getOptimizationEvents().errorSubject().subscribe(e -> myOut.println(e.getCause() + " " + e.getCode()));

	opti.getOptimizationEvents().warningSubject().subscribe(w -> myOut.println(w.getDescription() + w.getCode()));

	opti.getOptimizationEvents().statusSubject()
		.subscribe(s -> myOut.println(s.getDescription() + " " + s.getCode()));

	opti.getOptimizationEvents().resultFuture().thenAccept(myOut::println);
    }
    

    /**
     * To string.
     *
     * @return the string
     */
    public String toString() {
	return "Setting a type/skill with min and max expertise and cost models for resources and nodes.";
    }
}