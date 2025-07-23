package com.dna.jopt.touroptimizer.java.examples.advanced.builderpattern;
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
import static java.time.Month.MAY;

import tech.units.indriya.quantity.Quantities;

import java.io.IOException;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import javax.measure.Quantity;
import javax.measure.quantity.Length;

import com.dna.jopt.config.convert.ConvertException;
import com.dna.jopt.config.json.framework.IJSONOptimization;
import com.dna.jopt.config.json.framework.JSONOptimization;
import com.dna.jopt.config.json.types.JSONConfig;
import com.dna.jopt.config.json.types.OptimizationKeySetting;
import com.dna.jopt.config.serialize.SerializationException;
import com.dna.jopt.config.types.CapacityResource;
import com.dna.jopt.config.types.GeoNode;
import com.dna.jopt.config.types.Node;
import com.dna.jopt.config.types.OpeningHours;
import com.dna.jopt.config.types.OptimizationConfig;
import com.dna.jopt.config.types.OptimizationOptions;
import com.dna.jopt.config.types.Position;
import com.dna.jopt.config.types.Resource;
import com.dna.jopt.config.types.RouteElementDetail;
import com.dna.jopt.config.types.RouteHeader;
import com.dna.jopt.config.types.SolutionHeader;
import com.dna.jopt.config.types.WorkingHours;
import com.dna.jopt.framework.body.IOptimization;
import com.dna.jopt.framework.exception.caught.InvalidLicenceException;
import com.google.common.collect.ImmutableList;

/**
 * The Class BuilderPatternExample. Use a builder to create an Optimization and launch it via
 * JSONOptimization engine.
 *
 * @author jrich
 * @version Jun 5, 2021
 * @since Jun 5, 2021
 */
public class BuilderPatternExample {

  /** The Constant TIMEOUT_OPTIMIZATION. */
  private static final Duration TIMEOUT_OPTIMIZATION = Duration.ofMinutes(10);

  /**
   * The main method.
   *
   * @param args the arguments
   * @throws InterruptedException the interrupted exception
   * @throws ExecutionException the execution exception
   * @throws InvalidLicenceException the invalid licence exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ConvertException the convert exception
   * @throws SerializationException the serialization exception
   */
  public static void main(String[] args)
      throws InterruptedException, ExecutionException, InvalidLicenceException, IOException,
          ConvertException, SerializationException {
    new BuilderPatternExample().example();
  }

  /**
   * To string.
   *
   * @return the string
   */
  public String toString() {
    return "Use a builder to create an Optimization and launch it via JSONOptimization engine.";
  }

  /**
   * Example.
   *
   * @throws InterruptedException the interrupted exception
   * @throws ExecutionException the execution exception
   * @throws InvalidLicenceException the invalid licence exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ConvertException the convert exception
   * @throws SerializationException the serialization exception
   */
  public void example()
      throws InterruptedException, ExecutionException, InvalidLicenceException, IOException,
          ConvertException, SerializationException {

    // 1) Create a config object that is defining the whole optimization
    OptimizationConfig<JSONConfig> myConfig =
        OptimizationConfig.<JSONConfig>builder()
            .addAllNodes(createNodes())
            .addAllResources(createResources())
            .extension(createJSONExtension())
            .optimizationOptions(createOptimizationOptions())
            .build();

    // 2) Use the JSONOptimization as engine to run the defined config
    IJSONOptimization myOpti = new JSONOptimization();

    // 3) Attach to the Observables to see progress, errors, etc.
    attachToObservables(myOpti);

    // 4) Start the Optimization by providing the immutable config object
    CompletableFuture<OptimizationConfig<JSONConfig>> resultFuture = 
	    myOpti.startAsynchConfigFuture(myConfig, Optional.empty());

    // 5) We call get() on the future to wait until the Optimization is done
    OptimizationConfig<JSONConfig> result = resultFuture.get();

    /*
     *
     * Print the Result:
     *
     * 1) The JSON version - please also see example CreateRestTourOptimizerInputExample.java
     *
     * 2) The human readable text solution
     *
     * 3) Parsing the result
     *
     */

    // 1) JSON - can be used as input for our swagger interface - see also
    // CreateRestTourOptimizerInputExample.java
    System.out.println(JSONOptimization.asJSON(result));
    System.out.println();

    // 2) The result you know from other examples
    result.extension().ifPresent(e -> System.out.println(e.textSolution()));
    System.out.println();

    // 3) For representing the result on your custom layer.
    result
        .solution()
        .ifPresent(
            s -> {
              Optional<SolutionHeader> solutionHeaderOpt = s.header();

              // The header of the solution
              solutionHeaderOpt.ifPresent(System.out::println);

              s.routes()
                  .forEach(
                      r -> {
                        Optional<RouteHeader> curRouteHeaderOpt = r.header();
                        curRouteHeaderOpt.ifPresent(System.out::println);

                        ImmutableList<RouteElementDetail> elementDetails = r.elementDetails();

                        elementDetails.forEach(System.out::println);
                      });
            });
  }

