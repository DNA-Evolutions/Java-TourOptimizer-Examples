package com.dna.jopt.touroptimizer.java.examples.basic.io_03;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;

import com.dna.jopt.config.convert.ConvertException;
import com.dna.jopt.config.serialize.SerializationException;
import com.dna.jopt.framework.body.IOptimization;
import com.dna.jopt.framework.body.Optimization;
import com.dna.jopt.framework.exception.caught.InvalidLicenceException;
import com.dna.jopt.framework.outcomewrapper.IOptimizationProgress;
import com.dna.jopt.framework.outcomewrapper.IOptimizationResult;
import com.dna.jopt.io.importing.IOptimizationImporter;
import com.dna.jopt.io.importing.json.OptimizationJSONImporter;
import com.dna.jopt.touroptimizer.java.examples.ExampleLicenseHelper;

/**
 * This example shows how to load an optimization state from a JSON file.
 *
 * @author DNA
 * @version Mar 26, 2021
 * @since Mar 26, 2021
 */
public class LoadOptimizationFromJsonString extends Optimization {

  
  public static final String JSON_SNAPSHOT = "{\r\n"
  	+ "  \"optimizationStatus\" : {\r\n"
  	+ "    \"statusDescription\" : \"SUCCESS_WITH_SOLUTION\",\r\n"
  	+ "    \"error\" : \"NO_ERROR\",\r\n"
  	+ "    \"status\" : \"SUCCESS_WITH_SOLUTION\"\r\n"
  	+ "  },\r\n"
  	+ "  \"createdTimeStamp\" : 1741095862041,\r\n"
  	+ "  \"creator\" : \"DEFAULT_CREATOR\",\r\n"
  	+ "  \"ident\" : \"JOpt-Run-1741095860516\",\r\n"
  	+ "  \"nodes\" : [ {\r\n"
  	+ "    \"id\" : \"Koeln\",\r\n"
  	+ "    \"type\" : {\r\n"
  	+ "      \"position\" : {\r\n"
  	+ "        \"latitude\" : 50.9333,\r\n"
  	+ "        \"longitude\" : 6.95,\r\n"
  	+ "        \"locationId\" : \"Koeln\"\r\n"
  	+ "      },\r\n"
  	+ "      \"typeName\" : \"Geo\"\r\n"
  	+ "    },\r\n"
  	+ "    \"openingHours\" : [ {\r\n"
  	+ "      \"begin\" : \"2030-03-06T07:00:00Z\",\r\n"
  	+ "      \"end\" : \"2030-03-06T16:00:00Z\",\r\n"
  	+ "      \"zoneId\" : \"Europe/Berlin\"\r\n"
  	+ "    } ],\r\n"
  	+ "    \"visitDuration\" : \"PT20M\",\r\n"
  	+ "    \"priority\" : 1\r\n"
  	+ "  }, {\r\n"
  	+ "    \"id\" : \"Aachen\",\r\n"
  	+ "    \"type\" : {\r\n"
  	+ "      \"position\" : {\r\n"
  	+ "        \"latitude\" : 50.775346,\r\n"
  	+ "        \"longitude\" : 6.083887,\r\n"
  	+ "        \"locationId\" : \"Aachen\"\r\n"
  	+ "      },\r\n"
  	+ "      \"typeName\" : \"Geo\"\r\n"
  	+ "    },\r\n"
  	+ "    \"openingHours\" : [ {\r\n"
  	+ "      \"begin\" : \"2030-03-06T07:00:00Z\",\r\n"
  	+ "      \"end\" : \"2030-03-06T16:00:00Z\",\r\n"
  	+ "      \"zoneId\" : \"Europe/Berlin\"\r\n"
  	+ "    } ],\r\n"
  	+ "    \"visitDuration\" : \"PT20M\",\r\n"
  	+ "    \"priority\" : 1\r\n"
  	+ "  } ],\r\n"
  	+ "  \"resources\" : [ {\r\n"
  	+ "    \"id\" : \"Jack\",\r\n"
  	+ "    \"type\" : {\r\n"
  	+ "      \"typeName\" : \"Capacity\"\r\n"
  	+ "    },\r\n"
  	+ "    \"position\" : {\r\n"
  	+ "      \"latitude\" : 50.775346,\r\n"
  	+ "      \"longitude\" : 6.083887,\r\n"
  	+ "      \"locationId\" : \"Jack\"\r\n"
  	+ "    },\r\n"
  	+ "    \"workingHours\" : [ {\r\n"
  	+ "      \"begin\" : \"2030-03-06T07:00:00Z\",\r\n"
  	+ "      \"end\" : \"2030-03-06T16:00:00Z\",\r\n"
  	+ "      \"zoneId\" : \"Europe/Berlin\",\r\n"
  	+ "      \"isAvailableForStay\" : false\r\n"
  	+ "    }, {\r\n"
  	+ "      \"begin\" : \"2030-03-07T07:00:00Z\",\r\n"
  	+ "      \"end\" : \"2030-03-07T19:00:00Z\",\r\n"
  	+ "      \"zoneId\" : \"Europe/Berlin\",\r\n"
  	+ "      \"isAvailableForStay\" : false\r\n"
  	+ "    } ],\r\n"
  	+ "    \"maxTime\" : \"PT8H\",\r\n"
  	+ "    \"maxDistance\" : \"1200.0 km\",\r\n"
  	+ "    \"co2emissionFactor\" : 0.377\r\n"
  	+ "  } ],\r\n"
  	+ "  \"nodeRelations\" : [ {\r\n"
  	+ "    \"masterNodeId\" : \"Koeln\",\r\n"
  	+ "    \"relatedNodeIds\" : [ \"Aachen\" ],\r\n"
  	+ "    \"type\" : {\r\n"
  	+ "      \"minTimeDeviation\" : \"PT-33H-20M\",\r\n"
  	+ "      \"maxTimeDeviation\" : \"PT10M\",\r\n"
  	+ "      \"timeComparisonJuncture\" : {\r\n"
  	+ "        \"isMasterNodeWorkingStart\" : true,\r\n"
  	+ "        \"isRelatedNodeWorkingStart\" : true\r\n"
  	+ "      },\r\n"
  	+ "      \"typeName\" : \"TimeWindow\"\r\n"
  	+ "    },\r\n"
  	+ "    \"relationMode\" : \"STRONG\"\r\n"
  	+ "  } ],\r\n"
  	+ "  \"optimizationOptions\" : {\r\n"
  	+ "    \"properties\" : {\r\n"
  	+ "      \"JOpt.Algorithm.GE.OnProgressOutPercentage\" : \"1.0\",\r\n"
  	+ "      \"JOpt.Algorithm.Operator.2Opt.Threshold\" : \"10\",\r\n"
  	+ "      \"JOpt.Algorithm.Operator.GE.doForceReselect\" : \"FALSE\",\r\n"
  	+ "      \"JOpt.Algorithm.Operator.GE.numReselects\" : \"3\",\r\n"
  	+ "      \"JOpt.Algorithm.Operator.RequireHoursOverlap\" : \"TRUE\",\r\n"
  	+ "      \"JOpt.Algorithm.Operator.SA.doForceReselect\" : \"FALSE\",\r\n"
  	+ "      \"JOpt.Algorithm.Operator.SA.numReselects\" : \"5\",\r\n"
  	+ "      \"JOpt.Algorithm.PreOptimization.SA.CoolingRate\" : \"0.999985\",\r\n"
  	+ "      \"JOpt.Algorithm.PreOptimization.SA.NumIterations\" : \"100000\",\r\n"
  	+ "      \"JOpt.Algorithm.PreOptimization.SA.NumRepetions\" : \"1\",\r\n"
  	+ "      \"JOpt.Algorithm.PreOptimization.SA.OnProgressOutPercentage\" : \"1.0\",\r\n"
  	+ "      \"JOpt.Algorithm.PreOptimization.SA.StartingTemperature\" : \"10.0\",\r\n"
  	+ "      \"JOpt.Algorithm.PreOptimization.SA.StartingTemperatureLoadedSnapshot\" : \"0.1\",\r\n"
  	+ "      \"JOpt.Algorithm.Relation.NestingLimit\" : \"100\",\r\n"
  	+ "      \"JOpt.Algorithm.tryUseCluster2Opt\" : \"FALSE\",\r\n"
  	+ "      \"JOpt.Algorithm.tryUseClusterInsertion\" : \"FALSE\",\r\n"
  	+ "      \"JOpt.Assisted\" : \"TRUE\",\r\n"
  	+ "      \"JOpt.AutoFilter\" : \"FALSE\",\r\n"
  	+ "      \"JOpt.AutoFilter.EarlyArrivalMargin\" : \"0\",\r\n"
  	+ "      \"JOpt.AutoFilter.LateArrivalMargin\" : \"0\",\r\n"
  	+ "      \"JOpt.AutoFilter.RouteDistanceExceedMargin\" : \"0\",\r\n"
  	+ "      \"JOpt.AutoFilter.WorkingHoursExceedMargin\" : \"0\",\r\n"
  	+ "      \"JOpt.AutoFilter.afterFilteringResult\" : \"FALSE\",\r\n"
  	+ "      \"JOpt.AutoFilter.beforeFilteringResult\" : \"FALSE\",\r\n"
  	+ "      \"JOpt.AutoFilter.canDisableNodeProtection\" : \"TRUE\",\r\n"
  	+ "      \"JOpt.AutoFilter.collectivePenalization.maxImportance\" : \"5\",\r\n"
  	+ "      \"JOpt.AutoFilter.tryAvoidSplittingJoinedClusters\" : \"FALSE\",\r\n"
  	+ "      \"JOpt.AutoFilter.tryGracefulNodeFiltering\" : \"TRUE\",\r\n"
  	+ "      \"JOpt.AutoFilter.usePostOptimizationAfterStrictFiltering\" : \"TRUE\",\r\n"
  	+ "      \"JOpt.AutoFilter.useStrictFilterOnLastExecution\" : \"TRUE\",\r\n"
  	+ "      \"JOpt.AutoForceFilter.EarlyArrivalMargin\" : \"0\",\r\n"
  	+ "      \"JOpt.AutoForceFilter.LateArrivalMargin\" : \"0\",\r\n"
  	+ "      \"JOpt.AutoForceFilter.RouteDistanceExceedMargin\" : \"0\",\r\n"
  	+ "      \"JOpt.AutoForceFilter.WorkingHoursExceedMargin\" : \"0\",\r\n"
  	+ "      \"JOpt.CO.ActiveTimeTrigger\" : \"FALSE\",\r\n"
  	+ "      \"JOpt.CO.AutoSyncJoptTime\" : \"FALSE\",\r\n"
  	+ "      \"JOpt.CapacityDegradation.TreatSameLocationsAsOne\" : \"FALSE\",\r\n"
  	+ "      \"JOpt.Clustering.AutoZoneCodeClusters\" : \"FALSE\",\r\n"
  	+ "      \"JOpt.Clustering.AutoZoneCodeClusters.isHard\" : \"FALSE\",\r\n"
  	+ "      \"JOpt.Clustering.JointVisitDuration.doUseDistanceDivider\" : \"FALSE\",\r\n"
  	+ "      \"JOpt.Clustering.MaxDistanceUtilization\" : \"200.0\",\r\n"
  	+ "      \"JOpt.Clustering.MaxDurationUtilization\" : \"80.0\",\r\n"
  	+ "      \"JOpt.Clustering.PenlalizeZoneCodeCrossing\" : \"FALSE\",\r\n"
  	+ "      \"JOpt.Clustering.PenlalizeZoneCodeCrossingMultiplier\" : \"1.0\",\r\n"
  	+ "      \"JOpt.Clustering.ReassignOrphanedNodes\" : \"TRUE\",\r\n"
  	+ "      \"JOpt.Clustering.ResourceDutyHoursSplitValue\" : \"10\",\r\n"
  	+ "      \"JOpt.Clustering.ResourceSplitValue\" : \"30\",\r\n"
  	+ "      \"JOpt.Clustering.ShowDebugOutput\" : \"FALSE\",\r\n"
  	+ "      \"JOpt.CostAssessment.suppressInactiveFinalizedRouteCosts\" : \"TRUE\",\r\n"
  	+ "      \"JOpt.CostAssessment.useAverageCost\" : \"FALSE\",\r\n"
  	+ "      \"JOpt.DebugMode.NodeChecking\" : \"TRUE\",\r\n"
  	+ "      \"JOpt.EntityValidation.Debug\" : \"FALSE\",\r\n"
  	+ "      \"JOpt.FuzzyDemand\" : \"0\",\r\n"
  	+ "      \"JOpt.IntermediateStage.ThrowResults\" : \"FALSE\",\r\n"
  	+ "      \"JOpt.JOptPopulationSize\" : \"100\",\r\n"
  	+ "      \"JOpt.JointVisitDuration.maxRadiusMeter\" : \"10.0\",\r\n"
  	+ "      \"JOpt.JointVisitDuration.routeTimeBonusFactor\" : \"0.0\",\r\n"
  	+ "      \"JOpt.KeepZeroDriveForEventNodes\" : \"FALSE\",\r\n"
  	+ "      \"JOpt.Lockdown.Blockpillar.endOffset\" : \"0\",\r\n"
  	+ "      \"JOpt.Lockdown.Blockpillar.startOffset\" : \"0\",\r\n"
  	+ "      \"JOpt.Lockdown.LockdownUnchangedRoutes\" : \"TRUE\",\r\n"
  	+ "      \"JOpt.MovingEventNodes\" : \"TRUE\",\r\n"
  	+ "      \"JOpt.Multicore\" : \"1\",\r\n"
  	+ "      \"JOpt.NumCPUCores\" : \"4\",\r\n"
  	+ "      \"JOpt.PerformanceMode\" : \"FALSE\",\r\n"
  	+ "      \"JOpt.PillarDropper.AllowRescheduleMaximalRouteDistanceExceeded\" : \"TRUE\",\r\n"
  	+ "      \"JOpt.PillarDropper.AllowRescheduleWorkingHoursExceeded\" : \"TRUE\",\r\n"
  	+ "      \"JOpt.PlausibilityGuard.PropertyChecker.Active\" : \"TRUE\",\r\n"
  	+ "      \"JOpt.PropertyChecker.ShowWarnings\" : \"TRUE\",\r\n"
  	+ "      \"JOpt.SelectiveAutoFilter\" : \"FALSE\",\r\n"
  	+ "      \"JOpt.SelectiveAutoFilter.AfterEndAnchor\" : \"FALSE\",\r\n"
  	+ "      \"JOpt.SelectiveAutoFilter.CapacityOverload\" : \"FALSE\",\r\n"
  	+ "      \"JOpt.SelectiveAutoFilter.DoubleBooking\" : \"FALSE\",\r\n"
  	+ "      \"JOpt.SelectiveAutoFilter.MaximalRouteDistanceExceeded\" : \"FALSE\",\r\n"
  	+ "      \"JOpt.SelectiveAutoFilter.NodeType\" : \"FALSE\",\r\n"
  	+ "      \"JOpt.SelectiveAutoFilter.RelationShip\" : \"FALSE\",\r\n"
  	+ "      \"JOpt.SelectiveAutoFilter.ResourceMismatch\" : \"FALSE\",\r\n"
  	+ "      \"JOpt.SelectiveAutoFilter.TimeWindow.Early\" : \"FALSE\",\r\n"
  	+ "      \"JOpt.SelectiveAutoFilter.TimeWindow.Late\" : \"FALSE\",\r\n"
  	+ "      \"JOpt.SelectiveAutoFilter.WorkingHoursExceeded\" : \"FALSE\",\r\n"
  	+ "      \"JOpt.UnlocatedIdleTime\" : \"FALSE\",\r\n"
  	+ "      \"JOpt.plausibility.doInputCheck\" : \"TRUE\",\r\n"
  	+ "      \"JOpt.plausibility.doInputCheck.doCapacityCheck\" : \"TRUE\",\r\n"
  	+ "      \"JOptExitCondition.JOptGenerationCount\" : \"2000\",\r\n"
  	+ "      \"JOptWeight.CO2Emission\" : \"0.0\",\r\n"
  	+ "      \"JOptWeight.Capacity\" : \"50.0\",\r\n"
  	+ "      \"JOptWeight.ColorCapacity\" : \"10.0\",\r\n"
  	+ "      \"JOptWeight.CompactingFactor\" : \"1.0\",\r\n"
  	+ "      \"JOptWeight.DistanceExceeded\" : \"10.0\",\r\n"
  	+ "      \"JOptWeight.DistanceVariationPattern\" : \"0.0\",\r\n"
  	+ "      \"JOptWeight.ExpertiseLevelPreference\" : \"1.0\",\r\n"
  	+ "      \"JOptWeight.NodeCompactingFactor\" : \"0.0\",\r\n"
  	+ "      \"JOptWeight.NodeImportance\" : \"1.0\",\r\n"
  	+ "      \"JOptWeight.NodeType\" : \"1.0\",\r\n"
  	+ "      \"JOptWeight.PreferableResource\" : \"1.0\",\r\n"
  	+ "      \"JOptWeight.Relationships\" : \"100.0\",\r\n"
  	+ "      \"JOptWeight.ResourceActive\" : \"0.0\",\r\n"
  	+ "      \"JOptWeight.ResourceLocationBias\" : \"0.0\",\r\n"
  	+ "      \"JOptWeight.ResourceRadiusDrivingDistance\" : \"1.0\",\r\n"
  	+ "      \"JOptWeight.ResourceRadiusDrivingTime\" : \"1.0\",\r\n"
  	+ "      \"JOptWeight.ResourceWorkingTimeBalance\" : \"0.0\",\r\n"
  	+ "      \"JOptWeight.RouteDistance\" : \"1.0\",\r\n"
  	+ "      \"JOptWeight.RouteTime\" : \"1.0\",\r\n"
  	+ "      \"JOptWeight.TimeExceeded\" : \"100.0\",\r\n"
  	+ "      \"JOptWeight.TimeWindow\" : \"1.0\",\r\n"
  	+ "      \"JOptWeight.TimeWindow.AfterEndPillarAnchor\" : \"1000.0\",\r\n"
  	+ "      \"JOptWeight.TimeWindow.FlexTimePenalizeFactor\" : \"0.0\",\r\n"
  	+ "      \"JOptWeight.TimeWindow.OfferedNodeCostMulitplier\" : \"2.0\",\r\n"
  	+ "      \"JOptWeight.TimeWindow.ServiceHours\" : \"10.0\",\r\n"
  	+ "      \"JOptWeight.TimeWindow.TimeWindowEarlyFactor\" : \"10.0\",\r\n"
  	+ "      \"JOptWeight.TimeWindow.TimeWindowEarlyFactorAtFirstNode\" : \"1.0\",\r\n"
  	+ "      \"JOptWeight.TimeWindow.TimeWindowLateFactor\" : \"100.0\",\r\n"
  	+ "      \"JOptWeight.TotalDistance\" : \"1.0\",\r\n"
  	+ "      \"JOptWeight.TotalTime\" : \"1.0\",\r\n"
  	+ "      \"JOptWeight.UnPreferableResource\" : \"1.0\",\r\n"
  	+ "      \"JOptWeight.UselessUnloadAllVisitation\" : \"1.0\",\r\n"
  	+ "      \"JOptWeight.WorkingTime\" : \"5.0\",\r\n"
  	+ "      \"JOptWeight.ZoneCode\" : \"10.0\"\r\n"
  	+ "    }\r\n"
  	+ "  },\r\n"
  	+ "  \"extension\" : { }\r\n"
  	+ "}";
  
  
  public static void main(String[] args) throws InterruptedException, ExecutionException, InvalidLicenceException,
	  IOException {
      new LoadOptimizationFromJsonString().example();
  }
  
