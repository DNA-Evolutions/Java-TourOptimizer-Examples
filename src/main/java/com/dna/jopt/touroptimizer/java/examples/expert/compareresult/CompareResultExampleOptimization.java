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
import static tec.units.ri.unit.MetricPrefix.KILO;
import static tec.units.ri.unit.Units.METRE;
import static java.time.Month.MAY;

import tec.units.ri.quantity.Quantities;

import javax.measure.Quantity;
import javax.measure.quantity.Length;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.dna.jopt.config.convert.ConvertException;
import com.dna.jopt.config.serialize.SerializationException;
import com.dna.jopt.framework.body.IOptimization;
import com.dna.jopt.framework.body.Optimization;
import com.dna.jopt.framework.exception.caught.InvalidLicenceException;
import com.dna.jopt.framework.outcomewrapper.IOptimizationResult;
import com.dna.jopt.io.exporting.IEntityExporter;
import com.dna.jopt.io.exporting.kml.EntityKMLExporter;
import com.dna.jopt.member.unit.hours.IWorkingHours;
import com.dna.jopt.member.unit.hours.IOpeningHours;
import com.dna.jopt.member.unit.hours.WorkingHours;
import com.dna.jopt.member.unit.hours.OpeningHours;
import com.dna.jopt.member.unit.node.INode;
import com.dna.jopt.member.unit.node.geo.TimeWindowGeoNode;
import com.dna.jopt.member.unit.resource.CapacityResource;
import com.dna.jopt.member.unit.resource.IResource;
import com.dna.jopt.touroptimizer.java.examples.ExampleLicenseHelper;
import com.dna.jopt.util.optimizationresultanalyzer.advantages.container.JobAdvantagesController;
import com.dna.jopt.util.optimizationresultanalyzer.advantages.container.job.JobAdvantageResult;
import com.dna.jopt.util.optimizationresultanalyzer.advantages.interpreter.CostAdvantageInterpreter;
import com.dna.jopt.util.optimizationresultanalyzer.advantages.interpreter.ICostAdvantagesInterpreter;
import com.dna.jopt.util.optimizationresultanalyzer.exceptions.ResultStructureNotMatchingException;
import com.dna.jopt.util.optimizationresultmodifier.helper.IModificationTask;
import com.dna.jopt.util.optimizationresultmodifier.wrapper.OptimizationResultModificationWrapper;


