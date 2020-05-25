package com.dna.jopt.touroptimizer.java.examples.expert.openassessor.nodelevel.customimportance;
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

public class CustomNodeImportanceOrderRestriction
    extends AbstractCustomNodeLevelRestriction {

  public CustomNodeImportanceOrderRestriction(IPropertyProvider propertyProvider) {
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

	  // Get importance
    int curNodeImportance = node.getImportance();
    
    // Initially assign with same importance
    int prevNodeImportance = node.getImportance();
    
    if(prevElement instanceof INode) {
    	prevNodeImportance = ((INode)prevElement).getImportance();
    }
    
    // Calc delta of importances - If delta is positive the more importance node
    // is visited later. Therefore we assign costs
    int deltaImportance = curNodeImportance-prevNodeImportance;

    // Calculate deviation to the end of the openingHours

    double costWrongOrderedImprotance = 0.0;
    if (deltaImportance > 0) {
      // We are late => assign cost
    	costWrongOrderedImprotance = 100*deltaImportance;
    	// maybe use:
    	//this.getPropertyProvider().getPropertyProviderHelper().getWeightLateArrival();
    }

    // Add cost
    if (costWrongOrderedImprotance > 0) {
      route
          .getRouteCostAndViolationController()
          .setCostLate(route.getRouteCostAndViolationController().getCostLate() + costWrongOrderedImprotance);

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

    route.getRouteCostAndViolationController().addCost(costWrongOrderedImprotance);

    return myRestrictionResult;
  }

}
