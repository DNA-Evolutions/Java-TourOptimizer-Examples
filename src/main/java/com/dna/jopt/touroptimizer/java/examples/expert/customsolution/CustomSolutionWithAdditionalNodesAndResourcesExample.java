package com.dna.jopt.touroptimizer.java.examples.expert.customsolution;
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
import com.dna.jopt.member.bucket.entity.Entity;
import com.dna.jopt.member.bucket.entity.IEntity;
import com.dna.jopt.member.bucket.route.ILogicEntityRoute;
import com.dna.jopt.member.bucket.route.LogicEntityRoute;
import com.dna.jopt.member.unit.IOptimizationElement;
import com.dna.jopt.member.unit.hours.IOpeningHours;
import com.dna.jopt.member.unit.hours.IWorkingHours;
import com.dna.jopt.member.unit.hours.OpeningHours;
import com.dna.jopt.member.unit.hours.WorkingHours;
import com.dna.jopt.member.unit.node.INode;
import com.dna.jopt.member.unit.node.IPillarNode;
import com.dna.jopt.member.unit.node.geo.PillarTimeWindowGeoNode;
import com.dna.jopt.member.unit.node.geo.TimeWindowGeoNode;
import com.dna.jopt.member.unit.resource.CapacityResource;
import com.dna.jopt.member.unit.resource.IResource;
import com.dna.jopt.touroptimizer.java.examples.ExampleLicenseHelper;

import tech.units.indriya.quantity.Quantities;

/**
 * Starts the optimization from a custom solution and adds more nodes and resources, which the
 * optimizer puts into the solution before the search.
 *
 * <p>Scenario: the custom solution of CustomSolutionExample (Jack from Aachen, May 6 and May 7,
 * 2020, Europe/Berlin), plus the resource JackNew on May 10 and May 11, the nodes Koeln2, Dueren2
 * and Essen2 open on those days and the pillar testPillarKoeln on May 11 from 09:00 to 10:00. A
 * second resource Jack and a second node Essen repeat ids of the custom solution on purpose.
 *
 * <p>Look for: the status lines ADDITIONAL_ELEMENT, the warnings 5557 for the duplicate Jack and
 * the duplicate Essen, and the result with the routes of Jack and JackNew.
 *
 * <p>Prerequisites: None: without a license file the example uses the public evaluation license
 * (see ExampleLicenseHelper). The example writes
 * CustomSolutionWithAdditionalNodesAndResourcesExample.kml into the working directory.
 *
 * @see <a href="https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/customsolution/CustomSolution.md">CustomSolution.md</a>
 */