  public String toString() {
      return "Loading the current optimization state from a JSON String. The JSON string is wrapped in a bzip stream to allow default processing."
      	+ " By default, BZIP is always in use.";
  }

  public void example() throws InterruptedException, ExecutionException, InvalidLicenceException, IOException {

      // Set the license via helper
      ExampleLicenseHelper.setLicense(this);

      this.invokeFromJson(JSON_SNAPSHOT, this);

      // Set the Properties
      this.setProperties();

      attachToObservables(this);

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

  private void invokeFromJson(String json, IOptimization opti) throws IOException {

      IOptimizationImporter importer = new OptimizationJSONImporter();
      importer.update(compressStringToBZip2Stream(json), opti);
  }
  
  
  
  public static InputStream compressStringToBZip2Stream(String input) throws IOException {
      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

      // Create a BZip2 output stream wrapping the ByteArrayOutputStream
      try (BZip2CompressorOutputStream bzOut = new BZip2CompressorOutputStream(byteArrayOutputStream)) {
          bzOut.write(input.getBytes());
          bzOut.finish();  // Ensure all data is written
      }

      // Create an InputStream from the compressed byte array
      ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());

      return byteArrayInputStream; // This is now a valid BZip2 stream
  }
  
  

  @Override
  public void onAsynchronousOptimizationResult(IOptimizationResult rapoptResult) {
      System.out.println(rapoptResult);
  }

  /*
   * 
   * 
   * 
   */

  /**
   * Attach to different Events (Observables) of the optimization instance.
   *
   * @param opti the optimization instance
   */
  private static void attachToObservables(IOptimization opti) {

      opti.getOptimizationEvents().progressSubject().subscribe(p -> {
	  System.out.println(p.getProgressString());
      });

      opti.getOptimizationEvents().warningSubject().subscribe(w -> {
	  System.out.println(w.toString());
      });

      opti.getOptimizationEvents().statusSubject().subscribe(s -> {
	  System.out.println(s.toString());
      });

      opti.getOptimizationEvents().errorSubject().subscribe(e -> {
	  System.out.println(e.toString());
      });
  }

}
