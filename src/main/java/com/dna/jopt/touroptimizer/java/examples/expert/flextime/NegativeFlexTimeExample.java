package com.dna.jopt.touroptimizer.java.examples.expert.flextime;
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
import static java.time.Month.MAY;
import static javax.measure.MetricPrefix.KILO;
import static tech.units.indriya.unit.Units.METRE;

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

import javax.measure.Quantity;
import javax.measure.quantity.Length;

import com.dna.jopt.framework.body.IOptimization;
import com.dna.jopt.framework.body.Optimization;
import com.dna.jopt.framework.exception.caught.InvalidLicenceException;
import com.dna.jopt.framework.outcomewrapper.IOptimizationResult;
import com.dna.jopt.io.exporting.kml.EntityKMLExporter;
import com.dna.jopt.member.unit.hours.IOpeningHours;
import com.dna.jopt.member.unit.hours.IWorkingHours;
import com.dna.jopt.member.unit.hours.OpeningHours;
import com.dna.jopt.member.unit.hours.WorkingHours;
import com.dna.jopt.member.unit.node.INode;
import com.dna.jopt.member.unit.node.geo.TimeWindowGeoNode;
import com.dna.jopt.member.unit.resource.CapacityResource;
import com.dna.jopt.member.unit.resource.IResource;
import com.dna.jopt.touroptimizer.java.examples.ExampleLicenseHelper;

import tech.units.indriya.quantity.Quantities;

/**
 * Lets a resource drive to its first node up to 2 hours before its working hours start (negative
 * flex time), so that it can reach the node when the node opens.
 *
 * <p>Scenario: Jack starts in Aachen and visits eight nodes in Germany on May 6 and May 7, 2020. He
 * works from 08:00 to 16:00, and the nodes open from 08:00 to 17:00 (Europe/Berlin). No node lies at
 * Jack's start position, so each route starts with a drive.
 *
 * <p>Look for: the "Effective Route Start" before 08:00 and the negative "Reduc. time usage[min]"
 * of each route. A first node within 2 hours of driving is reached at 08:00; a first node farther
 * away is reached later. The example also writes the result to NegativeFlexTimeExample.kml.
 *
 * <p>Prerequisites: None: without a license file the example uses the public evaluation license
 * (see ExampleLicenseHelper).
 *
 * @see <a href="https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/flextime/Flextime.md">Flextime.md</a>
 */
public class NegativeFlexTimeExample extends Optimization {

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
    new NegativeFlexTimeExample().example();
  }

  @Override
  public String toString() {
    return "Lets a resource drive to its first node up to 2 hours before its working hours start"
        + " (negative flex time), so that it can reach the node when the node opens.";
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
   * Adds eight nodes that open when Jack's working hours start.
   *
   * @param opti the optimization
   */
  private static void addNodes(IOptimization opti) {
    // 08:00 to 17:00: the nodes open when Jack starts working.
    List<IOpeningHours> openingHours = new ArrayList<>();
    openingHours.add(
        new OpeningHours(
            ZonedDateTime.of(2020, MAY.getValue(), 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 6, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));
    openingHours.add(
        new OpeningHours(
            ZonedDateTime.of(2020, MAY.getValue(), 7, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 7, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    // Each visit takes 20 minutes and counts toward the working time, like the driving.
    Duration visitDuration = Duration.ofMinutes(20);

    // The same importance for every node, so no node is preferred over another.
    int importance = 1;

    INode koeln =
        new TimeWindowGeoNode("Koeln", 50.9333, 6.95, openingHours, visitDuration, importance);
    opti.addElement(koeln);

    INode oberhausen =
        new TimeWindowGeoNode(
            "Oberhausen", 51.4667, 6.85, openingHours, visitDuration, importance);
    opti.addElement(oberhausen);

    INode essen =
        new TimeWindowGeoNode("Essen", 51.45, 7.01667, openingHours, visitDuration, importance);
    opti.addElement(essen);

    INode dueren =
        new TimeWindowGeoNode("Dueren", 50.8, 6.48333, openingHours, visitDuration, importance);
    opti.addElement(dueren);

    INode nuernberg =
        new TimeWindowGeoNode(
            "Nuernberg", 49.4478, 11.0683, openingHours, visitDuration, importance);
    opti.addElement(nuernberg);

    INode heilbronn =
        new TimeWindowGeoNode(
            "Heilbronn", 49.1403, 9.22, openingHours, visitDuration, importance);
    opti.addElement(heilbronn);

    INode stuttgart =
        new TimeWindowGeoNode(
            "Stuttgart", 48.7667, 9.18333, openingHours, visitDuration, importance);
    opti.addElement(stuttgart);

    INode wuppertal =
        new TimeWindowGeoNode(
            "Wuppertal", 51.2667, 7.18333, openingHours, visitDuration, importance);
    opti.addElement(wuppertal);

    // No node in Aachen, as in PositiveFlexTimeExample: each route starts with a drive, which the
    // early start can move before the working hours.
  }

  /**
   * Adds Jack, who starts and ends each working day in Aachen and may drive up to 2 hours before
   * his working hours start.
   *
   * @param opti the optimization
   */
  private static void addResources(IOptimization opti) {
    List<IWorkingHours> workingHours = new ArrayList<>();
    workingHours.add(
        new WorkingHours(
            ZonedDateTime.of(2020, MAY.getValue(), 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 6, 16, 0, 0, 0, ZoneId.of("Europe/Berlin"))));
    workingHours.add(
        new WorkingHours(
            ZonedDateTime.of(2020, MAY.getValue(), 7, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 7, 16, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    // 13 h, more than the 8 h of the working hours: a day may run past 16:00 without overtime.
    Duration maxWorkingTime = Duration.ofHours(13);
    // 1200 km per day, more than the longest day of this plan needs.
    Quantity<Length> maxDistanceKmW = Quantities.getQuantity(1200.0, KILO(METRE));

    IResource jack =
        new CapacityResource(
            "Jack", 50.775346, 6.083887, maxWorkingTime, maxDistanceKmW, workingHours);

    // Negative flex time: Jack may start up to 2 hours before his working hours. true: he may use
    // the early time only for driving to the first node, not for working on it.
    jack.setMaxRouteStartReductionTime(Duration.ofHours(2), true);
    // true: the early start counts toward his working time and its 13 h maximum.
    jack.setReductionTimeIsIncludedInWorkingTime(true);

    opti.addElement(jack);
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
   * Writes the result to NegativeFlexTimeExample.kml in the working directory.
   *
   * @param result the result of the run
   * @throws IOException if a file cannot be read or written
   */
  private static void exportToKml(IOptimizationResult result) throws IOException {
    String kmlFile = "NegativeFlexTimeExample.kml";

    EntityKMLExporter kmlExporter = new EntityKMLExporter();
    kmlExporter.setTitle("NegativeFlexTimeExample");

    // export flushes the stream but does not close it; the try block closes it.
    try (OutputStream out = new FileOutputStream(kmlFile)) {
      if (!kmlExporter.export(result, out)) {
        System.out.println("KML export failed: " + kmlFile);
      }
    }
  }
}
