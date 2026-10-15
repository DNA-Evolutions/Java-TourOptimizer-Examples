package com.dna.jopt.touroptimizer.java.examples.basic.io_03;
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
import static java.time.Month.MARCH;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import com.dna.jopt.config.convert.ConvertException;
import com.dna.jopt.config.serialize.SerializationException;
import com.dna.jopt.framework.body.IOptimization;
import com.dna.jopt.framework.body.Optimization;
import com.dna.jopt.framework.exception.caught.InvalidLicenceException;
import com.dna.jopt.framework.outcomewrapper.IOptimizationProgress;
import com.dna.jopt.framework.outcomewrapper.IOptimizationResult;
import com.dna.jopt.io.importing.IOptimizationImporter;
import com.dna.jopt.io.importing.json.OptimizationJSONImporter;
import com.dna.jopt.member.unit.hours.IOpeningHours;
import com.dna.jopt.member.unit.hours.OpeningHours;
import com.dna.jopt.member.unit.node.INode;
import com.dna.jopt.member.unit.node.IPillarNode;
import com.dna.jopt.member.unit.node.geo.PillarTimeWindowGeoNode;
import com.dna.jopt.touroptimizer.java.examples.ExampleLicenseHelper;

/**
 * In this example we are loading the current optimization state from a file using JSON file and
 * add some further Nodes to the Optimization.
 *
 * @author DNA
 * @version Mar 26, 2026
 * @since Mar 26, 2026
 */
public class LoadOptimizationFromJsonAndReassignPillar extends Optimization {

  public static void main(String[] args)
      throws InterruptedException, ExecutionException, InvalidLicenceException, ConvertException,
          SerializationException, IOException {
    new LoadOptimizationFromJsonAndReassignPillar().example();
  }

  public String toString() {
    return "Loading the current optimization state from a file using JSON file. We're adding further "
        + "a Pillar via reassignment.";
  }

  public void example()
      throws InterruptedException, ExecutionException, InvalidLicenceException, ConvertException,
          SerializationException, IOException {

    // Set license via helper
    ExampleLicenseHelper.setLicense(this);

    String jsonFile = "myopti.json.bz2";

    try {
      this.invokeFromJson(new FileInputStream(jsonFile), this);

      // Additional Nodes are assigned to the Optimization
      this.addReassignNodes(getAdditionalNodes());

      // Set the Properties
      this.setProperties();

      this.startRunAsync().get();
    } catch (FileNotFoundException e) {
      System.out.println(
          "JsonFile not existing. Run example 'SaveOptimizationToJsonExample.java' first.");
      e.printStackTrace();
    }
  }

  private void setProperties() {

    Properties props = new Properties();

    props.setProperty("JOptExitCondition.JOptGenerationCount", "2000");
    props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumIterations", "100000");
    props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumRepetitions", "1");
    props.setProperty("JOpt.NumCPUCores", "4");

    this.addElement(props);
  }

  private List<INode> getAdditionalNodes() {
    List<INode> myNodesAdditonal = new ArrayList<>();

    List<IOpeningHours> weeklyOpeningHours = new ArrayList<>();
    weeklyOpeningHours.add(
        new OpeningHours(
            ZonedDateTime.of(2020, MARCH.getValue(), 7, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MARCH.getValue(), 7, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    Duration visitDuration = Duration.ofMinutes(20);

    // Define some Nodes
    IPillarNode koeln =
        new PillarTimeWindowGeoNode("Koeln-Pillar", 50.9333, 6.95, weeklyOpeningHours.get(0), visitDuration);
    myNodesAdditonal.add(koeln);


    return myNodesAdditonal;
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

  private void invokeFromJson(FileInputStream jsonFile, IOptimization opti)
      throws ConvertException, SerializationException, IOException {

    IOptimizationImporter importer = new OptimizationJSONImporter();
    importer.update(jsonFile, opti);
  }

  @Override
  public void onAsynchronousOptimizationResult(IOptimizationResult rapoptResult) {
    System.out.println(rapoptResult);
  }
}
