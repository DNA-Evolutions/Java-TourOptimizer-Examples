package com.dna.jopt.touroptimizer.java.examples.advanced.pickupanddelivery.deprecated;
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
 *     <p>Example of pick up and delivery optimization problem with unload all.
 */
public class PickupAndDeliveryUnloadAllExample extends Optimization {

  public static void main(String[] args)
      throws InvalidLicenceException, IOException, InterruptedException, ExecutionException {
    new PickupAndDeliveryUnloadAllExample().example();
  }

  public String toString() {
    return "Example of pick up and delivery optimization problem with unload all.";
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
    props.setProperty("JOptExitCondition.JOptGenerationCount", "2000");
    props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumIterations", "100000");

    // We have to tell the optimizer that we have an high interest in capacity planning, Default is
    // 100
    props.setProperty("JOptWeight.Capacity", "200");
    this.addElement(props);
  }

  public void addRes() {

    // ATTENTION: One route is exactly bound to one accessible working Hour. If we only provide one
    // workingHour the optimizer
    //            is forced to put all nodes in one route! WorkingHours that are not used are simply
    // later on neglected.

    List<IWorkingHours> workingHours = new ArrayList<>();
    workingHours.add(
        new WorkingHours(
            ZonedDateTime.of(2020, MARCH.getValue(), 11, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MARCH.getValue(), 11, 20, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    Duration maxWorkingTime = Duration.ofHours(10);

    Quantity<Length> maxDistanceKmW = Quantities.getQuantity(2800.0, KILO(METRE));

    IResource truck1 =
        new CapacityResource(
            "truck1", 50.1167, 7.68333, maxWorkingTime, maxDistanceKmW, workingHours);

    double[] truck1InitialLoad = {1, 1};
    truck1.setInitialLoad(truck1InitialLoad);

    //  Add the capacity
    truck1.setCapacity(new double[] {1,1});

    // As we set truck1InitialLoad to be 1,1 and the capacity to be 1,1, the Truck starts fully
    // loaded

    this.addElement(truck1);
  }

  public void addNodes() {

    // We request to pick up load from the nodes (minus sign)
    double[] unitsPerTrip = {-1, -1};

    List<IOpeningHours> weeklyOpeningHours = new ArrayList<>();
    weeklyOpeningHours.add(
        new OpeningHours(
            ZonedDateTime.of(2020, MARCH.getValue(), 11, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MARCH.getValue(), 11, 18, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    Duration visitDuration = Duration.ofMinutes(20);

    INode job1 = new TimeWindowGeoNode("job1", 50.9333, 6.95, weeklyOpeningHours, visitDuration, 1);
    job1.setLoad(unitsPerTrip);
    this.addElement(job1);

    INode unloadAll1 =
        new TimeWindowGeoNode("unloadAll1", 50.9333, 6.95, weeklyOpeningHours, visitDuration, 1);
    unloadAll1.setUnloadAll(true);
    this.addElement(unloadAll1);

    INode job2 = new TimeWindowGeoNode("job2", 51.4667, 6.85, weeklyOpeningHours, visitDuration, 1);
    job2.setLoad(unitsPerTrip);
    this.addElement(job2);

    INode unloadAll2 =
        new TimeWindowGeoNode("unloadAll2", 50.9333, 6.95, weeklyOpeningHours, visitDuration, 1);
    unloadAll2.setUnloadAll(true);
    this.addElement(unloadAll2);

    INode job3 =
        new TimeWindowGeoNode("job3", 51.45, 7.01667, weeklyOpeningHours, visitDuration, 1);
    job3.setLoad(unitsPerTrip);
    this.addElement(job3);

    INode unloadAll3 =
        new TimeWindowGeoNode("unloadAll3", 50.9333, 6.95, weeklyOpeningHours, visitDuration, 1);
    unloadAll3.setUnloadAll(true);
    this.addElement(unloadAll3);

    INode job4 = new TimeWindowGeoNode("job4", 50.8, 6.48333, weeklyOpeningHours, visitDuration, 1);
    job4.setLoad(unitsPerTrip);
    this.addElement(job4);

    INode unloadAll4 =
        new TimeWindowGeoNode("unloadAll4", 50.9333, 6.95, weeklyOpeningHours, visitDuration, 1);
    unloadAll4.setUnloadAll(true);
    this.addElement(unloadAll4);
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
