package com.dna.jopt.touroptimizer.java.examples.expert.uncaughtexception;
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
import static java.time.Month.MAY;

import tec.units.ri.quantity.Quantities;

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
import com.dna.jopt.touroptimizer.java.examples.ExampleLicenseHelper;
import com.dna.jopt.touroptimizer.java.examples.expert.uncaughtexception.customhandler.MyUncaughtExceptionHandler;

import io.reactivex.rxjava3.functions.Consumer;


/**
 * We implement some evil code within a custom onProgress subscription. Further, we define an
 * errorConsumer based on a custom uncaught exception handler and attach it during subscription
 * (MyUncaughtExceptionHandler).
 *
 * @author jrich
 * @version Apr 6, 2020
 * @since Apr 6, 2020
 */
public class UncaughtExceptionHandlingRxJavaWithCustomErrorConsumer extends Optimization {

  /**
   * The main method.
   *
   * @param args the arguments
   * @throws InterruptedException the interrupted exception
   * @throws ExecutionException the execution exception
   * @throws InvalidLicenceException the invalid licence exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static void main(String[] args)
      throws InterruptedException, ExecutionException, InvalidLicenceException, IOException {
    new UncaughtExceptionHandlingRxJavaWithCustomErrorConsumer().example();
  }

  public String toString() {
    return "An attached uncaught exception handler will be used to create an errorConsumer"
        + " for a custom subscription to the progress.";
  }

  /**
   * Method which executes the necessary parts for the optimization.
   *
   * @throws InterruptedException the interrupted exception
   * @throws ExecutionException the execution exception
   * @throws InvalidLicenceException the invalid licence exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public void example()
      throws InterruptedException, ExecutionException, InvalidLicenceException, IOException {

    ExampleLicenseHelper.setLicense(this);

    UncaughtExceptionHandlingRxJavaWithCustomErrorConsumer.addProperties(this);

    // Adding nodes
    UncaughtExceptionHandlingRxJavaWithCustomErrorConsumer.addNodes(this);

    // Adding resources
    UncaughtExceptionHandlingRxJavaWithCustomErrorConsumer.addResources(this);

    // subscribe/Attach to observables using an error consumer
    UncaughtExceptionHandlingRxJavaWithCustomErrorConsumer.attach2ProgressWithErrorConsumer(this);

    // Starting the optimization and presenting the result
    UncaughtExceptionHandlingRxJavaWithCustomErrorConsumer.startAndPresentResult(this);
  }

  /**
   * Attach 2 progress with error consumer.
   *
   * @param opti the opti
   */
  private static void attach2ProgressWithErrorConsumer(IOptimization opti) {
    // Create a custom UncaughtExceptionHandler
    MyUncaughtExceptionHandler myUncaughtExceptionHandler = new MyUncaughtExceptionHandler();

    // We attach the optimization to the handler
    myUncaughtExceptionHandler.attachOptimization(opti);

    // We create an error consumer based on our UncaughtExceptionHandler
    Consumer<? super Throwable> errorConsumer =
        t -> myUncaughtExceptionHandler.uncaughtException(Thread.currentThread(), t);

    // Attach to Observables with custom error consumer
    UncaughtExceptionHandlingRxJavaWithCustomErrorConsumer.attachToObservables(opti, errorConsumer);
  }

  /**
   * Attach to different events (observables) of the optimization instance.
   *
   * @param opti the optimization instance
   * @param errorConsumer the error consumer
   */
  private static void attachToObservables(
      IOptimization opti, Consumer<? super Throwable> errorConsumer) {

    opti.getOptimizationEvents()
        .progressSubject()
        .subscribe(
            p -> {

              // Some evil code
              int randomNum = ThreadLocalRandom.current().nextInt(0, 3);
              if (randomNum == 0) {
                throw new ArithmeticException("Test Exception from overridden subscribed progress");
              }

              System.out.println(p.getProgressString());
            },
            errorConsumer);
  }

  /**
   * Start the optimization and present the result.
   *
   * @param opti the optimization instance
   * @throws InvalidLicenceException the invalid licence exception
   * @throws InterruptedException the interrupted exception
   * @throws ExecutionException the execution exception
   */
  private static void startAndPresentResult(IOptimization opti)
      throws InvalidLicenceException, InterruptedException, ExecutionException {
    // Extracting a completable future for the optimization result
    CompletableFuture<IOptimizationResult> resultFuture = opti.startRunAsync();

    // It is important to block the call, otherwise optimization will be terminated
    IOptimizationResult result = resultFuture.get();

    // Presenting the result
    System.out.println(result);
  }

  /**
   * Adds the properties to the optimization.
   *
   * @param opti the optimization instance
   */
  private static void addProperties(IOptimization opti) {

    Properties props = new Properties();

    props.setProperty("JOptExitCondition.JOptGenerationCount", "2000");
    props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumIterations", "10000");
    props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumRepetions", "1");
    props.setProperty("JOpt.NumCPUCores", "4");

    opti.addElement(props);
  }

  /**
   * Adds the nodes to the optimization.
   *
   * @param opti the optimization instance
   */
  private static void addNodes(IOptimization opti) {

    List<IOpeningHours> weeklyOpeningHours = new ArrayList<>();
    weeklyOpeningHours.add(
        new OpeningHours(
            ZonedDateTime.of(2020, MAY.getValue(), 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 6, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    Duration visitDuration = Duration.ofMinutes(20);

    int importance = 1;

    // Define some nodes
    INode koeln =
        new TimeWindowGeoNode(
            "Koeln", 50.9333, 6.95, weeklyOpeningHours, visitDuration, importance);
    opti.addElement(koeln);
  }

  /**
   * Adds the resources to the optimization.
   *
   * @param opti the optimization instance
   */
  private static void addResources(IOptimization opti) {

    List<IWorkingHours> workingHours = new ArrayList<>();
    workingHours.add(
        new WorkingHours(
            ZonedDateTime.of(2020, MAY.getValue(), 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 6, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    Duration maxWorkingTime = Duration.ofHours(9);
    Quantity<Length> maxDistanceKmW = Quantities.getQuantity(1200.0, KILO(METRE));

    IResource jack =
        new CapacityResource(
            "Jack from Aachen", 50.775346, 6.083887, maxWorkingTime, maxDistanceKmW, workingHours);
    opti.addElement(jack);
  }
}
