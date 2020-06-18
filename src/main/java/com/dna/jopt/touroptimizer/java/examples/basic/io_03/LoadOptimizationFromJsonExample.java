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
import java.io.FileInputStream;
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

/** Loading the current optimization state from a file using JSON file. */
public class LoadOptimizationFromJsonExample extends Optimization {

  public static void main(String[] args)
      throws InterruptedException, ExecutionException, InvalidLicenceException, ConvertException,
          SerializationException, IOException {
    new LoadOptimizationFromJsonExample().example();
  }

  public String toString() {
    return "Loading the current optimization state from a file using JSON file.";
  }

  public void example()
      throws InterruptedException, ExecutionException, InvalidLicenceException, ConvertException,
          SerializationException, IOException {

    // Set license via helper
    // ExampleLicenseHelper.setLicense(this);

    String jsonFile = "myopti.json.bz2";
    this.invokeFromJson(new FileInputStream(jsonFile), this);

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
