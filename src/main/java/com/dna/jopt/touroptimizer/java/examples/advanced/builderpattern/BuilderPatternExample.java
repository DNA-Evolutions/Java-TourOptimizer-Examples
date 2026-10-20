package com.dna.jopt.touroptimizer.java.examples.advanced.builderpattern;
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

import java.io.IOException;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
 * Builds a complete optimization config with immutable builders and runs it with
 * JSONOptimization.
 *
 * <p>Scenario: Jack from Aachen visits Koeln and Essen on May 6, 2020. The nodes are open and
 * Jack works from 08:00 to 17:00 (Europe/Berlin).
 *
 * <p>Look for: the result config printed as JSON, then the text solution of the result, then the
 * solution header, the route header and the element details read from the result config.
 *
 * <p>Prerequisites: None: the example sets no license, and JOpt runs in its free mode, which these
 * three elements fit (see the license guide).
 *
 * @see <a href="https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/builderpattern/BuilderPatternExample.md">BuilderPatternExample.md</a>
 */
public class BuilderPatternExample {

  // The timeout of the config, a sample value, stored in its extension. The printed JSON carries
  // it; the run of this example ends long before.
  private static final Duration TIMEOUT_OPTIMIZATION = Duration.ofMinutes(10);

  /**
   * Runs the example.
   *
   * @param args not used
   * @throws InterruptedException if the wait for the result is interrupted
   * @throws ExecutionException if the optimization run fails
   * @throws TimeoutException if the run does not finish within the wait
   * @throws InvalidLicenceException if the license is not valid
   * @throws IOException if a file cannot be read or written
   * @throws ConvertException if the config cannot be converted
   * @throws SerializationException if the config cannot be serialized
   */
  public static void main(String[] args)
      throws InterruptedException, ExecutionException, TimeoutException, InvalidLicenceException,
      IOException, ConvertException, SerializationException {
    new BuilderPatternExample().example();
  }

  @Override
  public String toString() {
    return "Builds a complete optimization config with immutable builders and runs it with"
        + " JSONOptimization.";
  }

  /**
   * Builds the config, runs it and prints the result in three forms.
   *
   * @throws InterruptedException if the wait for the result is interrupted
   * @throws ExecutionException if the optimization run fails
   * @throws TimeoutException if the run does not finish within the wait
   * @throws InvalidLicenceException if the license is not valid
   * @throws IOException if a file cannot be read or written
   * @throws ConvertException if the config cannot be converted
   * @throws SerializationException if the config cannot be serialized
   */
  public void example()
      throws InterruptedException, ExecutionException, TimeoutException, InvalidLicenceException,
      IOException, ConvertException, SerializationException {
    OptimizationConfig<JSONConfig> config = createConfig();

    // JSONOptimization runs a config object instead of elements added one by one.
    IJSONOptimization opti = new JSONOptimization();
    attachToObservables(opti);

    OptimizationConfig<JSONConfig> result = startAndPresentResult(opti, config);
    printSolution(result);
  }

  /**
   * Creates the config that defines the whole optimization: nodes, resources, extension and
   * properties.
   *
   * @return the immutable config
   */
  private static OptimizationConfig<JSONConfig> createConfig() {
    return OptimizationConfig.<JSONConfig>builder()
        .addAllNodes(createNodes())
        .addAllResources(createResources())
        .extension(createJSONExtension())
        .optimizationOptions(createOptimizationOptions())
        .build();
  }

  /**
   * Creates the extension of the config with the timeout and, if present, the license.
   *
   * @return the extension
   */
  private static JSONConfig createJSONExtension() {
    // To use your own license, put its JSON string here: Optional.of("YOUR JSON LICENSE").
    // Without a license JOpt runs in its free mode; an invalid license makes the run fail.
    Optional<String> myJSONLicenseOpt = Optional.empty();

    if (myJSONLicenseOpt.isPresent()) {
      OptimizationKeySetting keySetting = OptimizationKeySetting.of(myJSONLicenseOpt.get());
      return JSONConfig.builder().keySetting(keySetting).timeOut(TIMEOUT_OPTIMIZATION).build();
    }
    return JSONConfig.builder().timeOut(TIMEOUT_OPTIMIZATION).build();
  }

  /**
   * Creates the properties of the run.
   *
   * @return the properties as optimization options
   */
  private static OptimizationOptions createOptimizationOptions() {
    Map<String, String> propMap = new LinkedHashMap<>();
    // Fewer generations and iterations than the defaults keep the run short.
    propMap.put("JOptExitCondition.JOptGenerationCount", "1000");
    propMap.put("JOpt.Algorithm.PreOptimization.SA.NumIterations", "100000");

    return OptimizationOptions.of(propMap);
  }

