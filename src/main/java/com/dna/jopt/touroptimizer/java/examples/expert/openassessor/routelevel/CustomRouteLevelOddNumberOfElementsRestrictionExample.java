package com.dna.jopt.touroptimizer.java.examples.expert.openassessor.routelevel;
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
import static java.util.Calendar.MAY;
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
import com.dna.jopt.touroptimizer.java.examples.expert.openassessor.routelevel.custom.OpenCostAssessorOptimizationSchemeWithOddNumberOfElementsRestriction;

import tech.units.indriya.quantity.Quantities;

/**
 * Inject a custom restriction for the assessor. Here, routes that have an odd number of nodes are
 * penalized.
 */
public class CustomRouteLevelOddNumberOfElementsRestrictionExample extends Optimization {

  public static void main(String[] args)
      throws InterruptedException, ExecutionException, InvalidLicenceException, IOException {
    new CustomRouteLevelOddNumberOfElementsRestrictionExample().example();
  }

  public String toString() {
    return "Inject a custom restriction for the assessor. Here, routes that have an odd number of nodes are\r\n"
        + " penalized. THIS EXAMPLE CAN BE ONLY RUN WITH A VALID FULL LICENSE!";
  }

  public void example() throws InterruptedException, ExecutionException, InvalidLicenceException, IOException {

    // Set license via helper
    ExampleLicenseHelper.setLicense(this);

    this.setOptimizationScheme(
        new OpenCostAssessorOptimizationSchemeWithOddNumberOfElementsRestriction(this));

    // Properties!
    this.setProperties();

    this.addNodes();
    this.addResources();

    System.out.println(this.startRunAsync().get());
  }

  private void setProperties() {

    Properties props = new Properties();

    props.setProperty("JOptExitCondition.JOptGenerationCount", "20000");
    props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumIterations", "1000000");
    props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumRepetions", "1");
    props.setProperty("JOpt.NumCPUCores", "4");

    this.addElement(props);
  }

