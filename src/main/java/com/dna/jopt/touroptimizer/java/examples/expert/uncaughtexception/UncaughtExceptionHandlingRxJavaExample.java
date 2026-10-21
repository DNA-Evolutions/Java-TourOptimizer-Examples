package com.dna.jopt.touroptimizer.java.examples.expert.uncaughtexception;
/*-
 * #%L
 * JOpt TourOptimizer Examples
 * %%
 * Copyright (C) 2017 - 2023 DNA Evolutions GmbH
 * %%
 * This file is subject to the terms and conditions defined in file 'src/main/resources/LICENSE.txt',
 * which is part of this repository.
 *
 * If not, see <https://www.dna-evolutions.com/>.
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
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.dna.jopt.framework.body.IOptimization;
import com.dna.jopt.framework.body.Optimization;
import com.dna.jopt.framework.body.setup.DefaultOptimizationSetup;
import com.dna.jopt.framework.body.setup.IOptimizationSetup;
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
import com.dna.jopt.member.unit.resource.IResource;
import com.dna.jopt.touroptimizer.java.examples.ExampleLicenseHelper;
import com.dna.jopt.touroptimizer.java.examples.expert.uncaughtexception.customhandler.MyUncaughtExceptionHandler;

/**
 * Throws an exception in the legacy callback method onProgress, which JOpt runs as a subscription
 * to the progress events, and ends the run with that exception through a custom uncaught exception
 * handler.
 *
 * <p>Scenario: Jack from Aachen visits Koeln on May 6, 2020 (Europe/Berlin). The overridden
 * onProgress(IOptimizationProgress) prints each progress event, but throws an ArithmeticException
 * in about one of three calls.
 *
 * <p>Look for: the log line of JOpt's handler, the error event UNCAUGHT_EXCEPTION_IN_OPTIMIZATION,
 * the two lines of MyUncaughtExceptionHandler and the line "The run failed:" with the exception.
 * How many progress lines come first changes between runs. The example catches the failed wait and
 * ends normally.
 *
 * <p>Prerequisites: None: without a license file the example uses the public evaluation license
 * (see ExampleLicenseHelper).
 *
 * @see <a href="https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/uncaughtexception/UncaughtExceptionHandling.md">UncaughtExceptionHandling.md</a>
 */
public class UncaughtExceptionHandlingRxJavaExample extends Optimization {

  /**
   * Creates the optimization with a setup of its own.
   *
   * @param setup the setup with the custom uncaught exception handler
   */
  public UncaughtExceptionHandlingRxJavaExample(IOptimizationSetup setup) {
    super(setup);
  }

  /**
   * Runs the example.
   *
   * @param args not used
   * @throws InterruptedException if the wait for the result is interrupted
   * @throws TimeoutException if the run does not finish within the wait
   * @throws InvalidLicenceException if the license is not valid
   * @throws IOException if a file cannot be read or written
   */
  public static void main(String[] args)
      throws InterruptedException, TimeoutException, InvalidLicenceException, IOException {
    new UncaughtExceptionHandlingRxJavaExample(createSetup()).example();
  }

  @Override
  public String toString() {
    return "Throws an exception in the legacy callback method onProgress, which JOpt runs as a"
        + " subscription to the progress events, and ends the run with that exception through a"
        + " custom uncaught exception handler.";
  }

  /**
   * Prints the progress of the run, but throws an ArithmeticException in about one of three calls.
   *
   * <p>This legacy callback method is the fault source of the example. JOpt runs it as a
   * subscription to the progress events. An exception in a subscription without an error consumer
   * would become an OnErrorNotImplementedException for the global RxJava error handler; JOpt
   * subscribes this method with an error consumer that calls the uncaught exception handler of the
   * setup, so the exception reaches MyUncaughtExceptionHandler.
   *
   * @param progress the progress of the run
   */
  @Override
  public void onProgress(IOptimizationProgress progress) {
    // The fault: a throw in about one of three progress events ends the run soon, after a number
    // of progress lines that changes between runs.
    int randomNum = ThreadLocalRandom.current().nextInt(0, 3);
    if (randomNum == 0) {
      throw new ArithmeticException("Test Exception from overridden onProgress()");
    }

    System.out.println(progress.getProgressString());
  }

