package com.dna.jopt.touroptimizer.java.examples.advanced.condition;
/*-
 * #%L
 * JOpt TourOptimizer Examples
 * %%
 * Copyright (C) 2017 - 2023 DNA Evolutions GmbH
 * %%
 * This file is subject to the terms and conditions defined in file 'src/main/resources/LICENSE.txt',
 * which is part of this repository.
 *
 * If not, see <https://www.dna-evolutions.com/>.
 * #L%
 */
import static javax.measure.MetricPrefix.KILO;
import static tech.units.indriya.unit.Units.METRE;
import static java.time.Month.MAY;

import tech.units.indriya.quantity.Quantities;

import javax.measure.Quantity;
import javax.measure.quantity.Length;

import java.io.IOException;
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

import com.dna.jopt.framework.body.IOptimization;
import com.dna.jopt.framework.body.Optimization;
import com.dna.jopt.framework.exception.caught.InvalidLicenceException;
import com.dna.jopt.framework.outcomewrapper.IOptimizationResult;
import com.dna.jopt.member.unit.condition.typewithexpertise.TypeWithExpertiseConstraint;
import com.dna.jopt.member.unit.condition.typewithexpertise.TypeWithExpertiseConstraint.SkillWithExpertiseCostModel;
import com.dna.jopt.member.unit.condition.typewithexpertise.TypeWithExpertiseQualification;
import com.dna.jopt.member.unit.hours.IOpeningHours;
import com.dna.jopt.member.unit.hours.IWorkingHours;
import com.dna.jopt.member.unit.hours.OpeningHours;
import com.dna.jopt.member.unit.hours.WorkingHours;
import com.dna.jopt.member.unit.node.INode;
import com.dna.jopt.member.unit.node.geo.TimeWindowGeoNode;
import com.dna.jopt.member.unit.resource.CapacityResource;
import com.dna.jopt.member.unit.resource.IResource;
import com.dna.jopt.touroptimizer.java.examples.ExampleLicenseHelper;

/**
 * Requires skills with minimum and maximum expertise levels and ranks the resources that meet
 * them with a cost model for each requirement.
 *
 * <p>Scenario: three slaters, Jack and Paula from Aachen and John from Oberhausen, each with a
 * repair level and a weight level, serve four customers in Oberhausen, Koeln, Wuppertal and
 * Aachen on May 6, 2025, from 08:00 to 17:00 (Europe/Berlin). Each customer needs a minimum
 * repair level and accepts a maximum weight level.
 *
 * <p>Look for: Koeln goes to Jack, Wuppertal and Aachen go to Paula, Oberhausen goes to John. The
 * costs of the cost models show as Cost NodeType in the route summaries. The node names show the
 * required levels, the resource names the offered ones.
 *
 * <p>Prerequisites: None: without a license file the example uses the public evaluation license
 * (see ExampleLicenseHelper).
 *
 * @see <a href="https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/condition/ResourceType.md">ResourceType.md</a>
 */
public class ResourceTypeWithExpertiseConditionAndCostModelExample extends Optimization {

  /** The skill type of roof repair and cleaning. */
  private static final String SKILL_TYPE_REPAIR = "Roof_Repair_Clean";

  /** The skill type of the weight of a slater with equipment. */
  private static final String SKILL_TYPE_WEIGHT = "Weight_Including_Equipment";

  /** The same importance for every customer, so no customer is preferred over another. */
  private static final int IMPORTANCE = 1;

  /**
   * Runs the example.
   *
   * @param args not used
   * @throws InterruptedException if the wait for the result is interrupted
   * @throws ExecutionException if the optimization run fails
   * @throws TimeoutException if the run does not finish within the wait
   * @throws InvalidLicenceException if the license is not valid
   * @throws IOException if a file cannot be read or written
   */
  public static void main(String[] args)
      throws InterruptedException, ExecutionException, TimeoutException, InvalidLicenceException,
      IOException {
    new ResourceTypeWithExpertiseConditionAndCostModelExample().example();
  }

