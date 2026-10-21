package com.dna.jopt.touroptimizer.java.examples.advanced.zonecode;
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
import com.dna.jopt.member.unit.condition.IQualification;
import com.dna.jopt.member.unit.condition.workinghour.zone.zonenumber.ZoneNumber;
import com.dna.jopt.member.unit.condition.workinghour.zone.zonenumber.ZoneNumberConstraint;
import com.dna.jopt.member.unit.condition.workinghour.zone.zonenumber.ZoneNumberQualification;
import com.dna.jopt.member.unit.hours.IOpeningHours;
import com.dna.jopt.member.unit.hours.IWorkingHours;
import com.dna.jopt.member.unit.hours.OpeningHours;
import com.dna.jopt.member.unit.hours.WorkingHours;
import com.dna.jopt.member.unit.node.INode;
import com.dna.jopt.member.unit.node.geo.TimeWindowGeoNode;
import com.dna.jopt.member.unit.resource.CapacityResource;
import com.dna.jopt.member.unit.resource.IResource;
import com.dna.jopt.touroptimizer.java.examples.ExampleLicenseHelper;

import tech.units.indriya.quantity.Quantities;

/**
 * Restricts each working day of a resource to a set of zone numbers.
 *
 * <p>Scenario: Jack starts in Aachen and works from 08:00 to 20:00 on May 6 and May 7, 2020
 * (Europe/Berlin). Eight nodes in Germany each belong to zone 1, 2 or 3; Oberhausen-Z1-Z2 belongs
 * to zones 1 and 2. On May 6 Jack may visit zones 1 and 2, on May 7 zone 3. The constraints are
 * hard.
 *
 * <p>Look for: the route of May 6 holds only nodes of zones 1 and 2, the route of May 7 only the
 * nodes of zone 3. The line "WOH Constraints" of each route names the zones of its day.
 *
 * <p>Prerequisites: None: without a license file the example uses the public evaluation license
 * (see ExampleLicenseHelper).
 *
 * @see <a href="https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/zonecode/ZoneCode.md">ZoneCode.md</a>
 * @see <a href="https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/zonecodes">ZoneCodes and territory management</a>
 */
