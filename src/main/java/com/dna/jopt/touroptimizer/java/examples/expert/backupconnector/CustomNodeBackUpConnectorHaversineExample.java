package com.dna.jopt.touroptimizer.java.examples.expert.backupconnector;
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
import com.dna.jopt.member.unit.nodeedge.INodeEdgeConnector;
import com.dna.jopt.member.unit.nodeedge.NodeEdgeConnector;
import com.dna.jopt.member.unit.nodeedge.backupconnector.DefaultFlatEarthAverageSpeedBackupElementConnector;
import com.dna.jopt.member.unit.resource.CapacityResource;
import com.dna.jopt.member.unit.resource.IResource;
import com.dna.jopt.touroptimizer.java.examples.ExampleLicenseHelper;

import tech.units.indriya.quantity.Quantities;

/**
 * Calculates the missing distances and driving times with a custom backup connector: the
 * haversine distance times 1.2.
 *
 * <p>Scenario: Jack starts in Aachen and visits eight nodes in Germany on May 6 and May 7, 2020,
 * from 08:00 to 17:00 (Europe/Berlin). The example gives no connections, so the backup connector
 * calculates all of them.
 *
 * <p>Look for: the result, with driving distances 1.2 times the haversine distance. Compare them
 * with the result of CustomNodeBackUpConnectorExample, which uses the flat-earth distance.
 *
 * <p>Prerequisites: None: without a license file the example uses the public evaluation license
 * (see ExampleLicenseHelper).
 *
 * @see <a href="https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/backupconnector/CustomNodeBackUpConnector.md">CustomNodeBackUpConnector.md</a>
 */
public class CustomNodeBackUpConnectorHaversineExample extends Optimization {

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
    new CustomNodeBackUpConnectorHaversineExample().example();
  }

  @Override
  public String toString() {
    return "Calculates the missing distances and driving times with a custom backup connector: the"
        + " haversine distance times 1.2.";
  }

  /**
   * Builds the optimization with the custom backup connector, runs it and prints the result.
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
    setBackupConnector(this);
    attachToObservables(this);
    startAndPresentResult(this);
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
   * Adds eight nodes with the same opening hours and visit duration.
   *
   * @param opti the optimization
   */
  private static void addNodes(IOptimization opti) {
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

    // No node in Nuernberg, unlike the other examples with these cities: with the 1.2 factor,
    // the nine nodes with Nuernberg did not fit into two days of 13 hours.
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
  }

  /**
   * Adds Jack, who starts and ends each working day in Aachen.
   *
   * @param opti the optimization
   */
  private static void addResources(IOptimization opti) {
    List<IWorkingHours> workingHours = new ArrayList<>();
    workingHours.add(
        new WorkingHours(
            ZonedDateTime.of(2020, MAY.getValue(), 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 6, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));
    workingHours.add(
        new WorkingHours(
            ZonedDateTime.of(2020, MAY.getValue(), 7, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 7, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    // 13 h, more than the 9 h of the working hours: a day may run past 17:00 without overtime.
    Duration maxWorkingTime = Duration.ofHours(13);
    // 1200 km per day, more than the longest day of this plan needs.
    Quantity<Length> maxDistanceKmW = Quantities.getQuantity(1200.0, KILO(METRE));

    IResource jack =
        new CapacityResource(
            "Jack", 50.775346, 6.083887, maxWorkingTime, maxDistanceKmW, workingHours);
    opti.addElement(jack);
  }

  /**
   * Gives the optimization a node connector with the custom backup connector.
   *
   * @param opti the optimization
   */
  private static void setBackupConnector(IOptimization opti) {
    // false: the connector calculates the driving time of a connection once, for the first
    // resource that needs it. Jack is the only resource, so nothing is lost.
    INodeEdgeConnector connector = new NodeEdgeConnector();
    connector.setBackupElementConnector(new MyBackupElementConnector(false));
    opti.setNodeConnector(connector);
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

  /**
   * Returns the great-circle distance between two positions on a sphere with the radius of the
   * earth (haversine formula).
   *
   * @param lon1 the longitude of the first position
   * @param lat1 the latitude of the first position
   * @param lon2 the longitude of the second position
   * @param lat2 the latitude of the second position
   * @return the distance in meters
   */
  public static double haversineDistanceMeter(double lon1, double lat1, double lon2, double lat2) {
    // The radius of the earth in meters.
    double r = 6371 * 1000.0;

    double deltaLatRad = toRad(lat2 - lat1);
    double deltaLonRad = toRad(lon2 - lon1);

    double a =
        Math.sin(deltaLatRad / 2) * Math.sin(deltaLatRad / 2)
            + Math.cos(toRad(lat1))
                * Math.cos(toRad(lat2))
                * Math.sin(deltaLonRad / 2)
                * Math.sin(deltaLonRad / 2);
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

    return r * c;
  }

  /**
   * Converts degrees to radians.
   *
   * @param value the angle in degrees
   * @return the angle in radians
   */
  private static double toRad(double value) {
    return value * Math.PI / 180;
  }

  /** A backup connector that makes every haversine distance 1.2 times longer. */
  private static class MyBackupElementConnector
      extends DefaultFlatEarthAverageSpeedBackupElementConnector {

    private static final long serialVersionUID = -7893999473615030027L;

    /**
     * Creates the backup connector.
     *
     * @param doRecalculateElement2ElementDuration true to calculate the driving time again for
     *     each resource, false to calculate it once
     */
    public MyBackupElementConnector(boolean doRecalculateElement2ElementDuration) {
      super(doRecalculateElement2ElementDuration);
    }

    /** Returns the haversine distance between two elements, times 1.2. */
    @Override
    public Quantity<Length> getElement2ElementDistance(
        String fromElementId,
        double fromElementLon,
        double fromElementLat,
        String toElementId,
        double toElementLon,
        double toElementLat,
        IResource visitor) {

      // 1.2: roads are longer than the straight line, for example through detours and terrain.
      // The core has the same formula (NodeEdgeConnector.distancePlacePlaceHaversine); the own
      // method shows where a distance model of your own goes.
      double distance =
          CustomNodeBackUpConnectorHaversineExample.haversineDistanceMeter(
              fromElementLon, fromElementLat, toElementLon, toElementLat)
              * 1.2;

      return Quantities.getQuantity(distance, METRE);
    }

    /** Returns the driving time: the distance divided by the average speed of the resource. */
    @Override
    public Duration getElement2ElementDuration(
        String fromElementId, String toElementId, double distanceMeter, IResource visitor) {
      // The formula of the base class, for the distance above; a custom time model goes here.
      long travelTimeMillis = (long) (distanceMeter / visitor.getAvgSpeed() * 1000L);

      return Duration.ofMillis(travelTimeMillis);
    }
  }
}
