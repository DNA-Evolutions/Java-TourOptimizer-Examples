package com.dna.jopt.touroptimizer.java.examples.expert.compareresult;

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
import com.dna.jopt.util.optimizationresultanalyzer.exceptions.ResultStructureNotMatchingException;
import com.dna.jopt.util.optimizationresultmodifier.helper.ExchangeOptimizableNodesModificationTask;
import com.dna.jopt.util.optimizationresultmodifier.helper.IModificationTask;

/**
 *  The class CompareResultsWithNodeExchangeExample:
 * 
 * The initial result will show a geographical problem at the nodes Duisburg and
 * Krefeld. (Duisburg is visited before Krefeld, which induces a higher total
 * distance).
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
public class CompareResultsWithNodeExchangeExample {

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
	new CompareResultsWithNodeExchangeExample().example();
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

	IOptimization opti = CompareResultExampleOptimization.createCompareExampleOpti(false);

	// Starting the Optimization completable Future and presenting the results
	IOptimizationResult orgResult = CompareResultExampleOptimization.createInitialResult(opti);

	/*
	 * We have a result that we export to a kml. The result will show a geographical
	 * problem at the nodes Duisburg and Krefeld. (Duisburg is visited before
	 * Krefeld, which induces a higher total distance) Most often, such a result
	 * raises questions from end customers. We move the node to a position that
	 * avoids the increased distance.
	 */
	CompareResultExampleOptimization
		.exportToKml("comparison_nodeexchange_intial_result_" + System.currentTimeMillis() + ".kml", orgResult);

	// Create a modified result where we exchange Krefeld and Duisburg and also
	// export to kml
	IOptimizationResult modResult = CompareResultsWithNodeExchangeExample.createModResult(opti, orgResult);

	CompareResultExampleOptimization
		.exportToKml("comparison_nodeexchange_mod_result_" + System.currentTimeMillis() + ".kml", modResult);

	// Compare results to find out which one is better
	CompareResultExampleOptimization.compareResults(opti, orgResult, modResult);
    }

    static IOptimizationResult createModResult(IOptimization opti, IOptimizationResult orgResult)
	    throws InterruptedException, ExecutionException, InvalidLicenceException, ConvertException,
	    SerializationException, IOException {

	/*
	 * We have a result that we export to a kml. The result will show a geographical
	 * problem at the nodes Duisburg and Krefeld. (Duisburg is visited before
	 * Krefeld, which induces a higher total distance) Most often, such a result
	 * raises questions from end customers. We move the node to a position that
	 * avoids the crossing.
	 */

	// Now modify the result - Exchange Duisburg and Krefeld
	IOptimizationResult modResult = CompareResultsWithNodeExchangeExample.exchangeNodes(opti, "Krefeld",
		"Duisburg");

	// Presenting the result
	System.out.println("\n\n\n#########################  RESULT ORG  #########################\n\n\n");
	System.out.println(orgResult);

	System.out.println("\n\n\n#########################  RESULT MOD  #########################\n\n\n");
	System.out.println(modResult);

	return modResult;

    }

    /**
     * Exchange nodes.
     *
     * @param opti      the opti
     * @param nodeOneId the node one id
     * @param nodeTwoId the node two id
     * @return the i optimization result
     * @throws InterruptedException    the interrupted exception
     * @throws ExecutionException      the execution exception
     * @throws InvalidLicenceException the invalid licence exception
     * @throws ConvertException        the convert exception
     * @throws SerializationException  the serialization exception
     * @throws IOException             Signals that an I/O exception has occurred.
     */
    static IOptimizationResult exchangeNodes(IOptimization opti, String nodeOneId, String nodeTwoId)
	    throws InterruptedException, ExecutionException, InvalidLicenceException, ConvertException,
	    SerializationException, IOException {

	IModificationTask moveTask = new ExchangeOptimizableNodesModificationTask(nodeOneId, nodeTwoId);

	List<IModificationTask> tasks = new ArrayList<>();
	tasks.add(moveTask);

	// Follow methods applyTasksAndCreateModResult to understand how modification is triggered
	return CompareResultExampleOptimization.applyTasksAndCreateModResult(opti, tasks);

    }

}
