package com.dna.jopt.touroptimizer.java.examples.advanced.zonecrossing;
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

import java.io.File;
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

import com.dna.jopt.config.convert.ConvertException;
import com.dna.jopt.config.json.types.JSONConfig;
import com.dna.jopt.config.serialize.SerializationException;
import com.dna.jopt.config.types.Position;
import com.dna.jopt.framework.body.IOptimization;
import com.dna.jopt.framework.body.Optimization;
import com.dna.jopt.framework.exception.caught.InvalidLicenceException;
import com.dna.jopt.framework.outcomewrapper.IOptimizationResult;
import com.dna.jopt.io.exporting.kml.EntityKMLExporter;
import com.dna.jopt.member.unit.condition.workinghour.zone.zonenumber.ZoneNumber;
import com.dna.jopt.member.unit.condition.workinghour.zone.zonenumber.ZoneNumberQualification;
import com.dna.jopt.member.unit.hours.IOpeningHours;
import com.dna.jopt.member.unit.hours.IWorkingHours;
import com.dna.jopt.member.unit.hours.OpeningHours;
import com.dna.jopt.member.unit.hours.WorkingHours;
import com.dna.jopt.member.unit.node.INode;
import com.dna.jopt.member.unit.node.geo.TimeWindowGeoNode;
import com.dna.jopt.member.unit.resource.CapacityResource;
import com.dna.jopt.member.unit.resource.IResource;
import com.dna.jopt.member.unit.zonemanager.IZoneManager;
import com.dna.jopt.member.unit.zonemanager.ZoneConnection;
import com.dna.jopt.touroptimizer.java.examples.ExampleLicenseHelper;
import com.dna.jopt.touroptimizer.java.examples.util.jsonprinter.ResultJsonPrinter;

/**
 * Adds a cost to every route step from a node of one zone to a node of another zone, so that the
 * routes stay on one side of the Hudson.
 *
 * <p>Scenario: Jack and Mel start in Queens and visit eight nodes in Manhattan (zone 1) and eight
 * JerseyCity nodes across the Hudson (zone 3) on May 6 and May 7, 2020 (America/New_York). A
 * crossing from zone 1 to zone 3 costs extra; the other direction does not.
 *
 * <p>Look for: with doPenalizeZoneCrossings set to true, routes that each stay in one zone; then
 * the JSON of the optimization, which holds no license.
 *
 * <p>Prerequisites: None: without a license file the example uses the public evaluation license
 * (see ExampleLicenseHelper). It needs that license, because it has more elements than JOpt
 * allows without one.
 *
 * @see <a href="https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/zonecrossing/BridgeTunnelCrossingZoneNumberConstraint.md">BridgeTunnelCrossingZoneNumberConstraint.md</a>
 * @see <a href="https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/zonecrossing">Zone crossing penalization</a>
 */
public class BridgeTunnelCrossingZoneNumberConstraintExample extends Optimization {

  /**
   * Runs the example.
   *
   * @param args not used
   * @throws InterruptedException if the wait for the result is interrupted
   * @throws ExecutionException if the optimization run fails
   * @throws TimeoutException if the run does not finish within the wait
   * @throws InvalidLicenceException if the license is not valid
   * @throws IOException if a file cannot be read or written
   * @throws ConvertException if the optimization cannot be converted to a config
   * @throws SerializationException if the config cannot be serialized
   */
  public static void main(String[] args)
      throws InterruptedException, ExecutionException, TimeoutException, InvalidLicenceException,
      IOException, ConvertException, SerializationException {
    new BridgeTunnelCrossingZoneNumberConstraintExample().example();
  }

  @Override
  public String toString() {
    return "Adds a cost to every route step from a node of one zone to a node of another zone, so"
        + " that the routes stay on one side of the Hudson.";
  }

  /**
   * Builds the optimization, runs it, prints the result, writes it to KML and prints the JSON.
   *
   * @throws InterruptedException if the wait for the result is interrupted
   * @throws ExecutionException if the optimization run fails
   * @throws TimeoutException if the run does not finish within the wait
   * @throws InvalidLicenceException if the license is not valid
   * @throws IOException if a file cannot be read or written
   * @throws ConvertException if the optimization cannot be converted to a config
   * @throws SerializationException if the config cannot be serialized
   */
  public void example()
      throws InterruptedException, ExecutionException, TimeoutException, InvalidLicenceException,
      IOException, ConvertException, SerializationException {
    // Set to false to see the routes without the crossing penalty.
    boolean doPenalizeZoneCrossings = true;

    // The public evaluation license, or your own license if you put a license file where
    // ExampleLicenseHelper looks for it.
    ExampleLicenseHelper.setLicense(this);
    addProperties(this, doPenalizeZoneCrossings);
    addNodes(this);
    addResources(this);
    addZoneConnections(this);
    attachToObservables(this);
    IOptimizationResult result = startAndPresentResult(this);
    exportToKml(result, doPenalizeZoneCrossings);
    printJson(this);
  }

