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

public class CustomNodePriorityBasedEarlyVisitRestriction
    extends AbstractCustomNodeLevelRestriction {

  public CustomNodePriorityBasedEarlyVisitRestriction(IPropertyProvider propertyProvider) {
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

    int curNodeImportante = node.getImportance();

    // Calculate deviation to the end of the openingHours
    long curNodeHoursStartMillis =
        node.getDutyHours().get(holder.getChoosenOpeningHoursIndex()).getTimeWindow().getHoursBeginInMillis();

    long deltaMillis = holder.getArrivalTime() - curNodeHoursStartMillis;

    double costLate = 0.0;
    if (deltaMillis > 0) {
      // We are late => assign cost
      costLate = this.calculatePenaltyCost(deltaMillis, curNodeImportante, route);
    }

    // Add cost
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
    }

    // Create the result
    EntityRestrictionResult myRestrictionResult = new EntityRestrictionResult();

    route.getRouteCostAndViolationController().addCost(costLate);

    return myRestrictionResult;
  }

  private double calculatePenaltyCost(
      long deltaMillis, int curNodeImportante, ILogicEntityRoute route) {

    // We connect this to the weight for late arrival
    double cost = curNodeImportante
            * this.getPropertyProvider().getPropertyProviderHelper().getWeightLateArrival()
            * deltaMillis
            / 1000.0
            / 60;

    // Now lets adjust the cost
    cost = this.getCostAdjuster().getAdjustedCost(route, cost, EntityCostAdjuster.TIME_UNIT_MIN);

    return cost;
  }

}