  @Override
  public String toString() {
    return "Requires skills with minimum and maximum expertise levels and ranks the resources that"
        + " meet them with a cost model for each requirement.";
  }

  /**
   * Builds the optimization, runs it and prints the result.
   *
   * @throws InterruptedException if the wait for the result is interrupted
   * @throws ExecutionException if the optimization run fails
   * @throws TimeoutException if the run does not finish within the wait
   * @throws InvalidLicenceException if the license is not valid
   * @throws IOException if a file cannot be read or written
   */
  public void example()
      throws InterruptedException, ExecutionException, TimeoutException, InvalidLicenceException,
      IOException {
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
  }

  /**
   * Adds the properties that keep the run short and weight the costs of the skill levels.
   *
   * @param opti the optimization
   */
  private static void addProperties(IOptimization opti) {
    Properties props = new Properties();
    // Fewer generations and iterations than the defaults keep the run short.
    props.setProperty("JOptExitCondition.JOptGenerationCount", "1000");
    props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumIterations", "100000");
    // A higher NodeType weight makes the costs of the cost models and of missed levels count more
    // against the driving costs.
    props.setProperty("JOptWeight.NodeType", "10.0");
    opti.addElement(props);
  }

  /**
   * Adds Oberhausen: repair level 2 or more as a soft requirement that prefers the highest level,
   * and weight level 10 or less as a hard limit.
   *
   * @param opti the optimization
   */
  private static void addCustomerOberhausen(IOptimization opti) {
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

    // 260 minutes: together with Koeln (280 minutes) and the drives, Jack's day would take longer
    // than his maximal working time.
    Duration visitDuration = Duration.ofMinutes(260);

    INode oberhausen =
        new TimeWindowGeoNode(
            "Oberhausen (R>=" + minRepairLevel + ", W<=" + maxWeightLevel + ")",
            51.4667,
            6.85,
            createOpeningHours(),
            visitDuration,
            IMPORTANCE);
    oberhausen.addConstraint(repairConstraint);
    oberhausen.addConstraint(weightConstraint);
    opti.addElement(oberhausen);
  }

  /**
   * Adds Koeln: repair level 8 or more and weight level 6 or less, both hard, in one constraint.
   *
   * @param opti the optimization
   */
  private static void addCustomerKoeln(IOptimization opti) {
    // Both requirements share one cost model, so one constraint holds both. Any slater who meets
    // both limits is fine. The levels of the customer are sample values.
    int minRepairLevel = 8;
    int maxWeightLevel = 6;
    TypeWithExpertiseConstraint constraint = new TypeWithExpertiseConstraint();
    constraint.setSkillCostingModel(SkillWithExpertiseCostModel.NO_PENALIZE_MATCHING_SKILL);
    constraint.addType(SKILL_TYPE_REPAIR, minRepairLevel);
    constraint.addType(SKILL_TYPE_WEIGHT, maxWeightLevel, false);
    // No slater below repair level 8 or above weight level 6.
    constraint.setIsHard(true);

    // 280 minutes: together with Oberhausen and the drives, Jack's day would take longer than his
    // maximal working time (see addCustomerOberhausen).
    Duration visitDuration = Duration.ofMinutes(280);

    INode koeln =
        new TimeWindowGeoNode(
            "Koeln (R>=" + minRepairLevel + ", W<=" + maxWeightLevel + ")",
            50.9333,
            6.95,
            createOpeningHours(),
            visitDuration,
            IMPORTANCE);
    koeln.addConstraint(constraint);
    opti.addElement(koeln);
  }

