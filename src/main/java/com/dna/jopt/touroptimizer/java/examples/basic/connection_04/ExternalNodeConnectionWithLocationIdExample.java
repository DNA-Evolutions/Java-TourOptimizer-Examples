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

import java.io.IOException;
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

import com.dna.jopt.framework.body.IOptimization;
import com.dna.jopt.framework.body.Optimization;
import com.dna.jopt.framework.exception.caught.InvalidLicenceException;
import com.dna.jopt.framework.outcomewrapper.IOptimizationResult;
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
 * Sets the distance and the driving time between two locations with a node connector and location
 * ids, so that every node at a location uses the connections of that location.
 *
 * <p>Scenario: the same eight nodes and two resources as in ExternalNodeConnectionExample, on March
 * 6 and 7, 2020, in the time zone Europe/Berlin. Koeln and Koeln1 have the same position and share
 * the location id "TestLocationId1".
 *
 * <p>Look for: the leg into Oberhausen with "Driving: 10.0 [min], 5.0 [km]". The order of Koeln
 * and Koeln1 can change from run to run; the leg uses the edge either way, because the connector
 * stores the edge for the location id that both nodes share.
 *
 * <p>Elements that share a location id are treated as one position. Give them the same coordinates
 * as well: the default backup connector computes the missing connections from the coordinates.
 *
 * <p>Prerequisites: None: without a license file the example uses the public evaluation license
 * (see ExampleLicenseHelper).
 *
 * @see <a href="https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic/connection_04/ExternalNodeConnection.md">ExternalNodeConnection.md</a>
 */
public class ExternalNodeConnectionWithLocationIdExample extends Optimization {

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
    new ExternalNodeConnectionWithLocationIdExample().example();
  }

  @Override
  public String toString() {
    return "Sets the distance and the driving time between two locations with a node connector and"
        + " location ids, so that every node at a location uses the connections of that location.";
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
    addNodes(this);
    addResources(this);
    attachToObservables(this);
    startAndPresentResult(this);
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

    // Koeln and Koeln1 share their position and therefore a location id. Every other node keeps
    // its id as its location id.
    INode koeln =
        new TimeWindowGeoNode(
            "Koeln", 50.9333, 6.95, openingHours, visitDuration, importance);
    koeln.setLocationId("TestLocationId1");
    opti.addElement(koeln);

    INode koeln1 =
        new TimeWindowGeoNode(
            "Koeln1", 50.9333, 6.95, openingHours, visitDuration, importance);
    koeln1.setLocationId("TestLocationId1");
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
   * Sets a node connector that holds one edge in each direction between the location of Koeln and
   * Oberhausen.
   *
   * @param opti the optimization
   * @param koeln the node Koeln
   * @param oberhausen the node Oberhausen
   */
  private static void addNodeConnections(IOptimization opti, INode koeln, INode oberhausen) {
    // Artificial on purpose: the straight line is about 60 km, so the given edge is easy to spot.
    Quantity<Length> distance = Quantities.getQuantity(5, KILO(METRE));
    Duration drivingTime = Duration.ofMinutes(10);

    // The connector stores a connection by the location ids of its two elements, so the items
    // below also connect Koeln1 and Oberhausen.
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

    IResource jack =
        new CapacityResource(
            "Jack", 50.775346, 6.083887, maxWorkingTimeJack, maxDistanceKmW, createWorkingHours());
    opti.addElement(jack);

    IResource john =
        new CapacityResource(
            "John", 50.775346, 6.083887, maxWorkingTimeJohn, maxDistanceKmW, createWorkingHours());
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
