package com.dna.jopt.touroptimizer.java.examples.advanced.openlocation;
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
import static java.util.Calendar.MAY;
import static tec.units.ri.unit.MetricPrefix.KILO;
import static tec.units.ri.unit.Units.METRE;

import java.io.IOException;
import java.io.PrintStream;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import javax.measure.Quantity;
import javax.measure.quantity.Length;

import com.dna.jopt.framework.body.IOptimization;
import com.dna.jopt.framework.body.Optimization;
import com.dna.jopt.framework.exception.caught.InvalidLicenceException;
import com.dna.jopt.framework.outcomewrapper.IOptimizationResult;
import com.dna.jopt.member.unit.hours.IWorkingHours;
import com.dna.jopt.member.unit.converter.openlocation.OpenLocation;
import com.dna.jopt.member.unit.hours.IOpeningHours;
import com.dna.jopt.member.unit.hours.WorkingHours;
import com.dna.jopt.member.unit.hours.OpeningHours;
import com.dna.jopt.member.unit.node.INode;
import com.dna.jopt.member.unit.node.geo.TimeWindowGeoNode;
import com.dna.jopt.member.unit.resource.CapacityResource;
import com.dna.jopt.member.unit.resource.IResource;
import com.dna.jopt.touroptimizer.java.examples.ExampleLicenseHelper;

import tec.units.ri.quantity.Quantities;

/**
 * Example on how to use Plus Code (https://maps.google.com/pluscodes/) to initialize Resources and
 * Nodes of an Optimization.
 *
 * @author jrich
 * @version Mar 26, 2021
 * @since Mar 26, 2021
 */
public class OptimizationWithOpenLocationCodesExample extends Optimization {

  /** The position nodes map. */
  private Map<String, String> positionNodesMap;

  /** The position ress map. */
  private Map<String, String> positionRessMap;

  /**
   * The main method.
   *
   * @param args the arguments
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws InvalidLicenceException the invalid licence exception
   * @throws InterruptedException the interrupted exception
   * @throws ExecutionException the execution exception
   */
  public static void main(String[] args)
      throws IOException, InvalidLicenceException, InterruptedException, ExecutionException {
    new OptimizationWithOpenLocationCodesExample().example();
  }

  /**
   * To string.
   *
   * @return the string
   */
  public String toString() {
    return "Example on how to use Plus Code to initialize Resources and Nodes of an Optimization.";
  }

  /**
   * Example.
   *
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws InvalidLicenceException the invalid licence exception
   * @throws InterruptedException the interrupted exception
   * @throws ExecutionException the execution exception
   */
  public void example()
      throws IOException, InvalidLicenceException, InterruptedException, ExecutionException {

    // Set license via helper
    ExampleLicenseHelper.setLicense(this);

    // Initialize the map of codes to be used
    this.initLocationCodeMaps();

    // Properties!
    this.setProperties();

    this.addNodes();
    this.addResources();

    // Attach do observables
    OptimizationWithOpenLocationCodesExample.attachToObservables(this);

    // Start the optimization
    CompletableFuture<IOptimizationResult> resultFuture = this.startRunAsync();

    // It is important to block the call, otherwise optimization will be terminated
    IOptimizationResult result = resultFuture.get();

    // Show result
    System.out.println(result);
  }

  /** Inits the location code maps. */
  private void initLocationCodeMaps() {

    // Map for nodes
    Map<String, String> posNodesMap = new HashMap<>();

    posNodesMap.put("Koeln", "9F28WXM2+82");
    posNodesMap.put("Oberhausen", "9F38FV82+M2");
    posNodesMap.put("Essen", "9F39F228+2M");
    posNodesMap.put("Nuernberg", "8FXHC3X9+48");
    posNodesMap.put("Heilbronn", "8FXF46RC+42");
    posNodesMap.put("Stuttgart", "8FWFQ58M+M8");
    posNodesMap.put("Wuppertal", "9F39758M+M8");

    this.positionNodesMap = posNodesMap;

    // Map for Resoruces
    Map<String, String> posRessMap = new HashMap<>();

    posRessMap.put("JackKoeln", "9F28WXM2+82");
    posRessMap.put("JackHeilbronn", "8FXF46RC+42");

    this.positionRessMap = posRessMap;
  }

  /** Sets the properties. */
  private void setProperties() {

    Properties props = new Properties();

    props.setProperty("JOptExitCondition.JOptGenerationCount", "1000");
    props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumIterations", "1000");

    this.addElement(props);
  }

  /**
   * Gets the working hours.
   *
   * @return the working hours
   */
  public static List<IWorkingHours> getWorkingHours() {

    List<IWorkingHours> workingHoursOne = new ArrayList<>();
    workingHoursOne.add(
        new WorkingHours(
            ZonedDateTime.of(2020, MAY, 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY, 6, 22, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    return workingHoursOne;
  }

  /** Adds the resources. */
  private void addResources() {

    Duration maxWorkingTime = Duration.ofHours(12);
    Quantity<Length> maxDistanceKmW = Quantities.getQuantity(2200.0, KILO(METRE));

    List<IResource> ress =
        this.positionRessMap
            .entrySet()
            .stream()
            .map(
                e -> {
                  IResource res =
                      new CapacityResource(
                          e.getKey(),
                          OpenLocation.of(e.getValue()).toPos(),
                          maxWorkingTime,
                          maxDistanceKmW,
                          getWorkingHours());

                  res.setCost(0, 1, 1);

                  return res;
                })
            .collect(Collectors.toList());

    ress.stream().forEach(this::addElement);
  }

  /** Adds the nodes. */
  private void addNodes() {

    List<IOpeningHours> weeklyOpeningHoursOne = new ArrayList<>();

    weeklyOpeningHoursOne.add(
        new OpeningHours(
            ZonedDateTime.of(2020, MAY, 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY, 6, 22, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    Duration visitDuration = Duration.ofMinutes(10);

    List<INode> nodes =
        this.positionNodesMap
            .entrySet()
            .stream()
            .map(
                e -> {
                  return new TimeWindowGeoNode(
                      e.getKey(),
                      OpenLocation.of(e.getValue()).toPos(),
                      weeklyOpeningHoursOne,
                      visitDuration,
                      1);
                })
            .collect(Collectors.toList());

    nodes.stream().forEach(this::addElement);
  }

  /**
   * Attach to observables.
   *
   * @param opti the opti
   */
  private static void attachToObservables(IOptimization opti) {

    PrintStream out = System.out;

    opti.getOptimizationEvents()
        .progressSubject()
        .subscribe(p -> out.println(p.getProgressString()));

    opti.getOptimizationEvents().warningSubject().subscribe(w -> out.println(w.toString()));

    opti.getOptimizationEvents().statusSubject().subscribe(s -> out.println(s.toString()));

    opti.getOptimizationEvents().errorSubject().subscribe(e -> out.println(e.toString()));
  }
}
