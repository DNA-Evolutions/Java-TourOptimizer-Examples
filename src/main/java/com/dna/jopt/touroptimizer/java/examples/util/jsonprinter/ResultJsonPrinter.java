package com.dna.jopt.touroptimizer.java.examples.util.jsonprinter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Optional;

import com.dna.jopt.config.convert.ConvertException;
import com.dna.jopt.config.convert.ExportTarget;
import com.dna.jopt.config.convert.OptimizationConfiguration;
import com.dna.jopt.config.json.framework.JSONOptimization;
import com.dna.jopt.config.json.types.JSONConfig;
import com.dna.jopt.config.json.types.OptimizationKeySetting;
import com.dna.jopt.config.serialize.ConfigSerialization;
import com.dna.jopt.config.serialize.SerializationException;
import com.dna.jopt.config.types.CoreConfig;
import com.dna.jopt.config.types.OptimizationConfig;
import com.dna.jopt.config.types.ext.CoreExtensionManifest;
import com.dna.jopt.framework.body.IOptimization;

public class ResultJsonPrinter {

    private ResultJsonPrinter() {
	// Nothing to do
    }

    public static void printResultAsJson(IOptimization opti, boolean keepNodeConnectionsInJson) throws IOException, ConvertException {
	// Serialize

	OptimizationConfig<CoreConfig> exportedConfig = OptimizationConfiguration.exportConfig(ExportTarget.of(opti),
		new CoreExtensionManifest());
	
	// Strip some info
	exportedConfig = exportedConfig.withCoreBuildOptions(Optional.empty())
		.withSolution(Optional.empty());
	
	
	 if(!keepNodeConnectionsInJson) {
	     exportedConfig = exportedConfig.withElementConnections(new ArrayList<>());
	 }

	// Without pretty directly call:
	// String serializedExportedConfig =
	// ConfigSerialization.serialize(exportedConfig);

	String serializedExportedConfig = prettySerialize(exportedConfig);

	System.out.println(serializedExportedConfig);

    }
    
    
    public static OptimizationConfig<JSONConfig> toJsonOptimization(IOptimization opti, String licenseKey, Duration timeOut, 
	    boolean keepSolutionIfExisting, boolean keepNodeConnectionsInJson) throws ConvertException, SerializationException, IOException {
	

	JSONConfig myExtension = JSONConfig.builder().keySetting(OptimizationKeySetting.of(licenseKey)).timeOut(timeOut)
		.build();
	
	return toJsonOptimization(opti, myExtension, keepSolutionIfExisting,keepNodeConnectionsInJson);
    }
    
    public static OptimizationConfig<JSONConfig> toJsonOptimization(IOptimization opti, JSONConfig myExtension, boolean keepSolutionIfExisting, boolean keepNodeConnectionsInJson)
	    throws ConvertException, SerializationException, IOException {
	
	 OptimizationConfig<JSONConfig> config = JSONOptimization.fromOptization(opti, Optional.of(myExtension));
	 
	 if(!keepSolutionIfExisting) {
	     config = config.withSolution(Optional.empty());
	 }
	 
	 if(!keepNodeConnectionsInJson) {
	     config = config.withElementConnections(new ArrayList<>());
	 }

	return config;

    }
    
    
    public static String toJsonOptimizationString(OptimizationConfig<JSONConfig>  jopti) throws IOException, ConvertException {
	return JSONOptimization.asJSON(jopti);
    }
    
    
    
    /*
     * Helper
     */

    public static String prettySerialize(OptimizationConfig<CoreConfig> exportedConfig) throws IOException {

	ByteArrayOutputStream outStream = new ByteArrayOutputStream();

	ConfigSerialization.objectMapper().writerWithDefaultPrettyPrinter().writeValue(outStream, exportedConfig);

	return new String(outStream.toByteArray(), StandardCharsets.UTF_8);
    }
    
    
    

}