  /**
   * Adds the properties that keep the run short and switch the crossing penalty on or off.
   *
   * @param opti the optimization
   * @param doPenalizeZoneCrossings whether a step between two zones costs extra
   */
  private static void addProperties(IOptimization opti, boolean doPenalizeZoneCrossings) {
    Properties props = new Properties();
    // Fewer generations and iterations than the defaults keep the run short.
    props.setProperty("JOptExitCondition.JOptGenerationCount", "1000");
    props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumIterations", "100000");

    props.setProperty(
        "JOpt.Clustering.PenalizeZoneCodeCrossing", String.valueOf(doPenalizeZoneCrossings));
    // The extra cost of a step between two zones: its distance times the TotalDistance and
    // RouteDistance weights, times the factor of its ZoneConnection, times this multiplier. With
    // 10.0, no route of this plan crosses between the zones (see the output of the example).
    props.setProperty("JOpt.Clustering.PenalizeZoneCodeCrossingMultiplier", "10.0");

    opti.addElement(props);
  }

  /**
   * Adds the nodes of both zones with the same opening hours.
   *
   * @param opti the optimization
   */
  private static void addNodes(IOptimization opti) {
    List<IOpeningHours> openingHours = new ArrayList<>();
    openingHours.add(
        new OpeningHours(
            ZonedDateTime.of(2020, MAY.getValue(), 6, 8, 0, 0, 0, ZoneId.of("America/New_York")),
            ZonedDateTime.of(
                2020, MAY.getValue(), 6, 17, 0, 0, 0, ZoneId.of("America/New_York"))));
    openingHours.add(
        new OpeningHours(
            ZonedDateTime.of(2020, MAY.getValue(), 7, 8, 0, 0, 0, ZoneId.of("America/New_York")),
            ZonedDateTime.of(
                2020, MAY.getValue(), 7, 17, 0, 0, 0, ZoneId.of("America/New_York"))));

    // Manhattan is zone 1; the JerseyCity nodes across the Hudson are zone 3. The extra codes 2
    // and 4 show that a qualification can hold several codes; the two areas share none, so every
    // step between them is a crossing.
    ZoneNumberQualification manhattanQuali = new ZoneNumberQualification(new ZoneNumber(1));
    manhattanQuali.addExtraCode(new ZoneNumber(2));

    ZoneNumberQualification jerseyQuali = new ZoneNumberQualification(new ZoneNumber(3));
    jerseyQuali.addExtraCode(new ZoneNumber(4));

    addManhattanNodes(opti, openingHours, manhattanQuali);
    addJerseyCityNodes(opti, openingHours, jerseyQuali);
  }

  /**
   * Adds eight nodes in Manhattan, all in zone 1.
   *
   * @param opti the optimization
   * @param openingHours the opening hours of the nodes
   * @param zoneQuali the zone qualification of the nodes
   */
  private static void addManhattanNodes(
      IOptimization opti, List<IOpeningHours> openingHours, ZoneNumberQualification zoneQuali) {
    List<Position> positions = new ArrayList<>();
    positions.add(Position.of(40.764279, -73.988988));
    positions.add(Position.of(40.761822, -73.968600));
    positions.add(Position.of(40.764162, -73.991906));
    positions.add(Position.of(40.723670, -73.998738));
    positions.add(Position.of(40.796056, -73.967102));
    positions.add(Position.of(40.761964, -73.972156));
    positions.add(Position.of(40.737567, -74.009090));
    positions.add(Position.of(40.733846, -74.009090));

    // 60 minutes per visit, a sample value: eight visits and the drives fit into one working day,
    // so one resource can serve all nodes of a zone on May 6.
    Duration visitDuration = Duration.ofMinutes(60);

    for (int ii = 0; ii < positions.size(); ii++) {
      INode node =
          new TimeWindowGeoNode(
              "Manhattan-" + ii, positions.get(ii), openingHours, visitDuration, 1);
      node.addQualification(zoneQuali);
      opti.addElement(node);
    }
  }

  /**
   * Adds eight nodes across the Hudson from Manhattan, all in zone 3.
   *
   * @param opti the optimization
   * @param openingHours the opening hours of the nodes
   * @param zoneQuali the zone qualification of the nodes
   */
  private static void addJerseyCityNodes(
      IOptimization opti, List<IOpeningHours> openingHours, ZoneNumberQualification zoneQuali) {
    List<Position> positions = new ArrayList<>();
    positions.add(Position.of(40.751788, -74.027374));
    positions.add(Position.of(40.725626, -74.037277));
    positions.add(Position.of(40.751106, -74.025960));
    positions.add(Position.of(40.759971, -74.023066));
    positions.add(Position.of(40.746625, -74.026088));
    positions.add(Position.of(40.748330, -74.057274));
    positions.add(Position.of(40.738185, -74.027539));
    positions.add(Position.of(40.740757, -74.026748));

    // 60 minutes per visit, a sample value, as in Manhattan.
    Duration visitDuration = Duration.ofMinutes(60);

    for (int ii = 0; ii < positions.size(); ii++) {
      INode node =
          new TimeWindowGeoNode(
              "JerseyCity-" + ii, positions.get(ii), openingHours, visitDuration, 1);
      node.addQualification(zoneQuali);
      opti.addElement(node);
    }
  }

