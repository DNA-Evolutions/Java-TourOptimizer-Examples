package com.dna.jopt.touroptimizer.java.examples.advanced.performancemode;
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
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.dna.jopt.config.types.Position;
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
import com.dna.jopt.member.unit.node.geo.TimeWindowGeoNode;
import com.dna.jopt.member.unit.resource.CapacityResource;
import com.dna.jopt.touroptimizer.java.examples.ExampleLicenseHelper;

/**
 * Runs performance mode on a plan for 2000 nodes and 40 resources in the west of Koeln, the setup
 * of the evaluation in the performance mode feature guide.
 *
 * <p>Needs a full license: the example has more elements than the public evaluation license
 * allows. With the public evaluation license the run ends with a license error before the
 * optimization starts.
 *
 * <p>Scenario: the nodes and the resources lie on two phyllotaxis patterns around a point in the
 * west of Koeln (50.9333, 6.85): element i lies at the distance spacing x sqrt(i) from that point
 * and at i times the golden angle. The nodes are open and the resources work on May 6, 2023, from
 * 08:00 to 22:00 (Europe/Berlin).
 *
 * <p>Look for: the routes of the result, the run time in the last line, and the file
 * PerformanceModeBigOptiExample.kml, which shows the routes on a map.
 *
 * <p>Prerequisites: your own full license file where ExampleLicenseHelper looks for it.
 *
 * @see <a href="https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/performancemode/PerformanceMode.md">PerformanceMode.md</a>
 */
public class PerformanceModeBigOptiExample extends Optimization {

  // Set to false and run the example again to compare the run time without performance mode.
  private static final boolean USE_PERFORMANCE_MODE = true;

  // true adds the skill matching of the evaluation in the performance mode feature guide: half of
  // the nodes need the type "efficient", and a third of the resources have it.
  private static final boolean ADD_SKILL_CONSTRAINTS = false;

  // true moves the nodes and resources with an even index to May 7, which splits the plan into two
  // days.
  private static final boolean ADD_EVEN_ODD_SPLITTING = false;

  // A fixed seed, so the same nodes and resources get the type in every run.
  private static final long SKILL_SEED = 123456789L;

  // A point in the west of Koeln, the same as in the clustering examples. Both patterns lie
  // around it.
  private static final Position PATTERN_ORIGIN = Position.of(50.9333, 6.85);

  // The size of the evaluation in the performance mode feature guide.
  private static final int NUM_NODES = 2000;
  private static final int NUM_RESOURCES = 40;

