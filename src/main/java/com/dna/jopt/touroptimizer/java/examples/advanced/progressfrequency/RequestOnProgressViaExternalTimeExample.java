package com.dna.jopt.touroptimizer.java.examples.advanced.progressfrequency;
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

import java.io.IOException;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static java.time.Month.MAY;

import javax.measure.Quantity;
import javax.measure.quantity.Length;

import com.dna.jopt.framework.body.IOptimization;
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

import tec.units.ri.quantity.Quantities;

/**
 * In this example we are requesting a progress update after a fixed amount of time.
 *
 *
 * @author DNA
 * @version Mar 23, 2021
 * @since Mar 23, 2021
 */
public class RequestOnProgressViaExternalTimeExample extends Optimization {

  public static void main(String[] args)
      throws InterruptedException, ExecutionException, InvalidLicenceException, IOException {
    new RequestOnProgressViaExternalTimeExample().example();
  }

  public String toString() {
    return "Request a progress output.";
  }

  public void example()
      throws InterruptedException, ExecutionException, InvalidLicenceException, IOException {

    // Set the Properties
    RequestOnProgressViaExternalTimeExample.setProperties(this);

    RequestOnProgressViaExternalTimeExample.addNodes(this);
    RequestOnProgressViaExternalTimeExample.addResources(this);

    // We are adding the external timer that requests a progress update
    RequestOnProgressViaExternalTimeExample.createAndAttachTimeForResultRequest(this);

    CompletableFuture<IOptimizationResult> resultFuture = this.startRunAsync();

    // It is important to block the call, otherwise the optimization will be terminated
    resultFuture.get();
  }

  private static void setProperties(IOptimization opti) {

    Properties props = new Properties();

    props.setProperty("JOptExitCondition.JOptGenerationCount", "20000");
    props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumIterations", "1000000");
    props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumRepetions", "1");

    props.setProperty("JOpt.NumCPUCores", "4");

    opti.addElement(props);
  }

  /**
   * When added to the example this method will request a progress update in a fixed time frequency.
   *
   * @param opti the optimization
   */
  private static void createAndAttachTimeForResultRequest(IOptimization opti) {

    Timer timer = new Timer();

    TimerTask task = new MyRequestProgressTimerTask(opti);

    timer.schedule(task, 0L, 500L);
  }

  static class MyRequestProgressTimerTask extends TimerTask {

    private final IOptimization opti;

    public MyRequestProgressTimerTask(IOptimization opti) {
      this.opti = opti;
    }

    @Override
    public void run() {
    	
    	System.out.println("-------- REQUESTING PROGRESS -----------");
    	this.opti.requestProgress();
    }
  }

  private static void addResources(IOptimization opti) {

    List<IWorkingHours> workingHours = new ArrayList<>();
    workingHours.add(
        new WorkingHours(
            ZonedDateTime.of(2020, MAY.getValue(), 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 6, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    workingHours.add(
        new WorkingHours(
            ZonedDateTime.of(2020, MAY.getValue(), 7, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 7, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    Duration maxWorkingTime = Duration.ofHours(13);
    Quantity<Length> maxDistanceKmW = Quantities.getQuantity(1200.0, KILO(METRE));

    CapacityResource rep1 =
        new CapacityResource(
            "Jack", 50.775346, 6.083887, maxWorkingTime, maxDistanceKmW, workingHours);
    rep1.setCost(0, 1, 1);
    opti.addElement(rep1);
  }

  private static void addNodes(IOptimization opti) {

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

    INode koeln =
        new TimeWindowGeoNode("Koeln", 50.9333, 6.95, weeklyOpeningHours, visitDuration, 1);
    opti.addElement(koeln);

    INode oberhausen =
        new TimeWindowGeoNode("Oberhausen", 51.4667, 6.85, weeklyOpeningHours, visitDuration, 1);
    opti.addElement(oberhausen);

    INode essen =
        new TimeWindowGeoNode("Essen", 51.45, 7.01667, weeklyOpeningHours, visitDuration, 1);
    opti.addElement(essen);

    INode dueren =
        new TimeWindowGeoNode("Dueren", 50.8, 6.48333, weeklyOpeningHours, visitDuration, 1);
    opti.addElement(dueren);

    INode nuernberg =
        new TimeWindowGeoNode("Nuernberg", 49.4478, 11.0683, weeklyOpeningHours, visitDuration, 1);
    opti.addElement(nuernberg);

    INode heilbronn =
        new TimeWindowGeoNode("Heilbronn", 49.1403, 9.22, weeklyOpeningHours, visitDuration, 1);
    opti.addElement(heilbronn);

    INode stuttgart =
        new TimeWindowGeoNode("Stuttgart", 48.7667, 9.18333, weeklyOpeningHours, visitDuration, 1);
    opti.addElement(stuttgart);

    INode wuppertal =
        new TimeWindowGeoNode("Wuppertal", 51.2667, 7.18333, weeklyOpeningHours, visitDuration, 1);
    opti.addElement(wuppertal);

    INode aachen =
        new TimeWindowGeoNode("Aachen", 50.775346, 6.083887, weeklyOpeningHours, visitDuration, 1);
    opti.addElement(aachen);
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