  /**
   * Creates the JSON extension.
   *
   * @return the JSON config
   */
  private static JSONConfig createJSONExtension() {

    // 1) Create an extension - Here we can store our license key if present, and we have to define
    // a timeout for the Optimization run

    /*
     * ATTENTION:
     * ==========
     *
     * If you have a valid JSON license, please add it wrapped in an Optional.of("YOUR JSON LIC")
     * instead of myJSONLicenseOpt = Optional.empty().
     * If not license is provided, JOpt will run in the free mode, with a maximum of ten elements.
     * Providing a non-valid license, will cause the Optimizer to throw an exception
     */
    Optional<String> myJSONLicenseOpt = Optional.empty();

    JSONConfig myExtension;

    if (myJSONLicenseOpt.isPresent()) {

      // Create a custom OptimizationKeySetting Object and attach license
      OptimizationKeySetting keySetting = OptimizationKeySetting.of(myJSONLicenseOpt.get());

      // Create a custom extension wrapping the OptimizationKeySetting and a desired timeOut for the
      // Optimization
      myExtension =
          JSONConfig.builder().keySetting(keySetting).timeOut(TIMEOUT_OPTIMIZATION).build();
    } else {
      // Create a custom extension wrapping the timeOut for the Optimization
      myExtension = JSONConfig.builder().timeOut(TIMEOUT_OPTIMIZATION).build();
    }

    return myExtension;
  }

  /**
   * Creates the workinghour.
   *
   * @return the working hours
   */
  private static WorkingHours createWorkinghour() {

    ZonedDateTime begin =
        ZonedDateTime.of(2020, MAY.getValue(), 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin"));
    ZonedDateTime end =
        ZonedDateTime.of(2020, MAY.getValue(), 6, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"));

    return WorkingHours.builder()
        .begin(begin.toInstant())
        .end(end.toInstant())
        .zoneId(begin.getZone())
        .maxTime(Duration.ofHours(10))
        .maxDistance(Quantities.getQuantity(1000, KILO(METRE)))
        .build();
  }

  /**
   * Creates the optimization options.
   *
   * @return the optimization options
   */
  private static OptimizationOptions createOptimizationOptions() {
    Map<String, String> propMap = new LinkedHashMap<>();

    propMap.put("JOptExitCondition.JOptGenerationCount", "2000");
    propMap.put("JOpt.Algorithm.PreOptimization.SA.NumIterations", "10000");
    propMap.put("JOpt.Algorithm.PreOptimization.SA.NumRepetions", "1");

    return OptimizationOptions.of(propMap);
  }

  /**
   * Creates the openinghour.
   *
   * @return the opening hours
   */
  private static OpeningHours createOpeninghour() {

    ZonedDateTime begin =
        ZonedDateTime.of(2020, MAY.getValue(), 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin"));
    ZonedDateTime end =
        ZonedDateTime.of(2020, MAY.getValue(), 6, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"));

    return OpeningHours.builder()
        .begin(begin.toInstant())
        .end(end.toInstant())
        .zoneId(begin.getZone())
        .build();
  }

  /**
   * Creates the nodes.
   *
   * @return the immutable list
   */
  private static ImmutableList<Node> createNodes() {

    return ImmutableList.of(
        Node.builder()
            .id("Koeln")
            .type(GeoNode.of(Position.of(50.9333, 6.95)))
            .visitDuration(Duration.ofMinutes(20))
            .addOpeningHours(createOpeninghour())
            .build(),
        Node.builder()
            .id("Essen")
            .type(GeoNode.of(Position.of(51.45, 7.01667)))
            .visitDuration(Duration.ofMinutes(20))
            .addOpeningHours(createOpeninghour())
            .build());
  }

  /**
   * Creates the resources.
   *
   * @return the immutable list
   */
  private static ImmutableList<Resource> createResources() {

    Quantity<Length> maxDistance = Quantities.getQuantity(1000, KILO(METRE));

    Position pos = Position.of(50.775346, 6.083887);

    Duration maxWorkingTime = Duration.ofHours(9);

    String id = "Jack from Aachen";

    return ImmutableList.of(
        Resource.builder()
            .id(id)
            .type(CapacityResource.of())
            .position(pos)
            .maxTime(maxWorkingTime)
            .maxDistance(maxDistance)
            .addWorkingHours(createWorkinghour())
            .build());
  }

  /**
   * Attach to different Events (Observables) of the optimization instance.
   *
   * @param opti the optimization instance
   */
  private static void attachToObservables(IOptimization opti) {

    opti.getOptimizationEvents()
        .progressSubject()
        .subscribe(
            p -> {
              System.out.println(p.getProgressString());
            });

    opti.getOptimizationEvents()
        .warningSubject()
        .subscribe(
            w -> {
              System.out.println(w.toString());
            });

    opti.getOptimizationEvents()
        .statusSubject()
        .subscribe(
            s -> {
              System.out.println(s.toString());
            });

    opti.getOptimizationEvents()
        .errorSubject()
        .subscribe(
            e -> {
              System.out.println(e.toString());
            });
  }
}