  // Spacings of the two patterns in degrees: the outermost node lies about 0.089 degrees from
  // PATTERN_ORIGIN and the outermost resource about 0.062, so the resources lie inside the area of
  // the nodes.
  private static final double NODE_SPACING = 0.002;
  private static final double RESOURCE_SPACING = 0.01;

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
    new PerformanceModeBigOptiExample().example();
  }

  @Override
  public String toString() {
    return "Runs performance mode on a plan for 2000 nodes and 40 resources in the west of Koeln,"
        + " the setup of the evaluation in the performance mode feature guide.";
  }

  /**
   * Builds the optimization, runs it, prints the result and the run time and writes the result to
   * a KML file.
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
    addProperties(this);
    addNodes(this);
    addResources(this);
    attachToObservables(this);
    IOptimizationResult result = startAndPresentResult(this);
    exportToKml(result);
  }

  /**
   * Adds the properties of the run, with performance mode set from USE_PERFORMANCE_MODE.
   *
   * @param opti the optimization
   */
  private static void addProperties(IOptimization opti) {
    Properties props = new Properties();
    // 100000 generations, the smallest generation count of the evaluation in the performance mode
    // feature guide.
    props.setProperty("JOptExitCondition.JOptGenerationCount", "100000");
    // Fewer iterations than the default keep the simulated annealing short.
    props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumIterations", "100000");
    props.setProperty("JOpt.PerformanceMode", String.valueOf(USE_PERFORMANCE_MODE));
    opti.addElement(props);
  }

  /**
   * Adds the nodes on a phyllotaxis pattern around a point in the west of Koeln.
   *
   * @param opti the optimization
   */
  private static void addNodes(IOptimization opti) {
    List<Position> positions = samplePhyllotaxis(PATTERN_ORIGIN, NUM_NODES, NODE_SPACING);

    // The nodes that need the type "efficient": half of them, chosen with the fixed seed.
    Set<Integer> constrainedNodes =
        ADD_SKILL_CONSTRAINTS ? pickIndices(NUM_NODES / 2, NUM_NODES, SKILL_SEED) : Set.of();

    // Each visit takes 5 minutes and counts toward the working time, like the driving.
    Duration visitDuration = Duration.ofMinutes(5);

    // The same importance for every node, so no node is preferred over another.
    int importance = 1;

    for (int ii = 0; ii < positions.size(); ii++) {
      Position pos = positions.get(ii);
      TimeWindowGeoNode node =
          new TimeWindowGeoNode(
              "N_" + ii,
              pos.latitude(),
              pos.longitude(),
              createOpeningHours(dayOfMay(ii)),
              visitDuration,
              importance);
      if (constrainedNodes.contains(ii)) {
        TypeConstraint typeConstraint = new TypeConstraint();
        typeConstraint.addType("efficient");
        node.addConstraint(typeConstraint);
      }
      opti.addElement(node);
    }
  }

  /**
   * Adds the resources on a phyllotaxis pattern around a point in the west of Koeln.
   *
   * @param opti the optimization
   */
  private static void addResources(IOptimization opti) {
    List<Position> positions = samplePhyllotaxis(PATTERN_ORIGIN, NUM_RESOURCES, RESOURCE_SPACING);

    // The resources that have the type "efficient": a third of them, chosen with the fixed seed.
    Set<Integer> qualifiedResources =
        ADD_SKILL_CONSTRAINTS
            ? pickIndices(NUM_RESOURCES / 3, NUM_RESOURCES, SKILL_SEED)
            : Set.of();

    // 12 h per day, a sample value.
    Duration maxWorkingTime = Duration.ofHours(12);
    // 2200 km per day, more than a resource can drive in 24 hours at its average speed
    // (setAvgSpeed).
    Quantity<Length> maxDistanceKmW = Quantities.getQuantity(2200.0, KILO(METRE));

    for (int ii = 0; ii < positions.size(); ii++) {
      Position pos = positions.get(ii);
      CapacityResource resource =
          new CapacityResource(
              "R_" + ii,
              pos.latitude(),
              pos.longitude(),
              maxWorkingTime,
              maxDistanceKmW,
              createWorkingHours(dayOfMay(ii)));
      if (qualifiedResources.contains(ii)) {
        TypeQualification typeQualification = new TypeQualification();
        typeQualification.addType("efficient");
        resource.addQualification(typeQualification);
      }
      opti.addElement(resource);
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
   * Picks distinct indices from 0 to bound - 1 with a seeded random generator.
   *
   * @param count the number of indices to pick, at most bound
   * @param bound the number of indices to pick from
   * @param seed the seed of the random generator
   * @return the picked indices
   */
  private static Set<Integer> pickIndices(int count, int bound, long seed) {
    Random random = new Random(seed);
    Set<Integer> indices = new TreeSet<>();
    while (indices.size() < count) {
      indices.add(random.nextInt(bound));
    }
    return indices;
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
   * Starts the run, waits for the result, prints it and the run time and returns it.
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
    long startNanos = System.nanoTime();
    CompletableFuture<IOptimizationResult> resultFuture = opti.startRunAsync();
    // Wait for the result; without the wait the JVM would end before the run does. A run of this
    // size can take several minutes (see the performance mode feature guide), so the wait is
    // longer than in the other examples.
    IOptimizationResult result = resultFuture.get(15, TimeUnit.MINUTES);
    long runMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
    System.out.println(result);
    // The run time depends on the machine: compare runs on the same machine.
    System.out.println(
        "JOpt.PerformanceMode " + USE_PERFORMANCE_MODE + ", run time [s]: " + runMillis / 1000.0);
    return result;
  }

  /**
   * Writes the result to PerformanceModeBigOptiExample.kml in the working directory.
   *
   * @param result the result of the run
   * @throws IOException if a file cannot be read or written
   */
  private static void exportToKml(IOptimizationResult result) throws IOException {
    String kmlFile = "PerformanceModeBigOptiExample.kml";

    EntityKMLExporter kmlExporter = new EntityKMLExporter();
    kmlExporter.setTitle("PerformanceModeBigOptiExample");

    // export flushes the stream but does not close it; the try block closes it.
    try (OutputStream out = new FileOutputStream(kmlFile)) {
      if (!kmlExporter.export(result, out)) {
        System.out.println("KML export failed: " + kmlFile);
      }
    }
  }
}
