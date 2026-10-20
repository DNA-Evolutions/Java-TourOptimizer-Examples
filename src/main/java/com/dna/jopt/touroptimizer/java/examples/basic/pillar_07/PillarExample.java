package com.dna.jopt.touroptimizer.java.examples.basic.pillar_07;
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
import static java.time.Month.JUNE;
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
import com.dna.jopt.member.unit.node.IPillarNode;
import com.dna.jopt.member.unit.node.event.PillarEventNode;
import com.dna.jopt.member.unit.node.geo.PillarTimeWindowGeoNode;
import com.dna.jopt.member.unit.node.geo.TimeWindowGeoNode;
import com.dna.jopt.member.unit.resource.CapacityResource;
import com.dna.jopt.member.unit.resource.IResource;
import com.dna.jopt.touroptimizer.java.examples.ExampleLicenseHelper;

import tech.units.indriya.quantity.Quantities;

/**
 * Adds pillar nodes: appointments that a route visits inside their time window or not at all, two
 * of them bound to one resource each.
 *
 * <p>Scenario: two resources, John and Jack, who start in Aachen and work June 4 to 6, 2019, from
 * 08:00 to 17:00 (Europe/Berlin); five TimeWindowGeoNodes with a visit of 60 minutes; and three
 * pillars: a plumbing job in Cologne for John (June 5, 10:00 to 10:50), a call for Jack (30
 * minutes between 10:30 and 12:00 on June 6) and a maintenance job in Stuttgart for either of them
 * (90 minutes between 13:30 and 16:00 on June 4).
 *
 * <p>Look for: the lines marked "Pillar" in the result. "Cologne Plumbing" is on a route of John,
 * "Important call" on a route of Jack, and each pillar lies inside its time window. The example
 * also writes the result to PillarExample.kml.
 *
 * <p>Prerequisites: None: without a license file the example uses the public evaluation license
 * (see ExampleLicenseHelper).
 *
 * @see <a href="https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic/pillar_07/Pillar.md">Pillar.md</a>
 */
