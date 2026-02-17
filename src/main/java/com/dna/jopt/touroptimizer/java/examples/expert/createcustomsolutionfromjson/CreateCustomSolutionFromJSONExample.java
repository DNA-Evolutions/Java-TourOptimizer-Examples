package com.dna.jopt.touroptimizer.java.examples.expert.createcustomsolutionfromjson;

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
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import com.dna.jopt.config.convert.ConvertException;
import com.dna.jopt.config.serialize.SerializationException;
import com.dna.jopt.framework.body.IOptimization;
import com.dna.jopt.framework.body.Optimization;
import com.dna.jopt.framework.exception.caught.InvalidLicenceException;
import com.dna.jopt.framework.outcomewrapper.IOptimizationProgress;
import com.dna.jopt.framework.outcomewrapper.IOptimizationResult;
import com.dna.jopt.io.importing.IOptimizationImporter;
import com.dna.jopt.io.importing.json.OptimizationJSONImporter;
import com.dna.jopt.member.bucket.entity.IEntity;
import com.dna.jopt.member.unit.node.INode;
import com.dna.jopt.member.unit.resource.IResource;
import com.dna.jopt.touroptimizer.java.examples.ExampleLicenseHelper;

/**
 * The Class CreateCustomSolutionFromJSONExample.
 */
public class CreateCustomSolutionFromJSONExample extends Optimization {

    public static void main(String[] args) throws InterruptedException, ExecutionException, InvalidLicenceException,
	    ConvertException, SerializationException, IOException {
	new CreateCustomSolutionFromJSONExample().example();
    }

    public String toString() {
	return "Loading elements from a JSON file.";
    }

    public void example() throws InterruptedException, ExecutionException, InvalidLicenceException, ConvertException,
	    SerializationException, IOException {

	// Set the license via helper
	ExampleLicenseHelper.setLicense(this);

	// Set the file to load
	boolean doLoadNodeConnector = true;
	boolean doLoadProperties = true;

	String jsonFile = "myopti.json.bz2";

	// Prepare the optimization by loading the Properties and NodeConnector and
	// extract the solution
	IEntity en = CreateCustomSolutionFromJSONExample.loadEntityFromJson(new FileInputStream(jsonFile), this,
		doLoadNodeConnector, doLoadProperties);

	// Load elements

	// YOUR TODO HERE THE CUSTOM SOLUTION CAN BE CREATED BY USING THE LOADED ELEMENTS
	List<INode> nodes = this.getNodes(en);
	List<IResource> ress = this.getResources(en);
	

	// For now we simply add the elements - But you could create your own custom solution
	this.addNodes(nodes);
	this.addResources(ress);

	this.startRunAsync().get();
    }

    private List<IResource> getResources(IEntity en) {
	return en.getAllEntityElements().stream().filter(e -> e instanceof IResource).map(e -> (IResource) e).distinct()
		.collect(Collectors.toList());
    }

    private List<INode> getNodes(IEntity en) {
	return en.getAllEntityElements().stream().filter(e -> e instanceof INode).map(e -> (INode) e).distinct()
		.collect(Collectors.toList());
    }

    private static IEntity loadEntityFromJson(FileInputStream jsonFile, IOptimization opti, boolean doLoadNodeConnector,
	    boolean doLoadProperties) {

	IOptimization dummyOptimization = new Optimization();

	IOptimizationImporter importer = new OptimizationJSONImporter();

	importer.update(jsonFile, dummyOptimization);

	IEntity en = dummyOptimization.getWorkEntity();

	if (doLoadNodeConnector) {
	    opti.setNodeConnector(dummyOptimization.getNodeConnector());
	}

	Optional<Properties> propertiesOpt = dummyOptimization.getUserProperties();
	if (doLoadProperties && propertiesOpt.isPresent()) {
	    opti.addElement(propertiesOpt.get());
	}

	return en;
    }

    @Override
    public void onAsynchronousOptimizationResult(IOptimizationResult rapoptResult) {
	System.out.println(rapoptResult);
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
    public void onProgress(IOptimizationProgress rapoptProgress) {
	//
    }
}