/**
 * Helps setting up an Optimization to show the Comparison Tool
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
public class CompareResultExampleOptimization extends Optimization {


    public String toString() {
	return "Class to create an Optimization used by the compare examples";
    }

    /**
     * The method which executes the necessary parts for the Optimization.
     * @return 
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
    public static IOptimization createCompareExampleOpti(boolean addFarAwayNodes) throws IOException {
	
	
	IOptimization opti = new CompareResultExampleOptimization();

	// We use the free mode for the example - please modify the ExampleLicenseHelper
	// in case you have a valid license.

	// Set license via helper
	ExampleLicenseHelper.setLicense(opti);

	// Setting Properties, adding all Elements
	// Adding properties
	CompareResultExampleOptimization.addProperties(opti);

	// Adding Nodes
	CompareResultExampleOptimization.addNodes(opti, addFarAwayNodes);

	// Adding Resources
	CompareResultExampleOptimization.addResources(opti);

	// Attaching to Observables
	CompareResultExampleOptimization.attachToObservables(opti);
	
	
	return opti;
    }


    /**
     * Compare results.
     *
     * @param opti      the opti
     * @param orgResult the org result
     * @param modResult the mod result
     * @throws ResultStructureNotMatchingException the result structure not matching
     *                                             exception
     */
    static void compareResults(IOptimization opti, IOptimizationResult orgResult, IOptimizationResult modResult)
	    throws ResultStructureNotMatchingException {

	// Compare
	Optional<JobAdvantageResult> comparissonResultOptional = JobAdvantagesController.compare(orgResult, modResult);

	if (comparissonResultOptional.isPresent()) {
	    JobAdvantageResult comparissonResult = comparissonResultOptional.get();

	    ICostAdvantagesInterpreter myInterpreter = new CostAdvantageInterpreter();

	    System.out.println("\n\n\n#########################  COMPARISON RESULT  #########################\n\n\n");
	    System.out.println(myInterpreter.generateTextReport(comparissonResult));

	} else {
	    System.out.println("Comparisson failed");
	}

    }

    /**
     * Creates the initial result.
     *
     * @param opti the opti
     * @return the i optimization result
     * @throws InvalidLicenceException the invalid licence exception
     * @throws InterruptedException the interrupted exception
     * @throws ExecutionException the execution exception
     * @throws TimeoutException the timeout exception
     */
    static IOptimizationResult createInitialResult(IOptimization opti)
	    throws InvalidLicenceException, InterruptedException, ExecutionException, TimeoutException {

	// Extracting a completable Future for the optimization result
	CompletableFuture<IOptimizationResult> resultFuture = opti.startRunAsync();

	// It is important to block the call, otherwise the Optimization will be
	// terminated
	IOptimizationResult result = resultFuture.get(5, TimeUnit.MINUTES);

	return result;

    }

    /**
     * Apply tasks and create mod result.
     *
     * @param opti the opti
     * @param tasks the tasks
     * @return the i optimization result
     * @throws InterruptedException the interrupted exception
     * @throws ExecutionException the execution exception
     * @throws InvalidLicenceException the invalid licence exception
     * @throws ConvertException the convert exception
     * @throws SerializationException the serialization exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    static IOptimizationResult applyTasksAndCreateModResult(IOptimization opti, List<IModificationTask> tasks)
	    throws InterruptedException, ExecutionException, InvalidLicenceException, ConvertException,
	    SerializationException, IOException {
	
	OptimizationResultModificationWrapper myWrapper = new OptimizationResultModificationWrapper();

	// FYI: You can do: 
	// IOptimizationResult recoveredOrgResult = myWrapper.getNewResult(opti, new ArrayList<>()); what creates an shallow copy of the result.
	
	IOptimizationResult modiResult = myWrapper.getNewResult(opti, tasks);

	return modiResult;
    }
    
    /*
     * 
     * 
     * 
     */

    /**
     * Adds the Properties to the Optimization.
     *
     * @param opti the optimization instance
     */
    private static void addProperties(IOptimization opti) {

	Properties props = new Properties();

	props.setProperty("JOptExitCondition.JOptGenerationCount", "200");
	props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumIterations", "10000");
	props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumRepetions", "1");

	props.setProperty("JOpt.NumCPUCores", "4");

	opti.addElement(props);
    }

    /**
     * Adds the Nodes to the Optimization.
     *
     * @param opti the optimization instance
     */
    private static void addNodes(IOptimization opti, boolean addFarAwayNodes ) {

	// Define the OpeningHours
	List<IOpeningHours> weeklyOpeningHours = new ArrayList<>();
	weeklyOpeningHours
		.add(new OpeningHours(ZonedDateTime.of(2030, MAY.getValue(), 8, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
			ZonedDateTime.of(2030, MAY.getValue(), 8, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));
	
	weeklyOpeningHours
	.add(new OpeningHours(ZonedDateTime.of(2030, MAY.getValue(), 9, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
		ZonedDateTime.of(2030, MAY.getValue(), 9, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

	Duration visitDuration = Duration.ofMinutes(30);

	int importance = 1;

	// Define some Nodes
	INode koeln = new TimeWindowGeoNode("Koeln", 50.9333, 6.95, weeklyOpeningHours, visitDuration, importance);
	opti.addElement(koeln);

	INode essen = new TimeWindowGeoNode("Essen", 51.45, 7.01667, weeklyOpeningHours, visitDuration, importance);
	opti.addElement(essen);

	INode dueren = new TimeWindowGeoNode("Dueren", 50.8, 6.48333, weeklyOpeningHours, visitDuration, importance);
	opti.addElement(dueren);

	INode wuppertal = new TimeWindowGeoNode("Wuppertal", 51.2667, 7.18333, weeklyOpeningHours, visitDuration,
		importance);
	opti.addElement(wuppertal);

	INode aachen = new TimeWindowGeoNode("Aachen", 50.775346, 6.083887, weeklyOpeningHours, visitDuration,
		importance);
	opti.addElement(aachen);

	INode krefeld = new TimeWindowGeoNode("Krefeld", 51.305449, 6.631279, weeklyOpeningHours, visitDuration,
		importance);
	opti.addElement(krefeld);

	
	
	if (addFarAwayNodes) {
	    INode nuernberg = new TimeWindowGeoNode("Nuernberg", 49.4478, 11.0683, weeklyOpeningHours, visitDuration,
		    importance);
	    opti.addElement(nuernberg);

	    INode heilbronn = new TimeWindowGeoNode("Heilbronn", 49.1403, 9.22, weeklyOpeningHours, visitDuration,
		    importance);
	    opti.addElement(heilbronn);
	}
	

	// On purpose, we define a stricter opening hour for Duisburg
	List<IOpeningHours> weeklyOpeningHoursStrict = new ArrayList<>();
	weeklyOpeningHoursStrict
		.add(new OpeningHours(ZonedDateTime.of(2030, MAY.getValue(), 8, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
			ZonedDateTime.of(2030, MAY.getValue(), 8, 9, 45, 0, 0, ZoneId.of("Europe/Berlin"))));

	INode duisburg = new TimeWindowGeoNode("Duisburg", 51.47994, 6.75966, weeklyOpeningHoursStrict, visitDuration,
		importance);
	opti.addElement(duisburg);
    }

    /**
     * Adds the Resources to the Optimization.
     *
     * @param opti the optimization instance
     */
    private static void addResources(IOptimization opti) {

	// Define the WorkingHours
	List<IWorkingHours> workingHours = new ArrayList<>();
	workingHours
		.add(new WorkingHours(ZonedDateTime.of(2030, MAY.getValue(), 8, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
			ZonedDateTime.of(2030, MAY.getValue(), 8, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

	workingHours
		.add(new WorkingHours(ZonedDateTime.of(2030, MAY.getValue(), 9, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
			ZonedDateTime.of(2030, MAY.getValue(), 9, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

	Duration maxWorkingTime = Duration.ofHours(9);
	Quantity<Length> maxDistanceKmW = Quantities.getQuantity(1200.0, KILO(METRE));

	// Define the Resource
	IResource jack = new CapacityResource("Jack from Aachen", 50.775346, 6.083887, maxWorkingTime, maxDistanceKmW,
		workingHours);
	opti.addElement(jack);
    }

    /**
     * Attach to different Events (Observables) of the optimization instance.
     *
     * @param opti the optimization instance
     */
    private static void attachToObservables(IOptimization opti) {

	opti.getOptimizationEvents().progressSubject().subscribe(p -> System.out.println(p.getProgressString()));

	opti.getOptimizationEvents().warningSubject().subscribe(w -> System.out.println(w.toString()));

	opti.getOptimizationEvents().statusSubject().subscribe(s -> System.out.println(s.toString()));

	opti.getOptimizationEvents().errorSubject().subscribe(e -> System.out.println(e.toString()));
    }

    /**
     * Export to kml.
     *
     * @param kmlFile the kml file
     * @param result  the result
     * @throws FileNotFoundException the file not found exception
     */
    static void exportToKml(String kmlFile, IOptimizationResult result) throws FileNotFoundException {

	IEntityExporter exporter = new EntityKMLExporter();
	exporter.export(result.getContainer(), new FileOutputStream(kmlFile));

    }
}
