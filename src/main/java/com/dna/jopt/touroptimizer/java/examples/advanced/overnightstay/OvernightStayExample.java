package com.dna.jopt.touroptimizer.java.examples.advanced.overnightstay;
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
import com.dna.jopt.member.unit.resource.CapacityResource;
import com.dna.jopt.touroptimizer.java.examples.ExampleLicenseHelper;

/**
 * Plans a tour over several days in which the resource ends a working day at a stay node and
 * starts the next working day there, instead of returning home.
 *
 * <p>Scenario: Jack starts in Aachen and works from 08:00 to 20:00 on six days, May 6 to May 11,
 * 2020 (Europe/Berlin). Each node is open on one of these days: Wien on May 6, Zagreb on May 7,
 * Innsbruck and the optional node VenedigOptional on May 8, Stuttgart on May 9. All five nodes are
 * stay nodes, and every working day allows a stay.
 *
 * <p>Look for: the routes of the result. A route whose Termination Id is a node ends the day at
 * that node, and the next route starts there (Start Id). The last route of the plan ends at Jack's
 * home.
 *
 * <p>Prerequisites: None: without a license file the example uses the public evaluation license
 * (see ExampleLicenseHelper).
 *
 * @see <a href="https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/overnightstay/OvernightStay.md">OvernightStay.md</a>
 */
public class OvernightStayExample extends Optimization {

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
    new OvernightStayExample().example();
  }

  @Override
  public String toString() {
    return "Plans a tour over several days in which the resource ends a working day at a stay node"
        + " and starts the next working day there, instead of returning home.";
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
   * Adds five stay nodes in Austria, Croatia, Germany and Italy, one of them optional.
   *
   * @param opti the optimization
   */
  private static void addNodes(IOptimization opti) {
    // Each visit takes one hour and counts toward the working time, like the driving.
    Duration visitDuration = Duration.ofHours(1);

    // The same importance for every node, so no node is preferred over another.
    int importance = 1;

    TimeWindowGeoNode zagreb =
        new TimeWindowGeoNode(
            "Zagreb", 45.815011, 15.981919, createOpeningHours(7), visitDuration, importance);
    // A stay node: a working day may end here, and the next working day starts here.
    zagreb.setIsStayNode(true);
    opti.addElement(zagreb);

    TimeWindowGeoNode venedigOptional =
        new TimeWindowGeoNode(
            "VenedigOptional",
            45.440847,
            12.315515,
            createOpeningHours(8),
            visitDuration,
            importance);
    // Optional: the optimizer may leave the node out of the plan.
    venedigOptional.setIsOptional(true);
    venedigOptional.setIsStayNode(true);
    opti.addElement(venedigOptional);

    TimeWindowGeoNode innsbruck =
        new TimeWindowGeoNode(
            "Innsbruck", 47.269212, 11.404102, createOpeningHours(8), visitDuration, importance);
    innsbruck.setIsStayNode(true);
    opti.addElement(innsbruck);

    TimeWindowGeoNode wien =
        new TimeWindowGeoNode(
            "Wien", 48.208174, 16.373819, createOpeningHours(6), visitDuration, importance);
    wien.setIsStayNode(true);
    opti.addElement(wien);

    TimeWindowGeoNode stuttgart =
        new TimeWindowGeoNode(
            "Stuttgart", 48.775846, 9.182932, createOpeningHours(9), visitDuration, importance);
    stuttgart.setIsStayNode(true);
    opti.addElement(stuttgart);
  }

  /**
   * Adds Jack, who starts in Aachen and may end a working day at a stay node.
   *
   * @param opti the optimization
   */
  private static void addResources(IOptimization opti) {
    List<IWorkingHours> workingHours = createWorkingHours();
    // Every working day may end at a stay node.
    workingHours.forEach(w -> w.setIsAvailableForStay(true));

    // 12 h per day, the length of the working hours. The drive from Aachen to Wien and back alone
    // takes longer, so the tour needs stays.
    Duration maxWorkingTime = Duration.ofHours(12);
    // 1200 km per day, more than the longest day of this plan needs.
    Quantity<Length> maxDistanceKmW = Quantities.getQuantity(1200.0, KILO(METRE));

    CapacityResource jack =
        new CapacityResource(
            "Jack", 50.775346, 6.083887, maxWorkingTime, maxDistanceKmW, workingHours);

    // The stay-out policy, with sample thresholds: a stay at a node is allowed only if the way
    // back from the node to Aachen is at least 100 km long or takes at least 4 hours. Each
    // threshold counts only when its flag is set.
    Quantity<Length> minDistanceForStayOut = Quantities.getQuantity(100, KILO(METRE));
    Duration minTimeForStayOut = Duration.ofHours(4);
    jack.setStayOutPolicy(minDistanceForStayOut, minTimeForStayOut);
    jack.setStayOutPolicyReturnDistanceActive(true);
    jack.setStayOutPolicyReturnTimeActive(true);

    // Sample limits: -1 sets no limit on the total number of stays, at most 4 stays in a row, and
    // at least 2 working days that end at home before the next stay.
    int totalStaysOut = -1;
    int staysOutInRow = 4;
    int minRecoverHours = 2;
    jack.setStaysOut(totalStaysOut, staysOutInRow, minRecoverHours);

    opti.addElement(jack);
  }

  /**
   * Creates the opening hours of a node: one day in May 2020, from 08:00 to 20:00.
   *
   * @param day the day in May 2020
   * @return the opening hours
   */
  private static List<IOpeningHours> createOpeningHours(int day) {
    // One day per node fixes the order of the tour; the optimizer decides where each working day
    // ends.
    List<IOpeningHours> openingHours = new ArrayList<>();
    openingHours.add(
        new OpeningHours(
            ZonedDateTime.of(2020, MAY.getValue(), day, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), day, 20, 0, 0, 0, ZoneId.of("Europe/Berlin"))));
    return openingHours;
  }

  /**
   * Creates Jack's working hours: May 6 to May 11, 2020, from 08:00 to 20:00.
   *
   * @return the working hours
   */
  private static List<IWorkingHours> createWorkingHours() {
    List<IWorkingHours> workingHours = new ArrayList<>();
    for (int day = 6; day <= 11; day++) {
      workingHours.add(
          new WorkingHours(
              ZonedDateTime.of(2020, MAY.getValue(), day, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
              ZonedDateTime.of(
                  2020, MAY.getValue(), day, 20, 0, 0, 0, ZoneId.of("Europe/Berlin"))));
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
