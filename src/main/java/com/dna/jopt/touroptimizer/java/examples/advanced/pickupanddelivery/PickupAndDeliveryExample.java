package com.dna.jopt.touroptimizer.java.examples.advanced.pickupanddelivery;
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
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import javax.measure.Quantity;
import javax.measure.quantity.Length;

import com.dna.jopt.framework.body.Optimization;
import com.dna.jopt.framework.exception.caught.InvalidLicenceException;
import com.dna.jopt.framework.outcomewrapper.IOptimizationProgress;
import com.dna.jopt.framework.outcomewrapper.IOptimizationResult;
import com.dna.jopt.member.unit.hours.IOpeningHours;
import com.dna.jopt.member.unit.hours.IWorkingHours;
import com.dna.jopt.member.unit.hours.OpeningHours;
import com.dna.jopt.member.unit.hours.WorkingHours;
import com.dna.jopt.member.unit.node.INode;
import com.dna.jopt.member.unit.node.geo.TimeWindowGeoNode;
import com.dna.jopt.member.unit.resource.CapacityResource;
import com.dna.jopt.member.unit.resource.IResource;
import com.dna.jopt.touroptimizer.java.examples.ExampleLicenseHelper;

import tec.units.ri.quantity.Quantities;

import java.util.List;

import static java.time.Month.MARCH;
import static tec.units.ri.unit.MetricPrefix.KILO;
import static tec.units.ri.unit.Units.METRE;

import java.io.IOException;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;

/**
 * The Class PickupAndDeliveryExample.
 *
 * @author Jens Richter
 * @version Jan 16, 2019
 * @since Jan 16, 2019
 *     <p>Example of pick up and delivery optimization problem.
 */
public class PickupAndDeliveryExample extends Optimization {

  public static void main(String[] args)
      throws InvalidLicenceException, IOException, InterruptedException, ExecutionException {
    new PickupAndDeliveryExample().example();
  }

  public String toString() {
    return "Example of pick up and delivery optimization problem.";
  }

  public void example()
      throws InvalidLicenceException, IOException, InterruptedException, ExecutionException {

    // Set license via helper
    ExampleLicenseHelper.setLicense(this);

    this.setProperties();
    this.addNodes();
    this.addRes();

    // 3.) start the optimization
    CompletableFuture<IOptimizationResult> resultFuture = this.startRunAsync();

    // It is important to block the call, otherwise optimization will be terminated
    resultFuture.get();
  }

  public void setProperties() {

    Properties props = new Properties();
    props.setProperty("JOptExitCondition.JOptGenerationCount", "2000");
    props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumIterations", "100000");

    // We have to tell the optimizer that we have an high interest in capacity planning, Default is
    // 100
    props.setProperty("JOptWeight.Capacity", "200");
    this.addElement(props);
  }

  public void addRes() {

    List<IWorkingHours> workingHours = new ArrayList<>();
    workingHours.add(
        new WorkingHours(
            ZonedDateTime.of(2020, MARCH.getValue(), 11, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MARCH.getValue(), 11, 20, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    workingHours.add(
        new WorkingHours(
            ZonedDateTime.of(2020, MARCH.getValue(), 12, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MARCH.getValue(), 12, 20, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    workingHours.add(
        new WorkingHours(
            ZonedDateTime.of(2020, MARCH.getValue(), 13, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MARCH.getValue(), 13, 20, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    workingHours.add(
        new WorkingHours(
            ZonedDateTime.of(2020, MARCH.getValue(), 14, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MARCH.getValue(), 14, 20, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    workingHours.add(
        new WorkingHours(
            ZonedDateTime.of(2020, MARCH.getValue(), 15, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MARCH.getValue(), 15, 20, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    Duration maxWorkingTime = Duration.ofHours(10);

    Quantity<Length> maxDistanceKmW = Quantities.getQuantity(2800.0, KILO(METRE));

    IResource truck1 =
        new CapacityResource(
            "truck1", 50.1167, 7.68333, maxWorkingTime, maxDistanceKmW, workingHours);

    double[] truck1InitialLoad = {0};
    truck1.setInitialLoad(truck1InitialLoad);

    // Add the capacity

    double truck1Capacity = 3;
    truck1.addCapacity(truck1Capacity);

    this.addElement(truck1);
  }

  public void addNodes() {

    // We request to deliver items to the nodes (plus sign)
    double[] unitsPerTrip = {1};

    List<IOpeningHours> weeklyOpeningHours = new ArrayList<>();
    weeklyOpeningHours.add(
        new OpeningHours(
            ZonedDateTime.of(2020, MARCH.getValue(), 11, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MARCH.getValue(), 11, 18, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    weeklyOpeningHours.add(
        new OpeningHours(
            ZonedDateTime.of(2020, MARCH.getValue(), 12, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MARCH.getValue(), 12, 18, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    weeklyOpeningHours.add(
        new OpeningHours(
            ZonedDateTime.of(2020, MARCH.getValue(), 13, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MARCH.getValue(), 13, 18, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    weeklyOpeningHours.add(
        new OpeningHours(
            ZonedDateTime.of(2020, MARCH.getValue(), 14, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MARCH.getValue(), 14, 18, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    weeklyOpeningHours.add(
        new OpeningHours(
            ZonedDateTime.of(2020, MARCH.getValue(), 15, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MARCH.getValue(), 15, 18, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    Duration visitDuration = Duration.ofMinutes(20);

    INode job1 = new TimeWindowGeoNode("job1", 50.9333, 6.95, weeklyOpeningHours, visitDuration, 1);
    job1.setLoad(unitsPerTrip);
    this.addElement(job1);

    INode job2 = new TimeWindowGeoNode("job2", 51.4667, 6.85, weeklyOpeningHours, visitDuration, 1);
    job2.setLoad(unitsPerTrip);
    this.addElement(job2);

    INode job3 =
        new TimeWindowGeoNode("job3", 51.45, 7.01667, weeklyOpeningHours, visitDuration, 1);
    job3.setLoad(unitsPerTrip);
    this.addElement(job3);

    INode job4 = new TimeWindowGeoNode("job4", 50.8, 6.48333, weeklyOpeningHours, visitDuration, 1);
    job4.setLoad(unitsPerTrip);
    this.addElement(job4);

    INode job5 =
        new TimeWindowGeoNode("job5", 49.4883, 8.46472, weeklyOpeningHours, visitDuration, 1);
    job5.setLoad(unitsPerTrip);
    this.addElement(job5);

    INode job6 =
        new TimeWindowGeoNode("job6", 48.15, 11.5833, weeklyOpeningHours, visitDuration, 1);
    job6.setLoad(unitsPerTrip);
    this.addElement(job6);

    INode job7 =
        new TimeWindowGeoNode("job7", 49.4478, 11.0683, weeklyOpeningHours, visitDuration, 1);
    job7.setLoad(unitsPerTrip);
    this.addElement(job7);

    INode job8 = new TimeWindowGeoNode("job8", 49.1403, 9.22, weeklyOpeningHours, visitDuration, 1);
    job8.setLoad(unitsPerTrip);
    this.addElement(job8);

    INode job9 =
        new TimeWindowGeoNode("job9", 48.7667, 9.18333, weeklyOpeningHours, visitDuration, 1);
    job9.setLoad(unitsPerTrip);
    this.addElement(job9);
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
