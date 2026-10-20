package com.dna.jopt.touroptimizer.java.examples.basic.io_03;
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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;

import com.dna.jopt.framework.body.IOptimization;
import com.dna.jopt.framework.body.Optimization;
import com.dna.jopt.framework.exception.caught.InvalidLicenceException;
import com.dna.jopt.framework.outcomewrapper.IOptimizationResult;
import com.dna.jopt.io.importing.IOptimizationImporter;
import com.dna.jopt.io.importing.json.OptimizationJSONImporter;
import com.dna.jopt.touroptimizer.java.examples.ExampleLicenseHelper;

/**
 * Loads an optimization state from a JSON string and continues from its solution.
 *
 * <p>Scenario: a sample snapshot of the SaveOptimizationToJsonExample scenario as plain JSON
 * text: eight nodes and the two resources Jack and John on March 6 and 7, 2020, time zone
 * Europe/Berlin, with properties and a solution. The example reads the text from the class path
 * resource json/LoadOptimizationFromJsonString.json; in an application the string could come from
 * a database or a message.
 *
 * <p>Look for: the status line OPTIMIZATION_USING_PROVIDED_SOLUTION, which shows that the run
 * starts from the loaded solution, and then the result.
 *
 * <p>Prerequisites: None: without a license file the example uses the public evaluation license
 * (see ExampleLicenseHelper). The JSON resource is part of this repository.
 *
 * @see <a href="https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic/io_03/LoadAndSaveOptimization.md">LoadAndSaveOptimization.md</a>
 */
public class LoadOptimizationFromJsonString extends Optimization {

  private static final String JSON_RESOURCE = "/json/LoadOptimizationFromJsonString.json";

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
    new LoadOptimizationFromJsonString().example();
  }

  @Override
  public String toString() {
    return "Loads an optimization state from a JSON string and continues from its solution.";
  }

  /**
   * Reads the JSON string, loads the optimization from it, runs it and prints the result.
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
    String json = readJsonString();
    if (!loadFromJsonString(this, json)) {
      return;
    }
    attachToObservables(this);
    startAndPresentResult(this);
  }

  /**
   * Reads the JSON resource from the class path into a string.
   *
   * @return the JSON text
   * @throws IOException if a file cannot be read or written
   */
  private static String readJsonString() throws IOException {
    try (InputStream in = LoadOptimizationFromJsonString.class.getResourceAsStream(JSON_RESOURCE)) {
      if (in == null) {
        throw new FileNotFoundException("Resource not found: " + JSON_RESOURCE);
      }
      return new String(in.readAllBytes(), StandardCharsets.UTF_8);
    }
  }

  /**
   * Loads the elements, the properties and the solution from the JSON string.
   *
   * @param opti the optimization
   * @param json the JSON text of a snapshot
   * @return true if the snapshot was loaded
   * @throws IOException if the string cannot be compressed
   */
  private static boolean loadFromJsonString(IOptimization opti, String json) throws IOException {
    IOptimizationImporter importer = new OptimizationJSONImporter();
    // OptimizationJSONImporter reads BZip2-compressed JSON, so the string is compressed first.
    try (InputStream in = compressToBZip2(json)) {
      boolean loaded = importer.update(in, opti);
      if (!loaded) {
        System.out.println("JSON import failed: " + JSON_RESOURCE);
      }
      return loaded;
    }
  }

  /**
   * Compresses a string into an in-memory BZip2 stream.
   *
   * @param text the text to compress
   * @return a stream that reads the compressed bytes
   * @throws IOException if the string cannot be compressed
   */
  private static InputStream compressToBZip2(String text) throws IOException {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    // Closing the BZip2 stream finishes it, so it is complete before the bytes are read.
    try (BZip2CompressorOutputStream bzOut = new BZip2CompressorOutputStream(bytes)) {
      bzOut.write(text.getBytes(StandardCharsets.UTF_8));
    }
    return new ByteArrayInputStream(bytes.toByteArray());
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
