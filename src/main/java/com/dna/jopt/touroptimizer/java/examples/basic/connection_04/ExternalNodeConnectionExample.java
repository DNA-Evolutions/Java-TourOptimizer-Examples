package com.dna.jopt.touroptimizer.java.examples.basic.connection_04;
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
import static java.time.Month.MARCH;
import static javax.measure.MetricPrefix.KILO;
import static tech.units.indriya.unit.Units.METRE;
import static tech.units.indriya.unit.Units.METRE_PER_SECOND;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.measure.Quantity;
import javax.measure.quantity.Length;
import javax.measure.quantity.Speed;

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
import com.dna.jopt.member.unit.nodeedge.INodeConnectorItem;
import com.dna.jopt.member.unit.nodeedge.INodeEdgeConnector;
import com.dna.jopt.member.unit.nodeedge.NodeEdgeConnector;
import com.dna.jopt.member.unit.nodeedge.NodeEdgeConnectorItem;
import com.dna.jopt.member.unit.resource.CapacityResource;
import com.dna.jopt.member.unit.resource.IResource;
import com.dna.jopt.touroptimizer.java.examples.ExampleLicenseHelper;

import tech.units.indriya.quantity.Quantities;

/**
 * Sets the distance and the driving time between two nodes with a node connector.
 *
 * <p>Scenario: eight nodes in Germany (Koeln and Koeln1 at the same position) and the two resources
 * Jack and John, who start in Aachen, on March 6 and 7, 2020, in the time zone Europe/Berlin.
 *
 * <p>Look for: the leg into Oberhausen with "Driving: 10.0 [min], 5.0 [km]", the values of the
 * edge from Koeln. The other legs carry the fallback values of the default backup connector.
 *
 * <p>Prerequisites: None: without a license file the example uses the public evaluation license
 * (see ExampleLicenseHelper). The example writes ExternalNodeConnectionExample.kml to the working
 * directory.
 *
 * @see <a href="https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic/connection_04/ExternalNodeConnection.md">ExternalNodeConnection.md</a>
 */
