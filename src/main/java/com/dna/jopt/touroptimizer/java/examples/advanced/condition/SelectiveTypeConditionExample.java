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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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
import com.dna.jopt.io.exporting.kml.EntityKMLExporter;
import com.dna.jopt.member.unit.condition.type.TypeConstraint;
import com.dna.jopt.member.unit.condition.type.TypeQualification;
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
 * Gives a resource its types per working day, so that a node goes to a day that offers the types
 * it needs.
 *
 * <p>Scenario: Jack starts in Aachen and works from 08:00 to 17:00 on May 6 to May 10, 2020
 * (Europe/Berlin). Each working day lists the equipment and the certification that Jack has on
 * that day. Nine nodes in Germany each need some of these types, as hard constraints.
 *
 * <p>Look for: each node on the route of a day that lists all of its types. Koeln and Essen are on
 * May 6, Heilbronn on May 7, Stuttgart on May 8, Nuernberg on May 9 and Dueren on May 10: for each
 * of them, that is the only day with all its types. The example also writes the result to
 * SelectiveTypeConditionExample.kml.
 *
 * <p>Prerequisites: None: without a license file the example uses the public evaluation license
 * (see ExampleLicenseHelper).
 *
 * @see <a href="https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/condition/ResourceType.md">ResourceType.md</a>
 */
public class SelectiveTypeConditionExample extends Optimization {

  /** A truck with cooling. */
  private static final String COOLING = "Cooling available";

  /** A chain saw on the truck. */
  private static final String CHAIN_SAW = "Chain Saw available";

  /** A loading ramp on the truck. */
  private static final String LOADING_RAMP = "Loading ramp available";

  /** A second person on the truck. */
  private static final String SECOND_PERSON = "Second person on truck available";

  /** A pallet lift truck on the truck. */
  private static final String PALLET_LIFT_TRUCK = "Pallet lift truck available";

