package com.dna.jopt.touroptimizer.java.examples.expert.optimizationscheme.selectalgorithms;
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
import static java.time.Month.MAY;
import static javax.measure.MetricPrefix.KILO;
import static tech.units.indriya.unit.Units.METRE;

import java.io.IOException;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.measure.Quantity;
import javax.measure.quantity.Length;

import com.dna.jopt.framework.body.IOptimization;
import com.dna.jopt.framework.body.Optimization;
import com.dna.jopt.framework.body.scheme.DefaultOptimizationScheme;
import com.dna.jopt.framework.body.scheme.IOptimizationScheme;
import com.dna.jopt.framework.body.scheme.helper.AbstractConstructionOptimizationAlgorithmConfig.ConstructionOptimizationAlgorithm;
import com.dna.jopt.framework.body.scheme.helper.AbstractHeuristicOptimizationAlgorithmConfig.OptimizationAlgorithmConfig;
import com.dna.jopt.framework.body.scheme.helper.ConstructionOptimizationAlgorithmConfig;
import com.dna.jopt.framework.body.scheme.helper.HeuristicOptimizationAlgorithmConfig;
import com.dna.jopt.framework.exception.caught.InvalidLicenceException;
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

import tech.units.indriya.quantity.Quantities;

/**
 * Selects the construction algorithm and the optimization phases of a run with a custom
 * optimization scheme.
 *
 * <p>Scenario: the data of the first optimization example: Jack from Aachen visits Koeln, Essen,
 * Dueren, Nuernberg, Heilbronn, Wuppertal and Aachen on May 6 and May 7, 2020, from 08:00 to 17:00
 * (Europe/Berlin). The scheme runs the smart construction, a short simulated annealing phase, a
 * longer simulated annealing phase and the genetic phase, in this order.
 *
 * <p>Look for: the status lines that name the algorithms as they start, the progress lines of
 * each phase, and the result: one route per day, and on the day with Nuernberg and Heilbronn a
 * MAXTIME violation with the overtime in hours.
 *
 * <p>Prerequisites: None: without a license file the example uses the public evaluation license
 * (see ExampleLicenseHelper).
 *
 * @see <a href="https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/optimizationscheme/OptimizationSchemeAlgorithmSelection.md">OptimizationSchemeAlgorithmSelection.md</a>
 */
public class OptimizationSchemeAlgorithmSelectionExample extends Optimization {

  /**
   * Runs the example.
   *
   * @param args not used
   * @throws InterruptedException if the wait for the result is interrupted
   * @throws ExecutionException if the optimization run fails
   * @throws TimeoutException if the run does not finish within the wait
   * @throws InvalidLicenceException if the license is not valid
   * @throws IOException if a file cannot be read or written
   */
  public static void main(String[] args)
      throws InterruptedException, ExecutionException, TimeoutException, InvalidLicenceException,
      IOException {
    new OptimizationSchemeAlgorithmSelectionExample().example();
  }

  @Override
  public String toString() {
    return "Selects the construction algorithm and the optimization phases of a run with a custom"
        + " optimization scheme.";
  }

  /**
   * Builds the optimization, runs it and prints the result.
   *
   * @throws InterruptedException if the wait for the result is interrupted
   * @throws ExecutionException if the optimization run fails
   * @throws TimeoutException if the run does not finish within the wait
   * @throws InvalidLicenceException if the license is not valid
   * @throws IOException if a file cannot be read or written
   */
  public void example()
      throws InterruptedException, ExecutionException, TimeoutException, InvalidLicenceException,
      IOException {
    // The public evaluation license, or your own license if you put a license file where
    // ExampleLicenseHelper looks for it.
    ExampleLicenseHelper.setLicense(this);
    setScheme(this);
    addProperties(this);
    addNodes(this);
    addResources(this);
    attachToObservables(this);
    startAndPresentResult(this);
  }