public class ZoneNumberConstraintExample extends Optimization {

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
    new ZoneNumberConstraintExample().example();
  }

  @Override
  public String toString() {
    return "Restricts each working day of a resource to a set of zone numbers.";
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
   * Adds the properties that keep the run short, and the zone code weight.
   *
   * @param opti the optimization
   */
  private static void addProperties(IOptimization opti) {
    Properties props = new Properties();
    // Fewer generations and iterations than the defaults keep the run short.
    props.setProperty("JOptExitCondition.JOptGenerationCount", "1000");
    props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumIterations", "100000");
    // A sample weight: it scales the cost of a visit outside the zones of a working day.
    props.setProperty("JOptWeight.ZoneCode", "10.0");
    opti.addElement(props);
  }

  /**
   * Adds eight nodes, each with the zones it belongs to.
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

    // The zone of a node is a ZoneNumberQualification. The zone numbers are sample values, and a
    // zone need not follow the map: Essen and Nuernberg share zone 3.
    IQualification zoneOneQuali = new ZoneNumberQualification(new ZoneNumber(1));
    IQualification zoneTwoQuali = new ZoneNumberQualification(new ZoneNumber(2));
    IQualification zoneThreeQuali = new ZoneNumberQualification(new ZoneNumber(3));

    INode koeln =
        new TimeWindowGeoNode("Koeln-Z1", 50.9333, 6.95, openingHours, visitDuration, importance);
    koeln.addQualification(zoneOneQuali);
    opti.addElement(koeln);

    // A node on the border of two zones gets one qualification with an extra code, so a working
    // day with either zone can include it. A node keeps one qualification per class: a second
    // ZoneNumberQualification would replace the first.
    ZoneNumberQualification borderQuali = new ZoneNumberQualification(new ZoneNumber(1));
    borderQuali.addExtraCode(new ZoneNumber(2));
    INode oberhausen =
        new TimeWindowGeoNode(
            "Oberhausen-Z1-Z2", 51.4667, 6.85, openingHours, visitDuration, importance);
    oberhausen.addQualification(borderQuali);
    opti.addElement(oberhausen);

    INode essen =
        new TimeWindowGeoNode(
            "Essen-Z3", 51.45, 7.01667, openingHours, visitDuration, importance);
    essen.addQualification(zoneThreeQuali);
    opti.addElement(essen);

    INode dueren =
        new TimeWindowGeoNode(
            "Dueren-Z2", 50.8, 6.48333, openingHours, visitDuration, importance);
    dueren.addQualification(zoneTwoQuali);
    opti.addElement(dueren);

    INode nuernberg =
        new TimeWindowGeoNode(
            "Nuernberg-Z3", 49.4478, 11.0683, openingHours, visitDuration, importance);
    nuernberg.addQualification(zoneThreeQuali);
    opti.addElement(nuernberg);

    INode stuttgart =
        new TimeWindowGeoNode(
            "Stuttgart-Z2", 48.7667, 9.18333, openingHours, visitDuration, importance);
    stuttgart.addQualification(zoneTwoQuali);
    opti.addElement(stuttgart);

    INode wuppertal =
        new TimeWindowGeoNode(
            "Wuppertal-Z2", 51.2667, 7.18333, openingHours, visitDuration, importance);
    wuppertal.addQualification(zoneTwoQuali);
    opti.addElement(wuppertal);

    INode aachen =
        new TimeWindowGeoNode(
            "Aachen-Z2", 50.775346, 6.083887, openingHours, visitDuration, importance);
    aachen.addQualification(zoneTwoQuali);
    opti.addElement(aachen);
  }

  /**
   * Adds Jack, who may visit zones 1 and 2 on May 6 and zone 3 on May 7.
   *
   * @param opti the optimization
   */
  private static void addResources(IOptimization opti) {
    IWorkingHours woh1 =
        new WorkingHours(
            ZonedDateTime.of(2020, MAY.getValue(), 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 6, 20, 0, 0, 0, ZoneId.of("Europe/Berlin")));
    IWorkingHours woh2 =
        new WorkingHours(
            ZonedDateTime.of(2020, MAY.getValue(), 7, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 7, 20, 0, 0, 0, ZoneId.of("Europe/Berlin")));

    // The zones of a working day are a ZoneNumberConstraint on its WorkingHours, so they can
    // differ from day to day. The constraints are hard: on May 6 Jack visits only nodes of zone
    // 1 or 2, on May 7 only nodes of zone 3.
    ZoneNumberConstraint zoneNumberConstraintWOHOne = new ZoneNumberConstraint();
    zoneNumberConstraintWOHOne.setIsHard(true);
    zoneNumberConstraintWOHOne.addZoneCode(new ZoneNumber(1));
    zoneNumberConstraintWOHOne.addZoneCode(new ZoneNumber(2));
    woh1.addConstraint(zoneNumberConstraintWOHOne);

    ZoneNumberConstraint zoneNumberConstraintWOHTwo = new ZoneNumberConstraint();
    zoneNumberConstraintWOHTwo.setIsHard(true);
    zoneNumberConstraintWOHTwo.addZoneCode(new ZoneNumber(3));
    woh2.addConstraint(zoneNumberConstraintWOHTwo);

    List<IWorkingHours> workingHours = new ArrayList<>();
    workingHours.add(woh1);
    workingHours.add(woh2);

    // 13 h per day, a sample value above what a day of this plan needs.
    Duration maxWorkingTime = Duration.ofHours(13);
    // 1200 km per day, more than the longest day of this plan needs.
    Quantity<Length> maxDistanceKmW = Quantities.getQuantity(1200.0, KILO(METRE));

    IResource jack =
        new CapacityResource(
            "Jack", 50.775346, 6.083887, maxWorkingTime, maxDistanceKmW, workingHours);
    opti.addElement(jack);
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