  /** The certification of the driver for dangerous goods. */
  private static final String DANGEROUS_GOODS = "Certification for dangerous goods";

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
    new SelectiveTypeConditionExample().example();
  }

  @Override
  public String toString() {
    return "Gives a resource its types per working day, so that a node goes to a day that offers"
        + " the types it needs.";
  }

  /**
   * Builds the optimization, runs it, prints the result and writes it to a KML file.
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
    IOptimizationResult result = startAndPresentResult(this);
    exportToKml(result);
  }

  /**
   * Adds the properties that keep the run short.
   *
   * @param opti the optimization
   */
  private static void addProperties(IOptimization opti) {
    Properties props = new Properties();
    // Fewer generations and iterations than the defaults keep the run short.
    props.setProperty("JOptExitCondition.JOptGenerationCount", "1000");
    props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumIterations", "100000");
    opti.addElement(props);
  }

  /**
   * Adds nine nodes, each with a hard constraint on the types it needs.
   *
   * @param opti the optimization
   */
  private static void addNodes(IOptimization opti) {
    List<IOpeningHours> openingHours = new ArrayList<>();
    for (int day = 6; day <= 10; day++) {
      openingHours.add(
          new OpeningHours(
              ZonedDateTime.of(2020, MAY.getValue(), day, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
              ZonedDateTime.of(
                  2020, MAY.getValue(), day, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));
    }

    // Each visit takes 60 minutes and counts toward the working time, like the driving.
    Duration visitDuration = Duration.ofMinutes(60);

    // The same importance for every node, so no node is preferred over another.
    int importance = 1;

    INode koeln =
        new TimeWindowGeoNode("Koeln", 50.9333, 6.95, openingHours, visitDuration, importance);
    koeln.addConstraint(createTypeConstraint(DANGEROUS_GOODS, PALLET_LIFT_TRUCK));
    opti.addElement(koeln);

    INode oberhausen =
        new TimeWindowGeoNode(
            "Oberhausen", 51.4667, 6.85, openingHours, visitDuration, importance);
    oberhausen.addConstraint(createTypeConstraint(SECOND_PERSON, COOLING));
    opti.addElement(oberhausen);

    INode essen =
        new TimeWindowGeoNode("Essen", 51.45, 7.01667, openingHours, visitDuration, importance);
    essen.addConstraint(createTypeConstraint(DANGEROUS_GOODS, COOLING));
    opti.addElement(essen);

    INode dueren =
        new TimeWindowGeoNode("Dueren", 50.8, 6.48333, openingHours, visitDuration, importance);
    dueren.addConstraint(createTypeConstraint(SECOND_PERSON, CHAIN_SAW, LOADING_RAMP));
    opti.addElement(dueren);

    INode nuernberg =
        new TimeWindowGeoNode(
            "Nuernberg", 49.4478, 11.0683, openingHours, visitDuration, importance);
    nuernberg.addConstraint(createTypeConstraint(PALLET_LIFT_TRUCK, LOADING_RAMP));
    opti.addElement(nuernberg);

    INode heilbronn =
        new TimeWindowGeoNode(
            "Heilbronn", 49.1403, 9.22, openingHours, visitDuration, importance);
    heilbronn.addConstraint(createTypeConstraint(DANGEROUS_GOODS, CHAIN_SAW));
    opti.addElement(heilbronn);

    INode stuttgart =
        new TimeWindowGeoNode(
            "Stuttgart", 48.7667, 9.18333, openingHours, visitDuration, importance);
    stuttgart.addConstraint(createTypeConstraint(PALLET_LIFT_TRUCK, CHAIN_SAW));
    opti.addElement(stuttgart);

    INode wuppertal =
        new TimeWindowGeoNode(
            "Wuppertal", 51.2667, 7.18333, openingHours, visitDuration, importance);
    wuppertal.addConstraint(createTypeConstraint(COOLING));
    opti.addElement(wuppertal);

    INode aachen =
        new TimeWindowGeoNode(
            "Aachen", 50.775346, 6.083887, openingHours, visitDuration, importance);
    aachen.addConstraint(createTypeConstraint(LOADING_RAMP));
    opti.addElement(aachen);
  }

  /**
   * Creates a hard type constraint: a route may visit the node only if it has all the types.
   *
   * @param types the types that the node needs
   * @return the type constraint
   */
  private static TypeConstraint createTypeConstraint(String... types) {
    TypeConstraint constraint = new TypeConstraint();
    for (String type : types) {
      constraint.addType(type);
    }
    constraint.setIsHard(true);
    return constraint;
  }

  /**
   * Adds Jack, whose types are set per working day.
   *
   * @param opti the optimization
   */
  private static void addResources(IOptimization opti) {
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

    // 13 h per day, a sample value above the 9 h of the working hours: the days to Heilbronn and
    // Nuernberg take longer.
    Duration maxWorkingTime = Duration.ofHours(13);
    // 1200 km per day, more than the longest day of this plan needs.
    Quantity<Length> maxDistanceKmW = Quantities.getQuantity(1200.0, KILO(METRE));

    IResource jack =
        new CapacityResource(
            "Jack", 50.775346, 6.083887, maxWorkingTime, maxDistanceKmW, workingHours);
    opti.addElement(jack);
  }

  /**
   * Creates the working hours of one day in May 2020, from 08:00 to 17:00, with the types that
   * Jack has on that day.
   *
   * @param day the day of the month
   * @param types the types of that day
   * @return the working hours
   */
  private static IWorkingHours createWorkingDay(int day, String... types) {
    IWorkingHours hours =
        new WorkingHours(
            ZonedDateTime.of(2020, MAY.getValue(), day, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), day, 17, 0, 0, 0, ZoneId.of("Europe/Berlin")));
    TypeQualification qualification = new TypeQualification();
    for (String type : types) {
      qualification.addType(type);
    }
    hours.addQualification(qualification);
    return hours;
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
   * Starts the run, waits for the result, prints it and returns it.
   *
   * @param opti the optimization
   * @return the result of the run
   * @throws InterruptedException if the wait for the result is interrupted
   * @throws ExecutionException if the optimization run fails
   * @throws TimeoutException if the run does not finish within the wait
   * @throws InvalidLicenceException if the license is not valid
   */
  private static IOptimizationResult startAndPresentResult(IOptimization opti)
      throws InterruptedException, ExecutionException, TimeoutException, InvalidLicenceException {
    CompletableFuture<IOptimizationResult> resultFuture = opti.startRunAsync();
    // Wait for the result; without the wait the JVM would end before the run does.
    IOptimizationResult result = resultFuture.get(5, TimeUnit.MINUTES);
    System.out.println(result);
    return result;
  }

  /**
   * Writes the result to SelectiveTypeConditionExample.kml in the working directory.
   *
   * @param result the result of the run
   * @throws IOException if a file cannot be read or written
   */
  private static void exportToKml(IOptimizationResult result) throws IOException {
    String kmlFile = "SelectiveTypeConditionExample.kml";

    EntityKMLExporter kmlExporter = new EntityKMLExporter();
    kmlExporter.setTitle("SelectiveTypeConditionExample");

    // export flushes the stream but does not close it; the try block closes it.
    try (OutputStream out = new FileOutputStream(kmlFile)) {
      if (!kmlExporter.export(result, out)) {
        System.out.println("KML export failed: " + kmlFile);
      }
    }
  }
}
