package com.dna.jopt.touroptimizer.java.examples.basic.io_03;
/*-
 * #%L
 * JOpt TourOptimizer Examples
 * %%
 * Copyright (C) 2017 - 2020 DNA Evolutions GmbH
 * %%
 * This file is subject to the terms and conditions defined in file 'LICENSE.txt',
 * which is part of this source code package.
 *
 * If not, see <https://www.dna-evolutions.com/agb-conditions-and-terms/>.
 * #L%
 */
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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
import com.dna.jopt.touroptimizer.java.examples.ExampleLicenseHelper;

/**
 * Here, we are using a JSON file only as database and ignore the solution. This way a new Optimization with the
 * same Elements can be started, not biased by the result of the last run's solution.
 *
 * @author DNA
 * @version Mar 26, 2021
 * @since Mar 26, 2021
 */
public class LoadOptimizationToFreshRunExample extends Optimization {

  public static void main(String[] args)
      throws InterruptedException, ExecutionException, InvalidLicenceException, ConvertException,
          SerializationException, FileNotFoundException, IOException {

    new LoadOptimizationToFreshRunExample().example();
  }

  public String toString() {
    return "Loading the current optimization state from a file using JSON file.";
  }

  public void example()
      throws InterruptedException, ExecutionException, InvalidLicenceException, ConvertException,
          SerializationException, FileNotFoundException, IOException {

    // Set license via helper
    ExampleLicenseHelper.setLicense(this);

    // In case we set ignoreLoadedSolution to "true" all parts of the snapshot
    // are loaded, e.g. NodeConnector, Properties and all other Elements. But when starting
    // the Optimization the previous solution is ignored.
    boolean ignoreLoadedSolution = true;
    String jsonFile = "myopti.json.bz2";

    try {
      this.invokeFromJson(new FileInputStream(jsonFile), this, ignoreLoadedSolution);
      // Properties!
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
    props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumRepetions", "1");
    props.setProperty("JOptLicense.CheckAutoLicensce", "FALSE");
    props.setProperty("JOpt.NumCPUCores", "4");

    this.addElement(props);
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

  private void invokeFromJson(
      FileInputStream jsonFile, IOptimization opti, boolean ignoreLoadedSolution)
      throws ConvertException, SerializationException, IOException {

    IOptimizationImporter importer = new OptimizationJSONImporter();
    importer.update(jsonFile, opti, ignoreLoadedSolution);
  }

  @Override
  public void onAsynchronousOptimizationResult(IOptimizationResult rapoptResult) {
    System.out.println(rapoptResult);
  }
}
