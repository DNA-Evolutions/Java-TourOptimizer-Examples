package com.dna.jopt.touroptimizer.java.examples.advanced.requestresult;
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
import static java.time.Month.MARCH;
import static java.time.Month.MAY;
import static tec.units.ri.unit.MetricPrefix.KILO;
import static tec.units.ri.unit.Units.METRE;

import java.io.IOException;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
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

/**
 * Request an optimization result during runtime and before optimization finishes.
 *
 * @author DNA
 * @version Mar 23, 2021
 * @since Mar 23, 2021
 */
public class RequestResultExample extends Optimization {

  public static void main(String[] args)
      throws IOException, InvalidLicenceException, InterruptedException, ExecutionException {
    new RequestResultExample().example();
  }

  public String toString() {
    return "Request an optimization result during runtime and before optimization finishes.";
  }

  public void example()
      throws IOException, InvalidLicenceException, InterruptedException, ExecutionException {

    // Set license via helper
    ExampleLicenseHelper.setLicense(this);

    this.addNodes();
    this.addResources();

    CompletableFuture<IOptimizationResult> resultFuture = this.startRunAsync();

    // It is important to block the call, otherwise the optimization will be terminated
    resultFuture.get();
  }

  private static List<IWorkingHours> getDefaultWorkingHours() {

    List<IWorkingHours> workingHours = new ArrayList<>();
    workingHours.add(
        new WorkingHours(
            ZonedDateTime.of(2020, MARCH.getValue(), 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MARCH.getValue(), 6, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    workingHours.add(
        new WorkingHours(
            ZonedDateTime.of(2020, MARCH.getValue(), 7, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MARCH.getValue(), 7, 20, 0, 0, 0, ZoneId.of("Europe/Berlin"))));
    return workingHours;
  }

  private void addResources() {

    Duration maxWorkingTimeJack = Duration.ofHours(8);
    Duration maxWorkingTimeJohn = Duration.ofHours(14);
    Quantity<Length> maxDistanceKmW = Quantities.getQuantity(1200.0, KILO(METRE));

    IResource rep1 =
        new CapacityResource(
            "Jack",
            50.775346,
            6.083887,
            maxWorkingTimeJack,
            maxDistanceKmW,
            getDefaultWorkingHours());
    rep1.setCost(0, 1, 1);

    this.addElement(rep1);

    IResource rep2 =
        new CapacityResource(
            "John",
            50.775346,
            6.083887,
            maxWorkingTimeJohn,
            maxDistanceKmW,
            getDefaultWorkingHours());
    rep2.setCost(0, 1, 1);
    this.addElement(rep2);
  }

  private void addNodes() {

    List<IOpeningHours> weeklyOpeningHours = new ArrayList<>();
    weeklyOpeningHours.add(
        new OpeningHours(
            ZonedDateTime.of(2020, MARCH.getValue(), 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MARCH.getValue(), 6, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    weeklyOpeningHours.add(
        new OpeningHours(
            ZonedDateTime.of(2020, MARCH.getValue(), 7, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MARCH.getValue(), 7, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    Duration visitDuration = Duration.ofMinutes(20);

    // Define some Nodes

    INode koeln =
        new TimeWindowGeoNode("Koeln", 50.9333, 6.95, weeklyOpeningHours, visitDuration, 1);
    this.addElement(koeln);

    INode koeln1 =
        new TimeWindowGeoNode("Koeln1", 50.9333, 6.95, weeklyOpeningHours, visitDuration, 1);
    this.addElement(koeln1);

    INode oberhausen =
        new TimeWindowGeoNode("Oberhausen", 51.4667, 6.85, weeklyOpeningHours, visitDuration, 1);
    this.addElement(oberhausen);

    INode essen =
        new TimeWindowGeoNode("Essen", 51.45, 7.01667, weeklyOpeningHours, visitDuration, 1);
    this.addElement(essen);

    INode heilbronn =
        new TimeWindowGeoNode("Heilbronn", 49.1403, 9.22, weeklyOpeningHours, visitDuration, 1);
    this.addElement(heilbronn);

    INode stuttgart =
        new TimeWindowGeoNode("Stuttgart", 48.7667, 9.18333, weeklyOpeningHours, visitDuration, 1);
    this.addElement(stuttgart);

    INode wuppertal =
        new TimeWindowGeoNode("Wuppertal", 51.2667, 7.18333, weeklyOpeningHours, visitDuration, 1);
    this.addElement(wuppertal);

    INode aachen =
        new TimeWindowGeoNode("Aachen", 50.775346, 6.083887, weeklyOpeningHours, visitDuration, 1);
    this.addElement(aachen);
  }

  @Override
  public void onError(int code, String message) {
    //

  }

  @Override
  public void onStatus(int code, String message) {
    //

  }

  @Override
  public void onWarning(int code, String message) {
    //

  }

  @Override
  public void onProgress(String winnerProgressString) {
    System.out.println(winnerProgressString);
  }

  /**
   * We are requesting a new result after every 20% of the total optimization iterations.
   *
   * @param winner the iOptimizationProgress of the best entity
   */
  @Override
  public void onProgress(IOptimizationProgress winner) {

    if (winner.getProgress() == 20.0) {
      System.out.println("\n##############################################");
      System.out.println("#             Requesting result              #");
      System.out.println("##############################################\n");
      this.requestResult();
    }
  }

  @Override
  public void onAsynchronousOptimizationResult(IOptimizationResult rapoptResult) {
    System.out.println(rapoptResult);
  }

  @Override
  public void requestedAsynchronousOptimizationResult(IOptimizationResult rapoptResult) {
    System.out.println(rapoptResult);
  }
}
