package com.dna.jopt.touroptimizer.java.examples.expert.backupconnector;
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
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static java.time.Month.MAY;

import javax.measure.Quantity;
import javax.measure.quantity.Length;

import com.dna.jopt.framework.body.Optimization;
import com.dna.jopt.framework.exception.caught.InvalidLicenceException;
import com.dna.jopt.framework.outcomewrapper.IOptimizationProgress;
import com.dna.jopt.framework.outcomewrapper.IOptimizationResult;
import com.dna.jopt.member.unit.hours.IWorkingHours;
import com.dna.jopt.member.unit.hours.IOpeningHours;
import com.dna.jopt.member.unit.hours.WorkingHours;
import com.dna.jopt.member.unit.hours.OpeningHours;
import com.dna.jopt.member.unit.node.geo.TimeWindowGeoNode;
import com.dna.jopt.member.unit.nodeedge.INodeEdgeConnector;
import com.dna.jopt.member.unit.nodeedge.NodeEdgeConnector;
import com.dna.jopt.member.unit.nodeedge.backupconnector.DefaultFlatEarthAverageSpeedBackupElementConnector;
import com.dna.jopt.member.unit.resource.CapacityResource;
import com.dna.jopt.member.unit.resource.IResource;
import com.dna.jopt.touroptimizer.java.examples.ExampleLicenseHelper;

import tech.units.indriya.quantity.Quantities;

/**
 * Using the Custom backup connector.
 *
 * @author jrich
 * @version Feb 16, 2021
 * @since Feb 16, 2021
 */
public class CustomNodeBackUpConnectorHaversineExample extends Optimization {

  /**
   * The main method.
   *
   * @param args the arguments
   * @throws InvalidLicenceException the invalid licence exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws InterruptedException the interrupted exception
   * @throws ExecutionException the execution exception
   */
  public static void main(String[] args)
      throws InvalidLicenceException, IOException, InterruptedException, ExecutionException {
    new CustomNodeBackUpConnectorHaversineExample().example();
  }

  /**
   * To string.
   *
   * @return the string
   */
  public String toString() {
    return "Example for using a custom backup connector. In case no element distances/driving time"
        + "are provided by the user, the optimizer uses an approximation to generate these values."
        + "By using a custom backup connector a custom calculation for generating these values can be defined.";
  }

  /**
   * Example.
   *
   * @throws InvalidLicenceException the invalid licence exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws InterruptedException the interrupted exception
   * @throws ExecutionException the execution exception
   */
  public void example()
      throws InvalidLicenceException, IOException, InterruptedException, ExecutionException {

    // Set license via helper
    ExampleLicenseHelper.setLicense(this);

    // Properties!
    this.setProperties();

    this.addNodes();
    this.addResources();

    // Set custom backup connector
    INodeEdgeConnector connector = new NodeEdgeConnector();
    connector.setBackupElementConnector(new MyBackupElementConnector(false));
    this.setNodeConnector(connector);

    CompletableFuture<IOptimizationResult> resultFuture = this.startRunAsync();

    // It is important to block the call, otherwise optimization will be terminated

    resultFuture.get();
  }

  /** Sets the properties. */
  private void setProperties() {

    Properties props = new Properties();

    props.setProperty("JOptExitCondition.JOptGenerationCount", "20000");
    props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumIterations", "1000000");
    props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumRepetions", "1");
    props.setProperty("JOpt.NumCPUCores", "4");

    this.addElement(props);
  }

