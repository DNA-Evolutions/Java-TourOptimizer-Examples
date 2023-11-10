package com.dna.jopt.touroptimizer.java.examples.restful.readinput;

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
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import com.dna.jopt.config.convert.ConvertException;
import com.dna.jopt.config.json.framework.IJSONOptimization;
import com.dna.jopt.config.json.framework.JSONOptimization;
import com.dna.jopt.config.json.types.JSONConfig;
import com.dna.jopt.config.serialize.ConfigSerialization;
import com.dna.jopt.config.serialize.SerializationException;
import com.dna.jopt.config.types.OptimizationConfig;
import com.dna.jopt.config.types.RestOptimization;
import com.dna.jopt.framework.body.IOptimization;
import com.dna.jopt.framework.exception.caught.InvalidLicenceException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The Class ReadJsonConfigAndRunExample. Reads REST-input for the
 * that can be also used (or originated) by the JOpt-TourOptimizer and runs a local optimization with JOpt Java core.
 * 
 * <p>
 * Visit <a href=
 * "https://docs.dna-evolutions.com/rest/touroptimizer/rest_touroptimizer.html">https://docs.dna-evolutions.com/rest/touroptimizer/rest_touroptimizer.html</a>
 * for more details.
 *
 * @author jrich
 * @version Jun 1, 2021
 * @since Jun 1, 2021
 *  
*/

public class ReadJsonConfigAndRunExample {

    /**
     * The main method.
     *
     * @param args the arguments
     * @throws ConvertException the convert exception
     * @throws InterruptedException the interrupted exception
     * @throws ExecutionException the execution exception
     * @throws InvalidLicenceException the invalid licence exception
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws SerializationException the serialization exception
     */
    public static void main(String[] args) throws ConvertException, InterruptedException, ExecutionException,
	    InvalidLicenceException, IOException, SerializationException {

	new ReadJsonConfigAndRunExample().example();
    }

    /**
     * To string.
     *
     * @return the string
     */
    public String toString() {

	return "Reads JOpt-TourOptimizer REST-JSON Input and runs it locally with JOpt. Visit "
		+ "https://docs.dna-evolutions.com/rest/touroptimizer/rest_touroptimizer.html" + " for more details.";
    }

    /**
     * Example.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws InvalidLicenceException the invalid licence exception
     * @throws InterruptedException the interrupted exception
     * @throws ExecutionException the execution exception
     * @throws ConvertException the convert exception
     * @throws SerializationException the serialization exception
     */
    public void example() throws IOException, InvalidLicenceException, InterruptedException, ExecutionException,
	    ConvertException, SerializationException {

	// Define input in JSON format - modify if desired

	String myInput = JSONInputProvider.JSON_INOUT_WITHOUT_SOLUTION;

	// Create an optimization object - the RestOptimization object is extending and
	// wrapping the OptimizationConfig
	RestOptimization opti = ReadJsonConfigAndRunExample.jsonToRestOptimization(myInput,
		ConfigSerialization.objectMapper());

	// Use the JSONOptimization as engine to run the defined config -
	// JSONOptimization is compatible with the core of JOpt
	IJSONOptimization myOpti = new JSONOptimization();

	// 3) Attach to the Observables to see progress, errors, etc.
	attachToObservables(myOpti);

	// Transform to OptimizationConfig object
	OptimizationConfig<JSONConfig> config = opti.asConfig();

	// Do a dummy modification by creating a modified copy of the orignal object
	config = dummyModify(config);

	// Start the Optimization by providing the immutable OptimizationConfig object
	CompletableFuture<OptimizationConfig<JSONConfig>> resultFuture = myOpti.startAsynchConfigFuture(config,
		Optional.empty());

	// We call get() on the future to wait until the Optimization is done
	OptimizationConfig<JSONConfig> result = resultFuture.get();

	System.out.println(JSONOptimization.asJSON(result, true));

    }

    /**
     * Attach to observables.
     *
     * @param opti the opti
     */
    private static void attachToObservables(IOptimization opti) {

	opti.getOptimizationEvents().progressSubject().subscribe(p -> System.out.println(p.getProgressString()));

	opti.getOptimizationEvents().warningSubject().subscribe(w -> System.out.println(w.toString()));

	opti.getOptimizationEvents().statusSubject().subscribe(s -> System.out.println(s.toString()));

	opti.getOptimizationEvents().errorSubject().subscribe(e -> System.out.println(e.toString()));
    }

    /*
     * 
     * 
     */

    /**
     * Dummy modify.
     *
     * @param config the config
     * @return the optimization config
     */
    public static OptimizationConfig<JSONConfig> dummyModify(OptimizationConfig<JSONConfig> config) {

	return config.withIdent("MyNewModifiedIdent");
    }

    /**
     * Json to rest optimization.
     *
     * @param src the src
     * @param mapper the mapper
     * @return the rest optimization
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static RestOptimization jsonToRestOptimization(String src, ObjectMapper mapper) throws IOException {

	return mapper.readValue(src, new TypeReference<RestOptimization>() {
	});
    }
}