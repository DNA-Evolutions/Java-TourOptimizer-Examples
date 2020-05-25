package com.dna.jopt.touroptimizer.java.examples.basic.io;
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
import com.dna.jopt.io.BZip2JsonOptimizationIO;
import com.dna.jopt.io.IOptimizationIO;
import com.dna.jopt.member.unit.hours.IOpeningHours;
import com.dna.jopt.member.unit.hours.OpeningHours;
import com.dna.jopt.member.unit.node.INode;
import com.dna.jopt.member.unit.node.geo.TimeWindowGeoNode;
import com.dna.jopt.touroptimizer.java.examples.ExampleLicenseHelper;

/** Loading the current optimization state from a file using JSON file. */
public class LoadOptimizationFromJsonAndReassignNodes extends Optimization {

  public static void main(String[] args)
      throws InterruptedException, ExecutionException, InvalidLicenceException, ConvertException,
          SerializationException, IOException {
    new LoadOptimizationFromJsonAndReassignNodes().example();
  }

  public String toString() {
    return "Loading the current optimization state from a file using JSON file. Further "
        + "add nodes via reassignment.";
  }

  public void example()
      throws InterruptedException, ExecutionException, InvalidLicenceException, ConvertException,
          SerializationException, IOException {

    // Set license via helper
    ExampleLicenseHelper.setLicense(this);

    String jsonFile = "myopti.json.bz2";
    this.invokeFromJson(new FileInputStream(jsonFile), this);

    this.addReassignNodes(getAdditionalNodes());

    // Properties!
    this.setProperties();

    this.startRunAsync().get();
  }

  private void setProperties() {

    Properties props = new Properties();

    props.setProperty("JOptExitCondition.JOptGenerationCount", "2000");
    props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumIterations", "100000");
    props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumRepetions", "1");
    props.setProperty("JOptLicense.CheckAutoLicensce", "FALSE");
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

    weeklyOpeningHours.add(
        new OpeningHours(
            ZonedDateTime.of(2020, MARCH.getValue(), 8, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MARCH.getValue(), 8, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    Duration visitDuration = Duration.ofMinutes(20);

    // Define some nodes
    TimeWindowGeoNode koeln =
        new TimeWindowGeoNode("Koeln2", 50.9333, 6.95, weeklyOpeningHours, visitDuration, 1);
    myNodesAdditonal.add(koeln);

    TimeWindowGeoNode dueren =
        new TimeWindowGeoNode("Dueren2", 50.8, 6.48333, weeklyOpeningHours, visitDuration, 1);
    myNodesAdditonal.add(dueren);

    TimeWindowGeoNode essen =
        new TimeWindowGeoNode("Essen2", 51.45, 7.01667, weeklyOpeningHours, visitDuration, 1);
    myNodesAdditonal.add(essen);

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

    IOptimizationIO<IOptimization> io = new BZip2JsonOptimizationIO();
    // Read from the snapshot and add to existingEmptyOpt
    io.read(jsonFile, opti);
  }

  @Override
  public void onAsynchronousOptimizationResult(IOptimizationResult rapoptResult) {
    System.out.println(rapoptResult);
  }
}