public class ExternalNodeConnectionExample extends Optimization {

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
    new ExternalNodeConnectionExample().example();
  }

  @Override
  public String toString() {
    return "Sets the distance and the driving time between two nodes with a node connector.";
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
    addNodes(this);
    addResources(this);
    attachToObservables(this);
    IOptimizationResult result = startAndPresentResult(this);
    exportToKml(result);
  }

  /**
   * Adds the eight nodes and then the node connections between Koeln and Oberhausen.
   *
   * @param opti the optimization
   */
  private static void addNodes(IOptimization opti) {
    List<IOpeningHours> openingHours = new ArrayList<>();
    openingHours.add(
        new OpeningHours(
            ZonedDateTime.of(2020, MARCH.getValue(), 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MARCH.getValue(), 6, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    openingHours.add(
        new OpeningHours(
            ZonedDateTime.of(2020, MARCH.getValue(), 7, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MARCH.getValue(), 7, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    // Each visit takes 20 minutes and counts toward the working time, like the driving.
    Duration visitDuration = Duration.ofMinutes(20);

    // The same importance for every node, so no node is preferred over another.
    int importance = 1;

    INode koeln =
        new TimeWindowGeoNode(
            "Koeln", 50.9333, 6.95, openingHours, visitDuration, importance);
    opti.addElement(koeln);

    INode koeln1 =
        new TimeWindowGeoNode(
            "Koeln1", 50.9333, 6.95, openingHours, visitDuration, importance);
    opti.addElement(koeln1);

    INode oberhausen =
        new TimeWindowGeoNode(
            "Oberhausen", 51.4667, 6.85, openingHours, visitDuration, importance);
    opti.addElement(oberhausen);

    INode essen =
        new TimeWindowGeoNode(
            "Essen", 51.45, 7.01667, openingHours, visitDuration, importance);
    opti.addElement(essen);

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

    INode aachen =
        new TimeWindowGeoNode(
            "Aachen", 50.775346, 6.083887, openingHours, visitDuration, importance);
    opti.addElement(aachen);

    addNodeConnections(opti, koeln, oberhausen);
  }

  /**
   * Sets a node connector that holds one edge in each direction between Koeln and Oberhausen.
   *
   * @param opti the optimization
   * @param koeln the node Koeln
   * @param oberhausen the node Oberhausen
   */
  private static void addNodeConnections(IOptimization opti, INode koeln, INode oberhausen) {
    // Artificial on purpose: the straight line is about 60 km, so the given edge is easy to spot.
    Quantity<Length> distance = Quantities.getQuantity(5, KILO(METRE));
    Duration drivingTime = Duration.ofMinutes(10);

    INodeConnectorItem koelnToOberhausen = new NodeEdgeConnectorItem();
    koelnToOberhausen.setFromOptimizationElement(koeln);
    koelnToOberhausen.setToOptimizationElement(oberhausen);
    koelnToOberhausen.setDistance(distance);
    koelnToOberhausen.setDrivingTime(drivingTime);

    // A connection has a direction, so the way back needs an item of its own.
    INodeConnectorItem oberhausenToKoeln = new NodeEdgeConnectorItem();
    oberhausenToKoeln.setFromOptimizationElement(oberhausen);
    oberhausenToKoeln.setToOptimizationElement(koeln);
    oberhausenToKoeln.setDistance(distance);
    oberhausenToKoeln.setDrivingTime(drivingTime);

    // Every other pair gets its values from the default backup connector: the straight-line
    // distance of a flat-earth approximation, and the driving time at the average speed of the
    // resource (setAvgSpeed).
    INodeEdgeConnector connector = new NodeEdgeConnector();
    connector.putNodeConnections(List.of(koelnToOberhausen, oberhausenToKoeln));
    opti.setNodeConnector(connector);
  }

  /**
   * Adds the two resources Jack and John.
   *
   * @param opti the optimization
   */
  private static void addResources(IOptimization opti) {
    // Jack and John differ only in their maximal working time, so the long route goes to John.
    Duration maxWorkingTimeJack = Duration.ofHours(8);
    Duration maxWorkingTimeJohn = Duration.ofHours(14);
    // 1200 km per day, more than the longest day of this plan needs.
    Quantity<Length> maxDistanceKmW = Quantities.getQuantity(1200.0, KILO(METRE));
    // 22 m/s (79.2 km/h), a sample value. The backup connector divides the straight-line
    // distance of every pair without a connection by this speed.
    Quantity<Speed> avgSpeed = Quantities.getQuantity(22.0, METRE_PER_SECOND);

    IResource jack =
        new CapacityResource(
            "Jack", 50.775346, 6.083887, maxWorkingTimeJack, maxDistanceKmW, createWorkingHours());
    jack.setAvgSpeed(avgSpeed);
    opti.addElement(jack);

    IResource john =
        new CapacityResource(
            "John", 50.775346, 6.083887, maxWorkingTimeJohn, maxDistanceKmW, createWorkingHours());
    john.setAvgSpeed(avgSpeed);
    opti.addElement(john);
  }

  /**
   * Creates the working hours of one resource: March 6, 2020 from 08:00 to 17:00 and March 7 from
   * 08:00 to 20:00.
   *
   * @return the working hours
   */
  private static List<IWorkingHours> createWorkingHours() {
    List<IWorkingHours> workingHours = new ArrayList<>();
    workingHours.add(
        new WorkingHours(
            ZonedDateTime.of(2020, MARCH.getValue(), 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MARCH.getValue(), 6, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    workingHours.add(
        new WorkingHours(
            ZonedDateTime.of(2020, MARCH.getValue(), 7, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MARCH.getValue(), 7, 20, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

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
   * Writes the result to ExternalNodeConnectionExample.kml in the working directory.
   *
   * @param result the result of the run
   * @throws IOException if a file cannot be read or written
   */
  private static void exportToKml(IOptimizationResult result) throws IOException {
    String name = ExternalNodeConnectionExample.class.getSimpleName();
    File file = new File(name + ".kml");

    EntityKMLExporter kmlExporter = new EntityKMLExporter();
    kmlExporter.setTitle(name);

    try (OutputStream out = new FileOutputStream(file)) {
      if (!kmlExporter.export(result, out)) {
        System.out.println("KML export failed: " + file);
      }
    }
  }
}
