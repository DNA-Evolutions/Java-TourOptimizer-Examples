package com.dna.jopt.touroptimizer.java.examples.expert.openassessor.nodelevel.custom;
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
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import com.dna.jopt.assessment.costadjustment.EntityCostAdjuster;
import com.dna.jopt.assessment.costassessor.EvaluatedNodeDataHolder;
import com.dna.jopt.assessment.costassessor.IEntityCostAssessor;
import com.dna.jopt.assessment.costassessorrestriction.nodelevel.custom.AbstractCustomNodeLevelRestriction;
import com.dna.jopt.assessment.costassessorrestriction.restrictionresult.EntityRestrictionResult;
import com.dna.jopt.assessment.costassessorrestriction.restrictionresult.IEntityRestrictionResult;
import com.dna.jopt.framework.inputplausibility.properties.IPropertyProvider;
import com.dna.jopt.member.bucket.entity.IEntity;
import com.dna.jopt.member.bucket.route.ILogicEntityRoute;
import com.dna.jopt.member.unit.IOptimizationElement;
import com.dna.jopt.member.unit.node.INode;
import com.dna.jopt.member.unit.violation.IViolation;
import com.dna.jopt.member.unit.violation.ViolationFactory;

public class CustomNodeLetterMRestriction extends AbstractCustomNodeLevelRestriction {

  public CustomNodeLetterMRestriction(IPropertyProvider propertyProvider) {
    super(propertyProvider);
  }

  @Override
  public IEntityRestrictionResult invokeRestriction(
		  IEntity en,
      ILogicEntityRoute route,
      INode node,
      IOptimizationElement lastGeoLocationElement,
      IOptimizationElement prevElement,
      EvaluatedNodeDataHolder holder,
      IEntityCostAssessor ca,
      boolean resultRequested) {

    // This examples shows how a custom restriction on node level can be designed

    // In this example we would like that all nodes starting with "M" are visited before 11pm (CET)

    if (!node.getId().startsWith("M")) {
      // We don't care
      return new EntityRestrictionResult();
    }

    // Extract arrival time
    LocalDateTime date =
        LocalDateTime.ofInstant(
            Instant.ofEpochMilli(holder.getArrivalTime()), ZoneId.systemDefault());

    // Create a 12pm matching date object
    LocalDateTime dateMatch =
        LocalDateTime.of(date.getYear(), date.getMonth(), date.getDayOfMonth(), 11, 0, 0, 0);
    dateMatch.atZone(ZoneId.of("Europe/Berlin"));

    Duration delta = Duration.between(dateMatch, date);

    // We assign more cost the more the node diverges from the targeted arrival time
    double costLate = 0.0;
    if (delta.getSeconds() > 0) {
      // We are late => assign cost
      costLate = this.calculatePenaltyCost(delta, route);
    }

    // Tell route that it has late violations
    IViolation curViolation = null;
    if (costLate > 0) {
      route
          .getRouteCostAndViolationController()
          .setCostLate(route.getRouteCostAndViolationController().getCostLate() + costLate);

      route
          .getRouteCostAndViolationController()
          .setNumConstraintViolations(
              route.getRouteCostAndViolationController().getNumConstraintViolations() + 1);
      route
          .getRouteCostAndViolationController()
          .setNumViolationsNodeTimewindowLate(
              route.getRouteCostAndViolationController().getNumViolationsNodeTimewindowLate() + 1);

      // In case we request a result we are also going to attach Violations
      if (resultRequested && costLate > 0) {
        curViolation = ViolationFactory.VIOLATION_CONSTRAINTVIOLATION_TIMECONSTRAINT_LATE.copy();
        curViolation.setValue(" Late M Violation time [min]: " + delta.getSeconds() / 60.0, String.valueOf(delta.getSeconds() / 60.0));
      }
    }

    // Create the result
    EntityRestrictionResult myRestrictionResult = new EntityRestrictionResult();

    myRestrictionResult.addViolation(curViolation);
    route.getRouteCostAndViolationController().addCost(costLate);

    return myRestrictionResult;
  }

  private double calculatePenaltyCost(Duration delta, ILogicEntityRoute route) {

    // We connect this to the weight for late arrival
    double baseCost = 10*
        this.getPropertyProvider().getPropertyProviderHelper().getWeightLateArrival()
            * delta.getSeconds()/60.0;

    // We also use an exponential multiplier..more late is even more cost
    double expMultiplier = CustomNodeLetterMRestriction.exp(0.00009 * delta.getSeconds());

    double cost = baseCost * expMultiplier;

    // Now lets adjust the cost
    cost = this.getCostAdjuster().getAdjustedCost(route, cost, EntityCostAdjuster.TIME_UNIT_MIN);

    return cost;
  }

  private static double exp(double val) {
    final long tmp = (long) (1512775 * val + 1072632447);
    return Double.longBitsToDouble(tmp << 32);
  }

}
