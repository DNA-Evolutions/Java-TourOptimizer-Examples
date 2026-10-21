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
import java.util.Optional;
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
import com.dna.jopt.member.unit.condition.workinghour.zone.ukpostcode.UKPostCode;
import com.dna.jopt.member.unit.condition.workinghour.zone.ukpostcode.UKPostCodeConstraint;
import com.dna.jopt.member.unit.condition.workinghour.zone.ukpostcode.UKPostCodeQualification;
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
 * Restricts each working day of a resource to a set of UK postcode districts.
 *
 * <p>Scenario: Jack starts in the centre of Birmingham and works from 08:00 to 20:00 on May 6 and
 * May 7, 2020 (Europe/London). Nine nodes lie in the postcode districts B31, B36, B37 and B48 of
 * the Birmingham postcode area. On May 6 Jack may visit B37 and B48, on May 7 B36. The
 * constraints are hard.
 *
 * <p>Look for: the route of May 6 holds the nodes of B37 and B48, the route of May 7 the nodes of
 * B36. Northfield-B31 matches no district of Jack's working days and is unassigned.
 *
 * <p>Prerequisites: None: without a license file the example uses the public evaluation license
 * (see ExampleLicenseHelper).
 *
 * @see <a href="https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/zonecode/ZoneCode.md">ZoneCode.md</a>
 * @see <a href="https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/zonecodes">ZoneCodes and territory management</a>
 */
