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
import static java.time.Month.MAY;
import static tec.units.ri.unit.MetricPrefix.KILO;
import static tec.units.ri.unit.Units.METRE;

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

import javax.measure.Quantity;
import javax.measure.quantity.Length;

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
import com.dna.jopt.member.unit.node.geo.TimeWindowGeoNode;
import com.dna.jopt.member.unit.resource.CapacityResource;
import com.dna.jopt.touroptimizer.java.examples.ExampleLicenseHelper;
import com.dna.jopt.touroptimizer.java.examples.expert.uncaughtexception.customhandler.MyUncaughtExceptionHandler;

import tec.units.ri.quantity.Quantities;

/**
 * We implement some evil code within the onProgess callback. Internally, this callback is a
 * subscription to an RxJava subject. The evil code therefore is executed within a subscription. The
 * default, or in our case, the customly defined MyUncaughtExceptionHandler is automatically
 * attached to the internal subject.
 *
 * @author jrich
 * @version Feb 16, 2021
 * @since Feb 16, 2021
 */
public class UncaughtExceptionHandlingRxJavaExample {

  public static void main(String[] args)
      throws IOException, InvalidLicenceException, InterruptedException, ExecutionException {

    // Use the default setup and attach a custom uncaught exception handler
    IOptimizationSetup currentSetup = new DefaultOptimizationSetup();
    currentSetup.setSlaveUncaughtExceptionHandler(new MyUncaughtExceptionHandler());

    new UncaughtExceptionReactiveJavaHandlingOptimization(currentSetup);
  }

  public String toString() {
    return "An attached uncaught exception handler will stop the optimization accordingly after some evil code"
        + " is excuted within the onProgress subscription.";
  }
}

class UncaughtExceptionReactiveJavaHandlingOptimization extends Optimization {

  @Override
  public void onProgress(IOptimizationProgress progress) {

    // Lets create some evil code, that will trigger an ArithmeticException.
    // The overridden onProgress() method internally is a subscription to the
    // onProgressEvent, therefore an error occurring within this method will generate
    // a io.reactivex.exceptions.OnErrorNotImplementedException if not backed by an errorConsumer.
    // By default, when subscribing, the provided UncaughtException handler is used to create
    // an error consumer.

    int randomNum = ThreadLocalRandom.current().nextInt(0, 3);
    if (randomNum == 0) {
      throw new ArithmeticException("Test Exception from overridden onProgress()");
    }

    System.out.println(progress.getProgressString());
  }

  public UncaughtExceptionReactiveJavaHandlingOptimization(IOptimizationSetup customSetup)
      throws IOException, InvalidLicenceException, InterruptedException, ExecutionException {

    // Use the custom setup
    super(customSetup);

    // Set license via helper
    ExampleLicenseHelper.setLicense(this);

    // Properties!
    this.setProperties();

    this.addNodes();
    this.addResources();

    CompletableFuture<IOptimizationResult> resultFuture = this.startRunAsync();

    // It is important to block the call, otherwise optimization will be terminated
    resultFuture.get();
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
            ZonedDateTime.of(2020, MAY.getValue(), 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 6, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    Duration maxWorkingTime = Duration.ofHours(13);
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
            ZonedDateTime.of(2020, MAY.getValue(), 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 6, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    Duration visitDuration = Duration.ofMinutes(20);

    // Define some nodes
    TimeWindowGeoNode koeln =
        new TimeWindowGeoNode("Koeln", 50.9333, 6.95, weeklyOpeningHours, visitDuration, 1);
    this.addElement(koeln);
  }
}
