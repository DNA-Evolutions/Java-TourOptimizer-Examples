package com.dna.jopt.touroptimizer.java.examples.advanced.clustering;
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
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.dna.jopt.config.types.Position;
import com.dna.jopt.framework.body.IOptimization;
import com.dna.jopt.framework.body.Optimization;
import com.dna.jopt.framework.body.scheme.DefaultOptimizationScheme;
import com.dna.jopt.framework.body.scheme.helper.AbstractConstructionOptimizationAlgorithmConfig.ConstructionOptimizationAlgorithm;
import com.dna.jopt.framework.body.scheme.helper.ConstructionOptimizationAlgorithmConfig;
import com.dna.jopt.framework.exception.caught.InvalidLicenceException;
import com.dna.jopt.framework.outcomewrapper.IOptimizationResult;
import com.dna.jopt.io.exporting.kml.EntityKMLExporter;
import com.dna.jopt.member.unit.hours.IOpeningHours;
import com.dna.jopt.member.unit.hours.IWorkingHours;
import com.dna.jopt.member.unit.hours.OpeningHours;
import com.dna.jopt.member.unit.hours.WorkingHours;
import com.dna.jopt.member.unit.node.geo.TimeWindowGeoNode;
import com.dna.jopt.member.unit.resource.CapacityResource;
import com.dna.jopt.touroptimizer.java.examples.ExampleLicenseHelper;

/**
 * Builds a plan for 1000 nodes and 50 resources in the west of Koeln with the clustering
 * construction alone, without the genetic evolution and the simulated annealing after it.
 *
 * <p>Needs a full license: the example has more elements than the public evaluation license
 * allows. With the public evaluation license the run ends with a license error before the
 * construction starts.
 *
 * <p>Scenario: the nodes and the resources lie on two phyllotaxis patterns around a point in the
 * west of Koeln (50.9333, 6.85): element i lies at the distance spacing x sqrt(i) from that point
 * and at i times the golden angle. The nodes are open and the resources work on May 6, 2023, from
 * 08:00 to 22:00 (Europe/Berlin).
 *
 * <p>Look for: the routes of the result, and the file ClusteringInnerCityExample.kml, which shows
 * them on a map.
 *
 * <p>Prerequisites: your own full license file where ExampleLicenseHelper looks for it.
 *
 * @see <a href="https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/clustering/Clustering.md">Clustering.md</a>
 */
public class ClusteringInnerCityExample extends Optimization {

  // A point in the west of Koeln, the area of the phyllotaxis figures in the clustering
  // feature guide (a setup of their own, with 40 resources). Both patterns lie around it.
  private static final Position PATTERN_ORIGIN = Position.of(50.9333, 6.85);

  private static final int NUM_RESOURCES = 50;
  private static final int NUM_NODES = 1000;

  // Spacings of the two patterns in degrees: the outermost resource lies 0.07 degrees from
  // PATTERN_ORIGIN and the outermost node about 0.063, so the resources spread over the area of
  // the nodes.
  private static final double RESOURCE_SPACING = 0.01;
  private static final double NODE_SPACING = 0.002;

