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
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import com.dna.jopt.config.convert.ConvertException;
import com.dna.jopt.config.serialize.SerializationException;
import com.dna.jopt.framework.body.IOptimization;
import com.dna.jopt.framework.exception.caught.InvalidLicenceException;
import com.dna.jopt.framework.outcomewrapper.IOptimizationResult;
import com.dna.jopt.member.bucket.route.ILogicEntityRoute;
import com.dna.jopt.touroptimizer.java.examples.ExampleLicenseHelper;
import com.dna.jopt.util.optimizationresultanalyzer.exceptions.ResultStructureNotMatchingException;
import com.dna.jopt.util.optimizationresultmodifier.helper.IModificationTask;
import com.dna.jopt.util.optimizationresultmodifier.helper.MoveOptimizableNodeModificationTask;

/**
 * Moves the nodes of the second route of an optimized result to the first route and compares the
 * moved result with the original.
 *
 * <p>Scenario: the data of CompareResultExampleOptimization with the far away nodes: Jack from
 * Aachen and nine nodes on May 8 and May 9, 2030 (Europe/Berlin). The optimizer plans two routes;
 * the example moves Nuernberg and Heilbronn from the second day to the end of the first.
 *
 * <p>Look for: the two original routes, the second with an overtime (MAXTIME) violation; the
 * modified first route, which ends after midnight with LATE violations at Nuernberg and Heilbronn
 * and more overtime; and the comparison report, whose job result says that the base job has the
 * lower cost.
 *
 * <p>Prerequisites: None: without a license file the example uses the public evaluation license
 * (see ExampleLicenseHelper). The example writes two KML files into the working directory.
 *
 * @see <a href="https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/compareresult/CompareResult.md">CompareResult.md</a>
 */
public class CompareResultsWithNodesMoveExample extends CompareResultExampleOptimization {

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
    new CompareResultsWithNodesMoveExample().example();
  }

  @Override
  public String toString() {
    return "Moves the nodes of the second route of an optimized result to the first route and "
        + "compares the moved result with the original.";
  }

  /**
   * Builds and runs the optimization, moves the nodes of the second route to the first route in a
   * copy of the result and compares the two results.
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
    addNodes(this, true);
    addResources(this);
    attachToObservables(this);
    IOptimizationResult orgResult = startAndPresentResult(this);
    exportToKml(orgResult, "CompareResultsWithNodesMoveExample_original.kml");
    IOptimizationResult modResult = createModResult(this, orgResult);
    exportToKml(modResult, "CompareResultsWithNodesMoveExample_modified.kml");
    compareResults(orgResult, modResult);
  }

  /**
   * Moves the nodes of the second route to the first route in a copy of the result, prints the
   * copy and returns it.
   *
   * @param opti the optimization that has run
   * @param orgResult the original result
   * @return the modified result
   * @throws InterruptedException if the wait for the result is interrupted
   * @throws ExecutionException if the optimization run fails
   * @throws InvalidLicenceException if the license is not valid
   * @throws IOException if a file cannot be read or written
   * @throws ConvertException if the optimization cannot be converted for the copy
   * @throws SerializationException if the copy cannot be serialized
   */
  private static IOptimizationResult createModResult(
      IOptimization opti, IOptimizationResult orgResult)
      throws InterruptedException, ExecutionException, InvalidLicenceException, IOException,
      ConvertException, SerializationException {
    // Customers often ask why the optimizer does not put all nodes into a single route; the move
    // shows what one route costs.
    IOptimizationResult modResult = moveSecondRouteNodesToFirstRoute(opti, orgResult);

    String banner = "#########################  RESULT MOD  #########################";
    System.out.println("\n\n\n" + banner + "\n\n\n");
    System.out.println(modResult);

    return modResult;
  }

  /**
   * Moves all nodes of the second route to the end of the first route in a copy of the result.
   *
   * @param opti the optimization that has run
   * @param orgResult the original result
   * @return the modified result
   * @throws InterruptedException if the wait for the result is interrupted
   * @throws ExecutionException if the optimization run fails
   * @throws InvalidLicenceException if the license is not valid
   * @throws IOException if a file cannot be read or written
   * @throws ConvertException if the optimization cannot be converted for the copy
   * @throws SerializationException if the copy cannot be serialized
   */
  private static IOptimizationResult moveSecondRouteNodesToFirstRoute(
      IOptimization opti, IOptimizationResult orgResult)
      throws InterruptedException, ExecutionException, InvalidLicenceException, IOException,
      ConvertException, SerializationException {
    List<ILogicEntityRoute> routes = orgResult.getRoutes();
    ILogicEntityRoute secondRoute = routes.get(1);
    List<String> secondRouteItemIds =
        secondRoute.getRouteElementsDetailController().getAsSortedListByArrival().stream()
            .map(node -> node.getElement().getId())
            .toList();

    // Target: Jack's first working day (index 0). Optional.empty() appends the moved nodes at
    // the end of the target route; a node id instead puts them before that node.
    IModificationTask moveTask =
        new MoveOptimizableNodeModificationTask(
            secondRouteItemIds, "Jack from Aachen", 0, Optional.empty());

    List<IModificationTask> tasks = new ArrayList<>();
    tasks.add(moveTask);

    return applyTasksAndCreateModResult(opti, tasks);
  }
}