  /** Adds the resources. */
  private void addResources() {

    List<IWorkingHours> workingHours = new ArrayList<>();
    workingHours.add(
        new WorkingHours(
            ZonedDateTime.of(2020, MAY.getValue(), 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 6, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    workingHours.add(
        new WorkingHours(
            ZonedDateTime.of(2020, MAY.getValue(), 7, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 7, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    Duration maxWorkingTime = Duration.ofHours(13);
    Quantity<Length> maxDistanceKmW = Quantities.getQuantity(1200.0, KILO(METRE));

    CapacityResource rep1 =
        new CapacityResource(
            "Jack", 50.775346, 6.083887, maxWorkingTime, maxDistanceKmW, workingHours);
    rep1.setCost(0, 1, 1);
    this.addElement(rep1);
  }

  /** Adds the nodes. */
  private void addNodes() {

    List<IOpeningHours> weeklyOpeningHours = new ArrayList<>();
    weeklyOpeningHours.add(
        new OpeningHours(
            ZonedDateTime.of(2020, MAY.getValue(), 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 6, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    weeklyOpeningHours.add(
        new OpeningHours(
            ZonedDateTime.of(2020, MAY.getValue(), 7, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 7, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    Duration visitDuration = Duration.ofMinutes(20);

    // Define some nodes
    TimeWindowGeoNode koeln =
        new TimeWindowGeoNode("Koeln", 50.9333, 6.95, weeklyOpeningHours, visitDuration, 1);
    this.addElement(koeln);

    TimeWindowGeoNode oberhausen =
        new TimeWindowGeoNode("Oberhausen", 51.4667, 6.85, weeklyOpeningHours, visitDuration, 1);
    this.addElement(oberhausen);

    TimeWindowGeoNode essen =
        new TimeWindowGeoNode("Essen", 51.45, 7.01667, weeklyOpeningHours, visitDuration, 1);
    this.addElement(essen);

    TimeWindowGeoNode dueren =
        new TimeWindowGeoNode("Dueren", 50.8, 6.48333, weeklyOpeningHours, visitDuration, 1);
    this.addElement(dueren);

    TimeWindowGeoNode nuernberg =
        new TimeWindowGeoNode("Nuernberg", 49.4478, 11.0683, weeklyOpeningHours, visitDuration, 1);
    this.addElement(nuernberg);

    TimeWindowGeoNode heilbronn =
        new TimeWindowGeoNode("Heilbronn", 49.1403, 9.22, weeklyOpeningHours, visitDuration, 1);
    this.addElement(heilbronn);

    TimeWindowGeoNode stuttgart =
        new TimeWindowGeoNode("Stuttgart", 48.7667, 9.18333, weeklyOpeningHours, visitDuration, 1);
    this.addElement(stuttgart);

    TimeWindowGeoNode wuppertal =
        new TimeWindowGeoNode("Wuppertal", 51.2667, 7.18333, weeklyOpeningHours, visitDuration, 1);
    this.addElement(wuppertal);

    TimeWindowGeoNode aachen =
        new TimeWindowGeoNode("Aachen", 50.775346, 6.083887, weeklyOpeningHours, visitDuration, 1);
    this.addElement(aachen);
  }

  /**
   * On error.
   *
   * @param code the code
   * @param message the message
   */
  @Override
  public void onError(int code, String message) {
    System.out.println("code: " + code + " message:" + message);
  }

  /**
   * On status.
   *
   * @param code the code
   * @param message the message
   */
  @Override
  public void onStatus(int code, String message) {
    System.out.println("code: " + code + " message:" + message);
  }

  /**
   * On warning.
   *
   * @param code the code
   * @param message the message
   */
  @Override
  public void onWarning(int code, String message) {
    //

  }

  /**
   * On progress.
   *
   * @param rapoptProgress the rapopt progress
   */
  @Override
  public void onProgress(IOptimizationProgress rapoptProgress) {
    System.out.println(rapoptProgress.getProgressString());
  }

  /**
   * On asynchronous optimization result.
   *
   * @param rapoptResult the rapopt result
   */
  @Override
  public void onAsynchronousOptimizationResult(IOptimizationResult rapoptResult) {
    System.out.println(rapoptResult);
  }

  public static double haversineDistanceMeter(double lon1, double lat1, double lon2, double lat2) {

    double r = 6371 * 1000.0; // Radius of the earth in meter

    double deltaLatRad = toRad(lat2 - lat1);
    double deltaLonRad = toRad(lon2 - lon1);

    double a =
        Math.sin(deltaLatRad / 2) * Math.sin(deltaLatRad / 2)
            + Math.cos(toRad(lat1))
                * Math.cos(toRad(lat2))
                * Math.sin(deltaLonRad / 2)
                * Math.sin(deltaLonRad / 2);
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

    return r * c; // Distance in meter

  }

  private static Double toRad(Double value) {
    return value * Math.PI / 180;
  }

  /**
   * The Class MyResourceDependentBackupElementConnector.
   *
   * @author jrich
   * @version Feb 16, 2021
   * @since Feb 16, 2021
   */
  private class MyBackupElementConnector
      extends DefaultFlatEarthAverageSpeedBackupElementConnector {

    /**
     * Instantiates a new my resource dependent backup element connector.
     *
     * @param doRecalculateElement2ElementDuration the do recalculate element 2 element duration
     */
    public MyBackupElementConnector(boolean doRecalculateElement2ElementDuration) {
      super(doRecalculateElement2ElementDuration);
    }

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -7893999473615030027L;

    /**
     * Gets the element 2 element distance.
     *
     * @param fromElementId the from element id
     * @param fromElementLon the from element lon
     * @param fromElementLat the from element lat
     * @param toElementId the to element id
     * @param toElementLon the to element lon
     * @param toElementLat the to element lat
     * @param visitor the visitor
     * @return the element 2 element distance
     */
    @Override
    public Quantity<Length> getElement2ElementDistance(
        String fromElementId,
        double fromElementLon,
        double fromElementLat,
        String toElementId,
        double toElementLon,
        double toElementLat,
        IResource visitor) {

      double distance =
          CustomNodeBackUpConnectorHaversineExample.haversineDistanceMeter(
                  fromElementLon, fromElementLat, toElementLon, toElementLat)
              * 1.2;

      return Quantities.getQuantity(distance, METRE);
    }

    /**
     * Gets the element 2 element duration.
     *
     * @param fromElementId the from element id
     * @param toElementId the to element id
     * @param distanceMeter the distance meter
     * @param visitor the visitor
     * @return the element 2 element duration
     */
    @Override
    public Duration getElement2ElementDuration(
        String fromElementId, String toElementId, double distanceMeter, IResource visitor) {

      long traveltimeMillis = (long) (distanceMeter / visitor.getAvgSpeed() * 1000L);

      return Duration.ofMillis(traveltimeMillis);
    }
  }
}
