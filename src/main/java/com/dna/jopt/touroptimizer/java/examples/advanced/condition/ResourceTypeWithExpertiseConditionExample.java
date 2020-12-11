package com.dna.jopt.touroptimizer.java.examples.advanced.condition;
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

import static tec.units.ri.unit.MetricPrefix.KILO;
import static tec.units.ri.unit.Units.METRE;

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
import com.dna.jopt.member.unit.condition.typewithexpertise.TypeWithExpertiseConstraint;
import com.dna.jopt.member.unit.condition.typewithexpertise.TypeWithExpertiseQualification;
import com.dna.jopt.member.unit.hours.IOpeningHours;
import com.dna.jopt.member.unit.hours.WorkingHours;
import com.dna.jopt.member.unit.hours.OpeningHours;
import com.dna.jopt.member.unit.node.INode;
import com.dna.jopt.member.unit.node.geo.TimeWindowGeoNode;
import com.dna.jopt.member.unit.resource.CapacityResource;
import com.dna.jopt.member.unit.resource.IResource;
import com.dna.jopt.touroptimizer.java.examples.ExampleLicenseHelper;

import tec.units.ri.quantity.Quantities;

/**
 * In this example, we have a look at the Resource Type Expertise condition. An Expertise can
 * express the level of knowledge a Resource has in a certain type. In this example, we assume three
 * Resources: "Jack", "John", and "Paula" carry a Qualification with the type "Repair". However,
 * each resource has a different experience in that field, indicated by their individual expertise
 * level of 10 (Jack), 2 (John), and 4 (Paula). Two nodes "Koeln" and "Oberhausen", need a Resource
 * providing the RepairType. However, Koeln needs at least a Resource with expertise-level 8 (as a
 * hard constraint) and Oberhausen at least need an expertise-level of 3 (soft constraint) to be
 * successfully served,
 */
public class ResourceTypeWithExpertiseConditionExample extends Optimization {

  private static final String SKILL_TYPE = "Repair";

  public static void main(String[] args)
      throws InterruptedException, ExecutionException, InvalidLicenceException, IOException,
          TimeoutException {
    new ResourceTypeWithExpertiseConditionExample().example();
  }

  public String toString() {
    return "Setting a type/skill with Expertise for a resources.";
  }

  public void example()
      throws InterruptedException, ExecutionException, InvalidLicenceException, IOException,
          TimeoutException {

    // Set license via helper
    ExampleLicenseHelper.setLicense(this);

    // Properties!
    this.setProperties(this);

    this.addNodes(this);
    this.addResources(this);

    CompletableFuture<IOptimizationResult> resultFuture = this.startRunAsync();

    // Subscribe to events
    subscribeToEvents(this);

    // It is important to block the call, otherwise optimization will be terminated
    resultFuture.get(1, TimeUnit.MINUTES);
  }

  private void setProperties(IOptimization opti) {

    Properties props = new Properties();

    props.setProperty("JOptExitCondition.JOptGenerationCount", "2000");
    props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumIterations", "100000");
    props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumRepetions", "1");

    // The NodeType setting also influences how important it is not to violate the minimal
    // required expertise level - In case we use a soft constraint.
    props.setProperty("JOptWeight.NodeType", "10.0"); // Default is 1.0
    props.setProperty("JOpt.NumCPUCores", "4");

    opti.addElement(props);
  }

