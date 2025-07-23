package com.dna.jopt.touroptimizer.java.examples.advanced.co2emission;
/*-
 * #%L
 * JOpt TourOptimizer Examples
 * %%
 * Copyright (C) 2017 - 2020 DNA Evolutions GmbH
 * %%
 * This file is subject to the terms and conditions defined in file 'LICENSE.txt',
 * which is part of this source code package.
 * 
 * If not, see <https://www.dna-evolutions.com/agb-conditions-and-terms/>.
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

import com.dna.jopt.framework.body.IOptimization;
import com.dna.jopt.framework.body.Optimization;
import com.dna.jopt.framework.exception.caught.InvalidLicenceException;
import com.dna.jopt.framework.outcomewrapper.IOptimizationResult;
import com.dna.jopt.member.unit.hours.IWorkingHours;
import com.dna.jopt.member.unit.hours.IOpeningHours;
import com.dna.jopt.member.unit.hours.WorkingHours;
import com.dna.jopt.member.unit.hours.OpeningHours;
import com.dna.jopt.member.unit.node.INode;
import com.dna.jopt.member.unit.node.geo.TimeWindowGeoNode;
import com.dna.jopt.member.unit.resource.CapacityResource;
import com.dna.jopt.member.unit.resource.IResource;

/**
 * In this example we use the CO2 emission feature with two vehicles of different CO2 emission
 * factor.
 *
 * @author jrich
 * @version Sep 28, 2021
 * @since Sep 28, 2021
 */
public class CO2EmissionOptimizationExample extends Optimization {

  /**
   * The main method.
   *
   * @param args the arguments
   * @throws InterruptedException the interrupted exception
   * @throws ExecutionException the execution exception
   * @throws InvalidLicenceException the invalid licence exception
   * @throws IOException
   */
  public static void main(String[] args)
      throws InterruptedException, ExecutionException, InvalidLicenceException, IOException {
    new CO2EmissionOptimizationExample().example();
  }

  public String toString() {
    return "CO2 emission factor example.";
  }

  /**
   * The method which executes the necessary parts for the Optimization.
   *
   * @throws InterruptedException the interrupted exception
   * @throws ExecutionException the execution exception
   * @throws InvalidLicenceException the invalid licence exception
   * @throws IOException
   */
  public void example()
      throws InterruptedException, ExecutionException, InvalidLicenceException, IOException {

    // We use the free mode for the example - please modify the ExampleLicenseHelper in case you
    // have a valid
    // license.

    // Setting Properties, adding all Elements, and attaching to Observables
    //    (1) Adding properties
    CO2EmissionOptimizationExample.addProperties(this);

    //    (2) Adding Nodes
    CO2EmissionOptimizationExample.addNodes(this);

    //    (3) Adding Resources
    CO2EmissionOptimizationExample.addResources(this);

    //    (4) Attaching to Observables
    CO2EmissionOptimizationExample.attachToObservables(this);

    //    (5) Starting the Optimization completable Future and presenting the results
    CO2EmissionOptimizationExample.startAndPresentResult(this);
  }

  /**
   * Start the Optimization and present the result.
   *
   * @param opti the optimization instance
   * @throws InvalidLicenceException the invalid licence exception
   * @throws InterruptedException the interrupted exception
   * @throws ExecutionException the execution exception
   */
  private static void startAndPresentResult(IOptimization opti)
      throws InvalidLicenceException, InterruptedException, ExecutionException {

    // Extracting a completable Future for the optimization result
    CompletableFuture<IOptimizationResult> resultFuture = opti.startRunAsync();

    // It is important to block the call, otherwise the Optimization will be terminated
    IOptimizationResult result = resultFuture.get();

    // Presenting the result
    System.out.println(result);
  }

  /**
   * Adds the Properties to the Optimization.
   *
   * @param opti the optimization instance
   */
  private static void addProperties(IOptimization opti) {

    Properties props = new Properties();

    props.setProperty("JOptExitCondition.JOptGenerationCount", "2000");
    props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumIterations", "10000");
    props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumRepetions", "1");

    // We need to enable CO2Emission optimization by setting the corresponding property to a value
    // higher than zero (default value)
    props.setProperty("JOptWeight.CO2Emission", "10.0");

    opti.addElement(props);
  }

