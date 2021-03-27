package com.dna.jopt.touroptimizer.java.examples.basic.pillar_07;
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
import static tec.units.ri.unit.MetricPrefix.KILO;
import static tec.units.ri.unit.Units.METRE;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static java.time.Month.JUNE;
import javax.measure.Quantity;
import javax.measure.quantity.Length;

import com.dna.jopt.framework.body.IOptimization;
import com.dna.jopt.framework.body.Optimization;
import com.dna.jopt.framework.exception.caught.InvalidLicenceException;
import com.dna.jopt.framework.outcomewrapper.IOptimizationProgress;
import com.dna.jopt.framework.outcomewrapper.IOptimizationResult;
import com.dna.jopt.io.exporting.IEntityExporter;
import com.dna.jopt.io.exporting.kml.EntityKMLExporter;
import com.dna.jopt.member.unit.hours.IWorkingHours;
import com.dna.jopt.member.unit.hours.IOpeningHours;
import com.dna.jopt.member.unit.hours.WorkingHours;
import com.dna.jopt.member.unit.hours.OpeningHours;
import com.dna.jopt.member.unit.node.INode;
import com.dna.jopt.member.unit.node.IPillarNode;
import com.dna.jopt.member.unit.node.event.PillarEventNode;
import com.dna.jopt.member.unit.node.geo.PillarTimeWindowGeoNode;
import com.dna.jopt.member.unit.node.geo.TimeWindowGeoNode;
import com.dna.jopt.member.unit.resource.CapacityResource;
import com.dna.jopt.member.unit.resource.IResource;
import com.dna.jopt.touroptimizer.java.examples.ExampleLicenseHelper;

import tec.units.ri.quantity.Quantities;

/** Doing an asynch run and defining pillar nodes */
public class PillarExample extends Optimization {

  public static void main(String[] args)
      throws InterruptedException, ExecutionException, IOException, InvalidLicenceException {
    new PillarExample().example();
  }

  private IResource johnRes;
  private IResource jackRes;

  public String toString() {
    return "Positioing a pillar node, attached to a resource.";
  }

  public void example()
      throws InterruptedException, ExecutionException, IOException, InvalidLicenceException {

    // Set license via helper
    ExampleLicenseHelper.setLicense(this);

    // Properties!
    this.setProperties(this);

    // Create res
    this.addResources(this);

    this.addNodes(this);
    this.addPillars(this);

    CompletableFuture<IOptimizationResult> resultFuture = this.startRunAsync();

    // It is important to block the call, otherwise optimization will be terminated
    resultFuture.get();
  }

  private void setProperties(IOptimization opti) {

    Properties props = new Properties();

    props.setProperty("JOptExitCondition.JOptGenerationCount", "20000");
    props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumIterations", "1000000");
    props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumRepetions", "1");
    props.setProperty("JOpt.NumCPUCores", "4");

    opti.addElement(props);
  }

