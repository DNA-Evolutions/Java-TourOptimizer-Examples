package com.dna.jopt.touroptimizer.java.examples.advanced.overnightstay;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
import com.dna.jopt.member.unit.resource.CapacityResource;
import com.dna.jopt.touroptimizer.java.examples.ExampleLicenseHelper;

import tech.units.indriya.quantity.Quantities;

/**
 * In this example we are enabling over night stays. Instead of going home a Resource may stay at a location (hotel)
 * till the next workingDay. We are also setting a custom policy for the Resource to be able to use overnight stays
 * and are setting a minimum recovery time.In contrary to open Routes or when setting custom last Nodes the Resource is starting
 * the next work day at the location where it ended the previous one when using over night stays.
 *
 *
 * @author DNA
 * @version Mar 23, 2021
 * @since Mar 23, 2021
 */
public class OvernightStayExample extends Optimization {

  public static void main(String[] args) throws IOException, InvalidLicenceException, InterruptedException, ExecutionException, TimeoutException {
    new OvernightStayExample().example();
  }
  
  public String toString() {
	  return "Instead of going home a Resource may stay at a location (hotel) till the next workingDay.";
  }

  public void example() throws IOException, InvalidLicenceException, InterruptedException, ExecutionException, TimeoutException {

    // Set license via helper
    ExampleLicenseHelper.setLicense(this);

    // Set the Properties
    this.setProperties();

    this.addNodes();
    this.addResources();

    this.startRunSync(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
  }

  private void setProperties() {

    Properties props = new Properties();

    props.setProperty("JOptExitCondition.JOptGenerationCount", "2000");
    props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumIterations", "500000");
    props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumRepetions", "1");
    props.setProperty("JOpt.NumCPUCores", "4");
    props.setProperty("JOptWeight.Capacity", "100");

    this.addElement(props);
  }

  private void addResources() {

    List<IWorkingHours> workingHours = new ArrayList<>();

    // We do not want to allow to have a stay out on this day
    IWorkingHours forbiddenStayOutWOH =
        new WorkingHours(
            ZonedDateTime.of(2020, MAY.getValue(), 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 6, 20, 0, 0, 0, ZoneId.of("Europe/Berlin")));

    forbiddenStayOutWOH.setIsAvailableForStay(false); // Default is true
    workingHours.add(forbiddenStayOutWOH);

    workingHours.add(
        new WorkingHours(
            ZonedDateTime.of(2020, MAY.getValue(), 7, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 7, 20, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    workingHours.add(
        new WorkingHours(
            ZonedDateTime.of(2020, MAY.getValue(), 8, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 8, 20, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    workingHours.add(
        new WorkingHours(
            ZonedDateTime.of(2020, MAY.getValue(), 9, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 9, 20, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    workingHours.add(
        new WorkingHours(
            ZonedDateTime.of(2020, MAY.getValue(), 10, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 10, 20, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    workingHours.add(
        new WorkingHours(
            ZonedDateTime.of(2020, MAY.getValue(), 11, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 11, 20, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    Duration maxWorkingTime = Duration.ofHours(12);
    Quantity<Length> maxDistanceKmW = Quantities.getQuantity(1200.0, KILO(METRE));

    CapacityResource rep1 =
        new CapacityResource(
            "Jack", 50.775346, 6.083887, maxWorkingTime, maxDistanceKmW, workingHours);
    rep1.setCost(0, 1, 1);

    /*
     * Policy
     */

    // We want at least 100 kilometers distance or 4 hours of traveling time from the Resource’s starting location
    // to allow an overnight stay
    Quantity<Length> minDistanceForStayOut = Quantities.getQuantity(100, KILO(METRE));
    Duration minTimeForStayOut = Duration.ofHours(4);
    rep1.setStayOutPolicy(minDistanceForStayOut, minTimeForStayOut);
    rep1.setStayOutPolicyReturnDistanceActive(true);

    /*
     * Stay out restrictions
     */

    int totalStaysOut = -1; // We allow as many total stays out as necessary
    int staysOutInRow = 4; // After three stays out in a row the Resource has to return to it’s home location
    int minRecoverHours = 2; // We want the recovery time at home to last at minimum 2 nights in a row
    rep1.setStaysOut(totalStaysOut, staysOutInRow, minRecoverHours);

    this.addElement(rep1);
  }

  public List<IOpeningHours> nodeOpeningHoursHelper(int day) {

    List<IOpeningHours> weeklyOpeningHours = new ArrayList<>();

    weeklyOpeningHours.add(
        new OpeningHours(
            ZonedDateTime.of(2020, MAY.getValue(), 6 + day, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(
                2020, MAY.getValue(), 6 + day, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    weeklyOpeningHours.add(
        new OpeningHours(
            ZonedDateTime.of(2020, MAY.getValue(), 7 + day, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(
                2020, MAY.getValue(), 7 + day, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    weeklyOpeningHours.add(
        new OpeningHours(
            ZonedDateTime.of(2020, MAY.getValue(), 8 + day, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(
                2020, MAY.getValue(), 8 + day, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    return weeklyOpeningHours;
  }

  private void addNodes() {

    boolean stayNodesEnabled = true;

    TimeWindowGeoNode zagreb =
        new TimeWindowGeoNode(
            "Zagreb", 45.815011, 15.981919, this.nodeOpeningHoursHelper(0), Duration.ofHours(1), 1);
    zagreb.setIsStayNode(stayNodesEnabled);
    this.addElement(zagreb);

    TimeWindowGeoNode venedigOptionalNode =
        new TimeWindowGeoNode(
            "VenedigOptional",
            45.440847,
            12.315515,
            this.nodeOpeningHoursHelper(0),
            Duration.ofHours(1),
            1);
    venedigOptionalNode.setIsOptional(true);
    venedigOptionalNode.setIsStayNode(stayNodesEnabled);
    this.addElement(venedigOptionalNode);

    TimeWindowGeoNode innsbruck =
        new TimeWindowGeoNode(
            "Innsbruck",
            47.269212,
            11.404102,
            this.nodeOpeningHoursHelper(1),
            Duration.ofHours(1),
            1);
    innsbruck.setIsStayNode(stayNodesEnabled);
    this.addElement(innsbruck);

    TimeWindowGeoNode wien =
        new TimeWindowGeoNode(
            "Wien", 48.208174, 16.373819, this.nodeOpeningHoursHelper(2), Duration.ofHours(1), 1);
    wien.setIsStayNode(stayNodesEnabled);
    this.addElement(wien);

    TimeWindowGeoNode mannheim =
        new TimeWindowGeoNode(
            "Mannheim",
            48.775846,
            9.182932,
            this.nodeOpeningHoursHelper(2),
            Duration.ofHours(1),
            1);
    mannheim.setIsStayNode(stayNodesEnabled);
    this.addElement(mannheim);
  }

  @Override
  public void onError(int code, String message) {
    System.out.println("code: " + code + " message:" + message);
  }

  @Override
  public void onStatus(int code, String message) {
    System.out.println("code: " + code + " message:" + message);
  }

  @Override
  public void onWarning(int code, String message) {
    //

  }

  @Override
  public void onProgress(String winnerProgressString) {
    System.out.println(winnerProgressString);
  }

  @Override
  public void onProgress(IOptimizationProgress rapoptProgress) {
    //
  }

  @Override
  public void onAsynchronousOptimizationResult(IOptimizationResult rapoptResult) {
    System.out.println(rapoptResult);
  }
}