public class CustomSolutionWithAdditionalNodesAndResourcesExample extends Optimization {

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
    new CustomSolutionWithAdditionalNodesAndResourcesExample().example();
  }

  @Override
  public String toString() {
    return "Starts the optimization from a custom solution and adds more nodes and resources, "
        + "which the optimizer puts into the solution before the search.";
  }

  /**
   * Builds the custom solution, sets it as the initial entity, adds the further nodes and
   * resources, runs the optimization and prints the result.
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
    addInitialSolution(this);
    addReassignElements(this);
    attachToObservables(this);
    startAndPresentResult(this);
  }

  /**
   * Adds the properties that keep the run short and switch off the construction.
   *
   * @param opti the optimization
   */
  private static void addProperties(IOptimization opti) {
    Properties props = new Properties();
    // Fewer generations and iterations than the defaults keep the run short.
    props.setProperty("JOptExitCondition.JOptGenerationCount", "1000");
    props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumIterations", "100000");
    // No construction of a start solution: the custom solution is the start. A custom solution
    // skips the construction anyway; FALSE skips it also when no solution is set.
    props.setProperty("JOpt.Assisted", "FALSE");
    opti.addElement(props);
  }

  /**
   * Builds the two routes of the custom solution and sets them as the initial entity.
   *
   * @param opti the optimization
   */
  private static void addInitialSolution(IOptimization opti) {
    IEntity initialEntity = createInitialSolution();
    opti.setInitialEntity(initialEntity);
  }

  /**
   * Adds the further resources and nodes, which the optimizer puts into the custom solution at
   * the start of the run.
   *
   * @param opti the optimization
   */
  private static void addReassignElements(IOptimization opti) {
    opti.addReassignResources(getAdditionalResources());
    opti.addReassignNodes(getAdditionalNodes());
  }

  /**
   * Creates an entity with two routes of Jack, one per working day, each with three nodes.
   *
   * @return the entity
   */
  private static IEntity createInitialSolution() {

    ILogicEntityRoute firstRoute = new LogicEntityRoute();
    ILogicEntityRoute secondRoute = new LogicEntityRoute();

    IResource myRes = getResource();

    // Each route gets the resource with the index of a working day, the start and the end, the
    // nodes in their order and the index as route id.
    firstRoute.setCurrentVisitingResource(myRes, 0);
    firstRoute.setRouteStart(myRes);
    firstRoute.setRouteTermination(myRes);
    firstRoute.addAllToOptimizableElements(getNodeSet1());
    firstRoute.setRouteId(0);

    secondRoute.setCurrentVisitingResource(myRes, 1);
    secondRoute.setRouteStart(myRes);
    secondRoute.setRouteTermination(myRes);
    secondRoute.addAllToOptimizableElements(getNodeSet2());
    secondRoute.setRouteId(1);

    IEntity entity = new Entity();
    entity.addRoute(firstRoute);
    entity.addRoute(secondRoute);

    return entity;
  }

  /**
   * Creates Jack, who starts and ends both working days in Aachen.
   *
   * @return the resource
   */
  private static IResource getResource() {

    List<IWorkingHours> workingHours = new ArrayList<>();
    workingHours.add(
        new WorkingHours(
            ZonedDateTime.of(2020, MAY.getValue(), 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 6, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    workingHours.add(
        new WorkingHours(
            ZonedDateTime.of(2020, MAY.getValue(), 7, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 7, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    // 13 h per day: the long day with Nuernberg, Heilbronn and Stuttgart fits into it.
    Duration maxWorkingTime = Duration.ofHours(13);
    // 1200 km per day, more than the longest day of this plan needs.
    Quantity<Length> maxDistanceKmW = Quantities.getQuantity(1200.0, KILO(METRE));

    IResource jack =
        new CapacityResource(
            "Jack", 50.775346, 6.083887, maxWorkingTime, maxDistanceKmW, workingHours);
    return jack;
  }

  /**
   * Creates the nodes of the first route: Koeln, Dueren and Essen.
   *
   * @return the nodes in the order of the route
   */
  private static List<IOptimizationElement> getNodeSet1() {
    List<IOptimizationElement> myNodes1 = new ArrayList<>();

    List<IOpeningHours> weeklyOpeningHours = new ArrayList<>();
    weeklyOpeningHours.add(
        new OpeningHours(
            ZonedDateTime.of(2020, MAY.getValue(), 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 6, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    weeklyOpeningHours.add(
        new OpeningHours(
            ZonedDateTime.of(2020, MAY.getValue(), 7, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 7, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    // Each visit takes 20 minutes and counts toward the working time, like the driving.
    Duration visitDuration = Duration.ofMinutes(20);

    TimeWindowGeoNode koeln =
        new TimeWindowGeoNode("Koeln", 50.9333, 6.95, weeklyOpeningHours, visitDuration, 1);
    myNodes1.add(koeln);

    TimeWindowGeoNode dueren =
        new TimeWindowGeoNode("Dueren", 50.8, 6.48333, weeklyOpeningHours, visitDuration, 1);
    myNodes1.add(dueren);

    TimeWindowGeoNode essen =
        new TimeWindowGeoNode("Essen", 51.45, 7.01667, weeklyOpeningHours, visitDuration, 1);
    myNodes1.add(essen);

    return myNodes1;
  }

  /**
   * Creates the nodes of the second route: Nuernberg, Heilbronn and Stuttgart.
   *
   * @return the nodes in the order of the route
   */
  private static List<IOptimizationElement> getNodeSet2() {
    List<IOptimizationElement> myNodes2 = new ArrayList<>();

    List<IOpeningHours> weeklyOpeningHours = new ArrayList<>();
    weeklyOpeningHours.add(
        new OpeningHours(
            ZonedDateTime.of(2020, MAY.getValue(), 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 6, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    weeklyOpeningHours.add(
        new OpeningHours(
            ZonedDateTime.of(2020, MAY.getValue(), 7, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 7, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    // Each visit takes 20 minutes and counts toward the working time, like the driving.
    Duration visitDuration = Duration.ofMinutes(20);

    TimeWindowGeoNode nuernberg =
        new TimeWindowGeoNode("Nuernberg", 49.4478, 11.0683, weeklyOpeningHours, visitDuration, 1);
    myNodes2.add(nuernberg);

    TimeWindowGeoNode heilbronn =
        new TimeWindowGeoNode("Heilbronn", 49.1403, 9.22, weeklyOpeningHours, visitDuration, 1);
    myNodes2.add(heilbronn);

    TimeWindowGeoNode stuttgart =
        new TimeWindowGeoNode("Stuttgart", 48.7667, 9.18333, weeklyOpeningHours, visitDuration, 1);
    myNodes2.add(stuttgart);

    return myNodes2;
  }

  /**
   * Creates the further nodes: Koeln2, Dueren2 and Essen2, open on May 10 and May 11, the pillar
   * testPillarKoeln, and a second node with the id Essen.
   *
   * @return the nodes
   */
  private static List<INode> getAdditionalNodes() {
    List<INode> additionalNodes = new ArrayList<>();

    List<IOpeningHours> weeklyOpeningHours = new ArrayList<>();
    weeklyOpeningHours.add(
        new OpeningHours(
            ZonedDateTime.of(2020, MAY.getValue(), 10, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 10, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    weeklyOpeningHours.add(
        new OpeningHours(
            ZonedDateTime.of(2020, MAY.getValue(), 11, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 11, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    // Each visit takes 20 minutes and counts toward the working time, like the driving.
    Duration visitDuration = Duration.ofMinutes(20);

    TimeWindowGeoNode koeln =
        new TimeWindowGeoNode("Koeln2", 50.9333, 6.95, weeklyOpeningHours, visitDuration, 1);
    additionalNodes.add(koeln);

    TimeWindowGeoNode dueren =
        new TimeWindowGeoNode("Dueren2", 50.8, 6.48333, weeklyOpeningHours, visitDuration, 1);
    additionalNodes.add(dueren);

    TimeWindowGeoNode essen =
        new TimeWindowGeoNode("Essen2", 51.45, 7.01667, weeklyOpeningHours, visitDuration, 1);
    additionalNodes.add(essen);

    // A pillar on May 11 from 09:00 to 10:00; only JackNew works on that day.
    OpeningHours pillarOpening =
        new OpeningHours(
            ZonedDateTime.of(2020, MAY.getValue(), 11, 9, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 11, 10, 0, 0, 0, ZoneId.of("Europe/Berlin")));

    IPillarNode pillarNode =
        new PillarTimeWindowGeoNode("testPillarKoeln", 50.9333, 6.95, pillarOpening);
    additionalNodes.add(pillarNode);

    // A node with the id of a node of the custom solution, on purpose: the duplicate id is
    // reported with a warning.
    TimeWindowGeoNode essenDuplicate =
        new TimeWindowGeoNode("Essen", 51.45, 7.01667, weeklyOpeningHours, visitDuration, 1);
    additionalNodes.add(essenDuplicate);

    return additionalNodes;
  }

  /**
   * Creates the further resources: JackNew on May 10 and May 11, and a second resource with the id
   * Jack.
   *
   * @return the resources
   */
  private static List<IResource> getAdditionalResources() {

    List<IResource> ress = new ArrayList<>();

    List<IWorkingHours> workingHours = new ArrayList<>();
    workingHours.add(
        new WorkingHours(
            ZonedDateTime.of(2020, MAY.getValue(), 10, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 10, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    workingHours.add(
        new WorkingHours(
            ZonedDateTime.of(2020, MAY.getValue(), 11, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 11, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    // 13 h per day, as for Jack.
    Duration maxWorkingTime = Duration.ofHours(13);
    // 1200 km per day, more than the longest day of this plan needs.
    Quantity<Length> maxDistanceKmW = Quantities.getQuantity(1200.0, KILO(METRE));

    IResource res1 =
        new CapacityResource(
            "JackNew", 50.775346, 6.083887, maxWorkingTime, maxDistanceKmW, workingHours);

    // A resource with the id of the resource of the custom solution, on purpose: the duplicate id
    // is reported with a warning.
    IResource res2 =
        new CapacityResource(
            "Jack", 50.775346, 6.083887, maxWorkingTime, maxDistanceKmW, workingHours);

    ress.add(res1);
    ress.add(res2);

    return ress;
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
   * Starts the run, waits for the result, prints it and writes it to a KML file.
   *
   * @param opti the optimization
   * @throws InterruptedException if the wait for the result is interrupted
   * @throws ExecutionException if the optimization run fails
   * @throws TimeoutException if the run does not finish within the wait
   * @throws InvalidLicenceException if the license is not valid
   * @throws IOException if a file cannot be read or written
   */
  private static void startAndPresentResult(IOptimization opti)
      throws InterruptedException, ExecutionException, TimeoutException, InvalidLicenceException,
      IOException {
    CompletableFuture<IOptimizationResult> resultFuture = opti.startRunAsync();
    // Wait for the result; without the wait the JVM would end before the run does.
    IOptimizationResult result = resultFuture.get(5, TimeUnit.MINUTES);
    System.out.println(result);
    exportToKml(result);
  }

  /**
   * Writes the result to a KML file, for a look at the routes on a map.
   *
   * @param result the result
   * @throws IOException if a file cannot be read or written
   */
  private static void exportToKml(IOptimizationResult result) throws IOException {
    String kmlFile = "CustomSolutionWithAdditionalNodesAndResourcesExample.kml";

    EntityKMLExporter kmlExporter = new EntityKMLExporter();
    kmlExporter.setTitle("CustomSolutionWithAdditionalNodesAndResourcesExample");

    // export flushes the stream but does not close it; the try block closes it.
    try (OutputStream out = new FileOutputStream(kmlFile)) {
      if (!kmlExporter.export(result, out)) {
        System.out.println("KML export failed: " + kmlFile);
      }
    }
  }
}