public class PillarExample extends Optimization {

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
    new PillarExample().example();
  }

  @Override
  public String toString() {
    return "Adds pillar nodes: appointments that a route visits inside their time window or not at"
        + " all, two of them bound to one resource each.";
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
    // addPillars attaches pillars to John and Jack, so the resources are created first.
    IResource john = addResource(this, "John");
    IResource jack = addResource(this, "Jack");
    addNodes(this);
    addPillars(this, john, jack);
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
   * Adds a resource that starts in Aachen and works June 4 to 6, 2019, from 08:00 to 17:00.
   *
   * @param opti the optimization
   * @param name the id of the resource
   * @return the resource, for the pillars that attach to it
   */
  private static IResource addResource(IOptimization opti, String name) {
    List<IWorkingHours> workingHours = new ArrayList<>();
    workingHours.add(
        new WorkingHours(
            ZonedDateTime.of(2019, JUNE.getValue(), 4, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2019, JUNE.getValue(), 4, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    workingHours.add(
        new WorkingHours(
            ZonedDateTime.of(2019, JUNE.getValue(), 5, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2019, JUNE.getValue(), 5, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    workingHours.add(
        new WorkingHours(
            ZonedDateTime.of(2019, JUNE.getValue(), 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2019, JUNE.getValue(), 6, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    // 13 h, more than the 9 h of the working hours: a day may run past 17:00 without overtime.
    Duration maxWorkingTime = Duration.ofHours(13);
    // 1200 km per day, more than the longest day of this plan needs.
    Quantity<Length> maxDistanceKmW = Quantities.getQuantity(1200.0, KILO(METRE));

    IResource resource =
        new CapacityResource(
            name, 50.775346, 6.083887, maxWorkingTime, maxDistanceKmW, workingHours);
    opti.addElement(resource);
    return resource;
  }

  /**
   * Adds five geo nodes that are not pillars.
   *
   * @param opti the optimization
   */
  private static void addNodes(IOptimization opti) {
    List<IOpeningHours> openingHours = new ArrayList<>();
    openingHours.add(
        new OpeningHours(
            ZonedDateTime.of(2019, JUNE.getValue(), 4, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2019, JUNE.getValue(), 4, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    openingHours.add(
        new OpeningHours(
            ZonedDateTime.of(2019, JUNE.getValue(), 5, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2019, JUNE.getValue(), 5, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    openingHours.add(
        new OpeningHours(
            ZonedDateTime.of(2019, JUNE.getValue(), 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2019, JUNE.getValue(), 6, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    Duration visitDuration = Duration.ofMinutes(60);

    // The same importance for every node, so no node is preferred over another.
    int importance = 1;

    INode koeln =
        new TimeWindowGeoNode(
            "Koeln", 50.9333, 6.95, openingHours, visitDuration, importance);
    opti.addElement(koeln);

    INode oberhausen =
        new TimeWindowGeoNode(
            "Oberhausen", 51.4667, 6.85, openingHours, visitDuration, importance);
    opti.addElement(oberhausen);

    INode nuernberg =
        new TimeWindowGeoNode(
            "Nuernberg",
            49.4478,
            11.0683,
            openingHours,
            visitDuration,
            importance);
    opti.addElement(nuernberg);

    INode heilbronn =
        new TimeWindowGeoNode(
            "Heilbronn", 49.1403, 9.22, openingHours, visitDuration, importance);
    opti.addElement(heilbronn);

    INode aachen =
        new TimeWindowGeoNode(
            "Aachen",
            50.775346,
            6.083887,
            openingHours,
            visitDuration,
            importance);
    opti.addElement(aachen);
  }

  /**
   * Adds three pillars: one bound to John, one bound to Jack and one that either may take.
   *
   * @param opti the optimization
   * @param john the resource for the plumbing job
   * @param jack the resource for the call
   */
  private static void addPillars(IOptimization opti, IResource john, IResource jack) {
    /*
     * Plumbing pillar for John in Cologne from 10:00 to 10:50. The visit fills the whole time
     * window, so the constructor takes no visit duration.
     */
    IOpeningHours plumbingOpeningHours =
        new OpeningHours(
            ZonedDateTime.of(2019, JUNE.getValue(), 5, 10, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2019, JUNE.getValue(), 5, 10, 50, 0, 0, ZoneId.of("Europe/Berlin")));

    IPillarNode colognePlumbing =
        new PillarTimeWindowGeoNode(
            "Cologne Plumbing", 50.9333, 6.95, plumbingOpeningHours);
    // Only John may visit this pillar.
    colognePlumbing.attachResource(john);
    opti.addElement(colognePlumbing);

    /*
     * Jack has to call a customer between 10:30 and 12:00 for half an hour. His location does not
     * matter, so the pillar is a PillarEventNode.
     */
    IOpeningHours callOpeningHours =
        new OpeningHours(
            ZonedDateTime.of(2019, JUNE.getValue(), 6, 10, 30, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2019, JUNE.getValue(), 6, 12, 0, 0, 0, ZoneId.of("Europe/Berlin")));

    Duration visitDurationPillarCallJack = Duration.ofMinutes(30);

    IPillarNode callJack =
        new PillarEventNode(
            "Important call", callOpeningHours, visitDurationPillarCallJack);
    // Only Jack may visit this pillar.
    callJack.attachResource(jack);
    opti.addElement(callJack);

    /*
     * Maintenance job in Stuttgart between 13:30 and 16:00 for 90 minutes. No resource is
     * attached, so John or Jack may take the job.
     */
    IOpeningHours maintenanceOpeningHours =
        new OpeningHours(
            ZonedDateTime.of(2019, JUNE.getValue(), 4, 13, 30, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2019, JUNE.getValue(), 4, 16, 0, 0, 0, ZoneId.of("Europe/Berlin")));

    Duration visitDurationPillarMaintenance = Duration.ofMinutes(90);

    IPillarNode maintenanceJob =
        new PillarTimeWindowGeoNode(
            "Maintenance job",
            48.7667,
            9.18333,
            maintenanceOpeningHours,
            visitDurationPillarMaintenance);
    opti.addElement(maintenanceJob);
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
   * Writes the result to PillarExample.kml in the working directory.
   *
   * @param result the result of the run
   * @throws IOException if a file cannot be read or written
   */
  private static void exportToKml(IOptimizationResult result) throws IOException {
    String kmlFile = "PillarExample.kml";

    EntityKMLExporter kmlExporter = new EntityKMLExporter();
    kmlExporter.setTitle("PillarExample");

    // export flushes the stream but does not close it; the try block closes it.
    try (OutputStream out = new FileOutputStream(kmlFile)) {
      if (!kmlExporter.export(result, out)) {
        System.out.println("KML export failed: " + kmlFile);
      }
    }
  }
}
