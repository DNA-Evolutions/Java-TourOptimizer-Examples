package com.dna.jopt.touroptimizer.java.examples.expert.compareresult;

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
import com.dna.jopt.util.optimizationresultanalyzer.exceptions.ResultStructureNotMatchingException;
import com.dna.jopt.util.optimizationresultmodifier.helper.IModificationTask;
import com.dna.jopt.util.optimizationresultmodifier.helper.MoveOptimizableNodeModificationTask;

/**
 * The class CompareResultsWithNodesMoveExample:
 * 
 * The initial result will show two separate routes. Customers may ask: Can't the optimizer keep all jobs in a single route?
 * 
 * <br><br>
 * Visit
 * <a
 * href="https://docs.dna-evolutions.com/overview_docs/comparison_tool/comparison_tool.html">https://docs.dna-evolutions.com/overview_docs/comparison_tool/comparison_tool.html</a>
 * for an explanation.
 *
 * @author jrich
 * @version Feb 5, 2025
 * @since Feb 5, 2025
 */
public class CompareResultsWithNodesMoveExample {

    /**
     * The main method.
     *
     * @param args the arguments
     * @throws InterruptedException                the interrupted exception
     * @throws ExecutionException                  the execution exception
     * @throws InvalidLicenceException             the invalid licence exception
     * @throws IOException
     * @throws TimeoutException
     * @throws SerializationException
     * @throws ConvertException
     * @throws ResultStructureNotMatchingException
     */
    public static void main(String[] args)
	    throws InterruptedException, ExecutionException, InvalidLicenceException, IOException, TimeoutException,
	    ConvertException, SerializationException, ResultStructureNotMatchingException {
	new CompareResultsWithNodesMoveExample().example();
    }

    public String toString() {
	return "Example of a result modification and comparison to understand decision done by the Optimizer.";
    }

    /**
     * The method which executes the necessary parts for the Optimization.
     *
     * @throws InterruptedException                the interrupted exception
     * @throws ExecutionException                  the execution exception
     * @throws InvalidLicenceException             the invalid licence exception
     * @throws IOException
     * @throws TimeoutException
     * @throws SerializationException
     * @throws ConvertException
     * @throws ResultStructureNotMatchingException
     */
    public void example() throws InterruptedException, ExecutionException, InvalidLicenceException, IOException,
	    TimeoutException, ConvertException, SerializationException, ResultStructureNotMatchingException {

	IOptimization opti = CompareResultExampleOptimization.createCompareExampleOpti(true);

	// Starting the Optimization completable Future and presenting the results
	IOptimizationResult orgResult = CompareResultExampleOptimization.createInitialResult(opti);

	/*
	 * We have a result that we export to a kml. The result will show a geographical
	 * problem at the nodes Duisburg and Krefeld. (Duisburg is visited before
	 * Krefeld, which induces a higher total distance) Most often, such a result
	 * raises questions from end customers. We move the node to a position that
	 * avoids the crossing.
	 */
	CompareResultExampleOptimization.exportToKml("comparison_nodemove_intial_result_" + System.currentTimeMillis() + ".kml", orgResult);

	// Create a modified result where we exchange Krefeld and Duisburg and also
	// export to kml
	IOptimizationResult modResult = CompareResultsWithNodesMoveExample.createModResult(opti, orgResult);

	CompareResultExampleOptimization.exportToKml("comparison_nodemove_mod_result_" + System.currentTimeMillis() + ".kml", modResult);

	// (8) Compare results to find out which one is better
	CompareResultExampleOptimization.compareResults(opti, orgResult, modResult);
    }

    static IOptimizationResult createModResult(IOptimization opti, IOptimizationResult orgResult)
	    throws InterruptedException, ExecutionException, InvalidLicenceException, ConvertException,
	    SerializationException, IOException {

	/*
	 * The initial result will show two separate routes to avoid overtime. However, a customer may ask why the Optimizer is not putting all nodes into a single route.
	 */

	// Now modify the result - We move all nodes from the second route into the first route.
	IOptimizationResult modResult = CompareResultsWithNodesMoveExample.moveSecondRouteNodesToFirstRoute(opti,orgResult);

	// Presenting the result
	System.out.println("\n\n\n#########################  RESULT ORG  #########################\n\n\n");
	System.out.println(orgResult);

	System.out.println("\n\n\n#########################  RESULT MOD  #########################\n\n\n");
	System.out.println(modResult);

	return modResult;

    }
    
    /**
     * Move second route nodes to first route. (THIS METHOD IS NOT IN USE IN THIS EXAMPLE)
     *
     * @param opti the opti
     * @param orgResult the org result
     * @return the i optimization result
     * @throws InterruptedException the interrupted exception
     * @throws ExecutionException the execution exception
     * @throws InvalidLicenceException the invalid licence exception
     * @throws ConvertException the convert exception
     * @throws SerializationException the serialization exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static IOptimizationResult moveSecondRouteNodesToFirstRoute(IOptimization opti,
	    IOptimizationResult orgResult) throws InterruptedException, ExecutionException, InvalidLicenceException,
	    ConvertException, SerializationException, IOException {

	// We want to generate a modified result version. We move all nodes from the
	// second day to the first day
	List<ILogicEntityRoute> routes = orgResult.getRoutes();
	ILogicEntityRoute secondRoute = routes.get(1);
	List<String> secondRouteItemIds = secondRoute.getRouteElementsDetailController().getAsSortedListByArrival()
		.stream().map(node -> node.getElement().getId()).toList();

	IModificationTask moveTask = new MoveOptimizableNodeModificationTask(secondRouteItemIds, // moveNodes
		"Jack from Aachen", // newResId - might be the same id if resource stays the same
		0, // newResWorkingHoursIndex - the index of the target working hours
		Optional.empty()); // afterNodeId - If nodes should be positioned that afterNodeId is the first
				   // nodes after the chunk of inserted nodes. Leave empty, if nodes are appended
				   // to the end

	List<IModificationTask> tasks = new ArrayList<>();
	tasks.add(moveTask);

	
	// Follow methods applyTasksAndCreateModResult to understand how modification is triggered
	return CompareResultExampleOptimization.applyTasksAndCreateModResult(opti, tasks);
    }
    
    
    

}