  /**
   * Adds the Nodes to the Optimization.
   *
   * @param opti the optimization instance
   */
  private static void addNodes(IOptimization opti) {

    // Define the OpeningHours
    List<IOpeningHours> weeklyOpeningHours = new ArrayList<>();
    weeklyOpeningHours.add(
        new OpeningHours(
            ZonedDateTime.of(2020, MAY.getValue(), 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 6, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    weeklyOpeningHours.add(
        new OpeningHours(
            ZonedDateTime.of(2020, MAY.getValue(), 7, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 7, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    Duration visitDuration = Duration.ofMinutes(20);

    int importance = 1;

    // Define some Nodes
    INode koeln =
        new TimeWindowGeoNode(
            "Koeln", 50.9333, 6.95, weeklyOpeningHours, visitDuration, importance);
    opti.addElement(koeln);

    INode essen =
        new TimeWindowGeoNode(
            "Essen", 51.45, 7.01667, weeklyOpeningHours, visitDuration, importance);
    opti.addElement(essen);

    INode dueren =
        new TimeWindowGeoNode(
            "Dueren", 50.8, 6.48333, weeklyOpeningHours, visitDuration, importance);
    opti.addElement(dueren);

    INode nuernberg =
        new TimeWindowGeoNode(
            "Nuernberg", 49.4478, 11.0683, weeklyOpeningHours, visitDuration, importance);
    opti.addElement(nuernberg);

    INode heilbronn =
        new TimeWindowGeoNode(
            "Heilbronn", 49.1403, 9.22, weeklyOpeningHours, visitDuration, importance);
    opti.addElement(heilbronn);

    INode wuppertal =
        new TimeWindowGeoNode(
            "Wuppertal", 51.2667, 7.18333, weeklyOpeningHours, visitDuration, importance);
    opti.addElement(wuppertal);

    INode aachen =
        new TimeWindowGeoNode(
            "Aachen", 50.775346, 6.083887, weeklyOpeningHours, visitDuration, importance);
    opti.addElement(aachen);
  }

  /**
   * Adds the Resources to the Optimization.
   *
   * @param opti the optimization instance
   */
  private static void addResources(IOptimization opti) {

    /*
     * We define two vehicles with different CO2 emission factor. The default value is 0.377 [kgCO2/km]
     * what is approximately the emission factor of a vehicle with an average fuel consumption of
     * 12-litre diesel per 100 km.
     *
     * Event though, vehicle two has a lower fuel consumption, we assume a higher fix cost of 1000 arbitrary units.
     *
     * As a result, vehicle one is utilized for a shorter trip, whereas vehicle two is utilized for a longer trip.
     * In case we decrease the fix cost, or increase the value of the property for CO2Optimization, we will see
     * that only vehicle two is utilized.
     *
     */

    double fuelConsumptionVehicleOne = 25.0 / 100; // Litre Diesel per 1 km
    double fuelConsumptionVehicleTwo = 8.0 / 100; // Litre petrol per 1 km

    double co2FactorDiesel = 2.629; // kg/l
    double co2FactorPetrol = 2.362; // kg/l

    Duration maxWorkingTime = Duration.ofHours(12);
    Quantity<Length> maxDistanceKmW = Quantities.getQuantity(1200.0, KILO(METRE));

    // Define the Resource
    IResource vehicleOne =
        new CapacityResource(
            "Vehicle One",
            50.775346,
            6.083887,
            maxWorkingTime,
            maxDistanceKmW,
            createWorkingHours());

    vehicleOne.setAverageCO2EmissionFactor(fuelConsumptionVehicleOne * co2FactorDiesel);
    opti.addElement(vehicleOne);

    // Define the Resource
    IResource vehicleTwo =
        new CapacityResource(
            "Vehicle Two",
            50.775346,
            6.083887,
            maxWorkingTime,
            maxDistanceKmW,
            createWorkingHours());
    vehicleTwo.setFixCost(1000.0); // Set an offset cost
    vehicleTwo.setAverageCO2EmissionFactor(fuelConsumptionVehicleTwo * co2FactorPetrol);
    opti.addElement(vehicleTwo);
  }

  private static List<IWorkingHours> createWorkingHours() {
    // Define the WorkingHours
    List<IWorkingHours> workingHours = new ArrayList<>();
    workingHours.add(
        new WorkingHours(
            ZonedDateTime.of(2020, MAY.getValue(), 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 6, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    workingHours.add(
        new WorkingHours(
            ZonedDateTime.of(2020, MAY.getValue(), 7, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 7, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    return workingHours;
  }

  /**
   * Attach to different Events (Observables) of the optimization instance.
   *
   * @param opti the optimization instance
   */
  private static void attachToObservables(IOptimization opti) {

    opti.getOptimizationEvents()
        .progressSubject()
        .subscribe(
            p -> {
              System.out.println(p.getProgressString());
            });

    opti.getOptimizationEvents()
        .warningSubject()
        .subscribe(
            w -> {
              System.out.println(w.toString());
            });

    opti.getOptimizationEvents()
        .statusSubject()
        .subscribe(
            s -> {
              System.out.println(s.toString());
            });

    opti.getOptimizationEvents()
        .errorSubject()
        .subscribe(
            e -> {
              System.out.println(e.toString());
            });
  }
}
