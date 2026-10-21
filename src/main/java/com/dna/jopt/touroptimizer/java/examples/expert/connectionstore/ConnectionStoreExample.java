package com.dna.jopt.touroptimizer.java.examples.expert.connectionstore;
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
import static java.time.Month.FEBRUARY;
import static javax.measure.MetricPrefix.KILO;
import static tech.units.indriya.unit.Units.METRE;

import tech.units.indriya.quantity.Quantities;

import javax.measure.Quantity;
import javax.measure.quantity.Length;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.dna.jopt.config.convert.ConvertException;
import com.dna.jopt.framework.body.IOptimization;
import com.dna.jopt.framework.body.Optimization;
import com.dna.jopt.framework.exception.caught.InvalidLicenceException;
import com.dna.jopt.framework.outcomewrapper.IOptimizationResult;
import com.dna.jopt.member.unit.IOptimizationElement;
import com.dna.jopt.member.unit.hours.IOpeningHours;
import com.dna.jopt.member.unit.hours.IWorkingHours;
import com.dna.jopt.member.unit.hours.OpeningHours;
import com.dna.jopt.member.unit.hours.WorkingHours;
import com.dna.jopt.member.unit.node.INode;
import com.dna.jopt.member.unit.node.geo.TimeWindowGeoNode;
import com.dna.jopt.member.unit.nodeedge.ConnectionBucket;
import com.dna.jopt.member.unit.nodeedge.INodeConnectorItem;
import com.dna.jopt.member.unit.nodeedge.INodeEdgeConnector;
import com.dna.jopt.member.unit.nodeedge.NodeEdgeConnector;
import com.dna.jopt.member.unit.nodeedge.NodeEdgeConnectorItem;
import com.dna.jopt.member.unit.nodeedge.TimedConnectionData;
import com.dna.jopt.member.unit.nodeedge.TimedNodeConnectorItem;
import com.dna.jopt.member.unit.nodeedge.time.DayDef;
import com.dna.jopt.member.unit.nodeedge.time.RangeDef;
import com.dna.jopt.member.unit.resource.CapacityResource;
import com.dna.jopt.member.unit.resource.IResource;
import com.dna.jopt.touroptimizer.java.examples.ExampleLicenseHelper;
import com.dna.jopt.touroptimizer.java.examples.util.jsonprinter.ResultJsonPrinter;

/**
 * Makes the driving times depend on the day of the week and the time of day, with a
 * time-dependent connection store.
 *
 * <p>Scenario: Jessi from Aachen visits Koeln, KoelnWeek, KoelnWeekend, Essen and Dueren on
 * Friday, February 19, and Sunday, February 21, 2100 (Europe/Berlin). Every connection between two
 * places carries a weekday profile with rush hours and a weekend profile with light delays.
 * KoelnWeek is open only on the Friday, KoelnWeekend only on the Sunday.
 *
 * <p>Look for: the driving times in the result. The drive from Aachen to the Koeln point at 08:00
 * takes longer on the Friday than on the Sunday. After the result the example prints the
 * optimization as JSON, with the driving time of every hour of each connection.
 *
 * <p>Prerequisites: None: without a license file the example uses the public evaluation license
 * (see ExampleLicenseHelper).
 *
 * @see <a href="https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/connectionstore/ConnectionStore.md">ConnectionStore.md</a>
 */
public class ConnectionStoreExample extends Optimization {

  /**
   * Runs the example.
   *
   * @param args not used
   * @throws InterruptedException if the wait for the result is interrupted
   * @throws ExecutionException if the optimization run fails
   * @throws TimeoutException if the run does not finish within the wait
   * @throws InvalidLicenceException if the license is not valid
   * @throws IOException if a file cannot be read or written
   * @throws ConvertException if the optimization cannot be converted to JSON
   */
  public static void main(String[] args)
      throws InterruptedException, ExecutionException, TimeoutException, InvalidLicenceException,
      IOException, ConvertException {
    new ConnectionStoreExample().example();
  }

  @Override
  public String toString() {
    return "Makes the driving times depend on the day of the week and the time of day, with a "
        + "time-dependent connection store.";
  }