  private static List<IWorkingHours> getDefaultWorkingHours() {

    List<IWorkingHours> workingHours = new ArrayList<>();
    workingHours.add(
        new WorkingHours(
            ZonedDateTime.of(2019, JUNE.getValue(), 4, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2019, JUNE.getValue(), 4, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    workingHours.add(
        new WorkingHours(
            ZonedDateTime.of(2019, JUNE.getValue(), 5, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2019, JUNE.getValue(), 5, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    workingHours.add(
        new WorkingHours(
            ZonedDateTime.of(2019, JUNE.getValue(), 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2019, JUNE.getValue(), 6, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    return workingHours;
  }

  private void addResources(IOptimization opti) {

    Duration maxWorkingTime = Duration.ofHours(13);
    Quantity<Length> maxDistanceKmW = Quantities.getQuantity(1200.0, KILO(METRE));

    this.johnRes =
        new CapacityResource(
            "John", 50.775346, 6.083887, maxWorkingTime, maxDistanceKmW, getDefaultWorkingHours());
    johnRes.setCost(0, 1, 1);
    opti.addElement(johnRes);

    this.jackRes =
        new CapacityResource(
            "Jack", 50.775346, 6.083887, maxWorkingTime, maxDistanceKmW, getDefaultWorkingHours());
    jackRes.setCost(0, 1, 1);
    opti.addElement(jackRes);
  }

  private void addNodes(IOptimization opti) {

    List<IOpeningHours> weeklyOpeningHoursNormalNodes = new ArrayList<>();
    weeklyOpeningHoursNormalNodes.add(
        new OpeningHours(
            ZonedDateTime.of(2019, JUNE.getValue(), 4, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2019, JUNE.getValue(), 4, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    weeklyOpeningHoursNormalNodes.add(
        new OpeningHours(
            ZonedDateTime.of(2019, JUNE.getValue(), 5, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2019, JUNE.getValue(), 5, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    weeklyOpeningHoursNormalNodes.add(
        new OpeningHours(
            ZonedDateTime.of(2019, JUNE.getValue(), 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2019, JUNE.getValue(), 6, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    Duration visitDuration = Duration.ofMinutes(60);

    // Define some nodes
    INode koeln =
        new TimeWindowGeoNode(
            "Koeln", 50.9333, 6.95, weeklyOpeningHoursNormalNodes, visitDuration, 1);
    opti.addElement(koeln);

    INode oberhausen =
        new TimeWindowGeoNode(
            "Oberhausen", 51.4667, 6.85, weeklyOpeningHoursNormalNodes, visitDuration, 1);
    opti.addElement(oberhausen);

    INode nuernberg =
        new TimeWindowGeoNode(
            "Nuernberg", 49.4478, 11.0683, weeklyOpeningHoursNormalNodes, visitDuration, 1);
    opti.addElement(nuernberg);

    INode heilbronn =
        new TimeWindowGeoNode(
            "Heilbronn", 49.1403, 9.22, weeklyOpeningHoursNormalNodes, visitDuration, 1);
    opti.addElement(heilbronn);

    INode aachen =
        new TimeWindowGeoNode(
            "Aachen", 50.775346, 6.083887, weeklyOpeningHoursNormalNodes, visitDuration, 1);
    opti.addElement(aachen);
  }

  private void addPillars(IOptimization opti) {

    /*
     * Plumbing pillar for john in cologne from 10:00 to 10:50. In case the visit duration equals the
     * opening hour it is not necessary to provide the duration again as an argument for constructing
     * the pillar node.
     */

    IOpeningHours weeklyOpeningHoursPillarNodePlumbingJohn =
        new OpeningHours(
            ZonedDateTime.of(2019, JUNE.getValue(), 5, 10, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2019, JUNE.getValue(), 5, 10, 50, 0, 0, ZoneId.of("Europe/Berlin")));

    IPillarNode colognePlumbing =
        new PillarTimeWindowGeoNode(
            "Cologne Plumbing", 50.9333, 6.95, weeklyOpeningHoursPillarNodePlumbingJohn);

    // Define John as attached resource
    colognePlumbing.attachResource(this.johnRes);

    opti.addElement(colognePlumbing);

    /*
     * Jack has to call a customer between 10:00 and 12:30 for half an hour.
     * His location does not matter.
     *
     */

    IOpeningHours weeklyOpeningHoursPillarCallJack =
        new OpeningHours(
            ZonedDateTime.of(2019, JUNE.getValue(), 6, 10, 30, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2019, JUNE.getValue(), 6, 12, 00, 0, 0, ZoneId.of("Europe/Berlin")));

    Duration visitDurationPillarCallJack = Duration.ofMinutes(30);

    IPillarNode callJack =
        new PillarEventNode(
            "Important call", weeklyOpeningHoursPillarCallJack, visitDurationPillarCallJack);

    // Define Jack as attached resource
    callJack.attachResource(this.jackRes);

    opti.addElement(callJack);

    /*
     * Maintenance Job in Stuttgart between 13:30 and 16:00 for 90 minutes.
     * It doesn't matter if John or Jack takes the job.
     *
     */
    IOpeningHours weeklyOpeningHoursPillarMaintenance =
        new OpeningHours(
            ZonedDateTime.of(2019, JUNE.getValue(), 4, 13, 30, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2019, JUNE.getValue(), 4, 16, 00, 0, 0, ZoneId.of("Europe/Berlin")));

    Duration visitDurationPillarMaintenance = Duration.ofMinutes(90);

    // We are not defining any attached res

    IPillarNode maintenanceJob =
        new PillarTimeWindowGeoNode(
            "Maintenance job",
            48.7667,
            9.18333,
            weeklyOpeningHoursPillarMaintenance,
            visitDurationPillarMaintenance);

    opti.addElement(maintenanceJob);
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

    IEntityExporter kmlExporter = new EntityKMLExporter();
    kmlExporter.setTitle("" + this.getClass().getSimpleName());

    try {

      kmlExporter.export(
          rapoptResult.getContainer(),
          new FileOutputStream(new File("./" + this.getClass().getSimpleName() + ".kml")));

    } catch (FileNotFoundException e) {
      //
      e.printStackTrace();
    }
  }
}