  /**
   * Sets a scheme with a selected construction algorithm and three optimization phases.
   *
   * @param opti the optimization
   */
  private static void setScheme(IOptimization opti) {
    // The smart construction builds the starting solution.
    ConstructionOptimizationAlgorithmConfig constructionAlgo =
        ConstructionOptimizationAlgorithmConfig.builder()
            .algorithm(ConstructionOptimizationAlgorithm.SMART_CONSTR_ALGO)
            .build();

    List<HeuristicOptimizationAlgorithmConfig> heuristicAlgoConfigs = new ArrayList<>();

    // A short simulated annealing phase first: the override of 1000 iterations replaces the
    // property JOpt.Algorithm.PreOptimization.SA.NumIterations for this phase only.
    // hasAutoFilter(false) leaves the AutoFilter out of this phase, whatever the properties say.
    HeuristicOptimizationAlgorithmConfig heuristicAlgoZero =
        HeuristicOptimizationAlgorithmConfig.builder()
            .algorithm(OptimizationAlgorithmConfig.SMART_SIMULATED_ANNEALING_ALGO)
            .simulatedAnnealingOverrideNumIterationsValue(1000)
            .hasAutoFilter(false)
            .build();

    // A longer simulated annealing phase: without an override, its iterations come from the
    // property JOpt.Algorithm.PreOptimization.SA.NumIterations.
    HeuristicOptimizationAlgorithmConfig heuristicAlgoOne =
        HeuristicOptimizationAlgorithmConfig.builder()
            .algorithm(OptimizationAlgorithmConfig.SMART_SIMULATED_ANNEALING_ALGO)
            .hasAutoFilter(false)
            .build();

    // The genetic phase last. hasAutoFilter(true) attaches the AutoFilter to it; the filter acts
    // only when the property JOpt.AutoFilter or JOpt.SelectiveAutoFilter is true, and this
    // example sets neither.
    HeuristicOptimizationAlgorithmConfig heuristicAlgoTwo =
        HeuristicOptimizationAlgorithmConfig.builder()
            .algorithm(OptimizationAlgorithmConfig.GENETIC_EVOLUTION_ALGO)
            .hasAutoFilter(true)
            .build();

    heuristicAlgoConfigs.add(heuristicAlgoZero);
    heuristicAlgoConfigs.add(heuristicAlgoOne);
    heuristicAlgoConfigs.add(heuristicAlgoTwo);

    // Without a construction config the scheme uses its default construction, and without a list
    // its default phases. A present but empty list runs the construction only.
    IOptimizationScheme scheme =
        new DefaultOptimizationScheme(
            opti, Optional.of(constructionAlgo), Optional.of(heuristicAlgoConfigs));

    opti.setOptimizationScheme(scheme);
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
   * Adds seven nodes with the same opening hours and visit duration.
   *
   * @param opti the optimization
   */
  private static void addNodes(IOptimization opti) {
    List<IOpeningHours> openingHours = new ArrayList<>();
    openingHours.add(
        new OpeningHours(
            ZonedDateTime.of(2020, MAY.getValue(), 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 6, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));
    openingHours.add(
        new OpeningHours(
            ZonedDateTime.of(2020, MAY.getValue(), 7, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 7, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    // Each visit takes 20 minutes and counts toward the working time, like the driving.
    Duration visitDuration = Duration.ofMinutes(20);

    // The same importance for every node, so no node is preferred over another.
    int importance = 1;

    INode koeln =
        new TimeWindowGeoNode("Koeln", 50.9333, 6.95, openingHours, visitDuration, importance);
    opti.addElement(koeln);

    INode essen =
        new TimeWindowGeoNode("Essen", 51.45, 7.01667, openingHours, visitDuration, importance);
    opti.addElement(essen);

    INode dueren =
        new TimeWindowGeoNode("Dueren", 50.8, 6.48333, openingHours, visitDuration, importance);
    opti.addElement(dueren);

    INode nuernberg =
        new TimeWindowGeoNode(
            "Nuernberg", 49.4478, 11.0683, openingHours, visitDuration, importance);
    opti.addElement(nuernberg);

    INode heilbronn =
        new TimeWindowGeoNode(
            "Heilbronn", 49.1403, 9.22, openingHours, visitDuration, importance);
    opti.addElement(heilbronn);

    INode wuppertal =
        new TimeWindowGeoNode(
            "Wuppertal", 51.2667, 7.18333, openingHours, visitDuration, importance);
    opti.addElement(wuppertal);

    INode aachen =
        new TimeWindowGeoNode(
            "Aachen", 50.775346, 6.083887, openingHours, visitDuration, importance);
    opti.addElement(aachen);
  }

  /**
   * Adds Jack, who starts and ends each working day in Aachen.
   *
   * @param opti the optimization
   */
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

    // 9 h per day, the length of the shift: the day with Nuernberg exceeds it (overtime).
    Duration maxWorkingTime = Duration.ofHours(9);
    // 1200 km per day, more than the longest day of this plan needs.
    Quantity<Length> maxDistanceKmW = Quantities.getQuantity(1200.0, KILO(METRE));

    IResource jack =
        new CapacityResource(
            "Jack from Aachen", 50.775346, 6.083887, maxWorkingTime, maxDistanceKmW, workingHours);
    opti.addElement(jack);
  }

  /**
   * Prints the progress, warning, status and error events of the run.
   *
   * @param opti the optimization
   */
  private static void attachToObservables(IOptimization opti) {
    // Subscribe before the start: the subjects replay only a few past events.
    opti.getOptimizationEvents()
        .progressSubject()
        .subscribe(p -> System.out.println(p.getProgressString()));
    opti.getOptimizationEvents().warningSubject().subscribe(w -> System.out.println(w.toString()));
    opti.getOptimizationEvents().statusSubject().subscribe(s -> System.out.println(s.toString()));
    opti.getOptimizationEvents().errorSubject().subscribe(e -> System.out.println(e.toString()));
  }

  /**
   * Starts the run, waits for the result and prints it.
   *
   * @param opti the optimization
   * @throws InterruptedException if the wait for the result is interrupted
   * @throws ExecutionException if the optimization run fails
   * @throws TimeoutException if the run does not finish within the wait
   * @throws InvalidLicenceException if the license is not valid
   */
  private static void startAndPresentResult(IOptimization opti)
      throws InterruptedException, ExecutionException, TimeoutException, InvalidLicenceException {
    CompletableFuture<IOptimizationResult> resultFuture = opti.startRunAsync();
    // Wait for the result; without the wait the JVM would end before the run does.
    IOptimizationResult result = resultFuture.get(5, TimeUnit.MINUTES);
    System.out.println(result);
  }
}