  private void addResources() {

    List<IWorkingHours> workingHours = new ArrayList<>();
    workingHours.add(
        new WorkingHours(
            ZonedDateTime.of(2020, MAY, 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY, 6, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    workingHours.add(
        new WorkingHours(
            ZonedDateTime.of(2020, MAY, 7, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY, 7, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    workingHours.add(
        new WorkingHours(
            ZonedDateTime.of(2020, MAY, 8, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY, 8, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    workingHours.add(
        new WorkingHours(
            ZonedDateTime.of(2020, MAY, 9, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY, 9, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    workingHours.add(
        new WorkingHours(
            ZonedDateTime.of(2020, MAY, 10, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY, 10, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    Duration maxWorkingTime = Duration.ofHours(10);
    Quantity<Length> maxDistanceKmW = Quantities.getQuantity(1200.0, KILO(METRE));

    CapacityResource rep1 =
        new CapacityResource(
            "Jack", 50.775346, 6.083887, maxWorkingTime, maxDistanceKmW, workingHours);
    rep1.setCost(0, 1, 1);
    this.addElement(rep1);
  }

  private void addNodes() {

    List<IOpeningHours> weeklyOpeningHours = new ArrayList<>();
    weeklyOpeningHours.add(
        new OpeningHours(
            ZonedDateTime.of(2020, MAY, 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY, 6, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    weeklyOpeningHours.add(
        new OpeningHours(
            ZonedDateTime.of(2020, MAY, 7, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY, 7, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    weeklyOpeningHours.add(
        new OpeningHours(
            ZonedDateTime.of(2020, MAY, 8, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY, 8, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    weeklyOpeningHours.add(
        new OpeningHours(
            ZonedDateTime.of(2020, MAY, 9, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY, 9, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    weeklyOpeningHours.add(
        new OpeningHours(
            ZonedDateTime.of(2020, MAY, 10, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY, 10, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    Duration visitDuration = Duration.ofMinutes(20);

    // Define some nodes
    TimeWindowGeoNode koeln1 =
        new TimeWindowGeoNode("Koeln1", 50.9333, 6.95, weeklyOpeningHours, visitDuration, 1);
    this.addElement(koeln1);

    TimeWindowGeoNode koeln2 =
        new TimeWindowGeoNode("Koeln2", 50.9333, 6.95, weeklyOpeningHours, visitDuration, 1);
    this.addElement(koeln2);

    TimeWindowGeoNode koeln3 =
        new TimeWindowGeoNode("Koeln3", 50.9333, 6.95, weeklyOpeningHours, visitDuration, 1);
    this.addElement(koeln3);

    TimeWindowGeoNode koeln4 =
        new TimeWindowGeoNode("Koeln4", 50.9333, 6.95, weeklyOpeningHours, visitDuration, 1);
    this.addElement(koeln4);

    TimeWindowGeoNode oberhausen1 =
        new TimeWindowGeoNode("Oberhausen1", 51.4667, 6.85, weeklyOpeningHours, visitDuration, 1);
    this.addElement(oberhausen1);

    TimeWindowGeoNode oberhausen2 =
        new TimeWindowGeoNode("Oberhausen2", 51.4667, 6.85, weeklyOpeningHours, visitDuration, 1);
    this.addElement(oberhausen2);

    TimeWindowGeoNode essen1 =
        new TimeWindowGeoNode("Essen1", 51.45, 7.01667, weeklyOpeningHours, visitDuration, 1);
    this.addElement(essen1);

    TimeWindowGeoNode essen2 =
        new TimeWindowGeoNode("Essen2", 51.45, 7.01667, weeklyOpeningHours, visitDuration, 1);
    this.addElement(essen2);

    TimeWindowGeoNode essen3 =
        new TimeWindowGeoNode("Essen3", 51.45, 7.01667, weeklyOpeningHours, visitDuration, 1);
    this.addElement(essen3);

    TimeWindowGeoNode essen4 =
        new TimeWindowGeoNode("Essen4", 51.45, 7.01667, weeklyOpeningHours, visitDuration, 1);
    this.addElement(essen4);

    TimeWindowGeoNode dueren1 =
        new TimeWindowGeoNode("Dueren1", 50.8, 6.48333, weeklyOpeningHours, visitDuration, 1);
    this.addElement(dueren1);

    TimeWindowGeoNode dueren2 =
        new TimeWindowGeoNode("Dueren2", 50.8, 6.48333, weeklyOpeningHours, visitDuration, 1);
    this.addElement(dueren2);

    TimeWindowGeoNode nuernberg1 =
        new TimeWindowGeoNode("Nuernberg1", 49.4478, 11.0683, weeklyOpeningHours, visitDuration, 1);
    this.addElement(nuernberg1);

    TimeWindowGeoNode nuernberg2 =
        new TimeWindowGeoNode("Nuernberg2", 49.4478, 11.0683, weeklyOpeningHours, visitDuration, 1);
    this.addElement(nuernberg2);

    TimeWindowGeoNode heilbronn1 =
        new TimeWindowGeoNode("Heilbronn1", 49.1403, 9.22, weeklyOpeningHours, visitDuration, 1);
    this.addElement(heilbronn1);

    TimeWindowGeoNode heilbronn2 =
        new TimeWindowGeoNode("Heilbronn2", 49.1403, 9.22, weeklyOpeningHours, visitDuration, 1);
    this.addElement(heilbronn2);

    TimeWindowGeoNode heilbronn3 =
        new TimeWindowGeoNode("Heilbronn3", 49.1403, 9.22, weeklyOpeningHours, visitDuration, 1);
    this.addElement(heilbronn3);

    TimeWindowGeoNode heilbronn4 =
        new TimeWindowGeoNode("Heilbronn4", 49.1403, 9.22, weeklyOpeningHours, visitDuration, 1);
    this.addElement(heilbronn4);

    TimeWindowGeoNode stuttgart1 =
        new TimeWindowGeoNode("Stuttgart1", 48.7667, 9.18333, weeklyOpeningHours, visitDuration, 1);
    this.addElement(stuttgart1);

    TimeWindowGeoNode stuttgart2 =
        new TimeWindowGeoNode("Stuttgart2", 48.7667, 9.18333, weeklyOpeningHours, visitDuration, 1);
    this.addElement(stuttgart2);

    TimeWindowGeoNode wuppertal1 =
        new TimeWindowGeoNode("Wuppertal1", 51.2667, 7.18333, weeklyOpeningHours, visitDuration, 1);
    this.addElement(wuppertal1);

    TimeWindowGeoNode wuppertal2 =
        new TimeWindowGeoNode("Wuppertal2", 51.2667, 7.18333, weeklyOpeningHours, visitDuration, 1);
    this.addElement(wuppertal2);

    TimeWindowGeoNode wuppertal3 =
        new TimeWindowGeoNode("Wuppertal3", 51.2667, 7.18333, weeklyOpeningHours, visitDuration, 1);
    this.addElement(wuppertal3);

    TimeWindowGeoNode wuppertal4 =
        new TimeWindowGeoNode("Wuppertal4", 51.2667, 7.18333, weeklyOpeningHours, visitDuration, 1);
    this.addElement(wuppertal4);

    TimeWindowGeoNode aachen1 =
        new TimeWindowGeoNode("Aachen1", 50.775346, 6.083887, weeklyOpeningHours, visitDuration, 1);
    this.addElement(aachen1);

    TimeWindowGeoNode aachen2 =
        new TimeWindowGeoNode("Aachen2", 50.775346, 6.083887, weeklyOpeningHours, visitDuration, 1);
    this.addElement(aachen2);
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
    System.out.println("code: " + code + " message:" + message);
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
    //

  }
}
