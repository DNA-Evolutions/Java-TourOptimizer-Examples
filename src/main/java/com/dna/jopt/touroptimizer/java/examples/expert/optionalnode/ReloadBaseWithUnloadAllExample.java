package com.dna.jopt.touroptimizer.java.examples.expert.optionalnode;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static java.time.Month.MAY;
import static java.time.temporal.ChronoUnit.MINUTES;

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
import com.dna.jopt.member.unit.node.INode;
import com.dna.jopt.member.unit.node.geo.TimeWindowGeoNode;
import com.dna.jopt.member.unit.resource.CapacityResource;
import com.dna.jopt.touroptimizer.java.examples.ExampleLicenseHelper;

import tech.units.indriya.quantity.Quantities;

/**
 * In this example optional Nodes can be used to restock or unload goods that need to be
 * delivered/were picked up elsewhere. If a Node is set to be optional, the optimizer can choose to
 * schedule it or not. Most optional Nodes in this example only allow to unload a set amount.
 * However, one optional Node has {@code unloadAll()} set to true, and therefore allows Resources drop the total
 * Capacity they are carrying.
 *
 * @author DNA
 * @version Mar 23, 2021
 * @since Mar 23, 2021
 */
public class ReloadBaseWithUnloadAllExample extends Optimization {

  public static void main(String[] args)
      throws InvalidLicenceException, InterruptedException, ExecutionException, IOException {
    new ReloadBaseWithUnloadAllExample().example();
  }

  public String toString() {
    return "In this example optional nodes can be used to unload or load goods. If a node is set to be\r\n"
        + " optional, the optimizer can choose to schedule it or not. THIS EXAMPLE CAN BE ONLY RUN WITH A VALID FULL LICENSE!";
  }

  public void example()
      throws InvalidLicenceException, InterruptedException, ExecutionException, IOException {

    // Set license via helper
    ExampleLicenseHelper.setLicense(this);

    // Set the Properties
    this.setProperties();

    this.addNodes();
    this.addResources();

    CompletableFuture<IOptimizationResult> resultFuture = this.startRunAsync();

    // It is important to block the call, otherwise the Optimization will be terminated
    resultFuture.get();
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

  /**
   * Here we define a Resource that has a total Capacity of 30 goods it can carry. Since we will later define the
   * pick up jobs at the Nodes at values -10 and -15, the Resource will be at full capacity after 2-3 Nodes.
   */
  private void addResources() {

    double[] rep1InitialLoad = {0};

    List<IWorkingHours> workingHours = new ArrayList<>();
    workingHours.add(
        new WorkingHours(
            ZonedDateTime.of(2020, MAY.getValue(), 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 6, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    workingHours.add(
        new WorkingHours(
            ZonedDateTime.of(2020, MAY.getValue(), 7, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 7, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    Duration maxWorkingTime = Duration.ofHours(10);
    Quantity<Length> maxDistanceKmW = Quantities.getQuantity(1200.0, KILO(METRE));

    CapacityResource rep1 =
        new CapacityResource(
            "Jack", 50.775346, 6.083887, maxWorkingTime, maxDistanceKmW, workingHours);
    rep1.setCost(0, 1, 1);
    rep1.setCapacity(new double[] {30});
    rep1.setCost(0, 1, 1);
    rep1.setInitialLoad(rep1InitialLoad);

    this.addElement(rep1);
  }

  /**
   * Here, we are adding the Nodes from which goods need to be picked up or can be dropped. Note, that from all the
   * optional Nodes, only "KoelnOptional" has unloadAll set to true. While the other optional Nodes only allow to
   * unload a set amount (10), "KoelnOptional" allows to drop the maximum Capacity of cargo a Resource can
   * carry (30).
   */
  private void addNodes() {

    List<IOpeningHours> weeklyOpeningHours = new ArrayList<>();

    weeklyOpeningHours.add(
        new OpeningHours(
            ZonedDateTime.of(2020, MAY.getValue(), 7, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 7, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    Duration visitDuration = Duration.of(20, MINUTES);

    double[] loadPerNode = {-10}; // We pick up 10 items
    double[] loadPerNodeHigh = {-15}; // We pick up 15 items
    double[] unloadPerOptionalNode = {10}; // We drop 10 items

    INode koelnOptional =
        new TimeWindowGeoNode("KoelnOptional", 50.9333, 6.95, weeklyOpeningHours, visitDuration, 1);
    koelnOptional.setIsOptional(true);
    koelnOptional.setUnloadAll(true);

    INode koeln2 =
        new TimeWindowGeoNode("Koeln2", 50.9333, 6.95, weeklyOpeningHours, visitDuration, 1);
    koeln2.setLoad(loadPerNodeHigh);

    INode koeln3 =
        new TimeWindowGeoNode("Koeln3", 50.9333, 6.95, weeklyOpeningHours, visitDuration, 1);
    koeln3.setLoad(loadPerNodeHigh);

    INode oberhausenOptional =
        new TimeWindowGeoNode(
            "OberhausenOptional", 51.4667, 6.85, weeklyOpeningHours, visitDuration, 1);
    oberhausenOptional.setIsOptional(true);
    oberhausenOptional.setLoad(unloadPerOptionalNode);

    INode oberhausenOptional2 =
        new TimeWindowGeoNode(
            "OberhausenOptional2", 51.4667, 6.85, weeklyOpeningHours, visitDuration, 1);
    oberhausenOptional2.setIsOptional(true);
    oberhausenOptional2.setLoad(unloadPerOptionalNode);

    INode oberhausen2 =
        new TimeWindowGeoNode("Oberhausen2", 51.4667, 6.85, weeklyOpeningHours, visitDuration, 1);
    oberhausen2.setLoad(loadPerNode);

    INode oberhausen3 =
        new TimeWindowGeoNode("Oberhausen3", 51.4667, 6.85, weeklyOpeningHours, visitDuration, 1);
    oberhausen3.setLoad(loadPerNode);

    INode essen1 =
        new TimeWindowGeoNode("Essen1", 51.45, 7.01667, weeklyOpeningHours, visitDuration, 1);
    essen1.setLoad(loadPerNode);

    INode dresdenOptionalFarFarAway =
        new TimeWindowGeoNode(
            "DresdenFarAwayOptional", 51.05347, 13.74316, weeklyOpeningHours, visitDuration, 1);
    dresdenOptionalFarFarAway.setIsOptional(true);
    dresdenOptionalFarFarAway.setLoad(unloadPerOptionalNode);

    this.addElement(koelnOptional);
    this.addElement(koeln2);
    this.addElement(koeln3);

    this.addElement(oberhausenOptional);
    this.addElement(oberhausenOptional2);
    this.addElement(oberhausen2);
    this.addElement(oberhausen3);

    this.addElement(essen1);

    this.addElement(dresdenOptionalFarFarAway);
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
