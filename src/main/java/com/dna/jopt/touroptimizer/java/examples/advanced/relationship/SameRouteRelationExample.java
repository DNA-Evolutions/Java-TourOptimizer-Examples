package com.dna.jopt.touroptimizer.java.examples.advanced.relationship;
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

import com.dna.jopt.framework.body.IOptimization;
import com.dna.jopt.framework.body.Optimization;
import com.dna.jopt.framework.exception.caught.InvalidLicenceException;
import com.dna.jopt.framework.outcomewrapper.IOptimizationResult;
import com.dna.jopt.member.unit.hours.IOpeningHours;
import com.dna.jopt.member.unit.hours.IWorkingHours;
import com.dna.jopt.member.unit.hours.OpeningHours;
import com.dna.jopt.member.unit.hours.WorkingHours;
import com.dna.jopt.member.unit.node.geo.TimeWindowGeoNode;
import com.dna.jopt.member.unit.relation.node2node.visitor.INode2NodeVisitorRelation;
import com.dna.jopt.member.unit.relation.node2node.visitor.RelativeVisitor2RelatedNodeRelation;
import com.dna.jopt.member.unit.resource.CapacityResource;
import com.dna.jopt.touroptimizer.java.examples.ExampleLicenseHelper;

/**
 * Asks with a relation that two nodes, Essen and Aachen, are on the same route.
 *
 * <p>Scenario: Jack starts in Aachen and works from 08:00 to 16:00 on May 6, May 7 and May 8, 2020
 * (Europe/Berlin). He visits Koeln, Essen, Dueren, Wuppertal and Aachen; each visit takes 150
 * minutes, so a working day holds at most two visits.
 *
 * <p>Look for: Essen and Aachen on the same route of the result.
 *
 * <p>Prerequisites: None: without a license file the example uses the public evaluation license
 * (see ExampleLicenseHelper).
 *
 * @see <a href="https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/relationship/Relations.md">Relations.md</a>
 */
public class SameRouteRelationExample extends Optimization {

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
    new SameRouteRelationExample().example();
  }

  @Override
  public String toString() {
    return "Asks with a relation that two nodes, Essen and Aachen, are on the same route.";
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
    addProperties(this);
    addNodes(this);
    addResources(this);
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
   * Adds five nodes and the relation that puts Essen and Aachen on the same route.
   *
   * @param opti the optimization
   */
  private static void addNodes(IOptimization opti) {
    // Each visit takes 150 minutes and counts toward the working time, like the driving.
    Duration visitDuration = Duration.ofMinutes(150);

    // The same importance for every node, so no node is preferred over another.
    int importance = 1;

    TimeWindowGeoNode koeln =
        new TimeWindowGeoNode(
            "Koeln", 50.9333, 6.95, createOpeningHours(), visitDuration, importance);
    opti.addElement(koeln);

    TimeWindowGeoNode essen =
        new TimeWindowGeoNode(
            "Essen", 51.45, 7.01667, createOpeningHours(), visitDuration, importance);
    opti.addElement(essen);

    TimeWindowGeoNode dueren =
        new TimeWindowGeoNode(
            "Dueren", 50.8, 6.48333, createOpeningHours(), visitDuration, importance);
    opti.addElement(dueren);

    TimeWindowGeoNode wuppertal =
        new TimeWindowGeoNode(
            "Wuppertal", 51.2667, 7.18333, createOpeningHours(), visitDuration, importance);
    opti.addElement(wuppertal);

    TimeWindowGeoNode aachen =
        new TimeWindowGeoNode(
            "Aachen", 50.775346, 6.083887, createOpeningHours(), visitDuration, importance);
    opti.addElement(aachen);

    // Essen is the master node and Aachen the related node. setIsForcedSameRoute asks for the
    // same route, which also means the same resource.
    INode2NodeVisitorRelation sameRoute = new RelativeVisitor2RelatedNodeRelation();
    sameRoute.setMasterNode(essen);
    sameRoute.setRelatedNode(aachen);
    sameRoute.setIsForcedSameRoute();

    essen.addNode2NodeRelation(sameRoute);
    aachen.addNode2NodeRelation(sameRoute);
  }

  /**
   * Adds Jack, who starts and ends each working day in Aachen.
   *
   * @param opti the optimization
   */
  private static void addResources(IOptimization opti) {
    // 8 h per day, the length of the working hours: Aachen and Essen with the drives between them
    // fit into one day.
    Duration maxWorkingTime = Duration.ofHours(8);
    // 1200 km per day, more than the longest day of this plan needs.
    Quantity<Length> maxDistanceKmW = Quantities.getQuantity(1200.0, KILO(METRE));

    CapacityResource jack =
        new CapacityResource(
            "Jack", 50.775346, 6.083887, maxWorkingTime, maxDistanceKmW, createWorkingHours());
    opti.addElement(jack);
  }

  /**
   * Creates the opening hours of a node: May 6 to May 8, 2020, from 08:00 to 18:00.
   *
   * @return the opening hours
   */
  private static List<IOpeningHours> createOpeningHours() {
    List<IOpeningHours> openingHours = new ArrayList<>();
    for (int day = 6; day <= 8; day++) {
      openingHours.add(
          new OpeningHours(
              ZonedDateTime.of(2020, MAY.getValue(), day, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
              ZonedDateTime.of(
                  2020, MAY.getValue(), day, 18, 0, 0, 0, ZoneId.of("Europe/Berlin"))));
    }
    return openingHours;
  }

  /**
   * Creates Jack's working hours: May 6 to May 8, 2020, from 08:00 to 16:00.
   *
   * @return the working hours
   */
  private static List<IWorkingHours> createWorkingHours() {
    List<IWorkingHours> workingHours = new ArrayList<>();
    for (int day = 6; day <= 8; day++) {
      workingHours.add(
          new WorkingHours(
              ZonedDateTime.of(2020, MAY.getValue(), day, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
              ZonedDateTime.of(
                  2020, MAY.getValue(), day, 16, 0, 0, 0, ZoneId.of("Europe/Berlin"))));
    }
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