  /**
   * Creates the working hours of Jack on May 6, with their own limits for this day.
   *
   * @return the working hours
   */
  private static WorkingHours createWorkingHours() {
    ZonedDateTime begin =
        ZonedDateTime.of(2020, MAY.getValue(), 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin"));
    ZonedDateTime end =
        ZonedDateTime.of(2020, MAY.getValue(), 6, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"));

    // Limits set on the working hours take precedence over the limits of the resource for this
    // day: 10 h instead of the resource's 9 h. The 1000 km are more than this day needs.
    return WorkingHours.builder()
        .begin(begin.toInstant())
        .end(end.toInstant())
        .zoneId(begin.getZone())
        .maxTime(Duration.ofHours(10))
        .maxDistance(Quantities.getQuantity(1000, KILO(METRE)))
        .build();
  }

  /**
   * Creates the opening hours of a node on May 6.
   *
   * @return the opening hours
   */
  private static OpeningHours createOpeningHours() {
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
   * Creates the nodes Koeln and Essen.
   *
   * @return the nodes
   */
  private static ImmutableList<Node> createNodes() {
    return ImmutableList.of(
        Node.builder()
            .id("Koeln")
            .type(GeoNode.of(Position.of(50.9333, 6.95)))
            .visitDuration(Duration.ofMinutes(20))
            .addOpeningHours(createOpeningHours())
            .build(),
        Node.builder()
            .id("Essen")
            .type(GeoNode.of(Position.of(51.45, 7.01667)))
            .visitDuration(Duration.ofMinutes(20))
            .addOpeningHours(createOpeningHours())
            .build());
  }

  /**
   * Creates Jack, who starts and ends his day in Aachen.
   *
   * @return the resources
   */
  private static ImmutableList<Resource> createResources() {
    // The resource needs its own limits. They apply to working hours without limits of their
    // own; the one working day of this example sets its own (see createWorkingHours).
    Duration maxWorkingTime = Duration.ofHours(9);
    Quantity<Length> maxDistance = Quantities.getQuantity(1000, KILO(METRE));

    return ImmutableList.of(
        Resource.builder()
            .id("Jack from Aachen")
            .type(CapacityResource.of())
            .position(Position.of(50.775346, 6.083887))
            .maxTime(maxWorkingTime)
            .maxDistance(maxDistance)
            .addWorkingHours(createWorkingHours())
            .build());
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
   * Starts the run, waits for the result, prints it and returns it.
   *
   * <p>The result is a config again: the input plus the solution. It prints first as JSON, the
   * form that CreateRestTourOptimizerInputWithSolutionExample also prints, then as the text
   * solution known from the other examples.
   *
   * @param opti the optimization
   * @param config the config to run
   * @return the result of the run
   * @throws InterruptedException if the wait for the result is interrupted
   * @throws ExecutionException if the optimization run fails
   * @throws TimeoutException if the run does not finish within the wait
   * @throws InvalidLicenceException if the license is not valid
   * @throws IOException if a file cannot be read or written
   * @throws ConvertException if the config cannot be converted
   * @throws SerializationException if the config cannot be serialized
   */
  private static OptimizationConfig<JSONConfig> startAndPresentResult(
      IJSONOptimization opti, OptimizationConfig<JSONConfig> config)
      throws InterruptedException, ExecutionException, TimeoutException, InvalidLicenceException,
      IOException, ConvertException, SerializationException {
    CompletableFuture<OptimizationConfig<JSONConfig>> resultFuture =
        opti.startAsynchConfigFuture(config, Optional.empty());
    // Wait for the result; without the wait the JVM would end before the run does.
    OptimizationConfig<JSONConfig> result = resultFuture.get(5, TimeUnit.MINUTES);

    System.out.println(JSONOptimization.asJSON(result));
    System.out.println();

    result
        .extension()
        .flatMap(JSONConfig::textSolution)
        .ifPresent(t -> System.out.println(t.textSolution()));
    System.out.println();

    return result;
  }

  /**
   * Prints the solution header, the route headers and the element details of the result, the
   * parts an application reads to show the result in its own layer.
   *
   * @param result the result of the run
   */
  private static void printSolution(OptimizationConfig<JSONConfig> result) {
    result
        .solution()
        .ifPresent(
            s -> {
              Optional<SolutionHeader> solutionHeaderOpt = s.header();
              solutionHeaderOpt.ifPresent(System.out::println);

              s.routes()
                  .forEach(
                      r -> {
                        Optional<RouteHeader> routeHeaderOpt = r.header();
                        routeHeaderOpt.ifPresent(System.out::println);

                        ImmutableList<RouteElementDetail> elementDetails = r.elementDetails();
                        elementDetails.forEach(System.out::println);
                      });
            });
  }
}