  /**
   * Adds Wuppertal: repair level 5 or more, as close to 5 as possible, and weight level 4 or
   * less, both hard.
   *
   * @param opti the optimization
   */
  private static void addCustomerWuppertal(IOptimization opti) {
    // The customer pays for the slater and wants a close match: HIGH_DELTA adds a cost that grows
    // with the gap between the level of the slater and the required level. The levels of the
    // customer are sample values.
    int minRepairLevel = 5;
    TypeWithExpertiseConstraint repairConstraint = new TypeWithExpertiseConstraint();
    repairConstraint.setSkillCostingModel(
        SkillWithExpertiseCostModel.PENALIZE_MATCHING_SKILL_HIGH_DELTA);
    repairConstraint.addType(SKILL_TYPE_REPAIR, minRepairLevel);
    // No slater below repair level 5.
    repairConstraint.setIsHard(true);

    int maxWeightLevel = 4;
    TypeWithExpertiseConstraint weightConstraint = new TypeWithExpertiseConstraint();
    weightConstraint.setSkillCostingModel(SkillWithExpertiseCostModel.NO_PENALIZE_MATCHING_SKILL);
    weightConstraint.addType(SKILL_TYPE_WEIGHT, maxWeightLevel, false);
    // No slater above weight level 4.
    weightConstraint.setIsHard(true);

    // 200 minutes: together with Aachen (120 minutes) and the drives, it fits into Paula's day.
    Duration visitDuration = Duration.ofMinutes(200);

    INode wuppertal =
        new TimeWindowGeoNode(
            "Wuppertal (R>=" + minRepairLevel + ", W<=" + maxWeightLevel + ")",
            51.2667,
            7.18333,
            createOpeningHours(),
            visitDuration,
            IMPORTANCE);
    wuppertal.addConstraint(repairConstraint);
    wuppertal.addConstraint(weightConstraint);
    opti.addElement(wuppertal);
  }

  /**
   * Adds Aachen: repair level 3 or more, as close to 3 as possible, and weight level 6 or less,
   * both hard.
   *
   * @param opti the optimization
   */
  private static void addCustomerAachen(IOptimization opti) {
    // HIGH_DELTA, as in Wuppertal: of the slaters who meet the level, the closest match costs
    // least. The levels of the customer are sample values.
    int minRepairLevel = 3;
    TypeWithExpertiseConstraint repairConstraint = new TypeWithExpertiseConstraint();
    repairConstraint.setSkillCostingModel(
        SkillWithExpertiseCostModel.PENALIZE_MATCHING_SKILL_HIGH_DELTA);
    repairConstraint.addType(SKILL_TYPE_REPAIR, minRepairLevel);
    // No slater below repair level 3.
    repairConstraint.setIsHard(true);

    int maxWeightLevel = 6;
    TypeWithExpertiseConstraint weightConstraint = new TypeWithExpertiseConstraint();
    weightConstraint.setSkillCostingModel(SkillWithExpertiseCostModel.NO_PENALIZE_MATCHING_SKILL);
    weightConstraint.addType(SKILL_TYPE_WEIGHT, maxWeightLevel, false);
    // No slater above weight level 6.
    weightConstraint.setIsHard(true);

    // 120 minutes: together with Wuppertal (200 minutes) and the drives, it fits into Paula's day.
    Duration visitDuration = Duration.ofMinutes(120);

    INode aachen =
        new TimeWindowGeoNode(
            "Aachen (R>=" + minRepairLevel + ", W<=" + maxWeightLevel + ")",
            50.775346,
            6.083887,
            createOpeningHours(),
            visitDuration,
            IMPORTANCE);
    aachen.addConstraint(repairConstraint);
    aachen.addConstraint(weightConstraint);
    opti.addElement(aachen);
  }