  /**
   * Adds Jack and Mel, who start and end each working day in Queens.
   *
   * @param opti the optimization
   */
  private static void addResources(IOptimization opti) {
    // Both resources start in Queens, outside both zones. The start is not a node, so the drive
    // to the first node of a route never counts as a crossing.
    Position queens = Position.of(40.742728, -73.870528);

    // The working hours carry no zone constraints: both resources may visit both zones, and only
    // the crossing cost keeps a route on one side.
    List<IWorkingHours> workingHoursJack = createWorkingHours();
    List<IWorkingHours> workingHoursMel = createWorkingHours();

    // 13 h and 1200 km per day, sample values above what a day of this plan needs.
    Duration maxWorkingTime = Duration.ofHours(13);
    Quantity<Length> maxDistanceKmW = Quantities.getQuantity(1200.0, KILO(METRE));

    IResource jack =
        new CapacityResource("Jack", queens, maxWorkingTime, maxDistanceKmW, workingHoursJack);
    opti.addElement(jack);

    IResource mel =
        new CapacityResource("Mel", queens, maxWorkingTime, maxDistanceKmW, workingHoursMel);
    opti.addElement(mel);
  }

  /**
   * Creates the working hours of one resource: 08:00 to 20:00 on May 6 and May 7, 2020.
   *
   * @return the working hours
   */
  private static List<IWorkingHours> createWorkingHours() {
    List<IWorkingHours> workingHours = new ArrayList<>();
    workingHours.add(
        new WorkingHours(
            ZonedDateTime.of(2020, MAY.getValue(), 6, 8, 0, 0, 0, ZoneId.of("America/New_York")),
            ZonedDateTime.of(
                2020, MAY.getValue(), 6, 20, 0, 0, 0, ZoneId.of("America/New_York"))));
    workingHours.add(
        new WorkingHours(
            ZonedDateTime.of(2020, MAY.getValue(), 7, 8, 0, 0, 0, ZoneId.of("America/New_York")),
            ZonedDateTime.of(
                2020, MAY.getValue(), 7, 20, 0, 0, 0, ZoneId.of("America/New_York"))));
    return workingHours;
  }

  /**
   * Adds the factors of the crossing penalty for both directions between zone 1 and zone 3.
   *
   * @param opti the optimization
   */
  private static void addZoneConnections(IOptimization opti) {
    IZoneManager zm = opti.getNodeConnector().getZoneManager();

    // The sample factor 5.0: a step from zone 1 to zone 3 costs five times the crossing penalty,
    // like a toll that is charged in one direction only.
    ZoneConnection zoneConnection13 =
        ZoneConnection.builder()
            .fromZoneId("1")
            .toZoneId("3")
            .crossingPenaltyMultiplier(5.0)
            .build();
    zm.putZoneConnection(zoneConnection13);

    // The factor 0.0 switches the penalty off for steps from zone 3 to zone 1.
    ZoneConnection zoneConnection31 =
        ZoneConnection.builder()
            .fromZoneId("3")
            .toZoneId("1")
            .crossingPenaltyMultiplier(0.0)
            .build();
    zm.putZoneConnection(zoneConnection31);
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
   * Writes the result to a KML file in the working directory, named after the class and the
   * setting of the penalty.
   *
   * @param result the result of the run
   * @param doPenalizeZoneCrossings whether the run penalized zone crossings
   * @throws IOException if a file cannot be read or written
   */
  private static void exportToKml(IOptimizationResult result, boolean doPenalizeZoneCrossings)
      throws IOException {
    String name = BridgeTunnelCrossingZoneNumberConstraintExample.class.getSimpleName();
    // One file per setting, so that the routes with and without the penalty can be compared.
    File file = new File(name + "-" + doPenalizeZoneCrossings + ".kml");

    EntityKMLExporter kmlExporter = new EntityKMLExporter();
    kmlExporter.setTitle(name);

    try (OutputStream out = new FileOutputStream(file)) {
      if (!kmlExporter.export(result, out)) {
        System.out.println("KML export failed: " + file);
      }
    }
  }

  /**
   * Prints the optimization as JSON, without the solution, the node connections and a license.
   *
   * @param opti the optimization
   * @throws IOException if a file cannot be read or written
   * @throws ConvertException if the optimization cannot be converted to a config
   * @throws SerializationException if the config cannot be serialized
   */
  private static void printJson(IOptimization opti)
      throws IOException, ConvertException, SerializationException {
    // The ZoneConnections work without the node connections, so the JSON leaves them out and
    // stays shorter. It leaves out the solution as well.
    boolean keepSolutionInJson = false;
    boolean keepNodeConnectionsInJson = false;

    // The extension holds a timeout of 10 minutes, a sample value, and no key setting, so the
    // printed JSON contains no license.
    JSONConfig extension = JSONConfig.builder().timeOut(Duration.ofMinutes(10)).build();

    String json =
        ResultJsonPrinter.toJsonOptimizationString(
            ResultJsonPrinter.toJsonOptimization(
                opti, extension, keepSolutionInJson, keepNodeConnectionsInJson));
    System.out.println(json);
  }
}