public class UKPostCodeExample extends Optimization {

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
    new UKPostCodeExample().example();
  }

  @Override
  public String toString() {
    return "Restricts each working day of a resource to a set of UK postcode districts.";
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
   * Adds nine nodes around Birmingham, each with the postcode of its district.
   *
   * @param opti the optimization
   */
  private static void addNodes(IOptimization opti) {
    List<IOpeningHours> openingHours = new ArrayList<>();
    openingHours.add(
        new OpeningHours(
            ZonedDateTime.of(2020, MAY.getValue(), 6, 8, 0, 0, 0, ZoneId.of("Europe/London")),
            ZonedDateTime.of(2020, MAY.getValue(), 6, 17, 0, 0, 0, ZoneId.of("Europe/London"))));
    openingHours.add(
        new OpeningHours(
            ZonedDateTime.of(2020, MAY.getValue(), 7, 8, 0, 0, 0, ZoneId.of("Europe/London")),
            ZonedDateTime.of(2020, MAY.getValue(), 7, 17, 0, 0, 0, ZoneId.of("Europe/London"))));

    // Each visit takes 20 minutes and counts toward the working time, like the driving.
    Duration visitDuration = Duration.ofMinutes(20);

    // The same importance for every node, so no node is preferred over another.
    int importance = 1;

    // Each node lies in the district that its name and its postcode give. The coordinates of each
    // place, and of Birmingham where Jack starts, are those that its article on the English
    // Wikipedia gives. The districts are those that the Wikipedia article "B postcode area"
    // (https://en.wikipedia.org/wiki/B_postcode_area) lists, or that the article of the place
    // gives. The sector and the unit of each postcode are sample values: the constraints of
    // Jack's working days name only the area and the district.

    // B31 is in no constraint of Jack's working days, so the hard constraints leave this node
    // unassigned.
    INode northfield =
        new TimeWindowGeoNode(
            "Northfield-B31", 52.408, -1.963, openingHours, visitDuration, importance);
    northfield.addQualification(
        new UKPostCodeQualification(
            new UKPostCode("B", Optional.of(31), Optional.of(2), Optional.of("G"))));
    opti.addElement(northfield);

    INode chelmsleyWood =
        new TimeWindowGeoNode(
            "ChelmsleyWood-B37", 52.47612, -1.72906, openingHours, visitDuration, importance);
    chelmsleyWood.addQualification(
        new UKPostCodeQualification(
            new UKPostCode("B", Optional.of(37), Optional.of(2), Optional.of("G"))));
    opti.addElement(chelmsleyWood);

    INode alvechurch =
        new TimeWindowGeoNode(
            "Alvechurch-B48", 52.35056, -1.96472, openingHours, visitDuration, importance);
    alvechurch.addQualification(
        new UKPostCodeQualification(
            new UKPostCode("B", Optional.of(48), Optional.of(2), Optional.of("G"))));
    opti.addElement(alvechurch);

    INode castleBromwich =
        new TimeWindowGeoNode(
            "CastleBromwich-B36", 52.5050, -1.7856, openingHours, visitDuration, importance);
    castleBromwich.addQualification(
        new UKPostCodeQualification(
            new UKPostCode("B", Optional.of(36), Optional.of(2), Optional.of("G"))));
    opti.addElement(castleBromwich);

    INode hodgeHill =
        new TimeWindowGeoNode(
            "HodgeHill-B36", 52.497, -1.819, openingHours, visitDuration, importance);
    hodgeHill.addQualification(
        new UKPostCodeQualification(
            new UKPostCode("B", Optional.of(36), Optional.of(2), Optional.of("H"))));
    opti.addElement(hodgeHill);

    INode hopwood =
        new TimeWindowGeoNode(
            "Hopwood-B48", 52.37214, -1.95736, openingHours, visitDuration, importance);
    hopwood.addQualification(
        new UKPostCodeQualification(
            new UKPostCode("B", Optional.of(48), Optional.of(2), Optional.of("H"))));
    opti.addElement(hopwood);

    INode smithsWood =
        new TimeWindowGeoNode(
            "SmithsWood-B36", 52.500, -1.747, openingHours, visitDuration, importance);
    smithsWood.addQualification(
        new UKPostCodeQualification(
            new UKPostCode("B", Optional.of(36), Optional.of(2), Optional.of("F"))));
    opti.addElement(smithsWood);

    INode marstonGreen =
        new TimeWindowGeoNode(
            "MarstonGreen-B37", 52.465, -1.740, openingHours, visitDuration, importance);
    marstonGreen.addQualification(
        new UKPostCodeQualification(
            new UKPostCode("B", Optional.of(37), Optional.of(2), Optional.of("F"))));
    opti.addElement(marstonGreen);

    INode rowneyGreen =
        new TimeWindowGeoNode(
            "RowneyGreen-B48", 52.344097, -1.937284, openingHours, visitDuration, importance);
    rowneyGreen.addQualification(
        new UKPostCodeQualification(
            new UKPostCode("B", Optional.of(48), Optional.of(2), Optional.of("F"))));
    opti.addElement(rowneyGreen);
  }

  /**
   * Adds Jack, who may visit the districts B37 and B48 on May 6 and B36 on May 7.
   *
   * @param opti the optimization
   */
  private static void addResources(IOptimization opti) {
    IWorkingHours woh1 =
        new WorkingHours(
            ZonedDateTime.of(2020, MAY.getValue(), 6, 8, 0, 0, 0, ZoneId.of("Europe/London")),
            ZonedDateTime.of(2020, MAY.getValue(), 6, 20, 0, 0, 0, ZoneId.of("Europe/London")));
    IWorkingHours woh2 =
        new WorkingHours(
            ZonedDateTime.of(2020, MAY.getValue(), 7, 8, 0, 0, 0, ZoneId.of("Europe/London")),
            ZonedDateTime.of(2020, MAY.getValue(), 7, 20, 0, 0, 0, ZoneId.of("Europe/London")));

    // A UKPostCode takes the area, the district, the sector and the unit. A part that the code of
    // a constraint leaves empty is not compared, so B37 matches every postcode of the district
    // B37. The constraints are hard: on May 6 Jack visits only nodes in B37 or B48, on May 7
    // only nodes in B36.
    UKPostCodeConstraint postCodeConstraintWoh1 = new UKPostCodeConstraint();
    postCodeConstraintWoh1.setIsHard(true);
    postCodeConstraintWoh1.addZoneCode(
        new UKPostCode("B", Optional.of(37), Optional.empty(), Optional.empty()));
    postCodeConstraintWoh1.addZoneCode(
        new UKPostCode("B", Optional.of(48), Optional.empty(), Optional.empty()));
    woh1.addConstraint(postCodeConstraintWoh1);

    UKPostCodeConstraint postCodeConstraintWoh2 = new UKPostCodeConstraint();
    postCodeConstraintWoh2.setIsHard(true);
    postCodeConstraintWoh2.addZoneCode(
        new UKPostCode("B", Optional.of(36), Optional.empty(), Optional.empty()));
    woh2.addConstraint(postCodeConstraintWoh2);

    List<IWorkingHours> workingHours = new ArrayList<>();
    workingHours.add(woh1);
    workingHours.add(woh2);

    // 13 h per day, a sample value above what a day of this plan needs.
    Duration maxWorkingTime = Duration.ofHours(13);
    // 1200 km per day, more than the longest day of this plan needs.
    Quantity<Length> maxDistanceKmW = Quantities.getQuantity(1200.0, KILO(METRE));

    // Jack starts and ends each working day in the centre of Birmingham; addNodes names the
    // source of the coordinates.
    IResource jack =
        new CapacityResource(
            "Jack", 52.48, -1.9025, maxWorkingTime, maxDistanceKmW, workingHours);
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