  /**
   * Builds the optimization, runs it and prints the result or the exception that ended the run.
   *
   * @throws InterruptedException if the wait for the result is interrupted
   * @throws TimeoutException if the run does not finish within the wait
   * @throws InvalidLicenceException if the license is not valid
   * @throws IOException if a file cannot be read or written
   */
  public void example()
      throws InterruptedException, TimeoutException, InvalidLicenceException, IOException {
    // The public evaluation license, or your own license if you put a license file where
    // ExampleLicenseHelper looks for it.
    ExampleLicenseHelper.setLicense(this);
    addProperties(this);
    addNodes(this);
    addResources(this);
    attachToObservables(this);
    startAndPresentResult(this);
  }

  /**
   * Creates the default setup with MyUncaughtExceptionHandler as its handler for uncaught
   * exceptions.
   *
   * @return the setup
   */
  private static IOptimizationSetup createSetup() {
    IOptimizationSetup setup = new DefaultOptimizationSetup();
    // JOpt wraps the handler in its own handler, which logs the exception, streams the error event
    // UNCAUGHT_EXCEPTION_IN_OPTIMIZATION and then calls MyUncaughtExceptionHandler.
    setup.setSlaveUncaughtExceptionHandler(new MyUncaughtExceptionHandler());
    return setup;
  }

  /**
   * Adds the properties that keep the run short.
   *
   * @param opti the optimization
   */
  private static void addProperties(IOptimization opti) {
    Properties props = new Properties();
    // Fewer generations and iterations than the defaults keep the run short.
    props.setProperty("JOptExitCondition.JOptGenerationCount", "1000");
    props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumIterations", "100000");
    opti.addElement(props);
  }

  /**
   * Adds one node.
   *
   * @param opti the optimization
   */
  private static void addNodes(IOptimization opti) {
    List<IOpeningHours> openingHours = new ArrayList<>();
    openingHours.add(
        new OpeningHours(
            ZonedDateTime.of(2020, MAY.getValue(), 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 6, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    // Each visit takes 20 minutes and counts toward the working time, like the driving.
    Duration visitDuration = Duration.ofMinutes(20);

    // The same importance for every node, so no node is preferred over another.
    int importance = 1;

    INode koeln =
        new TimeWindowGeoNode("Koeln", 50.9333, 6.95, openingHours, visitDuration, importance);
    opti.addElement(koeln);
  }

  /**
   * Adds Jack, who starts and ends his working day in Aachen.
   *
   * @param opti the optimization
   */
  private static void addResources(IOptimization opti) {
    List<IWorkingHours> workingHours = new ArrayList<>();
    workingHours.add(
        new WorkingHours(
            ZonedDateTime.of(2020, MAY.getValue(), 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 6, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    // Sample limits of 13 h and 1200 km per day: the run ends at the exception, before a result.
    Duration maxWorkingTime = Duration.ofHours(13);
    Quantity<Length> maxDistanceKmW = Quantities.getQuantity(1200.0, KILO(METRE));

    IResource jack =
        new CapacityResource(
            "Jack from Aachen", 50.775346, 6.083887, maxWorkingTime, maxDistanceKmW, workingHours);
    opti.addElement(jack);
  }

  /**
   * Prints the warning, status and error events of the run; the overridden onProgress prints the
   * progress.
   *
   * @param opti the optimization
   */
  private static void attachToObservables(IOptimization opti) {
    // Subscribe before the start: the subjects replay only a few past events.
    opti.getOptimizationEvents().warningSubject().subscribe(w -> System.out.println(w.toString()));
    opti.getOptimizationEvents().statusSubject().subscribe(s -> System.out.println(s.toString()));
    // The exception of onProgress arrives here as UNCAUGHT_EXCEPTION_IN_OPTIMIZATION.
    opti.getOptimizationEvents().errorSubject().subscribe(e -> System.out.println(e.toString()));
  }

  /**
   * Starts the run, waits for the result and prints it, or prints the exception that ended the
   * run.
   *
   * @param opti the optimization
   * @throws InterruptedException if the wait for the result is interrupted
   * @throws TimeoutException if the run does not finish within the wait
   * @throws InvalidLicenceException if the license is not valid
   */
  private static void startAndPresentResult(IOptimization opti)
      throws InterruptedException, TimeoutException, InvalidLicenceException {
    CompletableFuture<IOptimizationResult> resultFuture = opti.startRunAsync();
    try {
      // Wait for the result; without the wait the JVM would end before the run does.
      IOptimizationResult result = resultFuture.get(5, TimeUnit.MINUTES);
      System.out.println(result);
    } catch (ExecutionException e) {
      // MyUncaughtExceptionHandler completed the result future with the exception, so the wait
      // ends with it. The example prints the cause and ends normally.
      System.out.println("The run failed: " + e.getCause());
    }
  }
}
