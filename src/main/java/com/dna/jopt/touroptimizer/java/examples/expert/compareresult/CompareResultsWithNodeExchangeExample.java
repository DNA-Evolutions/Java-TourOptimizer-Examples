package com.dna.jopt.touroptimizer.java.examples.expert.compareresult;
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import com.dna.jopt.config.convert.ConvertException;
import com.dna.jopt.config.serialize.SerializationException;
import com.dna.jopt.framework.body.IOptimization;
import com.dna.jopt.framework.exception.caught.InvalidLicenceException;
import com.dna.jopt.framework.outcomewrapper.IOptimizationResult;
import com.dna.jopt.touroptimizer.java.examples.ExampleLicenseHelper;
import com.dna.jopt.util.optimizationresultanalyzer.exceptions.ResultStructureNotMatchingException;
import com.dna.jopt.util.optimizationresultmodifier.helper.ExchangeOptimizableNodesModificationTask;
import com.dna.jopt.util.optimizationresultmodifier.helper.IModificationTask;

/**
 * Swaps two nodes of an optimized route and compares the swapped result with the original.
 *
 * <p>Scenario: the data of CompareResultExampleOptimization without the far away nodes: Jack from
 * Aachen and seven nodes on May 8 and May 9, 2030 (Europe/Berlin). The optimizer visits Duisburg
 * before Krefeld, which looks like a detour on a map; the example swaps the two.
 *
 * <p>Look for: the original route with Duisburg before Krefeld, the modified route with a LATE
 * violation at Duisburg and a shorter distance, and the comparison report, whose job result says
 * that the base job has the lower cost.
 *
 * <p>Prerequisites: None: without a license file the example uses the public evaluation license
 * (see ExampleLicenseHelper). The example writes two KML files into the working directory.
 *
 * @see <a href="https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/compareresult/CompareResult.md">CompareResult.md</a>
 */
public class CompareResultsWithNodeExchangeExample extends CompareResultExampleOptimization {

  /**
   * Runs the example.
   *
   * @param args not used
   * @throws InterruptedException if the wait for the result is interrupted
   * @throws ExecutionException if the optimization run fails
   * @throws TimeoutException if the run does not finish within the wait
   * @throws InvalidLicenceException if the license is not valid
   * @throws IOException if a file cannot be read or written
   * @throws ConvertException if the optimization cannot be converted for the copy
   * @throws SerializationException if the copy cannot be serialized
   * @throws ResultStructureNotMatchingException if the two results do not have the same routes
   */
  public static void main(String[] args)
      throws InterruptedException, ExecutionException, TimeoutException, InvalidLicenceException,
      IOException, ConvertException, SerializationException, ResultStructureNotMatchingException {
    new CompareResultsWithNodeExchangeExample().example();
  }

  @Override
  public String toString() {
    return "Swaps two nodes of an optimized route and compares the swapped result with the "
        + "original.";
  }

  /**
   * Builds and runs the optimization, swaps Krefeld and Duisburg in a copy of the result and
   * compares the two results.
   *
   * @throws InterruptedException if the wait for the result is interrupted
   * @throws ExecutionException if the optimization run fails
   * @throws TimeoutException if the run does not finish within the wait
   * @throws InvalidLicenceException if the license is not valid
   * @throws IOException if a file cannot be read or written
   * @throws ConvertException if the optimization cannot be converted for the copy
   * @throws SerializationException if the copy cannot be serialized
   * @throws ResultStructureNotMatchingException if the two results do not have the same routes
   */
  public void example()
      throws InterruptedException, ExecutionException, TimeoutException, InvalidLicenceException,
      IOException, ConvertException, SerializationException, ResultStructureNotMatchingException {
    // The public evaluation license, or your own license if you put a license file where
    // ExampleLicenseHelper looks for it.
    ExampleLicenseHelper.setLicense(this);
    addProperties(this);
    addNodes(this, false);
    addResources(this);
    attachToObservables(this);
    IOptimizationResult orgResult = startAndPresentResult(this);
    exportToKml(orgResult, "CompareResultsWithNodeExchangeExample_original.kml");
    IOptimizationResult modResult = createModResult(this);
    exportToKml(modResult, "CompareResultsWithNodeExchangeExample_modified.kml");
    compareResults(orgResult, modResult);
  }

  /**
   * Swaps Krefeld and Duisburg in a copy of the result, prints the copy and returns it.
   *
   * @param opti the optimization that has run
   * @return the modified result
   * @throws InterruptedException if the wait for the result is interrupted
   * @throws ExecutionException if the optimization run fails
   * @throws InvalidLicenceException if the license is not valid
   * @throws IOException if a file cannot be read or written
   * @throws ConvertException if the optimization cannot be converted for the copy
   * @throws SerializationException if the copy cannot be serialized
   */
  private static IOptimizationResult createModResult(IOptimization opti)
      throws InterruptedException, ExecutionException, InvalidLicenceException, IOException,
      ConvertException, SerializationException {
    // Customers often ask why Duisburg comes before Krefeld; the swap shows what the other order
    // costs.
    IOptimizationResult modResult = exchangeNodes(opti, "Krefeld", "Duisburg");

    String banner = "#########################  RESULT MOD  #########################";
    System.out.println("\n\n\n" + banner + "\n\n\n");
    System.out.println(modResult);

    return modResult;
  }

  /**
   * Exchanges the positions of two nodes in a copy of the result.
   *
   * @param opti the optimization that has run
   * @param nodeOneId the id of the first node
   * @param nodeTwoId the id of the second node
   * @return the modified result
   * @throws InterruptedException if the wait for the result is interrupted
   * @throws ExecutionException if the optimization run fails
   * @throws InvalidLicenceException if the license is not valid
   * @throws IOException if a file cannot be read or written
   * @throws ConvertException if the optimization cannot be converted for the copy
   * @throws SerializationException if the copy cannot be serialized
   */
  private static IOptimizationResult exchangeNodes(
      IOptimization opti, String nodeOneId, String nodeTwoId)
      throws InterruptedException, ExecutionException, InvalidLicenceException, IOException,
      ConvertException, SerializationException {
    IModificationTask exchangeTask =
        new ExchangeOptimizableNodesModificationTask(nodeOneId, nodeTwoId);

    List<IModificationTask> tasks = new ArrayList<>();
    tasks.add(exchangeTask);

    return applyTasksAndCreateModResult(opti, tasks);
  }
}
