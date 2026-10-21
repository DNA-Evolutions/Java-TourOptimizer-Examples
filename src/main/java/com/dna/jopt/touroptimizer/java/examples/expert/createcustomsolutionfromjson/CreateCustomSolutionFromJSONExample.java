package com.dna.jopt.touroptimizer.java.examples.expert.createcustomsolutionfromjson;
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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import com.dna.jopt.framework.body.IOptimization;
import com.dna.jopt.framework.body.Optimization;
import com.dna.jopt.framework.exception.caught.InvalidLicenceException;
import com.dna.jopt.framework.outcomewrapper.IOptimizationResult;
import com.dna.jopt.io.importing.IOptimizationImporter;
import com.dna.jopt.io.importing.json.OptimizationJSONImporter;
import com.dna.jopt.member.bucket.entity.IEntity;
import com.dna.jopt.member.unit.node.INode;
import com.dna.jopt.member.unit.resource.IResource;
import com.dna.jopt.touroptimizer.java.examples.ExampleLicenseHelper;

/**
 * Loads a saved optimization from a JSON file into a second optimization and takes over its
 * nodes, resources, node connector and properties by hand, as the starting point for a custom
 * solution.
 *
 * <p>Scenario: the snapshot that SaveOptimizationToJsonExample writes: eight nodes and the two
 * resources Jack and John on March 6 and 7, 2020, time zone Europe/Berlin, with the properties of
 * that run. The example adds the loaded nodes and resources without their routes, so the run
 * starts fresh.
 *
 * <p>Look for: the status line OPTIMIZATION_ALGO_STARTING for SmartConstructionAlgo (the run
 * builds its own start solution), then the result.
 *
 * <p>Prerequisites: None for the license: without a license file the example uses the public
 * evaluation license (see ExampleLicenseHelper). Run SaveOptimizationToJsonExample first, in the
 * same working directory: it writes myopti.json.bz2. LoadOptimizationToFreshRunExample does the
 * same in one importer call.
 *
 * @see <a href="https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/createcustomsolutionfromjson/CreateCustomSolutionFromJSON.md">CreateCustomSolutionFromJSON.md</a>
 */
public class CreateCustomSolutionFromJSONExample extends Optimization {

  private static final String JSON_FILE = "myopti.json.bz2";

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
    new CreateCustomSolutionFromJSONExample().example();
  }

  @Override
  public String toString() {
    return "Loads a saved optimization from a JSON file into a second optimization and takes over "
        + "its nodes, resources, node connector and properties by hand, as the starting point for "
        + "a custom solution.";
  }

  /**
   * Loads the snapshot, takes over its elements, runs them and prints the result.
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
    if (!loadFromJson(this)) {
      return;
    }
    attachToObservables(this);
    startAndPresentResult(this);
  }

  /**
   * Loads the JSON file and adds its nodes and resources to the optimization.
   *
   * @param opti the optimization
   * @return true if the snapshot was loaded
   * @throws IOException if a file cannot be read or written
   */
  private static boolean loadFromJson(IOptimization opti) throws IOException {
    // Take over the node connector and the properties of the snapshot as well.
    boolean doLoadNodeConnector = true;
    boolean doLoadProperties = true;

    try (FileInputStream jsonFile = new FileInputStream(JSON_FILE)) {
      Optional<IEntity> enOpt =
          loadEntityFromJson(jsonFile, opti, doLoadNodeConnector, doLoadProperties);
      if (enOpt.isEmpty()) {
        System.out.println("JSON import failed: " + JSON_FILE);
        return false;
      }
      IEntity en = enOpt.get();

      // YOUR TODO HERE THE CUSTOM SOLUTION CAN BE CREATED BY USING THE LOADED ELEMENTS
      List<INode> nodes = getNodes(en);
      List<IResource> ress = getResources(en);

      // For now we simply add the elements - But you could create your own custom solution
      opti.addNodes(nodes);
      opti.addResources(ress);
      return true;
    } catch (FileNotFoundException e) {
      System.out.println(
          "JsonFile not existing. Run example 'SaveOptimizationToJsonExample.java' first.");
      return false;
    }
  }

  /**
   * Imports the JSON file into a second optimization and returns its entity; takes over the node
   * connector and the properties if asked for.
   *
   * @param jsonFile the JSON file
   * @param opti the optimization that takes over the node connector and the properties
   * @param doLoadNodeConnector true to take over the node connector
   * @param doLoadProperties true to take over the properties
   * @return the entity of the snapshot, or empty if the import failed
   */
  private static Optional<IEntity> loadEntityFromJson(
      FileInputStream jsonFile,
      IOptimization opti,
      boolean doLoadNodeConnector,
      boolean doLoadProperties) {

    IOptimization dummyOptimization = new Optimization();

    IOptimizationImporter importer = new OptimizationJSONImporter();

    if (!importer.update(jsonFile, dummyOptimization)) {
      return Optional.empty();
    }

    IEntity en = dummyOptimization.getWorkEntity();

    if (doLoadNodeConnector) {
      opti.setNodeConnector(dummyOptimization.getNodeConnector());
    }

    Optional<Properties> propertiesOpt = dummyOptimization.getUserProperties();
    if (doLoadProperties && propertiesOpt.isPresent()) {
      opti.addElement(propertiesOpt.get());
    }

    return Optional.of(en);
  }

  /**
   * Returns the resources of an entity, each once.
   *
   * @param en the entity
   * @return the resources
   */
  private static List<IResource> getResources(IEntity en) {
    return en.getAllEntityElements().stream()
        .filter(e -> e instanceof IResource)
        .map(e -> (IResource) e)
        .distinct()
        .collect(Collectors.toList());
  }

  /**
   * Returns the nodes of an entity, each once.
   *
   * @param en the entity
   * @return the nodes
   */
  private static List<INode> getNodes(IEntity en) {
    return en.getAllEntityElements().stream()
        .filter(e -> e instanceof INode)
        .map(e -> (INode) e)
        .distinct()
        .collect(Collectors.toList());
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
