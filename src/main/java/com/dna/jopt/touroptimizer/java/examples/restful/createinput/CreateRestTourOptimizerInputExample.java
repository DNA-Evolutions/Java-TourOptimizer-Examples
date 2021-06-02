package com.dna.jopt.touroptimizer.java.examples.restful.createinput;

import static java.time.Month.MAY;
import static tec.units.ri.unit.MetricPrefix.KILO;
import static tec.units.ri.unit.Units.METRE;

import java.io.IOException;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import javax.measure.Quantity;
import javax.measure.quantity.Length;

import com.dna.jopt.config.convert.ConvertException;
import com.dna.jopt.config.json.framework.IJSONOptimization;
import com.dna.jopt.config.json.framework.JSONOptimization;
import com.dna.jopt.config.json.types.JSONConfig;
import com.dna.jopt.config.json.types.OptimizationKeySetting;
import com.dna.jopt.config.types.OptimizationConfig;
import com.dna.jopt.framework.body.IOptimization;
import com.dna.jopt.framework.exception.caught.InvalidLicenceException;
import com.dna.jopt.member.unit.hours.IOpeningHours;
import com.dna.jopt.member.unit.hours.IWorkingHours;
import com.dna.jopt.member.unit.hours.OpeningHours;
import com.dna.jopt.member.unit.hours.WorkingHours;
import com.dna.jopt.member.unit.node.INode;
import com.dna.jopt.member.unit.node.geo.TimeWindowGeoNode;
import com.dna.jopt.member.unit.resource.CapacityResource;
import com.dna.jopt.member.unit.resource.IResource;

import tec.units.ri.quantity.Quantities;

/**
 * The Class CreateRestTourOptimizerInputExample. Create test input for the JOpt-TourOptimizer
 * swagger interface. The constructed example input is valid to be used by the endpoints:
 *
 * <p>
 *
 * <ul>
 *   <li>/api/optimize/config/runOnlyResult
 *   <li>/api/optimize/config/run
 * </ul>
 *
 * <p>For production purpose, you should generate a client by using our <a
 * href="https://swagger.dna-evolutions.com/v3/api-docs/OptimizeConfig">swagger annotation</a>.
 *
 * <p>Visit <a
 * href="https://docs.dna-evolutions.com/rest/touroptimizer/rest_touroptimizer.html">https://docs.dna-evolutions.com/rest/touroptimizer/rest_touroptimizer.html</a>
 * for more details.
 *
 * @author jrich
 * @version Jun 1, 2021
 * @since Jun 1, 2021
 */
public class CreateRestTourOptimizerInputExample {

  /** The Constant TIMEOUT_OPTIMIZATION. */
  private static final Duration TIMEOUT_OPTIMIZATION = Duration.ofMinutes(10);

  /**
   * The main method.
   *
   * @param args the arguments
   * @throws ConvertException the convert exception
   * @throws InterruptedException the interrupted exception
   * @throws ExecutionException the execution exception
   * @throws InvalidLicenceException the invalid licence exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static void main(String[] args)
      throws ConvertException, InterruptedException, ExecutionException, InvalidLicenceException,
          IOException {

    new CreateRestTourOptimizerInputExample().example();
  }

  /**
   * To string.
   *
   * @return the string
   */
  public String toString() {
    return "Create test input for the JOpt-TourOptimizer swagger interface. Visit "
        + "https://docs.dna-evolutions.com/rest/touroptimizer/rest_touroptimizer.html"
        + " for more details.";
  }

  /**
   * Example.
   *
   * @throws ConvertException the convert exception
   * @throws InterruptedException the interrupted exception
   * @throws ExecutionException the execution exception
   * @throws InvalidLicenceException the invalid licence exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public void example()
      throws ConvertException, InterruptedException, ExecutionException, InvalidLicenceException,
          IOException {

    // Creating a JSONOptimization object
    IJSONOptimization myOpti = new JSONOptimization();

    // Adding some nodes
    CreateRestTourOptimizerInputExample.addNodes(myOpti);

    // Adding some resources
    CreateRestTourOptimizerInputExample.addResources(myOpti);

    // If not set, an ident will be created
    myOpti.setOptimizationRunIdent("MyJOptRun");

    /*
     *
     * Do the transformation to a valid Input for JOpt-TourOptimizer
     *
     */