  /**
   * Builds the optimization with the connection store, runs it, prints the result and prints the
   * optimization as JSON.
   *
   * @throws InterruptedException if the wait for the result is interrupted
   * @throws ExecutionException if the optimization run fails
   * @throws TimeoutException if the run does not finish within the wait
   * @throws InvalidLicenceException if the license is not valid
   * @throws IOException if a file cannot be read or written
   * @throws ConvertException if the optimization cannot be converted to JSON
   */
  public void example()
      throws InterruptedException, ExecutionException, TimeoutException, InvalidLicenceException,
      IOException, ConvertException {
    // The public evaluation license, or your own license if you put a license file where
    // ExampleLicenseHelper looks for it.
    ExampleLicenseHelper.setLicense(this);
    addProperties(this);
    List<IOptimizationElement> elements = new ArrayList<>();
    elements.addAll(addNodes(this));
    elements.addAll(addResources(this));
    addConnections(this, elements);
    attachToObservables(this);
    startAndPresentResult(this);
    printAsJson(this);
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
   * Adds five nodes: Koeln, Essen and Dueren are open on both days, KoelnWeek only on the Friday
   * and KoelnWeekend only on the Sunday.
   *
   * @param opti the optimization
   * @return the nodes
   */
  private static List<INode> addNodes(IOptimization opti) {
    // A Friday and a Sunday (sample dates), so that each of the two traffic profiles applies.
    List<IOpeningHours> friday = new ArrayList<>();
    friday.add(
        new OpeningHours(
            ZonedDateTime.of(2100, FEBRUARY.getValue(), 19, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(
                2100, FEBRUARY.getValue(), 19, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    List<IOpeningHours> sunday = new ArrayList<>();
    sunday.add(
        new OpeningHours(
            ZonedDateTime.of(2100, FEBRUARY.getValue(), 21, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(
                2100, FEBRUARY.getValue(), 21, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    List<IOpeningHours> bothDays = new ArrayList<>();
    bothDays.addAll(friday);
    bothDays.addAll(sunday);

    // Each visit takes 60 minutes and counts toward the working time, like the driving.
    Duration visitDuration = Duration.ofMinutes(60);

    // The same importance for every node, so no node is preferred over another.
    int importance = 1;

    List<INode> nodes = new ArrayList<>();
    nodes.add(new TimeWindowGeoNode("Koeln", 50.9333, 6.95, bothDays, visitDuration, importance));
    // KoelnWeek and KoelnWeekend share the point of Koeln; only their opening days differ.
    nodes.add(
        new TimeWindowGeoNode("KoelnWeek", 50.9333, 6.95, friday, visitDuration, importance));
    nodes.add(
        new TimeWindowGeoNode("KoelnWeekend", 50.9333, 6.95, sunday, visitDuration, importance));
    nodes.add(new TimeWindowGeoNode("Essen", 51.45, 7.01667, bothDays, visitDuration, importance));
    nodes.add(new TimeWindowGeoNode("Dueren", 50.8, 6.48333, bothDays, visitDuration, importance));

    opti.addNodes(nodes);
    return nodes;
  }

  /**
   * Adds Jessi, who starts and ends both working days in Aachen.
   *
   * @param opti the optimization
   * @return the resources
   */
  private static List<IResource> addResources(IOptimization opti) {
    List<IWorkingHours> workingHours = new ArrayList<>();
    workingHours.add(
        new WorkingHours(
            ZonedDateTime.of(2100, FEBRUARY.getValue(), 19, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(
                2100, FEBRUARY.getValue(), 19, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));
    workingHours.add(
        new WorkingHours(
            ZonedDateTime.of(2100, FEBRUARY.getValue(), 21, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(
                2100, FEBRUARY.getValue(), 21, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    // 9 h per day, the length of the shift; both days of this plan stay below it.
    Duration maxWorkingTime = Duration.ofHours(9);
    // 1200 km per day, more than the longest day of this plan needs.
    Quantity<Length> maxDistanceKmW = Quantities.getQuantity(1200.0, KILO(METRE));

    IResource jessi =
        new CapacityResource(
            "Jessi from Aachen", 50.775346, 6.083887, maxWorkingTime, maxDistanceKmW, workingHours);

    List<IResource> resources = new ArrayList<>();
    resources.add(jessi);
    opti.addResources(resources);
    return resources;
  }

  /**
   * Adds a connection between every two elements, with the two traffic profiles where the
   * elements are at different places.
   *
   * @param opti the optimization
   * @param elements the nodes and the resources
   */
  private static void addConnections(IOptimization opti, List<IOptimizationElement> elements) {
    INodeEdgeConnector connector = new NodeEdgeConnector();
    connector.putNodeConnections(createConnections(elements));
    opti.setNodeConnector(connector);
  }

  /**
   * Creates the connections between every two elements, with the flat-earth distance and a base
   * driving time.
   *
   * @param elements the nodes and the resources
   * @return the connections
   */
  private static List<INodeConnectorItem> createConnections(List<IOptimizationElement> elements) {
    // The speed of the base driving times, in meters per second (a sample value).
    double speedMeterPerSecond = 22.0;

    List<INodeConnectorItem> connections = new ArrayList<>();

    for (IOptimizationElement from : elements) {
      for (IOptimizationElement to : elements) {
        if (from != to) {
          double distanceMeter =
              NodeEdgeConnector.distancePlacePlaceFlatEarth(
                  from.getLongitude(), from.getLatitude(), to.getLongitude(), to.getLatitude());

          INodeConnectorItem connection = new NodeEdgeConnectorItem();
          connection.setFromOptimizationElement(from);
          connection.setToOptimizationElement(to);
          connection.setDistance(Quantities.getQuantity(distanceMeter, METRE));
          connection.setDrivingTime(element2ElementDuration(distanceMeter, speedMeterPerSecond));

          connections.add(connection);

          // The three Koeln nodes share one point; their connections stay at zero, without
          // profiles.
          if (distanceMeter > 0.0) {
            // The core turns the points of both buckets into one driving time per hour, and it
            // sets the connection's own distance and driving time to the mean of all points.
            TimedNodeConnectorItem.setTimedBucketData(connection,
                createConnectionBucket(connection));
          }
        }
      }
    }

    return connections;
  }

  /**
   * Creates the two buckets of a connection: the weekend profile for Saturday and Sunday and the
   * weekday profile for Monday to Friday.
   *
   * @param connection the connection with its base distance and driving time
   * @return the buckets
   */
  private static List<ConnectionBucket> createConnectionBucket(INodeConnectorItem connection) {
    Quantity<Length> distance = Quantities.getQuantity(connection.getDistanceMeter(), METRE);
    Duration driving = Duration.ofMillis(connection.getBaseTimeMillis());

    ConnectionBucket weekendBucket =
        new ConnectionBucket(
            RangeDef.of(DayDef.of(DayOfWeek.SATURDAY), DayDef.of(DayOfWeek.SUNDAY)),
            createTimedWeekendConnections(distance, driving));

    ConnectionBucket weekBucket =
        new ConnectionBucket(
            RangeDef.of(DayDef.of(DayOfWeek.MONDAY), DayDef.of(DayOfWeek.FRIDAY)),
            createTimedWeekdayConnections(distance, driving));

    return List.of(weekendBucket, weekBucket);
  }

  /**
   * Creates the weekday profile (Monday to Friday) with a morning and an evening rush hour.
   *
   * <p>Each point sets the driving time of its hour. The core gives the hours between two points
   * the mean of the two, and the hours before the first and after the last point the mean of the
   * first and the last. The multipliers are sample values:
   *
   * <ul>
   *   <li><b>06:00</b> normal traffic (multiplier 1.0)
   *   <li><b>07:00 and 09:00</b> morning rush hour (multiplier 1.8)
   *   <li><b>13:00</b> normal midday traffic (multiplier 1.0)
   *   <li><b>16:00 and 19:00</b> evening rush hour (multiplier 1.6)
   *   <li><b>20:00</b> normal night traffic (multiplier 1.0)
   * </ul>
   *
   * @param distance the base distance of the connection
   * @param driving the base driving time of the connection
   * @return the points of the profile
   */
  private static List<TimedConnectionData> createTimedWeekdayConnections(
      Quantity<Length> distance, Duration driving) {
    double normalMultiplier = 1.0;
    double rushHourMorningMultiplier = 1.8;
    double rushHourEveningMultiplier = 1.6;

    LocalTime normalMorning = LocalTime.of(6, 0);
    LocalTime rushHourMorningStart = LocalTime.of(7, 0);
    LocalTime rushHourMorningEnd = LocalTime.of(9, 0);
    LocalTime normalNoon = LocalTime.of(13, 0);
    LocalTime rushHourEveningStart = LocalTime.of(16, 0);
    LocalTime rushHourEveningEnd = LocalTime.of(19, 0);
    LocalTime normalNight = LocalTime.of(20, 0);

    return List.of(
        createTimedConnection(normalMorning, distance, driving, normalMultiplier),
        createTimedConnection(rushHourMorningStart, distance, driving, rushHourMorningMultiplier),
        createTimedConnection(rushHourMorningEnd, distance, driving, rushHourMorningMultiplier),
        createTimedConnection(normalNoon, distance, driving, normalMultiplier),
        createTimedConnection(rushHourEveningStart, distance, driving, rushHourEveningMultiplier),
        createTimedConnection(rushHourEveningEnd, distance, driving, rushHourEveningMultiplier),
        createTimedConnection(normalNight, distance, driving, normalMultiplier));
  }

  /**
   * Creates the weekend profile (Saturday and Sunday) with light delays at the times of the
   * weekday rush hours. The multipliers are sample values.
   *
   * @param distance the base distance of the connection
   * @param driving the base driving time of the connection
   * @return the points of the profile
   */
  private static List<TimedConnectionData> createTimedWeekendConnections(
      Quantity<Length> distance, Duration driving) {
    double normalMultiplier = 1.0;
    double rushHourMorningMultiplier = 1.05;
    double rushHourEveningMultiplier = 1.05;

    LocalTime normalMorning = LocalTime.of(6, 0);
    LocalTime rushHourMorningStart = LocalTime.of(7, 0);
    LocalTime rushHourMorningEnd = LocalTime.of(9, 0);
    LocalTime normalNoon = LocalTime.of(13, 0);
    LocalTime rushHourEveningStart = LocalTime.of(16, 0);
    LocalTime rushHourEveningEnd = LocalTime.of(19, 0);
    LocalTime normalNight = LocalTime.of(20, 0);

    return List.of(
        createTimedConnection(normalMorning, distance, driving, normalMultiplier),
        createTimedConnection(rushHourMorningStart, distance, driving, rushHourMorningMultiplier),
        createTimedConnection(rushHourMorningEnd, distance, driving, rushHourMorningMultiplier),
        createTimedConnection(normalNoon, distance, driving, normalMultiplier),
        createTimedConnection(rushHourEveningStart, distance, driving, rushHourEveningMultiplier),
        createTimedConnection(rushHourEveningEnd, distance, driving, rushHourEveningMultiplier),
        createTimedConnection(normalNight, distance, driving, normalMultiplier));
  }

  /**
   * Calculates the driving time for a distance at a speed.
   *
   * @param distanceMeter the distance in meters
   * @param speedMeterPerSecond the speed in meters per second
   * @return the driving time
   */
  private static Duration element2ElementDuration(double distanceMeter,
      double speedMeterPerSecond) {
    long traveltimeMillis = (long) (distanceMeter / speedMeterPerSecond * 1000L);
    return Duration.ofMillis(traveltimeMillis);
  }

  /**
   * Creates one point of a profile: the base driving time times the multiplier, at a time of day.
   * The distance stays the base distance.
   *
   * @param time the time of day of the point
   * @param distance the base distance of the connection
   * @param driving the base driving time of the connection
   * @param multiplier the factor for the driving time
   * @return the point
   */
  private static TimedConnectionData createTimedConnection(
      LocalTime time, Quantity<Length> distance, Duration driving, double multiplier) {
    return new TimedConnectionData(
        time, distance, Duration.ofMillis(Math.round(driving.toMillis() * multiplier)));
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
   * Prints the optimization as JSON, without the solution and the build options but with the
   * connections, so the stored driving times of each connection show.
   *
   * @param opti the optimization
   * @throws IOException if a file cannot be read or written
   * @throws ConvertException if the optimization cannot be converted to JSON
   */
  private static void printAsJson(IOptimization opti) throws IOException, ConvertException {
    // true keeps the connections in the JSON.
    ResultJsonPrinter.printResultAsJson(opti, true);
  }
}
