package com.dna.jopt.touroptimizer.java.examples.advanced.condition;
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
import static javax.measure.MetricPrefix.KILO;
import static tech.units.indriya.unit.Units.METRE;

import java.io.File;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
import com.dna.jopt.framework.outcomewrapper.IOptimizationResult;
import com.dna.jopt.io.exporting.IEntityExporter;
import com.dna.jopt.io.exporting.kml.EntityKMLExporter;
import com.dna.jopt.member.unit.condition.resource.IConstraintResource;
import com.dna.jopt.member.unit.condition.resource.MandatoryResourceConstraint;
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
 * In this example we are setting a mandatory Resource for a Node through a hard constraint.
 *
 * @author Jens Richter
 * @version Mar 23, 2021
 * @since Mar 15, 2021
 */
public class MandatoryResourceConditionExample extends Optimization {

  public static void main(String[] args)
      throws IOException, InvalidLicenceException, InterruptedException, ExecutionException {
    new MandatoryResourceConditionExample().example();
  }

  public String toString() {
    return "Simple example of setting a mandatory (hard-constrained) Resource for a Node.";
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

  // Add some Resources
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

  // Define some Nodes
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

    // In order to ensure K�ln is visited by the Resource "John" we are defining a mandatory
    // resource constraint
    // and add the respective Resource and Node
    IConstraintResource johnMandConstraint = new MandatoryResourceConstraint();
    johnMandConstraint.addResource("John", 10);
    koeln1.addConstraint(johnMandConstraint);
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
  public void onAsynchronousOptimizationResult(IOptimizationResult rapoptResult) {
    System.out.println(rapoptResult);

    IEntityExporter kmlExporter = new EntityKMLExporter();
    kmlExporter.setTitle("TEST EXPORT");

    try {

      kmlExporter.export(
          rapoptResult.getContainer(),
          new FileOutputStream(new File("./RapotTestConditionOpti.kml")));

    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }
}