  /**
   * Adds the three slaters with their repair and weight levels.
   *
   * @param opti the optimization
   */
  private static void addResources(IOptimization opti) {
    // The repair and weight levels are sample values.
    int jackRepairLevel = 10;
    int jackWeightLevel = 5;

    int johnRepairLevel = 2;
    int johnWeightLevel = 8;

    int paulaRepairLevel = 5;
    int paulaWeightLevel = 2;

    TypeWithExpertiseQualification jackQualification = new TypeWithExpertiseQualification();
    jackQualification.addType(SKILL_TYPE_REPAIR, jackRepairLevel);
    jackQualification.addType(SKILL_TYPE_WEIGHT, jackWeightLevel);

    TypeWithExpertiseQualification johnQualification = new TypeWithExpertiseQualification();
    johnQualification.addType(SKILL_TYPE_REPAIR, johnRepairLevel);
    johnQualification.addType(SKILL_TYPE_WEIGHT, johnWeightLevel);

    TypeWithExpertiseQualification paulaQualification = new TypeWithExpertiseQualification();
    paulaQualification.addType(SKILL_TYPE_REPAIR, paulaRepairLevel);
    paulaQualification.addType(SKILL_TYPE_WEIGHT, paulaWeightLevel);

    // 10 h per day, a sample value: Koeln and Oberhausen together take longer, so Jack would
    // work overtime to serve both.
    Duration maxWorkingTime = Duration.ofHours(10);
    // 1200 km per day, more than a day of this plan needs.
    Quantity<Length> maxDistanceKmW = Quantities.getQuantity(1200.0, KILO(METRE));

    IResource jack =
        new CapacityResource(
            "Jack Aachen - Repair: " + jackRepairLevel + " Weight: " + jackWeightLevel,
            50.775346,
            6.083887,
            maxWorkingTime,
            maxDistanceKmW,
            createWorkingHours());
    jack.addQualification(jackQualification);
    opti.addElement(jack);

    IResource john =
        new CapacityResource(
            "John Oberhausen - Repair: " + johnRepairLevel + " Weight: " + johnWeightLevel,
            51.4667,
            6.85,
            maxWorkingTime,
            maxDistanceKmW,
            createWorkingHours());
    john.addQualification(johnQualification);
    opti.addElement(john);

    IResource paula =
        new CapacityResource(
            "Paula Aachen - Repair: " + paulaRepairLevel + " Weight: " + paulaWeightLevel,
            50.775346,
            6.083887,
            maxWorkingTime,
            maxDistanceKmW,
            createWorkingHours());
    paula.addQualification(paulaQualification);
    opti.addElement(paula);
  }

  /**
   * Creates the opening hours of a customer: 08:00 to 17:00 on May 6, 2025.
   *
   * @return the opening hours
   */
  private static List<IOpeningHours> createOpeningHours() {
    List<IOpeningHours> openingHours = new ArrayList<>();
    openingHours.add(
        new OpeningHours(
            ZonedDateTime.of(2025, MAY.getValue(), 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2025, MAY.getValue(), 6, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));
    return openingHours;
  }

  /**
   * Creates the working hours of a slater: 08:00 to 17:00 on May 6, 2025.
   *
   * @return the working hours
   */
  private static List<IWorkingHours> createWorkingHours() {
    List<IWorkingHours> workingHours = new ArrayList<>();
    workingHours.add(
        new WorkingHours(
            ZonedDateTime.of(2025, MAY.getValue(), 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2025, MAY.getValue(), 6, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));
    return workingHours;
  }

  /**
   * Prints the progress, warning, status and error events of the run.
   *
   * @param opti the optimization
   */
  private static void attachToObservables(IOptimization opti) {
    // Subscribe before the start: the subjects replay only a few past events.
    opti.getOptimizationEvents()
        .progressSubject()
        .subscribe(p -> System.out.println(p.getProgressString()));
    opti.getOptimizationEvents().warningSubject().subscribe(w -> System.out.println(w.toString()));
    opti.getOptimizationEvents().statusSubject().subscribe(s -> System.out.println(s.toString()));
    opti.getOptimizationEvents().errorSubject().subscribe(e -> System.out.println(e.toString()));
  }

  /**
   * Starts the run, waits for the result and prints it.
   *
   * @param opti the optimization
   * @throws InterruptedException if the wait for the result is interrupted
   * @throws ExecutionException if the optimization run fails
   * @throws TimeoutException if the run does not finish within the wait
   * @throws InvalidLicenceException if the license is not valid
   */
  private static void startAndPresentResult(IOptimization opti)
      throws InterruptedException, ExecutionException, TimeoutException, InvalidLicenceException {
    CompletableFuture<IOptimizationResult> resultFuture = opti.startRunAsync();
    // Wait for the result; without the wait the JVM would end before the run does.
    IOptimizationResult result = resultFuture.get(5, TimeUnit.MINUTES);
    System.out.println(result);
  }
}
