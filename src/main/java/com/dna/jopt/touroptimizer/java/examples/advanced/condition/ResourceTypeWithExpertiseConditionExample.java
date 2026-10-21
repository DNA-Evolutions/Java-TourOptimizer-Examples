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
 * Requires a minimum expertise level of a skill, once as a hard and once as a soft constraint.
 *
 * <p>Scenario: three resources with the skill Repair work on May 6, 2025, from 08:00 to 17:00
 * (Europe/Berlin): Jack (level 10) and Paula (level 4) from Aachen, and John (level 2) from
 * Oberhausen. Koeln needs level 8 or more, as a hard constraint; Oberhausen needs level 3 or
 * more, as a soft constraint.
 *
 * <p>Look for: Koeln and Oberhausen on Jack's route. John starts at Oberhausen but does not visit
 * it, because his level is below the soft minimum.
 *
 * <p>Prerequisites: None: without a license file the example uses the public evaluation license
 * (see ExampleLicenseHelper).
 *
 * @see <a href="https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/condition/ResourceType.md">ResourceType.md</a>
 */
public class ResourceTypeWithExpertiseConditionExample extends Optimization {

  /** The skill type that the resources offer and the nodes require. */
  private static final String SKILL_TYPE = "Repair";

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
    new ResourceTypeWithExpertiseConditionExample().example();
  }

  @Override
  public String toString() {
    return "Requires a minimum expertise level of a skill, once as a hard and once as a soft"
        + " constraint.";
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
    addNodes(this);
    addResources(this);
    attachToObservables(this);
    startAndPresentResult(this);
  }

  /**
   * Adds the properties that keep the run short and weight a missed expertise level.
   *
   * @param opti the optimization
   */
  private static void addProperties(IOptimization opti) {
    Properties props = new Properties();
    // Fewer generations and iterations than the defaults keep the run short.
    props.setProperty("JOptExitCondition.JOptGenerationCount", "1000");
    props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumIterations", "100000");
    // A higher NodeType weight makes a missed soft level cost more against the driving costs.
    props.setProperty("JOptWeight.NodeType", "10.0");
    opti.addElement(props);
  }

  /**
   * Adds Koeln with a hard and Oberhausen with a soft minimum level of the skill Repair.
   *
   * @param opti the optimization
   */
  private static void addNodes(IOptimization opti) {
    List<IOpeningHours> openingHours = new ArrayList<>();
    openingHours.add(
        new OpeningHours(
            ZonedDateTime.of(2025, MAY.getValue(), 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2025, MAY.getValue(), 6, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    // Each visit takes 60 minutes and counts toward the working time, like the driving.
    Duration visitDuration = Duration.ofMinutes(60);

    // The same importance for every node, so no node is preferred over another.
    int importance = 1;

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

    INode koeln =
        new TimeWindowGeoNode("Koeln", 50.9333, 6.95, openingHours, visitDuration, importance);
    koeln.addConstraint(highLevelConstraint);
    opti.addElement(koeln);

    INode oberhausen =
        new TimeWindowGeoNode(
            "Oberhausen", 51.4667, 6.85, openingHours, visitDuration, importance);
    oberhausen.addConstraint(mediumLevelConstraint);
    opti.addElement(oberhausen);
  }

  /**
   * Adds Jack, John and Paula, each with the skill Repair at a level of their own.
   *
   * @param opti the optimization
   */
  private static void addResources(IOptimization opti) {
    // The levels are sample values.
    int jackLevel = 10;
    int johnLevel = 2;
    int paulaLevel = 4;

    TypeWithExpertiseQualification jackQualification = new TypeWithExpertiseQualification();
    jackQualification.addType(SKILL_TYPE, jackLevel);

    TypeWithExpertiseQualification johnQualification = new TypeWithExpertiseQualification();
    johnQualification.addType(SKILL_TYPE, johnLevel);

    TypeWithExpertiseQualification paulaQualification = new TypeWithExpertiseQualification();
    paulaQualification.addType(SKILL_TYPE, paulaLevel);

    // 10 h per day, a sample value: the plan needs far less.
    Duration maxWorkingTime = Duration.ofHours(10);
    // 1200 km per day, more than the longest day of this plan needs.
    Quantity<Length> maxDistanceKmW = Quantities.getQuantity(1200.0, KILO(METRE));

    IResource jack =
        new CapacityResource(
            "Jack Aachen - Expertise " + jackLevel,
            50.775346,
            6.083887,
            maxWorkingTime,
            maxDistanceKmW,
            createWorkingHours());
    jack.addQualification(jackQualification);
    opti.addElement(jack);

    IResource john =
        new CapacityResource(
            "John Oberhausen - Expertise " + johnLevel,
            51.4667,
            6.85,
            maxWorkingTime,
            maxDistanceKmW,
            createWorkingHours());
    john.addQualification(johnQualification);
    opti.addElement(john);

    IResource paula =
        new CapacityResource(
            "Paula Aachen - Expertise " + paulaLevel,
            50.775346,
            6.083887,
            maxWorkingTime,
            maxDistanceKmW,
            createWorkingHours());
    paula.addQualification(paulaQualification);
    opti.addElement(paula);
  }

  /**
   * Creates the working hours of a resource: 08:00 to 17:00 on May 6, 2025.
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
