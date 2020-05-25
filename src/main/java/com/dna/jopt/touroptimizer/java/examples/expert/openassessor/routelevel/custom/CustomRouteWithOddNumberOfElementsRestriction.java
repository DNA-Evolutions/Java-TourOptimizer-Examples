package com.dna.jopt.touroptimizer.java.examples.expert.openassessor.routelevel.custom;
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
import java.util.Optional;

import com.dna.jopt.assessment.costassessor.IEntityCostAssessor;
import com.dna.jopt.assessment.costassessorrestriction.restrictionresult.EntityRestrictionResult;
import com.dna.jopt.assessment.costassessorrestriction.restrictionresult.IEntityRestrictionResult;
import com.dna.jopt.assessment.costassessorrestriction.routelevel.custom.AbstractCustomRouteLevelRestriction;
import com.dna.jopt.framework.inputplausibility.properties.IPropertyProvider;
import com.dna.jopt.member.bucket.route.ILogicEntityRoute;
import com.dna.jopt.member.unit.violation.IViolation;
import com.dna.jopt.member.unit.violation.Violation;

public class CustomRouteWithOddNumberOfElementsRestriction
    extends AbstractCustomRouteLevelRestriction {

  public static final IViolation VIOLATION_NOT_ODD_NUMBER_OF_ELEMENTS =
      new Violation("CONSTRAINTVIOLATION", "ROUTE SIZE", "NOT ODD NUMBER OF ELEMENTS", 999);

  public CustomRouteWithOddNumberOfElementsRestriction(IPropertyProvider propertyProvider) {
    super(propertyProvider);
  }

  @Override
  public IEntityRestrictionResult invokeRestriction(
      Optional<ILogicEntityRoute> arg0,
      ILogicEntityRoute route,
      IEntityCostAssessor arg2,
      boolean resultRequested) {

    EntityRestrictionResult myRestrictionResult = new EntityRestrictionResult();

    // Count the number of elements in the route
    int numOptimizableRouteElements = route.getRouteOptimizableElements().size();
    double penaltyCost = 0.0;
    IViolation curViolation = null;

    if ((numOptimizableRouteElements % 2) == 0) {
      // even => Assign cost

      penaltyCost = 1000.0;

      route
          .getRouteCostAndViolationController()
          .setNumConstraintViolations(
              route.getRouteCostAndViolationController().getNumConstraintViolations() + 1);

      route
          .getRouteCostAndViolationController()
          .setCostInjectedRestriction(
              route.getRouteCostAndViolationController().getCostInjectedRestriction()
                  + penaltyCost);

      route.getRouteCostAndViolationController().addCost(penaltyCost);

    } else {
      // odd => ok => no cost
    }

    if (resultRequested && penaltyCost > 0) {
      curViolation =
          CustomRouteWithOddNumberOfElementsRestriction.VIOLATION_NOT_ODD_NUMBER_OF_ELEMENTS.copy();
      curViolation.setValue(
          " Not odd number of nodes restriction. Count: " + numOptimizableRouteElements,
          String.valueOf(numOptimizableRouteElements));
    }

    myRestrictionResult.addViolation(curViolation);

    return myRestrictionResult;
  }
}