    // Create the config object by calling local method toConfig
    OptimizationConfig<JSONConfig> myConfig = toConfig(myOpti);

    // Transform to JSON string
    String json = JSONOptimization.asJSON(myConfig);

    /* 	JSONOptimization has an internal method to write the Config as String, however,
     * 	instead of using this method, the following snippet can also be used:
     *
     *		JSONOptimization.getMapper()
     *		.writerWithDefaultPrettyPrinter()
     *		.writeValueAsString(myConfig);
     **/

    // Print out the json
    System.out.println(json);
  }

  /**
   * To config.
   *
   * @param myOpti the my opti
   * @return the optimization config
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ConvertException the convert exception
   */
  public static OptimizationConfig<JSONConfig> toConfig(IJSONOptimization myOpti)
      throws IOException, ConvertException {

    // Transform the Optimization myOpti

    // By default, all properties are transformed. To save some space,
    // we overwrite the Properties from the Optimization with some custom Properties

    Properties customProperties = new Properties();

    customProperties.setProperty("JOptExitCondition.JOptGenerationCount", "2000");
    customProperties.setProperty("JOpt.Algorithm.PreOptimization.SA.NumIterations", "10000");
    customProperties.setProperty("JOpt.Algorithm.PreOptimization.SA.NumRepetions", "1");
    customProperties.setProperty("JOpt.NumCPUCores", "4");

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

    return myOpti.asConfig(myExtension, customProperties);
  }

  /*
   *
   * Helper
   *
   *
   */

  /**
   * Adds the nodes.
   *
   * @param opti the opti
   */
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

    int importance = 1;

    // Define some nodes
    INode koeln =
        new TimeWindowGeoNode(
            "Koeln", 50.9333, 6.95, weeklyOpeningHours, visitDuration, importance);
    opti.addElement(koeln);

    INode essen =
        new TimeWindowGeoNode(
            "Essen", 51.45, 7.01667, weeklyOpeningHours, visitDuration, importance);
    opti.addElement(essen);

    INode dueren =
        new TimeWindowGeoNode(
            "Dueren", 50.8, 6.48333, weeklyOpeningHours, visitDuration, importance);
    opti.addElement(dueren);

    INode nuernberg =
        new TimeWindowGeoNode(
            "Nuernberg", 49.4478, 11.0683, weeklyOpeningHours, visitDuration, importance);
    opti.addElement(nuernberg);

    INode heilbronn =
        new TimeWindowGeoNode(
            "Heilbronn", 49.1403, 9.22, weeklyOpeningHours, visitDuration, importance);
    opti.addElement(heilbronn);

    INode wuppertal =
        new TimeWindowGeoNode(
            "Wuppertal", 51.2667, 7.18333, weeklyOpeningHours, visitDuration, importance);
    opti.addElement(wuppertal);

    INode aachen =
        new TimeWindowGeoNode(
            "Aachen", 50.775346, 6.083887, weeklyOpeningHours, visitDuration, importance);
    opti.addElement(aachen);
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

    workingHours.add(
        new WorkingHours(
            ZonedDateTime.of(2020, MAY.getValue(), 7, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 7, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    Duration maxWorkingTime = Duration.ofHours(9);
    Quantity<Length> maxDistanceKmW = Quantities.getQuantity(1200.0, KILO(METRE));

    IResource jack =
        new CapacityResource(
            "Jack from Aachen", 50.775346, 6.083887, maxWorkingTime, maxDistanceKmW, workingHours);
    opti.addElement(jack);
  }
}