  // true moves the nodes and resources with an even index to May 7, which splits the plan into two
  // days.
  private static final boolean ADD_EVEN_ODD_SPLITTING = false;

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
    new ClusteringInnerCityExample().example();
  }

  @Override
  public String toString() {
    return "Builds a plan for 1000 nodes and 50 resources in the west of Koeln with the clustering"
        + " construction alone, without the genetic evolution and the simulated annealing"
        + " after it.";
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
    // Needs your own full license file where ExampleLicenseHelper looks for it: the public
    // evaluation license does not allow this many elements.
    ExampleLicenseHelper.setLicense(this);
    setConstructionAlgorithm(this);
    addProperties(this);
    addNodes(this);
    addResources(this);
    attachToObservables(this);
    IOptimizationResult result = startAndPresentResult(this);
    exportToKml(result);
  }

  /**
   * Chooses SmartConstructionAlgo as the construction algorithm of the run.
   *
   * @param opti the optimization
   */
  private static void setConstructionAlgorithm(IOptimization opti) {
    // SmartConstructionAlgo builds the start solution by clustering. Without a list of heuristic
    // algorithms (Optional.empty()), the scheme runs its own; the properties set their run values.
    ConstructionOptimizationAlgorithmConfig construction =
        ConstructionOptimizationAlgorithmConfig.builder()
            .algorithm(ConstructionOptimizationAlgorithm.SMART_CONSTR_ALGO)
            .build();
    opti.setOptimizationScheme(
        new DefaultOptimizationScheme(opti, Optional.of(construction), Optional.empty()));
  }

  /**
   * Adds the properties that leave out the genetic evolution and the simulated annealing.
   *
   * @param opti the optimization
   */
  private static void addProperties(IOptimization opti) {
    Properties props = new Properties();
    // Zero generations and zero iterations: no genetic evolution and no simulated annealing after
    // the construction.
    props.setProperty("JOptExitCondition.JOptGenerationCount", "0");
    props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumIterations", "0");
    opti.addElement(props);
  }

  /**
   * Adds the nodes on a phyllotaxis pattern around a point in the west of Koeln.
   *
   * @param opti the optimization
   */
  private static void addNodes(IOptimization opti) {
    List<Position> positions = samplePhyllotaxis(PATTERN_ORIGIN, NUM_NODES, NODE_SPACING);

    // Each visit takes 5 minutes and counts toward the working time, like the driving.
    Duration visitDuration = Duration.ofMinutes(5);

    // The same importance for every node, so no node is preferred over another.
    int importance = 1;

    for (int ii = 0; ii < positions.size(); ii++) {
      Position pos = positions.get(ii);
      opti.addElement(
          new TimeWindowGeoNode(
              "N_" + ii,
              pos.latitude(),
              pos.longitude(),
              createOpeningHours(dayOfMay(ii)),
              visitDuration,
              importance));
    }
  }

  /**
   * Adds the resources on a phyllotaxis pattern around a point in the west of Koeln.
   *
   * @param opti the optimization
   */
  private static void addResources(IOptimization opti) {
    List<Position> positions = samplePhyllotaxis(PATTERN_ORIGIN, NUM_RESOURCES, RESOURCE_SPACING);

    // 12 h per day, a sample value.
    Duration maxWorkingTime = Duration.ofHours(12);
    // 2200 km per day, more than a resource can drive in 24 hours at its average speed
    // (setAvgSpeed).
    Quantity<Length> maxDistanceKmW = Quantities.getQuantity(2200.0, KILO(METRE));

    for (int ii = 0; ii < positions.size(); ii++) {
      Position pos = positions.get(ii);
      opti.addElement(
          new CapacityResource(
              "R_" + ii,
              pos.latitude(),
              pos.longitude(),
              maxWorkingTime,
              maxDistanceKmW,
              createWorkingHours(dayOfMay(ii))));
    }
  }

  /**
   * Returns the day in May 2023 of the node or resource with the given index.
   *
   * @param index the index of the node or resource
   * @return 7 for an even index when ADD_EVEN_ODD_SPLITTING is true, 6 otherwise
   */
  private static int dayOfMay(int index) {
    return ADD_EVEN_ODD_SPLITTING && index % 2 == 0 ? 7 : 6;
  }

  /**
   * Creates the opening hours of a node: the given day in May 2023, from 08:00 to 22:00.
   *
   * @param day the day in May 2023
   * @return the opening hours
   */
  private static List<IOpeningHours> createOpeningHours(int day) {
    // The same frame as the working hours of the resources, so a node matches every resource
    // of its day in time.
    List<IOpeningHours> openingHours = new ArrayList<>();
    openingHours.add(
        new OpeningHours(
            ZonedDateTime.of(2023, MAY.getValue(), day, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2023, MAY.getValue(), day, 22, 0, 0, 0, ZoneId.of("Europe/Berlin"))));
    return openingHours;
  }

  /**
   * Creates the working hours of a resource: the given day in May 2023, from 08:00 to 22:00.
   *
   * @param day the day in May 2023
   * @return the working hours
   */
  private static List<IWorkingHours> createWorkingHours(int day) {
    // 08:00 to 22:00, a frame of 14 hours for the 12 h maximal working time: the working hours
    // are a frame for matching resources with the opening hours of the nodes.
    List<IWorkingHours> workingHours = new ArrayList<>();
    workingHours.add(
        new WorkingHours(
            ZonedDateTime.of(2023, MAY.getValue(), day, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2023, MAY.getValue(), day, 22, 0, 0, 0, ZoneId.of("Europe/Berlin"))));
    return workingHours;
  }

  /**
   * Places n points on a phyllotaxis pattern around an origin: point i lies at the distance spacing
   * x sqrt(i) from the origin and at i times the golden angle.
   *
   * @param origin the point around which the pattern lies
   * @param n the number of points
   * @param spacing the spacing factor in degrees
   * @return the positions of the points
   */
  private static List<Position> samplePhyllotaxis(Position origin, int n, double spacing) {
    // The golden angle in radians.
    double goldenAngle = Math.PI * (3 - Math.sqrt(5));

    List<Position> positions = new ArrayList<>();
    for (int i = 0; i < n; i++) {
      double radius = spacing * Math.sqrt(i);
      double angle = i * goldenAngle;
      positions.add(
          Position.of(
              origin.latitude() + radius * Math.sin(angle),
              origin.longitude() + radius * Math.cos(angle)));
    }
    return positions;
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
   * Writes the result to ClusteringInnerCityExample.kml in the working directory.
   *
   * @param result the result of the run
   * @throws IOException if a file cannot be read or written
   */
  private static void exportToKml(IOptimizationResult result) throws IOException {
    String kmlFile = "ClusteringInnerCityExample.kml";

    EntityKMLExporter kmlExporter = new EntityKMLExporter();
    kmlExporter.setTitle("ClusteringInnerCityExample");

    // export flushes the stream but does not close it; the try block closes it.
    try (OutputStream out = new FileOutputStream(kmlFile)) {
      if (!kmlExporter.export(result, out)) {
        System.out.println("KML export failed: " + kmlFile);
      }
    }
  }
}