  private void addResources(IOptimization opti) {

    // Setting Qualifications with Expertise
    int jackExpertiseLevel = 10;
    int johnExpertiseLevel = 2;
    int paulaExpertiseLevel = 4;

    TypeWithExpertiseQualification jackRepairQualification = new TypeWithExpertiseQualification();
    jackRepairQualification.addType(SKILL_TYPE, jackExpertiseLevel);

    TypeWithExpertiseQualification johnRepairQualification = new TypeWithExpertiseQualification();
    johnRepairQualification.addType(SKILL_TYPE, johnExpertiseLevel);

    TypeWithExpertiseQualification paulaRepairQualification = new TypeWithExpertiseQualification();
    paulaRepairQualification.addType(SKILL_TYPE, paulaExpertiseLevel);

    /*
     *  Defining Resources
     */

    // Create a working day
    List<IWorkingHours> workingHours = new ArrayList<>();

    IWorkingHours firstWoh =
        new WorkingHours(
            ZonedDateTime.of(2020, MAY.getValue(), 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 6, 17, 0, 0, 0, ZoneId.of("Europe/Berlin")));

    workingHours.add(firstWoh);

    // Max Time and max Distance
    Duration maxWorkingTime = Duration.ofHours(10);
    Quantity<Length> maxDistanceKmW = Quantities.getQuantity(1200.0, KILO(METRE));

    // Add Resources and assign qualification
    IResource resJack =
        new CapacityResource(
            "Jack Aachen - Expertise " + jackExpertiseLevel,
            50.775346,
            6.083887,
            maxWorkingTime,
            maxDistanceKmW,
            workingHours);

    IResource resJohn =
        new CapacityResource(
            "John Oberhausen - Expertise " + johnExpertiseLevel,
            51.4667,
            6.85,
            maxWorkingTime,
            maxDistanceKmW,
            workingHours);

    IResource resPaula =
        new CapacityResource(
            "Paula Cologne - Expertise " + paulaExpertiseLevel,
            50.775346,
            6.083887,
            maxWorkingTime,
            maxDistanceKmW,
            workingHours);

    // Adding qualifications
    resJack.addQualification(jackRepairQualification);
    resJohn.addQualification(johnRepairQualification);
    resPaula.addQualification(paulaRepairQualification);

    opti.addElement(resJack);
    opti.addElement(resJohn);
    opti.addElement(resPaula);
  }

  private void addNodes(IOptimization opti) {

    List<IOpeningHours> weeklyOpeningHours = new ArrayList<>();
    weeklyOpeningHours.add(
        new OpeningHours(
            ZonedDateTime.of(2020, MAY.getValue(), 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 6, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    Duration visitDuration = Duration.ofMinutes(60);

    int minEpxertiseLevelHigh = 8;
    TypeWithExpertiseConstraint repairConstraintHighExpertise = new TypeWithExpertiseConstraint();
    repairConstraintHighExpertise.addType(SKILL_TYPE, minEpxertiseLevelHigh);
    repairConstraintHighExpertise.setIsHard(true); // Forbidden to send Resource with low level

    int minEpxertiseLevelMedium = 3;
    TypeWithExpertiseConstraint repairConstraintMediumExpertise = new TypeWithExpertiseConstraint();
    repairConstraintMediumExpertise.addType(SKILL_TYPE, minEpxertiseLevelMedium);
    repairConstraintMediumExpertise.setIsHard(false); // Try to send matching expertise level

    // Define some nodes
    INode koeln =
        new TimeWindowGeoNode("Koeln", 50.9333, 6.95, weeklyOpeningHours, visitDuration, 1);
    koeln.addConstraint(repairConstraintHighExpertise);
    opti.addElement(koeln);

    INode oberhausen =
        new TimeWindowGeoNode("Oberhausen", 51.4667, 6.85, weeklyOpeningHours, visitDuration, 1);
    oberhausen.addConstraint(repairConstraintMediumExpertise);
    opti.addElement(oberhausen);
  }

  private static void subscribeToEvents(IOptimization opti) {

    PrintStream myOut = System.out;

    // Subscribe to events
    opti.getOptimizationEvents()
        .progressSubject()
        .subscribe(p -> myOut.println(p.getProgressString()));

    opti.getOptimizationEvents()
        .errorSubject()
        .subscribe(e -> myOut.println(e.getCause() + " " + e.getCode()));

    opti.getOptimizationEvents()
        .warningSubject()
        .subscribe(w -> myOut.println(w.getDescription() + w.getCode()));

    opti.getOptimizationEvents()
        .statusSubject()
        .subscribe(s -> myOut.println(s.getDescription() + " " + s.getCode()));

    opti.getOptimizationEvents().resultFuture().thenAccept(myOut::println);
  }
}
